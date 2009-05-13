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

# File: bin/sql_user_config.pl
#
# 	Allow user configuration database access.
#

use FindBin;
use lib "$FindBin::Bin/../lib";

use Remitt::DataStore::Configuration;

my $usage = "$0 (action) (parameters)\n".
	" actions:\n".
	"\tset (username) (key) (value)\n".
	"\tget (username) (key)\n";
my $action = shift || die ($usage);
my $user   = shift || die ($usage);
my $key    = shift || die ($usage);

if ($action eq 'set') {
	my $value = shift || die ($usage);
	my $c = Remitt::DataStore::Configuration->new($user);
	my $x = $c->SetValue($key, $value);
	if ($x) {
		print "Key '".$key."' set successfully.\n";
	} else {
		print "Key '".$key."' failed to set.\n";
	}
} elsif ($action eq 'get') {
	my $c = Remitt::DataStore::Configuration->new($user);
	my $x = $c->GetValue($key, $value);
	if ($x) {
		print $x."\n";
	} else {
		print "\n";
	}
} else {
	die($usage);
}

