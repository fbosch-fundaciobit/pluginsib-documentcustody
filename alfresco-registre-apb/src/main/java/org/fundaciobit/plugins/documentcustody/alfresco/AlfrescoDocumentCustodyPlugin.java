package org.fundaciobit.plugins.documentcustody.alfresco;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.alfresco.cmis.client.AlfrescoDocument;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.bindings.spi.webservices.CXFPortProvider;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.fundaciobit.plugins.documentcustody.api.AbstractDocumentCustodyPlugin;
import org.fundaciobit.plugins.documentcustody.api.CustodyException;
import org.fundaciobit.plugins.documentcustody.api.DocumentCustody;
import org.fundaciobit.plugins.documentcustody.api.NotSupportedCustodyException;
import org.fundaciobit.plugins.documentcustody.api.SignatureCustody;
import org.fundaciobit.plugins.documentcustody.alfresco.cmis.OpenCmisAlfrescoHelper;
import org.fundaciobit.plugins.documentcustody.alfresco.util.AlfrescoUtils;
import org.fundaciobit.plugins.utils.Metadata;
import org.fundaciobit.plugins.utils.MetadataFormatException;
import org.fundaciobit.plugins.utils.MetadataType;

/**
 * Implementació del plugin de custodia documental que guarda dins Alfresco.
 *  Si es defineix una URL base d'un servidor web, llavors es pot fer
 * que retorni la URL de validació.
 *
 *  @author Limit
 *  @author anadal (Adapta a DocumentCustody 2.0.0 i 3.0.0)
 */
public class AlfrescoDocumentCustodyPlugin extends AbstractDocumentCustodyPlugin {

	private Document a_documento;
	private Document a_firma;

  /**
   * 
   */
  public AlfrescoDocumentCustodyPlugin() {
    super();
  }
  
  /**
   * @param propertyKeyBase
   * @param properties
   */
  public AlfrescoDocumentCustodyPlugin(String propertyKeyBase) {
    super(propertyKeyBase);
  }

  

  /**
   * @param propertyKeyBase
   * @param properties
   */
  public AlfrescoDocumentCustodyPlugin(String propertyKeyBase, Properties properties) {
    super(propertyKeyBase, properties);
  }


  public static final String ALFRESCO_PROPERTY_BASE = DOCUMENTCUSTODY_BASE_PROPERTY + "alfresco-apb.";
  
  
  private String getUrlAlfresco() {
    return getProperty(ALFRESCO_PROPERTY_BASE + "url");
  }
  
  private String getUsernameAlfresco() {
	  return getProperty(ALFRESCO_PROPERTY_BASE + "access.user");
  }

  private String getPasswordAlfresco() {
	  return getProperty(ALFRESCO_PROPERTY_BASE + "access.pass");
  }
  
  private String getAccessMethodAlfresco() {
	  return getProperty(ALFRESCO_PROPERTY_BASE + "access.method");
  }
  
  private String getRepositoryAlfresco() {
	  return getProperty(ALFRESCO_PROPERTY_BASE + "repository");
  }
  
  private String getPathCarpetaDocuments() throws Exception {

    String site = getProperty(ALFRESCO_PROPERTY_BASE + "site");
    
    if (site != null) {
      return "/Sites/"+site+"/documentLibrary";
    } else {
      String fullSitePath = getProperty(ALFRESCO_PROPERTY_BASE + "fullsitepath");
      if (fullSitePath != null) {
        return fullSitePath;
      }
      String p1 = getPropertyName(ALFRESCO_PROPERTY_BASE + "site");
      String p2 = getPropertyName(ALFRESCO_PROPERTY_BASE + "fullsitepath");
      String msg = "Ha de definir una de les següent propietats: " + p1 + " o " + p2;
      log.error(msg, new Exception());
      throw new Exception(msg);
    }

  }
  
  // =================================================
  
  private Session cmisSession;
  
