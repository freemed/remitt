#!/usr/bin/perl -w
#
#	$Id$
#	$Author$
#
# Package: Remitt::Plugin::Translation::FixedFormXML
#
# 	Translation layer plugin to provide direct text rendering from
# 	fixed form output.
#

package Remitt::Plugin::Translation::FixedFormXML;

# Need XML parsing stuff
require XML::Simple;
use Data::Dumper;

sub PadToPosition {
	my ($o_row, $o_col, $n_row, $n_col) = @_;
	my $output = '';

	# Error checking
	if ( $o_row > $n_row ) {
		#print "failed with o_row/o_col = $o_row/$o_col, n_row/n_col = $n_row/$n_col\n";
		die("Remitt::Plugin::Translation::FixedFormXML::PadToPosition() error in text rendering has occurred\n");
	}
	if ( ($o_row == $n_row) and ($o_col > $n_col) ) {
		#print "failed with o_row/o_col = $o_row/$o_col, n_row/n_col = $n_row/$n_col\n";
		die("Remitt::Plugin::Translation::FixedFormXML::PadToPosition() error in text rendering has occurred\n");
	}

	# Check for proper position already, return no padding
	if ($o_row eq $n_row and $o_col eq $n_col) {
		return '';
	}

	# Otherwise, loop until we get there
	my $c_row = $o_row, $c_col = $o_col;
	while ($c_row+0 < $n_row+0) { $output .= "\n"; $c_row++; $c_col = 1; }
	while ($c_col+0 < $n_col+0) { $output .= ' '; $c_col++;  }

	# Return the padding
	#print "padded ".length($output)." chars\n";
	return $output;	
} # end sub PadToPosition

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
	my ( $p ) = @_;
	my %hash;
	my $output;

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

	# Set initial position
	my $currow = 1; my $curcol = 1; my $count = 0;

	#print "translation: processing page\n";

	foreach my $k (@h) {
		foreach my $e (values %{$k}) {
			#print Dumper($e);

			# Determine if we have to pad to new position
			#print "cur_r = $currow, cur_c = $curcol, new_r = ".$e->{row}.", new_c = ".$e->{column}."\n";
			$output .= Remitt::Plugin::Translation::FixedFormXML::PadToPosition(
				$currow, $curcol, $e->{row}, $e->{column}
			);

			# Readjust positions
			$currow = $e->{row}; $curcol = $e->{column};

			# Render the element
			my $render = Remitt::Plugin::Translation::FixedFormXML::ProcessElement($e);
			$curcol += length($render);
			#print "length of render = ".length($render).", render = '".$render."', curcol = $curcol\n";
			$output .= $render;
		}
	} # end second loop through elements

	# Pad to page length
	while ($currow < $p->{format}->{pagelength}) {
		$currow++; $output .= "\x0d\x0a";
	}

	#print "end page\n";

	return $output;
} # end sub ProcessPage

sub Translate {
	my ( $input ) = @_;

	my $xs = new XML::Simple(
		# see lib/Remitt/Plugin/Translation/XSLT.pm for more info
		NormalizeSpace => 0,
		ForceArray => [ 'element', 'page' ]
		);
	my $i = $xs->XMLin($input);
	$output = '';

	#print Dumper($i);

	# Loop through pages
	foreach my $p (@{$i->{page}}) {
		#print "looping into page\n";
		$output .= Remitt::Plugin::Translation::FixedFormXML::ProcessPage($p);
		#$output .= "content = ".$p->{content}."\x0d\x0a";
		#print "end page loop\n";
	}

	return $output;	
} # end sub Translate

sub Config {
	return +{
		'InputFormat'	=> 'fixedformxml',
		'OutputFormat'	=> 'text'
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
	print Remitt::Plugin::Translation::FixedFormXML::Translate($test);
	print "\n---\n";
} # end sub test

1;
