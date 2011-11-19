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
import org.remitt.parser.x12dto.ClaimAdjustment;
import org.remitt.parser.x12dto.ClaimInformation;
import org.remitt.parser.x12dto.ClaimPayment;
import org.remitt.parser.x12dto.Payee;
import org.remitt.parser.x12dto.Payer;
import org.remitt.parser.x12dto.ProviderClaimGroup;
import org.remitt.parser.x12dto.Remittance;
import org.remitt.prototype.ParserInterface;
import org.remitt.prototype.SegmentComparator;
import org.remitt.prototype.X12Message;

/**
 * Handle X12 835 remittance messages.
 * 
 * @author jeff@freemedsoftware.org
 */
public class X12Message835 extends X12Message implements ParserInterface {

	static final Logger log = Logger.getLogger(X12Message835.class);

	private Remittance remittance = null;

	protected void parseSegments() {
		if (getX12message() == null) {
			log.error("X12 message not set");
			return;
		}
		debug.println("Begin parsing 835");
		position = 1;
		debug.println("Extracting transaction header");
		List<Segment> header = extractLoop(new SegmentComparator[] {
				new SegmentComparator("ISA"), new SegmentComparator("GS"),
				new SegmentComparator("ST"), new SegmentComparator("BPR"),
				new SegmentComparator("NTE"), new SegmentComparator("TRN"),
				new SegmentComparator("CUR"), new SegmentComparator("REF"),
				new SegmentComparator("DTM") });
		Remittance remittance = new Remittance(header);
		debug.println("***** Loop 1000A *****");
		while (isNextSegmentIdentifier(new SegmentComparator("N1", 1,
				new String[] { "PR" }))) {
			List<Segment> loop1000a = extractLoop(new SegmentComparator[] {
					new SegmentComparator("N1", 1, new String[] { "PR" }),
					new SegmentComparator("N2"), new SegmentComparator("N3"),
					new SegmentComparator("N4"), new SegmentComparator("REF"),
					new SegmentComparator("PER", 1, new String[] { "CX" }) });
			debug.println("Serialize Payer DTO");
			Payer payer = new Payer(loop1000a);
			debug.println("***** Loop 1000B ***** ");
			while (isNextSegmentIdentifier(new SegmentComparator("N1", 1,
					new String[] { "PE" }))) {
				List<Segment> loop1000b = extractLoop(new SegmentComparator[] {
						new SegmentComparator("N1", 1, new String[] { "PE" }),
						new SegmentComparator("N3"),
						new SegmentComparator("N4"),
						new SegmentComparator("REF"),
						new SegmentComparator("RDM") });
				Payee payee = new Payee(loop1000b);
				while (isNextSegmentIdentifier("LX")) {
					debug.println("***** Loop 2000 *****");
					List<Segment> loop2000 = extractLoop(new SegmentComparator[] {
							new SegmentComparator("LX"),
							new SegmentComparator("TS3"),
							new SegmentComparator("TS2") });
					ProviderClaimGroup providerClaimGroup = new ProviderClaimGroup(
							loop2000);
					while (isNextSegmentIdentifier("CLP")) {
						debug.println("***** Loop 2100 *****");
						List<Segment> loop2100 = extractLoop(new SegmentComparator[] {
								new SegmentComparator("CLP"),
								new SegmentComparator("CAS"),
								new SegmentComparator("NM1", 1, new String[] {
										"QC", "IL", "74", "82", "TT", "PR" }),
								new SegmentComparator("MIA"),
								new SegmentComparator("MOA"),
								new SegmentComparator("REF"),
								new SegmentComparator("DTM", 1, new String[] {
										"036", "050", "232", "233" }),
								new SegmentComparator("PER", 1,
										new String[] { "CX" }),
								new SegmentComparator("AMT", 1, new String[] {
										"AU", "D8", "DY", "F5", "I", "NL", "T",
										"T2", "ZK", "ZL", "ZM", "ZN", "ZO",
										"ZZ" }),
								new SegmentComparator("QTY", 1, new String[] {
										"CA", "CD", "LA", "LE", "NA", "NE",
										"NR", "OU", "PS", "VS", "ZK", "ZL",
										"ZM", "ZN", "ZO" }) });
						providerClaimGroup.getClaimPayments().add(
								new ClaimPayment(loop2100));
						providerClaimGroup.getClaimAdjustments().add(
								new ClaimAdjustment(loop2100));
						debug.println("***** Loop 2110 *****");
						List<Segment> loop2110 = extractLoop(new SegmentComparator[] {
								new SegmentComparator("SVC"),
								new SegmentComparator("DTM", 1, new String[] {
										"150", "151", "472" }),
								new SegmentComparator("CAS", 1, new String[] {
										"CO", "CR", "OA", "PI", "PR" }),
								new SegmentComparator("REF", 1, new String[] {
										"1S", "6R", "BB", "E9", "G1", "G3",
										"LU", "RB" }),
								new SegmentComparator("REF", 1, new String[] {
										"1A", "1B", "1C", "1D", "1G", "1H",
										"1J", "HPI", "SY", "TJ", "XX" }),
								new SegmentComparator("AMT", 1, new String[] {
										"B6", "DY", "KH", "NE", "T", "T2",
										"ZK", "ZL", "ZM", "ZN", "ZO" }),
								new SegmentComparator("QTY", 1, new String[] {
										"NE", "ZK", "ZL", "ZM", "ZN", "ZO" }),
								new SegmentComparator("LQ", 1, new String[] {
										"HE", "RX" }) });
						providerClaimGroup.getClaimInformations().add(
								new ClaimInformation(loop2110));
						log.info("2110 finished");
					}
					payee.getProviderClaimGroup().add(providerClaimGroup);
					log.info("2100 finished");
				}
				payer.getPayees().add(payee);
				log.info("2000 finished");
			}
			remittance.getPayers().add(payer);
		}
		debug.println("Summary loop");
		/*
		List<Segment> summary = extractLoop(new SegmentComparator[] {
				new SegmentComparator("PLB"), new SegmentComparator("SE") });
				*/
		debug.println("Finished parsing 835");

		setRemittance(remittance);
		debug.println(remittance.toString());
	}

	public Remittance getRemittance() {
		return this.remittance;
	}

	public void setRemittance(Remittance r) {
		this.remittance = r;
	}

	@Override
	public String parseData(String inputData) throws Exception {
		this.parse(inputData);
		return getRemittance().toString();
	}

}
