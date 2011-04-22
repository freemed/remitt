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

package org.remitt.plugin.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.remitt.prototype.PluginInterface;

public class PreRenderedPlugin implements PluginInterface {

	static final Logger log = Logger.getLogger(PreRenderedPlugin.class);

	protected String defaultUsername = "";

	@Override
	public String getInputFormat() {
		return "x12";
	}

	@Override
	public HashMap<String, String> getOptions() {
		return null;
	}

	@Override
	public String getOutputFormat() {
		return "x12";
	}

	@Override
	public String getPluginName() {
		return "PreRendered";
	}

	@Override
	public Double getPluginVersion() {
		return 0.1;
	}

	@Override
	public byte[] render(Integer jobId, byte[] input, String option)
			throws Exception {
		log.info("Entered Render for job #" + jobId.toString());

		// Pass through input
		return input;
	}

	@Override
	public String[] getPluginConfigurationOptions() {
		List<String> options = new ArrayList<String>();
		return (String[]) options.toArray(new String[0]);
	}

	@Override
	public void setDefaultUsername(String username) {
		defaultUsername = username;
	}

}
