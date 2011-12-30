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

package org.remitt.plugin.eligibility;

import org.apache.log4j.Logger;

public class NCMedicaidEligibility extends SftpEligibility {

	static final Logger log = Logger.getLogger(NCMedicaidEligibility.class);

	public static String INSURANCE_NAME = "NC MEDICAID";
	public static String INSURANCE_ADDRESS_LINE1 = "";
	public static String INSURANCE_ADDRESS_CITY = "";
	public static String INSURANCE_ADDRESS_STATE = "NC";
	public static String INSURANCE_ADDRESS_POSTALCODE = "";
	public static String INSURANCE_ETIN = "";
	public static boolean PRODUCTION = true;

	public static String SFTP_HOST = "claims.ncmedicaid.com";
	public static Integer SFTP_PORT = 22;
	public static String SFTP_PATH = "In";

	public NCMedicaidEligibility() {
	}

	@Override
	public String getSftpHost(String userName) {
		return SFTP_HOST;
	}

	@Override
	public Integer getSftpPort(String userName) {
		return SFTP_PORT;
	}

	@Override
	public String getSftpPath(String userName) {
		return SFTP_PATH;
	}

	@Override
	public String getETIN() {
		return INSURANCE_ETIN;
	}

	@Override
	public String getInsuranceName() {
		return INSURANCE_NAME;
	}

	@Override
	public String getInsuranceAddressLine1() {
		return INSURANCE_ADDRESS_LINE1;
	}

	@Override
	public String getInsuranceAddressCity() {
		return INSURANCE_ADDRESS_CITY;
	}

	@Override
	public String getInsuranceAddressState() {
		return INSURANCE_ADDRESS_STATE;
	}

	@Override
	public String getInsuranceAddressPostalCode() {
		return INSURANCE_ADDRESS_POSTALCODE;
	}

	@Override
	public boolean isProduction() {
		return PRODUCTION;
	}

}
