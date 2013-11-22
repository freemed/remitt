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

package org.remitt.prototype;

import java.io.Serializable;

public class KeyringItem implements Serializable {

	private static final long serialVersionUID = 1L;

	private String keyname;

	private byte[] privatekey;

	private byte[] publickey;

	public void setKeyname(String keyname) {
		this.keyname = keyname;
	}

	public String getKeyname() {
		return keyname;
	}

	public void setPrivatekey(byte[] privatekey) {
		this.privatekey = privatekey;
	}

	public byte[] getPrivatekey() {
		return privatekey;
	}

	public void setPublickey(byte[] publickey) {
		this.publickey = publickey;
	}

	public byte[] getPublickey() {
		return publickey;
	}

}
