#!/usr/bin/perl -I./lib ../lib
#
#	$Id$
#	$Author$
#

package Remitt::Interface;

use Remitt::Session;
use Remitt::Utilities;
use Digest::MD5;
use Data::Dumper;

# Method: Remitt.Interface.Execute
#
#	Execute Render/Translation/Transport series.
#
# Parameters:
#
#	$input - Input document
#
#	$render - Name of rendering plugin
#
#	$renderoption - Name of rendering plugin option
#
#	$transport - Name of transport plugin
#
# Returns:
#
#	Output from transport plugin.
#
# Example:
#
#	Remitt.Interface.Execute($xmlfile, 'XSLT', '837p', 'X12Text');
#
sub Execute {
	shift if UNIVERSAL::isa($_[0] => __PACKAGE__);
	Remitt::Utilities::ForceAuthentication();

	my $input = shift || die("Input not given");
	my $render = shift || die("Render not given");
	my $renderoption = shift;
	# FIXME: Check to see if we have to have an option for error-checking
	my $transport = shift || die("Transport not given");

	# Sanititze (not input!)
	$render =~ s/\W//g;
	$renderoption =~ s/\W//g;
	$transport =~ s/\W//g;

	# Get resolve for translation plugin
	my $translation = Remitt::Utilities::ResolveTranslationPlugin (
		$render, $renderoption, $transport );
	die("No translation plugin found") if ($translation eq '');

	# Execute series
	eval 'use Remitt::Plugin::Render::'.$render.';';
	eval 'use Remitt::Plugin::Translation::'.$translation.';';
	eval 'use Remitt::Plugin::Transport::'.$transport.';';
	eval 'return Remitt::Plugin::Transport::'.$transport.'::Transport(Remitt::Plugin::Translation::'.$translation.'::Translate(Remitt::Plugin::Render::'.$render.'::Render($input, $renderoption)));';
} # end sub Execute

# Method: Remitt.Interface.ListOptions
#
#	Get list of options for a particular plugin.
#
# Parameters:
#
#	$type - Plugin type (Render, Translation, Transport, etc)
#
#	$plugin - Plugin name
#
# Returns:
#
#	Struct of arrays containing options and information, or empty
#	structure if there are no options.
#
sub ListOptions {
	shift if UNIVERSAL::isa($_[0] => __PACKAGE__);
	Remitt::Utilities::ForceAuthentication();

	# Get pararmeters
	my $type = shift;
	my $plugin = shift;

	# Sanitize parameters
	$type =~ s/\W//g;
	$plugin =~ s/\W//g;

	my $config = { };	
	eval 'use Remitt::Plugin::'.$type.'::'.$plugin.';';
	eval '$config = Remitt::Plugin::'.$type.'::'.$plugin.'::Config();';
	return $config->{'Options'};
} # end sub ListOptions

# Method: Remitt.Interface.ListPlugins
#
#	Get list of plugins for a particular type
#
# Parameters:
#
#	$type - Plugin type (Render, Translation, Transport, etc)
#
# Returns:
#
#	Array of plugins for the specified type.
#
sub ListPlugins {
	shift if UNIVERSAL::isa($_[0] => __PACKAGE__);
	Remitt::Utilities::ForceAuthentication();

	# Get pararmeters
	my $type = shift;

	# Sanitize parameters
	$type =~ s/\W//g;

	my $path = '/root/FMSF/remitt';
	my @plugins;
	opendir DH, $path.'/lib/Remitt/Plugin/'.$type.'/' or die('Invalid plugin type.');
	foreach my $plugin (readdir DH) {
		if ($plugin =~ /\.pm$/) {
			$plugin =~ s/\.pm$//;
			push @plugins, $plugin;
		}
	}
	return \@plugins;
} # end sub ListPlugins

# Method: Remitt.Interface.SystemLogin
#
#	Start a Remitt session. This function requires no authentication.
#
# Parameters:
#
#	$username - Username for this account
#
#	$password - Password for this account
#
# Returns:
#
#	Struct containing
#	* sessionid - Id of session, used as username in basic authentication
#	* key - Secret key, used as password in basic authentication
#
sub SystemLogin {
	shift if UNIVERSAL::isa($_[0] => __PACKAGE__);
	my ($username, $password) = @_;

	# TODO: Do we authenticate username/password pair against access list?

	# Create new session
	my ($sessionid, $key);

	# Generate unique session and key based on timestamp
	$key = Digest::MD5::md5_hex(gmtime() . $password);

	my $session = Remitt::Session->new($sessionid);
	$session->create(+{
		'username' => $username,
		'passkey'  => $key
	});

	$sessionid = $session->{session}->id();
	#print "SystemLogin: username = $username\n";
	#print "SystemLogin: session id = $sessionid\n";
	#print "SystemLogin: secret key = $key\n";
	#print "SystemLogin: key stored as ".$session->{session}->param('passkey')."\n";
	#print "----------------------------\n";

	$session->{session}->close();
	return +{
		'sessionid' => $sessionid,
		'key' => $key
	};
} # end sub SystemLogin

# Method: Remitt.Interface.SystemLogout
#
#	End a Remitt "session". This should only be used when transient
#	data does not need to be kept on the Remitt server. This function
#	requires authentication.
#
sub SystemLogout {
	shift if UNIVERSAL::isa($_[0] => __PACKAGE__);
	my (undef, $authstring) = split / /, $ENV{'HTTP_authorization'};
	my ($auth, $sessionid, $pass) = Remitt::Utilities::Authenticate($authstring);
	return Remitt::Utilities::Fault() if (!$auth);

	my $session = Remitt::Session->new($sessionid);
	$session->load();
	$session->{session}->delete();
	return 1;
} # end sub SystemLogout

# Method: Remitt.Interface.ProtocolVersion
#
#	Get the current version of the Remitt protocol being used.
#
# Returns:
#
#	Version of the Remitt protocol.
#
sub ProtocolVersion {
	shift if UNIVERSAL::isa($_[0] => __PACKAGE__);
	return 1.0;
} # end sub ProtocolVersion

1;
