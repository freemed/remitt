#!/usr/bin/perl -I../lib -I./lib
#
#	$Id$
#	$Author$
#
#	Quick and dirty test script to test RemittXML -> X12 837P rendering.
#	This should not be used in "real life", although it would work. It
#	is run from the root of the Remitt install.
#
#	Syntax: ./test/test_837p.pl XMLFILE (XSLFILE)
#


use Remitt::Plugin::Translation::X12XML;

use XML::LibXSLT;
use XML::LibXML;

# Get parameters
my $input = shift || 'remitt.test.xml';
my $xsl = shift || 'xsl/837p.xsl';

my $parser = XML::LibXML->new();
my $xslt = XML::LibXSLT->new();

my $source = $parser->parse_file($input);
my $style_doc = $parser->parse_file($xsl);

my $stylesheet = $xslt->parse_stylesheet($style_doc);

my $results = $stylesheet->transform($source);

my $output = $stylesheet->output_string($results);

print Remitt::Plugin::Translation::X12XML::Translate($output);
