package org.fundaciobit.plugins.documentcustody.arxiudigitalcaib.test;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import junit.framework.Assert;

import org.fundaciobit.plugins.documentcustody.api.AnnexCustody;
import org.fundaciobit.plugins.documentcustody.api.CustodyException;
import org.fundaciobit.plugins.documentcustody.api.DocumentCustody;
import org.fundaciobit.plugins.documentcustody.api.IDocumentCustodyPlugin;
import org.fundaciobit.plugins.documentcustody.api.SignatureCustody;
import org.fundaciobit.plugins.documentcustody.api.test.TestDocumentCustody;
import org.fundaciobit.plugins.documentcustody.arxiudigitalcaib.ArxiuDigitalCAIBDocumentCustodyPlugin;
import org.fundaciobit.plugins.documentcustody.arxiudigitalcaib.ExpedientCarpetaDocument;
import org.fundaciobit.plugins.documentcustody.arxiudigitalcaib.test.beans.Anexo;
import org.fundaciobit.plugins.documentcustody.arxiudigitalcaib.test.beans.Interesado;
import org.fundaciobit.plugins.documentcustody.arxiudigitalcaib.test.beans.Libro;
import org.fundaciobit.plugins.documentcustody.arxiudigitalcaib.test.beans.Oficina;
import org.fundaciobit.plugins.documentcustody.arxiudigitalcaib.test.beans.Organismo;
import org.fundaciobit.plugins.documentcustody.arxiudigitalcaib.test.beans.RegistroDetalle;
import org.fundaciobit.plugins.documentcustody.arxiudigitalcaib.test.beans.RegistroEntrada;
import org.fundaciobit.plugins.documentcustody.arxiudigitalcaib.test.beans.TipoDocumental;
import org.fundaciobit.plugins.documentcustody.arxiudigitalcaib.test.beans.Usuario;
import org.fundaciobit.plugins.documentcustody.arxiudigitalcaib.test.beans.UsuarioEntidad;
import org.fundaciobit.plugins.utils.FileUtils;
import org.fundaciobit.plugins.utils.Metadata;
import org.fundaciobit.plugins.utils.MetadataConstants;
import org.fundaciobit.plugins.utils.PluginsManager;

import es.caib.arxiudigital.apirest.ApiArchivoDigital;
import es.caib.arxiudigital.apirest.constantes.Aspectos;
import es.caib.arxiudigital.apirest.facade.pojos.Documento;
import es.caib.arxiudigital.apirest.facade.pojos.Expediente;
import es.caib.arxiudigital.apirest.facade.resultados.Resultado;
import es.caib.arxiudigital.apirest.facade.resultados.ResultadoSimple;

/**
 * 
 * @author anadal
 *
 */
public class TestArxiuDigitalCAIBDocumentCustody extends TestDocumentCustody {

  SimpleDateFormat SDF = new SimpleDateFormat("MMdd");
  
  public static Scanner scan=new Scanner(System.in);

  public static void waitForEnter() {
       System.out.print("Press any key to continue . . ." );
       scan.nextLine();
  }

  public static void main(String[] args) {
    try {

      System.out.println(ArxiuDigitalCAIBDocumentCustodyPlugin.class.getCanonicalName());

      TestArxiuDigitalCAIBDocumentCustody tester = new TestArxiuDigitalCAIBDocumentCustody();
      
      //tester.testInternalMetadata();

      tester.test3Combinacions();
     
      //tester.testSimpleDoc();

      // tester.testFull();

      //tester.testMetadades();

    } catch (Exception e) {
      e.printStackTrace();
    }

  }
  
  
 
  

  protected void compareDocument(String titol, AnnexCustody docSet, AnnexCustody docGet,
      boolean compareData) {

    if (docSet == null && docGet == null) {
      return; // OK
    }

    if ((docSet == null && docGet != null) || (docSet != null && docGet == null)) {
      Assert.assertNull(titol + ".- Algun dels dos valors val null i l'altre no: [SET = "
          + docSet + "][GET = " + docGet + "]");
    }

    Assert.assertEquals(titol + " Els noms no són iguals", docSet.getName(), docGet.getName());

    Assert.assertEquals(titol + " Les longituds no són iguals", docSet.getLength(),
        docGet.getLength());

    Assert
        .assertEquals(titol + " Els mimes no són iguals", docSet.getMime(), docGet.getMime());

    Assert
        .assertEquals(titol + " Els mimes no són iguals", docSet.getMime(), docGet.getMime());

    if (compareData) {
      if (!Arrays.equals(docSet.getData(), docGet.getData())) {
        Assert.fail(titol + " El contingut no es igual");
      }
    } else {
      // Comprovar que la dades del get valen NULL
      Assert.assertNull(titol
          + ".- Les dades del fitxer haurien de ser NUll i valen alguna cosa",
          docGet.getData());
    }
  }

