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

package org.remitt.server;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.remitt.prototype.PluginInterface;

/**
 * Servlet implementation class TestHarnessServlet
 */
public class TestHarnessServlet extends HttpServlet {

	private static final long serialVersionUID = 3712442448029750214L;

	static final Logger log = Logger.getLogger(TestHarnessServlet.class);

	public TestHarnessServlet() {
	}

	public void init() throws ServletException {
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
		String type = request.getParameter("type");
		String plugin = request.getParameter("plugin");
		String option = request.getParameter("option");
		String input = request.getParameter("input");

		log.info("Called for type " + type + " with plugin " + plugin);

		PluginInterface p = null;
		try {
			p = (PluginInterface) Class.forName(plugin).newInstance();
		} catch (InstantiationException e) {
			log.error(e);
		} catch (IllegalAccessException e) {
			log.error(e);
		} catch (ClassNotFoundException e) {
			log.error(e);
		} finally {
			try {
				String output = p.render(0, input, option);
				if (output.startsWith("<?xml ")) {
					// Handle XML output properly
					response.setContentType("text/xml");
				} else if (output.startsWith("PDF")) {
					// Return PDF
					response.setContentType("application/pdf");
				} else {
					// Assume plain text
					response.setContentType("text/plain");
				}
				PrintWriter pw = new PrintWriter(response.getOutputStream());
				pw.print(output);
				pw.flush();
				pw.close();
			} catch (Exception e) {
				log.error(e);
				throw new ServletException(e);
			}
		}
	}

}
