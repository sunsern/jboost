package test.jboost.booster;

import java.util.List;

import jboost.booster.RobustBoost;
import jboost.booster.TmpData;
import jboost.booster.bag.Bag;
import jboost.booster.bag.BinaryBag;
import jboost.booster.bag.RobustBinaryBag;
import jboost.booster.prediction.RobustBinaryPrediction;
import jboost.examples.attributes.Label;
import junit.framework.TestCase;

/**
 * This class is for testing RobustBoost and its bag.
 * @author Tassapol Athiapinya
 *
*/
public class RobustBoostTest extends TestCase {

	/**
	 * RobustBoost
	 */
	private RobustBoost m_booster1 = null;

	/**
	 * Sets up RobustBoostt.
	 */
	protected void setUp() throws Exception {
		m_booster1 = new RobustBoost();
	
	}
	
	/**
	 * Empty constructor
	 */
	public RobustBoostTest() {
		
	}
	
	/**
	 * Tests initialization of RobustBoost.
	 * It checks values of epsilon, theta, sigma_f, cost, rho, t, old_t, last_ds and last_dt.
	 */
	public void testInit() {
		//System.out.println(m_booster1);
		assertEquals(0,m_booster1.getM_numExamples());
		assertEquals(0.1,m_booster1.getM_epsilon());
		assertEquals(2,m_booster1.getM_theta().length);
		assertEquals(0.0,m_booster1.getM_theta()[0]);
		assertEquals(0.0,m_booster1.getM_theta()[1]);
		assertEquals(2,m_booster1.getM_sigma_f().length);
		assertEquals(0.1,m_booster1.getM_sigma_f()[0]);
		assertEquals(0.1,m_booster1.getM_sigma_f()[1]);
		assertEquals(2,m_booster1.getM_cost().length);
		assertEquals(1.0,m_booster1.getM_cost()[0]);
		assertEquals(1.0,m_booster1.getM_cost()[1]);
		assertEquals(2,m_booster1.getM_rho().length);
		assertEquals(0.8603093292026393,m_booster1.getM_rho()[0]);
		assertEquals(0.8603093292026393,m_booster1.getM_rho()[1]);
		assertEquals(0.0,m_booster1.getM_t());
		assertEquals(0.0,m_booster1.getM_old_t());
		assertEquals(0.0,m_booster1.getM_last_ds());
		assertEquals(0.0,m_booster1.getM_last_dt());
	}
	
	/**
	 * Tests adding examples of label 0 and label 1.
	 * Checking is done on number of examples and m_tmpList.
	 */
	public void testAddExample() {
		Label l;
		List<TmpData> list;
		TmpData tmp0,tmp1;
			
		
		l = new Label(0);
		m_booster1.addExample(0, l);
		l = new Label(1);
		m_booster1.addExample(1, l);
		list = m_booster1.getM_tmpList();
		tmp0 = list.get(0);
		tmp1 = list.get(1);
		
		//System.out.println(tmp1);
		assertEquals(0, tmp0.getIndex());
		assertEquals(0, tmp0.getLabel());
		assertEquals(1.0, tmp0.getWeight());
		assertEquals(0.0, tmp0.getMargin());
		
		assertEquals(1, tmp1.getIndex());
		assertEquals(1, tmp1.getLabel());
		assertEquals(1.0, tmp1.getWeight());
		assertEquals(0.0, tmp1.getMargin());
		
		assertEquals(2,m_booster1.getM_numExamples());
		
	}
	
	/**
	 * Tests calculate weight of RobustBoost.
	 * Different values of label, margins and t are checked.
	 */
	public void testCalculateWeight() {

		assertEquals(0.2586008573439855,m_booster1.calculateWeight(new Label(0),0,0));
		assertEquals(0.2586008573439855,m_booster1.calculateWeight(new Label(1),0,0));
		assertEquals(0.5530606972921066,m_booster1.calculateWeight(new Label(0),-1.0,0));
		assertEquals(0.5530606972921066,m_booster1.calculateWeight(new Label(1),-1.0,0));
		assertEquals(0.10731520043551161,m_booster1.calculateWeight(new Label(0),0,-1.0));
		assertEquals(0.10731520043551161,m_booster1.calculateWeight(new Label(1),0,-1.0));
		assertEquals(0.15812251930105242,m_booster1.calculateWeight(new Label(0),-1.0,-1.0));
		assertEquals(0.15812251930105242,m_booster1.calculateWeight(new Label(1),-1.0,-1.0));
		assertEquals(0.0,m_booster1.calculateWeight(new Label(0),10.0,10.0));
		assertEquals(0.0,m_booster1.calculateWeight(new Label(1),10.0,10.0));
		assertEquals(0.053367705858897066,m_booster1.calculateWeight(new Label(0),-10.0,-10.0));
		assertEquals(0.053367705858897066,m_booster1.calculateWeight(new Label(1),-10.0,-10.0));
	}

