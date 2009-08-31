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

package org.remitt.plugin.translation;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.remitt.prototype.PluginInterface;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class X12Xml implements PluginInterface {

	static final Logger log = Logger.getLogger(X12Xml.class);

	protected XPath xpath = null;

	protected HashMap<String, Integer> hlcount = new HashMap<String, Integer>();

	@Override
	public String getInputFormat() {
		return "x12xml";
	}

	public HashMap<String, String> getOptions() {
		return null;
	}

	@Override
	public String getOutputFormat() {
		return "text";
	}

	@Override
	public String getPluginName() {
		return "X12Xml";
	}

	@Override
	public Double getPluginVersion() {
		return 0.1;
	}

	@Override
	public String render(Integer jobId, String input, String option)
			throws Exception {
		log.info("Entered Translate for job #" + jobId.toString());

		StringBuilder sb = new StringBuilder();

		// Attempt to load input stream
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = dbFactory.newDocumentBuilder();
		log.debug("Loading input into XmlDocument");
		ByteArrayInputStream inputStream = new ByteArrayInputStream(input
				.getBytes("UTF-8"));
		org.w3c.dom.Document xmlInput = builder.parse(inputStream);

		XPathFactory xpFactory = XPathFactory.newInstance();
		xpath = xpFactory.newXPath();

		Node root = xmlInput.getDocumentElement();
		NodeList nodeList = (NodeList) xpath.evaluate("/render/x12segment",
				root, XPathConstants.NODESET);

		String eEnd = xpath.evaluate("/render/x12format/delimiter", root);
		String sEnd = xpath.evaluate("/render/x12format/endofline", root);

		// Loop through all segment elements
		for (int iter = 0; iter < nodeList.getLength(); iter++) {
			String segment = TranslateSegmentFromNode(nodeList.item(iter),
					eEnd, sEnd, iter);
			sb.append(segment);
			sb.append("\n");
		}

		return sb.toString();
	}

	private String TranslateSegmentFromNode(Node segment, String elementEnd,
			String segmentEnd, int segmentCount) {
		List<String> l = new ArrayList<String>();

		// Get the X12 segment id
		String segmentId = ((Element) segment).getAttribute("sid");

		// Get elements ...
		NodeList elementNodes = ((Element) segment)
				.getElementsByTagName("element");

		// Perform sorting by creating list
		ArrayList<Element> elements = new ArrayList<Element>();
		for (int iter = 0; iter < elementNodes.getLength(); iter++) {
			elements.add((Element) elementNodes.item(iter));
		}

		Iterator<Element> eIter = elements.iterator();
		while (eIter.hasNext()) {
			Element element = eIter.next();

			String content = "";
			String hl = "";

			// Check for segmentcounter
			try {
				@SuppressWarnings("unused")
				Node segmentCounter = ((Element) element).getElementsByTagName(
						"segmentcounter").item(0);
				if (segmentCounter != null) {
					log
							.info("Found segmentcounter element, using that for content at index "
									+ segmentCount);
					l.add(new Integer(segmentCount - 2).toString());
					continue;
				}
			} catch (Exception ex) {
				if (ex.toString().length() > 1) {
				}
				log.debug("No segmentcounter element, moving on to next check");
			}

			// Check for "hl" element, which is a counter
			// TODO: make this stuff actually work
			try {
				hl = ((Element) element).getElementsByTagName("hl").item(0)
						.getTextContent();
			} catch (Exception ex) {
				if (ex.toString().length() > 1) {
				}
				hl = "";
			}

			// Process content tag
			try {
				content = ((Element) element).getElementsByTagName("content")
						.item(0).getTextContent();
			} catch (Exception ex) {
				if (ex.toString().length() > 1) {
				}
				content = "";
			}
			// If there's no content tag content, try text attribute
			if (content.length() == 0) {
				try {
					content = ((Element) element).getElementsByTagName(
							"content").item(0).getAttributes().getNamedItem(
							"text").getTextContent();
				} catch (Exception ex) {
					if (ex.toString().length() > 1) {
					}
					content = "";
				}
			}
			l.add(content);
		}

		StringBuilder sb = new StringBuilder();

		// Start segment with segment id
		sb.append(segmentId);
		sb.append(elementEnd);

		// Iterate through all elements
		int elementCount = l.size();
		for (int iter = 0; iter < elementCount; iter++) {
			sb.append(l.get(iter));
			if (iter < (elementCount - 1)) {
				sb.append(elementEnd);
			}
		}
		sb.append(segmentEnd);
		return sb.toString();
	}

	@Override
	public String[] getPluginConfigurationOptions() {
		return null;
	}

}
