/*
 * Created on Dec 29, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package test.jboost.booster;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * This class is a test suite for testing Boosters.
 * @author Tassapol Athiapinya, cschavis
 */
public class BoosterTestSuite {

  public static Test suite() {
    TestSuite suite = new TestSuite("Test for jboost.booster");
    // $JUnit-BEGIN$
    suite.addTest(new TestSuite(AdaBoostTest.class));
    suite.addTest(new TestSuite(LogLossBoostTest.class));
    suite.addTest(new TestSuite(RobustBoostTest.class));
    // $JUnit-END$
    return suite;
  }

}
