/**
 * WSEligibilityInquiry.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package WebServices.GatewayEDI;

public class WSEligibilityInquiry  implements java.io.Serializable {
    private WebServices.GatewayEDI.MyNameValue[] parameters;

    private WebServices.GatewayEDI.WSResponseDataType responseDataType;

    public WSEligibilityInquiry() {
    }

    public WSEligibilityInquiry(
           WebServices.GatewayEDI.MyNameValue[] parameters,
           WebServices.GatewayEDI.WSResponseDataType responseDataType) {
           this.parameters = parameters;
           this.responseDataType = responseDataType;
    }


    /**
     * Gets the parameters value for this WSEligibilityInquiry.
     * 
     * @return parameters
     */
    public WebServices.GatewayEDI.MyNameValue[] getParameters() {
        return parameters;
    }


    /**
     * Sets the parameters value for this WSEligibilityInquiry.
     * 
     * @param parameters
     */
    public void setParameters(WebServices.GatewayEDI.MyNameValue[] parameters) {
        this.parameters = parameters;
    }


    /**
     * Gets the responseDataType value for this WSEligibilityInquiry.
     * 
     * @return responseDataType
     */
    public WebServices.GatewayEDI.WSResponseDataType getResponseDataType() {
        return responseDataType;
    }


    /**
     * Sets the responseDataType value for this WSEligibilityInquiry.
     * 
     * @param responseDataType
     */
    public void setResponseDataType(WebServices.GatewayEDI.WSResponseDataType responseDataType) {
        this.responseDataType = responseDataType;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof WSEligibilityInquiry)) return false;
        WSEligibilityInquiry other = (WSEligibilityInquiry) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.parameters==null && other.getParameters()==null) || 
             (this.parameters!=null &&
              java.util.Arrays.equals(this.parameters, other.getParameters()))) &&
            ((this.responseDataType==null && other.getResponseDataType()==null) || 
             (this.responseDataType!=null &&
              this.responseDataType.equals(other.getResponseDataType())));
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
        if (getParameters() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getParameters());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getParameters(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getResponseDataType() != null) {
            _hashCode += getResponseDataType().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(WSEligibilityInquiry.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("GatewayEDI.WebServices", "WSEligibilityInquiry"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("parameters");
        elemField.setXmlName(new javax.xml.namespace.QName("GatewayEDI.WebServices", "Parameters"));
        elemField.setXmlType(new javax.xml.namespace.QName("GatewayEDI.WebServices", "MyNameValue"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("GatewayEDI.WebServices", "MyNameValue"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("responseDataType");
        elemField.setXmlName(new javax.xml.namespace.QName("GatewayEDI.WebServices", "ResponseDataType"));
        elemField.setXmlType(new javax.xml.namespace.QName("GatewayEDI.WebServices", "WSResponseDataType"));
        elemField.setNillable(false);
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
