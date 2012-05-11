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
import java.util.HashMap;

public class EligibilityJob implements Serializable {

	private static final long serialVersionUID = -8275001154698452596L;

	private Integer id;
	private String username;
	private String plugin;
	private HashMap<EligibilityParameter, String> payload;
	private EligibilityResponse response;
	private boolean resubmission = false;

	public EligibilityJob() {
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPlugin() {
		return plugin;
	}

	public void setPlugin(String plugin) {
		this.plugin = plugin;
	}

	public HashMap<EligibilityParameter, String> getPayload() {
		return payload;
	}

	public void setPayload(HashMap<EligibilityParameter, String> payload) {
		this.payload = payload;
	}

	public EligibilityResponse getResponse() {
		return response;
	}

	public void setResponse(EligibilityResponse response) {
		this.response = response;
	}

	public boolean isResubmission() {
		return resubmission;
	}

	public void setResubmission(boolean resubmission) {
		this.resubmission = resubmission;
	}

}
