package org.fundaciobit.plugins.documentcustody.arxiudigitalcaib;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.fundaciobit.plugins.documentcustody.api.AbstractDocumentCustodyPlugin;
import org.fundaciobit.plugins.documentcustody.api.AnnexCustody;
import org.fundaciobit.plugins.documentcustody.api.CustodyException;
import org.fundaciobit.plugins.documentcustody.api.DocumentCustody;
import org.fundaciobit.plugins.documentcustody.api.IDocumentCustodyPlugin;
import org.fundaciobit.plugins.documentcustody.api.NotSupportedCustodyException;
import org.fundaciobit.plugins.documentcustody.api.SignatureCustody;
import org.fundaciobit.plugins.utils.AbstractPluginProperties;
import org.fundaciobit.plugins.utils.Base64;
import org.fundaciobit.plugins.utils.Metadata;
import org.fundaciobit.plugins.utils.MetadataConstants;
import org.fundaciobit.plugins.utils.MetadataFormatException;
import org.fundaciobit.plugins.utils.XTrustProvider;

import es.caib.arxiudigital.apirest.ApiArchivoDigital;
import es.caib.arxiudigital.apirest.CSGD.entidades.resultados.CreateDraftDocumentResult;
import es.caib.arxiudigital.apirest.constantes.Aspectos;
import es.caib.arxiudigital.apirest.constantes.CodigosResultadoPeticion;
import es.caib.arxiudigital.apirest.constantes.FormatosFichero;
import es.caib.arxiudigital.apirest.constantes.TiposFirma;
import es.caib.arxiudigital.apirest.constantes.TiposObjetoSGD;
import es.caib.arxiudigital.apirest.facade.pojos.CabeceraPeticion;
import es.caib.arxiudigital.apirest.facade.pojos.Directorio;
import es.caib.arxiudigital.apirest.facade.pojos.Documento;
import es.caib.arxiudigital.apirest.facade.pojos.Expediente;
import es.caib.arxiudigital.apirest.facade.pojos.FiltroBusquedaFacilExpedientes;
import es.caib.arxiudigital.apirest.facade.pojos.FirmaDocumento;
import es.caib.arxiudigital.apirest.facade.pojos.Nodo;
import es.caib.arxiudigital.apirest.facade.resultados.Resultado;
import es.caib.arxiudigital.apirest.facade.resultados.ResultadoBusqueda;
import es.caib.arxiudigital.apirest.facade.resultados.ResultadoSimple;
import es.caib.arxiudigital.apirest.utils.UtilidadesFechas;

/**
 * Implementació del plugin de custodia documental que guarda els documents dins
 * de l'Arxiu Digital de la CAIB
 * 
 * @author anadal
 */
