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

package org.remitt.plugin.transmission;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;

import net.sourceforge.htmlunit.corejs.javascript.Context;
import net.sourceforge.htmlunit.corejs.javascript.Scriptable;
import net.sourceforge.htmlunit.corejs.javascript.ScriptableObject;

import org.apache.log4j.Logger;
import org.remitt.prototype.PluginInterface;
import org.remitt.server.Configuration;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;

public class ScriptedHttpTransport implements PluginInterface {

	static final Logger log = Logger.getLogger(ScriptedHttpTransport.class);

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
		return new String[] { "script", "username", "password" };
	}

	@Override
	public String getPluginName() {
		return "ScriptedHttpTransport";
	}

	@Override
	public Double getPluginVersion() {
		return 0.1;
	}

	@Override
	public String render(Integer jobId, String input, String option)
			throws Exception {
		// TODO: get username
		String userName = "FIXME";

		// Get configuration
		String scriptName = Configuration.getPluginOption(this, userName,
				"script");

		// Initialize javascript VM
		Context cx = Context.enter();
		cx.setLanguageVersion(Context.VERSION_1_2);
		Scriptable scope = cx.initStandardObjects();

		WebClient webClient = new WebClient(BrowserVersion.FIREFOX_3);

		// Inject into global namespace for execution
		ScriptableObject
				.putProperty(scope, "log", Context.javaToJS(log, scope));
		ScriptableObject.putProperty(scope, "jobId", Context.javaToJS(jobId,
				scope));
		ScriptableObject.putProperty(scope, "input", Context.javaToJS(input,
				scope));
		ScriptableObject.putProperty(scope, "webClient", Context.javaToJS(
				webClient, scope));

		// Fetch and execute script.
		String script = readFileAsString(Configuration.getServletContext()
				.getServletContext().getRealPath(
						"/WEB-INF/scripts/" + scriptName));
		Object result = cx.evaluateString(scope, script, "webScript", 1, null);
		String output = Context.toString(result);

		// Close up shop.
		Context.exit();

		// Return the output from the script.
		return output;
	}

	/**
	 * Read a file into a string.
	 * 
	 * @param filePath
	 * @return
	 * @throws java.io.IOException
	 */
	private static String readFileAsString(String filePath)
			throws java.io.IOException {
		StringBuffer fileData = new StringBuffer(1000);
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
		char[] buf = new char[1024];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			fileData.append(buf, 0, numRead);
		}
		reader.close();
		return fileData.toString();
	}
}
