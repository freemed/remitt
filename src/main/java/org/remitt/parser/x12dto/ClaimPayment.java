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
import java.util.List;

import org.pb.x12.Segment;
import org.remitt.prototype.SegmentComparator;
import org.remitt.prototype.X12DTO;
import org.remitt.prototype.X12Message;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

public class ClaimPayment implements X12DTO {

	@Attribute(name = "claimId")
	private String claimId;

	@Attribute(name = "claimCode")
	private Integer claimCode;

	@Attribute(name = "claimStatus", required = false)
	private String claimStatus;

	@Element(name = "claimTotalAmount")
	private Double claimTotalAmount;

	@Element(name = "claimPaidAmount")
	private Double claimPaidAmount;

	@Element(name = "claimPatientResponsibilityAmount")
	private Double claimPatientResponsibilityAmount;

	@Element(name = "claimType")
	private String claimType;

	@Element(name = "patient")
	private Patient patient;

	@Element(name = "insured", required = false)
	private Insured insured;

	@ElementList(name = "amounts", required = false)
	private List<Amount> amounts = new ArrayList<Amount>();

	public ClaimPayment() {
	}

	public ClaimPayment(List<Segment> in) {
		processSegmentList(in);
	}

	@Override
	public void processSegmentList(List<Segment> in) {
		Segment CLP = X12Message.findSegmentByComparator(in,
				new SegmentComparator("CLP"));
		this.claimId = X12Message.getSafeElement(CLP, 1);
		this.claimCode = Integer.parseInt(X12Message.getSafeElement(CLP, 2));
		switch (this.claimCode.intValue()) {
		case 1:
			this.claimStatus = "PROCESSED: PRIMARY";
			break;
		case 2:
			this.claimStatus = "PROCESSED: SECONDARY";
			break;
		case 3:
			this.claimStatus = "PROCESSED: TERTIARY";
			break;
		case 4:
			this.claimStatus = "DENIED";
			break;
		case 5:
			this.claimStatus = "PENDED";
			break;
		case 10:
			this.claimStatus = "RECEIVED, NOT IN PROCESS";
			break;
		case 13:
			this.claimStatus = "SUSPENDED";
			break;
		case 15:
			this.claimStatus = "SUSPENDED, INVESTIGATION";
			break;
		case 16:
			this.claimStatus = "SUSPENDED, RETURN WITH MATERIAL";
			break;
		case 17:
			this.claimStatus = "SUSPENDED, REVIEW PENDING";
			break;
		case 19:
			this.claimStatus = "PROCESSED: PRIMARY, FORWARDED TO OTHER PAYER";
			break;
		case 20:
			this.claimStatus = "PROCESSED: SECONDARY, FORWARDED TO OTHER PAYER";
			break;
		default:
			break;
		}

		this.claimTotalAmount = Double.parseDouble(X12Message.getSafeElement(
				CLP, 3));
		this.claimPaidAmount = Double.parseDouble(X12Message.getSafeElement(
				CLP, 4));
		this.claimPatientResponsibilityAmount = Double.parseDouble(X12Message
				.getSafeElement(CLP, 5));
		this.claimType = X12Message.getSafeElement(CLP, 6);

		List<Amount> a = new ArrayList<Amount>();
		List<Segment> AMTs = X12Message.findSegmentsByComparator(in,
				new SegmentComparator("AMT"));
		for (Segment AMT : AMTs) {
			Amount e = new Amount(AMT);
			a.add(e);
		}
		this.amounts = a;

		this.patient = new Patient(in);
		this.insured = new Insured(in);
	}

	public List<Amount> getAmounts() {
		return this.amounts;
	}

	public String getClaimId() {
		return this.claimId;
	}

	public Double getClaimTotalAmount() {
		return this.claimTotalAmount;
	}

	public Double getClaimPaidAmount() {
		return this.claimPaidAmount;
	}

	public Double getClaimPatientResponsibilityAmount() {
		return this.claimPatientResponsibilityAmount;
	}

	public String getClaimType() {
		return this.claimType;
	}

	public Patient getPatient() {
		return this.patient;
	}

	public Insured getInsured() {
		return this.insured;
	}

	@Override
	public String toString() {
		return X12Message.serializeDTO(this);
	}

}
