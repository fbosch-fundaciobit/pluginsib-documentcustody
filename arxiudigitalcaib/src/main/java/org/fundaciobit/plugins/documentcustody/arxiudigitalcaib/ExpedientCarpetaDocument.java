package org.fundaciobit.plugins.documentcustody.arxiudigitalcaib;

import org.fundaciobit.plugins.documentcustody.api.CustodyException;

/**
 * 
 * @author anadal
 *
 */
public class ExpedientCarpetaDocument {

  protected static final String SEPARATOR_EXPEDIENT_CARPETA_DOCUMENT = "#";

  public final String expedientID;

  public final String carpetaID;

  public final String documentID;

  /**
   * @param expedientID
   * @param carpetaID
   */
  public ExpedientCarpetaDocument(String expedientID, String documentID) {
    super();
    this.expedientID = expedientID;
    this.carpetaID = null;
    this.documentID = documentID;
  }

  /**
   * @param expedientID
   * @param carpetaID
   */
  public ExpedientCarpetaDocument(String expedientID, String carpetaID, String documentID) {
    super();
    this.expedientID = expedientID;
    this.carpetaID = carpetaID;
    this.documentID = documentID;
  }

  @Override
  public String toString() {

    StringBuffer sb = new StringBuffer("E: " + expedientID);

    if (carpetaID != null) {
      sb.append(" | C: " + carpetaID);
    }

    sb.append("| D: " + this.documentID);

    return sb.toString();
  }

  public static ExpedientCarpetaDocument decodeCustodyID(String custodyID)
      throws CustodyException {

    if (custodyID == null) {
      throw new CustodyException("El custodyID val null");
    }

    String[] parts = custodyID.split(SEPARATOR_EXPEDIENT_CARPETA_DOCUMENT);

    switch (parts.length) {

    case 2:
      return new ExpedientCarpetaDocument(parts[0], null, parts[1]);

    case 3:
      return new ExpedientCarpetaDocument(parts[0], parts[1], parts[2]);

    default:

      throw new CustodyException("El custodyID no té un format correcte: " + custodyID);

    }

    /*
     * int pos = custodyID.indexOf(SEPARATOR_EXPEDIENT_CARPETA);
     * 
     * if (pos == -1) { return new ExpedientCarpetaDocument(custodyID, null); }
     * else { return new ExpedientCarpetaDocument(custodyID.substring(0, pos),
     * custodyID.substring(pos + 1)); }
     */
  }
  
  
  public String encodeCustodyID() {
    if (carpetaID == null) {
      return expedientID + SEPARATOR_EXPEDIENT_CARPETA_DOCUMENT + documentID;
    } else {
      
      return expedientID + SEPARATOR_EXPEDIENT_CARPETA_DOCUMENT + carpetaID
          + SEPARATOR_EXPEDIENT_CARPETA_DOCUMENT + documentID;
    }
  }

}