  public void testMetadades() throws Exception {

    Properties specificProperties = new Properties();
    IDocumentCustodyPlugin documentCustodyPlugin = instantiateDocumentCustodyPlugin(specificProperties);
    ArxiuDigitalCAIBDocumentCustodyPlugin plugin;
    plugin = (ArxiuDigitalCAIBDocumentCustodyPlugin) documentCustodyPlugin;

    if (!plugin.supportsMetadata()) {
      System.err.print("Aquest sistema de custòdia no suporta metadades");
      return;
    }


    {
      File f = new File("expedient.txt");
      if (f.exists()) {
        String custodyID = new String(FileUtils.readFromFile(f));

        documentCustodyPlugin.deleteCustody(custodyID);

      }
    }

    Map<String, Object> custodyParameters = new HashMap<String, Object>();

    RegistroEntrada registro = getRegistro();
    custodyParameters.put("registro", registro);
    Anexo anexo = getAnexo(registro);
    custodyParameters.put("anexo", anexo);

    String custodyID = documentCustodyPlugin.reserveCustodyID(custodyParameters);

    // XYZ ZZZ Eliminar
    FileOutputStream fos = new FileOutputStream(new File("expedient.txt"));
    fos.write(custodyID.getBytes());
    fos.close();

    DocumentCustody document = null;
    /*
     * document = new DocumentCustody(); document.setName("holacaracola.txt");
     * document.setData("holacaracola".getBytes());
     * document.setMime("text/plain");
     * document.setLength(document.getData().length);
     * 
     * documentCustodyPlugin.saveDocument(custodyID, custodyParameters,
     * document);
     */

    InputStream is2 = FileUtils.readResource(this.getClass(),
        "testarxiudigitalcaib/Firma2.pdf");
    byte[] data2 = FileUtils.toByteArray(is2);

    SignatureCustody signature2 = new SignatureCustody();
    signature2.setName("Firma2.pdf");
    signature2.setAttachedDocument(false);
    signature2.setData(data2);
    signature2.setLength(signature2.getData().length);
    signature2.setMime("application/pdf");
    signature2.setSignatureType(SignatureCustody.PADES_SIGNATURE);

    documentCustodyPlugin.saveAll(custodyID, custodyParameters, document,  signature2, null);
    // documentCustodyPlugin.saveAll(custodyID, custodyParameters, document,
    // signature2, null);

    Map<String, List<Metadata>> allmetas = plugin.getAllMetadata(custodyID);

    for (String k : allmetas.keySet()) {
      List<Metadata> list = allmetas.get(k);

      for (Metadata metadata : list) {
        System.out.println(metadata.getKey() + " => " + metadata.getValue());
      }

    }

    //
    final String key = "hola";
    Metadata mSet = new Metadata(key, "caracola");

    plugin.addMetadata(custodyID, mSet, custodyParameters);

    Metadata mGet = plugin.getOnlyOneMetadata(custodyID, key);

    System.out.println(" META = " + mGet.getKey() + ": " + mGet.getValue());

    Assert.assertEquals("M1 Les metadades no són iguals", mSet, mGet);

    plugin.deleteMetadata(custodyID, key);

    mGet = plugin.getOnlyOneMetadata(custodyID, key);

    Assert.assertNull("M2 La metadada amb clau " + key
        + " ha sigut esborrada però encara la puc llegir", mGet);

    // XYZ ZZZ Descomentar documentCustodyPlugin.deleteCustody(custodyID);

  }
  
  
  
  public void testSimpleDoc() throws Exception {

    // = true significa que no es sobre escriu
    // = false es sobreescriu sobre el mateix fitxer
    final boolean reservarCadaVegada =  true;

    //final TipusGuardat tipusGuardat =  TipusGuardat.SAVEALL;
    
        System.out.println();
        System.out.println(" =========================================");
        System.out.println("   -------- reservarCadaVegada = " + reservarCadaVegada);
        System.out.println(" =========================================");
        System.out.println();
        testSimpleDocConfigurable(reservarCadaVegada, false);
      
    

  }
  
  

