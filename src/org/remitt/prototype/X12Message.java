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

package org.remitt.prototype;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.pb.x12.FormatException;
import org.pb.x12.Parser;
import org.pb.x12.Segment;
import org.pb.x12.X12;

abstract public class X12Message {

	static final Logger log = Logger.getLogger(X12Message.class);

	protected StringWriter _debug = new StringWriter();
	protected PrintWriter debug = new PrintWriter(_debug);

	private X12 x12message = null;
	private int x12segmentCount = 0;
	protected int position = 0;

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
				return "Compare(" + segmentIdentifier + ","
						+ qualifiers.toString() + ")";
			}
		}
	}

	public X12Message() {
	}

	public X12Message(String rawMessage) throws FormatException {
		parse(rawMessage);
	}

	public void parse(String rawMessage) throws FormatException {
		Parser parser = new Parser();
		try {
			setX12message(parser.parse(rawMessage));
		} catch (IOException e) {
			log.error(e);
		}
		parseSegments();
	}

	/**
	 * Callback in inheirited classes to process X12 messages once assigned.
	 */
	abstract protected void parseSegments();

	/**
	 * @param x12message
	 *            the x12message to set
	 */
	public void setX12message(X12 x12message) {
		this.x12message = x12message;

		// Count number of segments during assignment
		this.position = 0;
		this.x12segmentCount = 0;
		Iterator<Segment> iter = this.x12message.iterator();
		while (iter.hasNext()) {
			@SuppressWarnings("unused")
			Segment thisSegment = iter.next();
			this.x12segmentCount++;
		}
	}

	/**
	 * @return the original x12message
	 */
	public X12 getX12message() {
		return x12message;
	}

	public int getSegmentCount() {
		return x12segmentCount;
	}

	/**
	 * Determine if the next segment identifier at indicated position equals
	 * identifier sId.
	 * 
	 * @param sId
	 * @return
	 */
	protected boolean isNextSegmentIdentifier(String sId) {
		return getX12message().getSegment(position).getElement(0).equals(sId);
	}

	/**
	 * Extract a list of X12 <Segment> elements from an X12 message based on a
	 * list of allowed segments for the current loop.
	 * 
	 * @param loopSegments
	 * @return
	 */
	protected List<Segment> extractLoop(SegmentComparator[] loopSegments) {
		List<Segment> segments = new ArrayList<Segment>();
		debug.println("Extract loop starting at position " + position);
		while (checkSegmentForComparators(getX12message().getSegment(position),
				loopSegments)) {
			// Log parsing information
			String segmentType = getX12message().getSegment(position)
					.getElement(0);
			if (segmentType.length() >= 2) {
				log.info("Found " + segmentType + " segment");
				debug.println("Found " + segmentType + " segment");
			}

			// If this is an allowable segment, continue
			segments.add(getX12message().getSegment(position));

			// Increment position to next segment
			position++;

			// Make sure that we're not over the allowable limit
			if (position > getSegmentCount()) {
				continue;
			}
		}
		debug.println("Extract loop ending at position " + position);
		return segments;
	}

	public boolean checkSegmentForComparators(Segment s, SegmentComparator[] c) {
		// debug.println(" -- [Segment id = " + s.getElement(0) + "]");
		for (int iter = 0; iter < c.length; iter++) {
			// Ensure that debug object is passed
			if (c[iter].getDebug() == null) {
				c[iter].setDebug(debug);
			}
			if (c[iter].check(s)) {
				// debug.println(" -- [Found segment for comparator " +
				// c[iter].toString() + "]");
				return true;
			} else {
				// debug.println(" -- Didn't find segment for comparator " +
				// c[iter].toString());
			}
		}
		return false;
	}

	public String getDebugLog() {
		return _debug.getBuffer().toString();
	}

	@Override
	public String toString() {
		return getDebugLog();
	}

}
