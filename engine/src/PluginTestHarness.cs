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
using System.IO;
using System.Text;

using log4net;
using log4net.Config;

namespace Remitt
{

	public class PluginTestHarness {

		private static readonly log4net.ILog log = log4net.LogManager.GetLogger( System.Reflection.MethodBase.GetCurrentMethod().DeclaringType );

		public static int Main ( string[] argv ) {
			if ( argv.Length < 3 ) {
				Console.WriteLine( "syntax: PluginTestHarness.exe plugintype pluginname inputfile [option]" );
				return 1;
			}

			// Set up log4net logging
			BasicConfigurator.Configure();

			// Create PluginManager instance
			PluginManager p = new PluginManager ( );
			PluginOption[] po = PluginManager.ReadXslOptions( );
			log.Debug( po.Length.ToString() + " xsl options found" );
			PluginConfiguration[] pc = p.GetPlugins( );
			log.Debug( pc.Length.ToString() + " plugins found" );

			// Read input file
			log.Debug( String.Format( "Reading {0} from file", argv[2] ) );
			StreamReader sr = new StreamReader ( argv[2] );
			string FileContents = sr.ReadToEnd( );

			if ( argv[0].Equals( "Render" ) ) {
				log.Debug( "Executing Render" );
				Console.WriteLine( p.ExecuteRenderPlugin( argv[1], FileContents, argv[3], 0 ) );
			}

			if ( argv[0].Equals( "Translation" ) ) {
				log.Debug( "Executing Translation" );
				Console.WriteLine( p.ExecuteTranslationPlugin( argv[1], FileContents, 0 ) );
			}

			if ( argv[0].Equals( "Transmission" ) ) {

			}

			return 0;
		}

	}

}

