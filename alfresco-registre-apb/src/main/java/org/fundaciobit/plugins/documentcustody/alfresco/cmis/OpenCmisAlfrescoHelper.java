package org.fundaciobit.plugins.documentcustody.alfresco.cmis;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.cmis.client.AlfrescoFolder;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.CmisExtensionElement;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.enums.ExtensionLevel;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.CmisExtensionElementImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.fundaciobit.plugins.documentcustody.api.AnnexCustody;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson.JacksonFactory;

/**
 * Limit Tecnologies
 * @author andreus
 * 
 */
public class OpenCmisAlfrescoHelper {

	public static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	public static final JsonFactory JSON_FACTORY = new JacksonFactory();
	
	private static HttpRequestFactory requestFactory;
	
	public static final String CMIS_DOCUMENT_PROPS = "P:APBRegistro:d_anexo";
	public static final String CMIS_DOCUMENT_TYPE  = "D:APBRegistro:anexo,P:APBRegistro:d_anexo,P:cm:titled";
	public static final String CMIS_DOCUMENT_TYPE_QUERY = "P:APBRegistro:d_anexo";
	public static final String CMIS_FOLDER_TYPE   = "F:APBRegistro:registro,P:APBRegistro:c_registro";

	public static Folder crearCarpeta(Session cmisSession, String rootPath, String folderName, Folder parent, Map<String, Object> folderProperties) {
		folderProperties.put(PropertyIds.NAME, folderName);
		if (parent!=null) {
			return parent.createFolder(folderProperties);
		}else{
			return crearCarpeta(cmisSession, rootPath, folderProperties);
		}
	}
	
	/**
	 * Crea una carpeta amb el nom indicat penjant de la carpeta arrel del site.
	 * El folder name no pot contenir barres, ja que no es crearan recursivamente les carpetas,
	 * per això esta el métode createFoldersPath(path)
	 * Retorna la carpeta creada.
	 */
	public static Folder crearCarpeta(Session cmisSession, String rootPath, Map<String, Object> props) {
		
		Folder carpCreada = null;
		
		if (props!=null) {
			
			String folderName = props.get(PropertyIds.NAME).toString();
			
			if (!folderName.startsWith("/")) { folderName = "/" + folderName; }

	        Folder rootFolder = (Folder)getRegistreDocsFolder(cmisSession, rootPath);
	        try {
	        	carpCreada = (Folder) cmisSession.getObjectByPath(rootFolder.getPath() + folderName);
	        } catch (CmisObjectNotFoundException onfe) {
	        	carpCreada = rootFolder.createFolder(props);
	        }
		}
		return carpCreada;
	}
	
	/**
	 * Crea una carpeta amb el nom indicat dins el properties (PropertyIds.NAME), penjant de la carpeta indicada en el segon parametre.
	 * @param folderType Tipus de carpeta, si no se li passa el nom de un custom object, es creara un de tipus generic (cmis:folder)
	 * @param parentFolderPath Carpeta pare, si no se li passa valor, no es creará la carpeta.
	 * @return La carpeta que s´acaba de crear.
	 */
	public static Folder crearCarpeta(Session cmisSession, String rootPath, Map<String, Object> props, String parentFolderPath) {
			
		Folder carpetaCreada = null;
		
		if (parentFolderPath!=null && !"".equals(parentFolderPath) && !"null".equalsIgnoreCase(parentFolderPath)) { 

			try {
				
				if (!parentFolderPath.startsWith("/")) { parentFolderPath = "/" + parentFolderPath; }
		        Folder carpetaPadre = (Folder)cmisSession.getObjectByPath(rootPath + parentFolderPath);	
		        carpetaCreada = carpetaPadre.createFolder(props);
			     
			} catch (CmisObjectNotFoundException onfe) {
				System.out.println("La carpeta contenidora ("+parentFolderPath+ " no existeix.");
				onfe.printStackTrace();
			} catch (Exception ex) {
				System.out.println("Error al crear una carpeta dins " +parentFolderPath+ ".");
				ex.printStackTrace();
			}
		}
		
		return carpetaCreada;
	}
	
