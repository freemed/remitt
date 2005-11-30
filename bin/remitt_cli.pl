#!/usr/bin/perl
#
#	$Id$
#	$Author$
#
# File: bin/remitt_cli.pl
#
#	Command Line Interface to translation tool

# Force internal use of SOAP::Lite patched library and Remitt libs
use FindBin;
use lib "$FindBin::Bin/../lib";

# Actual includes
use Remitt::Interface;
use Remitt::Utilities;
use Data::Dumper;
use Sys::Syslog;

sub options {
	return "usage: remitt_cli.pl ".
		"xmlfile renderplugin renderoption transportplugin\n";
}

# Get parameters
my $file = shift || die(options());
my $render = shift || die(options());
my $roption = shift || die(options());
my $transport = shift || die(options());

my $version = "0.2";
my $protocolversion = 0.2;
my $quiet = 1;

my $config = Remitt::Utilities::Configuration ( );

my $debug = 1;

# Open syslog
openlog ( 'remitt', 'pid', 'user' );

if (!$quiet) {
	print "REMITT CLI v$version\n";
}
syslog ('info', 'REMITT v'.$version.' CLI started');

undef $/;
open FILE, $file or die("Could not open $file\n");
my $buffer = <FILE>;
close FILE;

print Remitt::Interface::Execute($buffer, $render, $roption, $transport);

