/**
 * EligibilityLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package WebServices.GatewayEDI;

import javax.xml.rpc.ServiceException;

@SuppressWarnings("serial")
public class EligibilityLocator extends org.apache.axis.client.Service
		implements WebServices.GatewayEDI.Eligibility {

	public EligibilityLocator() {
	}

	public EligibilityLocator(org.apache.axis.EngineConfiguration config) {
		super(config);
	}

	public EligibilityLocator(java.lang.String wsdlLoc,
			javax.xml.namespace.QName sName) throws ServiceException {
		super(wsdlLoc, sName);
	}

	// Use to get a proxy class for EligibilitySoap
	private java.lang.String EligibilitySoap_address = "https://testservices.gatewayedi.com/Eligibility/Service.asmx";

	public java.lang.String getEligibilitySoapAddress() {
		return EligibilitySoap_address;
	}

	// The WSDD service name defaults to the port name.
	private java.lang.String EligibilitySoapWSDDServiceName = "EligibilitySoap";

	public java.lang.String getEligibilitySoapWSDDServiceName() {
		return EligibilitySoapWSDDServiceName;
	}

	public void setEligibilitySoapWSDDServiceName(java.lang.String name) {
		EligibilitySoapWSDDServiceName = name;
	}

	public WebServices.GatewayEDI.EligibilitySoap getEligibilitySoap()
			throws javax.xml.rpc.ServiceException {
		java.net.URL endpoint;
		try {
			endpoint = new java.net.URL(EligibilitySoap_address);
		} catch (java.net.MalformedURLException e) {
			throw new javax.xml.rpc.ServiceException(e);
		}
		return getEligibilitySoap(endpoint);
	}

	public WebServices.GatewayEDI.EligibilitySoap getEligibilitySoap(
			java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
		try {
			WebServices.GatewayEDI.EligibilitySoapStub _stub = new WebServices.GatewayEDI.EligibilitySoapStub(
					portAddress, this);
			_stub.setPortName(getEligibilitySoapWSDDServiceName());
			return _stub;
		} catch (org.apache.axis.AxisFault e) {
			return null;
		}
	}

	public void setEligibilitySoapEndpointAddress(java.lang.String address) {
		EligibilitySoap_address = address;
	}

	// Use to get a proxy class for EligibilitySoap12
	private java.lang.String EligibilitySoap12_address = "https://testservices.gatewayedi.com/Eligibility/Service.asmx";

	public java.lang.String getEligibilitySoap12Address() {
		return EligibilitySoap12_address;
	}

	// The WSDD service name defaults to the port name.
	private java.lang.String EligibilitySoap12WSDDServiceName = "EligibilitySoap12";

	public java.lang.String getEligibilitySoap12WSDDServiceName() {
		return EligibilitySoap12WSDDServiceName;
	}

	public void setEligibilitySoap12WSDDServiceName(java.lang.String name) {
		EligibilitySoap12WSDDServiceName = name;
	}

	public WebServices.GatewayEDI.EligibilitySoap getEligibilitySoap12()
			throws javax.xml.rpc.ServiceException {
		java.net.URL endpoint;
		try {
			endpoint = new java.net.URL(EligibilitySoap12_address);
		} catch (java.net.MalformedURLException e) {
			throw new javax.xml.rpc.ServiceException(e);
		}
		return getEligibilitySoap12(endpoint);
	}

	public WebServices.GatewayEDI.EligibilitySoap getEligibilitySoap12(
			java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
		try {
			WebServices.GatewayEDI.EligibilitySoap12Stub _stub = new WebServices.GatewayEDI.EligibilitySoap12Stub(
					portAddress, this);
			_stub.setPortName(getEligibilitySoap12WSDDServiceName());
			return _stub;
		} catch (org.apache.axis.AxisFault e) {
			return null;
		}
	}

	public void setEligibilitySoap12EndpointAddress(java.lang.String address) {
		EligibilitySoap12_address = address;
	}

	/**
	 * For the given interface, get the stub implementation. If this service has
	 * no port for the given interface, then ServiceException is thrown. This
	 * service has multiple ports for a given interface; the proxy
	 * implementation returned may be indeterminate.
	 */
	public java.rmi.Remote getPort(Class serviceEndpointInterface)
			throws javax.xml.rpc.ServiceException {
		try {
			if (WebServices.GatewayEDI.EligibilitySoap.class
					.isAssignableFrom(serviceEndpointInterface)) {
				WebServices.GatewayEDI.EligibilitySoapStub _stub = new WebServices.GatewayEDI.EligibilitySoapStub(
						new java.net.URL(EligibilitySoap_address), this);
				_stub.setPortName(getEligibilitySoapWSDDServiceName());
				return _stub;
			}
			if (WebServices.GatewayEDI.EligibilitySoap.class
					.isAssignableFrom(serviceEndpointInterface)) {
				WebServices.GatewayEDI.EligibilitySoap12Stub _stub = new WebServices.GatewayEDI.EligibilitySoap12Stub(
						new java.net.URL(EligibilitySoap12_address), this);
				_stub.setPortName(getEligibilitySoap12WSDDServiceName());
				return _stub;
			}
		} catch (java.lang.Throwable t) {
			throw new javax.xml.rpc.ServiceException(t);
		}
		throw new javax.xml.rpc.ServiceException(
				"There is no stub implementation for the interface:  "
						+ (serviceEndpointInterface == null ? "null"
								: serviceEndpointInterface.getName()));
	}

	/**
	 * For the given interface, get the stub implementation. If this service has
	 * no port for the given interface, then ServiceException is thrown.
	 */
	public java.rmi.Remote getPort(javax.xml.namespace.QName portName,
			Class serviceEndpointInterface)
			throws javax.xml.rpc.ServiceException {
		if (portName == null) {
			return getPort(serviceEndpointInterface);
		}
		java.lang.String inputPortName = portName.getLocalPart();
		if ("EligibilitySoap".equals(inputPortName)) {
			return getEligibilitySoap();
		} else if ("EligibilitySoap12".equals(inputPortName)) {
			return getEligibilitySoap12();
		} else {
			java.rmi.Remote _stub = getPort(serviceEndpointInterface);
			((org.apache.axis.client.Stub) _stub).setPortName(portName);
			return _stub;
		}
	}

	public javax.xml.namespace.QName getServiceName() {
		return new javax.xml.namespace.QName("GatewayEDI.WebServices",
				"Eligibility");
	}

	private java.util.HashSet ports = null;

	public java.util.Iterator getPorts() {
		if (ports == null) {
			ports = new java.util.HashSet();
			ports.add(new javax.xml.namespace.QName("GatewayEDI.WebServices",
					"EligibilitySoap"));
			ports.add(new javax.xml.namespace.QName("GatewayEDI.WebServices",
					"EligibilitySoap12"));
		}
		return ports.iterator();
	}

	/**
	 * Set the endpoint address for the specified port name.
	 */
	public void setEndpointAddress(java.lang.String portName,
			java.lang.String address) throws javax.xml.rpc.ServiceException {

		if ("EligibilitySoap".equals(portName)) {
			setEligibilitySoapEndpointAddress(address);
		} else if ("EligibilitySoap12".equals(portName)) {
			setEligibilitySoap12EndpointAddress(address);
		} else { // Unknown Port Name
			throw new javax.xml.rpc.ServiceException(
					" Cannot set Endpoint Address for Unknown Port" + portName);
		}
	}

	/**
	 * Set the endpoint address for the specified port name.
	 */
	public void setEndpointAddress(javax.xml.namespace.QName portName,
			java.lang.String address) throws javax.xml.rpc.ServiceException {
		setEndpointAddress(portName.getLocalPart(), address);
	}

}
