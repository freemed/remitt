# $Id$
#
# Authors:
# 	Jeff Buchbinder <jeff@freemedsoftware.org>
#
# REMITT Electronic Medical Information Translation and Transmission
# Copyright (C) 1999-2013 FreeMED Software Foundation
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.

DROP TABLE IF EXISTS `tUser`;
CREATE TABLE `tUser` (
	  id				SERIAL
	, username			VARCHAR(50) NOT NULL UNIQUE KEY
	, passhash			CHAR(32) NOT NULL
	, contactemail			VARCHAR(150)
	, callbackserviceuri		VARCHAR(150) COMMENT 'RemittCallback service URL'
	, callbackservicewsdluri	VARCHAR(150) COMMENT 'WSDL for RemittCallback service'
	, callbackusername		VARCHAR(50)
	, callbackpassword		VARCHAR(50)
	, INDEX				( `username` )
);

INSERT INTO `tUser` ( id, username, passhash, callbackserviceuri, callbackservicewsdluri ) VALUES ( 1, 'Administrator', MD5('password'), 'http://localhost/freemed/services/RemittCallback.php', 'http://localhost/freemed/services/RemittCallback.php?wsdl' );

DROP TABLE IF EXISTS `tRole`;
CREATE TABLE `tRole` (
	  id		SERIAL
	, username	VARCHAR(50) NOT NULL
	, rolename	VARCHAR(50) NOT NULL

	# Enforce unique combinations and cascading deletions
	, CONSTRAINT UNIQUE KEY ( `username`, `rolename` )
	, FOREIGN KEY ( `username` ) REFERENCES tUser ( `username` ) ON DELETE CASCADE
);

INSERT INTO `tRole` VALUES ( NULL, 'Administrator', 'admin' );
INSERT INTO `tRole` VALUES ( NULL, 'Administrator', 'default' );

DROP TABLE IF EXISTS `tUserConfig`;
CREATE TABLE `tUserConfig` (
	  user		VARCHAR(50) NOT NULL
	, cNamespace	VARCHAR(150) NOT NULL
	, cOption	VARCHAR(50) NOT NULL
	, cValue	BLOB

	, FOREIGN KEY ( `user` ) REFERENCES tUser ( `username` ) ON DELETE CASCADE
);

INSERT INTO `tUserConfig` VALUES
	  ( 'Administrator', 'org.remitt.plugin.transport.SftpTransport', 'sftpHost', '' )
	, ( 'Administrator', 'org.remitt.plugin.transport.SftpTransport', 'sftpPath', '' )
	, ( 'Administrator', 'org.remitt.plugin.transport.SftpTransport', 'sftpPort', '22' )
	, ( 'Administrator', 'org.remitt.plugin.transport.SftpTransport', 'sftpUsername', '' )
	, ( 'Administrator', 'org.remitt.plugin.transport.SftpTransport', 'sftpPassword', '' )
	, ( 'Administrator', 'org.remitt.plugin.transport.ScriptedHttpTransport', 'username', 'user' )
	, ( 'Administrator', 'org.remitt.plugin.transport.ScriptedHttpTransport', 'password', 'pass' )
;

DROP PROCEDURE IF EXISTS p_UserConfigUpdate;

DELIMITER //
CREATE PROCEDURE p_UserConfigUpdate (
	  IN c_user VARCHAR(100)
	, IN c_namespace VARCHAR(100)
	, IN c_option VARCHAR(100)
	, IN c_value BLOB )
BEGIN
	DECLARE c INT UNSIGNED;

	SELECT COUNT(*) INTO c FROM tUserConfig a
		WHERE
		    a.user = c_user
		AND a.cNameSpace = c_namespace
		AND a.cOption = c_option
		AND a.cValue = c_value;

	IF c > 0 THEN
		# Update
		UPDATE tUserConfig SET cValue = c_value
			WHERE user = c_user
			  AND cNameSpace = c_namespace
			  AND cOption = c_option;
	ELSE
		# Insert
		INSERT INTO tUserConfig (
				  user
				, cNamespace
				, cOption
				, cValue
			) VALUES (
				  c_user
				, c_namespace
				, c_option
				, c_value
			);
	END IF;
END//
DELIMITER ;

