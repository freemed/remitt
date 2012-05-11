/*
 * $Id$
 * 
 * Authors: Jeff Buchbinder <jeff@freemedsoftware.org>
 * 
 * REMITT Electronic Medical Information Translation and Transmission
 * Copyright (C) 1999-2012 FreeMED Software Foundation
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 675 Mass
 * Ave, Cambridge, MA 02139, USA.
 */

function validate() {
	log.info("In validate()");

	// Boilerplate
	var response = new ArrayList();

	// Add a dummy message to prove that this works.
	var msg = new ValidationResponseMessage(ValidationMessageType.INFO, "0000",
			"Dummy validation message, succeeded");
	response.add(msg);

	// Return list of messages.
	return response;
}
