package org.fundaciobit.plugins.documentcustody.api.test;

import org.fundaciobit.plugins.documentcustody.api.IDocumentCustodyPlugin;

/**
 * 
 * @author anadal
 *
 */
public class InfoExecutionTest {

  protected String custodyID;

  protected IDocumentCustodyPlugin documentCustodyPlugin;

  /**
   * 
   */
  public InfoExecutionTest() {
    super();
  }

  /**
   * @param custodyID
   * @param documentCustodyPlugin
   */
  public InfoExecutionTest(String custodyID, IDocumentCustodyPlugin documentCustodyPlugin) {
    super();
    this.custodyID = custodyID;
    this.documentCustodyPlugin = documentCustodyPlugin;
  }

  public String getCustodyID() {
    return custodyID;
  }

  public void setCustodyID(String custodyID) {
    this.custodyID = custodyID;
  }

  public IDocumentCustodyPlugin getDocumentCustodyPlugin() {
    return documentCustodyPlugin;
  }

  public void setDocumentCustodyPlugin(IDocumentCustodyPlugin documentCustodyPlugin) {
    this.documentCustodyPlugin = documentCustodyPlugin;
  }

}