@SuppressWarnings("deprecation")
public class ArxiuDigitalCAIBDocumentCustodyPlugin extends AbstractPluginProperties implements
    IDocumentCustodyPlugin {

  protected final Logger log = Logger.getLogger(getClass());

  public static final long SLEEP_SEND_TIMEOUT = 5000;

  public static final String FORMATO_UTF8 = "UTF-8";
  
  public static final String EXTENSIO_DOCUMENT_RESERVA = ".document_electronic_caib_reserva";

  public static final String FILE_INFO_NOM = "nom";
  public static final String FILE_INFO_TAMANY = "tamany";
  public static final String FILE_INFO_MIME = "mime";

  public static final String FILE_INFO_SIGNATURE_TYPE = "signtype";

  /**
   * true if data contains original document (attached). false if data does not
   * contain original document (dettached). null can not obtain this information
   * or is not necessary
   */
  public static final String FILE_INFO_SIGNATURE_ATTACHED_DOCUMENT = "attachedDocument";

  public static final String FILE_INFO_BASE_DOCUMENT = "document.";

  public static final String FILE_INFO_BASE_FIRMA = "firma.";

  public static final String FILE_INFO_BASE_METADATA = "metadata.";

  public static final String ARXIUDIGITALCAIB_PROPERTY_BASE = DOCUMENTCUSTODY_BASE_PROPERTY
      + "arxiudigitalcaib.";

  public static final Map<String, TiposFirma> TIPOFIRMA_SIMPLE = new HashMap<String, TiposFirma>();

  public static final Map<String, TiposFirma> TIPOFIRMA_COMPLEXE = new HashMap<String, TiposFirma>();

  public static final Map<String, FormatosFichero> FORMATS_BY_EXTENSION = new HashMap<String, FormatosFichero>();
  
  
  // NO MODIFICAR MAI !!!!!!!!
  public static final String NO_MODIFY_TEXT = "!!! NO MODIFICAR - DO NOT MODIFY !!!";
  
  public static final String NO_MODIFY_TEXT_URL_ENCODED;

  static {

    TIPOFIRMA_SIMPLE.put(SignatureCustody.PADES_SIGNATURE, TiposFirma.PADES);

    /**
     * true if signature contains original document (attached). false if
     * signature does not contain original document (dettached). null can not
     * obtain this information or is not necessary
     */

    TIPOFIRMA_COMPLEXE.put(SignatureCustody.CADES_SIGNATURE + "_null",
        TiposFirma.CADES_ATTACHED);
    TIPOFIRMA_COMPLEXE.put(SignatureCustody.CADES_SIGNATURE + "_true",
        TiposFirma.CADES_ATTACHED);
    TIPOFIRMA_COMPLEXE.put(SignatureCustody.CADES_SIGNATURE + "_false",
        TiposFirma.CADES_DETACHED);

    TIPOFIRMA_COMPLEXE.put(SignatureCustody.XADES_SIGNATURE + "_true",
        TiposFirma.XADES_ENVELOPED);

    FORMATS_BY_EXTENSION.put("wfs", FormatosFichero.WFS);
    FORMATS_BY_EXTENSION.put("wms", FormatosFichero.WMS);
    FORMATS_BY_EXTENSION.put("gzip", FormatosFichero.GZIP);
    FORMATS_BY_EXTENSION.put("zip", FormatosFichero.ZIP);
    FORMATS_BY_EXTENSION.put("avi", FormatosFichero.AVI);
    FORMATS_BY_EXTENSION.put("mp4a", FormatosFichero.MP4A);
    FORMATS_BY_EXTENSION.put("csv", FormatosFichero.CSV);
    FORMATS_BY_EXTENSION.put("html", FormatosFichero.HTML);
    FORMATS_BY_EXTENSION.put("htm", FormatosFichero.HTML);
    FORMATS_BY_EXTENSION.put("css", FormatosFichero.CSS);
    FORMATS_BY_EXTENSION.put("jpeg", FormatosFichero.JPEG);
    FORMATS_BY_EXTENSION.put("jpg", FormatosFichero.JPEG);
    FORMATS_BY_EXTENSION.put("mhtml", FormatosFichero.MHTML);
    FORMATS_BY_EXTENSION.put("oasis12", FormatosFichero.OASIS12);
    FORMATS_BY_EXTENSION.put("soxml", FormatosFichero.SOXML);
    FORMATS_BY_EXTENSION.put("pdf", FormatosFichero.PDF);
    // FORMATS_BY_EXTENSION.put("pdfa", FormatosFichero.PDFA);
    FORMATS_BY_EXTENSION.put("png", FormatosFichero.PNG);
    FORMATS_BY_EXTENSION.put("rtf", FormatosFichero.RTF);
    FORMATS_BY_EXTENSION.put("svg", FormatosFichero.SVG);
    FORMATS_BY_EXTENSION.put("tiff", FormatosFichero.TIFF);
    FORMATS_BY_EXTENSION.put("txt", FormatosFichero.TXT);
    FORMATS_BY_EXTENSION.put("xhtml", FormatosFichero.XHTML);
    FORMATS_BY_EXTENSION.put("mp3", FormatosFichero.MP3);
    FORMATS_BY_EXTENSION.put("ogg", FormatosFichero.OGG);
    FORMATS_BY_EXTENSION.put("mp4v", FormatosFichero.MP4V);
    FORMATS_BY_EXTENSION.put("webm", FormatosFichero.WEBM);

    String tmp;
    try {
      
     tmp = URLEncoder.encode("#" + NO_MODIFY_TEXT, "UTF-8");
      
    } catch(Throwable e) {
      
      tmp = URLEncoder.encode("#" + NO_MODIFY_TEXT);
    }
    
    NO_MODIFY_TEXT_URL_ENCODED = tmp;
    
  }

  // ----------------------------------------------------------------------------
  // ---------------------------- CONSTRUCTORS -----------------------------
  // ----------------------------------------------------------------------------

  /**
   * 
   */
  public ArxiuDigitalCAIBDocumentCustodyPlugin() {
    super();
  }

  /**
   * @param propertyKeyBase
   * @param properties
   */
  public ArxiuDigitalCAIBDocumentCustodyPlugin(String propertyKeyBase, Properties properties) {
    super(propertyKeyBase, properties);
  }

  /**
   * @param propertyKeyBase
   */
  public ArxiuDigitalCAIBDocumentCustodyPlugin(String propertyKeyBase) {
    super(propertyKeyBase);
  }

  // ----------------------------------------------------------------------------
  // ---------------------------- P R O P I E T A T S -------------------------
  // ----------------------------------------------------------------------------

  private String getPropertyBase() {
    return ARXIUDIGITALCAIB_PROPERTY_BASE;
  }

  public String getPropertyNomExpedientEL() throws Exception {
    return getPropertyRequired(ARXIUDIGITALCAIB_PROPERTY_BASE + "nom_expedient_EL");
  }

  public String getPropertyNomCarpetaEL() throws Exception {
    return getProperty(ARXIUDIGITALCAIB_PROPERTY_BASE + "nom_carpeta_EL");
  }

  public String getPropertySerieDocumentalEL() throws Exception {
    return getPropertyRequired(ARXIUDIGITALCAIB_PROPERTY_BASE + "serie_documental_EL");
  }

  public boolean isPropertyCreateDraft_EL(Map<String, Object> custodyParameters)  {
    
    String valueStr = getProperty(ARXIUDIGITALCAIB_PROPERTY_BASE + "createDraft_EL");
    
    if (valueStr == null || valueStr.trim().length() == 0) {
      return true;
    }
    
    valueStr = processOptionalProperty("CreateDraft_EL", valueStr, custodyParameters);
    if (isDebug()) {
      log.info("process(CreateDraft_EL)=[" + valueStr + "]");
    }

    if ("false".equals(valueStr.toLowerCase())) {
      return false;
    } else {
      return true;
    }
  }
  
  
  public boolean isPropertyTancarExpedient_EL(Map<String, Object> custodyParameters) {
    String valueStr = getProperty(ARXIUDIGITALCAIB_PROPERTY_BASE + "tancarExpedient_EL");
    
    
    if (valueStr == null || valueStr.trim().length() == 0) {
      return false;
    }
    
    valueStr = processOptionalProperty("tancarExpedient_EL", valueStr, custodyParameters);

    if (isDebug()) {
      log.info("process(tancarExpedient_EL)=[" + valueStr + "]");
    }
    
    if ("true".equals(valueStr.toLowerCase())) {
      return true;
    } else {
      return false;
    }
  }
  
  
  
  public boolean isSearchIfExpedientExistsInReserve() {
    String valueStr = getProperty(ARXIUDIGITALCAIB_PROPERTY_BASE + "searchifexpedientexistsinreserve");

    if (valueStr == null || valueStr.trim().length() == 0) {
      return false;
    }
    
    if ("true".equals(valueStr.toLowerCase())) {
      return true;
    } else {
      return false;
    }
  }
  
  public boolean isIgnoreErrorWhenTancarExpedient() {
    String valueStr = getProperty(ARXIUDIGITALCAIB_PROPERTY_BASE + "ignoreerrorwhentancarexpedient");
    if ("true".equals(valueStr)) {
      return true;
    } else {
      return false;
    }
  }
  

  /**
   * Separats per comma
   * 
   * @return
   * @throws Exception
   */
  public String getPropertyOrgansEL() throws Exception {
    return getPropertyRequired(ARXIUDIGITALCAIB_PROPERTY_BASE + "organs_EL");
  }

  public String getPropertyCodiProcedimentEL() throws Exception {
    return getPropertyRequired(ARXIUDIGITALCAIB_PROPERTY_BASE + "codi_procediment_EL");
  }

  public String getPropertyDataCreacioDocumentEL() throws Exception {
    return getProperty(ARXIUDIGITALCAIB_PROPERTY_BASE + "data_creacio_document_EL");
  }
  
  public String getPropertyDataCreacioExpedientEL() throws Exception {
    return getProperty(ARXIUDIGITALCAIB_PROPERTY_BASE + "data_creacio_expedient_EL");
  }

  public String getPropertyEstatElaboracioEL() throws Exception {
    return getPropertyRequired(ARXIUDIGITALCAIB_PROPERTY_BASE + "estat_elaboracio_EL");
  }

  public String getPropertyTipusDocumentalEL() throws Exception {
    return getPropertyRequired(ARXIUDIGITALCAIB_PROPERTY_BASE + "tipus_documental_EL");
  }
  
  public String getPropertyPerfilFirmaEL() throws Exception {
    return getPropertyRequired(ARXIUDIGITALCAIB_PROPERTY_BASE + "perfil_firma_EL");
  }


  /**
   * ADMINISTRACION ("1"), CIUDADANO ("0");
   * 
   * @return
   * @throws Exception
   */
  public String getPropertyOrigenDocumentEL() throws Exception {
    return getPropertyRequired(ARXIUDIGITALCAIB_PROPERTY_BASE + "origen_document_EL");
  }

  public String getPropertyURL() throws Exception {
    return getPropertyRequired(ARXIUDIGITALCAIB_PROPERTY_BASE + "connexio.url");
  }

  public String getPropertyCodiAplicacio() throws Exception {
    return getPropertyRequired(ARXIUDIGITALCAIB_PROPERTY_BASE + "connexio.codi_aplicacio");
  }

  public String getPropertyOrganitzacio() throws Exception {
    return getPropertyRequired(ARXIUDIGITALCAIB_PROPERTY_BASE + "connexio.organitzacio");
  }

  public String getPropertyLoginUsername() throws Exception {
    return getPropertyRequired(ARXIUDIGITALCAIB_PROPERTY_BASE + "connexio.login.username");
  }

  public String getPropertyLoginPassword() throws Exception {
    return getPropertyRequired(ARXIUDIGITALCAIB_PROPERTY_BASE + "connexio.login.password");
  }

  // Ciutada - Nom. Opcional
  public String getPropertySolicitantNomEL() throws Exception {
    return getProperty(ARXIUDIGITALCAIB_PROPERTY_BASE + "connexio.solicitant.nom_EL");
  }

  // Ciutada - NIF. Opcional
  public String getPropertySolicitantIdentificadorAdministratiuEL() throws Exception {
    return getProperty(ARXIUDIGITALCAIB_PROPERTY_BASE
        + "connexio.solicitant.identificador_administratiu_EL");
  }

  // Funcionari o client de l'aplicació - Username. Opcional
  public String getPropertyUsuariUsernameEL() throws Exception {
    return getProperty(ARXIUDIGITALCAIB_PROPERTY_BASE + "connexio.usuari.username_EL");
  }

  // Funcionari o client de l'aplicació - NIF. Opcional
  public String getPropertyUsuariIdentificadorAdministratiuEL() throws Exception {
    return getProperty(ARXIUDIGITALCAIB_PROPERTY_BASE
        + "connexio.usuari.identificador_administratiu_EL");
  }

  // Nom de Procediment (opcional)
  public String getPropertyNomProcedimentEL() throws Exception {
    return getProperty(ARXIUDIGITALCAIB_PROPERTY_BASE + "connexio.nom_procediment_EL");
  }

  public boolean isPropertyIgnoreServerCeriticates() throws Exception {
    String ignoreStr = getProperty(ARXIUDIGITALCAIB_PROPERTY_BASE
        + "connexio.ignore_server_certificates");
    if (ignoreStr == null || ignoreStr.trim().length() == 0) {
      return false;
    } else {
      return "true".equals(ignoreStr.trim());
    }
  }

  private Boolean isDebugValue = null;

  public boolean isDebug() {
    if (isDebugValue == null) {
      isDebugValue = log.isDebugEnabled()
          || "true".equals(getProperty(ARXIUDIGITALCAIB_PROPERTY_BASE + "debug"));
    }
    return isDebugValue;
  }

  // ----------------------------------------------------------------------------
  // ----------------------- METODES PRINCIPALS -------------------------------
  // ----------------------------------------------------------------------------

  @Override
  public String reserveCustodyID(Map<String, Object> custodyParameters)
      throws CustodyException {
    // Crearem una estructura
    // EXPEDIENT => CARPETA => DOCUMENT o
    // EXPEDIENT => DOCUMENT 
    final boolean debug = isDebug();
    try {
      final String nomExpedient = processEL(getPropertyNomExpedientEL(), custodyParameters);
      final String nomCarpeta = processELNullIfEmpty(getPropertyNomCarpetaEL(),
          custodyParameters);

      if (debug) {
        log.info("NomExpedient: " + nomExpedient);
        log.info("NomCarpeta: " + nomCarpeta);
      }

      //String uuidCSVFile = null;
      //String carpetaID = null;

      ApiArchivoDigital api = getApiArxiu(custodyParameters);
      
      // Només miram si existeix l'expedient 
      Expediente expedientCercat = null;
      
      if (isSearchIfExpedientExistsInReserve()) {

        FiltroBusquedaFacilExpedientes filtrosRequeridos = new FiltroBusquedaFacilExpedientes();
        filtrosRequeridos.setName(nomExpedient);
        filtrosRequeridos.setAppName(getPropertyCodiAplicacio());
        String serieDocumental = processEL(getPropertySerieDocumentalEL(), custodyParameters);
        filtrosRequeridos.setDocSeries(serieDocumental);
        
        if (debug) {
          log.info(" CERCA[Name] => " + filtrosRequeridos.getName() );
          log.info(" CERCA[AppName] => " + filtrosRequeridos.getAppName() );
          log.info(" CERCA[serieDocumental] => " + filtrosRequeridos.getDocSeries());
        }

        ResultadoBusqueda<Expediente> res;
        res = api.busquedaFacilExpedientes(filtrosRequeridos, null, 0);
        if (hiHaErrorEnCerca(res.getCodigoResultado())) {
          throw new CustodyException("Error Consultant si Expedient " + nomExpedient
              + " existeix: " + res.getCodigoResultado() + "-" + res.getMsjResultado());
        }
  
        List<Expediente> llista2 = res.getListaResultado();

        if (llista2 == null || llista2.size() == 0) {
          log.info(" CERCA[].size() = Llista null o buida (" + llista2.size() + ")");
          expedientCercat = null;
        } else {
          log.info(" CERCA[].size() = " + llista2.size());
          // TODO la cerca es fa del nom parescut al fitxer, per exemple
          // si cerques "Registre_20" et pot trobar Registre_20,
          // Registre_200, Registre_202, ...
          int countTrobats = 0;
          final int total = res.getNumeroTotalResultados();
          int parcial = 0;
          int pagina = 0;
          do {
            for (Expediente expediente : llista2) {
              parcial++;
              if (nomExpedient.equals(expediente.getName())) {
                countTrobats++;
                if (countTrobats > 1) {
                  log.error(" S'ha trobat coincidencia multiple " + expediente.getName() + " ("
                      + expediente.getId() + ") per la cerca de nomExpedient " + nomExpedient
                      + ")");
                } else {
                  expedientCercat = expediente;
                }
              }
            }
            
            if (countTrobats != 0) {
              break;
            }
            
            if (parcial <= total) {
              break;
            }
            pagina++;
            res = api.busquedaFacilExpedientes(filtrosRequeridos, null, pagina);
            if (hiHaErrorEnCerca(res.getCodigoResultado())) {
              throw new CustodyException("Error Consultant si Expedient " + nomExpedient
                  + " existeix: " + res.getCodigoResultado() + "-" + res.getMsjResultado());
            }
            
            llista2 = res.getListaResultado();
          } while(true);
  
          if (countTrobats == 0) {
            expedientCercat = null;
          } else if (countTrobats == 1) {
            // expedientCercat ja conté el valor
          } else if (countTrobats > 1) {
            // Hi ha multiple instancies que s'ajusten. No se quin triar
            String msg = "S'ha trobat coincidencia multiple " + expedientCercat.getName() + " ("
                + expedientCercat.getId() + ") per la cerca de nomExpedient " + nomExpedient
                + "). Veure logs per la resta de coincidències.";
            log.error(msg);
            throw new CustodyException(msg);
          }
        }
      }

      // log.info("Creacio Expedient::Llistat.SIZE: " + llista.size());
      String expedientID;
      String carpetaID = null;
      if (expedientCercat == null) {
        // Cream l'expedient
        Map<String, Object> llistaMetadades = getMetadadesPerExpedient(custodyParameters);

        Expediente miExpediente = new Expediente();

        miExpediente.expedienteParaCrear(true);
        miExpediente.setName(nomExpedient);
        miExpediente.setMetadataCollection(llistaMetadades);

        Resultado<Expediente> resultado = api.crearExpediente(miExpediente, true);

        String codErr = resultado.getCodigoResultado();
        if (hiHaError(codErr)) {
          
          if ("COD_021".equals(codErr) 
              && resultado.getMsjResultado().startsWith("Duplicate child name not allowed")) {
            throw new CustodyException("Error creant expedient " + nomExpedient + ": "
               + "Ja existeix un expedient amb aquest nom (la propietat de "
               + "configuracio nom_expedient_EL ha de generar noms únics)");
          } else {
            throw new CustodyException("Error creant expedient " + nomExpedient + ": "
              + resultado.getCodigoResultado() + "-" + resultado.getMsjResultado());
          }
        }

        expedientID = resultado.getElementoDevuelto().getId();

        if (debug) {
          log.info("Creacio Expedient:: CREAT: " + expedientID);
        }

      } else {

        // Expedient Ja Existeix

        expedientID = expedientCercat.getId();

        if (debug) {
          log.info("Creacio Expedient:: JA EXISTEIX: " + expedientID);
        }

        if (nomCarpeta != null) {

          List<Nodo> nodos = expedientCercat.getChilds();
          if (nodos == null || nodos.size() == 0) {
            if (debug) {
              log.info("Creacio Expedient:: Fills Expedients:  NO EN TE (OK)!!!!!");
            }
          } else {
            for (Nodo nodo : nodos) {
              // TiposObjetoSGD.DIRECTORIO
              if (debug) {
                log.info("Creacio Expedient:: Fills Expedients: " + nodo.getName() + " ["
                    + nodo.getType().getValue() + "]");
              }
              if (nomCarpeta.equals(nodo.getName())
                  && TiposObjetoSGD.DIRECTORIO.equals(nodo.getType())) {
                carpetaID = nodo.getId();
              }
            }
          }
        }

      }

      // (3) === CREAM CARPETA

      // (3.1) Si carpeta es null llavors no fa falta crear la carpeta
      if (nomCarpeta != null) {

        if (carpetaID == null)   {
          // Carpeta No Existeix

          Resultado<Directorio> carpeta = api.crearDirectorio(nomCarpeta, expedientID);

          if (hiHaError(carpeta.getCodigoResultado())) {
            throw new CustodyException("Error creant carpeta " + nomCarpeta + ": "
                + carpeta.getCodigoResultado() + "-" + carpeta.getMsjResultado());
          }

          Directorio directorio = carpeta.getElementoDevuelto();

          carpetaID = directorio.getId();
          if (debug) {
            log.info("Creacio Carpeta:: CREAT: " + carpetaID);
          }
        } else {
          // OK Carpeta està creada
        }
        
      }

      // Afegir CSV al document de informació per a que després, ho pugui
      // recuperar durant l'alta del document/firma
      Resultado<String> resCSV = api.generarCSV();
      if (hiHaError(resCSV.getCodigoResultado())) {
        throw new CustodyException("Error cridant a generar CSV: "
          + resCSV.getCodigoResultado() + "-" + resCSV.getMsjResultado());
      }
      String csv = resCSV.getElementoDevuelto(); 
      if (debug) {
        log.info("CODI SEGUR GENERAT = " + csv);
      }

      // Cream fitxer draft temporal amb informació del CSV, carpeta i expedient
      String uuidCSVFile = createSimpleDocCSV(api, csv, expedientID, carpetaID);

      return new ExpedientCarpetaDocument(expedientID, carpetaID, uuidCSVFile).encodeCustodyID();

    } catch (CustodyException ce) {
      throw ce;
    } catch (Exception e) {
      throw new CustodyException("Error reservant CustodyID: " + e.getMessage(), e);
    }

  }

  @Override
  public String getValidationUrl(String custodyID, Map<String, Object> custodyParameters)
      throws CustodyException {
    final String baseUrl = null;
    String baseUrlEL = getProperty(getPropertyBase() + "baseurl_EL");

    String hashPassword = getProperty(getPropertyBase()
        + AbstractDocumentCustodyPlugin.ABSTRACT_HASH_PASSWORD, "");

    // Valid values MD2, MD5, SHA,SHA-256,SHA-384,SHA-512
    String hashAlgorithm = getProperty(getPropertyBase()
        + AbstractDocumentCustodyPlugin.ABSTRACT_HASH_ALGORITHM, "MD5");

    Map<String, Object> custodyParametersExtended = new HashMap<String, Object>();

    // Recuperam el CSV
    custodyParametersExtended.put("csv", getCSV(custodyID, custodyParameters));
    custodyParametersExtended.putAll(custodyParameters);

    return AbstractDocumentCustodyPlugin.getValidationUrlStatic(custodyID,
        custodyParametersExtended, baseUrl, baseUrlEL, hashAlgorithm, hashPassword, log);
  }

  @Override
  public String getSpecialValue(String custodyID, Map<String, Object> custodyParameters)
      throws CustodyException {
    String specialValue = getProperty(getPropertyBase() + "specialValue_EL");
    if (specialValue == null || specialValue.trim().length() == 0) {
      return custodyID;
    } else {

      Map<String, Object> custodyParametersExtended = new HashMap<String, Object>();

      // Recuperam el CSV
      custodyParametersExtended.put("csv", getCSV(custodyID, custodyParameters));
      custodyParametersExtended.putAll(custodyParameters);

      return AbstractDocumentCustodyPlugin.processExpressionLanguage(specialValue,
          custodyParameters);
    }
  }

  @Override
  public void deleteCustody(String custodyID) throws CustodyException,
      NotSupportedCustodyException {
    ExpedientCarpetaDocument ec = ExpedientCarpetaDocument.decodeCustodyID(custodyID);

    try {
      ApiArchivoDigital api = getApiArxiu(null);
      if (ec.carpetaID == null) {
        api.eliminarExpediente(ec.expedientID);
      } else {
        ResultadoSimple rs = api.eliminarDirectorio(ec.carpetaID);

        if (hiHaError(rs.getCodigoResultado())) {
          log.warn("Error Esborrant Carpeta " + ec.carpetaID + ": " + rs.getCodigoResultado()
              + "-" + rs.getMsjResultado());
        }

        // TODO Eliminar expedient quan aquest no tengui fills ?????
      }
    } catch (Exception e) {
      throw new CustodyException("Error esborrant expedient amb uuid " + ec.expedientID + ": "
          + e.getMessage(), e);
    }

  }

  @Override
  public boolean supportsDeleteCustody() {
    return true;
  }

  @Override
  public void saveAll(String custodyID, Map<String, Object> custodyParameters,
      DocumentCustody documentCustody, SignatureCustody signatureCustody, Metadata[] metadata)
      throws CustodyException, NotSupportedCustodyException, MetadataFormatException {

    createUpdateAll(custodyID, custodyParameters, documentCustody, signatureCustody, metadata);
  }

  protected void createUpdateAll(String custodyID, Map<String, Object> custodyParameters,
      DocumentCustody documentCustody, SignatureCustody signatureCustody, Metadata[] metadatas)
      throws CustodyException {

    // boolean overWriteDocument, boolean overWriteSignature, boolean
    // overWriteMetadatas

    final boolean debug = isDebug();
    
    // CHECKS 
    final String nomFitxerDocument; 
    if (documentCustody != null) {
      if (signatureCustody == null) {
        
        
        if (!isPropertyCreateDraft_EL(custodyParameters)) {
          //   TODO TRADUIR
          throw new CustodyException("No podem guardar un document pla en mode NO BORRADOR (NO DRAFT)");
        }
      } 
      // ONLY FILE && DETACHED
      nomFitxerDocument  = documentCustody.getName();
    } else {
      // ATTACHED
      nomFitxerDocument = signatureCustody.getName();
    }

    if (isPropertyTancarExpedient_EL(custodyParameters) && isPropertyCreateDraft_EL(custodyParameters)) {
      // Son Incompatibles
      throw new CustodyException("La combinació de crear borradors (creatDraft) i de "
          + "tancar expedient (tancarExpedient) són incompatibles."); 
    }
    
    
    try {

      // 1.- Miram si el Document Electrònic Existeix
      ExpedientCarpetaDocument ecd = ExpedientCarpetaDocument.decodeCustodyID(custodyID);

      ApiArchivoDigital apiArxiu = getApiArxiu(custodyParameters);
      
      Documento doc = getDocumento(apiArxiu, ecd.documentID, false);

      if (doc == null) {
        throw new CustodyException("El document amb uuid " + ecd.documentID 
            + " no existeix en l'expedient/carpeta (" + ecd.expedientID + "/" 
            + ecd.carpetaID + ")");     
      }
      
      
      String csv2 = (String)doc.getMetadataCollection().get(MetadataConstants.ENI_CSV);
      
      if (csv2 == null) {
        log.error("S'ha intentat llegir el CSV del fitxer amb uuid " + ecd.documentID 
            + " però aquest val null");
      }
      
      
      final String documentElectronicID;

      if ((csv2 + EXTENSIO_DOCUMENT_RESERVA).equals(doc.getName())) {
        //csv = readSimpleDocCSV(apiArxiu, ecd.documentID);
        documentElectronicID = null;
        // EL CSV s'ha generat en la reserva i s'ha de guardat en
        // el fitxer de INFO. S'ha de recuperar del fitxer de Informacio.
        if (debug) {
          log.info("CODI SEGUR RECUPERAT = " + csv2);
        }
      } else {
        documentElectronicID = ecd.documentID;
      }

      /*
      // 1.1.- Cercam si hi ha l'expedient
      Resultado<Expediente> expedient = apiArxiu.obtenerExpediente(ec.expedientID);

      if (hiHaError(expedient.getCodigoResultado())) {
        // Checks
        throw new CustodyException("Error llegint expedient amb uuid " + ecd.expedientID + "("
            + expedient.getCodigoResultado() + ":" + expedient.getMsjResultado() + ")",
            new Exception());
      }

      // 1.2.- Cercam Fitxers dins la Carpeta (si en té) o directament dins
      // l'expedient
     

      // 1.3.- Cercam o el document o el fitxer CSV
      String documentElectronicID = null;
      String csv = null;
      Nodo nodoCSV = null;
      {
        FilesInfo filesInfo = getFileInfo(ec, nodosByName);

        if (filesInfo.nodoDocumentElectronic != null) {
          documentElectronicID = filesInfo.nodoDocumentElectronic.getId();
        } else {
          csv = readSimpleDocCSV(apiArxiu, filesInfo.nodoCSV.getId());
          nodoCSV = filesInfo.nodoCSV;
        }
      }
      */

      // =================================== INICIALITZACIO
      final Properties metasAndInfo;
      

      final Map<String, Object> generatedMetadataCollection = getMetadadesPerDocument(
          custodyParameters, signatureCustody);

      Map<String, Object> metadataCollection;
      metadataCollection = doc.getMetadataCollection();
      
      metadataCollection.putAll(generatedMetadataCollection);
      
      if (documentElectronicID == null) {

        // CREAR DOCUMENT-ElECTRONIC (canviar CSV per DOCUMENT-ElECTRONIC)
        metasAndInfo = new Properties();

        //documento = new Documento();
        // String idNuevoDocumento = SENSE_VALOR;

        // Configuració paràmetres document
        List<Aspectos> aspects = new ArrayList<Aspectos>();
        aspects.add(Aspectos.INTEROPERABLE);
        aspects.add(Aspectos.TRANSFERIBLE);
        
        
        doc.setId(ecd.documentID);
        doc.setAspects(aspects);

        doc.setEncoding(FORMATO_UTF8);

      } else {

        // ACTUALITZAR DOCUMENT-ELECTRONIC
        
        // Actualitzam les metadades amb les metadades generades
        metadataCollection.putAll(generatedMetadataCollection);

        // Per que hem de treure tot el document. Amb la informació de
        // eni:description n'hi ha suficient ?

        String metasAndInfoStr = (String) metadataCollection
            .get(MetadataConstants.ENI_DESCRIPCION);

        metasAndInfo = stringEniDescriptionToPropertiesCustody(metasAndInfoStr);
        
        if(metasAndInfo.containsKey(MetadataConstants.ENI_DESCRIPCION) 
            && !generatedMetadataCollection.containsKey(MetadataConstants.ENI_DESCRIPCION) ) {
          metadataCollection.put(MetadataConstants.ENI_DESCRIPCION, metasAndInfo.getProperty(MetadataConstants.ENI_DESCRIPCION));
        }

      }

      // ======================== CODI COMU
      if (debug) {
        for (String key : metadataCollection.keySet()) {
          log.info("Metadata Document [" + key + "] => " + metadataCollection.get(key));
        }
      }

      
      if (documentCustody == null) {

        metasAndInfo.remove(FILE_INFO_BASE_DOCUMENT + FILE_INFO_NOM);
        metasAndInfo.remove(FILE_INFO_BASE_DOCUMENT + FILE_INFO_TAMANY);
        metasAndInfo.remove(FILE_INFO_BASE_DOCUMENT + FILE_INFO_MIME);

      } else {
        byte[] data = documentCustody.getData();
        String mimetype = documentCustody.getMime();

        metasAndInfo.setProperty(FILE_INFO_BASE_DOCUMENT + FILE_INFO_NOM,
            documentCustody.getName());
        metasAndInfo.setProperty(FILE_INFO_BASE_DOCUMENT + FILE_INFO_TAMANY,
            String.valueOf(data.length));
        if (mimetype == null) {
          throw new CustodyException("No s'ha definit el mime type del DocumentCustody");
        }
        metasAndInfo.setProperty(FILE_INFO_BASE_DOCUMENT + FILE_INFO_MIME, mimetype);
      }
      

      
      if (signatureCustody == null) {

        metasAndInfo.remove(FILE_INFO_BASE_FIRMA + FILE_INFO_NOM);
        metasAndInfo.remove(FILE_INFO_BASE_FIRMA + FILE_INFO_TAMANY);
        metasAndInfo.remove(FILE_INFO_BASE_FIRMA + FILE_INFO_MIME);

        metasAndInfo.remove(FILE_INFO_BASE_FIRMA + FILE_INFO_SIGNATURE_ATTACHED_DOCUMENT);
        metasAndInfo.remove(FILE_INFO_BASE_FIRMA + FILE_INFO_SIGNATURE_TYPE);

      } else {

        final byte[] data = signatureCustody.getData();
        String mimeType = signatureCustody.getMime();
        
        if (!doc.getAspects().contains(Aspectos.FIRMADO)) {
          doc.getAspects().add(Aspectos.FIRMADO);
        }

        metasAndInfo.setProperty(FILE_INFO_BASE_FIRMA + FILE_INFO_NOM,
            signatureCustody.getName());
        metasAndInfo.setProperty(FILE_INFO_BASE_FIRMA + FILE_INFO_TAMANY,
            String.valueOf(data.length));
        if (mimeType == null) {
          throw new CustodyException("No s'ha definit el mime type del SignatureCustody");
        }
        metasAndInfo.setProperty(FILE_INFO_BASE_FIRMA + FILE_INFO_MIME, mimeType);

        if (signatureCustody.getAttachedDocument() == null) {
          metasAndInfo.remove(FILE_INFO_BASE_FIRMA + FILE_INFO_SIGNATURE_ATTACHED_DOCUMENT);
        } else {
          metasAndInfo.setProperty(FILE_INFO_BASE_FIRMA
              + FILE_INFO_SIGNATURE_ATTACHED_DOCUMENT,
              String.valueOf(signatureCustody.getAttachedDocument()));
        }
        metasAndInfo.setProperty(FILE_INFO_BASE_FIRMA + FILE_INFO_SIGNATURE_TYPE,
            signatureCustody.getSignatureType());
      }
      

      // Guardar/actualitzar metadades
      if (metadatas != null) {

        for (Metadata metadata : metadatas) {

          String key = metadata.getKey();
          if (key.startsWith("eni:") || key.startsWith("gdib:")) {
            // Metadades eni: o gdib: es guarden com Metadata del Document
            // NOTA: No s'actualitzaran les metadades generades
            if (generatedMetadataCollection.containsKey(key)) {
              // Es generada i no la podem sobreescriure
              log.warn("La metadada [" + key + "] no es pot guardar ja que és generada pel Sistema");              
            } else {         
              metadataCollection.put(key, metadata.getValue());
            }
          } else {
            // Processar metadades no eni o gdib: es guarden dins
            // "eni:description"
            metasAndInfo.put(FILE_INFO_BASE_METADATA + key, metadata.getValue());
          }
        }
      }

      
      // Ara es guarden info del document i signatura més les metadades
      metadataCollection.put(MetadataConstants.ENI_DESCRIPCION,
          propertiesCustodyToStringEniDescripcion(metasAndInfo,
              (String)metadataCollection.get(MetadataConstants.ENI_DESCRIPCION)));

      // ============================= MUNTAR DOCUMENTO

      // Muntar Documento segons especificació de l'arxiu Digital CAIB
      // Si la firma electrónica es implícita (tipos de firma: “TF02”, “TF03”,
      // “TF05” y “TF06”), es decir, contenido del documento y firma electrónica
      // coinciden, no es requerido aportar la firma electrónica del documento,
      // bastaría con informar el contenido del mismo.

      // Muntar document segons l'anterior
      
      doc.setName(nomFitxerDocument);
      
      if (documentCustody != null && signatureCustody != null) {
        // DETACHED

        // FILE
        byte[] dataFile = documentCustody.getData();
        doc.setContent(Base64.encode(dataFile));
        doc.setMimetype(documentCustody.getMime());

        // SIGNATURE
        FirmaDocumento firma = new FirmaDocumento();
        final byte[] dataSign = signatureCustody.getData();
        firma.setContent(Base64.encode(dataSign));
        firma.setEncoding(FORMATO_UTF8);
        firma.setMimetype(signatureCustody.getMime());

        doc.setListaFirmas(Arrays.asList(firma));

      } else {

        // ONLY FILE && ATTACHED

        // FILE if ONLY FILE or SIGNATURE if ATTACHED
        byte[] dataFile;
        String mime;

        if (signatureCustody != null) {
          dataFile = signatureCustody.getData();
          mime = signatureCustody.getMime();
        } else {
          dataFile = documentCustody.getData();
          mime = documentCustody.getMime();
        }

        doc.setContent(Base64.encode(dataFile));
        doc.setMimetype(mime);

        doc.setListaFirmas(new ArrayList<FirmaDocumento>());

      }


      

      if (documentElectronicID == null) {

        // CREAR DOCUMENT => CANVIAR CONTINGUT FITXER DE CSV PER DOC ELECTRONIC 

        doc.getAspects().remove(Aspectos.BORRADOR);
        
        if (isPropertyCreateDraft_EL(custodyParameters)) {

          ResultadoSimple rd = apiArxiu.actualizarDocumento(doc);

          final String errorCodi = rd.getCodigoResultado();
          if (hiHaError(errorCodi)) {
            String msg = rd.getMsjResultado();
            checkErrorsConeguts(errorCodi, msg, ecd);
            throw new CustodyException("Error creant Document Draft amb id " + custodyID + "("
                + errorCodi + " - " + msg + ")");
          }


        } else {
          
          // =========== REVISAR SI DOCUMENT NOM-DESTI EXISTEIX ========

          if (log.isDebugEnabled()) {
            log.info("custodyID = " + custodyID);
            log.info("ecd.expedientID = " + ecd.expedientID);
            log.info("ecd.carpetaID = " + ecd.carpetaID);
            log.info("doc.getName() = " + doc.getName());
          }
          
          Map<String, Nodo> nodos = getNodosByExpedientCarpeta(apiArxiu, ecd.expedientID, ecd.carpetaID);
          
          Nodo nodo = nodos.get(doc.getName());
          if (nodo != null) {
            if (log.isDebugEnabled()) {
              log.info(" =========================== ");
              log.info(" Eliminant document " + nodo.getName() + " (" + nodo.getId() + ")");
              log.info(" =========================== ");
            }
            ResultadoSimple rs = apiArxiu.eliminarDocumento(nodo.getId());
            final String errorCodi = rs.getCodigoResultado();
            if (hiHaError(errorCodi)) {
              String msg = rs.getMsjResultado();
              throw new CustodyException("Error intentant eliminar Document Definiti DUPLICAT amb id "
                + nodo.getName() + " (" + nodo.getId() + "):" + errorCodi + " => " + msg 
                + ". S'ha de posar en contacte amb l'Administrador per a que l'esborri de l'arxiu");
            }
            
          }

          // =========== FINAL REVISAR SI DOCUMENT NOM-DESTI EXISTEIX ========

          ResultadoSimple rd = apiArxiu.actualizarDocumento(doc);
          final String errorCodiActDoc = rd.getCodigoResultado();
          if (hiHaError(errorCodiActDoc)) {
            String msg = rd.getMsjResultado();

            apiArxiu.eliminarDocumento(doc.getId());
            
            checkErrorsConeguts(errorCodiActDoc, msg, ecd);
            throw new CustodyException("Error creant Document Final amb id " + custodyID + "("
                + errorCodiActDoc + " - " + msg + ")");
          }

          // Reintentam cada 5 segons durant 1 minut
          int reintents = (int)(60000/SLEEP_SEND_TIMEOUT);
          do {
            reintents --;
            try {
              rd = apiArxiu.finalizarDocumento(doc);
            } catch(Exception e) {
              // https://github.com/GovernIB/pluginsib/issues/52
              log.error("Error no controlat al finalizarDocumento()[Miram si podem reintentar...]: "
                + e.getMessage(), e);
              String msg = e.getMessage();
              if (reintents > 0 && msg != null && msg.contains("Proxy Error") 
                  && msg.contains("/services/setFinalDocument")) {
                // forçam el reintent
                rd = new ResultadoSimple();
                rd.setCodigoResultado("COD_020");
                rd.setMsjResultado("Send timeout");
              } else {
                throw e; 
              }
            }

            String errorCodi = rd.getCodigoResultado(); 
            if (hiHaError(errorCodi)) {
              String msg = rd.getMsjResultado();
              // Reintentam si COD_020-Send timeout
              if ("COD_020".equals(errorCodi) && msg.startsWith("Send timeout")) {
                log.warn("Gestió de reintents de apiArxiu.finalizarDocumento():"
                    + " reintent compte enrera " + reintents + ". Esperam " + SLEEP_SEND_TIMEOUT + " ms");
                Thread.sleep(SLEEP_SEND_TIMEOUT);
              } else if ("COD_021".equals(errorCodi) && msg.startsWith("Can not add the draft aspect to the node because is a final document")) {
                // COD_021 - Can not add the draft aspect to the node because is a final document)
                log.info("S'ha intentat finalitzarDocument però aquest ja és definitiu. Es dóna per bó.");
                break;
              } else {
                throw new CustodyException("Error cridant a finalizarDocumento() a "
                    + "l'hora de crearDocumentFinal amb id "
                  + custodyID + "(" + errorCodi + " - " + msg + ")");
              }
            } else {
              break;
            }
            
            if (reintents <= 0) {
              throw new CustodyException("S'han esgotat els reintents i no s'ha pogut "
                  + "tancar (finalitzar) el document amb uuid " + doc.getId() + "(" 
                  + doc.getName() + "): " + rd.getCodigoResultado() + " - " + rd.getMsjResultado());
            }
          } while(reintents > 0);
          
          
        }

        if (debug) {
          log.info("Convertit fitxer de CSV a Document Electronic = " + ecd.documentID);
        }

      } else {

        // ACTUALITZAR DOCUMENT => ACTUALITZAR DOC ELECTRONIC 
        if (debug) {
          log.info("ACTUALITZAR DOCUMENT AMB UUID = " + documentElectronicID);
        }

        
        doc.getAspects().remove(Aspectos.BORRADOR);

        ResultadoSimple rs = apiArxiu.actualizarDocumento(doc);

        String codi = rs.getCodigoResultado();
        if (hiHaError(codi)) {
          String msg = rs.getMsjResultado();
          checkErrorsConeguts(codi, msg, ecd);
          throw new CustodyException("Error actualitzant Document amb id " + custodyID
              + "[uuid:" + documentElectronicID + "]" + "(" + codi + " - "
              + msg + ")");
        }

      }


      if (debug) {
        try {
          Thread.sleep(1500);
          Map<String, Nodo> nodosByName2 = getNodosByCustodyID2(apiArxiu, custodyID);
          for (String key : nodosByName2.keySet()) {
            log.info("FITXERS A LA CARPETA[" + key + "] = " + nodosByName2.get(key).getId());
          }
        } catch(Throwable th) {
          log.error("Error llistant nodos de custodyID = " + custodyID + ":" + th.getMessage());
        }
      }
      
      if (isPropertyTancarExpedient_EL(custodyParameters)) {

        // Reintentam cada 5 segons durant 1 minut
        int reintents = (int)(60000/SLEEP_SEND_TIMEOUT);
        do {
          Resultado<String>  res;
          
          reintents --;
          try {
            res = apiArxiu.cerrarExpediente(ecd.expedientID);
          } catch(Exception e) {
            // https://github.com/GovernIB/pluginsib/issues/52
            log.error("Error no controlat al cerrarExpediente()[Miram si podem reintentar...]: "
               + e.getMessage(), e);
            String msg = e.getMessage();
            if (reintents > 0 && msg != null && msg.contains("Proxy Error") 
                && msg.contains("/services/closeFile")) {
              // forçam el reintent
              res = new Resultado<String>();
              res.setCodigoResultado("COD_020");
              res.setMsjResultado("Send timeout");
            } else {
              throw e; 
            }
          }

          String errorCodi = res.getCodigoResultado();

          if (hiHaError(errorCodi)) {
            String msg = res.getMsjResultado();
            if (isIgnoreErrorWhenTancarExpedient()) {
              log.warn("S'ha produit un error [" + errorCodi + ": " + msg + "] durant el"
                  + " tancament de l'expedient però la propietat {<<PACKAGE>>." 
                  + ARXIUDIGITALCAIB_PROPERTY_BASE + "ignoreerrorwhentancarexpedient = true}."
                  + "Ignoram l'error i presuposam que l'expedient-document " + custodyID
                  + " s'ha tancat correctament");
              break;
            }

            // Reintentam si COD_020-Send timeout
            if ("COD_020".equals(errorCodi) && msg.startsWith("Send timeout")) {
              log.warn("Gestió de reintents de apiArxiu.cerrarExpediente():"
                  + " reintent compte enrera " + reintents + ". Esperam " + SLEEP_SEND_TIMEOUT + " ms");
              Thread.sleep(SLEEP_SEND_TIMEOUT);
            } else if ("COD_021".equals(errorCodi) && msg.startsWith("Could not have the permission of Delete to perfom the operation")) {
              // Aquest error el dóna quan l'expedient ja s'ha mogut a RM
              // COD_021 - Could not have the permission of Delete to perfom the operation
              log.info("S'ha intentat cerrarExpediente() però aquest ja esta a RM. Es dóna per bó.");
              break;              
            } else {
              // Llaçam excepció
              throw new CustodyException("No s'ha pogut tancar l'expedient amb uuid " + ecd.expedientID + ": " 
                + res.getCodigoResultado() + " - " + res.getMsjResultado());
            }
          } else {
            break;
          }

          if (reintents <= 0) {
            throw new CustodyException("S'han esgotat els reintents i no s'ha pogut tancar l'expedient amb uuid " + ecd.expedientID + ": " 
                + res.getCodigoResultado() + " - " + res.getMsjResultado());
          }
        } while(reintents > 0);

      }
      

    } catch (CustodyException e) {
      throw e;
    } catch (Exception e) {
      throw new CustodyException("Error creant o actualitzant document electronic "
          + custodyID + ": " + e.getMessage(), e);
    }
  }
  
  
  protected void checkErrorsConeguts(String code, String msg,
      ExpedientCarpetaDocument ecd) throws CustodyException {
    
    // COD_021 - Duplicate child name not allowed: holacaracolaofdetachedsign.txt
    if ("COD_021".equals(code) && msg.startsWith("Duplicate child name not allowed:")) {
      int beginIndex = msg.indexOf(':');
      String filename = msg.substring(beginIndex + 2);
      throw new CustodyException("Esta intentant guardar el fitxer amb nom " + filename +
          ", però ja n'existeix un dins aquest expedient/carpeta(" + ecd.expedientID + "/" 
            + ecd.carpetaID + ")");
    }
  }

  /** XZY ZZZ
  protected FilesInfo getFileInfo2(ExpedientCarpetaDocument ec, Map<String, Nodo> nodosByName)
      throws CustodyException {
    
    // Cercarem entre totes els nodes un que és digui NOM_CSV_DOCUMENT_ELECTRONIC
    if (nodosByName.size() == 0) {
      throw new CustodyException("Aquest Expedient/Carpeta ("
          + ec.toString() + ") no conté cap fitxer.", new Exception());
    }
    
    if (nodosByName.size() != 1) {
      
      StringBuffer nodes = new StringBuffer();
      for (Nodo n : nodosByName.values()) {
        if (nodes.length() != 0) {
          nodes.append(", ");
        }
        nodes.append(n.getName() + "(" + n.getId() + ")");
      }
      
      throw new CustodyException("Aquest Expedient/Carpeta ("
          + ec.toString() + ") conté multiples fitxers: " + nodes.toString(), new Exception());
    }
    
    Nodo nodoCSV = nodosByName.get(NOM_CSV_DOCUMENT_ELECTRONIC);

    if (nodoCSV != null && TiposObjetoSGD.DOCUMENTO.equals(nodoCSV.getType())) {
      return new FilesInfo(null, nodoCSV);
    }
    
    String key = nodosByName.keySet().iterator().next();


    Nodo nodo = nodosByName.get(key); // NOM_DOCUMENT_ELECTRONIC);
    
    if (TiposObjetoSGD.DOCUMENTO.equals(nodo.getType())) {
      return new FilesInfo(nodo, null);
    } else {

      StringBuffer nodes = new StringBuffer();
      for (Nodo n : nodosByName.values()) {
        if (nodes.length() != 0) {
          nodes.append(", ");
        }
        nodes.append(n.getName() + "(" + n.getId() + ")");
      }
      throw new CustodyException("No s'ha trobat cap document electrònoc ni fitxer de csv en "
          + "aquest Expedient/Carpeta (" + ec.toString() + "): " + nodes.toString(),
          new Exception());
    }
  }
  */

  // ----------------------------------------------------------------------------
  // ---------------------------- D O C U M E N T ------------------------------
  // ----------------------------------------------------------------------------

  @Override
  @Deprecated
  public void saveDocument(String custodyID, Map<String, Object> custodyParameters,
      DocumentCustody documentCustody) throws CustodyException, NotSupportedCustodyException {

    throw new NotSupportedCustodyException();

    /*
     * 
     * final boolean overWriteDocument = true; final boolean overWriteSignature
     * = false; final boolean overWriteMetadatas = false; final SignatureCustody
     * signatureCustody = null; final Metadata[] metadatas = null;
     * createUpdateAll(custodyID, custodyParameters, documentCustody,
     * overWriteDocument, signatureCustody, overWriteSignature, metadatas,
     * overWriteMetadatas);
     */
  }

  @Override
  @Deprecated
  public void deleteDocument(String custodyID) throws CustodyException,
      NotSupportedCustodyException {

    throw new NotSupportedCustodyException();

    /*
     * 
     * final boolean overWriteDocument = true; final boolean overWriteSignature
     * = false; final boolean overWriteMetadatas = false; final SignatureCustody
     * signatureCustody = null; final DocumentCustody documentCustody = null;
     * final Metadata[] metadatas = null; // No s'utilitza final Map<String,
     * Object> custodyParameters = null; createUpdateAll(custodyID,
     * custodyParameters, documentCustody, overWriteDocument, signatureCustody,
     * overWriteSignature, metadatas, overWriteMetadatas);
     */
  }

  @Override
  public byte[] getDocument(String custodyID) throws CustodyException {

    final boolean retrieveData = true;
    DocumentCustody dc = internalGetDocument(custodyID, retrieveData);
    return (dc == null) ? null : dc.getData();

  }

  @Override
  public DocumentCustody getDocumentInfo(String custodyID) throws CustodyException {

    final boolean retrieveData = true;
    return internalGetDocument(custodyID, retrieveData);

  }

  @Override
  public DocumentCustody getDocumentInfoOnly(String custodyID) throws CustodyException {
    final boolean retrieveData = false;
    return internalGetDocument(custodyID, retrieveData);
  }

  protected DocumentCustody internalGetDocument(String custodyID, boolean retrieveContent)
      throws CustodyException {

    try {
  
      ApiArchivoDigital apiArxiu = getApiArxiu(null);

      String uuid = ExpedientCarpetaDocument.decodeCustodyID(custodyID).documentID;

      FullInfoDocumentElectronic fullInfo;
      fullInfo = getFullInfoOfDocumentElectronic(apiArxiu, uuid, retrieveContent);
      
      if(fullInfo == null) {
        return null;
      }

      Properties p = fullInfo.infoAndMetas;

      String nom = p.getProperty(FILE_INFO_BASE_DOCUMENT + FILE_INFO_NOM);

      if (nom == null) {
        return null;
      }

      DocumentCustody doc = new DocumentCustody();
      doc.setName(nom);
      doc.setLength(Long.parseLong(p.getProperty(FILE_INFO_BASE_DOCUMENT + FILE_INFO_TAMANY)));
      doc.setMime(p.getProperty(FILE_INFO_BASE_DOCUMENT + FILE_INFO_MIME));

      if (retrieveContent) {
        Documento documento = fullInfo.documento;
        byte[] data = Base64.decode(documento.getContent());
        doc.setData(data);
        doc.setLength(data.length);
      }

      return doc;

    } catch (IOException e) {
      throw new CustodyException(
          "Error intentant obtenir contingut del Document amb custodyID " + custodyID + ": "
              + e.getMessage(), e);
    }

  }

  @Override
  public boolean supportsDeleteDocument() {
    return isPropertyCreateDraft_EL(null);
  }

  // ----------------------------------------------------------------------------
  // ---------------------------- S I G N A T U R E ----------------------------
  // ----------------------------------------------------------------------------

  @Override
  @Deprecated
  public void saveSignature(String custodyID, Map<String, Object> custodyParameters,
      SignatureCustody signatureCustody) throws CustodyException, NotSupportedCustodyException {

    throw new NotSupportedCustodyException();

    /*
     * final boolean overWriteDocument = false; final boolean overWriteSignature
     * = true; final boolean overWriteMetadatas = false; final DocumentCustody
     * documentCustody = null; final Metadata[] metadatas = null;
     * createUpdateAll(custodyID, custodyParameters, documentCustody,
     * overWriteDocument, signatureCustody, overWriteSignature, metadatas,
     * overWriteMetadatas);
     */

  }

  @Override
  public byte[] getSignature(String custodyID) throws CustodyException {

    boolean retrieveContent = true;
    SignatureCustody sc = internalGetSignature(custodyID, retrieveContent);

    return (sc == null) ? null : sc.getData();

  }

  @Override
  public SignatureCustody getSignatureInfo(String custodyID) throws CustodyException {

    boolean retrieveContent = true;
    return internalGetSignature(custodyID, retrieveContent);
  }

  @Override
  public SignatureCustody getSignatureInfoOnly(String custodyID) throws CustodyException {
    boolean retrieveContent = false;
    return internalGetSignature(custodyID, retrieveContent);

  }

  public SignatureCustody internalGetSignature(String custodyID, boolean retrieveContent)
      throws CustodyException {
    try {
      ApiArchivoDigital apiArxiu = getApiArxiu(null);

      String uuid = ExpedientCarpetaDocument.decodeCustodyID(custodyID).documentID;

      FullInfoDocumentElectronic fullInfo;
      fullInfo = getFullInfoOfDocumentElectronic(apiArxiu, uuid, retrieveContent);
      
      if(fullInfo == null) {
        return null;
      }

      // Check Values

      Properties p = fullInfo.infoAndMetas;

      String nom = p.getProperty(FILE_INFO_BASE_FIRMA + FILE_INFO_NOM);

      if (nom == null) {
        return null;
      }

      SignatureCustody sign = new SignatureCustody();
      sign.setName(nom);
      sign.setLength(Long.parseLong(p.getProperty(FILE_INFO_BASE_FIRMA + FILE_INFO_TAMANY)));
      sign.setMime(p.getProperty(FILE_INFO_BASE_FIRMA + FILE_INFO_MIME));

      String attached = p.getProperty(FILE_INFO_BASE_FIRMA
          + FILE_INFO_SIGNATURE_ATTACHED_DOCUMENT);
      if (attached == null) {
        sign.setAttachedDocument(null);
      } else {
        sign.setAttachedDocument(Boolean.parseBoolean(attached));
      }

      sign.setSignatureType(p.getProperty(FILE_INFO_BASE_FIRMA + FILE_INFO_SIGNATURE_TYPE));

      if (retrieveContent) {
        //
        String nomFile = p.getProperty(FILE_INFO_BASE_DOCUMENT + FILE_INFO_NOM);
        byte[] data;
        if (nomFile == null) {
          // ATTACHED => Dins content trobarem la firma
          data = Base64.decode(fullInfo.documento.getContent());
          sign.setData(data);
        } else {
          // DETACHED => Dins ListaFirmas trobarem la firma
          FirmaDocumento firma = fullInfo.documento.getListaFirmas().get(0);
          data = Base64.decode(firma.getContent());
          sign.setData(data);
        }
        sign.setLength(data.length);
      }

      return sign;

    } catch (IOException e) {
      throw new CustodyException(
          "Error intentant obtenir contingut del Document amb custodyID " + custodyID + ": "
              + e.getMessage(), e);
    }

  }

  @Override
  @Deprecated
  public void deleteSignature(String custodyID) throws CustodyException,
      NotSupportedCustodyException {

    throw new NotSupportedCustodyException();

    /*
     * final boolean overWriteDocument = false; final boolean overWriteSignature
     * = true; final boolean overWriteMetadatas = false; final SignatureCustody
     * signatureCustody = null; final DocumentCustody documentCustody = null;
     * final Metadata[] metadatas = null; // No s'utilitza final Map<String,
     * Object> custodyParameters = null; createUpdateAll(custodyID,
     * custodyParameters, documentCustody, overWriteDocument, signatureCustody,
     * overWriteSignature, metadatas, overWriteMetadatas);
     */

  }

  @Override
  public boolean supportsDeleteSignature() {
    return true;
  }

  @Override
  public String[] getSupportedSignatureTypes() {

    return SignatureCustody.ALL_TYPES_OF_SIGNATURES;

  }

  @Override
  public Boolean supportsAutomaticRefreshSignature() {
    // TODO Es presuposa que si
    return true;
  }

  // ----------------------------------------------------------------------------
  // ---------------------------- M E T A D A T A
  // -------------------------------
  // ----------------------------------------------------------------------------

  @Override
  public boolean supportsMetadata() {
    return true;
  }

  @Override
  public void addMetadata(String custodyID, final Metadata metadata,
      Map<String, Object> custodyParameters) throws CustodyException,
      NotSupportedCustodyException, MetadataFormatException {

    // TODO S'ha de gestionar multipes valors associats a un única clau
    updateMetadata(custodyID, metadata, custodyParameters);
  }

  @Override
  public void addMetadata(String custodyID, final Metadata[] metadataList,
      Map<String, Object> custodyParameters) throws CustodyException,
      NotSupportedCustodyException, MetadataFormatException {
    // TODO S'ha de gestionar multipes valors associats a un única clau
    updateMetadata(custodyID, metadataList, custodyParameters);
  }

  @Override
  public void updateMetadata(String custodyID, final Metadata metadata,
      Map<String, Object> custodyParameters) throws CustodyException,
      NotSupportedCustodyException, MetadataFormatException {

    updateMetadata(custodyID, new Metadata[] { metadata }, custodyParameters);

  }

  @Override
  public void updateMetadata(String custodyID, final Metadata[] metadatas,
      Map<String, Object> custodyParameters) throws CustodyException,
      NotSupportedCustodyException, MetadataFormatException {

    try {
      ApiArchivoDigital apiArxiu = getApiArxiu(custodyParameters);
      
      String uuid = ExpedientCarpetaDocument.decodeCustodyID(custodyID).documentID;
      
      final boolean retrieveContent = false;
      
      FullInfoDocumentElectronic fullDoc = getFullInfoOfDocumentElectronic(apiArxiu,
          uuid, retrieveContent);
      
      for (Metadata metadata : metadatas) {
        
        String key = metadata.getKey();
        
        if ((key.startsWith("eni:") || key.startsWith("gdib:"))) {
          
          if (MetadataConstants.ENI_DESCRIPCION.equals(key) ||
              MetadataConstants.ENI_CSV.equals(key) ) {
            log.warn("No puc actualitzar metadada " + key);
          } else {
            fullDoc.documento.getMetadataCollection().put(key, metadata.getValue());
          }
          
        } else {
          fullDoc.infoAndMetas.setProperty(FILE_INFO_BASE_METADATA + key,  metadata.getValue()); 
        }

      }
      
      fullDoc.documento.getMetadataCollection().put(MetadataConstants.ENI_DESCRIPCION,
          propertiesCustodyToStringEniDescripcion(fullDoc.infoAndMetas,
              (String)fullDoc.documento.getMetadataCollection().get(MetadataConstants.ENI_DESCRIPCION)));
      
      Documento doc = new Documento();
      
      doc.setId(fullDoc.documento.getId());
      doc.setMetadataCollection(fullDoc.documento.getMetadataCollection());
      
      ResultadoSimple rd = apiArxiu.actualizarDocumento(doc);
      
      final String errorCodi = rd.getCodigoResultado();
      if (hiHaError(errorCodi)) {
        String msg = rd.getMsjResultado();
        throw new CustodyException("Error actualitzant Metadades " + custodyID + "("
            + errorCodi + " - " + msg + ")");
      }
      
    
    } catch (CustodyException ce) {
      throw ce;
    } catch (Exception e) {
      throw new CustodyException("Error intentant actualitzar les metadades de " + custodyID
          + ": " + e.getMessage(), e);
    }


  }

  @Override
  public Map<String, List<Metadata>> getAllMetadata(String custodyID) throws CustodyException,
      NotSupportedCustodyException {

    Map<String, List<Metadata>> map = internalGetAllMetadatas(custodyID, null);

    return map;

    /*
     * // TODO S'ha de gestionar multipes valors associats a un única clau
     * ApiArchivoDigital apiArxiu = getApiArxiu(null); Properties properties =
     * new MetadataAction(apiArxiu, custodyID) {
     * 
     * @Override public boolean modificarMetadades(Properties metadades) throws
     * Exception { return false; } }.doAction();
     * 
     * Map<String, List<Metadata>> result = new HashMap<String,
     * List<Metadata>>();
     * 
     * for (Object keyObj : properties.keySet()) { String key = (String) keyObj;
     * List<Metadata> list = new ArrayList<Metadata>(); list.add(new
     * Metadata(key, properties.getProperty(key))); result.put(key, list); }
     * return result;
     */
  }

  /**
   * Si el document existeix llegim les metadades
   * 
   * @return
   */
  protected Map<String, List<Metadata>> internalGetAllMetadatas(String custodyID,
      Map<String, Object> custodyParameters) throws CustodyException {
    ApiArchivoDigital apiArxiu = getApiArxiu(custodyParameters);

    try {
      /*
      Map<String, Nodo> nodosByName = getNodosByCustodyID(apiArxiu, custodyID);

      ExpedientCarpetaDocument ec = ExpedientCarpetaDocument.decodeCustodyID(custodyID);

      FilesInfo filesInfo = getFileInfo(ec, nodosByName);
      */

      String uuid = ExpedientCarpetaDocument.decodeCustodyID(custodyID).documentID;

      FullInfoDocumentElectronic fullInfo;
      fullInfo = getFullInfoOfDocumentElectronic(apiArxiu, uuid, false);

      Map<String, List<Metadata>> map = new HashMap<String, List<Metadata>>();

      
      
      //  (1) Afegir metadades de INFO 
      {
        
        for (Entry<Object, Object> entry : fullInfo.infoAndMetas.entrySet()) {
          String key = (String) entry.getKey();

          if (key.startsWith(FILE_INFO_BASE_METADATA)) {
            key = key.substring(FILE_INFO_BASE_METADATA.length() + 1);

            map.put(key, Arrays.asList(new Metadata(key, (String) entry.getValue())));

          }

        }
        
      }
      // (2) Afegir Metadades de METADATACOLLECTION (Metadades del document Electrònic)
      {
        // Afegir metadades ENI i GDIB
        Map<String, Object> metadataDocs = fullInfo.documento.getMetadataCollection();
        for (String key : metadataDocs.keySet()) {
          if (!key.equals(MetadataConstants.ENI_DESCRIPCION)) {
            Object value = metadataDocs.get(key);
            map.put(key, Arrays.asList(new Metadata(key, String.valueOf(value))));
          }
        }
       
      }
     
      

      return map;

    } catch (CustodyException ce) {
      throw ce;
    } catch (Exception e) {
      throw new CustodyException("Error intentant obtenir les metadades de " + custodyID
          + ": " + e.getMessage(), e);
    }

  }

  @Override
  public List<Metadata> getMetadata(String custodyID, String key) throws CustodyException,
      NotSupportedCustodyException {

    Map<String, List<Metadata>> map = internalGetAllMetadatas(custodyID, null);

    return map.get(key);

    // TODO S'ha de gestionar multipes valors associats a un única clau
    /*
     * ApiArchivoDigital apiArxiu = getApiArxiu(null); Properties properties =
     * new MetadataAction(apiArxiu, custodyID) {
     * 
     * @Override public boolean modificarMetadades(Properties metadades) throws
     * Exception { return false; } }.doAction();
     * 
     * List<Metadata> list = new ArrayList<Metadata>(); String value =
     * properties.getProperty(key);
     * 
     * if (value != null) { list.add(new Metadata(key, value)); }
     * 
     * return list;
     */
  }

  @Override
  public Metadata getOnlyOneMetadata(String custodyID, String key) throws CustodyException,
      NotSupportedCustodyException {
    List<Metadata> list = getMetadata(custodyID, key);
    if (list.size() != 0) {
      return list.get(0);
    } else {
      return null;
    }
  }

  @Override
  public void deleteAllMetadata(String custodyID) throws CustodyException {

    final boolean deleteAll = true;
    internalDeleteMetadata(custodyID, null, deleteAll);

  }

  @Override
  public List<Metadata> deleteMetadata(String custodyID, final String key)
      throws CustodyException {

    return deleteMetadata(custodyID, new String[] {key});
  }

  @Override
  public List<Metadata> deleteMetadata(String custodyID, final String[] keys)
      throws CustodyException {
    
    final boolean deleteAll = false;
    return internalDeleteMetadata(custodyID, keys, deleteAll);
  }
  
  
  
  protected List<Metadata> internalDeleteMetadata(String custodyID, final String[] keys,
      boolean deleteAll)  throws CustodyException {

  
    try {
      // TODO 
      ApiArchivoDigital apiArxiu = getApiArxiu(null);
      
      String uuid = ExpedientCarpetaDocument.decodeCustodyID(custodyID).documentID;
      
      final boolean retrieveContent = false;
      
      FullInfoDocumentElectronic fullDoc = getFullInfoOfDocumentElectronic(apiArxiu,
          uuid, retrieveContent);
      
      List<Metadata> esborrats = new ArrayList<Metadata>();
      
      if (deleteAll) {
        Set<Object> keysObject = fullDoc.infoAndMetas.keySet();
        
        for (Object keyO : keysObject) {
          String key = (String)keyO;
          if (key.startsWith(FILE_INFO_BASE_METADATA)) {
            fullDoc.infoAndMetas.remove(keyO);
            esborrats.add(new Metadata(key, ""));
          }
        }
        
        
      } else {
      
        for (String key : keys) {
          
          if ((key.startsWith("eni:") || key.startsWith("gdib:"))) {
            
            if (MetadataConstants.ENI_DESCRIPCION.equals(key) ||
                MetadataConstants.ENI_CSV.equals(key) ) {
              log.warn("No puc actualitzar metadada " + key);
            } else {
              fullDoc.documento.getMetadataCollection().remove(key);
              fullDoc.documento.getMetadataCollection().put("-" + key, "");
              esborrats.add(new Metadata(key, ""));
            }
            
          } else {
            if (fullDoc.infoAndMetas.remove(FILE_INFO_BASE_METADATA + key) != null) { 
              esborrats.add(new Metadata(key, ""));
            }
          }
  
        }
      }
      
      fullDoc.documento.getMetadataCollection().put(MetadataConstants.ENI_DESCRIPCION,
          propertiesCustodyToStringEniDescripcion(fullDoc.infoAndMetas,
              (String)fullDoc.documento.getMetadataCollection().get(MetadataConstants.ENI_DESCRIPCION)));
      
      Documento doc = new Documento();
      doc.setId(fullDoc.documento.getId());
      doc.setMetadataCollection(fullDoc.documento.getMetadataCollection());
      
      ResultadoSimple rd = apiArxiu.actualizarDocumento(doc);
      
      final String errorCodi = rd.getCodigoResultado();
      if (hiHaError(errorCodi)) {
        String msg = rd.getMsjResultado();
        throw new CustodyException("Error actualitzant Metadades " + custodyID + "("
            + errorCodi + " - " + msg + ")");
      }
      
      return esborrats;
    
    } catch (CustodyException ce) {
      throw ce;
    } catch (Exception e) {
      throw new CustodyException("Error intentant actualitzar les metadades de " + custodyID
          + ": " + e.getMessage(), e);
    }

   
  }

  @Override
  public boolean supportsDeleteMetadata() {
    return true;
  }

  // ----------------------------------------------------------------------------
  // ---------------------------- A N N E X -------------------------------
  // ----------------------------------------------------------------------------
  @Override
  public boolean supportsAnnexes() {
    return false;
  }

  @Override
  public boolean supportsDeleteAnnex() {
    return false;
  }

  @Override
  public String addAnnex(String custodyID, AnnexCustody annex, Map<String, Object> parameters)
      throws CustodyException, NotSupportedCustodyException {
    throw new NotSupportedCustodyException();
  }

  @Override
  public void deleteAnnex(String custodyID, String annexID) throws CustodyException,
      NotSupportedCustodyException {
    throw new NotSupportedCustodyException();

  }

  @Override
  public void deleteAllAnnexes(String custodyID) throws CustodyException,
      NotSupportedCustodyException {
    throw new NotSupportedCustodyException();

  }

  @Override
  public byte[] getAnnex(String custodyID, String annexID) throws CustodyException {
    throw new CustodyException("No es suporten Annexes");
  }

  @Override
  public AnnexCustody getAnnexInfo(String custodyID, String annexID) throws CustodyException {
    throw new CustodyException("No es suporten Annexes");
  }

  @Override
  public AnnexCustody getAnnexInfoOnly(String custodyID, String annexID)
      throws CustodyException {
    throw new CustodyException("No es suporten Annexes");
  }

  @Override
  public List<String> getAllAnnexes(String custodyID) throws CustodyException {
    throw new CustodyException("No es suporten Annexes");
  }

  // ----------------------------------------------------------------------------
  // ---------------------------- U T I L S -------------------------------
  // ----------------------------------------------------------------------------

  protected String processEL(String template, Map<String, Object> custodyParameters)
      throws CustodyException {
    return AbstractDocumentCustodyPlugin
        .processExpressionLanguage(template, custodyParameters);
  }

  protected String processELNullIfEmpty(String template, Map<String, Object> custodyParameters)
      throws CustodyException {
    if (template == null) {
      return null;
    }
    String tmp = AbstractDocumentCustodyPlugin.processExpressionLanguage(template,
        custodyParameters);

    if (tmp == null || tmp.trim().length() == 0) {
      return null;
    }

    return tmp;
  }

  public boolean hiHaError(String code) {
    return !CodigosResultadoPeticion.PETICION_CORRECTA.equals(code);
  }

  public boolean hiHaErrorEnCerca(String code) {
    return !CodigosResultadoPeticion.PETICION_CORRECTA.equals(code)
        && !CodigosResultadoPeticion.LISTA_VACIA.equals(code);
  }

  protected Map<String, Object> getMetadadesPerExpedient(Map<String, Object> custodyParameters)
      throws CustodyException, Exception {

    Map<String, Object> llistaMetadades = getMetadadesComuns(custodyParameters);

    // MetadatosExpediente.IDENTIFICADOR_PROCEDIMIENTO
    String codigoProcedimiento = processEL(getPropertyCodiProcedimentEL(), custodyParameters);
    llistaMetadades.put(MetadataConstants.ENI_ID_TRAMITE, codigoProcedimiento);

    // MetadatosExpediente.CODIGO_APLICACION_TRAMITE
    String codigoAplicacion = getPropertyCodiAplicacio();
    llistaMetadades.put(MetadataConstants.ENI_APP_TRAMITE_EXP, codigoAplicacion);

    List<Metadata> additionalMetas;
    additionalMetas = recollectAutomaticMetadatas(custodyParameters, "expedient");

    if (additionalMetas != null) {
      final boolean debug = isDebug();
      for (Metadata metadata : additionalMetas) {
        if (debug) {
          log.info("MetasExpedient::Afegint metadata addicional [" + metadata.getKey()
              + "] => " + metadata.getValue());
        }
        llistaMetadades.put(metadata.getKey(), metadata.getValue());
      }
    }
    
    
    // MetadatosExpediente.FECHA_INICIO
    // NOTA: Amb aquest canvi pots fer que l'expedient es guardi en una altre data
    // Date fecha = new SimpleDateFormat("dd/MM/yyyy").parse("04/04/2017");
    // String fechaCreacio = UtilidadesFechas.convertirDeDate_A_ISO8601(fecha);
    String templateDataCreacio = getPropertyDataCreacioExpedientEL();
    String dataCreacio;
    if (templateDataCreacio == null) {
      dataCreacio = UtilidadesFechas.fechaActualEnISO8601();
    } else {
      dataCreacio = processEL(templateDataCreacio, custodyParameters);
    }
    if (isDebug()) {
      log.info("DataCreacio Expedient: " + dataCreacio);
    }
    llistaMetadades.put(MetadataConstants.ENI_FECHA_INICIO, dataCreacio);
    

    return llistaMetadades;
  }

  protected Map<String, Object> getMetadadesPerDocument(Map<String, Object> custodyParameters,
      SignatureCustody signatureCustody) throws CustodyException, Exception {

    Map<String, Object> llistaMetadades = getMetadadesComuns(custodyParameters);

    List<Metadata> additionalMetas;
    additionalMetas = recollectAutomaticMetadatas(custodyParameters, "documentelectronic");

    if (additionalMetas.size() != 0) {
      for (Metadata metadata : additionalMetas) {
        llistaMetadades.put(metadata.getKey(), metadata.getValue());
      }
    }
    
    final boolean debug = isDebug();

    // MetadatosDocumento.CODIGO_APLICACION_TRAMITE
    String codigoAplicacion = getPropertyCodiAplicacio();
    llistaMetadades.put(MetadataConstants.ENI_APP_TRAMITE_DOC, codigoAplicacion);

    // MetadatosDocumento.ESTADO_ELABORACION : EE01,EE02, EE03, EE04, EE99
    String estadoElaboracion = processEL(getPropertyEstatElaboracioEL(), custodyParameters);
    if(debug) {
      log.info("getMetadadesPerDocument()::ESTADO_ELABORACION = ]" + estadoElaboracion + "[");
    }
    llistaMetadades.put(MetadataConstants.ENI_ESTADO_ELABORACION, estadoElaboracion);

    // (MetadatosDocumento.TIPO_DOC_ENI: TD01, TD02, TD03, ...
    String tipoDocEni = processEL(getPropertyTipusDocumentalEL(), custodyParameters);
    if(debug) {
      log.info("getMetadadesPerDocument()::TIPO_DOC_ENI = ]" + tipoDocEni + "[");
    }
    llistaMetadades.put(MetadataConstants.ENI_TIPO_DOCUMENTAL, tipoDocEni);

    if (signatureCustody != null) {
      String fileName = signatureCustody.getName();

      int i = fileName.lastIndexOf('.');
      String extensio = null;
      if (i != -1) {
        extensio = fileName.substring(i + 1).toLowerCase();
      }

      if (extensio != null) {
        if(debug) {
          log.info("getMetadadesPerDocument()::ENI_EXTENSION_FORMATO = ]" + extensio + "[");
        }
        llistaMetadades.put(MetadataConstants.ENI_EXTENSION_FORMATO, extensio);

        FormatosFichero format = FORMATS_BY_EXTENSION.get(extensio);

        if (format != null) {
          if(debug) {
            log.info("getMetadadesPerDocument()::ENI_NOMBRE_FORMATO = ]" + format + "[");
          }
          llistaMetadades.put(MetadataConstants.ENI_NOMBRE_FORMATO, format);
        }

      }

      // Afegir propietats associades al tipus de firma
      String tipusFirma = signatureCustody.getSignatureType();
      TiposFirma tipo = TIPOFIRMA_SIMPLE.get(tipusFirma);
      if (tipo == null) {
        tipo = TIPOFIRMA_COMPLEXE.get(tipusFirma + "_"
            + signatureCustody.getAttachedDocument());
      }
      if (tipo != null) {
        if(debug) {
          log.info("getMetadadesPerDocument()::ENI_TIPO_FIRMA = ]" + tipo.getValue() + "[");
        }
        llistaMetadades.put(MetadataConstants.ENI_TIPO_FIRMA, tipo.getValue());
      }

      // Afegir propietats associades al Perfil de Firma
      // (BASELINE B-Level,  EPES, T, C, X, XL, A, LTV,  BASELINE LT-Level,
      // BASELINE LTA-Level, BASELINE T-LevelLTV)
      String perfilFirma = processEL(getPropertyPerfilFirmaEL(), custodyParameters);
      if (debug) {
        log.info("getMetadadesPerDocument()::PERFIL FIRMA = ]" + perfilFirma + "[");
      }
      llistaMetadades.put(MetadataConstants.ENI_PERFIL_FIRMA, perfilFirma);

    }
    
    
    // MetadatosExpediente.FECHA_INICIO
    // Data de creació del Document
    // Date fecha = new SimpleDateFormat("dd/MM/yyyy").parse("04/04/2017");
    // String fechaCreacio = UtilidadesFechas.convertirDeDate_A_ISO8601(fecha);
    String templateDataCreacio = getPropertyDataCreacioDocumentEL();
    String dataCreacio;
    if (templateDataCreacio == null) {
      dataCreacio = UtilidadesFechas.fechaActualEnISO8601();
    } else {
      dataCreacio = processEL(templateDataCreacio, custodyParameters);
    }
    if (isDebug()) {
      log.info("DataCreacio Document: " + dataCreacio);
    }
    llistaMetadades.put(MetadataConstants.ENI_FECHA_INICIO, dataCreacio);

    return llistaMetadades;

  }

  protected Map<String, Object> getMetadadesComuns(Map<String, Object> custodyParameters)
      throws CustodyException, Exception {

    Map<String, Object> llistaMetadades = new HashMap<String, Object>();

    // MetadatosExpediente.CODIGO_CLASIFICACION
    String serieDocumental = processEL(getPropertySerieDocumentalEL(), custodyParameters);
    llistaMetadades.put(MetadataConstants.ENI_COD_CLASIFICACION, serieDocumental);
    // MetadatosExpediente.ORGANO
    String organos = processEL(getPropertyOrgansEL(), custodyParameters);
    final List<String> listaOrganos = Arrays.asList(organos.split(","));
    llistaMetadades.put(MetadataConstants.ENI_ORGANO, listaOrganos);
    // MetadatosExpediente.ORIGEN
    String origenDocument = processEL(getPropertyOrigenDocumentEL(), custodyParameters);
    llistaMetadades.put(MetadataConstants.ENI_ORIGEN, origenDocument);


    return llistaMetadades;
  }

  protected ApiArchivoDigital apiArxiuCache = null;

  public ApiArchivoDigital getApiArxiu(Map<String, Object> custodyParameters)
      throws CustodyException {

    try {

      if (apiArxiuCache == null) {
        // CabeceraPeticion cabecera = UtilCabeceras.generarCabeceraMock();

        CabeceraPeticion cabecera = new CabeceraPeticion();
        // intern api
        cabecera.setServiceVersion(ApiArchivoDigital.VERSION_SERVICIO);
        // aplicacio
        cabecera.setCodiAplicacion(getPropertyCodiAplicacio());
        cabecera.setUsuarioSeguridad(getPropertyLoginUsername());
        cabecera.setPasswordSeguridad(getPropertyLoginPassword());
        cabecera.setOrganizacion(getPropertyOrganitzacio());

        boolean debug = isDebug();

        // info login
        // + Ciutadà
        String ciutadaNom = processOptionalProperty("SolicitantNom",
            getPropertySolicitantNomEL(), custodyParameters);
        if (debug) {
          log.info(" LOGIN [ciutadaNom] = " + ciutadaNom);
        }

        cabecera.setNombreSolicitante(ciutadaNom);
        String ciutadaNIF = processOptionalProperty("SolicitantIdentificadorAdministratiu",
            getPropertySolicitantIdentificadorAdministratiuEL(), custodyParameters);
        cabecera.setDocumentoSolicitante(ciutadaNIF);
        if (debug) {
          log.info(" LOGIN [ciutadaNIF] = " + ciutadaNIF);
        }

        // + Funcionari o usuari de l'aplicació
        String funcionariUsername = processOptionalProperty("UsuariUsername",
            getPropertyUsuariUsernameEL(), custodyParameters);
        cabecera.setNombreUsuario(funcionariUsername);
        if (debug) {
          log.info(" LOGIN [funcionariUsername] = " + funcionariUsername);
        }

        String funcionariNIF = processOptionalProperty("UsuariIdentificadorAdministratiu",
            getPropertyUsuariIdentificadorAdministratiuEL(), custodyParameters);
        cabecera.setDocumentoUsuario(funcionariNIF);
        if (debug) {
          log.info(" LOGIN [funcionariNIF] = " + funcionariNIF);
        }

        // + info peticio
        cabecera.setNombreProcedimiento(processOptionalProperty("NomProcediment",
            getPropertyNomProcedimentEL(), custodyParameters));

        if (isPropertyIgnoreServerCeriticates()) {
          XTrustProvider.install();
        }

        // String urlBase = DatosConexion.ENDPOINT_DEV;

        ApiArchivoDigital apiArxiu = new ApiArchivoDigital(getPropertyURL(), cabecera);

        // TODO
        apiArxiu.setTrazas(false);

        apiArxiuCache = apiArxiu;

      }

      return apiArxiuCache;
    } catch (CustodyException ce) {
      throw ce;
    } catch (Exception e) {
      throw new CustodyException("Error instanciant l'API d'Arxiu Electrònic: "
          + e.getMessage(), e);
    }
  }

  /**
   * 
   * @param propertyValue
   * @param custodyParameters
   * @return
   */
  private String processOptionalProperty(String titol, String propertyValue,
      Map<String, Object> custodyParameters) {

    if (propertyValue == null || propertyValue.trim().length() == 0) {
      return propertyValue;
    }
    if (custodyParameters == null) {
      custodyParameters = new HashMap<String, Object>();
    }

    try {
      return AbstractDocumentCustodyPlugin.processExpressionLanguage(propertyValue,
          custodyParameters);
    } catch (CustodyException e) {
      log.warn(
          "S'ha intentat processar la propietat titulada " + titol + " amb valor "
              + propertyValue + " però s'ha produït un error al fer el processament EL: "
              + e.getMessage(), e);
      return null;
    }

  }



  /**
   * 
   * @param custodyParameters
   * @param ignoreErrors
   * @param prefix
   *          Por valer expedient, document, firma
   * @return
   * @throws CustodyException
   */
  protected List<Metadata> recollectAutomaticMetadatas(Map<String, Object> custodyParameters,
      String prefix) throws CustodyException {
    final boolean ignoreErrors = true;

    String propertyBase = getPropertyBase() + prefix + ".";

    final boolean debug = isDebug();

    if (debug) {
      log.info("recollectAutomaticMetadatas::Metadata property Base: " + propertyBase);
      log.info("recollectAutomaticMetadatas::" + propertyBase + "automaticmetadata_items = "
          + getProperty(propertyBase + "automaticmetadata_items"));
    }
    List<Metadata> list = AbstractDocumentCustodyPlugin.recollectAutomaticMetadatas(this,
        custodyParameters, getPropertyBase() + prefix + ".", ignoreErrors);

    if (list == null || list.isEmpty()) {
      if (debug) {
        log.info("recollectAutomaticMetadatas::LIST NULL or Empty: " + propertyBase
            + "(" + list + ")");
      }
      return new ArrayList<Metadata>();
    }

    if (debug) {
      log.info("recollectAutomaticMetadatas::LIST(" + list.size() + "): " + propertyBase );
    }
    return list;
  }



  protected String propertiesCustodyToStringEniDescripcion(Properties prop, String eni_desc) throws Exception {
    StringWriter writer = new StringWriter();
    
    if (eni_desc != null && eni_desc.trim().length() != 0) {
      prop.setProperty(FILE_INFO_BASE_METADATA + MetadataConstants.ENI_DESCRIPCION, eni_desc);
    }
    
    prop.store(writer, NO_MODIFY_TEXT);
    
    String core = writer.getBuffer().toString();
    
    core = URLEncoder.encode(core, "UTF-8");

    if (eni_desc == null || eni_desc.trim().length() == 0) {
      return core;
    } else {
      return eni_desc + "                                                    " + core;
    }
    
   
  }
  

  protected Properties stringEniDescriptionToPropertiesCustody(String str) throws IOException {
    Properties prop = new Properties();
    if (str == null || str.trim().length() == 0) {
      log.warn("La propietat que conte informació de DocumentCustody, SignatureCustody"
          + " i Metadades val null o està buida", new Exception());

    } else {
      // Cercar on comencen les propietats (al principi hi ha la descripció en clar
      
      int index = str.indexOf(NO_MODIFY_TEXT_URL_ENCODED);
      
      if (index == -1) {
           str = str.substring(index);      }
      
      String strOK = URLDecoder.decode(str, "UTF-8");
      prop.load(new StringReader(strOK));
    }
    return prop;
  }

  protected String createSimpleDocCSV(ApiArchivoDigital api, String csv,
      String expedientID, String carpetaID) throws IOException, CustodyException {
    Documento documento = new Documento();

    documento.setContent(null);
    documento.setName(csv + EXTENSIO_DOCUMENT_RESERVA);
    documento.setType(TiposObjetoSGD.DOCUMENTO);
    documento.setEncoding(ArxiuDigitalCAIBDocumentCustodyPlugin.FORMATO_UTF8);

    List<Aspectos> aspects = new ArrayList<Aspectos>();
    aspects.add(Aspectos.INTEROPERABLE);
    aspects.add(Aspectos.TRANSFERIBLE);
    documento.setAspects(aspects);
    documento.setMimetype("text/plain");

    Map<String, Object> llistaMetadades = new HashMap<String, Object>();
    
    llistaMetadades.put(MetadataConstants.ENI_CSV, csv);

    documento.setMetadataCollection(llistaMetadades);

    String uuidParent = (carpetaID == null) ? expedientID : carpetaID;
    CreateDraftDocumentResult result = api.crearDraftDocument(uuidParent, documento, false);
    String codi = result.getCreateDraftDocumentResult().getResult().getCode();
    if (hiHaError(codi)) {
      throw new CustodyException("Error creant DocumentSimple dins carpeta/Expedient"
          + " amb uuid " + uuidParent + "(" + codi  + " - "
          + result.getCreateDraftDocumentResult().getResult().getDescription() + ")");
    }
    
    return result.getCreateDraftDocumentResult().getResParam().getId();

  }


  protected String readSimpleDocCSV(ApiArchivoDigital api, String uuidDoc) throws IOException {

    final boolean retrieveContent = true;
    Resultado<Documento> resultat = api.obtenerDocumento(uuidDoc, retrieveContent);

    if (hiHaError(resultat.getCodigoResultado())) {
      log.error("No s'ha pogut llegir fitxer de CSV amb uuid " + uuidDoc + "("
          + resultat.getCodigoResultado() + ":" + resultat.getMsjResultado() + ")");
      return null;
    }
    
    Documento doc = resultat.getElementoDevuelto();
    String csv = (String)doc.getMetadataCollection().get(MetadataConstants.ENI_CSV);
    
    if (csv == null) {
      log.error("S'ha intentat llegir el CSV del fitxer amb uuid " + uuidDoc 
          + " però aquest val null");
    }
    
    return csv;

  }


  private Map<String, Nodo> getNodosByCustodyID2(ApiArchivoDigital apiArxiu, String custodyID)
      throws IOException, CustodyException {

    ExpedientCarpetaDocument ec = ExpedientCarpetaDocument.decodeCustodyID(custodyID);

    return getNodosByExpedientCarpeta(apiArxiu, ec.expedientID, ec.carpetaID);

  }

  protected Map<String, Nodo> getNodosByExpedientCarpeta(ApiArchivoDigital apiArxiu,
      //ExpedientCarpetaDocument ec
      String expedientID, String carpetaID
      ) throws IOException, CustodyException {
    // Cercam si hi ha un document
    List<Nodo> nodos;
    if (carpetaID == null) {
      Resultado<Expediente> expedient = apiArxiu.obtenerExpediente(expedientID);

      if (hiHaError(expedient.getCodigoResultado())) {
        throw new CustodyException(
            "Error intentant obtenir informació de l'expedient amb uuid " + expedientID
                + ": " + expedient.getCodigoResultado() + "-" + expedient.getMsjResultado());
      }

      nodos = expedient.getElementoDevuelto().getChilds();
    } else {
      Resultado<Directorio> dir = apiArxiu.obtenerDirectorio(carpetaID);
      if (hiHaError(dir.getCodigoResultado())) {
        throw new CustodyException(
            "Error intentant obtenir informació de la carpeta amb uuid " + carpetaID + ": "
                + dir.getCodigoResultado() + "-" + dir.getMsjResultado());
      }

      nodos = dir.getElementoDevuelto().getChilds();
    }
    Map<String, Nodo> nodosByName;
    nodosByName = new HashMap<String, Nodo>();
    if (nodos != null) {
      for (Nodo nodo : nodos) {
        nodosByName.put(nodo.getName(), nodo);
      }
    }

    return nodosByName;
  }

  
  
  protected String getCSV(String custodyID, Map<String, Object> custodyParameters)
      throws CustodyException {

    try {
      Map<String, List<Metadata>> metas = internalGetAllMetadatas(custodyID, custodyParameters);

      List<Metadata> list = metas.get(MetadataConstants.ENI_CSV);

      return list.get(0).getValue();

    } catch (CustodyException ce) {
      throw ce;
    } catch (Exception e) {
      throw new CustodyException("Error intentant obtenir el CSV de la custòdia amb id "
          + custodyID + ": " + e.getMessage(), e);
    }

  }

  /*
   * protected Properties getPropertiesFromCAIBDocument_All(ApiArchivoDigital
   * apiArxiu, String uuid) throws Exception, CustodyException { final boolean
   * custodyMetadatas = true; final boolean docElectronicMetadatas = true;
   * 
   * Properties prop = getPropertiesFromCAIBDocument(apiArxiu, uuid,
   * custodyMetadatas, docElectronicMetadatas); return prop; }
   * 
   * 
   * protected Properties getPropertiesFromCAIBDocument_Info(ApiArchivoDigital
   * apiArxiu, String uuid, boolean retrieveData) throws Exception,
   * CustodyException { final boolean custodyMetadatas = true; final boolean
   * docElectronicMetadatas = false;
   * 
   * Properties prop = getPropertiesFromCAIBDocument(apiArxiu, uuid,
   * custodyMetadatas, docElectronicMetadatas); return prop; }
   */

  protected FullInfoDocumentElectronic getFullInfoOfDocumentElectronic(
      ApiArchivoDigital apiArxiu, String uuid, boolean retrieveContent)
      throws CustodyException, IOException {
    
    Documento doc = getDocumento(apiArxiu, uuid, retrieveContent);

    Properties prop = new Properties();

    Map<String, Object> metas = doc.getMetadataCollection();

    String infoAndMetasStr = (String) metas.get(MetadataConstants.ENI_DESCRIPCION);

    if (infoAndMetasStr != null) {
      // Incloure Informació
      prop = stringEniDescriptionToPropertiesCustody(infoAndMetasStr);
    }

    return new FullInfoDocumentElectronic(prop, doc);
  }

  protected Documento getDocumento(ApiArchivoDigital apiArxiu, String uuid,
      boolean retrieveContent) throws IOException, CustodyException {
    Resultado<Documento> doc = apiArxiu.obtenerDocumento(uuid, retrieveContent);

    // Si no existeix s'ha de retornar null
    if (hiHaError(doc.getCodigoResultado())) {
      if ("COD_021".equals(doc.getCodigoResultado())
          && doc.getMsjResultado().startsWith("nodeId is not valid")) {
        return null;
      } else {
        throw new CustodyException(doc.getCodigoResultado() + ": " + doc.getMsjResultado());
      }
    }
    return doc.getElementoDevuelto();
  }

  protected Properties getMetadadesSenseEni(Properties metadades,
      Map<String, Object> metadataDocs) {
    Properties metadadesSenseEni = new Properties();
    metadadesSenseEni.putAll(metadades);

    // Eliminam les propietats ENI i GDIB del document
    if (metadataDocs != null) {
      for (String key : metadataDocs.keySet()) {
        // Object value = metadataDocs.get(key);
        metadadesSenseEni.remove(key);
      }
    }

    // Eliminam les propietats ENI i GDIB afegides manualment
    // List<String> keysEniGdibToDelete = new ArrayList<String>();
    boolean debug = isDebug();

    for (Object k : new ArrayList<Object>(metadadesSenseEni.keySet())) {
      String key = (String) k;
      if (key.startsWith("eni:") || key.startsWith("gdib:")) {
        if (debug) {
          log.warn("La metadada " + key + " no serà actualitzada !!!");
        }
        // keysEniGdibToDelete.add(key);
        metadadesSenseEni.remove(k);
      }
    }
    return metadadesSenseEni;
  }

  /**
   * 
   * @author anadal
   *
   */
  public class FilesInfo {

    public final Nodo nodoDocumentElectronic;

    public final Nodo nodoCSV;

    /**
     * @param nodoDocumentElectronic
     * @param nodoCSV
     */
    public FilesInfo(Nodo nodoDocumentElectronic, Nodo nodoCSV) {
      super();
      this.nodoDocumentElectronic = nodoDocumentElectronic;
      this.nodoCSV = nodoCSV;
    }

  }

  public class FullInfoDocumentElectronic {

    public final Properties infoAndMetas;

    public final Documento documento;

    /**
     * @param infoAndMetas
     * @param documento
     */
    public FullInfoDocumentElectronic(Properties infoAndMetas, Documento documento) {
      super();
      this.infoAndMetas = infoAndMetas;
      this.documento = documento;
    }

  }

  /**
   * 
   * @author anadal
   *
   */
  /*
   * public abstract class MetadataAction {
   * 
   * protected final ApiArchivoDigital apiArxiu;
   * 
   * protected final String custodyID;
   * 
   * public Object outputParameter = null;
   * 
   * 
   * public MetadataAction(ApiArchivoDigital apiArxiu, String custodyID) {
   * super(); this.apiArxiu = apiArxiu; this.custodyID = custodyID; }
   * 
   * public Properties doAction() throws CustodyException {
   * 
   * try { Map<String, Nodo> nodosByName = getNodosByCustodyID(apiArxiu,
   * custodyID); Nodo nodoMetas = nodosByName.get(NOM_METADADES); Properties
   * metadades = readPropertiesFromSimpleDoc(apiArxiu, nodoMetas.getId());
   * 
   * // Afegir metadades ENI i GDIB Nodo nodoDocElec =
   * nodosByName.get(NOM_DOCUMENT_ELECTRONIC); Map<String, Object> metadataDocs
   * = null; if (nodoDocElec != null) { Resultado<Documento> res =
   * apiArxiu.obtenerDocumento(nodoDocElec.getId(), false); metadataDocs =
   * res.getElementoDevuelto().getMetadataCollection();
   * 
   * for (String key : metadataDocs.keySet()) { Object value =
   * metadataDocs.get(key); metadades.setProperty(key, String.valueOf(value)); }
   * 
   * }
   * 
   * // metadatas.setProperty(metadata.getKey(), metadata.getValue()); if
   * (modificarMetadades(metadades)) {
   * 
   * Properties metadadesSenseEni = getMetadadesSenseEni(metadades,
   * metadataDocs);
   * 
   * // TODO Falta actualitzar metadades del document
   * 
   * // Actualitzam metadades createSimpleDoc(apiArxiu, NOM_METADADES,
   * metadadesSenseEni, custodyID, nodoMetas.getId());
   * 
   * }
   * 
   * return metadades;
   * 
   * } catch (CustodyException ce) { throw ce; } catch (Exception e) { throw new
   * CustodyException("Error desconegut: " + e.getMessage(), e); } }
   * 
   * 
   * 
   * public abstract boolean modificarMetadades(Properties metadades) throws
   * Exception;
   * 
   * }
   */

}
