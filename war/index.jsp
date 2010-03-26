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
<%@ page import="java.util.*"%>
<%@ page import="org.remitt.server.Configuration"%>

<%@ include file="/WEB-INF/jsp/header.jsp"%>

<table border="1" cellpadding="7">
	<tbody>
		<tr>
			<th>REMITT Version</th>
			<td><%@ include file="about.jsp"%></td>
		</tr>
		<tr>
			<th>Username</th>
			<td><%=request.getUserPrincipal().getName()%></td>
		</tr>
		<tr>
			<th>Server</th>
			<td><%=request.getLocalAddr()%>:<%=request.getLocalPort()%>
			connecting from <%=request.getRemoteAddr()%></td>
		</tr>
		<tr>
			<th>C3P0 Pool Status</th>
			<td><%=Configuration.getComboPooledDataSource()
							.getNumBusyConnections()%> / <%=Configuration.getComboPooledDataSource()
									.getMaxPoolSize()%> connections</td>
		</tr>
	</tbody>
</table>

<ul>
	<u>Servlet Resources</u>
	<li><a href="services/">Web Services</a> - SOAP and REST services</li>
	<li><a href="testHarness.jsp">Test Harness</a> - Plugin testing
	interface</li>
	<li><a href="configurationDisplay.jsp">Configuration</a> - Edit
	per-user configuration</li>
	<li><a href="TestServlet">Unit Tests</a> - Test internal REMITT
	functionality. Pretty much useless for end-users.</li>
</ul>

<%@ include file="/WEB-INF/jsp/footer.jsp"%>

