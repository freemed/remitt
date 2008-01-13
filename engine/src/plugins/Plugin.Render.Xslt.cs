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
using System.Xml;
using System.Xml.Xsl;
using System.IO;

using GotDotNet.Exslt;
using log4net;

namespace Remitt {

	public class Plugin_Render_Xslt {

		private static readonly log4net.ILog log = log4net.LogManager.GetLogger( System.Reflection.MethodBase.GetCurrentMethod().DeclaringType );

		public static PluginConfiguration Config ( ) {
			PluginConfiguration c = new PluginConfiguration ( );
			c.PluginName = "Xslt";
			c.Version = "0.1";
			c.InputFormat = "remittxml";
			c.OutputFormat = "variable";
			return c;
		}

		public static string Render ( int JobId, string Input, string Option ) {
			log.Info( "Entered Render for job #" + JobId.ToString() );

			RemittConfiguration Configuration = new RemittConfiguration ( );
			RemittSettings settings = Configuration.GetSettings();

			// Attempt to load input stream
			XmlDocument xmlInput = new XmlDocument ( );
			log.Debug( "Loading input into XmlDocument" );
			xmlInput.LoadXml( Input );

			// Create XSL Transformation
			ExsltTransform xsl = new ExsltTransform();
			String xsl_fq = String.Format( "{0}/xsl/{1}.xsl", settings.InstallLocation, Option );
			log.Debug( "Loading " + xsl_fq );
			xsl.Load( xsl_fq );

			log.Debug( "Performing transformation" );
			MemoryStream streamOutput = new MemoryStream ( );
			xsl.Transform( xmlInput, null, streamOutput );

			// Push stream to output
			streamOutput.Position = 1;
			StreamReader reader = new StreamReader( streamOutput );
			return reader.ReadToEnd( );
		}

	}

}

