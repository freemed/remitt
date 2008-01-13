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
using System.Security.Cryptography;
using System.Security.Principal;
using System.Text;

using log4net;
using log4net.Config;

using MySql.Data.MySqlClient;

namespace Remitt {

	public class AuthenticationProvider : BaseAuthenticationModule {

		private MySqlConnection conn;
		private static readonly log4net.ILog log = log4net.LogManager.GetLogger( System.Reflection.MethodBase.GetCurrentMethod().DeclaringType );

		public AuthenticationProvider()
		{
			REALM = "REMITT";

			// Set up logging, in case it hasn't been configured yet
			BasicConfigurator.Configure( );

			// Read global config
			RemittConfiguration config = new RemittConfiguration( );
			RemittSettings settings = config.GetSettings( );

       			string connectionString = String.Format(
				"server={0};user id={1}; password={2}; database={3}; pooling=false"
				, settings.DbHost
				, settings.DbUsername
				, settings.DbPassword
				, settings.DbName
			);
			conn = new MySqlConnection( connectionString );

		}
		
		// From : http://www.thescripts.com/forum/thread119990.html
		public static string MD5(string password) {
			byte[] textBytes = System.Text.Encoding.Default.GetBytes( password );
			try {
				System.Security.Cryptography.MD5CryptoServiceProvider cryptHandler;
				cryptHandler = new System.Security.Cryptography.MD5CryptoServiceProvider();
				byte[] hash = cryptHandler.ComputeHash (textBytes);
				string ret = "";
				foreach (byte a in hash) {
					if ( a < 16 ) {
						ret += "0" + a.ToString ("x");
					} else {
						ret += a.ToString ("x");
					}
				}
				log.Debug( "MD5 computed as " + ret );
				return ret;
			} catch {
				throw;
			}
		}

		protected override GenericPrincipal Authenticate( string[] Credentials ) {
			GenericPrincipal UserPrincipal = null;

			try {
				conn.Open( );
			} catch ( MySqlException ex ) {
				log.Error( ex );
				return UserPrincipal;
			}

			log.Debug( "Validating user " + Credentials[0] );

			MySqlCommand cmd = new MySqlCommand( );
			cmd.Connection = conn;
			cmd.CommandText = "SELECT COUNT(*) AS found FROM auth WHERE user = ?user AND pass = MD5( ?pass );";
			cmd.Prepare();
			cmd.Parameters.AddWithValue( "?user", Credentials[0] );
			cmd.Parameters.AddWithValue( "?pass", Credentials[1] );
			try {
				MySqlDataReader reader = cmd.ExecuteReader( );
				reader.Read( );
				if ( reader.GetInt32( 0 ) < 1 ) {
					try { conn.Close( ); } catch ( MySqlException ex1 ) { if ( ex1.ToString().Length > 1 ) { } }
					log.Info( "Incorrect password attempt on user " + Credentials[0] + " with password hash of " + MD5( Credentials[1] ) );
					return UserPrincipal;
				}
				try { conn.Close( ); } catch ( MySqlException ex1 ) { if ( ex1.ToString().Length > 1 ) { } }
			} catch ( MySqlException ex ) {
				try { conn.Close( ); } catch ( MySqlException ex1 ) { if ( ex1.ToString().Length > 1 ) { } }
				log.Error( ex );
				return UserPrincipal;
			}

			try { conn.Close( ); } catch ( MySqlException ex ) { if ( ex.ToString().Length > 1 ) { } }

			// Otherwise return proper auth
			UserPrincipal = new GenericPrincipal( new GenericIdentity( Credentials[0] ), new string[]{ } );
			return UserPrincipal;
		}
	}
}

