<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" />

	<!--
		Master template for document root This template processes the document
		root, and calls all child templates.
	-->

	<xsl:template match="/remitt">
		<render>
			<xsl:for-each select="patient">
				<xsl:call-template name="process-patient">
					<xsl:with-param name="patient">
						<xsl:value-of select="@id" />
					</xsl:with-param>
				</xsl:call-template>
			</xsl:for-each>
		</render>
	</xsl:template>

	<xsl:template name="process-patient">
		<xsl:param name="patient" />
		<patient>
			<id>
				<xsl:value-of select="$patient" />
			</id>
			<xsl:for-each select="/remitt/payer">
				<xsl:call-template name="process-payer">
					<xsl:with-param name="patient">
						<xsl:value-of select="$patient" />
					</xsl:with-param>
					<xsl:with-param name="payer">
						<xsl:value-of select="@id" />
					</xsl:with-param>
				</xsl:call-template>
			</xsl:for-each>
		</patient>
	</xsl:template>

	<xsl:template name="process-payer">
		<xsl:param name="patient" />
		<xsl:param name="payer" />
		<!-- check to see if we actually have payers matching criteria -->
		<xsl:variable name="procedure-count"
			select="count(/remitt/procedure[payerkey=$payer and patientkey=$patient])" />
		<xsl:if
			test="count(/remitt/procedure[payerkey=$payer and patientkey=$patient]) &gt; 0">
			<showid>
				<xsl:value-of select="$payer" />
			</showid>
			<showpat>
				<xsl:value-of select="$patient" />
			</showpat>
		</xsl:if>
	</xsl:template>

	<!--

		"render" format is: render page format pagelength element row column
		length content comment
	-->


</xsl:stylesheet>

