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
<title>REMITT Server: Test Harness</title>
</head>
<body>
<h1><a href="http://remitt.org/"><img src="img/remitt.jpg"
	border="0" /></a> REMITT Electronic Medical Information Translation and
Transmission</h1>

<script language="javascript">
function populateFieldFromFrame(f) {
	try {
		var x = document.getElementById('outputFrame').contentWindow.document.body.innerHTML;
		if (x.contains('&lt;')) {
			x = x.replace('<pre>', '').replace('</pre>', '').replace('&lt;', '<').replace('&gt;', '>').replace('&amp;', '&').replace('&quot;', '"');
		}
		document.getElementById(f).value = x;
	} catch (e) { }
</script>

<table width="100%" border="0">
	<tbody>
		<tr>
			<td>


			<h1>Render</h1>

			<form action="TestHarness" method="POST" target="_targetFrame">
			<table border="0">
				<tr>
					<td>Plugin :</td>
					<td><select name="plugin">
						<option value="org.remitt.plugin.render.XsltPlugin">org.remitt.plugin.render.XsltPlugin</option>
					</select></td>
				</tr>
				<tr>
					<td>Option :</td>
					<td><select name="option">
						<option value="837p">837P</option>
						<option value="hcfa1500">HCFA-1500/CMS-1500</option>
					</select></td>
				</tr>
				<tr>
					<td>Input :</td>
					<td><textarea name="input" rows="10" cols="60"></textarea></td>
				</tr>
			</table>

			<input type="submit" name="submit" value="Render" /></form>

			<hr />

			<h1>Translation</h1>

			<form action="TestHarness" method="POST" target="_targetFrame">
			<table border="0">
				<tr>
					<td>Plugin :</td>
					<td><select name="plugin">
						<option value="org.remitt.plugin.translation.FixedFormPdf">org.remitt.plugin.translation.FixedFormPdf</option>
						<option value="org.remitt.plugin.translation.FixedFormXml">org.remitt.plugin.translation.FixedFormXml</option>
						<option value="org.remitt.plugin.translation.X12Xml">org.remitt.plugin.translation.X12Xml</option>
					</select></td>
				</tr>
				<tr>
					<td>Option :</td>
					<td>NONE REQUIRED <input type="hidden" name="option" value="" /></td>
				</tr>
				<tr>
					<td>Input :</td>
					<td><textarea id="translationInput" name="input" rows="10"
						cols="60"></textarea></td>
				</tr>
			</table>

			<input type="submit" name="submit" value="Translate" /></form>
			<input type="button" value="Copy from output"
				onclick="populateFieldFromFrame('translationInput');" />

			<hr />

			<h1>Transport</h1>

			<form action="TestHarness" method="POST" target="_targetFrame">
			<table border="0">
				<tr>
					<td>Plugin :</td>
					<td><select name="plugin">
						<option
							value="org.remitt.plugin.transmission.ScriptedHttpTransport">org.remitt.plugin.transmission.ScriptedHttpTransport</option>
					</select></td>
				</tr>
				<tr>
					<td>Option :</td>
					<td><select name="option">
						<option value="ClaimLogic">ClaimLogic</option>
						<option value="FreeClaims">FreeClaims</option>
					</select></td>
				</tr>
				<tr>
					<td>Input :</td>
					<td><textarea id="transportInput" name="input" rows="10"
						cols="60"></textarea></td>
				</tr>
			</table>

			<input type="submit" name="submit" value="Transmit" /></form>
			<input type="button" value="Copy from output"
				onclick="populateFieldFromFrame('transportInput');" /></td>
			<td>
			<td style="border: 1px solid #000000;"><iframe id="outputFrame"
				name="_targetFrame" width="100%" height="600"></iframe></td>
		</tr>
	</tbody>
</table>


</body>
</html>
