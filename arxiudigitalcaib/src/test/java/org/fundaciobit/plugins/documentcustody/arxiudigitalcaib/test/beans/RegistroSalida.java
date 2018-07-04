package org.fundaciobit.plugins.documentcustody.arxiudigitalcaib.test.beans;

import java.util.Date;

/**
 * 
 * @author anadal
 *
 */
public class RegistroSalida {

  private Long id;

  private UsuarioEntidad usuario;

  private Oficina oficina;

  private Organismo origen;

  private String origenExternoCodigo;

  private String origenExternoDenominacion;

  private Date fecha;

  private Integer numeroRegistro;

  private String numeroRegistroFormateado;

  private Long estado;

  private RegistroDetalle registroDetalle;

  /**
     * 
     */
  public RegistroSalida() {
    super();
  }

  /**
   * @param id
   * @param usuario
   * @param oficina
   * @param origen
   * @param origenExternoCodigo
   * @param origenExternoDenominacion
   * @param fecha
   * @param numeroRegistro
   * @param numeroRegistroFormateado
   * @param estado
   * @param registroDetalle
   */
  public RegistroSalida(Long id, UsuarioEntidad usuario, Oficina oficina, Organismo origen,
      String origenExternoCodigo, String origenExternoDenominacion, Date fecha,
      Integer numeroRegistro, String numeroRegistroFormateado, Long estado,
      RegistroDetalle registroDetalle) {
    super();
    this.id = id;
    this.usuario = usuario;
    this.oficina = oficina;
    this.origen = origen;
    this.origenExternoCodigo = origenExternoCodigo;
    this.origenExternoDenominacion = origenExternoDenominacion;
    this.fecha = fecha;
    this.numeroRegistro = numeroRegistro;
    this.numeroRegistroFormateado = numeroRegistroFormateado;
    this.estado = estado;
    this.registroDetalle = registroDetalle;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public UsuarioEntidad getUsuario() {
    return usuario;
  }

  public void setUsuario(UsuarioEntidad usuario) {
    this.usuario = usuario;
  }

  public Oficina getOficina() {
    return oficina;
  }

  public void setOficina(Oficina oficina) {
    this.oficina = oficina;
  }

  public Organismo getOrigen() {
    return origen;
  }

  public void setOrigen(Organismo origen) {
    this.origen = origen;
  }

  public String getOrigenExternoCodigo() {
    return origenExternoCodigo;
  }

  public void setOrigenExternoCodigo(String origenExternoCodigo) {
    this.origenExternoCodigo = origenExternoCodigo;
  }

  public String getOrigenExternoDenominacion() {
    return origenExternoDenominacion;
  }

  public void setOrigenExternoDenominacion(String origenExternoDenominacion) {
    this.origenExternoDenominacion = origenExternoDenominacion;
  }

  public Date getFecha() {
    return fecha;
  }

  public void setFecha(Date fecha) {
    this.fecha = fecha;
  }

  public Integer getNumeroRegistro() {
    return numeroRegistro;
  }

  public void setNumeroRegistro(Integer numeroRegistro) {
    this.numeroRegistro = numeroRegistro;
  }

  public String getNumeroRegistroFormateado() {
    return numeroRegistroFormateado;
  }

  public void setNumeroRegistroFormateado(String numeroRegistroFormateado) {
    this.numeroRegistroFormateado = numeroRegistroFormateado;
  }

  public Long getEstado() {
    return estado;
  }

  public void setEstado(Long estado) {
    this.estado = estado;
  }

  public RegistroDetalle getRegistroDetalle() {
    return registroDetalle;
  }

  public void setRegistroDetalle(RegistroDetalle registroDetalle) {
    this.registroDetalle = registroDetalle;
  }

}
