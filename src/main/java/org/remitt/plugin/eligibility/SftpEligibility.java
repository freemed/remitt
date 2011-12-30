package org.remitt.plugin.eligibility;

import java.io.ByteArrayInputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.pb.x12.Context;
import org.pb.x12.Segment;
import org.pb.x12.X12;
import org.remitt.prototype.EligibilityInterface;
import org.remitt.prototype.EligibilityParameter;
import org.remitt.prototype.EligibilityResponse;
import org.remitt.prototype.EligibilityStatus;
import org.remitt.server.Configuration;

import WebServices.GatewayEDI.MyNameValue;

import com.sshtools.j2ssh.SftpClient;
import com.sshtools.j2ssh.SshClient;
import com.sshtools.j2ssh.authentication.AuthenticationProtocolState;
import com.sshtools.j2ssh.authentication.PasswordAuthenticationClient;
import com.sshtools.j2ssh.transport.IgnoreHostKeyVerification;

public class SftpEligibility implements EligibilityInterface {

	static final Logger log = Logger.getLogger(SftpEligibility.class);

	/**
	 * Should override this method to specify ETIN for a payer.
	 * 
	 * @return
	 */
	public String getETIN() {
		return "";
	}

	public String getInsuranceName() {
		return "";
	}

	public String getInsuranceAddressLine1() {
		return "";
	}

	public String getInsuranceAddressCity() {
		return "";
	}

	public String getInsuranceAddressState() {
		return "";
	}

	public String getInsuranceAddressPostalCode() {
		return "";
	}

	public boolean isProduction() {
		return true;
	}

	@Override
	public String getPluginName() {
		return "SftpEligibility";
	}

	@Override
	public Double getPluginVersion() {
		return 0.1;
	}

