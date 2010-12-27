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

package org.remitt.parser.x12dto;

import java.util.ArrayList;
import java.util.List;

import org.pb.x12.Segment;
import org.remitt.prototype.SegmentComparator;
import org.remitt.prototype.X12DTO;
import org.remitt.prototype.X12Message;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name = "payer")
public class Payer implements X12DTO {

	@Attribute(name = "idNumber")
	private String idNumber;

	@Attribute(name = "idQualifier")
	private String idQualifier;

	@Element(name = "name")
	private String name;

	@Element(name = "address")
	private Address address;

	@ElementList(name = "payees", required = false)
	private List<Payee> payees = new ArrayList<Payee>();

	public Payer() {
	}

	public Payer(List<Segment> in) {
		processSegmentList(in);
	}

	@Override
	public void processSegmentList(List<Segment> in) {
		Segment N1 = X12Message.findSegmentByComparator(in,
				new SegmentComparator("N1", 1, new String[] { "PR" }));
		this.name = X12Message.getSafeElement(N1, 2);
		this.address = new Address(in);
		Segment REF = X12Message.findSegmentByComparator(in,
				new SegmentComparator("REF"));
		this.idQualifier = X12Message.getSafeElement(REF, 1);
		this.idNumber = X12Message.getSafeElement(REF, 2);
	}

	public String getIdNumber() {
		return this.idNumber;
	}

	public String getIdQualifier() {
		return this.idQualifier;
	}

	public String getName() {
		return this.name;
	}

	public Address getAddress() {
		return this.address;
	}

	public List<Payee> getPayees() {
		return this.payees;
	}

	@Override
	public String toString() {
		return X12Message.serializeDTO(this);
	}

}