	/**
	 * Tests clear data.
	 */
	public void testClear() {
		testFinalizeData();
		m_booster1.clear();
		assertNull(m_booster1.getM_labels());
		assertNull(m_booster1.getM_margins());
		assertNull(m_booster1.getM_potentials());
		assertNull(m_booster1.getM_weights());
		assertNull(m_booster1.getM_oldWeights());
		assertNull(m_booster1.getM_sampleWeights());
		assertEquals(0,m_booster1.getM_tmpList().size());
		assertEquals(0, m_booster1.getNumExamples());
		assertEquals(0.0,m_booster1.getTotalWeight());
		assertEquals(0.0,m_booster1.getM_t());
		assertEquals(0.0,m_booster1.getM_last_ds());
		assertEquals(0.0,m_booster1.getM_last_dt());
	}
	
	/**
	 * Tests adding non binary label. Make sure that An exception is raised and caught.
	 */
	public void testAddNonBinaryLabel() {
		Label l;
		boolean exRaised = false;
		
		try {
			l = new Label(-1);
			m_booster1.addExample(0, l);
		} catch (RuntimeException rEx) {
			exRaised = true;
		} finally {
			assertTrue(exRaised);
		}
		exRaised = false;
		try {
			l = new Label(2);
			m_booster1.addExample(1, l);
		} catch (RuntimeException rEx) {
			exRaised = true;
		} finally {
			assertTrue(exRaised);
		}
	}
	
	/**
	 * Tests finalize data. It should generate arrays of m_margins, m_weights,m_labels,
	 * positive example indices and negative example indices.
	 */
	public void testFinalizeData() {
		int[] m_trainLabels;
		Label l;
		
		m_trainLabels = new int[] { 0,1,0,0,1,0,1,1 };
		for (int i=0; i < m_trainLabels.length; i++) {
			l = new Label(m_trainLabels[i]);
			m_booster1.addExample(i, l);
		}
		m_booster1.finalizeData();
		//System.out.println(m_booster1);
		
		double expectedMargin = 0.0;
		double expectedWeight = 0.2586008573439855;
		for (int i = 0; i < m_trainLabels.length; i++) {
			assertEquals(expectedMargin,m_booster1.getM_margins()[i]);
			assertEquals(expectedWeight,m_booster1.getM_weights()[i]);
			assertEquals(m_trainLabels[i],m_booster1.getM_labels()[i]);
		}
		//System.out.println(m_booster1.getM_numPosExamples());
		//System.out.println(m_booster1.getM_posExamples());
		//System.out.println(m_booster1);
	}
	
	/**
	 * Tests creating a new bag. Weights of positive and negative example must be zero.
	 */
	public void testNewBag() {
		BinaryBag b = (BinaryBag) m_booster1.newBag();
		assertEquals(0.0,b.getM_w0());
		assertEquals(0.0,b.getM_w1());
	}
	
