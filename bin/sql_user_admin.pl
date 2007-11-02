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

# File: bin/sql_user_admin.pl
#
# 	Allow user administration of SQL authentication database.
#

use FindBin;
use lib "$FindBin::Bin/../lib";

use Remitt::Plugin::Authentication::SQL;

my $usage = "$0 (action) (parameters)\n".
	" actions:\n".
	"\tcreate (username) (password)\n".
	"\tdelete (username)\n".
	"\tcheck (username) (password)\n";
my $action = shift || die ($usage);
my $user   = shift || die ($usage);

if ($action eq 'create') {
	my $pass = shift || die ("Password not specified!\n");
	my $x = Remitt::Plugin::Authentication::SQL::Create($user, $pass);
	if ($x) {
		print "User '".$user."' created successfully.\n";
	} else {
		print "User '".$user."' failed to create.\n";
	}
} elsif ($action eq 'delete') {
	my $x = Remitt::Plugin::Authentication::SQL::Delete($user);
	if ($x) {
		print "User '".$user."' deleted successfully.\n";
	} else {
		print "User '".$user."' failed to delete.\n";
	}
} elsif ($action eq 'check') {
	my $pass = shift || die ("Password not specified!\n");
	my $x = Remitt::Plugin::Authentication::SQL::Authenticate($user, $pass);
	if ($x) {
		print "User and password correct.\n";
	} else {
		print "User and password NOT correct.\n";
	}
} else {
	die($usage);
}

