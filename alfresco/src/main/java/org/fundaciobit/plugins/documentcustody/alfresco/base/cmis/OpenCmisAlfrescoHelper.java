package org.fundaciobit.plugins.documentcustody.alfresco.base.cmis;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.bindings.spi.webservices.CXFPortProvider;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.CmisExtensionElement;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.ExtensionLevel;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.CmisExtensionElementImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.fundaciobit.plugins.documentcustody.api.AnnexCustody;
import org.fundaciobit.plugins.documentcustody.alfresco.base.AlfrescoBaseDocumentCustodyPlugin;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson.JacksonFactory;

/**
 * Limit Tecnologies
 * 
 * @author andreus
 * @author anadal
 */
public class OpenCmisAlfrescoHelper {

  public static final String CMIS_DOCUMENT_TYPE = "cmis:document";
  public static final String CMIS_FOLDER_TYPE = "cmis:folder";

  protected static final Logger log = Logger.getLogger(OpenCmisAlfrescoHelper.class);

  protected final AlfrescoBaseDocumentCustodyPlugin alfresco;

  /**
   * 
   * @param alfresco
   */
  public OpenCmisAlfrescoHelper(AlfrescoBaseDocumentCustodyPlugin alfresco) {
    this.alfresco = alfresco;
  }

  /**
   * Retorna la carpeta pincipal de a on penjen els documents. Ja que la carpeta
   * root que et torna la classe CmisSession no es la carpeta arrel que esperam.
   * Una cridada a la funció printRootFolderItems() mostraría l´estructura de
   * carpetes desde la root de sistema.
   */
  public String getPathCarpetaDocs(String custodyId, String path) throws Exception {
    String site = alfresco.getSite();

    final boolean debug = log.isDebugEnabled();
    String relativePath;
    if (path == null || path.trim().length() == 0) {

      relativePath = alfresco.getBasePath() + ((custodyId == null) ? "" : ("/" + custodyId));
      if (debug) {
        log.debug("getPathCarpetaDocs()::path EMPTY");
      }

    } else {

      if (custodyId == null || custodyId.trim().length() == 0) {

        relativePath = alfresco.getBasePath() + path;

        if (debug) {
          log.debug("getPathCarpetaDocs()::custodyId EMPTY");
        }

      } else {

        // path = testReposWithFolder/1975/04/24/155/1234567890.DOC
        // custodyID = 1234567890
        // relative path =>
        // testReposWithFolder/1975/04/24/155/1234567890/1234567890.DOC
        int pos = path.lastIndexOf('/');

        String relativeFolder = path.substring(0, pos + 1);

        String custodyFolder = custodyId;

        String filename = path.substring(pos);

        if (debug) {
          log.debug("getPathCarpetaDocs()::relativeFolder = " + relativeFolder);
          log.debug("getPathCarpetaDocs()::custodyFolder = " + custodyFolder);
          log.debug("getPathCarpetaDocs()::filename = " + filename);
        }

        relativePath = alfresco.getBasePath() + relativeFolder + custodyFolder + filename;

      }
    }

    if (debug) {
      log.debug("getPathCarpetaDocs()::relativePATH = " + relativePath);
    }

    if (site != null) {
      return "/Sites/" + site + "/documentLibrary" + relativePath;
    } else {
      String fullSitePath = alfresco.getFullSitePath();
      if (fullSitePath != null) {
        return fullSitePath + relativePath;
      }
      String p1 = alfresco
          .getPropertyName(AlfrescoBaseDocumentCustodyPlugin.ALFRESCO_PROPERTY_BASE + "site");
      String p2 = alfresco
          .getPropertyName(AlfrescoBaseDocumentCustodyPlugin.ALFRESCO_PROPERTY_BASE
              + "fullsitepath");
      String msg = "Ha de definir una de les següent propietats: " + p1 + " o " + p2;
      log.error(msg, new Exception());
      throw new Exception(msg);
    }
  }

  /**
   * Crea una carpeta dins la carpeta arrel del site d'alfresco indicat al
   * fitxer de properties. Es pot utilitzar el valor de retorn per crear una
   * altra carpeta dins la carpeta creada.
   * 
   * @param folderName
   *          Nom de la carpeta, no ha de contenir barres.
   * @return Carpeta que s´acaba de crear
   */
  public Folder crearCarpeta(String folderName, String custodyId) throws Exception {

    return crearCarpeta(folderName, null, custodyId);
  }

