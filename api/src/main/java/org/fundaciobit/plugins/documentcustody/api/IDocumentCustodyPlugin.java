package org.fundaciobit.plugins.documentcustody.api;

import java.util.List;
import java.util.Map;

import org.fundaciobit.plugins.IPlugin;
import org.fundaciobit.plugins.utils.Metadata;
import org.fundaciobit.plugins.utils.MetadataFormatException;

/**
 * 
 * @author anadal
 * 
 */
public interface IDocumentCustodyPlugin extends IPlugin {
  
  public static final String DOCUMENTCUSTODY_BASE_PROPERTY = IPLUGIN_BASE_PROPERTIES + "documentcustody.";

  /**
   * =========================================================================
   * ================ M E T O  D E S    G E N E  R A L S =====================
   * =========================================================================
   */

  
  /**
   * 
   * @param proposedID Identificador proposat per la reserva
   * @param custodyParameters Parametres addicionals requerits per a la realització de la Custodia. 
   * @return 
   * @throws Exception
   */
  String reserveCustodyID(Map<String, Object> parameters) throws CustodyException;
  
  /**
  *
  * @param custodyID
  * @return
  * @throws Exception
  */
 String getValidationUrl(String custodyID, Map<String, Object> parameters) throws CustodyException;
 
 /**
  * Retorna un valor a partir de l'identificador de reserva que es pot
  * substituir quan calgui. Per defecte retornar custodyID 
  * @param custodyID
  * @return
  * @throws Exception
  */
  String getSpecialValue(String custodyID, Map<String, Object> parameters) throws CustodyException;
  
  /**
   * 
   * @param custodyID
   * @throws Exception
   */
  void deleteCustody(String custodyID) throws CustodyException,  NotSupportedCustodyException;


  /**
   * Indica si podem eliminar una Custodia completa
   * @return
   */
  boolean supportsDeleteCustody();
  
  
  
  /**
   * Custodia un document
   * @param custodyID
   * @param document
   * @throws Exception
   */
  void saveAll(String custodyID, Map<String, Object> parameters,
      DocumentCustody document,  SignatureCustody signatureCustody,
      Metadata[] metadata) throws CustodyException,  NotSupportedCustodyException, MetadataFormatException;
  
  
  

  /**
   * =========================================================================
   * ========================= D O C U M E  N T  =============================
   * =========================================================================
   */
  
  /**
   * Custodia un document
   * @param custodyID
   * @param document
   * @throws Exception
   */
  @Deprecated
  void saveDocument(String custodyID, Map<String, Object> parameters,
      DocumentCustody document) throws CustodyException, NotSupportedCustodyException;
  
  /**
   * 
   * @param custodyID
   * @param custodyParameters
   * @param document
   * @throws CustodyException
   * @throws NotSupportedCustodyException
   */
  @Deprecated
  void deleteDocument(String custodyID) throws CustodyException, NotSupportedCustodyException;
  
  /**
   * @param custodyID
   * @return Return content of document for plain document (without attached 
   * or detached signatures) or document with attached signatures
   * @throws Exception
   */
  byte[] getDocument(String custodyID) throws CustodyException;
  
  /**
   * @param custodyID
   * @return Return content of document for plain document (without attached 
   * or detached signatures) or document with attached signatures
   * @throws Exception
   */
  DocumentCustody getDocumentInfo(String custodyID) throws CustodyException;
  
  
  /**
   * @param custodyID
   * @return Return info without content of file
   * @throws Exception
   */
  DocumentCustody getDocumentInfoOnly(String custodyID) throws CustodyException;
  
  
  
  boolean supportsDeleteDocument();
  

  /**
   * =========================================================================
   * ========================= S I G N A T U R A  ===========================
   * =========================================================================
   */


  /**
   * 
   */
  @Deprecated
  void saveSignature(String custodyID, Map<String, Object> parameters,
      SignatureCustody signatureCustody) throws CustodyException, NotSupportedCustodyException;



  /**
   * Return detached sign if the document has it. For plain document (without attached 
   * or detached signatures) or document with attached signatures then return null 
   * @param custodyID
   * @return
   * @throws Exception
   */
  byte[] getSignature(String custodyID) throws CustodyException;
  
  /**
   * Return detached sign if the document has it. For plain document (without attached 
   * or detached signatures) or document with attached signatures then return null 
   * @param custodyID
   * @return null if does not exist signature.
   * @throws Exception
   */
  SignatureCustody getSignatureInfo(String custodyID) throws CustodyException;
  
  
  /**
   * @param custodyID
   * @return Return info without content of file
   * @throws Exception
   */
  SignatureCustody getSignatureInfoOnly(String custodyID) throws CustodyException;
  
  @Deprecated
  void deleteSignature(String custodyID) throws CustodyException, NotSupportedCustodyException;
  
  boolean supportsDeleteSignature();

  
  /** 
   * @return A list of suported signature types defined in SignatureCustody class 
   */
  String[] getSupportedSignatureTypes();


  /** 
   * @return true if system automaically refresh signature or document with signature 
   *  to not loss validate of signature. false Otherwise. Null if unknown.
   */
  Boolean supportsAutomaticRefreshSignature();
  

