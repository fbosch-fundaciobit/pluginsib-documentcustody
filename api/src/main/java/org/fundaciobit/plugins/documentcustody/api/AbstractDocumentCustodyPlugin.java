package org.fundaciobit.plugins.documentcustody.api;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.fundaciobit.plugins.utils.AbstractPluginProperties;
import org.fundaciobit.plugins.utils.Base64;
import org.fundaciobit.plugins.utils.Metadata;
import org.fundaciobit.plugins.utils.MetadataFormatException;

import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * Implementacio genèrica de DocumentCustody. Només s'han d'implementar 6 mètodes:
 *    - deleteFile(String custodyID, String ... relativePaths);
 *    - existsFile(String custodyID, String relativePath);
 *    - writeFile(String custodyID, String relativePath, byte[] data) throws Exception;
 *    - byte[] readFile(String custodyID, String relativePath) throws Exception;
 *    - long lengthFile(String custodyID, String relativePath) throws Exception;
 *    - String getPropertyBase();
 *
 * @author anadal
 *
 */
public abstract class AbstractDocumentCustodyPlugin extends AbstractPluginProperties
  implements IDocumentCustodyPlugin {

  protected final Logger log = Logger.getLogger(getClass());
  
  private String prefix = null;

  public static final String ABSTRACT_PREFIX = "prefix";
  
  public static final String ABSTRACT_BASE_URL = "baseurl";
  
  public static final String ABSTRACT_BASE_URL_EXPRESSION_LANGUAGE = "baseurl_expressionlanguage";
  
  public static final String ABSTRACT_HASH_PASSWORD = "hash.password";

  public static final String ABSTRACT_HASH_ALGORITHM= "hash.algorithm";
  
  public static final String ABSTRACT_FOLDER_EXPRESSION_LANGUAGE = "folder_expressionlanguage";
  
  public static final String ABSTRACT_GENERATEUNIQUE_CUSTODYID_EXPRESSION_LANGUAGE = "generate_custodyid_expressionlanguage";

  public static final String ABSTRACT_SPECIALVALUE_EXPRESSION_LANGUAGE = "specialvalue_expressionlanguage";
  
  public static final String ABSTRACT_AUTOMATIC_METADATA_ITEMS = "automaticmetadata_items";
  
  public static final String ABSTRACT_AUTOMATIC_METADATA = "automatic_metadata";
  
  /**
   * 
   */
  public AbstractDocumentCustodyPlugin() {
    super();
  }

  /**
   * @param propertyKeyBase
   * @param properties
   */
  public AbstractDocumentCustodyPlugin(String propertyKeyBase, Properties properties) {
    super(propertyKeyBase, properties);
  }

  /**
   * @param propertyKeyBase
   */
  public AbstractDocumentCustodyPlugin(String propertyKeyBase) {
    super(propertyKeyBase);
  }

  private final String getCustodyDocumentName(String custodyID, Map<String,Object> custodyParameters) throws CustodyException {
    return CUSTODY_PREFIX_AND_FOLDER(custodyParameters) + custodyID + ".DOC";
  }
  


  private final String getCustodyDocumentInfoName(String custodyID, Map<String,Object> custodyParameters) throws CustodyException {
    return CUSTODY_PREFIX_AND_FOLDER(custodyParameters) + custodyID + ".DOCINFO";
  }

  private final String getCustodySignatureName(String custodyID, Map<String,Object> custodyParameters) throws CustodyException {
    return CUSTODY_PREFIX_AND_FOLDER(custodyParameters) + custodyID + ".SIGN";
  }

  private final String getCustodySignatureInfoName(String custodyID, Map<String,Object> custodyParameters) throws CustodyException {
    return CUSTODY_PREFIX_AND_FOLDER(custodyParameters) +  custodyID + ".SIGNINFO";
  }
  
  
  private final String getCustodyAnnexName(String annexID, Map<String,Object> custodyParameters) throws CustodyException {
    return CUSTODY_PREFIX_AND_FOLDER(custodyParameters) + annexID + ".ANNEX";
  }


  private final String getCustodyAnnexInfoName(String annexID, Map<String,Object> custodyParameters) throws CustodyException {
    return CUSTODY_PREFIX_AND_FOLDER(custodyParameters) + annexID + ".ANNEXINFO";
  }
  
 
  private final String getCustodyMetadataInfoName(String custodyID, Map<String,Object> custodyParameters) throws CustodyException {
    return CUSTODY_PREFIX_AND_FOLDER(custodyParameters) + custodyID + ".METAINFO";
  }
  
  
  // USING ONLY CUSTODY
  private final String getCustodyDocumentName2(String custodyID) throws CustodyException {
    return CUSTODY_PREFIX_AND_FOLDER(custodyID) + custodyID + ".DOC";
  }
  
    private final String getCustodyDocumentInfoName2(String custodyID) throws CustodyException {
    return CUSTODY_PREFIX_AND_FOLDER(custodyID) + custodyID + ".DOCINFO";
  }

  private final String getCustodySignatureName2(String custodyID) throws CustodyException {
    return CUSTODY_PREFIX_AND_FOLDER(custodyID) + custodyID + ".SIGN";
  }

  private final String getCustodySignatureInfoName2(String custodyID) throws CustodyException {
    return CUSTODY_PREFIX_AND_FOLDER(custodyID) +  custodyID + ".SIGNINFO";
  }
  
  
  private final String getCustodyAnnexName2(String custodyID, String annexID) throws CustodyException {
    return CUSTODY_PREFIX_AND_FOLDER(custodyID) + annexID + ".ANNEX";
  }


  private final String getCustodyAnnexInfoName2(String custodyID, String annexID) throws CustodyException {
    return CUSTODY_PREFIX_AND_FOLDER(custodyID) + annexID + ".ANNEXINFO";
  }
  
  private final String getCustodyAnnexListName2(String custodyID) throws CustodyException {
    return CUSTODY_PREFIX_AND_FOLDER(custodyID) + custodyID + ".ANNEXLIST";
  }
  
  private final String getCustodyMetadataInfoName2(String custodyID) throws CustodyException {
    return CUSTODY_PREFIX_AND_FOLDER(custodyID) + custodyID + ".METAINFO";
  }

  
  private final String getCustodyHashesFile() {
    return CUSTODY_PREFIX() + "HASH__FILE.properties";
  }

  protected String CUSTODY_PREFIX() {
    if (prefix == null) {
      String pfix = getProperty(getPropertyBase() + ABSTRACT_PREFIX);
      if (pfix == null || pfix.trim().length() == 0) {
        pfix = "";
      } else {
        pfix = pfix.trim();
        if (!pfix.endsWith("_")) {
          pfix = pfix + "_";
        }
      }
      prefix = pfix;  
    }
    return prefix; 
  }

  protected String CUSTODY_PREFIX_AND_FOLDER(Map<String,Object> custodyParameters) throws CustodyException { // ) { //
    String pfix = CUSTODY_PREFIX();
    
    String realFolder = REALFOLDER(custodyParameters);
    
    return realFolder + pfix; 
  }
  
  
  protected String CUSTODY_PREFIX_AND_FOLDER(String custodyID) throws CustodyException { // ) { //
    String pfix = CUSTODY_PREFIX();
    
    String realFolder = getRealFolderFromCustodyID(custodyID);
    
    return realFolder + pfix; 
  }
  

  protected String REALFOLDER(Map<String, Object> custodyParameters) throws CustodyException {
    String folder = getProperty(getPropertyBase() + ABSTRACT_FOLDER_EXPRESSION_LANGUAGE);
    String realFolder;
    if (folder == null || folder.trim().length() == 0) {
      realFolder = "";  
    } else {
      realFolder = processExpressionLanguage(folder, custodyParameters);
    }
    return realFolder;
  }

  // TODO 
  private String getURLBase() {
    return getProperty(getPropertyBase() + ABSTRACT_BASE_URL);
  }
  
  
  // ---------------------------------------------------------------------
  // ---------------------------------------------------------------------
  // ---------------------- C O M M O N    C O D E  ----------------------
  // ---------------------------------------------------------------------
  // ---------------------------------------------------------------------


  @Override
  public synchronized String reserveCustodyID(Map<String, Object> custodyParameters) throws CustodyException {
    
    final String custodyID;
    
    String custodyIdEL = getProperty(getPropertyBase() + ABSTRACT_GENERATEUNIQUE_CUSTODYID_EXPRESSION_LANGUAGE);
    if (custodyIdEL == null || custodyIdEL.trim().length() == 0 ) {
       custodyID = generateUniqueCustodyID(custodyParameters);
    } else {
       custodyID = processExpressionLanguage(custodyIdEL, custodyParameters);
    }


    // Inicialitza Fitxers de HASH si la URL conté el sistema de HASH
    String baseUrl = getURLBase();
    if (baseUrl != null && baseUrl.indexOf("{2}") != -1) {
       String hash = generateHash(custodyID);
       
       try {
         Properties props = new Properties();
         final String path = getCustodyHashesFile();
         
         if (existsFile(custodyID, path)) {
           byte [] data = readFile(custodyID, path);
           props.load(new ByteArrayInputStream(data));
         }
         props.setProperty(hash, custodyID);
         //System.out.println(" Guardant fitxer(" + hashes.getAbsolutePath() + ");");
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         props.store(baos, "Hash Properties");
         
         writeFile(custodyID, path, baos.toByteArray());
         
       } catch(Exception e) {
         throw new CustodyException("Error adding hash value to properties file.", e);
       }

    }
    
    
    String realFolder = REALFOLDER(custodyParameters);
    if (realFolder != null && realFolder.trim().length() != 0) {
    
       String path = getCachePath(custodyID);

       try {
         writeFileCreateParentDir(custodyID, path, realFolder.getBytes());
       } catch(Exception e) {
         throw new CustodyException("Error guardant Cache de " + path, e);
       }
       
       
       try {
         //writeFile(custodyID, path, realFolder.getBytes());
         final String touchPath =  realFolder + "touch"; 
         writeFileCreateParentDir(custodyID, touchPath,
            "this file is temporal and if you see this, then there is an but".getBytes());
         deleteFile(custodyID, touchPath);
         
       } catch(Exception e) {
         throw new CustodyException("Error guardant Cache de .", e);
       }
          
    }
    
    // Pot ser que depengui de dades de fitxer o signatura
    final boolean ignoreErrors = true;
    updateAutomaticMetadatas(custodyID, custodyParameters, ignoreErrors);

    return custodyID;
  }


  protected String generateUniqueCustodyID(Map<String, Object> custodyParameters) throws CustodyException {
    // NOTA: S'afegiex nanoTime per quan hi ha un canvi d'hora
    String id = String.valueOf(System.currentTimeMillis() + "" + System.nanoTime());
         
    try { Thread.sleep(50); } catch(Exception e) { }         
         
    return id;
  }
  
  
  
  protected String getRealFolderFromCustodyID(String custodyID) throws CustodyException {
  
    String folder = getProperty(getPropertyBase() + ABSTRACT_FOLDER_EXPRESSION_LANGUAGE);
    
    if (folder == null || folder.trim().length() == 0) {
      return "";
    } else {
  
  
       // Indicar al sistema la ubicació del fitxer
       String path = getCachePath(custodyID);
       
       
       try {
         byte[] realFolder = readFile(custodyID, path);
         if (realFolder == null) {
           return "";
         } else {
           return new String(realFolder);
         }
       } catch(Exception e) {
         throw new CustodyException("Error guardant Cache de .", e);
       }
  
    }
  }

  protected String getCachePath(String custodyID) {
    String  b64 = Base64.encode(custodyID);
     
     String dir1,dir2;
     if (b64.length() < 2) {
        dir1 = "0";
        dir2 = "0";
     } else {
        b64 = b64.replace('=', ' ').trim();
        dir1 = "" + b64.charAt(b64.length() - 1);
        dir2 = "" + b64.charAt(b64.length() - 2);
     }

     String path = getCacheFolderName() + "/" + dir1 + "/" + dir2 + "/" + custodyID;
    return path;
  }
  
  
  public String getCacheFolderName() {
     return ".cachefolder";
  }
  
  
  @Override
  public boolean supportsDeleteCustody() {    
    return this.supportsDeleteDocument() 
        && this.supportsDeleteSignature()
        && this.supportsDeleteAnnex()
        && this.supportsDeleteMetadata();
  }


  @Override
  public void deleteCustody(String custodyID) throws CustodyException, NotSupportedCustodyException {

    if (!supportsDeleteCustody()) {
      throw new NotSupportedCustodyException();
    }
    
    this.deleteAllMetadata(custodyID);
    
    this.deleteAllAnnexes(custodyID);
    
    this.deleteSignature(custodyID);

    this.deleteDocument(custodyID);

    // TODO  Esborrar directori pare si tenim folder i esta buit????
    // Per ara no podem esborrar ja que no se si hi ha altres documents al mateix directori
    // Fins i tot pot existir el document electrònic però que no tengui cap arxiu relacionat.
    
    
    // Esborrar cache
    String folder = getProperty(getPropertyBase() + ABSTRACT_FOLDER_EXPRESSION_LANGUAGE);
    
    if (folder == null || folder.trim().length() == 0) {
      // OK No fer res.
    } else {
      String path = getCachePath(custodyID);
      try {
        deleteFile(custodyID, path);
      } catch (Exception e) {
        log.warn("Error desconegur esborrant fitxer amb path=" + path
           + "(custodyID=" + custodyID + ")");
      }
    }
  }

  /**
   * Si existeix el document de Signatura llavors el retornam, ja que és el que
   * es necessita per validar el document. En cas contrari o no té firmes o el
   * propi document ja duu adjunta la firma, per lo que retornam el document.
   * 
   * Valors de substitució:
   *     // {0} => custodyID
   *     // {1} => URLEncode(custodyID)
   *     // {2} => Hash(custodyID)
   */
  @Override
  public String getValidationUrl(String custodyID, Map<String, Object> parameters) throws CustodyException {

    String baseUrl = getURLBase();
    String baseUrlEL = getProperty(getPropertyBase() + ABSTRACT_BASE_URL_EXPRESSION_LANGUAGE);
    
    String hashPassword = getProperty(getPropertyBase() + ABSTRACT_HASH_PASSWORD,"");

    //  Valid values       MD2, MD5, SHA,SHA-256,SHA-384,SHA-512
    String hashAlgorithm =  getProperty(getPropertyBase() +  ABSTRACT_HASH_ALGORITHM,"MD5");
    
    return getValidationUrlStatic(custodyID, parameters, baseUrl, baseUrlEL,
        hashAlgorithm, hashPassword, log);
    

  }

  public static String getValidationUrlStatic(String custodyID, Map<String, Object> parameters,
      String baseUrl, String baseUrlEL, String hashAlgorithm, String hashPassword,
      Logger log ) throws CustodyException {
    // {0} => custodyID
    // {1} => URLEncode(custodyID)
    String urlEncoded;
    try {
      urlEncoded = URLEncoder.encode(custodyID, "UTF-8");
    } catch (UnsupportedEncodingException e1) {
      urlEncoded = custodyID;
    }
    // {2} => Hash(custodyID)
    final String hash = generateHash(custodyID, hashAlgorithm, hashPassword);
    
    
    if (baseUrl != null) {
      return MessageFormat.format(baseUrl, custodyID,urlEncoded, hash);
    } 

    
    if (baseUrlEL == null) {
      return null;
    }
    
    try {
      Map<String, Object> parameters2 = new HashMap<String, Object>();

      parameters2.put("validationUrl_custodyID", custodyID);
      parameters2.put("validationUrl_custodyID_URLEncode", urlEncoded);
      parameters2.put("validationUrl_custodyID_Hash", hash);

      if (parameters != null) {
        parameters2.putAll(parameters);
      }
      return processExpressionLanguage(baseUrlEL, parameters2);
    } catch (Exception e) {
      String msg = "No s'ha pogut processar la EL " + baseUrl + ": " + e.getMessage();
      log.error(msg, e);
      // TODO Translate
      throw new CustodyException(msg, e);
    }
  }
  
  
  
  public String generateHash(String data) {

    String hashPassword = getProperty(getPropertyBase() + ABSTRACT_HASH_PASSWORD,"");

    //  Valid values       MD2, MD5, SHA,SHA-256,SHA-384,SHA-512
    String hashAlgorithm =  getProperty(getPropertyBase() +  ABSTRACT_HASH_ALGORITHM,"MD5");

    return generateHash(data, hashAlgorithm, hashPassword);

  }
  
  
  
  public static String generateHash(String data, String algorithm, String salt) {
    try {
      
      java.security.MessageDigest md = java.security.MessageDigest.getInstance(algorithm);
      byte[] array = md.digest((data + salt).getBytes());
      StringBuffer sb = new StringBuffer();
      for (int i = 0; i < array.length; ++i) {
        sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
     }
      return sb.toString();
  } catch (java.security.NoSuchAlgorithmException e) {
  }
  return null;
}

  
  public static String processExpressionLanguage(String plantilla,
      Map<String, Object> custodyParameters) throws CustodyException {
    return processExpressionLanguage(plantilla, custodyParameters, null);
  }
  
  public static String processExpressionLanguage(String plantilla,
      Map<String, Object> custodyParameters,  Locale locale) throws CustodyException {
    try {
    if (custodyParameters == null) {
      custodyParameters = new  HashMap<String, Object>();
    }
    
    Configuration configuration;

    configuration = new Configuration(Configuration.VERSION_2_3_23);
    configuration.setDefaultEncoding("UTF-8");
    if (locale!= null) {
      configuration.setLocale(locale);
    }
    Template template;
    template = new Template("exampleTemplate", new StringReader(plantilla),
        configuration);

    Writer out = new StringWriter();
    template.process(custodyParameters, out);
    
    String res = out.toString();
    return res;
    } catch(Exception e) {
      final String msg = "No s'ha pogut processar l'Expression Language " + plantilla 
        + ":" + e.getMessage();
      throw new CustodyException(msg, e);
    }
  }
  
  

  @Override
  public String getSpecialValue(String custodyID, Map<String, Object> custodyParameters) throws CustodyException {
    
    String specialValue = getProperty(getPropertyBase() + ABSTRACT_SPECIALVALUE_EXPRESSION_LANGUAGE);
    if (specialValue == null || specialValue.trim().length() == 0) {
      return custodyID;
    } else {
      return processExpressionLanguage(specialValue, custodyParameters);
    }
  }
  



  /**
   * Custodia un document
   * @param custodyID
   * @param document
   * @throws Exception
   */
  @Override
  public void saveAll(String custodyID, Map<String, Object> custodyParameters,
      DocumentCustody document,  SignatureCustody signature,
      Metadata[] metadata) throws CustodyException,  NotSupportedCustodyException, MetadataFormatException {
    
    if (document == null) {
      deleteDocument(custodyID);
    } else {
      saveDocument(custodyID, custodyParameters, document);
    }
    
    if (signature == null) {
      deleteSignature(custodyID);
    } else {
      saveSignature(custodyID, custodyParameters, signature);
    }

    if (metadata != null && metadata.length != 0) {
      updateMetadata(custodyID, metadata, custodyParameters);
    }
    
  }
  
  
  
  
  // ---------------------------------------------------------------------
  // ---------------------------------------------------------------------
  // -------------------------- D O C U  M E N T -------------------------
  // ---------------------------------------------------------------------
  // ---------------------------------------------------------------------


  @Override
  @Deprecated
  public void saveDocument(String custodyID, Map<String, Object> custodyParameters, DocumentCustody document)
      throws CustodyException, NotSupportedCustodyException {

    String infoPath;
    String docPath;

    infoPath = getCustodyDocumentInfoName(custodyID, custodyParameters);
    docPath = getCustodyDocumentName(custodyID, custodyParameters);

    try {
      
      if (existsFile(custodyID, docPath)) {
        deleteFile(custodyID, docPath);
      }
      writeFile(custodyID, docPath, document.getData());
     

      
      DocumentCustody clone = new DocumentCustody(document);
      clone.setData(null);
      writeObject(custodyID, infoPath, clone);
      
      final boolean ignoreErrors = false;
      updateAutomaticMetadatas(custodyID, custodyParameters, ignoreErrors);

    } catch (Exception ex) {
      final String msg = "No s'ha pogut custodiar el document";
      log.error(msg, ex);
      throw new CustodyException(msg, ex);
    }

  }
  
  
  @Override
  public boolean supportsDeleteDocument() {
    return true;
  }
  
  
  /**
   * 
   * @param custodyID
   * @param custodyParameters
   * @param document
   * @throws CustodyException
   * @throws NotSupportedCustodyException
   */
  public void deleteDocument(String custodyID) throws CustodyException, NotSupportedCustodyException {
    deleteFile(custodyID, 
        getCustodyDocumentName2(custodyID), getCustodyDocumentInfoName2(custodyID));
    
  }
  
  

  @Override
  public DocumentCustody getDocumentInfo(String custodyID) throws CustodyException {

    if (custodyID == null) {
      return null;
    }

    String docPath = getCustodyDocumentName2(custodyID);
    String infoPath = getCustodyDocumentInfoName2(custodyID);
    return (DocumentCustody) getDocOrSign(custodyID, docPath, infoPath);
  }
  
  

  @Override
  public DocumentCustody getDocumentInfoOnly(String custodyID) throws CustodyException {
    if (custodyID == null) {
      return null;
    }

    String infoPath = getCustodyDocumentInfoName2(custodyID);
    DocumentCustody dc = (DocumentCustody) getDocOrSignOnlyInfo(custodyID, infoPath);
    
    String docPath = getCustodyDocumentName2(custodyID);
    checkSize(custodyID, dc, infoPath,  docPath);
    
    return dc;
  }


  @Override
  public byte[] getDocument(String custodyID) throws CustodyException {
    DocumentCustody info = getDocumentInfo(custodyID);
    if (info == null) {
      return null;
    }
    return info.getData();
  }


  
  // ---------------------------------------------------------------------
  // ---------------------------------------------------------------------
  // -------------------------- S I G N A T U R E -----------------------
  // ---------------------------------------------------------------------
  // ---------------------------------------------------------------------

  
  
  @Override
  @Deprecated
  public void saveSignature(String custodyID, Map<String, Object> custodyParameters,
      SignatureCustody document) throws CustodyException {

    String infoPath;
    String docPath;

    infoPath = getCustodySignatureInfoName(custodyID, custodyParameters);
    docPath = getCustodySignatureName(custodyID, custodyParameters);

    try {
      
      if (existsFile(custodyID, docPath)) {
        deleteFile(custodyID, docPath);
      }
      writeFile(custodyID, docPath, document.getData());

      SignatureCustody clone = new SignatureCustody(document);
      clone.setData(null);
      writeObject(custodyID, infoPath, clone);
      
      final boolean ignoreErrors = false;
      updateAutomaticMetadatas(custodyID, custodyParameters, ignoreErrors);

    } catch (Exception ex) {
      final String msg = "No s'ha pogut custodiar la firma del document";
      log.error(msg, ex);
      throw new CustodyException(msg, ex);
    }

  }
  
  
  @Override
  public boolean supportsDeleteSignature() {
    return true;
  }
  
  
  @Override
  public void deleteSignature(String custodyID) throws CustodyException, NotSupportedCustodyException {
    deleteFile(custodyID, getCustodySignatureName2(custodyID), getCustodySignatureInfoName2(custodyID));
  }
  

  @Override
  public SignatureCustody getSignatureInfo(String custodyID) throws CustodyException {

    if (custodyID == null) {
      return null;
    }

    String signPath = getCustodySignatureName2(custodyID);
    String infoPath = getCustodySignatureInfoName2(custodyID);
    return (SignatureCustody) getDocOrSign(custodyID, signPath, infoPath);
  }
  
  
  @Override
  public SignatureCustody getSignatureInfoOnly(String custodyID) throws CustodyException {
    if (custodyID == null) {
      return null;
    }
    String infoPath = getCustodySignatureInfoName2(custodyID);
    SignatureCustody sc = (SignatureCustody) getDocOrSignOnlyInfo(custodyID, infoPath);
    String signPath = getCustodySignatureName2(custodyID);
    checkSize(custodyID, sc, infoPath, signPath);
    return sc;
  }


  @Override
  public byte[] getSignature(String custodyID) throws CustodyException {
    SignatureCustody info = getSignatureInfo(custodyID);
    if (info == null) {
      return null;
    }
    return info.getData();
  }

  @Override
  public String[] getSupportedSignatureTypes() {
    return  SignatureCustody.ALL_TYPES_OF_SIGNATURES;
  }

  /**
   * @return true if system automaically refresh signature o document with
   *         signature to not loss validate of signature. false otherwise. Null unknown
   */
  @Override
  public Boolean supportsAutomaticRefreshSignature() {
    return false;
  }
  
  
  
  // ---------------------------------------------------------------------
  // ---------------------------------------------------------------------
  // ----------------------------- A N N E X  ----------------------------
  // ---------------------------------------------------------------------
  // ---------------------------------------------------------------------

  /**
   * 
   * @param custodyID
   * @param annex
   * @return AnnexID
   * @throws CustodyException
   */
  @Override
  public synchronized String addAnnex(String custodyID, AnnexCustody annex, Map<String, Object> custodyParameters) throws CustodyException,
      NotSupportedCustodyException {
    
    final String annexID = custodyID + "_" + System.nanoTime();
    try {
       Thread.sleep(500);      
    } catch (Exception e) {
    }
    
    String infoPath;
    String docPath;

    infoPath = getCustodyAnnexInfoName(annexID, custodyParameters);
    docPath = getCustodyAnnexName(annexID, custodyParameters);

    try {
      
      if (existsFile(custodyID, docPath)) {
        deleteFile(custodyID, docPath);
      }
      writeFile(custodyID, docPath, annex.getData());

      AnnexCustody clone = new AnnexCustody(annex);
      clone.setData(null);
      writeObject(custodyID, infoPath, clone);
      

      List<String> annexIDs = getAllAnnexes(custodyID);
      annexIDs.add(annexID);
      writeObject(custodyID, getCustodyAnnexListName2(custodyID), annexIDs );
      
      final boolean ignoreErrors = false;
      updateAutomaticMetadatas(custodyID, custodyParameters, ignoreErrors);

    } catch (Exception ex) {
      final String msg = "No s'ha pogut guardar un annexe del document";
      log.error(msg, ex);
      throw new CustodyException(msg, ex);
    }

    return annexID;
  }
  
  
  @Override
  public boolean supportsDeleteAnnex() {
    return true;
  }
  
  
  @Override
  public void deleteAllAnnexes(String custodyID) throws CustodyException,
      NotSupportedCustodyException {
    
    List<String> annexIDs = getAllAnnexes(custodyID);
    
    ArrayList<String> paths = new ArrayList<String>();
    
    for (String annexID : annexIDs) {
      paths.add(getCustodyAnnexName2(custodyID, annexID));
      paths.add(getCustodyAnnexInfoName2(custodyID, annexID));
    }

    paths.add(getCustodyAnnexListName2(custodyID));
    
    deleteFile(custodyID, paths.toArray(new String[paths.size()]));

  }
  
  
  @Override
  public void deleteAnnex(String custodyID, String annexID) 
      throws CustodyException, NotSupportedCustodyException {
    deleteFile(custodyID, getCustodyAnnexName2(custodyID, annexID), getCustodyAnnexInfoName2(custodyID, annexID));

    List<String> annexIDs = getAllAnnexes(custodyID);
    annexIDs.remove(annexID);
    String listPath = getCustodyAnnexListName2(custodyID);
    writeObject(custodyID, listPath, annexIDs);

  }
  
  

  @Override
  public List<String> getAllAnnexes(String custodyID) throws CustodyException {
    
    String listPath = getCustodyAnnexListName2(custodyID);
  
    if (existsFile(custodyID, listPath)) {
      List<String> annexIDs = (List<String>)readObject(custodyID, listPath);
      return annexIDs; 
    } else {
      return new ArrayList<String>();
    }
   
  }


  /**
   * 
   * @param custodyID
   * @param annexID
   * @return null if annex not found
   */
  @Override
  public byte[] getAnnex(String custodyID, String annexID) throws CustodyException {
    AnnexCustody ac = getAnnexInfo(custodyID, annexID);
    if (ac == null) {
      return null;
    }
    return ac.getData();
  }


  /**
   * 
   * @param custodyID
   * @param annexID
   * @return null if annex not found
   */
  @Override
  public AnnexCustody getAnnexInfo(String custodyID, String annexID) throws CustodyException {

      if (custodyID == null || annexID == null) {
        return null;
      }

      String docPath = getCustodyAnnexName2(custodyID, annexID);
      String infoPath = getCustodyAnnexInfoName2(custodyID, annexID);
      return (AnnexCustody) getDocOrSign(custodyID, docPath, infoPath);

  }
  
  
  @Override
  public AnnexCustody getAnnexInfoOnly(String custodyID, String annexID)
      throws CustodyException {
    if (custodyID == null || annexID == null) {
      return null;
    }

    String infoPath = getCustodyAnnexInfoName2(custodyID, annexID);
    AnnexCustody annex = (AnnexCustody) getDocOrSignOnlyInfo(custodyID, infoPath);
    
     String annexPath = getCustodyAnnexName2(custodyID, annexID);;
    checkSize(custodyID, annex, infoPath, annexPath);
    
    return annex;
  }
  

  

  @Override
  public boolean supportsAnnexes() {
    return true;
  }
  

  
  // ---------------------------------------------------------------------
  // ---------------------------------------------------------------------
  // -------------------------- M E T A D A T A --------------------------
  // ---------------------------------------------------------------------
  // ---------------------------------------------------------------------


  @Override
  public boolean supportsMetadata() {
    return true;
  }
  
  @Override
  public boolean supportsDeleteMetadata() {
    return true;
  }
  
  
  /**
   * 
   * @param custodyID
   * @param custodyParameters
   * @return
   * @throws CustodyException
   */
  protected List<Metadata> updateAutomaticMetadatas(String custodyID,
      Map<String, Object> custodyParameters, boolean ignoreErrors) throws CustodyException {

    if (supportsMetadata()) {
      final String propertyBase = getPropertyBase();
    
      List<Metadata> list = recollectAutomaticMetadatas(this, custodyParameters, 
          propertyBase, ignoreErrors);
      
      if (list != null && list.size() != 0) {
        try {
          updateMetadata(custodyID, list.toArray(new Metadata[list.size()]), custodyParameters);
        } catch (Exception e) {
          final String msg = "Error guardant les metadades automàtiques: " + e.getMessage();
          log.error(msg, e);
          throw new CustodyException(msg, e);
        }
      }
      
      return list;
      
    } else {
      return null;
    }
  }

  /**
   * 
   * @param documentCustody
   * @param custodyParameters
   * @param propertyBase
   * @return
   * @throws CustodyException
   */
  public static List<Metadata> recollectAutomaticMetadatas(
      AbstractPluginProperties pluginProperties, Map<String, Object> custodyParameters,
      String propertyBase, boolean ignoreErrors) throws CustodyException {

    List<Metadata> list = null;
    String itemsStr = pluginProperties.getProperty(propertyBase + ABSTRACT_AUTOMATIC_METADATA_ITEMS);

    if (itemsStr != null && itemsStr.trim().length() != 0) {

      String[] items = itemsStr.split(",");

      list = new ArrayList<Metadata>(); 

      for (int i = 0; i < items.length; i++) {
        String item = items[i].trim();
        String metadata = propertyBase + ABSTRACT_AUTOMATIC_METADATA + "." + item + ".name";
        try {
          String name = pluginProperties.getPropertyRequired(metadata);

          metadata = propertyBase + ABSTRACT_AUTOMATIC_METADATA + "." + item + ".valueEL";

          String valueEL =  pluginProperties.getPropertyRequired(metadata);
          try {
            String value = processExpressionLanguage(valueEL, custodyParameters);  
            if (value != null) {
              list.add(new Metadata(name, value));
            }
          } catch (Exception e) {
            if (!ignoreErrors) {
              throw new CustodyException("AutmaticMetadatas::Error processant el valor de "
                  + valueEL + "(item = " + item + ")");
            }
          }
        } catch (CustodyException ce) {
          throw ce;
        } catch (Exception e) {
          if (!ignoreErrors) {
            final String msg = "Error intentant obtenir el nom de la metadada "
              + metadata + " o el seu valor: " + e.getMessage();
            throw new CustodyException(msg, e);
          }
        }
      }
    }
    return list;
  }
  
  
  @Override
  public void addMetadata(String custodyID, Metadata metadata, Map<String, Object> custodyParameters) throws CustodyException,  NotSupportedCustodyException, MetadataFormatException {
    
    if (metadata != null) {
      Metadata.checkMetadata(metadata);
      Map<String, List<Metadata>> metas = readMetadataInfo(custodyID);
      List<Metadata> list = metas.get(metadata.getKey());
      if (list == null) {
        list = new ArrayList<Metadata>();
        metas.put(metadata.getKey(), list);
      }
      list.add(metadata);
      // writeMetadataInfo(custodyID, metas, custodyParameters);
      final String path = getCustodyMetadataInfoName(custodyID, custodyParameters); // 
      writeObject(custodyID, path, metas);
    }
            
  }
  
  @Override
  public void addMetadata(String custodyID, Metadata[] metadata, Map<String, Object> custodyParameters) throws CustodyException,  NotSupportedCustodyException, MetadataFormatException {
    if (metadata != null) {
      // TODO Optimitzar
      for (int i = 0; i < metadata.length; i++) {
        if(metadata[i] != null) {
          Metadata.checkMetadata(metadata[i]);
        };
      }
      for (int i = 0; i < metadata.length; i++) {
        addMetadata(custodyID, metadata[i], custodyParameters);
      }
    }
  }
  
  

  @Override
  public void updateMetadata(String custodyID, Metadata metadata, Map<String, Object> custodyParameters) throws CustodyException,
      NotSupportedCustodyException, MetadataFormatException {
    
    if (metadata != null && metadata.getKey() != null) {
      deleteMetadata(custodyID, metadata.getKey() );
    }
    addMetadata(custodyID, metadata, custodyParameters);
  }

  @Override
  public void updateMetadata(String custodyID, Metadata[] metadata, Map<String, Object> custodyParameters) throws CustodyException,
      NotSupportedCustodyException, MetadataFormatException {
    if (metadata != null) {
      for (Metadata m : metadata) {
        updateMetadata(custodyID, m, custodyParameters);
      } 
    }
  }

  @Override
  public List<Metadata> deleteMetadata(String custodyID, String[] keys)
      throws CustodyException {
    // TODO OPTIMITZAR
    ArrayList<Metadata> borrades = new ArrayList<Metadata>();
    if (keys != null && keys.length != 0) {
      for (int i = 0; i < keys.length; i++) {
        borrades.addAll(deleteMetadata(custodyID, keys[i]));
      }
    }
    return borrades;
  }

  
  @Override  
  public Map<String, List<Metadata>> getAllMetadata(String custodyID) throws NotSupportedCustodyException, CustodyException {

    Map<String, List<Metadata>> all = readMetadataInfo(custodyID);

    return (Map<String, List<Metadata>>)all;
  }
  
  @Override  
  public List<Metadata> getMetadata(String custodyID, String key) throws CustodyException, NotSupportedCustodyException {
    return readMetadataInfo(custodyID).get(key);
  }
  
  @Override
  public Metadata getOnlyOneMetadata(String custodyID, String key) throws CustodyException,
      NotSupportedCustodyException {
    List<Metadata> list = getMetadata(custodyID, key);
    if (list == null || list.size() == 0) {
      return null;
    }
    return list.get(0);
  }
  
 
  @Override
  public void deleteAllMetadata(String custodyID) throws CustodyException {

    deleteFile(custodyID, getCustodyMetadataInfoName2(custodyID));

  }
  
  @Override
  public List<Metadata> deleteMetadata(String custodyID, String key) throws CustodyException {

    Map<String, List<Metadata>> metas = readMetadataInfo(custodyID);
    
    List<Metadata> borrades = metas.remove(key);
    
    // writeMetadataInfo(custodyID, metas);
    final String path = getCustodyMetadataInfoName2(custodyID); // , parameters
    writeObject(custodyID, path, metas);
    
    return borrades;
    
  }
  
  /**
   * 
   * @return
   */
  protected Map<String, List<Metadata>> readMetadataInfo(String custodyID) throws CustodyException {
    final String path = getCustodyMetadataInfoName2(custodyID);

    if (existsFile(custodyID, path)) {
      return (Map<String, List<Metadata>>)readObject(custodyID, path);
    } else {
      return new HashMap<String, List<Metadata>>();
    }
  }
  
 
  
  // ---------------------------------------------------------------------
  // ---------------------------------------------------------------------
  // ------------------------------- UTILS -------------------------------
  // ---------------------------------------------------------------------
  // ---------------------------------------------------------------------

  protected AnnexCustody getDocOrSign(String custodyID, String docPath, String infoPath)
      throws CustodyException {
    try {

      
      if (!existsFile(custodyID, docPath)) {
        return null;
      }
      
      AnnexCustody dc = getDocOrSignOnlyInfo(custodyID, infoPath);
      
      if (dc != null ) {
        checkSize(custodyID, dc, infoPath, docPath);
        byte[] data = readFile(custodyID, docPath);
        dc.setData(data);
      }

      return dc;

    } catch (Exception ex) {
      final String msg = "Error intentant obtenir el document custodia amb ID = "
        + custodyID + " en el path " + docPath + " (" + infoPath + ")";
      log.error(msg, ex);
      throw new CustodyException(msg, ex);
    }
  }

  
  
  
  protected void checkSize(String custodyID, AnnexCustody dc, String infoPath, String filePath) throws CustodyException {
    if (dc != null && dc.getLength() < 0) {
      try {
        long len = lengthFile(custodyID, filePath);
        dc.setLength(len);
        
        writeObject(custodyID, infoPath, dc);
      } catch (Exception ex) {
        final String msg = "No s'ha pogut actualitzar la informació de tamany del document " + filePath;
        log.error(msg, ex);
        throw new CustodyException(msg, ex);
      }
      
    }
  }
  
  
  
  protected AnnexCustody getDocOrSignOnlyInfo(String custodyID, String infoPath)
      throws CustodyException {
    
    AnnexCustody dc = (AnnexCustody) readObject(custodyID, infoPath);
    return dc;
    
  }
  
  
  
  protected void writeObject(String custodyID, String relativePath, Object object) throws CustodyException {
    try {
      java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
      XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(
          baos));
      encoder.writeObject(object);
      encoder.close();
      writeFile(custodyID, relativePath, baos.toByteArray());
    } catch (Exception e) {
      throw new CustodyException(e);
    } 
  }

  
  protected Object readObject(String custodyID, String relativePath) throws CustodyException {
    try {
      byte[] data = readFile(custodyID, relativePath);
      if (data == null) {
        return null;
      }
      
      // Replace old API to new API
      String dataStr = new String(data, "UTF8");
      
      dataStr = dataStr.replace("class=\"org.fundaciobit.plugins.documentcustody.AnnexCustody\"", "class=\"org.fundaciobit.plugins.documentcustody.api.AnnexCustody\"");
      dataStr = dataStr.replace("class=\"org.fundaciobit.plugins.documentcustody.DocumentCustody\"", "class=\"org.fundaciobit.plugins.documentcustody.api.DocumentCustody\"");
      dataStr = dataStr.replace("class=\"org.fundaciobit.plugins.documentcustody.SignatureCustody\"", "class=\"org.fundaciobit.plugins.documentcustody.api.SignatureCustody\"");

      
      XMLDecoder decoder = new XMLDecoder(new ByteArrayInputStream(dataStr.getBytes("UTF8")));
      Object o = (Object) decoder.readObject();
      decoder.close();
      return o;
    } catch (Exception e) {
      throw new CustodyException(e);
    }
  }
  
  // Implementar 
  protected abstract void deleteFile(String custodyID, String ... relativePaths);

  // Implementar
  protected abstract boolean existsFile(String custodyID, String relativePath);
  
  // Implementar
  protected abstract void writeFile(String custodyID, String relativePath, byte[] data) throws Exception;
  
  // Implementar
  protected abstract void writeFileCreateParentDir(String custodyID, String relativePath, byte[] data) throws Exception;
  
  // Implementar
  protected abstract byte[] readFile(String custodyID, String relativePath) throws Exception;
  
  // Implementar
  protected abstract String getPropertyBase();
  
  // implementar
  protected abstract long lengthFile(String custodyID, String relativePath) throws Exception;

}