	public Session getCmisSession() {
		
		if (cmisSession == null) {

//			if (cmisSession!=null) {
//				cmisSession.clear();
//				cmisSession = null;
//			}
			
			SessionFactory factory = SessionFactoryImpl.newInstance();
			Map<String, String> parameter = new HashMap<String, String>();
			String am = getAccessMethodAlfresco();

			if ("ATOM".equals(am)) {
				
				parameter.put(SessionParameter.ATOMPUB_URL, getUrlAlfresco());
				parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
				parameter.put(SessionParameter.AUTH_HTTP_BASIC, "true");
				parameter.put(SessionParameter.USER, getUsernameAlfresco());
				parameter.put(SessionParameter.PASSWORD, getPasswordAlfresco());
				parameter.put(SessionParameter.OBJECT_FACTORY_CLASS, "org.alfresco.cmis.client.impl.AlfrescoObjectFactoryImpl");

				List<Repository> repositories = factory.getRepositories(parameter);
				cmisSession = repositories.get(0).createSession();
				
			}else if ("WS".equals(am)) {
				
				parameter.put(SessionParameter.USER, getUsernameAlfresco());
				parameter.put(SessionParameter.PASSWORD, getPasswordAlfresco());
				parameter.put(SessionParameter.BINDING_TYPE, BindingType.WEBSERVICES.value());
				parameter.put(SessionParameter.REPOSITORY_ID, getRepositoryAlfresco());
				parameter.put(SessionParameter.WEBSERVICES_PORT_PROVIDER_CLASS, CXFPortProvider.class.getName());
				parameter.put(SessionParameter.OBJECT_FACTORY_CLASS, "org.alfresco.cmis.client.impl.AlfrescoObjectFactoryImpl");
				//parameter.put(SessionParameter.WEBSERVICES_JAXWS_IMPL, "sunjre");
				parameter.put(SessionParameter.WEBSERVICES_ACL_SERVICE, getUrlAlfresco()+"/ACLService?WSDL");
				parameter.put(SessionParameter.WEBSERVICES_DISCOVERY_SERVICE, getUrlAlfresco()+"/DiscoveryService?WSDL");
				parameter.put(SessionParameter.WEBSERVICES_MULTIFILING_SERVICE, getUrlAlfresco()+"/MultiFilingService?WSDL");
				parameter.put(SessionParameter.WEBSERVICES_NAVIGATION_SERVICE, getUrlAlfresco()+"/NavigationService?WSDL");
				parameter.put(SessionParameter.WEBSERVICES_OBJECT_SERVICE, getUrlAlfresco()+"/ObjectService?WSDL");
				parameter.put(SessionParameter.WEBSERVICES_POLICY_SERVICE, getUrlAlfresco()+"/PolicyService?WSDL");
				parameter.put(SessionParameter.WEBSERVICES_RELATIONSHIP_SERVICE, getUrlAlfresco()+"/RelationshipService?WSDL");
				parameter.put(SessionParameter.WEBSERVICES_REPOSITORY_SERVICE, getUrlAlfresco()+"/RepositoryService?WSDL");
				parameter.put(SessionParameter.WEBSERVICES_VERSIONING_SERVICE, getUrlAlfresco()+"/VersioningService?WSDL");
				parameter.put(SessionParameter.LOCALE_ISO3166_COUNTRY, "ES");
				parameter.put(SessionParameter.LOCALE_ISO639_LANGUAGE, "es");
				parameter.put(SessionParameter.LOCALE_VARIANT, "");
				
				/*List<Repository> repositories = factory.getRepositories(parameter);
				for (Repository r : repositories) {
				    System.out.println("Found repository: " + r.getName() + " (" + r.getId() + ")");
				}*/
				
				cmisSession = factory.createSession(parameter);
			}

		}

		return cmisSession;
	}

  @Override
  protected void deleteFile(String custodyID, String... relativePaths) {
    // TODO Auto-generated method stub
    
  }

  @Override
  protected boolean existsFile(String custodyID, String relativePath) {
	  String docName = OpenCmisAlfrescoHelper.getDocumentNameById(getCmisSession(), custodyID);
	  if (docName!=null && !"".equals(docName)) {
		  return true;
	  }else{
		  return false;
	  }
  }

