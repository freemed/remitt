<!-- 
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
 -->
<html>
<head>
<title>REMITT Server: Self Test</title>
<link href="css/stylesheet.css" rel="stylesheet" type="text/css"/>
<%@ page import="java.sql.*"%>
<%@ page import="org.remitt.server.Configuration"%>
<%@ page import="org.apache.log4j.Logger"%>
</head>
<body>
<h1><a href="http://remitt.org/"><img src="img/remitt.jpg"
	border="0" /></a> REMITT Electronic Medical Information Translation and
Transmission</h1>

<h2>Self Test</h2>

<pre><%
	Logger log = Logger.getLogger(this.getClass());
	String username = request.getUserPrincipal().getName();

	// Test database connection
	out.print("Testing database connection ... ");
	Connection c = null;
	try {
		c = Configuration.getConnection();
	} catch (Exception ex) {
		out.println("FAIL");
		out.println(ex.toString());
	} finally {
		out.println("OK");
	}

	out.print("Resolve translation plugin for 837p ... ");
	try {
		String ret = Configuration.resolveTranslationPlugin( "org.remitt.plugin.render.XsltPlugin", "837p", "org.remitt.plugin.transmission.ScriptedHttpTransport", "" );
		out.println(ret);
	} catch (Exception ex) {
		out.println("FAIL");
		out.println(ex.toString());
	}
	
	out.print("Resolve translation plugin for hcfa1500 ... ");
	try {
		String ret = Configuration.resolveTranslationPlugin( "org.remitt.plugin.render.XsltPlugin", "hcfa1500", "org.remitt.plugin.transmission.ScriptedHttpTransport", "" );
		out.println(ret);
	} catch (Exception ex) {
		out.println("FAIL");
		out.println(ex.toString());
	}
	
%></pre>


</body>
</html>
