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

<%@ include file="/WEB-INF/jsp/header.jsp"%>

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
			<input type="hidden" name="type" value="render" />
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
			<input type="hidden" name="type" value="translation" />
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
			<input type="hidden" name="type" value="transport" />
			<table border="0">
				<tr>
					<td>Plugin :</td>
					<td><select name="plugin">
						<option
							value="org.remitt.plugin.transport.ScriptedHttpTransport">org.remitt.plugin.transport.ScriptedHttpTransport</option>
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
				onclick="populateFieldFromFrame('transportInput');" />

			<hr/>

			<h1>Parser</h1>

			<form action="TestHarness" method="POST" target="_targetFrame">
			<input type="hidden" name="type" value="parser" />
			<table border="0">
				<tr>
					<td>Parser Class:</td>
					<td><select name="plugin">
						<option value="org.remitt.parser.X12Message835">org.remitt.parser.X12Message835</option>
					</select></td>
				</tr>
				<tr>
					<td>Input :</td>
					<td><textarea name="input" rows="10" cols="60">ISA*00*          *00*          *ZZ*V0202          *ZZ*382069753      *021001*0100*U*00401*000006020*1*T*|
GS*HP*V0202*382069753*20021001*113658*6020*X*004010X091
ST*835*5222
BPR*I*2438.2*C*FWT**01*173423842*DA*342129*1312004389**01*789451783*DA*3245623*20021001
TRN*1*0124326845*1312004389
DTM*405*20021001
N1*PR*DISNEY BENEFITS INCORPORATED
N3*5760 HILLVIEW DRIVE
N4*FLORISSANT*MO*63031
REF*EO*23009VSDF3
N1*PE*DISNEY PHYSICIAN'S FAMILY PRACTICE*FI*233458322
N3*2391 LANTERN LANE
N4*FLORISSANT*MO*63031
LX*1
CLP*D92093134*1*287*124.2*10*12*457845213457789
CAS*PR*3*10
CAS*PI*96*27
NM1*QC*1*SQUAREPANTS*SPONGEBOB****MI*239230493
NM1*IL*1*SQUAREPANTS*JANE*Q***MI*123901283
DTM*232*20020915
DTM*233*20020915
AMT*F5*10
AMT*T*5
AMT*I*10.75
QTY*CD*1
SVC*HC|87420*141*15.20
DTM*472*20020915
CAS*CO*A2*25.80
CAS*PI*50*100
SVC*HC|87804*73*73
DTM*472*20020915
LQ*HE*M118
SVC*HC|94664*73*73
DTM*472*20020915
CLP*3249DS903*4*780.23*0*50*12*20020921938827
CAS*PR*1*50
CAS*CO*42*208
CAS*OA*101*522.23
NM1*QC*1*HARTLEY*SUE****MI*309201131
DTM*232*20020928
DTM*233*20020928
PER*CX**TE*8001231234
CLP*2390953020*4*175.22*0**12
NM1*QC*1*BUNNY*BUGS****34*023029852
NM1*IL*1*BUNNY*MISSY****34*104296742
NM1*82*1*LITTLE*DEBBIE****FI*120349684
REF*IG*32489U32-42343
REF*1B*003492321
DTM*232*20020920
SVC*HC|J3301|RT*89.72*0**2
CAS*PI*55*89.72
LQ*HE*M67
LQ*HE*M118
SVC*HC|90471*85.5*0
CAS*PI*56*85.5
LQ*HE*M86
CLP*0906502334*2*455*400*55*12
CAS*PR*3*55
NM1*QC*1*REN*STIMPY****MI*23560569083
DTM*232*20020919
SVC*HC|73030|LT*455*455
PLB*6745324*20021001*L6*-10.75*CT*-2000*L3*96.75
SE*61*5222
GE*1*6020
IEA*1*000006020
</textarea></td>
				</tr>
			</table>

			<input type="submit" name="submit" value="Test" /></form>

			</td>
			<td>
			<td style="border: 1px solid #000000;"><iframe id="outputFrame"
				name="_targetFrame" width="100%" height="600"></iframe></td>
		</tr>
	</tbody>
</table>

<%@ include file="/WEB-INF/jsp/footer.jsp"%>

