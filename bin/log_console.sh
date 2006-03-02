#!/bin/bash
#
#	$Id$
#	jeff@freemedsoftware.org
#

if [ ! -x "$(which sqlite3)" ]; then
	echo "sqlite3 binary required"
	exit
fi

watch --interval=2 'sqlite3 spool/log.db "SELECT stamp,username,method,message FROM log ORDER BY OID DESC LIMIT 25"'