	/**
	 * Crea recursivament una ruta de carpetes partint de la carpeta principal de documents del registre.
	 * El path es dividirá en carpetes que s´anirán creant de forma anidada.
	 * Les carpetes creades serán del tipus cmis:folder
	 * Retorna la ultima carpeta creada.
	 */
	public static Folder crearRutaDeCarpetes(Session cmisSession, String rootPath, String path, Map<String, Object> folderProperties) {
		
		if (path!=null && path!="") {

			if (path.indexOf("/")!=-1 && path.startsWith("/")) {
				
				// ** Partim de la carpeta principal de documentació del site ** //
		        Folder folderActual = (Folder)getRegistreDocsFolder(cmisSession, rootPath);
				String[] carpetesAcrear = path.split("/");
				String rutaCarpActual = rootPath;
				
				for (int numCarp = 1; numCarp<carpetesAcrear.length; numCarp++) {

					try {
						folderActual = (Folder) cmisSession.getObjectByPath(rutaCarpActual + "/" + carpetesAcrear[numCarp]);
			        } catch (CmisObjectNotFoundException onfe) {
			        	//Nomes la darrera carpeta de la ruta, conté les metadades del registre, la resta son carpetes normals
			        	if (numCarp==carpetesAcrear.length-1) {
			        		folderActual = crearCarpeta(cmisSession, rootPath, carpetesAcrear[numCarp], folderActual, folderProperties);
			        	}else{
			    	    	Map<String, Object> basicFolderProperties = new HashMap<String, Object>();
			    	    	basicFolderProperties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
			    	    	basicFolderProperties.put(PropertyIds.NAME, folderProperties.get(PropertyIds.NAME));
			        		folderActual = crearCarpeta(cmisSession, rootPath, carpetesAcrear[numCarp], folderActual, basicFolderProperties);
			        	}
					}

					rutaCarpActual = rutaCarpActual + "/" + carpetesAcrear[numCarp];
				}
				
				
				
				return folderActual;

			}else{
				System.out.println("El path passat a la funció createFoldersPath(String path) no comença amb una barra.");
			}
		}
		
		return null;
	}
		
	/**
	 * Borra una carpeta del servidor.
	 * @param rutaCarpeta Path de la carpeta que es vol eliminar.
	 * @param recursiu Si false, s´intentará borrar la carpeta, si té fills, no es borrarà. Si true, es borrará la carpeta i tot el que contengui.
	 */
	public static void borrarCarpeta(Session cmisSession, String rootPath, String rutaCarpeta, boolean recursiu) {
		
		try {
			
			if (!rutaCarpeta.startsWith("/")) { rutaCarpeta = "/" + rutaCarpeta; }
			if ("/".equals(rutaCarpeta)) { rutaCarpeta = ""; }
			
			String rutaCompleta = rootPath+rutaCarpeta;
			Folder folderAborrar = (Folder) cmisSession.getObjectByPath(rutaCompleta);
			
			if (!recursiu) {
				folderAborrar.delete();
			}else{
				borrarCarpetaIfills(folderAborrar);
			}
		}catch (CmisConstraintException coEx) {
			System.out.println("La carpeta: "+rutaCarpeta+" conté documents o subcarpetes. No s´ha borrat. Feis la cridada amb recursiu=true per borrar-los.");
			coEx.printStackTrace();
		}catch (Exception ex) {
			System.out.println("Error al intentar borrar la carpeta: "+rutaCarpeta);
			ex.printStackTrace();
		}
	}
	
	private static void borrarCarpetaIfills(Folder carpeta) {
		
		ItemIterable<CmisObject> fills = carpeta.getChildren();

		for(CmisObject fill: fills) {
			if (fill instanceof Document) {
				((Document)fill).delete();
			}else if (fill instanceof Folder) {
				borrarCarpetaIfills((Folder)fill);
			}
		}

		carpeta.delete();
	}
	
