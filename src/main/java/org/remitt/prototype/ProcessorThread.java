/*
 * $Id$
 *
 * Authors:
 *      Jeff Buchbinder <jeff@freemedsoftware.org>
 *
 * REMITT Electronic Medical Information Translation and Transmission
 * Copyright (C) 1999-2012 FreeMED Software Foundation
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

package org.remitt.prototype;

import org.apache.log4j.Logger;
import org.remitt.server.Configuration;

public abstract class ProcessorThread extends Thread {

	public enum ThreadType {
		VALIDATION, RENDER, TRANSLATION, TRANSPORT;

		@Override
		public String toString() {
			return name().toString().toLowerCase();
		}
	};

	static final Logger log = Logger.getLogger(ProcessorThread.class);

	protected static int SLEEP_TIME = 500;

	protected JobThreadState jobThreadState = new JobThreadState();

	public void run() {
		log.info("Thread [" + getThreadTypeString() + getId()
				+ "] initializing");
		while (!isInterrupted()) {
			try {
				Thread.sleep(SLEEP_TIME);
				log.trace("Thread [" + getThreadTypeString() + getId()
						+ "] Waking up after " + SLEEP_TIME
						+ "ms to check for work");
				Integer job = Configuration.getControlThread()
						.getPayloadForThread(getId());
				if (job != null) {
					work(job);
				}
			} catch (InterruptedException e) {
				log.warn(e);
			}
		}
	}

	public abstract ThreadType getThreadType();

	/**
	 * Produce string representation of thread type.
	 * 
	 * @return Textual representation of thread type.
	 */
	public String getThreadTypeString() {
		ThreadType t = getThreadType();
		switch (t) {
		case VALIDATION:
			return "Validation";
		case RENDER:
			return "Render";
		case TRANSLATION:
			return "Translation";
		case TRANSPORT:
			return "Transport";
		default:
			return "";
		}
	}

	/**
	 * Prototype for performing work as part of this thread.
	 * 
	 * @param jobId
	 *            tPayload table id
	 * @return Success
	 */
	protected abstract boolean work(Integer jobId);

	public JobThreadState getJobThreadState() {
		return jobThreadState;
	}

	public void setJobThreadState(JobThreadState s) {
		jobThreadState = s;
	}

}
