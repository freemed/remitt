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

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;

public class ValidationResponseMessage implements Serializable {

	private static final long serialVersionUID = 2248349311468644252L;

	private ValidationMessageType type;
	private String code;
	private String message;

	public ValidationResponseMessage() {
	}

	public ValidationResponseMessage(ValidationMessageType type, String code,
			String message) {
		this.type = type;
		this.code = code;
		this.message = message;
	}

	public void setType(ValidationMessageType type) {
		this.type = type;
	}

	public ValidationMessageType getType() {
		return type;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("type", type).append("code",
				code).append("message", message).toString();
	}

}
