package test.jboost.atree;

import java.util.Vector;

import jboost.CandidateSplit;
import jboost.atree.*;
import jboost.booster.*;
import jboost.booster.bag.AdaBoostBinaryBag;
import jboost.booster.bag.BinaryBag;
import jboost.booster.prediction.BinaryPrediction;
import jboost.controller.Configuration;
import jboost.controller.Controller;
import jboost.examples.*;
import jboost.examples.attributes.Attribute;
import jboost.examples.attributes.IntegerAttribute;
import jboost.examples.attributes.Label;
import jboost.examples.attributes.descriptions.AttributeDescription;
import jboost.exceptions.InstrumentException;
import jboost.exceptions.NotSupportedException;
import jboost.learner.splitter_builders.InequalitySplitterBuilder;
import jboost.learner.splitter_builders.SplitterBuilder;
import jboost.learner.splitters.InequalitySplitter;
import jboost.learner.splitters.Splitter;
import jboost.tokenizer.*;
import junit.framework.TestCase;

/**
 * This class is the JUnit test case for InstrumentedAlternatingTree
 * with input data of one feature and binary label.
 * Splitters are of type of inequality on its feature.
 * The booster is AdaBoost.
 * @author Tassapol Athiapinya
 *
 */
public class InstrumentedAlternatingTreeTest2 extends TestCase {

	/**
	 * AdaBoost
	 */
	private AdaBoost m_booster;
	/**
	 * Vector of splitter builders
	 */
	private Vector<SplitterBuilder> m_builders;
	/**
	 * Indices of examples
	 */
	private int[] m_exampleIndices;
	/**
	 * Example description of training data
	 */
	private ExampleDescription exDescription;
	
	/**
	 * Setups the training data, splitter builder and booster.
	 */
	protected void setUp() throws Exception {
		InequalitySplitterBuilder m_IEBuilder1;
		//InequalitySplitterBuilder m_IBuilder1;
		
		DataStream m_datastream = new jboost_DataStream(false, "feature1 number\n labels (one,two)\n");
	    exDescription = m_datastream.getExampleDescription();
	    //ExampleSet m_examples = new ExampleSet(description);
	    m_booster = new AdaBoost();
	    m_IEBuilder1 = new InequalitySplitterBuilder(0, m_booster, new AttributeDescription[] { exDescription.getAttributeDescription(0) });
	    
	    int[] m_trainLabels = new int[] { 0, 0, 1, 0, 0, 1, 0, 0, 1, 1, 1, 0 };
	    int[] m_trainFeature1 = new int[] { 0, 100, 200, 2, 1, 2, 0, 1, 0, 0, 2, 1 };
	    m_exampleIndices =  new int[m_trainLabels.length];
	    Attribute[] attributes = new Attribute[1];
	    
	    for (int i = 0; i < m_trainLabels.length; i++) {
	    	Label l = new Label(m_trainLabels[i]);
	    	attributes[0] = new IntegerAttribute(m_trainFeature1[i]);
	    	Example x = new Example(attributes, l);
	    	
	    	m_IEBuilder1.addExample(i, x);
	    	m_booster.addExample(i, l);
	    	m_exampleIndices[i] = i;
	    }
	    m_IEBuilder1.finalizeData();
	    m_booster.finalizeData();
	    
	    m_builders = new Vector<SplitterBuilder>();
	    m_builders.add(m_IEBuilder1);
	}
	
	/**
	 * Empty constructor
	 */
	public InstrumentedAlternatingTreeTest2() {
		
	}
	
