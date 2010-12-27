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

import java.util.List;

import org.pb.x12.Segment;
import org.remitt.prototype.SegmentComparator;
import org.remitt.prototype.X12DTO;
import org.remitt.prototype.X12Message;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "address")
public class Address implements X12DTO {

	@Element(name = "streetAddressLine1", required = false)
	private String streetAddressLine1;

	@Element(name = "streetAddressLine2", required = false)
	private String streetAddressLine2;

	@Element(name = "city")
	private String city;

	@Element(name = "stateProvince")
	private String stateProvince;

	@Element(name = "postalCode")
	private String postalCode;

	public Address() {
	}

	public Address(List<Segment> in) {
		processSegmentList(in);
	}

	@Override
	public void processSegmentList(List<Segment> in) {
		Segment N3 = X12Message.findSegmentByComparator(in,
				new SegmentComparator("N3"));
		this.streetAddressLine1 = X12Message.getSafeElement(N3, 1);
		this.streetAddressLine2 = X12Message.getSafeElement(N3, 2);
		Segment N4 = X12Message.findSegmentByComparator(in,
				new SegmentComparator("N4"));
		this.city = X12Message.getSafeElement(N4, 1);
		this.stateProvince = X12Message.getSafeElement(N4, 2);
		this.postalCode = X12Message.getSafeElement(N4, 3);
	}

	@Override
	public String toString() {
		return X12Message.serializeDTO(this);
	}

	public String getStreetAddressLine1() {
		return this.streetAddressLine1;
	}

	public String getStreetAddressLine2() {
		return this.streetAddressLine2;
	}

	public String getCity() {
		return this.city;
	}

	public String getStateProvince() {
		return this.stateProvince;
	}

	public String getPostalCode() {
		return this.postalCode;
	}

}