	public static void borrarCarpeta(Session cmisSession, String rootPath, String rutaCarpeta) {
		borrarCarpeta(cmisSession, rootPath, rutaCarpeta, true);
	}
	
	/**
	 * Imprimeix la informacio basica (tipus i nom) de carpetes y subcarpetes de sistema 
	 * que penjen de la carpeta root que retorna el metode cmisSession.getRootFolrder()
	 * Els documents que es pujen a Alfresco via Web es guarden dins /Sites/{nom-del-site}/documentLibrary
	 */
	public static void printRootFolderItems(Session cmisSession) {

		Folder folder = (Folder) cmisSession.getRootFolder();
		ItemIterable<CmisObject> children = folder.getChildren();
	
		for(CmisObject chind: children) {
			
		    System.out.println("["+chind.getType().getId()+"] : "+chind.getName());
		    
		    if (chind.getName().equals("Sites")) {
		    	
		    	Folder sitios = (Folder)chind;
		    	
				ItemIterable<CmisObject> childrenSitios = sitios.getChildren();
				
				for(CmisObject chisd: childrenSitios) {
					
					System.out.println("	- ["+chind.getType().getId()+"] : "+chisd.getName());
					
					//if (chisd.getName().equals("registro-regweb")) {
				    	
				    	Folder regWeb = (Folder)chisd;
						ItemIterable<CmisObject> childrenregWeb = regWeb.getChildren();
						
						for(CmisObject chidrw: childrenregWeb) {
							
							System.out.println("		- ["+chind.getType().getId()+"] : "+chidrw.getName());
							
							if (chidrw.getName().equals("documentLibrary")) {
						    	
								Folder regWeb_dl = (Folder)chidrw;
								ItemIterable<CmisObject> childrenDocsLib = regWeb_dl.getChildren();
																
								for(CmisObject chiddl: childrenDocsLib) {

									System.out.println("		- ["+chiddl.getType().getId()+"] : "+chiddl.getName());
								}
							}
							
						}
					//}
				}
		    }
		}
	}

	/**
	 * Recupera l´objecte Folder corresponent a la carpeta pare de on penja tots els documents i resta de carpetes.
	 */
	public static Folder getRegistreDocsFolder(Session cmisSession, String rootPath) {
		return (Folder)cmisSession.getObjectByPath(rootPath);
	}
	
	/**
	 * Crea un document dins la ruta indicada, amb data de l'annex i amb les propietats indicades
	 * @param document Conte el array de bites del fitxer final.
	 * @param fileName Nom del fitxer
	 * @param path Ruta a on es creará el document
	 * @param fileProperties Metadades del document
	 * @return El document creat o null si no s´ha pogut crear.
	 */
	public static Document crearDocument(Session cmisSession, String rootPath, AnnexCustody document, String fileName, String path, Map<String, Object> fileProperties, Map<String, Object> folderProperties) {

		Folder parentFolder = null;
		Document documentCreat = null;
		
		try {
			if (path!=null && !"".equals(path)) {
				if (!path.startsWith("/")) { 
					path = "/"+path;
				}
			}else{
				path = "";
			}
			parentFolder = (Folder) cmisSession.getObjectByPath(rootPath+path);
		}catch (CmisObjectNotFoundException onfEx) {
			//Si la carpeta proposada no existeix, es creará la ruta necessaria
			parentFolder = crearRutaDeCarpetes(cmisSession, rootPath, path, folderProperties);
		}
		
		if (parentFolder!=null) {

			
			
			try {
				//Actualitzam les metadades de la carpeta
				AlfrescoFolder alfFol = (AlfrescoFolder)parentFolder;
				alfFol.updateProperties(folderProperties);
			}catch (Exception ex) {
				System.out.println(" * Erro Alfresco: No s'han pogut actualitzar les metadades de la carpeta contenidora del document ("+path+"). - "+ ex.getCause());
			}
			
			// contingut
			byte[] content = document.getData();
			InputStream stream = new ByteArrayInputStream(content);
			ContentStream contentStream = new ContentStreamImpl(fileName, BigInteger.valueOf(content.length), document.getMime(), stream);

			documentCreat = parentFolder.createDocument(fileProperties, contentStream, VersioningState.MAJOR);			
		}
		
		return documentCreat;
	}
	
