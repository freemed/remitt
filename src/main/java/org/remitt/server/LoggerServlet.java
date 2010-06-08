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

package org.remitt.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * Servlet implementation class LoggerServlet
 */
public class LoggerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	static final Logger logger = Logger.getLogger(LoggerServlet.class);

	/**
	 * Default constructor.
	 */
	public LoggerServlet() {
		// TODO Auto-generated constructor stub
	}

	public void init() throws ServletException {
		System.out.println("LogggerServlet init() starting.");

		// Attempt to divine base install, and if we're using jetty, shim it
		if (System.getProperty("jetty.home") != null
				&& System.getProperty("jetty.home") != "") {
			System.setProperty("catalina.home", System
					.getProperty("jetty.home"));
		}

		String log4jfile = getInitParameter("log4j-properties");
		System.out.println("log4j-properties: " + log4jfile);
		if (log4jfile != null) {
			String propertiesFilename = getServletContext().getRealPath(
					log4jfile);
			System.out.println("Using file " + propertiesFilename);
			PropertyConfigurator.configure(propertiesFilename);
			logger.info("logger configured.");
		} else {
			String propertiesFilename = getServletContext().getRealPath(
					"/WEB-INF/log4j.properties");
			System.out.println("Using file " + propertiesFilename);
			PropertyConfigurator.configure(propertiesFilename);
			logger.info("logger configured.");
		}
		System.out.println("LoggerServlet init() done.");
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
	}

}
