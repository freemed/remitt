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

package org.remitt.plugin.eligibility;

import java.util.Map;

import org.apache.log4j.Logger;
import org.remitt.prototype.EligibilityInterface;
import org.remitt.prototype.EligibilityParameter;
import org.remitt.prototype.EligibilityResponse;
import org.remitt.prototype.EligibilitySuccessCode;

public class DummyEligibility implements EligibilityInterface {

	static final Logger log = Logger.getLogger(DummyEligibility.class);

	@Override
	public String getPluginName() {
		return "DummyEligibility";
	}

	@Override
	public Double getPluginVersion() {
		return 0.1;
	}

	@Override
	public EligibilityResponse checkEligibility(String userName,
			Map<EligibilityParameter, String> values, boolean resubmission,
			Integer jobId) throws Exception {
		log.info("Started dummy eligibility check for user " + userName);
		EligibilityResponse r = new EligibilityResponse();

		if (Math.random() >= .5) {
			r.setSuccessCode(EligibilitySuccessCode.SUCCESS);
			r.setMessages(new String[] { "DUMMY BACKEND APPROVES!" });
		} else {
			r.setSuccessCode(EligibilitySuccessCode.VALIDATION_FAILURE);
			r.setMessages(new String[] { "DUMMY BACKEND DISAPPROVES!" });
		}

		return r;
	}

	@Override
	public String[] getPluginConfigurationOptions() {
		return null;
	}

}