DROP TABLE IF EXISTS `tPayload`;
CREATE TABLE `tPayload` (
	  id			SERIAL
	, insert_stamp		TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
	, user			VARCHAR(50) NOT NULL
	, payload		LONGBLOB
	, originalId		VARCHAR(100)
	, renderPlugin		VARCHAR(100) NOT NULL
	, renderOption		VARCHAR(100) NOT NULL
	, transportPlugin	VARCHAR(100) NOT NULL
	, transportOption	VARCHAR(100)
	, payloadState		ENUM ( 'valid', 'failed', 'completed' ) DEFAULT 'valid'

	, KEY			( `payloadState` )
	, FOREIGN KEY		( `user` ) REFERENCES tUser ( `username` ) ON DELETE CASCADE
);

DROP VIEW IF EXISTS vPayload;
CREATE VIEW vPayload AS SELECT id, insert_stamp, user, originalId, renderPlugin, renderOption, transportPlugin, transportOption, payloadState FROM tPayload;

##### Processor Tables #####

DROP TABLE IF EXISTS `tProcessor`;
CREATE TABLE `tProcessor` (
	  id		SERIAL
	, threadId	INT UNSIGNED NOT NULL DEFAULT 0
	, payloadId	BIGINT UNSIGNED NOT NULL
	, stage		ENUM ( 'validation', 'render', 'translation', 'transport' )
	, plugin	VARCHAR (100) NOT NULL
	, tsStart	TIMESTAMP NULL DEFAULT NULL
	, tsEnd		TIMESTAMP NULL DEFAULT NULL
	, pInput	LONGBLOB
	, pOutput	LONGBLOB

	, FOREIGN KEY ( `payloadId` ) REFERENCES tPayload ( `id` ) ON DELETE CASCADE
);

DROP PROCEDURE IF EXISTS p_GetStatus;

DELIMITER //
CREATE PROCEDURE p_GetStatus (
	  IN username VARCHAR( 100 )
	, IN jobId INT UNSIGNED
	)
BEGIN
	DECLARE s ENUM ( 'valid', 'failed', 'completed' );
	DECLARE c ENUM ( 'validation', 'render', 'translation', 'transport' );
	DECLARE x ENUM ( 'validation', 'render', 'translation', 'transport' );

	SELECT NULL INTO c;
	SELECT NULL INTO s;

	#--- Check for "completed" status
	SELECT
		payloadState INTO s
	FROM tPayload p
	WHERE p.id = jobId AND p.user = username;

	IF s = 'completed' THEN
		SELECT 1 AS 'status', 'completed' AS 'stage';
	ELSEIF s = 'failed' THEN
		SELECT 1 AS 'status', 'failed' AS 'stage';
	ELSE
		#--- Find a stage with a null timestamp, which indicates work in progress
		SELECT
			stage INTO c
		FROM tProcessor p
			LEFT OUTER JOIN tPayload a ON p.payloadId = a.id
		WHERE ISNULL( tsEnd ) AND a.id = jobId AND a.user = username;

		#--- If we can't find anything that looks like work in progress ...
		IF ISNULL( c ) THEN
			#---- Check to see if latest stage is completed
			SELECT p.stage INTO x
			FROM tProcessor p
				LEFT OUTER JOIN tPayload a ON p.payloadId = a.id
			WHERE a.id = jobId AND a.user = username
			ORDER BY p.tsStart DESC LIMIT 1;
	
			IF x = 'transport' THEN
				SELECT 1 AS 'status', 'completed' AS 'stage';
			ELSE
				SELECT 0 AS 'status', NULL AS 'stage';
			END IF;
		ELSE
			SELECT 0 AS 'status', c AS 'stage';		
		END IF;
	END IF;
END//

DELIMITER ;

### Plugin Lookup ###

DROP TABLE IF EXISTS `tPlugins`;
CREATE TABLE `tPlugins` (
	  plugin	VARCHAR( 100 ) NOT NULL
	, version	VARCHAR( 30 ) NOT NULL
	, author	VARCHAR( 100 ) NOT NULL
	, category	ENUM ( 'validation', 'render', 'translation', 'transport', 'eligibility', 'scooper' ) NOT NULL
	, inputFormat	VARCHAR( 100 )
	, outputFormat	VARCHAR( 100 )
);

