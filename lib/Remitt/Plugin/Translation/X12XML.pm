#!/usr/bin/perl -w
#
#	$Id$
#	$Author$
#
#	Simple X12 intermediary format to X12 parser
#

package Remitt::Plugin::Translation::X12XML;

# Need XML parsing stuff
require XML::Simple;
use Data::Dumper;

my $hlcounter;

sub ProcessSegment {
	my ( $p, $delim, $eol, $count ) = @_;
	my $segment;

	# Push segment header as first item in hash
	$segment = $p->{sid};

	# First, sort everything by row, then column. Hash should do it.
	foreach my $e (@{$p->{element}}) {
		if (defined ($e->{segmentcount})) {
			# Give segment count minus header
			$segment .= $delim . ( $count - 2);
		} elsif (defined ($e->{hlcounter})) {
			# Handle an HL counter, with increment
			$Remitt::Translation::X12XML::hlcounter ++;
			$segment .= $delim . $Remitt::Translation::X12XML::hlcounter;
		#} elsif (defined ($e->{hlcounterparent})) {
		#	$segment .= $delim . ($Remitt::Translation::X12XML::hlcounter - 1);
		} else {
			my $content = $e->{content};
			if ($content->{text}) { $content = $content->{text}; }
			# Handle null array instances
			if ($content =~ /HASH\(/) {
				$content = '';
			}
			#print "e->content = ".Dumper($content)."\n";
			$segment .= $delim . $content; 
		}
		#print "Element = ".Dumper($e)."\n";
	} # end foreach element

	# Append ending characters
	$segment .= $eol . "\n";

	return $segment;
} # end sub ProcessPage

sub Translate {
	my ( $input ) = @_;

	# Reset hlcounter, if it exists
	$Remitt::Translation::X12XML::hlcounter = 0;

	my $xs = new XML::Simple(
			# NormalizeSpace - 
			# Set to preserve spaces in <content> tags so that
			# spaces are not lost.
		NormalizeSpace => 0, 
			# ForceArray -
			# Causes <element> tags to always be read as an
			# array so that single element <x12segment>s
			# do not choke the parser.
		ForceArray => [ 'element' ]
	);
	my $i = $xs->XMLin($input);
	my $output = '';
	my $count = 0;

	#print Dumper($i);

	# Loop through segments
	foreach my $s (@{$i->{x12segment}}) {
		$count += 1;
		$output .= Remitt::Plugin::Translation::X12XML::ProcessSegment($s, $i->{x12format}->{delimiter}, $i->{x12format}->{endofline}, $count);
	}

	return $output;	
} # end sub Translate

sub Config {
	return +{
		'InputFormat' => 'x12xml',
		'OutputFormat' => 'x12'
	};
} # end sub Config

sub test {
	my $test = "<?xml version=\"1.0\"?>\n".
		"<render>\n".
		"	<x12format>\n".
		"		<endofline>~</endofline>\n".
		"		<delimiter>*</delimiter>\n".
		"	</x12format>\n".
		"	<x12segment sid=\"ISA\">\n".
		"		<comment>ISA Header segment</comment>\n".
		"		<element>\n".
		"			<comment>something here</comment>\n".
		"			<content>SECOND</content>\n".
		"		</element>\n".
		"		<element>\n".
		"			<content>FIRST</content>\n".
		"		</element>\n".
		"	</x12segment>\n".
		"	<x12segment sid=\"GS\">\n".
		"		<comment>GS Header segment</comment>\n".
		"		<element>\n".
		"			<comment>something here</comment>\n".
		"			<content>12345</content>\n".
		"		</element>\n".
		"		<element>\n".
		"			<content>H12</content>\n".
		"		</element>\n".
		"	</x12segment>\n".
		"	<x12segment sid=\"SE\">\n".
		"		<comment>Count segment</comment>\n".
		"		<element>\n".
		"			<segmentcount />\n".
		"		</element>\n".
		"		<element>\n".
		"			<content>H12</content>\n".
		"		</element>\n".
		"	</x12segment>\n".
		"</render>\n";

	print "\n---\n";
	print "Initial string = ";
	print $test;
	print "\n---\n";
	print "Output:\n";
	print Remitt::Plugin::Translation::X12XML::Translate($test);
	print "\n---\n";
} # end sub test

1;
