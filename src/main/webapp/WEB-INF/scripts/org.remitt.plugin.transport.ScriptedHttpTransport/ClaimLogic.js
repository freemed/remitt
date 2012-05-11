/*
 * $Id$
 * 
 * Authors: Jeff Buchbinder <jeff@freemedsoftware.org>
 * 
 * REMITT Electronic Medical Information Translation and Transmission
 * Copyright (C) 1999-2012 FreeMED Software Foundation
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
	loginfo("Executing ClaimLogic.js");

	loginfo("Loading login page");

	webClient.setJavaScriptEnabled(false);

	loginPage = webClient
			.getPage("https://www.claimlogic.com/start/index2.html");
	loginForm = loginPage.getFormByName("main");
	loginForm.getInputByName("session_user_id").setValueAttribute(username);
	loginForm.getInputByName("session_password").setValueAttribute(password);
	loginfo("Loading authentication page");
	loggedInPage = loginPage.getByXPath("//INPUT[@value='Login']").get(0)
			.click();
	validLogin = false;

	// Check for nav page for login
	body = loggedInPage.asXml();
	loginfo(body);
	if (body.indexOf('go_main') != -1) {
		loginfo("Successfully logged in with username " + username);
		validLogin = true;
	} else {
		logerror("Failed to log in.");
		loginfo(body);
	}

	if (validLogin) {
		loginfo("Extracting login token");
		token = loggedInPage.getFormByName("go_main").getInputByName(
				"login_token").getValueAttribute();
		loginfo("Got login_token " + token);
		loginfo("Attempting to load main page with login token");

		webClient.setJavaScriptEnabled(true);

		uploadPage = webClient
				.getPage("https://www.claimlogic.com/v3/upload_form.php?user_id="
						+ username + "&login_token=" + token);
		loginfo(uploadPage.asXml());

		uploadForm = uploadPage.getFormByName("upload_file");
		uploadForm.getSelectByName("file_type").setSelectedAttribute("Claims",
				true);
		uploadForm.getSelectByName("test_mode").setSelectedAttribute("Test",
				true);
		// uploadForm.getInputByName("test_mode").setSelectedAttribute("Production",
		// true);

		// Upload using full file upload method from temp file
		loginfo("Dumping byte array to temporary file for upload");
		f = File.createTempFile("claimlogic", ".x12");
		FileUtils.writeByteArrayToFile(f, input);

		// Set the path to the temporary file for upload
		uploadForm.getInputByName("upload_file").setValueAttribute(
				f.getAbsolutePath());

		// Click on A tag for send_file
		loginfo("Submitting upload form");
		uploadedPage = uploadPage.getAnchorByHref("javascript:send_file()")
				.click();

		// Clean up
		// f.delete();

		loginfo("Got response page : " + uploadedPage.asXml());
	} else {
		logerror("Failed to login with username '" + username + "'");
	}

	loginfo("Ending ClaimLogic.js script");

	return _log;
}

