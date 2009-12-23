/**
 * WSX12EligibilityResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package WebServices.GatewayEDI;

public class WSX12EligibilityResponse  implements java.io.Serializable {
    private java.lang.String responseAsRawString;

    private java.lang.String responseAsXml;

    private WebServices.GatewayEDI.ValidationFailureCollection extraProcessingInfo;

    private WebServices.GatewayEDI.SuccessCode successCode;

    private WebServices.GatewayEDI.WSX12EligibilityInquiry originalInquiry;

    public WSX12EligibilityResponse() {
    }

    public WSX12EligibilityResponse(
           java.lang.String responseAsRawString,
           java.lang.String responseAsXml,
           WebServices.GatewayEDI.ValidationFailureCollection extraProcessingInfo,
           WebServices.GatewayEDI.SuccessCode successCode,
           WebServices.GatewayEDI.WSX12EligibilityInquiry originalInquiry) {
           this.responseAsRawString = responseAsRawString;
           this.responseAsXml = responseAsXml;
           this.extraProcessingInfo = extraProcessingInfo;
           this.successCode = successCode;
           this.originalInquiry = originalInquiry;
    }


    /**
     * Gets the responseAsRawString value for this WSX12EligibilityResponse.
     * 
     * @return responseAsRawString
     */
    public java.lang.String getResponseAsRawString() {
        return responseAsRawString;
    }


    /**
     * Sets the responseAsRawString value for this WSX12EligibilityResponse.
     * 
     * @param responseAsRawString
     */
    public void setResponseAsRawString(java.lang.String responseAsRawString) {
        this.responseAsRawString = responseAsRawString;
    }


    /**
     * Gets the responseAsXml value for this WSX12EligibilityResponse.
     * 
     * @return responseAsXml
     */
    public java.lang.String getResponseAsXml() {
        return responseAsXml;
    }


    /**
     * Sets the responseAsXml value for this WSX12EligibilityResponse.
     * 
     * @param responseAsXml
     */
    public void setResponseAsXml(java.lang.String responseAsXml) {
        this.responseAsXml = responseAsXml;
    }


    /**
     * Gets the extraProcessingInfo value for this WSX12EligibilityResponse.
     * 
     * @return extraProcessingInfo
     */
    public WebServices.GatewayEDI.ValidationFailureCollection getExtraProcessingInfo() {
        return extraProcessingInfo;
    }


    /**
     * Sets the extraProcessingInfo value for this WSX12EligibilityResponse.
     * 
     * @param extraProcessingInfo
     */
    public void setExtraProcessingInfo(WebServices.GatewayEDI.ValidationFailureCollection extraProcessingInfo) {
        this.extraProcessingInfo = extraProcessingInfo;
    }


    /**
     * Gets the successCode value for this WSX12EligibilityResponse.
     * 
     * @return successCode
     */
    public WebServices.GatewayEDI.SuccessCode getSuccessCode() {
        return successCode;
    }


    /**
     * Sets the successCode value for this WSX12EligibilityResponse.
     * 
     * @param successCode
     */
    public void setSuccessCode(WebServices.GatewayEDI.SuccessCode successCode) {
        this.successCode = successCode;
    }


    /**
     * Gets the originalInquiry value for this WSX12EligibilityResponse.
     * 
     * @return originalInquiry
     */
    public WebServices.GatewayEDI.WSX12EligibilityInquiry getOriginalInquiry() {
        return originalInquiry;
    }


    /**
     * Sets the originalInquiry value for this WSX12EligibilityResponse.
     * 
     * @param originalInquiry
     */
    public void setOriginalInquiry(WebServices.GatewayEDI.WSX12EligibilityInquiry originalInquiry) {
        this.originalInquiry = originalInquiry;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof WSX12EligibilityResponse)) return false;
        WSX12EligibilityResponse other = (WSX12EligibilityResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.responseAsRawString==null && other.getResponseAsRawString()==null) || 
             (this.responseAsRawString!=null &&
              this.responseAsRawString.equals(other.getResponseAsRawString()))) &&
            ((this.responseAsXml==null && other.getResponseAsXml()==null) || 
             (this.responseAsXml!=null &&
              this.responseAsXml.equals(other.getResponseAsXml()))) &&
            ((this.extraProcessingInfo==null && other.getExtraProcessingInfo()==null) || 
             (this.extraProcessingInfo!=null &&
              this.extraProcessingInfo.equals(other.getExtraProcessingInfo()))) &&
            ((this.successCode==null && other.getSuccessCode()==null) || 
             (this.successCode!=null &&
              this.successCode.equals(other.getSuccessCode()))) &&
            ((this.originalInquiry==null && other.getOriginalInquiry()==null) || 
             (this.originalInquiry!=null &&
              this.originalInquiry.equals(other.getOriginalInquiry())));
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
        if (getResponseAsRawString() != null) {
            _hashCode += getResponseAsRawString().hashCode();
        }
        if (getResponseAsXml() != null) {
            _hashCode += getResponseAsXml().hashCode();
        }
        if (getExtraProcessingInfo() != null) {
            _hashCode += getExtraProcessingInfo().hashCode();
        }
        if (getSuccessCode() != null) {
            _hashCode += getSuccessCode().hashCode();
        }
        if (getOriginalInquiry() != null) {
            _hashCode += getOriginalInquiry().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(WSX12EligibilityResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("GatewayEDI.WebServices", "WSX12EligibilityResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("responseAsRawString");
        elemField.setXmlName(new javax.xml.namespace.QName("GatewayEDI.WebServices", "ResponseAsRawString"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("responseAsXml");
        elemField.setXmlName(new javax.xml.namespace.QName("GatewayEDI.WebServices", "ResponseAsXml"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("extraProcessingInfo");
        elemField.setXmlName(new javax.xml.namespace.QName("GatewayEDI.WebServices", "ExtraProcessingInfo"));
        elemField.setXmlType(new javax.xml.namespace.QName("GatewayEDI.WebServices", "ValidationFailureCollection"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("successCode");
        elemField.setXmlName(new javax.xml.namespace.QName("GatewayEDI.WebServices", "SuccessCode"));
        elemField.setXmlType(new javax.xml.namespace.QName("GatewayEDI.WebServices", "SuccessCode"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("originalInquiry");
        elemField.setXmlName(new javax.xml.namespace.QName("GatewayEDI.WebServices", "OriginalInquiry"));
        elemField.setXmlType(new javax.xml.namespace.QName("GatewayEDI.WebServices", "WSX12EligibilityInquiry"));
        elemField.setMinOccurs(0);
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