	@Override
	public EligibilityResponse checkEligibility(String userName,
			Map<EligibilityParameter, String> values, boolean resubmission,
			Integer jobId) throws Exception {
		// Form X12 5010 270 message
		X12 m = new X12(new Context());
		Integer segmentCount = 0;

		SimpleDateFormat yyyymmdd = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat hhmm = new SimpleDateFormat("hhmm");

		DecimalFormat nineDigitFormat = new DecimalFormat("000000000");

		{
			// ISA
			Segment ISA = m.addSegment("ISA");
			ISA.addElement(1, "00"); // Authorization implementation qualifier
			ISA.addElement(2, "          "); // Authorization information
			ISA.addElement(3, "00"); // No security info present
			ISA.addElement(4, "          "); // Authorization information
			ISA.addElement(5, "ZZ"); // Sender qualifier
			ISA.addElement(6, ""); // Sender id
			ISA.addElement(7, "ZZ"); // Receiver qualifier
			ISA.addElement(8, ""); // Receiver id
			ISA.addElement(9, yyyymmdd.format(new Date())); // YYYYMMDD
			ISA.addElement(10, hhmm.format(new Date())); // HHMM
			ISA.addElement(11, "^"); // Separator
			ISA.addElement(12, "00501");
			ISA.addElement(13, nineDigitFormat.format(jobId)); // Control number
																// (9 digits)
			ISA.addElement(14, "1"); // ACK requested
			ISA.addElement(15, isProduction() ? "P" : "T"); // Production/testing
		}

		{
			// GS
			Segment GS = m.addSegment("GS");
			GS.addElement(1, "XX");
			GS.addElement(2, ""); // Sender code
			GS.addElement(3, ""); // Receiver code
			GS.addElement(4, yyyymmdd.format(new Date())); // YYYYMMDD
			GS.addElement(5, hhmm.format(new Date())); // HHMM
			GS.addElement(6, nineDigitFormat.format(jobId));
			GS.addElement(7, "X");
			GS.addElement(8, "005010X279A1");
		}

		{
			// ST : ST*270*0001*005010X279A1~
			Segment ST = m.addSegment("ST");
			ST.addElement(1, "270");
			ST.addElement(2, "0001"); // FIXME: should increment
			ST.addElement(3, "005010X279A1");
			segmentCount++;
		}

		{
			// BHT
			Segment BHT = m.addSegment("BHT");
			BHT.addElement(1, "0022"); // Information source
			BHT.addElement(2, "13"); // Request
			BHT.addElement(3, ""); // Reference identifier, situational
			BHT.addElement(4,
					new SimpleDateFormat("yyyyMMdd").format(new Date())); // CCYYMMDD
																			// //
																			// FIXME
			BHT.addElement(5, new SimpleDateFormat("HHmmss").format(new Date())); // HHMMSS
																					// //
																					// FIXME
			// BHT.addElement(6, ""); // RT = spend down, RU = medical services
			// reservation
			segmentCount++;
		}

		// Loop 2000

		{
			// HL : HL*1**20*1~
			Segment HL = m.addSegment("HL");
			HL.addElement(1, "1");
			HL.addElement(2, "");
			HL.addElement(3, "20"); // 20 = information source
			HL.addElement(4, "1"); // 1 = additional HL segments
			segmentCount++;
		}

		// Loop 2100A

		{
			// NM1 : NM1*PR*2*ABC INSURANCE COMPANY*****PI*842610001~
			Segment NM1 = m.addSegment("NM1");
			NM1.addElement(1, "PR"); // PR = payer
			NM1.addElement(2, "2"); // 2 = non person entity
			NM1.addElement(3, getInsuranceName());
			NM1.addElement(4, "");
			NM1.addElement(5, "");
			NM1.addElement(6, "");
			NM1.addElement(7, "");
			NM1.addElement(8, "PI"); // PI = payer identifier
			NM1.addElement(8, getETIN());
			segmentCount++;
		}

		// Loop 2000B

		{
			// HL : HL*2*1*21*1~
			Segment HL = m.addSegment("HL");
			HL.addElement(1, "2"); // this HL segment
			HL.addElement(2, "1"); // parent number
			HL.addElement(3, "21"); // 21 = information receiver
			HL.addElement(4, "1");
			segmentCount++;
		}

		// Loop 2100B

		{
			// NM1 :
			Segment NM1 = m.addSegment("NM1");
			NM1.addElement(1, "1P"); // 1P = provider
			NM1.addElement(2, "1"); // entity type person
			NM1.addElement(3, ""); // last name
			NM1.addElement(4, ""); // first name
			NM1.addElement(5, ""); // middle name
			NM1.addElement(6, ""); // prefix
			NM1.addElement(7, ""); // suffix
			NM1.addElement(8, "XX"); // XX = NPI
			NM1.addElement(9,
					values.get(EligibilityParameter.ELIGIBILITY_PARAMETER_NPI)); // NPI
																					// identifier
			segmentCount++;
		}

		if (false) {
			Segment REF = m.addSegment("REF");
			REF.addElement(1, "");
			REF.addElement(2, "");
			REF.addElement(3, "");
			segmentCount++;
		}

		{
			Segment N3 = m.addSegment("N3");
			N3.addElement(1, ""); // address line 1
			N3.addElement(2, ""); // address line 2
			segmentCount++;
		}

		{
			Segment N4 = m.addSegment("N4");
			N4.addElement(1, ""); // city
			N4.addElement(2, ""); // state
			N4.addElement(3, ""); // postal code
			segmentCount++;
		}

		// Loop 2000C: Subscriber Level

		{
			// HL : HL*3*2*22*1~
			Segment HL = m.addSegment("HL");
			HL.addElement(1, "3");
			HL.addElement(2, "2");
			HL.addElement(3, "22"); // 22 = subscriber level
			HL.addElement(4, "1"); // 1 = additional HL segments
			segmentCount++;
		}

		// TRN segment, situational

		// Loop 2100C: Subscriber name

		{
			// NM1 :
			Segment NM1 = m.addSegment("NM1");
			NM1.addElement(1, "IL"); // IL = insured/subscribed
			NM1.addElement(2, "1"); // entity type person
			NM1.addElement(
					3,
					values.get(EligibilityParameter.ELIGIBILITY_PARAMETER_INSURED_LAST_NAME)); // last
																								// name
			NM1.addElement(
					4,
					values.get(EligibilityParameter.ELIGIBILITY_PARAMETER_INSURED_FIRST_NAME)); // first
																								// name
			NM1.addElement(5, ""); // middle name
			NM1.addElement(6, ""); // prefix
			NM1.addElement(7, ""); // suffix
			NM1.addElement(8, "MI"); // MI = member identification number
			NM1.addElement(
					9,
					values.get(EligibilityParameter.ELIGIBILITY_PARAMETER_INSURANCE_ID));
			segmentCount++;
		}

		if (false) {
			Segment REF = m.addSegment("REF");
			REF.addElement(1, "");
			REF.addElement(2, "");
			REF.addElement(3, "");
			segmentCount++;
		}

		{
			Segment N3 = m.addSegment("N3");
			N3.addElement(1, ""); // address line 1
			N3.addElement(2, ""); // address line 2
			segmentCount++;
		}

		{
			Segment N4 = m.addSegment("N4");
			N4.addElement(1, ""); // city
			N4.addElement(
					2,
					values.get(EligibilityParameter.ELIGIBILITY_PARAMETER_INSURED_STATE)); // state
			N4.addElement(3, ""); // postal code
			segmentCount++;
		}

		// PRV - Provider Information

		// DMG - Subscriber demographic information
		{
			Segment DMG = m.addSegment("DMG");
			DMG.addElement(1, "D8"); // D8 = DOB
			DMG.addElement(
					2,
					values.get(EligibilityParameter.ELIGIBILITY_PARAMETER_INSURED_DOB)); // DOB
			DMG.addElement(
					3,
					values.get(EligibilityParameter.ELIGIBILITY_PARAMETER_INSURED_GENDER)); // gender
			segmentCount++;
		}

		// DMG - Subscriber demographic information
		{
			Segment DMG = m.addSegment("DMG");
			DMG.addElement(1, "102"); // issue
			DMG.addElement(2, "D8"); // D8 = date
			DMG.addElement(
					3,
					values.get(EligibilityParameter.ELIGIBILITY_PARAMETER_CARD_ISSUE_DATE)); // date
																								// issued
			segmentCount++;
		}

		// Loop 2100C

		// EQ

		// AMT

		// AMT

		// III

		// REF

		// DTP

		{
			segmentCount++; // this segment
			Segment SE = m.addSegment("SE");
			SE.addElement(1, segmentCount.toString());
			SE.addElement(2, "0001");
		}

		{
			// GE
			Segment GE = m.addSegment("GE");
			GE.addElement(1, "1"); // Number of transactions
			GE.addElement(2, nineDigitFormat.format(jobId)); // == GS06
		}

		{
			// IEA
			Segment IEA = m.addSegment("IEA");
			IEA.addElement(1, "1"); // Number of included functional groups
			IEA.addElement(2, nineDigitFormat.format(jobId)); // Interchange control number
		}

		// Actual transmittal
		transmit(userName, m.toString());

		EligibilityResponse er = new EligibilityResponse();
		er.setStatus(EligibilityStatus.PROCESSING);
		return er;
	}

