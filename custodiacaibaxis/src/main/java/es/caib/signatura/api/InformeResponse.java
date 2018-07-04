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
 *         &lt;element ref="{urn:oasis:names:tc:dss:1.0:core:schema}Result"/>
 *         &lt;element ref="{http://www.caib.es.signatura.custodia}Documento" minOccurs="0"/>
 *         &lt;element ref="{http://www.caib.es.signatura.custodia}ResultadosInformeAccion" minOccurs="0"/>
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
    "result",
    "documento",
    "resultadosInformeAccion"
})
@XmlRootElement(name = "InformeResponse", namespace = "http://www.caib.es.signatura.custodia")
public class InformeResponse {

    @XmlElement(name = "Result", required = true)
    protected Result result;
    @XmlElement(name = "Documento", namespace = "http://www.caib.es.signatura.custodia")
    protected Documento documento;
    @XmlElement(name = "ResultadosInformeAccion", namespace = "http://www.caib.es.signatura.custodia")
    protected ResultadosInformeAccion resultadosInformeAccion;

    /**
     * Gets the value of the result property.
     * 
     * @return
     *     possible object is
     *     {@link Result }
     *     
     */
    public Result getResult() {
        return result;
    }

    /**
     * Sets the value of the result property.
     * 
     * @param value
     *     allowed object is
     *     {@link Result }
     *     
     */
    public void setResult(Result value) {
        this.result = value;
    }

    /**
     * Gets the value of the documento property.
     * 
     * @return
     *     possible object is
     *     {@link Documento }
     *     
     */
    public Documento getDocumento() {
        return documento;
    }

    /**
     * Sets the value of the documento property.
     * 
     * @param value
     *     allowed object is
     *     {@link Documento }
     *     
     */
    public void setDocumento(Documento value) {
        this.documento = value;
    }

    /**
     * Gets the value of the resultadosInformeAccion property.
     * 
     * @return
     *     possible object is
     *     {@link ResultadosInformeAccion }
     *     
     */
    public ResultadosInformeAccion getResultadosInformeAccion() {
        return resultadosInformeAccion;
    }

    /**
     * Sets the value of the resultadosInformeAccion property.
     * 
     * @param value
     *     allowed object is
     *     {@link ResultadosInformeAccion }
     *     
     */
    public void setResultadosInformeAccion(ResultadosInformeAccion value) {
        this.resultadosInformeAccion = value;
    }

}
