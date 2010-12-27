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

package org.remitt.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.remitt.datastore.ProcessorStore;
import org.remitt.prototype.JobThreadState;
import org.remitt.prototype.PayloadDto;
import org.remitt.prototype.ProcessorThread;
import org.remitt.prototype.ProcessorThread.ThreadType;

/**
 * Singleton control thread which controls work.
 * 
 * @author jeff@freemedsoftware.org
 * 
 */
public class ControlThread extends Thread {

	/**
	 * Exception thrown when there are no free threads available in the
	 * processor thread pool to take a new processing job.
	 * 
	 * @author jeff@freemedsoftware.org
	 * 
	 */
	public class FreeThreadNotFoundException extends Exception {

		private static final long serialVersionUID = 20090803000L;

	}

	static final Logger log = Logger.getLogger(ControlThread.class);

	protected int SLEEP_TIME = 500;

	protected MasterControl servletContext = null;

	protected Map<Long, ProcessorThread> workerThreads = new HashMap<Long, ProcessorThread>();

	protected List<JobThreadState> threadPool = new ArrayList<JobThreadState>();

	public void run() {
		Configuration.loadConfiguration();
		log.info("Thread [ControlThread] initializing");

		SLEEP_TIME = Configuration.getConfiguration().getInt(
				"remitt.control.sleepTime");
		log.info("Initialized with SLEEP_TIME = " + SLEEP_TIME);

		startChildren();
		while (!isInterrupted() && servletContext != null) {
			try {
				Thread.sleep(SLEEP_TIME);
				log.trace("Thread [ControlThread] Waking up after "
						+ SLEEP_TIME + "ms to check for work");
				work();
			} catch (InterruptedException e) {
				log.warn(e);
				stopChildren();
			}
		}
	}

	public void setServletContext(MasterControl mc) {
		servletContext = mc;
	}

	public List<JobThreadState> getThreadPool() {
		return this.threadPool;
	}

	/**
	 * Find the current running payload.
	 * 
	 * @param threadId
	 * @return
	 */
	public Integer getPayloadForThread(Long threadId) {
		Iterator<JobThreadState> iter = threadPool.iterator();
		while (iter.hasNext()) {
			JobThreadState s = iter.next();
			if (s.getProcessorId() == null) {
				// Skip processing if there's nothing assigned to this
				continue;
			}
			if (s.getProcessorId() == 0) {
				// Skip processing if there's nothing assigned to this
				continue;
			}
			if (s.getThreadId() == threadId) {
				// If we find the thread id, return the payload
				return s.getProcessorId();
			}
		}
		// If no job, return null
		return null;
	}

	/**
	 * Get an input payload from a tPayload id.
	 * 
	 * @return
	 */
	public PayloadDto getPayloadById(Integer payloadId) {
		Connection c = Configuration.getConnection();

		PayloadDto payload = new PayloadDto();
		PreparedStatement cStmt = null;
		try {
			cStmt = c.prepareStatement("SELECT * FROM tPayload WHERE id = ?;");
			cStmt.setInt(1, payloadId);

			cStmt.execute();
			ResultSet rs = cStmt.getResultSet();
			rs.next();

			payload.setId(rs.getInt("id"));
			payload.setPayload(rs.getBytes("payload"));
			payload.setRenderPlugin(rs.getString("renderPlugin"));
			payload.setRenderOption(rs.getString("renderOption"));
			payload.setTransportPlugin(rs.getString("transportPlugin"));
			payload.setTransportOption(rs.getString("transportOption"));
			payload.setUserName(rs.getString("user"));

			rs.close();
		} catch (NullPointerException npe) {
			log.error("Caught NullPointerException", npe);
			payload = null;
		} catch (SQLException e) {
			log.error("Caught SQLException", e);
			payload = null;
		} finally {
			DbUtil.closeSafely(cStmt);
			DbUtil.closeSafely(c);
		}

		return payload;
	}

