package org.fundaciobit.plugins.documentcustody.custodiacaibaxis;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.fundaciobit.plugins.documentcustody.api.AnnexCustody;
import org.fundaciobit.plugins.documentcustody.api.CustodyException;
import org.fundaciobit.plugins.documentcustody.api.DocumentCustody;
import org.fundaciobit.plugins.documentcustody.api.IDocumentCustodyPlugin;
import org.fundaciobit.plugins.documentcustody.api.NotSupportedCustodyException;
import org.fundaciobit.plugins.documentcustody.api.SignatureCustody;
import org.fundaciobit.plugins.utils.AbstractPluginProperties;
import org.fundaciobit.plugins.utils.Metadata;
import org.fundaciobit.plugins.utils.MetadataFormatException;

import es.caib.signatura.api.AnyType;
import es.caib.signatura.api.ConsultaResponse;
import es.caib.signatura.api.CustodiaResponse;
import es.caib.signatura.api.Documento;
import es.caib.signatura.api.EliminacionResponse;
import es.caib.signatura.api.InformeResponse;
import es.caib.signatura.api.ReservaResponse;
import es.caib.signatura.api.ResponseBaseType;
import es.caib.signatura.api.Result;
import es.caib.signatura.api.ResultadoFirma;
import es.caib.signatura.api.ResultadoFirmas;
import es.caib.signatura.api.ValidacionCertificado;
import es.caib.signatura.cliente.custodia.ClienteCustodia;

/**
 * 
 * @author anadal
 * 
 */
