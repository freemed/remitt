/*
 * $Id$
 *
 * Authors:
 *      Jeff Buchbinder <jeff@freemedsoftware.org>
 *
 * REMITT Electronic Medical Information Translation and Transmission
 * Copyright (C) 1999-2010 FreeMED Software Foundation
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

public class DbFileStore {

	static final Logger log = Logger.getLogger(DbFileStore.class);

	public DbFileStore() {
	}

	/**
	 * Retrieve file contents from database-backed file store.
	 * 
	 * @param username
	 * @param category
	 * @param file
	 * @return
	 */
	public static byte[] getFile(String username, String category, String file) {
		Connection c = Configuration.getConnection();

		byte[] ret = null;

		PreparedStatement cStmt = null;
		try {
			cStmt = c.prepareStatement("SELECT content FROM tFileStore "
					+ " WHERE username = ? AND category = ? and filename = ? "
					+ ";");
			cStmt.setString(1, username);
			cStmt.setString(2, category);
			cStmt.setString(3, file);

			if (cStmt.execute()) {
				ResultSet rs = cStmt.getResultSet();
				rs.next();
				ret = rs.getBytes("content");
				rs.close();
			}
		} catch (NullPointerException npe) {
			log.error("Caught NullPointerException", npe);
		} catch (Throwable e) {
		} finally {
			DbUtil.closeSafely(cStmt);
			DbUtil.closeSafely(c);
		}

		return ret;
	}

	/**
	 * Insert file into database-backed file store. Duplicates throw SQL errors,
	 * since the db backed file store has a constraint specifying a unique index
	 * for the file names.
	 * 
	 * @param username
	 * @param category
	 * @param filename
	 * @param content
	 * @return Success, true or false.
	 */
	public static boolean putFile(String username, String category,
			String filename, byte[] content, Integer processorId) {
		Connection c = Configuration.getConnection();

		boolean success = false;

		Integer payloadId = 0;
		try {
			payloadId = Configuration.getControlThread()
					.getPayloadFromProcessor(processorId).getId();
		} catch (Exception ex) {
			log.error("Couldn't get payloadId from processorId = "
					+ (processorId == null ? "null" : processorId));
		}

		PreparedStatement cStmt = null;
		try {
			cStmt = c.prepareStatement("INSERT INTO tFileStore "
					+ " ( user, stamp, category, filename, content, "
					+ " contentsize, processorId, payloadId ) "
					+ " VALUES ( ?, NOW(), ?, ?, ?, ?, ?, ? ) " + ";");
			cStmt.setString(1, username);
			cStmt.setString(2, category);
			cStmt.setString(3, filename);
			cStmt.setBytes(4, content);
			cStmt.setInt(5, content.length);
			cStmt.setInt(6, processorId);
			cStmt.setInt(7, payloadId);
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
