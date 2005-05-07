#!/usr/bin/perl -w
#
#	$Id$
#	$Author$
#
# Package: Remitt::DataStore
#
#	Manage REMITT output using SQLite
#

package Remitt::DataStore;

use Remitt::Utilities;
use Data::Dumper;
use Compress::Zlib;
use MIME::Base64;
use DBI;
use POSIX;
use Sys::Syslog;
use File::Path;
use Data::Dumper;

require DBD::SQLite;

# Method: Create
#
# 	Adds a transaction to the data store
#
# Parameters:
#
# 	$username - Username for current account
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
	my ( $username, $format, $transport, $data ) = @_;

	# Make sure database is initialized
	my $_x = Remitt::DataStore::Init($username);

	# Start deflation
	#my $compressed_data = Compress::Zlib::memGzip($data);
	my $compressed_data = encode_base64(Compress::Zlib::memGzip($data));
	#print "original length = ".length($data)."\n";
	#print "compressed_data/base64 length = ".length($compressed_data)."\n";
	#print Dumper($compressed_data);

	# Open appropriate file
	my $d = Remitt::DataStore::_Handle ( $username );
	my $s = $d->prepare('INSERT INTO data '.
		'( generated, status, used_format, used_transport, original_data ) '.
		'VALUES ( date(\'now\'), ?, ?, ?, ? )');
	my $r = $s->execute(
		'0',			# status is 0, since it isn't done
		$format,		# format used in rendering
		$transport,		# transport used
		$compressed_data	# original data, slightly thinner
	);

	# Get id to give back
	my $s2 = $d->prepare('SELECT OID,JULIANDAY(generated) FROM data ORDER BY JULIANDAY(generated) DESC');
	my $r2 = $s2->execute;
	if ($r2) {
		my $data = $s2->fetchrow_arrayref;
		return $data->[0];
	} else {
		return 0;
	}
} # end method Create

# Method: GetFilename
#
# Parameters:
#
# 	$username - Username for current account
#
# 	$id - Unique OID describing field
#
# Returns:
#
# 	Status of file specified by OID. (0 = not finished, 1 = finished)
# 	
sub GetFilename {
	my ($username, $id) = @_;

	# Make sure database is initialized
	my $_x = Remitt::DataStore::Init($username);
	my $d = Remitt::DataStore::_Handle($username);
	my $s = $d->prepare('SELECT filename FROM data WHERE OID=?');
	my $r = $s->execute($id);
	if ($r) {
		my $h = $s->fetchrow_arrayref;
		return $h->[0];
	} else {
		return 0;
	}
} # end method GetFilename

# Method: GetStatus
#
# Parameters:
#
# 	$username - Username for current account
#
# 	$id - Unique OID describing field
#
# Returns:
#
# 	Status of file specified by OID. (0 = not finished, 1 = finished)
# 	
sub GetStatus {
	my ($username, $id) = @_;

	# Make sure database is initialized
	my $_x = Remitt::DataStore::Init($username);
	my $d = Remitt::DataStore::_Handle($username);
	my $s = $d->prepare('SELECT status FROM data WHERE OID=?');
	my $r = $s->execute($id);
	if ($r) {
		my $h = $s->fetchrow_arrayref;
		return $h->[0];
	} else {
		return 0;
	}
} # end method GetStatus

# Method: Init
#
# 	Initialize the database, if this has not been done so already.
#
# Parameters:
#
# 	$username - Username information
#
# Returns:
#
# 	Boolean, depending on success.
#
sub Init {
	my ( $username ) = @_;

	# Open appropriate file
	my $config = Remitt::Utilities::Configuration ( );
	my $p = $config->val('installation', 'path').'/spool/'.$username;
	my $f = $p.'/data.db';
	#print "(file = $f)\n";
	if ( -e $f ) {
		# Skip
		return 1;
	} else {
		syslog('info', "Remitt.DataStore.Init| creating $f for $username");
		umask 000;
		mkpath($p, 1, 0755);
		my $d = DBI->connect('dbi:SQLite:dbname='.$f, '', '');
		my $s = $d->do('CREATE TABLE data ( '.
			'filename VARCHAR UNIQUE, '.
			'generated DATE, '.
			'generated_end DATE, '.
			'status INTEGER, '.
			'used_format VARCHAR, '.
			'used_transport VARCHAR, '.
			'original_data BLOB '.
		')');
		if ($s) { return 1; } else { return 0; }
	}
} # end method Init

# Method: SetStatus
#
# Parameters:
#
# 	$username - Username for current account
#
# 	$id - Unique OID describing field
#
# 	$status - New status to set
#
# 	$filename - New filename to set
#
sub SetStatus {
	my ($username, $id, $status, $filename) = @_;

	# Make sure database is initialized
	my $_x = Remitt::DataStore::Init($username);
	my $d = Remitt::DataStore::_Handle($username);
	my $s = $d->prepare('UPDATE data SET status=?, filename=?, generated_end=date(\'now\') WHERE OID=?');
	my $r = $s->execute($status, $filename, $id);
} # end method SetStatus

# Method: _Handle
# 
# 	Return appropriate database handle
# 	
# Parameters:
#
# 	$username - Current user account
#
# Returns:
#
# 	DBI handle
#
sub _Handle {
	my $username = shift;
	# Open appropriate file
	my $config = Remitt::Utilities::Configuration ( );
	my $f = $config->val('installation', 'path').'/spool/'.$username.'/data.db';
	return DBI->connect('dbi:SQLite:dbname='.$f, '', '');
} # end sub _Handle

sub test {
	#print " * Creating temporary output state ... "; 
	#my $x = Remitt::DataStore::Create('test', 'testformat', 'testtransport', '<?xml version="1.0"?><test/>');
	#if ($x) { print "passed ($x)\n"; } else { print "failed\n"; }
	print " * Checking status of 11 ... "; 
	my $x2 = Remitt::DataStore::GetStatus('test', '11');
	if ($x2) { print "passed ($x2)\n"; } else { print "failed ($x2)\n"; }
	print " * Checking filename of 11 ... "; 
	my $x3 = Remitt::DataStore::GetFilename('test', '11');
	if ($x3) { print "passed ($x3)\n"; } else { print "failed ($x3)\n"; }
} # end sub test

1;
