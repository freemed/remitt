#!/usr/bin/perl
#
#	$Id$
#	$Author$
#

package Remitt::Utilities;

use Remitt::Session;
use MIME::Base64;
use Data::Dumper;

sub Authenticate {
	my ($hash) = @_;
	my ($sessionid, $key) = split /:/, decode_base64($hash);
	
	# Get rid of trailing slash
	$key =~ s/\\$//;

	#print "[Utilities::Authenticate] sessionid = $sessionid, key = $key\n";

	# Check the session to see if we are authenticated or not
	#print "Authenticate: opening session $sessionid ... ";
	my $session = Remitt::Session->new($sessionid);
	my $load = $session->load();
	#print Dumper($session->{session});
	#print "[Utilities::Authenticate] key passed from client = '$key'\n";

	# Get passkey
	my $passkey = $session->{session}->param('passkey');
	#print "[Utilities::Authenticate] ".Dumper($session->{session})."\n";

	#print "[auth present]\n" if ($session->{session}->param('authenticated'));
	#print "[key matches]\n" if ($session->{session}->param('passkey') == $key);
	#print "\n\npass key = ".$passkey."\n";
	#print "key = ".$key."\n";
	#print "[stored key = ".$session->{session}->param('passkey').", my key = $key]\n";

	return +(
		$session->{session}->param('authenticated') and ($session->{session}->param('passkey') == $key),
		$session->{session}->param('username')
	);
} # end sub Authenticate

# Function: Fault
#
#	Produce "authentication failed" XML-RPC fault.
#	
sub Fault {
	die ("Authentication failed. User not logged in");
} # end sub Fault

# Function: ForceAuthentication
#
#	Force authentication check using basic authentication. Uses
#	basic authentication username and password to check against
#	session data, and returns an XML-RPC fault if authentication
#	fails.
#
sub ForceAuthentication {
	my (undef, $authstring) = split / /, $ENV{'HTTP_authorization'};
	my ($auth, $sessionid) = Remitt::Utilities::Authenticate($authstring);
	Remitt::Utilities::Fault() if (!$auth);
	return 1;
} # end sub ForceAuthentication

# Function: ResolveTranslationPlugin
#
#	The "Translation" layer of plugins for Remitt is a hidden layer,
#	which means that the user only has to specify a "Render" layer
#	and "Transport" layer plugin. This function resolves the
#	plugin that is to be used for the "Translation" layer.
#
# Parameters:
#
#	$render - Rendering plugin
#
#	$renderoption - Rendering plugin option. If this is declared as
#	'', no render option is needed.
#
#	$transport - Transport plugin
#
# Returns:
#
#	Name of the Translation plugin to be used.
#
sub ResolveTranslationPlugin {
	# Get parameters
	my ($render, $renderoption, $transport) = @_;

	my $path = '/root/FMSF/remitt';

	# Sanitize parameters
	$render =~ s/\W//g;
	$renderoption =~ s/\W//g;
	$transport =~ s/\W//g;

	# Cache the options for render and transport plugins
	my $config = { };
	eval 'use Remitt::Plugin::Render::'.$render.';';
	eval '$config{"Render"} = Remitt::Plugin::Render::'.$render.'::Config();';
	eval 'use Remitt::Plugin::Transport::'.$transport.';';
	eval '$config{"Transport"} = Remitt::Plugin::Transport::'.$transport.'::Config();';

	my $input;
	# Handle variable output formats with render plugin
	if ($config{'Render'}->{'OutputFormat'} eq 'variable') {
		$input = $config{'Render'}->{'Options'}->{$renderoption}->{'OutputFormat'};
	} else {
		$input = $config{'Render'}->{'OutputFormat'};
	}
	my $output = $config{'Transport'}->{'InputFormat'};

	# Cache the translation layer options
	my @plugins;
	opendir DH, $path.'/lib/Remitt/Plugin/Translation/' or die('No translation plugins found.');
	foreach my $plugin (readdir DH) {
		if ($plugin =~ /\.pm$/) {
			my $pluginoptions;
			$plugin =~ s/\.pm$//;

			# Read information from plugin
			eval 'use Remitt::Plugin::Translation::'.$plugin.';';
			eval '$config{"Transport"} = Remitt::Plugin::Translation::'.$plugin.'::Config();';
			if (($config{'Transport'}->{'InputFormat'} eq $input) and ($config{'Transport'}->{'OutputFormat'} eq $output)) {
				return $plugin;
			}
		}
	}
	# If nothing is found, return null
	return '';
} # end sub ResolveTranslationPlugin

1;
