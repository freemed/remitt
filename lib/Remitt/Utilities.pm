#!/usr/bin/perl
#
#	$Id$
#	$Author$
#
# Package: Remitt::Utilities
#
#	Contains general purpose utility functions necessary for the
#	low-level operation of Remitt.
#

package Remitt::Utilities;

use Remitt::Session;
use MIME::Base64;
use Config::IniFiles;
use File::Path;
use Data::Dumper;
use POSIX;

# Method: Remitt::Utilities::Authenticate
#
# 	Perform authentication for XML-RPC server basic authentication.
#
# Returns:
#
# 	Array containing ( boolean authenticated, string username )
#
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

# Function: Remitt::Utilities::Configuration
#
#	Get the global configuration object of type <Config::IniFiles>.
#	Currently this is not cached.
#
# Returns:
#
#	Configuration object.
#
sub Configuration {
	my $file;
	my $my_config_file;
	my @config_files = (
		'/etc/remitt/remitt.conf',
		'/usr/share/remitt/remitt.conf',
		'/usr/lib/remitt/remitt.conf',
		'./remitt.conf',
		'../remitt.conf'
	);
	foreach $file (@config_files) {
		$my_config_file = $file if -r $file;
	}
	die("Could not load any valid configuration file.\n") if (!$my_config_file);
	return new Config::IniFiles( -file => $my_config_file );
} # end sub Configuration

# Function: Remitt::Utilities::Fault
#
#	Produce "authentication failed" XML-RPC fault.
#	
sub Fault {
	die ("Authentication failed. User not logged in");
} # end sub Fault

# Function: Remitt::Utilities::ForceAuthentication
#
#	Force authentication check using basic authentication. Uses
#	basic authentication username and password to check against
#	session data, and returns an XML-RPC fault if authentication
#	fails.
#
sub ForceAuthentication {
	# Check for XMLRPC/basic authentication
	if (!defined $main::auth) { return 1; }
	my (undef, $authstring) = split / /, $ENV{'HTTP_authorization'};
	my ($auth, $sessionid) = Remitt::Utilities::Authenticate($authstring);
	Remitt::Utilities::Fault() if (!$auth);
	return 1;
} # end sub ForceAuthentication

# Function: Remitt::Utilities::ResolveTranslationPlugin
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

	# Get path from the configuration
	my $config = Remitt::Utilities::Configuration();
	my $path = $config->val('installation', 'path');

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
			if ($output =~ /ARRAY\(/ ) {
				# Loop through array of possibles
				foreach my $o (@{$output}) {
					my $output = $o;
					if (($config{'Transport'}->{'InputFormat'} eq $input) and ($config{'Transport'}->{'OutputFormat'} eq $output)) {
						return $plugin;
					}
				}
			} else {
				# No array of possible formats
				if (($config{'Transport'}->{'InputFormat'} eq $input) and ($config{'Transport'}->{'OutputFormat'} eq $output)) {
					return $plugin;
				}
			}
		}
	}
	# If nothing is found, return null
	return '';
} # end sub ResolveTranslationPlugin

# Method: Remitt::Utilities::StoreContents
#
# 	Store output from a transport plugin, and return the data. This
# 	is mostly necessary because the command-line interface does not
# 	want the data stored on the server, but rather returned
# 	immediately.
#
# Parameters:
#
# 	$input - Input data from the output of the transport layer plugin.
#
# 	$transport - Name of the transport plugin that is being used.
#
# 	$extension - Filename extension to use.
#
# Return:
#
# 	Data to return to calling subroutine.
#
sub StoreContents {
	my ($input, $transport, $extension) = @_;

        if (!defined $main::auth) {
                return $input;
        } else {
		my (undef, $authstring) = split / /, $ENV{'HTTP_authorization'};
		my ($auth, $sessionid, $pass) = Remitt::Utilities::Authenticate($authstring);
		return Remitt::Utilities::Fault() if (!$auth);

		# Get username information
		my $session = Remitt::Session->new($sessionid);
		$session->load();
		#print Dumper($session->{session});
		my $username = $session->{session}->{_OPTIONS}[0];
		#print "username = $username \n";

		my $filename = strftime('%Y%m%d.%H%M%S', localtime(time)).
			'.' . $transport . '.' . $extension;

		Remitt::Utilities::StoreFile($username, $filename, 'output', $input);	
		#print "returning $filename to calling application\n";
		return $filename;
	}
} # end sub StoreContents

# Method: Remitt::Utilities::StoreFile
#
# 	Store actual file in the REMITT spool for a particular user. This
# 	is more or less an internal function, and is called by
# 	<Remitt::Utilities::StoreContents>.
#
# Parameters:
#
# 	$user - User name of user logged into REMITT server.
#
# 	$name - File name.
#
# 	$category - Type of file.
#
# 	$contents - String containing file contents.
#
# Returns:
#
# 	True if successful.
#
# SeeAlso:
# 	<Remitt::Utilities::StoreContents>
#
sub StoreFile {
	my $user = shift;
	my $name = shift;
	my $category = shift;
	my $contents = shift;
	my $config = Remitt::Utilities::Configuration ( );
	#print "StoreFile ( $user, $name, $category, ".length($contents)." ) called\n";

	# Form path
	my $path = $config->val('installation', 'path').'/spool/'.$user.'/'.$category.'/';
	umask 000;
	mkpath($path, 1, 0755);
	
	# Write contents to file
	open FILE, '>' . $path . '/' . $name or die("Could not write to $path/$file\n");
	print FILE $contents;
	close FILE;

	return 1;
} # end sub StoreFile

1;
