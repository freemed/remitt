/*
 * $Id$
 *
 * Authors:
 *      Jeff Buchbinder <jeff@freemedsoftware.org>
 *
 * REMITT Electronic Medical Information Translation and Transmission
 * Copyright (C) 1999-2011 FreeMED Software Foundation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package org.remitt.server;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.ws.rs.PathParam;

import org.apache.cxf.annotations.GZIP;
import org.remitt.prototype.ConfigurationOption;
import org.remitt.prototype.EligibilityRequest;
import org.remitt.prototype.EligibilityResponse;
import org.remitt.prototype.FileListingItem;
import org.remitt.prototype.UserDTO;
import org.remitt.prototype.ValidationResponse;

@GZIP
@WebService(targetNamespace = "http://server.remitt.org/")
public interface Service {

	/**
	 * Get version number of REMITT protocol being used.
	 * 
	 * @return String representation of protocol version.
	 */
	public String getProtocolVersion();

	/**
	 * Change current user's password
	 * 
	 * @param newPassword
	 *            New password
	 * @return Success
	 */
	public Boolean changePassword(
			@PathParam("pw") @WebParam(name = "pw") String newPassword);

	/**
	 * Get the currently authenticated user's name.
	 * 
	 * @return String representation of the user's name.
	 */
	public String getCurrentUserName();

	/**
	 * Insert payload into the system for processing.
	 * 
	 * @param originalId
	 *            Foreign system id for this payload
	 * @param inputPayload
	 *            XML text payload
	 * @param renderPlugin
	 *            Fully qualified Java class name of rendering plugin.
	 * @param renderOption
	 *            Optional option for render plugin.
	 * @param transportPlugin
	 *            Fully qualified Java class name of transport plugin
	 * @param transportOption
	 *            Optional option for the transport plugin
	 * @return tPayload unique identifier for this job.
	 */
	public Integer insertPayload(
			@PathParam("originalId") @WebParam(name = "originalId") String originalId,
			@PathParam("inputPayload") @WebParam(name = "inputPayload") String inputPayload,
			@PathParam("renderPlugin") @WebParam(name = "renderPlugin") String renderPlugin,
			@PathParam("renderOption") @WebParam(name = "renderOption") String renderOption,
			@PathParam("transportPlugin") @WebParam(name = "transportPlugin") String transportPlugin,
			@PathParam("transportOption") @WebParam(name = "transportOption") String transportOption);

	/**
	 * Resubmit an existing payload.
	 * 
	 * @param originalPayloadId
	 *            tPayload unique identifier for the original payload.
	 * @return New tPayload unique identifier for this job.
	 */
	public Integer resubmitPayload(
			@PathParam("originalPayloadId") @WebParam(name = "originalPayloadId") Integer originalPayloadId);

	/**
	 * Get all configuration values for a user.
	 * 
	 * @return
	 */
	public ConfigurationOption[] getConfigValues();

	/**
	 * Set configuration option.
	 * 
	 * @param namespace
	 *            Fully qualified class name of plugin.
	 * @param option
	 *            Option key.
	 * @param value
	 *            Option value.
	 * @return Success.
	 */
	public Boolean setConfigValue(
			@PathParam("namespace") @WebParam(name = "namespace") String namespace,
			@PathParam("option") @WebParam(name = "option") String option,
			@PathParam("value") @WebParam(name = "value") String value);

	/**
	 * Retrieve current job status
	 * 
	 * @param jobId
	 * @return Integer indicating current status. 0 = completed, 1 =
	 *         verification, 2 = rendering, 3 = translation, 4 = transport, 5 =
	 *         unknown, 6 = failed
	 */
	public Integer getStatus(
			@PathParam("jobId") @WebParam(name = "jobId") Integer jobId);

	/**
	 * Retrieve current job status for a list of job ids.
	 * 
	 * @param jobIds
	 *            Array of payload ids.
	 * @return Array of Integers indicating current status. 0 = completed, 1 =
	 *         verification, 2 = rendering, 3 = translation, 4 = transport, 5 =
	 *         unknown
	 */
	public Integer[] getBulkStatus(
			@PathParam("jobIds") @WebParam(name = "jobIds") Integer[] jobIds);

	/**
	 * Retrieve list of file names that match the provided criteria.
	 * 
	 * @param category
	 * @param criteria
	 * @param value
	 * @return
	 */
	public FileListingItem[] getFileList(
			@PathParam("category") @WebParam(name = "category") String category,
			@PathParam("criteria") @WebParam(name = "criteria") String criteria,
			@PathParam("value") @WebParam(name = "value") String value);

	/**
	 * Get list of plugins for a specified category.
	 * 
	 * @param category
	 * @return
	 */
	public String[] getPlugins(
			@PathParam("category") @WebParam(name = "category") String category);

	/**
	 * Retrieve output file.
	 * 
	 * @param category
	 *            Output file category.
	 * @param fileName
	 *            Name of file to be retrieved.
	 * @return Contents of target file as byte array.
	 */
	public byte[] getFile(
			@PathParam("category") @WebParam(name = "category") String category,
			@PathParam("filename") @WebParam(name = "filename") String fileName);

	/**
	 * Get list of years for which the system has output files.
	 * 
	 * @return
	 */
	public Integer[][] getOutputYears();

	/**
	 * Get list of months in a target year for which the system has output
	 * files.
	 * 
	 * @param targetYear
	 * @return
	 */
	public String[] getOutputMonths(
			@PathParam("targetyear") @WebParam(name = "targetYear") Integer targetYear);

	/**
	 * Get list of plugin options.
	 * 
	 * @param pluginClass
	 *            Fully qualified class name of target plugin.
	 * @param qualifyingOption
	 *            Optional qualifier, required by render plugin.
	 * @return
	 */
	public String[] getPluginOptions(
			@PathParam("pluginclass") @WebParam(name = "pluginclass") String pluginClass,
			@PathParam("qualifyingoption") @WebParam(name = "qualifyingoption") String qualifyingOption);

	/**
	 * Check for eligibility.
	 * 
	 * @param request
	 * @return
	 */
	public EligibilityResponse getEligibility(
			@PathParam("request") @WebParam(name = "request") EligibilityRequest request);

	/**
	 * Insert batches of eligibility checks.
	 * 
	 * @param requests
	 * @return
	 */
	public Integer batchEligibilityCheck(
			@PathParam("requests") @WebParam(name = "requests") EligibilityRequest[] requests);

	/**
	 * Use a REMITT parser to parse source data.
	 * 
	 * @param parserClass
	 *            <ParserInterface> descendent class name
	 * @param data
	 *            Raw data to be parsed
	 * @return
	 */
	public String parseData(
			@PathParam("parserClass") @WebParam(name = "parserClass") String parserClass,
			@PathParam("data") @WebParam(name = "data") String data);

	/**
	 * Validate an arbitrary
	 * 
	 * @param validatorClass
	 *            <ValidationPlugin> descendent class name
	 * @param data
	 *            Raw data to be validated.
	 * @return Payload describing validator output
	 */
	public ValidationResponse validatePayload(
			@PathParam("parserClass") @WebParam(name = "validatorClass") String validatorClass,
			@PathParam("data") @WebParam(name = "data") byte[] data);

	/**
	 * Add a REMITT user.
	 * 
	 * @param user
	 * @return
	 */
	public boolean addRemittUser(
			@PathParam("user") @WebParam(name = "user") UserDTO user);

	/**
	 * Get array of REMITT user objects.
	 * 
	 * @return
	 */
	public UserDTO[] listRemittUsers();

	/**
	 * Add an encryption key to the REMITT user's key ring.
	 * 
	 * @param keyname
	 * @param privatekey
	 * @param publickey
	 * @return
	 */
	public boolean addKeyToKeyring(
			@PathParam("keyname") @WebParam(name = "keyname") String keyname,
			@PathParam("privatekey") @WebParam(name = "privatekey") byte[] privatekey,
			@PathParam("publickey") @WebParam(name = "publickey") byte[] publickey);

}
