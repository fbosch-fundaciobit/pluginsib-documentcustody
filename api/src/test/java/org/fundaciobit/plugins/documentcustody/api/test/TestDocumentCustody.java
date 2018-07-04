package org.fundaciobit.plugins.documentcustody.api.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.fundaciobit.plugins.documentcustody.api.AbstractDocumentCustodyPlugin;
import org.fundaciobit.plugins.documentcustody.api.AnnexCustody;
import org.fundaciobit.plugins.documentcustody.api.CustodyException;
import org.fundaciobit.plugins.documentcustody.api.DocumentCustody;
import org.fundaciobit.plugins.documentcustody.api.IDocumentCustodyPlugin;
import org.fundaciobit.plugins.documentcustody.api.NotSupportedCustodyException;
import org.fundaciobit.plugins.documentcustody.api.SignatureCustody;
import org.fundaciobit.plugins.utils.Metadata;
import org.fundaciobit.plugins.utils.MetadataFormatException;
import org.fundaciobit.plugins.utils.MetadataType;
import org.junit.Assert;

/**
 * 
 * @author anadal
 *
 */
public abstract class TestDocumentCustody {

  public abstract IDocumentCustodyPlugin instantiateDocumentCustodyPlugin(
      Properties specificProperties) throws CustodyException;

  public abstract String getPropertyBase();

  /**
   * 
   * @param documentCustodyPlugin
   * @param deleteOnFinish
   * @return CustodyID
   * @throws CustodyException
   * @throws NotSupportedCustodyException
   * @throws MetadataFormatException
   */
  protected InfoExecutionTest internalTestGeneralDocumentCustody(
      Properties specificProperties, Map<String, Object> custodyParameters,
      boolean deleteOnFinish) throws CustodyException, NotSupportedCustodyException,
      MetadataFormatException {
    return internalTestGeneralDocumentCustody(specificProperties, custodyParameters,
        deleteOnFinish, false);
  }

