<!-- 
 * $Id$
 *
 * Authors:
 *      Jeff Buchbinder <jeff@freemedsoftware.org>
 *
 * REMITT Electronic Medical Information Translation and Transmission
 * Copyright (C) 1999-2009 FreeMED Software Foundation
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
 -->
<html>
<head>
<title>REMITT Server: Configuration</title>
<%@ page import="java.sql.*"%>
<%@ page import="org.remitt.server.Configuration"%>
<%@ page import="org.apache.log4j.Logger"%>
</head>
<body>
<h1><a href="http://remitt.org/"><img src="img/remitt.jpg"
	border="0" /></a> REMITT Electronic Medical Information Translation and
Transmission</h1>

<h2>Configuration Display</h2>

<table border="1" cellpadding="5">
	<thead>
		<tr>
			<th>Namespace</th>
			<th>Key</th>
			<th>Value</th>
			<th>Action</th>
		</tr>
	</thead>
	<tbody>
		<%
			Logger log = Logger.getLogger(this.getClass());
			String username = request.getUserPrincipal().getName();
			try {
				Connection c = Configuration.getConnection();
				PreparedStatement p = c
						.prepareStatement("SELECT * FROM tUserConfig "
								+ " WHERE user = ?");
				p.setString(0, username);
				p.execute();
				ResultSet rs = p.getResultSet();
				while (rs.next()) {
					System.out
							.println("<tr><form method=\"post\" action=\"configurationCommit.jsp\">");
					System.out
							.println("<input type=\"hidden\" name=\"id\" value=\""
									+ rs.getString("id") + "\" />");
					System.out
							.println("<td><input type=\"text\" name=\"namespace\" value=\""
									+ rs.getString("cNamespace")
									+ "\" disabled=\"disabled\" /></td>");
					System.out
							.println("<td><input type=\"text\" name=\"option\" value=\""
									+ rs.getString("cOption")
									+ "\" disabled=\"disabled\" /></td>");
					System.out
							.println("<td><input type=\"text\" name=\"value\" value=\""
									+ rs.getString("cValue") + "\"></td>");
					System.out.println("<td><input type=\"submit\" value=\""
							+ "Save Value" + "\" /></td>");
					System.out.println("</form></tr>");
				}
				c.close();
			} catch (SQLException se) {
				System.out.println("<tr><td colspan=\"4\"><b>Exception: "
						+ se.toString() + "</b></tr></tr>");
			}
		%>
	</tbody>
</table>

</body>
</html>
