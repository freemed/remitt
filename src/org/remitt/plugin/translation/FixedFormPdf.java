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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.remitt.prototype.PluginInterface;
import org.remitt.server.Configuration;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;

public class FixedFormPdf implements PluginInterface {

	static final Logger log = Logger.getLogger(FixedFormPdf.class);

	XPath xpath = null;

	@Override
	public String getInputFormat() {
		return "fixedformxml";
	}

	@Override
	public HashMap<String, String> getOptions() {
		return null;
	}

	@Override
	public String getOutputFormat() {
		return "pdf";
	}

	@Override
	public String getPluginName() {
		return "FixedFormPdf";
	}

	@Override
	public Double getPluginVersion() {
		return 0.1;
	}

	@Override
	public String render(Integer jobId, String input, String option)
			throws Exception {
		log.info("Entered Translate for job #" + jobId.toString());

		Document newDocument = null;

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

		// Create PDF document. If template, then let's use first page.
		String pdfTemplate = "";
		int pdfPageNumber = 1;
		try {
			pdfTemplate = xpath.evaluate(
					"/fixedform/page/format/pdf/@template", root);
			pdfPageNumber = Integer.parseInt(xpath.evaluate(
					"/fixedform/page/format/pdf/@page", root));
		} catch (Exception ex) {
			log.trace(ex.toString());
			pdfTemplate = "";
			pdfPageNumber = 1;
		}

		if (pdfTemplate.length() == 0) {
			log
					.debug("No PDF template found in XML stream, defaulting to LETTER size");
			newDocument = new Document(PageSize.LETTER);
		} else {
			String templateFqdn = Configuration.getServletContext()
					.getServletContext().getRealPath(
							"/WEB-INF/pdf/" + pdfTemplate + ".pdf");
			log.debug("Found template path = " + templateFqdn);
			PdfReader srcDocument = new PdfReader(templateFqdn);
			Rectangle srcPageRectangle = srcDocument
					.getPageSizeWithRotation(pdfPageNumber);
			newDocument = new Document(srcPageRectangle, 0, 0, 0, 0);
		}

		PdfWriter writer = PdfWriter.getInstance(newDocument,
				new ByteArrayOutputStream());
		newDocument.open();

		// Loop through all page elements
		int pageCount = 0;
		for (int iter = 0; iter < nodeList.getLength(); iter++) {
			Node page = nodeList.item(iter);
			if (pageCount > 0) {
				// Skip to next page
				newDocument.newPage();
			}

			// Append page translation
			translatePageFromNode(newDocument, writer, page);

			// Increment page counter
			pageCount += 1;
		}

		// Close document
		newDocument.close();
		return "";
	}

	private void translatePageFromNode(Document doc, PdfWriter writer, Node page) {
		// Grab template if necessary
		String pdfTemplate = "";
		int pdfPageNumber = 1;
		try {
			pdfTemplate = xpath.evaluate("./format/pdf/@template", page);
			pdfPageNumber = Integer.parseInt(xpath.evaluate(
					"./format/pdf/@page", page));
		} catch (Exception ex) {
			log.trace(ex.toString());
			pdfTemplate = "";
			pdfPageNumber = 1;
		}

		// -------- Calculate offsets --------

		int vOffset;
		try {
			vOffset = Integer.parseInt(xpath.evaluate(
					"./format/pdf/offset/@vertical", page));
		} catch (Exception ex) {
			log.trace(ex.toString());
			vOffset = 0;
		}

		int hOffset;
		try {
			hOffset = Integer.parseInt(xpath.evaluate(
					"./format/pdf/offset/@horizontal", page));
		} catch (Exception ex) {
			log.trace(ex.toString());
			hOffset = 0;
		}

		double vScaling;
		try {
			vScaling = Double.parseDouble(xpath.evaluate(
					"./format/pdf/scaling/@vertical", page));
		} catch (Exception ex) {
			log.trace(ex.toString());
			vScaling = 0;
		}

		double hScaling;
		try {
			hScaling = Double.parseDouble(xpath.evaluate(
					"./format/pdf/scaling/@horizontal", page));
		} catch (Exception ex) {
			log.trace(ex.toString());
			hScaling = 0;
		}

		// Prep ContentByte
		PdfContentByte cb = writer.getDirectContent();

		int vSize = 792;
		if (pdfTemplate.length() == 0) {
			vSize = 792;
		} else {
			// Use template
			try {
				String templateFqdn = Configuration.getInstallLocation()
						+ "/pdf/" + pdfTemplate + ".pdf";
				PdfReader srcDocument = new PdfReader(templateFqdn);
				Rectangle srcPageRectangle = srcDocument
						.getPageSizeWithRotation(pdfPageNumber);
				vSize = (int) srcPageRectangle.getHeight();
				log.debug("vertical size = " + vSize);
				PdfImportedPage tp = writer.getImportedPage(srcDocument,
						pdfPageNumber);
				cb.addTemplate(tp, 0, 0);
			} catch (Exception ex) {
				log.trace(ex.toString());
				vSize = 792;
			}
		}

		// Begin text
		cb.beginText();
		BaseFont bf = null;
		try {
			bf = BaseFont.createFont(BaseFont.COURIER, BaseFont.CP1252,
					BaseFont.EMBEDDED);
		} catch (DocumentException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		cb.setFontAndSize(bf, 10); // FIXME

		// Get elements ...
		NodeList elementNodes = null;
		try {
			elementNodes = (NodeList) xpath.evaluate("./element", page,
					XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			log.error("Found no elements " + e.toString());
		}
		for (int jIter = 0; jIter < elementNodes.getLength(); jIter++) {
			Node element = elementNodes.item(jIter);

			int elementRow;
			int elementColumn;

			try {
				elementRow = Integer.parseInt(xpath.evaluate("./row", element));
			} catch (Exception ex) {
				log.trace(ex.toString());
				elementRow = 0;
			}

			try {
				elementColumn = Integer.parseInt(xpath.evaluate("./column",
						element));
			} catch (Exception ex) {
				log.trace(ex.toString());
				elementColumn = 0;
			}

			if ((elementRow > 0) && (elementColumn > 0)) {
				// Calculate positions
				int colPos = (int) ((elementColumn * hScaling) + hOffset);
				int rowPos = (int) ((vSize - (elementRow * vScaling)) - vOffset);

				// Calculate effective element contents
				String elementContents = ProcessElement(element);

				// Reposition and display
				cb.setTextMatrix(colPos, rowPos);
				cb.showText(elementContents);
			} else {
				log.debug("Found null element, skipping.");
			}

		}

		// Close out text block on page
		cb.endText();
	}

	private String ProcessElement(Node element) {
		String elementContent;
		Integer elementLength;
		try {
			elementLength = Integer.parseInt(((Element) element)
					.getElementsByTagName("length").item(0).getTextContent());
		} catch (Exception ex) {
			log.trace(ex.toString());
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
			log.trace(ex.toString());
			elementContent = "";
		}

		// Chop string if it's too long
		if (elementContent.length() > elementLength) {
			return elementContent.substring(0, elementLength);
		}

		// Block to check for right alignment
		try {
			Integer alignment = Integer.parseInt(((Element) ((Element) element)
					.getElementsByTagName("format").item(0))
					.getAttribute("right"));
			if (alignment == 1) {
				return StringUtils.leftPad(elementContent, elementLength);
			}
		} catch (Exception ex) {
			log.trace(ex.toString());
		}

		// If formatting doesn't dictate otherwise, return padded output
		return StringUtils.rightPad(elementContent, elementLength);
	}

	@Override
	public String[] getPluginConfigurationOptions() {
		return null;
	}

}
