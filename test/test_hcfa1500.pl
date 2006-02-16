#!/usr/bin/perl
#
#	$Id$
#	$Author: jeff $
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
