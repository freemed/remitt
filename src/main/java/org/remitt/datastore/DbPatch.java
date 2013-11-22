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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Scanner;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.remitt.server.Configuration;
import org.remitt.server.DbUtil;

public class DbPatch {

	static final Logger log = Logger.getLogger(DbPatch.class);

	/**
	 * Attempt to run a database patch.
	 * 
	 * @param patchFilename
	 * @return Success.
	 */
	public static boolean applyPatch(String patchFilename) {
		Connection c = Configuration.getConnection();

		String patch = null;

		Scanner scanner;
		try {
			scanner = new Scanner(new File(patchFilename)).useDelimiter("\\Z");
			patch = scanner.next();
			scanner.close();
		} catch (FileNotFoundException ex) {
			log.error(ex);
			return false;
		}

		PreparedStatement cStmt = null;
		boolean status = false;
		try {
			log.debug("Using patch length = " + patch.length());
			cStmt = c.prepareStatement(patch);
			cStmt.execute();
			log.info("Patch succeeded");
			status = true;
		} catch (NullPointerException npe) {
			log.error("Caught NullPointerException", npe);
		} catch (Throwable e) {
			log.error(e.toString());
		} finally {
			DbUtil.closeSafely(cStmt);
			DbUtil.closeSafely(c);
		}

		return status;
	}

	/**
	 * Determine if a patch has been applied yet.
	 * 
	 * @param patchName
	 * @return Success.
	 */
	public static boolean isPatchApplied(String patchName) {
		Connection c = Configuration.getConnection();

		int found = 0;

		PreparedStatement cStmt = null;
		try {
			cStmt = c.prepareStatement("SELECT COUNT(*) FROM tPatch "
					+ " WHERE patchName = ? " + ";");
			cStmt.setString(1, patchName);

			boolean hadResults = cStmt.execute();
			if (hadResults) {
				ResultSet rs = cStmt.getResultSet();
				rs.next();
				found = rs.getInt(1);
				rs.close();
			}
		} catch (NullPointerException npe) {
			log.error("Caught NullPointerException", npe);
		} catch (Throwable e) {
		} finally {
			DbUtil.closeSafely(cStmt);
			DbUtil.closeSafely(c);
		}

		return (boolean) (found > 0);
	}

	/**
	 * Record record of patch into tPatch table so that patches only run once.
	 * 
	 * @param patchName
	 * @return Success.
	 */
	public static boolean recordPatch(String patchName) {
		Connection c = Configuration.getConnection();

		boolean status = false;
		PreparedStatement cStmt = null;
		try {
			cStmt = c.prepareStatement("INSERT INTO tPatch "
					+ " ( patchName, stamp ) " + " VALUES ( ?, NOW() ) " + ";");
			cStmt.setString(1, patchName);

			cStmt.execute();
			status = true;
		} catch (NullPointerException npe) {
			log.error("Caught NullPointerException", npe);
		} catch (SQLException sq) {
			log.error("Caught SQLException", sq);
		} catch (Throwable e) {
		} finally {
			DbUtil.closeSafely(cStmt);
			DbUtil.closeSafely(c);
		}

		return status;
	}

	public static void dbPatcher(String patchLocation) {
		log.info("Database patching started for " + patchLocation);

		File patchDirectoryObject = new File(patchLocation);
		String[] children = patchDirectoryObject.list(new FilenameFilter() {
			@Override
			public boolean accept(File file, String name) {
				if (name.startsWith(".")) {
					log.debug("Skipping " + name + " (dot file)");
					return false;
				}
				return true;
			}
		});
		if (children != null) {
			// Sort all patches into name order.
			Arrays.sort(children);

			// Process patches
			log.info("Found " + children.length + " patches to process");
			for (String patchFilename : children) {
				String patchName = FilenameUtils.getBaseName(patchFilename);
				if (DbPatch.isPatchApplied(patchName)) {
					log.info("Patch " + patchName + " already applied.");
					continue;
				} else {
					log.info("Applying patch " + patchName + ", source file = "
							+ patchFilename);
					boolean success = DbPatch.applyPatch(patchDirectoryObject
							.getAbsolutePath()
							+ File.separatorChar + patchFilename);
					if (success) {
						DbPatch.recordPatch(patchName);
					} else {
						log.error("Failed to apply " + patchName
								+ ", stopping patch sequence.");
						return;
					}
				}
			}
		}
		log.info("Database patching completed");
	}

}
