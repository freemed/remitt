/**
 * RemittCallback_PortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.remitt.client.RemittCallback;

public interface RemittCallback_PortType extends java.rmi.Remote {
    public double getProtocolVersion() throws java.rmi.RemoteException;
    public int sendRemittancePayload(int payloadType, java.lang.String originalReference, java.lang.String payload) throws java.rmi.RemoteException;
}
