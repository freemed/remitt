#!/usr/bin/perl -w
#
#	$Id$
#	$Author$
#
# Package: Remitt::DataStore::Log
#
#	REMITT internal logging facility
#

package Remitt::DataStore::Log;

use FindBin;
use lib "$FindBin::Bin/../lib";

use Remitt::Utilities;
use Data::Dumper;
use DBI;
use POSIX;
use Sys::Syslog;
use File::Path;
use Data::Dumper;

require DBD::SQLite;

# Method: new
#
# 	Constructor; initializes log database if necessary.
#
sub new {
	my $class = shift;
	my $self = {};
	bless $self, $class;
	$self->Init();
	$self->{handle} = $self->_Handle();
	return $self;
} # end constructor

# Method: Log
#
# 	Adds a transaction to the system log
#
# Parameters:
#
#	$username - Username
#
#	$verbosity - Level of verbosity (1 = minimum, 10 = serious debug)
#
#	$method - Method information for logger
#
#	$message - Message to be logged
#
sub Log {
	my $self = shift;
	my ( $username, $verbosity, $method, $message ) = @_;

	# Open appropriate file
	my $d = $self->{handle};
	my $s = $d->prepare('INSERT INTO log '.
		'( stamp, verbosity, username, method, message ) '.
		'VALUES ( DATETIME(\'now\'), ?, ?, ?, ? )');
	my $r = $s->execute(
		$username,
		$verbosity,
		$method,
		$message
	);
} # end method Log

# Method: GetLogDate
#
# 	Get log for a specified date
#
# Parameters:
#
#	$date - Date in SQL date format (YYYY-MM-DD)
#
#	$verbosity - (optional) Level of verbosity to display. Defaults
#	to 3.
# 
# Returns:
#
# 	Array of hash of items in log, or undef if none
#
sub GetLog {
	my ( $self ) = @_;
	my $date = shift;
	my $verbosity = shift || 3;
	my $d = $self->{handle};
	my $s = $d->prepare('SELECT *,OID FROM log '.
		'WHERE date(stamp) = date(?) AND verbosity <= ? '.
		'ORDER BY OID');
	my $r = $s->execute($date, $verbosity);
	if ($r) {
		my @results;
		while (my $data = $s->fetchrow_hashref) {
			push @results, $data;
		}
		return @results;
	} else {
		return undef;
	}
} # end method GetLog

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
	my $p = $config->val('installation', 'path').'/spool';
	my $f = $p.'/log.db';
	#print "(file = $f)\n";
	if ( -e $f ) {
		# Skip
		return 1;
	} else {
		syslog('info', "Remitt.DataStore.Log.Init| creating $f");
		umask 000;
		mkpath($p, 1, 0755);
		my $d = DBI->connect('dbi:SQLite:dbname='.$f, '', '');
		my $s = $d->do('CREATE TABLE log ( '.
			'stamp DATE, '.
			'verbosity INTEGER, '.
			'username VARCHAR, '.
			'method VARCHAR, '.
			'message VARCHAR '.
		')');
		if ($s) { return 1; } else { return 0; }
	}
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
	# Open appropriate file
	my $config = Remitt::Utilities::Configuration ( );
	my $f = $config->val('installation', 'path').'/spool/log.db';
	return DBI->connect('dbi:SQLite:dbname='.$f, '', '');
} # end sub _Handle

sub test {
	$obj = new Remitt::DataStore::Log->new;
} # end sub test

1;
