#!/usr/bin/perl
#
#	$Id$
#	$Author$
#

package Remitt::Plugin::Render::XSLT;

use XML::LibXSLT;
use XML::LibXML;

sub Render {
	my ($input, $option) = @_;

	die("Option not specified!") if (!$option);

	# Read xsl file
	open FILE, "/root/FMSF/remitt/xsl/".$option.".xsl" or die("Could not open $option");
	undef $/; # Undefine the line separator
	my $xsl = <FILE>;
	close FILE;

	my $parser = XML::LibXML->new();
	my $xslt = XML::LibXSLT->new();

	my $source = $parser->parse_string($input);
	my $style_doc = $parser->parse_string($xsl);

	my $stylesheet = $xslt->parse_stylesheet($style_doc);

	my $results = $stylesheet->transform($source);

	return $stylesheet->output_string($results);	
} # end sub Render

sub Config {
	# Return configuration to be stored in global
	return +{
		'Options' => {
			'837p' => {
				'Description' => 'X12 NSF 837 Professional',
				'Media' => 'Electronic',
				'OutputFormat' => 'x12xml'
			}
		},
		'InputFormat' => 'remittxml',
		# 'variable' causes lookup based on options
		'OutputFormat' => 'variable'
	};
} # end sub Config

sub test {
	my $xsl = <<XSL;
<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" />

	<xsl:template match="/">
	<rootelement>
		<xsl:apply-templates/>
	</rootelement>
	</xsl:template>

	<xsl:template match="sub">
		<sub>found element</sub>
	</xsl:template>
</xsl:stylesheet>
XSL

	my $xml = <<XML;
<?xml version="1.0"?>
<something>
	<sub>Test</sub>
</something>
XML

	print "Running renderer ... \n";
	print Remitt::Plugin::Render::XSLT::Render($xsl, $xml);
	print "[done]\n";
} # end sub test

1;

