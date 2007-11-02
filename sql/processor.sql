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

CREATE TABLE processorqueue (
	  username		VARCHAR( 100 ) NOT NULL
	, data			BLOB
	, render		VARCHAR( 100 )
	, renderoption		VARCHAR( 100 )
	, translation		VARCHAR( 100 )
	, transport		VARCHAR( 100 )
	, unique_id		VARCHAR( 32 )

	, INDEX 		( username, unique_id )
);

CREATE TABLE executequeue (
	  username		VARCHAR( 100 ) NOT NULL 
	, unique_id		VARCHAR( 100 ) NOT NULL
);

