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

import org.apache.commons.collections.ListUtils;
import org.apache.log4j.Logger;
import org.pb.x12.Segment;
import org.remitt.parser.x12dto.FunctionalAcknowledgement;
import org.remitt.parser.x12dto.TransactionSet;
import org.remitt.prototype.ParserInterface;
import org.remitt.prototype.SegmentComparator;
import org.remitt.prototype.X12Message;

/**
 * Handle X12 997 functional response messages.
 * 
 * @author jeff@freemedsoftware.org
 */
public class X12Message997 extends X12Message implements ParserInterface {

	static final Logger log = Logger.getLogger(X12Message997.class);

	protected FunctionalAcknowledgement functionalAcknowledgement = null;

	@SuppressWarnings("unchecked")
	protected void parseSegments() {
		if (getX12message() == null) {
			log.error("X12 message not set");
			return;
		}
		debug.println("Begin parsing 997");

		functionalAcknowledgement = new FunctionalAcknowledgement();

		debug.println("Extracting transaction header");
		List<Segment> header = extractLoop(new SegmentComparator[] {
				new SegmentComparator("ISA"), new SegmentComparator("GS"),
				new SegmentComparator("ST"), new SegmentComparator("AK1") });
		while (isNextSegmentIdentifier(new SegmentComparator[] {
				new SegmentComparator("AK2"), new SegmentComparator("AK5") })) {
			TransactionSet transactionSet = new TransactionSet();

			debug.println("***** Loop AK2 *****");
			List<Segment> loopak2 = extractLoop(new SegmentComparator[] { new SegmentComparator(
					"AK2") });

			while (isNextSegmentIdentifier(new SegmentComparator[] { new SegmentComparator(
					"AK3") })) {
				debug.println("***** Loop AK3 *****");
				List<Segment> loopak3 = extractLoop(new SegmentComparator[] {
						new SegmentComparator("AK3"),
						new SegmentComparator("AK4") });
				
				// TODO: correctly object wrap loop AK3
			}

			// Extract trailer segment
			List<Segment> loopak5 = extractLoop(new SegmentComparator[] { new SegmentComparator(
					"AK5") });

			transactionSet.processSegmentList((List<Segment>) ListUtils.sum(
					loopak2, loopak5));
			functionalAcknowledgement.addTransactionSet(transactionSet);
		}

		debug.println("Extracting transaction trailer");
		List<Segment> trailer = extractLoop(new SegmentComparator[] {
				new SegmentComparator("AK9"), new SegmentComparator("SE"),
				new SegmentComparator("GE"), new SegmentComparator("IEA") });

		// Parse appropriate pieces
		functionalAcknowledgement.processSegmentList((List<Segment>) ListUtils
				.sum(header, trailer));

		debug.println("Finished parsing 997");
	}

	@Override
	public String parseData(String inputData) throws Exception {
		this.parse(inputData);
		return functionalAcknowledgement.toString();
	}

}
