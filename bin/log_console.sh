#!/bin/bash
#
#	$Id$
#	jeff@freemedsoftware.org
#

if [ ! -x "$(which sqlite3)" ]; then
	echo "sqlite3 binary required"
	exit
fi

if [ ! -r "spool/log.db" ]; then
	echo "You either don't have permission to read spool/log.db, or you are running"
	echo "this from a place other than the REMITT root directory (which is usually"
	echo "/usr/share/remitt) ... "
	exit
fi

watch --interval=2 'sqlite3 spool/log.db "SELECT stamp,username,method,message FROM log ORDER BY OID DESC LIMIT 25"'
