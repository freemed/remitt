#!/usr/bin/perl
#
# $Id$
#
# Authors:
#      Jeff Buchbinder <jeff@freemedsoftware.org>
#
# REMITT Electronic Medical Information Translation and Transmission
# Copyright (C) 1999-2009 FreeMED Software Foundation
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.

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
