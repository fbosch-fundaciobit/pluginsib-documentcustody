package es.caib.signatura.cliente.custodia;

import es.caib.signatura.cliente.services.custodia.Custodia;
import es.caib.signatura.cliente.services.custodia.CustodiaService;
import es.caib.signatura.cliente.services.custodia.CustodiaServiceLocator;
import es.caib.signatura.cliente.services.custodia.CustodiaSoapBindingStub;

import es.caib.signatura.utils.ClientMultipartFormData;
import java.io.ByteArrayInputStream;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

import org.apache.log4j.Logger;



/**
 * cliente que se conecta al Web-Service de custodia documental. 
 * 
 * @author  Fernando Guardiola Ruiz (FGU), Esteban Luengo, Pere Joseph
 * @version 03/06/2008, 2.0 6/11/2009 se aoade la version 2.0 de los motodos de custodia para enviar el XMl como un array de bytes
 * @version 3.0 25/02/2011 se modifica el envoo de documentos a custodiar mediante un upload POST
 */
public class ClienteCustodia {

  private String usuario = null;
  private String password = null;
  private String urlServicioCustodia = null;

  private CustodiaService service = null;  
  private Custodia custodiaPort = null;
  public static String SMIME = "SMIME";
  public static String PDF_FIRMADO = "PDF_FIRMADO";
  public static String XADES = "XADES";
  public static String SIN_FIRMAR = "SIN_FIRMAR";

  /** Constructor vacio. */  
  public ClienteCustodia() {
  }  
  
  private void init() {
    
    try {
      
      if (custodiaPort==null) {        
        service = new CustodiaServiceLocator();      
        custodiaPort = service.getCustodia(new URL(this.urlServicioCustodia));
        ((CustodiaSoapBindingStub)custodiaPort).setUsername(this.usuario);
        ((CustodiaSoapBindingStub)custodiaPort).setPassword(this.password);                      
      }
      
    }catch (ServiceException sex) {
    	Logger.getLogger(this.getClass()).error(sex.getMessage(),sex);
    }catch (MalformedURLException mux) {
    	Logger.getLogger(this.getClass()).error(mux.getMessage(),mux);
    }
  }
  
  
  /**
   * Obtiene el usuario con el que el servicio se identifica en el sistema de custodia. 
   * @return El usuario con el que el servicio se identifica en el sistema de custodia. 
   */
  public String getUsuario() {
    return this.usuario;
  }
  
 /**
  * Establece el usuario con el que el servicio se identifica en el sistema de custodia. 
  * @param usuario el usuario con el que el servicio se identifica en el sistema de custodia. 
  */
  public void setUsuario(String usuario) {
    this.usuario = usuario;
  }
  
 /**
  * Obtiene el password con el que el servicio se autentica en el sistema de custodia. 
  * @return El password con el que el servicio se autentica en el sistema de custodia. 
  */  
  public String getPassword() {
    return this.password;
  }

 /**
  * Establece el password con el que el servicio se autentica en el sistema de custodia. 
  * @param usuario El password con el que el servicio se autentica en el sistema de custodia. 
  */  
  public void setPassword(String password) {
    this.password = password;
  }
  
  /**
   * Recupera la url en la que esta el web service de custodia
   * @return La url en la que esta el web service de custodia
   */
  public String getUrlServicioCustodia() {
    return this.urlServicioCustodia;
  }

 /**
  * Establece la url en la que esta el web service de custodia
  * @param urlServicioCustodia La url en la que esta el web service de custodia
  */
  public void setUrlServicioCustodia(String urlServicioCustodia) {
    this.urlServicioCustodia = urlServicioCustodia;
  }
  
    
  
