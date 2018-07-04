package org.fundaciobit.plugins.documentcustody.arxiudigitalcaib.test.beans;

import java.io.Serializable;

/**
 * @author anadal (index)
 */

public class Organismo implements Serializable {


  private String codigo;

  private String denominacion;

  public Organismo() {
  }

  /**
   * @param id
   * @param codigo
   * @param denominacion
   */
  public Organismo(String codigo, String denominacion) {
    super();
    
    this.codigo = codigo;
    this.denominacion = denominacion;
  }


  public String getCodigo() {
    return codigo;
  }

  public void setCodigo(String codigo) {
    this.codigo = codigo;
  }

  public String getDenominacion() {
    return denominacion;
  }

  public void setDenominacion(String denominacion) {
    this.denominacion = denominacion;
  }

}
