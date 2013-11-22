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

import org.apache.commons.lang.StringUtils;

public class UserDTO implements Serializable {

	private static final long serialVersionUID = 2010032100000L;

	private String username;
	private String password;
	private String contactEmail;
	private String callbackServiceUri;
	private String callbackServiceWsdlUri;
	private String callbackUsername;
	private String callbackPassword;
	private String[] roles;

	public UserDTO() {
	}

	@Override
	public String toString() {
		return "UserDTO object [" + getUsername() + ", roles = "
				+ StringUtils.join(getRoles(), ',') + "]";
	}

	/**
	 * @param username
	 *            the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param password
	 *            the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param contactEmail
	 *            the contactEmail to set
	 */
	public void setContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
	}

	/**
	 * @return the contactEmail
	 */
	public String getContactEmail() {
		return contactEmail;
	}

	/**
	 * @param callbackServiceUri
	 *            the callbackServiceUri to set
	 */
	public void setCallbackServiceUri(String callbackServiceUri) {
		this.callbackServiceUri = callbackServiceUri;
	}

	/**
	 * @return the callbackServiceUri
	 */
	public String getCallbackServiceUri() {
		return callbackServiceUri;
	}

	/**
	 * @param callbackServiceWsdlUri
	 *            the callbackServiceWsdlUri to set
	 */
	public void setCallbackServiceWsdlUri(String callbackServiceWsdlUri) {
		this.callbackServiceWsdlUri = callbackServiceWsdlUri;
	}

	/**
	 * @return the callbackServiceWsdlUri
	 */
	public String getCallbackServiceWsdlUri() {
		return callbackServiceWsdlUri;
	}

	/**
	 * @param callbackUsername
	 *            the callbackUsername to set
	 */
	public void setCallbackUsername(String callbackUsername) {
		this.callbackUsername = callbackUsername;
	}

	/**
	 * @return the callbackUsername
	 */
	public String getCallbackUsername() {
		return callbackUsername;
	}

	/**
	 * @param callbackPassword
	 *            the callbackPassword to set
	 */
	public void setCallbackPassword(String callbackPassword) {
		this.callbackPassword = callbackPassword;
	}

	/**
	 * @return the callbackPassword
	 */
	public String getCallbackPassword() {
		return callbackPassword;
	}

	/**
	 * @param roles
	 *            the roles to set
	 */
	public void setRoles(String[] roles) {
		this.roles = roles;
	}

	/**
	 * @return the roles
	 */
	public String[] getRoles() {
		return roles;
	}

}