	public static Document crearDocument(Session cmisSession, String rootPath, AnnexCustody document, String fileName, String path) throws FileNotFoundException {

    	Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(PropertyIds.OBJECT_TYPE_ID, CMIS_DOCUMENT_TYPE);
		properties.put(PropertyIds.NAME, fileName);
		
		return crearDocument(cmisSession, rootPath, document, fileName, path, properties, null);
	}

	public static Document crearDocument(Session cmisSession, String rootPath, AnnexCustody document, String fileName, Map<String, Object> properties) throws FileNotFoundException {
		
		return crearDocument(cmisSession, rootPath, document, fileName, "", properties, null);
	}
	
	public static Document crearDocument(Session cmisSession, String rootPath, AnnexCustody document, String fileName) throws FileNotFoundException {
	
    	Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(PropertyIds.OBJECT_TYPE_ID, CMIS_DOCUMENT_TYPE);
		properties.put(PropertyIds.NAME, fileName);
		
		return crearDocument(cmisSession, rootPath, document, fileName, "", properties, null);
	}
	
	public static void borrarDocument(Session cmisSession, String rootPath, String rutaDoc) {
		try {
			String rutaCompleta = rootPath+rutaDoc;
			Document docAborrar = (Document) cmisSession.getObjectByPath(rutaCompleta);
			docAborrar.delete();
		} catch (CmisObjectNotFoundException onfe) {
			System.out.println("El document "+rutaDoc+" no existeix.");
			onfe.printStackTrace();
		} catch (ClassCastException cce) {
			System.out.println("El document "+rutaDoc+" no es de tipus document.");
			cce.printStackTrace();
		}catch (Exception ex) {
			System.out.println("Error al intentar borrar el document: "+rutaDoc);
			ex.printStackTrace();
		}
	}
	
	/**
	 * Recupera un document del repositori Alfresco donat una ruta
	 */
 	public static Document getDocument(Session cmisSession, String rootPath, String path) {
		return (Document) cmisSession.getObjectByPath(rootPath+path);		
	}

 	/**
 	 * Recupera la firma de una custodia si se li passa nomes el custodyID
 	 */
 	public static Document getSignatureById(Session cmisSession, String custodyID) {
		
 		Document sig = null;
 		
	 	ObjectType type = cmisSession.getTypeDefinition(CMIS_DOCUMENT_TYPE_QUERY);
	 	PropertyDefinition<?> objectIdPropDef = type.getPropertyDefinitions().get(PropertyIds.OBJECT_ID);
	 	String objectIdQueryName = objectIdPropDef.getQueryName();
	
	 	String queryString = "SELECT " + objectIdQueryName + " FROM " + type.getQueryName() + " WHERE APBRegistro:dr_custodyID = '" +custodyID+"' AND APBRegistro:dr_tipoArchivo='S'" ;
	 	
	 	//System.out.println(queryString);
	 	
	 	ItemIterable<QueryResult> results = cmisSession.query(queryString, false);
	
	 	for (QueryResult qResult : results) {
	 		String objectId = qResult.getPropertyValueByQueryName(objectIdQueryName);
	 		sig = (Document)cmisSession.getObject(cmisSession.createObjectId(objectId));
	 	}
	 	
	 	return sig;
 	}
 	
 	public static String getSignatureNameById(Session cmisSession, String custodyID) {
		
	 	ObjectType type = cmisSession.getTypeDefinition(CMIS_DOCUMENT_TYPE_QUERY);
	 	PropertyDefinition<?> objectIdPropDef = type.getPropertyDefinitions().get(PropertyIds.NAME);
	 	String objectIdQueryName = objectIdPropDef.getQueryName();
	
	 	String queryString = "SELECT " + objectIdQueryName + " FROM " + type.getQueryName() + " WHERE APBRegistro:dr_custodyID = '" +custodyID+"' AND APBRegistro:dr_tipoArchivo='S'" ;
	 	
	 	//System.out.println(queryString);
	 	
	 	ItemIterable<QueryResult> results = cmisSession.query(queryString, false);
	
	 	String objectName = "";
	 	
	 	for (QueryResult qResult : results) {
	 		objectName = qResult.getPropertyValueByQueryName(objectIdQueryName);
	 	}
	 	
	 	return objectName;
 	}
 	
