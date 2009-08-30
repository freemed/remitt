<html>
<head>
<title>REMITT Server: Test Harness</title>
</head>
<body>
<h1><a href="http://remitt.org/"><img src="img/remitt.jpg"
	border="0" /></a> REMITT Electronic Medical Information Translation and
Transmission</h1>

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
					<td><textarea name="input" rows="10" cols="60"></textarea></td>
				</tr>
			</table>
			<input type="submit" name="submit" value="Translate" /></form>

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
						<option value="FreeClaims">FreeClaims</option>
					</select></td>
				</tr>
				<tr>
					<td>Input :</td>
					<td><textarea name="input" rows="10" cols="60"></textarea></td>
				</tr>
			</table>
			<input type="submit" name="submit" value="Transmit" /></form>

			</td>
			<td>
			<td style="border: 1px solid #000000;"><iframe
				name="_targetFrame" width="100%" height="600"></iframe></td>
		</tr>
	</tbody>
</table>


</body>
</html>
