package test.jboost;

import test.jboost.atree.AtreeTestSuite;
import test.jboost.booster.BoosterTestSuite;
import test.jboost.controller.ControllerTestSuite;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * This class calls every test suite in JBoost.
 * @author Tassapol Athiapinya
 *
 */
public class TestAll extends TestSuite {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test all for JBoost");
		suite.addTest(AtreeTestSuite.suite());
		suite.addTest(BoosterTestSuite.suite());
		suite.addTest(ControllerTestSuite.suite());
		return suite;
	}
}
