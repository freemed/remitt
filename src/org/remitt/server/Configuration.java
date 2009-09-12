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

import java.io.File;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServlet;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.log4j.Logger;
import org.remitt.prototype.PluginInterface;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class Configuration {

	public static String DEFAULT_CONFIG = "/WEB-INF/remitt.properties";
	public static String OVERRIDE_CONFIG = System.getProperty("properties");

	static final Logger log = Logger.getLogger(Configuration.class);

	protected static CompositeConfiguration compositeConfiguration = null;

	protected static HttpServlet servletContext = null;

	protected static ControlThread controlThread = null;

	protected static ComboPooledDataSource comboPooledDataSource = null;

	/**
	 * Get servlet object.
	 * 
	 * @return
	 */
	public static HttpServlet getServletContext() {
		return servletContext;
	}

	/**
	 * Store servlet object.
	 * 
	 * @param hS
	 */
	public static void setServletContext(HttpServlet hS) {
		servletContext = hS;
	}

	/**
	 * Get current global configuration object.
	 * 
	 * @return
	 */
	public static CompositeConfiguration getConfiguration() {
		if (compositeConfiguration == null) {
			Configuration.loadConfiguration();
		}
		if (compositeConfiguration == null) {
			log
					.error("Should never be null here, configuration is failing to load!");
		}
		return compositeConfiguration;
	}

	/**
	 * Load configuration from both template and override properties files.
	 */
	public static void loadConfiguration() {
		log.trace("Entered loadConfiguration");
		if (compositeConfiguration == null) {
			log.info("Configuration object not present, instantiating");
			compositeConfiguration = new CompositeConfiguration();

			PropertiesConfiguration defaults = null;
			try {
				defaults = new PropertiesConfiguration(servletContext
						.getServletContext().getRealPath(DEFAULT_CONFIG));
				log.info("Loading default configuration from "
						+ servletContext.getServletContext().getRealPath(
								DEFAULT_CONFIG));
			} catch (ConfigurationException e) {
				log.error("Could not load default configuration from "
						+ servletContext.getServletContext().getRealPath(
								DEFAULT_CONFIG));
				e.printStackTrace();
			}
			if (OVERRIDE_CONFIG != null) {
				PropertiesConfiguration overrides = null;
				try {
					overrides = new PropertiesConfiguration();
					overrides.setFile(new File(OVERRIDE_CONFIG));
					overrides
							.setReloadingStrategy(new FileChangedReloadingStrategy());
					overrides.load();
				} catch (ConfigurationException e) {
					log.info("Could not load overrides", e);
				}
				compositeConfiguration.addConfiguration(overrides);
			}
			// Afterwards, add defaults so they're read second.
			compositeConfiguration.addConfiguration(defaults);
		}
	}

	public static void setControlThread(ControlThread ct) {
		controlThread = ct;
	}

	public static ControlThread getControlThread() {
		return controlThread;
	}

	public static String getInstallLocation() {
		String home = System.getenv("REMITT_HOME");
		return (home == "") ? "." : home;
	}

	public static Connection getConnection() {
		try {
			return comboPooledDataSource.getConnection();
		} catch (SQLException e) {
			log.error(e);
			return null;
		}
	}

	public static ComboPooledDataSource getComboPooledDataSource() {
		return comboPooledDataSource;
	}

	public static void setComboPooledDataSource(ComboPooledDataSource c) {
		comboPooledDataSource = c;
	}

	/**
	 * Retrieve user-set configuration option for a particular plugin namespace.
	 * 
	 * @param plugin
	 *            Fully qualified class name with path (example:
	 *            "org.remitt.plugin.render.XsltPlugin")
	 * @param userName
	 *            Corresponds to username field in tUser table.
	 * @param option
	 *            Option string name.
	 * @return
	 */
	public static String getPluginOption(PluginInterface plugin,
			String userName, String option) {
		Connection c = getConnection();
		String className = plugin.getClass().getName();

		PreparedStatement cStmt = null;
		try {
			cStmt = c.prepareStatement("SELECT cValue FROM tUserConfig WHERE "
					+ " cNamespace=? AND user=? AND cOption=?;");

			cStmt.setString(1, className);
			cStmt.setString(2, userName);
			cStmt.setString(3, option);
			boolean hasResult = cStmt.execute();
			if (hasResult) {
				ResultSet r = cStmt.getResultSet();
				r.next();
				String ret = r.getString(1);
				c.close();
				return ret;
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
			log.error("Caught SQLException", e);
			log.info("Statement: SELECT cValue FROM tUserConfig WHERE "
					+ "cNameSpace = '" + className + "' AND user = '"
					+ userName + "' AND cOption = " + option);
			if (c != null) {
				try {
					c.close();
				} catch (SQLException e1) {
					log.error(e1);
				}
			}
		}

		return "";
	}

	/**
	 * Set the configuration value for an option.
	 * 
	 * @param user
	 * @param namespace
	 * @param option
	 * @param value
	 */
	public static void setConfigValue(String user, String namespace,
			String option, String value) {
		log.info("setConfigValue: " + user + ", " + namespace + ", " + option
				+ ", " + value);

		Connection c = Configuration.getConnection();
		CallableStatement cStmt = null;
		try {
			cStmt = c.prepareCall("{ CALL p_UserConfigUpdate( ?, ?, ?, ? ); }");
			cStmt.setString(1, user);
			cStmt.setString(2, namespace);
			cStmt.setString(3, option);
			cStmt.setString(4, value);

			cStmt.execute();
			cStmt.getResultSet();
		} catch (NullPointerException npe) {
			log.error("Caught NullPointerException", npe);
		} catch (SQLException e) {
			log.error("Caught SQLException", e);
		}
	}

	/**
	 * Get a constructed file name based on the jobId presented with a
	 * particular file extension.
	 * 
	 * @param jobId
	 * @param fileExtension
	 * @return
	 */
	public static String getFileName(Integer jobId, String fileExtension) {
		return jobId.toString() + "-" + System.currentTimeMillis() + "."
				+ fileExtension;
	}

}
