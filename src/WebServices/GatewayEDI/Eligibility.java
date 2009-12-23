/**
 * Eligibility.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package WebServices.GatewayEDI;

public interface Eligibility extends javax.xml.rpc.Service {
	public java.lang.String getEligibilitySoapAddress();

	public WebServices.GatewayEDI.EligibilitySoap getEligibilitySoap()
			throws javax.xml.rpc.ServiceException;

	public WebServices.GatewayEDI.EligibilitySoap getEligibilitySoap(
			java.net.URL portAddress) throws javax.xml.rpc.ServiceException;

	public java.lang.String getEligibilitySoap12Address();

	public WebServices.GatewayEDI.EligibilitySoap getEligibilitySoap12()
			throws javax.xml.rpc.ServiceException;

	public WebServices.GatewayEDI.EligibilitySoap getEligibilitySoap12(
			java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
