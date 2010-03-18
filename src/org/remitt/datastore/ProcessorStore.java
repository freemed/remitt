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
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.remitt.prototype.JobThreadState;
import org.remitt.prototype.ProcessorThread.ThreadType;
import org.remitt.server.Configuration;

public class ProcessorStore {

	static final Logger log = Logger.getLogger(ProcessorStore.class);

	private Integer payloadId = 0;

	/**
	 * Constructor.
	 * 
	 * @param pId
	 *            tPayload id
	 */
	public ProcessorStore(Integer pId) {
		payloadId = pId;
	}

	/**
	 * Get JobThreadState object indicating first available non-null thread and
	 * descriptor information.
	 * 
	 * @param tType
	 * @return
	 */
	public JobThreadState getJobThreadState(ThreadType tType) {
		JobThreadState tS = new JobThreadState();

		Connection c = Configuration.getConnection();

		PreparedStatement cStmt = null;
		try {
			cStmt = c
					.prepareCall("{ SELECT * FROM tProcessor WHERE payloadId=? AND stage=? AND ISNULL(tsEnd) LIMIT 1 }");

			cStmt.setInt(1, payloadId);
			cStmt.setString(2, tType.toString());

			boolean hadResults = cStmt.execute();
			if (hadResults) {
				ResultSet r = cStmt.getResultSet();
				while (r.next()) {
					tS.setPlugin(r.getString("plugin"));
					tS.setThreadId(r.getLong("threadId"));
					tS.setProcessorId(payloadId);
					tS.setThreadType(tType);
				}
			}
		} catch (NullPointerException npe) {
			log.error("Caught NullPointerException", npe);
			return null;
		} catch (SQLException e) {
			log.error("Caught SQLException", e);
			return null;
		}

		return tS;
	}
}
