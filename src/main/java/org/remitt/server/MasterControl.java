/*
 * $Id$
 *
 * Authors:
 *      Jeff Buchbinder <jeff@freemedsoftware.org>
 *
 * REMITT Electronic Medical Information Translation and Transmission
 * Copyright (C) 1999-2012 FreeMED Software Foundation
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

import it.sauronsoftware.cron4j.Scheduler;
import it.sauronsoftware.cron4j.SchedulerListener;
import it.sauronsoftware.cron4j.SchedulingPattern;
import it.sauronsoftware.cron4j.Task;
import it.sauronsoftware.cron4j.TaskCollector;
import it.sauronsoftware.cron4j.TaskExecutor;
import it.sauronsoftware.cron4j.TaskTable;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.remitt.datastore.DbPatch;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * Servlet implementation class MasterControl. MasterControl is the servlet
 * which runs all background threads and processes other than the logging
 * servlet.
 */
public class MasterControl extends HttpServlet {

	private static final long serialVersionUID = 2009072801000L;

	static final Logger log = Logger.getLogger(MasterControl.class);

	static final String PATCH_PATH = "/WEB-INF/dbpatch";

	private static Scheduler scheduler = null;

	/**
	 * Default constructor.
	 */
	public MasterControl() {
	}

	public void init() throws ServletException {
		log.info("MasterControl servlet initializing");
		System.out.println("Master Control setting ServletContext");
		Configuration.setServletContext(this);
		System.out.println("Master Control loading Configuration");
		Configuration.loadConfiguration();

		String jdbcUrl = null;
		String jdbcDriver = null;
		System.out.println("Master Control creating db connections");
		try {
			jdbcUrl = Configuration.getConfiguration().getString("db.url");
			log.debug("Found db.url string = " + jdbcUrl);
			jdbcDriver = Configuration.getConfiguration()
					.getString("db.driver");
			log.debug("Found db.driver string = " + jdbcDriver);
		} catch (Exception ex) {
			log.error("Could not get db.url", ex);
			throw new ServletException();
		}

		try {
			Class.forName(jdbcDriver).newInstance();
		} catch (Exception ex) {
			log.error("Unable to load driver.", ex);
			throw new ServletException();
		}

		// Connection pool
		ComboPooledDataSource cpds = new ComboPooledDataSource();
		try {
			cpds.setDriverClass(jdbcDriver);
		} catch (PropertyVetoException e) {
			log.error(e);
			throw new ServletException();
		}
		cpds.setJdbcUrl(jdbcUrl);
		cpds.setDataSourceName("jdbc/remitt");

		// Set settings from configuration file
		cpds.setMaxStatements(Configuration.getConfiguration().getInt(
				"c3p0.maxStatements"));
		cpds.setMaxIdleTime(Configuration.getConfiguration().getInt(
				"c3p0.maxIdleTime"));

		// Save connection
		Configuration.setComboPooledDataSource(cpds);
		getServletContext().setAttribute("combopooleddatasource", cpds);

		// Attempt db patching before we do anything crazy
		DbPatch.dbPatcher(getServletContext().getRealPath(PATCH_PATH));

		// Start control thread
		ControlThread control = new ControlThread();
		Configuration.setControlThread(control);
		control.setServletContext(this);
		control.start();

		// Start scheduler
		scheduler = new Scheduler();
		scheduler.addSchedulerListener(new SchedulerListener() {
			@Override
			public void taskFailed(TaskExecutor arg0, Throwable arg1) {
				log.error("Cron task FAILED: " + arg0.getTask().toString()
						+ " (" + arg1.getMessage() + ")");
			}

			@Override
			public void taskLaunching(TaskExecutor arg0) {
				log.info("Cron task launching: " + arg0.getTask().toString());
			}

			@Override
			public void taskSucceeded(TaskExecutor arg0) {
				log.info("Cron task succeeded: " + arg0.getTask().toString()
						+ " (" + arg0.getStatusMessage() + ")");
			}
		});
		scheduler.addTaskCollector(new TaskCollector() {
			@Override
			public TaskTable getTasks() {
				TaskTable t = new TaskTable();

				Connection c = Configuration.getConnection();

				PreparedStatement cStmt = null;
				try {
					cStmt = c
							.prepareStatement("SELECT jobSchedule, jobClass FROM tJobs "
									+ " WHERE jobEnabled = TRUE");

					if (cStmt.execute()) {
						ResultSet r = cStmt.getResultSet();
						while (r.next()) {
							String jobSchedule = r.getString(1);
							String jobClass = r.getString(2);
							shoeHornTask(t, jobSchedule, jobClass);
						}
						r.close();
						c.close();
					}
				} catch (NullPointerException npe) {
					log.error("Caught NullPointerException", npe);
					if (c != null) {
						try {
							c.close();
						} catch (SQLException e1) {
							log.error(e1);
						}
					}
				} catch (SQLException e) {
					log.trace("Caught SQLException", e);
					if (c != null) {
						try {
							c.close();
						} catch (SQLException e1) {
							log.error(e1);
						}
					}
				}

				// TODO: form task table.
				return t;
			}

			protected void shoeHornTask(TaskTable tt, String schedule,
					String className) {
				Task t = null;
				try {
					t = (Task) Class.forName(className).newInstance();
				} catch (ClassNotFoundException x) {
					log
							.error("Attempted to instantiate task for non-existant class "
									+ className);
				} catch (InstantiationException e) {
					log.error("Failed to instantiate task class " + className);
				} catch (IllegalAccessException e) {
					log.error("No permissions to access class " + className);
				} finally {
					SchedulingPattern s = new SchedulingPattern(schedule);
					tt.add(s, t);
				}
			}
		});
		log.info("Starting scheduler");
		scheduler.start();
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

	@Override
	public void destroy() {
		log.info("Stopping scheduler");
		scheduler.stop();
		super.destroy();
	}

}
