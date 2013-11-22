<?xml version="1.0"?>
<!--

  $Id$

  Authors:
 	Jeff Buchbinder <jeff@freemedsoftware.org>
 
  REMITT Electronic Medical Information Translation and Transmission
  Copyright (C) 1999-2014 FreeMED Software Foundation
 
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
		extension-element-prefixes="exsl date"
		xmlns:set="http://exslt.org/sets"
		exclude-result-prefixes="set"
		xmlns:date="http://exslt.org/dates-and-times"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:output method="xml" indent="yes" />

	<!-- Parameters -->
	<xsl:param name="currentTime" />
	<xsl:param name="jobId" />

<!--
		PROCESSING ARCHITECTURE:
/remitt
	process-patient
		split-into-forms
		render-form

-->

	<xsl:template match="/remitt">
	<fixedform>
	<xsl:variable name="patients" select="//patient" />
	<xsl:for-each select="$patients">
		<xsl:call-template name="process-patient">
			<xsl:with-param name="patient">
				<xsl:value-of select="@id" />
			</xsl:with-param>
		</xsl:call-template>
	</xsl:for-each>
	</fixedform>
	</xsl:template>

	<xsl:template name="process-patient">
		<xsl:param name="patient" />
		<xsl:variable name="procs" select="//procedure[patientkey=$patient]" />

		<!-- Handle more than X items -->
		<xsl:call-template name="render-form">
			<xsl:with-param name="patient" select="$patient" />
			<xsl:with-param name="procs" select="$procs" />
		</xsl:call-template>
	</xsl:template>

	<xsl:template name="render-form">
		<xsl:param name="patient" />
		<xsl:param name="procs" />

		<xsl:variable name="patobj" select="//patient[@id=$patient]" />

		<!-- Numeric totals -->
		<xsl:variable name="totalcharges" select="format-number(sum(exsl:node-set($procs/cptcharges)), '####0.00')" />
		<xsl:variable name="totalamountpaid" select="format-number(sum(exsl:node-set($procs/amountpaid)), '####0.00')" />
		<xsl:variable name="totalamountdue" select="format-number(($totalcharges - $totalamountpaid), '####0.00')" />

		<page>

		<format>
			<pagelength>66</pagelength>
			<pdf template="statement" page="1">
				<font name="Courier" size="10" />
				<scaling vertical="12" horizontal="7.1" />
				<offset vertical="16" horizontal="25" />
			</pdf>
		</format>

		<!-- Patient Name -->
		<element>
			<row>8</row>
			<column>8</column>
			<length>30</length>
			<content><xsl:value-of select="concat($patobj/name/first, ' ', $patobj/name/last)" /></content>
		</element>

		<!-- Patient Street Address -->
		<element>
			<row>9</row>
			<column>8</column>
			<length>30</length>
			<content><xsl:value-of select="$patobj/address/streetaddress" /></content>
		</element>

		<!-- Patient City, State Zip -->
		<element>
			<row>10</row>
			<column>8</column>
			<length>30</length>
			<content><xsl:value-of select="concat($patobj/address/city, ', ', $patobj/address/state, ' ', $patobj/address/zipcode)" /></content>
		</element>

		<!-- Patient Id -->
		<element>
			<row>10</row>
			<column>62</column>
			<length>10</length>
			<content><xsl:value-of select="$patobj/account" /></content>
		</element>

		<!-- Statement Date -->
		<element>
			<row>13</row>
			<column>62</column>
			<length>10</length>
			<content><xsl:value-of select="concat(//global/currentdate/year, '-', //global/currentdate/month, '-', //global/currentdate/day)" /></content>
		</element>

		<xsl:for-each select="$procs">
			<xsl:variable name="thisproc" select="." />
			<xsl:variable name="pos" select="position()" />
			<xsl:call-template name="render-proc">
				<xsl:with-param name="patobj" select="$patobj" />
				<xsl:with-param name="proc" select="$thisproc" />
				<xsl:with-param name="line" select="$pos" />
			</xsl:call-template>
		</xsl:for-each>

		<!-- Display total at the bottom -->
		<element>
			<row>53</row>
			<column>73</column>
			<length>7</length>
			<content><xsl:value-of select="$totalamountdue" /></content>
			<format right="1" />
		</element>

		</page>
	</xsl:template>

	<xsl:template name="render-proc">
		<xsl:param name="patobj" />
		<xsl:param name="proc" />
		<xsl:param name="line" />

		<xsl:variable name="offset" select="18" />
		<xsl:variable name="diag" select="//diagnosis[@id=$proc/diagnosiskey]" />

		<!-- Diagnosis Code -->
		<element>
			<row><xsl:value-of select="($line + $offset)" /></row>
			<column>2</column>
			<length>8</length>
			<content><xsl:value-of select="$diag/icd9code" /></content>
		</element>

		<!-- Date of Service -->
		<element>
			<row><xsl:value-of select="($line + $offset)" /></row>
			<column>12</column>
			<length>10</length>
			<content><xsl:value-of select="concat($proc/dateofservicestart/month, '/', $proc/dateofservicestart/day, '/', $proc/dateofservicestart/year)" /></content>
		</element>

		<!-- Procedure Code -->
		<element>
			<row><xsl:value-of select="($line + $offset)" /></row>
			<column>21</column>
			<length>10</length>
			<content><xsl:value-of select="$proc/cpt4code" /></content>
		</element>

		<!-- Description -->
		<element>
			<row><xsl:value-of select="($line + $offset)" /></row>
			<column>31</column>
			<length>28</length>
			<content><xsl:value-of select="$proc/cptdescription" /></content>
		</element>

		<!-- Charges -->
		<element>
			<row><xsl:value-of select="($line + $offset)" /></row>
			<column>59</column>
			<length>7</length>
			<content><xsl:value-of select="format-number($proc/cptcharges, '####0.00')" /></content>
			<format right="1" />
		</element>

		<!-- Amount Paid -->
		<element>
			<row><xsl:value-of select="($line + $offset)" /></row>
			<column>66</column>
			<length>7</length>
			<content><xsl:value-of select="format-number($proc/amountpaid, '####0.00')" /></content>
			<format right="1" />
		</element>

		<!-- Balance / Amount Due -->
		<element>
			<row><xsl:value-of select="($line + $offset)" /></row>
			<column>73</column>
			<length>7</length>
			<content><xsl:value-of select="format-number(($proc/cptcharges - $proc/amountpaid), '####0.00')" /></content>
			<format right="1" />
		</element>
	</xsl:template>

</xsl:stylesheet>
