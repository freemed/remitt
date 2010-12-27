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

package org.remitt.test;

import java.io.File;

import junit.framework.TestCase;

import org.apache.cxf.helpers.FileUtils;
import org.remitt.parser.X12Message835;
import org.remitt.parser.x12dto.Remittance;
import org.remitt.server.Configuration;

public class RemittanceTest extends TestCase {

	public RemittanceTest(String s) {
		super(s);
	}

	private String get835testPayload() {
		String data = FileUtils.getStringFromFile(new File(Configuration
				.getServletContext().getServletContext().getRealPath(
						"/WEB-INF/testdata/835.x12")));
		return data;
	}

	public void testParse835toXML() throws Exception {
		String data = get835testPayload();
		X12Message835 parser = new X12Message835();
		parser.parse(data);
		Remittance r = parser.getRemittance();
		assertEquals("5222", r.getTransactionSetControlNumber());
	}

}