	/**
	 * Get an input payload from a tProcessor id.
	 * 
	 * @return
	 */
	public PayloadDto getPayloadFromProcessor(Integer processorId) {
		Connection c = Configuration.getConnection();

		// Safety check so that if there isn't a value, we tell the calling
		// function so.
		if (processorId == -1) {
			return null;
		}

		PreparedStatement cStmt = null;
		PayloadDto payload = null;
		try {
			log.trace("SELECT payloadId FROM tProcessor WHERE id = "
					+ processorId.toString());
			cStmt = c
					.prepareStatement("SELECT payloadId FROM tProcessor WHERE id = ?;");
			cStmt.setInt(1, processorId);
			cStmt.execute();
			ResultSet rs = cStmt.getResultSet();
			rs.next();
			Integer payloadId = rs.getInt(1);
			rs.close();
			log.trace("getPayloadFromProcessor for " + processorId
					+ " returned " + payloadId);
			payload = getPayloadById(payloadId);
		} catch (NullPointerException npe) {
			log.error("Caught NullPointerException", npe);
		} catch (SQLException e) {
			log.error("Caught SQLException", e);
		} finally {
			DbUtil.closeSafely(cStmt);
			DbUtil.closeSafely(c);
		}

		return payload;
	}

	/**
	 * Record data from the output of a ProcessorThread to the database table
	 * tProcessor.
	 * 
	 * @param payloadId
	 * @param availThread
	 * @param input
	 * @param output
	 * @param threadType
	 * @param plugin
	 * @param tsStart
	 * @param tsEnd
	 */
	public Integer migratePayloadToProcessor(Integer payloadId,
			Long availThread, byte[] input, ThreadType threadType,
			String plugin, Date tsStart) {
		Connection c = Configuration.getConnection();

		PreparedStatement cStmt = null;
		Integer ret = null;
		try {
			cStmt = c.prepareStatement("INSERT INTO tProcessor ( "
					+ " threadId, payloadId, stage, plugin, tsStart, pInput "
					+ " ) VALUES ( " + "?, ?, ?, ?, ?, ? " + " );",
					PreparedStatement.RETURN_GENERATED_KEYS);
			log.trace("INSERT INTO tProcessor ( "
					+ " threadId, payloadId, stage, plugin, tsStart, pInput "
					+ " ) VALUES ( " + availThread + ", " + payloadId + ", "
					+ threadType.toString() + ", " + plugin + ", "
					+ tsStart.getTime() + ", PAYLOAD " + " );");

			cStmt.setLong(1, availThread);
			cStmt.setInt(2, payloadId);
			cStmt.setString(3, threadType.toString());
			cStmt.setString(4, plugin);
			cStmt.setTimestamp(5, new Timestamp(tsStart.getTime()));
			cStmt.setBytes(6, input);

			cStmt.execute();
			ResultSet newKey = cStmt.getGeneratedKeys();
			newKey.next();
			ret = newKey.getInt(1);
			newKey.close();
		} catch (NullPointerException npe) {
			log.error("Caught NullPointerException", npe);
		} catch (SQLException e) {
			log.error("Caught SQLException", e);
		} finally {
			DbUtil.closeSafely(cStmt);
			DbUtil.closeSafely(c);
		}

		return ret;
	}

	/**
	 * Record data from the output of a ProcessorThread to the database table
	 * tProcessor.
	 */
	public void commitPayloadRun(Integer processorId, byte[] output,
			ThreadType threadType, Date tsEnd) {
		Connection c = Configuration.getConnection();

		if (tsEnd == null) {
			tsEnd = new Date();
		}

		PreparedStatement cStmt = null;
		try {
			cStmt = c.prepareStatement("UPDATE tProcessor SET "
					+ " tsEnd = ?, " + "pOutput = ? " + " WHERE id = ? " + ";");
			log.trace("UPDATE tProcessor SET " + " tsEnd = " + tsEnd.getTime()
					+ ", " + "pOutput = OUTPUT " + " WHERE id = " + processorId
					+ " ;");

			cStmt.setTimestamp(1, new Timestamp(tsEnd.getTime()));
			cStmt.setBytes(2, output);
			cStmt.setInt(3, processorId);

			cStmt.executeUpdate();
		} catch (NullPointerException npe) {
			log.error("Caught NullPointerException", npe);
		} catch (SQLException e) {
			log.error("Caught SQLException", e);
		} finally {
			DbUtil.closeSafely(cStmt);
			DbUtil.closeSafely(c);
		}
	}

