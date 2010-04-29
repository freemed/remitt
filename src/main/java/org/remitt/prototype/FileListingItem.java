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

package org.remitt.prototype;

import java.io.Serializable;
import java.util.Date;

public class FileListingItem implements Serializable {

	private static final long serialVersionUID = 20100405145800L;

	private String filename;
	private Integer filesize;
	private Date inserted;
	private String originalId;

	public FileListingItem() {
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilesize(Integer filesize) {
		this.filesize = filesize;
	}

	public Integer getFilesize() {
		return filesize;
	}

	public void setInserted(Date inserted) {
		this.inserted = inserted;
	}

	public Date getInserted() {
		return inserted;
	}

	public void setOriginalId(String originalId) {
		this.originalId = originalId;
	}

	public String getOriginalId() {
		return originalId;
	}

}
