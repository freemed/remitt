#!/usr/bin/perl -w
#
#	$Id$
#	$Author$
#
# Package: Remitt::Plugin::Authentication::SQLite
#
#	Authentication plugin using SQLite engine.
#

package Remitt::Plugin::Authentication::SQLite;

use Remitt::Utilities;
use Data::Dumper;
use DBI;
use Digest::MD5 qw(md5_base64);
require DBD::SQLite;

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
# 	SQLite database. This database is located at (home)/spool/auth.db.
# 	
sub Authenticate {
	my ( $user, $pass ) = @_;

	# Make sure database is initialized
	Remitt::Plugin::Authentication::SQLite::Init();

	# Open appropriate file
	my $config = Remitt::Utilities::Configuration ( );
	my $d = DBI->connect('dbi:SQLite:dbname='.$config->val('installation', 'path').'/spool/auth.db', '', '');
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
	my $_x = Remitt::Plugin::Authentication::SQLite::Init();

	# Open appropriate file
	my $config = Remitt::Utilities::Configuration ( );
	my $d = DBI->connect('dbi:SQLite:dbname='.$config->val('installation', 'path').'/spool/auth.db', '', '');
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
# 	Remove user from SQLite database
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
	Remitt::Plugin::Authentication::SQLite::Init();

	# Open appropriate file
	my $config = Remitt::Utilities::Configuration ( );
	my $d = DBI->connect('dbi:SQLite:dbname='.$config->val('installation', 'path').'/spool/auth.db', '', '');
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
	my $f = $config->val('installation', 'path').'/spool/auth.db';
	#print "(file = $f) ";
	if ( -e $f ) {
		# Skip
		return 1;
	} else {
		my $d = DBI->connect('dbi:SQLite:dbname='.$config->val('installation', 'path').'/spool/auth.db', '', '');
		my $s = $d->do('CREATE TABLE auth ( user varchari unique, pass varchar)');
		if ($s) { return 1; } else { return 0; }
	}
} # end method Init

sub test {
	print " * Creating temporary user ... "; 
	my $x = Remitt::Plugin::Authentication::SQLite::Create('_test', 'test');
	if ($x) { print "passed\n"; } else { print "failed\n"; }
	print " * Checking authentication for user (correct) ... ";
	$x = Remitt::Plugin::Authentication::SQLite::Authenticate('_test', 'test');
	if ($x) { print "passed\n"; } else { print "failed\n"; }
	print " * Checking authentication for user (incorrect password) ... ";
	$x = Remitt::Plugin::Authentication::SQLite::Authenticate('_test', 'test2');
	if (!$x) { print "passed\n"; } else { print "failed\n"; }
	print " * Checking authentication for user (incorrect name) ... ";
	$x = Remitt::Plugin::Authentication::SQLite::Authenticate('_test123', 'test');
	if (!$x) { print "passed\n"; } else { print "failed\n"; }
	print " * Removing temporary user ... "; if (Remitt::Plugin::Authentication::SQLite::Delete('_test', 'test')) { print "passed\n"; } else { print "failed\n"; }
} # end sub test

1;
