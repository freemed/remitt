/*
 * $Id$
 *
 * Authors:
 *      Jeff Buchbinder <jeff@freemedsoftware.org>
 *
 * REMITT Electronic Medical Information Translation and Transmission
 * Copyright (C) 1999-2014 FreeMED Software Foundation
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

public enum EligibilityParameter {

	ELIGIBILITY_PARAMETER_NPI("npi"), ELIGIBILITY_PARAMETER_INSURANCE_ID(
			"insuranceId"), ELIGIBILITY_PARAMETER_INSURED_LAST_NAME(
			"insuredLastName"), ELIGIBILITY_PARAMETER_INSURED_FIRST_NAME(
			"insuredFirstName"), ELIGIBILITY_PARAMETER_INSURED_DOB(
			"insuredDateOfBirth"), ELIGIBILITY_PARAMETER_INSURED_GENDER(
			"insuredGender"), ELIGIBILITY_PARAMETER_INSURED_STATE(
			"insuredState"), ELIGIBILITY_PARAMETER_INSURED_SSN("insuredSsn"), ELIGIBILITY_PARAMETER_DEPENDENT_LAST_NAME(
			"dependentLastName"), ELIGIBILITY_PARAMETER_DEPENDENT_FIRST_NAME(
			"dependentFirstName"), ELIGIBILITY_PARAMETER_DEPENDENT_DOB(
			"dependentDateOfBirth"), ELIGIBILITY_PARAMETER_DEPENDENT_GENDER(
			"dependentGender"), ELIGIBILITY_PARAMETER_DEPENDENT_RELATIONSHIP(
			"dependentRelationship"), ELIGIBILITY_PARAMETER_SERVICE_TYPE(
			"serviceType"), ELIGIBILITY_PARAMETER_CARD_ISSUE_DATE(
			"cardIssueDate"), ELIGIBILITY_PARAMETER_GROUP_NUMBER("groupId"), ELIGIBILITY_PARAMETER_DATE_OF_SERVICE(
			"serviceDate");

	private final String value;

	private EligibilityParameter(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}

	public String toString() {
		return this.value;
	}
}
