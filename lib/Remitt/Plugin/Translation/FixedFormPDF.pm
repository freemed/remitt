#!/usr/bin/perl -w
#
#	$Id$
#	$Author$
#
# Package: Remitt::Plugin::Translation::FixedFormPDF
#
# 	Translation layer plugin to provide PDF export from fixed
# 	form description.
#

package Remitt::Plugin::Translation::FixedFormPDF;

# Need XML parsing stuff
require XML::Simple;
use Remitt::Utilities;
use PDF::API2;
use Data::Dumper;

sub ProcessElement {
	my ( $e ) = @_;

	my $content = $e->{content};
	my $clength  = $e->{length};
	#print "processelement content=$content, length=$clength\n";

	# Handle null array instances
	if ($content =~ /HASH\(/) {
		if ($content->{text}) { $content = $content->{text}; }
		else { $content = ''; }
	}

	if (length($content) == $clength) {
		# Return as is
		return $content;
	} elsif (length($content) < $clength) {
		# Add spaces
		while (length($content) < $clength) {
			$content .= ' ';
		}
		#print "length of content = ".length($content)."\n";
		return $content;
	} elss {
		# Shorten
		return substr($content, 0, $clength);
	}
} # end sub ProcessElement

sub ProcessPage {
	my ( $p, $pdf, $pagecount ) = @_;
	my %hash;
	my $output;

	#print "Entered ProcessPage\n";

	# Get configuration
	my $config = Remitt::Utilities::Configuration ( );
	my $path = $config->val('installation', 'path');

	# Get formatting information
	my $template = $p->{format}->{pdf}->{template};
	my $template_page = $p->{format}->{pdf}->{page};
	my $font_name = $p->{format}->{pdf}->{font}->{name};
	my $font_size = $p->{format}->{pdf}->{font}->{size};
	my $h_offset = $p->{format}->{pdf}->{offset}->{horizontal};
	my $v_offset = $p->{format}->{pdf}->{offset}->{vertical};
	my $h_scaling = $p->{format}->{pdf}->{scaling}->{horizontal};
	my $v_scaling = $p->{format}->{pdf}->{scaling}->{vertical};

	# Import template page if one is specified
	if (-f $path . '/pdf/' . $template . '.pdf') {
		#print "Importing page $path/pdf/$template.pdf ... \n";
		my $original = PDF::API2->open ( $path . '/pdf/' . $template . '.pdf' );
		#my $img = $pdf->pdfimageobj($original, $template_page);
		#my $gfx = $page->gfx;
		#$gfx->pdfimage($img, 10, -45, 1);
		$pdf->importpage( $original, $template_page, $pagecount );
		#print "[done]\n";
	}

	# Create text page object
	my $page = $pdf->openpage($pagecount);
	my $txt = $page->text;
	$txt->font($pdf->corefont($font_name, 1), $font_size);

	# First, sort everything by row, then column. Hash should do it.
	foreach my $e (@{$p->{element}}) {
		# row.column will increment properly
		my $hash_key = 1000000 + ($e->{row} << 8) + $e->{column};
		#print "row = ".$e->{row}." row hash = ".($e->{row} << 8)." col = ".$e->{column}." hash key = $hash_key \n";
		$hash{$hash_key} = $e;
		#print "Element = ".Dumper($e)."\n";
	} # end foreach element

	# Sort by key so that everything is in order
	my @h = map { { ($_ => $hash{$_}) } } sort keys %hash;

	foreach my $k (@h) {
		foreach my $e (values %{$k}) {
			#print Dumper($e);

			# Set positioning
			$txt->translate(($e->{column} * $h_scaling) + $h_offset,
				(792 - ($e->{row} * $v_scaling)) - $v_offset);

			# Render text
			$txt->text(ProcessElement($e));
		}
	} # end second loop through elements

	# End page and attach
	$txt->compress;
} # end sub ProcessPage

sub Translate {
	my ( $input ) = @_;

	#print "Entered PDF::Transport\n";

	my $xs = new XML::Simple(
		# see lib/Remitt/Plugin/Translation/XSLT.pm for more info
		NormalizeSpace => 0,
		ForceArray => [ 'element', 'page' ]
		);
	my $i = $xs->XMLin($input);

	#print Dumper($i);

	# Create PDF document
	my $pdf = PDF::API2->new;
	delete $pdf->{forcecompresss};
	#print "Created PDF document\n";

	# Loop through pages
	my $pagecount = 0;
	foreach my $p (@{$i->{page}}) {
		$pagecount ++;
		#print "looping for page $pagecount\n";
		Remitt::Plugin::Translation::FixedFormPDF::ProcessPage($p, $pdf, $pagecount);
	}

	# Close PDF
	my $output = $pdf->stringify;
	$pdf->end;

	return $output;	
} # end sub Translate

sub Config {
	return +{
		'InputFormat'	=> 'fixedformxml',
		'OutputFormat'	=> 'pdf'
	};
} # end sub Config

sub test {
	my $test = "<?xml version=\"1.0\"?>\n".
		"<render>\n".
		"	<page>\n".
		"		<format>\n".
		"			<pagelength>5</pagelength>\n".
		"		</format>\n".
		"		<element>\n".
		"			<row>1</row>\n".
		"			<column>12</column>\n".
		"			<length>11</length>\n".
		"			<content>SECOND</content>\n".
		"			<comment>Who cares?</comment>\n".
		"		</element>\n".
		"		<element>\n".
		"			<row>1</row>\n".
		"			<column>1</column>\n".
		"			<length>6</length>\n".
		"			<content>FIRST</content>\n".
		"			<comment>Who cares?</comment>\n".
		"		</element>\n".
		"	</page>\n".
		"	<page>\n".
		"	</page>\n".
		"</render>\n";

	print "\n---\n";
	print "Initial string = ";
	print $test;
	print "\n---\n";
	print "Output:\n";
	print Remitt::Plugin::Translation::FixedFormPDF::Translate($test);
	print "\n---\n";
} # end sub test

1;
