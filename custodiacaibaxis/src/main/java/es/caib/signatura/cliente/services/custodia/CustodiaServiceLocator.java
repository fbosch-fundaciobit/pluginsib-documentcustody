/**
 * CustodiaServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package es.caib.signatura.cliente.services.custodia;

@SuppressWarnings("rawtypes")
public class CustodiaServiceLocator extends org.apache.axis.client.Service implements es.caib.signatura.cliente.services.custodia.CustodiaService {

    public CustodiaServiceLocator() {
    }


    public CustodiaServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public CustodiaServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for Custodia
    private java.lang.String Custodia_address = "http://10.215.2.17:18080/signatura/services/Custodia";

    public java.lang.String getCustodiaAddress() {
        return Custodia_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String CustodiaWSDDServiceName = "Custodia";

    public java.lang.String getCustodiaWSDDServiceName() {
        return CustodiaWSDDServiceName;
    }

    public void setCustodiaWSDDServiceName(java.lang.String name) {
        CustodiaWSDDServiceName = name;
    }

    public es.caib.signatura.cliente.services.custodia.Custodia getCustodia() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(Custodia_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getCustodia(endpoint);
    }

    public es.caib.signatura.cliente.services.custodia.Custodia getCustodia(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            es.caib.signatura.cliente.services.custodia.CustodiaSoapBindingStub _stub = new es.caib.signatura.cliente.services.custodia.CustodiaSoapBindingStub(portAddress, this);
            _stub.setPortName(getCustodiaWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setCustodiaEndpointAddress(java.lang.String address) {
        Custodia_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (es.caib.signatura.cliente.services.custodia.Custodia.class.isAssignableFrom(serviceEndpointInterface)) {
                es.caib.signatura.cliente.services.custodia.CustodiaSoapBindingStub _stub = new es.caib.signatura.cliente.services.custodia.CustodiaSoapBindingStub(new java.net.URL(Custodia_address), this);
                _stub.setPortName(getCustodiaWSDDServiceName());
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
        if ("Custodia".equals(inputPortName)) {
            return getCustodia();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://10.215.2.17:18080/signatura/services/Custodia", "CustodiaService");
    }

    
    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://10.215.2.17:18080/signatura/services/Custodia", "Custodia"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("Custodia".equals(portName)) {
            setCustodiaEndpointAddress(address);
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
