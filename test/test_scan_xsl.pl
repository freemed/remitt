#!/usr/bin/perl
#
#	$Id$
#	$Author$
#
#	Test scan meta-information from XSL file
#

my $file = shift || 'xsl/837p.xsl';

open FILE, $file or die("Could not open XSL\n");
my $line = 0;
while (<FILE>) {
	$line ++;
	my $buf = $_;
	if ($buf =~ /([^:\-\<\!\$]*)?: (.*)?/ and $line < 10) { 
		my $name = $1;
		my $value = $2;
		$name =~ s/^\s//; $name =~ s/\s$//;
		$value =~ s/^\s//; $value =~ s/\s$//;
		if ( ! ( $name =~ / / ) ) {
			print "NAME : $name, ";
			print "VALU : $value\n";
		}
	}
}
close FILE;


