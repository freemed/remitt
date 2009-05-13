#!/usr/bin/perl -w
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

# Package: Remitt::Plugin::Transport::Text
#
#	Dummy filed form plugin to return fixed form text
#

package Remitt::Plugin::Transport::Text;

use FindBin;
use lib "$FindBin::Bin/../lib";

use Data::Dumper;

sub Transport {
        my $input = shift;
        my $username = shift || Remitt::Utilities::GetUsername();
        my $id = shift || 0;

	return Remitt::Utilities::StoreContents ( $input, $id, 'txt', $username );
} # end method Transport

sub Config {
	return +{
		'InputFormat' => [ 'text', 'x12' ]
	};
} # end sub Config

sub test {
	my $test = "XYZ";
	print "Initial string = ";
	print $test;
	print "\n---\n";
	print "Output:\n";
	print Remitt::Plugin::Transport::Text::Transport($test);
	print "\n---\n";
} # end sub test

1;
