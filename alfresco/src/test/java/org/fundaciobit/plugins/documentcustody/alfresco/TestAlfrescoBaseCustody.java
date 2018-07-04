package org.fundaciobit.plugins.documentcustody.alfresco;

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import junit.framework.Assert;


import org.fundaciobit.plugins.documentcustody.alfresco.base.AlfrescoBaseDocumentCustodyPlugin;
import org.fundaciobit.plugins.documentcustody.api.AbstractDocumentCustodyPlugin;
import org.fundaciobit.plugins.documentcustody.api.AnnexCustody;
import org.fundaciobit.plugins.documentcustody.api.CustodyException;
import org.fundaciobit.plugins.documentcustody.api.DocumentCustody;
import org.fundaciobit.plugins.documentcustody.api.IDocumentCustodyPlugin;
import org.fundaciobit.plugins.documentcustody.api.test.InfoExecutionTest;
import org.fundaciobit.plugins.documentcustody.api.test.Registre;
import org.fundaciobit.plugins.documentcustody.api.test.TestDocumentCustody;
import org.fundaciobit.plugins.utils.PluginsManager;

/**
 *
 * @author anadal
 *
 */
public class TestAlfrescoBaseCustody extends TestDocumentCustody {

  public static final String packageBase = "es.caib.example.";

  public static final String propertyBase = packageBase
      + AlfrescoBaseDocumentCustodyPlugin.ALFRESCO_PROPERTY_BASE;

  @Override
  public IDocumentCustodyPlugin instantiateDocumentCustodyPlugin(Properties specificProperties)
      throws CustodyException {
    Properties alfrescoProperties = new Properties();

    try {
      alfrescoProperties.load(new FileInputStream("test.properties"));
    } catch (Exception e) {
      throw new CustodyException("Error llegint properties des de test.properties", e);
    }

    alfrescoProperties.putAll(specificProperties);

    // Ficar propietats ALFRESCO
    /*
     * TODO XYZ
     * 
     * final String propertyBase = packageBase +
     * AlfrescoDocumentCustodyPlugin.ALFRESCO_PROPERTY_BASE;
     * 
     * 
     * 
     * alfrescoProperties.setProperty(propertyBase + "url",
     * "http://localhost:9080/alfresco/api/-default-/public/cmis/versions/1.0/atom"
     * );
     * 
     * 
     * //workspace://SpacesStore/b886bad2-998d-4674-a120-1fcc2f1f533c
     * 
     * alfrescoProperties.setProperty(propertyBase + "repository",
     * "b886bad2-998d-4674-a120-1fcc2f1f533c"); //"USER_HOMES/anadal/test/");
     * 
     * 
     * 
     * alfrescoProperties.setProperty(propertyBase + "basepath","/test");
     * 
     * alfrescoProperties.setProperty(propertyBase + "site","ODES");
     * alfrescoProperties.setProperty(propertyBase + "access.user", "anadal");
     * 
     * 
     * alfrescoProperties.setProperty(propertyBase + "access.pass", "anadal");
     * 
     * // WS ATOM alfrescoProperties.setProperty(propertyBase + "access.method",
     * "ATOM");
     */

    IDocumentCustodyPlugin documentCustodyPlugin;
    documentCustodyPlugin = (IDocumentCustodyPlugin) PluginsManager.instancePluginByClass(
        AlfrescoBaseDocumentCustodyPlugin.class, packageBase, alfrescoProperties);

    return documentCustodyPlugin;
  }

  @Override
  public String getPropertyBase() {
    return propertyBase;
  }

  @org.junit.Test
  public void testRetro() throws Exception {

    final String custodyID = "606443123400344";

    // Check SIZE
    Properties specificProperties = new Properties();

    // specificProperties.setProperty(getPropertyBase() +
    // AbstractDocumentCustodyPlugin.
    //

    IDocumentCustodyPlugin documentCustodyPlugin = instantiateDocumentCustodyPlugin(specificProperties);

    for (int i = 0; i <= 2; i++) {

      // Check de tamany

      AnnexCustody doc = null;
      int expectedSize = -1;
      switch (i) {

      case 0:
        doc = documentCustodyPlugin.getDocumentInfoOnly(custodyID);
        expectedSize = 12;
        break;

      case 1:
        doc = documentCustodyPlugin.getSignatureInfoOnly(custodyID);
        expectedSize = 11;
        break;

      case 2:

        List<String> annexes = documentCustodyPlugin.getAllAnnexes(custodyID);

        System.out.println(" Annexes.size() = " + annexes.size());

        expectedSize = 18;

        for (String annexID : annexes) {
          doc = documentCustodyPlugin.getAnnexInfoOnly(custodyID, annexID);

          System.out.println(doc.getName() + " => " + doc.getLength());

          if (doc.getLength() != expectedSize) {
            Assert.fail("El tamany de l'annexe  " + doc.getName()
                + " no correspon amb l'esperat (" + doc.getLength() + " != " + expectedSize
                + ")");
          }
        }

        Assert.assertNotNull("No s'han trobat annexes !!!!", doc);

        break;

      default:
        throw new Exception("tipus desconegut " + i);

      }

      System.out.println(" Size = " + doc.getLength());

      if (doc.getLength() != expectedSize) {
        Assert.fail("El tamany del fitxer " + doc.getName() + " no correspon amb l'esperat ("
            + doc.getLength() + " != " + expectedSize + ")");
      }

    }

  }

