#!/usr/bin/perl
#
# $Id$
#
# Authors:
#      Jeff Buchbinder <jeff@freemedsoftware.org>
#
# REMITT Electronic Medical Information Translation and Transmission
# Copyright (C) 1999-2009 FreeMED Software Foundation
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.

# Package: Remitt::Plugin::Transport::FreeClaims
#
#	FreeClaims transport plugin. This allows claims to be sent to the
#	freeclaims.com clearinghouse transparently.
#

package Remitt::Plugin::Transport::FreeClaims;

use FindBin;
use lib "$FindBin::Bin/../lib/";

use Remitt::Utilities;
use Remitt::DataStore::Configuration;
use Remitt::DataStore::Log;
use Remitt::DataStore::Output;
use File::Temp ();	# comes with Perl 5.8.x
use WWW::Mechanize;
use Data::Dumper;

sub Transport {
	my $input = shift;
	my $username = shift || Remitt::Utilities::GetUsername();
	my $id = shift || 0;

	my $log = Remitt::DataStore::Log->new;
	my $messages = "";

	$messages .= Remitt::Utilities::DateStamp()." [INFO] Beginning FreeClaims billing transmission run\n";

	# Get from configuration datastore
	my $c = Remitt::DataStore::Configuration->new($username);
	my $f_username = $c->GetValue('freeclaims_username');
	my $f_password = $c->GetValue('freeclaims_password');

	# Have to write input to temporary bill file ...
	# name should be stored in "$tempbillfile"
	my $fh = new File::Temp ( UNLINK => 1 );
	my $tempbillfile = $fh->filename;
	$log->Log($username, 3, 'Remitt.Plugin.Transport.FreeClaims', "Exporting data to file ${tempbillfile}");
	$messages .= Remitt::Utilities::DateStamp()." [INFO] Creating temporary file ${tempbillfile}\n";

	# Put date into file
	open TEMP, '>'.$tempbillfile or die("$!");
	print TEMP $input;
	close TEMP;
	$log->Log($username, 3, 'Remitt.Plugin.Transport.FreeClaims', "Finished exporting data successfully");

	# Login to FreeClaims
	my $url = 'https://sfreeclaims.anvicare.com/docs/member_login.asp';
	my $m = WWW::Mechanize->new();
	$m->agent_alias( 'Windows IE 6' );
	$log->Log($username, 3, 'Remitt.Plugin.Transport.FreeClaims', "Fetching initial logon page");
	$m->get($url);
	$messages .= Remitt::Utilities::DateStamp()." [INFO] Connected to freeclaims server\n";

	$log->Log($username, 3, 'Remitt.Plugin.Transport.FreeClaims', "Logging in with account ${f_username}");
	$m->submit_form(
		form_name => 'loginForm',
		fields => {
			username => $f_username,
			userpassword => $f_password
		}
	);
	if ($m->content =~ /login attempt was not/) {
		$messages .= Remitt::Utilities::DateStamp()." [ERROR] failed to login to FreeClaims site using provided credentials.\n";
		return Remitt::Utilities::StoreContents ( $messages, 'freeclaims', 'log', $username);
	}
	$log->Log($username, 3, 'Remitt.Plugin.Transport.FreeClaims', "Logged in successfully");
	$messages .= Remitt::Utilities::DateStamp()." [INFO] logged into Freeclaims site using account # ${f_username}\n";

	# Upload claim file
	$log->Log($username, 3, 'Remitt.Plugin.Transport.FreeClaims', "Fetching upload form");
	$m->get('https://sfreeclaims.anvicare.com/docs/upload.asp');
	
	$messages .= Remitt::Utilities::DateStamp()." [INFO] beginning transmission of file to FreeClaims\n";
	$log->Log($username, 3, 'Remitt.Plugin.Transport.FreeClaims', "Uploading ${tempbillfile} to the server");
	$m->submit_form(
		form_name => 'Upload',
		fields => {
			file1 => $tempbillfile
		}
	);
	if ($m->content =~ />([0-9_]+\.EMC)</) {
		my $foreign_id = $1;
		$messages .= Remitt::Utilities::DateStamp()." [INFO] ".length($input)." bytes sent to freeclaims as ${foreign_id}\n";
		# If there's an id passed here, push as foreign_id
		if ( $id > 0 ) {
			my $ds = Remitt::DataStore::Output->new($username);
			$ds->SetForeignId( $id, $foreign_id );
		}
	} else {
		$messages .= Remitt::Utilities::DateStamp()." [INFO] ".length($input)." bytes FAILED transmission to freeclaims\n"; 
	}

	$messages .= Remitt::Utilities::DateStamp()." [INFO] completed FreeClaims billing transmission\n\n";

	return Remitt::Utilities::StoreContents ( $messages, 'freeclaims', 'log', $username);
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

