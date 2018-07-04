package org.fundaciobit.plugins.documentcustody.filesystem;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import org.fundaciobit.plugins.documentcustody.api.AbstractDocumentCustodyPlugin;


/**
 * Implementació del plugin de custodia documental que guarda les signatures en
 * un fitxer. Si es defineix una URL base d'un servidor web, llavors es pot fer
 * que retorni la URL de validació.
 * 
 * @author anadal
 */
public class FileSystemDocumentCustodyPlugin extends AbstractDocumentCustodyPlugin {

  public static final String FILESYSTEM_PROPERTY_BASE = DOCUMENTCUSTODY_BASE_PROPERTY + "filesystem.";

  public static final String FILESYSTEM_PROPERTY_BASEDIR = FILESYSTEM_PROPERTY_BASE + "basedir";

  /**
   * 
   */
  public FileSystemDocumentCustodyPlugin() {
    super();
  }

  /**
   * @param propertyKeyBase
   * @param properties
   */
  public FileSystemDocumentCustodyPlugin(String propertyKeyBase, Properties properties) {
    super(propertyKeyBase, properties);
  }

  /**
   * @param propertyKeyBase
   */
  public FileSystemDocumentCustodyPlugin(String propertyKeyBase) {
    super(propertyKeyBase);
  }


  @Override
  protected String getPropertyBase() {
    return FILESYSTEM_PROPERTY_BASE;
  }

  
  private String getBaseDir() throws Exception  {
    return getPropertyRequired(FILESYSTEM_PROPERTY_BASEDIR );
  }


  @Override
  protected boolean existsFile(String custodyID, String relativePath) {
    try {
      File f = new File(getBaseDir(), relativePath);
      return f.exists();
    } catch (Exception e) {
      String msg = "Error descobrint si un fitxer existeix: " + e.getMessage(); 
      log.error(msg ,e);
      throw new RuntimeException(msg, e);
    }

  }


  @Override
  protected void deleteFile(String custodyID, String... relativePaths) {
    for (String path : relativePaths) {
      try {
        deleteFile(path);
      } catch (Exception e) {
        String msg = "Error intentant esborrar el fitxer " + path + ": " + e.getMessage(); 
        log.error(msg ,e);
        throw new RuntimeException(msg, e);
      }
    }
  }
  
  

  private void deleteFile(String relativePath) throws Exception {
    File f = new File(getBaseDir(), relativePath);
    if (f.exists()) {
      if (!f.delete()) {
        log.warn("No s'ha pogut esborrar fitxer " + f.getAbsolutePath(), new Exception());
        f.deleteOnExit();
      }
    }
  }
  

  @Override
  protected void writeFile(String custodyID, String relativePath, byte[] data)
      throws Exception {
    FileOutputStream fos = new FileOutputStream(new File(getBaseDir(), relativePath));
    fos.write(data);
    fos.close();
  }
  
  @Override
  protected void writeFileCreateParentDir(String custodyID, String relativePath, byte[] data)
      throws Exception {
    File f = new File(getBaseDir(), relativePath);
    File parent = f.getParentFile(); 
    if (!parent.exists()) {
      parent.mkdirs();
    }
    writeFile(custodyID, relativePath, data);
    
  }
  
  @Override
  protected byte[] readFile(String custodyID, String relativePath) throws Exception {
    
    File file = new File(getBaseDir(), relativePath);
    if (!file.exists()) {
      return null;
    }
    
    FileInputStream fis = new FileInputStream(file);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final byte[] buffer = new byte[10 * 1024]; // 10Kb
    int total;
    while((total = fis.read(buffer)) != -1) {
      baos.write(buffer, 0, total);
    }
    fis.close();
    return baos.toByteArray();
  }

  @Override
  protected long lengthFile(String custodyID, String relativePath) throws Exception {
    File file = new File(getBaseDir(), relativePath);
    if (!file.exists()) {
      return -1;
    }
    return file.length();
  }

}
