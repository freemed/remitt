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

import java.util.ArrayList;
import java.util.List;

import org.pb.x12.Segment;
import org.remitt.prototype.SegmentComparator;
import org.remitt.prototype.X12DTO;
import org.remitt.prototype.X12Message;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "amount")
public class Amount implements X12DTO {

	@Attribute(name = "qualifier")
	private String qualifier = "";

	@Attribute(name = "amount")
	private Double amount = 0.00;

	public Amount() {
	}

	public Amount(List<Segment> in) {
		processSegmentList(in);
	}

	public Amount(Segment segment) {
		List<Segment> s = new ArrayList<Segment>();
		s.add(segment);
		processSegmentList(s);
	}

	@Override
	public void processSegmentList(List<Segment> in) {
		Segment AMT = X12Message.findSegmentByComparator(in,
				new SegmentComparator("AMT"));
		this.qualifier = X12Message.getSafeElement(AMT, 1);
		this.amount = Double.valueOf(X12Message.getSafeElement(AMT, 2));
	}

	public Double getAmount() {
		return this.amount;
	}

	public String getQualifier() {
		return this.qualifier;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public void setQualifier(String qualifier) {
		this.qualifier = qualifier;
	}

	@Override
	public String toString() {
		return X12Message.serializeDTO(this);
	}

}
