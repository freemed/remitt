/**
 * RemittCallback_ServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.remitt.client.RemittCallback;

public class RemittCallback_ServiceLocator extends org.apache.axis.client.Service implements org.remitt.client.RemittCallback.RemittCallback_Service {

    public RemittCallback_ServiceLocator() {
    }


    public RemittCallback_ServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public RemittCallback_ServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for RemittCallbackSOAP
    private java.lang.String RemittCallbackSOAP_address = "http://client.remitt.org/";

    public java.lang.String getRemittCallbackSOAPAddress() {
        return RemittCallbackSOAP_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String RemittCallbackSOAPWSDDServiceName = "RemittCallbackSOAP";

    public java.lang.String getRemittCallbackSOAPWSDDServiceName() {
        return RemittCallbackSOAPWSDDServiceName;
    }

    public void setRemittCallbackSOAPWSDDServiceName(java.lang.String name) {
        RemittCallbackSOAPWSDDServiceName = name;
    }

    public org.remitt.client.RemittCallback.RemittCallback_PortType getRemittCallbackSOAP() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(RemittCallbackSOAP_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getRemittCallbackSOAP(endpoint);
    }

    public org.remitt.client.RemittCallback.RemittCallback_PortType getRemittCallbackSOAP(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            org.remitt.client.RemittCallback.RemittCallbackSOAPStub _stub = new org.remitt.client.RemittCallback.RemittCallbackSOAPStub(portAddress, this);
            _stub.setPortName(getRemittCallbackSOAPWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setRemittCallbackSOAPEndpointAddress(java.lang.String address) {
        RemittCallbackSOAP_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (org.remitt.client.RemittCallback.RemittCallback_PortType.class.isAssignableFrom(serviceEndpointInterface)) {
                org.remitt.client.RemittCallback.RemittCallbackSOAPStub _stub = new org.remitt.client.RemittCallback.RemittCallbackSOAPStub(new java.net.URL(RemittCallbackSOAP_address), this);
                _stub.setPortName(getRemittCallbackSOAPWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("RemittCallbackSOAP".equals(inputPortName)) {
            return getRemittCallbackSOAP();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://client.remitt.org/RemittCallback/", "RemittCallback");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://client.remitt.org/RemittCallback/", "RemittCallbackSOAP"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("RemittCallbackSOAP".equals(portName)) {
            setRemittCallbackSOAPEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
