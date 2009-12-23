/**
 * ValidationFailure.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package WebServices.GatewayEDI;

public class ValidationFailure  implements java.io.Serializable {
    private java.lang.String message;

    private java.lang.String[] affectedFields;

    public ValidationFailure() {
    }

    public ValidationFailure(
           java.lang.String message,
           java.lang.String[] affectedFields) {
           this.message = message;
           this.affectedFields = affectedFields;
    }


    /**
     * Gets the message value for this ValidationFailure.
     * 
     * @return message
     */
    public java.lang.String getMessage() {
        return message;
    }


    /**
     * Sets the message value for this ValidationFailure.
     * 
     * @param message
     */
    public void setMessage(java.lang.String message) {
        this.message = message;
    }


    /**
     * Gets the affectedFields value for this ValidationFailure.
     * 
     * @return affectedFields
     */
    public java.lang.String[] getAffectedFields() {
        return affectedFields;
    }


    /**
     * Sets the affectedFields value for this ValidationFailure.
     * 
     * @param affectedFields
     */
    public void setAffectedFields(java.lang.String[] affectedFields) {
        this.affectedFields = affectedFields;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ValidationFailure)) return false;
        ValidationFailure other = (ValidationFailure) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.message==null && other.getMessage()==null) || 
             (this.message!=null &&
              this.message.equals(other.getMessage()))) &&
            ((this.affectedFields==null && other.getAffectedFields()==null) || 
             (this.affectedFields!=null &&
              java.util.Arrays.equals(this.affectedFields, other.getAffectedFields())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getMessage() != null) {
            _hashCode += getMessage().hashCode();
        }
        if (getAffectedFields() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getAffectedFields());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getAffectedFields(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ValidationFailure.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("GatewayEDI.WebServices", "ValidationFailure"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("message");
        elemField.setXmlName(new javax.xml.namespace.QName("GatewayEDI.WebServices", "Message"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("affectedFields");
        elemField.setXmlName(new javax.xml.namespace.QName("GatewayEDI.WebServices", "AffectedFields"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("GatewayEDI.WebServices", "string"));
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
