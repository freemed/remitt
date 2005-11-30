#!/usr/bin/perl
#	$Id$
#	$Author$
#
# File: bin/sqlite_user_admin.pl
#
# 	Allow user administration of SQLite authentication database.
#

use FindBin;
use lib "$FindBin::Bin/../lib";

use Remitt::Plugin::Authentication::SQLite;

my $usage = "$0 (action) (parameters)\n".
	" actions:\n".
	"\tcreate (username) (password)\n".
	"\tdelete (username)\n".
	"\tcheck (username) (password)\n";
my $action = shift || die ($usage);
my $user   = shift || die ($usage);

if ($action eq 'create') {
	my $pass = shift || die ("Password not specified!\n");
	my $x = Remitt::Plugin::Authentication::SQLite::Create($user, $pass);
	if ($x) {
		print "User '".$user."' created successfully.\n";
	} else {
		print "User '".$user."' failed to create.\n";
	}
} elsif ($action eq 'delete') {
	my $x = Remitt::Plugin::Authentication::SQLite::Delete($user);
	if ($x) {
		print "User '".$user."' deleted successfully.\n";
	} else {
		print "User '".$user."' failed to delete.\n";
	}
} elsif ($action eq 'check') {
	my $pass = shift || die ("Password not specified!\n");
	my $x = Remitt::Plugin::Authentication::SQLite::Authenticate($user, $pass);
	if ($x) {
		print "User and password correct.\n";
	} else {
		print "User and password NOT correct.\n";
	}
} else {
	die($usage);
}

