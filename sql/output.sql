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

CREATE TABLE output (
	  OID			SERIAL
	, username		VARCHAR( 100 )
	, filename		VARCHAR( 100 ) UNIQUE
	, filesize		INT UNSIGNED NOT NULL DEFAULT 0
	, generated		DATE
	, generated_end		DATE
	, status		INT NOT NULL DEFAULT 0
	, used_format		VARCHAR( 100 )
	, used_transport	VARCHAR( 100 )
	, original_data		BLOB 

	, PRIMARY KEY ( OID )
	, INDEX ( username )
);
