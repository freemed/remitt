<?xml version="1.0"?>
<xsl:stylesheet version="1.0" 
xmlns:exsl="http://exslt.org/functions"
xmlns:set="http://exslt.org/sets"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
        <xsl:output method="xml" />

	<xsl:template match="remitt">
        <render>
		<xsl:for-each select="patient">
			<xsl:call-template name="process">
				<xsl:with-param name="set">
					<xsl:value-of select="/remit/procedure[patientkey = @id]"/>
				</xsl:with-param>	
			</xsl:call-template>
		</xsl:for-each>
        </render>
	</xsl:template>

	<xsl:template name="process">
		<xsl:param name="set" />
		<patient>
		<id><xsl:value-of select="$set" /></id>
		<xsl:for-each select="$set">
			<procedure>
			<xsl:value-of select="set:distinct($set/diagnosiskey)" />
			</procedure>
		</xsl:for-each>
		</patient>
	</xsl:template>

	<!-- EXSLT functions 						-->

	<exsl:function name="set:distinct">
		<xsl:param name="nodes" select="/.."/>
		<xsl:choose>
		<xsl:when test="not($nodes)">
			<exsl:result select="/.."/>
		</xsl:when>
		<xsl:otherwise>
			<xsl:variable name="distinct" select="set:distinct($nodes[position() &gt; 1])"/>
			<exsl:result select="$distinct | $nodes[1][. != $distinct]"/>
		</xsl:otherwise>
		</xsl:choose>
	</exsl:function>

</xsl:stylesheet>

