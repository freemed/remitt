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
# File: test/test_resolve.pl
#
#	Test translation layer resolving.
#

use FindBin;
use lib "$FindBin::Bin/../lib";

use Remitt::Utilities;

print "Translation layer resolving\n";
print "by Jeff Buchbinder\n";
print "\n";

my $render = shift || 'XSLT';
my $renderoption = shift || '837p';
my $transport = shift || 'X12Text';

print "For Render::$render (option $renderoption), Transport::$transport: \n\t";
print Remitt::Utilities::ResolveTranslationPlugin($render,$renderoption,$transport)."\n\n";

print "DEBUGGING INFO:\n";
if ( ! -f "xsl/${renderoption}.xsl") {
	print "	${renderoption}.xsl stylesheet missing\n";
} else {
	print "	${renderoption}.xsl stylesheet present\n";
}

1;
