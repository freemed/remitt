#!/usr/bin/perl
#
#	$Id$
#	$Author$
#

# Force internal use of SOAP::Lite patched library and Remitt libs
use lib qw(./lib ../lib);

# Actual includes
use XMLRPC::Transport::HTTP;
use Data::Dumper;
use Remitt::Utilities;

my $version = "0.1";
my $protocolversion = 0.1;
my $quiet = 0;

my $config = Remitt::Utilities::Configuration ( );

my $port = $config->val('installation', 'port') || 7688;
my $path = $config->val('installation', 'path');

my $debug = 1;

#my $plugin_types = [
#	'Render',
#	'Translation',
#	'Transport'
#];

if (!$quiet) {
	print "REMITT (XMLRPC Server) v$version\n";
}

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
$daemon->handle;

