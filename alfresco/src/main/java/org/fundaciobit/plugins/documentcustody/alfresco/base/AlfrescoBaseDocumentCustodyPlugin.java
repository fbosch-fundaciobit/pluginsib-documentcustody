package org.fundaciobit.plugins.documentcustody.alfresco.base;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.fundaciobit.plugins.documentcustody.api.AbstractDocumentCustodyPlugin;
import org.fundaciobit.plugins.documentcustody.api.CustodyException;
import org.fundaciobit.plugins.documentcustody.api.NotSupportedCustodyException;
import org.fundaciobit.plugins.documentcustody.alfresco.base.cmis.OpenCmisAlfrescoHelper;

/**
 * Implementació del plugin de custodia documental que guarda dins Alfresco. Si
 * es defineix una URL base d'un servidor web, llavors es pot fer que retorni la
 * URL de validació.
 *
 * @author anadal
 */
public class AlfrescoBaseDocumentCustodyPlugin extends AbstractDocumentCustodyPlugin {

  protected OpenCmisAlfrescoHelper openCmisAlfrescoHelper = new OpenCmisAlfrescoHelper(this);

  /**
   *
   */
  public AlfrescoBaseDocumentCustodyPlugin() {
    super();
  }

  /**
   * @param propertyKeyBase
   * @param properties
   */
  public AlfrescoBaseDocumentCustodyPlugin(String propertyKeyBase, Properties properties) {
    super(propertyKeyBase, properties);
  }

  public static final String ALFRESCO_PROPERTY_BASE = DOCUMENTCUSTODY_BASE_PROPERTY
      + "alfresco.";

  public static final String ALFRESCO_BASE_PATH = ALFRESCO_PROPERTY_BASE + "basepath";

  // TODO XYZ ELIMINAR
  public OpenCmisAlfrescoHelper getAlfresco() {
    return openCmisAlfrescoHelper;
  }

  public String getAlfrescoUrl() {
    return getProperty(ALFRESCO_PROPERTY_BASE + "url");
  }

  public String getRepositoryID() {
    return getProperty(ALFRESCO_PROPERTY_BASE + "repository");
  }

  public String getSite() {
    return getProperty(ALFRESCO_PROPERTY_BASE + "site");
  }

  public String getFullSitePath() {
    return getProperty(ALFRESCO_PROPERTY_BASE + "fullsitepath");
  }

  public String getBasePath() {
    return getProperty(ALFRESCO_BASE_PATH, "");
  }

  public String getUsername() {
    return getProperty(ALFRESCO_PROPERTY_BASE + "access.user");
  }

  public String getPassword() {
    return getProperty(ALFRESCO_PROPERTY_BASE + "access.pass");
  }

  public String getAccessMethod() {
    return getProperty(ALFRESCO_PROPERTY_BASE + "access.method", "WS");
  }

  @Override
  protected void writeFile(String custodyID, String relativePath, byte[] data)
      throws Exception {

    try {

      if (existsFile(custodyID, relativePath)) {
        deleteFile(custodyID, relativePath);
      }

      int pos = relativePath.replace('\\', '/').lastIndexOf('/');

      String fileFinalName;
      String path;
      if (pos == -1) {
        fileFinalName = relativePath;
        path = "";
      } else {
        fileFinalName = relativePath.substring(pos + 1, relativePath.length()); // 0,pos
                                                                                // +
                                                                                // 1);
        path = relativePath.substring(0, pos);
      }

      if (log.isDebugEnabled()) {
        log.debug("FinalName = ]" + fileFinalName + "[");
        log.debug("PATH = ]" + path + "[");
      }

      Map<String, Object> properties = new HashMap<String, Object>();
      properties.put(PropertyIds.OBJECT_TYPE_ID, OpenCmisAlfrescoHelper.CMIS_DOCUMENT_TYPE);
      properties.put(PropertyIds.DESCRIPTION, custodyID);
      properties.put(PropertyIds.NAME, fileFinalName);

      // TODO: Treurer els parametres que es vulguin guardar dins properties del
      // document i passarlos

      openCmisAlfrescoHelper.crearDocument(data, fileFinalName, path, properties, custodyID);

      if (log.isDebugEnabled()) {
        log.debug("Pujat Document a Alfresco: " + relativePath);
      }

    } catch (Exception ex) {
      final String msg = "No s'ha pogut guardar el document amb id=" + custodyID;
      log.error(msg, ex);
    }

  }

