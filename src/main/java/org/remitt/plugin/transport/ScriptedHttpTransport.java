/*
 * $Id$
 *
 * Authors:
 *      Jeff Buchbinder <jeff@freemedsoftware.org>
 *
 * REMITT Electronic Medical Information Translation and Transmission
 * Copyright (C) 1999-2014 FreeMED Software Foundation
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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.HashMap;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.log4j.Logger;
import org.remitt.datastore.DbFileStore;
import org.remitt.prototype.PluginInterface;
import org.remitt.server.Configuration;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.RefreshHandler;
import com.gargoylesoftware.htmlunit.WebClient;

public class ScriptedHttpTransport implements PluginInterface {

	static final Logger log = Logger.getLogger(ScriptedHttpTransport.class);

	protected String defaultUsername = "";

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
	public byte[] render(Integer jobId, byte[] input, String option)
			throws Exception {
		log.info("Entered Transport for job #" + jobId.toString());

		String userName = null;
		if (jobId == 0 || jobId == null) {
			// No job id, no user name
			userName = defaultUsername;
		} else {
			userName = Configuration.getControlThread()
					.getPayloadFromProcessor(jobId).getUserName();
		}

		// Get configuration
		String scriptName = null;
		if (option == null) {
			scriptName = Configuration
					.getPluginOption(this, userName, "script");
		} else {
			scriptName = option;
		}

		ScriptEngineManager engineMgr = new ScriptEngineManager();
		ScriptEngine engine = engineMgr.getEngineByName("JavaScript");

		// Fetch and execute script.
		String scriptPath = "/WEB-INF/scripts/org.remitt.plugin.transport.ScriptedHttpTransport/"
				+ scriptName + ".js";
		String commonScriptPath = "/WEB-INF/scripts/org.remitt.plugin.transport.ScriptedHttpTransport/Common.js";
		String realCommonScriptPath = Configuration.getServletContext()
				.getServletContext().getRealPath(commonScriptPath);
		String realScriptPath = Configuration.getServletContext()
				.getServletContext().getRealPath(scriptPath);

		// Instantiate web client (htmlunit)
		WebClient webClient = new WebClient(BrowserVersion.FIREFOX_17);
		// DEPRECATED: webClient.setJavaScriptEnabled(true);
		webClient.setRefreshHandler(new RefreshHandler() {
			@Override
			public void handleRefresh(Page arg0, URL arg1, int arg2)
					throws IOException {
				log.info("Attempted refresh to " + arg1.getPath());
			}
		});

		// Inject objects
		engine.put("log", log);
		engine.put("jobId", jobId);
		engine.put("input", input);
		engine.put("webClient", webClient);
		log.info("username = "
				+ Configuration.getPluginOption(this, userName, "username"));
		engine.put("username", Configuration.getPluginOption(this, userName,
				"username"));
		log.info("password = "
				+ Configuration.getPluginOption(this, userName, "password"));
		engine.put("password", Configuration.getPluginOption(this, userName,
				"password"));

		log.info("Leaving Transport for job #" + jobId.toString());

		InputStream is = new FileInputStream(realScriptPath);
		InputStream cis = new FileInputStream(realCommonScriptPath);
		byte[] out = null;
		try {
			// Evaluate common code
			Reader cReader = new InputStreamReader(cis);
			engine.eval(cReader);

			// Evaluate plugin
			Reader reader = new InputStreamReader(is);
			engine.eval(reader);

			Invocable invocableEngine = (Invocable) engine;
			Object output = invocableEngine.invokeFunction("transport");
			if (output != null) {
				if (output instanceof String) {
					out = ((String) output).getBytes();
				} else {
					out = output.toString().getBytes();
				}
			} else {
				out = new String("").getBytes();
			}
		} catch (ScriptException ex) {
			log.error(ex);
			out = new String("").getBytes();
		}

		String tempPathName = new Long(System.currentTimeMillis()).toString()
				+ ".log";

		// Store this file
		DbFileStore.putFile(userName, "output", tempPathName, out, jobId);

		// .. and return it.
		return out;
	}

	@Override
	public void setDefaultUsername(String username) {
		defaultUsername = username;
	}

}
