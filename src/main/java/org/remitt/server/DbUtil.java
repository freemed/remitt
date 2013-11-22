/*
 * $Id$
 *
 * Authors:
 *      Jeff Buchbinder <jeff@freemedsoftware.org>
 *
 * REMITT Electronic Medical Information Translation and Transmission
 * Copyright (C) 1999-2014 FreeMED Software Foundation
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

package org.remitt.server;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DbUtil {

	public static void closeSafely(Connection c) {
		if (c != null) {
			try {
				c.close();
			} catch (Exception ex) {
			}
		}
	}

	public static void closeSafely(PreparedStatement c) {
		if (c != null) {
			try {
				c.close();
			} catch (Exception ex) {
			}
		}
	}

	public static void closeSafely(CallableStatement c) {
		if (c != null) {
			try {
				c.close();
			} catch (Exception ex) {
			}
		}
	}

	public static void closeSafely(ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (Exception ex) {
			}
		}
	}

}
