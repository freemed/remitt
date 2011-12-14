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

	public NCMedicaidEligibility() {
	}

	@Override
	public String getSftpHost(String userName) {
		return "claims.ncmedicaid.com";
	}

	@Override
	public Integer getSftpPort(String userName) {
		return 22;
	}

	@Override
	public String getSftpPath(String userName) {
		return "In";
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

}