	/**
	 * Record a failed run to the processor table.
	 * 
	 * @param processorId
	 * @param tsEnd
	 */
	public void setFailedPayloadRun(Integer processorId, Date tsEnd) {
		Connection c = Configuration.getConnection();

		if (tsEnd == null) {
			tsEnd = new Date();
		}

		PreparedStatement cStmt = null;
		PreparedStatement cStmt2 = null;
		try {
			cStmt = c.prepareStatement("UPDATE tProcessor SET " + " tsEnd = ? "
					+ " WHERE id = ? " + ";");
			log.trace("UPDATE tProcessor SET " + " tsEnd = " + tsEnd.getTime()
					+ " WHERE id = " + processorId + " ;");

			cStmt.setTimestamp(1, new Timestamp(tsEnd.getTime()));
			cStmt.setInt(2, processorId);
			cStmt.executeUpdate();

			cStmt2 = c
					.prepareStatement("UPDATE tPayload SET payloadState = 'failed' "
							+ " WHERE id = "
							+ " ( SELECT payloadId FROM tProcessor WHERE id = ? ); ");
			cStmt2.setInt(1, processorId);
			cStmt2.executeUpdate();
		} catch (NullPointerException npe) {
			log.error("Caught NullPointerException", npe);
		} catch (SQLException e) {
			log.error("Caught SQLException", e);
		} finally {
			DbUtil.closeSafely(cStmt);
			DbUtil.closeSafely(cStmt2);
			DbUtil.closeSafely(c);
		}
	}

	/**
	 * Mark a payload as having finished processing.
	 * 
	 * @param payloadId
	 */
	public void setPayloadCompleted(Integer payloadId) {
		Connection c = Configuration.getConnection();

		PreparedStatement cStmt = null;
		try {
			log.trace("UPDATE tProcessor SET " + " payloadState = 'completed' "
					+ " WHERE id = " + payloadId + " ;");
			cStmt = c.prepareStatement("UPDATE tPayload SET "
					+ " payloadState = 'completed' " + " WHERE id = ? " + ";");

			cStmt.setInt(1, payloadId);

			cStmt.executeUpdate();
		} catch (NullPointerException npe) {
			log.error("Caught NullPointerException", npe);
		} catch (SQLException e) {
			log.error("Caught SQLException", e);
		} finally {
			DbUtil.closeSafely(cStmt);
			DbUtil.closeSafely(c);
		}
	}

	/**
	 * Clear the internal thread/payload status for the specified thread,
	 * effectively signalling that the thread in question is no longer working
	 * on the payload it was working on.
	 * 
	 * @param threadId
	 */
	public void clearProcessorForThread(Long threadId) {
		setProcessorForThread(threadId, 0);
	}

	/**
	 * Push processor id to thread.
	 * 
	 * @param threadId
	 * @param processorId
	 */
	public void setProcessorForThread(Long threadId, Integer processorId) {
		Iterator<JobThreadState> iter = threadPool.iterator();
		while (iter.hasNext()) {
			JobThreadState s = iter.next();
			if (s.getThreadId() == threadId) {
				((ProcessorThread) workerThreads.get(threadId))
						.setJobThreadState(s);
				s.setProcessorId(processorId);
			}
		}
	}

