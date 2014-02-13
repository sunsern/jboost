/*
 * Created on Jan 5, 2004
 */
package test.jboost.controller;

import java.io.File;

import jboost.atree.InstrumentedAlternatingTree;
import jboost.booster.AdaBoost;
import jboost.booster.LogLossBoost;
import jboost.booster.RobustBoost;
import jboost.controller.Configuration;
import jboost.controller.Controller;
import jboost.monitor.Monitor;
import junit.framework.TestCase;

/**
 * This class is for testing Controller or main program.
 * @author cschavis, Tassapol Athiapinya
 */
public class ControllerTest extends TestCase {

  /**
   * Controller
   */
  private Controller m_controller;
  /**
   * Configuration
   */
  private Configuration m_config;

  /**
   * Sets up configuration file and initialize classes.
   * @see TestCase#setUp()
   */
  protected void setUp() throws Exception {
    super.setUp();
    String[] args = { "-CONFIG", "src/test/config/jboost.config" };
    System.out.println(args[1]);
    m_config = new Configuration(null, args);
    Monitor.init_log(m_config);
    m_controller = new Controller(m_config);

  }

  /**
   * Sets up configuration file for weighted examples
   * and initialize classes.
   */
  protected void setUpWeighted() throws Exception {
    String[] args = { "-CONFIG", "src/test/config/weightedjboost.config" };
    System.out.println(args[1]);
    m_config = new Configuration(null, args);
    Monitor.init_log(m_config);
    m_controller = new Controller(m_config);
  }

  /**
   * Empty constructor
   */
  public ControllerTest() {
	  
  }
  
  /**
   * Empty tearDown
   * @see TestCase#tearDown()
   */
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  /**
   * Tests the processing of configuration options This test executes
   * getInputFileNames(), getSpecFileName(), getTrainFileName()
   * getTestFileName(), getResultOutputFileName().
   */
  public final void testConfigurationOptions() {

  }

  /**
   * Tests building the default booster, which is AdaBoost This test passes in
   * different configuration options for the booster selection.
   */
  public final void testBuildingDefaultBooster() {
    // first verify that the correct booster is set up by default.
    // should be AdaBoost
    Class<?> boosterClass = m_controller.getBooster().getClass();
    AdaBoost adaboost = new AdaBoost();
    assertTrue(boosterClass.isInstance(adaboost));
  }

  /**
   * Tests building the LogLoss booster.
   */
  public final void testBuildingLogLossBooster() {
    // change booster type
    m_config.addOption("booster_type", "jboost.booster.LogLossBoost");
    try {
      m_controller = new Controller(m_config);
    }
    catch (Exception e) {
      fail("Unexepected Exception");
    }
    Class<?> boosterClass = m_controller.getBooster().getClass();
    LogLossBoost logloss = new LogLossBoost();
    assertTrue(boosterClass.isInstance(logloss));
  }

  /**
   * Tests building the Robust booster.
   */
  public final void testBuildingRobustBooster() {
    // change booster type
    m_config.addOption("booster_type", "jboost.booster.RobustBoost");
    try {
      m_controller = new Controller(m_config);
    }
    catch (Exception e) {
      fail("Unexepected Exception");
    }
    Class<?> boosterClass = m_controller.getBooster().getClass();
    RobustBoost robust = new RobustBoost();
    assertTrue(boosterClass.isInstance(robust));
  }
  
  /**
   * Tests building an invalid booster.
   */
  public final void testBuildingInvalidBooster() {
    // try to create controller with bogus booster
    m_config.addOption("booster_type", "jboost.booster.invalid");
    try {
      m_controller = new Controller(m_config);
      fail("Exception expected with invalid Booster name.");
    }
    catch (Exception success) {
    }
  }

