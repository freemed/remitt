<?xml version="1.0"?>
<!--

  $Id$
 
  Authors:
 	Jeff Buchbinder <jeff@freemedsoftware.org>
 
  REMITT Electronic Medical Information Translation and Transmission
  Copyright (C) 1999-2008 FreeMED Software Foundation
 
  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.

-->
<xsl:stylesheet version="1.0" 
		xmlns:exsl="http://exslt.org/common"
		extension-element-prefixes="exsl"
		xmlns:set="http://exslt.org/sets"
		exclude-result-prefixes="set"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

        <xsl:output method="xml" />

	<!--
		NOTES:

		When dealing with templates, do not use xsl:value-of
		embedded in the xsl:with-param blocks ... it does not
		actually pass tree fragments, and passes text instead,
		causing wildly unpredictable results.

		In element->content elements, use content="" attribute
		tag for blank elements with length > 0 so that
		XML::Simple does not discard the spaces.
	-->

	<!--
		Define keys to use
	-->
	<xsl:variable name="lowercase" select="'abcdefghijklmnopqrstuvwxyz'" />
	<xsl:variable name="uppercase" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'" />
	<xsl:variable name="insuredall"    select="//insured"        />
	<xsl:variable name="patientall"    select="//patient"        />
		<!-- Calculate HL offsets -->
	<xsl:variable name="insuredoffset" select="1" />
	<xsl:variable name="patientoffset" select="count($insuredall)+$insuredoffset" />

	<!--
		Master template for document root

		This template processes the document root, and calls all
		child templates.
	-->

	<xsl:template match="/remitt">
        <render>
		<x12format>
			<delimiter>*</delimiter>
			<endofline></endofline>
		</x12format>

		<!-- Generate static segments -->

		<x12segment sid="ISA">
			<comment>ISA Interchange Control Header #0</comment>
			<element>
				<content>00</content>
			</element>
			<element>
				<comment>10 spaces</comment>
				<content text="REMITT837P" />
			</element>
			<element>
				<content>00</content>
			</element>
			<element>
				<comment>10 spaces</comment>
				<content text="SECURITY01" />
			</element>
			<element>
				<content>ZZ</content>
			</element>
			<element>
				<content>000000000012345</content>
			</element>
			<element>
				<content>ZZ</content>
			</element>
			<element>
				<!-- has to be 15 characters -->
				<content text="00000000000048M"/>
			</element>
			<element>
				<content>030911</content>
			</element>
			<element>
				<content>1630</content>
			</element>
			<element>
				<content>U</content>
			</element>
			<element>
				<content>00401</content>
			</element>
			<element>
				<content>000000001</content>
			</element>
			<element>
				<!-- ACK requested 0 = no, 1 = yes -->
				<content>1</content>
			</element>
			<element>
				<!-- T = test data, P = patient data -->
				<content>P</content>
			</element>
			<element>
				<!-- Element split character -->
				<content>:</content>
			</element>
		</x12segment>

		<x12segment sid="GS">
			<comment>GS Functional Group Header Segment</comment>
			<element>
				<content>HC</content>
			</element>
			<element>
				<content><xsl:value-of select="//clearinghouse/x12gssenderid" /></content>
			</element>
			<element>
				<content><xsl:value-of select="//clearinghouse/x12gsreceiverid" /></content>
			</element>
			<element>
				<comment>Current date YYYYMMDD</comment>
				<content><xsl:value-of select="concat(//global/currentdate/year,//global/currentdate/month,//global/currentdate/day)" /></content>
			</element>
			<element>
				<comment>Current date hour + minute FIXME</comment>
				<content><xsl:value-of select="concat(//global/currenttime/hour,//global/currenttime/minute)" /></content>
			</element>
			<element>
				<content>1</content>
			</element>
			<element>
				<content>X</content>
			</element>
			<element>
				<!-- Transmission code for addendum 1 -->
				<content>004010X098A1</content>
			</element>
		</x12segment>

		<x12segment sid="ST">
			<comment>ST Transaction Set Header #1</comment>
			<element>
				<content>837</content>
			</element>
			<element>
				<content>0021</content>
			</element>
		</x12segment>

		<x12segment sid="BHT">
			<comment>BHT Beginning of Heirachical Transaction</comment>
			<element>
				<content>0019</content>
			</element>
			<element>
				<!-- Original (not reissue) -->
				<content>00</content>
			</element>
			<element>
				<!-- FIXME: This should be the billkey -->
				<content>0123</content>
			</element>
			<element>
				<!-- Creation date (YYYYMMDD) -->
				<content><xsl:value-of select="concat(//global/currentdate/year,//global/currentdate/month,//global/currentdate/day)" /></content>
			</element>
			<element>
				<!-- Creation time (HHMM) -->
				<content><xsl:value-of select="concat(//global/currenttime/hour,//global/currenttime/minute)" /></content>
			</element>
			<element>
				<content>RP</content>
			</element>
		</x12segment>

		<x12segment sid="REF">
			<comment>REF Generates REF line #3</comment>
			<element>
				<content>87</content>
			</element>
			<element>
				<!-- Transmission type code (is this right?) -->
				<content>004010X098A1</content>
			</element>
		</x12segment>

		<xsl:comment>Loop 1000A: Submitter Name</xsl:comment>

		<x12segment sid="NM1">
			<comment>NM1 - Submitter item #4</comment>
			<element>
				<content>41</content>
			</element>
			<element>
				<content>2</content>
			</element>
			<element>
				<content><xsl:value-of select="translate(//billingservice/name, $lowercase, $uppercase)" /></content>
			</element>
			<element>
				<content/>
			</element>
			<element>
				<content/>
			</element>
			<element>
				<content/>
			</element>
			<element>
				<content/>
			</element>
			<element>
				<content>46</content>
			</element>
			<element>
				<content><xsl:value-of select="//billingservice/etin" /></content>
			</element>
		</x12segment>

		<x12segment sid="PER">
			<comment>PER - Submitter EDI contact information (p72)</comment>
			<element>
				<!-- Information Contact code -->
				<content>IC</content>
			</element>
			<element>
				<content><xsl:value-of select="translate(//billingcontact/name, $lowercase, $uppercase)" /></content>
			</element>
			<element>
				<!-- Use telephone number as qualifier -->
				<content>TE</content>
			</element>
			<element>
				<content><xsl:value-of select="concat(//billingcontact/phone/area, //billingcontact/phone/number)" /></content>
			</element>
			<xsl:if test="boolean(string(//billingcontact/phone/extension))">
			<element>
				<content>EX</content>
			</element>
			<element>
				<content><xsl:value-of select="//billingcontact/phone/extension" /></content>
			</element>
			</xsl:if>
		</x12segment>

		<xsl:comment>Loop 1000B: Receiver</xsl:comment>

		<x12segment sid="NM1">
			<comment>NM1: Receiver name item (p74)</comment>
			<element>
				<content>40</content>
			</element>
			<element>
				<content>2</content>
			</element>
			<element>
				<content><xsl:value-of select="translate(//clearinghouse/name, $lowercase, $uppercase)" /></content>
			</element>
			<element>
				<content></content>
			</element>
			<element>
				<content></content>
			</element>
			<element>
				<content></content>
			</element>
			<element>
				<content></content>
			</element>
			<element>
				<content>46</content>
			</element>
			<element>
				<content><xsl:value-of select="//clearinghouse/etin" /></content>
			</element>
		</x12segment>

		<xsl:comment>Loop 2000A: Pay-to Provider</xsl:comment>

		<x12segment sid="HL">
			<comment>HL Pay-to provider (p77)</comment>
			<element>
				<!-- Set current HL counter -->
				<!-- <content>1</content> -->
				<hl>STARTING</hl>
			</element>
			<element>
				<content></content>
			</element>
			<element>
				<!-- Heirarchical level code -->
				<content>20</content>
			</element>
			<element>
				<!-- Heirarchical child code -->
				<content>1</content>
			</element>
		</x12segment>

		<!-- CUR - Currency information (p82) optional -->
		<x12segment sid="CUR">
			<element>
				<!-- Set Entity ID as Billing Provider -->
				<content>85</content>
			</element>
			<element>
				<!-- Some services require US Dollars -->
				<content>USD</content>
			</element>
		</x12segment>

		<xsl:comment>Loop 2010AA: Billing Service Loop (p84)</xsl:comment>

		<!-- This is a horrible misnomer; it's actually billing
			services ... -->

		<x12segment sid="NM1">
			<comment>NM1 Billing Service</comment>
			<element>
				<content>85</content>
			</element>
			<element>
				<content>2</content>
			</element>
			<element>
				<content><xsl:value-of select="translate(//billingservice/name, $lowercase, $uppercase)" /></content>
			</element>
			<element>
				<content></content>
			</element>
			<element>
				<content></content>
			</element>
			<element>
				<content></content>
			</element>
			<element>
				<content></content>
			</element>
			<element>
				<content>24</content>
			</element>
			<element>
				<content><xsl:value-of select="//billingservice/tin" /></content>
			</element>
		</x12segment>

		<x12segment sid="N3">
			<comment>N3 - Billing Provider (p88)</comment>
			<element>
				<content><xsl:value-of select="translate(//billingservice/address/streetaddress, $lowercase, $uppercase)" /></content>
			</element>
		</x12segment>

		<x12segment sid="N4">
			<comment>N4 - Billing Provider (p89)</comment>
			<element>
				<content><xsl:value-of select="translate(//billingservice/address/city, $lowercase, $uppercase)" /></content>
			</element>
			<element>
				<content><xsl:value-of select="//billingservice/address/state" /></content>
			</element>
			<element>
				<content><xsl:value-of select="//billingservice/address/zipcode" /></content>
			</element>
		</x12segment>

		<!-- FreeB has a REF here ... FIXME FIXME! -->

		<xsl:comment>Loop 2000B: Patient/Insured Loop</xsl:comment>

		<xsl:variable name="practices" select="set:distinct(exsl:node-set(//procedure/practicekey))" />

		<xsl:for-each select="$practices">
			<xsl:call-template name="process-practice">
				<xsl:with-param name="practice">
					<xsl:value-of select="."/>
				</xsl:with-param>	
			</xsl:call-template>
		</xsl:for-each>

		<xsl:comment>SE - Transaction Set Trailer</xsl:comment>
		<x12segment sid="SE">
			<comment>SE - Transaction Set Trailer</comment>
			<element>
				<segmentcount />
			</element>
			<element>
				<content>0021</content>
			</element>
		</x12segment>

		<xsl:comment>GE Loop End #100</xsl:comment>
		<x12segment sid="GE">
			<comment>GE - Functional Group Trailer</comment>
			<element>
				<content>1</content>
			</element>
			<element>
				<content>1</content>
			</element>
		</x12segment>

		<xsl:comment>IEA Trailer #101</xsl:comment>
		<x12segment sid="IEA">
			<comment>IEA - Interchange Control Trailer</comment>
			<element>
				<content>1</content>
			</element>
			<element>
				<content>000000001</content>
			</element>
		</x12segment>

        </render>
	</xsl:template>

	<xsl:template name="process-practice">
		<xsl:param name="practice" />

		<!-- Generate loop header -->
		<xsl:variable name="thispractice" select="//practice[@id=$practice]" />

		<x12segment sid="REF">
			<!-- Should this be in this loop ? -->
			<comment>REF segment for 2000B</comment>
			<element>
				<content><xsl:value-of select="$thispractice/x12idtype" /></content>
			</element>
			<element>
				<content><xsl:value-of select="$thispractice/x12id" /></content>
			</element>
		</x12segment>

		<xsl:comment>2010AB Loop (p99)</xsl:comment>

		<xsl:comment>
		<x12segment sid="NM1">
			<comment>NM1 (p99)</comment>
			<element>
				<!-- Pay-to provider = 87 -->
				<content>87</content>
			</element>
			<element>
				<content>2</content>
			</element>
			<element>
				<!-- Practice name -->
				<content><xsl:value-of select="translate($thispractice/name, $lowercase, $uppercase)" /></content>
			</element>
			<element>
				<content />
			</element>
			<element>
				<content />
			</element>
			<element>
				<content />
			</element>
			<element>
				<content />
			</element>
			<element>
				<!-- EIN -->
				<content>24</content>
			</element>
			<element>
				<!-- Supposed to be EIN number -->
				<content><xsl:value-of select="$thispractice/ein" /></content>
			</element>
		</x12segment>

		<x12segment sid="N3">
			<element>
				<content><xsl:value-of select="translate($thispractice/address/streetaddress, $lowercase, $uppercase)" /></content>
			</element>
		</x12segment>

		<x12segment sid="N4">
			<element>
				<content><xsl:value-of select="translate($thispractice/address/city, $lowercase, $uppercase)" /></content>
			</element>
			<element>
				<content><xsl:value-of select="$thispractice/address/state" /></content>
			</element>
			<element>
				<content><xsl:value-of select="$thispractice/address/zipcode" /></content>
			</element>
		</x12segment>
		</xsl:comment>

		-- selecting patients for <xsl:value-of select="$practice" /> --

		<!--
			Need to get distinct patient/insured pairs.
			Get patients from this practice, then pass
			to process-insured .... 2000B subscriber loop
		-->
		<xsl:variable name="patients" select="set:distinct(exsl:node-set(//procedure[practicekey=$practice]/patientkey))" />
		<xsl:for-each select="$patients">
			--
			Patient <xsl:value-of select="." />
			<xsl:call-template name="process-patient">
				<xsl:with-param name="practice" select="$practice" />
				<xsl:with-param name="patient" select="." />
			</xsl:call-template>
		</xsl:for-each>
	</xsl:template>

	<!--
		process-patient template

		Get distinct insured entities for each patient, then
		call 2000B loop with them both.
	-->
	<xsl:template name="process-patient">
		<xsl:param name="practice" />
		<xsl:param name="patient" />

		<xsl:comment>Processing practice <xsl:value-of select="$practice" /> and patient <xsl:value-of select="$patient" /></xsl:comment>

		<xsl:variable name="insureds" select="set:distinct(exsl:node-set(//procedure[patientkey=$patient and practicekey=$practice]/insuredkey))" />
		<xsl:for-each select="$insureds">
			<!-- TODO: Different processing for null insured? -->
			<xsl:call-template name="process-insured">
				<xsl:with-param name="patient" select="$patient" />
				<xsl:with-param name="practice" select="$practice" />
				<xsl:with-param name="insured" select="." />
			</xsl:call-template>
		</xsl:for-each>
	</xsl:template>

	<!--
		process-insured

		Process distinct patient/insured pairs using 2000B loopset
	-->
	<xsl:template name="process-insured">
		<xsl:param name="patient" />
		<xsl:param name="practice" />
		<xsl:param name="insured" />

		<xsl:variable name="insuredobj" select="//insured[@id=$insured]" />
		<xsl:variable name="patientobj" select="//patient[@id=$patient]" />
		<!-- Extract payer by insured -->
		<xsl:variable name="payer" select="//procedure[insuredkey=$insured]/payerkey" />
		<xsl:variable name="payerobj" select="//payer[@id=$payer[1]]" />

		<!-- determine HLpatient status 0 or 1 
			if relationship = S, status = 0 -->
		<xsl:variable name="hlpatient">
			<xsl:choose>
				<xsl:when test="translate($insuredobj/relationship, $lowercase, $uppercase) = 'S'">0</xsl:when>
				<xsl:otherwise>1</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<xsl:comment>2000B Loop with $patient and $insured in $practice</xsl:comment>

		<!-- Get the insured and patient HL -->	
		<xsl:variable name="insuredhlrel">
			<xsl:call-template name="sequence-location">
				<xsl:with-param name="id" select="$insured"/>
				<xsl:with-param name="nodes" select="$insuredall"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:variable name="insuredhl" select="$insuredhlrel + $insuredoffset" />
		<xsl:variable name="patienthlrel">
			<xsl:call-template name="sequence-location">
				<xsl:with-param name="id" select="$patient"/>
				<xsl:with-param name="nodes" select="$patientall"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:variable name="patienthl" select="$patienthlrel + $patientoffset" />
	
		<x12segment sid="HL">
			<comment>HL - Subscriber Heirarchical Level 2000B (p108)</comment>
			<element>
				<!-- Insured HL -->
				<!-- <content><xsl:value-of select="$insuredhl" /></content> -->
				<hl><xsl:value-of select="concat($insuredhl, 'x', $practice)" /></hl>
			</element>
			<element>
				<!-- Parent HL code ... -->
				<content>1</content>
			</element>
			<element>
				<!-- Heirarchical level code (22 = subscriber) -->
				<content>22</content>
			</element>
			<element>
				<!-- 0 = subscriber level is patient level, 1 = extra patient level -->
				<content><xsl:value-of select="$hlpatient" /></content>
			</element>
		</x12segment>

		<!-- Loop 2000B: SBR Subscriber Information (p110) -->
		<x12segment sid="SBR">
			<element>
				<!-- FIXME: Primary=P, Secondary=S, Tertiary=T -->
				<content>P</content>
			</element>
			<element>
				<!-- If self, this is 18 -->
				<content><xsl:if test="$insuredobj/relationship = 'S'">18</xsl:if></content>
			</element>
			<element>
				<content><xsl:value-of select="$insuredobj/groupnumber" /></content>
			</element>
			<element>
				<content><xsl:value-of select="translate($insuredobj/groupname, $lowercase, $uppercase)" /></content>
			</element>
			<element>
				<!-- FIXME: SBR05 special instance, medicare only -->
				<content><!-- 47 --></content>
			</element>
			<element>
				<content></content>
			</element>
			<element>
				<content></content>
			</element>
			<element>
				<content></content>
			</element>
			<element>
				<!-- X12 Claim Type from Payer -->
				<content><xsl:value-of select="translate($payerobj/x12claimtype, $lowercase, $uppercase)" /></content>
			</element>
		</x12segment>

		<!-- FIXME: If isDead, need PAT (p114) -->

		<!-- 2010BA: Subscriber Name (p117) required -->
		<x12segment sid="NM1">
			<element>
				<!-- Insured or subscriber -->
				<content>IL</content>
			</element>
			<element>
				<!-- 1 = person -->
				<content>1</content>
			</element>
			<element>
				<content><xsl:choose>
					<xsl:when test="$insuredobj/relationship = 'S'"><xsl:value-of select="translate($patientobj/name/last, $lowercase, $uppercase)" /></xsl:when>
					<xsl:otherwise><xsl:value-of select="translate($insuredobj/name/last, $lowercase, $uppercase)" /></xsl:otherwise>
				</xsl:choose></content>
			</element>
			<element>
				<content><xsl:choose>
					<xsl:when test="$insuredobj/relationship = 'S'"><xsl:value-of select="translate($patientobj/name/first, $lowercase, $uppercase)" /></xsl:when>
					<xsl:otherwise><xsl:value-of select="translate($insuredobj/name/first, $lowercase, $uppercase)" /></xsl:otherwise>
				</xsl:choose></content>
			</element>
			<element>
				<content><xsl:choose>
					<xsl:when test="$insuredobj/relationship = 'S'"><xsl:value-of select="translate($patientobj/name/middle, $lowercase, $uppercase)" /></xsl:when>
					<xsl:otherwise><xsl:value-of select="translate($insuredobj/name/middle, $lowercase, $uppercase)" /></xsl:otherwise>
				</xsl:choose></content>
			</element>
			<element>
				<!-- Name prefix -->
				<content></content>
			</element>
			<element>
				<!-- Name suffix -->
				<content></content>
			</element>
			<element>
				<!-- Identification code qualifier (MI = member ID, ZZ = mutually defined) -->
				<content>MI</content>
			</element>
			<element>
				<!-- Identification code -->
				<content><xsl:value-of select="$insuredobj/id" /></content>
			</element>
		</x12segment>

		<!-- 2010BA: Subscriber Address (p121) -->
		<x12segment sid="N3">
			<element>
				<content><xsl:choose>
					<xsl:when test="$insuredobj/relationship = 'S'"><xsl:value-of select="translate($patientobj/address/streetaddress, $lowercase, $uppercase)" /></xsl:when>
					<xsl:otherwise><xsl:value-of select="translate($insuredobj/address/streetaddress, $lowercase, $uppercase)" /></xsl:otherwise>
				</xsl:choose></content>
			</element>
		</x12segment>

		<!-- FIXME FIXME NEEDS CHECK FOR relationship!! (p122) -->
		<x12segment sid="N4">
			<element>
				<content><xsl:value-of select="translate($insuredobj/address/city, $lowercase, $uppercase)" /></content>
			</element>
			<element>
				<content><xsl:value-of select="translate($insuredobj/address/state, $lowercase, $uppercase)" /></content>
			</element>
			<element>
				<content><xsl:value-of select="$insuredobj/address/zipcode" /></content>
			</element>
		</x12segment>

		<xsl:if test="$insuredobj/relationship = 'S'">
		<x12segment sid="DMG">
			<element>
				<!-- Specify CCYYMMDD date -->
				<content>D8</content>
			</element>
			<element>
				<!-- DOB of insured CCYYMMDD -->
				<content><xsl:value-of select="concat($insuredobj/dateofbirth/year, $insuredobj/dateofbirth/month, $insuredobj/dateofbirth/day)" /></content>
			</element>
			<element>
				<!-- Gender (transcode "T"ransgender as U) -->
				<content><xsl:value-of select="translate($insuredobj/sex, 'mftTu', 'MFUUU')" /></content>
			</element>
		</x12segment>
		</xsl:if>

		<!-- 2010BB: Payer Name (p???) required -->
		<x12segment sid="NM1">
			<element>
				<!-- Payer -->
				<content>PR</content>
			</element>
			<element>
				<!-- 2 = non-person entity -->
				<content>2</content>
			</element>
			<element>
				<!-- NM103: Payer Name -->
				<content><xsl:value-of select="translate($payerobj/name, $lowercase, $uppercase)" /></content>
			</element>
			<element>
				<content/>
			</element>
			<element>
				<content/>
			</element>
			<element>
				<content/>
			</element>
			<element>
				<content/>
			</element>
			<element>
				<!-- NM108: ID Code Qualifier ( PI = payor id, XV = HCFA PIN ) -->
				<content>PI</content>
			</element>
			<element>
				<!-- NM109: Identification Code -->
				<content><xsl:value-of select="$payerobj/x12id" /></content>
			</element>
		</x12segment>

		<xsl:comment>2000C HL Heirarchical level (p152)</xsl:comment>

		<!-- If insured = patient, skip this loop -->
		<xsl:if test="not($insuredobj/relationship = 'S')">
		<x12segment sid="HL">
			<element>
				<!-- Currently assigned ID number -->
				<!-- <content><xsl:value-of select="$patienthl" /></content> -->
				<hl><xsl:value-of select="concat($patienthl, 'x', $practice)" /></hl>
			</element>
			<element>
				<!-- Parent ID. In this case, the insuredhl -->
				<content><xsl:value-of select="concat($insuredhl, 'x', $practice)" /></content>
			</element>
			<element>
				<!-- HL Code (23 = dependent) -->
				<content>23</content>
			</element>
			<element>
				<!-- Heirarchical child code (0 = none) -->
				<content>0</content>
			</element>
		</x12segment>

		<x12segment sid="PAT">
			<comment>PAT - Patient Name (p154)</comment>
			<element>
				<!-- Entity Identifier Code -->
				<content><xsl:choose>
						<!-- Husband or wife (spouse) is 01 -->
					<xsl:when test="($insuredobj/relationship = 'H') or ($insuredobj/relationship = 'W')">01</xsl:when>
						<!-- Child is 19 -->
					<xsl:when test="$insuredobj/relationship = 'C'">19</xsl:when>
						<!-- Stepchild is 17 -->
					<xsl:when test="$insuredobj/relationship = 'SC'">17</xsl:when>
						<!-- Foster child is 10 -->
					<xsl:when test="$insuredobj/relationship = 'FC'">10</xsl:when>
						<!-- Sponsored dependent is 23 -->
					<xsl:when test="$insuredobj/relationship = 'SD'">23</xsl:when>
						<!-- Handicapped dependent is 22 -->
					<xsl:when test="$insuredobj/relationship = 'HD'">22</xsl:when>
						<!-- Other relationship is G8 -->
					<xsl:when test="$insuredobj/relationship = 'O'">G8</xsl:when>
						<!-- Unknown is 21 -->
					<xsl:otherwise>21</xsl:otherwise>
				</xsl:choose></content>
			</element>
			<element>
				<content/>
			</element>
			<element>
				<content/>
			</element>
			<element>
				<content/>
			</element>
			<element>
				<!-- Death date qualifier, if patient is deceased -->
				<content><xsl:if test="$patientobj/isdead = 1">D8</xsl:if></content>
			</element>
			<element>
				<!-- Date of death, if patient is deceased -->
				<content><xsl:if test="$patientobj/isdead = 1"><xsl:value-of select="concat($patientobj/dateofdeath/year, $patientobj/dateofdeath/month, $patientobj/dateofdeath/day)" /></xsl:if></content>
			</element>
			<element>
				<!-- Unit or basis for measurement -->
				<!-- Baby weight, grams would be 'GR' -->
				<content/>
			</element>
			<element>
				<!-- Birth weight FIXME: required if under 28 days -->
				<content/>
			</element>
			<element>
				<!-- Pregnancy code Y/N FIXME! FIXME! -->
				<content/>
			</element>
		</x12segment>

		<x12segment sid="NM1">
			<comment>NM1 - Subscriber Name (p157)</comment>
			<element>
				<!-- Entity Identifier Code (QC = patient) -->
				<content>QC</content>
			</element>
			<element>
				<!-- Entity Type Qualifier (1 = person) -->
				<content>1</content>
			</element>
			<element>
				<comment>Name, Last</comment>
				<content><xsl:value-of select="translate($patientobj/name/last, $lowercase, $uppercase)" /></content>
			</element>
			<element>
				<comment>Name, First</comment>
				<content><xsl:value-of select="translate($patientobj/name/first, $lowercase, $uppercase)" /></content>
			</element>
			<element>
				<comment>Name, Middle</comment>
				<content><xsl:value-of select="translate($patientobj/name/middle, $lowercase, $uppercase)" /></content>
			</element>
			<element>
				<comment>Name Prefix</comment>
				<content></content>
			</element>
			<element>
				<!-- NM108 - Identification Code Qualifier -->
				<content>MI</content>
			</element>
			<element>
				<!-- NM109 - Identification Code -->
				<!-- FIXME: needs to be pulled from "insured" or other coverage -->
				<content></content>
			</element>
			<element>
				<!-- NM110 - Entity Relationship Code (not used) -->
				<content />
			</element>
			<element>
				<!-- NM111 - Entity Identifier Code (not used) -->
				<content />
			</element>
		</x12segment>

		<x12segment sid="N3">
			<comment>2010BA Subscriber Name / Address</comment>
			<element>
				<content><xsl:value-of select="translate($patientobj/address/streetaddress, $lowercase, $uppercase)" /></content>
			</element>
		</x12segment>

		<x12segment sid="N4">
			<comment>2010BA Subscriber Name / CSZ</comment>
			<element>
				<content><xsl:value-of select="translate($patientobj/address/city, $lowercase, $uppercase)" /></content>
			</element>
			<element>
				<content><xsl:value-of select="translate($patientobj/address/state, $lowercase, $uppercase)" /></content>
			</element>
			<element>
				<content><xsl:value-of select="$patientobj/address/zipcode" /></content>
			</element>
		</x12segment>
		</xsl:if> <!-- end 2000C chunk -->

		<!-- DMG Segment is the demographic segment. It should only
		     be used when the patient is the same as the insured!!! -->
			insured = <xsl:value-of select="$insured" />
			--
		<xsl:comment>
		<xsl:if test="$insuredobj/relationship = 'S'">
		<x12segment sid="DMG">
			<element>
				<!-- DMG01 - Date Time Period Format Qualifier -->
				<content>D8</content>
			</element>
			<element>
				<xsl:variable name="dob" select="$patientobj/dateofbirth" />
				<content><xsl:value-of select="concat($dob/year, $dob/month, $dob/day)" /></content>
			</element>
			<element>
				<!-- DMG03 - Gender Code -->
				<content><xsl:value-of select="translate($patientobj/sex, $lowercase, $uppercase)" /></content>
			</element>
			<element>
				<content></content>
			</element>
			<element>
				<content></content>
			</element>
			<element>
				<content></content>
			</element>
			<element>
				<content></content>
			</element>
			<element>
				<content></content>
			</element>
			<element>
				<content></content>
			</element>
		</x12segment>
		</xsl:if>
		</xsl:comment>

		<xsl:if test="boolean(string($patientobj/socialsecuritynumber))">
		<x12segment sid="REF">
			<!-- REF (Loop 2010BA p126) -->
			<!-- REF segment required to give SSN or secondary
			     identifier. Should probably be insured, not
			     patient. FIXME FIXME -->
			<element>
				<comment>REF01 - Social Security Number</comment>
				<content>SY</content>
			</element>
			<element>
				<content><xsl:value-of select="$patientobj/socialsecuritynumber" /></content>
			</element>
		</x12segment>
		</xsl:if>

		<xsl:variable name="procs" select="//procedure[patientkey=$patient and practicekey=$practice and insuredkey=$insured and payerkey=$payer]" />
		<xsl:variable name="facilities" select="set:distinct(exsl:node-set($procs/facilitykey))" />

		<!-- For 2300 loop, we're going to farm out to another
			template so that we don't have to deal with multiple
			facilities in here -->
		<xsl:for-each select="$facilities">
			<xsl:call-template name="process-facility-claims">
				<xsl:with-param name="procs" select="$procs" />
				<xsl:with-param name="practice" select="$practice" />
				<xsl:with-param name="patient" select="$patient" />
				<xsl:with-param name="insured" select="$insured" />
				<xsl:with-param name="facility" select="." />
			</xsl:call-template>
		</xsl:for-each>
	</xsl:template>

	<xsl:template name="process-facility-claims">
		<xsl:param name="procs" />
		<xsl:param name="patient" />
		<xsl:param name="practice" />
		<xsl:param name="insured" />
		<xsl:param name="facility" />

		<!-- 2300 Loop: Claim Information (p170) -->
		<x12segment sid="CLM">
			<element>
				<!-- CLM01 - Claim Identifier -->
				<!-- According to 837P documents, this should be the patient id -->
				<!-- Should be unique identifier for set of procedures -->
				<content><xsl:value-of select="$patient" /></content>
			</element>
			<element>
				<!-- Total charges for all these procedures -->
				<content><xsl:value-of select="format-number(sum($procs[facilitykey=$facility]/cptcharges), '####.00')" /></content>
			</element>
			<element>
				<content></content>
			</element>
			<element>
				<content></content>
			</element>
			<element>
				<!-- Place of Service Code -->
				<!--	1:	Facility type code -->
				<!--	2:	Not used -->
				<!--	3:	Frequency type (1) -->
				<content><xsl:value-of select="//facility[@id=$facility]/x12code"/>::1</content>
			</element>
			<element>
				<!-- Provider signature on file Y/N -->
				<content>Y</content>
			</element>
			<element>
				<!-- Provider accept assignment code -->
				<content>A</content>
			</element>
			<element>
				<!-- Assignment of benefits indicator -->
				<content>Y</content>
			</element>
			<element>
				<!-- Release of information code -->
				<content>Y</content>
			</element>
			<element>
				<!-- Patient signature source code -->
				<content>B</content>
			</element>
			<xsl:if test="0 &gt; 1"><!-- FIXME!!!!!!!!!! -->
			<element>
				<!-- Health causes code -->
				<!-- FIXME p176 need xsl:choose for this -->
				<content></content>
			</element>
			</xsl:if>
		</x12segment>

		<!-- 2300 Loop: DTP Referral Date (p184) need if referral FIXME-->
		<!-- 2300 Loop: DTP Date Last Seen (p186) need for psych FIXME-->
		<!-- 2300 Loop: DTP Date of Onset (p188) eoc? FIXME-->
		<!-- 2300 Loop: DTP Date of Last Similar (p192) eoc? FIXME-->
		<!-- 2300 Loop: DTP Date of Accident (p194) eoc? FIXME-->
		<!-- 2300 Loop: DTP Date of Disable Start (p201) eoc? FIXME-->
		<!-- 2300 Loop: DTP Date of Disable End (p203) eoc? FIXME-->

		<!-- 2300 Loop: DTP Date of Admit (p208) eoc? FIXME-->
		<xsl:variable name="facx12" select="//facility[@id=$facility]/x12code" />
		<xsl:if test="($facx12 = '21') or ($facx12 = '51')">
		<x12segment sid="DTP">
			<element>
				<!-- Time ID - 435 = Admission -->
				<content>435</content>
			</element>
			<element>
				<!-- D8 = date expressed CCYYMMDD -->
				<content>D8</content>
			</element>
			<element>
				<!-- Date of admission -->
				<content><xsl:value-of select="concat($procs[1]/dateofhospitalstart/year, $procs[1]/dateofhospitalstart/month, $procs[1]/dateofhospitalstart/day)" /></content>
			</element>
		</x12segment>

		<!-- 2300 Loop: DTP Date of Discharge (p210) eoc? FIXME-->
		<xsl:if test="$procs[1]/dateofhospitalend/year &gt; 1990">
		<x12segment sid="DTP">
			<element>
				<!-- Time ID - 096 = Admission -->
				<content>096</content>
			</element>
			<element>
				<!-- D8 = date expressed CCYYMMDD -->
				<content>D8</content>
			</element>
			<element>
				<!-- Date of discharge -->
				<content><xsl:value-of select="concat($procs[1]/dateofhospitalend/year, $procs[1]/dateofhospitalend/month, $procs[1]/dateofhospitalend/day)" /></content>
			</element>
		</x12segment>
		</xsl:if> <!-- end if there is a discharge date -->
		</xsl:if> <!-- end if x12 code is 21 or 51 -->

		<!-- 2300 Loop: AMT Patient Amount Paid (p220) -->
		<x12segment sid="AMT">
			<element>
				<!-- Amount qualifier (F5 = patient amt paid) -->
				<content>F5</content>
			</element>
			<element>
				<!-- Monetary amount -->
				<content><xsl:value-of select="format-number(sum($procs[facilitykey=$facility]/amountpaid), '#.00')" /></content>
			</element>
		</x12segment>

		<!-- 2300 Loop: AMT Total Purchased Service Amount (p221) -->
		<x12segment sid="AMT">
			<element>
				<!-- Amount qualifier (NE = net billed) -->
				<content>NE</content>
			</element>
			<element>
				<!-- Monetary amount -->
				<content><xsl:value-of select="format-number(sum($procs[facilitykey=$facility]/cptcharges), '#.00')" /></content>
			</element>
		</x12segment>

		<!-- 2300 Loop: REF Prior Auth or Referral (p227) FIXME -->

		<!-- 2300 Loop: REF Medical Record Num (p241) -->
		<x12segment sid="REF">
			<element>
				<content>EA</content>
			</element>
			<element>
				<content><xsl:value-of select="$patient" /></content>
			</element>
		</x12segment>

		<!-- Set diagnoses is distinct set ... -->
		<xsl:variable name="diags" select="set:distinct(exsl:node-set($procs/diagnosiskey))" />

		<!-- 2300 Loop: HI Healthcare information codes (p266) -->
		<x12segment sid="HI">
			<!-- Loop for codes, but can't have more than 12 -->
			<xsl:for-each select="$diags">
				<element>
					<!-- Actual ICD code -->
					<!-- <content><xsl:choose><xsl:when test="position() = 1">BK</xsl:when><xsl:otherwise>BF</xsl:otherwise></xsl:choose>:<xsl:value-of select="//diagnosis[@id=$code]/icd9code" /> (<xsl:value-of select="$code" />)</content> -->
					<content><xsl:choose><xsl:when test="position() = 1">BK</xsl:when><xsl:otherwise>BF</xsl:otherwise></xsl:choose>:<xsl:call-template name="display-diagnosis">
						<xsl:with-param name="diag" select="." />
					</xsl:call-template></content>
				</element>
			</xsl:for-each>
		</x12segment>

		<!-- 2310A Loop: NM1 Referring Provider Name (p282) -->

		<!-- Get TIN from procedure 1 -->
		<xsl:variable name="firstprov" select="//provider[@id = $procs[1]/providerkey]" />

		<!-- 2310B Loop: Rendering Provider -->
		<x12segment sid="NM1">
			<element>
				<!-- Rendering provider -->
				<content>82</content>
			</element>
			<element>
				<!-- Person -->
				<content>1</content>
			</element>
			<element>
				<content><xsl:value-of select="translate($firstprov/name/last, $lowercase, $uppercase)" /></content>
			</element>
			<element>
				<content><xsl:value-of select="translate($firstprov/name/first, $lowercase, $uppercase)" /></content>
			</element>
			<element>
				<content><xsl:value-of select="translate($firstprov/name/middle, $lowercase, $uppercase)" /></content>
			</element>
			<element>
				<!-- Prefix -->
				<content />
			</element>
			<element>
				<!-- Suffix -->
				<content />
			</element>
			<element>
				<!-- ID Code qualifier ( 24 = EIN, 34 = SSN, XX = HCFA ID ) -->
				<content>34</content>
			</element>
			<element>
				<!-- ID Code -->
				<content><xsl:value-of select="$firstprov/socialsecuritynumber" /></content>
			</element>
		</x12segment>

		<!-- 2310D Loop: NM1 Service Facility Location (p303) -->
		<x12segment sid="NM1">
			<element>
				<!-- Entity identifier (FA = facility) -->
				<content>FA</content>
			</element>
			<element>
				<!-- Non-person entity -->
				<content>2</content>
			</element>
			<element>
				<!-- Location/facility name -->
				<content><xsl:value-of select="translate(//facility[@id=$facility]/name, $lowercase, $uppercase)"/></content>
			</element>
			<element>
				<content/>
			</element>
			<element>
				<content/>
			</element>
			<element>
				<content/>
			</element>
			<element>
				<content/>
			</element>
			<element>
				<content>24</content>
			</element>
			<element>
				<!-- HACK: Provider TIN -->
				<content><xsl:value-of select="$firstprov/tin" /></content>
			</element>
		</x12segment>

		<!-- 2310D Loop: N3 Service Facility Location (p???) -->
		<x12segment sid="N3">
			<element>
				<content><xsl:value-of select="translate(//facility[@id=$facility]/address/streetaddress, $lowercase, $uppercase)"/></content>
			</element>
		</x12segment>

		<!-- 2310D Loop: N4 Service Facility Location (p???) -->
		<x12segment sid="N4">
			<element>
				<content><xsl:value-of select="translate(//facility[@id=$facility]/address/city, $lowercase, $uppercase)"/></content>
			</element>
			<element>
				<content><xsl:value-of select="translate(//facility[@id=$facility]/address/state, $lowercase, $uppercase)"/></content>
			</element>
			<element>
				<content><xsl:value-of select="//facility[@id=$facility]/address/zipcode"/></content>
			</element>
		</x12segment>

		<!-- FREEB X12.XML ITEM #29 -->

		<!-- 2310E Loop: N3 Service Facility Address (p355) -->
		<!-- 2310E Loop: N4 Service Facility Address (p356) -->
		<!-- 2320 Loop: OI Other Coverage Info (p391) -->
		<!-- 2330A Loop: NM1 Other subscriber name (p401) sometimes -->
		<!-- 2330B Loop: NM1 Other payer name (p411) sometimes -->


		<!-- Loop through all claims that apply for this combination -->
		<xsl:for-each select="$procs[facilitykey=$facility]">
			<xsl:call-template name="process-procedure">
				<xsl:with-param name="procedure" select="@id" />
				<xsl:with-param name="sequence" select="position()" />
				<xsl:with-param name="diags" select="$diags" />
			</xsl:call-template>
		</xsl:for-each>
	</xsl:template>

	<!--
		display-diagnosis

		Format a diagnosis based on it's key attribute, and
		return it in a format ready to be displayed in an X12
		HI segment.
	-->
	<xsl:template name="display-diagnosis">
		<xsl:param name="diag" />
		<xsl:value-of select="translate(//diagnosis[@id=$diag]/icd9code, '.', '')" />
	</xsl:template>

	<xsl:template name="process-procedure">
		<xsl:param name="procedure" />
		<xsl:param name="sequence" />
		<xsl:param name="diags" />
		
		<xsl:variable name="procobj" select="//procedure[@id = $procedure]" />

		<!-- 2400 Loop: LX Service Line Counter (p399) -->
		<x12segment sid="LX">
			<element>
				<!-- Counter, individual for line items -->
				<xsl:element name="counter">
					<xsl:attribute name="name">LX<xsl:value-of select="concat($procobj/patientkey, 'X', $procobj/facilitykey)" /></xsl:attribute>
				</xsl:element>
			</element>
		</x12segment>

		<!-- 2400 Loop: SV1 Professional Service (p446) -->
		<x12segment sid="SV1">
			<element>
				<!-- Procedure code [ and modifier ] -->
				<content><xsl:value-of select="concat('HC:', $procobj/cpt4code)" /><xsl:if test="boolean(string($procobj/cptmodifier))"><xsl:value-of select="concat(':', $procobj/cptmodifier)" /></xsl:if></content>
			</element>
			<element>
				<!-- Monetary amount -->
				<content><xsl:value-of select="format-number($procobj/cptcharges, '#.00')" /></content>
			</element>
			<element>
				<!-- Units of measurement -->
				<content>UN</content>
			</element>
			<element>
				<!-- Quantity -->
				<content><xsl:value-of select="$procobj/cptunits" /></content>
			</element>
			<element>
				<!-- Facility X12 code -->
				<content><xsl:value-of select="//facility[@id=$procobj/facilitykey]/x12code" /></content>
			</element>
			<element>
				<!-- Service type code (NOT USED) -->
				<content></content>
			</element>
			<element>
				<!-- Diagnosis code references -->
				<content><xsl:call-template name="lookup-diagnoses">
					<xsl:with-param name="diags" select="$procobj/diagnosiskey" />
					<xsl:with-param name="set" select="$diags" />
				</xsl:call-template></content>
			</element>
		</x12segment>

		<!-- 2400 Loop: DTP Service Dates (p435) -->
		<x12segment sid="DTP">
			<element>
				<!-- Date/Time Qualifier -->
				<content>472</content>
			</element>
			<element>
				<!-- Date Time Period Format Qualifier -->
				<content>D8</content>
			</element>
			<element>
				<!-- Date/Time Period -->
				<content><xsl:value-of select="concat($procobj/dateofservicestart/year, $procobj/dateofservicestart/month, $procobj/dateofservicestart/day)" /></content>
			</element>
		</x12segment>

		<!-- Deal with prior authorization number if given -->
		<!-- Black magic trick #133: boolean(string(x)) is the same
			as PHP's !empty(x) ... -->
		<xsl:if test="boolean(string($procobj/priorauth))">
		<x12segment sid="REF">
			<element>
				<!-- Reference Qualifier -->
				<content>G1</content>
			</element>
			<element>
				<!-- Auth number -->
				<content><xsl:value-of select="$procobj/priorauth" /></content>
			</element>
		</x12segment>
		</xsl:if>

		<!-- Loop 2420A: Rendering Provider per Procedure (p501) -->
		<xsl:variable name="provobj" select="//provider[@id=$procobj/providerkey]" />
		<x12segment sid="NM1">
			<element>
				<!-- Entity Identifier: rending provider -->
				<content>82</content>
			</element>
			<element>
				<!-- Entity Type Qualifier -->
				<content>1</content>
			</element>
			<element>
				<!-- Last name -->
				<content><xsl:value-of select="translate($provobj/name/last, $lowercase, $uppercase)" /></content>
			</element>
			<element>
				<!-- First name -->
				<content><xsl:value-of select="translate($provobj/name/first, $lowercase, $uppercase)" /></content>
			</element>
			<element>
				<!-- Middle name -->
				<content><xsl:value-of select="translate($provobj/name/middle, $lowercase, $uppercase)" /></content>
			</element>
			<element>
				<!-- Name Prefix -->
				<content />
			</element>
			<element>
				<!-- Name Suffix -->
				<content />
			</element>
			<element>
				<!-- Identification Code Qualifier -->
				<!-- 24 = EIN, 34 = SSN, XX = HCFA Pin -->
				<!-- FIXME: 34 cannot be used with Medicare -->
				<content>34</content>
			</element>
			<element>
				<!-- Identification code -->
				<content><xsl:value-of select="$provobj/socialsecuritynumber" /></content>
			</element>
		</x12segment>

		<xsl:comment>FIXME: PRV disabled for now</xsl:comment>
		<xsl:if test="0 &gt; 1">
		<x12segment sid="PRV">
			<!-- PRV (p504) -->
			<element>
				<!-- Provider Code (PE = performing) -->
				<content>PE</content>
			</element>
			<element>
				<!-- Reference Identification Qualifier -->
				<!-- Mutually defined taxonomy list -->
				<content>ZZ</content>
			</element>
			<element>
				<!-- FIXME! We don't store this code in the spec yet! -->
				<content></content>
			</element>
		</x12segment>
		</xsl:if>

		<!-- Loop 2420C: Service Facility Location (p514) optional -->
		<!-- Loop 2420F: Referring Provider (p541) FIXME -->
	</xsl:template>

	<xsl:template name="lookup-diagnoses">
		<xsl:param name="diags" />
		<xsl:param name="set" />
		<xsl:variable name="resultset">
			<xsl:for-each select="$diags">

				<xsl:call-template name="lookup-diagnosis">
					<xsl:with-param name="diag" select="current()" />
					<xsl:with-param name="set" select="$set" />
				</xsl:call-template>
				-
			</xsl:for-each>
		</xsl:variable>

		<!-- Stupid pet trick #441: translate, normalize-space and
			translate again to use ':' as spacers. Due to it
			not putting enough space in, I inserted a '-'
			character, which is removed in this step. -->
		<xsl:value-of select="translate(normalize-space(translate($resultset, '-', '')), ' ', ':')" />
	</xsl:template>

	<xsl:template name="lookup-diagnosis">
		<xsl:param name="diag" />
		<xsl:param name="set" />
		<xsl:for-each select="$set">
			<xsl:if test="current()=$diag"><xsl:value-of select="position()" /></xsl:if>
		</xsl:for-each>
	</xsl:template>

	<!-- 
		EXSLT functions

		Please note that only function which are not included in
		the libxslt library are included here, since that is the
		XSLT compiler we are using. EXSLT functions for anything
		else can be found at http://exslt.org/.
	-->

	<!--
	<exsl:function name="set:distinct">
		<xsl:param name="nodes" select="/.."/>
		<xsl:choose>
			<xsl:when test="not($nodes)">
				<exsl:result select="/.." />
			</xsl:when>
			<xsl:otherwise>
				<xsl:variable name="distinct" select="set:distinct($nodes[position() &gt; 1])"/>
				<exsl:result select="$distinct | $nodes[1][. != $distinct]"/>
			</xsl:otherwise>
		</xsl:choose>
	</exsl:function>
	-->

	<xsl:template name="sequence-location">
		<xsl:param name="id"    />
		<xsl:param name="nodes" />
		<xsl:for-each select="$nodes">
			<xsl:if test="@id = $id">
				<xsl:value-of select="position()" />
			</xsl:if>
		</xsl:for-each>
	</xsl:template>

</xsl:stylesheet>

