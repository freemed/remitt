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
using System.Configuration;
using System.Data;
using System.Diagnostics;
using System.ServiceProcess;
using System.Reflection;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using System.Xml.Serialization;

using log4net;
using log4net.Config;

/* 3rd Party */
using MySql.Data.MySqlClient;

namespace Remitt {
	
	public class EngineService : System.ServiceProcess.ServiceBase {

		private System.ComponentModel.Container components = null;

		private bool isStopped = false;
		private int  initializedExecuteThreads = 0;

		private static readonly log4net.ILog log = log4net.LogManager.GetLogger( System.Reflection.MethodBase.GetCurrentMethod().DeclaringType );

		public string settingsFile;
		public RemittSettings settings;
		public PluginManager Plugins;
		public MySqlConnection conn;

		public EngineService ( ) {
			// Set up log4net logging
			//BasicConfigurator.Configure();
			Console.WriteLine( Path.GetDirectoryName( System.Reflection.Assembly.GetExecutingAssembly().Location ) + "/../../log4net.xml" );
			XmlConfigurator.Configure( new System.IO.FileInfo( Path.GetDirectoryName( System.Reflection.Assembly.GetExecutingAssembly().Location ) + "/../../log4net.xml" ) );

			log.Info( "InitializeComponent needs to load" );
			InitializeComponent();
		}

		// The main entry point for the process
		static void Main()
		{
			log.Debug( "Entered main" );
			System.ServiceProcess.ServiceBase[] ServicesToRun;
			log.Debug( "Creating ServicesToRun" );
			ServicesToRun = new System.ServiceProcess.ServiceBase[] { new EngineService() };
			log.Debug( "Running services" );
			System.ServiceProcess.ServiceBase.Run( ServicesToRun );
		}

		private void InitializeComponent()
		{
			log.Debug( "InitializeComponent" );

			// 
			// EngineService
			// 
			this.CanShutdown = true;
			this.ServiceName = "RemittEngineService";

			RemittConfiguration Configuration = new RemittConfiguration ( );
			this.settings = Configuration.GetSettings();

			// Read plugins
			this.Plugins = new Remitt.PluginManager();
			this.Plugins.ReadPlugins();
		}

		/// <summary>
		/// Clean up any resources being used.
		/// </summary>
		protected override void Dispose( bool disposing )
		{
			if( disposing )
			{
				if (components != null) 
				{
					components.Dispose();
				}
			}
			base.Dispose( disposing );
		}

		protected override void OnStart(string[] args) {
			this.isStopped = false;
			log.Info( this.ServiceName + " Started" );

			log.Info( "Spawning master ProcessorThread" );
			Thread Thread_Processor = new Thread( delegate() { this.ProcessorThread( (int) 1 ); } );
			Thread_Processor.Start( );
		}
	 
		protected override void OnStop() {
			this.isStopped = true;

			while ( this.initializedExecuteThreads > 0 ) {
				log.Info( "Waiting for execute threads to terminate (" + this.initializedExecuteThreads.ToString() + " remaining)" );
				System.Threading.Thread.Sleep( 1000 );
			}
		}

