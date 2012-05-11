<%-- 
 * $Id$
 *
 * Authors:
 *      Jeff Buchbinder <jeff@freemedsoftware.org>
 *
 * REMITT Electronic Medical Information Translation and Transmission
 * Copyright (C) 1999-2012 FreeMED Software Foundation
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

<h2>Keyring Maintenance</h2>

<table border="1" cellpadding="5">
	<thead>
		<tr>
			<th>Name</th>
			<th>Private</th>
			<th>Public</th>
			<th>Action</th>
		</tr>
	</thead>
	<tbody>
		<%
			Logger log = Logger.getLogger(this.getClass());
			String username = request.getUserPrincipal().getName();
			KeyringItem[] keys = KeyringStore.getKeys(username);
			for (KeyringItem key : keys) {
				out.println("<tr><form method=\"post\" action=\"keyringCommit.jsp\">");
				out.println("<td><input type=\"hidden\" name=\"keyname\" value=\""
					+ key.getKeyname()
					+ "\" size=\"25\" /><code>"
					+ key.getKeyname()
					+ "</code></td>");
				out.println("<td><textarea name=\"privatekey\" width=\"40\""
					+ " wrap=\"virtual\" height=\"10\">"
					+ new String(key.getPrivatekey()) + "</textarea></td>");
				out.println("<td><textarea name=\"publickey\" width=\"40\"" 
					+ " wrap=\"virtual\" height=\"10\">"
					+ new String(key.getPublickey()) + "</textarea></td>");
				out.println("<td><input type=\"submit\" name=\"action\" value=\""
					+ "save"
					+ "\" />"
					+ "<input type=\"submit\" name=\"action\" value=\""
					+ "delete" + "\" /></td>");
				out.println("</form></tr>");
			}
		%>
		<tr>
			<form method="post" action="keyringCommit.jsp">
			<td><input type="text" name="keyname" size="25" value="" /></td>
			<td><textarea name="privatekey" width="40" wrap="virtual" height="10"></textarea></td>
			<td><textarea name="publickey" width="40" wrap="virtual" height="10"></textarea></td>
			<td><input type="submit" name="action" value="add" /></td>
			</form>
		</tr>
	</tbody>
</table>

<%@ include file="/WEB-INF/jsp/footer.jsp"%>

