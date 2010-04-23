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

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.HashMap;

import javax.xml.namespace.QName;

import WebServices.GatewayEDI.SuccessCode;

public class EligibilityStatus implements Serializable {

	private static final long serialVersionUID = 1L;

	private String _value_;
	private static HashMap<String, EligibilityStatus> _table_ = new HashMap<String, EligibilityStatus>();

	public EligibilityStatus() {
	}

	// Constructor
	protected EligibilityStatus(String value) {
		_value_ = value;
		_table_.put(_value_, this);
	}

	public static final String _OK = "OK";
	public static final String _BAD = "BAD";
	public static final String _CONTINUATION = "CONTINUATION";
	public static final String _SERVER_ERROR = "SERVER_ERROR";
	public static final EligibilityStatus OK = new EligibilityStatus(_OK);
	public static final EligibilityStatus BAD = new EligibilityStatus(_BAD);
	public static final EligibilityStatus CONTINUATION = new EligibilityStatus(
			_CONTINUATION);
	public static final EligibilityStatus SERVER_ERROR = new EligibilityStatus(
			_SERVER_ERROR);

	public String getValue() {
		return _value_;
	}

	public static EligibilityStatus fromValue(String value)
			throws IllegalArgumentException {
		EligibilityStatus enumeration = (EligibilityStatus) _table_.get(value);
		if (enumeration == null)
			throw new IllegalArgumentException();
		return enumeration;
	}

	public static EligibilityStatus fromString(String value)
			throws IllegalArgumentException {
		return fromValue(value);
	}

	public boolean equals(java.lang.Object obj) {
		return (obj == this);
	}

	public int hashCode() {
		return toString().hashCode();
	}

	public String toString() {
		return _value_;
	}

	public Object readResolve() throws ObjectStreamException {
		return fromValue(_value_);
	}

	public static org.apache.axis.encoding.Serializer getSerializer(
			String mechType, Class _javaType, QName _xmlType) {
		return new org.apache.axis.encoding.ser.EnumSerializer(_javaType,
				_xmlType);
	}

	public static org.apache.axis.encoding.Deserializer getDeserializer(
			String mechType, Class _javaType, QName _xmlType) {
		return new org.apache.axis.encoding.ser.EnumDeserializer(_javaType,
				_xmlType);
	}

	// Type metadata
	private static org.apache.axis.description.TypeDesc typeDesc = new org.apache.axis.description.TypeDesc(
			SuccessCode.class);

	static {
		typeDesc.setXmlType(new javax.xml.namespace.QName(
				"GatewayEDI.WebServices", "SuccessCode"));
	}

	/**
	 * Return type metadata object
	 */
	public static org.apache.axis.description.TypeDesc getTypeDesc() {
		return typeDesc;
	}

}
