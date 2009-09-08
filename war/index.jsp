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
<title>REMITT Server</title>
</head>
<body>
<h1><a href="http://remitt.org/"><img src="img/remitt.jpg"
	border="0" /></a> REMITT Electronic Medical Information Translation and
Transmission</h1>

<div class="usernamePanel">
<b>Username:</b> <%= request.getUserPrincipal().getName() %>
</div>

<ul>
	<u>Servlet Resources</u>
	<li><a href="services/">Web Services</a> - SOAP and REST services</li>
	<li><a href="testHarness.jsp">Test Harness</a> - Plugin testing
	interface</li>
	<li><a href="configurationDisplay.jsp">Configuration</a> - Edit
	per-user configuration</li>
</ul>

</body>
</html>
