package org.fundaciobit.plugins.documentcustody.arxiudigitalcaib.test.beans;



public class Libro  {

    private Long id;

    private String nombre;

    private String codigo;
   

    public Libro() {
    }

    public Libro(Long id, String codigo) {
        this.id = id;
        this.codigo = codigo;
        
    }

    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    
    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    
}
