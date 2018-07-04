package org.fundaciobit.plugins.documentcustody.filesystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import junit.framework.Assert;

import org.fundaciobit.plugins.documentcustody.api.AbstractDocumentCustodyPlugin;
import org.fundaciobit.plugins.documentcustody.api.AnnexCustody;
import org.fundaciobit.plugins.documentcustody.api.CustodyException;
import org.fundaciobit.plugins.documentcustody.api.IDocumentCustodyPlugin;
import org.fundaciobit.plugins.documentcustody.api.test.TestDocumentCustody;
import org.fundaciobit.plugins.utils.FileUtils;
import org.fundaciobit.plugins.utils.PluginsManager;

/**
 * 
 * @author anadal
 *
 */
public class TestFileSystemCustody extends TestDocumentCustody {

  public static final String packageBase = "es.caib.portafib.";

  public static final String propertyBase = packageBase
      + FileSystemDocumentCustodyPlugin.FILESYSTEM_PROPERTY_BASE;

  // @org.junit.Test
  public void testHash() throws Exception {

    /*
     * MD2 MD5 SHA SHA-256 SHA-384 SHA-512
     */

    String algorithm = "MD5"; // "SHA-512"); //"MD5");

    String dades = "dades a \\à encriptar";
    String password = "pwd";
    for (int i = 1; i < 10; i++) {
      String result = FileSystemDocumentCustodyPlugin.generateHash(dades, "MD5", password + i);

      System.out.println(" MD5: (" + i + "): " + result);
      if (!URLEncoder.encode(result, "utf-8").equals(result)) {
        break;
      }
    }
    System.out.println(" MD5: "
        + FileSystemDocumentCustodyPlugin.generateHash(dades, algorithm, password));

  }

  /**
   * Revisa si versions anteriors són compatibles amb el mòdul actual.
   * 
   * @throws Exception
   */
  @org.junit.Test
  public void testRetro() throws Exception {

    final String custodyID = "624839770629068";

    File baseDir = new File("./testReposOldVersion");

    String[][] filesToReset = new String[][] { { "cust_624839770629068.DOCINFO", "0" },
        { "cust_624839770629068.SIGNINFO", "1" },
        { "cust_624839770629068_624839855282562.ANNEXINFO", "2" },
        { "cust_624839770629068_624840362780564.ANNEXINFO", "2" } };

    // Check SIZE
    Properties specificProperties = new Properties();

    specificProperties.setProperty(packageBase
        + FileSystemDocumentCustodyPlugin.FILESYSTEM_PROPERTY_BASEDIR,
        baseDir.getAbsolutePath());
    IDocumentCustodyPlugin documentCustodyPlugin = instantiateDocumentCustodyPlugin(specificProperties);

    for (int i = 0; i < filesToReset.length; i++) {

      String[] dades = filesToReset[i];

      String file = dades[0];

      // Restauram el fitxer original
      File oldInfo = new File(baseDir, file);
      oldInfo.delete();

      File originalInfo = new File(baseDir, file + "_BACKUP");

      OutputStream dest = new FileOutputStream(oldInfo);
      FileUtils.copy(new FileInputStream(originalInfo), dest);

      // Check de tamany
      int tipus = Integer.parseInt(dades[1]);
      AnnexCustody doc;
      int expectedSize;
      switch (tipus) {

      case 0:
        doc = documentCustodyPlugin.getDocumentInfoOnly(custodyID);
        expectedSize = 12;
        break;

      case 1:
        doc = documentCustodyPlugin.getSignatureInfoOnly(custodyID);
        expectedSize = 11;
        break;

      case 2:
        String annexID = file.substring(file.indexOf('_') + 1, file.lastIndexOf('.'));
        doc = documentCustodyPlugin.getAnnexInfoOnly(custodyID, annexID);
        expectedSize = 18;
        break;

      default:
        throw new Exception("tipus desconegut " + dades[1]);

      }

      System.out.println(" Size = " + doc.getLength());

      if (doc.getLength() != expectedSize) {
        Assert.fail("El tamany del fitxer " + file + " no correspon amb l'esperat ("
            + doc.getLength() + " != " + expectedSize + ")");
      }

    }

  }

  @org.junit.Test
  public void testFull() throws Exception {

    // IDocumentCustodyPlugin documentCustodyPlugin = initialize(null);

    Map<String, Object> custodyParameters = new HashMap<String, Object>();

    Properties specificProperties = new Properties();

    internalTestGeneralDocumentCustody(specificProperties, custodyParameters, true);

  }

  @org.junit.Test
  public void testFolderFromCustodyParameters() throws Exception {

    File baseDir = new File("./testReposWithFolder");
    baseDir.mkdirs();

    Properties specificProperties = new Properties();

    specificProperties.setProperty(packageBase
        + FileSystemDocumentCustodyPlugin.FILESYSTEM_PROPERTY_BASEDIR,
        baseDir.getAbsolutePath());

    boolean deleteOnFinish = true; // true;

    internalTestFolderFromCustodyParameters(specificProperties, deleteOnFinish);

  }

  @org.junit.Test
  public void testAutomaticMetadatas() throws Exception {

    File baseDir = new File("./testAutomaticMetadatas");
    baseDir.mkdirs();

    Properties specificProperties = new Properties();

    specificProperties.setProperty(packageBase
        + FileSystemDocumentCustodyPlugin.FILESYSTEM_PROPERTY_BASEDIR,
        baseDir.getAbsolutePath());

    final boolean deleteOnFinish = true;

    internalTestAutomaticMetadatas(specificProperties, deleteOnFinish);

  }

  @Override
  public IDocumentCustodyPlugin instantiateDocumentCustodyPlugin(Properties specificProperties)
      throws CustodyException {

    Properties fsProperties = new Properties();

    File f = new File("./testRepos");
    f.mkdirs();

    // Ficar propietats AbstractDocumentCustody
    fsProperties.setProperty(packageBase
        + FileSystemDocumentCustodyPlugin.FILESYSTEM_PROPERTY_BASEDIR, f.getAbsolutePath());

    fsProperties.setProperty(propertyBase + AbstractDocumentCustodyPlugin.ABSTRACT_PREFIX,
        "cust");

    if (specificProperties != null) {
      fsProperties.putAll(specificProperties);
    }

    IDocumentCustodyPlugin documentCustodyPlugin;
    documentCustodyPlugin = (IDocumentCustodyPlugin) PluginsManager.instancePluginByClass(
        FileSystemDocumentCustodyPlugin.class, packageBase, fsProperties);
    return documentCustodyPlugin;
  }

  public static void main(String[] args) {
    try {

      System.out.println(FileSystemDocumentCustodyPlugin.class.getCanonicalName());

      TestFileSystemCustody tester = new TestFileSystemCustody();

      //tester.testAutomaticMetadatas();

      //tester.testFull();

      tester.testFolderFromCustodyParameters();

      //tester.testRetro();

    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  @Override
  public String getPropertyBase() {
    return propertyBase;
  }

}
