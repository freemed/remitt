<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:exsl="http://exslt.org/common"
	extension-element-prefixes="exsl" xmlns:set="http://exslt.org/sets"
	exclude-result-prefixes="set" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:output method="xml" />

	<!--
		NOTES: When dealing with templates, do not use xsl:value-of embedded
		in the xsl:with-param blocks ... it does not actually pass tree
		fragments, and passes text instead, causing wildly unpredictable
		results.
	-->

	<!--
		Define keys to use
	-->

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
		<patient id="{$patient}">
			<xsl:for-each select="//provider">
				<xsl:variable name="prid" select="@id" />
				<xsl:if
					test="count(//procedure[patientkey = $patient and providerkey=$prid]) &gt; 0">
					<!--
						<debug><xsl:text>pat,prov|</xsl:text> <xsl:value-of
						select="$patient" /> <xsl:text>|</xsl:text> <xsl:value-of
						select="@id" /> </debug>
					-->
					<xsl:call-template name="process-provider">
						<xsl:with-param name="patient">
							<xsl:value-of select="$patient" />
						</xsl:with-param>
						<xsl:with-param name="provider">
							<xsl:value-of select="$prid" />
						</xsl:with-param>
					</xsl:call-template>
				</xsl:if>
			</xsl:for-each>
		</patient>
		--
	</xsl:template>

	<xsl:template name="process-provider">
		<xsl:param name="patient" />
		<xsl:param name="provider" />

		<xsl:for-each select="//payer">
			<xsl:variable name="payid" select="@id" />
			<xsl:if
				test="count(//procedure[patientkey = $patient and providerkey=$provider and payerkey=$payid]) &gt; 0">
				<!--
					<debug><xsl:text>patient key = </xsl:text> <xsl:value-of
					select="$patient"/> <xsl:text>, provider key = </xsl:text>
					<xsl:value-of select="$provider"/> <xsl:text>, payer key =
					</xsl:text> <xsl:value-of select="$payid"/> </debug>
				-->
				<xsl:call-template name="process-payer">
					<xsl:with-param name="patient">
						<xsl:value-of select="$patient" />
					</xsl:with-param>
					<xsl:with-param name="provider">
						<xsl:value-of select="$provider" />
					</xsl:with-param>
					<xsl:with-param name="payer">
						<xsl:value-of select="$payid" />
					</xsl:with-param>
				</xsl:call-template>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>

	<xsl:template name="process-payer">
		<xsl:param name="patient" />
		<xsl:param name="provider" />
		<xsl:param name="payer" />
		<xsl:variable name="set"
			select="//procedure[patientkey = $patient and providerkey = $provider and payerkey = $payer]" />
		<debug>
			<xsl:value-of select="count($set)" />
			<xsl:text> procedure(s) found</xsl:text>
		</debug>
		<!--
			Now we use the craptastic method of producing a single form for a
			single procedure. What a crock.
		-->
		<!--
			<xsl:for-each select="$set"> <debug><xsl:text>in loop|</xsl:text>
			<xsl:value-of select="$patient"/> <xsl:text>|</xsl:text>
			<xsl:value-of select="$provider"/> <xsl:text>|</xsl:text>
			<xsl:value-of select="$payer"/> <xsl:text>|</xsl:text> <xsl:value-of
			select="@id"/> </debug>
		-->
		<xsl:if test="count($set) &gt; 0">
			<xsl:call-template name="process-procedure-set">
				<xsl:with-param name="set" select="$set" />
			</xsl:call-template>
		</xsl:if>
	</xsl:template>

	<!--
		process-procedure-set Performs procedure transformation for a form
		from a set of procedures and recursively generates forms. Parameters:
		set - set of procedures Returns: Set of procedures remaining, if there
		are any.
	-->
	<xsl:template name="process-procedure-set">
		<xsl:param name="set" />

		<!-- get original diagnosis set -->
		<xsl:variable name="diagnosisset"
			select="set:distinct(exsl:node-set($set/diagnosis))" />
		<xsl:value-of select="$diagnosisset" />

		# of diags:
		<xsl:value-of select="count($diagnosisset)" />
		raw diags (distinct):
		<xsl:value-of select="$diagnosisset" />

		<!-- get a valid subset by diagnoses -->
		<!-- <xsl:variable name="subset"> -->
		<xsl:call-template name="get-valid-diagnosis-set">
			<xsl:with-param name="procedureset" select="$set" />
		</xsl:call-template>
		<!-- </xsl:variable> -->
		<xsl:variable name="subset" select="$set" />

		<!--
			<debug><xsl:text>raw subset: </xsl:text><xsl:value-of
			select="$subset"/></debug> <debug><xsl:text>subset:
			</xsl:text><xsl:value-of select="set:intersection(/*/procedure,
			$subset)"/></debug>
		-->

		<!-- call template generate form -->
		<xsl:call-template name="generate-form">
			<xsl:with-param name="procedures" select="$subset" />
		</xsl:call-template>

		<!-- get valid remaining subset -->
		<xsl:variable name="remaining"
			select="exsl:node-set(set:difference($set, $subset))" />
		<debug>
			<xsl:text>count remaining: </xsl:text>
			<xsl:value-of select="count(set:difference($set, $subset))" />
		</debug>

		<!-- if we have remaining items, time to recurse -->
		<!--
			<xsl:variable name="left" value="count($remaining) + 0" /> <xsl:if
			test="$left &gt; 0"> <debug><xsl:text>recursing (left &gt;
			0)</xsl:text></debug> <xsl:call-template
			name="process-procedure-set"> <xsl:with-param name="set"
			select="$remaining" /> </xsl:call-template> </xsl:if>
		-->
	</xsl:template>

	<!--
		get-valid-diagnosis-set Parameters: procedureset - Set of procedures

		Returns: Set of procedures fitting in the proper number of diagnosis
		slots.
	-->
	<xsl:template name="get-valid-diagnosis-set">
		<xsl:param name="procedureset" />
		<xsl:variable name="diagnosisset"
			select="set:distinct(exsl:node-set($procedureset/diagnosiskey))" />
		<debug>
			<xsl:text>debug diagset</xsl:text>
			<xsl:copy-of select="$diagnosisset" />
		</debug>

		<xsl:choose>
			<xsl:when test="count($diagnosisset) &gt; 4">

				<!-- have to return set - last element -->
				<element>found more than 4 diagnoses in this set</element>
				<!-- reduce set by sending back set - last into recursion -->
				<xsl:call-template name="get-valid-diagnosis-set">
					<xsl:with-param name="procedureset"
						select="$procedureset[position() &lt;= count(exsl:node-set($procedureset))]" />
				</xsl:call-template>
				<!-- <xsl:copy-of select="$procedureset[position() &gt; 0]"/> -->
			</xsl:when>
			<xsl:otherwise>

				<!-- return proper set -->
				<xsl:copy-of select="$procedureset" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="generate-form">
		<xsl:param name="procedures" />
		<debug>generating form</debug>

		<xsl:choose>
			<xsl:when test="exsl:node-set($procedures)[4]">
				More than 4 procedures
			</xsl:when>
			<xsl:otherwise>
				Less than 4 procedures
			</xsl:otherwise>
		</xsl:choose>

		<!--
			<xsl:variable name="procedure" select="//procedure[@id =
			$procedures]" /> <debugprocedure><xsl:value-of
			select="$procedure/cpt4code"/></debugprocedure>
			<mydebug><xsl:value-of select="//payer[@id = //procedure[@id =
			$procedures]/payerkey]/name" /></mydebug>
		-->
	</xsl:template>

	<!--

		"render" format is: render ( page, format, pagelength ), element+ (
		row, column, length, content, comment )
	-->

	<!--
		EXSLT functions Please note that only function which are not included
		in the libxslt library are included here, since that is the XSLT
		compiler we are using. EXSLT functions for anything else can be found
		at http://exslt.org/.
	-->

	<exsl:function name="set:distinct">
		<xsl:param name="nodes" select="/.." />
		<xsl:choose>
			<xsl:when test="not($nodes)">
				<exsl:result select="/.." />
			</xsl:when>
			<xsl:otherwise>
				<xsl:variable name="distinct"
					select="set:distinct($nodes[position() &gt; 1])" />
				<exsl:result select="$distinct | $nodes[1][. != $distinct]" />
			</xsl:otherwise>
		</xsl:choose>
	</exsl:function>

</xsl:stylesheet>

