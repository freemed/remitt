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

import java.sql.Connection;

import junit.framework.TestCase;

import org.apache.commons.configuration.CompositeConfiguration;
import org.remitt.server.Configuration;

public class ConfigurationTest extends TestCase {

	public ConfigurationTest(String s) {
		super(s);
	}

	public void testAssignments() throws Exception {
		assertNotNull("getConfiguration() != null", Configuration
				.getConfiguration());
		assertTrue(
				"getConfiguration() instanceof CompositeConfiguration",
				Configuration.getConfiguration() instanceof CompositeConfiguration);
		assertNotNull("getConnection != null", Configuration.getConnection());
		assertTrue("getConnection() instanceof Connection", Configuration
				.getConnection() instanceof Connection);
	}

	public void testResolveTranslationPlugin() throws Exception {
		assertEquals(
				"org.remitt.plugin.translation.X12Xml",
				Configuration
						.resolveTranslationPlugin(
								"org.remitt.plugin.render.XsltPlugin",
								"4010_837p",
								"org.remitt.plugin.transport.ScriptedHttpTransport",
								""));
		assertEquals("org.remitt.plugin.translation.FixedFormXml",
				Configuration.resolveTranslationPlugin(
						"org.remitt.plugin.render.XsltPlugin", "cms1500",
						"org.remitt.plugin.transport.StoreFile", ""));
		assertEquals("org.remitt.plugin.translation.FixedFormPdf",
				Configuration.resolveTranslationPlugin(
						"org.remitt.plugin.render.XsltPlugin", "cms1500",
						"org.remitt.plugin.transport.StoreFilePdf", ""));
	}

}
