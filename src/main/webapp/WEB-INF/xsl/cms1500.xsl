<?xml version="1.0"?>
<!--

  $Id$

  Authors:
 	Jeff Buchbinder <jeff@freemedsoftware.org>
 
  REMITT Electronic Medical Information Translation and Transmission
  Copyright (C) 1999-2016 FreeMED Software Foundation
 
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

        <xsl:output method="xml" indent="yes" />

	<!-- Parameters -->
	<xsl:param name="currentTime" />
	<xsl:param name="jobId" />

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

	<!--
		Master template for document root

		This template processes the document root, and calls all
		child templates.
	-->

	<xsl:template match="/remitt">
        <fixedform>
		<xsl:variable name="practices" select="//practice" />

		<xsl:for-each select="$practices">
			<xsl:call-template name="process-practice">
				<xsl:with-param name="practice">
					<xsl:value-of select="@id"/>
				</xsl:with-param>	
			</xsl:call-template>
		</xsl:for-each>
        </fixedform>
	</xsl:template>

	<xsl:template name="process-practice">
		<xsl:param name="practice" />

		<xsl:variable name="providers" select="set:distinct(exsl:node-set(//procedure[practicekey=$practice]/providerkey))" />
		<xsl:for-each select="$providers">
			<xsl:call-template name="process-provider">
				<xsl:with-param name="practice" select="$practice" />
				<xsl:with-param name="provider" select="." />
			</xsl:call-template>
		</xsl:for-each>
	</xsl:template>

	<xsl:template name="process-provider">
		<xsl:param name="practice" />
		<xsl:param name="provider" />

		<xsl:variable name="payers" select="set:distinct(exsl:node-set(//procedure[practicekey=$practice and providerkey=$provider]/payerkey))" />
		<xsl:for-each select="$payers">
			<xsl:call-template name="process-payer">
				<xsl:with-param name="practice" select="$practice" />
				<xsl:with-param name="provider" select="$provider" />
				<xsl:with-param name="payer" select="." />
			</xsl:call-template>
		</xsl:for-each>
	</xsl:template>

	<xsl:template name="process-payer">
		<xsl:param name="practice" />
		<xsl:param name="provider" />
		<xsl:param name="payer" />

		<xsl:variable name="facilities" select="set:distinct(exsl:node-set(//procedure[practicekey=$practice and providerkey=$provider and payerkey=$payer]/facilitykey))" />
		<xsl:for-each select="$facilities">
			<xsl:call-template name="process-facility">
				<xsl:with-param name="practice" select="$practice" />
				<xsl:with-param name="provider" select="$provider" />
				<xsl:with-param name="payer" select="$payer" />
				<xsl:with-param name="facility" select="." />
			</xsl:call-template>
		</xsl:for-each>
	</xsl:template>

	<xsl:template name="process-facility">
		<xsl:param name="practice" />
		<xsl:param name="provider" />
		<xsl:param name="payer" />
		<xsl:param name="facility" />

		<xsl:variable name="patients" select="set:distinct(exsl:node-set(//procedure[practicekey=$practice and providerkey=$provider and payerkey=$payer and facilitykey=$facility]/patientkey))" />
		<xsl:for-each select="$patients">
			<xsl:call-template name="process-patient">
				<xsl:with-param name="practice" select="$practice" />
				<xsl:with-param name="provider" select="$provider" />
				<xsl:with-param name="payer" select="$payer" />
				<xsl:with-param name="facility" select="$facility" />
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
		<xsl:param name="provider" />
		<xsl:param name="payer" />
		<xsl:param name="facility" />
		<xsl:param name="patient" />

		<xsl:variable name="insureds" select="set:distinct(exsl:node-set(//procedure[practicekey=$practice and providerkey=$provider and payerkey=$payer and facilitykey=$facility and patientkey=$patient]/insuredkey))" />
		<xsl:for-each select="$insureds">
			<xsl:call-template name="process-insured">
				<xsl:with-param name="practice" select="$practice" />
				<xsl:with-param name="provider" select="$provider" />
				<xsl:with-param name="payer" select="$payer" />
				<xsl:with-param name="facility" select="$facility" />
				<xsl:with-param name="patient" select="$patient" />
				<xsl:with-param name="insured" select="." />
			</xsl:call-template>
		</xsl:for-each>
	</xsl:template>

	<!--
		process-insured
	-->
	<xsl:template name="process-insured">
		<xsl:param name="practice" />
		<xsl:param name="provider" />
		<xsl:param name="payer" />
		<xsl:param name="facility" />
		<xsl:param name="patient" />
		<xsl:param name="insured" />

		<!-- Get initial procedure set -->
		<xsl:variable name="procs" select="set:distinct(exsl:node-set(//procedure[practicekey=$practice and providerkey=$provider and payerkey=$payer and facilitykey=$facility and patientkey=$patient and insuredkey = $insured]))" />
		<xsl:call-template name="process-procedure-set">
			<xsl:with-param name="procs" select="$procs" />
		</xsl:call-template>
	</xsl:template>

	<xsl:template name="process-procedure-set">
		<xsl:param name="procs" />

		<!-- If there are procedures ... -->
		<xsl:if test="count($procs) &gt; 0">
			<!-- If there are too many diagnoses ... -->
			<xsl:choose>
				<xsl:when test="count(set:distinct(exsl:node-set($procs/diagnosiskey))) &gt; 4 or count($procs) &gt; 6">
					<!-- Get largest subset -->
					<xsl:variable name="biggestindex">
						<xsl:call-template name="procs-get-largest-subset">
							<xsl:with-param name="procs" select="$procs" />
						</xsl:call-template>
					</xsl:variable>

					<!-- Split segments -->
					<xsl:variable name="fit" select="$procs[position() &lt;= $biggestindex]" />
					<xsl:variable name="overflow" select="$procs[position() &gt; $biggestindex]" />

					<!-- Process using first segment -->
					<!-- <xsl:message>Processing fit segment of <xsl:value-of select="count($fit)" /></xsl:message> -->
					<xsl:call-template name="render-form">
						<xsl:with-param name="procs" select="$fit/@id" />
						<xsl:with-param name="procobjs" select="$fit" />
						<xsl:with-param name="diags" select="set:distinct(exsl:node-set($fit/diagnosiskey))" />
					</xsl:call-template>

					<!-- Recurse using overflow -->
					<!-- <xsl:message>Processing overflow of <xsl:value-of select="count($overflow)" /></xsl:message> -->
					<xsl:call-template name="process-procedure-set">
						<xsl:with-param name="procs" select="$overflow" />
					</xsl:call-template>
				</xsl:when>
				<xsl:otherwise>
					<xsl:call-template name="render-form">
						<xsl:with-param name="procs" select="$procs/@id" />
						<xsl:with-param name="procobjs" select="$procs" />
						<xsl:with-param name="diags" select="set:distinct(exsl:node-set($procs/diagnosiskey))" />
					</xsl:call-template>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:if>
	</xsl:template>

	<!--
		render-form

		Does all the actual form rendering for the HCFA-1500 form.
	-->
	<xsl:template name="render-form">
		<xsl:param name="procs" />
		<xsl:param name="procobjs" />
		<xsl:param name="diags" />

		<!-- <xsl:message>[render-form] rendering <xsl:value-of select="count($procs)" /> procedures</xsl:message> -->

		<!-- Since everything else is the same, get info from first. -->
		<xsl:variable name="insured" select="//procedure[@id = $procs[1]]/insuredkey" />
		<xsl:variable name="insuredobj" select="//insured[@id = $insured]" />
		<xsl:variable name="patient" select="//procedure[@id = $procs[1]]/patientkey" />
		<xsl:variable name="patientobj" select="//patient[@id = $patient]" />
		<xsl:variable name="payer" select="//procedure[@id = $procs[1]]/payerkey" />
		<xsl:variable name="payerobj" select="//payer[@id = $payer]" />
		<xsl:variable name="facility" select="//procedure[@id = $procs[1]]/facilitykey" />
		<xsl:variable name="facilityobj" select="//facility[@id = $facility]" />
		<xsl:variable name="provider" select="//procedure[@id = $procs[1]]/providerkey" />
		<xsl:variable name="providerobj" select="//provider[@id = $provider]" />
		<xsl:variable name="practice" select="//procedure[@id = $procs[1]]/practicekey" />
		<xsl:variable name="practiceobj" select="//practice[@id = $practice]" />

		<!-- Primary diagnosis object -->
		<xsl:variable name="diag" select="$diags[1]" />
		<xsl:variable name="diagobj" select="//diagnosis[@id = $diag]" />
		<xsl:variable name="procfirstobj" select="//procedure[@id = $procs[1]]" />

		<!-- Secondary payer information -->
		<xsl:variable name="secondary" select="//procedure[@id = $procs[1]]/secondinsuredkey" />
		<xsl:variable name="secondaryobj" select="//insured[@id = $secondary]" />
		<xsl:variable name="secondpayer" select="//procedure[@id = $procs[1]]/secondpayerkey" />
		<xsl:variable name="secondpayerobj" select="//payer[@id = $secondpayer]" />

		<page>
			<format>
				<pagelength>66</pagelength>
				<pdf template="cms1500" page="1">
					<font name="Courier" size="10" />
					<scaling vertical="12" horizontal="7.2" />
					<offset vertical="-38" horizontal="55" />
				</pdf>
			</format>
	
			<element>
				<!-- Insurance Company Name -->
				<row>1</row>
				<column>41</column>
				<length>25</length>
				<content><xsl:value-of select="translate($payerobj/name, $lowercase, $uppercase)" /></content>
			</element>
	
			<element>
				<!-- Insurance Company Attn -->
				<row>2</row>
				<column>41</column>
				<length>25</length>
				<content><xsl:value-of select="translate($payerobj/attn, $lowercase, $uppercase)" /></content>
			</element>
	
			<element>
				<!-- Insurance Company Street Address -->
				<row>3</row>
				<column>41</column>
				<length>25</length>
				<content><xsl:value-of select="translate($payerobj/address/streetaddress, $lowercase, $uppercase)" /></content>
			</element>
	
			<element>
				<!-- Insurance Company Street City, State Zipcode -->
				<row>4</row>
				<column>41</column>
				<length>30</length>
				<content><xsl:value-of select="translate(concat($payerobj/address/city,', ', $payerobj/address/state, ' ', $payerobj/address/zipcode), $lowercase, $uppercase)" /></content>
			</element>
	
			<xsl:if test="boolean(string($secondpayer))">
			<element omitpdf="1">
				<!-- Secondary Insurance Company Name -->
				<row>1</row>
				<column>1</column>
				<length>25</length>
				<content><xsl:value-of select="translate($secondpayerobj/name, $lowercase, $uppercase)" /></content>
			</element>
	
			<element omitpdf="1">
				<!-- Secondary Insurance Company Attn -->
				<row>2</row>
				<column>1</column>
				<length>25</length>
				<content><xsl:value-of select="translate($secondpayerobj/attn, $lowercase, $uppercase)" /></content>
			</element>
	
			<element omitpdf="1">
				<!-- Secondary Insurance Company Street Address -->
				<row>3</row>
				<column>1</column>
				<length>25</length>
				<content><xsl:value-of select="translate($secondpayerobj/address/streetaddress, $lowercase, $uppercase)" /></content>
			</element>
	
			<element omitpdf="1">
				<!-- Secondary Insurance Company Street City, State Zipcode -->
				<row>4</row>
				<column>1</column>
				<length>30</length>
				<content><xsl:value-of select="translate(concat($secondpayerobj/address/city,', ', $secondpayerobj/address/state, ' ', $secondpayerobj/address/zipcode), $lowercase, $uppercase)" /></content>
			</element>

			<element omitpdf="1">
				<!-- Secondary Insurance/Facility has Provider -  PIN # -->
				<row>5</row>
				<column>1</column>
				<length>12</length>
				<content><xsl:value-of select="//practice[@id = $procfirstobj/practicekey]/id[@physician = $provider and @payer=$secondpayer]" /></content>
			</element>

			<element omitpdf="1">
				<!-- Secondary Insurance/Facility has Provider -  Group # -->
				<row>5</row>
				<column>17</column>
				<length>12</length>
				<content><xsl:value-of select="//practice[@id = $procfirstobj/practicekey]/groupid[@physician = $provider and @payer=$secondpayer]" /></content>
			</element>
			
			</xsl:if>
	
			<xsl:if test="$payerobj/ismedicare = 1">
			<element>
				<!-- Is Medicare? -->
				<row>8</row>
				<column>1</column>
				<length>1</length>
				<content>X</content>
			</element>
			</xsl:if>

			<xsl:if test="$payerobj/ismedicaid = 1">
			<element>
				<!-- Is Medicaid? -->
				<row>8</row>
				<column>8</column>
				<length>1</length>
				<content>X</content>
			</element>
			</xsl:if>

			<xsl:if test="$payerobj/ischampus = 1">
			<element>
				<!-- Is Champus? -->
				<row>8</row>
				<column>15</column>
				<length>1</length>
				<content>X</content>
			</element>
			</xsl:if>

			<xsl:if test="$payerobj/ischampusva = 1">
			<element>
				<!-- Is ChampusVA? -->
				<row>8</row>
				<column>24</column>
				<length>1</length>
				<content>X</content>
			</element>
			</xsl:if>

			<xsl:if test="$payerobj/ismedicare = 1">
			<element>
				<!-- Is Medicare? -->
				<row>8</row>
				<column>1</column>
				<length>1</length>
				<content>X</content>
			</element>
			</xsl:if>

			<xsl:if test="$payerobj/isbcbs = 1">
			<element>
				<!-- Is BCBS? -->
				<row>8</row>
				<column>31</column>
				<length>1</length>
				<content>X</content>
			</element>
			</xsl:if>

			<xsl:if test="$payerobj/isfeca = 1">
			<element>
				<!-- Is FECA? -->
				<row>8</row>
				<column>39</column>
				<length>1</length>
				<content>X</content>
			</element>
			</xsl:if>

			<xsl:if test="$payerobj/isotherhcfa = 1">
			<element>
				<!-- Is HCFA "other"? -->
				<row>8</row>
				<column>45</column>
				<length>1</length>
				<content>X</content>
			</element>
			</xsl:if>

			<element>
				<!-- Box 1a: Insured ID Number -->
				<row>8</row>
				<column>50</column>
				<length>17</length>
				<content><xsl:value-of select="$insuredobj/id" /></content>
			</element>

			<element>
				<!-- Box 2: Patient Name / Last -->
				<row>10</row>
				<column>1</column>
				<length>17</length>
				<content><xsl:value-of select="translate($patientobj/name/last, $lowercase, $uppercase)" /></content>
			</element>

			<element>
				<!-- Box 2: Patient Name / First -->
				<row>10</row>
				<column>19</column>
				<length>9</length>
				<content><xsl:value-of select="translate($patientobj/name/first, $lowercase, $uppercase)" /></content>
			</element>

			<element>
				<!-- Box 2: Patient Name / Middle -->
				<row>10</row>
				<column>29</column>
				<length>1</length>
				<content><xsl:value-of select="translate($patientobj/name/middle, $lowercase, $uppercase)" /></content>
			</element>

			<element>
				<!-- Box 3: Birth Date / MM -->
				<row>10</row>
				<column>31</column>
				<length>2</length>
				<content><xsl:value-of select="$patientobj/dateofbirth/month" /></content>
			</element>

			<element>
				<!-- Box 3: Birth Date / DD -->
				<row>10</row>
				<column>34</column>
				<length>2</length>
				<content><xsl:value-of select="$patientobj/dateofbirth/day" /></content>
			</element>

			<element>
				<!-- Box 3: Birth Date / YY -->
				<row>10</row>
				<column>37</column>
				<length>2</length>
				<content><xsl:value-of select="substring($patientobj/dateofbirth/year, 3, 2)" /></content>
			</element>

			<xsl:if test="translate($patientobj/sex, $lowercase, $uppercase) = 'M'">
			<element>
				<!-- Box 3: Gender / M -->
				<row>10</row>
				<column>42</column>
				<length>1</length>
				<content>X</content>
			</element>
			</xsl:if>

			<xsl:if test="translate($patientobj/sex, $lowercase, $uppercase) = 'F'">
			<element>
				<!-- Box 3: Gender / F -->
				<row>10</row>
				<column>47</column>
				<length>1</length>
				<content>X</content>
			</element>
			</xsl:if>

			<element>
				<!-- Box 4: Insured Name / Last -->
				<row>10</row>
				<column>50</column>
				<length>16</length>
				<content><xsl:value-of select="translate($insuredobj/name/last, $lowercase, $uppercase)" /></content>
			</element>

			<element>
				<!-- Box 4: Insured Name / First -->
				<row>10</row>
				<column>67</column>
				<length>9</length>
				<content><xsl:value-of select="translate($insuredobj/name/first, $lowercase, $uppercase)" /></content>
			</element>

			<element>
				<!-- Box 4: Insured Name / Middle -->
				<row>10</row>
				<column>77</column>
				<length>1</length>
				<content><xsl:value-of select="translate($insuredobj/name/middle, $lowercase, $uppercase)" /></content>
			</element>

			<element>
				<!-- Box 5: Patient Address / Street -->
				<row>12</row>
				<column>1</column>
				<length>25</length>
				<content><xsl:value-of select="translate($patientobj/address/streetaddress, $lowercase, $uppercase)" /></content>
			</element>

			<xsl:if test="translate($insuredobj/relationship, $lowercase, $uppercase) = 'S'">
			<element>
				<!-- Box 6: Relationship / Self -->
				<row>12</row>
				<column>33</column>
				<length>1</length>
				<content>X</content>
			</element>
			</xsl:if>

			<xsl:if test="translate($insuredobj/relationship, $lowercase, $uppercase) = 'H' or translate($insuredobj/relationship, $lowercase, $uppercase) = 'W'">
			<element>
				<!-- Box 6: Relationship / Husband or Wife -->
				<row>12</row>
				<column>38</column>
				<length>1</length>
				<content>X</content>
			</element>
			</xsl:if>

			<xsl:if test="translate($insuredobj/relationship, $lowercase, $uppercase) = 'C'">
			<element>
				<!-- Box 6: Relationship / Child -->
				<row>12</row>
				<column>42</column>
				<length>1</length>
				<content>X</content>
			</element>
			</xsl:if>

			<xsl:if test="translate($insuredobj/relationship, $lowercase, $uppercase) = 'O'">
			<element>
				<!-- Box 6: Relationship / Other -->
				<row>12</row>
				<column>47</column>
				<length>1</length>
				<content>X</content>
			</element>
			</xsl:if>

			<element>
				<!-- Box 7: Insured Address -->
				<row>12</row>
				<column>50</column>
				<length>25</length>
				<content><xsl:value-of select="translate($insuredobj/address/streetaddress, $lowercase, $uppercase)" /></content>
			</element>

			<element>
				<!-- Box 5: Patient Address / City -->
				<row>14</row>
				<column>1</column>
				<length>15</length>
				<content><xsl:value-of select="translate($patientobj/address/city, $lowercase, $uppercase)" /></content>
			</element>

			<element>
				<!-- Box 5: Patient Address / State -->
				<row>14</row>
				<column>26</column>
				<length>2</length>
				<content><xsl:value-of select="translate($patientobj/address/state, $lowercase, $uppercase)" /></content>
			</element>

			<xsl:if test="$patientobj/issingle = 1">
			<element>
				<!-- Box 8: Patient Status / Single -->
				<row>14</row>
				<column>35</column>
				<length>1</length>
				<content>X</content>
			</element>
			</xsl:if>

			<xsl:if test="$patientobj/ismarried = 1">
			<element>
				<!-- Box 8: Patient Status / Married -->
				<row>14</row>
				<column>41</column>
				<length>1</length>
				<content>X</content>
			</element>
			</xsl:if>

			<xsl:if test="$patientobj/ismaritalotherhcfa = 1">
			<element>
				<!-- Box 8: Patient Status / Other -->
				<row>14</row>
				<column>47</column>
				<length>1</length>
				<content>X</content>
			</element>
			</xsl:if>

			<element>
				<!-- Box 5: Insured Address / City -->
				<row>14</row>
				<column>50</column>
				<length>15</length>
				<content><xsl:value-of select="translate($insuredobj/address/city, $lowercase, $uppercase)" /></content>
			</element>

			<element>
				<!-- Box 5: Insured Address / State -->
				<row>14</row>
				<column>74</column>
				<length>2</length>
				<content><xsl:value-of select="translate($insuredobj/address/state, $lowercase, $uppercase)" /></content>
			</element>

			<element>
				<!-- Box 5: Patient Address / Zip -->
				<row>16</row>
				<column>1</column>
				<length>10</length>
				<content><xsl:value-of select="$patientobj/address/zipcode" /></content>
			</element>

			<element>
				<!-- Box 5: Patient Phone Number -->
				<row>16</row>
				<column>14</column>
				<length>14</length>
				<content><xsl:value-of select="concat(' ',$patientobj/phone/area, ' ', $patientobj/phone/number)" /></content>
			</element>

			<xsl:if test="$patientobj/isemployed = 1">
			<element>
				<!-- Box 8: Patient Status / Employed -->
				<row>16</row>
				<column>35</column>
				<length>1</length>
				<content>X</content>
			</element>
			</xsl:if>

			<xsl:if test="$patientobj/isfulltimestudent = 1">
			<element>
				<!-- Box 8: Patient Status / FT Student -->
				<row>16</row>
				<column>41</column>
				<length>1</length>
				<content>X</content>
			</element>
			</xsl:if>

			<xsl:if test="$patientobj/isparttimestudent = 1">
			<element>
				<!-- Box 8: Patient Status / PT Student -->
				<row>16</row>
				<column>47</column>
				<length>1</length>
				<content>X</content>
			</element>
			</xsl:if>

			<element>
				<!-- Box 7: Insured Address / Zip -->
				<row>16</row>
				<column>50</column>
				<length>10</length>
				<content><xsl:value-of select="$insuredobj/address/zipcode" /></content>
			</element>

			<element>
				<!-- Box 7: Insured Phone Number -->
				<row>16</row>
				<column>64</column>
				<length>14</length>
				<content><xsl:value-of select="concat(' ',$insuredobj/phone/area, ' ', $insuredobj/phone/number)" /></content>
			</element>

			<element>
				<!-- Box 10: Condition Related To -->
				<row>18</row>
				<column>31</column>
				<length>10</length>
				<content><xsl:value-of select="$diagobj/relatedtohcfa" /></content>
			</element>

			<element>
				<!-- Box 11: Group Number -->
				<row>18</row>
				<column>50</column>
				<length>30</length>
				<content><xsl:value-of select="$insuredobj/groupnumber" /></content>
			</element>

			<xsl:if test="boolean(string($secondpayer))">
			<element>
				<!-- Box 9: Other Insured's Name -->
				<row>18</row>
				<column>1</column>
				<length>28</length>
				<content><xsl:value-of select="translate(concat($secondaryobj/name/first, ' ', $secondaryobj/name/last), $lowercase, $uppercase)" /></content>
			</element>
			<element>
				<!-- Box 9a: Other Insured Policy/Group Number -->
				<row>20</row>
				<column>1</column>
				<length>25</length>
				<content><xsl:value-of select="translate($secondaryobj/id, $lowercase, $uppercase)" /></content>
			</element>
			<element>
				<!-- Box 9d: Other Insurance Plan Name -->
				<row>26</row>
				<column>1</column>
				<length>25</length>
				<content><xsl:value-of select="translate(//payer[@id = $secondpayer]/name, $lowercase, $uppercase)" /></content>
			</element>
			</xsl:if>

			<xsl:if test="$diagobj/isrelatedtoemployment = 1">
			<element>
				<!-- Box 10a: Related to Employment / Yes -->
				<row>20</row>
				<column>35</column>
				<length>1</length>
				<content>X</content>
			</element>
			</xsl:if>

			<xsl:if test="not($diagobj/isrelatedtoemployment = 1)">
			<element>
				<!-- Box 10a: Related to Employment / No -->
				<row>20</row>
				<column>41</column>
				<length>1</length>
				<content>X</content>
			</element>
			</xsl:if>

			<xsl:if test="$insuredobj/dateofbirth/month &gt; 0">
			<element>
				<!-- Box 11a: Insured DOB / MM -->
				<row>20</row>
				<column>54</column>
				<length>2</length>
				<content><xsl:value-of select="$insuredobj/dateofbirth/month" /></content>
			</element>

			<element>
				<!-- Box 11a: Insured DOB / DD -->
				<row>20</row>
				<column>57</column>
				<length>2</length>
				<content><xsl:value-of select="$insuredobj/dateofbirth/day" /></content>
			</element>

			<element>
				<!-- Box 11a: Insured DOB / YY -->
				<row>20</row>
				<column>60</column>
				<length>2</length>
				<content><xsl:value-of select="substring($insuredobj/dateofbirth/year, 3, 2)" /></content>
			</element>
			</xsl:if> <!-- if there is an insured DOB -->

			<xsl:if test="translate($insuredobj/sex, $lowercase, $uppercase) = 'M'">
			<element>
				<!-- Box 11a: Insured Sex / Male -->
				<row>20</row>
				<column>68</column>
				<length>1</length>
				<content>X</content>
			</element>
			</xsl:if>

			<xsl:if test="translate($insuredobj/sex, $lowercase, $uppercase) = 'F'">
			<element>
				<!-- Box 11a: Insured Sex / Female -->
				<row>20</row>
				<column>75</column>
				<length>1</length>
				<content>X</content>
			</element>
			</xsl:if>

			<xsl:if test="$insuredobj/dateofbirth/month &gt; 0">
			<element>
				<!-- Box 9b: Insured DOB / MM -->
				<row>22</row>
				<column>2</column>
				<length>2</length>
				<content><xsl:value-of select="$insuredobj/dateofbirth/month" /></content>
			</element>

			<element>
				<!-- Box 9b: Insured DOB / DD -->
				<row>22</row>
				<column>5</column>
				<length>2</length>
				<content><xsl:value-of select="$insuredobj/dateofbirth/day" /></content>
			</element>

			<element>
				<!-- Box 9b: Insured DOB / YY -->
				<row>22</row>
				<column>8</column>
				<length>2</length>
				<content><xsl:value-of select="substring($insuredobj/dateofbirth/year, 3, 2)" /></content>
			</element>
			</xsl:if>

			<xsl:if test="translate($insuredobj/sex, $lowercase, $uppercase) = 'M'">
			<element>
				<!-- Box 9b: Insured Sex / Male -->
				<row>22</row>
				<column>18</column>
				<length>1</length>
				<content>X</content>
			</element>
			</xsl:if>

			<xsl:if test="translate($insuredobj/sex, $lowercase, $uppercase) = 'F'">
			<element>
				<!-- Box 9b: Insured Sex / Female -->
				<row>22</row>
				<column>24</column>
				<length>1</length>
				<content>X</content>
			</element>
			</xsl:if>

			<xsl:if test="$diagobj/isrelatedtoautoaccident = 1">
			<element>
				<!-- Box 10b: Related to Auto Accident / Yes -->
				<row>22</row>
				<column>35</column>
				<length>1</length>
				<content>X</content>
			</element>
			</xsl:if>

			<xsl:if test="not($diagobj/isrelatedtoautoaccident = 1)">
			<element>
				<!-- Box 10b: Related to Auto Accident / Yes -->
				<row>22</row>
				<column>41</column>
				<length>1</length>
				<content>X</content>
			</element>
			</xsl:if>

			<xsl:if test="$diagobj/isrelatedtoautoaccident = 1">
			<element>
				<!-- Box 10b: Related to Auto Accident / State -->
				<row>22</row>
				<column>45</column>
				<length>2</length>
				<content><xsl:value-of select="$diagobj/autoaccidentstate" /></content>
			</element>
			</xsl:if>

			<xsl:if test="$insuredobj/isemployed = 1 or $insuredobj/isstudent = 1">
			<element>
				<!-- Box XX: Employer Name of Insured -->
				<row>22</row>
				<column>50</column>
				<length>30</length>
				<content><xsl:choose>
					<xsl:when test="not($insuredobj/isemployed = 1) and $insuredobj/isstudent = 1"><xsl:value-of select="$insuredobj/schoolname" /></xsl:when>
					<xsl:otherwise><xsl:value-of select="$insuredobj/employername" /></xsl:otherwise>
				</xsl:choose></content>
			</element>
			</xsl:if>

			<xsl:if test="$diagobj/isrelatedtootheraccident = 1">
			<element>
				<!-- Box 10c: Related to other accident -->
				<row>24</row>
				<column>35</column>
				<length>1</length>
				<content>X</content>
			</element>
			</xsl:if>

			<xsl:if test="not($diagobj/isrelatedtootheraccident = 1)">
			<element>
				<!-- Box 10c: Not related to other accident -->
				<row>24</row>
				<column>41</column>
				<length>1</length>
				<content>X</content>
			</element>
			</xsl:if>	

			<element>
				<!-- Box 11c: Insurance Plan / Company -->
				<row>24</row>
				<column>50</column>
				<length>25</length>
				<content><xsl:value-of select="translate($payerobj/name, $lowercase, $uppercase)" /></content>
			</element>

			<element>
				<!-- Box 10d: Local Use 10d -->
				<row>26</row>
				<column>30</column>
				<length>15</length>
				<xsl:choose>
					<!-- Handle medicaid as second payer 10d special case -->
					<xsl:when test="boolean(string($secondpayer)) and //payer[@id = $secondpayer]/ismedicaid = 1">
						<xsl:message><xsl:value-of select="translate($secondary, $lowercase, $uppercase)" /></xsl:message>
						<content><xsl:value-of select="translate(//insured[@id = $secondary]/id, $lowercase, $uppercase)" /></content>
					</xsl:when>
					<xsl:otherwise>
						<content><xsl:value-of select="$procfirstobj/hcfalocaluse10d" /></content>
					</xsl:otherwise>
				</xsl:choose>
			</element>

			<xsl:choose>
			<xsl:when test="boolean(string($secondpayer))">
			<element>
				<!-- Box 11d: Secondary Insurance / Y -->
				<row>26</row>
				<column>52</column>
				<length>1</length>
				<content>X</content>
			</element>
			</xsl:when>
			<xsl:otherwise>
			<element>
				<!-- Box 11d: Secondary Insurance / N -->
				<row>26</row>
				<column>57</column>
				<length>1</length>
				<content>X</content>
			</element>
			</xsl:otherwise>
			</xsl:choose>

			<element>
				<!-- Box 12: Authorized Signature -->
				<row>30</row>
				<column>7</column>
				<length>20</length>
				<content>SIGNATURE ON FILE</content>
			</element>

			<element>
				<!-- Box 12: Authorized Signature / Date -->
				<row>30</row>
				<column>38</column>
				<length>10</length>
				<content><xsl:value-of select="concat(//global/currentdate/month, ' ', //global/currentdate/day, ' ', //global/currentdate/year)" /></content>
			</element>

			<xsl:if test="$insuredobj/isassigning = 1">
			<element>
				<!-- Box XX: Authorized Signature -->
				<row>30</row>
				<column>55</column>
				<length>17</length>
				<content>SIGNATURE ON FILE</content>
			</element>
			</xsl:if>

			<xsl:if test="$diagobj/dateofonset/month &gt; 0">
			<element>
				<!-- Box 14: Date of Onset / MM-->
				<row>32</row>
				<column>2</column>
				<length>2</length>
				<content><xsl:value-of select="$diagobj/dateofonset/month" /></content>
			</element>

			<element>
				<!-- Box 14: Date of Onset / DD -->
				<row>32</row>
				<column>5</column>
				<length>2</length>
				<content><xsl:value-of select="$diagobj/dateofonset/day" /></content>
			</element>

			<element>
				<!-- Box 14: Date of Onset / YY -->
				<row>32</row>
				<column>8</column>
				<length>4</length>
				<content><xsl:value-of select="$diagobj/dateofonset/year" /></content>
			</element>
			</xsl:if>

			<!-- FIXME: Check for first occurance and insert date -->
			<!-- FIXME: isCantWork equiv -->

			<xsl:if test="($patientobj/referringprovider + 0) &gt; 0">
			<xsl:variable name="refprov" select="//provider[@id = $procfirstobj/referringproviderkey]" />
			<element>
				<!-- Box 17: Referring Physician / Name -->
				<row>34</row>
				<col>1</col>
				<length>25</length>
				<content><xsl:value-of select="translate(concat($refprov/name/first, ' ', $refprov/name/last), $lowercase, $uppercase)" /></content>
			</element>

			<element>
				<!-- Box 17b: Referring Physician / NPI -->
				<row>34</row>
				<column>33</column>
				<length>10</length>
				<content><xsl:value-of select="$refprov/npi" /></content>
			</element>
			</xsl:if>

		<!-- Hospitalization dates -->
		<xsl:comment>#### HOSPITALIZATION DATES ####</xsl:comment>
		<xsl:if test="$procfirstobj/ishospitalized = 1">
		<element>
			<!-- Box 18: Date of Hospitalization Start -->
			<row>35</row>
			<column>55</column>
			<length>17</length>
			<content><xsl:value-of select="concat($procfirstobj/dateofhospitalstart/month, '   ', $procfirstobj/dateofhospitalstart/day, '    ', $procfirstobj/dateofhospitalstart/year)" /></content>
		</element>
		<element>
			<!-- Box 18: Date of Hospitalization End -->
			<row>35</row>
			<column>69</column>
			<length>17</length>
			<content><xsl:value-of select="concat($procfirstobj/dateofhospitalend/month, '   ', $procfirstobj/dateofhospitalend/day, '    ', $procfirstobj/dateofhospitalend/year)" /></content>
		</element>
		</xsl:if>


			<element>
				<!-- Box 19: Local Use -->
				<row>36</row>
				<column>1</column>
				<length>50</length>
				<content><xsl:value-of select="$procfirstobj/hcfalocaluse19" /></content>
			</element>

			<xsl:if test="$procfirstobj/isoutsidelab = 1">
			<element>
				<!-- Box 20: Outside Lab / Y -->
				<row>36</row>
				<column>52</column>
				<length>1</length>
				<content>X</content>
			</element>

			<element>
				<!-- Box 20: Outside Lab / Charges -->
				<row>36</row>
				<column>65</column>
				<length>8</length>
				<content><xsl:value-of select="format-number($procfirstobj/outsidelabcharges, '###0.00')" /></content>
				<format right="1" />
			</element>
			</xsl:if>

			<xsl:if test="not($procfirstobj/isoutsidelab = 1)">
			<element>
				<!-- Box 20: Outside Lab / N -->
				<row>36</row>
				<column>57</column>
				<length>1</length>
				<content>X</content>
			</element>
			</xsl:if>

			<!-- Handle ICD codes, if they exist -->
			<element periodstrippdf="1">
				<!-- Box 21: ICD Code / #1 -->
				<row>38</row>
				<column>3</column>
				<length>10</length>
				<content><xsl:value-of select="//diagnosis[@id = $diags[1]]/icd9code" /></content>
			</element>

			<xsl:if test="count($diags) &gt; 1">
			<element periodstrippdf="1">
				<!-- Box 21: ICD Code / #2 -->
				<row>40</row>
				<column>3</column>
				<length>10</length>
				<content><xsl:value-of select="//diagnosis[@id = $diags[2]]/icd9code" /></content>
			</element>
			</xsl:if>

			<xsl:if test="count($diags) &gt; 2">
			<element periodstrippdf="1">
				<!-- Box 21: ICD Code / #3 -->
				<row>38</row>
				<column>30</column>
				<length>10</length>
				<content><xsl:value-of select="//diagnosis[@id = $diags[3]]/icd9code" /></content>
			</element>
			</xsl:if>

			<xsl:if test="count($diags) &gt; 3">
			<element periodstrippdf="1">
				<!-- Box 21: ICD Code / #4 -->
				<row>40</row>
				<column>30</column>
				<length>10</length>
				<content><xsl:value-of select="//diagnosis[@id = $diags[4]]/icd9code" /></content>
			</element>
			</xsl:if>

			<element>
				<!-- Box 22: Medicare Resubmission Code -->
				<row>38</row>
				<column>50</column>
				<length>10</length>
				<content><xsl:value-of select="$procfirstobj/medicaidresubmissioncode" /></content>
			</element>

			<element>
				<!-- Box 22b: Medicare Original Reference -->
				<row>38</row>
				<column>62</column>
				<length>10</length>
				<content><xsl:value-of select="$procfirstobj/medicaidoriginalreference" /></content>
			</element>

			<element>
				<!-- Box 23: Prior Authorization -->
				<row>40</row>
				<column>50</column>
				<length>15</length>
				<content><xsl:value-of select="$procfirstobj/priorauth" /></content>
			</element>

			<!-- Loop through procedures -->

			<xsl:for-each select="$procs">
				<!-- <xsl:message>calling render service line with <xsl:value-of select="//procedure[@id = $procs[position()]]/cpt4code" /> ( <xsl:value-of select="." /> ) at <xsl:value-of select="position()" /></xsl:message> -->
				<xsl:variable name="pos" select="position()" />
				<xsl:call-template name="render-service-line">
					<xsl:with-param name="diags" select="$diags" />
					<xsl:with-param name="cptline" select="(($pos - 1) * 2) + 44" />
					<xsl:with-param name="curproc" select="//procedure[@id = $procs[$pos]]" />
					<xsl:with-param name="facilityobj" select="$facilityobj" />
					<xsl:with-param name="providerobj" select="$providerobj" />
				</xsl:call-template>
			</xsl:for-each>

			<element>
				<!-- Box 25: EIN -->
				<row>56</row>
				<column>1</column>
				<length>15</length>
				<content><xsl:value-of select="$facilityobj/ein"/></content>
			</element>

			<element>
				<!-- Box 25: EIN - Check block -->
				<row>56</row>
				<column>19</column>
				<length>1</length>
				<content>X</content>
			</element>

			<element>
				<!-- Box 26: Patient Account -->
				<row>56</row>
				<column>23</column>
				<length>15</length>
				<content><xsl:value-of select="$patientobj/account"/></content>
			</element>

			<xsl:if test="$insuredobj/isassigning = 1">
			<element>
				<!-- Box 27: Accept Assignment / Y -->
				<row>56</row>
				<column>38</column>
				<length>1</length>
				<content>X</content>
			</element>
			</xsl:if>

			<xsl:if test="not($insuredobj/isassigning = 1)">
			<element>
				<!-- Box 27: Accept Assignment / N -->
				<row>56</row>
				<column>43</column>
				<length>1</length>
				<content>X</content>
			</element>
			</xsl:if>

			<element periodstrippdf="1">
				<!-- Box 28: Total Charge -->
				<row>56</row>
				<column>52</column>
				<length>8</length>
				<content><xsl:value-of select="format-number(sum(exsl:node-set($procobjs/cptcharges)), '###0.00')" /></content>
				<format right="1" />
			</element>

			<element periodstrippdf="1">
				<!-- Box 29: Amount Paid -->
				<row>56</row>
				<column>63</column>
				<length>7</length>
				<content><xsl:value-of select="format-number(sum(exsl:node-set($procobjs/amountpaid)), '###0.00')" /></content>
				<format right="1" />
			</element>

			<element periodstrippdf="1">
				<!-- Box 30: Balance Due -->
				<row>56</row>
				<column>72</column>
				<length>7</length>
				<content><xsl:value-of select="format-number(sum(exsl:node-set($procobjs/cptcharges)) - sum(exsl:node-set($procobjs/amountpaid)), '###0.00')" /></content>
				<format right="1" />
			</element>

			<element>
				<!-- Box 32: Facility Name -->
				<row>58</row>
				<column>23</column>
				<length>25</length>
				<content><xsl:value-of select="translate($facilityobj/name, $lowercase, $uppercase)" /></content>
			</element>

			<element>
				<!-- Box 33: Physician Contact Name -->
				<row>58</row>
				<column>50</column>
				<length>25</length>
		                <content><xsl:value-of select="translate($practiceobj/name, $lowercase, $uppercase)" /></content>
				<!-- <content><xsl:value-of select="translate($facilityobj/description, $lowercase, $uppercase)" /></content> -->
		        </element>

			<element>
				<!-- Box 32: Facility Rendering Facility Address -->
				<row>59</row>
				<column>23</column>
				<length>27</length>
				<content><xsl:value-of select="translate($facilityobj/address/streetaddress, $lowercase, $uppercase)" /></content>
			</element>

			<element>
				<!-- Box 33: Physician Contact Address -->
				<row>59</row>
				<column>50</column>
				<length>23</length>
				<content><xsl:value-of select="translate($practiceobj/address/streetaddress, $lowercase, $uppercase)" /></content>
			</element>

			<element>
				<!-- Box 32: Facility City State Zip -->
				<row>60</row>
				<column>23</column>
				<length>27</length>
				<content><xsl:value-of select="translate(concat($facilityobj/address/city,', ',$facilityobj/address/state, ' ', $facilityobj/address/zipcode), $lowercase, $uppercase)" /></content>
			</element>

			<element>
				<!-- Box 33: Physician Contact -->
				<row>60</row>
				<column>50</column>
				<length>30</length>
				<content><xsl:value-of select="translate(concat($practiceobj/address/city, ', ', $practiceobj/address/state, ' ', $practiceobj/address/zipcode), $lowercase, $uppercase)" /></content>
			</element>

			<xsl:comment>
			<xsl:if test="boolean(string($practiceobj/phone/area))">
			<!-- skip the phone number if not present -->
			<element>
				<!-- Box 33: Physician Phone -->
				<row>60</row>
				<column>67</column>
				<length>14</length>
				<content><xsl:value-of select="concat($practiceobj/phone/area, $practiceobj/phone/number)" /></content>
			</element>
			</xsl:if>
			</xsl:comment>

			<element>
				<!-- Box 31: Authorized Signature -->
				<row>60</row>
				<column>1</column>
				<length>17</length>
				<content>SIGNATURE ON FILE</content>
			</element>

			<element>
				<!-- Box 31: Signature and Date -->
				<row>61</row>
				<column>5</column>
				<length>8</length>
				<content><xsl:value-of select="concat(//global/currentdate/month, '-', //global/currentdate/day, '-', substring(//global/currentdate/year, 3, 2))" /></content>
			</element>

			<element>
				<!-- Box 32: Group Rendering NPI # -->
				<row>61</row>
				<column>24</column>
				<length>10</length>
				<content><xsl:value-of select="$facilityobj/npi" /></content>
			</element>

			<element>
				<!-- Box 32b: Other Numbers -->
				<row>61</row>
				<column>36</column>
				<length>10</length>
				<content><xsl:value-of select="$facilityobj/taxonomy" /></content>
			</element>

			<element>
				<!-- Box 33: Provider Billing NPI # -->
				<row>61</row>
				<column>52</column>
				<length>12</length>
				<content><xsl:value-of select="$providerobj/npi" /></content>
			</element>

			<element>
				<!-- Box 33: Group # -->
				<row>61</row>
				<column>67</column>
				<length>12</length>
				<content><xsl:value-of select="//practice[@id = $procfirstobj/practicekey]/groupid[@physician = $provider and @payer=$payer]" /></content>
			</element>

		<!-- BOOKMARK BOOKMARK BOOKMARK -->

		</page>
	</xsl:template>

	<xsl:template name="render-service-line">
		<xsl:param name="diags" />
		<xsl:param name="cptline" />
		<xsl:param name="curproc" />
		<xsl:param name="facilityobj" />
		<xsl:param name="providerobj" />
				
		<element>
			<!-- Box 24a: Date of Service S / MM -->
			<row><xsl:value-of select="$cptline" /></row>
			<column>1</column>
			<length>2</length>
			<content><xsl:value-of select="$curproc/dateofservicestart/month" /></content>
		</element>

		<element>
			<!-- Box 24a: Date of Service S / DD -->
			<row><xsl:value-of select="$cptline" /></row>
			<column>4</column>
			<length>2</length>
			<content><xsl:value-of select="$curproc/dateofservicestart/day" /></content>
		</element>

		<element>
			<!-- Box 24a: Date of Service S / YY -->
			<row><xsl:value-of select="$cptline" /></row>
			<column>7</column>
			<length>2</length>
			<content><xsl:value-of select="substring($curproc/dateofservicestart/year, 3, 2)" /></content>
		</element>

		<element>
			<!-- Box 24a: Date of Service E / MM -->
			<row><xsl:value-of select="$cptline" /></row>
			<column>10</column>
			<length>2</length>
			<content><xsl:value-of select="$curproc/dateofserviceend/month" /></content>
		</element>

		<element>
			<!-- Box 24a: Date of Service E / DD -->
			<row><xsl:value-of select="$cptline" /></row>
			<column>13</column>
			<length>2</length>
			<content><xsl:value-of select="$curproc/dateofserviceend/day" /></content>
		</element>

		<element>
			<!-- Box 24a: Date of Service E / YY -->
			<row><xsl:value-of select="$cptline" /></row>
			<column>16</column>
			<length>2</length>
			<content><xsl:value-of select="substring($curproc/dateofserviceend/year, 3, 2)" /></content>
		</element>

		<element>
			<!-- Box 24b: Place of Service -->
			<row><xsl:value-of select="$cptline" /></row>
			<column>19</column>
			<length>2</length>
			<content><xsl:value-of select="$facilityobj/hcfacode" /></content>
		</element>

		<element>
			<!-- Box 24c: Type of Service --> 
			<row><xsl:value-of select="$cptline" /></row>
			<column>22</column>
			<length>2</length>
			<content><xsl:value-of select="$curproc/typeofservice" /></content>
		</element>

		<element>
			<!-- Box 24d: Procedures --> 
			<row><xsl:value-of select="$cptline" /></row>
			<column>25</column>
			<length>7</length>
			<content><xsl:value-of select="$curproc/cpt4code" /></content>
		</element>

		<element>
			<!-- Box 24d: Procedure / Modifier --> 
			<row><xsl:value-of select="$cptline" /></row>
			<column>33</column>
			<length>6</length>
			<content><xsl:value-of select="$curproc/cptmodifier" /></content>
		</element>

		<element>
			<!-- Box 24e: Diagnosis References --> 
			<row><xsl:value-of select="$cptline" /></row>
			<column>42</column>
			<length>7</length>
			<content><xsl:call-template name="lookup-diagnoses">
				<xsl:with-param name="diags" select="set:distinct(exsl:node-set($curproc/diagnosiskey))" />	
				<xsl:with-param name="set" select="$diags" />	
			</xsl:call-template></content>
		</element>

		<element periodstrippdf="1">
			<!-- Box 24f: Charges -->
			<row><xsl:value-of select="$cptline" /></row>
			<column>51</column>
			<length>7</length>
			<content><xsl:value-of select="format-number($curproc/cptcharges, '#0.00')" /></content>
			<format right="1" />
		</element>

		<element>
			<!-- Box 24g: Units of Service -->
			<row><xsl:value-of select="$cptline" /></row>
			<column>59</column>
			<length>3</length>
			<content><xsl:value-of select="($curproc/cptunits)+0" /></content>
		</element>

		<xsl:if test="$curproc/cptepsdt = 1">
		<element>
			<!-- Box 24h: EPSDT -->
			<row><xsl:value-of select="$cptline" /></row>
			<column>62</column>
			<length>1</length>
			<content>X</content>
		</element>
		</xsl:if>

		<xsl:choose>
			<xsl:when test="boolean(string($curproc/hcfalocaluse24k))">
				<element>
					<!-- Box 24i: Non-NPI Qualifier -->
					<row><xsl:value-of select="$cptline" /></row>
					<column>66</column>
					<length>2</length>
					<content>1C</content>
				</element>

				<element>
					<!-- Box 24j: Rendering Provider Non-NPI -->
					<row><xsl:value-of select="$cptline" /></row>
					<column>69</column>
					<length>10</length>
					<content><xsl:value-of select="$curproc/hcfalocaluse24k" /></content>
				</element>
			</xsl:when>
			<xsl:otherwise>
				<element>
					<!-- Box 24j: Rendering Provider NPI -->
					<row><xsl:value-of select="$cptline" /></row>
					<column>69</column>
					<length>10</length>
					<content><xsl:value-of select="$providerobj/npi" /></content>
				</element>
			</xsl:otherwise>
		</xsl:choose>

	</xsl:template>

	<!-- UTILITY TEMPLATES *********************************** -->

	<!--
		lookup-diagnoses

		Get a list of comma-delimited diagnoses reference numbers,
		as used by the HCFA-1500/CMS-1500 form.

		Parameters:
			diags - Node set of diagnosis keys
			set - Full set of diagnosis keys to index against

		Returns:
			String representation of comma seperated references.
	-->
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
			translate again to use ',' as spacers. Due to it
			not putting enough space in, I inserted a '-'
			character, which is removed in this step. Originally
			used *this* trick for the 837P template. -->
		<!-- <xsl:message>fount <xsl:value-of select="translate(normalize-space(translate($resultset, '-', '')), ' ', ',')" /> for diags</xsl:message> -->
		<xsl:value-of select="translate(normalize-space(translate($resultset, '-', '')), ' ', ',')" />
	</xsl:template>

	<!--
		lookup-diagnosis

		Lookup a single diagnosis reference number

		Parameters:
			diag - Diagnosis key
			set - Full set of diagnoses to reference against

		Returns:
			Reference number for specified diagnosis key
	-->
	<xsl:template name="lookup-diagnosis">
		<xsl:param name="diag" />
		<xsl:param name="set" />
		<xsl:for-each select="$set">
			<xsl:if test="current()=$diag"><xsl:value-of select="position()" /></xsl:if>
		</xsl:for-each>
	</xsl:template>

	<!--
		sequence-location

		Parameters:
			id - ID tag to search for
			nodes - Nodes to search for $id in

		Returns:
			Numeric location of id in sequence
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

	<!--
		procs-get-largest-subset

		Parameters: 
			$procs - Set of //procedure trees

		Returns: 
			Index of largest possible working subset
	-->
	<xsl:template name="procs-get-largest-subset">
		<xsl:param name="procs" />

		<xsl:variable name="pcount" select="count($procs)" />

		<xsl:choose>
			<xsl:when test="not(count(set:distinct(exsl:node-set($procs/diagnosiskey))) &gt; 4 or $pcount &gt; 6)">
				<!-- If procs fit, send back count -->
				<xsl:value-of select="$pcount" />
			</xsl:when>
			<xsl:otherwise>
				<!-- Try with 1 ... (n-1) -->
				<xsl:call-template name="procs-get-largest-subset">
					<xsl:with-param name="procs" select="$procs[position() &lt; $pcount]" />
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
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

</xsl:stylesheet>
