#!/usr/bin/perl -w
#
#	$Id$
#	$Author$
#
# Package: Remitt::Plugin::Transport::GatewayEDI
#
#	GatewayEID transport plugin. This allows claims to be sent to the
#	gatewayedi.com clearinghouse transparently.
#

package Remitt::Plugin::Transport::GatewayEDI;

use FindBin;
use lib "$FindBin::Bin/../lib";

use Remitt::Utilities;
use Remitt::DataStore::Configuration;
use File::Temp ();	# comes with Perl 5.8.x
use WWW::Mechanize;
use Data::Dumper;

sub Transport {
	my $input = shift;
	my $username = shift || Remitt::Utilities::GetUsername();

	#	Get from configuration datastore
	my $c = Remitt::DataStore::Configuration->new($username);
	my $f_username = $c->GetValue('gatewayedi_username');
	my $f_password = $c->GetValue('gatewayedi_password');

	#	Have to write input to temporary bill file ... name should be stored in "$tempbillfile"
	my $fh = new File::Temp ( UNLINK => 1 );
	my $tempbillfile = $fh->filename;

	#	Put date into file
	open TEMP, '>'.$tempbillfile or die("$!");
	print TEMP $input;
	close TEMP;

	#	Fetch logon form
	my $url = 'https://www.gatewayedi.com/gedi/logon.aspx';
	my $m = WWW::Mechanize->new();
	#print " * Getting initial logon page ... ";
	$m->get($url);
	#print "[done]\n";

	#	Sanity check
	if ($m->content() !~ /LogonForm/) {
		if (defined($main::log)) {
			$main::log->Log($username, 1, 'Remitt.Plugin.Transport.GatewayEDI', "Failed to get logon form");
		}
		return '';
	}

	#	Submit authentication, etc
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
		print "Failed to log in ($f_username)\n";
		return '';
	}
	if (defined($main::log)) {
		$main::log->Log($username, 2, 'Remitt.Plugin.Transport.GatewayEDI', "Logged into gatewayedi.com with username ${f_username} from REMITT acct ${username}");
	}

	#	Upload claim file
	#print " * Fetching upload form ... ";
	$m->get('https://www.gatewayedi.com/gedi/SendAndGet/httpupload.aspx?uploadtype=claims');
	#print "[done]\n";

	#print " * Uploading $tempbillfile to server ... ";
	#$m->submit_form(
		form_name => '_ctl0',
		fields => {
			'UploadedFile' => $tempbillfile
		}
	);
	#print "[done]\n";

	#	Sanity check for whether or not this was received properly
	if ($m->content !~ /was uploaded successfully/) {
		if (defined($main::log)) {
			$main::log->Log($username, 2, 'Remitt.Plugin.Transport.GatewayEDI', "Logged into gatewayedi.com with username ${f_username} from REMITT acct ${username}");
		}
		return '';
	}

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
	my $data = "DUMMY FILE TEST DATA\n";
	Transport($data, 'test');
} # end sub test

1;

