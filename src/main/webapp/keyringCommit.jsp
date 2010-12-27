<%-- 
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
 --%>

<%@ page import="org.remitt.datastore.KeyringStore"%>
<%@ page import="org.remitt.prototype.KeyringItem"%>
<%@ page import="org.remitt.server.Configuration"%>
<%@ page import="org.apache.log4j.Logger"%>

<%@ include file="/WEB-INF/jsp/header.jsp"%>

<h2>Configuration</h2>
<%
	Logger log = Logger.getLogger(this.getClass());
	String username = request.getUserPrincipal().getName();
	try {
		if (request.getParameter("action").compareTo("add") == 0) {
			KeyringStore.putKey(username, 
				request.getParameter("keyname"),
				request.getParameter("privatekey").getBytes(),
				request.getParameter("publickey").getBytes());
		} else if (request.getParameter("action").compareTo("save") == 0) {
			KeyringStore.putKey(username, 
				request.getParameter("keyname"),
				request.getParameter("privatekey").getBytes(),
				request.getParameter("publickey").getBytes());
		} else if (request.getParameter("action").compareTo("delete") == 0) {
			KeyringStore.deleteKey(username, request.getParameter("keyname"));
		} else {
			throw new Exception("No valid action given.");
		}
	} catch (Exception ex) {
		out.println("<b>Bad request. ( " + ex.toString() + " )</b>");
	} finally {
		out
				.println("<br/><br/><a href=\"keyring.jsp\">Keyring Maintenance</a>");
	}
%>

<%@ include file="/WEB-INF/jsp/footer.jsp"%>

