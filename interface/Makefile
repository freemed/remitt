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

all: clean 3rdparty bin/Remitt.Interface.dll

bin/Remitt.Interface.dll:
	@echo " > Compiling $@ "
	@echo "    * Building $@"
	@gmcs \
		-r:System,System.Web,System.Web.Services \
		-r:../3rdparty/log4net.dll \
		-r:../3rdparty/MySql.Data.dll \
		-r:../library/bin/Remitt.Configuration.dll \
		src/Remitt.Interface.cs \
		-t:library \
		-out:bin/Remitt.Interface.dll

3rdparty:
	@echo " > Installing 3rd party libraries "
	@cp ../3rdparty/*.dll ../library/bin/*.dll bin/ -f

clean:
	@echo " > Cleaning "
	@echo "    * Cleaning old build directories and files"
	@rm -f bin/*

