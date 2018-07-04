//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.05.19 at 03:36:18 PM CEST 
//


package es.caib.signatura.api;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="CodigoExterno" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Nombre" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Clase" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Tipo" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="AplicacionCustodia" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="RutaFichero" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "codigoExterno",
    "nombre",
    "clase",
    "tipo",
    "aplicacionCustodia",
    "rutaFichero"
})
@XmlRootElement(name = "Documento", namespace = "http://www.caib.es.signatura.custodia")
public class Documento {

    @XmlElement(name = "CodigoExterno", namespace = "http://www.caib.es.signatura.custodia", required = true)
    protected String codigoExterno;
    @XmlElement(name = "Nombre", namespace = "http://www.caib.es.signatura.custodia", required = true)
    protected String nombre;
    @XmlElement(name = "Clase", namespace = "http://www.caib.es.signatura.custodia", required = true)
    protected String clase;
    @XmlElement(name = "Tipo", namespace = "http://www.caib.es.signatura.custodia", required = true)
    protected String tipo;
    @XmlElement(name = "AplicacionCustodia", namespace = "http://www.caib.es.signatura.custodia", required = true)
    protected String aplicacionCustodia;
    @XmlElement(name = "RutaFichero", namespace = "http://www.caib.es.signatura.custodia", required = true)
    protected String rutaFichero;

    /**
     * Gets the value of the codigoExterno property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCodigoExterno() {
        return codigoExterno;
    }

    /**
     * Sets the value of the codigoExterno property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCodigoExterno(String value) {
        this.codigoExterno = value;
    }

    /**
     * Gets the value of the nombre property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Sets the value of the nombre property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNombre(String value) {
        this.nombre = value;
    }

    /**
     * Gets the value of the clase property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClase() {
        return clase;
    }

    /**
     * Sets the value of the clase property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClase(String value) {
        this.clase = value;
    }

    /**
     * Gets the value of the tipo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTipo() {
        return tipo;
    }

    /**
     * Sets the value of the tipo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTipo(String value) {
        this.tipo = value;
    }

    /**
     * Gets the value of the aplicacionCustodia property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAplicacionCustodia() {
        return aplicacionCustodia;
    }

    /**
     * Sets the value of the aplicacionCustodia property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAplicacionCustodia(String value) {
        this.aplicacionCustodia = value;
    }

    /**
     * Gets the value of the rutaFichero property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRutaFichero() {
        return rutaFichero;
    }

    /**
     * Sets the value of the rutaFichero property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRutaFichero(String value) {
        this.rutaFichero = value;
    }

}
