<%-- 
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
 --%>

<%@ page import="java.sql.*"%>
<%@ page import="org.remitt.server.Configuration"%>
<%@ page import="org.remitt.server.DbUtil"%>
<%@ page import="org.apache.log4j.Logger"%>

<%@ include file="/WEB-INF/jsp/header.jsp"%>

<h2>Configuration</h2>
<%
	Logger log = Logger.getLogger(this.getClass());
	String username = request.getUserPrincipal().getName();
	Connection c = null;
	PreparedStatement p = null;
	try {
		c = Configuration.getConnection();

		if (request.getParameter("action").compareTo("add") == 0) {
			p = c.prepareStatement("INSERT INTO tUserConfig "
					+ " ( user, cNamespace, cOption, cValue ) "
					+ " VALUES ( ?, ?, ?, ? ) ");
			p.setString(1, username);
			p.setString(2, request.getParameter("namespace"));
			p.setString(3, request.getParameter("option"));
			p.setString(4, request.getParameter("value"));
		} else if (request.getParameter("action").compareTo("save") == 0) {
			p = c.prepareStatement("UPDATE tUserConfig "
 					+ " SET cValue = ? "
					+ " WHERE "
					+ " user = ? " + " AND cNamespace = ? "
					+ " AND cOption = ? ");
			p.setString(1, request.getParameter("value"));
			p.setString(2, username);
			p.setString(3, request.getParameter("namespace"));
			p.setString(4, request.getParameter("option"));
		} else if (request.getParameter("action").compareTo("delete") == 0) {
			p = c.prepareStatement("DELETE FROM tUserConfig WHERE "
					+ " user = ? " + " AND cNamespace = ? "
					+ " AND cOption = ? ");
			p.setString(1, username);
			p.setString(2, request.getParameter("namespace"));
			p.setString(3, request.getParameter("option"));
		} else {
			throw new Exception("No valid action given.");
		}

		p.execute();
	} catch (SQLException se) {
		out.println("<b>Exception: " + se.toString() + "</b>");
	} catch (Exception ex) {
		out.println("<b>Bad request. ( " + ex.toString() + " )</b>");
	} finally {
		out
				.println("<br/><br/><a href=\"configurationDisplay.jsp\">Configuration</a>");
		DbUtil.closeSafely(p);
		DbUtil.closeSafely(c);
	}
%>

<%@ include file="/WEB-INF/jsp/footer.jsp"%>

