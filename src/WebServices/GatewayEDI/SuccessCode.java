/**
 * SuccessCode.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package WebServices.GatewayEDI;

public class SuccessCode implements java.io.Serializable {
	private java.lang.String _value_;
	private static java.util.HashMap _table_ = new java.util.HashMap();

	// Constructor
	protected SuccessCode(java.lang.String value) {
		_value_ = value;
		_table_.put(_value_, this);
	}

	public static final java.lang.String _Success = "Success";
	public static final java.lang.String _ValidationFailure = "ValidationFailure";
	public static final java.lang.String _PayerTimeout = "PayerTimeout";
	public static final java.lang.String _PayerNotSupported = "PayerNotSupported";
	public static final java.lang.String _SystemError = "SystemError";
	public static final java.lang.String _PayerEnrollmentRequired = "PayerEnrollmentRequired";
	public static final java.lang.String _ProviderEnrollmentRequired = "ProviderEnrollmentRequired";
	public static final java.lang.String _ProductRequired = "ProductRequired";
	public static final SuccessCode Success = new SuccessCode(_Success);
	public static final SuccessCode ValidationFailure = new SuccessCode(
			_ValidationFailure);
	public static final SuccessCode PayerTimeout = new SuccessCode(
			_PayerTimeout);
	public static final SuccessCode PayerNotSupported = new SuccessCode(
			_PayerNotSupported);
	public static final SuccessCode SystemError = new SuccessCode(_SystemError);
	public static final SuccessCode PayerEnrollmentRequired = new SuccessCode(
			_PayerEnrollmentRequired);
	public static final SuccessCode ProviderEnrollmentRequired = new SuccessCode(
			_ProviderEnrollmentRequired);
	public static final SuccessCode ProductRequired = new SuccessCode(
			_ProductRequired);

	public java.lang.String getValue() {
		return _value_;
	}

	public static SuccessCode fromValue(java.lang.String value)
			throws java.lang.IllegalArgumentException {
		SuccessCode enumeration = (SuccessCode) _table_.get(value);
		if (enumeration == null)
			throw new java.lang.IllegalArgumentException();
		return enumeration;
	}

	public static SuccessCode fromString(java.lang.String value)
			throws java.lang.IllegalArgumentException {
		return fromValue(value);
	}

	public boolean equals(java.lang.Object obj) {
		return (obj == this);
	}

	public int hashCode() {
		return toString().hashCode();
	}

	public java.lang.String toString() {
		return _value_;
	}

	public java.lang.Object readResolve() throws java.io.ObjectStreamException {
		return fromValue(_value_);
	}

	public static org.apache.axis.encoding.Serializer getSerializer(
			java.lang.String mechType, java.lang.Class _javaType,
			javax.xml.namespace.QName _xmlType) {
		return new org.apache.axis.encoding.ser.EnumSerializer(_javaType,
				_xmlType);
	}

	public static org.apache.axis.encoding.Deserializer getDeserializer(
			java.lang.String mechType, java.lang.Class _javaType,
			javax.xml.namespace.QName _xmlType) {
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