INSERT INTO `tPlugins` VALUES
		### Render plugins ###
	  ( 'org.remitt.plugin.render.XsltPlugin', '0.1', 'jeff@freemedsoftware.org', 'render', NULL, 'various' )
	, ( 'org.remitt.plugin.render.PreRenderedPlugin', '0.1', 'jeff@freemedsoftware.org', 'render', NULL, 'x12' )
		### Translation plugins ###
	, ( 'org.remitt.plugin.translation.FixedFormPdf', '0.1', 'jeff@freemedsoftware.org', 'translation', 'fixedformxml', 'pdf' )
	, ( 'org.remitt.plugin.translation.FixedFormXml', '0.1', 'jeff@freemedsoftware.org', 'translation', 'fixedformxml', 'text' )
	, ( 'org.remitt.plugin.translation.X12Xml', '0.1', 'jeff@freemedsoftware.org', 'translation', 'x12xml', 'text' )
	, ( 'org.remitt.plugin.translation.X12Passthrough', '0.1', 'jeff@freemedsoftware.org', 'translation', 'x12', 'text' )
		### Transport plugins ###
	, ( 'org.remitt.plugin.transport.ClaimLogicTransport', '0.1', 'jeff@freemedsoftware.org', 'transport', 'text', NULL )
	, ( 'org.remitt.plugin.transport.GatewayEdiTransport', '0.1', 'jeff@freemedsoftware.org', 'transport', 'text', NULL )
	, ( 'org.remitt.plugin.transport.ScriptedHttpTransport', '0.1', 'jeff@freemedsoftware.org', 'transport', 'text', NULL )
	, ( 'org.remitt.plugin.transport.SftpTransport', '0.1', 'jeff@freemedsoftware.org', 'transport', 'text', NULL )
	, ( 'org.remitt.plugin.transport.StoreFile', '0.1', 'jeff@freemedsoftware.org', 'transport', 'text', NULL )
	, ( 'org.remitt.plugin.transport.StoreFilePdf', '0.1', 'jeff@freemedsoftware.org', 'transport', 'pdf', NULL )
		### Eligibility plugins ###
	, ( 'org.remitt.plugin.eligibility.DummyEligibility', '0.1', 'jeff@freemedsoftware.org', 'eligibility', NULL, NULL )
	, ( 'org.remitt.plugin.eligibility.GatewayEDIEligibility', '0.1', 'jeff@freemedsoftware.org', 'eligibility', NULL, NULL )
	, ( 'org.remitt.plugin.eligibility.NCMedicaidEligibility', '0.1', 'jeff@freemedsoftware.org', 'eligibility', NULL, NULL )
		### Scooper plugins ###
	, ( 'org.remitt.plugin.scooper.GatewayEdiSftpScooper', '0.1', 'jeff@freemedsoftware.org', 'scooper', NULL, NULL )
	, ( 'org.remitt.plugin.scooper.SftpScooper', '0.1', 'jeff@freemedsoftware.org', 'scooper', NULL, NULL )
;

DROP TABLE IF EXISTS `tPluginOptions`;
CREATE TABLE `tPluginOptions` (
	  poption	VARCHAR( 100 ) NOT NULL
	, plugin	VARCHAR( 100 ) NOT NULL
	, fullname	VARCHAR( 100 ) NOT NULL
	, version	VARCHAR( 30 ) NOT NULL
	, author	VARCHAR( 100 ) NOT NULL
	, category	ENUM ( 'render', 'transport' ) NOT NULL
	, inputFormat	VARCHAR( 100 )
	, outputFormat	VARCHAR( 100 )
);

INSERT INTO `tPluginOptions` VALUES
		### Render plugin options ###
	  ( '4010_837p', 'org.remitt.plugin.render.XsltPlugin', 'ANSI X12 4010 837 Professional', '0.1', 'jeff@freemedsoftware.org', 'render', NULL, 'x12xml' )
	, ( '5010_837p', 'org.remitt.plugin.render.XsltPlugin', 'ANSI X12 5010 837 Professional', '0.1', 'jeff@freemedsoftware.org', 'render', NULL, 'x12xml' )
	, ( 'cms1500', 'org.remitt.plugin.render.XsltPlugin', 'CMS HCFA-1500', '0.1', 'jeff@freemedsoftware.org', 'render', NULL, 'fixedformxml' )
	, ( 'statement', 'org.remitt.plugin.render.XsltPlugin', 'Patient Statement', '0.1', 'jeff@freemedsoftware.org', 'render', NULL, 'statementxml' )
		### Transport plugin options ###
	, ( 'ClaimLogic', 'org.remitt.plugin.transport.ScriptedHttpTransport', 'ClaimLogic', '0.1', 'jeff@freemedsoftware.org', 'transport', 'text', NULL )
	, ( 'FreeClaims', 'org.remitt.plugin.transport.ScriptedHttpTransport', 'FreeClaims', '0.1', 'jeff@freemedsoftware.org', 'transport', 'text', NULL )
