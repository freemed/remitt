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

package org.remitt.prototype;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ValidationResponse implements Serializable {

	private static final long serialVersionUID = 7874460334444969370L;

	private ValidationStatus status;
	private List<ValidationResponseMessage> messages = new ArrayList<ValidationResponseMessage>();

	public ValidationResponse() {
	}

	public List<ValidationResponseMessage> getMessages() {
		return messages;
	}

	public void setMessages(List<ValidationResponseMessage> messages) {
		this.messages = messages;
	}

	public void addMessage(ValidationResponseMessage message) {
		this.messages.add(message);
	}

	public void setStatus(ValidationStatus status) {
		this.status = status;
	}

	public ValidationStatus getStatus() {
		return status;
	}

}
