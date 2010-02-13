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

package org.remitt.parser.x12dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pb.x12.Segment;
import org.remitt.prototype.SegmentComparator;
import org.remitt.prototype.X12DTO;
import org.remitt.prototype.X12Message;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

public class ClaimAdjustment implements X12DTO {

	public class Reason implements X12DTO {

		@Attribute(name = "code")
		private String code;

		@Attribute(name = "quantity")
		private Integer quantity;

		@Attribute(name = "amount")
		private Double amount;

		private int position;

		public Reason() {
		}

		public Reason(Segment cas, int position) {
			List<Segment> in = new ArrayList<Segment>();
			in.add(cas);
			processSegmentList(in);
		}

		@Override
		public void processSegmentList(List<Segment> in) {
			Segment CAS = X12Message.findSegmentByComparator(in,
					new SegmentComparator("CAS"));
			try {
				this.quantity = Integer.parseInt(X12Message.getSafeElement(CAS,
						position));
				this.code = X12Message.getSafeElement(CAS, position + 1);
				this.amount = Double.parseDouble(X12Message.getSafeElement(CAS,
						position + 2));
			} catch (Exception ex) {
			}
		}

		public String getCode() {
			return this.code;
		}

		public Integer getQuantity() {
			return this.quantity;
		}

		public Double getAmount() {
			return this.amount;
		}

	}

	@Attribute(name = "adjustmentGroupCode")
	private String adjustmentGroupCode;

	@Element(name = "adjustmentGroup", required = false)
	private String adjustmentGroup;

	@Element(name = "adjustmentReasonCode")
	private String adjustmentReasonCode;

	@Element(name = "adjustmentAmount")
	private Double adjustmentAmount;

	@ElementList(name = "reasons", required = false)
	private List<Reason> reasons = new ArrayList<Reason>();

	private Map<String, String> adjustmentGroupCodeLookup = new HashMap<String, String>();

	public ClaimAdjustment() {
	}

	public ClaimAdjustment(List<Segment> in) {
		processSegmentList(in);
	}

	@Override
	public void processSegmentList(List<Segment> in) {
		this.populateAdjustmentGroupCodeLookup();
		Segment CAS = X12Message.findSegmentByComparator(in,
				new SegmentComparator("CAS"));
		this.adjustmentGroupCode = X12Message.getSafeElement(CAS, 1);
		this.adjustmentGroup = this.adjustmentGroupCodeLookup
				.get(this.adjustmentGroupCode);
		this.adjustmentReasonCode = X12Message.getSafeElement(CAS, 2);
		this.adjustmentAmount = Double.parseDouble(X12Message.getSafeElement(
				CAS, 3));
		// Attempt to populate "reasons"
		for (int iter = 4; iter < 19; iter += 3) {
			Reason r = new Reason(CAS, iter);
			if (r.getCode() != null) {
				reasons.add(r);
			}
		}
	}

	private void populateAdjustmentGroupCodeLookup() {
		adjustmentGroupCodeLookup.put("CO", "CONTRACTUAL OBLIGATIONS");
		adjustmentGroupCodeLookup.put("CH", "CORRECTIONS AND REVERSALS");
		adjustmentGroupCodeLookup.put("OA", "OTHER ADJUSTMENTS");
		adjustmentGroupCodeLookup.put("PI", "PAYOR INITIATED REDUCTIONS");
		adjustmentGroupCodeLookup.put("PR", "PATIENT RESPONSIBILITY");
	}

	public String getAdjustmentGroupCode() {
		return this.adjustmentGroupCode;
	}

	public String getAdjustmentGroup() {
		return this.adjustmentGroup;
	}

	public String getAdjustmentReasonCode() {
		return this.adjustmentReasonCode;
	}

	public Double getAdjustmentAmount() {
		return this.adjustmentAmount;
	}

	@Override
	public String toString() {
		return X12Message.serializeDTO(this);
	}

}