	/**
	 * Tests add a candidate by simulating a candidate, add it and check the ADTree result.
	 */
	public void testAddCandidate() {
		double loss = -1.2501787069967858;
		BinaryBag[] partition = new BinaryBag[2];
		partition[0] = new AdaBoostBinaryBag(m_booster);
		partition[1] = new AdaBoostBinaryBag(m_booster);
		partition[0].setM_w0(5.994441870043727);
		partition[0].setM_w1(4.670993664969138);
		partition[1].setM_w0(0.0);
		partition[1].setM_w1(1.167748416242284);
		Splitter s = new InequalitySplitter(0, 150.0, exDescription.getAttributeDescription(0));
		CandidateSplit cand = new CandidateSplit(m_builders.get(0), s, partition, loss);
		
		AtreeCandidateSplit acand = new AtreeCandidateSplit(0, cand);
		InstrumentedAlternatingTree iat;
		iat = new InstrumentedAlternatingTree(m_builders, m_booster, m_exampleIndices, new Configuration());
		try {
			iat.addCandidate(acand);
		} catch (InstrumentException e) {
			e.printStackTrace();
		}
		//System.out.println(iat);
		
		assertEquals(3,iat.getM_predictors().size());
		assertEquals(new BinaryPrediction(-0.1550774641519198),iat.getM_predictors().get(0).getPrediction());
		assertEquals(new BinaryPrediction(-0.11407804656887706),iat.getM_predictors().get(1).getPrediction());
		assertEquals(new BinaryPrediction(0.6072220520966155),iat.getM_predictors().get(2).getPrediction());
		assertEquals(1,iat.getM_splitters().size());
		SplitterNode sp = iat.getM_splitters().get(0); 
		assertEquals(iat.getM_predictors().get(0),sp.getParent());
		assertEquals(iat.getM_predictors().get(1),sp.getPredictorNodes()[0]);
		assertEquals(iat.getM_predictors().get(2),sp.getPredictorNodes()[1]);
		assertEquals(0,sp.splitter.getIndex());
		assertEquals(-1,sp.splitter.getDegree());
		assertEquals(150.0,((InequalitySplitter) sp.splitter).getThreshold());
	}
	
