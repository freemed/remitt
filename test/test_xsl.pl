#!/usr/bin/perl
#
#	$Id$
#	$Author$
#

use XML::LibXSLT;
use XML::LibXML;

# Get parameters
my $xsl = shift;
my $input = shift;

my $parser = XML::LibXML->new();
my $xslt = XML::LibXSLT->new();

my $source = $parser->parse_file($input);
my $style_doc = $parser->parse_file($xsl);

my $stylesheet = $xslt->parse_stylesheet($style_doc);

my $results = $stylesheet->transform($source);

print $stylesheet->output_string($results);	


