#!/usr/bin/perl -I./lib -I../lib
#
#	$Id$
#	$Author$
#

use XMLRPC::Lite;
use Data::Dumper;

my $action = shift || 255;

my $user   = 'demo';
my $pass   = 'demo';
my $host   = '127.0.0.1';
my $port   = '7688';
my $debug  = 0;

print "REMITT Regression Test XML-RPC Client\n";
print "(c) 2004 FreeMED Software Foundation\n\n";

# Non-authenticated, first

my $xmlrpc = XMLRPC::Lite
	-> proxy('http://'.$host.':'.$port.'/RPC2');
if ($debug) { $xmlrpc->on_debug(sub { print @_; }); }
my ($key, $sessionid);

if ($action & 1) {
	print " * Running protocol version test ... ";
	my $call = $xmlrpc->call('Remitt.Interface.ProtocolVersion');
	my $result = $call->result;
	print $result."\n";
}

if ($action & 2) {
	print " * Running login test ... ";
	my $call = $xmlrpc->call('Remitt.Interface.SystemLogin', $user, $pass);
	my $result = $call->result;
	if (length($result->{sessionid})<16 or length($result->{key})<16) {
		print "FAILED\n";
		die();
	} else {
		print "PASSED\n";
	}
	$sessionid = $result->{sessionid};
	$key = $result->{key};
	#print "SESSION ID = $sessionid, KEY = $key\n";
}

# Authenticated next
my $xmlrpc = XMLRPC::Lite
	-> proxy('http://'.$sessionid.':'.$key.'@'.$host.':'.$port.'/RPC2');

if ($action & 4) {
	print " * Running ListPlugins(Translation) test ... ";
	my $call = $xmlrpc->call('Remitt.Interface.ListPlugins', 'Translation');
	my $result = $call->result;
	print "done\n";
	print Dumper($result);
}

# Authenticated next
my $xmlrpc = XMLRPC::Lite
	-> proxy('http://'.$sessionid.':'.$key.'@'.$host.':'.$port.'/RPC2');

if ($action & 4) {
	print " * Running ListOptions(Render,XSLT) test ... ";
	my $call = $xmlrpc->call('Remitt.Interface.ListOptions', 'Render', 'XSLT');
	my $result = $call->result;
	print "done\n";
	print Dumper($result);
} 

if ($action & 8) {
	undef $/;
	open FILE, "remitt.smallset.xml" or die("Could not open remitt test file\n");
	my $input = <FILE>;
	close FILE;
	print " * Running Execute(XSLT, 837p, X12Text) test ... ";
	my $call = $xmlrpc->call('Remitt.Interface.Execute', $input, 'XSLT', '837p', 'Text');
	my $result = $call->result;
	print "done\n";
	print Dumper($result);
}

#$call = $xmlrpc->call('something', {param=>1, param=>2});
#$result = $xmlrpc->result;

if ($action & 16) {
	undef $/;
	open FILE, "remitt.smallset.xml" or die("Could not open remitt test file\n");
	my $input = <FILE>;
	close FILE;
	print " * Running Execute(XSLT, hcfa1500, FixedFormText) test ... ";
	my $call = $xmlrpc->call('Remitt.Interface.Execute', $input, 'XSLT', 'hcfa1500', 'Text');
	my $result = $call->result;
	print "done\n";
	print Dumper($result);
}

#$call = $xmlrpc->call('something', {param=>1, param=>2});
#$result = $xmlrpc->result;
