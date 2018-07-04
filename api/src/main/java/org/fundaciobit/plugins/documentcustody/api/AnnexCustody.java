package org.fundaciobit.plugins.documentcustody.api;

/**
 * 
 * @author anadal
 *
 */
public class AnnexCustody {

  protected String name;

  protected byte[] data;

  protected long length = -1;

  protected String mime;

  /**
   * 
   */
  public AnnexCustody() {
    super();
  }

  /**
   * @param name
   * @param data
   */
  public AnnexCustody(String name, byte[] data) {
    super();
    this.name = name;
    this.data = data;
    this.length = data.length;
  }

  /**
   * @param name
   * @param mime
   * @param data
   */
  public AnnexCustody(String name, String mime, byte[] data) {
    super();
    this.name = name;
    this.mime = mime;
    this.data = data;
    this.length = data.length;
  }

  /**
   * @param name
   * @param mime
   * @param length
   */
  public AnnexCustody(String name, String mime, long length) {
    super();
    this.name = name;
    this.mime = mime;
    this.length = length;
  }

  /**
   * @param name
   * @param data
   */
  public AnnexCustody(AnnexCustody annexCustody) {
    super();
    this.name = annexCustody.name;
    this.data = annexCustody.data;
    this.mime = annexCustody.mime;
    this.length = annexCustody.length;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public byte[] getData() {
    return data;
  }

  public void setData(byte[] data) {
    this.data = data;
  }

  public String getMime() {
    return mime;
  }

  public void setMime(String mime) {
    this.mime = mime;
  }

  public long getLength() {
    return length;
  }

  public void setLength(long length) {
    this.length = length;
  }

}
