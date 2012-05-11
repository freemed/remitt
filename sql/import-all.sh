#!/bin/bash
#
# $Id$
#
# Authors:
# 	Jeff Buchbinder <jeff@freemedsoftware.org>
#
# REMITT Electronic Medical Information Translation and Transmission
# Copyright (C) 1999-2012 FreeMED Software Foundation
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

WORKING=$( dirname "$0" )

echo " - Creating MySQL account for REMITT user (please enter password"
echo "   for root user when prompted, otherwise hit \"enter\"):"
mysql -uroot -p -e "DROP DATABASE IF EXISTS remitt; CREATE DATABASE remitt; GRANT ALL ON remitt.* TO remitt@localhost IDENTIFIED BY 'remitt'; FLUSH PRIVILEGES;"

for s in $WORKING/*.sql; do
	echo " - Importing $(basename $s) : "
	mysql -uremitt -premitt remitt -e "SOURCE ${s};"
done

