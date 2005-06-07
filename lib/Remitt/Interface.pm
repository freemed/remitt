#!/usr/bin/perl -I./lib ../lib
#
#	$Id$
#	$Author$
#
# Package: Remitt::Interface
#
#	Contains all methods exported through the XML-RPC interface
#	for Remitt. It is possible that these methods could also be
#	exported using SOAP (since the SOAP::Lite library is being
#	used to provide XML-RPC functionality).
#
#	Functions that require authentication use the
#	<Remitt::Utilities::ForceAuthentication> method from the
#	<Remitt::Utilities> package. This uses basic authentication.
#

package Remitt::Interface;

use Remitt::Session;
use Remitt::Utilities;
use Remitt::DataStore::Output;
use Digest::MD5;
use Sys::Syslog;
use POSIX;

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
#	Output from transport plugin if used from the command line interface
#	(CLI), or a unique identifier used to get the actual output from
#	<Remitt.Interface.GetStatus>.
#
# Example:
#
#	$x = Remitt.Interface.Execute($xmlfile, 'XSLT', '837p', 'Text');
#
sub Execute {
	shift if UNIVERSAL::isa($_[0] => __PACKAGE__);
	Remitt::Utilities::ForceAuthentication();

	my $input = shift || die("Input not given");
	my $render = shift || die("Render not given");
	my $renderoption = shift;

	# FIXME: Check to see if we have to have an option for error-checking
	my $transport = shift || die("Transport not given");

	# Fix old FreeB mappings
	if ($renderoption eq "hcfa") { $renderoption = "hcfa1500"; }
	if ($renderoption eq "x12") { $renderoption = "837p"; }
	if ($transport eq "txt") { $transport = "Text"; }
	if ($transport eq "pdf") { $transport = "PDF"; }
	if ($transport eq "mcsi") { $transport = "MCSI"; }

	syslog('info', 'Remitt.Interface.Execute called with %s, %s, %s, data length = %d', $render, $renderoption, $transport, length($input));
	#print "Running Execute ( length of ".length($input).", $render, $renderoption, $transport ) ... \n";

	# Sanitize (not input!)
	$render =~ s/\W//g;
	$renderoption =~ s/\W//g;
	$transport =~ s/\W//g;

	# Get username information
	my (undef, $authstring) = split / /, $ENV{'HTTP_authorization'};
	my ($auth, $sessionid, $pass) = Remitt::Utilities::Authenticate($authstring);
	my $session = Remitt::Session->new($sessionid);
	$session->load();
	my $username = $session->{session}->param('username');

	# Get resolve for translation plugin
	my $translation = Remitt::Utilities::ResolveTranslationPlugin (
		$render, $renderoption, $transport );
	syslog('info', 'Remitt.Utilities.ResolveTranslation ( '.$render.', '.$renderoption.', '.$transport.' ) = '.$translation);
	die("No translation plugin found") if ($translation eq '');

	# Deal with CLI seperately
	if (!defined $main::auth) {
		my ( $x, $y, $z );
		eval 'use Remitt::Plugin::Render::'.$render.';';
		eval 'use Remitt::Plugin::Translation::'.$translation.';';
		eval 'use Remitt::Plugin::Transport::'.$transport.';';
		eval '$x = Remitt::Plugin::Render::'.$render.'::Render($input, $renderoption);';
		eval '$y = Remitt::Plugin::Translation::'.$translation.'::Translate($x);';
		eval '$z = Remitt::Plugin::Transport::'.$transport.'::Transport($y);';
		return $z;
		#eval 'return Remitt::Plugin::Transport::'.$transport.'::Transport(Remitt::Plugin::Translation::'.$translation.'::Translate(Remitt::Plugin::Render::'.$render.'::Render($input, $renderoption)));';
		die ( "Should never get here\n" );
	}

	# Use the data store to create a unique id
	#print "Calling Remitt::DataStore::Create ( $renderoption, $transport, XML )\n";
	my $ds = Remitt::DataStore::Output->new($username);
	my $unique = $ds->Create( $renderoption, $transport, $input );

	# Here, we fork a new process, so that we can return a value in realtime.
	my $results;
	my $child;
	if (!defined($child = fork())) {
		die "Cannot fork process: $!\n";
	} elsif ($child == 0) {
		#----- Child branch
		print "D-Child: running execute code\n";
		my ( $x, $y, $results );
		eval 'use Remitt::Plugin::Render::'.$render.';';
		eval 'use Remitt::Plugin::Translation::'.$translation.';';
		eval 'use Remitt::Plugin::Transport::'.$transport.';';
		eval '$x = Remitt::Plugin::Render::'.$render.'::Render($input, $renderoption);';
		eval '$y = Remitt::Plugin::Translation::'.$translation.'::Translate($x);';
		eval '$results = Remitt::Plugin::Transport::'.$transport.'::Transport($y);';
		#eval '$results = Remitt::Plugin::Transport::'.$transport.'::Transport(Remitt::Plugin::Translation::'.$translation.'::Translate(Remitt::Plugin::Render::'.$render.'::Render($input, $renderoption)));';

		# Store value in proper place in 'state' directory
		if (defined($main::auth)) {
			print "D-Child: storing state after successful run\n";
			$ds->SetStatus($unique, 1, $results);
		}
		
		# Terminate child process
		exit;
	} else {
		#----- Parent branch
		#
		# We would use waitpid($child, 0); to actually wait for the fork,
		# which we don't want to do unless we're actually using the
		# CLI, which needs the output immediately.
		
		# Deal with actual child process
		#
		# Proper behavior with server:
		# return unique identifier and no waiting ...
			
		# Reaper code for child process
		use POSIX ":sys_wait_h";
		$SIG{CHLD} = 'IGNORE';
	
		# Return actual value
		print "D-Parent: returning $unique to calling program\n";

		# Prefix this with a non-numeric character to prevent autotyping
		return 'Z'.$unique;
	} # end forking
} # end sub Execute

