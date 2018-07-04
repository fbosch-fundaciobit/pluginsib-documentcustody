package org.fundaciobit.plugins.documentcustody.api;

/**
 * Reprenta un arxiu sense cap tipus de firma (ni enveloped ni enveloping)
 * @author anadal
 * 
 */
public class DocumentCustody extends AnnexCustody {


  public DocumentCustody() {
  }

  /**
   * @param name
   * @param mime
   * @param length
   */
  public DocumentCustody(String name, String mime, long length) {
    super(name, mime, length);
  }

  /**
   * @param name
   * @param mimeType
   * @param data
   */
  public DocumentCustody(String name, byte[] data) {
    super(name, data);
  }
  
  public DocumentCustody(String name, String mime, byte[] data) {
    super(name, mime, data);
  }

  /**
   * 
   */
  public DocumentCustody(DocumentCustody dc) {
    super(dc);
  }

}