  @Override
  public void saveDocument(String custodyID, Map<String, Object> custodyParameters,
      DocumentCustody document) throws CustodyException, NotSupportedCustodyException {
	
		  try {
			
			  	//System.out.println(custodyParameters);
			  
			    //Posam com a sufixe del document a pujar el custodyID per si es pujen varis docs amb el mateix nom
				String fileFinalame = AlfrescoUtils.getFileNameWithCustodyId(document.getName(), custodyID, false);
				
		    	Map<String, Object> fileProperties = new HashMap<String, Object>();
				fileProperties.put(PropertyIds.OBJECT_TYPE_ID, OpenCmisAlfrescoHelper.CMIS_DOCUMENT_TYPE);
				fileProperties.put("APBRegistro:dr_custodyID", custodyID);
				fileProperties.put("APBRegistro:dr_tipoArchivo", "D");
				fileProperties.put(PropertyIds.NAME, fileFinalame);
				
		    	Map<String, Object> folderProperties = new HashMap<String, Object>();
		    	folderProperties.put(PropertyIds.OBJECT_TYPE_ID, OpenCmisAlfrescoHelper.CMIS_FOLDER_TYPE);
			  
				List<Object> registreDecod = AlfrescoUtils.decodificaStringToObject(custodyParameters);
	
				//Retorna la ruta a on s´ha de guarda el document segons especificacions del client
				String docPath = AlfrescoUtils.getPathFromRegistreObject(registreDecod.get(0));
				
				//System.out.println(" * Determinat el path del document alfresco a: " + docPath);
				
				AlfrescoUtils.getPropertiesFromRegistreObject(registreDecod, fileProperties, custodyID);
				fileProperties.put("APBRegistro:dr_formato", document.getMime());
				AlfrescoUtils.getFolderPropertiesFromRegistreObject(registreDecod, folderProperties);

				//Si el document no existeix, el cream. En altre cas, borram i tornam a pujar
				String jaExisteix = OpenCmisAlfrescoHelper.getDocumentNameById(getCmisSession(), custodyID);
				
				if (jaExisteix!=null && !"".equals(jaExisteix)) {
					Document aBorrar = OpenCmisAlfrescoHelper.getDocumentById(getCmisSession(), custodyID);
					aBorrar.delete();
				}
				
				this.a_documento = OpenCmisAlfrescoHelper.crearDocument(getCmisSession(), getPathCarpetaDocuments(), document, fileFinalame, docPath, fileProperties, folderProperties);
				
		    	log.debug("Pujat Document a Alfresco: "+docPath+"/"+fileFinalame);
	
		  	}catch (CmisPermissionDeniedException pdEx) {
				final String msg = "No s'ha pogut guardar el document amb id "+custodyID+". "+pdEx.getMessage();
				throw new CustodyException(msg);
		    } catch (Exception ex) {
				final String msg = "No s'ha pogut guardar el document amb id "+custodyID+". "+ex.getMessage();
				log.error(msg, ex);
				throw new CustodyException(msg);
		    }
  }

  @Override
  public DocumentCustody getDocumentInfo(String custodyID) throws CustodyException {
	  
	  Document doc = OpenCmisAlfrescoHelper.getDocumentById(getCmisSession(), custodyID);
	  
	  if (doc!=null) {
			  
		  try {
			  
			  DocumentCustody cusDoc = new DocumentCustody();
			  cusDoc.setData(AlfrescoUtils.getCmisObjectContent(doc));
			  String nomArxiu = AlfrescoUtils.removeCustodyIdFromFilename(doc.getName(), false);
			  cusDoc.setName(nomArxiu);
			  cusDoc.setMime(doc.getContentStreamMimeType());
			  
			  return cusDoc;
		  }catch (CmisPermissionDeniedException pdEx) {
			final String msg = "No s'ha pogut contingut el document amb id "+custodyID+". "+pdEx.getMessage();
			throw new CustodyException(msg);
		  }catch (Exception ex) {
		      final String msg = "Error al recuperar el contingut del document amb custodyID: "+custodyID;
		      log.error(msg, ex);
		      throw new CustodyException(msg, ex);
		  }

	  }else{
	      final String msg = "No s´ha trobat cap document amb custodyID: "+custodyID;
	      log.error(msg);
	      throw new CustodyException(msg);
	  }
  }
  
