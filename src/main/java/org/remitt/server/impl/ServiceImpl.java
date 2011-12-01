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

package org.remitt.server.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.apache.log4j.Logger;
import org.remitt.datastore.DbEligibilityJob;
import org.remitt.datastore.DbFileStore;
import org.remitt.datastore.KeyringStore;
import org.remitt.datastore.UserManagement;
import org.remitt.prototype.ConfigurationOption;
import org.remitt.prototype.EligibilityInterface;
import org.remitt.prototype.EligibilityRequest;
import org.remitt.prototype.EligibilityResponse;
import org.remitt.prototype.FileListingItem;
import org.remitt.prototype.ParserInterface;
import org.remitt.prototype.PluginInterface;
import org.remitt.prototype.UserDTO;
import org.remitt.prototype.ValidationInterface;
import org.remitt.prototype.ValidationResponse;
import org.remitt.server.Configuration;
import org.remitt.server.DbUtil;
import org.remitt.server.Service;

@WebService(targetNamespace = "http://server.remitt.org/", endpointInterface = "org.remitt.server.Service", serviceName = "RemittService")
public class ServiceImpl implements Service {

	@Resource
	WebServiceContext context;

	static final Logger log = Logger.getLogger(Service.class);

	@GET
	@Path("protocolversion")
	@Produces("application/json")
	public String getProtocolVersion() {
		return "2.0";
	}

	@POST
	@Path("changepassword/{pw}")
	@Produces("application/json")
	public Boolean changePassword(String newPassword) {
		Connection c = getConnection();

		String userName = getCurrentUserName();

		Boolean returnValue = false;
		PreparedStatement cStmt = null;
		try {
			cStmt = c
					.prepareCall("UPDATE tUser SET passhash = MD5( ? ) WHERE username = ?;");

			cStmt.setString(1, newPassword);
			cStmt.setString(2, userName);

			cStmt.execute();
			returnValue = (cStmt.getUpdateCount() == 1);
		} catch (NullPointerException npe) {
			log.error("Caught NullPointerException", npe);
		} catch (SQLException e) {
			log.error("Caught SQLException", e);
		} finally {
			DbUtil.closeSafely(cStmt);
			DbUtil.closeSafely(c);
		}
		return returnValue;
	}

	@GET
	@Path("username")
	@Produces("application/json")
	public String getCurrentUserName() {
		MessageContext ctx = context.getMessageContext();
		if (ctx != null) {
			return (String) ctx.get("principal");
		}
		return context.getUserPrincipal().getName();
	}

	@POST
	@Path("submit")
	@Produces("application/json")
	public Integer insertPayload(String originalId, String inputPayload,
			String renderPlugin, String renderOption, String transportPlugin,
			String transportOption) {
		Connection c = getConnection();

		String userName = getCurrentUserName();

		log.debug("Submit job for " + userName + " [payload length = "
				+ inputPayload.length() + "]");

		Integer returnValue = null;
		PreparedStatement cStmt = null;
		try {
			cStmt = c.prepareStatement("INSERT INTO tPayload ( "
					+ "user, payload, renderPlugin, renderOption, "
					+ "transportPlugin, transportOption, originalId "
					+ " ) VALUES ( ?, ?, ?, ?, ?, ?, ? );",
					PreparedStatement.RETURN_GENERATED_KEYS);

			cStmt.setString(1, userName);
			cStmt.setString(2, inputPayload);
			cStmt.setString(3, renderPlugin);
			cStmt.setString(4, renderOption);
			cStmt.setString(5, transportPlugin);
			cStmt.setString(6, transportOption);
			cStmt.setString(7, originalId);

			@SuppressWarnings("unused")
			boolean hadResults = cStmt.execute();
			ResultSet newKey = cStmt.getGeneratedKeys();
			newKey.next();
			returnValue = newKey.getInt(1);
			DbUtil.closeSafely(newKey);
		} catch (NullPointerException npe) {
			log.error("Caught NullPointerException", npe);
		} catch (SQLException e) {
			log.error("Caught SQLException", e);
		} finally {
			DbUtil.closeSafely(cStmt);
			DbUtil.closeSafely(c);
		}
		return returnValue;
	}

	@POST
	@Path("getoptions")
	@Produces("application/json")
	public ConfigurationOption[] getConfigValues() {
		String userName = getCurrentUserName();
		return Configuration.getConfigValues(userName);
	}

