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

import java.io.UnsupportedEncodingException;
import java.util.Date;

import org.apache.log4j.Logger;
import org.remitt.prototype.PayloadDto;
import org.remitt.prototype.PluginInterface;
import org.remitt.prototype.ProcessorThread;

public class RenderProcessorThread extends ProcessorThread {

	static final Logger log = Logger.getLogger(RenderProcessorThread.class);

	@Override
	public ThreadType getThreadType() {
		return ThreadType.RENDER;
	}

	@Override
	protected boolean work(Integer jobId) {
		Date tsStart = new Date();
		Date tsEnd = new Date();
		tsStart.setTime(System.currentTimeMillis());

		// Pull from tProcessor -> tPayload
		PayloadDto payload = Configuration.getControlThread()
				.getPayloadFromProcessor(jobId);
		String input = payload.getPayload();
		String option = payload.getRenderOption();
		String pluginClass = Configuration.getControlThread().resolvePlugin(
				payload, ThreadType.RENDER);

		log.info("Using pluginClass = " + pluginClass + " with option "
				+ option);

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

		byte[] output = null;
		try {
			output = p.render(jobId, input, option);
			tsEnd.setTime(System.currentTimeMillis());
		} catch (Exception e) {
			log.error(e);
			// Clear the thread, since we can't process any further.
			Configuration.getControlThread().clearProcessorForThread(
					(int) getId());

			// TODO: Update with error status so that frontend can inform
			// "client"
			return false;
		}

		// Store output
		try {
			Configuration.getControlThread().commitPayloadRun(jobId,
					new String(output, "UTF-8"), getThreadType(), tsEnd);
		} catch (UnsupportedEncodingException e) {
			log.error(e);
			// Clear the thread, since we can't process any further.
			Configuration.getControlThread().clearProcessorForThread(
					(int) getId());
			// TODO: Update with error status so that frontend can inform
			// "client"
			return false;
		}

		// Clear thread
		Configuration.getControlThread().clearProcessorForThread((int) getId());

		return true;
	}
}