	/**
	 * Tests get candidates, add a candidate and get candidates again.
	 */
	public void testGetAddCandidates1() {
		InstrumentedAlternatingTree iat;
		iat = new InstrumentedAlternatingTree(m_builders, m_booster, m_exampleIndices, new Configuration());
		Vector<CandidateSplit> cands = null;
		try {
			cands = iat.getCandidates();
		} catch (NotSupportedException e) {
			e.printStackTrace();
		}
		//System.out.println(cands);
		assertEquals(2,cands.size());
		AtreeCandidateSplit cand0 = (AtreeCandidateSplit) cands.get(0);
		AtreeCandidateSplit cand1 = (AtreeCandidateSplit) cands.get(1);
		assertEquals(-0.0010243850559170653,cand0.getLoss());
		assertEquals(0,cand0.getPredictorNode());
		assertNull(cand0.getSplitter());
		assertEquals(-1.2501787069967858,cand1.getLoss());
		assertEquals(0,cand1.getPredictorNode());
		assertEquals(InequalitySplitter.class,cand1.getSplitter().getClass());
		assertEquals(-1,((InequalitySplitter) cand1.getSplitter()).getDegree());
		assertEquals(0,((InequalitySplitter) cand1.getSplitter()).getIndex());
		assertEquals(150.0,((InequalitySplitter) cand1.getSplitter()).getThreshold());
		
		assertNull(cand0.getPartition());
		assertEquals(5.994441870043727,cand1.getPartition()[0].getWeights()[0]);
		assertEquals(4.670993664969138,cand1.getPartition()[0].getWeights()[1]);
		assertEquals(0.0,cand1.getPartition()[1].getWeights()[0]);
		assertEquals(1.167748416242284,cand1.getPartition()[1].getWeights()[1]);
		try {
			iat.addCandidate(cand1);
		} catch (InstrumentException e) {
			e.printStackTrace();
		}
		/**
		 * Here, iat should be
		 * 	0	[R] prediction = BinaryPrediction. p(1)= -0.1550774641519198
			1	[R.0] Splitter = InequalitySplitter. feature1 < 150.0
			1	[R.0:0] prediction = BinaryPrediction. p(1)= -0.11407804656887706
			1	[R.0:1] prediction = BinaryPrediction. p(1)= 0.6072220520966155
		 */
		try {
			cands = iat.getCandidates();
		} catch (NotSupportedException e) {
			e.printStackTrace();
		}
		
		//System.out.println(cands);
		assertEquals(4,cands.size());
		cand0 = (AtreeCandidateSplit) cands.get(0);
		cand1 = (AtreeCandidateSplit) cands.get(1);
		AtreeCandidateSplit cand2 = (AtreeCandidateSplit) cands.get(2);
		AtreeCandidateSplit cand3 = (AtreeCandidateSplit) cands.get(3);
		
		assertEquals(-0.012220670474685669,cand0.getLoss());
		assertEquals(0 ,cand0.getPredictorNode());
		assertNull(cand0.getSplitter());
		assertEquals(-0.6368625932403291 ,cand1.getLoss());
		assertEquals(0 ,cand1.getPredictorNode());
		assertEquals(InequalitySplitter.class,cand1.getSplitter().getClass());
		
		assertEquals(-1,((InequalitySplitter)cand1.getSplitter()).getDegree());
		assertEquals(0,((InequalitySplitter)cand1.getSplitter()).getIndex());
		assertEquals(150.0,((InequalitySplitter)cand1.getSplitter()).getThreshold());
		assertEquals(-0.7856467229031385 ,cand2.getLoss());
		assertEquals(1 ,cand2.getPredictorNode());
		assertEquals(InequalitySplitter.class,cand2.getSplitter().getClass());
		assertEquals(-1,((InequalitySplitter)cand2.getSplitter()).getDegree());
		assertEquals(0,((InequalitySplitter)cand2.getSplitter()).getIndex());
		assertEquals(51.0,((InequalitySplitter)cand2.getSplitter()).getThreshold());
		assertEquals(-0.6362621672349738 ,cand3.getLoss());
		assertEquals(2 ,cand3.getPredictorNode());
		assertEquals(InequalitySplitter.class,cand3.getSplitter().getClass());
		assertEquals(-1,((InequalitySplitter)cand3.getSplitter()).getDegree());
		assertEquals(0,((InequalitySplitter)cand3.getSplitter()).getIndex());
		assertEquals(-1.7976931348623157E308,((InequalitySplitter)cand3.getSplitter()).getThreshold());
		
		assertNull(cand0.getPartition());
		assertEquals(5.3481710269337235 ,cand1.getPartition()[0].getWeights()[0]);
		assertEquals(5.235434667102126 ,cand1.getPartition()[0].getWeights()[1]);
		assertEquals(2.220446049250313E-16 ,cand1.getPartition()[1].getWeights()[0]);
		assertEquals(0.6362621672349738 ,cand1.getPartition()[1].getWeights()[1]);
		assertEquals(4.58414659451462 ,cand2.getPartition()[0].getWeights()[0]);
		assertEquals(5.235434667102126 ,cand2.getPartition()[0].getWeights()[1]);
		assertEquals(0.7640244324191037 ,cand2.getPartition()[1].getWeights()[0]);
		assertEquals(0.0 ,cand2.getPartition()[1].getWeights()[1]);
		assertEquals(0.0 ,cand3.getPartition()[0].getWeights()[0]);
		assertEquals(0.0 ,cand3.getPartition()[0].getWeights()[1]);
		assertEquals(0.0 ,cand3.getPartition()[1].getWeights()[0]);
		assertEquals(0.6362621672349738 ,cand3.getPartition()[1].getWeights()[1]);
		
	}