	@POST
	@Path("setoption/{namespace}/{option}/{value}")
	@Produces("application/json")
	public Boolean setConfigValue(String namespace, String option, String value) {
		String userName = getCurrentUserName();
		try {
			Configuration.setConfigValue(userName, namespace, option, value);
		} catch (Exception ex) {
			log.error(ex);
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}

	@POST
	@Path("getstatus/{jobId}")
	@Produces("application/json")
	public Integer getStatus(Integer jobId) {
		String userName = getCurrentUserName();

		log.info("getStatus called for user = " + userName + ", jobId = "
				+ (jobId == null ? "null" : jobId.toString()));

		Connection c = getConnection();

		CallableStatement cStmt = null;
		int returnValue = 5;
		try {
			cStmt = c.prepareCall("CALL p_GetStatus( ?, ? );");
			cStmt.setString(1, userName);
			cStmt.setInt(2, jobId);

			boolean hadResults = cStmt.execute();
			if (hadResults) {
				ResultSet r = cStmt.getResultSet();
				r.next();
				Integer status = r.getInt("status");
				String stage = r.getString("stage");

				if (stage == null) {
					// Handle null stage
				} else {
					if (status.equals(0)) {
						if (stage.equalsIgnoreCase("validation")) {
							returnValue = 1; // validation
						} else if (stage.equalsIgnoreCase("render")) {
							returnValue = 2; // render
						} else if (stage.equalsIgnoreCase("translation")) {
							returnValue = 3; // translation
						} else if (stage.equalsIgnoreCase("transport")) {
							returnValue = 4; // transport
						} else if (stage.equalsIgnoreCase("failed")) {
							returnValue = 6; // failed
						}
					} else {
						returnValue = 0; // completed
					}
				}

				r.close();
			} else {
				returnValue = 5; // unknown
			}
		} catch (NullPointerException npe) {
			log.error("Caught NullPointerException", npe);
		} catch (SQLException e) {
			log.error("Caught SQLException", e);
		} finally {
			DbUtil.closeSafely(cStmt);
			DbUtil.closeSafely(c);
		}
		return returnValue;
	}

	@POST
	@Path("getbulkstatus/{jobsIds}")
	@Produces("application/json")
	@Override
	public Integer[] getBulkStatus(Integer[] jobIds) {
		List<Integer> ret = new ArrayList<Integer>();
		for (Integer i : jobIds) {
			ret.add(getStatus(i));
		}
		return (Integer[]) ret.toArray(new Integer[0]);
	}

	@POST
	@Path("plugins/{category}")
	@Produces("application/json")
	@Override
	public String[] getPlugins(String category) {
		String userName = getCurrentUserName();

		log.debug("Plugin list for " + userName + " [category = " + category
				+ "]");

		return Configuration.getPlugins(category).toArray(new String[0]);
	}

	@POST
	@Path("file/{category}/{filename}")
	@Produces("application/json")
	@Override
	public byte[] getFile(String category, String fileName) {
		String userName = getCurrentUserName();

		log.debug("getFile for " + userName + " [category = " + category
				+ ", filename = " + fileName + "]");

		return DbFileStore.getFile(userName, category, fileName);
	}

	@POST
	@Path("pluginoptions/{pluginclass}/{qualifyingoption}")
	@Produces("application/json")
	@Override
	public String[] getPluginOptions(String pluginClass, String qualifyingOption) {
		PluginInterface p = null;
		try {
			p = (PluginInterface) Class.forName(pluginClass).newInstance();
		} catch (InstantiationException e) {
			log.error(e);
			return null;
		} catch (IllegalAccessException e) {
			log.error(e);
			return null;
		} catch (ClassNotFoundException e) {
			log.error(e);
			return null;
		}
		return p.getPluginConfigurationOptions();
	}

	@POST
	@Path("filelist/{category}/{criteria}/{value}")
	@Produces("application/json")
	@Override
	public FileListingItem[] getFileList(String category, String criteria,
			String value) {
		Connection c = getConnection();

		String userName = getCurrentUserName();

		log.debug("getFileList for " + userName + " [criteria = "
				+ (criteria == null ? "null" : criteria) + ", value = "
				+ (value == null ? "null" : value) + ", category = " + category
				+ "]");

		FileListingItem[] returnValue = null;
		PreparedStatement cStmt = null;
		String queryBase = "SELECT f.filename " + " , f.contentsize "
				+ " , p.originalId " + " , p.insert_stamp "
				+ " FROM tFileStore f "
				+ " LEFT OUTER JOIN tPayload p ON p.id = f.payloadId "
				+ " WHERE f.user = ? " + " AND f.category = ? " + " AND ";
		try {
			if (criteria.equalsIgnoreCase("month")) {
				cStmt = c.prepareStatement(queryBase
						+ " DATE_FORMAT(f.stamp, '%Y-%m') = ? " + ";");
			} else if (criteria.equalsIgnoreCase("year")) {
				cStmt = c.prepareStatement(queryBase
						+ " DATE_FORMAT(f.stamp, '%Y') = ? " + ";");
			} else if (criteria.equalsIgnoreCase("payload")) {
				cStmt = c.prepareStatement(queryBase + " f.payloadId = ? "
						+ ";");
			} else {
				DbUtil.closeSafely(cStmt);
				DbUtil.closeSafely(c);
				return null;
			}
			cStmt.setString(1, userName);
			cStmt.setString(2, category);
			cStmt.setString(3, value);

			boolean hadResults = cStmt.execute();
			List<FileListingItem> results = new ArrayList<FileListingItem>();
			if (hadResults) {
				ResultSet rs = cStmt.getResultSet();
				while (rs.next()) {
					FileListingItem i = new FileListingItem();
					i.setFilename(rs.getString(1));
					i.setFilesize(rs.getInt(2));
					i.setOriginalId(rs.getString(3));
					i.setInserted(rs.getDate(4));
					results.add(i);
				}
				rs.close();
			}
			returnValue = results.toArray(new FileListingItem[0]);
		} catch (NullPointerException npe) {
			log.error("Caught NullPointerException", npe);
		} catch (SQLException e) {
			log.error("Caught SQLException", e);
		} finally {
			DbUtil.closeSafely(cStmt);
			DbUtil.closeSafely(c);
		}
		return returnValue;
	}

	@POST
	@Path("outputmonths/{targetyear}")
	@Produces("application/json")
	@Override
	public String[] getOutputMonths(Integer targetYear) {
		Connection c = getConnection();

		String userName = getCurrentUserName();

		log.debug("getOutputMonths for " + userName + " [targetYear = "
				+ targetYear + "]");

		String[] returnValue = null;
		PreparedStatement cStmt = null;
		try {
			cStmt = c
					.prepareStatement("SELECT DATE_FORMAT(stamp, '%Y-%m') AS m "
							+ " FROM tFileStore "
							+ " WHERE user = ? AND YEAR(stamp) = ? "
							+ " GROUP BY m ;");
			cStmt.setString(1, userName);
			cStmt.setInt(2, targetYear);

			boolean hadResults = cStmt.execute();
			List<String> results = new ArrayList<String>();
			if (hadResults) {
				ResultSet rs = cStmt.getResultSet();
				while (rs.next()) {
					results.add(rs.getString("m"));
				}
				rs.close();
			}
			returnValue = results.toArray(new String[0]);
		} catch (NullPointerException npe) {
			log.error("Caught NullPointerException", npe);
		} catch (SQLException e) {
			log.error("Caught SQLException", e);
		} finally {
			DbUtil.closeSafely(cStmt);
			DbUtil.closeSafely(c);
		}
		return returnValue;
	}

	@POST
	@Path("outputyears")
	@Produces("application/json")
	@Override
	public Integer[][] getOutputYears() {
		Connection c = getConnection();

		String userName = getCurrentUserName();

		log.debug("getOutputYears for " + userName);

		Integer[][] returnValue = null;
		PreparedStatement cStmt = null;
		try {
			cStmt = c.prepareStatement("SELECT "
					+ " DISTINCT(YEAR(stamp)) AS year "
					+ ", COUNT(YEAR(stamp)) AS c " + " FROM tFileStore "
					+ "WHERE user = ? " + " GROUP BY YEAR(stamp) " + ";");
			cStmt.setString(1, userName);

			boolean hadResults = cStmt.execute();
			List<Integer[]> results = new ArrayList<Integer[]>();
			if (hadResults) {
				ResultSet rs = cStmt.getResultSet();
				while (rs.next()) {
					List<Integer> atomicResult = new ArrayList<Integer>();
					atomicResult.add(rs.getInt(1));
					atomicResult.add(rs.getInt(2));
					results.add(atomicResult.toArray(new Integer[0]));
				}
				rs.close();
			}
			returnValue = results.toArray(new Integer[0][0]);
		} catch (NullPointerException npe) {
			log.error("Caught NullPointerException", npe);
		} catch (SQLException e) {
			log.error("Caught SQLException", e);
		} finally {
			DbUtil.closeSafely(cStmt);
			DbUtil.closeSafely(c);
		}
		return returnValue;
	}

	@POST
	@Path("eligibility")
	@Produces("application/json")
	@Override
	public EligibilityResponse getEligibility(EligibilityRequest request) {
		String userName = getCurrentUserName();

		EligibilityInterface p = null;
		try {
			p = (EligibilityInterface) Class.forName(request.getPlugin())
					.newInstance();
		} catch (InstantiationException e) {
			log.error(e);
			return null;
		} catch (IllegalAccessException e) {
			log.error(e);
			return null;
		} catch (ClassNotFoundException e) {
			log.error(e);
			return null;
		}
		try {
			return p.checkEligibility(userName, request.getRequest());
		} catch (Exception e) {
			log.error(e);
			return null;
		}
	}

	@POST
	@Path("eligibilitybatch")
	@Produces("application/json")
	@Override
	public Integer batchEligibilityCheck(EligibilityRequest[] requests) {
		String userName = getCurrentUserName();
		for (EligibilityRequest param : requests) {
			DbEligibilityJob.addEligibilityJob(userName, param.getPlugin(),
					param.getRequest());
		}
		return 1;
	}

	@POST
	@Path("parse")
	@Produces("application/json")
	@Override
	public String parseData(String parserClass, String data) {
		// String userName = getCurrentUserName();

		ParserInterface p = null;
		try {
			p = (ParserInterface) Class.forName(parserClass).newInstance();
		} catch (InstantiationException e) {
			log.error(e);
			return null;
		} catch (IllegalAccessException e) {
			log.error(e);
			return null;
		} catch (ClassNotFoundException e) {
			log.error(e);
			return null;
		}
		try {
			return p.parseData(data);
		} catch (Exception e) {
			log.error(e);
			return null;
		}
	}

	@POST
	@Path("addkey/{keyname}/{privatekey}/{publickey}")
	@Produces("application/json")
	@Override
	public boolean addKeyToKeyring(String keyname, byte[] privatekey,
			byte[] publickey) {
		return KeyringStore.putKey(getCurrentUserName(), keyname, privatekey,
				publickey);
	}

	@POST
	@Path("adduser/{user}")
	@Produces("application/json")
	@Override
	public boolean addRemittUser(UserDTO user) {
		if (!context.isUserInRole("admin")) {
			log.error("Attempt to add a user by a non-admin account");
			return false;
		}
		return UserManagement.addUser(user.getUsername(), user.getPassword(),
				user.getCallbackServiceUri(), user.getCallbackServiceWsdlUri(),
				user.getCallbackUsername(), user.getCallbackPassword());
	}

	@POST
	@Path("listusers")
	@Produces("application/json")
	@Override
	public UserDTO[] listRemittUsers() {
		if (!context.isUserInRole("admin")) {
			log.error("Attempt to list users by a non-admin account");
			return null;
		}
		return UserManagement.listUsers().toArray(new UserDTO[0]);
	}

	@POST
	@Path("validate/{validatorClass}")
	@Produces("application/json")
	@Override
	public ValidationResponse validatePayload(String validatorClass, byte[] data) {
		String userName = getCurrentUserName();

		ValidationInterface v = null;
		try {
			v = (ValidationInterface) Class.forName(validatorClass)
					.newInstance();
		} catch (InstantiationException e) {
			log.error(e);
			return null;
		} catch (IllegalAccessException e) {
			log.error(e);
			return null;
		} catch (ClassNotFoundException e) {
			log.error(e);
			return null;
		}
		try {
			return v.validate(userName, data);
		} catch (Exception e) {
			log.error(e);
			return null;
		}
	}

	/**
	 * Internal method to get a database connection.
	 * 
	 * @return
	 */
	protected Connection getConnection() {
		// Connection c = (Connection) Configuration.getServletContext()
		// .getServletContext().getAttribute("connection");
		// return c;
		return Configuration.getConnection();
	}

}
