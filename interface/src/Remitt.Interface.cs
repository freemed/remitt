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
using System.Web;
using System.Web.Services;

using log4net;
using MySql.Data.MySqlClient;

namespace Remitt
{
	[WebService (Description="REMITT public web service", Namespace="http://remitt.org/")]
	public class Interface : System.Web.Services.WebService
	{
		// Logging
		private static readonly log4net.ILog log = log4net.LogManager.GetLogger( System.Reflection.MethodBase.GetCurrentMethod().DeclaringType );

		[WebMethod (Description="Retrieve active protocol version")]
		public string ProtocolVersion ( )
		{
			return "2.0";
		}

		[WebMethod (Description="Submit a job for processing by the REMITT engine")]
		public int SubmitJob( string InputPayload, string RenderPlugin, string RenderOption, string TransportPlugin )
		{
			string Username = System.Web.HttpContext.Current.User.Identity.Name;
			log.Debug( "Submit job for " + Username + " [payload length = " + InputPayload.Length.ToString() + "]" );

			MySqlConnection sql = this.OpenDbConnection( );
			
			MySqlCommand cmd = new MySqlCommand( );
			cmd.Connection = sql;
			cmd.CommandText = "INSERT INTO jobs ( user, original_data, render_plugin, render_option, transport_plugin ) VALUES ( ?user, ?data, ?renderplugin, ?renderoption, ?transportplugin );";
			cmd.Prepare();
			cmd.Parameters.AddWithValue( "?user", Username );
			cmd.Parameters.AddWithValue( "?data", InputPayload );
			cmd.Parameters.AddWithValue( "?renderplugin", RenderPlugin );
			cmd.Parameters.AddWithValue( "?renderoption", RenderOption );
			cmd.Parameters.AddWithValue( "?transportplugin", TransportPlugin );
			try {
				cmd.ExecuteNonQuery( );
			} catch ( MySqlException ex ) {
				// In the case of an exception with the insert, return 0
				log.Error( ex );
				sql.Close( );
				return 0;
			}

			// Get last ID to return
			int OID = 0;
			MySqlCommand lastInsertCmd = new MySqlCommand( "SELECT LAST_INSERT_ID();", sql );
			try {
				MySqlDataReader reader = lastInsertCmd.ExecuteReader( );
				reader.Read( );
				OID = reader.GetInt32( 0 );
			} catch ( MySqlException ex ) {
				// In the case of an exception with the insert, return 0
				log.Error( ex );
				sql.Close( );
				return 0;
			}

			try { sql.Close( ); } catch ( MySqlException ex ) { log.Error( ex ); }
			return OID;
		}

		[WebMethod (Description="Change stored password for current user")]
		public bool UpdatePassword( string Password )
		{
			string Username = System.Web.HttpContext.Current.User.Identity.Name;
			log.Info( "Password change for " + Username );

			MySqlConnection sql = this.OpenDbConnection( );
			
			MySqlCommand cmd = new MySqlCommand( );
			cmd.Connection = sql;
			cmd.CommandText = "UPDATE auth SET pass = ?pass WHERE user = ?user";
			cmd.Prepare();
			cmd.Parameters.AddWithValue( "?user", Username );
			cmd.Parameters.AddWithValue( "?pass", Password );
			try {
				cmd.ExecuteNonQuery( );
				try { sql.Close( ); } catch ( MySqlException ex1 ) { log.Error( ex1 ); }
				return true;
			} catch ( MySqlException ex ) {
				log.Error( ex );
				try { sql.Close( ); } catch ( MySqlException ex1 ) { log.Error( ex1 ); }
				return false;
			}
		}

		private MySqlConnection OpenDbConnection ( ) {
			RemittConfiguration config = new RemittConfiguration( );
			RemittSettings settings = config.GetSettings( );
			MySqlConnection conn;

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
