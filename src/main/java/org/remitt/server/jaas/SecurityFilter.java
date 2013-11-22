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

package org.remitt.server.jaas;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextOutputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;

public class SecurityFilter implements Filter {

	/**
	 * An extension for the HTTPServletRequest that overrides the
	 * getUserPrincipal() and isUserInRole(). We supply these implementations
	 * here, where they are not normally populated unless we are going through
	 * the facility provided by the container.
	 * <p>
	 * If he user or roles are null on this wrapper, the parent request is
	 * consulted to try to fetch what ever the container has set for us. This is
	 * intended to be created and used by the UserRoleFilter.
	 * 
	 * @author thein
	 * 
	 */
	public class UserRoleRequestWrapper extends HttpServletRequestWrapper {

		String user;
		List<String> roles = null;
		HttpServletRequest realRequest;

		public UserRoleRequestWrapper(String user, List<String> roles,
				HttpServletRequest request) {
			super(request);
			this.user = user;
			this.roles = roles;
			this.realRequest = request;
			if (this.roles == null) {
				this.roles = new ArrayList<String>();
			}
		}

		@Override
		public boolean isUserInRole(String role) {
			if (roles == null) {
				return this.realRequest.isUserInRole(role);
			}
			return roles.contains(role);
		}

		@Override
		public Principal getUserPrincipal() {
			if (this.user == null) {
				return realRequest.getUserPrincipal();
			}

			// make an anonymous implementation to just return our user
			return new Principal() {
				@Override
				public String getName() {
					return user;
				}
			};
		}
	}

	class HttpAuthCallbackHandler implements CallbackHandler {

		private String userName;
		private String password;

		public HttpAuthCallbackHandler(HttpServletRequest request) {
			/*
			 * Enumeration<String> hs = request.getHeaderNames(); while
			 * (hs.hasMoreElements()) { String e = hs.nextElement(); System.out
			 * .println("Header " + e + " = " + request.getHeader(e)); }
			 */
			// Don't know target case, so try both:
			String authheader = request.getHeader("Authorization");
			if (authheader == null) {
				authheader = request.getHeader("authorization");
			}
			if (authheader != null) {
				// Can't use request.getRemoteUser() because it isn't set...
				userName = (getDecodedCredentials(authheader.substring(6))
						.split(":"))[0];
				password = (getDecodedCredentials(authheader.substring(6))
						.split(":"))[1];
			}

			// System.out.println("SecurityFilter::HttpAuthCallbackHander: "
			// + "Remote user is: " + userName + ", pw length = "
			// + (password == null ? "0" : password.length()));
		}

		public void handle(Callback[] callbacks) throws IOException,
				UnsupportedCallbackException {
			for (int i = 0; i < callbacks.length; i++) {
				if (callbacks[i] instanceof TextOutputCallback) {
					// display a message according to a specified type
				} else if (callbacks[i] instanceof NameCallback) {
					NameCallback nc = (NameCallback) callbacks[i];
					nc.setName(userName);
				} else if (callbacks[i] instanceof PasswordCallback) {
					PasswordCallback nc = (PasswordCallback) callbacks[i];
					if (password != null)
						nc.setPassword(password.toCharArray());
				} else {
					throw new UnsupportedCallbackException(callbacks[i],
							"Unrecognized Callback");
				}
			}
		}

		/**
		 * @param credentials
		 *            to be decoded
		 * @return String decoded credentials <b>name:password </b>
		 */
		private String getDecodedCredentials(String credentials) {
			return (new String(Base64.decodeBase64(credentials.getBytes())));
		}

	}

	public static FilterConfig filterConfig = null;

	public static FilterConfig getFilterConfig() {
		return filterConfig;
	}

	public void init(FilterConfig config) throws ServletException {
		filterConfig = config;
		System.setProperty("java.security.auth.login.config", config
				.getServletContext().getRealPath("/WEB-INF/login.conf"));
	}

	public void destroy() {
		// config = null;
	}

	public void doFilter(ServletRequest sreq, ServletResponse sres,
			FilterChain chain) throws IOException, ServletException {
		// System.out.println("Starting SecurityFilter.doFilter");

		HttpServletResponse response = (HttpServletResponse) sres;
		HttpServletRequest request = (HttpServletRequest) sreq;

		HttpSession session = request.getSession(true);
		Subject subject = (Subject) session
				.getAttribute("javax.security.auth.subject");

		if (subject == null) {
			subject = new Subject();
		}

		session.setAttribute("javax.security.auth.subject", subject);

		LoginContext lc = null;
		try {
			lc = new LoginContext("Jaas", subject, new HttpAuthCallbackHandler(
					request));
			session.setAttribute("javax.security.auth.subject", subject);
			// System.out.println("established new logincontext");
		} catch (LoginException le) {
			le.printStackTrace();
			sendAuthRequest(response);
			return;
		}

		try {
			lc.login();
			// if we return with no exception, authentication succeeded
			// Split from subject
		} catch (Exception e) {
			System.out.println("Login failed: " + e);
			sendAuthRequest(response);
			return;
		}

		try {
			// System.out.println("Subject is " + lc.getSubject());
			if (subject.getPrincipals().size() > 0) {
				Iterator<Principal> iterP = subject.getPrincipals().iterator();
				String user = iterP.next().getName();
				List<String> roles = new ArrayList<String>();
				while (iterP.hasNext()) {
					roles.add(iterP.next().getName());
				}
				chain.doFilter(
						new UserRoleRequestWrapper(user, roles, request),
						response);
			} else {
				chain.doFilter(request, response);
			}
		} catch (SecurityException se) {
			sendAuthRequest(response);
		}
	}

	/**
	 * Send a 401 request for BASIC authentication.
	 * @param response
	 */
	protected void sendAuthRequest(HttpServletResponse response) {
		response.setHeader("WWW-Authenticate", "Basic realm=\"REMITT\"");
		try {
			response.sendError(401);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
