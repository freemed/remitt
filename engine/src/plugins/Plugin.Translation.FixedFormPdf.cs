/*
 * $Id$
 *
 * Authors:
 *	Jeff Buchbinder <jeff@freemedsoftware.org>
 *
 * REMITT Electronic Medical Information Translation and Transmission
 * Copyright (C) 1999-2008 FreeMED Software Foundation
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

using System;
using System.Collections;
using System.Xml;
using System.Xml.Xsl;
using System.IO;
using System.Text;

using iTextSharp.text;
using iTextSharp.text.pdf;

using log4net;

namespace Remitt {

	public class Plugin_Translation_FixedFormPDF {

		private static readonly log4net.ILog log = log4net.LogManager.GetLogger( System.Reflection.MethodBase.GetCurrentMethod().DeclaringType );

		private static RemittSettings settings;

		public static PluginConfiguration Config ( ) {
			PluginConfiguration c = new PluginConfiguration ( );
			c.PluginName = "FixedFormPdf";
			c.Version = "0.1";
			c.InputFormat = "fixedformxml";
			c.OutputFormat = "pdf";
			return c;
		}

		public static string Translate ( int JobId, string Input ) {
			log.Info( "Entered Translate for job #" + JobId.ToString() );

			Document newDocument;

			RemittConfiguration Configuration = new RemittConfiguration ( );
			settings = Configuration.GetSettings();

			// Attempt to load input stream
			XmlDocument xmlInput = new XmlDocument ( );
			log.Debug( "Loading input into XmlDocument" );
			xmlInput.LoadXml( Input );

			XmlNode root = xmlInput.DocumentElement;
			XmlNodeList nodeList = root.SelectNodes( "/fixedform/page" );

			// Create PDF document. If template, then let's use first page.
			String pdfTemplate = "";
			int pdfPageNumber = 1;
			try {
				pdfTemplate = root.SelectSingleNode( "/fixedform/page/format/pdf/@template" ).InnerText;
				string pdfPageNumberString = root.SelectSingleNode( "/fixedform/page/format/pdf/@page" ).InnerText;
				pdfPageNumber = Int32.Parse( pdfPageNumberString );
			} catch ( NullReferenceException ex ) {
				if ( ex.ToString().Length > 1 ) { }
				pdfTemplate = "";
				pdfPageNumber = 1;
			}

			if ( pdfTemplate.Equals( "" ) ) {
				log.Debug( "No PDF template found in XML stream, defaulting to LETTER size" );
				newDocument = new Document( PageSize.LETTER );
			} else {
				String templateFqdn = String.Format( "{0}/pdf/{1}.pdf", settings.InstallLocation, pdfTemplate );
				PdfReader srcDocument = new PdfReader( templateFqdn );
				Rectangle srcPageRectangle = srcDocument.GetPageSizeWithRotation( pdfPageNumber );
				newDocument = new Document( srcPageRectangle, 0, 0, 0, 0 );
			}

			PdfWriter writer = PdfWriter.GetInstance( newDocument, new FileStream( "test.pdf", FileMode.Create ) );
			newDocument.Open( );

			// Loop through all page elements
			int pageCount = 0;
			foreach ( XmlNode page in nodeList ) {
				if ( pageCount > 0 ) {
					// Skip to next page
					newDocument.NewPage( );
				}

				// Append page translation
				TranslatePageFromNode ( newDocument, writer, page );

				// Increment page counter
				pageCount += 1;
			}

			// Close document
			newDocument.Close( );
			return "";
		}

		private static void TranslatePageFromNode ( Document doc, PdfWriter writer, XmlNode page ) {
			// Grab template if necessary 
			String pdfTemplate = "";
			int pdfPageNumber = 1;
			try {
				pdfTemplate = page.SelectSingleNode( "./format/pdf/@template" ).InnerText;
				string pdfPageNumberString = page.SelectSingleNode( "./format/pdf/@page" ).InnerText;
				pdfPageNumber = Int32.Parse( pdfPageNumberString );
			} catch ( NullReferenceException ex ) {
				if ( ex.ToString().Length > 1 ) { }
				pdfTemplate = "";
				pdfPageNumber = 1;
			}

			// -------- Calculate offsets --------

			int vOffset;
			try {
				vOffset = Int32.Parse( page.SelectSingleNode( "./format/pdf/offset/@vertical" ).InnerText );
			} catch ( NullReferenceException ex ) {
				if ( ex.ToString().Length > 1 ) { }
				vOffset = 0;
			}

			int hOffset;
			try {
				hOffset = Int32.Parse( page.SelectSingleNode( "./format/pdf/offset/@horizontal" ).InnerText );
			} catch ( NullReferenceException ex ) {
				if ( ex.ToString().Length > 1 ) { }
				hOffset = 0;
			}

			// -------- Calculate scaling --------

			double vScaling;
			try {
				vScaling = Double.Parse( page.SelectSingleNode( "./format/pdf/scaling/@vertical" ).InnerText );
			} catch ( NullReferenceException ex ) {
				if ( ex.ToString().Length > 1 ) { }
				vScaling = 0;
			}

			double hScaling;
			try {
				hScaling = Double.Parse( page.SelectSingleNode( "./format/pdf/scaling/@horizontal" ).InnerText );
			} catch ( NullReferenceException ex ) {
				if ( ex.ToString().Length > 1 ) { }
				hScaling = 0;
			}

			// Prep ContentByte
			PdfContentByte cb = writer.DirectContent;

			int vSize = 792;
			if ( pdfTemplate.Equals( "" ) ) {
				vSize = 792;
			} else {
				// Use template
				try {
					String templateFqdn = String.Format( "{0}/pdf/{1}.pdf", settings.InstallLocation, pdfTemplate );
					PdfReader srcDocument = new PdfReader( templateFqdn );
					Rectangle srcPageRectangle = srcDocument.GetPageSizeWithRotation( pdfPageNumber );
					vSize = (int) srcPageRectangle.Height;
					log.Debug( "vertical size = " + vSize.ToString() );
					PdfImportedPage tp = writer.GetImportedPage( srcDocument, pdfPageNumber );
					cb.AddTemplate( tp, 0, 0 );
				} catch ( NullReferenceException ex ) {
					if ( ex.ToString().Length > 1 ) { }
					vSize = 792;
				}
			}

			// Begin text
			cb.BeginText( );
			BaseFont bf = BaseFont.CreateFont(
					  BaseFont.COURIER
					, BaseFont.CP1252
					, BaseFont.EMBEDDED
				);
			cb.SetFontAndSize( bf, 10 ); // FIXME

			// Get elements ...
			XmlNodeList elementNodes = page.SelectNodes( "./element" );
			foreach ( XmlNode element in elementNodes ) {
				int ElementRow;
				int ElementColumn;

				try {
					string StringElementRow = element.SelectSingleNode( "./row" ).InnerText;
					ElementRow = Int32.Parse( StringElementRow );
				} catch ( NullReferenceException ex ) {
					if ( ex.ToString().Length > 1 ) { }
					ElementRow = 0;
				}

				try {
					string StringElementColumn = element.SelectSingleNode( "./column" ).InnerText;
					ElementColumn = Int32.Parse( StringElementColumn );
				} catch ( NullReferenceException ex ) {
					if ( ex.ToString().Length > 1 ) { }
					ElementColumn = 0;
				}

				if ( ( ElementRow > 0 ) && ( ElementColumn > 0 ) ) {
					// Calculate positions
					int colPos = (int)(( ElementColumn * hScaling ) + hOffset);
					int rowPos = (int)(( vSize - ( ElementRow * vScaling ) ) - vOffset);

					// Calculate effective element contents
					string ElementContents = ProcessElement ( element );

					// Reposition and display
					cb.SetTextMatrix( colPos, rowPos );
					cb.ShowText( ElementContents );
				} else {
					log.Debug( "Found null element, skipping." );
				}

			}

			// Close out text block on page
			cb.EndText( );
		}

		private static string ProcessElement ( XmlNode element ) {
			string ElementContent;
			int ElementLength;
			try {
				string ElementLengthString = element.SelectSingleNode( "./length" ).InnerText;
				ElementLength = Int32.Parse( ElementLengthString );
			} catch ( NullReferenceException ex ) {
				if ( ex.ToString().Length > 1 ) { }
				ElementLength = 0;
			}

			// Make sure null is sent back if there's no length to be had here
			if ( ElementLength == 0 ) {
				return "";
			}

			try {
				ElementContent = element.SelectSingleNode( "./content" ).InnerText;
			} catch ( NullReferenceException ex ) {
				if ( ex.ToString().Length > 1 ) { }
				ElementContent = "";
			}

			// Chop string if it's too long
			if ( ElementContent.Length > ElementLength ) {
				return ElementContent.Substring( 0, ElementLength );
			}

			// Block to check for right alignment
			try {
				string Alignment = element.SelectSingleNode( "./format/@right" ).InnerText;
				if ( Int32.Parse( Alignment ) == 1 ) {
					return ElementContent.PadLeft( ElementLength );
				}
			} catch ( NullReferenceException ex ) {
				if ( ex.ToString().Length > 1 ) { }
			}

			// If formatting doesn't dictate otherwise, return padded output
			return ElementContent.PadRight( ElementLength );
		}

	}

}