  @Override
  public void deleteDocument(String custodyID) throws CustodyException, NotSupportedCustodyException {
	  
	  Document doc = OpenCmisAlfrescoHelper.getDocumentById(getCmisSession(), custodyID);
	  
	  if (doc!=null) {

		  try {
			  
			  doc.delete();
			  
			  this.a_documento = null;
			  
		  }catch (CmisPermissionDeniedException pdEx) {
				final String msg = "No s'ha pogut borrar el document amb id "+custodyID+". "+pdEx.getMessage();
				throw new CustodyException(msg);
		  }catch (Exception ex) {
		      final String msg = "Error al borrar el document amb custodyID: "+custodyID;
		      log.error(msg, ex);
		      throw new CustodyException(msg, ex);
		  }
	  }
  }
  
  @Override
  public void saveSignature(String custodyID, Map<String, Object> custodyParameters, 
      SignatureCustody document) throws CustodyException {
	  
	  try {
			
			String fileFinalame = AlfrescoUtils.getFileNameWithCustodyId(document.getName(), custodyID, true);

	    	Map<String, Object> fileProperties = new HashMap<String, Object>();
			fileProperties.put(PropertyIds.OBJECT_TYPE_ID, OpenCmisAlfrescoHelper.CMIS_DOCUMENT_TYPE);
			fileProperties.put("APBRegistro:dr_custodyID", custodyID);
			fileProperties.put("APBRegistro:dr_tipoArchivo", "S");
			fileProperties.put(PropertyIds.NAME, fileFinalame);
			
	    	Map<String, Object> folderProperties = new HashMap<String, Object>();
	    	folderProperties.put(PropertyIds.OBJECT_TYPE_ID, OpenCmisAlfrescoHelper.CMIS_FOLDER_TYPE);
		  
			List<Object> registreDecod = AlfrescoUtils.decodificaStringToObject(custodyParameters);
			
			String docPath = AlfrescoUtils.getPathFromRegistreObject(registreDecod.get(0));

			AlfrescoUtils.getPropertiesFromRegistreObject(registreDecod, fileProperties, custodyID);
			fileProperties.put("APBRegistro:dr_formato", document.getMime());
			AlfrescoUtils.getFolderPropertiesFromRegistreObject(registreDecod, folderProperties);			
//			
//			//Si el document no existeix, el cream. En altre cas, borram i tornam a pujar
//			Document aBorrar = OpenCmisAlfrescoHelper.getSignatureById(custodyID);
//			
//			if (aBorrar!=null) {
//				aBorrar.delete();
//			}

			this.a_firma = OpenCmisAlfrescoHelper.crearDocument(getCmisSession(), getPathCarpetaDocuments(), document, fileFinalame, docPath, fileProperties, folderProperties);
			
			log.debug("Pujada firma a Alfresco: "+docPath+"/"+fileFinalame);
	  	}catch (CmisPermissionDeniedException pdEx) {
			final String msg = "No s'ha pogut guardar la firma amb id "+custodyID+". "+pdEx.getMessage();
			throw new CustodyException(msg);
	    } catch (Exception ex) {
			final String msg = "No s'ha pogut guardar la firma amb id "+custodyID+". "+ex.getCause();
			log.error(msg, ex);
			throw new CustodyException(msg);
	    }
  }
  
  @Override
  public SignatureCustody getSignatureInfo(String custodyID) throws CustodyException {

	  Document sig = OpenCmisAlfrescoHelper.getSignatureById(getCmisSession(), custodyID);

	  if (sig!=null) {

		  try {
			  
			  SignatureCustody regSig = new SignatureCustody();
			  regSig.setData(AlfrescoUtils.getCmisObjectContent(sig));
			  String nomArxiu = AlfrescoUtils.removeCustodyIdFromFilename(sig.getName(), true);
			  regSig.setName(nomArxiu);
			  regSig.setMime(sig.getContentStreamMimeType());
			  
			  return regSig;
		  }catch (CmisPermissionDeniedException pdEx) {
			final String msg = "No s'ha pogut recuperar la firma amb id "+custodyID+". "+pdEx.getMessage();
			throw new CustodyException(msg);
		  }catch (Exception ex) {
		      final String msg = "Error al recuperar el contingut de la firma amb custodyID: "+custodyID;
		      log.error(msg, ex);
		      throw new CustodyException(msg, ex);
		  }
	  }else{
	      final String msg = "No s´ha trobat cap firma amb custodyID: "+custodyID;
	      log.error(msg);
	      throw new CustodyException(msg);
	  }
  }
  