	protected void transmit(String userName, String payload) throws Exception {
		SshClient ssh = new SshClient();

		String tempPathName = getOutputFileName(payload.getBytes());

		String host = getSftpHost(userName);
		Integer port = getSftpPort(userName);
		String sftpUser = getSftpUser(userName);
		String sftpPassword = getSftpPassword(userName);
		String sftpPath = getSftpPath(userName);

		// Perform initial connection
		ssh.connect(host, port, new IgnoreHostKeyVerification());

		// Authenticate
		PasswordAuthenticationClient passwordAuthenticationClient = new PasswordAuthenticationClient();
		passwordAuthenticationClient.setUsername(sftpUser);
		passwordAuthenticationClient.setPassword(sftpPassword);
		int result = ssh.authenticate(passwordAuthenticationClient);
		if (result != AuthenticationProtocolState.COMPLETE) {
			throw new Exception("Login to " + host + ":" + port + " "
					+ sftpUser + "/" + sftpPassword + " failed");
		}
		// Open the SFTP channel
		SftpClient client = ssh.openSftpClient();

		if (sftpPath != null && sftpPath != "") {
			client.cd(sftpPath);
		}

		// Convert string to input stream for transfer
		ByteArrayInputStream bs = new ByteArrayInputStream(payload.getBytes());

		// Send the file
		client.put(bs, tempPathName);

		// Disconnect
		client.quit();
		ssh.disconnect();
	}

	protected void addNameValue(List<MyNameValue> dest,
			Map<EligibilityParameter, String> map,
			EligibilityParameter mapName, String name) {
		if (map.get(mapName) != null) {
			MyNameValue nv = new MyNameValue();
			nv.setName(name);
			nv.setValue(map.get(mapName));
			dest.add(nv);
		}
	}

	@Override
	public String[] getPluginConfigurationOptions() {
		return new String[] { "sftpUsername", "sftpPassword" };
	}

	protected String getSftpHost(String userName) {
		return Configuration.getPluginOption(this, userName, "sftpHost");
	}

	protected Integer getSftpPort(String userName) {
		return Integer.parseInt(Configuration.getPluginOption(this, userName,
				"sftpPort"));
	}

	protected String getSftpUser(String userName) {
		return Configuration.getPluginOption(this, userName, "sftpUsername");
	}

	protected String getSftpPassword(String userName) {
		return Configuration.getPluginOption(this, userName, "sftpPassword");
	}

	protected String getSftpPath(String userName) {
		return Configuration.getPluginOption(this, userName, "sftpPath");
	}

	protected String getOutputFileName(byte[] input) {
		String outputType = "x12";
		return new Long(System.currentTimeMillis()).toString() + "."
				+ outputType;
	}

}