  public Folder crearCarpeta(String folderName, Folder parent, String custodyId)
      throws Exception {

    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
    properties.put(PropertyIds.NAME, folderName);

    if (parent != null) {
      return parent.createFolder(properties);
    } else {
      return crearCarpeta(properties, custodyId);
    }
  }

  /**
   * Crea una carpeta amb el nom indicat penjant de la carpeta arrel del site.
   * El folder name no pot contenir barres, ja que no es crearan recursivamente
   * les carpetas, per això esta el métode createFoldersPath(path) Retorna la
   * carpeta creada.
   */
  public Folder crearCarpeta(Map<String, Object> props, String custodyId) throws Exception {

    Folder carpCreada = null;

    if (props != null) {

      String folderName = props.get(PropertyIds.NAME).toString();

      if (!folderName.startsWith("/")) {
        folderName = "/" + folderName;
      }

      Session cmisSession = getCmisSession();
      Folder rootFolder = (Folder) getBaseDocsFolder(custodyId);
      try {
        carpCreada = (Folder) cmisSession.getObjectByPath(rootFolder.getPath() + folderName);
        log.warn("La carpeta " + folderName + " ja existeix dins " + rootFolder.getPath()
            + ".");
      } catch (CmisObjectNotFoundException onfe) {
        carpCreada = rootFolder.createFolder(props);
      }
    }
    return carpCreada;
  }

  /**
   * Crea una carpeta amb el nom indicat dins el properties (PropertyIds.NAME),
   * penjant de la carpeta indicada en el segon parametre.
   * 
   * @param folderType
   *          Tipus de carpeta, si no se li passa el nom de un custom object, es
   *          creara un de tipus generic (cmis:folder)
   * @param parentFolderPath
   *          Carpeta pare, si no se li passa valor, no es creará la carpeta.
   * @return La carpeta que s´acaba de crear.
   */
  public Folder crearCarpeta(Map<String, Object> props, String parentFolderPath,
      String custodyId) {

    // Session cmisSession = getCmisSession();
    Folder carpetaCreada = null;

    if (parentFolderPath != null && !"".equals(parentFolderPath)
        && !"null".equalsIgnoreCase(parentFolderPath)) {

      try {

        if (!parentFolderPath.startsWith("/")) {
          parentFolderPath = "/" + parentFolderPath;
        }
        Folder carpetaPadre = (Folder) getCmisSession().getObjectByPath(
            getPathCarpetaDocs(custodyId, parentFolderPath));
        carpetaCreada = carpetaPadre.createFolder(props);

      } catch (CmisObjectNotFoundException onfe) {
        log.error("La carpeta contenidora (" + parentFolderPath + " no existeix.", onfe);
      } catch (Exception ex) {
        log.error("Error al crear una carpeta dins " + parentFolderPath + ".", ex);
      }
    }

    return carpetaCreada;
  }

  /**
   * Crea recursivament una ruta de carpetes partint de la carpeta principal de
   * documents del registre. El path es dividirá en carpetes que s´anirán creant
   * de forma anidada. Les carpetes creades serán del tipus cmis:folder Retorna
   * la ultima carpeta creada.
   */
  public Folder crearRutaDeCarpetes(String path, String custodyId) throws Exception {

    final boolean debug = log.isDebugEnabled();

    if (path != null && path.trim().length() != 0) {

      if (path.indexOf("/") != -1 && path.startsWith("/")) {

        Session cmisSession = getCmisSession();
        // ** Partim de la carpeta principal de documentació del site ** //
        Folder folderActual = (Folder) getBaseDocsFolder(null);

        if (debug) {
          log.debug("crearRutaDeCarpetes():: path =" + path);
        }
        if (custodyId != null) {
          path = path + "/" + custodyId;
        }

        if (debug) {
          log.debug("crearRutaDeCarpetes():: path + cust =" + path);
        }

        String[] carpetesAcrear = path.split("/");

        String rutaCarpActual = getPathCarpetaDocs(null, "");

        if (debug) {
          log.debug("crearRutaDeCarpetes():: ROOT  =" + rutaCarpActual);
        }

        for (int numCarp = 1; numCarp < carpetesAcrear.length; numCarp++) {

          String novaRuta = rutaCarpActual + "/" + carpetesAcrear[numCarp];

          if (debug) {
            log.debug("crearRutaDeCarpetes():: novaRuta[" + numCarp + "] = " + novaRuta);
          }

          try {
            folderActual = (Folder) cmisSession.getObjectByPath(novaRuta);
          } catch (CmisObjectNotFoundException onfe) {
            folderActual = crearCarpeta(carpetesAcrear[numCarp], folderActual, null);
          }

          rutaCarpActual = novaRuta;
        }

        return folderActual;

      } else {
        if (debug) {
          log.debug("El path passat a la funció createFoldersPath(String path) no comença amb una barra.");
        }
      }
    }

    return null;
  }

