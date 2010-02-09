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

import java.io.IOException;

import org.apache.log4j.Logger;
import org.pb.x12.FormatException;
import org.pb.x12.Parser;
import org.pb.x12.X12;

abstract public class X12Message {

	static final Logger log = Logger.getLogger(X12Message.class);

	private X12 x12message = null;

	public X12Message() {
	}

	public X12Message(String rawMessage) throws FormatException {
		parse(rawMessage);
	}

	public void parse(String rawMessage) throws FormatException {
		Parser parser = new Parser();
		try {
			setX12message(parser.parse(rawMessage));
		} catch (IOException e) {
			log.error(e);
		}
		parseSegments();
	}

	abstract protected void parseSegments();

	/**
	 * @param x12message
	 *            the x12message to set
	 */
	public void setX12message(X12 x12message) {
		this.x12message = x12message;
	}

	/**
	 * @return the original x12message
	 */
	public X12 getX12message() {
		return x12message;
	}

}
