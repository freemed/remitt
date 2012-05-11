/*
 * $Id $
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

import java.io.Serializable;

public class EligibilityResponse implements Serializable {

	private static final long serialVersionUID = 80195456387213L;

	private EligibilityStatus status;
	private EligibilitySuccessCode successCode;
	private String rawResponse;
	private String[] messages;

	public EligibilityResponse() {
	}

	public String[] getMessages() {
		return messages;
	}

	public void setMessages(String[] m) {
		messages = m;
	}

	public String getRawResponse() {
		return rawResponse;
	}

	public void setRawResponse(String r) {
		rawResponse = r;
	}

	public EligibilityStatus getStatus() {
		return status;
	}

	public void setStatus(EligibilityStatus s) {
		status = s;
	}

	public EligibilitySuccessCode getSuccessCode() {
		return successCode;
	}

	public void setSuccessCode(EligibilitySuccessCode s) {
		successCode = s;
	}
}
