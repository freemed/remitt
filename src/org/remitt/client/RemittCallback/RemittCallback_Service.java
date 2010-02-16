/**
 * RemittCallback_Service.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.remitt.client.RemittCallback;

public interface RemittCallback_Service extends javax.xml.rpc.Service {
    public java.lang.String getRemittCallbackSOAPAddress();

    public org.remitt.client.RemittCallback.RemittCallback_PortType getRemittCallbackSOAP() throws javax.xml.rpc.ServiceException;

    public org.remitt.client.RemittCallback.RemittCallback_PortType getRemittCallbackSOAP(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
