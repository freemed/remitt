#!/usr/bin/perl -w
#
#	$Id$
#	$Author$
#
#	Dummy filed form plugin to return fixed form PDF
#

package Remitt::Plugin::Transport::PDF;

use Data::Dumper;

sub Transport {
	my ( $input ) = @_;

	return $input;
} # end method Transport

sub Config {
	return +{
		'InputFormat' => [ 'pdf' ]
	};
} # end sub Config

sub test {
	my $test = "XYZ";
	print "Initial string = ";
	print $test;
	print "\n---\n";
	print "Output:\n";
	print Remitt::Plugin::Transport::PDF::Transport($test);
	print "\n---\n";
} # end sub test

1;
