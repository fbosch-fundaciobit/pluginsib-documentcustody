<%@page import="java.io.ByteArrayOutputStream"
%><%@page import="java.io.OutputStream"
%><%@page import="java.io.FileInputStream"
%><%@page import="java.io.File"
%><%@page import="java.util.Properties"
%><%@page language="java" 
%><%!

 
  private static final String CUSTODY_PREFIX = << MODIFICA AQUEST VALOR AMB LA PROPIETAT PREFIX DEFINIDA AL PLUGIN !!!! >>;

  /** Modificar aquest valor segons si accedim as fitxer:
    *  custodyID (per exemple http://localhost:8080/custodia/index.jsp?custodyID={1}) ha de valer false
    *  hash  (per exemple http://localhost:8080/custodia/index.jsp?hash={2}) ha de valer true
	* Si volem suportar els dos sistemes llavors ha de valer false.
	*/
  private static final boolean onlyHash = true;

  public static final String PADES_SIGNATURE = "pades";

  public static final String CADES_SIGNATURE = "cades";

  public static final String XADES_SIGNATURE = "xades";

  public static final String SMIME_SIGNATURE = "smime";

  public static final String OOXML_SIGNATURE = "ooxml";

  public static final String ODT_SIGNATURE = "odt";

  public static final String NONE_SIGNATURE = "none";

  private final String getCustodyDocumentName(String custodyID) {
    return CUSTODY_PREFIX + custodyID + ".DOC";
  }
  
  private final String getCustodyDocumentInfoName(String custodyID) {
    return CUSTODY_PREFIX + custodyID + ".DOCINFO";
  }

  private final String getCustodySignatureName(String custodyID) {
    return CUSTODY_PREFIX + custodyID + ".SIGN";
  }

  private final String getCustodySignatureInfoName(String custodyID) {
    return CUSTODY_PREFIX +  custodyID + ".SIGNINFO";
  }
  
  private final String getCustodyHashesFile() {
    return CUSTODY_PREFIX + "HASH__FILE.properties";
  }

    
    public byte[] readFile(File info) throws Exception {
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      FileInputStream input = new FileInputStream(info);
      
      byte[] buffer = new byte[4096];
      int n = 0;
      while (-1 != (n = input.read(buffer))) {
        output.write(buffer, 0, n);
      }
     
      input.close();
      
      return output.toByteArray();
    }
    
    
    public String get(String match, String text) {
      
      /*
      <void property="mimeType"> 
      <string>text/plain</string> 
     </void> 
      */
      
      String search = "<void property=\"" + match + "\">";
      int index = text.indexOf(search) + search.length();
      
      index = text.indexOf("<string>", index) + "<string>".length();
      
      int index2 = text.indexOf("</string>", index);
      
      return text.substring(index, index2);
      
    }
    
    
    
%><%
    String custodyID = null;
    try {
		final File baseDir = new java.io.File(application.getRealPath("/"));
		
		System.out.println("BASE DIR INDEX:JSP : " + baseDir);

		
		String hash = request.getParameter("hash");
		
		if (hash != null) {
		  
		  File hashes = new File(baseDir, getCustodyHashesFile());
		  if (hashes.exists()) {
			Properties props = new Properties();
		    FileInputStream fis = new FileInputStream(hashes);
		    props.load(fis);
		    fis.close();
			custodyID = props.getProperty(hash);
			if (custodyID == null) {
				throw new Exception(" No existeix custodyId pel valor de hash " + hash); 
			}
		  } else {
			 throw new Exception("No existeix fitxer " + hashes.getAbsolutePath());
		  }
		} else {
		  if (onlyHash) {
			  throw new Exception("Nom&eacute;s es suporta HASH per obtenir el fitxer. No es permet la descarrega emprant directament l'identificador");
		  }
		  custodyID = request.getParameter("custodyID");
		}
		
		//System.out.println("   ---------------------------------------- ");
		//System.out.println("CustodyID : " + custodyID);
		
		
		
		File toDownload;
		String fileInfo;
		
		String type;
    
      
        File doc = new File(baseDir,getCustodyDocumentName(custodyID));
		
        //System.out.println(" Cercant document: " + doc.getAbsolutePath());
        if (doc.exists()) {
			toDownload = doc;
			fileInfo = getCustodyDocumentInfoName(custodyID);  
			type = "documentType";
        } else {
			File sign = new File(baseDir, getCustodySignatureName(custodyID));
			//System.out.println(" Cercant signatura: " + sign.getAbsolutePath());
			if (sign.exists()) {
				toDownload = sign;
				fileInfo = getCustodySignatureInfoName(custodyID);
				type = "signatureType";
			} else {
				System.out.println("ERROR: No s'ha trobat ni signatura ni document per ID " + custodyID);
				response.sendError(response.SC_NOT_FOUND);
				return;
			}
      }
	  
	  
	  String info = new String(readFile(new File(baseDir,fileInfo)));
	  String filename = get("name", info);
	  String signatureType = get(type, info);
	  String contentType;
	  if (PADES_SIGNATURE.equals(signatureType)) {
		contentType = "application/pdf";
	  } else if (XADES_SIGNATURE.equals(signatureType)) {
		contentType = "text/xml";
	  } else if (SMIME_SIGNATURE.equals(signatureType)) {
		contentType = "application/pkcs7-mime";
	  } else if (ODT_SIGNATURE.equals(signatureType)) {
		contentType = "application/vnd.oasis.opendocument.text";
	  } else {
		contentType = "application/octet-stream";
	  }

      FileInputStream input;

      response.setContentType(contentType);
      response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
      response.setContentLength((int) toDownload.length());

      OutputStream output = response.getOutputStream();
      input = new FileInputStream(toDownload);
      
      byte[] buffer = new byte[4096];
      int n = 0;
      while (-1 != (n = input.read(buffer))) {
        output.write(buffer, 0, n);
      }
     
      input.close();
 
      
    } catch (Exception e) {
      System.err.println("Error retornant document amd ID=" + custodyID + ": " + e.getMessage());
      e.printStackTrace(System.err);
      response.sendError(response.SC_NOT_FOUND, e.getMessage());
      return;
    }

%>