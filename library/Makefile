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

LIBS= \
	bin/Remitt.AuthenticationProvider.dll \
	bin/Remitt.Configuration.dll \
	bin/Remitt.PluginManager.dll

all: clean .targets

.targets: $(LIBS)

bin/Remitt.AuthenticationProvider.dll: bin/Remitt.Configuration.dll
	@echo " > Compiling $@ "
	@echo "    * Building $@"
	@gmcs \
		-out:$@ \
		-target:library \
		-r:../3rdparty/log4net.dll \
		-r:../3rdparty/MySql.Data.dll \
		-r:System.Data \
		-r:System.Web \
		-r:bin/Remitt.Configuration.dll \
		src/Remitt.AuthenticationProvider.cs \
		src/Remitt.BaseAuthenticationModule.cs

bin/Remitt.Configuration.dll:
	@echo " > Compiling $@ "
	@echo "    * Building $@"
	@gmcs \
		-out:$@ \
		-target:library \
		-r:System.Data \
		-r:../3rdparty/log4net.dll \
		src/Remitt.Configuration.cs

bin/Remitt.PluginManager.dll:
	@echo " > Compiling $@ "
	@echo "    * Building $@"
	@gmcs \
		-out:$@ \
		-target:library \
		-r:../3rdparty/log4net.dll \
		-r:System.Data \
		-r:bin/Remitt.Configuration.dll \
		src/Remitt.PluginManager.cs

clean:
	@echo " > Cleaning "
	@echo "    * Cleaning old build directories and files"
	@rm -f bin/*.dll

