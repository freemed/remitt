#!/usr/bin/perl -w
#
#	$Id$
#	$Author$
#

package Remitt::Plugin::Translation::FixedFormXML;

# Need XML parsing stuff
require XML::Simple;
use Data::Dumper;

sub PadToPosition {
	my ($o_row, $o_col, $n_row, $n_col) = @_;
	my $output = '';

	# Error checking
	if ( ($o_row gt $n_row) or ( ($o_row eq $n_row) and ($o_col+0 > $n_col+0) ) ) {
		#print "failed with o_row/o_col = $o_row/$o_col, n_row/n_col = $n_row/$n_col\n";
		die("Remitt::Plugin::Translation::FixedFormXML::PadToPosition() error in text rendering has occurred\n");
	}

	# Check for proper position already, return no padding
	if ($o_row eq $n_row and $o_col eq $n_col) {
		return '';
	}

	# Otherwise, loop until we get there
	my $c_row = $o_row, $c_col = $o_col;
	while ($c_row+0 < $n_row+0) { $output .= "\n"; $c_row++; }
	while ($c_col+0 < $n_col+0) { $output .= ' '; $c_col++;  }

	# Return the padding
	#print "padded ".length($output)." chars\n";
	return $output;	
} # end sub PadToPosition

sub ProcessElement {
	my ( $e ) = @_;

	if (length($e->{content}) eq $e->{length}) {
		# Return as is
		return $e->{content};
	} elsif (length($e->{content}) lt $e->{length}) {
		# Add spaces
		my $content = $e->{content};
		while (length($content) lt $e->{length}) {
			$content .= ' ';
		}
		return $content;
	} elss {
		# Shorten
		return substr($e->{content}, 0, $e->{length});
	}
} # end sub ProcessElement

sub ProcessPage {
	my ( $p ) = @_;
	my %hash;
	my $output;

	# First, sort everything by row, then column. Hash should do it.
	foreach my $e (@{$p->{element}}) {
		# row.column will increment properly
		my $hash_key = $e->{row}.'.'.$e->{column};
		$hash{$hash_key} = $e;
		#print "Element = ".Dumper($e)."\n";
	} # end foreach element

	# Sort by key so that everything is in order
	my @h = map { { ($_ => $hash{$_}) } } sort keys %hash;

	# Set initial position
	my $currow = 1; my $curcol = 1; my $count = 0;

	foreach my $k (@h) {
		foreach my $e (values %{$k}) {
			#print Dumper($e);

			# Determine if we have to pad to new position
			$output .= Remitt::Plugin::Translation::FixedFormXML::PadToPosition(
				$currow, $curcol, $e->{row}, $e->{column}
			);

			# Readjust positions
			$currow = $e->{row}; $curcol = $e->{column};

			# Render the element
			my $render = Remitt::Plugin::Translation::FixedFormXML::ProcessElement($e);
			$curcol += length($render);
			$output .= $render;
		}
	} # end second loop through elements

	# Pad to page length
	while ($currow < $p->{format}->{pagelength}) {
		$currow++; $output .= "\n";
	}
	
	return $output;
} # end sub ProcessPage

sub Translate {
	my ( $input ) = @_;

	my $xs = new XML::Simple();
	my $i = $xs->XMLin($input);
	$output = '';

	#print Dumper($i);

	# Loop through pages
	foreach my $p (@{$i->{page}}) {
		$output .= Remitt::Plugin::Translation::FixedFormXML::ProcessPage($p);
		#$output .= "content = ".$p->{content}."\n";
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