  /**
   * Envoa el documento S/MIME al sistema de validacino para su custodia, con el nombre y codigo indicados. 
   * @param documento Flujo del que leer el documento
   * @param nombreDocumento Nombre del documento en el sistema de custodia
   * @param codigoExterno Codigo con el que se custodiaro el documento
   * @param codigoExternoTipoDocumento Codigo para identificar el tipo de documento.
   * @return UN XML con el resultado de la custodia
   * @throws RemoteException Si se produce algun error 
   */
   public byte[] custodiarDocumentoSMIME(ByteArrayInputStream documento, String nombreDocumento, String codigoExterno, String codigoExternoTipoDocumento) throws RemoteException {      
//      init();
//      CustodiaRequestBuilder custodiaRequestBuilder = new CustodiaRequestBuilder(this.usuario,this.password);
//      byte[] xml = custodiaRequestBuilder.buildXML(documento,nombreDocumento,codigoExterno,codigoExternoTipoDocumento);
//      return custodiaPort.custodiarDocumentoSMIME_v2(xml);
        return uploadDocumento(documento, SMIME, nombreDocumento,codigoExterno,codigoExternoTipoDocumento,this.usuario,this.password);
  }

  /**
   * Envoa el documento XAdES al sistema de validacion para su custodia, con el nombre y codigo indicados.
   * @param documento Flujo del que leer el documento
   * @param nombreDocumento Nombre del documento en el sistema de custodia
   * @param codigoExterno Codigo con el que se custodiaro el documento
   * @param codigoExternoTipoDocumento Codigo para identificar el tipo de documento.
   * @return UN XML con el resultado de la custodia
   * @throws RemoteException Si se produce algun error
   */
   public byte[] custodiarDocumentoXAdES(ByteArrayInputStream documento, String nombreDocumento, String codigoExterno, String codigoExternoTipoDocumento) throws RemoteException {
//      init();
//      CustodiaRequestBuilder custodiaRequestBuilder = new CustodiaRequestBuilder(this.usuario,this.password);
//      byte[] xml = custodiaRequestBuilder.buildXML(documento,nombreDocumento,codigoExterno,codigoExternoTipoDocumento);
//      return custodiaPort.custodiarDocumentoXAdES(xml);
        return uploadDocumento(documento, XADES, nombreDocumento,codigoExterno,codigoExternoTipoDocumento,this.usuario,this.password);
  }

  /**
   * Envoa el documento al sistema de validacino para su custodia, con el nombre y codigo indicados. 
   * @param documento Flujo del que leer el documento
   * @param nombreDocumento Nombre del documento en el sistema de custodia
   * @param codigoExterno Codigo con el que se custodiaro el documento
   * @param codigoExternoTipoDocumento Codigo para identificar el tipo de documento.
   * @return UN XML con el resultado de la custodia
   * @throws RemoteException Si se produce algun error 
   */
  public byte[] custodiarDocumento(ByteArrayInputStream documento, String nombreDocumento, String codigoExterno,String codigoExternoTipoDocumento) throws RemoteException {  
//    init();
//    CustodiaRequestBuilder custodiaRequestBuilder = new CustodiaRequestBuilder(this.usuario,this.password);
//    byte[] xml = custodiaRequestBuilder.buildXML(documento,nombreDocumento,codigoExterno,codigoExternoTipoDocumento);
//    return custodiaPort.custodiarDocumento_v2(xml);
       return uploadDocumento(documento, SIN_FIRMAR, nombreDocumento,codigoExterno,codigoExternoTipoDocumento,this.usuario,this.password);
  }
  
  /**
   * Envoa el PDF Firmado al sistema de validacino para su custodia, con el nombre y codigo indicados. 
   * @param documento Flujo del que leer el documento
   * @param nombreDocumento Nombre del documento en el sistema de custodia
   * @param codigoExterno Codigo con el que se custodiaro el documento
   * @param codigoExternoTipoDocumento Codigo para identificar el tipo de documento.
   * @return UN XML con el resultado de la custodia
   * @throws RemoteException Si se produce algun error 
   */
  public byte[] custodiarPDFFirmado(ByteArrayInputStream documento, String nombreDocumento, String codigoExterno,String codigoExternoTipoDocumento) throws RemoteException {
//    init();
//    CustodiaRequestBuilder custodiaRequestBuilder = new CustodiaRequestBuilder(this.usuario,this.password);
//    byte[] xml = custodiaRequestBuilder.buildXML(documento,nombreDocumento,codigoExterno,codigoExternoTipoDocumento);
//    return custodiaPort.custodiarPDFFirmado_v2(xml);
    return uploadDocumento(documento, PDF_FIRMADO, nombreDocumento,codigoExterno,codigoExternoTipoDocumento,this.usuario,this.password);

  }
  
