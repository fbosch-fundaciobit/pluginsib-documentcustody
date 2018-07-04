package org.fundaciobit.plugins.documentcustody.alfresco;

import java.beans.XMLDecoder;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.fundaciobit.plugins.documentcustody.api.DocumentCustody;
import org.fundaciobit.plugins.documentcustody.api.IDocumentCustodyPlugin;
import org.fundaciobit.plugins.utils.PluginsManager;

/**
 * @author Limit
 * @author anadal (Adaptar a DocumentCustody 3.0.0)
 *
 */
public class TestAlfrescoApbCustody {

  
  public static void main(String[] args) {
    try {
      
      System.out.println(AlfrescoDocumentCustodyPlugin.class.getCanonicalName());

      final String packageBase = "es.caib.example.";

      Properties alfrescoProperties = new Properties();

      alfrescoProperties.load(new FileInputStream("test.properties"));

      //File f = new File("./testRepos");
      //f.mkdirs();

      IDocumentCustodyPlugin documentCustodyPlugin;
      documentCustodyPlugin = (IDocumentCustodyPlugin)PluginsManager.instancePluginByClass(AlfrescoDocumentCustodyPlugin.class, packageBase, alfrescoProperties);
      
      
      DocumentCustody doc = new DocumentCustody();
      doc.setName("holacaracola.txt");
      doc.setData("holacaracola".getBytes());
      
      
      ClassLoader classLoader = new TestAlfrescoApbCustody().getClass().getClassLoader();
      InputStream is = classLoader.getResource("registre.xml").openStream();
      if (is == null) {
        System.out.println(" No trob fitxer registre.xml");
        return;
      }
      //ByteArrayInputStream stream=new ByteArrayInputStream(registreXML.getBytes("UTF-8"));
      XMLDecoder xmlDec=new XMLDecoder(is);
      //List<Object> out = new ArrayList<Object>();
      
      Map<String, Object> custodyParameters = new HashMap<String, Object>();
      
      boolean seguentObjecte = true;
      int count = 0; 
      while (seguentObjecte) {
        try {
          Object object=xmlDec.readObject();
          
          String name;
          if (count == 0) {
            name = "registro";
          } else if (count == 1) {
            name = "anexo";
          } else {
            name = "object_" + count;
          }
          count++;
          custodyParameters.put(name, object);
        }catch (Exception objExc) {
          seguentObjecte = false;
        }
      }

      xmlDec.close();

      
      //String custodyParameters = IOUtils.toString(is, "utf-8");
      
      String custodyID = documentCustodyPlugin.reserveCustodyID(custodyParameters);
      
      documentCustodyPlugin.saveDocument(custodyID, custodyParameters, doc);

    } catch (Exception e) {
      e.printStackTrace();
    }
    
    
  }
}
