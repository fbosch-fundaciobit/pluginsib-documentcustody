/**
 * CustodiaService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package es.caib.signatura.cliente.services.custodia;

public interface CustodiaService extends javax.xml.rpc.Service {
    public java.lang.String getCustodiaAddress();

    public es.caib.signatura.cliente.services.custodia.Custodia getCustodia() throws javax.xml.rpc.ServiceException;

    public es.caib.signatura.cliente.services.custodia.Custodia getCustodia(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
