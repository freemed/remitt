/*
 * $Id$
 *
 * Authors:
 *      Jeff Buchbinder <jeff@freemedsoftware.org>
 *
 * REMITT Electronic Medical Information Translation and Transmission
 * Copyright (C) 1999-2010 FreeMED Software Foundation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package org.remitt.parser;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.pb.x12.Segment;
import org.remitt.prototype.X12Message;

public class X12Message835 extends X12Message {

	static final Logger log = Logger.getLogger(X12Message835.class);

	protected String parsedOutput = "";

	protected void parseSegments() {
		if (getX12message() == null) {
			log.error("X12 message not set");
			return;
		}
		Iterator<Segment> iter = getX12message().iterator();
		while (iter.hasNext()) {
			Segment s = iter.next();
			String segmentType = s.getElement(0);
			if (segmentType.length() >= 2) {
				log.info("Found " + segmentType + " segment");
				parsedOutput += "Found " + segmentType + " segment\n";
			}
		}
	}

	@Override
	public String toString() {
		return parsedOutput;
	}

}
