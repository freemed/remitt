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

package org.remitt.datastore;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.remitt.prototype.UserDTO;
import org.remitt.server.Configuration;
import org.remitt.server.DbUtil;

public class UserManagement {

	static final Logger log = Logger.getLogger(UserManagement.class);

	public static final String SQL_GET_USER = "SELECT "
			+ " u.username AS username " + " , u.contactemail AS contactemail "
			+ " , u.callbackserviceuri AS callbackserviceuri "
			+ " , u.callbackservicewsdluri AS callbackservicewsdluri "
			+ " , u.callbackusername AS callbackusername"
			+ " , u.callbackpassword AS callbackpassword "
			+ " , GROUP_CONCAT(r.rolename) AS roles " + " FROM tUser u "
			+ " LEFT OUTER JOIN tRole r ON r.username = u.username "
			+ " WHERE u.username = ? " + " GROUP BY u.username " + " LIMIT 1 "
			+ ";";

	public static final String SQL_LIST_USERS = "SELECT "
			+ " u.username AS username " + " , u.contactemail AS contactemail "
			+ " , u.callbackserviceuri AS callbackserviceuri "
			+ " , u.callbackservicewsdluri AS callbackservicewsdluri "
			+ " , u.callbackusername AS callbackusername"
			+ " , u.callbackpassword AS callbackpassword "
			+ " , GROUP_CONCAT(r.rolename) AS roles " + " FROM tUser u "
			+ " LEFT OUTER JOIN tRole r ON r.username = u.username "
			+ " GROUP BY u.username " + ";";

	public UserManagement() {
	}

	/**
	 * Add REMITT user with "default" role.
	 * 
	 * @param username
	 * @param password
	 * @param callbackServiceUri
	 * @param callbackServiceWsdlUri
	 * @param callbackUsername
	 * @param callbackPassword
	 * @return Success.
	 */
	public static boolean addUser(String username, String password,
			String callbackServiceUri, String callbackServiceWsdlUri,
			String callbackUsername, String callbackPassword) {
		Connection c = Configuration.getConnection();

		PreparedStatement cStmt = null;
		try {
			cStmt = c.prepareStatement("INSERT INTO tUser "
					+ " (  username, passhash, "
					+ " callbackserviceuri, callbackservicewsdluri, "
					+ " callbackusername, callbackpassword ) "
					+ " VALUES ( ?, MD5(?), ?, ?, ?, ? ) " + ";");
			cStmt.setString(1, username);
			cStmt.setString(2, password);
			cStmt.setString(3, callbackServiceUri);
			cStmt.setString(4, callbackServiceWsdlUri);
			cStmt.setString(5, callbackUsername);
			cStmt.setString(6, callbackPassword);
			cStmt.execute();
		} catch (NullPointerException npe) {
			log.error("Caught NullPointerException", npe);
			DbUtil.closeSafely(cStmt);
			DbUtil.closeSafely(c);
			return false;
		} catch (Throwable e) {
			DbUtil.closeSafely(cStmt);
			DbUtil.closeSafely(c);
			return false;
		}

		boolean status = false;
		PreparedStatement cStmt2 = null;
		try {
			cStmt2 = c.prepareStatement("INSERT INTO tRole "
					+ " ( username, rolename ) " + " VALUES ( ?, ? ) " + ";");
			cStmt2.setString(1, username);
			cStmt2.setString(2, "default");
			cStmt2.execute();
			c.close();
			status = true;
		} catch (NullPointerException npe) {
			log.error("Caught NullPointerException", npe);
		} catch (Throwable e) {
			log.error(e);
		} finally {
			DbUtil.closeSafely(cStmt);
			DbUtil.closeSafely(c);
		}

		return status;
	}

	/**
	 * Retrieve a single <UserDTO> object for a named user.
	 * 
	 * @param username
	 * @return User object.
	 */
	public static UserDTO getUser(String username) {
		Connection c = Configuration.getConnection();

		UserDTO ret = new UserDTO();

		PreparedStatement cStmt = null;
		try {
			cStmt = c.prepareStatement(SQL_GET_USER);
			log.debug(SQL_GET_USER + " with param " + username);
			cStmt.setString(1, username);
			cStmt.execute();
			ResultSet rs = cStmt.getResultSet();
			rs.next();
			ret.setUsername(rs.getString(1));
			ret.setContactEmail(rs.getString(2));
			ret.setCallbackServiceUri(rs.getString(3));
			ret.setCallbackServiceWsdlUri(rs.getString(4));
			ret.setCallbackUsername(rs.getString(5));
			ret.setCallbackPassword(rs.getString(6));
			ret.setRoles(rs.getString(7).split(","));
			rs.close();
		} catch (NullPointerException npe) {
			log.error("Caught NullPointerException", npe);
		} catch (Throwable e) {
			log.error(e);
		} finally {
			DbUtil.closeSafely(cStmt);
			DbUtil.closeSafely(c);
		}

		return ret;
	}

	/**
	 * Get list of all users in the system.
	 * 
	 * @return List of user objects.
	 */
	public static List<UserDTO> listUsers() {
		Connection c = Configuration.getConnection();

		List<UserDTO> ret = new ArrayList<UserDTO>();

		PreparedStatement cStmt = null;
		try {
			cStmt = c.prepareStatement(SQL_LIST_USERS);
			if (cStmt.execute()) {
				ResultSet rs = cStmt.getResultSet();
				while (rs.next()) {
					UserDTO u = new UserDTO();
					u.setUsername(rs.getString("username"));
					u.setContactEmail(rs.getString("contactemail"));
					u.setCallbackServiceUri(rs.getString("callbackserviceuri"));
					u.setCallbackServiceWsdlUri(rs
							.getString("callbackservicewsdluri"));
					u.setCallbackUsername(rs.getString("callbackusername"));
					u.setCallbackPassword(rs.getString("callbackpassword"));
					u.setRoles(rs.getString("roles").split(","));
					ret.add(u);
				}
				rs.close();
			}
		} catch (NullPointerException npe) {
			log.error("Caught NullPointerException", npe);
		} catch (Throwable e) {
		} finally {
			DbUtil.closeSafely(cStmt);
			DbUtil.closeSafely(c);
		}

		return ret;
	}

}
