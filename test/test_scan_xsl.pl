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

#
#	Test scan meta-information from XSL file
#

my $file = shift || 'xsl/837p.xsl';

open FILE, $file or die("Could not open XSL\n");
my $line = 0;
while (<FILE>) {
	$line ++;
	my $buf = $_;
	if ($buf =~ /([^:\-\<\!\$]*)?: (.*)?/ and $line < 10) { 
		my $name = $1;
		my $value = $2;
		$name =~ s/^\s//; $name =~ s/\s$//;
		$value =~ s/^\s//; $value =~ s/\s$//;
		if ( ! ( $name =~ / / ) ) {
			print "NAME : $name, ";
			print "VALU : $value\n";
		}
	}
}
close FILE;


