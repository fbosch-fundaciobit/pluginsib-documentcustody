package org.fundaciobit.plugins.documentcustody.arxiudigitalcaib.test.beans;

import javax.xml.bind.annotation.*;

import java.io.Serializable;

/**
 * 
 * @author anadal
 */

public class Interesado implements Serializable {

  @XmlElement
  private String nombre;
  @XmlElement
  private String apellido1;
  @XmlElement
  private String apellido2;
  @XmlElement
  private String documento;

  public Interesado() {
  }



  /**
   * @param nombre
   * @param apellido1
   * @param apellido2
   * @param documento
   */
  public Interesado(String nombre, String apellido1, String apellido2, String documento) {
    super();
    this.nombre = nombre;
    this.apellido1 = apellido1;
    this.apellido2 = apellido2;
    this.documento = documento;
  }



  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public String getApellido1() {
    return apellido1;
  }

  public void setApellido1(String apellido1) {
    this.apellido1 = apellido1;
  }

  public String getApellido2() {
    return apellido2;
  }

  public void setApellido2(String apellido2) {
    this.apellido2 = apellido2;
  }

  public String getDocumento() {
    return documento;
  }

  public void setDocumento(String documento) {
    this.documento = documento;
  }

}
