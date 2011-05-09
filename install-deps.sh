#!/bin/bash
# $Id$
#
# Authors:
#      Jeff Buchbinder <jeff@freemedsoftware.org>
#
# REMITT Electronic Medical Information Translation and Transmission
# Copyright (C) 1999-2011 FreeMED Software Foundation
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

mvn install:install-file -DgroupId=cron4j -DartifactId=cron4j -Dversion=2.2.2 -Dpackaging=jar -Dfile=dependencies/cron4j-2.2.2.jar
mvn install:install-file -DgroupId=j2ssh -DartifactId=j2ssh-core -Dversion=0.2.9 -Dpackaging=jar -Dfile=dependencies/j2ssh-core-0.2.9.jar
mvn install:install-file -DgroupId=j2ssh -DartifactId=j2ssh-common -Dversion=0.2.9 -Dpackaging=jar -Dfile=dependencies/j2ssh-common-0.2.9.jar
mvn install:install-file -DgroupId=junitee -DartifactId=junitee -Dversion=1.11 -Dpackaging=jar -Dfile=dependencies/junitee-1.11.jar
mvn install:install-file -DgroupId=sojo -DartifactId=sojo -Dversion=1.0.0 -Dpackaging=jar -Dfile=dependencies/sojo-1.0.0.jar
mvn install:install-file -DgroupId=x12 -DartifactId=x12 -Dversion=0.6 -Dpackaging=jar -Dfile=dependencies/X12-0.6.jar