 	/**
 	 * Recupera tots els documents de una custodia si se li passa nomes el custodyID
 	 * Si se li afegeix el sufixe "D" o "S", recuperará nomes el document o la firma respectivament
 	 */
 	public static Document getDocumentById(Session cmisSession, String custodyID) {
 		
		Document doc = null;
		
	 	ObjectType type = cmisSession.getTypeDefinition(CMIS_DOCUMENT_TYPE_QUERY);
	 	PropertyDefinition<?> objectIdPropDef = type.getPropertyDefinitions().get(PropertyIds.OBJECT_ID);
	 	String objectIdQueryName = objectIdPropDef.getQueryName();
	
	 	//String queryString = "SELECT " + objectIdQueryName + " FROM " + type.getQueryName() + " WHERE "+PropertyIds.DESCRIPTION+" LIKE '" +id+"%'" ;
	 	String queryString = "SELECT " + objectIdQueryName + " FROM " + type.getQueryName() + " WHERE APBRegistro:dr_custodyID = '" +custodyID+"' AND APBRegistro:dr_tipoArchivo='D'" ;
	 	
	 	//System.out.println(queryString);
	 	
	 	ItemIterable<QueryResult> results = cmisSession.query(queryString, false);
	 	
	 	for (QueryResult qResult : results) {
	 	   String objectId = qResult.getPropertyValueByQueryName(objectIdQueryName);
	 	   doc = (Document) cmisSession.getObject(cmisSession.createObjectId(objectId));
	 	}
	 	
	 	return doc;

	}
 	
 	public static String getDocumentNameById(Session cmisSession, String custodyID) {
		
	 	ObjectType type = cmisSession.getTypeDefinition(CMIS_DOCUMENT_TYPE_QUERY);
	 	PropertyDefinition<?> objectIdPropDef = type.getPropertyDefinitions().get(PropertyIds.NAME);
	 	String objectIdQueryName = objectIdPropDef.getQueryName();
	
	 	//String queryString = "SELECT " + objectIdQueryName + " FROM " + type.getQueryName() + " WHERE "+PropertyIds.DESCRIPTION+" LIKE '" +id+"%'" ;
	 	String queryString = "SELECT " + objectIdQueryName + " FROM " + type.getQueryName() + " WHERE APBRegistro:dr_custodyID = '" +custodyID+"' AND APBRegistro:dr_tipoArchivo='D'" ;
	 	
	 	System.out.println(queryString);
	 	
	 	ItemIterable<QueryResult> results = cmisSession.query(queryString, false);
	 	
	 	String objectName = "";
	 	
	 	for (QueryResult qResult : results) {
	 		objectName = qResult.getPropertyValueByQueryName(objectIdQueryName);
	 	}
	 	
	 	return objectName;
	} 	
 	
	/**
	 * Afegeix una metadada al document o carpeta
	 */
	public static void addMetaDataToCMISObject(Folder parentFolder, Document doc, String nom, String valor) {
		
		List<CmisExtensionElement> extensions = doc.getExtensions(ExtensionLevel.PROPERTIES);
		
		for(CmisExtensionElement ext: extensions) {

			for(CmisExtensionElement child: ext.getChildren()) {
				
				if ("P:APBRegistro:d_anexo".equals(child.getValue())) {
					
					for(CmisExtensionElement p_child: child.getChildren()) {

						if (p_child.getAttributes().get("propertyDefinitionId").equals(nom)) {
							CmisExtensionElement newExt = new CmisExtensionElementImpl(p_child.getNamespace(), p_child.getName(), p_child.getAttributes(), valor);
							child.getChildren().add(newExt);
						}
					}
				}
			}
		}
		
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(PropertyIds.OBJECT_TYPE_ID, "D:APBRegistro:anexo");
		properties.put(PropertyIds.NAME, doc.getProperty(PropertyIds.NAME));
		
		parentFolder.createDocument(properties, doc.getContentStream(), VersioningState.MAJOR);
	}
	
