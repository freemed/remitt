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
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.pb.x12.Segment;
import org.remitt.prototype.SegmentComparator;
import org.remitt.prototype.X12DTO;
import org.remitt.prototype.X12Message;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementArray;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name = "claimInformation")
public class ClaimInformation implements X12DTO {

	@Element(name = "serviceCodeQualifier")
	private String serviceCodeQualifier;

	@Element(name = "serviceCode")
	private String serviceCode;

	@ElementArray(name = "serviceCodeModifiers", entry = "serviceCodeModifier")
	private String[] serviceCodeModifiers = {};

	@Element(name = "servicePeriodStart", required = false)
	private Date servicePeriodStart;

	@Element(name = "servicePeriodEnd", required = false)
	private Date servicePeriodEnd;

	@Element(name = "lineItemChargeAmount")
	private Double lineItemChargeAmount;

	@Element(name = "lineItemProviderPaymentAmount")
	private Double lineItemProviderPaymentAmount;

	@Element(name = "quantity")
	private Integer quantity = 1;

	@ElementList(name = "claimAdjustments", required = false)
	private List<ClaimAdjustment> claimAdjustments = new ArrayList<ClaimAdjustment>();

	public ClaimInformation(List<Segment> in) {
		processSegmentList(in);
	}

	@Override
	public void processSegmentList(List<Segment> in) {
		Segment SVC = X12Message.findSegmentByComparator(in,
				new SegmentComparator("SVC"));
		List<String> serviceCodeComposite = Arrays.asList(SVC.getElement(1)
				.split("\\|"));
		this.serviceCodeQualifier = serviceCodeComposite.get(0);
		try {
			this.serviceCode = serviceCodeComposite.get(1);
		} catch (Exception ex) { }
		for (int iter = 3; iter <= 6; iter++) {
			// Jump out of the loop if we have nothing
			if (iter > serviceCodeComposite.size()) {
				break;
			}
			this.serviceCodeModifiers = (String[]) ArrayUtils.add(
					this.serviceCodeModifiers, serviceCodeComposite
							.get(iter - 1));
		}
		try {
			setLineItemChargeAmount(Double.parseDouble(SVC.getElement(2)));
		} catch (Exception ex) {
		}
		try {
			setLineItemProviderPaymentAmount(Double.parseDouble(SVC
					.getElement(3)));
		} catch (Exception ex) {
		}
		try {
			this.quantity = Integer.parseInt(SVC.getElement(5).trim());
			if (this.quantity < 1) {
				this.quantity = 1;
			}
		} catch (IndexOutOfBoundsException ex) {
			if (this.quantity < 1) {
				this.quantity = 1;
			}
		} catch (NumberFormatException ex) {
			if (this.quantity < 1) {
				this.quantity = 1;
			}
		} catch (NullPointerException ex) {
			if (this.quantity < 1) {
				this.quantity = 1;
			}
		}

		List<Segment> DTMs = X12Message.findSegmentsByComparator(in,
				new SegmentComparator("DTM"));
		for (Segment DTM : DTMs) {
			switch (Integer.parseInt(DTM.getElement(1))) {
			case 150:
				this.servicePeriodStart = X12Message.parseDate(DTM
						.getElement(2));
				break;
			case 151:
				this.servicePeriodEnd = X12Message.parseDate(DTM.getElement(2));
				break;
			case 472:
				this.servicePeriodStart = X12Message.parseDate(DTM
						.getElement(2));
				this.servicePeriodEnd = X12Message.parseDate(DTM.getElement(2));
				break;
			default:
				break;
			}
		}

		// Handle adjustments
		List<Segment> CASs = X12Message.findSegmentsByComparator(in,
				new SegmentComparator("CAS", 1, new String[] { "CO", "CR",
						"OA", "PI", "PR" }));
		for (Segment CAS : CASs) {
			getClaimAdjustments().add(new ClaimAdjustment(CAS));
		}
	}

	public void servicePeriodStart(Date servicePeriodStart) {
		this.servicePeriodStart = servicePeriodStart;
	}

	public Date getServicePeriodStart() {
		return servicePeriodStart;
	}

	public void setServicePeriodEnd(Date servicePeriodEnd) {
		this.servicePeriodEnd = servicePeriodEnd;
	}

	public Date getServicePeriodEnd() {
		return servicePeriodEnd;
	}

	public void setServiceCodetype(String serviceCodetype) {
		this.serviceCodeQualifier = serviceCodetype;
	}

	public String getServiceCodetype() {
		return serviceCodeQualifier;
	}

	public void setServiceCode(String serviceCode) {
		this.serviceCode = serviceCode;
	}

	public String getServiceCode() {
		return serviceCode;
	}

	public void setServiceCodeModifiers(String[] serviceCodeModifiers) {
		this.serviceCodeModifiers = serviceCodeModifiers;
	}

	public String[] getServiceCodeModifiers() {
		return serviceCodeModifiers;
	}

	public void setProcedureAmount(Double procedureAmount) {
		this.lineItemChargeAmount = procedureAmount;
	}

	public Double getProcedureAmount() {
		return lineItemChargeAmount;
	}

	public void setLineItemChargeAmount(Double lineItemChargeAmount) {
		this.lineItemChargeAmount = lineItemChargeAmount;
	}

	public void setLineItemProviderPaymentAmount(
			Double lineItemProviderPaymentAmount) {
		this.lineItemProviderPaymentAmount = lineItemProviderPaymentAmount;
	}

	public Double getLineItemProviderPaymentAmount() {
		return lineItemProviderPaymentAmount;
	}

	public void setClaimAdjustments(List<ClaimAdjustment> claimAdjustments) {
		this.claimAdjustments = claimAdjustments;
	}

	public List<ClaimAdjustment> getClaimAdjustments() {
		return claimAdjustments;
	}

}
