#!/bin/bash
# $Id$
# $Author$
#
#	Script to generate naturaldocs documentation. Assumes that
#	NaturalDocs is installed in /usr/share/naturaldocs.
#

if [ ! -e ./doc/naturaldocs/src/generate.sh ]; then \
	echo "You need to run this script from the REMITT root directory."; \
	exit; \
fi

# Get minor version number
NV=`perl /usr/share/naturaldocs/NaturalDocs -h | grep version | awk '{ print \$4 }' | grep 1.2`
if [ ! "x${NV}" = "x" ]; then \
	echo "You need to have NaturalDocs version 1.3x installed in /usr/share/naturaldocs/"; \
	exit; \
fi
echo "Found Naturaldocs v$NV"

perl /usr/share/naturaldocs/NaturalDocs -i . -p ./doc/naturaldocs/src -o HTML ./doc/naturaldocs -xi doc -xi .svn

