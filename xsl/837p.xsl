<?xml version="1.0"?>
<!--
	$Id$
	$Author$

	Description: X12 NSF 837 Professional
	TranslationPlugin: X12XML

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

	<!--
		Master template for document root

		This template processes the document root, and calls all
		child templates.
	-->

	<xsl:template match="/remitt">
        <render>
		<x12format>
			<delimiter>*</delimiter>
			<endofline>~</endofline>
		</x12format>

		<!-- Generate static segments -->

		<x12segment sid="ISA">
			<comment>ISA Interchange Control Header #0</comment>
			<element>
				<content>00</content>
			</element>
			<element>
				<comment>10 spaces</comment>
				<content text="          " />
			</element>
			<element>
				<content>00</content>
			</element>
			<element>
				<comment>10 spaces</comment>
				<content text="          " />
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
				<content text="48M            "/>
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
				<content>1</content>
			</element>
			<element>
				<content>T</content>
			</element>
			<element>
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
				<content>0101</content>
			</element>
			<element>
				<content>1</content>
			</element>
			<element>
				<content>X</content>
			</element>
			<element>
				<comment>Transmission type code (is this right?)</comment>
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
				<!-- FIXME: Creation time (HHMM) -->
				<content><xsl:value-of select="'0810'" /></content>
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
			<element>
				<content>EX</content>
			</element>
			<element>
				<content><xsl:value-of select="//billingcontact/phone/extension" /></content>
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
				<hlcounter/>
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

		<xsl:variable name="practices" select="//practice" />

		<xsl:for-each select="$practices">
			<xsl:call-template name="process-practice">
				<xsl:with-param name="practice">
					<xsl:value-of select="@id"/>
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
				<!-- EIN -->
				<content>24</content>
			</element>
			<element>
				<!-- Supposed to be EIN number FIXME FIXME -->
				<content><xsl:value-of select="$thispractice/tin" /></content>
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


		<!--
			Need to get distinct patient/insured pairs.
			Get patients from this practice, then pass
			to process-insured .... 2000B subscriber loop
		-->
		<xsl:variable name="patients" select="set:distinct(exsl:node-set(//procedure[practicekey=$practice]/patientkey))" />
		<xsl:for-each select="$patients">
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
		<!-- Extract payer by insured -->
		<xsl:variable name="payer" select="//procedure[insuredkey=$insured]/payerkey" />
		<xsl:variable name="payerobj" select="//payer[@id=$payer[0]]" />

		<!-- determine HLpatient status 0 or 1 
			if relationship = S, status = 0 -->
		<xsl:variable name="hlpatient">
			<xsl:choose>
				<xsl:when test="translate($insuredobj/relationship, $lowercase, $uppercase) = 'S'">0</xsl:when>
				<xsl:otherwise>1</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<xsl:comment>2000B Loop with $patient and $insured in $practice</xsl:comment>
		
		<x12segment sid="HL">
			<comment>HL - Subscriber Heirarchical Level 2000B (p109)</comment>
			<element>
				<comment>Call the counter to incremement</comment>
				<hlcounter/>
			</element>
			<element>
				<comment>FIXME: same as last segment</comment>
				<content>1</content>
			</element>
			<element>
				<content>22</content>
			</element>
			<element>
				<comment>0 = subscriber level is patient level, 1 = extra patient level</comment>
				<content><xsl:value-of select="$hlpatient" /></content>
			</element>
		</x12segment>

		<!-- Loop 2000B: SBR Subscriber Information (p109) -->
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
				<!-- FIXME: SBR05 special instance -->
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
				<content><xsl:value-of select="translate($insuredobj/name/last, $lowercase, $uppercase)" /></content>
			</element>
			<element>
				<content><xsl:value-of select="translate($insuredobj/name/first, $lowercase, $uppercase)" /></content>
			</element>
			<element>
				<content><xsl:value-of select="translate($insuredobj/name/middle, $lowercase, $uppercase)" /></content>
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
				<!-- Identification code qualifier -->
				<content>M</content>
			</element>
			<element>
				<!-- Identification code -->
				<content><xsl:value-of select="$insuredobj/id" /></content>
			</element>
			<element>
				<content></content>
			</element>
			<element>
				<content></content>
			</element>
		</x12segment>

		<!-- 2010BA: Subscriber Address (p121) -->
		<x12segment sid="N3">
			<element>
				<content><xsl:value-of select="translate($insuredobj/address/streetaddress, $lowercase, $uppercase)" /></content>
			</element>
			<element>
				<content></content>
			</element>
		</x12segment>

		<!-- (p122) -->
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

		<!-- Loop through payer (2010BB??) -->

		<xsl:variable name="payers" select="set:distinct(//procedure[patientkey=$patient and insuredkey=$insured and practicekey=$practice]/payerkey)" />
		<xsl:for-each select="$payers">
			<xsl:call-template name="process-payer">
				<xsl:with-param name="patient" select="$patient" />
				<xsl:with-param name="practice" select="$practice" />
				<xsl:with-param name="insured" select="$insured" />
				<xsl:with-param name="payer" select="." />
			</xsl:call-template>
		</xsl:for-each>
	</xsl:template>

	<!--
		process-payer

		Process payer loop (2010BB??) with passed patient, practice,
		insured, and payer.
	-->
	<xsl:template name="process-payer">
		<xsl:param name="patient" />
		<xsl:param name="practice" />
		<xsl:param name="insured" />
		<xsl:param name="payer" />

			<!-- BOOKMARK BOOKMARK -->

		<xsl:variable name="patientobj" select="//patient[@id=$patient]" />
		<xsl:variable name="payerobj" select="//payer[@id=$payer]" />

		<xsl:choose>
		<xsl:when test="string($insured)">NON NULL STRING?</xsl:when>
		<xsl:otherwise>NULL INSURED</xsl:otherwise>
		</xsl:choose>
		-

		<!-- 2000C HL Heirarchical level (p152) -->

		<x12segment sid="HL">
			<element>
				<hlcounter />
			</element>
			<element>
				<content>FIXME</content>
			</element>
		</x12segment>

		<x12segment sid="PAT">
			<comment>NM1 - Subscriber Name (p108)</comment>
			<element>
				<comment>Entity Identifier Code</comment>
				<content>IL</content>
			</element>
			<element>
				<comment>Entity Type Qualifier</comment>
				<content>1</content>
			</element>
			<element>
				<comment>Name, Last</comment>
				<content>
					<xsl:choose>
					<xsl:when test="not(string(//insured[@id=$insured]/name/last))"><xsl:value-of select="translate($patientobj/name/last, $lowercase, $uppercase)" /></xsl:when>
					<xsl:otherwise><xsl:value-of select="//insured[@id=$insured]/name/last" /></xsl:otherwise>
				</xsl:choose></content>
			</element>
			<element>
				<comment>Name, First</comment>
				<content><xsl:choose>
					<xsl:when test="not(string(//insured[@id=$insured]/name/first))"><xsl:value-of select="translate($patientobj/name/first, $lowercase, $uppercase)" /></xsl:when>
					<xsl:otherwise><xsl:value-of select="//insured[@id=$insured]/name/first" /></xsl:otherwise>
				</xsl:choose></content>
			</element>
			<element>
				<comment>Name, Middle</comment>
				<content>
					<xsl:choose>
					<xsl:when test="not(string(//insured[@id=$insured]/name/middle))"><xsl:value-of select="translate($patientobj/name/middle, $lowercase, $uppercase)" /></xsl:when>
					<xsl:otherwise><xsl:value-of select="//insured[@id=$insured]/name/middle" /></xsl:otherwise>
				</xsl:choose></content>
			</element>
			<element>
				<comment>Name Prefix</comment>
				<content></content>
			</element>
			<element>
				<comment>NM108 - Identification Code Qualifier</comment>
				<content>MI</content>
			</element>
			<element>
				<comment>NM109 - Identification Code</comment>
				<!-- FIXME: needs to be pulled from "insured" or other coverage -->
				<content></content>
			</element>
			<element>
				<comment>NM110 - Entity Relationship Code (not used)</comment>
				<content></content>
			</element>
			<element>
				<comment>NM111 - Entity Identifier Code (not used)</comment>
				<content></content>
			</element>
		</x12segment>

		<x12segment sid="N3">
			<comment>2010BA Subscriber Name / Address</comment>
			<element>
				<content><xsl:value-of select="translate($patientobj/address/streetaddress, $lowercase, $uppercase)" /></content>
			</element>
			<element>
				<content></content>
			</element>
		</x12segment>

		<x12segment sid="N4">
			<comment>2010BA Subscriber Name / CSZ</comment>
			<element>
				<content><xsl:value-of select="translate($patientobj/address/city, $lowercase, $uppercase)" /></content>
			</element>
			<element>
				<content><xsl:value-of select="//patient[@id=$patient]/address/state" /></content>
			</element>
			<element>
				<content><xsl:value-of select="//patient[@id=$patient]/address/zipcode" /></content>
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

		<!-- DMG Segment is the demographic segment. It should only
		     be used when the patient is the same as the insured!!! -->
		<xsl:if test="$insuredobj/relationship = 'S'">
		<x12segment sid="DMG">
			<element>
				<comment>DMG01 - Date Time Period Format Qualifier</comment>
				<content>D8</content>
			</element>
			<element>
				<xsl:variable name="dob" select="$patientobj/dateofbirth" />
				<content><xsl:value-of select="concat($dob/year, $dob/month, $dob/day)" /></content>
			</element>
			<element>
				<comment>DMG03 - Gender Code</comment>
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
			<element>
				<content></content>
			</element>
			<element>
				<content></content>
			</element>
		</x12segment>

		<!--- PAYER NAME: REQUIRED LOOP 2010BB (p130) -->

		<x12segment sid="NM1">
			<element>
				<!-- Describing payer entity -->
				<content>PR</content>
			</element>
			<element>
				<!-- Non-person entity -->
				<content>2</content>
			</element>
			<element>
				<content><xsl:value-of select="translate($payerobj/name, $lowercase, $uppercase)" /></content>
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
				<!-- Identification code qualifier -->
				<!-- PI - Payer identification -->
				<!-- XV - HCFA National plan ID -->
				<content>PI</content>
			</element>
			<element>
				<!-- Identification code (Payer ID) -->
				<content><xsl:value-of select="$payerobj/x12id" /></content>
			</element>
			<element>
				<content></content>
			</element>
			<element>
				<content></content>
			</element>
		</x12segment>

		<x12segment sid="N3">
			<element>
				<!-- Address line -->
				<content><xsl:value-of select="translate($payerobj/address/streetaddress, $lowercase, $uppercase)" /></content>
			</element>
			<element>
				<!-- Secondary address line, if it exists -->
				<content></content>
			</element>
		</x12segment>

		<x12segment sid="N4">
			<element>
				<content><xsl:value-of select="$payerobj/address/city" /></content>
			</element>
			<element>
				<content><xsl:value-of select="$payerobj/address/state" /></content>
			</element>
			<element>
				<content><xsl:value-of select="$payerobj/address/zipcode" /></content>
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

		<xsl:variable name="procs" select="//procedure[patientkey=$patient and practicekey=$practice and insuredkey=$insured and payerkey=$payer]" />

		<!-- 2300 Loop: Claim Information (p170) -->
		<x12segment sid="CLM">
			<element>
				<!-- CLM01 - Claim Identifier -->
				<!-- According to 837P documents, this should be the patient id -->Should be unique identifier for set of procedures -->
				<content><xsl:value-of select="$patient" /></content>
			</element>
			<element>
				<!-- Total charges for all these procedures -->
				<content><xsl:value-of select="sum($procs/cptcharges)" /></content>
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
				<content><xsl:value-of select="" />::1</content>
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
			<element>
				<!-- Health causes code -->
				<!-- FIXME p175 need xsl:choose for this -->
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
			<element>
				<!-- CLM18 - EOB Required (req field) -->
				<content>N</content>
			</element>
			<element>
				<content></content>
			</element>
		</x12segment>

		<!-- 2300 Loop: DTP (p180) -->
		<!-- FIXME: Needs to pull earliest date for procedures -->
		<xsl:if test="0 = 1">
		<x12segment sid="DTP">
			<element>
				<!-- Date/Time qualifier -->
				<content>938</content>
			</element>
			<element>
				<!-- Format qualifier -->
				<content>D8</content>
			</element>
			<element>
				<content></content>
			</element>
		</x12segment>
		</xsl:if>

		<!-- 2300 Loop: DTP Referral Date (p184) need if referral FIXME-->
		<!-- 2300 Loop: DTP Date Last Seen (p186) need for psych FIXME-->
		<!-- 2300 Loop: DTP Date of Onset (p188) eoc? FIXME-->
		<!-- 2300 Loop: DTP Date of Last Similar (p192) eoc? FIXME-->
		<!-- 2300 Loop: DTP Date of Accident (p194) eoc? FIXME-->
		<!-- 2300 Loop: DTP Date of Disable Start (p201) eoc? FIXME-->
		<!-- 2300 Loop: DTP Date of Disable End (p203) eoc? FIXME-->
		<!-- 2300 Loop: DTP Date of Admit (p208) eoc? FIXME-->
		<!-- 2300 Loop: DTP Date of Discharge (p210) eoc? FIXME-->

		<!-- 2300 Loop: AMT Patient Amount Paid (p220) FIXME-->

		<!-- 2300 Loop: REF Prior Auth or Referral (p227) FIXME -->

		<!-- 2300 Loop: REF Medical Record Num (p241) -->
		<x12segment sid="REF">
			<element>
				<content>EA</content>
			</element>
			<element>
				<content><xsl:value-of select="$patient" /></content>
			</element>
			<element>
				<content></content>
			</element>
			<element>
				<content></content>
			</element>
		</x12segment>

		<!-- 2300 Loop: HI Healthcare information codes (p266) -->
		<xsl:variable name="diag" select="//diagnosis[@id = $procs[0]/diagnosis[0]]"/>
		<x12segment sid="HI">
			<element>
				<!-- Actual ICD code -->
				<content>BK:<xsl:value-of select="$diag/icd9code" /></content>
			</element>
			<!-- FIXME: Support for more than one diagnosis code -->
			<element>
				<content/>
			</element>
		</x12segment>

		<!-- 2310A Loop: NM1 Referring Provider Name (p282) -->

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
				<content><xsl:value-of select="translate($facilityobj/name, $lowercase, $uppercase)" /></content>
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
				<!-- FIXME: Provider TIN -->
				<content/>
			</element>
		</x12segment>

		<!-- FREEB X12.XML ITEM #29 -->


		<!-- 2310E Loop: N3 Service Facility Address (p355) -->
		<!-- 2310E Loop: N4 Service Facility Address (p356) -->
		<!-- 2320 Loop: OI Other Coverage Info (p391) -->
		<!-- 2330A Loop: NM1 Other subscriber name (p401) sometimes -->
		<!-- 2330B Loop: NM1 Other payer name (p411) sometimes -->


		<!-- Loop through all claims that apply for this combination -->
		<xsl:for-each select="$procs">
			<xsl:call-template name="process-procedure">
				<xsl:with-param name="procedure" select="@id" />
				<xsl:with-param name="sequence" select="position()" />
			</xsl:call-template>
		</xsl:for-each>
	</xsl:template>

	<xsl:template name="process-procedure">
		<xsl:param name="procedure" />
		<xsl:param name="sequence" />
		
		<xsl:variable name="procobj" select="//procedure[@id = $procedure]" />

		<!-- 2400 Loop: LX Service Line Counter -->
		<x12segment sid="LX">
			<element>
				<content><xsl:value-of select="$sequence" /></content>
			</element>
		</x12segment>

		<!-- 2400 Loop: SV1 Professional Service (p446) -->
		<x12segment sid="SV1">
			<element>
				<content><xsl:value-of select="concat('HC:', $procobj/cpt4code)" /></content>
			</element>
			<element>
				<content><xsl:value-of select="$procobj/cptcharges" /></content>
			</element>
			<element>
				<content>UN</content>
			</element>
			<element>
				<content><xsl:value-of select="$procobj/cptunits" /></content>
			</element>
			<element>
				<content></content>
			</element>
			<element>
				<content></content>
			</element>
			<element>
				<content>1</content>
			</element>
		</x12segment>

		<!-- 2400 Loop: DTP Service Dates -->
		<x12segment sid="DTP">
			<element>
				<content>472</content>
			</element>
			<element>
				<content>D8</content>
			</element>
			<element>
				<content><xsl:value-of select="concat($procobj/dateofservicestart/year, $procobj/dateofservicestart/month, $procobj/dateofservicestart/day)" /></content>
			</element>
		</x12segment>
	</xsl:template>

	<!-- 
		EXSLT functions

		Please note that only function which are not included in
		the libxslt library are included here, since that is the
		XSLT compiler we are using. EXSLT functions for anything
		else can be found at http://exslt.org/.
	-->

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

</xsl:stylesheet>

