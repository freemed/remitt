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

package org.remitt.prototype;

import java.util.HashMap;

import WebServices.GatewayEDI.SuccessCode;

public interface EligibilityInterface {

	public class EligibilityResponse {
		private Status status;
		private SuccessCode successCode;
		private String rawResponse;

		public String getRawResponse() {
			return rawResponse;
		}

		public void setRawResponse(String r) {
			rawResponse = r;
		}

		public Status getStatus() {
			return status;
		}

		public void setStatus(Status s) {
			status = s;
		}

		public SuccessCode getSuccessCode() {
			return successCode;
		}

		public void setSuccessCode(SuccessCode s) {
			successCode = s;
		}
	}

	public static enum Status {
		OK, BAD, CONTINUATION, SERVER_ERROR
	};

	public String getPluginName();

	public Double getPluginVersion();

	public EligibilityResponse checkEligibility(HashMap<String, String> values)
			throws Exception;

	public String[] getPluginConfigurationOptions();

}