  /**
   * 
   * @param documentCustodyPlugin
   * @param deleteOnFinish
   * @return CustodyID
   * @throws CustodyException
   * @throws NotSupportedCustodyException
   * @throws MetadataFormatException
   */
  @SuppressWarnings("deprecation")
  protected InfoExecutionTest internalTestGeneralDocumentCustody(
      Properties specificProperties, Map<String, Object> custodyParameters,
      boolean deleteOnFinish, boolean checkMetadata) throws CustodyException,
      NotSupportedCustodyException, MetadataFormatException {

    IDocumentCustodyPlugin documentCustodyPlugin = instantiateDocumentCustodyPlugin(specificProperties);

    // Reserve ID
    final String custodyID = documentCustodyPlugin.reserveCustodyID(custodyParameters);

    // SAVE DOCUMENT
    DocumentCustody doc = new DocumentCustody();
    doc.setName("holacaracola.txt");
    doc.setData("holacaracola".getBytes());
    doc.setMime("text/plain");

    documentCustodyPlugin.saveDocument(custodyID, custodyParameters, doc);

    if (documentCustodyPlugin.getDocumentInfo(custodyID) == null) {
      Assert.fail("No pot llegir document info");
    }

    if (documentCustodyPlugin.getDocument(custodyID) == null) {
      Assert.fail("No pot llegir document byte []");
    }

    // SAVE SIGNATURE
    SignatureCustody signatureCustody = new SignatureCustody();

    signatureCustody.setAttachedDocument(false);
    signatureCustody.setData("firma dades".getBytes());
    signatureCustody.setName("firma.xml");
    signatureCustody.setSignatureType(SignatureCustody.XADES_SIGNATURE);
    signatureCustody.setMime("text/xml");

    documentCustodyPlugin.saveSignature(custodyID, custodyParameters, signatureCustody);

    if (documentCustodyPlugin.getSignatureInfo(custodyID) == null) {
      Assert.fail("Nopot llegir Signature info");
    }

    if (documentCustodyPlugin.getSignature(custodyID) == null) {
      Assert.fail("Nopot llegir Signature byte []");
    }

    // ANNEXES
    
    if (documentCustodyPlugin.supportsAnnexes()) {

    AnnexCustody annexCustody = new AnnexCustody();

    annexCustody.setData("annex content file".getBytes());
    annexCustody.setName("firma.xml");
    annexCustody.setMime("application/octet-stream");

    documentCustodyPlugin.addAnnex(custodyID, annexCustody, custodyParameters);

    documentCustodyPlugin.addAnnex(custodyID, annexCustody, custodyParameters);

    List<String> annexes = documentCustodyPlugin.getAllAnnexes(custodyID);

    Assert.assertEquals(2, annexes.size());
    }

    // Metadata
    if (documentCustodyPlugin.supportsMetadata() &&  checkMetadata) {
      String key = "k1";
      documentCustodyPlugin.addMetadata(custodyID, new Metadata(key, "value11",
          MetadataType.STRING), custodyParameters);

      List<Metadata> list = documentCustodyPlugin.getMetadata(custodyID, key);
      Assert.assertEquals(1, list.size());

      documentCustodyPlugin.addMetadata(custodyID, new Metadata(key, "value22",
          MetadataType.STRING), custodyParameters);

      list = documentCustodyPlugin.getMetadata(custodyID, key);
      Assert.assertEquals(2, list.size());

      documentCustodyPlugin.addMetadata(custodyID, new Metadata("k2", "12",
          MetadataType.INTEGER), custodyParameters);

      list = documentCustodyPlugin.getMetadata(custodyID, key);
      Assert.assertEquals(2, list.size());

      list = documentCustodyPlugin.getMetadata(custodyID, "k2");
      Assert.assertEquals(1, list.size());

      documentCustodyPlugin.deleteMetadata(custodyID, key);
      list = documentCustodyPlugin.getMetadata(custodyID, key);
      Assert.assertNull(list);

      Assert.assertEquals(1, documentCustodyPlugin.getMetadata(custodyID, "k2").size());

      Metadata[] errors = new Metadata[] {
          new Metadata("key", "kkkkzññ", MetadataType.BASE64),
          new Metadata("key", "12.8", MetadataType.INTEGER),
          new Metadata("key", "12,8", MetadataType.DECIMAL),
      // new Metadata("key", "truez", MetadataType.BOOLEAN),

      };
      for (Metadata error : errors) {
        try {
          documentCustodyPlugin.addMetadata(custodyID, error, custodyParameters);
          Assert.fail("metadata erronia s'ha afegit com a bona");
        } catch (MetadataFormatException mfe) {
          // OK
        }
      }
    }

    if (deleteOnFinish) {
      documentCustodyPlugin.deleteCustody(custodyID);
    }

    return new InfoExecutionTest(custodyID, documentCustodyPlugin);
  }

  protected List<InfoExecutionTest> internalTestFolderFromCustodyParameters(
      Properties specificProperties, boolean deleteOnFinish) throws CustodyException,
      NotSupportedCustodyException, MetadataFormatException {

    final String folder = "${registre.data?string[\"yyyy/MM/dd\"]}/${registre.id}/";
    specificProperties.setProperty(getPropertyBase()
        + AbstractDocumentCustodyPlugin.ABSTRACT_FOLDER_EXPRESSION_LANGUAGE, folder);

    // IDocumentCustodyPlugin documentCustodyPlugin =
    // initialize(specificProperties);

    ArrayList<InfoExecutionTest> custodyIDs;

    Registre[] registres = new Registre[] { 
        new Registre("155", new Date(167526000000L)),
        new Registre("123455", new Date(50799600000L)),
        new Registre("123456", new Date(50799600000L)),
    };

    custodyIDs = new ArrayList<InfoExecutionTest>();

    for (Registre registre : registres) {
      Map<String, Object> custodyParameters = new HashMap<String, Object>();
      custodyParameters.put("registre", registre);

      InfoExecutionTest custodyID = internalTestGeneralDocumentCustody(specificProperties,
          custodyParameters, deleteOnFinish);

      custodyIDs.add(custodyID);

      // documentCustodyPlugin.deleteCustody(custodyID);
    }

    return custodyIDs;
  }

