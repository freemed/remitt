#!/usr/bin/perl
#
# $Id$
#
# Authors:
#      Jeff Buchbinder <jeff@freemedsoftware.org>
#
# REMITT Electronic Medical Information Translation and Transmission
# Copyright (C) 1999-2007 FreeMED Software Foundation
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

# Package: Remitt::DataStore::Configuration
#
#	REMITT per-user configuration facility
#

package Remitt::DataStore::Configuration;

use FindBin;
use lib "$FindBin::Bin/../lib";

use Remitt::Utilities;
use Data::Dumper;
use DBI;
use POSIX;
use Sys::Syslog;
use File::Path;
use Data::Dumper;

# Method: new
#
# 	Constructor; initializes config database if necessary.
#
# Parameters:
#
# 	$username - Username to which this is attached
#
sub new {
	my $class = shift;
	my $username = shift;
	my $self = {};
	$self->{username} = $username;
	bless $self, $class;
	$self->Init();
	$self->{handle} = $self->_Handle();
	return $self;
} # end constructor

# Method: SetValue
#
# 	Set a key/value pair in the configuration database
#
# Parameters:
#
#	$key - Configuration key
#
#	$value - New value
#
sub SetValue {
	my $self = shift;
	my ($key, $value) = @_;

	# Open appropriate file
	my $d = $self->{handle};

	# Remove old value (if there is one)
	my $k = $d->prepare( 'DELETE FROM config WHERE username = ? AND k = ?' );
	my $k_r = $k->execute( $self->{username}, $key );

	# ... and insert a new one
	my $s = $d->prepare( 'INSERT INTO config '.
		'( username, k, v ) '.
		'VALUES ( ?, ?, ? )' );
	my $r = $s->execute( $self->{username}, $key, $value );
} # end method SetValue

# Method: GetValue
#
# 	Get configuration value for the specified key
#
# Parameters:
#
#	$key - Configuration key
# 
# Returns:
#
# 	String
#
sub GetValue {
	my $self = shift;
	my ( $key ) = @_;
	my $d = $self->{handle};
	my $s = $d->prepare( 'SELECT * FROM config WHERE username = ? AND k = ?' );
	my $r = $s->execute( $self->{username}, $key );
	if ($r) {
		my $data = $s->fetchrow_hashref;
		return $data->{v};
	} else {
		return '';
	}
} # end method GetValue

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
	$obj = new Remitt::DataStore::Configuration->new;
} # end sub test

1;
