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

SERVICE_SOURCES= \
	src/Installer.cs \
	src/EngineService.cs

DEPENDENCIES= \
	-r:System.Configuration.Install \
	-r:System.Data \
	-r:GotDotNet.Exslt \
	-r:System.ServiceProcess \
	-r:MySql.Data

LIBS= \
	-lib:../3rdparty \
	-lib:../library/bin \
	-r:../library/bin/Remitt.Configuration.dll \
	-r:../library/bin/Remitt.PluginManager.dll \
	-r:../3rdparty/itextsharp.dll \
	-r:../3rdparty/log4net.dll

all: clean .targets

.targets: bin/PluginTestHarness.exe bin/RemittEngineService.exe plugins

bin/RemittEngineService.exe:
	@echo " > Compiling engine "
	@echo "    * Building $@"
	@gmcs \
		-out:$@ \
		$(LIBS) \
		$(DEPENDENCIES) \
		$(SERVICE_SOURCES)

bin/PluginTestHarness.exe:
	@echo " > Compiling test harness for plugins "
	@echo "    * Building $@"
	@gmcs \
		-out:$@ \
		$(LIBS) \
		src/PluginTestHarness.cs

plugins:
	@echo " > Compiling plugins "
	@echo "    * Building plugins"
	@for i in src/plugins/Plugin.*.cs; do \
		j=bin/plugins/`basename $$i | sed -e 's|cs|dll|;'`; \
		echo "        - Compiling $$i -> "; \
		echo "                    $$j ... "; \
		gmcs \
			-t:library \
			$(LIBS) \
			$(DEPENDENCIES) \
			-out:$$j $$i; \
	done

clean:
	@echo " > Cleaning "
	@echo "    * Cleaning old build directories and files"
	@rm -f bin/*.exe bin/*.dll bin/plugins/*.dll

test: .targets
	@echo " > Running engine "
	@./start.sh

