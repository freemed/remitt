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

package org.remitt.plugin.render;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.remitt.datastore.DbPlugin;
import org.remitt.prototype.PluginInterface;
import org.remitt.server.Configuration;

public class XsltPlugin implements PluginInterface {

	static final Logger log = Logger.getLogger(XsltPlugin.class);

	protected String defaultUsername = "";

	@Override
	public String getInputFormat() {
		return "remittxml";
	}

	@Override
	public HashMap<String, String> getOptions() {
		return null;
	}

	@Override
	public String getOutputFormat() {
		return "variable";
	}

	@Override
	public String getPluginName() {
		return "Xslt";
	}

	@Override
	public Double getPluginVersion() {
		return 0.1;
	}

	@Override
	public byte[] render(Integer jobId, byte[] input, String option)
			throws Exception {
		log.info("Entered Render for job #" + jobId.toString());

		TransformerFactory tFactory = TransformerFactory.newInstance();

		String transformedOption = DbPlugin.resolvePluginOption(
				"org.remitt.plugin.render.XsltPlugin", option);

		log.info("Original plugin option = " + option + ", transformed to "
				+ transformedOption);

		String xsltPath = Configuration.getServletContext().getServletContext()
				.getRealPath("/WEB-INF/xsl/" + transformedOption + ".xsl");

		// Input data, load
		Source xmlInput = new StreamSource(new StringReader(new String(input)));

		// Create XSL Transformation
		log.debug("Loading xsl into transformer");
		Transformer transformer;
		try {
			transformer = tFactory.newTransformer(new StreamSource(xsltPath));
		} catch (TransformerConfigurationException e) {
			log.error(e);
			throw new Exception(e);
		}

		log.debug("Passing parameters to transform");
		transformer.setParameter("currentTime", new Long(System
				.currentTimeMillis()).toString());
		transformer.setParameter("jobId", jobId == 0 ? new Long(System
				.currentTimeMillis()).toString() : jobId.toString());

		StreamResult xmlOutput = new StreamResult(new ByteArrayOutputStream());

		log.debug("Performing transformation");
		transformer.transform(xmlInput, xmlOutput);

		log.info("Leaving Render for job #" + jobId.toString());

		// Push stream to output
		return xmlOutput.getOutputStream().toString().getBytes("UTF-8");
	}

	@Override
	public String[] getPluginConfigurationOptions() {
		List<String> options = new ArrayList<String>();

		File dir = new File(Configuration.getServletContext()
				.getServletContext().getRealPath("/WEB-INF/xsl/"));

		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return !name.startsWith(".") && name.endsWith(".xsl");
			}
		};
		String[] xslFiles = dir.list(filter);

		if (xslFiles == null) {
			return null;
		} else {
			for (int i = 0; i < xslFiles.length; i++) {
				// Get filename of file or directory
				options.add(xslFiles[i].replaceAll("\\.xsl$", ""));
			}
		}

		return (String[]) options.toArray(new String[0]);
	}

	@Override
	public void setDefaultUsername(String username) {
		defaultUsername = username;
	}

}
