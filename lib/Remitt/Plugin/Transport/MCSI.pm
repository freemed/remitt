#!/usr/bin/perl -w
#
#	$Id$
#	$Author: jeff $
#
# Package: Remitt::Plugin::Transport::MCSI
#
#	MCSI transport plugin. The functions and checks are pulled from
#	the FreeB module that was written for the same purpose. This is
#	fine, as I was the author of that module, and it is quite GPL'd.
#

package Remitt::Plugin::Transport::MCSI;

use Remitt::Utilities;
use File::Temp ();	# comes with Perl 5.8.x
use Data::Dumper;

# Method: _curl_open
#
# 	Wrap command-line cURL executable, since Perl curl extension
# 	does not work properly when posting files with authentication.
#
# Parameters:
#
# 	$url - URL to contact
#
# 	%param - Hash of parameters to be passed.
#
# Returns:
#
# 	Contents of target webpage.
#
sub _curl_open {
	my $url = shift;
	my $_param = shift; my %param = %{$_param};

	my $config = Remitt::Utilities::Configuration ( );

	# Get user name and put in '$user' variable
	my $username = Remitt::Utilities::GetUsername();

	my $parameters;
	foreach $key (keys %param) {
		#print "adding key = $key, value = ".$param{$key}."\n";
		$parameters .= '-F "' . $key . '=' . $param{$key} . '" ';
	}

	my $cmd = 'curl '.
		'--cookie-jar '.$config->val('installation', 'path').'/spool/MCSI_COOKIEJAR '.
		'--connect-timeout 30 '.
		'--silent '.
		'--referer http://trymcs.com/mainssl.html '.
		'--user '.
			$config->val('mcsi', $user.'-username') . ':' .
			$config->val('mcsi', $user.'-password') . ' ' .
		$parameters . ' ' .
		$url;

	$value = `$cmd`;
	return $value;
} # end method _curl_open

sub Transport {
	my ( $input ) = @_;

	# Here's the cluster-fsck ...
	#
	# 	When dealing with a multi-user environment, there isn't a
	# 	good way to centrally store the MCSI username and password
	# 	that is to be used. Going to hack a temporary solution, but
	# 	that isn't going to fly with HIPAA regulations, methinks.

	# Have to write input to temporary bill file ...
	# name should be stored in "$tempbillfile"
	my $fh = new File::Temp ( UNLINK => 1 );
	my $tempbillfile = $fh->filename;

	# Put date into file
	open TEMP, '>'.$tempbillfile or die("$!");
	print TEMP $input;
	close TEMP;

	# Login
	my $login = _curl_open('https://trymcs.com/cgi-win/cgi.plc', 
		{'function' => '03'});
	if (!($login =~ /Authentication Received/)) {
		syslog('notice', 'MCSI transport| Authentication failed');
		return '';
	} else {
		syslog('notice', 'MCSI transport| Authentication succeeded');
	}

	# Submit the actual information. MCSI does not care whether it is HCFA,
	# X12, or moon writing... it is all transmitted through the same form.
	my $submittal = _curl_open('https://trymcs.com/cgi-win/cgi.plc',
		{
			'function' => '01',
			'intype' => '1',
			'filenami' => '@'.$tempbillfile
		}
	);
	if ($submittal =~ /Error in Uploading a file/i) {
		syslog('notice', 'MCSI transport| An error occurred while trying to upload the claims');
		return '';
	} else {
		syslog('notice', 'MCSI transport| Claim file received');
	}

	return Remitt::Utilities::StoreContents ( $input, 'plaintext', 'txt');
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
	print "\nNo test for MCSI transport yet ... \n";
	print "\n---\n";
} # end sub test

1;

