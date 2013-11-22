/*
 * $Id$
 * 
 * Authors: Jeff Buchbinder <jeff@freemedsoftware.org>
 * 
 * REMITT Electronic Medical Information Translation and Transmission
 * Copyright (C) 1999-2014 FreeMED Software Foundation
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 675 Mass
 * Ave, Cambridge, MA 02139, USA.
 */

function transport() {
	loginfo("Executing FreeClaims.js");

	loginfo("Loading login page");

	// Disable javascript due to bum login page
	webClient.setJavaScriptEnabled(false);

	loginPage = webClient
			.getPage("https://sfreeclaims.anvicare.com/docs/member_login.asp");
	loginForm = loginPage.getFormByName("loginForm");
	loginForm.getInputByName("username").setValueAttribute(username);
	loginForm.getInputByName("userpassword").setValueAttribute(password);
	loggedInPage = loginPage.getByXPath("//INPUT[@value='Login']").get(0)
			.click();
	validLogin = false;

	// Check for nav page for login
	body = loggedInPage.asXml();
	loginfo(body);
	if (body.indexOf('/docs/mynav.htm') != -1) {
		loginfo("Successfully logged in with username " + username);
		validLogin = true;
	}

	// TODO: validate form submission
	if (validLogin) {
		loginfo("Loading upload page");
		uploadPage = webClient
				.getPage("https://sfreeclaims.anvicare.com/docs/upload.asp");
		uploadForm = uploadPage.getFormByName("Upload");

		// Upload using full file upload method from temp file
		loginfo("Dumping byte array to temporary file for upload");
		f = File.createTempFile("freeclaims", ".x12");
		FileUtils.writeByteArrayToFile(f, input);

		// Set the path to the temporary file for upload
		uploadForm.getInputByName("file1").setValueAttribute(
				f.getAbsolutePath());

		// Force upload submit
		uploadedPage = uploadPage.getByXPath("//INPUT[@name='submit1']").get(0)
				.click();

		// Clean up
		// f.delete();

		loginfo("Upload response page: " + uploadedPage.asXml());
	} else {
		logerror("Failed to login with username '" + username + "'");
	}

	loginfo("Ending FreeClaims.js script");

	return _log;
}