  @org.junit.Test
  public void testFull() throws Exception {

    // IDocumentCustodyPlugin documentCustodyPlugin = initialize(null);

    Map<String, Object> custodyParameters = new HashMap<String, Object>();

    Properties specificProperties = new Properties();

    specificProperties.setProperty(packageBase
        + AlfrescoBaseDocumentCustodyPlugin.ALFRESCO_BASE_PATH, "/testFull");

    InfoExecutionTest iet;
    iet = internalTestGeneralDocumentCustody(specificProperties, custodyParameters, true);

    System.out.println(" CUSTODYID = " + iet.getCustodyID());

  }

  @org.junit.Test
  public void testFolderFromCustodyParameters() throws Exception {

    File baseDir = new File("./testReposWithFolder");
    baseDir.mkdirs();

    Properties specificProperties = new Properties();

    specificProperties.setProperty(packageBase
        + AlfrescoBaseDocumentCustodyPlugin.ALFRESCO_BASE_PATH, "/testReposWithFolder");

    boolean deleteOnFinish = true; // true;

    internalTestFolderFromCustodyParameters(specificProperties, deleteOnFinish);

  }

  public void testSimpleDoc() throws Exception {

    Properties specificProperties = new Properties();

    specificProperties.setProperty(packageBase
        + AlfrescoBaseDocumentCustodyPlugin.ALFRESCO_BASE_PATH, "/testSimpleDoc");

    final String folder = "${registre.data?string[\"yyyy/MM/dd\"]}/${registre.id}/";
    specificProperties.setProperty(getPropertyBase()
        + AbstractDocumentCustodyPlugin.ABSTRACT_FOLDER_EXPRESSION_LANGUAGE, folder);

    Registre registre = new Registre("155", new Date(167526000000L));

    Map<String, Object> custodyParameters = new HashMap<String, Object>();

    custodyParameters.put("registre", registre);

    IDocumentCustodyPlugin documentCustodyPlugin = instantiateDocumentCustodyPlugin(specificProperties);

    // OpenCmisAlfrescoHelper openCmisAlfrescoHelper =
    // ((AlfrescoBaseDocumentCustodyPlugin)documentCustodyPlugin).getAlfresco();

    // /testReposWithFolder/1975/04/24/155/1490697246388509314447643676/
    // openCmisAlfrescoHelper.crearRutaDeCarpetes("/1975/04/24/155",
    // "1490697246388509314447643676");

    // Reserve ID

    final String custodyID = documentCustodyPlugin.reserveCustodyID(custodyParameters);

    // SAVE DOCUMENT
    DocumentCustody doc = new DocumentCustody();
    doc.setName("holacaracola.txt");
    doc.setData("holacaracola".getBytes());

    documentCustodyPlugin.saveDocument(custodyID, custodyParameters, doc);
    
    
    doc.setData("holacaracola_v2.0".getBytes());
    documentCustodyPlugin.saveDocument(custodyID, custodyParameters, doc);

    DocumentCustody docInfo = documentCustodyPlugin.getDocumentInfo(custodyID);

    if (docInfo == null) {
      Assert.fail("No pot llegir document info");
    }

    System.out.println("Tamany Info = " + docInfo.getLength());

    byte[] data = documentCustodyPlugin.getDocument(custodyID);
    if (data == null) {
      Assert.fail("No pot llegir document byte []");
    }

    System.out.println("Tamany Info = " + docInfo.getLength());
    System.out.println("Tamany byte[] = " + data.length);

    documentCustodyPlugin.deleteCustody(custodyID);

  }

  @org.junit.Test
  public void testAutomaticMetadatas() throws Exception {

    Properties specificProperties = new Properties();
    specificProperties.setProperty(packageBase
        + AlfrescoBaseDocumentCustodyPlugin.ALFRESCO_BASE_PATH, "/testAutomaticMetadatas");

    final boolean deleteOnFinish = true;

    internalTestAutomaticMetadatas(specificProperties, deleteOnFinish);

  }

  public static void main(String[] args) {
    try {

      System.out.println(AlfrescoBaseDocumentCustodyPlugin.class.getCanonicalName());

      // org.apache.log4j.LogManager.getRootLogger().setLevel(org.apache.log4j.Level.DEBUG);

      TestAlfrescoBaseCustody tester = new TestAlfrescoBaseCustody();

      
      tester.testFull();

      tester.testRetro();

      tester.testFolderFromCustodyParameters();
      

      tester.testSimpleDoc();

      tester.testAutomaticMetadatas();

      /*
       * 
       * TestAlfrescoBaseCustody test = new TestAlfrescoBaseCustody(); boolean
       * deleteOnFinish = false; test.testDocumentCustody(documentCustodyPlugin,
       * deleteOnFinish);
       * 
       * 
       * // Que passa quan intentam esborrar String custodyID =
       * "13221341235415"; // No existeix
       * documentCustodyPlugin.deleteDocument(custodyID);
       * 
       * documentCustodyPlugin.deleteCustody(custodyID);
       * documentCustodyPlugin.getSignatureInfoOnly(custodyID);
       */

      System.out.println(" --- FINAL  ---");

    } catch (Exception e) {
      e.printStackTrace();
    }

  }

}
