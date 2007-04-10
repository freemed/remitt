#!/bin/bash
#
#	$Id$
#	$Author$
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