  /**
   * =========================================================================
   * ============================= A N N E X E S =============================
   * =========================================================================
   */

  /**
   * 
   * @param custodyID
   * @param annex
   * @return AnnexID
   * @throws CustodyException
   */
  String addAnnex(String custodyID, AnnexCustody annex, Map<String, Object> parameters) throws CustodyException,  NotSupportedCustodyException;
  
  /**
   * 
   * @param custodyID
   * @param annexID
   * @throws CustodyException
   * @throws NotSupportedCustodyException
   */
  void deleteAnnex(String custodyID, String annexID) throws CustodyException, NotSupportedCustodyException;
  
  
  /**
   * 
   * @param custodyID
   * @param annexID
   * @throws CustodyException
   * @throws NotSupportedCustodyException
   */
  void deleteAllAnnexes(String custodyID) throws CustodyException, NotSupportedCustodyException;
  
  
  
  /**
   * 
   * @param custodyID
   * @param annexID
   * @return null if annex not found
   */
  byte[] getAnnex(String custodyID, String annexID) throws CustodyException ;
  
  /**
   * 
   * @param custodyID
   * @param annexID
   * @return null if annex not found
   */
  AnnexCustody getAnnexInfo(String custodyID, String annexID) throws CustodyException ;
  
  
  /**
   * 
   * @param custodyID
   * @param annexID
   * @return null if annex not found
   */
  AnnexCustody getAnnexInfoOnly(String custodyID, String annexID) throws CustodyException ;
  
  /**
   * 
   * @param custodyID
   * @return
   * @throws CustodyException
   */
  List<String> getAllAnnexes(String custodyID) throws CustodyException;

  
  boolean supportsAnnexes();
  
  boolean supportsDeleteAnnex();
  
  /**
   * =========================================================================
   * =========================== M E T A D A D E S ===========================
   * =========================================================================
   */
  

  /**
   * 
   * @param custodyID
   * @param metadata
   * @throws CustodyException
   * @throws NotSupportedCustodyException
   */ 
  void addMetadata(String custodyID, Metadata metadata, Map<String, Object> parameters) throws CustodyException,  NotSupportedCustodyException, MetadataFormatException;

  /**
   * Afegeix noves Metadades al sistema. Si les claus d'aquestes ja exeisteixen llavors s'afegeix un nou valor a la clau. 
   * @param custodyID
   * @param metadata
   * @throws CustodyException
   * @throws NotSupportedCustodyException
   */
  void addMetadata(String custodyID, Metadata[] metadata, Map<String, Object> parameters) throws CustodyException,  NotSupportedCustodyException, MetadataFormatException;
  
  /**
   * Afegeix o actualitza una Metadada al sistema. Si no existeix l'afegeix.
   * Si existeix i nomes hi ha una instància llavors sobreescriu el valor. 
   * 
   * En resum si existeix la metadada (tengui o no tengui varis valors) l'esborra 
   * i afegeix les metadades
   * 
   * @param custodyID
   * @param metadata
   * @throws CustodyException
   * @throws NotSupportedCustodyException
   */
  void updateMetadata(String custodyID, Metadata metadata, Map<String, Object> parameters) throws CustodyException,  NotSupportedCustodyException, MetadataFormatException;
  
  /**
   * Afegeix o actualitza noves Metadades al sistema. Si no existeixen les afegeix.
   * Si existeixen (tengui la metadada un valor o tengui varis valors) les esborra 
   * i afegeix les metadades
   * 
   * @param custodyID
   * @param metadata
   * @throws CustodyException
   * @throws NotSupportedCustodyException
   */
  void updateMetadata(String custodyID, Metadata[] metadata,Map<String, Object> parameters) throws CustodyException,  NotSupportedCustodyException, MetadataFormatException;
  
  /**
   * 
   * @param custodyID
   * @return
   * @throws NotSupportedCustodyException
   */
  Map<String, List<Metadata>> getAllMetadata(String custodyID) throws  CustodyException,NotSupportedCustodyException;
  
  /**
   * 
   * @param custodyID
   * @return
   * @throws NotSupportedCustodyException
   */
  List<Metadata> getMetadata(String custodyID, String key) throws CustodyException, NotSupportedCustodyException;
  
  /**
   * 
   * @param custodyID
   * @param key
   * @return
   * @throws CustodyException
   * @throws NotSupportedCustodyException
   */
  Metadata getOnlyOneMetadata(String custodyID, String key) throws CustodyException, NotSupportedCustodyException;

  /**
   * 
   * @return
   */
  boolean supportsMetadata();

  /**
   * 
   * @param custodyID
   * @throws CustodyException
   */
  void deleteAllMetadata(String custodyID) throws CustodyException;

  /**
   * 
   * @param custodyID
   * @param key
   * @return
   * @throws CustodyException
   */
  List<Metadata> deleteMetadata(String custodyID, String key) throws CustodyException;


  /**
   * 
   * @param custodyID
   * @param keys
   * @return
   * @throws CustodyException
   */
  List<Metadata> deleteMetadata(String custodyID, String[] keys) throws CustodyException;
  
  
  boolean supportsDeleteMetadata();

}
