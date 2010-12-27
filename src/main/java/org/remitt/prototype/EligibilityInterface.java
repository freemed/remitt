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

package org.remitt.prototype;

import java.util.HashMap;

public interface EligibilityInterface {

	public final String ELIGIBILITY_PARAMETER_NPI = "npi";
	public final String ELIGIBILITY_PARAMETER_INSURANCE_ID = "insuranceId";
	public final String ELIGIBILITY_PARAMETER_INSURED_LAST_NAME = "insuredLastName";
	public final String ELIGIBILITY_PARAMETER_INSURED_FIRST_NAME = "insuredFirstName";
	public final String ELIGIBILITY_PARAMETER_INSURED_DOB = "insuredDateOfBirth";
	public final String ELIGIBILITY_PARAMETER_INSURED_GENDER = "insuredGender";
	public final String ELIGIBILITY_PARAMETER_INSURED_STATE = "insuredState";
	public final String ELIGIBILITY_PARAMETER_INSURED_SSN = "insuredSsn";
	public final String ELIGIBILITY_PARAMETER_DEPENDENT_LAST_NAME = "dependentLastName";
	public final String ELIGIBILITY_PARAMETER_DEPENDENT_FIRST_NAME = "dependentFirstName";
	public final String ELIGIBILITY_PARAMETER_DEPENDENT_DOB = "dependentDateOfBirth";
	public final String ELIGIBILITY_PARAMETER_DEPENDENT_GENDER = "dependentGender";
	public final String ELIGIBILITY_PARAMETER_DEPENDENT_RELATIONSHIP = "dependentRelationship";
	public final String ELIGIBILITY_PARAMETER_SERVICE_TYPE = "serviceType";
	public final String ELIGIBILITY_PARAMETER_CARD_ISSUE_DATE = "cardIssueDate";
	public final String ELIGIBILITY_PARAMETER_GROUP_NUMBER = "groupId";

	public String getPluginName();

	public Double getPluginVersion();

	public EligibilityResponse checkEligibility(String userName,
			HashMap<String, String> values) throws Exception;

	public String[] getPluginConfigurationOptions();

}
