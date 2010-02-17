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

package org.remitt.server.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.xml.ws.WebServiceContext;

import org.apache.log4j.Logger;
import org.remitt.prototype.EligibilityInterface;
import org.remitt.prototype.EligibilityResponse;
import org.remitt.prototype.PluginInterface;
import org.remitt.server.Configuration;
import org.remitt.server.Service;

@WebService(endpointInterface = "org.remitt.server.Service", serviceName = "remittService")
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
	public Boolean changePassword(@PathParam("pw") String newPassword) {
		Connection c = getConnection();

		String userName = getCurrentUserName();

		PreparedStatement cStmt = null;
		try {
			cStmt = c
					.prepareCall("UPDATE tUser SET passhash = MD5( ? ) WHERE username = ?;");

			cStmt.setString(1, newPassword);
			cStmt.setString(2, userName);

			@SuppressWarnings("unused")
			boolean hadResults = cStmt.execute();

			Boolean returnValue = (cStmt.getUpdateCount() == 1);
			try {
				cStmt.close();
			} catch (Exception ex) {
			}
			return returnValue;
		} catch (NullPointerException npe) {
			log.error("Caught NullPointerException", npe);
			try {
				cStmt.close();
			} catch (Exception ex) {
			}
			return false;
		} catch (SQLException e) {
			log.error("Caught SQLException", e);
			try {
				cStmt.close();
			} catch (Exception ex) {
			}
			return false;
		}
	}

	@GET
	@Path("username")
	@Produces("application/json")
	public String getCurrentUserName() {
		return context.getUserPrincipal().getName();
	}

	@POST
	@Path("submit")
	@Produces("application/json")
	public Integer insertPayload(
			@PathParam("inputPayload") String inputPayload,
			@PathParam("renderPlugin") String renderPlugin,
			@PathParam("renderOption") String renderOption,
			@PathParam("transportPlugin") String transportPlugin,
			@PathParam("transportOption") String transportOption) {
		Connection c = getConnection();

		String userName = getCurrentUserName();

		log.debug("Submit job for " + userName + " [payload length = "
				+ inputPayload.length() + "]");

		PreparedStatement cStmt = null;
		try {
			cStmt = c.prepareStatement("INSERT INTO tPayload ( "
					+ "user, payload, renderPlugin, renderOption, "
					+ "transportPlugin, transportOption "
					+ " ) VALUES ( ?, ?, ?, ?, ?, ? );",
					PreparedStatement.RETURN_GENERATED_KEYS);

			cStmt.setString(1, userName);
			cStmt.setString(2, inputPayload);
			cStmt.setString(3, renderPlugin);
			cStmt.setString(4, renderOption);
			cStmt.setString(5, transportPlugin);
			cStmt.setString(6, transportOption);

			@SuppressWarnings("unused")
			boolean hadResults = cStmt.execute();
			ResultSet newKey = cStmt.getGeneratedKeys();
			Integer returnValue = newKey.getInt("id");
			newKey.close();
			try {
				cStmt.close();
			} catch (Exception ex) {
			}
			return returnValue;
		} catch (NullPointerException npe) {
			log.error("Caught NullPointerException", npe);
			try {
				cStmt.close();
			} catch (Exception ex) {
			}
			return null;
		} catch (SQLException e) {
			log.error("Caught SQLException", e);
			try {
				cStmt.close();
			} catch (Exception ex) {
			}
			return null;
		}
	}

	@POST
	@Path("setoption/{namespace}/{option}/{value}")
	@Produces("application/json")
	public Boolean setConfigValue(@PathParam("namespace") String namespace,
			@PathParam("option") String option, @PathParam("value") String value) {
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
	@Path("getstatus/{jobid}")
	@Produces("application/json")
	public Integer getStatus(@PathParam("jobid") Integer jobId) {
		String userName = getCurrentUserName();

		Connection c = getConnection();

		CallableStatement cStmt = null;
		try {
			cStmt = c.prepareCall("{ CALL p_GetStatus( ?, ? ); }");
			cStmt.setString(1, userName);
			cStmt.setInt(2, jobId);

			boolean hadResults = cStmt.execute();
			int returnValue = 5;
			if (hadResults) {
				ResultSet r = cStmt.getResultSet();
				String status = r.getString("status");
				String stage = r.getString("stage");

				if (status.equalsIgnoreCase("incomplete")) {
					if (status.equalsIgnoreCase("validation")) {
						returnValue = 1; // validation
					} else if (status.equalsIgnoreCase("render")) {
						returnValue = 2; // render
					} else if (status.equalsIgnoreCase("translation")) {
						returnValue = 3; // translation
					} else if (status.equalsIgnoreCase("transmission")) {
						returnValue = 4; // transmission/transport
					}
				} else {
					returnValue = 0; // completed
				}

				r.close();
			} else {
				returnValue = 5; // unknown
			}

			try {
				cStmt.close();
			} catch (Exception ex) {
			}

			return returnValue;
		} catch (NullPointerException npe) {
			log.error("Caught NullPointerException", npe);
			try {
				cStmt.close();
			} catch (Exception ex) {
			}
			return null;
		} catch (SQLException e) {
			log.error("Caught SQLException", e);
			try {
				cStmt.close();
			} catch (Exception ex) {
			}
			return null;
		}
	}

	@POST
	@Path("plugins/{category}")
	@Produces("application/json")
	@Override
	public String[] getPlugins(@PathParam("category") String category) {
		if (category.equalsIgnoreCase("validation")) {
			category = "validation"; // validation
		} else if (category.equalsIgnoreCase("render")) {
			category = "render"; // render
		} else if (category.equalsIgnoreCase("translation")) {
			category = "translation"; // translation
		} else if (category.equalsIgnoreCase("transmission")) {
			category = "transmission"; // transmission/transport
		} else if (category.equalsIgnoreCase("eligibility")) {
			category = "eligibility"; // eligibility
		} else {
			// No plugins for dud categories.
			return null;
		}

		Connection c = getConnection();

		String userName = getCurrentUserName();

		log.debug("Plugin list for " + userName + " [category = " + category
				+ "]");

		String[] returnValue = null;
		PreparedStatement cStmt = null;
		try {
			cStmt = c.prepareStatement("SELECT * FROM tPlugins "
					+ "WHERE category = ?");

			cStmt.setString(1, category);

			boolean hadResults = cStmt.execute();
			List<String> results = new ArrayList<String>();
			if (hadResults) {
				ResultSet rs = cStmt.getResultSet();
				while (rs.next()) {
					results.add(rs.getString("plugin"));
				}
				rs.close();
			}
			returnValue = results.toArray(new String[0]);
			try {
				cStmt.close();
			} catch (Exception ex) {
			}
			return returnValue;
		} catch (NullPointerException npe) {
			log.error("Caught NullPointerException", npe);
			try {
				cStmt.close();
			} catch (Exception ex) {
			}
			return null;
		} catch (SQLException e) {
			log.error("Caught SQLException", e);
			try {
				cStmt.close();
			} catch (Exception ex) {
			}
			return null;
		}
	}

	@POST
	@Path("file/{category}/{filename}")
	@Produces("application/json")
	@Override
	public byte[] getFile(@PathParam("category") String category,
			@PathParam("filename") String fileName) {
		// TODO Auto-generated method stub
		return null;
	}

	@POST
	@Path("pluginoptions/{pluginclass}/{qualifyingoption}")
	@Produces("application/json")
	@Override
	public String[] getPluginOptions(
			@PathParam("pluginclass") String pluginClass,
			@PathParam("qualifyingoption") String qualifyingOption) {
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
	public String[] getFileList(@PathParam("category") String category,
			@PathParam("criteria") String criteria,
			@PathParam("value") String value) {
		// TODO Auto-generated method stub
		return null;
	}

	@POST
	@Path("outputmonths/{targetyear}")
	@Produces("application/json")
	@Override
	public String[] getOutputMonths(@PathParam("targetyear") Integer targetYear) {
		// TODO Auto-generated method stub
		return null;
	}

	@POST
	@Path("outputyears")
	@Produces("application/json")
	@Override
	public Integer[] getOutputYears() {
		// TODO Auto-generated method stub
		return null;
	}

	@POST
	@Path("eligibility")
	@Produces("application/json")
	@Override
	public EligibilityResponse getEligibility(String plugin,
			HashMap<String, String> parameters) {
		String userName = getCurrentUserName();

		EligibilityInterface p = null;
		try {
			p = (EligibilityInterface) Class.forName(plugin).newInstance();
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
			return p.checkEligibility(userName, parameters);
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