  @Override
  protected byte[] readFile(String custodyID, String relativePath) throws Exception {
    final boolean debug = log.isDebugEnabled();

    if (debug) {
      log.debug("readFile():: Llegint Document " + relativePath);
    }

    try {
      Document document = openCmisAlfrescoHelper.getDocument(relativePath, custodyID);

      return OpenCmisAlfrescoHelper.getCmisObjectContent(document);
    } catch (CmisObjectNotFoundException onfe) {
      if (log.isDebugEnabled()) {
        log.debug("El document " + relativePath + "/" + custodyID + " no existeix.");
      }
      return null;
    }

  }

  @Override
  protected String getPropertyBase() {
    return ALFRESCO_PROPERTY_BASE;
  }

  @Override
  protected void deleteFile(String custodyID, String... relativePaths) {

    for (String filePath : relativePaths) {
      try {
        openCmisAlfrescoHelper.borrarDocument(filePath, custodyID);
      } catch (Throwable th) {
        log.warn("No ha pogut borrar el fitxer = " + filePath, th);
      }
    }

  }

  @Override
  protected boolean existsFile(String custodyID, String relativePath) {
    try {
      Document document = openCmisAlfrescoHelper.getDocument(relativePath, custodyID);
      if (document == null) {
        return false;
      } else {
        return true;
      }
    } catch (Throwable th) {
      log.debug("No pot determinar si existeix fitxer en el següent path = " + relativePath,
          th);
      return false;
    }

  }

  @Override
  public void deleteCustody(String custodyID) throws CustodyException,
      NotSupportedCustodyException {

    String realPath = CUSTODY_PREFIX_AND_FOLDER(custodyID);

    super.deleteCustody(custodyID);

    // (1) Esborrar carpeta de documents
    final boolean debug = log.isDebugEnabled();

    if (debug) {
      log.debug("deleteCustody(): esborrant carpeta de custodia " + realPath);
    }
    try {
      openCmisAlfrescoHelper.borrarCarpeta(realPath + custodyID, null);
    } catch (Exception e) {
      log.warn("Error desconegur esborrant carpeta de custodia " + realPath + "(custodyID="
          + custodyID + ")");
    }

    // (2) Esborrar carpeta de _cachefolder
    String folder = getProperty(getPropertyBase() + ABSTRACT_FOLDER_EXPRESSION_LANGUAGE);

    if (folder == null || folder.trim().length() == 0) {
      // No fe res
    } else {
      String path = getCachePath(custodyID);
      if (debug) {
        log.debug("deleteCustody(): esborrant carpeta de de cache " + path);
      }

      try {
        openCmisAlfrescoHelper.borrarCarpeta(path, null);
      } catch (Exception e) {
        log.warn("Error desconegur esborrant carpeta de cache " + path + "(custodyID="
            + custodyID + ")");
      }
    }

  }


  public boolean checkPrefix = false;

  @Override
  public String reserveCustodyID(Map<String, Object> custodyParameters)
      throws CustodyException {

    // DO NOT SUPPORT PREFIX
    if (!checkPrefix) {
      String prefix = CUSTODY_PREFIX();
      if (prefix != null && prefix.trim().length() != 0) {
        throw new CustodyException("Plugin d'Alfresco no suporta la propietat 'prefix'.");
      }
    }

    String custodyId = super.reserveCustodyID(custodyParameters);

    return custodyId;
  }

  @Override
  public String generateUniqueCustodyID(Map<String, Object> custodyParameters) throws CustodyException {
    String custodyID = super.generateUniqueCustodyID(custodyParameters);

    // CARPETA BASE
    String folder = getProperty(getPropertyBase() + ABSTRACT_FOLDER_EXPRESSION_LANGUAGE);
    if (folder == null || folder.trim().length() == 0) {

       try {
        //  Està bé que passem custodyID com a Stirng buit
         openCmisAlfrescoHelper.crearCarpeta(custodyID, null, "");
       } catch(Exception e) {
         throw new CustodyException(e.getMessage(), e);
       }
    }

    return custodyID;
  }




