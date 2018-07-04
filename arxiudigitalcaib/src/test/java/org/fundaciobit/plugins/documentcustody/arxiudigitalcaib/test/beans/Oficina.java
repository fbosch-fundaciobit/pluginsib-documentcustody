package org.fundaciobit.plugins.documentcustody.arxiudigitalcaib.test.beans;


import java.io.Serializable;



/**
 * @author anadal (index)
 */

public class Oficina implements Serializable{


    private Long id;

    private String codigo;
   
    public Oficina() {
      super();
    }
    
    

    /**
     * @param id
     * @param codigo
     */
    public Oficina(Long id, String codigo) {
      super();
      this.id = id;
      this.codigo = codigo;
    }



    public Long getId() {
      return id;
    }

    public void setId(Long id) {
      this.id = id;
    }

    public String getCodigo() {
      return codigo;
    }

    public void setCodigo(String codigo) {
      this.codigo = codigo;
    }

    
    
}