  public void testSimpleDocFull() throws Exception {

    // = true significa que no es sobre escriu
    // = false es sobreescriu sobre el mateix fitxer
    final boolean[] reservarCadaVegada = {  false , true};

    //final TipusGuardat[] tipusGuardat = { TipusGuardat.SAVEALL, TipusGuardat.DOCUMENT_PRIMER,
    //    TipusGuardat.FIRMA_PRIMER };

    for (int r = 0; r < reservarCadaVegada.length; r++) {

      //for (int t = 0; t < tipusGuardat.length; t++) {

        System.out.println();
        System.out.println(" =========================================");
        System.out.println("   -------- reservarCadaVegada[r] = " + reservarCadaVegada[r]);
        //System.out.println("   -------- tipusGuardat[t] = " + tipusGuardat[t]);
        System.out.println(" =========================================");
        System.out.println();
        testSimpleDocConfigurable(reservarCadaVegada[r],  true);
      //}
    }

  }

  public void testSimpleDocConfigurable(boolean reservarCadaVegada, 
      boolean deleteOnFinish)
      throws Exception {

    Properties specificProperties = new Properties();
    IDocumentCustodyPlugin documentCustodyPlugin = instantiateDocumentCustodyPlugin(specificProperties);
    ArxiuDigitalCAIBDocumentCustodyPlugin plugin;
    plugin = (ArxiuDigitalCAIBDocumentCustodyPlugin) documentCustodyPlugin;
    ApiArchivoDigital api = plugin.getApiArxiu(null);


    // XYZ ZZZ Eliminar
    {
      File f = new File("expedient.txt");
      if (f.exists()) {
        String custodyID = new String(FileUtils.readFromFile(f));

        documentCustodyPlugin.deleteCustody(custodyID);

      }
    }

    Map<String, Object> custodyParameters = new HashMap<String, Object>();

    // new Date(167526000000L)

    RegistroEntrada registro = getRegistro();

    custodyParameters.put("registro", registro);

    Anexo anexo = getAnexo(registro);
    custodyParameters.put("anexo", anexo);

    String custodyID = null;

    if (!reservarCadaVegada) {
      // Reserve ID
      custodyID = documentCustodyPlugin.reserveCustodyID(custodyParameters);

      System.out.println(" Reservat custodyID = " + custodyID);

      // XYZ ZZZ Eliminar
      FileOutputStream fos = new FileOutputStream(new File("expedient.txt"));
      fos.write(custodyID.getBytes());
      fos.close();

      ExpedientCarpetaDocument ec = ExpedientCarpetaDocument.decodeCustodyID(custodyID);

      Resultado<Expediente> expedient = api.obtenerExpediente(ec.expedientID);

      Map<String, Object> metadatas = expedient.getElementoDevuelto().getMetadataCollection();

      for (String key : metadatas.keySet()) {
        Object value = metadatas.get(key);

        System.out.println(" META[" + key + "] = " + value);

      }

    }

    final DocumentCustody[] documents;

    final SignatureCustody[] signatures;

    Metadata[] metas = null;
    {

      DocumentCustody document = new DocumentCustody();
      document.setName("holacaracola.txt");
      document.setData("holacaracola".getBytes());
      document.setMime("text/plain");
      document.setLength(document.getData().length);

      DocumentCustody document2 = new DocumentCustody();
      document2.setName("holacaracola.xml");
      document2.setData("holacaracolaV2222222".getBytes());
      document2.setMime("text/plain");
      document2.setLength(document2.getData().length);

      InputStream is = FileUtils.readResource(this.getClass(),
          "testarxiudigitalcaib/Firma.pdf");
      byte[] data = FileUtils.toByteArray(is);

      SignatureCustody signature = new SignatureCustody();
      signature.setName("Firma.pdf");
      signature.setAttachedDocument(false);
      signature.setData(data);
      signature.setLength(signature.getData().length);
      signature.setMime("application/pdf");
      signature.setSignatureType(SignatureCustody.PADES_SIGNATURE);

      InputStream is2 = FileUtils.readResource(this.getClass(),
          "testarxiudigitalcaib/Firma2.pdf");
      byte[] data2 = FileUtils.toByteArray(is2);

      SignatureCustody signature2 = new SignatureCustody();
      signature2.setName("Firma2.pdf");
      signature2.setAttachedDocument(false);
      signature2.setData(data2);
      signature2.setLength(signature2.getData().length);
      signature2.setMime("application/pdf");
      signature2.setSignatureType(SignatureCustody.PADES_SIGNATURE);

      if (reservarCadaVegada) {

        documents = new DocumentCustody[] { document2, document, null, document2, document2 };

        signatures = new SignatureCustody[] { null, signature, signature, signature,
            signature2 };

      } else {
        // TODO TESTS ERRONIS
        // (1) Pasar de document a null
        // (2) Passar de signature a null.

        // MULTIPLE TESTS
        documents = new DocumentCustody[] { document, document2, document };
        signatures = new SignatureCustody[] { null, signature, signature2 };
      }
    }

    // custodyID = documentCustodyPlugin.reserveCustodyID(custodyParameters);

    for (int i = 0; i < signatures.length; i++) {

      if (reservarCadaVegada) {
        custodyID = documentCustodyPlugin.reserveCustodyID(custodyParameters);
      }

      sleep();
      System.out.println();
      System.out.println(" ---------  Test[" + i + "]  --------");
      System.out.println();
      System.out.flush();
      Thread.sleep(500);

      DocumentCustody doc = documents[i];
      SignatureCustody sign = signatures[i];

      //switch (tipusGuardat) {
      //case SAVEALL:
        documentCustodyPlugin.saveAll(custodyID, custodyParameters, doc, sign, metas);
//        break;
//      case DOCUMENT_PRIMER:
//        if (doc != null) {
//          documentCustodyPlugin.saveDocument(custodyID, custodyParameters, doc);
//        }
//        if (sign != null) {
//          documentCustodyPlugin.saveSignature(custodyID, custodyParameters, sign);
//        }
//        break;
//      case FIRMA_PRIMER:
//        if (sign != null) {
//          documentCustodyPlugin.saveSignature(custodyID, custodyParameters, sign);
//        }
//        if (doc != null) {
//          documentCustodyPlugin.saveDocument(custodyID, custodyParameters, doc);
//        }
//        break;
//      }

      // ----------- DOCUMENT
      {

        System.out.println("     * Check D1");
        byte[] binData = documentCustodyPlugin.getDocument(custodyID);

        if (doc == null) {
          Assert.assertNull("D1.- S'ha guardat un document NULL però retorna valors", binData);
        } else {
          if (!Arrays.equals(binData, doc.getData())) {
            Assert.fail("D1.- El contingut del document No es igual");
          }
        }

        DocumentCustody onlyinfo = documentCustodyPlugin.getDocumentInfoOnly(custodyID);
        System.out.println("     * Check D2");
        compareDocument("D2", doc, onlyinfo, false);

        DocumentCustody fulldoc = documentCustodyPlugin.getDocumentInfo(custodyID);
        System.out.println("     * Check D3: ");
        compareDocument("D3", doc, fulldoc, true);
      }
      // ----------- SIGNATURE
      {

        System.out.println("     * Check S1");
        byte[] binData = documentCustodyPlugin.getSignature(custodyID);

        if (sign == null) {
          Assert.assertNull("S1.- S'ha guardat una signatura NULL però retorna valors",
              binData);
        } else {
          if (!Arrays.equals(binData, sign.getData())) {
            Assert.fail("S1.- El contingut del document No es igual");
          }
        }

        SignatureCustody onlyinfo = documentCustodyPlugin.getSignatureInfoOnly(custodyID);
        System.out.println("     * Check S2");
        compareDocument("S2", sign, onlyinfo, false);

        SignatureCustody fulldoc = documentCustodyPlugin.getSignatureInfo(custodyID);
        System.out.println("     * Check S3: ");
        compareDocument("S3", sign, fulldoc, true);
      }

      if (reservarCadaVegada) {
        documentCustodyPlugin.deleteCustody(custodyID);
      }

    }

    /*
     * // UPDATE DOCUMENT doc.setData("holacaracola_v2.0".getBytes());
     * documentCustodyPlugin.saveDocument(custodyID, custodyParameters, doc);
     * 
     * DocumentCustody docInfo =
     * documentCustodyPlugin.getDocumentInfo(custodyID);
     * 
     * if (docInfo == null) { Assert.fail("No pot llegir document info"); }
     * 
     * System.out.println("Tamany Info = " + docInfo.getLength());
     * 
     * byte[] data = documentCustodyPlugin.getDocument(custodyID); if (data ==
     * null) { Assert.fail("No pot llegir document byte []"); }
     * 
     * System.out.println("Tamany Info = " + docInfo.getLength());
     * System.out.println("Tamany byte[] = " + data.length);
     */

    if (!deleteOnFinish) {
      documentCustodyPlugin.deleteCustody(custodyID);
    }

  }

