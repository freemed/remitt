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

package org.remitt.plugin.transport;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.remitt.datastore.DbFileStore;
import org.remitt.prototype.PluginInterface;
import org.remitt.server.Configuration;

public class StoreFile implements PluginInterface {

	static final Logger log = Logger.getLogger(StoreFile.class);

	protected String defaultUsername = null;

	@Override
	public String getInputFormat() {
		return "text";
	}

	@Override
	public HashMap<String, String> getOptions() {
		return null;
	}

	@Override
	public String getOutputFormat() {
		return null;
	}

	@Override
	public String[] getPluginConfigurationOptions() {
		return null;
	}

	@Override
	public String getPluginName() {
		return "StoreFile";
	}

	@Override
	public Double getPluginVersion() {
		return 0.1;
	}

	@Override
	public byte[] render(Integer jobId, byte[] input, String option)
			throws Exception {
		log.info("Entered Transport for job #" + jobId.toString());

		String userName = null;
		if (jobId == 0) {
			userName = defaultUsername;
		} else {
			userName = Configuration.getControlThread()
					.getPayloadFromProcessor(jobId).getUserName();
		}

		String inputString = new String(input);

		String outputType = "";
		if (inputString.startsWith("%PDF")) {
			outputType = "pdf";
		} else if (inputString.startsWith("<?xml ")) {
			outputType = "xml";
		} else if (inputString.startsWith("ISA*")) {
			outputType = "x12";
		} else {
			outputType = "txt";
		}

		String tempPathName = new Long(System.currentTimeMillis()).toString()
				+ "." + outputType;

		// Store this file
		DbFileStore.putFile(userName, "output", tempPathName, input, jobId);

		log.info("Leaving Transport for job #" + jobId.toString());

		// Return filename
		return tempPathName.getBytes();
	}

	@Override
	public void setDefaultUsername(String username) {
		this.defaultUsername = username;
	}

}