public class CustodiaCaibAxisPlugin extends AbstractPluginProperties
  implements IDocumentCustodyPlugin {

  protected final Logger log = Logger.getLogger(getClass());

  public static final String BASEKEY = DOCUMENTCUSTODY_BASE_PROPERTY + "custodiacaib.";

  public static final String URLVALIDATION = BASEKEY + "urlvalidation";
  public static final String SERVER = BASEKEY + "server";
  public static final String USERNAME = BASEKEY + "username";
  public static final String PASSWORD = BASEKEY + "password";
  
  public static final String DOCUMENTTYPE_PARAM = "documenttype";
  public static final String RESERVEPREFIX_PARAM = "reserveprefix";
  
  public static final String DEFAULT_DOCUMENTTYPE_PROPERTY = BASEKEY + "defaultdocumenttype";
  public static final String DEFAULT_RESERVEPREFIX_PROPERTY = BASEKEY + "defaultreserveprefix";
  


  public static final Set<String> SUPPORTED_SIGN_TYPES = new TreeSet<String>();

  public static final String[] SUPPORTED_SIGN_TYPES_ARRAY;

  static {
    SUPPORTED_SIGN_TYPES.add(SignatureCustody.PADES_SIGNATURE);
    SUPPORTED_SIGN_TYPES.add(SignatureCustody.XADES_SIGNATURE);
    SUPPORTED_SIGN_TYPES.add(SignatureCustody.SMIME_SIGNATURE);

    SUPPORTED_SIGN_TYPES_ARRAY = SUPPORTED_SIGN_TYPES.toArray(new String[SUPPORTED_SIGN_TYPES
        .size()]);
  }

  


  /**
   * 
   */
  public CustodiaCaibAxisPlugin() {
    super();
  }



  /**
   * @param propertyKeyBase
   * @param properties
   */
  public CustodiaCaibAxisPlugin(String propertyKeyBase, Properties properties) {
    super(propertyKeyBase, properties);
  }



  /**
   * @param propertyKeyBase
   */
  public CustodiaCaibAxisPlugin(String propertyKeyBase) {
    super(propertyKeyBase);
  }



  protected ClienteCustodia getClienteCustodia() {
    ClienteCustodia clienteCustodia = new ClienteCustodia();
    clienteCustodia.setUrlServicioCustodia(getProperty(SERVER));
    clienteCustodia.setUsuario(getProperty(USERNAME));
    clienteCustodia.setPassword(getProperty(PASSWORD));
    return clienteCustodia;
  }

  @Override
  public String reserveCustodyID(String custodyParameters)
      throws CustodyException {
    
    final String proposedID = String.valueOf(System.nanoTime()); 

    log.info(" ============= RESERVA ================");
    log.info(" proposedID = " + proposedID);
    log.info(" custodyParameters = " + custodyParameters);

    String reservePrefix = getReservePrefix(custodyParameters);
    log.info(" reservePrefix = " + reservePrefix);

    String codiExtern = reservePrefix + proposedID;
    log.info(" codiExtern = " + codiExtern);

    ReservaResponse resp = internalReserveCustody(codiExtern);

    log.info(" ReservaResponse.CODIGO = " + resp.getCodigo());
    log.info(" ReservaResponse.HASH = " + resp.getHash());

    return codiExtern;
  }

  protected ReservaResponse internalReserveCustody(String codiExtern)
      throws CustodyException {
    byte[] resposta;
    ReservaResponse resp;
    try {
      resposta = getClienteCustodia().reservarDocumento_v2(codiExtern);

      JAXBContext jaxbContext = JAXBContext.newInstance(ReservaResponse.class);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      resp = (ReservaResponse) jaxbUnmarshaller.unmarshal(new ByteArrayInputStream(resposta));

    } catch (Exception e) {
      throw new CustodyException(e);
    }

    checkResult(resposta, resp.getResult());
    return resp;
  }

  /**
   * 
   * @param custodyParameters
   * @return
   * @throws IOException
   * @throws Exception
   */
  protected String getDocumentType(String custodyParameters) throws CustodyException {
    final String paramProperty = DOCUMENTTYPE_PARAM;
    final String defaultProperty = DEFAULT_DOCUMENTTYPE_PROPERTY;
    return getValueFromCustodyParameters(custodyParameters, paramProperty,
        defaultProperty);
  }
  
  
  protected String getReservePrefix(String custodyParameters) throws CustodyException {
    final String paramProperty = RESERVEPREFIX_PARAM;
    final String defaultProperty = DEFAULT_RESERVEPREFIX_PROPERTY;
    return getValueFromCustodyParameters(custodyParameters, paramProperty,
        defaultProperty);
  }
  
  private String getValueFromCustodyParameters(String custodyParameters, String paramProperty,
      String defaultProperty) throws CustodyException {
    String tipusDocument = null;
    if (custodyParameters != null && custodyParameters.trim().length() != 0) {
      Properties prop = new Properties();
      try {
        prop.load(new ByteArrayInputStream(custodyParameters.getBytes()));
      } catch (IOException e) {
        throw new CustodyException("custodyParameters incorrectes", e);
      }
      tipusDocument = prop.getProperty(paramProperty);
    }

    if (tipusDocument == null) {
      tipusDocument = getProperty(defaultProperty);

      if (tipusDocument == null) {
        throw new CustodyException("No s'ha definit la propietat "
            + defaultProperty);
      }
    }

    return tipusDocument;
  }
  
  
  /**
   * Custodia un document
   * @param custodyID
   * @param document
   * @throws Exception
   */
  @Override
  public void saveAll(String custodyID, String custodyParameters,
      DocumentCustody document,  SignatureCustody signature,
      Metadata[] metadata) throws CustodyException,  NotSupportedCustodyException, MetadataFormatException {
    
    if (document != null) {
      saveDocument(custodyID, custodyParameters, document);
    }
    
    if (signature != null) {
      saveSignature(custodyID, custodyParameters, signature);
    }
    
    if (metadata != null) {
      addMetadata(custodyID, metadata);
    }
    
  }
  

  /**
   * 
   */
  @Override
  public void saveDocument(String custodyID, String custodyParameters,
      DocumentCustody document) throws CustodyException,
      NotSupportedCustodyException {
    
    internalsaveDocument(custodyID, custodyParameters,
         document, null);
  }
  
  
  @Override
  public void saveSignature(String custodyID, String custodyParameters,
      SignatureCustody signature) throws CustodyException,
      NotSupportedCustodyException {

    String signatureType = signature.getSignatureType();
    if (signatureType == null || !SUPPORTED_SIGN_TYPES.contains(signatureType)) {
      throw new NotSupportedCustodyException(signatureType);
    }
    
    internalsaveDocument(custodyID, custodyParameters,
         signature, signatureType);
  }
  
  
  
  public void internalsaveDocument(String custodyID, String custodyParameters,
      AnnexCustody document, String type) throws CustodyException,
      NotSupportedCustodyException {
    log.info(" ================== custodyDocument [" + custodyID + "] ==================");
    byte[] response;
    CustodiaResponse resp;
    ClienteCustodia clienteCustodia = getClienteCustodia();
    try {
      String codigoExternoTipoDocumento = getDocumentType(custodyParameters);
      log.info(" codigoExternoTipoDocumento =  ]" + codigoExternoTipoDocumento + "[");
      if (SignatureCustody.PADES_SIGNATURE.equals(type)) {
        log.info(" Custodia PADES");
        response = clienteCustodia.custodiarPDFFirmado(
            new ByteArrayInputStream(document.getData()), document.getName(), custodyID,
            codigoExternoTipoDocumento);

      } else if (SignatureCustody.XADES_SIGNATURE.equals(type)) {
        log.info(" Custodia XADES");
        response = clienteCustodia.custodiarDocumentoXAdES(
            new ByteArrayInputStream(document.getData()), document.getName(), custodyID,
            codigoExternoTipoDocumento);
      } else if (SignatureCustody.SMIME_SIGNATURE.equals(type)) {
        log.info(" Custodia SMIME");
        response = clienteCustodia.custodiarDocumentoSMIME(
            new ByteArrayInputStream(document.getData()), document.getName(), custodyID,
            codigoExternoTipoDocumento);
      } else {
        // NONE_SIGNATURE
        log.info(" Custodia PLAIN DOC");
        response = clienteCustodia.custodiarDocumento(
            new ByteArrayInputStream(document.getData()), document.getName(), custodyID,
            codigoExternoTipoDocumento);
      }

      // CUSTODIA RESPONSE
      JAXBContext jaxbContext = JAXBContext.newInstance(CustodiaResponse.class);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      resp = (CustodiaResponse) jaxbUnmarshaller.unmarshal(new ByteArrayInputStream(response));
    } catch (Exception re) {
      throw new CustodyException(re);
    }
    // TODO Debug
    ResponseBaseType rbt = resp.getVerifyResponse();
    Result result = rbt.getResult();

    checkResult(response, result);

    // TODO DEBUG
    AnyType anyType = rbt.getOptionalOutputs();
    if (anyType == null || anyType.getAny() == null || anyType.getAny().isEmpty()) {
      return;
    }

    List<Object> list = anyType.getAny();

    for (Object object : list) {

      if (object instanceof ResultadoFirmas) {
        ResultadoFirmas rf = (ResultadoFirmas) object;

        List<ResultadoFirma> listRF = rf.getResultadoFirma();
        for (ResultadoFirma resultadoFirma : listRF) {
          log.info("=== ResultadoFirma ");
          List<ValidacionCertificado> vcList = resultadoFirma.getValidacionCertificado();
          for (ValidacionCertificado validacionCertificado : vcList) {
            log.info("=== +++ ValidacionCertificado ");
            log.info("        * NAME: " + validacionCertificado.getSubjectName());
            log.info("        * SERI: " + validacionCertificado.getNumeroSerie());
            log.info("        * VERI: " + validacionCertificado.isVerificado());
            log.info("        * URL : " + validacionCertificado.getUrl());
          }
        }
      }
    }
  }

  protected void checkResult(byte[] response, Result result) throws CustodyException {
    // TODO DEBUG
    log.info("Result::major = " + result.getResultMajor());
    log.info("Result::minor = " + result.getResultMinor());
    log.info("Result::msg = " + result.getResultMessage());

    if (!"Success".equals(result.getResultMajor())) {
      log.error(new String(response));
      
      System.out.println(new String(response));
      
      String msg;
      if (result.getResultMessage() != null) {
        msg = result.getResultMessage().getLang() + "::"
            + result.getResultMessage().getValue();
      } else {
        msg = "Error desconegut";
      }
      throw new CustodyException(msg);
    }
  }

  @Override
  public byte[] getDocument(String custodyID) throws CustodyException {
    try {
      byte[] consultar = getClienteCustodia().consultarDocumento(custodyID);
      byte[] iniciXml = new byte[5];
      // TODO optimitzar obtenir 5 primers bytes
      for (int i = 0; i < 5; i++) {
        iniciXml[i] = consultar[i];
      }
      if ("<?xml".equals(new String(iniciXml))) {
        // Hem de veure si és un error o una firma XAdES
        try {
          JAXBContext jaxbContext = JAXBContext.newInstance(ConsultaResponse.class);
          Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

          ConsultaResponse resp = (ConsultaResponse) jaxbUnmarshaller
              .unmarshal(new ByteArrayInputStream(consultar));

          checkResult(consultar, resp.getResult());

        } catch (javax.xml.bind.UnmarshalException e) {
          // És una firma XAdES
        }
      }

      return consultar;
    } catch (Exception e) {
      throw new CustodyException(e);
    }

  }



  @Override
  public void deleteCustody(String custodyID) throws CustodyException {
    try {

      byte[] response = getClienteCustodia().eliminarDocumento(custodyID);

      JAXBContext jaxbContext = JAXBContext.newInstance(EliminacionResponse.class);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

      EliminacionResponse resp = (EliminacionResponse) jaxbUnmarshaller
          .unmarshal(new ByteArrayInputStream(response));

      checkResult(response, resp.getResult());

    } catch (Exception e) {
      throw new CustodyException(e);
    }

  }

  /**
   * {0} = HASH | {1} = CodiExtern | {2} = Codi Especial
   */
  @Override
  public String getValidationUrl(String custodyID) throws CustodyException {

    ReservaResponse rr = internalReserveCustody(custodyID);

    String url = getProperty(URLVALIDATION);

    return MessageFormat.format(url, rr.getHash(), rr.getCodigo(), getSpecialValue(custodyID));
  }

  @Override
  public String getSpecialValue(String custodyID) throws CustodyException {
    return custodyID;
  }
  

  @Override
  public String[] getSupportedSignatureTypes() {
    return SUPPORTED_SIGN_TYPES_ARRAY;
  }

  @Override
  public DocumentCustody getDocumentInfo(String custodyID) throws CustodyException {

    Documento doc = getInfoDocumentoCustodiaCAIB(custodyID);

    DocumentCustody dc = new DocumentCustody();

    dc.setData(getDocument(custodyID));
    dc.setName(doc.getNombre());
    
    //String clase = doc.getClase();

    return dc;
  }
  
  

  @Override
  public DocumentCustody getDocumentInfoOnly(String custodyID) throws CustodyException {
    
    // TODO Optimitzar per a que nomes obtengui la info i no el contingut del fitxer
    DocumentCustody dc = getDocumentInfo(custodyID);
    
    if (dc != null) {
      dc.setData(null);
    }
    
    return dc;
  }

  
  @Override
  public byte[] getSignature(String custodyID) throws CustodyException {
    return getDocument(custodyID);
  }
  
  
  @Override
  public SignatureCustody getSignatureInfo(String custodyID) throws CustodyException {

    Documento doc = getInfoDocumentoCustodiaCAIB(custodyID);

    SignatureCustody dc = new SignatureCustody();

    dc.setData(getDocument(custodyID));
    dc.setName(doc.getNombre());
    
    String clase = doc.getClase();
    
   if (ClienteCustodia.XADES.equals(clase)) {
      dc.setSignatureType(SignatureCustody.XADES_SIGNATURE);
      dc.setAttachedDocument(null);
    } else if (ClienteCustodia.SMIME.equals(clase)) {
      dc.setSignatureType(SignatureCustody.SMIME_SIGNATURE);
      dc.setAttachedDocument(null);
    } else if (ClienteCustodia.PDF_FIRMADO.equals(clase)) {
      dc.setSignatureType(SignatureCustody.PADES_SIGNATURE);
      dc.setAttachedDocument(null);
    } else {
      return null;
    }
    return dc;
  }
  
  
  @Override
  public SignatureCustody getSignatureInfoOnly(String custodyID) throws CustodyException {
    // TODO Optimitzar per a que nomes obtengui la info i no el contingut del fitxer
    
    SignatureCustody sc = getSignatureInfo(custodyID);
    
    if (sc != null) {
      sc.setData(null);
    }
    
    return sc;
  }




  private Documento getInfoDocumentoCustodiaCAIB(String custodyID) throws CustodyException {
    byte[] response;
    InformeResponse informe;
    try {
      response = getClienteCustodia().obtenerInformeDocumento(custodyID);

      JAXBContext jaxbContext = JAXBContext.newInstance(InformeResponse.class);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

      informe = (InformeResponse) jaxbUnmarshaller
          .unmarshal(new ByteArrayInputStream(response));
    } catch (Exception e) {
      throw new CustodyException();
    }
    checkResult(response, informe.getResult());

    Documento doc = informe.getDocumento();
    return doc;
  }

  



  /**
   * 
   * @param custodyID
   * @param annex
   * @return AnnexID
   * @throws CustodyException
   */
  @Override
  public String addAnnex(String custodyID, AnnexCustody annex) throws CustodyException,
      NotSupportedCustodyException {
    throw new NotSupportedCustodyException();
  }

  /**
   * 
   * @param custodyID
   * @param annexID
   * @return null if annex not found
   */
  @Override
  public byte[] getAnnex(String custodyID, String annexID) {
    return null;
  }

  /**
   * 
   * @param custodyID
   * @param annexID
   * @return null if annex not found
   */
  @Override
  public AnnexCustody getAnnexInfo(String custodyID, String annexID) {
    return null;
  }
  
  

  @Override
  public AnnexCustody getAnnexInfoOnly(String custodyID, String annexID)
      throws CustodyException {
    // TODO Optimitzar per a que nomes obtengui la info i no el contingut del fitxer
    return null;
  }



  @Override
  public void deleteDocument(String custodyID) throws CustodyException,
      NotSupportedCustodyException {
    
    
  }



  @Override
  public boolean supportsDeleteDocument() {
    
    return false;
  }



  @Override
  public void deleteSignature(String custodyID) throws CustodyException,
      NotSupportedCustodyException {
    
    
  }



  @Override
  public boolean supportsDeleteSignature() {
    
    return false;
  }



  @Override
  public Boolean supportsAutomaticRefreshSignature() {
    return false;
  }



  @Override
  public void deleteAnnex(String custodyID, String annexID) throws CustodyException,
      NotSupportedCustodyException {
        throw new NotSupportedCustodyException();
  }



  @Override
  public void deleteAllAnnexes(String custodyID) throws CustodyException,
      NotSupportedCustodyException {
    throw new NotSupportedCustodyException();
    
  }



  @Override
  public ArrayList<String> getAllAnnexes(String custodyID) throws CustodyException {
    return null; 
  }



  @Override
  public boolean supportsAnnexes() {
    return false;
  }



  @Override
  public boolean supportsDeleteAnnex() {
    return false;
  }



  @Override
  public void addMetadata(String custodyID, Metadata metadata) throws CustodyException,
      NotSupportedCustodyException, MetadataFormatException {
    throw new NotSupportedCustodyException();
    
  }



  @Override
  public void addMetadata(String custodyID, Metadata[] metadata) throws CustodyException,
      NotSupportedCustodyException, MetadataFormatException {
    throw new NotSupportedCustodyException();
    
  }
  
  
  @Override
  public void updateMetadata(String custodyID, Metadata metadata) throws CustodyException,
      NotSupportedCustodyException, MetadataFormatException {
    throw new NotSupportedCustodyException();
    
  }



  @Override
  public void updateMetadata(String custodyID, Metadata[] metadata) throws CustodyException,
      NotSupportedCustodyException, MetadataFormatException {
    throw new NotSupportedCustodyException();
    
  }



  @Override
  public ArrayList<Metadata> deleteMetadata(String custodyID, String[] keys)
      throws CustodyException {
    return null;
  }




  @Override
  public HashMap<String, ArrayList<Metadata>> getAllMetadata(String custodyID)
      throws CustodyException, NotSupportedCustodyException {
    throw new NotSupportedCustodyException();
  }



  @Override
  public ArrayList<Metadata> getMetadata(String custodyID, String key)
      throws CustodyException, NotSupportedCustodyException {
    throw new NotSupportedCustodyException();
  }



  @Override
  public Metadata getOnlyOneMetadata(String custodyID, String key) throws CustodyException,
      NotSupportedCustodyException {
    throw new NotSupportedCustodyException();
  }



  @Override
  public boolean supportsMetadata() {
    return false;
  }



  @Override
  public void deleteAllMetadata(String custodyID) throws CustodyException {
  }



  @Override
  public ArrayList<Metadata> deleteMetadata(String custodyID, String key)
      throws CustodyException {
    return null;
  }



  @Override
  public boolean supportsDeleteMetadata() {
    return false;
  }



  @Override
  public boolean supportsDeleteCustody() {
    return true;
  }








}
