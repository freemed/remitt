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

package org.remitt.server.tasks;

import it.sauronsoftware.cron4j.Task;
import it.sauronsoftware.cron4j.TaskExecutionContext;

import java.util.List;

import org.apache.log4j.Logger;
import org.remitt.prototype.ScooperInterface;
import org.remitt.server.Configuration;

public class ScooperTask extends Task {

	static final Logger log = Logger.getLogger(ScooperTask.class);

	@Override
	public void execute(TaskExecutionContext arg0) throws RuntimeException {
		log.info(this.getClass().getCanonicalName() + " executing");

		for (String plugin : Configuration.getPlugins("scooper")) {
			log.debug("Looking for users using the " + plugin
					+ " scooper plugin");
			for (String user : Configuration.getUsersForScooper(plugin)) {
				log
						.info("Found user " + user + " for scooper plugin "
								+ plugin);
				ScooperInterface p = null;
				try {
					p = (ScooperInterface) Class.forName(plugin).newInstance();
				} catch (InstantiationException e) {
					log.error(e);
					continue;
				} catch (IllegalAccessException e) {
					log.error(e);
					continue;
				} catch (ClassNotFoundException e) {
					log.error(e);
					continue;
				}
				try {
					p.setUsername(user);
					List<Integer> ids = p.scoop();
					for (Integer id : ids) {
						log.debug("Scooped file for user " + user + " with id "
								+ id.toString());
					}
				} catch (Exception e) {
					log.error(e);
					continue;
				}
			}
		}
	}

	@Override
	public boolean canBePaused() {
		return false;
	}

}
