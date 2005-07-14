#!/usr/bin/perl -I./lib -I../lib
#	$Id$
#	$Author$
#	Test script to render a FixedFormXML stream into a PDF document
#
#	Use:
#		cat file.xml | ./test_render_pdf.pl > file.pdf

use Remitt::Plugin::Translation::FixedFormPDF;

my $input;
while (<>) { $input .= $_; }
print Remitt::Plugin::Translation::FixedFormPDF::Translate($input);

