#!/bin/bash
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

#
#	Syntax: ./test/test_dependencies.sh
#
if [ ! -d /usr/share/remitt ]; then
	echo "REMITT must be installed in /usr/share/remitt to run this script."
	exit 1
fi

cd "$(dirname "$0")/.."
for p in lib/Remitt/Plugin/*; do
	for i in lib/Remitt/Plugin/$(basename "$p")/*.pm; do
		X=$(basename "$i"); X=${X//.pm/}
		echo -n "Loading ${i} : "
		perl -I./lib -MRemitt::Plugin::$(basename "$p")::${X} -e "print 1;"
		echo " "
	done
done

