package org.fundaciobit.plugins.documentcustody.arxiudigitalcaib.test.beans;


import java.util.Date;

/**
 * 
 * @author anadal
 *
 */
public class RegistroEntrada {


  private Long id;

  private UsuarioEntidad usuario;

  private Oficina oficina;

  private Organismo destino;

  private String destinoExternoCodigo;

  private String destinoExternoDenominacion;

  private Date fecha;

  private Integer numeroRegistro;

  private String numeroRegistroFormateado;

  private RegistroDetalle registroDetalle;
  
  private Libro libro;

  /**
     * 
     */
  public RegistroEntrada() {
    super();
  }

  /**
   * @param id
   * @param usuario
   * @param oficina
   * @param destino
   * @param destinoExternoCodigo
   * @param destinoExternoDenominacion
   * @param fecha
   * @param numeroRegistro
   * @param numeroRegistroFormateado
   * @param registroDetalle
   */
  public RegistroEntrada(Long id, UsuarioEntidad usuario, Oficina oficina, Organismo destino,
      String destinoExternoCodigo, String destinoExternoDenominacion, Date fecha,
      Integer numeroRegistro, String numeroRegistroFormateado,
      RegistroDetalle registroDetalle, Libro libro) {
    super();
    this.id = id;
    this.usuario = usuario;
    this.oficina = oficina;
    this.destino = destino;
    this.destinoExternoCodigo = destinoExternoCodigo;
    this.destinoExternoDenominacion = destinoExternoDenominacion;
    this.fecha = fecha;
    this.numeroRegistro = numeroRegistro;
    this.numeroRegistroFormateado = numeroRegistroFormateado;
    this.registroDetalle = registroDetalle;
    this.libro = libro;
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

  public Organismo getDestino() {
    return destino;
  }

  public void setDestino(Organismo destino) {
    this.destino = destino;
  }

  public String getDestinoExternoCodigo() {
    return destinoExternoCodigo;
  }

  public void setDestinoExternoCodigo(String destinoExternoCodigo) {
    this.destinoExternoCodigo = destinoExternoCodigo;
  }

  public String getDestinoExternoDenominacion() {
    return destinoExternoDenominacion;
  }

  public void setDestinoExternoDenominacion(String destinoExternoDenominacion) {
    this.destinoExternoDenominacion = destinoExternoDenominacion;
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

  public RegistroDetalle getRegistroDetalle() {
    return registroDetalle;
  }

  public void setRegistroDetalle(RegistroDetalle registroDetalle) {
    this.registroDetalle = registroDetalle;
  }

  public Libro getLibro() {
    return libro;
  }

  public void setLibro(Libro libro) {
    this.libro = libro;
  }

}
