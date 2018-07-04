package org.fundaciobit.plugins.documentcustody.arxiudigitalcaib.test.beans;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @author anadal
 *
 */
public class Anexo implements Serializable {

  private Long id;

  private String titulo; // Campo descriptivo del anexo.

  private TipoDocumental tipoDocumental; // reso, acord, factura, ..

  private Long validezDocumento;

  private Long tipoDocumento;

  private RegistroDetalle registroDetalle;

  private String observaciones;

  private Integer origenCiudadanoAdmin;

  private Date fechaCaptura;

  private int modoFirma;

  private byte[] certificado;

  private byte[] firma; // Corresponde al campo Firma del Documento del segmento
                        // "De_Anexo"( solo viene informado cuando la firma es
                        // CSV)

  private byte[] validacionOCSPCertificado;

  private byte[] timestamp;

  private byte[] hash;

  private String custodiaID;

  private String csv; // TODO este campo parece que sobra, verificar que no se
                      // emplee en NTI

  // SIR
  private Boolean firmaValida; // Indicará si la firma es vàlida o no
  private Boolean justificante; // Indica si el anexo es justificante.
  

  private String signProfile;

  public Anexo() {
  }

  /**
   * @param id
   * @param titulo
   * @param tipoDocumental
   * @param validezDocumento
   * @param tipoDocumento
   * @param registroDetalle
   * @param observaciones
   * @param origenCiudadanoAdmin
   * @param fechaCaptura
   * @param modoFirma
   */
  public Anexo(Long id, String titulo, TipoDocumental tipoDocumental, Long validezDocumento,
      Long tipoDocumento, RegistroDetalle registroDetalle, String observaciones,
      Integer origenCiudadanoAdmin, Date fechaCaptura, int modoFirma, String signProfile) {
    super();
    this.id = id;
    this.titulo = titulo;
    this.tipoDocumental = tipoDocumental;
    this.validezDocumento = validezDocumento;
    this.tipoDocumento = tipoDocumento;
    this.registroDetalle = registroDetalle;
    this.observaciones = observaciones;
    this.origenCiudadanoAdmin = origenCiudadanoAdmin;
    this.fechaCaptura = fechaCaptura;
    this.modoFirma = modoFirma;
    this.signProfile= signProfile;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTitulo() {
    return titulo;
  }

  public void setTitulo(String titulo) {
    this.titulo = titulo;
  }

  public TipoDocumental getTipoDocumental() {
    return tipoDocumental;
  }

  public void setTipoDocumental(TipoDocumental tipoDocumental) {
    this.tipoDocumental = tipoDocumental;
  }

  public Long getValidezDocumento() {
    return validezDocumento;
  }

  public void setValidezDocumento(Long validezDocumento) {
    this.validezDocumento = validezDocumento;
  }

  public Long getTipoDocumento() {
    return tipoDocumento;
  }

  public void setTipoDocumento(Long tipoDocumento) {
    this.tipoDocumento = tipoDocumento;
  }

  public RegistroDetalle getRegistroDetalle() {
    return registroDetalle;
  }

  public void setRegistroDetalle(RegistroDetalle registroDetalle) {
    this.registroDetalle = registroDetalle;
  }

  public String getObservaciones() {
    return observaciones;
  }

  public void setObservaciones(String observaciones) {
    this.observaciones = observaciones;
  }

  public Integer getOrigenCiudadanoAdmin() {
    return origenCiudadanoAdmin;
  }

  public void setOrigenCiudadanoAdmin(Integer origenCiudadanoAdmin) {
    this.origenCiudadanoAdmin = origenCiudadanoAdmin;
  }

  public Date getFechaCaptura() {
    return fechaCaptura;
  }

  public void setFechaCaptura(Date fechaCaptura) {
    this.fechaCaptura = fechaCaptura;
  }

  public int getModoFirma() {
    return modoFirma;
  }

  public void setModoFirma(int modoFirma) {
    this.modoFirma = modoFirma;
  }

  public byte[] getCertificado() {
    return certificado;
  }

  public void setCertificado(byte[] certificado) {
    this.certificado = certificado;
  }

  public byte[] getFirma() {
    return firma;
  }

  public void setFirma(byte[] firma) {
    this.firma = firma;
  }

  public byte[] getValidacionOCSPCertificado() {
    return validacionOCSPCertificado;
  }

  public void setValidacionOCSPCertificado(byte[] validacionOCSPCertificado) {
    this.validacionOCSPCertificado = validacionOCSPCertificado;
  }

  public byte[] getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(byte[] timestamp) {
    this.timestamp = timestamp;
  }

  public byte[] getHash() {
    return hash;
  }

  public void setHash(byte[] hash) {
    this.hash = hash;
  }

  public String getCustodiaID() {
    return custodiaID;
  }

  public void setCustodiaID(String custodiaID) {
    this.custodiaID = custodiaID;
  }

  public String getCsv() {
    return csv;
  }

  public void setCsv(String csv) {
    this.csv = csv;
  }

  public Boolean getFirmaValida() {
    return firmaValida;
  }

  public void setFirmaValida(Boolean firmaValida) {
    this.firmaValida = firmaValida;
  }

  public Boolean getJustificante() {
    return justificante;
  }

  public void setJustificante(Boolean justificante) {
    this.justificante = justificante;
  }

  public String getSignProfile() {
    return signProfile;
  }

  public void setSignProfile(String signProfile) {
    this.signProfile = signProfile;
  }

}
