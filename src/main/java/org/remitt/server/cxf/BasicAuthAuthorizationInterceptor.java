/*
 * $Id$
 *
 * Authors:
 *      Jeff Buchbinder <jeff@freemedsoftware.org>
 *
 * CXF Interceptor that provides HTTP Basic Authentication validation.
 * 
 * Based on the concepts outline here:
 *    http://chrisdail.com/2008/03/31/apache-cxf-with-http-basic-authentication
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

package org.remitt.server.cxf;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.cxf.binding.soap.interceptor.SoapHeaderInterceptor;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.apache.log4j.Logger;
import org.remitt.server.Configuration;
import org.remitt.server.DbUtil;

public class BasicAuthAuthorizationInterceptor extends SoapHeaderInterceptor {

	protected boolean DEBUG = true;

	public static final String REALM = "REMITT Services";

	public static final String SQL_GET_USERS = "SELECT "
			+ " u.username AS username, " + " u.passhash AS passhash "
			+ " FROM tUser u "
			+ " LEFT OUTER JOIN tRole r ON r.username = u.username "
			+ " WHERE r.rolename = 'default' " + " GROUP BY u.username;";

	protected Logger log = Logger
			.getLogger(BasicAuthAuthorizationInterceptor.class);

	private static final char[] HEX_CHARS = { '0', '1', '2', '3', '4', '5',
			'6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', };

	/** Map of allowed users to this system with their corresponding passwords. */
	private static Map<String, String> users = null;

	/* -Required */
	public void setUsers(Map<String, String> u) {
		users = u;
	}

	/**
	 * Cache user credentials in the system.
	 */
	protected void loadUsers() {
		debug("BasicAuthAuthorizationInterceptor.loadUsers() called");
		if (users == null) {
			debug("BasicAuthAuthorizationInterceptor.loadUsers(): users not loaded, loading");
			Configuration.loadConfiguration();
			Connection conn = Configuration.getConnection();
			users = new HashMap<String, String>();
			PreparedStatement cStmt = null;
			try {
				cStmt = conn.prepareStatement(SQL_GET_USERS);
				if (cStmt.execute()) {
					ResultSet r = cStmt.getResultSet();
					while (r.next()) {
						String user = r.getString("username");
						String pass = r.getString("passhash");
						users.put(user, pass);
						debug("Found user = " + user + ", passhash = " + pass);
					}
					r.close();
				}
			} catch (NullPointerException npe) {
				log.error("Caught NullPointerException", npe);
				debug("Caught NullPointerException: " + npe.toString());
			} catch (SQLException e) {
				log.error("Caught SQLException", e);
				debug("Caught SQLException: " + e.toString());
			} finally {
				DbUtil.closeSafely(cStmt);
				DbUtil.closeSafely(conn);
			}
		}
	}

	@Override
	public void handleMessage(Message message) throws Fault {
		debug("BasicAuthAuthorizationInterceptor.handleMessage() called");

		/*
		 * Attempt to load users hash, will skip if users have already been
		 * loaded.
		 */
		loadUsers();

		// This is set by CXF
		AuthorizationPolicy policy = message.get(AuthorizationPolicy.class);

		/*
		 * If the policy is not set, the user did not specify credentials, a 401
		 * is sent to the client to indicate that authentication is required
		 */
		if (policy == null) {
			if (log.isDebugEnabled()) {
				log.debug("User attempted to log in with no credentials");
				debug("User attempted to log in with no credentials");
			}
			sendErrorResponse(message, HttpURLConnection.HTTP_UNAUTHORIZED);
			return;
		}

		if (log.isDebugEnabled()) {
			log.debug("Logging in use: " + policy.getUserName());
		}

		// Verify the password
		String realPassword = users.get(policy.getUserName());
		if (DEBUG) {
			debug("md5 hash of users.get(user) = " + realPassword);
			debug("md5 hash of policy's password = "
					+ md5hash(policy.getPassword()));
		}
		if (realPassword == null
				|| !realPassword.equals(md5hash(policy.getPassword()))) {
			log.warn("Invalid username or password for user: "
					+ policy.getUserName());
			debug("Invalid username or password for user: "
					+ policy.getUserName());
			sendErrorResponse(message, HttpURLConnection.HTTP_FORBIDDEN);
		}
		debug("Message should be clear to finish being handled, auth succeeded");
		message.getInterceptorChain().resume();
	}

	@SuppressWarnings("unchecked")
	private void sendErrorResponse(Message message, int responseCode) {
		Message outMessage = getOutMessage(message);
		outMessage.put(Message.RESPONSE_CODE, responseCode);

		// Set the response headers
		Map<String, List<String>> responseHeaders = (Map<String, List<String>>) message
				.get(Message.PROTOCOL_HEADERS);
		if (responseHeaders != null) {
			responseHeaders.put("WWW-Authenticate", Arrays
					.asList(new String[] { "Basic realm=" + REALM }));
			responseHeaders.put("Content-length", Arrays
					.asList(new String[] { "0" }));
		}
		message.getInterceptorChain().abort();
		try {
			getConduit(message).prepare(outMessage);
			close(outMessage);
		} catch (IOException e) {
			log.warn(e.getMessage(), e);
		}
	}

	private Message getOutMessage(Message inMessage) {
		Exchange exchange = inMessage.getExchange();
		Message outMessage = exchange.getOutMessage();
		if (outMessage == null) {
			Endpoint endpoint = exchange.get(Endpoint.class);
			outMessage = endpoint.getBinding().createMessage();
			exchange.setOutMessage(outMessage);
		}
		outMessage.putAll(inMessage);
		return outMessage;
	}

	private Conduit getConduit(Message inMessage) throws IOException {
		Exchange exchange = inMessage.getExchange();
		EndpointReferenceType target = exchange
				.get(EndpointReferenceType.class);
		Conduit conduit = exchange.getDestination().getBackChannel(inMessage,
				null, target);
		exchange.setConduit(conduit);
		return conduit;
	}

	private void close(Message outMessage) throws IOException {
		OutputStream os = outMessage.getContent(OutputStream.class);
		os.flush();
		os.close();
	}

	/**
	 * Get MD5 hash of a string.
	 * 
	 * @param original
	 * @return
	 */
	protected String md5hash(String original) {
		log.info("md5 hash for " + original);
		MessageDigest digest = null;
		try {
			digest = java.security.MessageDigest.getInstance("MD5");
			digest.update(original.getBytes());
			byte[] hash = digest.digest();
			String hashed = asHex(hash);
			log.info("md5 hashed to " + hashed);
			debug("md5 hashed to " + hashed);
			return hashed;
		} catch (NoSuchAlgorithmException e) {
			log.error("Could not find MD5 algorithm", e);
			debug("Could not find MD5 algorithm: " + e.toString());
		}
		return null;
	}

	/**
	 * Turns array of bytes into string representing each byte as unsigned hex
	 * number.
	 * 
	 * @param hash
	 *            Array of bytes to convert to hex-string
	 * @return Generated hex string
	 */
	protected String asHex(byte hash[]) {
		char buf[] = new char[hash.length * 2];
		for (int i = 0, x = 0; i < hash.length; i++) {
			buf[x++] = HEX_CHARS[(hash[i] >>> 4) & 0xf];
			buf[x++] = HEX_CHARS[hash[i] & 0xf];
		}
		return new String(buf);
	}

	protected void debug(String st) {
		if (DEBUG) {
			System.out.println(this.getClass().getName() + "| " + st);
		}
	}

}
