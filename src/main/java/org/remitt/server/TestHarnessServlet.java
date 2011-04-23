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

package org.remitt.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.pb.x12.FormatException;
import org.remitt.prototype.PluginInterface;
import org.remitt.prototype.ValidationInterface;
import org.remitt.prototype.ValidationResponse;
import org.remitt.prototype.X12Message;

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

		// Handle parser tests
		if (type.equals("parser")) {
			String plugin = request.getParameter("plugin");
			String input = request.getParameter("input");
			parserTest(plugin, input, response.getOutputStream());
			return;
		}

		// Handle parser tests
		if (type.equals("callback")) {
			String input = request.getParameter("input");
			String user = request.getUserPrincipal().getName();
			Configuration.pushRemittanceData(user, input.getBytes());
			return;
		}

		// Handle parser tests
		if (type.equals("validate")) {
			String plugin = request.getParameter("plugin");
			String input = request.getParameter("input");
			validatorTest(plugin, input, response.getOutputStream());
			return;
		}

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
				p.setDefaultUsername(request.getUserPrincipal().getName());
				byte[] output = p.render(0, input.getBytes("UTF-8"), option);
				if (new String(output, "UTF-8").startsWith("<?xml ")) {
					// Handle XML output properly
					response.setContentType("text/plain");
					response.setContentLength(output.length);
				} else if (new String(output, "UTF-8").startsWith("%PDF-")) {
					// Return PDF
					response.setContentType("application/pdf");
					response.setContentLength(output.length);
					response.addHeader("Content-disposition",
							"attachment; filename=remitt-"
									+ System.currentTimeMillis() + ".pdf");
				} else {
					// Assume plain text
					response.setContentType("text/plain");
				}

				// Push byte array of output to servlet output
				response.getOutputStream().write(output);
				response.getOutputStream().flush();
				response.getOutputStream().close();
			} catch (Exception e) {
				log.error(e);
				throw new ServletException(e);
			}
		}
	}

	public void parserTest(String parserClass, String input,
			ServletOutputStream out) throws IOException {
		X12Message p = null;
		try {
			p = (X12Message) Class.forName(parserClass).newInstance();
		} catch (InstantiationException e) {
			log.error(e);
			out.println(e.toString());
			return;
		} catch (IllegalAccessException e) {
			log.error(e);
			out.println(e.toString());
			return;
		} catch (ClassNotFoundException e) {
			log.error(e);
			out.println(e.toString());
			return;
		}
		try {
			p.parse(input);
		} catch (FormatException e) {
			log.error(e);
			out.println(e.toString());
			return;
		}
		out.println(p.toString());
	}

	public void validatorTest(String validatorClass, String input,
			ServletOutputStream out) throws IOException {
		ValidationInterface v = null;
		try {
			v = (ValidationInterface) Class.forName(validatorClass)
					.newInstance();
		} catch (InstantiationException e) {
			log.error(e);
			out.println(e.toString());
			return;
		} catch (IllegalAccessException e) {
			log.error(e);
			out.println(e.toString());
			return;
		} catch (ClassNotFoundException e) {
			log.error(e);
			out.println(e.toString());
			return;
		}
		try {
			ValidationResponse vr = v.validate("", input.getBytes());
			out.println(vr.toString());
			return;
		} catch (Exception e) {
			log.error(e);
			out.println(e.toString());
			return;
		}
	}

}