		public void ProcessorThread( object ThisThreadId ) {
			log.Info( "ProcessorThread #" + ThisThreadId.ToString() + " starting" );

			MySqlConnection conn = this.OpenDbConnection( );

			log.Info( "Clearing and recreating execute processor thread states" );
			try {
				MySqlCommand clearCmd = new MySqlCommand(
						  "DELETE FROM executestate;"
						, conn
					);
				clearCmd.ExecuteNonQuery( );
			} catch ( MySqlException ex ) {
				log.Error( ex );
				throw new Exception( "Failed to clear execute processor thread states" );
			}

			// Spawn children thread to handle each step
			log.Info( "Spawning all execution threads" );
			int TotalThreadCount = 0;
			for ( int ThreadCount=1; ThreadCount <= settings.ExecuteThreads; ThreadCount++ ) {
				//----- Render threads -----
				log.Debug( "Spawning execute[render] thread #" + ThreadCount.ToString( ) );
				Thread Thread_Execute_Render = new Thread( delegate() { this.ExecuteRenderThread( (int) TotalThreadCount ); } );
				Thread_Execute_Render.Start( );
				TotalThreadCount++;

				//----- Translation threads -----
				log.Debug( "Spawning execute[translation] thread #" + ThreadCount.ToString( ) );
				Thread Thread_Execute_Translation = new Thread( delegate() { this.ExecuteTranslationThread( (int) TotalThreadCount ); } );
				Thread_Execute_Translation.Start( );
				TotalThreadCount++;

				//----- Transmission threads -----
				log.Debug( "Spawning execute[transmission] thread #" + ThreadCount.ToString( ) );
				Thread Thread_Execute_Transmission = new Thread( delegate() { this.ExecuteTransmissionThread( (int) TotalThreadCount ); } );
				Thread_Execute_Transmission.Start( );
				TotalThreadCount++;
			}

			while ( this.initializedExecuteThreads < TotalThreadCount ) {
				log.Debug( "Waiting for execute threads to finish initializing" );
				System.Threading.Thread.Sleep( 1000 );
			}

			log.Info( "Startup completed" );

			while (!this.isStopped) {
				//log.Debug( "Starting loop" );

				// While in event loop, just sleep
				System.Threading.Thread.Sleep( 1000 );

				// Consume empty items in processor queue
				MySqlCommand cmd = new MySqlCommand(
						  "SELECT p.OID FROM processorqueue p LEFT OUTER JOIN executequeue e ON p.OID = e.pOID WHERE e.OID IS NULL"
						, conn
					);
				try {
					MySqlDataReader reader = cmd.ExecuteReader();

					// Read items into an arraylist
					ArrayList Items = new ArrayList( );
					while ( reader.Read() ) {
						Items.Add( reader.GetInt32( 0 ) );
						log.Debug( "Found OID = " + reader.GetString( 0 ) );
					}
					reader.Close();

					// If we've got something, process
					if ( Items.Count > 0 ) {
						foreach ( int Item in Items ) {
							log.Debug( "Consuming item from processorqueue : " + Item.ToString() );
						}
					}
				} catch ( NullReferenceException ex ) {
					if ( ex.ToString().Length > 1 ) { }
				} catch ( Exception ex ) {
					log.Error( ex );
				}
				//log.Debug( "Finished run" );
			}
		}

		public void ExecuteRenderThread( object ThisThreadId ) {
			log.Info( "Thread #" + ThisThreadId.ToString() + " starting" );

			MySqlConnection conn = this.OpenDbConnection( );

			log.Info( "Clearing and recreating execute thread state" );
			try {
				MySqlCommand clearCmd = new MySqlCommand(
						  "DELETE FROM executestate WHERE thread_id = " + ThisThreadId.ToString() + ";"
						, conn
					);
				clearCmd.ExecuteNonQuery( );
				MySqlCommand createCmd = new MySqlCommand(
						  "INSERT INTO executestate ( thread_id ) VALUES ( " + ThisThreadId.ToString() + " ) ;"
						, conn
					);
				createCmd.ExecuteNonQuery( );
			} catch ( MySqlException ex ) {
				log.Error( ex );
				throw new Exception( "Failed to clear execute thread state" );
			}

			// Mark that this thread has completed initialization
			this.initializedExecuteThreads++;

			while (!this.isStopped) {
				//log.Debug( "Starting loop" );

				// While in event loop, just sleep
				System.Threading.Thread.Sleep( 1000 );

				MySqlCommand cmd = new MySqlCommand(
						  "SELECT OID FROM processorqueue"
						, conn
					);
				try {
					MySqlDataReader reader = cmd.ExecuteReader();

					// Read items into an arraylist
					ArrayList Items = new ArrayList( );
					while ( reader.Read() ) {
						Items.Add( reader.GetInt32( 0 ) );
						log.Debug( "Found OID = " + reader.GetString( 0 ) );
					}
					reader.Close();

					// If we've got something, process
					if ( Items.Count > 0 ) {
						log.Debug( "Items to process..." );
					}
				} catch ( NullReferenceException ex ) {
					if ( ex.ToString().Length > 1 ) { }
				} catch ( Exception ex ) {
					log.Error( ex );
				}
				//log.Debug( "Finished run" );
			}
			log.Info( "Thread #" + ThisThreadId.ToString() + " exiting" );
			this.initializedExecuteThreads--;
		}