  @Override
  public void deleteSignature(String custodyID) throws CustodyException, NotSupportedCustodyException {

	  Document sig = OpenCmisAlfrescoHelper.getSignatureById(getCmisSession(), custodyID);

	  if (sig!=null) {

		  try {

			  sig.delete();
			  
			  this.a_firma = null;

		  }catch (CmisPermissionDeniedException pdEx) {
			final String msg = "No s'ha pogut borrar la firma amb id "+custodyID+". "+pdEx.getMessage();
			throw new CustodyException(msg);
		  }catch (Exception ex) {
		      final String msg = "Error al borrar la firma amb custodyID: "+custodyID;
		      log.error(msg, ex);
		      throw new CustodyException(msg, ex);
		  }
	  }
  }
  
  @Override
  public void deleteCustody(String custodyID) throws CustodyException, NotSupportedCustodyException {

    if (!supportsDeleteCustody()) {
      throw new NotSupportedCustodyException();
    }

    //Borram la firma si en té
    deleteSignature(custodyID);
    
    deleteDocument(custodyID);
    
//    Document doc = OpenCmisAlfrescoHelper.getDocumentById(getCmisSession(), custodyID);
//
//    if (doc!=null) {
//    	
//    	//Abans de borrar el document, recuperam la carpeta pare per borrarla despres si escau
//	    List<Folder> carpetaPare = doc.getParents();
//	    doc.delete();
//		  
//		//Si la carpeta/es que contenia els docs de custodia ha quedat buida, la borram també
//		if (carpetaPare!=null) {
//			for (int c=0; c<carpetaPare.size(); c++) {
//				Folder carp = (Folder)carpetaPare.get(c);
//				Iterable<CmisObject> fills = carp.getChildren();
//				if (fills==null || !fills.iterator().hasNext()) {
//					carp.delete();
//				}
//			}
//		}
//    }
    
    this.a_documento = null;
    this.a_firma = null;
  }
  
  @Override
  public void addMetadata(String custodyID, Metadata metadata,
      Map<String, Object> custodyParameters) throws CustodyException,  NotSupportedCustodyException, MetadataFormatException {
	  
	  if (custodyID!=null && !"".equals(custodyID) && metadata!=null) {
		  
		  List<Document> docs = new ArrayList<Document>();
		  
		  Document doc = OpenCmisAlfrescoHelper.getDocumentById(getCmisSession(), custodyID);
		  Document sig = OpenCmisAlfrescoHelper.getSignatureById(getCmisSession(), custodyID);
	
		  docs.add(doc);
		  docs.add(sig);
		  
		  if (docs!=null && docs.size()>0) {
			  
			  for (int d=0; d<docs.size(); d++) {
				  
				  Document docAux = docs.get(d);
			  
				  if (docAux!=null) {
					  try {
						  AlfrescoDocument alfDoc = (AlfrescoDocument)docAux;
						  Map<String, Object> properties = new HashMap<String, Object>();
						  String cmisPropName = getPropNameFromMetadata(metadata);
						  //log.debug("Actualitzant Metadada "+metadata.getKey() + " corresponent a la propietat Alfresco: "+cmisPropName);
						  if (metadata.getMetadataType()==MetadataType.STRING) {
							  properties.put(cmisPropName, metadata.getValue());
						  }else if (metadata.getMetadataType()==MetadataType.DATE) {
							  properties.put(cmisPropName, AlfrescoUtils.stringToGregorianCalendar(metadata.getValue()));
						  }
						  alfDoc.updateProperties(properties);
					} catch (Exception ex) {
						final String msg = "No s'ha pogut actualitzar la metadada: "+metadata.getValue();
						log.error(msg, ex);
					}
				  }
			  }
		  }
	  }
  }
  
