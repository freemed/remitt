/*
 * $Id$
 *
 * Authors:
 *      Jeff Buchbinder <jeff@freemedsoftware.org>
 *
 * REMITT Electronic Medical Information Translation and Transmission
 * Copyright (C) 1999-2014 FreeMED Software Foundation
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

package org.remitt.plugin.transport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;
import org.remitt.plugin.transport.SftpTransport;
import org.remitt.server.Configuration;

public class ClaimLogicTransport extends SftpTransport {

	static final Logger log = Logger.getLogger(ClaimLogicTransport.class);

	@Override
	protected byte[] prepareInput(byte[] input) {
		String inputString = new String(input);

		String outputType = "";
		if (inputString.startsWith("<?xml ")) {
			outputType = "xml";
		} else if (inputString.startsWith("ISA*")) {
			outputType = "x12";
		} else {
			outputType = "txt";
		}

		String temporaryName = System.currentTimeMillis() + "." + outputType;

		byte[] buffer = new byte[18024];

		log.info("Creating zip container for " + temporaryName);

		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		ZipOutputStream out = new ZipOutputStream(bs);

		try {
			// Set the compression ratio
			out.setLevel(Deflater.DEFAULT_COMPRESSION);

			// Associate a file input stream for the current file
			ByteArrayInputStream in = new ByteArrayInputStream(input);

			// Add ZIP entry to output stream.
			out.putNextEntry(new ZipEntry(temporaryName));

			// Transfer bytes from the current file to the ZIP file
			// out.write(buffer, 0, in.read(buffer));

			int len;
			while ((len = in.read(buffer)) > 0) {
				out.write(buffer, 0, len);
			}

			out.closeEntry();
			in.close();
			out.close();
			bs.close();
		} catch (IllegalArgumentException iae) {
			iae.printStackTrace();
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return bs.toByteArray();
	}

	@Override
	protected String getSftpHost(String userName) {
		return Configuration.getConfiguration().getString(
				"remitt.transport.claimlogic.host");
	}

	@Override
	protected Integer getSftpPort(String userName) {
		return Configuration.getConfiguration().getInt(
				"remitt.transport.claimlogic.port");
	}

	@Override
	protected String getSftpPath(String userName) {
		return Configuration.getConfiguration().getString(
				"remitt.transport.claimlogic.path");
	}

}
