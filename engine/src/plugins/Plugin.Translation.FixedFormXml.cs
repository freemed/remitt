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
using System.Reflection;
using System.Xml;
using System.Xml.Xsl;
using System.IO;
using System.Text;

using log4net;

[assembly: AssemblyVersion ("0.0.*")]
namespace Remitt {

	public class Plugin_Translation_FixedFormXML {

		private static readonly log4net.ILog log = log4net.LogManager.GetLogger( System.Reflection.MethodBase.GetCurrentMethod().DeclaringType );

		public static PluginConfiguration Config ( ) {
			PluginConfiguration c = new PluginConfiguration ( );
			c.PluginName = "FixedFormXml";
			c.Version = "0.1";
			c.InputFormat = "fixedformxml";
			c.OutputFormat = "text";
			return c;
		}

		public static string Translate ( int JobId, string Input ) {
			log.Info( "Entered Translate for job #" + JobId.ToString() );

			StringBuilder sb = new StringBuilder ( );

			// Attempt to load input stream
			XmlDocument xmlInput = new XmlDocument ( );
			log.Debug( "Loading input into XmlDocument" );
			xmlInput.LoadXml( Input );

			XmlNode root = xmlInput.DocumentElement;
			XmlNodeList nodeList = root.SelectNodes( "/fixedform/page" );
			// Loop through all page elements
			foreach ( XmlNode page in nodeList ) {
				// Append page translation
				sb.Append( TranslatePageFromNode ( page ) );
			}
			return sb.ToString ( );
		}

		private static string TranslatePageFromNode ( XmlNode page ) {
			int CurrentRow = 1;
			int CurrentCol = 1;

			StringBuilder sb = new StringBuilder ( );

			// Get elements ...
			XmlNodeList elementNodes = page.SelectNodes( "./element" );

			// Perform sorting by creating list
			ArrayList elements = new ArrayList ( );
			foreach ( XmlNode e in elementNodes ) {
				elements.Add( e );
			}
			// ... then doing sort using delegates
			elements.Sort( new ElementComparer ( ) );

			foreach ( XmlNode element in elements ) {
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
					sb.Append( PadToPosition ( CurrentRow, CurrentCol, ElementRow, ElementColumn ) );
					CurrentRow = ElementRow; CurrentCol = ElementColumn;
					string ElementContents = ProcessElement ( element );
					sb.Append( ElementContents );
					CurrentCol += ElementContents.Length;
				} else {
					log.Debug( "Found null element, skipping." );
				}

			}

			// Divine page length from /fixedform/page/format/pagelength element
			int PageLength = 0;
			try {
				string PageLengthString = page.SelectSingleNode( "./format/pagelength" ).InnerText;
				PageLength = Int32.Parse( PageLengthString );
			} catch ( NullReferenceException ex ) {
				if ( ex.ToString().Length > 1 ) { }
				PageLength = 0;
			}

			// Move cursor to the end of the page
			while ( CurrentRow < PageLength ) {
				sb.Append( "\x0d\x0a" );
				CurrentRow += 1; CurrentCol = 1;
			}

			return sb.ToString ( );
		}

		private static string PadToPosition ( int OldRow, int OldColumn, int NewRow, int NewColumn ) {
			// Sanity checks
			if ( OldRow > NewRow ) {
				log.Debug( String.Format( "OldRow = {0}, NewRow = {1}", OldRow, NewRow ) );
				return "";
			}
			if ( ( OldRow == NewRow ) && ( OldColumn > NewColumn ) ) {
				log.Debug( String.Format( "OldRow = {0}, NewRow = {1} ( {2} > {3} )", OldRow, NewRow, OldColumn, NewColumn ) );
				return "";
			}
			if ( ( OldRow == NewRow ) && ( OldColumn == NewColumn ) ) {
				return "";
			}

			StringBuilder sb = new StringBuilder ( );
			int CurrentRow = OldRow; int CurrentColumn = OldColumn;
			while ( CurrentRow < NewRow ) {
				sb.Append( "\x0d\x0a" );
				CurrentRow += 1; CurrentColumn = 1;
			}
			while ( CurrentColumn < NewColumn ) {
				sb.Append( " " );
				CurrentColumn += 1;
			}
			return sb.ToString ( );
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

		private class ElementComparer: IComparer {
			public int Compare( object elementA_Object, object elementB_Object ) {
				// Recast parameters
				XmlNode elementA = (XmlNode) elementA_Object;
				XmlNode elementB = (XmlNode) elementB_Object;

				// -1 implies A < B
				// 0  implies A = B
				// +1 implies A > B

				int rowA; int rowB;
				int colA; int colB;					
				//--------------------- ElementA -------------------
				try {
					string rowA_String = elementA.SelectSingleNode( "./row" ).InnerText;
					rowA = Int32.Parse( rowA_String );
				} catch ( NullReferenceException ex ) {
					if ( ex.ToString().Length > 1 ) { }
					rowA = 0;
				}

				try {
					string colA_String = elementA.SelectSingleNode( "./column" ).InnerText;
					colA = Int32.Parse( colA_String );
				} catch ( NullReferenceException ex ) {
					if ( ex.ToString().Length > 1 ) { }
					colA = 0;
				}

				//--------------------- ElementB -------------------
				try {
					string rowB_String = elementB.SelectSingleNode( "./row" ).InnerText;
					rowB = Int32.Parse( rowB_String );
				} catch ( NullReferenceException ex ) {
					if ( ex.ToString().Length > 1 ) { }
					rowB = 0;
				}

				try {
					string colB_String = elementB.SelectSingleNode( "./column" ).InnerText;
					colB = Int32.Parse( colB_String );
				} catch ( NullReferenceException ex ) {
					if ( ex.ToString().Length > 1 ) { }
					colB = 0;
				}

				// Compare ...
				if ( rowA < rowB ) { return -1; }
				if ( rowA > rowB ) { return  1; }
				if ( ( rowA == rowB ) && ( colA < colB ) ) { return -1; }
				if ( ( rowA == rowB ) && ( colA > colB ) ) { return  1; }
				return 0;
			}
		}

	}

}