  @Override
  public void updateMetadata(String custodyID, Metadata[] metadata,
      Map<String, Object> custodyParameters) throws CustodyException, NotSupportedCustodyException, MetadataFormatException {
	  
	  if (custodyID!=null && !"".equals(custodyID) && metadata!=null) {
		  
		  List<Document> docs = new ArrayList<Document>();
		  
 		  if (this.a_documento!=null) {
 			  //Cas de nou annex (previament s´ha cridat a saveDocument)
 			  docs.add(this.a_documento);
 		  }else{
 			  //Cas de modificar dades de l´annex sense modificar l'arxiu (no s´ha cridat a saveDocument)
 			  docs.add(OpenCmisAlfrescoHelper.getDocumentById(getCmisSession(), custodyID));
 		  }

		  if (this.a_firma!=null) {
			  docs.add(this.a_firma);
		  }else{
 			  docs.add(OpenCmisAlfrescoHelper.getSignatureById(getCmisSession(), custodyID));
 		  }
		  
		  if (docs!=null && docs.size()>0) {
		  
			  for (int d=0; d<docs.size(); d++) {
				  
				  Document docAux = docs.get(d);
			  
				  if (docAux!=null) {
					  
					  try {
						  
						  AlfrescoDocument alfDoc = (AlfrescoDocument)docAux;
						  Map<String, Object> properties = new HashMap<String, Object>();
						  
						  for (int m=0; m<metadata.length; m++) {
	
							  Metadata metadada = metadata[m];
							  
							  String cmisPropName = getPropNameFromMetadata(metadada);

							  if (cmisPropName!=null) {
							  
								  log.debug("Actualitzant Metadada "+metadada.getKey() + " corresponent a la propietat Alfresco: "+cmisPropName);
	
								  if (metadada.getMetadataType()==MetadataType.STRING) {
									  properties.put(cmisPropName, metadada.getValue());
								  }else if (metadada.getMetadataType()==MetadataType.DATE) {
									  properties.put(cmisPropName, AlfrescoUtils.stringToGregorianCalendar(metadada.getValue()));
								  }
							  }
						  }

						  alfDoc.updateProperties(properties);

					} catch (Exception ex) {
						final String msg = "No s'ha pogut actualitzar la metadada.";
						log.error(msg, ex);
					}
				  }
			  }
		  }
		  
		  this.a_documento = null;
		  this.a_firma = null;
	  }
  }
  
  @Override
  public ArrayList<Metadata> deleteMetadata(String custodyID, String key) throws CustodyException {
	  
	  ArrayList<Metadata> out = new ArrayList<Metadata>();
	  
	  if (custodyID!=null && !"".equals(custodyID) && key!=null && !"".equals(key)) {
		  
		  List<Document> docs = new ArrayList<Document>();
		  
		  Document doc = OpenCmisAlfrescoHelper.getDocumentById(getCmisSession(), custodyID);
		  Document sig = OpenCmisAlfrescoHelper.getSignatureById(getCmisSession(), custodyID);
	
		  docs.add(doc);
		  docs.add(sig);
		  
		  if (docs!=null && docs.size()>0) {
			  
			  for (int d=0; d<docs.size(); d++) {
			  
				  Document docAux = docs.get(d);
				  
				  if (docAux!=null) {
					  
					  try {
						  //Borram el valor de la propietat a alfresco
						  AlfrescoDocument alfDoc = (AlfrescoDocument)docAux;
						  Map<String, Object> properties = new HashMap<String, Object>();
						  String cmisPropName = getPropNameFromMetadataId(key);
						  log.debug("Borrant Metadada "+ key + " corresponent a la propietat Alfresco: "+cmisPropName);
						  properties.put(cmisPropName, null);
						  alfDoc.updateProperties(properties);
						  
						  //TODO: Llegir la llista de properties resultant, per retornar la llista de metadades actualitzades
						  
					} catch (Exception ex) {
						final String msg = "No s'ha pogut actualitzar la metadada "+key + ". "+ex.getCause().toString();
						log.error(msg, ex);
					}
				  }
			  }
		  }
	  }
	  return out;
  }
  
  //Com que els identificadors de alfresco i de Metadata no son els mateixos per cada client,
  //An aquest mètode, feim el mapeig
  private String getPropNameFromMetadata(Metadata met) {
	  if (met!=null) {
		  return getPropNameFromMetadataId(met.getKey());
	  }
	  return null;
  }
  