# Method: Remitt.Interface.FileList
#
#	Get list of available files.
#
# Parameters:
# 
# 	$category - Category of file under the current user.
#
# 	$criteria - Criteria to narrow by (year, etc)
#
# 	$value - Value to narrow by
#
# Returns:
#
# 	Array of files found for the current user and category criteria.
#
# TODO: Needs to work with CLI as well
#
sub FileList {
	shift if UNIVERSAL::isa($_[0] => __PACKAGE__);
	my ( $category, $criteria, $value ) = @_;
	my (undef, $authstring) = split / /, $ENV{'HTTP_authorization'};
	my ($auth, $sessionid, $pass) = Remitt::Utilities::Authenticate($authstring);
	return Remitt::Utilities::Fault() if (!$auth);

	# Get username information
	my $session = Remitt::Session->new($sessionid);
		$session->load();
	my $username = $session->{session}->param('username');

	syslog('info', 'Remitt.Interface.FileList called by %s for %s', $username, Dumper($category));

	# Get configuration information
	my $config = Remitt::Utilities::Configuration ( );
	
	# Handle issues with path names
	return [ ] if $category =~ /[\\\/.\ \;\[\]\(\)]/;

	# Handle output
	if ($category eq 'output') {
		my $ds = Remitt::DataStore::Output->new($username);
		#print "for output, searching $criteria = $value\n";
		my $reports = $ds->Search($criteria, $value);
		return $reports;
	}
} # end sub FileList

# Method: Remitt.Interface.GetFile
#
#	Retrieve the contents of a file stored in the REMITT spool. This
#	must be executed by a logged in user.
#
# Parameters:
#
# 	$category - Name of the category that the file is in
#
# 	$file - Name of the file
#
# Returns:
#
# 	Contents of the specified file, or NULL if there is an error.
#
sub GetFile {
	shift if UNIVERSAL::isa($_[0] => __PACKAGE__);
        my $category = shift;
	my $file = shift;
	my (undef, $authstring) = split / /, $ENV{'HTTP_authorization'};
	my ($auth, $sessionid, $pass) = Remitt::Utilities::Authenticate($authstring);
	return Remitt::Utilities::Fault() if (!$auth);

	# Get username information
	my $session = Remitt::Session->new($sessionid);
	$session->load();
	my $username = $session->{session}->param('username');
	
	syslog('info', 'Remitt.Interface.GetFile called by %s for %s / %s', $username, $category, $file);

	# Get configuration information
	my $config = Remitt::Utilities::Configuration ( );
	
	# Handle issues with path names
	return [ ] if $category =~ /[\\\/.\ \;\[\]\(\)]/;
	return [ ] if $file =~ /[\;\[\]\(\)]/;

	my $filename = $config->val('installation', 'path') .
		'/spool/' . $username . '/' . $category . '/' . $file;
	print "filename resolved to $filename\n";

	# Retrieve file and return (will autotype to base64)
	my $buffer;
	open FILE, $filename or return '';
	while (<FILE>) { $buffer .= $_; }
	close FILE;
	return $buffer;
} # end sub GetFile