  protected Anexo getAnexo(RegistroEntrada registro) {
    TipoDocumental tipoDocumental = new TipoDocumental(77L, "TD02", "FundacioBit");

    Long validezDocumento = 1L; // TIPOVALIDEZDOCUMENTO_COPIA

    Long tipoDocumento = 1L; // TIPO_DOCUMENTO_FORMULARIO = 1L

    Integer origenCiudadanoAdmin = 1;

    Date fechaCaptura = new Date(System.currentTimeMillis());
    int modoFirma = 1; // MODO_FIRMA_ANEXO_ATTACHED = 1;

    Anexo anexo = new Anexo(222L, "Titulo Anexo", tipoDocumental, validezDocumento,
        tipoDocumento, registro.getRegistroDetalle(), "Observacions de l'Anexo",
        origenCiudadanoAdmin, fechaCaptura, modoFirma, "AdES-EPES");
    return anexo;
  }

  protected RegistroEntrada getRegistro() throws Exception {
    Usuario usuario = new Usuario("Antoni", "Nadal", "Bennasar", "12345678Z", "anadal");

    UsuarioEntidad usuarioEntitat = new UsuarioEntidad(usuario, "FundacioBit");

    Oficina oficina = new Oficina(33L, "OFI44556677");

    Organismo destino = new Organismo("ORGA_CAIB", "Comunitat Autonoma Illes Balears");

    // Date fecha = new SimpleDateFormat("dd/MM/yyyy").parse("04/04/2017");

    Date fecha = new Date(System.currentTimeMillis());

    Integer numeroRegistro = Integer.parseInt(SDF.format(fecha));

    String numeroRegistroFormateado = "FORMAT_" + numeroRegistro;

    Long tipoDocumentacionFisica = 1L;
    Long idioma = 1L;

    String codigoAsunto = "COD_ASU_001";

    Oficina oficinaOrigen = new Oficina(66L, "OFI112233");

    List<Interesado> interesados = new ArrayList<Interesado>();
    {
      Interesado interesado = new Interesado("Antoni", "Nadal", "Bennasar", "12345678Z");
      interesados.add(interesado);
    }

    RegistroDetalle registroDetalle = new RegistroDetalle("Prova de Arxiu Digital CAIB ",
        tipoDocumentacionFisica, idioma, codigoAsunto, oficinaOrigen, interesados);
    
    Libro llibre = new Libro(62L, "LLIB");

    RegistroEntrada registro = new RegistroEntrada(12345L, usuarioEntitat, oficina, destino,
        destino.getCodigo(), destino.getDenominacion(), fecha, numeroRegistro,
        numeroRegistroFormateado, registroDetalle, llibre);
    return registro;
  }