  /**
   * Per defecte val ".cachefolder" però a Alfresco el "." pareix que no li
   * agrada. Ho canviam per "_cachefolder"
   */
  @Override
  public String getCacheFolderName() {
    return "_cachefolder";
  }

  /*
   * @Override public void saveDocument(String custodyID, String
   * custodyParameters, DocumentCustody document) throws CustodyException,
   * NotSupportedCustodyException {
   *
   * try {
   *
   * String fileFinalame =
   * AlfrescoUtils.getFileNameWithCustodyId(document.getName(), custodyID,
   * false);
   *
   * Map<String, Object> properties = new HashMap<String, Object>();
   * properties.put(PropertyIds.OBJECT_TYPE_ID,
   * OpenCmisAlfrescoHelper.CMIS_DOCUMENT_TYPE);
   * properties.put(PropertyIds.DESCRIPTION, custodyID+"D");
   * properties.put(PropertyIds.NAME, fileFinalame);
   *
   * //TODO: Treurer els parametres que es vulguin guardar dins properties del
   * document i passarlos
   *
   * String docPath = getPathFromCustodyParameters(custodyParameters);
   *
   * OpenCmisAlfrescoHelper.crearDocument(getCmisSession(), getSite(), document,
   * fileFinalame, docPath, properties);
   *
   * log.debug("Pujat Document a Alfresco: "+docPath+"/"+fileFinalame);
   *
   * } catch (Exception ex) { final String msg =
   * "No s'ha pogut guardar el document amb id="+custodyID; log.error(msg, ex);
   * } }
   *
   *
   * @Override public DocumentCustody getDocumentInfo(String custodyID) throws
   * CustodyException {
   *
   * List<Document> docs =
   * OpenCmisAlfrescoHelper.getDocumentById(getCmisSession(), custodyID+"D");
   *
   * if (docs!=null) {
   *
   * if (docs.size()==1) {
   *
   * try {
   *
   * Document alfDoc = docs.get(0);
   *
   * DocumentCustody cusDoc = new DocumentCustody();
   * cusDoc.setData(AlfrescoUtils.getCmisObjectContent(alfDoc)); String nomArxiu
   * = AlfrescoUtils.removeCustodyIdFromFilename(alfDoc.getName(), false);
   * cusDoc.setName(nomArxiu);
   * cusDoc.setMime(alfDoc.getContentStreamMimeType());
   *
   * return cusDoc;
   *
   * }catch (Exception ex) { final String msg =
   * "Error al recuperar el contingut del document amb custodyID: "+custodyID;
   * log.error(msg, ex); throw new CustodyException(msg, ex); }
   *
   * }else{ final String msg =
   * "S´ha trobat mes de un document amb el mateix custodyID! ("+custodyID+")";
   * log.error(msg); throw new CustodyException(msg); } }else{ final String msg
   * = "No s´ha trobat cap document amb custodyID: "+custodyID; log.error(msg);
   * throw new CustodyException(msg); } }
   *
   *
   * @Override public void deleteDocument(String custodyID) throws
   * CustodyException, NotSupportedCustodyException {
   *
   * List<Document> docs =
   * OpenCmisAlfrescoHelper.getDocumentById(getCmisSession(), custodyID+"D");
   *
   * if (docs!=null) {
   *
   * if (docs.size()==1) {
   *
   * try {
   *
   * docs.get(0).delete();
   *
   * }catch (Exception ex) { final String msg =
   * "Error al borrar el document amb custodyID: "+custodyID; log.error(msg,
   * ex); throw new CustodyException(msg, ex); }
   *
   * }else{ final String msg =
   * "S´ha trobat mes de un document amb el mateix custodyID! ("+custodyID+")";
   * log.error(msg); throw new CustodyException(msg); } }else{ final String msg
   * = "No s´ha trobat cap document amb custodyID: "+custodyID; log.error(msg);
   * throw new CustodyException(msg); } }
   *
   * @Override public void saveSignature(String custodyID, String
   * custodyParameters, SignatureCustody document) throws CustodyException {
   *
   * try {
   *
   * String fileFinalame =
   * AlfrescoUtils.getFileNameWithCustodyId(document.getName(), custodyID,
   * true);
   *
   * Map<String, Object> properties = new HashMap<String, Object>();
   * properties.put(PropertyIds.OBJECT_TYPE_ID,
   * OpenCmisAlfrescoHelper.CMIS_DOCUMENT_TYPE);
   * properties.put(PropertyIds.DESCRIPTION, custodyID+"S");
   * properties.put(PropertyIds.NAME, fileFinalame);
   *
   * String docPath = getPathFromCustodyParameters(custodyParameters);
   *
   * OpenCmisAlfrescoHelper.crearDocument(getCmisSession(), getSite(), document,
   * fileFinalame, docPath, properties);
   *
   * log.debug("Pujada firma a Alfresco: "+docPath+"/"+fileFinalame);
   *
   * } catch (Exception ex) { final String msg =
   * "No s'ha pogut guardar el document amb id="+custodyID; log.error(msg, ex);
   * } }
   *
   * @Override public SignatureCustody getSignatureInfo(String custodyID) throws
   * CustodyException {
   *
   * List<Document> docs =
   * OpenCmisAlfrescoHelper.getDocumentById(getCmisSession(), custodyID+"S");
   *
   * if (docs!=null) {
   *
   * if (docs.size()==1) {
   *
   * try {
   *
   * Document alfDoc = docs.get(0);
   *
   * SignatureCustody regSig = new SignatureCustody();
   * regSig.setData(AlfrescoUtils.getCmisObjectContent(alfDoc)); String nomArxiu
   * = AlfrescoUtils.removeCustodyIdFromFilename(alfDoc.getName(), true);
   * regSig.setName(nomArxiu);
   * regSig.setMime(alfDoc.getContentStreamMimeType());
   *
   * return regSig;
   *
   * }catch (Exception ex) { final String msg =
   * "Error al recuperar el contingut de la firma amb custodyID: "+custodyID;
   * log.error(msg, ex); throw new CustodyException(msg, ex); } }else{ final
   * String msg =
   * "S´ha trobat mes de una firma amb el mateix custodyID! ("+custodyID+")";
   * log.error(msg); throw new CustodyException(msg); } }else{ final String msg
   * = "No s´ha trobat cap firma amb custodyID: "+custodyID; log.error(msg);
   * throw new CustodyException(msg); } }
   *
   * @Override public void deleteSignature(String custodyID) throws
   * CustodyException, NotSupportedCustodyException {
   *
   * List<Document> docs =
   * OpenCmisAlfrescoHelper.getDocumentById(getCmisSession(), custodyID+"S");
   *
   * if (docs!=null) {
   *
   * if (docs.size()==1) {
   *
   * try {
   *
   * docs.get(0).delete();
   *
   * }catch (Exception ex) { final String msg =
   * "Error al borrar la firma amb custodyID: "+custodyID; log.error(msg, ex);
   * throw new CustodyException(msg, ex); } }else{ final String msg =
   * "S´ha trobat mes de una firma amb el mateix custodyID! ("+custodyID+")";
   * log.error(msg); throw new CustodyException(msg); } }else{ final String msg
   * = "No s´ha trobat cap firma amb custodyID: "+custodyID; log.error(msg);
   * throw new CustodyException(msg); } }
   */

  // =================================================

  @Override
  protected void writeFileCreateParentDir(String custodyID, String relativePath, byte[] data)
      throws Exception {

    final boolean debug = log.isDebugEnabled();

    if (debug) {
      log.debug("writeFileCreateParentDir()::relativePath = " + relativePath);
    }
    writeFile(custodyID, relativePath, data);

  }

  @Override
  protected long lengthFile(String custodyID, String relativePath) throws Exception {
    try {
      Document document = openCmisAlfrescoHelper.getDocument(relativePath, custodyID);
      if (document == null) {
        return -1;
      } else {
        return document.getContentStreamLength();
      }
    } catch (Throwable th) {
      String msg = "No pot determinar si existeix fitxer en el següent path = " + relativePath
          + ": " + th.getMessage();
      log.error(msg, th);
      throw new Exception(msg, th);
    }
  }

}
