# $Id$
#
# Authors:
#      Jeff Buchbinder <jeff@freemedsoftware.org>
#
# REMITT Electronic Medical Information Translation and Transmission
# Copyright (C) 1999-2007 FreeMED Software Foundation
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

DROP TABLE IF EXISTS processorqueue;

CREATE TABLE processorqueue (
	  username		VARCHAR( 100 ) NOT NULL
	, data			BLOB
	, render		VARCHAR( 100 )
	, renderoption		VARCHAR( 100 )
	, translation		VARCHAR( 100 )
	, transport		VARCHAR( 100 )
	, unique_id		VARCHAR( 32 )
	, OID			SERIAL

	, INDEX 		( username, unique_id )
);

DROP TABLE IF EXISTS executequeue;

# executequeue is the table created by the processorqueue to mark items
# which are currently being processed.
CREATE TABLE executequeue (
	  username		VARCHAR( 100 ) NOT NULL 
	, unique_id		VARCHAR( 100 ) NOT NULL
	, pOID			INT COMMENT 'processor queue OID'
	, OID			SERIAL
);

DROP TABLE IF EXISTS executestate;

# executestate is an internal table which is cleared on each execution
# which tracks the internal thread states
CREATE TABLE executestate (
	  thread_id		INT UNSIGNED NOT NULL
	, queue_id		INT UNSIGNED NOT NULL
	, type			ENUM ( 'render', 'translation', 'transmission' ) NOT NULL
	, active		BOOL NOT NULL DEFAULT FALSE

	, KEY			( thread_id )
);

DROP PROCEDURE IF EXISTS RemittClearQueues;

DELIMITER //
CREATE PROCEDURE RemittClearQueues ( )
BEGIN
	DELETE FROM executestate;
END//
DELIMITER ;

DROP PROCEDURE IF EXISTS RemittInsertQueue;

DELIMITER //
CREATE PROCEDURE RemittInsertQueue ( IN this_thread_id INT )
BEGIN
	DELETE FROM executestate WHERE thread_id = this_thread_id;
	INSERT INTO executestate ( thread_id ) VALUE ( this_thread_id );
END//
DELIMITER ;

