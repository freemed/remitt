#!/usr/bin/perl -w
#
#	$Id$
#	$Author$
#
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

require DBD::SQLite;

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
	my $k = $d->prepare('DELETE FROM config WHERE k = ?');
	my $k_r = $k->execute($key);

	# ... and insert a new one
	my $s = $d->prepare('INSERT INTO config '.
		'( k, v ) '.
		'VALUES ( ?, ? )');
	my $r = $s->execute($key, $value);
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
	my $s = $d->prepare('SELECT * FROM config WHERE k = ?');
	my $r = $s->execute($key);
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
	my $p = $config->val('installation', 'path').'/spool';
	my $f = $p.'/'.$self->{username}.'/config.db';
	#print "(file = $f)\n";
	if ( -e $f ) {
		# Skip
		return 1;
	} else {
		syslog('info', "Remitt.DataStore.Configuration.Init| creating $f");
		umask 000;
		mkpath($p, 1, 0755);
		my $d = DBI->connect('dbi:SQLite:dbname='.$f, '', '');
		my $s = $d->do('CREATE TABLE config ( '.
			'k VARCHAR, '.
			'v VARCHAR '.
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
	my $f = $config->val('installation', 'path').'/spool/'.$self->{username}.'/config.db';
	return DBI->connect('dbi:SQLite:dbname='.$f, '', '');
} # end sub _Handle

sub test {
	$obj = new Remitt::DataStore::Configuration->new;
} # end sub test

1;
