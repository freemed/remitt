#!/usr/bin/perl -I./lib
#
#	$Id$
#	$Author$
#
# File: test/test_resolve.pl
#
#	Test translation layer resolving.
#

use Remitt::Utilities;

print "Translation layer resolving\n";
print "by Jeff Buchbinder\n";
print "\n";

my $render = shift || 'XSLT';
my $renderoption = shift || '837p';
my $transport = shift || 'X12Text';

print "For Render::$render (option $renderoption), Transport::$transport: \n\t";
print Remitt::Utilities::ResolveTranslationPlugin($render,$renderoption,$transport)."\n";

1;
