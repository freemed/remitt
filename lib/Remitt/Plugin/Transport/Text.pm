#!/usr/bin/perl -w
#
#	$Id$
#	$Author$
#
#	Dummy filed form plugin to return fixed form text
#

package Remitt::Plugin::Transport::Text;

use Data::Dumper;

sub Transport {
	my ( $input ) = @_;

	return Remitt::Utilities::StoreContents ( $input, 'plaintext', 'txt');
} # end method Transport

sub Config {
	return +{
		'InputFormat' => [ 'text', 'x12' ]
	};
} # end sub Config

sub test {
	my $test = "XYZ";
	print "Initial string = ";
	print $test;
	print "\n---\n";
	print "Output:\n";
	print Remitt::Plugin::Transport::Text::Transport($test);
	print "\n---\n";
} # end sub test

1;
