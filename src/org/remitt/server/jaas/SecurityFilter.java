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
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;

public class SecurityFilter implements Filter {

	class HttpAuthCallbackHandler implements CallbackHandler {

		private String userName;
		private String password;

		public HttpAuthCallbackHandler(HttpServletRequest request) {
			userName = request.getRemoteUser();

			String authheader = request.getHeader("Authorization");
			if (authheader != null) {
				password = (getDecodedCredentials(authheader.substring(6))
						.split(":"))[1];
			}

			System.out.println("SecurityFilter::HttpAuthCallbackHander: "
					+ "Remote user is: " + request.getRemoteUser());
			// System.out.println("Password is: " + password);
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
		System.out.println("Starting SecurityFilter.doFilter");

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
			// System.out.println("established new logincontext");
		} catch (LoginException le) {
			le.printStackTrace();
			response.sendError(HttpServletResponse.SC_FORBIDDEN, request
					.getRequestURI());
			return;
		}

		try {
			lc.login();
			// if we return with no exception, authentication succeeded
		} catch (Exception e) {
			System.out.println("Login failed: " + e);
			response.sendError(HttpServletResponse.SC_FORBIDDEN, request
					.getRequestURI());
			return;
		}

		try {
			// System.out.println("Subject is " + lc.getSubject());
			chain.doFilter(request, response);
		} catch (SecurityException se) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, request
					.getRequestURI());
		}
	}

}
