#!/usr/bin/perl
#
#	$Id$
#	$Author$
#
# Package: Remitt::Plugin::Transport::FreeClaims
#
#	FreeClaims transport plugin. This allows claims to be sent to the
#	freeclaims.com clearinghouse transparently.
#

package Remitt::Plugin::Transport::FreeClaims;

use FindBin;
use lib "$FindBin::Bin/../../../";

use Remitt::Utilities;
use Remitt::DataStore::Configuration;
use Remitt::DataStore::Log;
use File::Temp ();	# comes with Perl 5.8.x
use WWW::Mechanize;
use Data::Dumper;

sub Transport {
	my $input = shift;
	my $username = shift || Remitt::Utilities::GetUsername();

	# Get from configuration datastore
	my $c = Remitt::DataStore::Configuration->new($username);
	my $f_username = $c->GetValue('freeclaims_username');
	my $f_password = $c->GetValue('freeclaims_password');

	# Have to write input to temporary bill file ...
	# name should be stored in "$tempbillfile"
	my $fh = new File::Temp ( UNLINK => 1 );
	my $tempbillfile = $fh->filename;
	$log->Log($username, 3, 'Remitt.Plugin.Transport.FreeClaims', "Exporting data to file ${tempbillfile}");

	# Put date into file
	open TEMP, '>'.$tempbillfile or die("$!");
	print TEMP $input;
	close TEMP;
	$log->Log($username, 3, 'Remitt.Plugin.Transport.FreeClaims', "Finished exporting data successfully");

	# Login to FreeClaims
	my $url = 'https://secure.freeclaims.com/docs/login.asp';
	my $m = WWW::Mechanize->new();
	$log->Log($username, 3, 'Remitt.Plugin.Transport.FreeClaims', "Fetching initial logon page");
	$m->get($url);

	$log->Log($username, 3, 'Remitt.Plugin.Transport.FreeClaims', "Logging in with account ${f_username}");
	$m->submit_form(
		form_name => 'loginForm',
		fields => {
			username => $f_username,
			userpassword => $f_password
		}
	);
	$log->Log($username, 3, 'Remitt.Plugin.Transport.FreeClaims', "Logged in successfully");

	# Upload claim file
	$log->Log($username, 3, 'Remitt.Plugin.Transport.FreeClaims', "Fetching upload form");
	$m->get('https://secure.freeclaims.com/docs/upload.asp');
	
	print " * Uploading $tempbillfile to server ... ";
	$log->Log($username, 3, 'Remitt.Plugin.Transport.FreeClaims', "Uploading ${tempbillfile} to the server");
	$m->submit_form(
		form_name => 'Upload',
		fields => {
			file1 => $tempbillfile
		}
	);

	return Remitt::Utilities::StoreContents ( $input, 'plaintext', 'txt', $username);
} # end method Transport

# Method: Config
#
# 	Return configuration hash for this plugin.
#
# Returns:
#
# 	Hash containing the configuration information
#
sub Config {
	return +{
		'InputFormat' => [ 'text', 'x12' ]
	};
} # end sub Config

sub test {
	my $data = "DUMMY FILE TEST DATA\n" x 20;
	Transport($data, 'test');
} # end sub test

1;

