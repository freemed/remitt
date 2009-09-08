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

import org.apache.log4j.Logger;

public class SecurityFilter implements Filter {

	class HttpAuthCallbackHandler implements CallbackHandler {

		final Logger innerLog = Logger.getLogger(HttpAuthCallbackHandler.class);

		private String userName;

		public HttpAuthCallbackHandler(HttpServletRequest request) {
			userName = request.getRemoteUser();
			innerLog.debug("Remote user is: " + request.getRemoteUser());
		}

		public void handle(Callback[] cb) throws IOException,
				UnsupportedCallbackException {

			for (int i = 0; i < cb.length; i++) {
				if (cb[i] instanceof NameCallback) {
					NameCallback nc = (NameCallback) cb[i];
					nc.setName(userName);
				} else
					throw new UnsupportedCallbackException(cb[i],
							"HttpAuthCallbackHandler");
			}
		}
	}

	static final Logger log = Logger.getLogger(SecurityFilter.class);

	public void init(FilterConfig config) throws ServletException {
	}

	public void destroy() {
		// config = null;
	}

	public void doFilter(ServletRequest sreq, ServletResponse sres,
			FilterChain chain) throws IOException, ServletException {
		log.trace("Starting SecurityFilter.doFilter");

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
			System.out.println("established new logincontext");
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
			System.out.println("Subject is " + lc.getSubject());
			chain.doFilter(request, response);
		} catch (SecurityException se) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, request
					.getRequestURI());
		}
	}

}
