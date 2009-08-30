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
			sb
					.append(TranslateSegmentFromNode(nodeList.item(iter), eEnd,
							sEnd));
			sb.append("\n");
		}

		return sb.toString();
	}

	private String TranslateSegmentFromNode(Node segment, String elementEnd,
			String segmentEnd) {
		List<String> l = new ArrayList<String>();

		// Get elements ...
		NodeList elementNodes = ((Element) segment)
				.getElementsByTagName("element");

		// Perform sorting by creating list
		ArrayList<Element> elements = new ArrayList<Element>();
		for (int iter = 0; iter < elementNodes.getLength(); iter++) {
		}

		Iterator<Element> eIter = elements.iterator();
		while (eIter.hasNext()) {
			Element element = eIter.next();

			String content = "";

			try {
				content = ((Element) element).getElementsByTagName("content")
						.item(0).getTextContent();
			} catch (Exception ex) {
				if (ex.toString().length() > 1) {
				}
				content = "";
			}
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

	/**
	 * Pad a string from one page position to another.
	 * 
	 * @param oldRow
	 *            Originating row
	 * @param oldColumn
	 *            Originating column
	 * @param newRow
	 *            Desired destination row
	 * @param newColumn
	 *            Desired destination column
	 * @return
	 */
	protected String padToPosition(int oldRow, int oldColumn, int newRow,
			int newColumn) {
		// Sanity checks
		if (oldRow > newRow) {
			log.debug("oldRow = " + oldRow + ", newRow = " + newRow);
			return "";
		}
		if ((oldRow == newRow) && (oldColumn > newColumn)) {
			log.debug("oldRow = " + oldRow + ", newRow = " + newRow + " ( "
					+ oldColumn + " > " + newColumn + " )");
			return "";
		}
		if ((oldRow == newRow) && (oldColumn == newColumn)) {
			return "";
		}

		StringBuilder sb = new StringBuilder();
		int currentRow = oldRow;
		int currentColumn = oldColumn;
		while (currentRow < newRow) {
			sb.append("\r\n");
			currentRow += 1;
			currentColumn = 1;
		}
		while (currentColumn < newColumn) {
			sb.append(" ");
			currentColumn += 1;
		}
		return sb.toString();
	}

	@Override
	public String[] getPluginConfigurationOptions() {
		return null;
	}

}
