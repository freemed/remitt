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

package org.remitt.server;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.sojo.interchange.Serializer;
import net.sf.sojo.interchange.json.JsonSerializer;

import org.apache.log4j.Logger;

/**
 * Servlet implementation class TestHarnessServlet
 */
public class UserConfigurationServlet extends HttpServlet {

	protected class UserConfig {
		public String namespace;
		public String option;
		public String value;

		public void setNamespace(String v) {
			namespace = v;
		}

		public void setOption(String v) {
			option = v;
		}

		public void setValue(String v) {
			option = v;
		}
	}

	private static final long serialVersionUID = 5083654958306724495L;

	static final Logger log = Logger.getLogger(UserConfigurationServlet.class);

	public UserConfigurationServlet() {
	}

	public void init() throws ServletException {
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String action = request.getParameter("action");

		if (action.compareTo("set") == 0) {
			log.info("set action");
			String user = request.getRemoteUser();
			String namespace = request.getParameter("namespace");
			String option = request.getParameter("option");
			String value = request.getParameter("value");
			Configuration.setConfigValue(user, namespace, option, value);
		} else if (action.compareTo("getAll") == 0) {
			log.info("getAll action");
			String user = request.getRemoteUser();
			String namespace = request.getParameter("namespace");
			UserConfig[] c = getConfigValues(user, namespace);
			// Serialize and return values
			Serializer serializer = new JsonSerializer();
			OutputStream outputStream = response.getOutputStream();
			outputStream.write(serializer.serialize(c).toString().getBytes(
					Charset.defaultCharset()));
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
	}

	protected UserConfig[] getConfigValues(String user, String namespace) {
		log.info("getConfigValues: " + user + ", " + namespace);

		List<UserConfig> results = new ArrayList<UserConfig>();

		Connection c = Configuration.getConnection();
		PreparedStatement cStmt = null;
		try {
			if (namespace != null) {
				cStmt = c
						.prepareStatement("{ SELECT * FROM tUserConfig WHERE user = ? AND cNamespace = ? }");
				cStmt.setString(1, user);
				cStmt.setString(2, namespace);
			} else {
				cStmt = c
						.prepareStatement("{ SELECT * FROM tUserConfig WHERE user = ? AND cNamespace = ? }");
				cStmt.setString(1, user);
			}
			cStmt.execute();

			ResultSet rs = cStmt.getResultSet();
			while (rs.next()) {
				UserConfig item = new UserConfig();
				item.setNamespace(rs.getString("cNamespace"));
				item.setOption(rs.getString("cOption"));
				item.setValue(rs.getString("cValue"));
				results.add(item);
			}
			rs.close();
		} catch (NullPointerException npe) {
			log.error("Caught NullPointerException", npe);
		} catch (SQLException e) {
			log.error("Caught SQLException", e);
		} finally {
			DbUtil.closeSafely(cStmt);
			DbUtil.closeSafely(c);
		}

		return results.toArray(new UserConfig[0]);
	}

}
