package org.fundaciobit.plugins.documentcustody.api.test;

import java.util.Date;

/**
 * 
 * @author anadal
 *
 */
public class Registre {

  private String id;

  private Date data;

  private int origen;

  private String estadoElaboracion;

  private String tipoDocumental;

  /**
   * 
   */
  public Registre() {
    super();
  }

  /**
   * @param id
   * @param data
   */
  public Registre(String id, Date data) {
    super();
    this.id = id;
    this.data = data;
  }
  
  
  

  /**
   * @param id
   * @param data
   * @param origen
   * @param estadoElaboracion
   * @param tipoDocumental
   */
  public Registre(String id, Date data, int origen, String estadoElaboracion,
      String tipoDocumental) {
    super();
    this.id = id;
    this.data = data;
    this.origen = origen;
    this.estadoElaboracion = estadoElaboracion;
    this.tipoDocumental = tipoDocumental;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Date getData() {
    return data;
  }

  public void setData(Date data) {
    this.data = data;
  }

  public int getOrigen() {
    return origen;
  }

  public void setOrigen(int origen) {
    this.origen = origen;
  }

  public String getEstadoElaboracion() {
    return estadoElaboracion;
  }

  public void setEstadoElaboracion(String estadoElaboracion) {
    this.estadoElaboracion = estadoElaboracion;
  }

  public String getTipoDocumental() {
    return tipoDocumental;
  }

  public void setTipoDocumental(String tipoDocumental) {
    this.tipoDocumental = tipoDocumental;
  }

}
