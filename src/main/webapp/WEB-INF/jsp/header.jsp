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
<html>
<head>
<title>REMITT Server v<%@ include file="/about.jsp"%></title>
<%@ page import="java.util.*"%>
<%@ page import="org.remitt.server.Configuration"%>
<link href="css/stylesheet.css" rel="stylesheet" type="text/css"/>
</head>
<body>
<h1><a href="http://remitt.org/"><img src="img/remitt.jpg"
	border="0" /></a> REMITT Electronic Medical Information Translation and
Transmission</h1>
<ul id="menubar">
	<li><a href="index.jsp">Home</a></li>
	<li><a href="services/">Services</a></li>
	<li><a href="configurationDisplay.jsp">Configuration</a></li>
	<li><a href="testHarness.jsp">Test Harness</a></li>
	<li><a href="TestServlet">Unit Tests</a></li>
</ul>

<!-- end header.jsp -->

