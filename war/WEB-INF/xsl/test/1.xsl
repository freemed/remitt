<?xml version="1.0"?>
<xsl:stylesheet version="1.0" 
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
        <xsl:output method="xml" />

	<xsl:template match="remitt">
        <render>
		<xsl:for-each select="patient">
			<xsl:call-template name="process-patient">
				<xsl:with-param name="patient">
					<xsl:value-of select="@id"/>
				</xsl:with-param>	
			</xsl:call-template>
		</xsl:for-each>
        </render>
	</xsl:template>

	<xsl:template name="process-patient">
		<xsl:param name="patient" />
		<patient>
		<id><xsl:value-of select="$patient" /></id>
		<xsl:for-each select="/remitt/procedure[patientkey=$patient]">
			<procedure>
			<xsl:value-of select="@id" />
			</procedure>
		</xsl:for-each>
		</patient>
	</xsl:template>

	<!--

		"render" format is:

		render
			page
				format
					pagelength
				element
					row
					column
					length
					content
					comment
	-->


</xsl:stylesheet>

