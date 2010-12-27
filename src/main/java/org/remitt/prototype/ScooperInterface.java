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

import java.util.List;
import java.util.Map;

/**
 * Interface defining file scoopers.
 * 
 * @author jeff@freemedsoftware.org
 */
public interface ScooperInterface {

	/**
	 * Attempt to run a scooper job.
	 * 
	 * @return List of new tScooper table entries
	 * @throws Exception
	 */
	public List<Integer> scoop() throws Exception;

	/**
	 * Set parameters for the current scooper as map of string values
	 * 
	 * @param params
	 */
	public void setParameters(Map<String, String> params);

	/**
	 * Set the current username.
	 * 
	 * @param user
	 */
	public void setUsername(String user);

	/**
	 * Get configuration variable which indicates a user is using this scooper.
	 * 
	 * @return Textual represnetation of configuration value.
	 */
	public String getEnabledConfigValue();

}
