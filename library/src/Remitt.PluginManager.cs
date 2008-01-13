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

using log4net;

namespace Remitt {

	public class PluginConfiguration {
		public string		PluginName;
		public string		PluginFile;
		public string		Version;
		public StringDictionary	Options;
		public string		InputFormat;
		public string		OutputFormat;

		public override string ToString ( ) {
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
				, this.Options
				, this.InputFormat
				, this.OutputFormat
			);
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
					//PluginConfiguration ThisPlugin = new

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
			this.AlreadyReadPlugins = true;
		}

		public string ResolveTranslationPlugin ( ) {
			return "";
		}

		public string ExecuteRenderPlugin ( string PluginName, string Input, string Option ) {
			string FullName = String.Format( "{0}/Plugin.Render.{1}.dll", this.PluginPath, PluginName );
			log.Debug( String.Format( "FullName = {0}", FullName ) );
			Assembly a = Assembly.LoadFrom( FullName );
			Type [] types = a.GetTypes( );
			foreach ( Type t in types ) {
				MethodInfo ThisMethod = t.GetMethod( "Render" );
				object[] args = { Input, Option };
				string Output = (string) ThisMethod.Invoke( null, args );
				return Output;
			}
			// Should never reach here.
			return "";
		}

		public string ExecuteTranslationPlugin ( string PluginName, string Input ) {
			string FullName = String.Format( "{0}/Plugin.Translation.{1}.dll", this.PluginPath, PluginName );
			log.Debug( String.Format( "FullName = {0}", FullName ) );
			Assembly a = Assembly.LoadFrom( FullName );
			Type [] types = a.GetTypes( );
			foreach ( Type t in types ) {
				MethodInfo ThisMethod = t.GetMethod( "Translate" );
				object[] args = { Input };
				string Output = (string) ThisMethod.Invoke( null, args );
				return Output;
			}
			// Should never reach here.
			return "";
		}

	}

}

