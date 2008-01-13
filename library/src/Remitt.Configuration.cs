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
using System.Collections;
using System.Collections.Specialized;
using System.ComponentModel;
using System.Data;
using System.Reflection;
using System.Text;
using System.Xml.Serialization;

using log4net;

namespace Remitt {

	[Serializable]
	public class RemittSettings {
		private string f_install = "";
		public string InstallLocation {
			get { return f_install; }
			set { f_install = value; }
		}

		private string f_dbhost = "";
		public string DbHost {
			get { return f_dbhost; }
			set { f_dbhost = value; }
		}

		private string f_dbusername = "";
		public string DbUsername {
			get { return f_dbusername; }
			set { f_dbusername = value; }
		}

		private string f_dbpassword = "";
		public string DbPassword {
			get { return f_dbpassword; }
			set { f_dbpassword = value; }
		}

		private string f_dbname = "";
		public string DbName {
			get { return f_dbname; }
			set { f_dbname = value; }
		}

		private int f_executethreads = 2;
		public int ExecuteThreads {
			get { return f_executethreads; }
			set { f_executethreads = value; }
		}
	}

	public class RemittConfiguration {

		private static readonly log4net.ILog log = log4net.LogManager.GetLogger( System.Reflection.MethodBase.GetCurrentMethod().DeclaringType );

		private Remitt.RemittSettings Settings;

		public RemittConfiguration ( ) {
			string[] ConfigLocations = {
				  "./"
				, "../"
				, "../../"
				, "C:\\Program Files\\Remitt\\"
	 			, "C:\\Remitt\\"
				, "/etc/"
				, "/usr/local/etc/"
			};

 			// Determine settings location
 			string settingsFile = "";
			foreach ( string ThisLocation in ConfigLocations ) {
				if ( settingsFile.Equals( "" ) ) {
					if ( File.Exists ( ThisLocation + "Remitt.xml" ) ) {
						settingsFile = ThisLocation + "Remitt.xml";
					} else if ( File.Exists ( ThisLocation + "remitt.xml" ) ) {
						settingsFile = ThisLocation + "remitt.xml";
					}
				}
			}

	 		if ( settingsFile.Equals ( "" ) ) {
				throw new Exception ("Could not load settings.");
			}
			log.Debug( "Loading settings from " + settingsFile );

			// Read settings from XML file
			XmlSerializer formatter = new XmlSerializer( typeof( RemittSettings ) );
			log.Debug( "XmlSerializer created" );
			Stream s = new FileStream( settingsFile, FileMode.Open, FileAccess.Read, FileShare.None );
			log.Debug( "FileStream created" );
			this.Settings = null;
			try {
				this.Settings = (RemittSettings) formatter.Deserialize( s );
			} finally {
				s.Close( );
			}
			log.Debug( "Loaded settings from " + settingsFile );
		}

		public RemittSettings GetSettings ( ) {
			return this.Settings;
		}

	}

}
