package es.caib.signatura.cliente.custodia;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import javax.xml.transform.dom.DOMSource;

import javax.xml.transform.stream.StreamResult;

import org.bouncycastle.util.encoders.Base64;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import es.caib.signatura.utils.BitLog;

/**
 * Clase que construye un XML con una peticion para custodiar un documento para una aplicacion
 * especifica.
 *
 * @author Fernando Guardiola Ruiz, Esteban Luengo
 * @version 19/03/2008, 2.0  6/11/2009Los XML se retornan en array de bytes
 */
public class CustodiaRequestBuilder {

  private DocumentBuilderFactory documentBuilderFactory = null;

  private Document document = null;

  private String usuario;
  private String password;

  /**
   * Constructor de la clase para custodiar documento para la aplicacion de usuario
   * <code>user</code> y password <code>password</code>.
   *
   * @param usuario Usuario de la aplicacion.
   * @param password  Password de la aplicacion.
   */
  public CustodiaRequestBuilder(String usuario,String password) {
    this.usuario = usuario;
    this.password = password;

    System.clearProperty("javax.xml.parsers.DocumentBuilderFactory");
  }


  /**
   * Construye el XML con la peticion para custodiar el documento que lee del flujo <code>datos</code>
   * y que se custodiaro con el nombre <code>nombreDoc/code> y codigo de documento <code>codigoDoc</code>.
   *
   * @param datos Flujo de donde leer el documento a custodiar.
   * @param nombreDoc Nombre con el que se custodiaro el documento.
   * @param codigoDoc Codigo con el que se custodiaro el documento.
   * @param tipoDoc Codigo externo que identifica al tipo de documento.
   * @return El XML con la peticion de custodia.
   */
  public byte[] buildXML(InputStream datos, String nombreDoc, String codigoDoc, String tipoDoc) {

    try {

      System.clearProperty("javax.xml.parsers.DocumentBuilderFactory");
      DocumentBuilder documentBuilder = null;
      documentBuilderFactory = DocumentBuilderFactory.newInstance();
      documentBuilder = documentBuilderFactory.newDocumentBuilder();
      document = documentBuilder.newDocument();



      Element custodiaRequest = document.createElement("cus:CustodiaRequest");
      Attr namespaceCustodia = document.createAttribute("xmlns:cus");
      namespaceCustodia.setNodeValue("http://www.caib.es.signatura.custodia");
      custodiaRequest.setAttributeNode(namespaceCustodia);

      document.appendChild(custodiaRequest);

      //Los datos del documento
      Element datosDocumento = document.createElement("cus:DatosDocumento");
      custodiaRequest.appendChild(datosDocumento);

      Element nombre = document.createElement("cus:nombre");
      Text t = document.createTextNode("");
      t.setNodeValue(nombreDoc);
      nombre.appendChild(t);
      Element codigo = document.createElement("cus:codigo");
      t = document.createTextNode("");
      t.setNodeValue(codigoDoc);
      codigo.appendChild(t);
      Element tipo = document.createElement("cus:tipo");
      t = document.createTextNode("");
      t.setNodeValue(tipoDoc);
      tipo.appendChild(t);


      datosDocumento.appendChild(nombre);
      datosDocumento.appendChild(codigo);
      datosDocumento.appendChild(tipo);


      //El VerifyRequest
      Attr namespaceDss = document.createAttribute("xmlns:dss");
      namespaceDss.setValue("urn:oasis:names:tc:dss:1.0:core:schema");

      Element verifyRequest = document.createElement("dss:VerifyRequest");
      verifyRequest.setAttributeNode(namespaceDss);

      Element optionalInputs = document.createElement("dss:OptionalInputs");
      verifyRequest.appendChild(optionalInputs);


      custodiaRequest.appendChild(verifyRequest);
      //ClaimedIdentity
      Element claimedIdentity = document.createElement("dss:ClaimedIdentity");

      Element name = document.createElement("dss:Name");
      t = document.createTextNode("");
      t.setNodeValue(this.usuario);
      name.appendChild(t);

      Element supportingInfo = document.createElement("dss:SupportingInfo");
      Element password = document.createElement("cus:password");
      supportingInfo.appendChild(password);

      t = document.createTextNode("");
      t.setNodeValue(this.password);
      password.appendChild(t);

      claimedIdentity.appendChild(name);
      claimedIdentity.appendChild(supportingInfo);
      optionalInputs.appendChild(claimedIdentity);
      //SignatureObject
      Element signatureObject = document.createElement("dss:SignatureObject");

      Element base64Signature = document.createElement("dss:Base64Signature");
      signatureObject.appendChild(base64Signature);

      byte b[] = new byte[datos.available()];
      for (int i=0;i<b.length;i++) {
        b[i] = (byte)datos.read();
      }
      byte[] datosBase64 = Base64.encode(b);

      t = document.createTextNode("");
      t.setNodeValue(new String(datosBase64));
      base64Signature.appendChild(t);

      verifyRequest.appendChild(signatureObject);

      //Pasmoa el document a un String
      TransformerFactory tf = TransformerFactory.newInstance();
      Transformer transformer = tf.newTransformer();
      transformer.setOutputProperty("encoding", "UTF-8");
      DOMSource source = new DOMSource(document);
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      StreamResult result = new StreamResult(bos);

      transformer.transform(source, result);

      return bos.toByteArray();

    }catch (Exception e) {
    	BitLog.error(e.getMessage(),e);
      return null;
    }
  }

}
