#!/usr/bin/perl
#
#	$Id$
#	$Author$
#
# File: test/test_loader.pl
#
#	Regression testing bootstrap. This script runs the "test" method on
#	whatever perl package it is called on. The packages are in the
#	Remitt::Package::Whatever format.
#

use FindBin;
use lib "$FindBin::Bin/../lib";

print "Module Test Loader\n";
print "by Jeff Buchbinder\n";
print "\n";

my $module = shift;

print "Attempting to run method test on $module\n\n";

if (!$module) {
	die ("Could not load module, none specified!\n");
}

eval "use ".$module.";";

eval $module."::test();";
