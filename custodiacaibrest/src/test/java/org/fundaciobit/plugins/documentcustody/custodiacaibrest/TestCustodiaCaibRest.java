package org.fundaciobit.plugins.documentcustody.custodiacaibrest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;


import org.apache.commons.io.IOUtils;

import es.caib.signatura.mbean.ClienteCustodia;









/**
 * 
 * @author anadal
 * 
 */
public class TestCustodiaCaibRest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {

    try {

      final String hostport = "192.168.31.143:8888";

      // http://10.215.216.188:8080/signatura/services/CustodiaDocumentos?wsdl

      java.lang.String endpointURL = "http://" + hostport
          + "/signatura/services/CustodiaDocumentos";
      final String usr = "valcust";
      final String pwd = "valcust";

      ClienteCustodia custodia = new ClienteCustodia();

      custodia.setUrlServicioCustodia(endpointURL);
      custodia.setUsuario(usr);
      custodia.setPassword(pwd);

      
      
      //ClienteCustodia custodia = new ClienteCustodia("ClienteCustodiaHelium");
      

      // custodia.

      // 1 Obtenim Hash que identificará el document a Custodia
      String codigoExterno = "PORTAFIB" + System.currentTimeMillis() + "";
      byte[] response = custodia.reservarDocumento_v2(codigoExterno);
      
      if (response == null) {
        System.out.print(" Retornada reserva NULL");
        return;        
      }
      
      String reservaStr = new String(response);

      final String search = "<con:Hash>";
      int start = reservaStr.indexOf(search) + search.length();
      int end = reservaStr.indexOf("</con:Hash>");

      String codicustodia = reservaStr.substring(start, end);
      System.out.println("RESERVA : |" + reservaStr + "|");
      System.out.println("RESERVA : |" + codicustodia + "|");

      String nom = "holat.pdf";
      String codigoExternoTipoDocumento = "PDF";
      InputStream is = TestCustodiaCaibRest.class.getResourceAsStream("/" + nom);

      byte[] data = IOUtils.toByteArray(is);

      // 4 Custodiam el document signat

      byte[] response2 = custodia.custodiarPDFFirmado(new ByteArrayInputStream(data), nom,
          codigoExterno, codigoExternoTipoDocumento);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
