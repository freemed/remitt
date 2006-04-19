#!/usr/bin/perl
#
#	$Id$
#	$Author$
#
# File: bin/original_xml.pl
#
# 	Extract original XML from data store
#

use FindBin;
use lib "$FindBin::Bin/../lib";

use Remitt::DataStore::Configuration;

my $usage = "$0 (username) (oid)\n";
my $user  = shift || die ($usage);
my $oid   = shift || die ($usage);

my $o = Remitt::DataStore::Output->new( $user );
print $o->GetOriginalXml( $oid );

