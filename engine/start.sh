#!/bin/bash
#
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

if [ ! -f bin/RemittEngineService.exe ]; then
	echo "RemittEngineService.exe needs to be built first."
	exit
fi

echo " > Running engine "
echo "    * Removing old service locks (if they exist) "
rm -f /tmp/RemittEngineService.exe.lock
echo "    * Running RemittEngineService ... "
mono \
	--runtime=v2.0.50727 \
	/usr/lib/mono/2.0/mono-service.exe \
		-d:bin \
		-m:RemittEngineService \
		-l:/tmp/RemittEngineService.exe.lock \
		RemittEngineService.exe