  @org.junit.Test
  public void testFull() throws Exception {

    Properties specificProperties = new Properties();

    Map<String, Object> custodyParameters = createCustodyParameters();

    internalTestGeneralDocumentCustody(specificProperties, custodyParameters, true);

  }

  /*
   * @org.junit.Test public void testFolderFromCustodyParameters() throws
   * Exception {
   * 
   * File baseDir = new File("./testReposWithFolder"); baseDir.mkdirs();
   * 
   * Properties specificProperties = new Properties();
   * 
   * specificProperties.setProperty(packageBase +
   * ArxiuDigitalCAIBDocumentCustodyPlugin.FILESYSTEM_PROPERTY_BASEDIR,
   * baseDir.getAbsolutePath());
   * 
   * boolean deleteOnFinish = true; // true;
   * 
   * internalTestFolderFromCustodyParameters(specificProperties,
   * deleteOnFinish);
   * 
   * }
   */

  /*
   * @org.junit.Test public void testAutomaticMetadatas() throws Exception {
   * 
   * File baseDir = new File("./testAutomaticMetadatas"); baseDir.mkdirs();
   * 
   * Properties specificProperties = new Properties();
   * 
   * specificProperties.setProperty(packageBase +
   * ArxiuDigitalCAIBDocumentCustodyPlugin.FILESYSTEM_PROPERTY_BASEDIR,
   * baseDir.getAbsolutePath());
   * 
   * final boolean deleteOnFinish = true;
   * 
   * internalTestAutomaticMetadatas(specificProperties, deleteOnFinish);
   * 
   * }
   */

  @Override
  public IDocumentCustodyPlugin instantiateDocumentCustodyPlugin(Properties specificProperties)
      throws CustodyException {

    Properties fsProperties = new Properties();

    try {
      fsProperties.load(new FileInputStream(getPropertiesFile()));
    } catch (Exception e) {
      throw new CustodyException("Error llegint fitxer plugin.properties: " + e.getMessage(),
          e);
    }

    if (specificProperties != null) {
      fsProperties.putAll(specificProperties);
    }

    IDocumentCustodyPlugin documentCustodyPlugin;
    documentCustodyPlugin = (IDocumentCustodyPlugin) PluginsManager.instancePluginByClass(
        ArxiuDigitalCAIBDocumentCustodyPlugin.class, getPackageBase(), fsProperties);
    return documentCustodyPlugin;
  }


  



