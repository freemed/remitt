package org.remitt.plugin.eligibility;

import java.io.ByteArrayInputStream;
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
			Map<EligibilityParameter, String> values) throws Exception {
		// Form X12 5010 270 message
		X12 m = new X12(new Context());

		{
			// ST : ST*270*0001*005010X279A1~
			Segment ST = m.addSegment();
			ST.addElement(1, "270");
			ST.addElement(2, "0001"); // FIXME: should increment
			ST.addElement(3, "005010X279A1");
		}

		{
			// BHT
			Segment BHT = m.addSegment();
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
		}

		// Loop 2000

		{
			// HL : HL*1**20*1~
			Segment HL = m.addSegment();
			HL.addElement(1, "1");
			HL.addElement(2, "");
			HL.addElement(3, "20"); // 20 = information source
			HL.addElement(4, "1"); // 1 = additional HL segments
		}

		// Loop 2100A

		{
			// NM1 : NM1*PR*2*ABC INSURANCE COMPANY*****PI*842610001~
			Segment NM1 = m.addSegment();
			NM1.addElement(1, "PR"); // PR = payer
			NM1.addElement(2, "2"); // 2 = non person entity
			NM1.addElement(3, getInsuranceName());
			NM1.addElement(4, "");
			NM1.addElement(5, "");
			NM1.addElement(6, "");
			NM1.addElement(7, "");
			NM1.addElement(8, "PI"); // PI = payer identifier
			NM1.addElement(8, getETIN());
		}

		// Loop 2000B

		{
			// HL : HL*2*1*21*1~
			Segment HL = m.addSegment();
			HL.addElement(1, "2"); // this HL segment
			HL.addElement(2, "1"); // parent number
			HL.addElement(3, "21"); // 21 = information receiver
			HL.addElement(4, "1");
		}

		// Loop 2100B

		{
			// NM1 :
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
