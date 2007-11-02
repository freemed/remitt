#!/usr/bin/perl
#
# $Id$
#
# Authors:
#      Jeff Buchbinder <jeff@freemedsoftware.org>
#
# REMITT Electronic Medical Information Translation and Transmission
# Copyright (C) 1999-2007 FreeMED Software Foundation
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