  /**
   * Borra una carpeta del servidor.
   * 
   * @param rutaCarpeta
   *          Path de la carpeta que es vol eliminar.
   * @param recursiu
   *          Si false, s´intentará borrar la carpeta, si té fills, no es
   *          borrarà. Si true, es borrará la carpeta i tot el que contengui.
   */
  public void borrarCarpeta(String rutaCarpeta, boolean recursiu, String custodyId) {

    try {
      // Session cmisSession = getCmisSession();

      if (!rutaCarpeta.startsWith("/")) {
        rutaCarpeta = "/" + rutaCarpeta;
      }
      if ("/".equals(rutaCarpeta)) {
        rutaCarpeta = "";
      }

      Session cmisSession = getCmisSession();

      String rutaCompleta = getPathCarpetaDocs(custodyId, rutaCarpeta);
      Folder folderAborrar = (Folder) cmisSession.getObjectByPath(rutaCompleta);

      if (!recursiu) {
        folderAborrar.delete();
      } else {
        borrarCarpetaIfills(folderAborrar);
      }
    } catch (CmisConstraintException coEx) {
      log.error(
          "La carpeta: "
              + rutaCarpeta
              + " conté documents o subcarpetes. No s´ha borrat. Feis la cridada amb recursiu=true per esborrar-los.",
          coEx);
    } catch (Exception ex) {
      log.error("Error al intentar borrar la carpeta: " + rutaCarpeta, ex);
    }
  }

  private static void borrarCarpetaIfills(Folder carpeta) {


    ItemIterable<CmisObject> fills = carpeta.getChildren();

    final boolean debug = log.isDebugEnabled();

    if (debug) {
      log.debug("borrarCarpetaIfills()::Entrant a carpeta: " + carpeta.getName());
    }

    for (CmisObject fill : fills) {
      if (fill instanceof Document) {
        ((Document) fill).delete();
        if (debug) {
          log.debug("borrarCarpetaIfills()::Document esborrat: " + fill.getName());
        }
      } else if (fill instanceof Folder) {
        borrarCarpetaIfills((Folder) fill);
      }
    }

    carpeta.delete();
    if (debug) {
      log.debug("borrarCarpetaIfills()::Document borrat: " + carpeta.getName());
    }
  }

  public void borrarCarpeta(String rutaCarpeta, String custodyId) {
    borrarCarpeta(rutaCarpeta, true, custodyId);
  }

  /**
   * Imprimeix la informacio basica (tipus i nom) de carpetes y subcarpetes de
   * sistema que penjen de la carpeta root que retorna el metode
   * cmisSession.getRootFolrder() Els documents que es pujen a Alfresco via Web
   * es guarden dins /Sites/{nom-del-site}/documentLibrary
   */
  public void printRootFolderItems() {

    Folder folder = (Folder) getCmisSession().getRootFolder();
    ItemIterable<CmisObject> children = folder.getChildren();

    for (CmisObject chind : children) {

      System.out.println("[" + chind.getType().getId() + "] : " + chind.getName());

      if (chind.getName().equals("Sites")) {

        Folder sitios = (Folder) chind;

        ItemIterable<CmisObject> childrenSitios = sitios.getChildren();

        for (CmisObject chisd : childrenSitios) {

          System.out.println("	- [" + chind.getType().getId() + "] : " + chisd.getName());

          // if (chisd.getName().equals("registro-regweb")) {

          Folder regWeb = (Folder) chisd;
          ItemIterable<CmisObject> childrenregWeb = regWeb.getChildren();

          for (CmisObject chidrw : childrenregWeb) {

            System.out.println("		- [" + chind.getType().getId() + "] : " + chidrw.getName());

            if (chidrw.getName().equals("documentLibrary")) {

              Folder regWeb_dl = (Folder) chidrw;
              ItemIterable<CmisObject> childrenDocsLib = regWeb_dl.getChildren();

              for (CmisObject chiddl : childrenDocsLib) {

                System.out.println("		- [" + chiddl.getType().getId() + "] : "
                    + chiddl.getName());
              }
            }

          }
          // }
        }
      }
    }
  }