	/**
	 * Start all processor threads and add them to the thread pool.
	 */
	protected void startChildren() {
		Integer numberOfWorkers = Configuration.getConfiguration().getInt(
				"remitt.worker.threadPoolSize", 5);

		// Spawn RenderProcessThreads
		for (int iter = 0; iter < numberOfWorkers; iter++) {
			log.debug("Spawning RenderProcessorThread #" + (iter + 1));
			RenderProcessorThread t = new RenderProcessorThread();
			t.start();
			workerThreads.put(t.getId(), t);
			addThreadToPool(t);

			// Create a small delay to avoid pig-piling on threads
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// Spawn TranslationProcessThreads
		for (int iter = 0; iter < numberOfWorkers; iter++) {
			log.debug("Spawning TranslationProcessorThread #" + (iter + 1));
			TranslationProcessorThread t = new TranslationProcessorThread();
			t.start();
			workerThreads.put(t.getId(), t);
			addThreadToPool(t);

			// Create a small delay to avoid pig-piling on threads
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// Spawn TransportProcessThreads
		for (int iter = 0; iter < numberOfWorkers; iter++) {
			log.debug("Spawning TransportProcessorThread #" + (iter + 1));
			TransportProcessorThread t = new TransportProcessorThread();
			t.start();
			workerThreads.put(t.getId(), t);
			addThreadToPool(t);

			// Create a small delay to avoid pig-piling on threads
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * Stop all processor threads.
	 */
	protected void stopChildren() {
		log.info("Stopping all worker threads");
		Iterator<ProcessorThread> iter = workerThreads.values().iterator();
		while (iter.hasNext()) {
			Thread t = iter.next();
			log.info("Interrupting thread #" + t.getId());
			t.interrupt();
			workerThreads.remove(t);
		}

		// Attempt to reap threads until they're all dead
		while (workerThreads.size() > 0) {
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			for (Thread t : workerThreads.values()) {
				if (!t.isAlive()) {
					log.info("Reaping thread #" + t.getId());
					workerThreads.remove(t);
				}
				workerThreads.remove(t);
			}
		}
		log.info("All worker threads destroyed");
	}

	/**
	 * Internal method to add a processor thread to the <ControlThread> pool of
	 * worker threads, as well as setting up the <JobThreadState> both for the
	 * <ProcessorThread> and internally.
	 * 
	 * @param pt
	 */
	protected void addThreadToPool(ProcessorThread pt) {
		JobThreadState s = new JobThreadState();
		s.setThreadId(pt.getId());
		s.setProcessorId(0);
		s.setThreadType(pt.getThreadType());
		pt.setJobThreadState(s);
		threadPool.add(pt.getJobThreadState());
	}

	/**
	 * Request next available thread for a particular processor type and assign
	 * a payload to its internal state.
	 * 
	 * @param threadType
	 * @param payloadId
	 * @return Grabbed thread id.
	 * @throws FreeThreadNotFoundException
	 */
	protected Long getNextAvailableThread(ThreadType threadType,
			Integer payloadId) throws FreeThreadNotFoundException {
		Iterator<JobThreadState> iter = threadPool.iterator();
		Long found = 0L;
		log.debug("Iterating through threads");
		while (found == 0 && iter.hasNext()) {
			JobThreadState s = iter.next();
			if (s.getProcessorId() > 0) {
				log.debug("Skipping as processorId is already defined");
				continue;
			}
			// Deal with wait state
			if (s.getProcessorId() == -1) {
				log.debug("Skipping as processorId indicates wait state (-1)");
				continue;
			}
			if (s.getThreadType() != threadType) {
				log.debug("Wrong thread type (" + s.getThreadType() + ")");
				continue;
			}
			found = s.getThreadId();
			log.debug("Found thread id " + found);
			// If we've found something, grab it so no other thread sees it
			s.setProcessorId(-1);
		}
		if (found == 0) {
			throw new FreeThreadNotFoundException();
		} else {
			return found;
		}
	}

	/**
	 * Get the list of tPayload id entries which haven't been assigned to any
	 * tProcessor table entries yet.
	 * 
	 * @return Array of id
	 */
	protected Integer[] getUnassignedPayloads() {
		List<Integer> r = new ArrayList<Integer>();
		Connection c = Configuration.getConnection();

		PreparedStatement cStmt = null;
		try {
			cStmt = c.prepareStatement("SELECT a.id AS id FROM tPayload AS a "
					+ " WHERE a.id NOT IN "
					+ " ( SELECT b.payloadId FROM tProcessor AS b ) "
					+ " AND a.payloadState = 'valid' "
					+ " ORDER BY a.insert_stamp " + ";");

			if (cStmt.execute()) {
				ResultSet rs = cStmt.getResultSet();
				while (rs.next()) {
					r.add(rs.getInt("id"));
				}
				rs.close();
			}
		} catch (NullPointerException npe) {
			log.error("Caught NullPointerException", npe);
		} catch (Throwable e) {
		} finally {
			DbUtil.closeSafely(cStmt);
			DbUtil.closeSafely(c);
		}

		return (Integer[]) r.toArray(new Integer[0]);
	}

	protected boolean work() {
		log.trace("Entering control thread work cycle");

		// Set first step for insertion
		ThreadType initialStep = null;
		try {
			initialStep = ThreadType.valueOf(Configuration.getConfiguration()
					.getString("remitt.control.initialStep"));
		} catch (IllegalArgumentException e) {
			log.warn(e);
			initialStep = ThreadType.RENDER;
		} catch (NullPointerException e) {
			log.warn(e);
			initialStep = ThreadType.RENDER;
		}
		log.trace("Using remitt.control.initialStep = " + initialStep.name());

		// Search for unassigned payloads which are not being processed
		Integer[] newWork = getUnassignedPayloads();
		if (newWork.length == 0) {
			log.trace("No new work found for this cycle");
		} else {
			log.debug("Found " + newWork.length + " payloads to process");
		}

		// For each payload ...
		for (int iter = 0; iter < newWork.length; iter++) {
			// ... attempt to insert into ThreadType.[[initialStep]] threads and
			// let the processing begin.

			Long availThread = null;
			try {
				// Attempt to get the next available thread for insertion, which
				// will "lock" the found thread with a -1 payloadId entry
				availThread = getNextAvailableThread(initialStep, newWork[iter]);
				log.trace("Grabbed next available thread " + availThread);

				// ... and populate the appropriate pieces
				PayloadDto payload = getPayloadById(newWork[iter]);
				log
						.trace("Created payload dto with render plugin identified as "
								+ payload.getRenderPlugin()
								+ ", option = "
								+ payload.getRenderOption());
				Integer processorId = migratePayloadToProcessor(
						payload.getId(),
						availThread,
						payload.getPayload(),
						initialStep,
						(initialStep.equals(ThreadType.RENDER) ? payload
								.getRenderPlugin() : payload.getRenderPlugin()),
						new Date(System.currentTimeMillis()));

				// Push processor entry
				setProcessorForThread(availThread, processorId);
			} catch (FreeThreadNotFoundException e) {
				log.trace(e);
				log
						.info("Cannot insert tPayload "
								+ newWork[iter]
								+ " due to lack of free threads. Skipping rest of queue.");
				return false;
			}
		}

		// Reap finished records which aren't being used anymore from the
		// database.

		log.trace("Exiting control thread work cycle");
		return true;
	}

	/**
	 * Get full namespace for class of plugin for a particular payload for a
	 * particular thread type.
	 * 
	 * @param payload
	 * @param tType
	 * @return
	 */
	public String resolvePlugin(PayloadDto payload, ThreadType tType) {
		switch (tType) {
		case RENDER:
			return payload.getRenderPlugin();
		case TRANSLATION:
			return Configuration.resolveTranslationPlugin(payload
					.getRenderPlugin(), payload.getRenderOption(), payload
					.getTransportPlugin(), payload.getTransportOption());
		case TRANSPORT:
			return payload.getTransportPlugin();
		default:
			return null;
		}
	}

	/**
	 * Determine payload to be used by stage from either tPayload or tProcessor,
	 * depending on where the data is supposed to source from.
	 * 
	 * @param payload
	 *            <PayloadDto> object with information regarding current
	 *            processing payload.
	 * @param type
	 *            Current thread type.
	 * @return Input payload to be used by plugin/stage.
	 */
	public byte[] getPayloadForProcessorStage(PayloadDto payload,
			ThreadType type) {
		ProcessorStore s = new ProcessorStore(payload.getId());
		byte[] ret = null;
		if (type == ThreadType.RENDER) {
			log.info("getPayloadForProcessor using original payload");
			ret = payload.getPayload();
			log.info("getPayloadForProcessor get payload size = " + ret.length);
		}
		if (type == ThreadType.TRANSLATION) {
			log.info("getPayloadForProcessor using RENDER payload");
			ret = s.getProcessorOutputPayload(ThreadType.RENDER);
			log.info("getPayloadForProcessor get payload size = " + ret.length);
		}
		if (type == ThreadType.TRANSPORT) {
			log.info("getPayloadForProcessor using TRANSLATION payload");
			ret = s.getProcessorOutputPayload(ThreadType.TRANSLATION);
			log.info("getPayloadForProcessor get payload size = " + ret.length);
		}
		log.trace("getPayloadForProcessor " + type.toString() + " returned : "
				+ ret);
		return ret;
	}

	/**
	 * "Move" a tProcessor entry into the next queue stage.
	 * 
	 * @param tS
	 * @param nextType
	 * @param plugin
	 * @return Success
	 */
	public synchronized boolean moveProcessorEntry(JobThreadState tS,
			ThreadType nextType, String plugin) {
		Long availThread = null;
		boolean done = false;
		while (!done && !isInterrupted()) {
			try {
				PayloadDto originalPayload = getPayloadFromProcessor(tS
						.getProcessorId());

				// Attempt to get the next available thread for insertion, which
				// will "lock" the found thread with a -1 payloadId entry
				availThread = getNextAvailableThread(nextType, originalPayload
						.getId());

				// ... and populate the appropriate pieces
				PayloadDto payload = getPayloadFromProcessor(tS
						.getProcessorId());
				Integer processorId = migratePayloadToProcessor(
						payload.getId(), availThread,
						getPayloadForProcessorStage(payload, nextType),
						nextType, resolvePlugin(payload, nextType), new Date(
								System.currentTimeMillis()));

				// Push processor entry
				setProcessorForThread(availThread, processorId);
				done = true;
			} catch (FreeThreadNotFoundException e) {
				log.trace(e);
				log
						.info("Cannot insert tPayload due to lack of free threads. Waiting for one to free up.");
				try {
					Thread.sleep(SLEEP_TIME);
				} catch (InterruptedException e1) {
					log.warn(e1);
					log.info("Exiting thread " + getId()
							+ " due to interruption");
					return false;
				}
			}
		}
		return done;
	}

}
