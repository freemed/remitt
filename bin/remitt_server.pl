#!/usr/bin/perl
#
#	$Id$
#	$Author$
#
# File: bin/remitt_server.pl
#
# 	REMITT XML-RPC server. This process is run by an init.d script
# 	to provide XML-RPC connectivity for REMITT.
#

# Force internal use of SOAP::Lite patched library and Remitt libs
use lib qw(./lib ../lib /usr/share/remitt/lib);

# Actual includes
use XMLRPC::Transport::HTTP;
use Data::Dumper;
use Remitt::Utilities;
use Sys::Syslog;

my $version = "0.2";
my $protocolversion = 0.2;
my $quiet = 0;

my $config = Remitt::Utilities::Configuration ( );

my $port = $config->val('installation', 'port') || 7688;
my $path = $config->val('installation', 'path');

my $debug = 1;

# Open log file
openlog ( 'remitt', 'cons,pid', 'daemon' );

# Enable basic authentication
$auth = 1;

#my $plugin_types = [
#	'Render',
#	'Translation',
#	'Transport'
#];

if (!$quiet) {
	print "REMITT (XMLRPC Server) v$version\n";
} else {
	syslog('info', 'REMITT v'.$version.' XML-RPC server started');
}

# Start processor thread
my $processor = new Thread \&Remitt::Utilities::ProcessorThread;

$daemon = XMLRPC::Transport::HTTP::Daemon
	-> new ( 
		LocalPort => $port,
		Reuse => 1
	 )
	-> dispatch_to('Remitt::Interface')
	-> options({ compress_threshold => 10000 });
if (!$quiet) {
	print " * Running at ", $daemon->url, "\n";
	print " * Starting daemon ... \n";
}
syslog('info', 'Daemon running at '.$daemon->url);
$daemon->handle;