;

DROP TABLE IF EXISTS `tPluginOptionTransform`;
CREATE TABLE `tPluginOptionTransform` (
	  poptionold	VARCHAR( 100 ) NOT NULL
	, poption	VARCHAR( 100 ) NOT NULL
	, plugin	VARCHAR( 100 ) NOT NULL
);

INSERT INTO `tPluginOptionTransform` VALUES
	  ( '837p', '4010_837p', 'org.remitt.plugin.render.XsltPlugin' )
;

### Plugin helper functions ###

DROP FUNCTION IF EXISTS renderPluginOutputFormat;
DROP FUNCTION IF EXISTS transportPluginInputFormat;

DELIMITER //

CREATE FUNCTION renderPluginOutputFormat (
		  pluginClass VARCHAR (100)
		, pluginOption VARCHAR (100)
	) RETURNS VARCHAR (100)
BEGIN
	DECLARE ret VARCHAR (100);

	SELECT outputFormat INTO ret FROM tPlugins WHERE plugin = pluginClass;

	### For 'various' types, check the plugin ###
	IF ret = 'various' THEN
		SELECT outputFormat INTO ret FROM tPluginOptions WHERE plugin = pluginClass AND poption = pluginOption;
	END IF;

	RETURN ret;
END//

CREATE FUNCTION transportPluginInputFormat (
		  pluginClass VARCHAR (100)
		, pluginOption VARCHAR (100)
	) RETURNS VARCHAR (100)
BEGIN
	DECLARE ret VARCHAR (100);

	SELECT inputFormat INTO ret FROM tPlugins WHERE plugin = pluginClass;

	### For 'various' types, check the plugin ###
	IF ret = 'various' THEN
		SELECT inputFormat INTO ret FROM tPluginOptions WHERE plugin = pluginClass AND poption = pluginOption;
	END IF;

	RETURN ret;
END//

DELIMITER ;

### Translation Lookup ###

DROP TABLE IF EXISTS `tTranslation`;
CREATE TABLE `tTranslation` (
	  plugin	VARCHAR( 100 ) NOT NULL
	, inputFormat	VARCHAR( 100 ) NOT NULL
	, outputFormat	VARCHAR( 100 ) NOT NULL
);

INSERT INTO `tTranslation` VALUES
	  ( 'org.remitt.plugin.translation.FixedFormPdf', 'fixedformxml', 'pdf' )
	, ( 'org.remitt.plugin.translation.FixedFormXml', 'fixedformxml', 'text' )
	, ( 'org.remitt.plugin.translation.X12Xml', 'x12xml', 'text' )
;

DROP PROCEDURE IF EXISTS p_ResolveTranslationPlugin;

DELIMITER //

CREATE PROCEDURE p_ResolveTranslationPlugin (
		  renderPlugin VARCHAR (100)
		, renderOption VARCHAR (100)
		, transportPlugin VARCHAR (100)
		, transportOption VARCHAR (100)
	)
BEGIN
	SELECT plugin FROM tTranslation WHERE 
		inputFormat = renderPluginOutputFormat( renderPlugin, renderOption )
		AND FIND_IN_SET( outputFormat, transportPluginInputFormat( transportPlugin, transportOption ) )
		LIMIT 1;
END//

DELIMITER ;

### Job Scheduler ###

DROP TABLE IF EXISTS `tJobs`;

CREATE TABLE `tJobs` (
	  id		SERIAL
	, jobSchedule	VARCHAR(50) NOT NULL
	, jobClass	VARCHAR(100) NOT NULL
	, jobEnabled	BOOL NOT NULL DEFAULT TRUE
);

INSERT INTO tJobs VALUES
	  ( 1, '* * * * *', 'org.remitt.server.tasks.ScooperTask', TRUE )
	, ( 2, '*/30 * * * *', 'org.remitt.server.tasks.EligibiltyTask', TRUE )
;

### File Store ###

DROP TABLE IF EXISTS `tFileStore`;