  protected File getPropertiesFile() {
    return new File("plugin.properties");
  }

  protected Map<String, Object> createCustodyParameters() throws Exception {
    Map<String, Object> custodyParameters;
    custodyParameters = new HashMap<String, Object>();

    // new Date(167526000000L)

    RegistroEntrada registro = getRegistro();

    custodyParameters.put("registro", registro);

    Anexo anexo = getAnexo(registro);
    custodyParameters.put("anexo", anexo);
    
    Usuario usuario = new Usuario("Victor", "Heerera" , "", "87654321Z", "vherrera");
    
    UsuarioEntidad usuarioEntidad = new UsuarioEntidad(usuario, "caib");
    
    custodyParameters.put("usuarioEntidad", usuarioEntidad);
    
    custodyParameters.put("ciudadano_nombre", "Ciudadano Ejemplar");
    custodyParameters.put("ciudadano_idadministrativo", "12345678Z");

    return custodyParameters;
  }



  @Override
  public String getPropertyBase() {
    return getPackageBase()
        + ArxiuDigitalCAIBDocumentCustodyPlugin.ARXIUDIGITALCAIB_PROPERTY_BASE;
  }
  
  
  public String getPackageBase() {
    return "es.caib.exemple.";
  }
  
  
  public void sleep() {
    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
  
  
  
  public void testInternalMetadata() throws Exception {
    
    Properties specificProperties = new Properties();
    
    //specificProperties.setProperty(getPropertyBase() + "createDraft", "false");
    
    //es.caib.exemple.plugins.documentcustody.arxiudigitalcaib.
    
    IDocumentCustodyPlugin documentCustodyPlugin = instantiateDocumentCustodyPlugin(specificProperties);
    ArxiuDigitalCAIBDocumentCustodyPlugin plugin;
    plugin = (ArxiuDigitalCAIBDocumentCustodyPlugin) documentCustodyPlugin;
    
    Map<String, Object> custodyParameters = createCustodyParameters();
    
    System.out.println(" CREATE DRAFT = " + plugin.isPropertyCreateDraft_EL(custodyParameters));
    

    

    String custodyID = plugin.reserveCustodyID(createCustodyParameters());
    System.out.println(" CustodyID = " + custodyID);
    
    InputStream is2 = FileUtils.readResource(this.getClass(),
        //"testarxiudigitalcaib/Firma2.pdf");
        "testarxiudigitalcaib/test-signed-BES.pdf");
        
        
    byte[] data2 = FileUtils.toByteArray(is2);

    SignatureCustody signature2 = new SignatureCustody();
    signature2.setName("Firma2.pdf");
    signature2.setAttachedDocument(false);
    signature2.setData(data2);
    signature2.setLength(signature2.getData().length);
    signature2.setMime("application/pdf");
    signature2.setSignatureType(SignatureCustody.PADES_SIGNATURE);
    
    DocumentCustody documentCustody = null;
    
    
    Map<String, List<Metadata>> metas = plugin.getAllMetadata(custodyID);
     
    for (String key : metas.keySet()) {
      System.out.println("PRE[" + key + "] = " + metas.get(key).get(0).getValue());
    }

    
    Metadata[] metadata = null;

    plugin.saveAll(custodyID, custodyParameters, documentCustody, signature2, metadata);
    
    System.out.println();
    
    byte[] dataSign = plugin.getSignature(custodyID);
    SignatureCustody sign1 = plugin.getSignatureInfo(custodyID);
    SignatureCustody sign2 = plugin.getSignatureInfoOnly(custodyID);

    checkEmptyFile(plugin, custodyID);

    checkSign(signature2, dataSign, sign1, sign2);

    
    metas = plugin.getAllMetadata(custodyID);
    
    for (String key : metas.keySet()) {
      System.out.println("POST[" + key + "] = " + metas.get(key).get(0).getValue());
    }
    
    
    Thread.sleep(3000);
    
    // ACTUALITZAR
    ApiArchivoDigital api = plugin.getApiArxiu(custodyParameters);
    
    ExpedientCarpetaDocument ecd =  ExpedientCarpetaDocument.decodeCustodyID(custodyID);
    
    
    
    /*
    Documento docSet = new Documento();
    docSet.setId(ecd.documentID);
    
    Map<String, Object> metadataCollection = new HashMap<String, Object>();
    metadataCollection.put(MetadataConstants.ENI_IDIOMA, "es_es");
    docSet.setMetadataCollection(metadataCollection);
    
    ResultadoSimple rs = api.actualizarDocumento(docSet);
    */
    
    
    Resultado<Documento> docGet = api.obtenerDocumento(ecd.documentID, false);
    
    docGet.getElementoDevuelto().getMetadataCollection().put(MetadataConstants.ENI_IDIOMA, "es_es");
    
    docGet.getElementoDevuelto().getAspects().remove(Aspectos.BORRADOR);
    
    ResultadoSimple rs = api.actualizarDocumento(docGet.getElementoDevuelto());
    
        
    System.out.println("Resultat Actualitzar DOCUMENT: " + rs.getCodigoResultado() + ": " +
            rs.getMsjResultado());
    
    
    //System.out.println("TANCAR EXPEDIENT: " + ec.expedientID);
    
    //Resultado<String> res = api.cerrarExpediente(ec.expedientID);
    
    //System.out.println("Resultat tancar Expedient: " + res.getCodigoResultado() + ": " +
    //    res.getMsjResultado());
    
    System.out.println("ESBORRAR EXPEDIENT");
    plugin.deleteCustody(custodyID);
    
  }
  
  
  
  public void test3Combinacions()
      throws Exception {
    
    final boolean waitInput = true;
    
    Properties specificProperties = new Properties();
    IDocumentCustodyPlugin documentCustodyPlugin = instantiateDocumentCustodyPlugin(specificProperties);
    ArxiuDigitalCAIBDocumentCustodyPlugin plugin;
    plugin = (ArxiuDigitalCAIBDocumentCustodyPlugin) documentCustodyPlugin;
    

    Map<String, Object> custodyParameters = createCustodyParameters();

    final String custodyID = plugin.reserveCustodyID(createCustodyParameters());
    System.out.println(" CustodyID = " + custodyID);
    
    // obtenir CSV
    Metadata csv = plugin.getOnlyOneMetadata(custodyID, MetadataConstants.ENI_CSV);
    System.out.println(" CSV = " + csv.getValue());
    
    // Obtenir file i no ha de fallar
    checkEmptyFile(plugin, custodyID);

    // Obtenir firma i no ha de fallar
    checkEmptySignature(plugin, custodyID);
    
    System.out.println(" Reserva Feta ");
    
    if (waitInput) {
      waitForEnter();
    }
    
    
    // compbinacions doc, sign i doc+sign

    // ============ ONLY DOC
    {
      DocumentCustody documentCustody = new DocumentCustody();
      documentCustody.setName("holacaracola.txt");
      documentCustody.setData("holacaracola".getBytes());
      documentCustody.setMime("text/plain");
  
      SignatureCustody signatureCustody = null;
      
      Metadata[] metadata = null;
  
      plugin.saveAll(custodyID, custodyParameters, documentCustody, signatureCustody, metadata);
      
      byte[] dataFile = plugin.getDocument(custodyID);
      DocumentCustody file1 = plugin.getDocumentInfo(custodyID);
      DocumentCustody file2 = plugin.getDocumentInfoOnly(custodyID);
      
      checkFile(documentCustody, dataFile, file1, file2);
      
      checkEmptySignature(plugin, custodyID);
      

  
      //plugin.deleteCustody(custodyID);
    }
    
    
    if (waitInput) {
      waitForEnter();
    } else {
      sleep();
    }
    
    
    // ============  ONLY SIGN
    {
      // custodyID = plugin.reserveCustodyID(createCustodyParameters());
      InputStream is2 = FileUtils.readResource(this.getClass(),
          "testarxiudigitalcaib/Firma2.pdf");
          //"testarxiudigitalcaib/test-signed-BES.pdf");
          
          
      byte[] data2 = FileUtils.toByteArray(is2);

      SignatureCustody signatureCustody = new SignatureCustody();
      signatureCustody.setName("Firma2.pdf");
      signatureCustody.setAttachedDocument(false);
      signatureCustody.setData(data2);
      signatureCustody.setLength(data2.length);
      signatureCustody.setMime("application/pdf");
      signatureCustody.setSignatureType(SignatureCustody.PADES_SIGNATURE);
      
      DocumentCustody documentCustody = null;

      
      Metadata[] metadata = null;
  
      plugin.saveAll(custodyID, custodyParameters, documentCustody, signatureCustody, metadata);
      
      
      byte[] dataSign = plugin.getSignature(custodyID);
      SignatureCustody sign1 = plugin.getSignatureInfo(custodyID);
      SignatureCustody sign2 = plugin.getSignatureInfoOnly(custodyID);

      checkEmptyFile(plugin, custodyID);

      checkSign(signatureCustody, dataSign, sign1, sign2);
  
      // NO ESBORRAM I PROVAM L'ACTUALITZACIO
      // plugin.deleteCustody(custodyID);
    }
    
    if (waitInput) {
      waitForEnter();
    } else {
      sleep();
    }
    
    // ============  FILE & SIGN
    {

      
      SignatureCustody signatureCustody = new SignatureCustody();
      signatureCustody.setName("holacaracolaFIRMADETACHED.txt");
      signatureCustody.setData("holacaracolaFIRMA".getBytes());
      signatureCustody.setMime("text/plain");
      signatureCustody.setAttachedDocument(true);
      signatureCustody.setSignatureType(SignatureCustody.XADES_SIGNATURE);


      DocumentCustody documentCustody = new DocumentCustody();
      documentCustody.setName("holacaracolaofdetachedsign.txt");
      documentCustody.setData("holacaracola".getBytes());
      documentCustody.setMime("text/plain");

      Metadata[] metadata = null;
  
      plugin.saveAll(custodyID, custodyParameters, documentCustody, signatureCustody, metadata);
      
      
      // FILE
      byte[] dataFile = plugin.getDocument(custodyID);
      DocumentCustody file1 = plugin.getDocumentInfo(custodyID);
      DocumentCustody file2 = plugin.getDocumentInfoOnly(custodyID);
      
      checkFile(documentCustody, dataFile, file1, file2);
      
      // SIGN
      byte[] dataSign = plugin.getSignature(custodyID);
      SignatureCustody sign1 = plugin.getSignatureInfo(custodyID);
      SignatureCustody sign2 = plugin.getSignatureInfoOnly(custodyID);

      checkSign(signatureCustody, dataSign, sign1, sign2);
  
      waitForEnter();
      // XYZ ZZZ      
      //plugin.deleteCustody(custodyID);
    }
    
    

  }

  protected void checkEmptySignature(ArxiuDigitalCAIBDocumentCustodyPlugin plugin,
      String custodyID) throws CustodyException {
    byte[] dataSign = plugin.getSignature(custodyID);
    SignatureCustody sign1 = plugin.getSignatureInfo(custodyID);
    SignatureCustody sign2 = plugin.getSignatureInfoOnly(custodyID);
    
    Assert.assertNull(sign1);
    Assert.assertNull(dataSign);
    Assert.assertNull(sign2);
  }

  protected void checkEmptyFile(ArxiuDigitalCAIBDocumentCustodyPlugin plugin, String custodyID)
      throws CustodyException {
    byte[] dataFile = plugin.getDocument(custodyID);
    DocumentCustody file1 = plugin.getDocumentInfo(custodyID);
    DocumentCustody file2 = plugin.getDocumentInfoOnly(custodyID);
    
    
    Assert.assertNull(file2);
    Assert.assertNull(dataFile);
    Assert.assertNull(file1);
  }

  
  protected void checkSign(SignatureCustody original, byte[] dataFile, SignatureCustody file1, SignatureCustody file2) {
    
    checkFile(original, dataFile, file1, file2);

    // Sign type
    Assert.assertEquals(original.getSignatureType(), file1.getSignatureType());
    Assert.assertEquals(original.getSignatureType(), file2.getSignatureType());
    
    // ATTACHED
    Assert.assertEquals(original.getAttachedDocument(), file1.getAttachedDocument());
    Assert.assertEquals(original.getAttachedDocument(), file2.getAttachedDocument());

    
  }
    


  protected void checkFile(AnnexCustody original, byte[] dataFile, AnnexCustody file1, AnnexCustody file2) {
    
    // NOM
    Assert.assertEquals(original.getName(), file1.getName());
    Assert.assertEquals(file1.getName(), file2.getName());


    // TAMANY
    Assert.assertEquals(original.getData().length, file1.getData().length);
    Assert.assertEquals(file1.getData().length, dataFile.length);
    
    
    // CONTINGUT
    for (int i = 0; i < file1.getData().length; i++) {
      byte val1 = file1.getData()[i];
      byte val2 = dataFile[i];
      
      if (val1 != val2) {
        Assert.fail("La posició " + i + " del  plugin.getDocument() i del"
            + " plugin.getDocumentInfo() són diferents (" + Integer.toHexString((int)val2)
            + " != " + Integer.toHexString((int)val1)  + ")");
      }
    }

    System.out.println("file1.getData() = " + file1.getData().length);
    System.out.println("dataFile = " + dataFile.length);
    
    // Infoonly
    Assert.assertNull(file2.getData());
  }

}