  /**
   * Tests the read/write tree functionality.
   */
  public final void testLoadTree() {
    try {
      System.out.println("Learning from stream");
      m_controller.startLearning();
      m_controller.outputLearningResults();
      InstrumentedAlternatingTree tree1 = m_controller.getTree();
      // set config option for loading tree
      m_config.addOption("serialTreeInput", "src/test/config/atree.serialized");
      m_controller = null;
      m_controller = new Controller(m_config);
      m_controller.initializeTree();
      InstrumentedAlternatingTree tree2 = m_controller.getTree();
      assertTrue(tree1.roughlyEquals(tree2));
    }
    catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  /**
   * Tests demo files using AdaBoost Write out serialized tree, then re-read and
   * compare a result with original tree. Also, compares output to known result.
   */
  public final void testAdaBoostCycle() {
    try {
      int rounds = 80;
      // learn for 80 rounds, write to file, reload and compare
      m_config.addOption("numRounds", Integer.toString(rounds));
      m_controller.startLearning();
      m_controller.outputLearningResults();
      InstrumentedAlternatingTree firstTree = m_controller.getTree();
      m_config.addOption("serialTreeInput", "src/test/config/atree.serialized");
      m_controller = null;
      m_controller = new Controller(m_config);
      m_controller.initializeTree();
      InstrumentedAlternatingTree secondTree = m_controller.getTree();
      //System.out.println(firstTree.toString());
      //System.out.println(secondTree.toString());
      assertTrue(firstTree.roughlyEquals(secondTree));
      
      m_config.addOption("serialTreeInput", "src/test/config/atree.ada.serialized.expected_result");
      m_controller = null;
      m_controller = new Controller(m_config);
      m_controller.initializeTree();
      InstrumentedAlternatingTree expectedTree = m_controller.getTree();    
      assertTrue(firstTree.roughlyEquals(expectedTree));
    }
    catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  /**
   * Tests demo files using LogLossBoost Write out serialized tree, then re-read and
   * compare a result with original tree. Also, compares output to known result.
   */
  public final void testLogLossBoostCycle() {
    try {
      int rounds = 80;
      // learn for 80 rounds, write to file, reload and compare
      m_config.addOption("numRounds", Integer.toString(rounds));
      m_config.addOption("booster_type", "jboost.booster.LogLossBoost");
      m_controller = new Controller(m_config);
      m_controller.startLearning();
      m_controller.outputLearningResults();
      InstrumentedAlternatingTree firstTree = m_controller.getTree();
      m_config.addOption("booster_type", "jboost.booster.LogLossBoost");
      m_config.addOption("serialTreeInput", "src/test/config/atree.serialized");
      m_controller = null;
      m_controller = new Controller(m_config);
      m_controller.initializeTree();
      InstrumentedAlternatingTree secondTree = m_controller.getTree();
      //System.out.println(firstTree.toString());
      //System.out.println(secondTree.toString());
      assertTrue(firstTree.roughlyEquals(secondTree));
      
      m_config.addOption("serialTreeInput", "src/test/config/atree.logloss.serialized.expected_result");
      m_controller = null;
      m_controller = new Controller(m_config);
      m_controller.initializeTree();
      InstrumentedAlternatingTree expectedTree = m_controller.getTree();
      assertTrue(firstTree.roughlyEquals(expectedTree));
    }
    catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  /**
   * Tests demo files using RobustBoost Write out serialized tree, then re-read and
   * compare a result with original tree. Also, compares output to known result.
   */
  public final void testRobustBoostCycle() {
    try {
      int rounds = 80;
      // learn for 80 rounds, write to file, reload and compare
      m_config.addOption("numRounds", Integer.toString(rounds));
      m_config.addOption("booster_type", "jboost.booster.RobustBoost");
      m_config.addOption("rb_t", "0");
      m_config.addOption("rb_epsilon", "0.1");
      m_config.addOption("rb_theta", "0.0");
      m_config.addOption("rb_sigma_f", "0.01");
      m_controller = new Controller(m_config);
      m_controller.startLearning();
      m_controller.outputLearningResults();
      InstrumentedAlternatingTree firstTree = m_controller.getTree();
      double[] a1 = m_controller.getMarginsDistribution();

      m_config.addOption("booster_type", "jboost.booster.LogLossBoost");
      m_config.addOption("serialTreeInput", "src/test/config/atree.serialized");
      m_controller = null;
      m_controller = new Controller(m_config);
      m_controller.initializeTree();
      InstrumentedAlternatingTree secondTree = m_controller.getTree();
      double[] a2 = m_controller.getMarginsDistribution();

      for (int i = 0; i < a1.length; i++) {
        assertEquals(a1[i], a2[i], 1e-7);
      }
      //System.out.println(firstTree.toString());
      //System.out.println(secondTree.toString());  
      assertTrue(firstTree.roughlyEquals(secondTree));
      
      m_config.addOption("serialTreeInput", "src/test/config/atree.robust.serialized.expected_result");
      m_controller = null;
      m_controller = new Controller(m_config);
      m_controller.initializeTree();
      InstrumentedAlternatingTree expectedTree = m_controller.getTree();     
      assertTrue(firstTree.roughlyEquals(expectedTree));
    }
    catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }
  
}
