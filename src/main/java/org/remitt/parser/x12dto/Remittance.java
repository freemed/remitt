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

@Root(name = "remittance")
public class Remittance implements X12DTO {

	@Attribute(name = "creditDebitFlagCode")
	private char creditDebitFlagCode = 0;

	@Attribute(name = "paymentMethodCode")
	private String paymentMethodCode = "";

	@Attribute(name = "transactionSetControlNumber")
	private String transactionSetControlNumber = "";

	@Attribute(name = "transactionHandlingCode")
	private char transactionHandlingCode = 0;

	@Element(name = "checkTraceNumber")
	private String checkTraceNumber = "";

	@Element(name = "totalPaymentAmount")
	private Double totalPaymentAmount = 0.0;

	@ElementList(name = "payers")
	private List<Payer> payers = new ArrayList<Payer>();

	public Remittance() {
	}

	public Remittance(List<Segment> in) {
		processSegmentList(in);
	}

	@Override
	public void processSegmentList(List<Segment> in) {
		Segment ST = X12Message.findSegmentByComparator(in,
				new SegmentComparator("ST"));
		this.transactionSetControlNumber = X12Message.getSafeElement(ST, 2);
		Segment BPR = X12Message.findSegmentByComparator(in,
				new SegmentComparator("BPR"));
		this.transactionHandlingCode = X12Message.getSafeElement(BPR, 1)
				.charAt(0);
		this.totalPaymentAmount = Double.parseDouble(X12Message.getSafeElement(
				BPR, 2));
		this.creditDebitFlagCode = X12Message.getSafeElement(BPR, 3).charAt(0);
		this.paymentMethodCode = X12Message.getSafeElement(BPR, 4);
		Segment TRN = X12Message.findSegmentByComparator(in,
				new SegmentComparator("TRN"));
		this.checkTraceNumber = X12Message.getSafeElement(TRN, 2);
	}

	public String getCheckTraceNumber() {
		return this.checkTraceNumber;
	}

	public char getCreditDebitFlagCode() {
		return this.creditDebitFlagCode;
	}

	public List<Payer> getPayers() {
		return this.payers;
	}

	public String getPaymentMethodCode() {
		return this.paymentMethodCode;
	}

	public Double getTotalPaymentAmount() {
		return this.totalPaymentAmount;
	}

	public String getTransactionSetControlNumber() {
		return this.transactionSetControlNumber;
	}

	public char getTransactionHandlingCode() {
		return this.transactionHandlingCode;
	}

	public void setCheckTraceNumber(String checkTraceNumber) {
		this.checkTraceNumber = checkTraceNumber;
	}

	public void setCreditDebitFlagCode(char creditDebitFlagCode) {
		this.creditDebitFlagCode = creditDebitFlagCode;
	}

	public void setPayers(List<Payer> payers) {
		this.payers = payers;
	}

	public void setPaymentMethodCode(String paymentMethodCode) {
		this.paymentMethodCode = paymentMethodCode;
	}

	public void setTotalPaymentAmount(Double totalPaymentAmount) {
		this.totalPaymentAmount = totalPaymentAmount;
	}

	public void setTransactionSetControlNumber(
			String transactionSetControlNumber) {
		this.transactionSetControlNumber = transactionSetControlNumber;
	}

	public void setTransactionHandlingCode(char transactionHandlingCode) {
		this.transactionHandlingCode = transactionHandlingCode;
	}

	@Override
	public String toString() {
		return X12Message.serializeDTO(this);
	}

}