	/**
	 * Imprimeix informacion de les metadades o extensions de un document CMIS
	 */
	public static void printMetadataCMISobject(Document docmnt, ExtensionLevel eLvl) {
		
		if (eLvl!=null) {
		
			List<CmisExtensionElement> extensions = docmnt.getExtensions(eLvl);
	
			System.out.println("#### IMPRIMIENDO EXTENSIONES DEL NIVEL "+eLvl.value() + " ####");
			
			if (extensions!=null) {
			
				for(CmisExtensionElement ext: extensions) {
					
					System.out.println("Extension --> Name: " + ext.getName());
					System.out.println("Extension --> NameSpace: " + ext.getNamespace());
					System.out.println("Extension --> Value: " + ext.getValue());
					
					for(CmisExtensionElement child: ext.getChildren()) {
						
						System.out.println(" - Extension Nvl2 --> Clild Name: " + child.getName());
						System.out.println(" - Extension Nvl2 --> Clild NS: " + child.getNamespace());
						System.out.println(" - Extension Nvl2 --> Clild Value: " + child.getValue());
							
						for(CmisExtensionElement p_child: child.getChildren()) {
							
							System.out.println(" 	- Property --> Clild Name: " + p_child.getName());
							System.out.println(" 	- Property --> Clild NS: " + p_child.getNamespace());
							System.out.println(" 	- Property --> Clild Value: " + p_child.getValue());
							
							Iterator<String> attribs = p_child.getAttributes().keySet().iterator();
							while(attribs.hasNext()) {
								String att_key = attribs.next();
								System.out.println(" 		- Attribute --> "+att_key+"=" + p_child.getAttributes().get(att_key));
							}
						}
					}
				}
			}
			
			System.out.println("#### FIN EXTENSIONES DEL NIVEL "+eLvl.value() + " ####");
		}
	}
	
	public static void setObjectProperties(CmisObject object) {
		// some dummy data
		String typeId = "MyType";
		String objectId = "1111-2222-3333";
		String name = "MyDocument";

		// find a namespace for the extensions that is different from the CMIS namespaces
		String ns = "http://apache.org/opencmis/example";

		// create a list for the first level of our extension
		List<CmisExtensionElement> extElements = new ArrayList<CmisExtensionElement>();

		// set up an attribute (Avoid attributes! They will not work with the JSON binding!)
		Map<String, String> attr = new HashMap<String, String>();
		attr.put("type", typeId);

		// add two leafs to the extension
		extElements.add(new CmisExtensionElementImpl(ns, "objectId", attr, objectId));
		extElements.add(new CmisExtensionElementImpl(ns, "name", null, name));

		// set the extension list
		List<CmisExtensionElement> extensions = new ArrayList<CmisExtensionElement>();
		extensions.add(new CmisExtensionElementImpl(ns, "exampleExtension", null, extElements));
		//object.setExtensions(extensions);
	}
	
	public static HttpRequestFactory getRequestFactory() {
		if (requestFactory == null) {
    		requestFactory = HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
    			public void initialize(HttpRequest request) throws IOException {
    				request.setParser(new JsonObjectParser(new JacksonFactory()));
    				request.getHeaders().setBasicAuthentication(getUsername(), getPassword());
    			}
    		});
		}
		return requestFactory;
	}
	
	public static String getAlfrescoUrl() {
		return System.getProperty("es.caib.regweb.annex.plugins.documentcustody.alfresco.url");
	}

	public static String getUsername() {
		return System.getProperty("es.caib.regweb.annex.plugins.documentcustody.alfresco.access.user"); 
	}

	public static String getPassword() {
		return System.getProperty("es.caib.regweb.annex.plugins.documentcustody.alfresco.access.pass");		 		
	}

}
