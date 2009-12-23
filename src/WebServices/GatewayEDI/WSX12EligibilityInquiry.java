/**
 * WSX12EligibilityInquiry.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package WebServices.GatewayEDI;

public class WSX12EligibilityInquiry  implements java.io.Serializable {
    private java.lang.String x12Input;

    private java.lang.String gediPayerID;

    private WebServices.GatewayEDI.WSResponseDataType responseDataType;

    public WSX12EligibilityInquiry() {
    }

    public WSX12EligibilityInquiry(
           java.lang.String x12Input,
           java.lang.String gediPayerID,
           WebServices.GatewayEDI.WSResponseDataType responseDataType) {
           this.x12Input = x12Input;
           this.gediPayerID = gediPayerID;
           this.responseDataType = responseDataType;
    }


    /**
     * Gets the x12Input value for this WSX12EligibilityInquiry.
     * 
     * @return x12Input
     */
    public java.lang.String getX12Input() {
        return x12Input;
    }


    /**
     * Sets the x12Input value for this WSX12EligibilityInquiry.
     * 
     * @param x12Input
     */
    public void setX12Input(java.lang.String x12Input) {
        this.x12Input = x12Input;
    }


    /**
     * Gets the gediPayerID value for this WSX12EligibilityInquiry.
     * 
     * @return gediPayerID
     */
    public java.lang.String getGediPayerID() {
        return gediPayerID;
    }


    /**
     * Sets the gediPayerID value for this WSX12EligibilityInquiry.
     * 
     * @param gediPayerID
     */
    public void setGediPayerID(java.lang.String gediPayerID) {
        this.gediPayerID = gediPayerID;
    }


    /**
     * Gets the responseDataType value for this WSX12EligibilityInquiry.
     * 
     * @return responseDataType
     */
    public WebServices.GatewayEDI.WSResponseDataType getResponseDataType() {
        return responseDataType;
    }


    /**
     * Sets the responseDataType value for this WSX12EligibilityInquiry.
     * 
     * @param responseDataType
     */
    public void setResponseDataType(WebServices.GatewayEDI.WSResponseDataType responseDataType) {
        this.responseDataType = responseDataType;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof WSX12EligibilityInquiry)) return false;
        WSX12EligibilityInquiry other = (WSX12EligibilityInquiry) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.x12Input==null && other.getX12Input()==null) || 
             (this.x12Input!=null &&
              this.x12Input.equals(other.getX12Input()))) &&
            ((this.gediPayerID==null && other.getGediPayerID()==null) || 
             (this.gediPayerID!=null &&
              this.gediPayerID.equals(other.getGediPayerID()))) &&
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
        if (getX12Input() != null) {
            _hashCode += getX12Input().hashCode();
        }
        if (getGediPayerID() != null) {
            _hashCode += getGediPayerID().hashCode();
        }
        if (getResponseDataType() != null) {
            _hashCode += getResponseDataType().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(WSX12EligibilityInquiry.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("GatewayEDI.WebServices", "WSX12EligibilityInquiry"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("x12Input");
        elemField.setXmlName(new javax.xml.namespace.QName("GatewayEDI.WebServices", "X12Input"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("gediPayerID");
        elemField.setXmlName(new javax.xml.namespace.QName("GatewayEDI.WebServices", "GediPayerID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
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
