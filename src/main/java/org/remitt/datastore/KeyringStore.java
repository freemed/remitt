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

package org.remitt.datastore;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.remitt.prototype.KeyringItem;
import org.remitt.server.Configuration;
import org.remitt.server.DbUtil;

public class KeyringStore {

	static final Logger log = Logger.getLogger(KeyringStore.class);

	public KeyringStore() {
	}

	/**
	 * Remove a key from a user's keyring.
	 * 
	 * @param username
	 * @param keyname
	 * @return Success.
	 */
	public static boolean deleteKey(String username, String keyname) {
		Connection c = Configuration.getConnection();

		boolean success = false;

		PreparedStatement cStmt = null;
		try {
			cStmt = c.prepareStatement("DELETE FROM tKeyring "
					+ " WHERE user = ? " + " AND keyname = ? " + ";");
			cStmt.setString(1, username);
			cStmt.setString(2, keyname);
			cStmt.execute();
			success = true;
		} catch (NullPointerException npe) {
			log.error("Caught NullPointerException", npe);
		} catch (Throwable e) {
			log.error("Caught Throwable", e);
		} finally {
			DbUtil.closeSafely(cStmt);
			DbUtil.closeSafely(c);
		}

		return success;
	}

	/**
	 * Get a key from a user's keyring.
	 * 
	 * @param username
	 *            User name
	 * @param keyname
	 *            Canonical key name
	 * @return Keyring item object.
	 */
	public static KeyringItem getKey(String username, String keyname) {
		Connection c = Configuration.getConnection();

		KeyringItem ret = null;

		PreparedStatement cStmt = null;
		try {
			cStmt = c.prepareStatement("SELECT " + " privatekey "
					+ " , publickey " + " FROM tKeyring "
					+ " WHERE user = ? AND keyname = ? " + ";");
			cStmt.setString(1, username);
			cStmt.setString(2, keyname);

			if (cStmt.execute()) {
				ResultSet rs = cStmt.getResultSet();
				rs.next();
				ret = new KeyringItem();
				ret.setKeyname(keyname);
				ret.setPrivatekey(rs.getBytes(1));
				ret.setPublickey(rs.getBytes(2));
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

	/**
	 * Get keyring for user.
	 * 
	 * @param username
	 * @return All items on keyring as array.
	 */
	public static KeyringItem[] getKeys(String username) {
		Connection c = Configuration.getConnection();

		List<KeyringItem> ret = new ArrayList<KeyringItem>();

		PreparedStatement cStmt = null;
		try {
			cStmt = c.prepareStatement("SELECT " + " keyname "
					+ ", privatekey " + " , publickey " + " FROM tKeyring "
					+ " WHERE user = ? " + ";");
			cStmt.setString(1, username);

			if (cStmt.execute()) {
				ResultSet rs = cStmt.getResultSet();
				while (rs.next()) {
					KeyringItem i = new KeyringItem();
					i.setKeyname(rs.getString(1));
					i.setPrivatekey(rs.getBytes(2));
					i.setPublickey(rs.getBytes(3));
					ret.add(i);
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

		return ret.toArray(new KeyringItem[0]);
	}

	/**
	 * Get a named public or private key from a user's keyring.
	 * 
	 * @param username
	 *            User name
	 * @param keyname
	 *            Canonical key name
	 * @param privateKey
	 *            Whether or not the key being requested is a private key. If
	 *            false, the public key will be returned.
	 * @return Contents of key.
	 */
	public static byte[] getKey(String username, String keyname,
			boolean privateKey) {
		Connection c = Configuration.getConnection();

		byte[] ret = null;

		PreparedStatement cStmt = null;
		try {
			cStmt = c.prepareStatement("SELECT "
					+ (privateKey ? "privatekey" : "publickey")
					+ " FROM tKeyring " + " WHERE user = ? AND keyname = ? "
					+ ";");
			cStmt.setString(1, username);
			cStmt.setString(2, keyname);

			if (cStmt.execute()) {
				ResultSet rs = cStmt.getResultSet();
				rs.next();
				ret = rs.getBytes(1);
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

	/**
	 * Add a key to the user's keyring.
	 * 
	 * @param username
	 * @param keyname
	 * @param privatekey
	 * @param publickey
	 * @return Success.
	 */
	public static boolean putKey(String username, String keyname,
			byte[] privatekey, byte[] publickey) {
		Connection c = Configuration.getConnection();

		boolean success = false;

		try {
			log.trace("Executing deleteKey so we don't have duplicates");
			deleteKey(username, keyname);
		} catch (Exception ex) {
			log.debug(ex);
		}

		PreparedStatement cStmt = null;
		try {
			cStmt = c.prepareStatement("INSERT INTO tKeyring "
					+ " ( user, keyname, privatekey, publickey ) "
					+ " VALUES ( ?, ?, ?, ? ) " + ";");
			cStmt.setString(1, username);
			cStmt.setString(2, keyname);
			cStmt.setBytes(3, privatekey);
			cStmt.setBytes(4, publickey);
			cStmt.execute();
			success = true;
		} catch (NullPointerException npe) {
			log.error("Caught NullPointerException", npe);
		} catch (Throwable e) {
			log.error("Caught Throwable", e);
		} finally {
			DbUtil.closeSafely(cStmt);
			DbUtil.closeSafely(c);
		}

		return success;
	}

}
