/**
 * ValidationFailureCollection.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package WebServices.GatewayEDI;

public class ValidationFailureCollection  implements java.io.Serializable {
    private java.lang.String[] allMessages;

    private WebServices.GatewayEDI.ValidationFailure[] failures;

    public ValidationFailureCollection() {
    }

    public ValidationFailureCollection(
           java.lang.String[] allMessages,
           WebServices.GatewayEDI.ValidationFailure[] failures) {
           this.allMessages = allMessages;
           this.failures = failures;
    }


    /**
     * Gets the allMessages value for this ValidationFailureCollection.
     * 
     * @return allMessages
     */
    public java.lang.String[] getAllMessages() {
        return allMessages;
    }


    /**
     * Sets the allMessages value for this ValidationFailureCollection.
     * 
     * @param allMessages
     */
    public void setAllMessages(java.lang.String[] allMessages) {
        this.allMessages = allMessages;
    }


    /**
     * Gets the failures value for this ValidationFailureCollection.
     * 
     * @return failures
     */
    public WebServices.GatewayEDI.ValidationFailure[] getFailures() {
        return failures;
    }


    /**
     * Sets the failures value for this ValidationFailureCollection.
     * 
     * @param failures
     */
    public void setFailures(WebServices.GatewayEDI.ValidationFailure[] failures) {
        this.failures = failures;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ValidationFailureCollection)) return false;
        ValidationFailureCollection other = (ValidationFailureCollection) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.allMessages==null && other.getAllMessages()==null) || 
             (this.allMessages!=null &&
              java.util.Arrays.equals(this.allMessages, other.getAllMessages()))) &&
            ((this.failures==null && other.getFailures()==null) || 
             (this.failures!=null &&
              java.util.Arrays.equals(this.failures, other.getFailures())));
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
        if (getAllMessages() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getAllMessages());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getAllMessages(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getFailures() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getFailures());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getFailures(), i);
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
        new org.apache.axis.description.TypeDesc(ValidationFailureCollection.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("GatewayEDI.WebServices", "ValidationFailureCollection"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("allMessages");
        elemField.setXmlName(new javax.xml.namespace.QName("GatewayEDI.WebServices", "AllMessages"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("GatewayEDI.WebServices", "string"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("failures");
        elemField.setXmlName(new javax.xml.namespace.QName("GatewayEDI.WebServices", "Failures"));
        elemField.setXmlType(new javax.xml.namespace.QName("GatewayEDI.WebServices", "ValidationFailure"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("GatewayEDI.WebServices", "ValidationFailure"));
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
