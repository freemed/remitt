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
using System.Reflection;
using System.Text.RegularExpressions;
using System.Xml.Serialization;

using log4net;

[assembly: AssemblyVersion ("0.0.*")]
namespace Remitt {

	public class PluginConfiguration {
		public string		PluginName;
		public string		PluginFile;
		public string		Version;
		public object[]		Options;
		public string		InputFormat;
		public string		OutputFormat;

		public override string ToString ( ) {
			int size = 0;
			try {
				size = this.Options.Length;
			} catch ( NullReferenceException ex ) {
				if ( ex.ToString().Length > 1 ) { }
			}
			return String.Format( "\n" +
				"PluginFile = {0}\n" +
				"\tPluginName = {1}\n" +
				"\tVersion = {2}\n" +
				"\tOptions = {3}\n" +
				"\tInputFormat = {4}\n" +
				"\tOutputFormat = {5}\n"
				, this.PluginFile
				, this.PluginName
				, this.Version
				, size.ToString()
				, this.InputFormat
				, this.OutputFormat
			);
		}
	}

	[Serializable]
	public class PluginOption {
		private string f_pluginfile = "";
		public string PluginFile {
			get { return f_pluginfile; }
			set { f_pluginfile = value; }
		}

		private string f_description = "";
		public string Description {
			get { return f_description; }
			set { f_description = value; }
		}

		private string f_inputformat = "";
		public string InputFormat {
			get { return f_inputformat; }
			set { f_inputformat = value; }
		}

		private string f_outputformat = "";
		public string OutputFormat {
			get { return f_outputformat; }
			set { f_outputformat = value; }
		}

		private string f_media = "";
		public string Media {
			get { return f_media; }
			set { f_media = value; }
		}
	}

	public class PluginManager {

		private static readonly log4net.ILog log = log4net.LogManager.GetLogger( System.Reflection.MethodBase.GetCurrentMethod().DeclaringType );

		private string PluginPath = null;
		private PluginConfiguration[] Plugins;
		private RemittSettings Settings;
		private bool AlreadyReadPlugins = false;

		public PluginManager ( ) {
			// Read configuration and form plugin directory path
			RemittConfiguration Configuration = new RemittConfiguration ( );
			this.Settings = Configuration.GetSettings ( );
			this.PluginPath = String.Format ( "{0}/engine/bin/plugins", this.Settings.InstallLocation );
			if ( ! Directory.Exists ( this.PluginPath ) ) {
				log.Error( String.Format( "Plugin path {0} does not exist!", this.PluginPath ) );
			} else {
				log.Info( String.Format( "Using {0} as Plugin path", this.PluginPath ) );
			}

		}

		public PluginConfiguration[] GetPlugins ( ) {
			if ( ! this.AlreadyReadPlugins ) {
				this.ReadPlugins( );
			}
			return ( PluginConfiguration[] ) this.Plugins;
		}

		public void ReadPlugins ( ) {
			// Wipe past configuration / create new configuration array
			this.Plugins = (Remitt.PluginConfiguration[]) Array.CreateInstance( typeof( PluginConfiguration ), 100 );

			// Loop through directory content
			int count = 0;
			DirectoryInfo d = new DirectoryInfo ( this.PluginPath );
			foreach ( FileInfo f in d.GetFiles ( ) ) {
				if ( Regex.IsMatch ( f.FullName, ".*/Plugin\\..+\\.dll" ) ) {
					Assembly a = Assembly.LoadFrom( f.FullName );
					Type [] types = a.GetTypes ( );
					log.Debug( "Assembly: " + f.FullName );
					foreach ( Type t in types ) {
						MethodInfo ThisPluginOptions = t.GetMethod ( "Config" );
						if ( ThisPluginOptions == null ) {
							log.Error( "Could not load Config method" );
							continue;
						}
						if ( ! ThisPluginOptions.IsStatic ) {
							log.Error( "Could not load static method" );
							continue;
						}

						// Index the actual plugin
						PluginConfiguration ThisPlugin = (PluginConfiguration) ThisPluginOptions.Invoke( null, new Type [0] );
						ThisPlugin.PluginFile = f.FullName;
						log.Debug( ThisPlugin.ToString() );
						this.Plugins[ count ] = ThisPlugin;
						count++;
					}
				}
			}
			Array.Resize( ref (Remitt.PluginConfiguration[]) this.Plugins, count );
			this.AlreadyReadPlugins = true;
		}

		public static PluginOption[] ReadXslOptions ( ) {
			PluginOption[] Options = (Remitt.PluginOption[]) Array.CreateInstance( typeof( PluginOption ), 100 );

			RemittConfiguration Configuration = new RemittConfiguration ( );
			RemittSettings settings = Configuration.GetSettings();

			int count = 0;
			string xslLocation = String.Format ( "{0}/xsl", settings.InstallLocation );
			log.Debug( "XSL location = " + xslLocation );
			DirectoryInfo d = new DirectoryInfo ( xslLocation );
			foreach ( FileInfo f in d.GetFiles ( ) ) {
				log.Debug( "Found file = " + f.FullName );
				if ( Regex.IsMatch ( f.FullName, ".*/.*\\.xsl\\.xml" ) ) {
					log.Debug( f.FullName );

					// Deserialize
					XmlSerializer formatter = new XmlSerializer( typeof( PluginOption ) );
					Stream s = new FileStream( f.FullName, FileMode.Open, FileAccess.Read, FileShare.None );
					Options[ count ] = null;
					try {
						Options[ count ] = ( PluginOption ) formatter.Deserialize( s );
					} finally {
						s.Close( );
					}
					count++;
				}
			}
			Array.Resize( ref (PluginOption[]) Options, count );
			log.Info( "Read " + count.ToString() + " xsl options" );
			return Options;
		}

		public string ResolveTranslationPlugin ( ) {
			return "";
		}

		public string ExecuteRenderPlugin ( string PluginName, string Input, string Option, int OID ) {
			string FullName = String.Format( "{0}/Plugin.Render.{1}.dll", this.PluginPath, PluginName );
			log.Debug( String.Format( "FullName = {0}", FullName ) );
			Assembly a = Assembly.LoadFrom( FullName );
			Type [] types = a.GetTypes( );
			foreach ( Type t in types ) {
				MethodInfo ThisMethod = t.GetMethod( "Render" );
				object[] args = { OID, Input, Option };
				string Output = (string) ThisMethod.Invoke( null, args );
				return Output;
			}
			// Should never reach here.
			return "";
		}

		public string ExecuteTranslationPlugin ( string PluginName, string Input, int OID ) {
			string FullName = String.Format( "{0}/Plugin.Translation.{1}.dll", this.PluginPath, PluginName );
			log.Debug( String.Format( "FullName = {0}", FullName ) );
			Assembly a = Assembly.LoadFrom( FullName );
			Type [] types = a.GetTypes( );
			foreach ( Type t in types ) {
				MethodInfo ThisMethod = t.GetMethod( "Translate" );
				object[] args = { OID, Input };
				string Output = (string) ThisMethod.Invoke( null, args );
				return Output;
			}
			// Should never reach here.
			return "";
		}

	}

}