  /**
   * Indica al sistema de custodia que purgue el documento con el codigo indicado.
   * @param codigoExterno Codigo del documento a purgar 
   * @return Un XML con la respuesta de la operacion 
   * @throws RemoteException Si se produce algun error. 
   */
  public byte[] purgarDocumento(String codigoExterno) throws RemoteException {    
    init();
    return custodiaPort.purgarDocumento_v2(this.usuario,this.password,codigoExterno);
  }
  
  /**
   * Indica al sistema de custodia que recupera el documento con el codigo indicado.
   * @param codigoExterno Codigo del documento a recuperar 
   * @return Un XML con la respuesta de la operacion 
   * @throws RemoteException Si se produce algun error. 
   */
  public byte[] recuperarDocumento(String codigoExterno) throws RemoteException {
    init();
    return custodiaPort.recuperarDocumento_v2(this.usuario,this.password,codigoExterno);
  }
  
  /**
   * Indica al sistema de custodia que elimine el documento con el codigo indicado.
   * @param codigoExterno Codigo del documento a eliminar. 
   * @return Un XML con la respuesta de la operacion. 
   * @throws RemoteException Si se produce algun error. 
   */
  public byte[] eliminarDocumento(String codigoExterno) throws RemoteException {
    init();
    return custodiaPort.eliminarDocumento_v2(this.usuario,this.password,codigoExterno);
  }
  
  /**
   * Indica al sistema de custodia que verifique el documento con el codigo indicado.
   * @param codigoExterno Codigo del documento a verificar 
   * @return Un XML con la respuesta de la operacion 
   * @throws RemoteException Si se produce algun error. 
   */
  public byte[] verificarDocumento(String codigoExterno) throws RemoteException {
    init();
    return custodiaPort.verificarDocumento_v2(this.usuario,this.password,codigoExterno);
  }
  
  /**
   * Indica al sistema de custodia que obtenga un informe con todas las operacion que ha sufrido el documento desde que se envio 
   * para su custodia por primera vez.
   * @param codigoExterno Codigo del documento del que obtenemos el informe.
   * @return Un XML con el informe. 
   * @throws RemoteException Si se produce algun error. 
   */
  public byte[] obtenerInformeDocumento(String codigoExterno) throws RemoteException {
    init();
    return custodiaPort.obtenerInformeDocumento_v2(this.usuario,this.password,codigoExterno);
  }
  
  /**
   * Indica al sistema de custodia que obtenga el documento con el codigo indicado.
   * @param codigoExterno Codigo del documento a consultar. 
   * @return Un XML con el error si la consulta no se produjo o el documento si la operacion fue correcta. 
   * @throws RemoteException Si se produce algun error. 
   */
  public byte[] consultarDocumento(String codigoExterno) throws RemoteException {
    init();
    return custodiaPort.consultarDocumento_v2(this.usuario,this.password,codigoExterno);
  }

