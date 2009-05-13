#!/usr/bin/perl
#
# $Id$
#
# Authors:
#      Jeff Buchbinder <jeff@freemedsoftware.org>
#
# REMITT Electronic Medical Information Translation and Transmission
# Copyright (C) 1999-2009 FreeMED Software Foundation
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.

# Package: Remitt::DataStore::Output
#
#	Manage REMITT output using SQL
#

package Remitt::DataStore::Output;

use FindBin;
use lib "$FindBin::Bin/../lib";

use Remitt::Utilities;
use Remitt::DataStore::Log;
use Data::Dumper;
use Compress::Zlib;
use MIME::Base64;
use DBI;
use POSIX;
use Sys::Syslog;
use File::Path;

# Method: new
#
# 	Constructor
#
# Parameters:
#
# 	$username - Username for REMITT account
#
sub new {
	my $class = shift;
	my ( $username ) = @_;
	my $self = {};
	$self->{username} = $username;
	bless $self, $class;
	return $self;
} # end constructor

# Method: Create
#
# 	Adds a transaction to the data store
#
# Parameters:
#
# 	$format - Format used in Render plugin
#
# 	$transport - Transport used
#
# 	$data - Original XML used in the transaction
#
# Return:
#
# 	Boolean, depending on success.
#
sub Create {
	my ( $self, $format, $transport, $data ) = @_;

	my $log = Remitt::DataStore::Log->new;

	# Make sure database is initialized
	my $_x = $self->Init();

	# Start deflation
	#my $compressed_data = Compress::Zlib::memGzip($data);
	my $compressed_data = encode_base64(Compress::Zlib::memGzip($data));
	#print "original length = ".length($data)."\n";
	#print "compressed_data/base64 length = ".length($compressed_data)."\n";
	#print Dumper($compressed_data);

	# Open appropriate file
	my $d = $self->_Handle();
	my $s = $d->prepare( 'INSERT INTO output '.
		'( username, generated, status, used_format, used_transport, original_data ) '.
		'VALUES ( ?, NOW(), ?, ?, ?, ? )' );
	my $r = $s->execute(
		$self->{username},
		'0',			# status is 0, since it isn't done
		$format,		# format used in rendering
		$transport,		# transport used
		$compressed_data	# original data, slightly thinner
	);
	$log->Log('SYSTEM', 5, 'Remitt.DataStore.Output', Dumper($r));

	# Get id to give back
	my $s2 = $d->prepare( 'SELECT OID, generated FROM output WHERE username = ? AND original_data=? ORDER BY generated DESC');
	my $r2 = $s2->execute( $self->{username}, $compressed_data );
	if ($r2) {
		my $data = $s2->fetchrow_arrayref;
		$log->Log('SYSTEM', 5, 'Remitt.DataStore.Output', Dumper($data));
		return $data->[0];
	} else {
		$log->Log('SYSTEM', 5, 'Remitt.DataStore.Output', Dumper($r2));
		return 0;
	}
} # end method Create

# Method: DistinctMonths
#
# 	Determine all distinct months of output in a given year.
#
# Parameters:
#
# 	$year - (optional) Year to scan for output months.
#
# Returns:
#
# 	Array of distinct years in output generation stamps.
#
sub DistinctMonths {
	my $self = shift;
	my ( $year ) = @_;
	my $_x = $self->Init();
	my $d = $self->_Handle();
	$year =~ s/[^0-9]//g;
	my $s;
	if ($y) {
		$s = $d->prepare('SELECT DATE_FORMAT(generated, \'%Y-%m\') AS month, DATE_FORMAT(generated, \'%Y\') AS year, COUNT(OID) AS my_count FROM output WHERE username = ? AND year=\''.$year.'\' AND LENGTH(filename) > 0 GROUP BY month ORDER BY month DESC');
	} else {
		$s = $d->prepare('SELECT DATE_FORMAT(generated, \'%Y-%m\') AS month, DATE_FORMAT(generated, \'%Y\') AS year, COUNT(OID) AS my_count FROM output WHERE username = ? AND LENGTH(filename) > 0 GROUP BY month ORDER BY month DESC');
	}
	my $r = $s->execute( $self->{username} ); #($year);
	if ($r) {
		my $results;
		while (my $data = $s->fetchrow_hashref) {
			#print Dumper($data);
			$results->{$data->{'month'}} = $data->{'my_count'};
		}
		return $results;
	} else {
		return 0;
	}
} # end method DistinctMonths

# Method: DistinctYears
#
# 	Determine all distinct years of output.
#
# Returns:
#
# 	Array of distinct years in output generation stamps.
#
sub DistinctYears {
	my ( $self ) = @_;
	my $_x = $self->Init();
	my $d = $self->_Handle();
	my $s = $d->prepare('SELECT DATE_FORMAT(generated, \'%Y\') AS year, COUNT(*) AS counted FROM output WHERE username = ? GROUP BY year ORDER BY year');
	my $r = $s->execute( $self->{username} );
	if ($r) {
		my $results;
		while (my $data = $s->fetchrow_arrayref) {
			#print "found $data->[0] count of $data->[1]\n";
			$results->{$data->[0]} = $data->[1];
		}
		return $results;
	} else {
		return 0;
	}
} # end method DistinctYears

# Method: GetFilename
#
# Parameters:
#
# 	$id - Unique OID describing field
#
# Returns:
#
# 	Status of file specified by OID. (0 = not finished, 1 = finished)
# 	
sub GetFilename {
	my ($self, $id) = @_;

	# Make sure database is initialized
	my $_x = $self->Init();
	my $d = $self->_Handle();
	my $s = $d->prepare('SELECT filename FROM output WHERE username = ? AND OID = ?');
	my $r = $s->execute( $self->{username}, $id );
	if ($r) {
		my $h = $s->fetchrow_arrayref;
		#print Dumper($h);
		return $h->[0];
	} else {
		return 0;
	}
} # end method GetFilename