	/**
	 * Loops for specific rounds for learning. In each loop, it selects the best candidate
	 * with best loss and add it to the tree. 
	 * @see Controller#learn(int)
	 * @param rounds number of rounds
	 * @param iat ADTree
	 */
	private void loopLearning(int rounds,InstrumentedAlternatingTree iat) {
		for (int i=0; i < rounds; i++) {
			Vector<CandidateSplit> cands = null;
			try {
				cands = iat.getCandidates();
			} catch (NotSupportedException e) {
				e.printStackTrace();
			}
	        
	        int bestIndex = 0;
	        double bestLoss = cands.get(0).getLoss();
	        double tmpLoss;
	        for (int j = 1; j < cands.size(); j++) {
	          if ((tmpLoss = cands.get(j).getLoss()) < bestLoss) {
	            bestLoss = tmpLoss;
	            bestIndex = j;
	          }
	        }
	        try {
				iat.addCandidate(cands.get(bestIndex));
			} catch (InstrumentException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Learns for 100 rounds and checks the ADTree result. 
	 */
	public void testLearningResult() {
		InstrumentedAlternatingTree iat = new InstrumentedAlternatingTree(m_builders, m_booster, m_exampleIndices, new Configuration());
		int rounds = 100;
		loopLearning(rounds, iat);
		//System.out.println(iat);
		assertEquals(9,iat.getM_predictors().size());
		assertEquals(new BinaryPrediction(-0.1550774641519198 ),iat.getM_predictors().get(0).getPrediction());
		assertEquals(new BinaryPrediction(-0.3164899559391501 ),iat.getM_predictors().get(1).getPrediction());
		assertEquals(new BinaryPrediction(4.127387707365453 ),iat.getM_predictors().get(2).getPrediction());
		assertEquals(new BinaryPrediction(0.39140795427472447 ),iat.getM_predictors().get(3).getPrediction());
		assertEquals(new BinaryPrediction(-3.5053318622395726 ),iat.getM_predictors().get(4).getPrediction());
		assertEquals(new BinaryPrediction(-0.2180487349915263 ),iat.getM_predictors().get(5).getPrediction());
		assertEquals(new BinaryPrediction(0.4174695014195815 ),iat.getM_predictors().get(6).getPrediction());
		assertEquals(new BinaryPrediction(0.29838294618911626 ),iat.getM_predictors().get(7).getPrediction());
		assertEquals(new BinaryPrediction(-4.785990800990959 ),iat.getM_predictors().get(8).getPrediction());
		
		assertEquals(4,iat.getM_splitters().size());
		SplitterNode sp = null;
		sp = iat.getM_splitters().get(0); 
		assertEquals(iat.getM_predictors().get(0),sp.getParent());
		assertEquals(iat.getM_predictors().get(1),sp.getPredictorNodes()[0]);
		assertEquals(iat.getM_predictors().get(2),sp.getPredictorNodes()[1]);
		assertEquals(0,sp.splitter.getIndex());
		assertEquals(-1,sp.splitter.getDegree());
		assertEquals(-1,((InequalitySplitter) sp.splitter).getDegree());
		assertEquals(0,((InequalitySplitter) sp.splitter).getIndex());
		assertEquals(150.0,((InequalitySplitter) sp.splitter).getThreshold());
		sp = iat.getM_splitters().get(1);
		assertEquals(iat.getM_predictors().get(1),sp.getParent());
		assertEquals(iat.getM_predictors().get(3),sp.getPredictorNodes()[0]);
		assertEquals(iat.getM_predictors().get(4),sp.getPredictorNodes()[1]);
		assertEquals(0,sp.splitter.getIndex());
		assertEquals(-1,sp.splitter.getDegree());
		assertEquals(-1,((InequalitySplitter) sp.splitter).getDegree());
		assertEquals(0,((InequalitySplitter) sp.splitter).getIndex());
		assertEquals(51.0,((InequalitySplitter) sp.splitter).getThreshold());
		sp = iat.getM_splitters().get(2); 
		assertEquals(iat.getM_predictors().get(3),sp.getParent());
		assertEquals(iat.getM_predictors().get(5),sp.getPredictorNodes()[0]);
		assertEquals(iat.getM_predictors().get(6),sp.getPredictorNodes()[1]);
		assertEquals(0,sp.splitter.getIndex());
		assertEquals(-1,sp.splitter.getDegree());
		assertEquals(-1,((InequalitySplitter) sp.splitter).getDegree());
		assertEquals(0,((InequalitySplitter) sp.splitter).getIndex());
		assertEquals(1.5,((InequalitySplitter) sp.splitter).getThreshold());
		sp = iat.getM_splitters().get(3); 
		assertEquals(iat.getM_predictors().get(5),sp.getParent());
		assertEquals(iat.getM_predictors().get(7),sp.getPredictorNodes()[0]);
		assertEquals(iat.getM_predictors().get(8),sp.getPredictorNodes()[1]);
		assertEquals(0,sp.splitter.getIndex());
		assertEquals(-1,sp.splitter.getDegree());
		assertEquals(-1,((InequalitySplitter) sp.splitter).getDegree());
		assertEquals(0,((InequalitySplitter) sp.splitter).getIndex());
		assertEquals(0.5,((InequalitySplitter) sp.splitter).getThreshold());
		
	}
	
}
