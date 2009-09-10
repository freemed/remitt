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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.remitt.prototype.PluginInterface;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class FixedFormXml implements PluginInterface {

	static final Logger log = Logger.getLogger(FixedFormXml.class);

	protected XPath xpath = null;

	protected String defaultUsername = "";

	@Override
	public String getInputFormat() {
		return "fixedformxml";
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
		return "FixedFormXml";
	}

	@Override
	public Double getPluginVersion() {
		return 0.1;
	}

	@Override
	public byte[] render(Integer jobId, String input, String option)
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
		NodeList nodeList = (NodeList) xpath.evaluate("/fixedform/page", root,
				XPathConstants.NODESET);

		// Loop through all page elements
		for (int iter = 0; iter < nodeList.getLength(); iter++) {
			sb.append(TranslatePageFromNode(nodeList.item(iter)));
		}

		return sb.toString().getBytes();
	}

	private String TranslatePageFromNode(Node page) {
		int currentRow = 1;
		int currentCol = 1;

		StringBuilder sb = new StringBuilder();

		// Get elements ...
		NodeList elementNodes = ((Element) page)
				.getElementsByTagName("element");

		// Perform sorting by creating list
		ArrayList<Element> elements = new ArrayList<Element>();
		for (int iter = 0; iter < elementNodes.getLength(); iter++) {
			elements.add((Element) elementNodes.item(iter));
		}
		// ... then doing sort using delegates
		Collections.sort(elements, new Comparator<Element>() {
			@Override
			public int compare(Element elementA, Element elementB) {
				// -1 implies A < B
				// 0 implies A = B
				// +1 implies A > B

				int rowA = 0;
				int rowB = 0;
				int colA = 0;
				int colB = 0;
				// --------------------- ElementA -------------------
				try {
					rowA = Integer.parseInt(elementA
							.getElementsByTagName("row").item(0)
							.getTextContent());
				} catch (Exception ex) {
					log.trace(ex.toString());
					rowA = 0;
				}

				try {
					colA = Integer.parseInt(elementA.getElementsByTagName(
							"column").item(0).getTextContent());
				} catch (Exception ex) {
					log.trace(ex.toString());
					colA = 0;
				}

				// --------------------- ElementB -------------------
				try {
					rowB = Integer.parseInt(elementA
							.getElementsByTagName("row").item(0)
							.getTextContent());
				} catch (Exception ex) {
					log.trace(ex.toString());
					rowB = 0;
				}

				try {
					colB = Integer.parseInt(elementA.getElementsByTagName(
							"column").item(0).getTextContent());
				} catch (Exception ex) {
					log.trace(ex.toString());
					colB = 0;
				}

				// Compare ...
				if (rowA < rowB) {
					return -1;
				}
				if (rowA > rowB) {
					return 1;
				}
				if ((rowA == rowB) && (colA < colB)) {
					return -1;
				}
				if ((rowA == rowB) && (colA > colB)) {
					return 1;
				}
				return 0;
			}
		});

		Iterator<Element> eIter = elements.iterator();
		while (eIter.hasNext()) {
			Element element = eIter.next();

			int elementRow;
			int elementColumn;

			try {
				elementRow = Integer.parseInt(((Element) element)
						.getElementsByTagName("row").item(0).getTextContent());
			} catch (Exception ex) {
				if (ex.toString().length() > 1) {
				}
				elementRow = 0;
			}

			try {
				elementColumn = Integer.parseInt(((Element) element)
						.getElementsByTagName("column").item(0)
						.getTextContent());
			} catch (Exception ex) {
				if (ex.toString().length() > 1) {
				}
				elementColumn = 0;
			}

			if ((elementRow > 0) && (elementColumn > 0)) {
				sb.append(padToPosition(currentRow, currentCol, elementRow,
						elementColumn));
				currentRow = elementRow;
				currentCol = elementColumn;
				String elementContents = processElement(element);
				sb.append(elementContents);
				currentCol += elementContents.length();
			} else {
				log.debug("Found null element, skipping.");
			}
		}
		// Divine page length from /fixedform/page/format/pagelength element
		int pageLength = 0;
		try {
			pageLength = Integer.parseInt(((Element) ((Element) page)
					.getElementsByTagName("format").item(0))
					.getElementsByTagName("pagelength").item(0)
					.getTextContent());
		} catch (Exception ex) {
			if (ex.toString().length() > 1) {
			}
			pageLength = 0;
		}

		// Move cursor to the end of the page
		while (currentRow < pageLength) {
			sb.append("\r\n");
			currentRow += 1;
			currentCol = 1;
		}

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

	private String processElement(Node element) {
		String elementContent = null;
		int elementLength = 0;
		try {
			elementLength = Integer.parseInt(((Element) element)
					.getElementsByTagName("length").item(0).getTextContent());
		} catch (Exception ex) {
			if (ex.toString().length() > 1) {
			}
			elementLength = 0;
		}

		// Make sure null is sent back if there's no length to be had here
		if (elementLength == 0) {
			return "";
		}

		try {
			elementContent = ((Element) element)
					.getElementsByTagName("content").item(0).getTextContent();
		} catch (Exception ex) {
			if (ex.toString().length() > 1) {
			}
			elementContent = "";
		}

		// Chop string if it's too long
		if (elementContent.length() > elementLength) {
			return elementContent.substring(0, elementLength);
		}

		// Block to check for right alignment
		try {
			String alignment = ((Element) element).getElementsByTagName(
					"format").item(0).getAttributes().getNamedItem("right")
					.getTextContent();
			if (Integer.parseInt(alignment) == 1) {
				return StringUtils.leftPad(elementContent, elementLength);
			}
		} catch (Exception ex) {
			if (ex.toString().length() > 1) {
			}
		}

		// If formatting doesn't dictate otherwise, return padded output
		return StringUtils.rightPad(elementContent, elementLength);
	}

	@Override
	public String[] getPluginConfigurationOptions() {
		return null;
	}

	@Override
	public void setDefaultUsername(String username) {
		defaultUsername = username;
	}

}