  /**
   * Indica al sistema de custodia que reserve el documento con el codigo indicado.
   * @param codigoExterno Codigo del documento a reservar.
   * @return Un XML con el error si la consulta no se produjo o el hash si la operacion fue correcta.
   * @throws RemoteException Si se produce algun error.
   */
  public byte[] reservarDocumento(String codigoExterno) throws RemoteException {
    init();
    return custodiaPort.reservarDocumento(this.usuario,this.password,codigoExterno);
  }

  
  /**
   * Indica al sistema de custodia que reserve el documento con el codigo indicado.
   * @param codigoExterno Codigo del documento a reservar.
   * @return Un XML con el error si la consulta no se produjo o el hash si la operacion fue correcta.
   * @throws RemoteException Si se produce algun error.
   */
  public byte[] reservarDocumento_v2(String codigoExterno) throws RemoteException {
    init();
    return custodiaPort.reservarDocumento_v2(this.usuario,this.password,codigoExterno);
  }
  /**
   * Indica al sistema de custodia que obtenga la reserva con el hash indicado.
   * @param hash Hash de la reserva a consultar.
   * @return Un XML con el error si la consulta no se produjo o los datos de la reserva si la operacion fue correcta.
   * @throws RemoteException Si se produce algun error.
   */
  public byte[] consultarReservaDocumento(String hash) throws RemoteException {
    init();
    return custodiaPort.consultarReservaDocumento(this.usuario,this.password,hash);
  }


  /**
     * Sube el documento a custodiar junto con todos los parometros de custodia enviando un mensaje
     * multipart/form-data mediante HTTP Post a un Servlet
     * @param documento representa el documento a custodiar como un ByteArrayInputStream
     * @param claseDocumento representa la clase de documento: SMIME, PDF_FIRMADO, XADES, SIN_FIRMAR
     * @param nombreDocumento nombre del documento en la aplicacion origen
     * @param codigoExterno codigo del documento en la aplicacion origen
     * @param codigoExternoTipoDocumento codigo del tipo de documento con el que identificaremos la jeraraquoa de firmas
     * @param usuario usuario de aplicacion
     * @param password password de aplicacion
     * @return el motodo retorna un array de bytes con el XML en UTF-8 que contiene la respuesta de custodia
     */
    private byte[] uploadDocumento(ByteArrayInputStream documento, String claseDocumento, String nombreDocumento,
            String codigoExterno, String codigoExternoTipoDocumento,String usuario, String password) {
        try {
            String urlServlet = this.urlServicioCustodia.substring(0, this.urlServicioCustodia.indexOf("services/CustodiaDocumentos"))+"uploaddocument";
            ClientMultipartFormData client = new ClientMultipartFormData(urlServlet);
            client.addParameter("claseDoc", claseDocumento);
            client.addParameter("nombreDoc", nombreDocumento);
            client.addParameter("codExt", codigoExterno);
            client.addParameter("codExtTipDoc", codigoExternoTipoDocumento);
            client.addParameter("usuario", usuario);
            client.addParameter("password", password);
            String extension = getExtension(claseDocumento,nombreDocumento);
            client.addParameter("datos", "datos."+extension, documento);
            InputStream input = client.send();
            Logger.getLogger(this.getClass()).debug("["+usuario+"] inputStream "+input.getClass().getName());
            
            
            Logger.getLogger(this.getClass()).debug("["+usuario+"] received message [available "+input.available()+"]");
            
            byte[] resp = new byte[4096];
            ByteArrayOutputStream read=new ByteArrayOutputStream();
            int readed=0;
            while((readed=input.read(resp))!=-1){
            	read.write(resp, 0, readed);
            }
            
            Logger.getLogger(this.getClass()).debug("["+usuario+"] received message: "+new String(resp,"UTF-8"));
            input.close();
            
            return read.toByteArray();
        } catch (Throwable ex) {
        	Logger.getLogger(this.getClass()).error("["+usuario+"] "+ex.getMessage(),ex);
            return null;
        }
    }

        /**
     *
     * @param claseDocumento
     * @param nombreDocumento
     * @return
     */
    private static String getExtension(String claseDocumento, String nombreDocumento){
        String extension = "";
        if (claseDocumento.equals(SMIME)) return "smime";
        if (claseDocumento.equals(PDF_FIRMADO)) return "pdf";
        if (claseDocumento.equals(XADES)) return "xades";
        if (claseDocumento.equals(SIN_FIRMAR)){
            int index = nombreDocumento.lastIndexOf(".");
            if (index != -1){
                return nombreDocumento.substring(index+1, nombreDocumento.length());
            }else
                return "";
        }

        return extension;
    }

  
}
