/*
 * $Id$
 *
 * Authors:
 *      Jeff Buchbinder <jeff@freemedsoftware.org>
 *
 * REMITT Electronic Medical Information Translation and Transmission
 * Copyright (C) 1999-2010 FreeMED Software Foundation
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

public class PayloadDto {

	private Integer id;
	private String renderPlugin;
	private String renderOption;
	private String transmissionPlugin;
	private String transmissionOption;
	private String payload;
	private String userName;

	public void setId(Integer i) {
		id = i;
	}

	public void setRenderPlugin(String p) {
		renderPlugin = p;
	}

	public void setRenderOption(String o) {
		renderOption = o;
	}

	public void setTransmissionPlugin(String p) {
		transmissionPlugin = p;
	}

	public void setTransmissionOption(String o) {
		transmissionOption = o;
	}

	public void setPayload(String p) {
		payload = p;
	}

	public void setUserName(String u) {
		userName = u;
	}

	public Integer getId() {
		return id;
	}

	public String getRenderPlugin() {
		return renderPlugin;
	}

	public String getRenderOption() {
		return renderOption;
	}

	public String getTransmissionPlugin() {
		return transmissionPlugin;
	}

	public String getTransmissionOption() {
		return transmissionOption;
	}

	public String getPayload() {
		return payload;
	}

	public String getUserName() {
		return userName;
	}

}
