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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.pb.x12.FormatException;
import org.pb.x12.Parser;
import org.pb.x12.Segment;
import org.pb.x12.X12;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

/**
 * X12Message wraps the parsing and serialization of <X12> with additional
 * <Segment> comparison and <Element> extraction methods to allow programmatic
 * access and query to X12 data. It is meant to be extended to handle various
 * message types.
 * 
 * @author jeff@freemedsoftware.org
 */
abstract public class X12Message {

	static final Logger log = Logger.getLogger(X12Message.class);

	protected StringWriter _debug = new StringWriter();
	protected PrintWriter debug = new PrintWriter(_debug);

	private X12 x12message = null;
	private int x12segmentCount = 0;
	protected int position = 0;

	private static SimpleDateFormat x12dateFormat = new SimpleDateFormat(
			"yyyyMMdd");

	public X12Message() {
	}

	/**
	 * Create new <X12Message> object with message text.
	 * 
	 * @param rawMessage
	 *            X12 message text
	 * @throws FormatException
	 */
	public X12Message(String rawMessage) throws FormatException {
		parse(rawMessage);
	}

	/**
	 * Parse the <String> representation of an <X12> message into the
	 * appropriate <X12> object.
	 * 
	 * @param rawMessage
	 * @throws FormatException
	 */
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
	 * Use a <SegmentComparator> to find the first matching X12 <Segment>.
	 * 
	 * @param segments
	 * @param c
	 * @return
	 */
	public static Segment findSegmentByComparator(List<Segment> segments,
			SegmentComparator c) {
		Iterator<Segment> iter = segments.iterator();
		while (iter.hasNext()) {
			Segment s = iter.next();
			if (c.check(s)) {
				return s;
			}
		}
		return null;
	}

	/**
	 * Use a <SegmentComparator> to find matching X12 segments in a <List> of
	 * <Segment> objects.
	 * 
	 * @param segments
	 * @param c
	 * @return
	 */
	public static List<Segment> findSegmentsByComparator(
			List<Segment> segments, SegmentComparator c) {
		List<Segment> results = new ArrayList<Segment>();
		Iterator<Segment> iter = segments.iterator();
		while (iter.hasNext()) {
			Segment s = iter.next();
			if (c.check(s)) {
				results.add(s);
			}
		}
		return results;
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

	/**
	 * Get number of segments in the current <X12> message.
	 * 
	 * @return
	 */
	public int getSegmentCount() {
		return x12segmentCount;
	}

	/**
	 * Static method to create XML serialization of <X12DTO> object.
	 * 
	 * @param dto
	 * @return
	 */
	public static String serializeDTO(X12DTO dto) {
		Serializer serializer = new Persister();
		StringWriter stringWriter = new StringWriter();
		try {
			serializer.write(dto, stringWriter);
			stringWriter.flush();
		} catch (Exception e) {
			log.error(e);
		}
		return stringWriter.getBuffer().toString();
	}

	/**
	 * "Safe" method for getting text of an element from a <Segment> without
	 * worrying about catching Exceptions.
	 * 
	 * @param segment
	 * @param position
	 * @return <String> for the position or null if it can't be found.
	 */
	public static String getSafeElement(Segment segment, int position) {
		if (segment == null) {
			return null;
		}
		try {
			return segment.getElement(position);
		} catch (Exception ex) {
		}
		return null;
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
	 * Determine if the next segment at indicated position matches using a
	 * <SegmentComparator>.
	 * 
	 * @param sC
	 * @return
	 */
	protected boolean isNextSegmentIdentifier(SegmentComparator sC) {
		return sC.check(getX12message().getSegment(position));
	}

	/**
	 * Determine if the next segment identifier at indicated position equals any
	 * of the identifiers in the sIds array.
	 * 
	 * @param sIds
	 * @return
	 */
	protected boolean isNextSegmentIdentifier(String[] sIds) {
		for (int iter = 0; iter < sIds.length; iter++) {
			if (getX12message().getSegment(position).getElement(0).equals(
					sIds[iter])) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Determine if the next segment at indicated position is matched by any of
	 * the <SegmentComparator> objects in the sIds array.
	 * 
	 * @param sCs
	 * @return
	 */
	protected boolean isNextSegmentIdentifier(SegmentComparator[] sCs) {
		for (int iter = 0; iter < sCs.length; iter++) {
			if (sCs[iter].check(getX12message().getSegment(position))) {
				return true;
			}
		}
		return false;
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
				log
						.info("Found "
								+ segmentType
								+ " segment ["
								+ getX12message().getSegment(position)
										.toString() + "]");
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

	/**
	 * Determine if a <Segment> matches ones or more <SegmentComparator>
	 * objects' functionality.
	 * 
	 * @param s
	 *            <Segment> to examine
	 * @param c
	 *            Array of <SegmentComparator> objects
	 * @return true if a match is found, false if none
	 */
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

	/**
	 * Parse X12 formatted date.
	 * 
	 * @param date
	 * @return
	 */
	public static Date parseDate(String date) {
		try {
			return x12dateFormat.parse(date);
		} catch (ParseException ex) {
			log.error(ex);
			return null;
		}
	}

	public String getDebugLog() {
		return _debug.getBuffer().toString();
	}

	@Override
	public String toString() {
		return getDebugLog();
	}

}