  protected List<InfoExecutionTest> internalTestAutomaticMetadatas(
      Properties specificProperties, boolean deleteOnFinish) throws CustodyException,
      NotSupportedCustodyException, MetadataFormatException {

    // final String folder=
    // "${registre.data?string[\"yyyy/MM/dd\"]}/${registre.id}/";
    specificProperties.setProperty(getPropertyBase()
        + AbstractDocumentCustodyPlugin.ABSTRACT_AUTOMATIC_METADATA_ITEMS, "1,2,22,3,4");

    final String metaProp = getPropertyBase()
        + AbstractDocumentCustodyPlugin.ABSTRACT_AUTOMATIC_METADATA;

    // XXXXX.meta.1.name=eni:v_nti
    specificProperties.setProperty(metaProp + ".1.name", "eni:v_nti");
    specificProperties.setProperty(metaProp + ".1.valueEL",
        "http://administracionelectronica.gob.es/ENI/XSD/v1.0/documento-e");

    // 1=Administracion || 0=Ciutadano
    specificProperties.setProperty(metaProp + ".2.name", "eni:origen");
    specificProperties.setProperty(metaProp + ".2.valueEL", "${registre.origen}");

    // 1=Administracion || 0=Ciudadano
    specificProperties.setProperty(metaProp + ".22.name", "eni:origenStr");
    specificProperties
        .setProperty(
            metaProp + ".22.valueEL",
            "<#if registre.origen == 1>Administracion<#elseif registre.origen == 0>Ciudadano<#else>DESCONEGUT ${registre.origen}</#if>");

    // eni:estado_elaboracion=EE01
    specificProperties.setProperty(metaProp + ".3.name", "eni:estado_elaboracion");
    specificProperties.setProperty(metaProp + ".3.valueEL", "${registre.estadoElaboracion}");

    // eni:tipo_doc_ENI=TD02 // "Acuerdo");
    specificProperties.setProperty(metaProp + ".4.name", "eni:tipo_doc_ENI");
    specificProperties.setProperty(metaProp + ".4.valueEL", "${registre.tipoDocumental}");

    specificProperties.setProperty(getPropertyBase()
        + AbstractDocumentCustodyPlugin.ABSTRACT_PREFIX, "");

    Registre[] registres = new Registre[] {
        // new Registre("155", new Date(167526000000L)),
        new Registre("155M", new Date(167526000000L), 0, "EE01", "TD2"),

        new Registre("123455", new Date(50799600000L), 1, "EE02", "TD9")
    // new Registre("123456", new Date(50799600000L)),

    };

    ArrayList<InfoExecutionTest> custodyIDs = new ArrayList<InfoExecutionTest>();

    for (Registre registre : registres) {
      Map<String, String> expected = new HashMap<String, String>();
      expected.put("eni:v_nti",
          "http://administracionelectronica.gob.es/ENI/XSD/v1.0/documento-e");
      expected.put("eni:origen", String.valueOf(registre.getOrigen()));
      expected
          .put("eni:origenStr", registre.getOrigen() == 1 ? "Administracion" : "Ciudadano");
      expected.put("eni:estado_elaboracion", registre.getEstadoElaboracion());
      expected.put("eni:tipo_doc_ENI", registre.getTipoDocumental());

      Map<String, Object> custodyParameters = new HashMap<String, Object>();
      custodyParameters.put("registre", registre);

      InfoExecutionTest iet;
      iet = internalTestGeneralDocumentCustody(specificProperties, custodyParameters, false,
          false);
      custodyIDs.add(iet);

      IDocumentCustodyPlugin documentCustodyPlugin = iet.getDocumentCustodyPlugin();

      String custodyID = iet.getCustodyID();

      // Llistar metadades
      Map<String, List<Metadata>> allmetadatas = documentCustodyPlugin
          .getAllMetadata(custodyID);

      Map<String, String> results = new HashMap<String, String>();

      for (String nom : allmetadatas.keySet()) {
        List<Metadata> list = allmetadatas.get(nom);
        for (Metadata metadata : list) {
          System.out.println("Meta[" + nom + "/" + metadata.getKey() + "] = "
              + metadata.getValue());
          results.put(nom, metadata.getValue());
        }
      }

      if (deleteOnFinish && custodyID != null) {
        documentCustodyPlugin.deleteCustody(custodyID);
      }

      if (!expected.equals(results)) {
        Assert.fail("Les metadades esperades són diferents"
            + " a les metadades llegides del DocumentCustody");
      }
    }

    return custodyIDs;

  }

}
