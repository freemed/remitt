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
#	Quick and dirty test script to test RemittXML -> HCFA1500 PRN rendering.
#	This should not be used in "real life", although it would work. It
#	is run from the root of the REMITT install.
#
#	Syntax: ./test/test_hcfa1500.pl XMLFILE (XSLFILE)
#

use FindBin;
use lib "$FindBin::Bin/../lib";

use XML::LibXSLT;
use XML::LibXML;

# Get parameters
my $input = shift || 'remitt.test.xml';
my $outputformat = shift || 'txt';
my $xsl = shift || 'xsl/hcfa1500.xsl';

if (! -f $input) {
	die "Could not read $input\n";
}

my $parser = XML::LibXML->new();
my $xslt = XML::LibXSLT->new();

my $source = $parser->parse_file($input);
my $style_doc = $parser->parse_file($xsl);

my $stylesheet = $xslt->parse_stylesheet($style_doc);

my $results = $stylesheet->transform($source);

my $output = $stylesheet->output_string($results);

if ($outputformat eq 'pdf') {
	use Remitt::Plugin::Translation::FixedFormPDF;
	print Remitt::Plugin::Translation::FixedFormPDF::Translate($output);
} else {
	use Remitt::Plugin::Translation::FixedFormXML;
	print Remitt::Plugin::Translation::FixedFormXML::Translate($output);
}
