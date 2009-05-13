#!/usr/bin/perl -w
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

# Package: Remitt::Plugin::Authentication::SQL
#
#	Authentication plugin using SQL (DBD) engine.
#

package Remitt::Plugin::Authentication::SQL;

use FindBin;
use lib "$FindBin::Bin/../lib";

use Remitt::Utilities;
use Data::Dumper;
use DBI;
use Digest::MD5 qw(md5_base64);

# Method: Authenticate
# 
# 	Determine if a given username/password pair is valid.
#
# Parameters:
#
# 	$user - Username
#
# 	$pass - Password
#
# Returns:
#
# 	Boolean value, depending on whether or not it is matched in the
# 	SQL database. This database is located at (home)/spool/auth.db.
# 	
sub Authenticate {
	my ( $user, $pass ) = @_;

	# Make sure database is initialized
	Remitt::Plugin::Authentication::SQL::Init();

	# Open appropriate file
	my $d = Remitt::Utilities::SqlConnection( );
	my $s = $d->prepare('SELECT pass FROM auth WHERE user=?');
	my $r = $s->execute($user);
	my $data = $s->fetchrow_hashref;
	if ($data->{'pass'} eq md5_base64($pass)) {
		#print "matched ( ".$data->{'pass'}." == ".md5_base64($pass)." ) ";
		return 1;
	} else {
		#print "no match ( ".$data->{'pass'}." != ".md5_base64($pass)." ) ";
		return 0;
	}
} # end method Authenticate

# Method: Create
#
# 	Add a username/password pair to the authentication system.
#
# Parameters:
#
# 	$user - Username
#
# 	$pass - Password
#
# Return:
#
# 	Boolean, depending on success.
#
sub Create {
	my ( $user, $pass ) = @_;

	# Make sure database is initialized
	my $_x = Remitt::Plugin::Authentication::SQL::Init();

	# Open appropriate file
	my $d = Remitt::Utilities::SqlConnection( );
	my $s = $d->prepare('INSERT INTO auth ( user, pass ) VALUES ( ?, ? )');
	my $r = $s->execute($user, md5_base64($pass));

	if ($r) { return 1; } else { return 0; }
} # end method Create

sub Config {
	return +{
	};
} # end sub Config

# Method: Delete
#
# 	Remove user from SQL database
#
# Parameters:
#
# 	$user - Name of user to delete
#
# Returns:
#
# 	Boolean, depending on success
#
sub Delete {
	my ( $user ) = @_;

	# Make sure database is initialized
	Remitt::Plugin::Authentication::SQL::Init();

	# Open appropriate file
	my $d = Remitt::Utilities::SqlConnection( );
	my $s = $d->prepare('DELETE FROM auth WHERE user=?');
	my $r = $s->execute($user);

	if ($r) { return 1; } else { return 0; }
} # end method Delete

# Method: Init
#
# 	Initialize the database, if this has not been done so already.
#
# Returns:
#
# 	Boolean, depending on success.
#
sub Init {
	# Open appropriate file
	my $config = Remitt::Utilities::Configuration ( );
	return 1;
} # end method Init

sub test {
	print " * Creating temporary user ... "; 
	my $x = Remitt::Plugin::Authentication::SQL::Create('_test', 'test');
	if ($x) { print "passed\n"; } else { print "failed\n"; }
	print " * Checking authentication for user (correct) ... ";
	$x = Remitt::Plugin::Authentication::SQL::Authenticate('_test', 'test');
	if ($x) { print "passed\n"; } else { print "failed\n"; }
	print " * Checking authentication for user (incorrect password) ... ";
	$x = Remitt::Plugin::Authentication::SQL::Authenticate('_test', 'test2');
	if (!$x) { print "passed\n"; } else { print "failed\n"; }
	print " * Checking authentication for user (incorrect name) ... ";
	$x = Remitt::Plugin::Authentication::SQL::Authenticate('_test123', 'test');
	if (!$x) { print "passed\n"; } else { print "failed\n"; }
	print " * Removing temporary user ... "; if (Remitt::Plugin::Authentication::SQL::Delete('_test', 'test')) { print "passed\n"; } else { print "failed\n"; }
} # end sub test

1;
