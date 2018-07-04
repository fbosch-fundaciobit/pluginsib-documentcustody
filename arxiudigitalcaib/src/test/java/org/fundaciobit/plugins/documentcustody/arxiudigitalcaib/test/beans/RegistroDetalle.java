package org.fundaciobit.plugins.documentcustody.arxiudigitalcaib.test.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author anadal
 *
 */
public class RegistroDetalle implements Serializable {

  private String extracto;

  private Long tipoDocumentacionFisica;

  private Long idioma;

  private String codigoAsunto;

  private Oficina oficinaOrigen;

  private List<Interesado> interesados = new ArrayList<Interesado>();

  public RegistroDetalle() {
  }

  /**
   * @param id
   * @param extracto
   * @param tipoDocumentacionFisica
   * @param idioma
   * @param codigoAsunto
   * @param oficinaOrigen
   */
  public RegistroDetalle(String extracto, Long tipoDocumentacionFisica, Long idioma,
      String codigoAsunto, Oficina oficinaOrigen, List<Interesado> interesados) {
    super();

    this.extracto = extracto;
    this.tipoDocumentacionFisica = tipoDocumentacionFisica;
    this.idioma = idioma;
    this.codigoAsunto = codigoAsunto;
    this.oficinaOrigen = oficinaOrigen;
    this.interesados = interesados;
  }

  public String getExtracto() {
    return extracto;
  }

  public void setExtracto(String extracto) {
    this.extracto = extracto;
  }

  public Long getTipoDocumentacionFisica() {
    return tipoDocumentacionFisica;
  }

  public void setTipoDocumentacionFisica(Long tipoDocumentacionFisica) {
    this.tipoDocumentacionFisica = tipoDocumentacionFisica;
  }

  public Long getIdioma() {
    return idioma;
  }

  public void setIdioma(Long idioma) {
    this.idioma = idioma;
  }

  public String getCodigoAsunto() {
    return codigoAsunto;
  }

  public void setCodigoAsunto(String codigoAsunto) {
    this.codigoAsunto = codigoAsunto;
  }

  public Oficina getOficinaOrigen() {
    return oficinaOrigen;
  }

  public void setOficinaOrigen(Oficina oficinaOrigen) {
    this.oficinaOrigen = oficinaOrigen;
  }

  public List<Interesado> getInteresados() {
    return interesados;
  }

  public void setInteresados(List<Interesado> interesados) {
    this.interesados = interesados;
  }

}