  /**
   * Recupera l´objecte Folder corresponent a la carpeta pare de on penja tots
   * els documents i resta de carpetes.
   */
  public Folder getBaseDocsFolder(String custodyId) throws Exception {
    return (Folder) getCmisSession().getObjectByPath(getPathCarpetaDocs(custodyId, ""));
  }

  /**
   * Crea un document dins la ruta indicada, amb data de l'annex i amb les
   * propietats indicades
   * 
   * @param document
   *          Conte el array de bites del fitxer final.
   * @param fileName
   *          Nom del fitxer
   * @param path
   *          Ruta a on es creará el document
   * @param properties
   *          Metadades del document
   * @return El document creat o null si no s´ha pogut crear.
   */
  public String crearDocument(AnnexCustody document, String fileName, String path,
      Map<String, Object> properties, String custodyId) throws Exception {

    // Session cmisSession = getCmisSession();
    Folder parentFolder = null;
    Document documentCreat = null;

    try {
      if (path != null && !"".equals(path)) {
        if (!path.startsWith("/")) {
          path = "/" + path;
        }
      } else {
        path = "";
      }
      parentFolder = (Folder) getCmisSession().getObjectByPath(
          getPathCarpetaDocs(custodyId, path));
    } catch (CmisObjectNotFoundException onfEx) {
      // Si la carpeta proposada no existeix, es creará la ruta necessaria
      parentFolder = crearRutaDeCarpetes(path, custodyId);
    }

    if (parentFolder != null) {
      // contingut
      String mime = document.getMime();
      if (mime == null) {
        mime = "application/octet-stream";
      }

      byte[] content = document.getData();
      InputStream stream = new ByteArrayInputStream(content);
      ContentStream contentStream = new ContentStreamImpl(fileName,
          BigInteger.valueOf(content.length), mime, stream);

      // versionat
      documentCreat = parentFolder.createDocument(properties, contentStream,
          VersioningState.MAJOR);

      // Document docRecenCreat = getDocument(path+"/"+fileName);
      // printMetadataCMISobject(docRecenCreat, ExtensionLevel.PROPERTIES);
    }

    return documentCreat.getId();
  }

  public String crearDocument(AnnexCustody document, String fileName, String path,
      String custodyId) throws Exception {

    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put(PropertyIds.OBJECT_TYPE_ID, CMIS_DOCUMENT_TYPE);
    properties.put(PropertyIds.NAME, fileName);

    return crearDocument(document, fileName, path, properties, custodyId);
  }

  public String crearDocument(AnnexCustody document, String fileName,
      Map<String, Object> properties, String custodyId) throws Exception {

    return crearDocument(document, fileName, "", properties, custodyId);
  }

  public String crearDocument(AnnexCustody document, String fileName, String custodyId)
      throws Exception {

    // Map<String, Object> properties = new HashMap<String, Object>();
    // properties.put(PropertyIds.OBJECT_TYPE_ID, CMIS_DOCUMENT_TYPE);
    // properties.put(PropertyIds.OBJECT_ID, "1234567890");
    // properties.put(PropertyIds.NAME, fileName);

    // TODO HOLA

    Map<String, Object> properties = new HashMap<String, Object>();
    // properties.put(PropertyIds.OBJECT_TYPE_ID, "D:APBRegistro:anexo");
    properties.put(PropertyIds.OBJECT_TYPE_ID, "D:APBRegistro:anexo,P:APBRegistro:d_anexo");
    properties.put(PropertyIds.NAME, fileName);
    properties.put("APBRegistro:dr_obsAlfresco", "HOLA");

    return crearDocument(document, fileName, "", properties, custodyId);
  }

