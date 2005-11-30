#!/usr/bin/perl
#	$Id$
#	$Author$
#	Test script to render a FixedFormXML stream into a PDF document
#
#	Use:
#		cat file.xml | ./test_render_pdf.pl > file.pdf

use FindBin;
use lib "$FindBin::Bin/../lib";

use Remitt::Plugin::Translation::FixedFormPDF;

my $input;
while (<>) { $input .= $_; }
print Remitt::Plugin::Translation::FixedFormPDF::Translate($input);

