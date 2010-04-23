/**
 * EligibilitySoap.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package WebServices.GatewayEDI;

public interface EligibilitySoap extends java.rmi.Remote {

    /**
     * Executes an eligibility lookup with an external payer by constructing
     * an EDI transaction from the key/value pairs received.
     */
    public WebServices.GatewayEDI.WSEligibilityResponse doInquiry(WebServices.GatewayEDI.WSEligibilityInquiry inquiry) throws java.rmi.RemoteException;

    /**
     * Executes an eligibility lookup with an external payer using
     * the information in the raw 270 request.
     */
    public WebServices.GatewayEDI.WSX12EligibilityResponse doInquiryByX12Data(WebServices.GatewayEDI.WSX12EligibilityInquiry inquiry) throws java.rmi.RemoteException;

    /**
     * Executes an eligibility lookup with an external payer using
     * the information in the raw 270 request and returning a 271 response.
     */
    public WebServices.GatewayEDI.WSX12EligibilityResponse doInquiryByX12DataWith271Response(WebServices.GatewayEDI.WSX12EligibilityInquiry inquiry) throws java.rmi.RemoteException;
}