  public void borrarDocument(String path, String custodyId) {
    if (path != null && !"".equals(path)) {
      if (!path.startsWith("/")) {
        path = "/" + path;
      }
    } else {
      path = "";
    }

    try {
      Session cmisSession = getCmisSession();
      String rutaCompleta = getPathCarpetaDocs(custodyId, path);
      Document docAborrar = (Document) cmisSession.getObjectByPath(rutaCompleta);
      docAborrar.delete();
    } catch (CmisObjectNotFoundException onfe) {
      if (log.isDebugEnabled()) {
        final String msg = "El document " + path + " no existeix.";
        log.warn(msg, onfe);
      }
    } catch (ClassCastException cce) {
      log.error("El document " + path + " no es de tipus document.", cce);
    } catch (Exception ex) {
      log.error("Error al intentar borrar el document: " + path, ex);
    }
  }

  /**
   * Recupera un document del repositori Alfresco donat una ruta
   */
  public Document getDocument(String path, String custodyId) throws Exception {
    // Session cmisSession = getCmisSession();
    if (path != null && !"".equals(path)) {
      if (!path.startsWith("/")) {
        path = "/" + path;
      }
    } else {
      path = "";
    }

    final boolean debug = log.isDebugEnabled();

    if (debug) {
      log.debug(" OpenCMIS.getDocument().path = " + path);
    }

    String fullPath = getPathCarpetaDocs(custodyId, path);

    if (debug) {
      log.debug(" OpenCMIS.getDocument().fullPath = " + fullPath);
    }

    return (Document) getCmisSession().getObjectByPath(fullPath);
  }

  /**
   * Recupera tots els documents de una custodia si se li passa nomes el
   * custodyID Si se li afegeix el sufixe "D" o "S", recuperará nomes el
   * document o la firma respectivament
   */
  public List<Document> getDocumentById(String id) {

    Session cmisSession = getCmisSession();
    List<Document> docs = new ArrayList<Document>();

    ObjectType type = cmisSession.getTypeDefinition(CMIS_DOCUMENT_TYPE);
    PropertyDefinition<?> objectIdPropDef = type.getPropertyDefinitions().get(
        PropertyIds.OBJECT_ID);
    String objectIdQueryName = objectIdPropDef.getQueryName();

    String queryString = "SELECT " + objectIdQueryName + " FROM " + type.getQueryName()
        + " WHERE " + PropertyIds.DESCRIPTION + " LIKE '" + id + "%'";

    System.out.println(queryString);

    ItemIterable<QueryResult> results = cmisSession.query(queryString, false);

    for (QueryResult qResult : results) {
      String objectId = qResult.getPropertyValueByQueryName(objectIdQueryName);
      docs.add((Document) cmisSession.getObject(cmisSession.createObjectId(objectId)));
    }

    return docs;

  }