		public void ExecuteTranslationThread( object ThisThreadId ) {
			log.Info( "Thread #" + ThisThreadId.ToString() + " starting" );

			MySqlConnection conn = this.OpenDbConnection( );

			log.Info( "Clearing and recreating execute thread state" );
			try {
				MySqlCommand clearCmd = new MySqlCommand(
						  "DELETE FROM executestate WHERE thread_id = " + ThisThreadId.ToString() + ";"
						, conn
					);
				clearCmd.ExecuteNonQuery( );
				MySqlCommand createCmd = new MySqlCommand(
						  "INSERT INTO executestate ( thread_id ) VALUES ( " + ThisThreadId.ToString() + " ) ;"
						, conn
					);
				createCmd.ExecuteNonQuery( );
			} catch ( MySqlException ex ) {
				log.Error( ex );
				throw new Exception( "Failed to clear execute thread state" );
			}

			// Mark that this thread has completed initialization
			this.initializedExecuteThreads++;

			while (!this.isStopped) {
				//log.Debug( "Starting loop" );

				// While in event loop, just sleep
				System.Threading.Thread.Sleep( 1000 );

				MySqlCommand cmd = new MySqlCommand(
						  "SELECT OID FROM processorqueue"
						, conn
					);
				try {
					MySqlDataReader reader = cmd.ExecuteReader();

					// Read items into an arraylist
					ArrayList Items = new ArrayList( );
					while ( reader.Read() ) {
						Items.Add( reader.GetInt32( 0 ) );
						log.Debug( "Found OID = " + reader.GetString( 0 ) );
					}
					reader.Close();

					// If we've got something, process
					if ( Items.Count > 0 ) {
						log.Debug( "Items to process..." );
					}
				} catch ( NullReferenceException ex ) {
					if ( ex.ToString().Length > 1 ) { }
				} catch ( Exception ex ) {
					log.Error( ex );
				}
				//log.Debug( "Finished run" );
			}
			log.Info( "Thread #" + ThisThreadId.ToString() + " exiting" );
			this.initializedExecuteThreads--;
		}

		public void ExecuteTransmissionThread( object ThisThreadId ) {
			log.Info( "Thread #" + ThisThreadId.ToString() + " starting" );

			MySqlConnection conn = this.OpenDbConnection( );

			log.Info( "Clearing and recreating execute thread state" );
			try {
				MySqlCommand clearCmd = new MySqlCommand(
						  "DELETE FROM executestate WHERE thread_id = " + ThisThreadId.ToString() + ";"
						, conn
					);
				clearCmd.ExecuteNonQuery( );
				MySqlCommand createCmd = new MySqlCommand(
						  "INSERT INTO executestate ( thread_id ) VALUES ( " + ThisThreadId.ToString() + " ) ;"
						, conn
					);
				createCmd.ExecuteNonQuery( );
			} catch ( MySqlException ex ) {
				log.Error( ex );
				throw new Exception( "Failed to clear execute thread state" );
			}

			// Mark that this thread has completed initialization
			this.initializedExecuteThreads++;

			while (!this.isStopped) {
				//log.Debug( "Starting loop" );

				// While in event loop, just sleep
				System.Threading.Thread.Sleep( 1000 );

				MySqlCommand cmd = new MySqlCommand(
						  "SELECT OID FROM processorqueue"
						, conn
					);
				try {
					MySqlDataReader reader = cmd.ExecuteReader();

					// Read items into an arraylist
					ArrayList Items = new ArrayList( );
					while ( reader.Read() ) {
						Items.Add( reader.GetInt32( 0 ) );
						log.Debug( "Found OID = " + reader.GetString( 0 ) );
					}
					reader.Close();

					// If we've got something, process
					if ( Items.Count > 0 ) {
						log.Debug( "Items to process..." );
					}
				} catch ( NullReferenceException ex ) {
					if ( ex.ToString().Length > 1 ) { }
				} catch ( Exception ex ) {
					log.Error( ex );
				}
				//log.Debug( "Finished run" );
			}
			log.Info( "Thread #" + ThisThreadId.ToString() + " exiting" );
			this.initializedExecuteThreads--;
		}

		public MySqlConnection OpenDbConnection ( ) {
			string connectionString = String.Format(
				  "server={0};user id={1}; password={2}; database={3}; pooling=false"
				, settings.DbHost
				, settings.DbUsername
				, settings.DbPassword
				, settings.DbName
			);
			try {
				conn = new MySqlConnection( connectionString );
				conn.Open( );
				return conn;
			} catch ( MySqlException ex ) {
				log.Error( ex );
				log.Error( "Error establishing connection : " + connectionString );
				throw new Exception ( "Error establishing connection to MySQL server" );
			}
		}

	}

}

