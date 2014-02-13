/*
 * Created on Jan 26, 2004
 *
 */
package test.jboost.atree;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * This class is a test suite for testing InstrumentedAlternatingTrees.
 * @author Tassapol Athiapinya, cschavis
 */
public class AtreeTestSuite {

  public static Test suite() {
    TestSuite suite = new TestSuite("Test for jboost.atree");
    // $JUnit-BEGIN$
    suite.addTestSuite(InstrumentedAlternatingTreeTest1.class);
    suite.addTestSuite(InstrumentedAlternatingTreeTest2.class);
    suite.addTestSuite(InstrumentedAlternatingTreeTest3.class);
    // $JUnit-END$
    return suite;
  }
}
