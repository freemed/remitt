/*
 * $Id$
 *
 * Authors:
 *      Jeff Buchbinder <jeff@freemedsoftware.org>
 *
 * REMITT Electronic Medical Information Translation and Transmission
 * Copyright (C) 1999-2011 FreeMED Software Foundation
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

import java.util.List;

import org.apache.log4j.Logger;
import org.pb.x12.Segment;
import org.remitt.parser.x12dto.Remittance;
import org.remitt.prototype.ParserInterface;
import org.remitt.prototype.SegmentComparator;
import org.remitt.prototype.X12Message;

/**
 * Handle X12 271 eligibility response messages.
 * 
 * @author jeff@freemedsoftware.org
 */
public class X12Message271 extends X12Message implements ParserInterface {

	static final Logger log = Logger.getLogger(X12Message271.class);

	private Remittance remittance = null;

	protected void parseSegments() {
		if (getX12message() == null) {
			log.error("X12 message not set");
			return;
		}
		debug.println("Begin parsing 271");
		position = 1;
		debug.println("Extracting transaction header");
		List<Segment> header = extractLoop(new SegmentComparator[] {
				new SegmentComparator("ISA"), new SegmentComparator("GS"),
				new SegmentComparator("ST"), new SegmentComparator("BHT") });
		debug.println("***** Loop 2000A *****");
		List<Segment> loop2000a = extractLoop(new SegmentComparator[] {
				new SegmentComparator("HL"), new SegmentComparator("AAA"), });
		debug.println("***** Loop 2100A *****");
		List<Segment> loop2100a = extractLoop(new SegmentComparator[] {
				new SegmentComparator("NM1"), new SegmentComparator("PER"),
				new SegmentComparator("AAA") });
		debug.println("***** Loop 2100B *****");
		List<Segment> loop2100b = extractLoop(new SegmentComparator[] {
				new SegmentComparator("HL"), new SegmentComparator("NM1"),
				new SegmentComparator("REF"), new SegmentComparator("N3"),
				new SegmentComparator("N4"), new SegmentComparator("AAA"),
				new SegmentComparator("PRV") });
		debug.println("***** Loop 2100C *****");
		List<Segment> loop2100c = extractLoop(new SegmentComparator[] {
				new SegmentComparator("HL"), new SegmentComparator("TRN"),
				new SegmentComparator("NM1"), new SegmentComparator("REF"),
				new SegmentComparator("N3"), new SegmentComparator("N4"),
				new SegmentComparator("AAA"), new SegmentComparator("PRV"),
				new SegmentComparator("DMG"), new SegmentComparator("INS"),
				new SegmentComparator("HI"), new SegmentComparator("DTP"),
				new SegmentComparator("MPI"), });

		debug.println("***** Loop 2110C *****");
		List<Segment> loop2110c = extractLoop(new SegmentComparator[] {
				new SegmentComparator("EB"), new SegmentComparator("HSD"),
				new SegmentComparator("REF"), new SegmentComparator("DTP"),
				new SegmentComparator("AAA"), new SegmentComparator("MSG") });

		debug.println("***** Loop 2115C *****");
		List<Segment> loop2115c = extractLoop(new SegmentComparator[] {
				new SegmentComparator("III"), new SegmentComparator("LS") });

		debug.println("***** Loop 2120C *****");
		List<Segment> loop2120c = extractLoop(new SegmentComparator[] {
				new SegmentComparator("NM1"), new SegmentComparator("REF"),
				new SegmentComparator("N3"), new SegmentComparator("N4"),
				new SegmentComparator("AAA"), new SegmentComparator("PRV"),
				new SegmentComparator("DMG"), new SegmentComparator("INS"),
				new SegmentComparator("HI"), new SegmentComparator("DTP"),
				new SegmentComparator("MPI"), });

		debug.println("***** Loop 2110D *****");
		List<Segment> loop2110d = extractLoop(new SegmentComparator[] {
				new SegmentComparator("EB"), new SegmentComparator("HSD"),
				new SegmentComparator("REF"), new SegmentComparator("DTP"),
				new SegmentComparator("AAA"), new SegmentComparator("MSG") });

		debug.println("***** Loop 2115D *****");
		List<Segment> loop2115d = extractLoop(new SegmentComparator[] {
				new SegmentComparator("III"), new SegmentComparator("LS") });

		debug.println("***** Loop 2120D *****");
		List<Segment> loop2120d = extractLoop(new SegmentComparator[] {
				new SegmentComparator("NM1"), new SegmentComparator("N3"),
				new SegmentComparator("N4"), new SegmentComparator("PER"),
				new SegmentComparator("PRV") });

		debug.println("Finished parsing 270");
	}

	@Override
	public String parseData(String inputData) throws Exception {
		this.parse(inputData);
		return ""; // TODO: FIXME
	}

}
