package org.fundaciobit.plugins.documentcustody.custodiacaibaxis;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.IOUtils;
import org.fundaciobit.plugins.documentcustody.api.DocumentCustody;
import org.fundaciobit.plugins.documentcustody.api.IDocumentCustodyPlugin;

import es.caib.signatura.api.CustodiaResponse;
import es.caib.signatura.api.ReservaResponse;
import es.caib.signatura.api.ResultadoFirma;
import es.caib.signatura.api.ResultadoFirmas;
import es.caib.signatura.api.ValidacionCertificado;
import es.caib.signatura.cliente.custodia.ClienteCustodia;


/**
 * 
 * @author anadal
 * 
 */
public class TestCustodiaCaibAxis {

  

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    
      try {

        java.lang.String endpointURL = "http://192.168.31.143:8080/signatura/services/CustodiaDocumentos";
        // usuari i contrasenya definit al servidor de Custòdia
        final String usr = "valcust";
        final String pwd = "valcust";

        ClienteCustodia custodia = new ClienteCustodia();
        custodia.setUsuario(usr);
        custodia.setPassword(pwd);
        custodia.setUrlServicioCustodia(endpointURL);

        /*
        String codigoExterno="PORTAFIB1400586190721";
        byte[] reserva = custodia.reservarDocumento_v2(codigoExterno); //  + System.nanoTime()
        
        String reservaStr = new String(reserva);
        final String search = "<con:Hash>";
        int start = reservaStr.indexOf(search) + search.length();
        int end = reservaStr.indexOf("</con:Hash>");

        System.out.println("RESERVA : |" + reservaStr + "|");
        System.out.println("RESERVA HASH: |" + reservaStr.substring(start, end) + "|");
        */
        
        //String codigoExterno;
        // codigoExterno = fullTestPDF(custodia);
        
        //codigoExterno = testFullDocPlain(custodia);
        
        // testParseReservaResponse() 
       
        // testUnmarshallCustodiaresponse()
        
        // testDeleteDocument(custodia, codigoExterno);
        
        
        
        Properties prop = new Properties();
        prop.put(CustodiaCaibAxisPlugin.SERVER, endpointURL);
        prop.put(CustodiaCaibAxisPlugin.USERNAME, usr);
        prop.put(CustodiaCaibAxisPlugin.PASSWORD, pwd);
        prop.put(CustodiaCaibAxisPlugin.URLVALIDATION, "http://192.168.31.143:8080/signatura/sigpub/viewdoc?hash={0}");
        prop.put(CustodiaCaibAxisPlugin.DEFAULT_DOCUMENTTYPE_PROPERTY, "PDF");
        prop.put(CustodiaCaibAxisPlugin.DEFAULT_RESERVEPREFIX_PROPERTY, "PORTAFIB");
        
        IDocumentCustodyPlugin plugin = new CustodiaCaibAxisPlugin("", prop);
        
        String[][] dades = new String[][] {
            //{ "xades_dnie.xml", DocumentCustody.XADES_SIGNATURE, ""},
            //{"hola_dnie.pdf", DocumentCustody.PADES_SIGNATURE, "documenttype=PDF\\" }
            {"hola.pdf", "documenttype=PDF\nreserveprefix=PORTAFIBXX" }
        };
        
        
        for (int i = 0; i < dades.length; i++) {
          String file= dades[i][0];
          String custodyParameters= dades[i][1];
          
          InputStream is = TestCustodiaCaibAxis.class.getResourceAsStream("/" + file);
          byte[] data = IOUtils.toByteArray(is);
          
        
          //String proposedID= String.valueOf(System.currentTimeMillis());
          
          String custodyID = plugin.reserveCustodyID(custodyParameters);
          System.out.println("Código Externo = " + custodyID);

          DocumentCustody document = new DocumentCustody();
          document.setName(file);
          document.setData(data);
          //document.setDocumentType(signatureType);
          
          plugin.saveDocument(custodyID, custodyParameters, document);
          
          
          byte[] data2 = plugin.getDocument(custodyID);
          System.out.println("(a) D1 = D2 || " + data.length + " = "  + data2.length);
          
          DocumentCustody retornat = plugin.getDocumentInfo(custodyID);
          
          System.out.println("(b) D1 = D2 || " + data.length + " = "  + retornat.getData().length);
          System.out.println("(b) NOM = " + file + " = "  + retornat.getName());

          String url = plugin.getValidationUrl(custodyID);
          System.out.println("URL = " + url);
      
        }

      } catch (Exception e) {
        e.printStackTrace();
      }

    }


  public static void testDeleteDocument(ClienteCustodia custodia, String codigoExterno) {
    try {
      byte[] resposta = custodia.eliminarDocumento(codigoExterno);
      System.out.println(" RESPOSTA BORRAR = " + new String(resposta)); 
    } catch(Exception e) {
       System.err.print("Error borrant: " + e.getMessage());
    }
  }


  public static String  testFullDocPlain(ClienteCustodia custodia) throws IOException,
      RemoteException {
    String codigoExterno = "PORTAFIB" + System.currentTimeMillis() + "";
    
    System.out.println(" codigoExterno = " + codigoExterno);
    
    
    byte[] reserva = custodia.reservarDocumento_v2(codigoExterno); //  + System.nanoTime()
    
    String reservaStr = new String(reserva);
    final String search = "<con:Hash>";
    int start = reservaStr.indexOf(search) + search.length();
    int end = reservaStr.indexOf("</con:Hash>");

    System.out.println("RESERVA : |" + reservaStr + "|");
    System.out.println("RESERVA HASH: |" + reservaStr.substring(start, end) + "|");
    
    
    // CUSTODIA DOCUMENT SENSE FIRMA
    String nom = "hola.pdf";        
    InputStream is = TestCustodiaCaibAxis.class.getResourceAsStream("/" + nom);
    byte[] data = IOUtils.toByteArray(is);
    
    
    String codigoTipoDocumento = "PDF"; // Definit al servidor de Custòdia


    byte[] dades = custodia.custodiarDocumento(
        new ByteArrayInputStream(data), nom,
        codigoExterno, codigoTipoDocumento);

    // byte[] dades = custodia.custodiarPDFFirmado_v2(baos.toByteArray());

    System.out.println("RETORNAT ]" + new String(dades) + "[");
    
    System.out.println(new String(custodia.obtenerInformeDocumento(codigoExterno)));
    
    return codigoExterno;
  }
  
  

  
   

  public static void testUnmarshallCustodiaresponse() {
    try {
      

    String nom = "custodiaresponse.xml";        
    InputStream is = TestCustodiaCaibAxis.class.getResourceAsStream("/" + nom);
    
    JAXBContext jaxbContext = JAXBContext.newInstance(CustodiaResponse.class);
    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    CustodiaResponse resp = (CustodiaResponse) jaxbUnmarshaller.unmarshal(is);
    
    List<Object> list = resp.getVerifyResponse().getOptionalOutputs().getAny();
    
    for (Object object : list) {
      System.out.println(object.getClass());
      if (object instanceof ResultadoFirmas) {
        ResultadoFirmas rf = (ResultadoFirmas)object;
        System.out.println(rf.getVersionJerarquiaFirmas());
        List<ResultadoFirma> listRF = rf.getResultadoFirma();
        for (ResultadoFirma resultadoFirma : listRF) {
          System.out.println("=== ResultadoFirma ");
          List<ValidacionCertificado> vcList = resultadoFirma.getValidacionCertificado();
          for (ValidacionCertificado validacionCertificado : vcList) {
            System.out.println("=== +++ ValidacionCertificado ");
            System.out.println("        * NAME: " + validacionCertificado.getSubjectName());
            System.out.println("        * SERI: " + validacionCertificado.getNumeroSerie());
            System.out.println("        * VERI: " + validacionCertificado.isVerificado());
            System.out.println("        * URL : " + validacionCertificado.getUrl());
          }
          
        }
        
      }
    }
    
    } catch (Exception e) {
      e.printStackTrace();
    }
    
  }
  
  
  public static void testParseReservaResponse() {
    try {
      String nom = "reservaresponse.xml";        
      InputStream is = TestCustodiaCaibAxis.class.getResourceAsStream("/" + nom);
      //byte[] data = IOUtils.toByteArray(is);

     // CustodiaResponse
      
      JAXBContext jaxbContext = JAXBContext.newInstance(ReservaResponse.class);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      ReservaResponse reserva = (ReservaResponse) jaxbUnmarshaller.unmarshal(is);
      
      System.out.println(" Codigo: " + reserva.getCodigo());
      System.out.println("   Hash: " + reserva.getHash());
      
    } catch (Exception e) {
      // TODO: handle exception
      e.printStackTrace();
    }
    
  }
  
  
  public static String  testFullSignedPDF(ClienteCustodia custodia) throws Exception {
    
    String codigoExterno = "PORTAFIB" + System.currentTimeMillis() + "";
    
    System.out.println(" codigoExterno = " + codigoExterno);
    
    byte[] reserva = custodia.reservarDocumento_v2(codigoExterno); //  + System.nanoTime()
    
    String reservaStr = new String(reserva);
    final String search = "<con:Hash>";
    int start = reservaStr.indexOf(search) + search.length();
    int end = reservaStr.indexOf("</con:Hash>");

    System.out.println("RESERVA : |" + reservaStr + "|");
    System.out.println("RESERVA HASH: |" + reservaStr.substring(start, end) + "|");
    
    // CUSTODIA DOCUMENT PDF FIRMAT 
    String nom = "hola_dnie.pdf";        
    InputStream is = TestCustodiaCaibAxis.class.getResourceAsStream("/" + nom);
    byte[] data = IOUtils.toByteArray(is);
    
    
    String codigoTipoDocumento = "PDF"; // Definit al servidor de Custòdia


    byte[] dades = custodia.custodiarPDFFirmado(
        new ByteArrayInputStream(data), nom,
        codigoExterno, codigoTipoDocumento);

    // byte[] dades = custodia.custodiarPDFFirmado_v2(baos.toByteArray());

    System.out.println("RETORNAT ]" + new String(dades) + "[");

    System.out.println(new String(custodia.obtenerInformeDocumento(codigoExterno)));
    
    return codigoExterno;
    
  }
   


}
