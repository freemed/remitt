#!/bin/bash
# $Id$
#
# Authors:
# 	Jeff Buchbinder <jeff@freemedsoftware.org>
#
# REMITT Electronic Medical Information Translation and Transmission
# Copyright (C) 1999-2008 FreeMED Software Foundation
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

OUR_PATH="$( cd "$( dirname "$0" )/../" ; pwd )" 
export MONO_PATH=${OUR_PATH}/3rdparty:${OUR_PATH}/library/bin
xsp2 \
	--appconfigfile Web.config \
	--applications /:. \
	--port 7688 \
	--verbose

