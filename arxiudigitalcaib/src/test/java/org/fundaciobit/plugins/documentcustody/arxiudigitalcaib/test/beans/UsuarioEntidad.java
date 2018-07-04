package org.fundaciobit.plugins.documentcustody.arxiudigitalcaib.test.beans;

import java.io.Serializable;

/**
 * 
 * @author anadal
 *
 */
public class UsuarioEntidad implements Serializable {

  private Usuario usuario;

  private String entidad;

  public UsuarioEntidad() {
  }

  /**
   * @param id
   * @param usuario
   * @param entidad
   */
  public UsuarioEntidad(Usuario usuario, String entidad) {
    this.usuario = usuario;
    this.entidad = entidad;
  }


  public Usuario getUsuario() {
    return usuario;
  }

  public void setUsuario(Usuario usuario) {
    this.usuario = usuario;
  }

  public String getEntidad() {
    return entidad;
  }

  public void setEntidad(String entidad) {
    this.entidad = entidad;
  }

}
