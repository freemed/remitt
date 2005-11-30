#!/usr/bin/perl -w
#
#	$Id$
#	$Author$
#
# Package: Remitt::Plugin::Transport::PDF
#
#	Dummy filed form plugin to return fixed form PDF
#

package Remitt::Plugin::Transport::PDF;

use FindBin;
use lib "$FindBin::Bin/../lib";

use Remitt::Utilities;
use Remitt::Session;
use Data::Dumper;

sub Transport {
	my ( $input, $username ) = @_;

	return Remitt::Utilities::StoreContents ( $input, 'PDF', 'pdf', $username );
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