  /**
   * Afegeix una metadada al document o carpeta
   */
  public void addMetaDataToCMISObject(Folder parentFolder, Document doc, String nom,
      String valor) {

    List<CmisExtensionElement> extensions = doc.getExtensions(ExtensionLevel.PROPERTIES);

    for (CmisExtensionElement ext : extensions) {

      for (CmisExtensionElement child : ext.getChildren()) {

        if ("P:APBRegistro:d_anexo".equals(child.getValue())) {

          for (CmisExtensionElement p_child : child.getChildren()) {

            if (p_child.getAttributes().get("propertyDefinitionId").equals(nom)) {
              CmisExtensionElement newExt = new CmisExtensionElementImpl(
                  p_child.getNamespace(), p_child.getName(), p_child.getAttributes(), valor);
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
   * Imprimeix informacion de les metadades o extensions de un objecte CMIS
   */
  public void printMetadataCMISobject(Folder docmnt, ExtensionLevel eLvl) {

    if (eLvl != null) {

      List<CmisExtensionElement> extensions = docmnt.getExtensions(eLvl);

      System.out.println("#### IMPRIMIENDO EXTENSIONES DEL NIVEL " + eLvl.value() + " ####");

      if (extensions != null) {

        for (CmisExtensionElement ext : extensions) {

          System.out.println("Extension --> Name: " + ext.getName());
          System.out.println("Extension --> NameSpace: " + ext.getNamespace());
          System.out.println("Extension --> Value: " + ext.getValue());

          for (CmisExtensionElement child : ext.getChildren()) {

            System.out.println(" - Extension Nvl2 --> Clild Name: " + child.getName());
            System.out.println(" - Extension Nvl2 --> Clild NS: " + child.getNamespace());
            System.out.println(" - Extension Nvl2 --> Clild Value: " + child.getValue());

            for (CmisExtensionElement p_child : child.getChildren()) {

              System.out.println(" 	- Property --> Clild Name: " + p_child.getName());
              System.out.println(" 	- Property --> Clild NS: " + p_child.getNamespace());
              System.out.println(" 	- Property --> Clild Value: " + p_child.getValue());

              Iterator<String> attribs = p_child.getAttributes().keySet().iterator();
              while (attribs.hasNext()) {
                String att_key = attribs.next();
                System.out.println(" 		- Attribute --> " + att_key + "="
                    + p_child.getAttributes().get(att_key));
              }
            }
          }
        }
      }

      System.out.println("#### FIN EXTENSIONES DEL NIVEL " + eLvl.value() + " ####");
    }
  }

  public void setObjectProperties(CmisObject object) {
    // some dummy data
    String typeId = "MyType";
    String objectId = "1111-2222-3333";
    String name = "MyDocument";

    // find a namespace for the extensions that is different from the CMIS
    // namespaces
    String ns = "http://apache.org/opencmis/example";

    // create a list for the first level of our extension
    List<CmisExtensionElement> extElements = new ArrayList<CmisExtensionElement>();

    // set up an attribute (Avoid attributes! They will not work with the JSON
    // binding!)
    Map<String, String> attr = new HashMap<String, String>();
    attr.put("type", typeId);

    // add two leafs to the extension
    extElements.add(new CmisExtensionElementImpl(ns, "objectId", attr, objectId));
    extElements.add(new CmisExtensionElementImpl(ns, "name", null, name));

    // set the extension list
    List<CmisExtensionElement> extensions = new ArrayList<CmisExtensionElement>();
    extensions.add(new CmisExtensionElementImpl(ns, "exampleExtension", null, extElements));
    // object.setExtensions(extensions);
  }

  // --------------------------------

  public String crearDocument(byte[] data, String fileName, String path,
      Map<String, Object> properties, String custodyId) throws Exception {

    // Session cmisSession = getCmisSession();
    Folder parentFolder = null;
    Document documentCreat = null;
    final boolean debug = log.isDebugEnabled();


    try {
      if (path != null && !"".equals(path)) {
        if (!path.startsWith("/")) {
          path = "/" + path;
        }
      } else {
        path = "";
      }

      String pathAdaptat = getPathCarpetaDocs(custodyId, path);


      if (debug) {
        log.debug("CrearDocument = PATH ADAPTAT = " + pathAdaptat);
      }

      parentFolder = (Folder) getCmisSession().getObjectByPath(pathAdaptat);
    } catch (CmisObjectNotFoundException onfEx) {
      // Si la carpeta proposada no existeix, es creará la ruta necessaria
      parentFolder = crearRutaDeCarpetes(path, custodyId);
    }
   
    if (debug) {
      log.debug("CrearDocument()::parentFolder isNULL = " + parentFolder);
    }

    if (parentFolder != null) {
      
      if (debug) {
        log.debug("CrearDocument()::parentFolder getPath() = " + parentFolder.getPath());
        log.debug("CrearDocument()::parentFolder getName() = " + parentFolder.getPath());
      }
      
      // contingut
      String mime = "application/octet-stream";

      InputStream stream = new ByteArrayInputStream(data);
      ContentStream contentStream = new ContentStreamImpl(fileName,
          BigInteger.valueOf(data.length), mime, stream);

      // versionat
      documentCreat = parentFolder.createDocument(properties, contentStream,
          VersioningState.MAJOR);

      // Document docRecenCreat = getDocument(path+"/"+fileName);
      // printMetadataCMISobject(docRecenCreat, ExtensionLevel.PROPERTIES);
    }

    return documentCreat.getId();
  }

  private Session cmisSession2;

  public Session getCmisSession() {

    if (cmisSession2 == null) {

      SessionFactory factory = SessionFactoryImpl.newInstance();
      Map<String, String> parameter = new HashMap<String, String>();
      String methodAccess = alfresco.getAccessMethod();

      if ("ATOM".equals(methodAccess)) {

        parameter.put(SessionParameter.ATOMPUB_URL, alfresco.getAlfrescoUrl());
        parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
        parameter.put(SessionParameter.AUTH_HTTP_BASIC, "true");
        parameter.put(SessionParameter.USER, alfresco.getUsername());
        parameter.put(SessionParameter.PASSWORD, alfresco.getPassword());
        parameter.put(SessionParameter.OBJECT_FACTORY_CLASS,
            "org.alfresco.cmis.client.impl.AlfrescoObjectFactoryImpl");
        parameter.put(SessionParameter.REPOSITORY_ID, alfresco.getRepositoryID());

        List<Repository> repositories = factory.getRepositories(parameter);
        final boolean debug = log.isDebugEnabled();

        if (debug) {
          log.debug("Repositoris: " + repositories);
          if (repositories != null) {
            log.debug("Repositoris.size(): " + repositories.size());
          }
        }

        cmisSession2 = repositories.get(0).createSession();

      } else if ("WS".equals(methodAccess)) {

        parameter.put(SessionParameter.USER, alfresco.getUsername());
        parameter.put(SessionParameter.PASSWORD, alfresco.getPassword());
        parameter.put(SessionParameter.BINDING_TYPE, BindingType.WEBSERVICES.value());
        parameter.put(SessionParameter.REPOSITORY_ID, alfresco.getRepositoryID());
        parameter.put(SessionParameter.WEBSERVICES_PORT_PROVIDER_CLASS,
            CXFPortProvider.class.getName());
        parameter.put(SessionParameter.OBJECT_FACTORY_CLASS,
            "org.alfresco.cmis.client.impl.AlfrescoObjectFactoryImpl");
        // parameter.put(SessionParameter.WEBSERVICES_JAXWS_IMPL, "sunjre");
        parameter.put(SessionParameter.WEBSERVICES_ACL_SERVICE, alfresco.getAlfrescoUrl()
            + "/ACLService?WSDL");
        parameter.put(SessionParameter.WEBSERVICES_DISCOVERY_SERVICE,
            alfresco.getAlfrescoUrl() + "/DiscoveryService?WSDL");
        parameter.put(SessionParameter.WEBSERVICES_MULTIFILING_SERVICE,
            alfresco.getAlfrescoUrl() + "/MultiFilingService?WSDL");
        parameter.put(SessionParameter.WEBSERVICES_NAVIGATION_SERVICE,
            alfresco.getAlfrescoUrl() + "/NavigationService?WSDL");
        parameter.put(SessionParameter.WEBSERVICES_OBJECT_SERVICE, alfresco.getAlfrescoUrl()
            + "/ObjectService?WSDL");
        parameter.put(SessionParameter.WEBSERVICES_POLICY_SERVICE, alfresco.getAlfrescoUrl()
            + "/PolicyService?WSDL");
        parameter.put(SessionParameter.WEBSERVICES_RELATIONSHIP_SERVICE,
            alfresco.getAlfrescoUrl() + "/RelationshipService?WSDL");
        parameter.put(SessionParameter.WEBSERVICES_REPOSITORY_SERVICE,
            alfresco.getAlfrescoUrl() + "/RepositoryService?WSDL");
        parameter.put(SessionParameter.WEBSERVICES_VERSIONING_SERVICE,
            alfresco.getAlfrescoUrl() + "/VersioningService?WSDL");
        parameter.put(SessionParameter.LOCALE_ISO3166_COUNTRY, "ES");
        parameter.put(SessionParameter.LOCALE_ISO639_LANGUAGE, "es");
        parameter.put(SessionParameter.LOCALE_VARIANT, "");

        /*
         * List<Repository> repositories = factory.getRepositories(parameter);
         * for (Repository r : repositories) {
         * System.out.println("Found repository: " + r.getName()); }
         */

        cmisSession2 = factory.createSession(parameter);
      }

    }

    return cmisSession2;
  }

  public static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

  private HttpRequestFactory requestFactory;

  public HttpRequestFactory getRequestFactory() {
    if (requestFactory == null) {
      requestFactory = HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
        public void initialize(HttpRequest request) throws IOException {
          request.setParser(new JsonObjectParser(new JacksonFactory()));
          request.getHeaders().setBasicAuthentication(alfresco.getUsername(),
              alfresco.getPassword());
        }
      });
    }
    return requestFactory;
  }

  public static byte[] getCmisObjectContent(Document cmisDoc) throws IOException {
    if (cmisDoc != null) {
      ContentStream cs = cmisDoc.getContentStream();
      return IOUtils.toByteArray(cs.getStream());
    } else {
      return null;
    }
  }

}
