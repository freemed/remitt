#!/usr/bin/perl
#
#	$Id$
#	$Author$
#
#	Command Line Interface to translation tool

# Force internal use of SOAP::Lite patched library and Remitt libs
use lib qw(./lib ../lib);

# Actual includes
use Remitt::Interface;
use Remitt::Utilities;
use Data::Dumper;

sub options {
	return "usage: remitt_cli.pl ".
		"xmlfile renderplugin renderoption transportplugin\n";
}

# Get parameters
my $file = shift || die(options());
my $render = shift || die(options());
my $roption = shift || die(options());
my $transport = shift || die(options());

my $version = "0.1";
my $protocolversion = 0.1;
my $quiet = 0;

my $config = Remitt::Utilities::Configuration ( );

my $debug = 1;

if (!$quiet) {
	print "REMITT CLI v$version\n";
}

undef $/;
open FILE, $file or die("Could not open $file\n");
my $buffer = <FILE>;
close FILE;

print Remitt::Interface::Execute($buffer, $render, $roption, $transport);
