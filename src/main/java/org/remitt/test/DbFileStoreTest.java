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

import junit.framework.TestCase;

import org.remitt.datastore.DbFileStore;

public class DbFileStoreTest extends TestCase {

	private static final String USERNAME = "Administrator";
	private static final String CATEGORY = "test";
	private static final byte[] TEST_CONTENT = "Test file content.".getBytes();

	public DbFileStoreTest(String s) {
		super(s);
	}

	public void testDbFileStore() throws Exception {
		String filename = DbFileStore.generateFilename("txt");
		assertNotNull("generateFilename != null", filename);
		assertTrue("putFile", DbFileStore.putFile(USERNAME, CATEGORY, filename,
				TEST_CONTENT, 0));
		assertEquals(new String(TEST_CONTENT), new String(DbFileStore.getFile(
				USERNAME, CATEGORY, filename)));
		assertTrue("deleteFile", DbFileStore.deleteFile(USERNAME, CATEGORY,
				filename));
		assertNull("Successfully deleted file.", DbFileStore.getFile(USERNAME,
				CATEGORY, filename));
	}
}
