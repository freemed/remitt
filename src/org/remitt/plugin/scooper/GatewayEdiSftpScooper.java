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

package org.remitt.plugin.scooper;

import org.apache.log4j.Logger;

public class GatewayEdiSftpScooper extends SftpScooper {

	static final Logger log = Logger.getLogger(GatewayEdiSftpScooper.class);

	public static String SCOOPER_CLASS = "org.remitt.plugin.scooper.GatewayEdiSftpScooper";
	public static String SCOOPER_ENABLED = "org.remitt.plugin.scooper.GatewayEdiSftpScooper.enabled";

	public static String SFTP_USERNAME = "org.remitt.plugin.scooper.GatewayEdiSftpScooper.sftpUsername";
	public static String SFTP_PASSWORD = "org.remitt.plugin.scooper.GatewayEdiSftpScooper.sftpPassword";

	public static String GEDI_SCOOPER_HOST = "sftp.gatewayedi.com";
	public static Integer GEDI_SCOOPER_PORT = 22;
	public static String GEDI_SCOOPER_PATH = "remits";

	@Override
	public byte[] postprocess(byte[] in, String filename) {
		return in;
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
