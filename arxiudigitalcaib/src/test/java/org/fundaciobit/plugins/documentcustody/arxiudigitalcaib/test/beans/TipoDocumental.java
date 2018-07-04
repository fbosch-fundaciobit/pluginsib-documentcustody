package org.fundaciobit.plugins.documentcustody.arxiudigitalcaib.test.beans;


/**
 * 
 * @author anadal
 *
 */
public class TipoDocumental  {


    private Long id;

    private String codigoNTI;

    private String entidad;

    public TipoDocumental() {
    }

    /**
     * @param id
     * @param codigoNTI
     * @param entidad
     */
    public TipoDocumental(Long id, String codigoNTI, String entidad) {
      super();
      this.id = id;
      this.codigoNTI = codigoNTI;
      this.entidad = entidad;
    }

    public Long getId() {
      return id;
    }

    public void setId(Long id) {
      this.id = id;
    }

    public String getCodigoNTI() {
      return codigoNTI;
    }

    public void setCodigoNTI(String codigoNTI) {
      this.codigoNTI = codigoNTI;
    }

    public String getEntidad() {
      return entidad;
    }

    public void setEntidad(String entidad) {
      this.entidad = entidad;
    }

   
}
