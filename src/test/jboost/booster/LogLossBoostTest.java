package test.jboost.booster;

import java.util.List;

import jboost.booster.LogLossBoost;
import jboost.booster.TmpData;
import jboost.booster.bag.AdaBoostBinaryBag;
import jboost.booster.bag.BinaryBag;
import jboost.booster.prediction.BinaryPrediction;
import jboost.examples.attributes.*;
import junit.framework.TestCase;

/**
 * This class is for testing LogLossBoost and its bag.
 * @author Tassapol Athiapinya
 *
 */
public class LogLossBoostTest extends TestCase {

	/**
	 * LogLossBoost
	 */
	private LogLossBoost m_booster1 = null;

	/**
	 * Sets up LogLossBoost 
	 */
	protected void setUp() throws Exception {
		m_booster1 = new LogLossBoost();
	
	}
	
	/**
	 * Empty constructor
	 */
	public LogLossBoostTest() {
		
	}
	
	/**
	 * Tests adding examples of label 0 and label 1.
	 * Checking is done on number of examples, number of positive examples and
	 * m_tmpList.
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
		assertEquals(1,m_booster1.getM_numPosExamples());
	}
	
	/**
	 * Tests calculate weight of LogLossBoost.
	 * Margins of 0.0, 1.0, -1.0 and 3.0 are checked.
	 */
	public void testCalculateWeight() {
		
		//System.out.println(m_booster1.calculateWeight(0.0));
		//System.out.println(m_booster1.calculateWeight(1.0));
		//System.out.println(m_booster1.calculateWeight(-1.0));
		//System.out.println(m_booster1.calculateWeight(3.0));
		assertEquals(0.5,m_booster1.calculateWeight(0.0));
		assertEquals(0.2689414213699951,m_booster1.calculateWeight(1.0));
		assertEquals(0.7310585786300049,m_booster1.calculateWeight(-1.0));
		assertEquals(0.04742587317756678,m_booster1.calculateWeight(3.0));
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
		
		double[] expectedMargin = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
		double[] expectedWeight = {0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5 };
		for (int i = 0; i < m_trainLabels.length; i++) {
			assertEquals(expectedMargin[i],m_booster1.getM_margins()[i]);
			assertEquals(expectedWeight[i],m_booster1.getM_weights()[i]);
			assertEquals(m_trainLabels[i],m_booster1.getM_labels()[i]);
		}
		//System.out.println(m_booster1.getM_numPosExamples());
		//System.out.println(m_booster1.getM_posExamples());
		assertEquals(4,m_booster1.getM_numPosExamples());
		assertEquals(0,m_booster1.getM_posExamples()[0]);
		assertEquals(2,m_booster1.getM_posExamples()[1]);
		assertEquals(3,m_booster1.getM_posExamples()[2]);
		assertEquals(5,m_booster1.getM_posExamples()[3]);
		assertEquals(4,m_booster1.getM_numNegExamples());
		assertEquals(1,m_booster1.getM_negExamples()[0]);
		assertEquals(4,m_booster1.getM_negExamples()[1]);
		assertEquals(6,m_booster1.getM_negExamples()[2]);
		assertEquals(7,m_booster1.getM_negExamples()[3]);
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
		BinaryPrediction[] predictions = { new BinaryPrediction(-0.91315892442759), 
											new BinaryPrediction(0.2399462578475506) };
		int[][] exampleIndex = {{4,7,11}, {0,6,8,9,1,2,3,5,10}};
		double[] m_weights = { 0.8460102442248163, 0.8460102442248163, 1.182018783846148, 0.8460102442248163, 0.8460102442248163, 1.182018783846148, 0.8460102442248163, 0.8460102442248163, 1.182018783846148, 1.182018783846148, 1.182018783846148, 0.8460102442248163 };
		short[] m_labels = { 0, 0, 1, 0, 0, 1, 0, 0, 1, 1, 1, 0 };
		double[] m_sampleWeights = { 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0 };
		double[] m_oldWeights = new double[12];
		double[] m_margins = {0.1672238104364421, 0.1672238104364421, -0.1672238104364421, 0.1672238104364421, 0.1672238104364421, -0.1672238104364421, 0.1672238104364421, 0.1672238104364421, -0.1672238104364421, -0.1672238104364421, -0.1672238104364421, 0.1672238104364421};
		m_booster1.setM_weights(m_weights);
		m_booster1.setM_labels(m_labels);
		m_booster1.setM_sampleWeights(m_sampleWeights);
		m_booster1.setM_totalWeight(11.832165628804454);
		m_booster1.setM_oldWeights(m_oldWeights);
		m_booster1.setM_margins(m_margins);
		m_booster1.update(predictions, exampleIndex);
		
		//System.out.println(m_booster1);
		
		double[] expectedM_weights = { 0.5181726036584247, 0.5181726036584247, 0.4818273963415754, 0.5181726036584247, 0.2534335944758885, 0.4818273963415754, 0.5181726036584247, 0.2534335944758885, 0.4818273963415754, 0.4818273963415754, 0.4818273963415754, 0.2534335944758885 };
		double[] expectedM_margins = { -0.0727224474111085, -0.0727224474111085, 0.0727224474111085, -0.0727224474111085, 1.080382734864032, 0.0727224474111085, -0.0727224474111085, 1.080382734864032, 0.0727224474111085, 0.0727224474111085, 0.0727224474111085, 1.080382734864032 };
		double expectedM_totalWeight = 11.832165628804454;
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
	 * Tests getting prediction value with simulated bag.
	 */
	public void testGetPrediction() {
		m_booster1.setM_epsilon(0.041666666666666664);
		m_booster1.setM_totalWeight(12.0);
		AdaBoostBinaryBag bag = new AdaBoostBinaryBag(m_booster1);
		bag.setM_w0(7.0);
		bag.setM_w1(5.0);
		BinaryPrediction p = (BinaryPrediction) m_booster1.getPrediction(bag);
		assertEquals(-0.1550774641519198,p.getClassScores()[1]);
		assertEquals(0.1550774641519198,p.getClassScores()[0]);
	}
}
