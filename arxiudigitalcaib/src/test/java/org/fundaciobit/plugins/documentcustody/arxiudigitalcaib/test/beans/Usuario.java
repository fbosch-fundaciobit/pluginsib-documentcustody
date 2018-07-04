package org.fundaciobit.plugins.documentcustody.arxiudigitalcaib.test.beans;

import java.io.Serializable;

/**
 * 
 * @author anadal
 *
 */
public class Usuario implements Serializable {

  private String nombre;

  private String apellido1;

  private String apellido2;

  private String documento;

  private String identificador;

  /**
     *
     */
  public Usuario() {
    super();
  }

  /**
   * @param nombre
   * @param apellido1
   * @param apellido2
   * @param documento
   * @param identificador
   */
  public Usuario(String nombre, String apellido1, String apellido2, String documento,
      String identificador) {
    super();
    this.nombre = nombre;
    this.apellido1 = apellido1;
    this.apellido2 = apellido2;
    this.documento = documento;
    this.identificador = identificador;
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

  public String getIdentificador() {
    return identificador;
  }

  public void setIdentificador(String identificador) {
    this.identificador = identificador;
  }

}
