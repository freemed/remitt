/*
 * $Id$
 *
 * Authors:
 *      Jeff Buchbinder <jeff@freemedsoftware.org>
 *
 * REMITT Electronic Medical Information Translation and Transmission
 * Copyright (C) 1999-2011 FreeMED Software Foundation
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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.remitt.prototype.EligibilityJob;
import org.remitt.prototype.EligibilityParameter;
import org.remitt.prototype.EligibilityResponse;
import org.remitt.server.Configuration;
import org.remitt.server.DbUtil;

public class DbEligibilityJob {

	static final Logger log = Logger.getLogger(DbEligibilityJob.class);

	public static boolean addEligibilityJob(String username, String plugin,
			HashMap<EligibilityParameter, String> payload) {
		Connection c = Configuration.getConnection();

		boolean success = false;

		PreparedStatement cStmt = null;
		try {
			cStmt = c.prepareStatement("INSERT INTO tEligibilityJobs "
					+ " ( user, stamp, plugin, payload, completed ) "
					+ " VALUES ( ?, NOW(), ?, ?, FALSE ) " + ";");
			cStmt.setString(1, username);
			cStmt.setString(2, plugin);
			cStmt.setObject(3, payload);
			cStmt.execute();
			success = true;
		} catch (NullPointerException npe) {
			log.error("Caught NullPointerException", npe);
		} catch (Throwable e) {
			log.error("Caught Throwable", e);
		} finally {
			DbUtil.closeSafely(cStmt);
			DbUtil.closeSafely(c);
		}

		return success;
	}

	public static List<Integer> getUnprocessedEligibilityJobList() {
		Connection c = Configuration.getConnection();

		List<Integer> output = new ArrayList<Integer>();

		PreparedStatement cStmt = null;
		try {
			cStmt = c.prepareStatement("SELECT id " + " FROM tEligibilityJobs "
					+ " WHERE completed = FALSE;");

			boolean hadResults = cStmt.execute();
			if (hadResults) {
				ResultSet r = cStmt.getResultSet();
				while (r.next()) {
					output.add(r.getInt("id"));
				}
				r.close();
			}
		} catch (NullPointerException npe) {
			log.error("Caught NullPointerException", npe);
		} catch (SQLException e) {
			log.error("Caught SQLException", e);
		} finally {
			DbUtil.closeSafely(cStmt);
			DbUtil.closeSafely(c);
		}

		return output;
	}

	@SuppressWarnings("unchecked")
	public static EligibilityJob getEligibilityJobById(Integer id) {
		Connection c = Configuration.getConnection();

		EligibilityJob output = new EligibilityJob();

		PreparedStatement cStmt = null;
		try {
			cStmt = c.prepareStatement("SELECT * " + " FROM tEligibilityJobs "
					+ " WHERE id = ?;");
			cStmt.setInt(1, id);

			boolean hadResults = cStmt.execute();
			if (hadResults) {
				ResultSet r = cStmt.getResultSet();
				r.next();
				output.setId(r.getInt("id"));
				output.setUsername(r.getString("username"));
				output.setPlugin(r.getString("plugin"));
				output.setPayload((HashMap<EligibilityParameter, String>) r
						.getObject("payload"));
				output.setResponse((EligibilityResponse) r
						.getObject("response"));
				r.close();
			}
		} catch (NullPointerException npe) {
			log.error("Caught NullPointerException", npe);
			output = null;
		} catch (SQLException e) {
			log.error("Caught SQLException", e);
			output = null;
		} finally {
			DbUtil.closeSafely(cStmt);
			DbUtil.closeSafely(c);
		}

		return output;
	}

	public static boolean saveEligibilityJob(String username, Integer id,
			EligibilityResponse response) {
		Connection c = Configuration.getConnection();

		boolean success = false;

		PreparedStatement cStmt = null;
		try {
			cStmt = c.prepareStatement("UPDATE tEligibilityJobs "
					+ " SET response = ?, processed = NOW(), completed = TRUE "
					+ " WHERE user = ? AND id = ? ; ");
			cStmt.setObject(1, response);
			cStmt.setString(2, username);
			cStmt.setInt(3, id);
			cStmt.execute();
			success = true;
		} catch (NullPointerException npe) {
			log.error("Caught NullPointerException", npe);
		} catch (Throwable e) {
			log.error("Caught Throwable", e);
		} finally {
			DbUtil.closeSafely(cStmt);
			DbUtil.closeSafely(c);
		}

		return success;
	}

}
