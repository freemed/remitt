<?xml version="1.0"?>
<!--
	$Id$
	$Author$

	Description: HCFA-1500/CMS-1500
	OutputFormat: fixedformxml
	Media: Paper

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
        <fixedform>
		<x12format>
			<delimiter>*</delimiter>
			<endofline>~</endofline>
		</x12format>

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
		<xsl:variable name="procs" select="set:distinct(exsl:node-set(//procedure[practicekey=$practice and providerkey=$provider and payerkey=$payer and facilitykey=$facility and patientkey=$patient and insuredkey = $insured]/@id))" />
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
					FIXME FIXME
					--
					FIXME FIXME
				</xsl:when>
				<xsl:otherwise>
					<xsl:call-template name="render-form">
						<xsl:with-param name="procs" select="$procs" />
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

		<!-- Since everything else is the same, get info from first. -->
		<xsl:variable name="insured" select="//procedure[@id = $procs[1]]/insuredkey" />
		<xsl:variable name="insuredobj" select="//insured[@id = $insured]" />
		<xsl:variable name="patient" select="//procedure[@id = $procs[1]]/patientkey" />
		<xsl:variable name="patientobj" select="//patient[@id = $patient]" />
		<xsl:variable name="payer" select="//procedure[@id = $procs[1]]/payerkey" />
		<xsl:variable name="payerobj" select="//payer[@id = $payer]" />

		<!-- Primary diagnosis object -->
		<xsl:variable name="diag" select="//procedure[@id = $procs[1]]/diagnosiskey" />
		<xsl:variable name="diagobj" select="//diagnosis[@id = $diag]" />

		<page>
			<format>
				<pagelength>60</pagelength>
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
				<!-- Insurance Company Street City -->
				<row>4</row>
				<column>41</column>
				<length>20</length>
				<content><xsl:value-of select="translate($payerobj/address/city, $lowercase, $uppercase)" /></content>
			</element>
	
			<element>
				<!-- Insurance Company Street State -->
				<row>4</row>
				<column>65</column>
				<length>2</length>
				<content><xsl:value-of select="translate($payerobj/address/state, $lowercase, $uppercase)" /></content>
			</element>

			<!-- FIXME: Is Medicare/Medicaid/Champus, etc -->

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
				<length>8</length>
				<content><xsl:value-of select="translate($patientobj/name/first, $lowercase, $uppercase)" /></content>
			</element>

			<element>
				<!-- Box 2: Patient Name / Middle -->
				<row>10</row>
				<column>28</column>
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
				<column>63</column>
				<length>14</length>
				<content><xsl:value-of select="concat(' ',$insuredobj/phone/area, ' ', $insuredobj/phone/number)" /></content>
			</element>

			<element>
				<!-- Box 10: Condition Related To -->
				<row>18</row>
				<column>31</column>
				<length>10</length>
				<!-- diagnosis/relatedtohcfa -->
				<content>FIXME</content>
			</element>

			<element>
				<!-- Box 11: Group Number -->
				<row>18</row>
				<column>50</column>
				<length>30</length>
				<content><xsl:value-of select="$insuredobj/groupnumber" /></content>
			</element>

		<xsl:variable name="secondary" select="//procedure[@id = $procs[1]]/otherinsuredkey" />
		<xsl:variable name="secondaryobj" select="//insured[@id = $secondary]" />

			<!-- FIXME: Box 9a Secondary Insurance / Name -->

			<xsl:if test="$diagobj/isrelatedtoemployment = 1">
			<element>
				<!-- Box 10a: Related to Employement / Yes -->
				<row>20</row>
				<column>35</column>
				<length>1</length>
				<content>X</content>
			</element>
			</xsl:if>

			<xsl:if test="not($diagobj/isrelatedtoemployment = 1)">
			<element>
				<!-- Box 10a: Related to Employement / No -->
				<row>20</row>
				<column>41</column>
				<length>1</length>
				<content>X</content>
			</element>
			</xsl:if>

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

			<xsl:if test="$insuredobj/isemployed = 1">
			<element>
				<!-- Box XX: Employer Name of Insured -->
				<row>22</row>
				<column>50</column>
				<length>30</length>
				<content><xsl:value-of select="$insuredobj/employername" /></content>
			</element>
			</xsl:if>

		</page>
	</xsl:template>

		<!-- BOOKMARK BOOKMARK BOOKMARK -->

	<!-- UTILITY TEMPLATES *********************************** -->

			<!-- Diagnosis code references
			<content><xsl:call-template name="lookup-diagnoses">
				<xsl:with-param name="diags" select="$procobj/diagnosiskey" />
				<xsl:with-param name="set" select="$diags" />
			</xsl:call-template></content>
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
		<xsl:value-of select="translate(normalize-space(translate($resultset, '-', '')), ' ', ',')" />
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