  private String getPropNameFromMetadataId(String key) {
	  if (key!=null && !"".equals(key)) {
		  if ("anexo.fechaCaptura".equals(key)) {
			  return "APBRegistro:dr_fecEntrada";
		  }else if ("anexo.tipoDocumento".equals(key)) {
			  return "APBRegistro:dr_tipDocumento";
		  }else if ("oficina".equals(key)) {
			  return "APBRegistro:dr_ofiAnexa";
		  }else if ("anexo.origen".equals(key)) {
			  return "APBRegistro:dr_origen";
		  }else if ("anexo.validezDocumento".equals(key)) {
			  return "APBRegistro:dr_validez";
		  }else if ("anexo.formato".equals(key)) {
			  return "APBRegistro:dr_formato";
		  }else if ("anexo.tipoDocumental.descripcion".equals(key)) {
			  return "APBRegistro:dr_tipDocumental";
		  }else if ("anexo.observaciones".equals(key)) {
			  return "APBRegistro:dr_obsRegistro";
		  }else if ("anexo.titulo".equals(key)) {
			  return "cm:title";
		  }
	  }
	  return null;
  }
  
  @Override
  public DocumentCustody getDocumentInfoOnly(String custodyID) throws CustodyException {
	  
	  try {
		  
		  /*DocumentCustody doc = null;
		  
		  Document alfDoc = OpenCmisAlfrescoHelper.getDocumentById(custodyID);
		  if (alfDoc!=null) {
			  doc = new DocumentCustody();
			  doc.setMime(alfDoc.getContentStreamMimeType());
			  doc.setName(AlfrescoUtils.removeCustodyIdFromFilename(alfDoc.getName(), false));
			  doc.setData(AlfrescoUtils.getCmisObjectContent(alfDoc));
		  }*/	  
		  
		  String nom = OpenCmisAlfrescoHelper.getDocumentNameById(getCmisSession(), custodyID);
		  
		  if (nom!=null && !"".equals(nom)) {
			  DocumentCustody doc = new DocumentCustody();
			  doc.setName(AlfrescoUtils.removeCustodyIdFromFilename(nom, false));
			  return doc;
		  }else{
			  return null;
		  }

	  }catch (Exception ex) {
		  throw new CustodyException("No s´ha pogut llegir el contingut del document amb custodyID="+custodyID+".", ex);
	  }
  }
  
  @Override
  public SignatureCustody getSignatureInfoOnly(String custodyID) throws CustodyException {
	  try {
		  
		  /*SignatureCustody doc = null;
		  Document alfDoc = OpenCmisAlfrescoHelper.getSignatureById(custodyID);
		  if (alfDoc!=null) {
			  doc = new SignatureCustody();
			  doc.setMime(alfDoc.getContentStreamMimeType());
			  doc.setName(AlfrescoUtils.removeCustodyIdFromFilename(alfDoc.getName(), true));
			  doc.setData(AlfrescoUtils.getCmisObjectContent(alfDoc));
		  }*/

		  String nom = OpenCmisAlfrescoHelper.getSignatureNameById(getCmisSession(), custodyID);
		  
		  if (nom!=null && !"".equals(nom)) {
			  SignatureCustody doc = new SignatureCustody();
			  doc.setName(AlfrescoUtils.removeCustodyIdFromFilename(nom, true));
			  return doc;
		  }else{
			  return null;
		  }
		  
	  }catch (Exception ex) {
		  throw new CustodyException("No s´ha pogut llegir el contingut de la firma amb custodyID="+custodyID+".", ex);
	  }
  }
  
  @Override
  protected void writeFile(String custodyID, String relativePath, byte[] data) throws Exception {
	  // TODO Auto-generated method stub
  }

  @Override
  protected byte[] readFile(String custodyID, String relativePath) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected String getPropertyBase() {
    return ALFRESCO_PROPERTY_BASE;
  }

  @Override
  protected void writeFileCreateParentDir(String custodyID, String relativePath, byte[] data)
      throws Exception {
    // TODO Auto-generated method stub    
  }

  @Override
  protected long lengthFile(String custodyID, String relativePath) throws Exception {
    try {
      Document document = OpenCmisAlfrescoHelper.getDocument(cmisSession, relativePath, custodyID);
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
