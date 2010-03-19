/*
 * $Id$
 *
 * Authors:
 *      Jeff Buchbinder <jeff@freemedsoftware.org>
 *
 * REMITT Electronic Medical Information Translation and Transmission
 * Copyright (C) 1999-2010 FreeMED Software Foundation
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

import java.util.HashMap;

import javax.jws.WebParam;
import javax.jws.WebService;

import org.remitt.prototype.ConfigurationOption;
import org.remitt.prototype.EligibilityResponse;

@WebService
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
	public Boolean changePassword(@WebParam(name = "pw") String newPassword);

	/**
	 * Get the currently authenticated user's name.
	 * 
	 * @return String representation of the user's name.
	 */
	public String getCurrentUserName();

	/**
	 * Insert payload into the system for processing.
	 * 
	 * @param inputPayload
	 *            XML text payload
	 * @param renderPlugin
	 *            Fully qualified Java class name of rendering plugin.
	 * @param renderOption
	 *            Optional option for render plugin.
	 * @param transportPlugin
	 *            Fully qualified Java class name of transport/transmission
	 *            plugin
	 * @param transportOption
	 *            Optional option for the transport/transmission plugin
	 * @return tPayload unique identifier for this job.
	 */
	public Integer insertPayload(
			@WebParam(name = "inputPayload") String inputPayload,
			@WebParam(name = "renderPlugin") String renderPlugin,
			@WebParam(name = "renderOption") String renderOption,
			@WebParam(name = "transportPlugin") String transportPlugin,
			@WebParam(name = "transportOption") String transportOption);

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
			@WebParam(name = "namespace") String namespace,
			@WebParam(name = "option") String option,
			@WebParam(name = "value") String value);

	/**
	 * Retrieve current job status
	 * 
	 * @param jobId
	 * @return Integer indicating current status. 0 = completed, 1 =
	 *         verification, 2 = rendering, 3 = translation, 4 =
	 *         transmission/transport, 5 = unknown
	 */
	public Integer getStatus(@WebParam(name = "jobId") Integer jobId);

	/**
	 * Retrieve current job status for a list of job ids.
	 * 
	 * @param jobIds
	 *            Array of payload ids.
	 * @return Array of Integers indicating current status. 0 = completed, 1 =
	 *         verification, 2 = rendering, 3 = translation, 4 =
	 *         transmission/transport, 5 = unknown
	 */
	public Integer[] getBulkStatus(@WebParam(name = "jobIds") Integer[] jobIds);

	/**
	 * Retrieve list of file names that match the provided criteria.
	 * 
	 * @param category
	 * @param criteria
	 * @param value
	 * @return
	 */
	public String[] getFileList(@WebParam(name = "category") String category,
			@WebParam(name = "criteria") String criteria,
			@WebParam(name = "value") String value);

	/**
	 * Get list of plugins for a specified category.
	 * 
	 * @param category
	 * @return
	 */
	public String[] getPlugins(@WebParam(name = "category") String category);

	/**
	 * Retrieve output file.
	 * 
	 * @param category
	 *            Output file category.
	 * @param fileName
	 *            Name of file to be retrieved.
	 * @return Contents of target file as byte array.
	 */
	public byte[] getFile(@WebParam(name = "category") String category,
			@WebParam(name = "filename") String fileName);

	/**
	 * Get list of years for which the system has output files.
	 * 
	 * @return
	 */
	public Integer[] getOutputYears();

	/**
	 * Get list of months in a target year for which the system has output
	 * files.
	 * 
	 * @param targetYear
	 * @return
	 */
	public String[] getOutputMonths(
			@WebParam(name = "targetYear") Integer targetYear);

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
			@WebParam(name = "pluginclass") String pluginClass,
			@WebParam(name = "qualifyingoption") String qualifyingOption);

	/**
	 * Check for eligibility.
	 * 
	 * @param plugin
	 * @param parameters
	 * @return
	 */
	public EligibilityResponse getEligibility(
			@WebParam(name = "plugin") String plugin,
			@WebParam(name = "parameters") HashMap<String, String> parameters);

	/**
	 * Use a REMITT parser to parse source data.
	 * 
	 * @param parserClass
	 *            <ParserInterface> descendent class name
	 * @param data
	 *            Raw data to be parsed
	 * @return
	 */
	public String parseData(@WebParam(name = "parserClass") String parserClass,
			@WebParam(name = "data") String data);

}
