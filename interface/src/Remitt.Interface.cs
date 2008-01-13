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

namespace Remitt
{
	[WebService (Description="REMITT public web service", Namespace="http://remitt.org/")]
	public class Interface : System.Web.Services.WebService
	{
		[WebMethod (Description="Adds two numbers")]
		public int Add (int firstNumber, int secondNumber)
		{
			return firstNumber + secondNumber;
		}

		[WebMethod (Description="Retrieve active protocol version")]
		public string ProtocolVersion ( )
		{
			return "2.0";
		}

	}
}