CREATE TABLE `tFileStore` (
	  id		SERIAL
	, user		VARCHAR(50) NOT NULL
	, stamp		TIMESTAMP NOT NULL
	, category	VARCHAR(50) NOT NULL
	, filename	VARCHAR(150) NOT NULL
	, payloadId	BIGINT UNSIGNED NOT NULL
	, processorId	BIGINT UNSIGNED NOT NULL
	, content	LONGBLOB
	, contentsize	BIGINT NOT NULL DEFAULT 0

	# Force db constraint to avoid multiple files for users
	, CONSTRAINT UNIQUE KEY	( `user`, `category`, `filename` )
	, KEY			( `stamp` )
	, FOREIGN KEY		( `payloadId` ) REFERENCES tPayload ( `id` ) ON DELETE CASCADE
	, FOREIGN KEY		( `processorId` ) REFERENCES tProcessor ( `id` ) ON DELETE CASCADE
);

DROP VIEW IF EXISTS vFileStore;
CREATE VIEW vFileStore AS SELECT id, user, stamp, category, filename, payloadId, processorId, contentsize FROM tFileStore;

### Eligibility ###

DROP TABLE IF EXISTS `tEligibilityJobs`;

CREATE TABLE `tEligibilityJobs` (
	  id		SERIAL
	, user		VARCHAR(50) NOT NULL
	, inserted	TIMESTAMP NOT NULL
	, processed	TIMESTAMP NULL DEFAULT NULL
	, plugin	VARCHAR (100) NOT NULL
	, payload	LONGBLOB
	, response	LONGBLOB
	, resubmission	BOOL NOT NULL DEFAULT FALSE
	, completed	BOOL NOT NULL DEFAULT FALSE

	# Keys
	, FOREIGN KEY ( `user` ) REFERENCES tUser ( `username` ) ON DELETE CASCADE
);

### Scooper ###

DROP TABLE IF EXISTS `tScooper`;

CREATE TABLE `tScooper` (
	  id		SERIAL
	, scooperClass	VARCHAR(100) NOT NULL DEFAULT 'org.remitt.plugin.scooper.SftpScooper'
	, user		VARCHAR(50) NOT NULL
	, stamp		TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
	, host		VARCHAR(50) NOT NULL
	, path		VARCHAR(150) NOT NULL DEFAULT '/'
	, filename	VARCHAR(150) NOT NULL
	, content	LONGBLOB

	, KEY		( `scooperClass`, `user`, `host`, `path`, `filename` )
	, FOREIGN KEY	( `user` ) REFERENCES tUser ( `username` ) ON DELETE CASCADE
);

### Key Ring ###

DROP TABLE IF EXISTS `tKeyring`;

CREATE TABLE `tKeyring` (
	  id		SERIAL
	, user		VARCHAR (50) NOT NULL
	, keyname	VARCHAR (150) NOT NULL
	, privatekey	BLOB
	, publickey	BLOB

	, KEY		( `user`, `keyname` )
	, FOREIGN KEY	( `user` ) REFERENCES tUser ( `username` ) ON DELETE CASCADE
);

### Host Keys ###

DROP TABLE IF EXISTS `tSshHostKeys`;

CREATE TABLE `tSshHostKeys` (
	  id		SERIAL
	, hostname	VARCHAR (150) NOT NULL
	, port		INT UNSIGNED NOT NULL DEFAULT 22
	, hostkey	TEXT

	, CONSTRAINT UNIQUE KEY ( `hostname`, `port` )
);

###INSERT INTO `tSshHostKeys` ( hostname, hostkey ) VALUES ( 'sftp.claimlogic.com', '' );
INSERT INTO `tSshHostKeys` ( hostname, hostkey ) VALUES ( 'sftp.gatewayedi.com', 'ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAAAgQDCh9qdcv1i9Y6nDwpspLaW1OosdrrtOl0t7uiof2/QYs0RTmT1DVRz0D0SNweNjtB/5069pFaNMthEh591gNrnipxy2FA2Zz7x5fv0v/AbTjmTujK14GYDBvMQTA58jGf1NWRn0+CkJvhCqY4eylkYgXdn4Y5QgGQYoEvN9P6zdQ==' );

### Database Patches ###

DROP TABLE IF EXISTS `tPatch`;

CREATE TABLE `tPatch` (
	  id		SERIAL
	, patchName	VARCHAR(150) NOT NULL
	, stamp		TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP

	# Ensure patches are unique entries
	, CONSTRAINT UNIQUE KEY ( `patchName` )
);

INSERT INTO tPatch VALUES
	  ( NULL, "00-schema", NOW() )
;

