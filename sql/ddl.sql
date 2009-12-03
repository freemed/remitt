# $Id$
#
# Authors:
# 	Jeff Buchbinder <jeff@freemedsoftware.org>
#
# REMITT Electronic Medical Information Translation and Transmission
# Copyright (C) 1999-2009 FreeMED Software Foundation
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
	  id		SERIAL
	, username	VARCHAR(50) NOT NULL UNIQUE KEY
	, passhash	CHAR(32) NOT NULL
	, apiurl	VARCHAR(150) COMMENT 'For later use'
);

INSERT INTO `tUser` VALUES ( 1, 'Administrator', MD5('password'), NULL );

DROP TABLE IF EXISTS `tRole`;
CREATE TABLE `tRole` (
	  id		SERIAL
	, username	VARCHAR(50) NOT NULL
	, rolename	VARCHAR(50) NOT NULL
	, PRIMARY KEY ( username, rolename )
);

INSERT INTO `tRole` VALUES ( NULL, 'Administrator', 'admin' );
INSERT INTO `tRole` VALUES ( NULL, 'Administrator', 'default' );

DROP TABLE IF EXISTS `tUserConfig`;
CREATE TABLE `tUserConfig` (
	  user		VARCHAR(50) NOT NULL
	, cNamespace	VARCHAR(150) NOT NULL
	, cOption	VARCHAR(50) NOT NULL
	, cValue	BLOB

	, FOREIGN KEY ( user ) REFERENCES tUser.username ON DELETE CASCADE
);

INSERT INTO `tUserConfig` VALUES
	  ( 'Administrator', 'org.remitt.plugin.transmission.SftpTransport', 'sftpHost', '' )
	, ( 'Administrator', 'org.remitt.plugin.transmission.SftpTransport', 'sftpPort', '22' )
	, ( 'Administrator', 'org.remitt.plugin.transmission.SftpTransport', 'sftpUsername', '' )
	, ( 'Administrator', 'org.remitt.plugin.transmission.SftpTransport', 'sftpPassword', '' )
	, ( 'Administrator', 'org.remitt.plugin.transmission.ScriptedHttpTransport', 'username', 'user' )
	, ( 'Administrator', 'org.remitt.plugin.transmission.ScriptedHttpTransport', 'password', 'pass' )
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
	, payload		BLOB
	, renderPlugin		VARCHAR(100) NOT NULL
	, renderOption		VARCHAR(100) NOT NULL
	, transmissionPlugin	VARCHAR(100) NOT NULL
	, transmissionOption	VARCHAR(100) NOT NULL
);

##### Processor Tables #####

DROP TABLE IF EXISTS `tProcessor`;
CREATE TABLE `tProcessor` (
	  id		SERIAL
	, threadId	INT UNSIGNED NOT NULL DEFAULT 0
	, payloadId	INT UNSIGNED NOT NULL
	, stage		ENUM ( 'validation', 'render', 'translation', 'transmission' )
	, plugin	VARCHAR (100) NOT NULL
	, tsStart	TIMESTAMP NULL DEFAULT NULL
	, tsEnd		TIMESTAMP NULL DEFAULT NULL
	, pInput	BLOB
	, pOutput	BLOB

	, FOREIGN KEY ( payloadId ) REFERENCES tPayload.id ON DELETE CASCADE
);

DROP PROCEDURE IF EXISTS p_GetStatus;

DELIMITER //
CREATE PROCEDURE p_GetStatus (
	  IN username VARCHAR( 100 )
	, IN jobId INT UNSIGNED
	)
BEGIN
	DECLARE c ENUM ( 'validation', 'render', 'translation', 'transmission' );
	DECLARE x INT UNSIGNED;

	SELECT NULL INTO c;

	SELECT
		stage INTO c
	FROM tProcessor p
		LEFT OUTER JOIN tPayload a ON p.payloadId = a.id
	WHERE ISNULL( tsEnd ) AND a.id = jobId AND a.user = username;

	#--- If we can't find anything...
	IF ISNULL( c ) THEN
		#---- Check to see if there's output
		SELECT COUNT(*) INTO x
		FROM tOutput o
			LEFT OUTER JOIN tPayload p ON o.payloadId = p.id
		WHERE p.id = jobId AND p.user = username;
		IF x > 0 THEN
			SELECT 1 AS 'status', 'completed' AS 'stage';
		ELSE
			SELECT 0 AS 'status', NULL AS 'stage';
		END IF;
	ELSE
		SELECT 0 AS 'status', c AS 'stage';		
	END IF;
END//

DELIMITER ;

DROP TABLE IF EXISTS `tThreadState`;
CREATE TABLE `tThreadState` (
	  threadId	INT UNSIGNED NOT NULL UNIQUE
	, processorId	INT UNSIGNED DEFAULT NULL
);

DROP TABLE IF EXISTS `tOutput`;
CREATE TABLE `tOutput` (
	  id		SERIAL
	, tsCreated	TIMESTAMP DEFAULT CURRENT_TIMESTAMP
	, payloadId	INT UNSIGNED NOT NULL
	, processorId	INT UNSIGNED NOT NULL
	, filename	VARCHAR(150) NOT NULL
	, filesize	INT UNSIGNED NOT NULL DEFAULT 0

	, FOREIGN KEY ( payloadId ) REFERENCES tPayload.id ON DELETE CASCADE
	, FOREIGN KEY ( processorId ) REFERENCES tProcessor.id ON DELETE CASCADE
);

### Plugin Lookup ###

DROP TABLE IF EXISTS `tPlugins`;
CREATE TABLE `tPlugins` (
	  plugin	VARCHAR( 100 ) NOT NULL
	, version	VARCHAR( 30 ) NOT NULL
	, author	VARCHAR( 100 ) NOT NULL
	, category	ENUM ( 'validation', 'render', 'translation', 'transmission' ) NOT NULL
	, inputFormat	VARCHAR( 100 )
	, outputFormat	VARCHAR( 100 )
);

INSERT INTO `tPlugins` VALUES
		### Render plugins ###
	  ( 'org.remitt.plugin.render.XsltPlugin', '0.1', 'jeff@freemedsoftware.org', 'render', NULL, 'various' )
		### Translation plugins ###
	, ( 'org.remitt.plugin.translation.FixedFormPdf', '0.1', 'jeff@freemedsoftware.org', 'translation', 'fixedformxml', 'pdf' )
	, ( 'org.remitt.plugin.translation.FixedFormXml', '0.1', 'jeff@freemedsoftware.org', 'translation', 'fixedformxml', 'text' )
	, ( 'org.remitt.plugin.translation.X12Xml', '0.1', 'jeff@freemedsoftware.org', 'translation', 'x12xml', 'text' )
		### Transmission plugins ###
	, ( 'org.remitt.plugin.transmission.ScriptedHttpTransport', '0.1', 'jeff@freemedsoftware.org', 'transmission', 'text', NULL )
	, ( 'org.remitt.plugin.transmission.SftpTransport', '0.1', 'jeff@freemedsoftware.org', 'transmission', 'text', NULL )
;

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

### Job Scheduler ###

DROP TABLE IF EXISTS `tJobs`;

CREATE TABLE `tJobs` (
	  id		SERIAL
	, jobSchedule	VARCHAR(50) NOT NULL
	, jobClass	VARCHAR(100) NOT NULL
	, jobEnabled	BOOL NOT NULL DEFAULT TRUE
);

