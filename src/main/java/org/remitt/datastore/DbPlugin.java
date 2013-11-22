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

package org.remitt.datastore;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.log4j.Logger;
import org.remitt.server.Configuration;
import org.remitt.server.DbUtil;

public class DbPlugin {

	static final Logger log = Logger.getLogger(DbPlugin.class);

	public DbPlugin() {
	}

	/**
	 * Resolve any plugin option "transformations" which have to occur (i.e.
	 * supporting old stylesheet names).
	 * 
	 * @param plugin
	 * @param givenOption
	 * @return Either the new plugin option name which pertains to givenOption,
	 *         or the value of givenOption if there is no "transformation"
	 *         specified.
	 */
	public static String resolvePluginOption(String plugin, String givenOption) {
		Connection c = Configuration.getConnection();

		String ret = null;

		PreparedStatement cStmt = null;
		try {
			cStmt = c
					.prepareStatement("SELECT poption FROM tPluginOptionTransform "
							+ " WHERE plugin = ? AND poptionold = ? " + ";");
			cStmt.setString(1, plugin);
			cStmt.setString(2, givenOption);

			if (cStmt.execute()) {
				ResultSet rs = cStmt.getResultSet();
				rs.next();
				ret = rs.getString("poption");
				rs.close();
			}

			return ret;
		} catch (NullPointerException npe) {
		} catch (Throwable e) {
		} finally {
			DbUtil.closeSafely(cStmt);
			DbUtil.closeSafely(c);
		}

		return givenOption;
	}

}
