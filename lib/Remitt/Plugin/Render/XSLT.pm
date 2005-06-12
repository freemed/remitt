#!/usr/bin/perl
#
#	$Id$
#	$Author$
#
# Package: Remitt::Plugin::Render::XSLT
#
# 	Render layer plugin for XSLT transformations. There really doesn't
# 	have to be another plugin in use in this layer, as the fundamentals
# 	of REMITT rely on this functionality, but it is hypothetically
# 	possible to use another XSL transformation engine or another method
# 	of rendering a document.
#

package Remitt::Plugin::Render::XSLT;

use Remitt::Utilities;
use XML::LibXSLT;
use XML::LibXML;
use Data::Dumper;

# Method: Remitt::Plugin::Render::XSLT::Render
#
# 	Perform XSL transform. This is part of the Render plugin layer
# 	API.
#
# Parameters:
#
# 	$input - Text to be transformed
#
# 	$option - Option to be passed to the XSL transformation engine.
# 	(This is actually the XSL stylesheet to be used, without the
# 	path or .xsl suffix)
#
# Returns:
#
# 	Intermediate XML file.
#
sub Render {
	my ($input, $option) = @_;

	die("Option not specified!") if (!$option);

	my $config = Remitt::Utilities::Configuration ( );
	my $path = $config->val('installation', 'path');

	# Read xsl file
	open FILE, $path.'/xsl/'.$option.'.xsl' or die("Could not open $option");
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

# Method: Remitt::Plugin::Render::XSLT::Config
#
# 	Get configuration data for this plugin. This is part of the
# 	REMITT Plugin API. This particular function reads the information
# 	per XSL transformation stylesheet (which is stored in a specific
# 	format in the XSL comment header) and returns a hash containing
# 	the information for each sheet.
#
# 	This should probably be optimized, as there is no caching
# 	presently implemented for this metainformation.
#
# Parameters:
#
# 	$option - (optional) Specify the input XML format required.
#
# Returns:
#
# 	Hash of configuration data.
#
# SeeAlso:
# 	<GetConfigFromXSL>
#
sub Config {
	# Read from all plugins
	my %c;

	my $option = shift;

	my $config = Remitt::Utilities::Configuration ( );
	my $path = $config->val('installation', 'path');

	opendir DH, $path.'/xsl/' or die ('Could not open XSL path');
	foreach my $xsl (readdir DH) {
		if ($xsl =~ /\.xsl$/) {
			$xsl =~ s/\.xsl$//;
			if ($option) {
				my $temp = GetConfigFromXSL($xsl);
				$c{$xsl} = $temp if ($temp->{InputFormat} eq $option);
			} else {
				$c{$xsl} = GetConfigFromXSL($xsl);
			}
		}
	}

	# Return configuration to be stored in global
	return +{
		'Options' => \%c,
		'InputFormat' => 'remittxml',
		# 'variable' causes lookup based on options
		'OutputFormat' => 'variable'
	};
} # end sub Config

# Method: Remitt::Plugin::Render::XSLT::GetConfigFromXSL
#
# 	Internal method used to read the individual configuration from
# 	an XSL transform stylesheet's metainformation.
#
# Parameters:
#
# 	$xsl - Name of the XSL sheet, without the path or .xsl suffix
#
# Returns:
#
# 	Hash of configuration information.
#
# SeeAlso:
# 	<Config>
#
sub GetConfigFromXSL {
	my $xsl = shift;

	my $config = Remitt::Utilities::Configuration ( );
	my $path = $config->val('installation', 'path');

	# Read xsl file
	open FILE, $path.'/xsl/'.$xsl.'.xsl' or die("Could not open $option");
	my $line = 0;
	my %c;

	# Make sure that the line delimiter is set properly
	$/ = "\n";

	# Read file
	while (<FILE>) {
		$line ++;
		my $buf = $_;
		if ($buf =~ /([^:\-\<\!\$]*)?: (.*)?/ and $line < 10) {
			my $name = $1;
			my $value = $2;
			$name =~ s/^\s//; $name =~ s/\s$//;
			$value =~ s/^\s//; $value =~ s/\s$//;
			if ( ! ( $name =~ / / ) )  {
				$c{$name} = $value;
			}
		}
	}
	close FILE;

	return \%c;
} # end sub GetConfigFromXSL

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

