#!/usr/bin/perl
#
#	$Id$
#	$Author$
#

package Remitt::Utilities;

use Remitt::Session;
use MIME::Base64;
use Data::Dumper;

sub Authenticate {
	my ($hash) = @_;
	my ($sessionid, $key) = split /:/, decode_base64($hash);
	
	# Get rid of trailing slash
	$key =~ s/\\$//;

	#print "[Utilities::Authenticate] sessionid = $sessionid, key = $key\n";

	# Check the session to see if we are authenticated or not
	#print "Authenticate: opening session $sessionid ... ";
	my $session = Remitt::Session->new($sessionid);
	my $load = $session->load();
	#print Dumper($session->{session});
	#print "[Utilities::Authenticate] key passed from client = '$key'\n";

	# Get passkey
	my $passkey = $session->{session}->param('passkey');
	#print "[Utilities::Authenticate] ".Dumper($session->{session})."\n";

	#print "[auth present]\n" if ($session->{session}->param('authenticated'));
	#print "[key matches]\n" if ($session->{session}->param('passkey') == $key);
	#print "\n\npass key = ".$passkey."\n";
	#print "key = ".$key."\n";
	#print "[stored key = ".$session->{session}->param('passkey').", my key = $key]\n";

	return +(
		$session->{session}->param('authenticated') and ($session->{session}->param('passkey') == $key),
		$session->{session}->param('username')
	);
} # end sub Authenticate

sub Fault {
	die ("Authentication failed. User not logged in");
} # end sub Fault

sub ForceAuthentication {
	my (undef, $authstring) = split / /, $ENV{'HTTP_authorization'};
	my ($auth, $sessionid) = Remitt::Utilities::Authenticate($authstring);
	Remitt::Utilities::Fault() if (!$auth);
	return 1;
} # end sub ForceAuthentication

1;
