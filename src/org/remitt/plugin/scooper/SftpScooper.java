/*
 * $Id$
 *
 * Authors:
 *      Jeff Buchbinder <jeff@freemedsoftware.org>
 *
 * REMITT Electronic Medical Information Translation and Transmission
 * Copyright (C) 1999-2010 FreeMED Software Foundation
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

package org.remitt.plugin.scooper;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.remitt.prototype.ScooperInterface;
import org.remitt.server.Configuration;

import com.sshtools.j2ssh.SftpClient;
import com.sshtools.j2ssh.SshClient;
import com.sshtools.j2ssh.authentication.AuthenticationProtocolState;
import com.sshtools.j2ssh.authentication.PasswordAuthenticationClient;
import com.sshtools.j2ssh.sftp.SftpFile;

public class SftpScooper implements ScooperInterface {

	static final Logger log = Logger.getLogger(SftpScooper.class);

	public static String SCOOPER_CLASS = "org.remitt.plugin.scooper.SftpScooper";
	public static String SCOOPER_ENABLED = "org.remitt.plugin.scooper.SftpScooper.enabled";

	public static String SFTP_USERNAME = "org.remitt.plugin.scooper.SftpScooper.sftpUsername";
	public static String SFTP_PASSWORD = "org.remitt.plugin.scooper.SftpScooper.sftpPassword";
	public static String SFTP_HOST = "org.remitt.plugin.scooper.SftpScooper.sftpHost";
	public static String SFTP_PORT = "org.remitt.plugin.scooper.SftpScooper.sftpPort";
	public static String SFTP_PATH = "org.remitt.plugin.scooper.SftpScooper.sftpPath";

	protected Map<String, String> parameters = null;

	protected String username = "";

	@Override
	public void setParameters(Map<String, String> params) {
		this.parameters = params;
	}

	@Override
	public void setUsername(String user) {
		this.username = user;
	}

	@Override
	public List<Integer> scoop() throws Exception {
		log.info("Beginning scooper run for " + SCOOPER_CLASS);

		List<Integer> results = new ArrayList<Integer>();

		SshClient ssh = new SshClient();

		String host = parameters.get(SFTP_HOST);
		Integer port = Integer.parseInt(parameters.get(SFTP_PORT));
		String sftpUser = parameters.get(SFTP_USERNAME);
		String sftpPassword = parameters.get(SFTP_PASSWORD);
		String sftpPath = parameters.get(SFTP_PATH);

		List<String> previouslyScoopedFiles = Configuration.getScoopedFiles(
				username, SCOOPER_CLASS, host, sftpPath);

		// Perform initial connection
		ssh.connect(host, port);

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

		@SuppressWarnings("unchecked")
		List<SftpFile> filesList = (List<SftpFile>) client.ls();
		for (SftpFile file : filesList) {
			// Skip anything that isn't a file
			if (!(file.isFile() || file.isLink())) {
				log.debug("File not link or file: " + file.getFilename());
				continue;
			}

			if (previouslyScoopedFiles.contains(file.getFilename())) {
				log
						.debug("Found previously scooped file "
								+ file.getFilename());
				continue;
			}

			log.info("Retrieving " + file.getFilename() + " ("
					+ file.getAttributes().getSize() + " bytes)");
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			client.get(file.getFilename(), out);
			log.info("Completed receiving " + file.getFilename());

			byte[] processedOut = postprocess(out.toByteArray(), file
					.getFilename());

			Integer id = Configuration.addScoopedFile(SCOOPER_CLASS, username,
					host, sftpPath, file.getFilename(), processedOut);
			log.info("Added to tScooper [id = " + id.toString() + "]");
			results.add(id);
		}

		// Disconnect
		log.debug("Disconnecting from host");
		client.quit();
		ssh.disconnect();

		return results;
	}

	/**
	 * Post processing stub.
	 * 
	 * @param in
	 * @param filename
	 * @return
	 */
	public byte[] postprocess(byte[] in, String filename) {
		return in;
	}

	public String getHost() {
		return parameters.get(SFTP_HOST);
	}

	public Integer getPort() {
		return Integer.parseInt(parameters.get(SFTP_PORT));
	}

	public String getPath() {
		return parameters.get(SFTP_PATH);
	}

	@Override
	public String getEnabledConfigValue() {
		return SCOOPER_ENABLED;
	}

}
