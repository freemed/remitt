#!/bin/bash
# $Id$
# $Author$
#
#	Script to generate naturaldocs documentation. Assumes that
#	NaturalDocs is installed in /usr/share/naturaldocs.
#

/usr/share/naturaldocs/NaturalDocs -i /root/FMSF/remitt -p doc/naturaldocs/src -o HTML doc/naturaldocs
