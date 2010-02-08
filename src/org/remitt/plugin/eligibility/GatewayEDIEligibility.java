/*
 * $Id$
 *
 * Authors:
 *      Jeff Buchbinder <jeff@freemedsoftware.org>
 *
 * REMITT Electronic Medical Information Translation and Transmission
 * Copyright (C) 1999-2009 FreeMED Software Foundation
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

package org.remitt.plugin.eligibility;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.rpc.ServiceException;

import org.apache.axis.client.Call;
import org.apache.axis.client.Stub;
import org.apache.log4j.Logger;
import org.remitt.prototype.EligibilityInterface;
import org.remitt.prototype.EligibilityResponse;
import org.remitt.prototype.EligibilityStatus;
import org.remitt.server.Configuration;

import WebServices.GatewayEDI.Eligibility;
import WebServices.GatewayEDI.EligibilityLocator;
import WebServices.GatewayEDI.EligibilitySoap;
import WebServices.GatewayEDI.MyNameValue;
import WebServices.GatewayEDI.SuccessCode;
import WebServices.GatewayEDI.WSEligibilityInquiry;
import WebServices.GatewayEDI.WSEligibilityResponse;
import WebServices.GatewayEDI.WSResponseDataType;

public class GatewayEDIEligibility implements EligibilityInterface {

	static final Logger log = Logger.getLogger(GatewayEDIEligibility.class);

	@Override
	public String getPluginName() {
		return "GatewayEDIEligibility";
	}

	@Override
	public Double getPluginVersion() {
		return 0.1;
	}

	@Override
	public EligibilityResponse checkEligibility(String userName,
			HashMap<String, String> values) throws Exception {
		// Make a service
		Eligibility service = new EligibilityLocator();

		EligibilityResponse er = new EligibilityResponse();

		// Now use the service to get a stub which implements the SDI.
		EligibilitySoap port = null;
		try {
			port = service.getEligibilitySoap();
		} catch (ServiceException e) {
			log.error(e);
			er.setStatus(EligibilityStatus.SERVER_ERROR);
			return er;
		}

		List<MyNameValue> params = new ArrayList<MyNameValue>();
		addNameValue(params, values, "npi", "NPI");
		addNameValue(params, values, "insuranceId", "InsuranceNum");
		addNameValue(params, values, "insuredLastName", "InsuredLastName");
		addNameValue(params, values, "insuredFirstName", "InsuredFirstName");
		addNameValue(params, values, "insuredDateOfBirth", "InsuredDob");
		addNameValue(params, values, "insuredGender", "InsuredGender");
		addNameValue(params, values, "insuredState", "InsuredState");
		addNameValue(params, values, "insuredSsn", "InsuredSsn");
		addNameValue(params, values, "dependentLastName", "DependentLastName");
		addNameValue(params, values, "dependentFirstName", "DependentFirstName");
		addNameValue(params, values, "dependentDateOfBirth", "DependentDob");
		addNameValue(params, values, "dependentGender", "DependentGender");
		addNameValue(params, values, "dependentRelationship",
				"DependentRelationshipCode");
		addNameValue(params, values, "serviceType", "ServiceTypeCode");
		addNameValue(params, values, "cardIssueDate", "CardIssueDate");
		addNameValue(params, values, "groupId", "GroupNumber");

		// Set HTTP Authentication:
		((Stub) service)._setProperty(Call.USERNAME_PROPERTY, Configuration
				.getPluginOption(this, userName, "gatewayEdiUsername"));
		((Stub) service)._setProperty(Call.PASSWORD_PROPERTY, Configuration
				.getPluginOption(this, userName, "gatewayEdiPassword"));

		WSEligibilityInquiry inq = new WSEligibilityInquiry();
		inq.setParameters(params.toArray(new MyNameValue[0]));
		inq.setResponseDataType(WSResponseDataType.Xml);

		WSEligibilityResponse response = null;
		try {
			response = port.doInquiry(inq);
		} catch (RemoteException e) {
			log.error(e);
			er.setStatus(EligibilityStatus.SERVER_ERROR);
			return er;
		}

		String rawResponse = response.getResponseAsRawString();
		log.info(rawResponse);
		er.setRawResponse(rawResponse);
		er.setSuccessCode(response.getSuccessCode());

		// Try to pass on all processing messages as well, just in case.
		try {
			er.setMessages(response.getExtraProcessingInfo().getAllMessages());
		} catch (Exception ex) {
			log.error(ex);
		}

		if (response.getSuccessCode() == SuccessCode.Success) {
			er.setStatus(EligibilityStatus.OK);
			return er;
		}

		if (response.getSuccessCode() == SuccessCode.ValidationFailure) {
			er.setStatus(EligibilityStatus.BAD);
			return er;
		}

		if (response.getSuccessCode() == SuccessCode.PayerEnrollmentRequired) {
			er.setStatus(EligibilityStatus.BAD);
			return er;
		}

		if (response.getSuccessCode() == SuccessCode.PayerNotSupported) {
			er.setStatus(EligibilityStatus.SERVER_ERROR);
			return er;
		}

		if (response.getSuccessCode() == SuccessCode.PayerTimeout) {
			er.setStatus(EligibilityStatus.SERVER_ERROR);
			return er;
		}

		if (response.getSuccessCode() == SuccessCode.ProviderEnrollmentRequired) {
			er.setStatus(EligibilityStatus.BAD);
			return er;
		}

		if (response.getSuccessCode() == SuccessCode.SystemError) {
			er.setStatus(EligibilityStatus.SERVER_ERROR);
			return er;
		}

		er.setStatus(EligibilityStatus.SERVER_ERROR);
		return er;
	}

	protected void addNameValue(List<MyNameValue> dest,
			HashMap<String, String> map, String mapName, String name) {
		if (map.get(mapName) != null) {
			MyNameValue nv = new MyNameValue();
			nv.setName(name);
			nv.setValue(map.get(mapName));
			dest.add(nv);
		}
	}

	@Override
	public String[] getPluginConfigurationOptions() {
		return new String[] { "gatewayEdiUsername", "gatewayEdiPassword" };
	}

}
