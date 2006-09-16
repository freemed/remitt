#!/usr/bin/perl
#
#	$Id$
#	$Author$
#
# Package: Remitt::Plugin::Transport::GatewayEDI
#
#	GatewayEDI transport plugin. This allows claims to be sent to the
#	gatewayedi.com clearinghouse transparently.
#

package Remitt::Plugin::Transport::GatewayEDI;

use FindBin;
use lib "$FindBin::Bin/../lib/";

use Remitt::Utilities;
use Remitt::DataStore::Configuration;
use Remitt::DataStore::Log;
use File::Temp ();	# comes with Perl 5.8.x
use WWW::Mechanize;
use Data::Dumper;

sub Transport {
	my $input = shift;
	my $username = shift || Remitt::Utilities::GetUsername();
	my $messages;

	my $log = Remitt::DataStore::Log->new;

	$messages .= Remitt::Utilities::DateStamp()." [INFO] starting GatewayEDI billing transmission\n";

	#	Get from configuration datastore
	my $c = Remitt::DataStore::Configuration->new($username);
	my $f_username = $c->GetValue('gatewayedi_username');
	my $f_password = $c->GetValue('gatewayedi_password');

	#	Have to write input to temporary bill file ... name should be stored in "$tempbillfile"
	my $fh = new File::Temp ( UNLINK => 1 );
	my $tempbillfile = $fh->filename;

	#	Put date into file
	$log->Log($username, 3, 'Remitt.Plugin.Transport.GatewayEDI', "Exporting data to file ${tempbillfile}");
	open TEMP, '>'.$tempbillfile or die("$!");
	print TEMP $input;
	close TEMP;
	$log->Log($username, 3, 'Remitt.Plugin.Transport.GatewayEDI', "Finished exporting data successfully");

	#	Fetch logon form
	my $url = 'https://www.gatewayedi.com/gedi/logon.aspx';
	my $m = WWW::Mechanize->new();
	#print " * Getting initial logon page ... ";
	$messages .= Remitt::Utilities::DateStamp()." [INFO] connecting to GatewayEDI\n";
	$log->Log($username, 3, 'Remitt.Plugin.Transport.GatewayEDI', "Getting initial gatewayedi.com login page");
	$m->get($url);

	#	Sanity check
	if ($m->content() !~ /LogonForm/) {
		$log->Log($username, 1, 'Remitt.Plugin.Transport.GatewayEDI', "Failed to get logon form");
		return '';
	}

	#	Submit authentication, etc
	$log->Log($username, 3, 'Remitt.Plugin.Transport.GatewayEDI', "Sending authentication information");
	$m->submit_form(
		form_name => 'LogonForm',
		fields => {
			UserID => $f_username,
			UserPass => $f_password,
		},
		button => 'Button1'
	);

	# Check for failed logon producing new logon form
	if ($m->content() =~ /name="LogonForm"/) {
		#print "Failed to log in ($f_username)\n";
		$messages .= Remitt::Utilities::DateStamp()." [ERROR] failed to log in using account # ${f_username}\n";
		return Remitt::Utilities::StoreContents ( $messages, 'gatewayedi', 'log', $username);
	}
	$log->Log($username, 2, 'Remitt.Plugin.Transport.GatewayEDI', "Logged into gatewayedi.com with username ${f_username} from REMITT acct ${username}");

	#	Upload claim file
	$log->Log($username, 3, 'Remitt.Plugin.Transport.GatewayEDI', "Fetching upload form");
	$m->get('https://www.gatewayedi.com/gedi/SendAndGet/httpupload.aspx?uploadtype=claims');

	$messages .= Remitt::Utilities::DateStamp()." [INFO] transmitting ${tempbillfile} to server\n";
	$log->Log($username, 3, 'Remitt.Plugin.Transport.GatewayEDI', "Uploading ${tempbillfile} to server");
	$m->submit_form(
		form_name => '_ctl0',
		fields => {
			'UploadedFile' => $tempbillfile
		}
	);

	#	Sanity check for whether or not this was received properly
	if ($m->content !~ /was uploaded successfully/) {
		$log->Log($username, 2, 'Remitt.Plugin.Transport.GatewayEDI', "Upload for ${tempbillfile} FAILED!");
		$messages .= Remitt::Utilities::DateStamp()." [ERROR] failed to upload ${tempbillfile}\n";
		return Remitt::Utilities::StoreContents ( $messages, 'gatewayedi', 'log', $username);
	} else {
		$log->Log($username, 2, 'Remitt.Plugin.Transport.GatewayEDI', "Upload for ${tempbillfile} successful.");
	}

	$messages .= Remitt::Utilities::DateStamp()." [INFO] completed GatewayEDI billing transmission\n\n";

	return Remitt::Utilities::StoreContents ( $messages, 'gatewayedi', 'log', $username);
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
	my $data = "DUMMY FILE TEST DATA\n";
	Transport($data, 'test');
} # end sub test

1;

