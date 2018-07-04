package org.fundaciobit.plugins.documentcustody.api;
/**
 * 
 * @author anadal
 *
 */
public class CustodyException extends Exception {

  /**
   * 
   */
  public CustodyException() {
    super();
  }

  /**
   * @param message
   * @param cause
   */
  public CustodyException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * @param message
   */
  public CustodyException(String message) {
    super(message);
  }

  /**
   * @param cause
   */
  public CustodyException(Throwable cause) {
    super(cause);
  }

}
