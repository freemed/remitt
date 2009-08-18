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

import java.beans.PropertyVetoException;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * Servlet implementation class MasterControl. MasterControl is the servlet
 * which runs all background threads and processes other than the logging
 * servlet.
 */
public class MasterControl extends HttpServlet {

	private static final long serialVersionUID = 2009072801000L;

	static final Logger logger = Logger.getLogger(MasterControl.class);

	/**
	 * Default constructor.
	 */
	public MasterControl() {
	}

	public void init() throws ServletException {
		logger.info("MasterControl servlet initializing");
		Configuration.setServletContext(this);
		Configuration.loadConfiguration();

		String jdbcUrl = null;
		String jdbcDriver = null;
		try {
			jdbcUrl = Configuration.getConfiguration().getString("db.url");
			logger.debug("Found db.url string = " + jdbcUrl);
			jdbcDriver = Configuration.getConfiguration()
					.getString("db.driver");
			logger.debug("Found db.driver string = " + jdbcDriver);
		} catch (Exception ex) {
			logger.error("Could not get db.url", ex);
			throw new ServletException();
		}

		try {
			Class.forName(jdbcDriver).newInstance();
		} catch (Exception ex) {
			logger.error("Unable to load driver.", ex);
			throw new ServletException();
		}

		// Connection pool
		ComboPooledDataSource cpds = new ComboPooledDataSource();
		try {
			cpds.setDriverClass(jdbcDriver);
		} catch (PropertyVetoException e) {
			logger.error(e);
			throw new ServletException();
		}
		cpds.setJdbcUrl(jdbcUrl);

		// Set settings from configuration file
		cpds.setMaxStatements(Configuration.getConfiguration().getInt(
				"c3p0.maxStatements"));
		cpds.setMaxIdleTime(Configuration.getConfiguration().getInt(
				"c3p0.maxIdleTime"));

		// Save connection
		Configuration.setComboPooledDataSource(cpds);
		getServletContext().setAttribute("combopooleddatasource", cpds);

		// Start control thread
		ControlThread control = new ControlThread();
		Configuration.setControlThread(control);
		control.setServletContext(this);
		control.start();
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