# Method: GetOriginalXml
#
#	Get original XML stored in the output table in data.db which was used to
#	generate the specified batch.
#
# Parameters:
#
#	$id - Unique OID describing fields
#
# Returns:
#
# 	XML data
#
sub GetOriginalXml {
	my ($self, $id) = @_;

	# Make sure database is initialized
	my $_x = $self->Init();
	my $d = $self->_Handle();
	my $s = $d->prepare( 'SELECT original_data FROM output WHERE username = ? AND OID = ?' );
	my $r = $s->execute( $self->{username}, $id );
	if ($r) {
		my $h = $s->fetchrow_arrayref;
		return Compress::Zlib::memGunzip(decode_base64($h->[0]));
	}
	return '';
} # end method GetOriginalXml

# Method: GetStatus
#
# Parameters:
#
# 	$id - Unique OID describing field
#
# Returns:
#
# 	Status of file specified by OID. (0 = not finished, 1 = finished)
# 	
sub GetStatus {
	my ($self, $id) = @_;

	# Make sure database is initialized
	my $_x = $self->Init();
	my $d = $self->_Handle();
	my $s = $d->prepare( 'SELECT status FROM output WHERE username = ? AND OID = ?' );
	my $r = $s->execute( $self->{username}, $id );
	if ($r) {
		my $h = $s->fetchrow_arrayref;
		#print "GetStatus: "; print Dumper($h);
		return $h->[0];
	} else {
		return 0;
	}
} # end method GetStatus

# Method: SetForeignId
#
# Parameters:
#
# 	$id - Unique OID describing field
# 	
# 	$foreign - New foreign id
#
sub SetForeignId {
	my ($self, $id, $foreign) = @_;

	# Make sure database is initialized
	my $_x = $self->Init();
	my $d = $self->_Handle();
	my $s = $d->prepare( 'UPDATE output SET foreign_id = ? WHERE username = ? AND OID = ?' );
	$s->execute( $foreign, $self->{username}, $id );
} # end method SetForeignId

# Method: Init
#
# 	Initialize the database, if this has not been done so already.
#
# Returns:
#
# 	Boolean, depending on success.
#
sub Init {
	my ( $self ) = @_;

	# Open appropriate file
	my $config = Remitt::Utilities::Configuration ( );
	return 1;
} # end method Init

# Method: Search
#
# Parameters:
#
# 	$criteria - Type to search by
#
# 	$value - Value to search on
#
# Returns:
#
# 	Array of results
#
sub Search {
	my ( $self, $criteria, $value ) = @_;
	my $clause;
	if ($criteria eq 'year') {
		$clause = 'DATE_FORMAT(generated, \'%Y\') = \''.$value.'\'';
	} elsif ($criteria eq 'month') {
		$clause = 'DATE_FORMAT(generated, \'%Y-%m\') = \''.$value.'\'';
	} else {
		# Return nothing if unknown
		return [];
	}

	my $_x = $self->Init();
	my $d = $self->_Handle();
	my $q ='SELECT filename, generated AS generated_on, used_format, used_transport, filesize, ABS( generated_end - generated ) AS execute_time FROM output WHERE username = ? AND '.$clause.' ORDER BY generated';
	my $s = $d->prepare( $q );
	my $r = $s->execute( $self->{username} );
	if ($r) {
		my $results;
		while (my $data = $s->fetchrow_arrayref) {
			#print "found $data->[0] count of $data->[1]\n";
			#print Dumper($data);
			$results->{$data->[0]} = {
				'generated' => $data->[1],
				'format' => $data->[2],
				'transport' => $data->[3],
				'filesize' => $data->[4],
				'time' => $data->[5]
			};
		}
		return $results;
	} else {
		return [];
	}
} # end method Search

# Method: SetStatus
#
# Parameters:
#
# 	$id - Unique OID describing field
#
# 	$status - New status to set
#
# 	$filename - New filename to set
#
sub SetStatus {
	my ( $self, $id, $status, $filename) = @_;

	# Get file size
	my $config = Remitt::Utilities::Configuration ( );
	my $p = $config->val('installation', 'path').'/spool/'.$self->{username}.'/output/'.$filename;
	my $file_size = ( -s $p );
	#print "file size for $p is $file_size\n";

	# Make sure database is initialized
	my $_x = $self->Init();
	my $d = $self->_Handle();
	my $s = $d->prepare( 'UPDATE output SET status=?, filename=?, filesize=?, generated_end=NOW() WHERE username = ? AND OID = ?' );
	my $r = $s->execute( $status, $filename, $file_size, $self->{username}, $id );
} # end method SetStatus

# Method: _Handle
# 
# 	Return appropriate database handle
# 	
# Returns:
#
# 	DBI handle
#
sub _Handle {
	my ( $self ) = shift;
	return Remitt::Utilities::SqlConnection( );
} # end sub _Handle

sub test {
	$obj = new Remitt::DataStore::Output->new ( 'test' );
	#print " * Creating temporary output state ... "; 
	#my $x = $obj->Create( 'testformat', 'testtransport', '<?xml version="1.0"?><test/>' );
	#if ($x) { print "passed ($x)\n"; } else { print "failed\n"; }
	print " * Checking status of 11 ... "; 
	my $x2 = $obj->GetStatus( '11' );
	if ($x2) { print "passed ($x2)\n"; } else { print "failed ($x2)\n"; }
	print " * Checking filename of 11 ... "; 
	my $x3 = $obj->GetFilename( '11' );
	if ($x3) { print "passed ($x3)\n"; } else { print "failed ($x3)\n"; }
} # end sub test

1;