	/**
	 * Tests update margins and weights with simulated data.
	 */
	public void testUpdate() {
		RobustBinaryPrediction[] predictions = { new RobustBinaryPrediction(-0.91315892442759), 
											new RobustBinaryPrediction(0.2399462578475506) };
		int[][] exampleIndex = {{4,7,11}, {0,6,8,9,1,2,3,5,10}};
		double[] m_weights = { 0.8460102442248163, 0.8460102442248163, 1.182018783846148, 0.8460102442248163, 0.8460102442248163, 1.182018783846148, 0.8460102442248163, 0.8460102442248163, 1.182018783846148, 1.182018783846148, 1.182018783846148, 0.8460102442248163 };
		short[] m_labels = { 0, 0, 1, 0, 0, 1, 0, 0, 1, 1, 1, 0 };
		double[] m_sampleWeights = { 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0 };
		double[] m_oldWeights = new double[12];
		double[] m_potentials = new double[12];
		double[] m_margins = {0.1672238104364421, 0.1672238104364421, -0.1672238104364421, 0.1672238104364421, 0.1672238104364421, -0.1672238104364421, 0.1672238104364421, 0.1672238104364421, -0.1672238104364421, -0.1672238104364421, -0.1672238104364421, 0.1672238104364421};
		m_booster1.setM_weights(m_weights);
		m_booster1.setM_labels(m_labels);
		m_booster1.setM_sampleWeights(m_sampleWeights);
		m_booster1.setM_totalWeight(11.832165628804454);
		m_booster1.setM_oldWeights(m_oldWeights);
		m_booster1.setM_margins(m_margins);
		m_booster1.setM_potentials(m_potentials);
		m_booster1.setM_numExamples(12);
		m_booster1.update(predictions, exampleIndex);
		
		//System.out.println(m_booster1);
		
		double[] expectedM_weights = { 0.35884075989100395, 0.35884075989100395, 0.2870726079072544, 0.35884075989100395, 2.3266590963320225E-8, 0.2870726079072544, 0.35884075989100395, 2.3266590963320225E-8, 0.2870726079072544, 0.2870726079072544, 0.2870726079072544, 2.3266590963320225E-8 };
		double[] expectedM_margins = { -0.10721643774517556, -0.10721643774517556, 0.10721643774517556, -0.10721643774517556, 6.400363880701314, 0.10721643774517556, -0.10721643774517556, 6.400363880701314, 0.10721643774517556, 0.10721643774517556, 0.10721643774517556, 6.400363880701314 };
		double expectedM_totalWeight = 2.8707261489000606;
		double[] expectedM_oldWeight = { 0.8460102442248163, 0.8460102442248163, 1.182018783846148, 0.8460102442248163, 0.8460102442248163, 1.182018783846148, 0.8460102442248163, 0.8460102442248163, 1.182018783846148, 1.182018783846148, 1.182018783846148, 0.8460102442248163 };
		
		assertEquals(expectedM_totalWeight,m_booster1.getM_totalWeight());
		for (int i=0; i < m_labels.length; i++) {
			assertEquals(expectedM_weights[i],m_booster1.getM_weights()[i]);
			assertEquals(expectedM_margins[i],m_booster1.getM_margins()[i]);
			assertEquals(expectedM_oldWeight[i],m_booster1.getM_oldWeights()[i]);
		}
		//assert m_oldweights, m_margin, m_totalweight, m_weights, m_totalweight 
	}
	
	
	/**
	 * Tests getting prediction value with one simulated bag.
	 */
	public void testGetPrediction1() {
		m_booster1.setM_epsilon(0.041666666666666664);
		m_booster1.setM_totalWeight(12.0);
		Bag[] bags = new Bag[1];
		RobustBinaryBag bag = new RobustBinaryBag(m_booster1);
		bag.setM_w0(7.0);
		bag.setM_w1(5.0);
		bags[0] = bag;
		int[][] dummy = null;
		RobustBinaryPrediction[] p = (RobustBinaryPrediction[]) m_booster1.getPredictions(bags,dummy);
		assertEquals(1,p.length);
		assertTrue(p[0].equals(new RobustBinaryPrediction(1.0,0.0)));
	}
	
	/**
	 * Tests getting prediction value with two simulated bags.
	 */
	public void testGetPrediction2() {
		m_booster1.setM_epsilon(0.041666666666666664);
		m_booster1.setM_totalWeight(12.0);
		Bag[] bags = new Bag[2];
		RobustBinaryBag bag = new RobustBinaryBag(m_booster1);
		bag.setM_w0(7.0);
		bag.setM_w1(5.0);
		bags[0] = bag;
		bag = new RobustBinaryBag(m_booster1);
		bag.setM_w0(-7.0);
		bag.setM_w1(-5.0);
		bags[1] = bag;
		int[][] dummy = null;
		RobustBinaryPrediction[] p = (RobustBinaryPrediction[]) m_booster1.getPredictions(bags,dummy);
		assertEquals(2,p.length);
		assertTrue(p[0].equals(new RobustBinaryPrediction(1.0)));
		assertTrue(p[1].equals(new RobustBinaryPrediction(0.0)));
	}
	
	/**
	 * Tests calculate mu.
	 */
	public void testCalculateMu() {
		assertEquals(2.0,RobustBoost.calculateMu(2.0, 2.0, 2.0));
		assertEquals(0.7025574585997436,RobustBoost.calculateMu(2.0, 2.0, 0.5));
		assertEquals(1.175639364649936,RobustBoost.calculateMu(1.0, 1.5, 0.5));
	}
	
	/**
	 * Tests calculate sigma.
	 */
	public void testCalculateSigma() {
		assertEquals(1.0,RobustBoost.calculateSigma(1.0, 2.0));
		assertEquals(2.106315184609865,RobustBoost.calculateSigma(1.0, 0.5));
		assertEquals(1.8363236351181478,RobustBoost.calculateSigma(0.78, 0.5));
	}
	
	/**
	 * Tests calculate error function.
	 */
	public void testErf() {
		assertEquals(0.995322265010666, RobustBoost.erf(2.0));
		assertEquals(0.0, RobustBoost.erf(0.0));
		assertEquals(0.5204998778130466, RobustBoost.erf(0.5));
	}
	
	/**
	 * Tests calculate potential function.
	 */
	public void testCalculatePotential() {
		assertEquals(0.06922961428871999 , RobustBoost.calculatePotential(0.1, 0.2, 0.3, 2, 2.0, 0.5));
	}
	
}
