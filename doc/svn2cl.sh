#!/bin/bash
# $Id$
# $Author$
#
#	Script to generate Changelog from Subversion XML output.
#

if [ ! -e ./doc/svn2cl.sh ]; then \
	echo "You need to run this script from the REMITT root directory."; \
	exit; \
fi

svn log -r HEAD:0 --xml --verbose | xsltproc --stringparam strip-prefix dir/subdir doc/svn2cl.xsl - > doc/ChangeLog
