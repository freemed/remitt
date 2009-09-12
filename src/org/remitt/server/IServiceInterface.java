/*
 * $Id$
 *
 * Authors:
 *      Jeff Buchbinder <jeff@freemedsoftware.org>
 *
 * REMITT Electronic Medical Information Translation and Transmission
 * Copyright (C) 1999-2009 FreeMED Software Foundation
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

import javax.jws.WebService;

@WebService
public interface IServiceInterface {

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
	public Boolean changePassword(String newPassword);

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
	public Integer insertPayload(String inputPayload, String renderPlugin,
			String renderOption, String transportPlugin, String transportOption);

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
	public Boolean setConfigValue(String namespace, String option, String value);

}