# Method: Remitt.Interface.GetStatus
#
# 	Get status of job identified by unique identifier, as returned by
# 	<Remitt.Interface.Execute>. This method is not used by the command
# 	line interface (CLI) because it does not store state files.
#
# Parameters:
#
# 	$unique - Unique id key to query about
#
# Returns:
#
#	Mixed; -1 if incomplete, or the actual contents of the unique
#	identifier status file (usually a file name), or -2 if there
#	is a parameter error.
#
sub GetStatus {
	shift if UNIVERSAL::isa($_[0] => __PACKAGE__);
	my $unique = shift;
	my (undef, $authstring) = split / /, $ENV{'HTTP_authorization'};
	my ($auth, $sessionid, $pass) = Remitt::Utilities::Authenticate($authstring);
	return Remitt::Utilities::Fault() if (!$auth);

	syslog('info', "Remitt.Interface.GetStatus called for $unique");

	# Get username information
	my $session = Remitt::Session->new($sessionid);
	$session->load();
	my $username = $session->{session}->param('username');
	
	syslog('info', 'Remitt.Interface.GetStatus called by %s for %s', $username, $unique);

	# Get configuration information
	my $config = Remitt::Utilities::Configuration ( );
	
	# Handle issues with path names
	return -2 if $unique =~ /[^0-9A-Z\.]/;

	# Make sure leading 'Z' is stripped, if it's still there
	$unique =~ s/^Z//;

	# Get information from data store
	#print "Calling getstatus\n";
	my $ds = Remitt::DataStore::Output->new($username);
	my $status = $ds->GetStatus( $unique );
	#print "returned ".Dumper($status)."\n";

	# If the file doesn't exist, return -1
	if (!$status) {
		syslog('info', "Remitt.Interface.GetStatus| Output doesn't exist yet (status was 0)");
		return -1;
	} else {
		# Send back filename from data store
		syslog('info', "Remitt.Interface.GetStatus| Returning filename from datastore ( $username, $unique )");
		#print Dumper($ds->GetFilename( $unique ) );
		return $ds->GetFilename( $unique );
	}
} # end sub GetStatus

# Method: Remitt.Interface.GetOutputMonths
#
#	Get list of available months of output for a particular year.
#
# Returns:
#
# 	Array of month stamps (YYYY-MM).
#
sub GetOutputMonths {
	shift if UNIVERSAL::isa($_[0] => __PACKAGE__);
	my $year = shift;

	my (undef, $authstring) = split / /, $ENV{'HTTP_authorization'};
	my ($auth, $sessionid, $pass) = Remitt::Utilities::Authenticate($authstring);
	return Remitt::Utilities::Fault() if (!$auth);

	# Get username information
	my $session = Remitt::Session->new($sessionid);
	$session->load();
	my $username = $session->{session}->param('username');

	syslog('info', 'Remitt.Interface.GetOutputMonths called by %s with %d', $username, $year);

	my $ds = Remitt::DataStore::Output->new($username);
	return $ds->DistinctMonths( $year );
} # end Remitt.Interface.GetOutputMonths

# Method: Remitt.Interface.GetOutputYears
#
#	Get list of available years of output
#
# Returns:
#
# 	Array of years.
#
sub GetOutputYears {
	shift if UNIVERSAL::isa($_[0] => __PACKAGE__);
	my (undef, $authstring) = split / /, $ENV{'HTTP_authorization'};
	my ($auth, $sessionid, $pass) = Remitt::Utilities::Authenticate($authstring);
	return Remitt::Utilities::Fault() if (!$auth);

	# Get username information
	my $session = Remitt::Session->new($sessionid);
	$session->load();
	my $username = $session->{session}->param('username');

	syslog('info', 'Remitt.Interface.GetOutputYears called by %s', $username);

	my $ds = Remitt::DataStore::Output->new($username);
	return $ds->DistinctYears( );
} # end Remitt.Interface.GetOutputYears

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

	#syslog('info', 'Remitt.Interface.ListPlugins called for %s', $type);

	my $config = Remitt::Utilities::Configuration ( );
	my $path = $config->val('installation', 'path');
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

	#print "Executing Login ($username, $password) ... \n";
	syslog('info', 'Remitt.Interface.SystemLogin attempt for %s', $username);

	# Do we authenticate username/password pair against access list?	
	my $config = Remitt::Utilities::Configuration ( );

	# Only verify if we are sure that we need to
	if ($config->val('installation', 'authentication') eq 'conf') {
		# Built-in configuration file
		die ("Incorrect username or password") if ($password != $config->val('users', $username));
	} elsif ($config->val('installation', 'authentication') eq 'none') {
		# Skip, no auth here	
	} else {
		# Use plugin
		syslog('info', 'Remitt.Interface.SystemLogin using %s plugin', $config->val('installation', 'authentication'));
		eval 'use Remitt::Plugin::Authentication::'.$config->val('installation', 'authentication').';';
		eval 'die ("Incorrect username or password") if (!Remitt::Plugin::Authentication::'.$config->val('installation', 'authentication').'($username, $password);';
	}

	syslog('info', 'Remitt.Interface.SystemLogin successful login for %s', $username);

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
	return "1.1";
} # end sub ProtocolVersion

1;
