package org.fundaciobit.plugins.documentcustody.api;

/**
 * Repesenta un fitxer que d'alguna forma conté una firma (enveloled o enveloping)
 * @author anadal
 *
 */
public class SignatureCustody extends AnnexCustody {

  public static final String CADES_SIGNATURE = "cades";

  public static final String XADES_SIGNATURE = "xades";

  public static final String SMIME_SIGNATURE = "smime";
  
  public static final String PADES_SIGNATURE = "pades";

  // Office Open XML
  // https://ca.wikipedia.org/wiki/Office_Open_XML
  public static final String OOXML_SIGNATURE = "ooxml";

  // OpenDocument Format (.odt, .ods, .odp, .odg, ...)
  // https://en.wikipedia.org/wiki/OpenDocument
  public static final String ODF_SIGNATURE = "odf";
  
  // Ignore attachedDocument value
  public static final String OTHER_DOCUMENT_WITH_ATTACHED_SIGNATURE = "enveloped";
  
  // Ignore attachedDocument value
  public static final String OTHER_SIGNATURE_WITH_ATTACHED_DOCUMENT = "enveloping";
  
  // Ignore attachedDocument value
  public static final String OTHER_SIGNATURE_WITH_DETACHED_DOCUMENT = "detached";
  
  // Només sabem que és una firma però res més.
  public static final String UNKNOWN_SIGNATURE = "unknown";

  
  public static final String[] ALL_TYPES_OF_SIGNATURES = {
    CADES_SIGNATURE, XADES_SIGNATURE, SMIME_SIGNATURE,
    PADES_SIGNATURE, OOXML_SIGNATURE, ODF_SIGNATURE,
    OTHER_DOCUMENT_WITH_ATTACHED_SIGNATURE,
    OTHER_SIGNATURE_WITH_ATTACHED_DOCUMENT,
    OTHER_SIGNATURE_WITH_DETACHED_DOCUMENT
  };
  
  /**
   * true if data contains original document (attached).
   * false if data does not contain original document (dettached).
   * null can not obtain this information or is not necessary
   */
  protected Boolean attachedDocument = null;

  protected  String signatureType;

  public SignatureCustody() {
    super();
  }
  
  public SignatureCustody(String name, byte[] data, String signatureType) {
    this(name, null, data,signatureType, null);
  }

  
  public SignatureCustody(String name, byte[] data, String signatureType,
      Boolean attachedDocument) {
    this(name, null, data, signatureType, null);
  }
  
  
  public SignatureCustody(String name, long length, String signatureType) {
    this(name, null, length,signatureType, null);
  }

  
  public SignatureCustody(String name,long length, String signatureType,
      Boolean attachedDocument) {
    this(name, null, length, signatureType, null);
  }
  
  
  
  /**
   * @param name
   * @param mimeType
   * @param data
   * @param signatureType
   *          Available values are PADES_SIGNATURE, CADES_SIGNATURE,
   *          XADES_SIGNATURE, SMIME_SIGNATURE, OOXML_SIGNATURE, ODF_SIGNATURE,
   * @param attachedDocument
   *          When signature type has ambiguous attached or detached value, then
   *          use this boolean to know it. true if data contains original
   *          document (attached). false if data does not contain original
   *          document. null when this value is not necessary
   */
  public SignatureCustody(String name, String mime, byte[] data, String signatureType,
      Boolean attachedDocument) {
    super(name, mime, data);
    this.signatureType = signatureType;
    this.attachedDocument = attachedDocument;
  }
  
  
  public SignatureCustody(String name, String mime, long length, String signatureType,
      Boolean attachedDocument) {
    super(name, mime, length);
    this.signatureType = signatureType;
    this.attachedDocument = attachedDocument;
  }
  
  

  /**
   * @param name
   * @param mimeType
   * @param data
   * @param attachedSignature
   */
  public SignatureCustody(SignatureCustody sc) {
    super(sc);
    this.attachedDocument = sc.attachedDocument;
    this.signatureType = sc.signatureType;
  }

  public String getSignatureType() {
    return signatureType;
  }

  public void setSignatureType(String signatureType) {
    this.signatureType = signatureType;
  }

  public Boolean getAttachedDocument() {
    return attachedDocument;
  }

  public void setAttachedDocument(Boolean attachedDocument) {
    this.attachedDocument = attachedDocument;
  }

}
