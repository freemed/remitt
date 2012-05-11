/*
 * $Id$
 *
 * Authors:
 *      Jeff Buchbinder <jeff@freemedsoftware.org>
 *
 * REMITT Electronic Medical Information Translation and Transmission
 * Copyright (C) 1999-2012 FreeMED Software Foundation
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

package org.remitt.prototype;

import java.io.PrintWriter;
import java.util.Arrays;

import org.pb.x12.Segment;

public class SegmentComparator {
	protected String[] qualifiers = new String[] {};
	protected Integer qualifierSegment = 1;
	protected String segmentIdentifier = "";
	protected PrintWriter debug = null;

	public SegmentComparator(String sId) {
		segmentIdentifier = sId;
		qualifierSegment = null;
		qualifiers = null;
	}

	public SegmentComparator(String sId, Integer qPos, String[] q) {
		segmentIdentifier = sId;
		qualifierSegment = qPos;
		qualifiers = q;
	}

	public PrintWriter getDebug() {
		return debug;
	}

	public void setDebug(PrintWriter d) {
		debug = d;
	}

	public boolean check(Segment s) {
		if (s == null || segmentIdentifier == null) {
			return false;
		}

		// Deal with simple cases first
		if (qualifierSegment == null || qualifiers == null) {
			if (segmentIdentifier.equals(s.getElement(0))) {
				return true;
			} else {
				return false;
			}
		}
		// More complicated selection checking now
		if (segmentIdentifier.equals(s.getElement(0))
				&& Arrays.asList(qualifiers).contains(
						s.getElement(qualifierSegment))) {
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		if (qualifierSegment == null || qualifiers == null) {
			return "Compare(" + segmentIdentifier + ")";
		} else {
			return "Compare(" + segmentIdentifier + "," + qualifiers.toString()
					+ ")";
		}
	}

}
