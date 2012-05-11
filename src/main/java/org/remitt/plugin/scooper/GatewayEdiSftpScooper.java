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

package org.remitt.plugin.scooper;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.remitt.datastore.KeyringStore;
import org.remitt.server.PGPProvider;

public class GatewayEdiSftpScooper extends SftpScooper {

	static final Logger log = Logger.getLogger(GatewayEdiSftpScooper.class);

	public static String SCOOPER_CLASS = "org.remitt.plugin.scooper.GatewayEdiSftpScooper";
	public static String SCOOPER_ENABLED = "org.remitt.plugin.scooper.GatewayEdiSftpScooper.enabled";

	public static String SFTP_USERNAME = "org.remitt.plugin.scooper.GatewayEdiSftpScooper.sftpUsername";
	public static String SFTP_PASSWORD = "org.remitt.plugin.scooper.GatewayEdiSftpScooper.sftpPassword";

	/**
	 * Name of the tKeyring entry which contains the key for this scooper.
	 */
	public static String GEDI_KEYNAME = "GatewayEDI";

	/**
	 * Host name for this SFTP provider.
	 */
	public static String GEDI_SCOOPER_HOST = "sftp.gatewayedi.com";

	/**
	 * Port number for this SFTP provider.
	 */
	public static Integer GEDI_SCOOPER_PORT = 22;

	/**
	 * Path for the scooper to search on the SFTP host.
	 */
	public static String GEDI_SCOOPER_PATH = "remits";

	@Override
	public byte[] postprocess(byte[] in, String filename) throws Exception {
		return PGPProvider.decryptMessage(
				(InputStream) new ByteArrayInputStream(in),
				(InputStream) new ByteArrayInputStream(KeyringStore.getKey(
						username, GEDI_KEYNAME, true)), null);
	}

	@Override
	public String getHost() {
		return GEDI_SCOOPER_HOST;
	}

	@Override
	public Integer getPort() {
		return GEDI_SCOOPER_PORT;
	}

	@Override
	public String getPath() {
		return GEDI_SCOOPER_PATH;
	}

	@Override
	public String getEnabledConfigValue() {
		return SCOOPER_ENABLED;
	}

}
