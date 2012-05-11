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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.pb.x12.Segment;
import org.remitt.prototype.SegmentComparator;
import org.remitt.prototype.X12DTO;
import org.remitt.prototype.X12Message;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name = "providerClaimGroup")
public class ProviderClaimGroup implements X12DTO {

	@Attribute(name = "providerId", required = false)
	private String providerId;

	@Attribute(name = "facilityId", required = false)
	private String facilityId;

	@Element(name = "fiscalPeriodDate", required = false)
	private Date fiscalPeriodDate;

	@Element(name = "totalClaimCount", required = false)
	private Integer totalClaimCount;

	@Element(name = "totalClaimAmount", required = false)
	private Double totalClaimAmount;

	@ElementList(name = "claimPayments")
	private List<ClaimPayment> claimPayments = new ArrayList<ClaimPayment>();

	@ElementList(name = "claimAdjustments", required = false)
	private List<ClaimAdjustment> claimAdjustments = new ArrayList<ClaimAdjustment>();

	@ElementList(name = "claimInformations", required = false)
	private List<ClaimInformation> claimInformations = new ArrayList<ClaimInformation>();

	private static final SimpleDateFormat x12dateFormat = new SimpleDateFormat("yyyyMMdd");

	static final Logger log = Logger.getLogger(ProviderClaimGroup.class);

	public ProviderClaimGroup() {
	}

	public ProviderClaimGroup(List<Segment> in) {
		processSegmentList(in);
	}

	@Override
	public void processSegmentList(List<Segment> in) {
		Segment TS3 = X12Message.findSegmentByComparator(in,
				new SegmentComparator("TS3"));
		if (TS3 != null) {
			this.providerId = X12Message.getSafeElement(TS3, 1);
			this.facilityId = X12Message.getSafeElement(TS3, 2);
			try {
				synchronized (x12dateFormat) {
					this.fiscalPeriodDate = x12dateFormat.parse(X12Message
							.getSafeElement(TS3, 3));
				};
			} catch (ParseException e) {
				log.error(e);
			}
			this.totalClaimCount = Integer.parseInt(X12Message.getSafeElement(
					TS3, 4));
			this.totalClaimAmount = Double.parseDouble(X12Message
					.getSafeElement(TS3, 5));
		}
	}

	public String getProviderId() {
		return this.providerId;
	}

	public String getFacilityId() {
		return this.facilityId;
	}

	public Date getFiscalPeriodDate() {
		return this.fiscalPeriodDate;
	}

	public Integer getTotalClaimCount() {
		return this.totalClaimCount;
	}

	public Double getTotalClaimAmount() {
		return this.totalClaimAmount;
	}

	public List<ClaimPayment> getClaimPayments() {
		return this.claimPayments;
	}

	public List<ClaimAdjustment> getClaimAdjustments() {
		return this.claimAdjustments;
	}

	public List<ClaimInformation> getClaimInformations() {
		return claimInformations;
	}

	@Override
	public String toString() {
		return X12Message.serializeDTO(this);
	}

}
