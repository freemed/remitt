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

package org.remitt.server.jaas;

import java.io.IOException;
import java.io.Serializable;
import java.security.Principal;
import java.security.acl.Group;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.apache.log4j.Logger;
import org.remitt.server.Configuration;

import com.sun.security.auth.UserPrincipal;

public class HttpLoginModule implements LoginModule {

	public class MyPrincipal implements Principal, Serializable {

		private static final long serialVersionUID = -9015495525770274310L;
		private final String name;

		public MyPrincipal(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public boolean equals(Object o) {
			if (!(o instanceof MyPrincipal))
				return false;
			if (((MyPrincipal) o).getName().compareTo(this.getName()) == 0)
				return true;
			return false;
		}

	}

	public class MyGroup implements Group, Serializable {
		private static final long serialVersionUID = 1L;
		private final String name;
		private final Set<Principal> users = new HashSet<Principal>();

		public MyGroup(String name) {
			this.name = name;
		}

		public boolean addMember(Principal user) {
			return users.add(user);
		}

		public boolean removeMember(Principal user) {
			return users.remove(user);
		}

		public boolean isMember(Principal member) {
			return users.contains(member);
		}

		public Enumeration<? extends Principal> members() {
			return Collections.enumeration(users);
		}

		public String getName() {
			return name;
		}

		public boolean equals(Object o) {
			if (!(o instanceof MyGroup))
				return false;
			if (((MyGroup) o).getName().compareTo(this.getName()) == 0)
				return true;
			return false;
		}

	}

	private Subject subject;
	private CallbackHandler callbackHandler;
	private Map<String, ?> sharedState = null;
	private Map<String, ?> options = null;

	private boolean commitSucceeded = false;
	private boolean loginSucceeded = false;

	private String username;
	private Principal user;
	private Principal[] roles;

	static final Logger log = Logger.getLogger(HttpLoginModule.class);

	@Override
	public boolean abort() throws LoginException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean commit() throws LoginException {
		log.debug("commit()");

		if (!loginSucceeded) {
			// We didn't authenticate the user, but someone else did.
			// Clean up our state, but don't add our principal to
			// the subject
			username = null;
			return false;
		}

		assignPrincipal(new UserPrincipal(username));

		// Based on the username, we can assign principals here
		// Some examples for test....
		// assignPrincipal(new Principal("authenticateduser"));
		// assignPrincipal(new Principal("administrator"));
		// assignPrincipal(new Principal("company1"));

		// Clean up our internal state
		username = null;
		commitSucceeded = true;
		return true;
	}

	private void assignPrincipal(Principal p) {
		// Make sure we don't add duplicate principals
		if (!subject.getPrincipals().contains(p)) {
			subject.getPrincipals().add(p);
		}

		log.debug("Assigned principal " + p.getName() + " of type "
				+ p.getClass().getName() + " to user " + username);
	}

	@Override
	public void initialize(Subject subject, CallbackHandler callbackHandler,
			Map<String, ?> sharedState, Map<String, ?> options) {
		this.subject = subject;
		this.callbackHandler = callbackHandler;
		this.sharedState = sharedState;
		this.options = options;
	}

	@Override
	public boolean login() throws LoginException {
		NameCallback nameCallback = new NameCallback("Username");
		PasswordCallback passwordCallback = new PasswordCallback("Password",
				false);
		Callback[] callbacks = new Callback[] { nameCallback, passwordCallback };
		try {
			callbackHandler.handle(callbacks);
		} catch (IOException e) {
			throw new LoginException(e.toString());
		} catch (UnsupportedCallbackException e) {
			throw new LoginException(e.toString());
		}

		username = nameCallback.getName();
		char[] password = passwordCallback.getPassword();
		passwordCallback.clearPassword();

		Connection c = Configuration.getConnection();

		PreparedStatement cStmt = null;
		try {
			cStmt = c.prepareStatement("SELECT COUNT(*) AS c FROM tUser "
					+ " WHERE username = ? AND passhash = MD5(?);");
			cStmt.setString(1, username);
			cStmt.setString(2, new String(password));

			cStmt.execute();
			ResultSet rs = cStmt.getResultSet();
			rs.next();
			if (rs.getInt("c") < 1) {
				log.info("Was not able to login user " + username
						+ ", no user with that password found");
				return false;
			}
			rs.close();
			c.close();
		} catch (NullPointerException npe) {
			log.error("Caught NullPointerException", npe);
			if (c != null) {
				try {
					c.close();
				} catch (SQLException e1) {
					log.error(e1);
				}
			}
			return false;
		} catch (SQLException e) {
			log.error("Caught SQLException", e);
			if (c != null) {
				try {
					c.close();
				} catch (SQLException e1) {
					log.error(e1);
				}
			}
			return false;
		}

		// Validate user and password
		user = new MyPrincipal(username);
		roles = new Principal[] { new MyPrincipal("admin") };
		return true;
	}

	@Override
	public boolean logout() throws LoginException {
		return false;
	}

}
