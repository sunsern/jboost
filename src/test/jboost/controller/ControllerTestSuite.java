/*
 * Created on Jan 4, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package test.jboost.controller;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * This class is a test suite for testing Controllers.
 * @author Tassapol Athiapinya, cschavis
 */
public class ControllerTestSuite {

  public static Test suite() {
    TestSuite suite = new TestSuite("Test for jboost.controller");
    // $JUnit-BEGIN$
    suite.addTestSuite(ConfigurationTest.class);
    suite.addTestSuite(ControllerTest.class);
    // $JUnit-END$
    return suite;
  }
}
