#!/usr/bin/env php
<?php
/*
 * $Id$
 *
 * Authors:
 *      Jeff Buchbinder <jeff@freemedsoftware.org>
 *
 * REMITT Electronic Medical Information Translation and Transmission
 * Copyright (C) 1999-2014 FreeMED Software Foundation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

$username = 'Administrator';
$password = 'password';

$url = "http://localhost:8080/remitt/services/interface";
$wsdl = $url . "?wsdl";
$sc = new SoapClient( $wsdl, array (
	  'login' => $username
	, 'password' => $password
	, 'compression' => SOAP_COMPRESSION_ACCEPT
	, 'location' => $url
));

print "getProtocolVersion : \n";
print_r( $sc->getProtocolVersion() );
print "\n";

print "getCurrentUserName : \n";
print_r( $sc->getCurrentUserName() );
print "\n";

print "getConfigValues: \n";
print_r( $sc->getConfigValues() );
print "\n";

print "parseData(835): \n";
$a = array (
	  'parserClass' => 'org.remitt.parser.X12Message835'
	, 'data' => file_get_contents( dirname(__FILE__) . '/835.x12' )
);
print_r( $sc->parseData((object)$a) );
print "\n";

?>
