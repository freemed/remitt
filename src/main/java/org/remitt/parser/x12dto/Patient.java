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

package org.remitt.parser.x12dto;

import java.util.List;

import org.apache.log4j.Logger;
import org.pb.x12.Segment;
import org.remitt.prototype.SegmentComparator;
import org.remitt.prototype.X12DTO;
import org.remitt.prototype.X12Message;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "patient")
public class Patient implements X12DTO {

	static final Logger log = Logger.getLogger(Patient.class);

	@Attribute(name = "idQualifier", required = false)
	private String idQualifier;

	@Attribute(name = "idNumber", required = false)
	private String idNumber;

	@Element(name = "lastName", required = false)
	private String lastName;

	@Element(name = "firstName", required = false)
	private String firstName;

	@Element(name = "middleName", required = false)
	private String middleName;

	@Element(name = "suffix", required = false)
	private String suffix;

	public Patient() {
	}

	public Patient(List<Segment> in) {
		processSegmentList(in);
	}

	@Override
	public void processSegmentList(List<Segment> in) {
		Segment NM1 = X12Message.findSegmentByComparator(in,
				new SegmentComparator("NM1", 1, new String[] { "QC" }));
		log.info(NM1.toString());
		processSegmentListInternal(NM1);
	}

	public void processSegmentListInternal(Segment NM1) {
		// log.info("NM103: " + X12Message.getSafeElement(NM1, 3));
		this.lastName = X12Message.getSafeElement(NM1, 3);
		// log.info("NM104: " + X12Message.getSafeElement(NM1, 4));
		this.firstName = X12Message.getSafeElement(NM1, 4);
		this.middleName = X12Message.getSafeElement(NM1, 5);
		this.suffix = X12Message.getSafeElement(NM1, 7);
		this.idQualifier = X12Message.getSafeElement(NM1, 8);
		this.idNumber = X12Message.getSafeElement(NM1, 9);
	}

	public String getIdQualifier() {
		return this.idQualifier;
	}

	public String getIdNumber() {
		return this.idNumber;
	}

	public String getLastName() {
		return this.lastName;
	}

	public String getFirstName() {
		return this.firstName;
	}

	public String getMiddleName() {
		return this.middleName;
	}

	public String getSuffix() {
		return this.suffix;
	}

	@Override
	public String toString() {
		return X12Message.serializeDTO(this);
	}

}
