/*
 * $Id$
 *
 * Authors:
 *	Santosh K Sahoo ( http://www.codeproject.com/script/Articles/MemberArticles.aspx?amid=910952 )
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
using System.Web.Security;
using System.Security.Principal;

namespace Remitt {

	/// <summary>
	/// Summary description for AuthenticationModule.
	/// </summary>
	public class BaseAuthenticationModule : System.Web.IHttpModule
	{
		protected string REALM = "My Application";

		#region IHttpModule Members

		public void Init(HttpApplication context)
		{
			// TODO:  Add BasicModule.Init implementation
			context.AuthenticateRequest +=new EventHandler(context_AuthenticateRequest);
			context.EndRequest +=new EventHandler(context_EndRequest);
		}

		public void Dispose()
		{
			// TODO:  Add BasicModule.Dispose implementation
		}

		#endregion

		/// <summary>
		/// Handles the AuthenticateRequest event of the application context.
		/// </summary>
		/// <param name="sender">The Application instance.</param>
		/// <param name="e">The <see cref="System.EventArgs"/> instance containing the event data.</param>
		private void context_AuthenticateRequest(object sender, EventArgs e)
		{
			HttpApplication application = (HttpApplication) sender;
			if (!application.Context.Request.IsAuthenticated)
			{
				string sAUTH = application.Request.ServerVariables["HTTP_AUTHORIZATION"];
				if(sAUTH == null) return;
				//Received Credentials, Authenticate user
				if(sAUTH.Substring(0, 5).ToUpper() == "BASIC")	
				{
					string[] sCredentials;
					sCredentials = Base64Decode(sAUTH.Substring(6)).Split(':');
					GenericPrincipal UserPrincipal = Authenticate(sCredentials);
					if(UserPrincipal != null)
					{
						FormsAuthentication.Authenticate(sCredentials[0], sCredentials[1]);
						application.Context.User = UserPrincipal;
					}
				}
			}
		}

		/// <summary>
		/// Authenticates the specified credentials.
		/// </summary>
		/// <param name="Credentials">The credentials (Username and Password).</param>
		/// <param name="Roles">The string array containing roles.</param>
		/// <returns></returns>
		protected virtual GenericPrincipal Authenticate(string[] Credentials)
		{
			string[] Roles = null;
			GenericPrincipal UserPrincipal = new GenericPrincipal(new GenericIdentity(Credentials[0]), Roles);
			return UserPrincipal;
		}
		
		/// <summary>
		/// Decodes Base64 encoded string.
		/// </summary>
		/// <param name="EncodedData">The encoded data.</param>
		/// <returns></returns>
		private string Base64Decode(string EncodedData)
		{
			try
			{
				System.Text.UTF8Encoding encoder = new System.Text.UTF8Encoding();  
				System.Text.Decoder utf8Decode = encoder.GetDecoder();    
				byte[] todecode_byte = Convert.FromBase64String(EncodedData);
				int charCount = utf8Decode.GetCharCount(todecode_byte, 0, todecode_byte.Length);
				char[] decoded_char = new char[charCount];
				utf8Decode.GetChars(todecode_byte, 0, todecode_byte.Length, decoded_char, 0);
				return new String(decoded_char);
			}
			catch(Exception e)
			{
				throw new Exception("Error in base64Decode" + e.Message);
			}
		}

		/// <summary>
		/// Handles the EndRequest event of the application context.
		/// </summary>
		/// <param name="sender">The source of the event.</param>
		/// <param name="e">The <see cref="System.EventArgs"/> instance containing the event data.</param>
		private void context_EndRequest(object sender, EventArgs e)
		{
			HttpApplication application = (HttpApplication) sender;
			if(application.Response.StatusCode == 401)
			{
				application.Response.AddHeader("WWW-Authenticate","Basic Realm=" + REALM);
			}
		}
	}
}

