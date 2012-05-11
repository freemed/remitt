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

package org.remitt.server.tasks;

import it.sauronsoftware.cron4j.Task;
import it.sauronsoftware.cron4j.TaskExecutionContext;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.remitt.datastore.DbEligibilityJob;
import org.remitt.prototype.EligibilityInterface;
import org.remitt.prototype.EligibilityJob;

public class EligibilityTask extends Task {

	static final Logger log = Logger.getLogger(EligibilityTask.class);

	public static AtomicBoolean bigDumbLock = new AtomicBoolean(false);

	@Override
	public void execute(TaskExecutionContext arg0) throws RuntimeException {
		log.info(this.getClass().getCanonicalName() + " executing");
		if (!bigDumbLock.compareAndSet(false, true)) {
			log.info("Already running, dropping out.");
			return;
		}
		List<Integer> ids = DbEligibilityJob.getUnprocessedEligibilityJobList();
		if (ids == null) {
			bigDumbLock.set(false);
			return;
		}
		log.info("Processing " + ids.size() + " eligibility batch items.");
		for (Integer id : ids) {
			EligibilityJob job = DbEligibilityJob.getEligibilityJobById(id);
			EligibilityInterface p = null;
			try {
				p = (EligibilityInterface) Class.forName(job.getPlugin())
						.newInstance();
				job.setResponse(p.checkEligibility(job.getUsername(),
						job.getPayload(), job.isResubmission(), id));
				DbEligibilityJob.saveEligibilityJob(job.getUsername(),
						job.getId(), job.getResponse());
			} catch (Exception e) {
				log.error(e);

				// Resubmit job on failure in transmission, etc.
				DbEligibilityJob.resubmitEligibilityJob(job.getUsername(), id);
			}
		}
		bigDumbLock.set(false);
	}

}
