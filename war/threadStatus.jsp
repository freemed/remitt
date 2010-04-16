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

<%@ page import="java.util.List"%>
<%@ page import="org.remitt.prototype.JobThreadState"%>
<%@ page import="org.remitt.prototype.ProcessorThread.ThreadType"%>
<%@ page import="org.remitt.server.Configuration"%>
<%@ page import="org.remitt.server.ControlThread"%>
<%@ page import="org.apache.log4j.Logger"%>

<%@ include file="/WEB-INF/jsp/header.jsp"%>

<h2>Thread Pool</h2>

<table border="1" width="100%">
<tr>
	<th>Thread ID</th>
	<th>Type</th>
	<th>Processor ID</th>
	<th>Plugin Class</th>
</tr>
<%
	Logger log = Logger.getLogger(this.getClass());
	List<JobThreadState> pool = Configuration.getControlThread().getThreadPool();
	for (JobThreadState s : pool) {
		out.println("<tr>");
		out.println("<td>" + s.getThreadId() + "</td>");		
		out.println("<td>" + s.getThreadType().toString() + "</td>");		
		out.println("<td>" + s.getProcessorId() + "</td>");		
		out.println("<td>" + s.getPlugin() + "&nbsp;</td>");		
		out.println("</tr>");
	}
%>
</table>

<%@ include file="/WEB-INF/jsp/footer.jsp"%>

