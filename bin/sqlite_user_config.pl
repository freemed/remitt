#!/usr/bin/perl
#	$Id$
#	$Author$
#
# File: bin/sqlite_user_config.pl
#
# 	Allow user configuration database access.
#

use lib qw(./lib ../lib /usr/share/remitt/lib);

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

