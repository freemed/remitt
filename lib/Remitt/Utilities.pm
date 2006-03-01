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

use FindBin;
use lib "$FindBin::Bin/../lib";

use Remitt::Session;
use Remitt::DataStore::Log;
use Remitt::DataStore::Output;
use Remitt::DataStore::Processor;
use Compress::Zlib;
use MIME::Base64;
use Config::IniFiles;
use File::Path;
use Sys::Syslog;
use Data::Dumper;
use Thread;
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
		$sessionid,
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
	my $c;
	my @p = ( '/etc/remitt', '/usr/share/remitt', '/usr/lib/remitt', '.', '..' );
	foreach my $f (@p) { if ( -r $f.'/remitt.conf' and -s $f.'/remitt.conf' ) { $c = $f.'/remitt.conf'; } }
	if (!$c) { die("REMITT could not find a remitt.conf file.\n"); }
	return new Config::IniFiles( -file => $c );
} # end sub Configuration

# Function: Remitt::Utilities::Fault
#
#	Produce "authentication failed" XML-RPC fault.
#	
sub Fault {
	die ("Authentication failed. User not logged in");
} # end sub Fault

# Function: Remitt::Utilities::GetUsername
#
# 	Derive username of current user from HTTP headers.
#
# Returns:
#
# 	String containing username.
#
sub GetUsername {
	my (undef, $authstring) = split / /, $ENV{'HTTP_authorization'};
	my ($auth, $sessionid, $pass) = Remitt::Utilities::Authenticate($authstring);
	my $session = Remitt::Session->new($sessionid);
	$session->load();
	return $session->{session}->param('username');
} # end sub GetUsername        

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

# Function: Remitt::Utilities::ExecuteThread
#
# 	Thread function which carries out the actual execution of an
# 	Execute request. This is usually called by
# 	<Remitt::Utilities::ProcessorThread>.
#
# Parameters:
#
# 	$username - REMITT username
#
# 	$input - Input XML
#
# 	$render - Render plugin
#
# 	$renderoption - Render plugin option
#
# 	$translation - Translation plugin
#
# 	$transport - Transport plugin
# 	
# 	$unique - Unique id
#
sub ExecuteThread {
	my ( $username, $input, $render, $renderoption, $translation, $transport, $unique ) = @_;

	my $log = Remitt::DataStore::Log->new;

	$log->Log($username, 3, 'Remitt.Utilities.ExecuteThread', 'Started execute thread for '.$username.' ('.$unique.')');

	my $ds = Remitt::DataStore::Output->new($username);

	#----- Child branch
	#print "D-Child: running execute code\n";
	my ( $x, $y, $results );
	eval 'use Remitt::Plugin::Render::'.$render.';';
	eval 'use Remitt::Plugin::Translation::'.$translation.';';
	eval 'use Remitt::Plugin::Transport::'.$transport.';';
	$log->Log($username, 3, 'Remitt.Utilities.ExecuteThread', $unique.' :: render');
	eval '$x = Remitt::Plugin::Render::'.$render.'::Render($input, $renderoption);';
	$log->Log($username, 3, 'Remitt.Utilities.ExecuteThread', $unique.' :: translation');
	eval '$y = Remitt::Plugin::Translation::'.$translation.'::Translate($x);';
	$log->Log($username, 3, 'Remitt.Utilities.ExecuteThread', $unique.' :: transport');
	eval '$results = Remitt::Plugin::Transport::'.$transport.'::Transport($y, $username);';
	#eval '$results = Remitt::Plugin::Transport::'.$transport.'::Transport(Remitt::Plugin::Translation::'.$translation.'::Translate(Remitt::Plugin::Render::'.$render.'::Render($input, $renderoption)), $username);';
	# Store value in proper place in 'state' directory
	$log->Log($username, 3, 'Remitt.Utilities.ExecuteThread', 'child thread: storing state after successful run');
	$ds->SetStatus($unique, 1, $results);
		
	# Terminate child thread
	#print "ExecuteThread end\n";
} # end method ExecuteThread

# Function: Remitt::Utilities::ProcessorThread
#
#	Main processor thread. This function runs in the background,
#	and checks the processor queue for jobs, which it then spawns
#	execute threads for.
#
# Parameters:
#
# 	$poll - (optional) Number of seconds during idle between polling
# 	for new data in the queue. Defaults to 5 seconds.
#
sub ProcessorThread {
	# Set polling interval
	my $poll = shift || 5;

	my $log = Remitt::DataStore::Log->new;

	$log->Log('SYSTEM', 2, 'Remitt.Utilities.ProcessorThread', 'Started processor thread with polling interval of '.$poll.'s');

	my $p = Remitt::DataStore::Processor->new;

	# Loop endlessly
	while (1) {
		# Check for new entries
		my @items = $p->GetQueue();
		if (defined($items[0]) {
			foreach my $item (@items) {
				# Remove from the queue and start threads to
				# handle. Need to optimize at some point.
				$p->RemoveFromQueue($item->{rowid});
				my $thread = new Thread \&ExecuteThread,
					$item->{username},
					Compress::Zlib::memGunzip(decode_base64($item->{data})),
					$item->{render},
					$item->{renderoption},
					$item->{translation},
					$item->{transport},
					$item->{unique_id};
			} # end foreach item loop
		} else {
			# If there is nothing, Wait $poll seconds between polls
			sleep $poll;
		} # end if defined
	} # "end" of endless while loop
} # end method ProcessorThread

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
	my ($input, $transport, $extension, $username) = @_;

        if (!defined($username)) {
                return $input;
        } else {
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
