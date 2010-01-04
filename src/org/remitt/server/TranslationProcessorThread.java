/*
 * $Id$
 *
 * Authors:
 *      Jeff Buchbinder <jeff@freemedsoftware.org>
 *
 * REMITT Electronic Medical Information Translation and Transmission
 * Copyright (C) 1999-2009 FreeMED Software Foundation
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

import org.apache.log4j.Logger;
import org.remitt.prototype.PayloadDto;
import org.remitt.prototype.PluginInterface;
import org.remitt.prototype.ProcessorThread;

public class TranslationProcessorThread extends ProcessorThread {

	static final Logger log = Logger
			.getLogger(TranslationProcessorThread.class);

	@Override
	public ThreadType getThreadType() {
		return ThreadType.TRANSLATION;
	}

	@Override
	protected boolean work(Integer jobId) {
		PayloadDto payload = Configuration.getControlThread()
				.getPayloadFromProcessor(jobId);
		String pluginClass = Configuration.getControlThread().resolvePlugin(
				payload, getThreadType());

		//

		//

		PluginInterface p = null;
		try {
			p = (PluginInterface) Class.forName(pluginClass).newInstance();
		} catch (InstantiationException e) {
			log.error(e);
			return false;
		} catch (IllegalAccessException e) {
			log.error(e);
			return false;
		} catch (ClassNotFoundException e) {
			log.error(e);
			return false;
		}

		String input = payload.getPayload();
		byte[] output = null;
		try {
			output = p.render(jobId, input, null);
		} catch (Exception e) {
			log.error(e);
			return false;
		}

		// Store output
		Configuration.getControlThread().commitPayloadRun(jobId, output,
				getThreadType(), null);

		// Push to next state
		Configuration.getControlThread().moveProcessorEntry(
				getJobThreadState(),
				ThreadType.TRANSMISSION,
				Configuration.getControlThread().resolvePlugin(payload,
						ThreadType.TRANSMISSION));

		// Clear thread
		Configuration.getControlThread().clearProcessorForThread((int) getId());
		
		return true;
	}
}
