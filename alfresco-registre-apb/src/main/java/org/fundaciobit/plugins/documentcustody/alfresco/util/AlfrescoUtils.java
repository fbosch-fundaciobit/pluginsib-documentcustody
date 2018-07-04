package org.fundaciobit.plugins.documentcustody.alfresco.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.commons.io.IOUtils;
import org.fundaciobit.plugins.documentcustody.api.CustodyException;

/**
 * 
 * @author anadal (Adapta a DocumentCustody 3.0.0)
 *
 */
public class AlfrescoUtils {

	public static File bytesTofile(byte[] bFile, String fileName) {

        try {

        	File file = new File(fileName);
        	
        	FileOutputStream fileOuputStream = new FileOutputStream(file); 
		    fileOuputStream.write(bFile);
		    fileOuputStream.close();
		    
		    return file;

        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
	}
	
	public static byte[] getCmisObjectContent(Document cmisDoc) throws IOException {
		if (cmisDoc!=null) {
			ContentStream cs = cmisDoc.getContentStream();
			return IOUtils.toByteArray(cs.getStream());
		}else{
			return null;
		}
	}
	
	public static String getFileExtension(String name) {
		String extension = "";
		try {
			if (name!=null) {
				int indexLastDot = name.lastIndexOf(".");
				if (indexLastDot>0) {
					return name.substring(indexLastDot, name.length());
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return extension;
	}
	
	public static String getActualYear() {
		Calendar cal = Calendar.getInstance();
		return Integer.toString(cal.get(Calendar.YEAR));
	}
	
	public static String getFileNameWithCustodyId(String name, String custodyId, boolean signature) {
		String out = "";
		String sign = "";
		try {
			if (name!=null) {
				
				if (signature) { sign = "_SIGN"; }

				int indexLastDot = name.lastIndexOf(".");
				if (indexLastDot>0) {
					return name.substring(0, indexLastDot) + "_" + custodyId + sign + name.substring(indexLastDot, name.length());
				}
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return out;
	}
	
	public static String removeCustodyIdFromFilename(String name, boolean signature) {
		
		String out = "";
		try {
			
			if (name!=null) {
				
				if (signature) {
					name.replaceAll("_SIGN", "");
				}

				int indexLastDot = name.lastIndexOf(".");
				int indexLastGb  = name.lastIndexOf("_");
				if (indexLastDot>0) {
					return name.substring(0, indexLastGb) + name.substring(indexLastDot, name.length());
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return out;
	}
	
	public static List<Object> decodificaStringToObject(Map<String,Object> custodyParameters) throws UnsupportedEncodingException {
   /*
		ByteArrayInputStream stream=new ByteArrayInputStream(registreXML.getBytes("UTF-8"));
    	XMLDecoder xmlDec=new XMLDecoder(stream);
    	List<Object> out = new ArrayList<Object>();
    	
    	boolean seguentObjecte = true;
    	
    	while (seguentObjecte) {
    		try {
    			Object object=xmlDec.readObject();
    			out.add(object);
    		}catch (Exception objExc) {
    			seguentObjecte = false;
    		}
    	}

    	xmlDec.close();
    	return out;
    	*/
	  
	  List<Object> out = new ArrayList<Object>();
	  out.add(custodyParameters.get("registro"));
	  out.add(custodyParameters.get("anexo"));
	  
	  return out;
	  
	}
	
	public static String getPathFromRegistreObject(Object object) throws UnsupportedEncodingException, IllegalAccessException, ParseException, CustodyException{

    	//Variables necessaries per crear la ruta per el document
    	String regES  = "";
    	String regANY = "";
    	String regNUM = "";
    	
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy");

    	for (Field field : object.getClass().getDeclaredFields()) {
    		
    	    field.setAccessible(true);
    	    Object value = field.get(object);

    	    
    	    
    	    if (value != null) {
    	    	
    	    	//System.out.print(" * El valor del camp "+field.getName()+" del objecte registre es: "+value.toString());
    	    	
    	    	if ("destino".equals(field.getName())) { regES="Entrada"; }
    	    	if ("origen".equals(field.getName()))  { regES="Salida";  }
    	    	if ("numeroRegistro".equals(field.getName()))  { regNUM=value.toString();  }
    	    	if ("fecha".equals(field.getName())) {
   	    			regANY = sdf.format(AlfrescoUtils.getDateFieldFromDecodedObject("fecha", object));
    	    	}
    	    }else{
    	    	//System.out.print(" * El valor del camp "+field.getName()+" del objecte registre es: null");
    	    }
    	}
    	
    	if (regNUM==null || "".equals(regNUM)) { throw new CustodyException("No s´ha pogut establir correctament el directori a on guardar el fitxer. Numero de registre no s´ha pogut determinar."); }
    	if (regES==null || "".equals(regES)) { throw new CustodyException("No s´ha pogut establir correctament el directori a on guardar el fitxer. Tipus de registre (E/S) no s´ha pogut determinar."); }
    	if (regANY==null || "".equals(regANY)) {
    		throw new CustodyException("No s´ha pogut establir correctament el directori a on guardar el fitxer. La data del registre no s´ha pogut determinar.");
    		//regANY = sdf.format(new Date());
    	}
    	
    	return "/" + regANY + "/" + regES + "/" + regNUM;
	}
	
	//Fica dins els properties, els atributs corresponents als arxius de la APB
	  public static void getPropertiesFromRegistreObject(List<Object> registreDecod, Map<String, Object> properties, String custodyID) throws IllegalAccessException, ParseException {
		  
		  Object ObjecteRegistre = registreDecod.get(0);
		  
		  properties.put("APBRegistro:dr_tipDocumental", getFieldValueFromDecodedObject("registroDetalle.tipoDocumentacionFisica", ObjecteRegistre));
		  properties.put("APBRegistro:dr_ofiAnexa", getFieldValueFromDecodedObject("oficina.denominacion", ObjecteRegistre));
  		  properties.put("APBRegistro:dr_obsAlfresco", "");
  		  
  		  Object ObjecteAnexo = registreDecod.get(1);
  		  
  		  properties.put("cm:title", getFieldValueFromDecodedObject("titulo", ObjecteAnexo));
  		
  		  //LA RESTA DE PROPERTIES ES FIQUEN VIA ADDMETADATA UN COP PUJAT EL DOCUMENT
		  //properties.put("APBRegistro:dr_origen", "");
		  //properties.put("APBRegistro:dr_validez", "");
		  //properties.put("APBRegistro:dr_tipDocumental", "");
		  //properties.put("APBRegistro:dr_obsRegistro", "");
		  //properties.put("APBRegistro:dr_fecEntrada", new GregorianCalendar());
		  //properties.put("APBRegistro:dr_formato", "");
	  }
	  
	  public static void getFolderPropertiesFromRegistreObject(List<Object> registreDecod, Map<String, Object> properties) throws IllegalAccessException, ParseException, NoSuchFieldException {

		  Object ObjecteRegistre = registreDecod.get(0);
		  
		  if (!"".equals(getFieldValueFromDecodedObject("destino", ObjecteRegistre))) {
			  properties.put("APBRegistro:cr_tipRegistro", "E");
		  }else{
			  properties.put("APBRegistro:cr_tipRegistro", "S");
		  }
		  properties.put("APBRegistro:cr_ofiRegistro", getFieldValueFromDecodedObject("oficina.denominacion", ObjecteRegistre));
		  properties.put("APBRegistro:cr_fecRegistro", AlfrescoUtils.dateToGregorianCalendar(getDateFieldFromDecodedObject("fecha", ObjecteRegistre)));
		  properties.put("APBRegistro:cr_aplRegistro", getFieldValueFromDecodedObject("registroDetalle.aplicacion", ObjecteRegistre));
		  properties.put("APBRegistro:cr_codAsunto", getFieldValueFromDecodedObject("registroDetalle.tipoAsunto.codigo", ObjecteRegistre));
		  properties.put("APBRegistro:cr_resumen", getFieldValueFromDecodedObject("registroDetalle.extracto", ObjecteRegistre));

		  String descTA = "";
		  
		  try {
			  
			  Object tradsTA = getObjectFieldFromDecodedObject("registroDetalle.tipoAsunto", ObjecteRegistre);
			  Field auxValue = tradsTA.getClass().getSuperclass().getDeclaredField("traducciones");
			  auxValue.setAccessible(true);
			  HashMap<String, Object> tradAsuntos = (HashMap<String, Object>)auxValue.get(tradsTA);
			  
			  if (tradAsuntos!=null && tradAsuntos.keySet()!=null && tradAsuntos.keySet().size()>0) {
				  Set<String> claves = tradAsuntos.keySet();
				  
				  boolean tradTrobada = false;
				  for (int c=0; (c<claves.size() && !tradTrobada); c++) {
					  String traduccio = getFieldValueFromDecodedObject("nombre", tradAsuntos.get(claves.toArray()[c]));
					  if(traduccio!=null && !"".equals(traduccio) && !"null".equals(traduccio)) {
						  descTA = traduccio;
						  tradTrobada = true;
					  }
				  }
			  }
			  

		  }catch (Exception taExc) {
			  System.out.println("No s´ha pogut obtenir la traduccio del tipo Asunto per indicar com a metadada del document: "+taExc.getCause());
		  }
		  
		  properties.put("APBRegistro:cr_tipAsunto", descTA);
		  
		  properties.put("APBRegistro:cr_distribucion", "");
		  properties.put("APBRegistro:cr_fecDistribucion", null);
		  properties.put("APBRegistro:cr_obsRegistro", getFieldValueFromDecodedObject("registroDetalle.observaciones", ObjecteRegistre));
		  properties.put("APBRegistro:cr_obsAlfresco", "");
		  
		  String strInt = "";
		  
		  try {
		  
		  Object interesados = getObjectFieldFromDecodedObject("registroDetalle.interesados", ObjecteRegistre);
		  ArrayList<Object> inters = (ArrayList<Object>)interesados;
		  String tipoInteresado = "";

		  for (int i=0; i<inters.size(); i++) {

			  tipoInteresado = getFieldValueFromDecodedObject("tipo", inters.get(i));
			  
			  //Administració
			  if ("1".equals(tipoInteresado)) {
				  strInt += getFieldValueFromDecodedObject("nombre", inters.get(i)) + " (" + getFieldValueFromDecodedObject("codigoDir3", inters.get(i)) +") ; ";
			  //Persona fisica
			  }else if ("2".equals(tipoInteresado)) {
				  strInt += getFieldValueFromDecodedObject("nombre", inters.get(i));
				  
				  String ape1 = getFieldValueFromDecodedObject("apellido1", inters.get(i));
				  if (ape1!=null && !"".equals(ape1) && !"null".equals(ape1)) {
					  strInt += " "+ape1;
				  }
				  
				  String ape2 = getFieldValueFromDecodedObject("apellido2", inters.get(i));
				  if (ape2!=null && !"".equals(ape2) && !"null".equals(ape2)) {
					  strInt += " "+ape2;
				  }
				  
				  strInt += " (";
				  
				  strInt += getFieldValueFromDecodedObject("documento", inters.get(i)) +" ) ; ";
			  //Persona Jurídica
			  }else if ("3".equals(tipoInteresado)) {
				  strInt += getFieldValueFromDecodedObject("razonSocial", inters.get(i)) + " (" + getFieldValueFromDecodedObject("documento", inters.get(i)) +") ; ";
			  }
		  }
		  }catch (Exception intExc) {
			  System.out.println("No s´ha pogut obtenir la llista de tots els interessats per indicar com a metadada del document: "+intExc.getCause());
		  }
		  properties.put("APBRegistro:cr_interesados", strInt);
	  }
	  
	  public static GregorianCalendar dateToGregorianCalendar(java.util.Date in) {
		  if (in!=null) {
			  GregorianCalendar gc = new GregorianCalendar();
			  gc.setTime(in);
			  return gc;
		  }else{
			  return null;
		  }
	  }
	  
	  public static GregorianCalendar stringToGregorianCalendar(String in) {
		  //TODO: posar format de fecha com a property. Perque hi ha massa casos depenenet de versió de java, servidor, locale, etc..
		  try {
			  if (in!=null) {
				  
				  java.util.Date fechaDate = null;

				  //Eliminam la T que separa la data de la hora, ja que no es acceptada per el parser
				  in = in.replace("T", " ");
				  
				  try {
					  //Cas 1, fecha amb zona horaria (Exemple: 2015-06-11T15:45:46+02:00)
					  DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssX");
					  fechaDate = df.parse(in);
				  }catch (Exception pex) {
					  try {
						  //Cas 1, fecha tipo timestamp, amb milisegons (Exemple: 2015-06-10 14:08:06.419)
						  DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
						  fechaDate = df.parse(in);
					  }catch (Exception pex2) {
						  try {
							  int acabenSegons = in.lastIndexOf(":");
							  if (in.lastIndexOf(":")>0) {
								  DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
								  fechaDate = df.parse(in.substring(0, acabenSegons));
							  }
						 }catch (Exception pex3) {
							 int acabenSegons = in.indexOf("+");
							 if (acabenSegons>0) {
								  DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
								  fechaDate = df.parse(in.substring(0, acabenSegons));
							 }
						}
					  }
				  }
				  GregorianCalendar gc = new GregorianCalendar();
				  gc.setTime(fechaDate);
				  return gc;
			  }
		  }catch (Exception ex) {
			  System.out.println("No s´ha pogut establir com a date l'string " + in+": "+ex.getCause());
			  ex.printStackTrace();
		  }
		  return null;
	  }
	  
	  public static Object getObjectFieldFromDecodedObject(String fieldPath, Object decodedObj) {
		  
		  Object out = "";

		  try {
		  
			  if (fieldPath!=null) {
				  
				  if (fieldPath.indexOf(".")!=-1) {
				  
				  String[] pathFields = fieldPath.split("\\.");
				  Field auxValue = null;
				  Object auxObj = decodedObj;
				  
				  if (decodedObj!=null) {
				  
					  for (int p=0; p<pathFields.length; p++) {
						  auxValue = auxObj.getClass().getDeclaredField(pathFields[p]);
						  auxValue.setAccessible(true);
						  auxObj = auxValue.get(auxObj);
					  }
					  out = auxObj;
				  }
				  }else{
					  Field f = decodedObj.getClass().getDeclaredField(fieldPath);
					  f.setAccessible(true);
					  out = f.get(decodedObj);
				  }
			  }
		  
		  }catch(Exception ex) {
			  System.out.println("Error al obtenir l´objecte "+fieldPath+" de l´objecte decodificat: " + ex.getCause());
			  out = null;
		  }
		  
		  return out;
	  }

	  public static String getFieldValueFromDecodedObject(String fieldPath, Object decodedObj) {
		  
		  Object obj = getObjectFieldFromDecodedObject(fieldPath, decodedObj);
		  if (obj!=null) {
			  return obj.toString();
		  }else{
			  return "";
		  }
	  }
	  
	  public static java.util.Date getDateFieldFromDecodedObject(String fieldPath, Object decodedObj) {
		  
		  Object obj = getObjectFieldFromDecodedObject(fieldPath, decodedObj);
		  
		  if (obj!=null) {
			  java.util.Date data = (java.util.Date)obj;
			  return data;
		  }else{
			  return null;
		  }
	  }
}