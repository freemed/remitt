
DROP TABLE IF EXISTS `tUser`;
CREATE TABLE `tUser` (
	  id		SERIAL
	, username	VARCHAR(50) NOT NULL UNIQUE KEY
	, passhash	CHAR(16) NOT NULL
	, apiurl	VARCHAR(150) COMMENT 'For later use'
);

INSERT INTO `tUser` VALUES ( 1, 'Administrator', MD5('password') );

DROP TABLE IF EXISTS `tRole`;
CREATE TABLE `tRole` (
	  id		SERIAL
	, username	VARCHAR(50) NOT NULL
	, rolename	VARCHAR(50) NOT NULL
	, PRIMARY KEY ( username, rolename )
);

INSERT INTO `tRole` VALUES ( 1, 'Administrator', 'admin' );

DROP TABLE IF EXISTS `tUserConfig`;
CREATE TABLE `tUserConfig` (
	  user		VARCHAR(50) NOT NULL
	, cNamespace	VARCHAR(150) NOT NULL
	, cOption	VARCHAR(50) NOT NULL
	, cValue	BLOB

	, FOREIGN KEY ( user ) REFERENCES tUser.username ON DELETE CASCADE
);

DROP TABLE IF EXISTS `tPayload`;
CREATE TABLE `tPayload` (
	  id			SERIAL
	, insert_stamp		TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
	, user			VARCHAR(50) NOT NULL
	, payload		BLOB
	, renderPlugin		VARCHAR(100) NOT NULL
	, renderOption		VARCHAR(100) NOT NULL
	, transmissionPlugin	VARCHAR(100) NOT NULL
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
;

