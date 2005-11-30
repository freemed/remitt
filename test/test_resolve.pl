#!/usr/bin/perl
#
#	$Id$
#	$Author$
#
# File: test/test_resolve.pl
#
#	Test translation layer resolving.
#

use FindBin;
use lib "$FindBin::Bin/../lib";

use Remitt::Utilities;

print "Translation layer resolving\n";
print "by Jeff Buchbinder\n";
print "\n";

my $render = shift || 'XSLT';
my $renderoption = shift || '837p';
my $transport = shift || 'X12Text';

print "For Render::$render (option $renderoption), Transport::$transport: \n\t";
print Remitt::Utilities::ResolveTranslationPlugin($render,$renderoption,$transport)."\n\n";

print "DEBUGGING INFO:\n";
if ( ! -f "xsl/${renderoption}.xsl") {
	print "	${renderoption}.xsl stylesheet missing\n";
} else {
	print "	${renderoption}.xsl stylesheet present\n";
}

1;
