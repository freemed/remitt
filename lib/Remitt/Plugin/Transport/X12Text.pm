#!/usr/bin/perl -w
#
#	$Id$
#	$Author$
#
#	Dummy X12 plugin to return X12 text
#

package Remitt::Plugin::Transport::X12Text;

use Data::Dumper;

sub Transport {
	my ( $input ) = @_;

	return $input;
} # end method Transport

sub Config {
	return +{
		'InputFormat' => 'x12',
		'OutputFormat' => 'text'
	};
} # end sub Config

sub test {
	my $test = "XYZ";
	print "Initial string = ";
	print $test;
	print "\n---\n";
	print "Output:\n";
	print Remitt::Plugin::Transport::X12Text::Transport($test);
	print "\n---\n";
} # end sub test

1;
