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
import jboost.examples.attributes.DiscreteAttribute;
import jboost.examples.attributes.IntegerAttribute;
import jboost.examples.attributes.Label;
import jboost.examples.attributes.descriptions.AttributeDescription;
import jboost.exceptions.InstrumentException;
import jboost.exceptions.NotSupportedException;
import jboost.learner.splitter_builders.EqualitySplitterBuilder;
import jboost.learner.splitter_builders.InequalitySplitterBuilder;
import jboost.learner.splitter_builders.SplitterBuilder;
import jboost.learner.splitters.EqualitySplitter;
import jboost.learner.splitters.InequalitySplitter;
import jboost.learner.splitters.Splitter;
import jboost.tokenizer.*;
import junit.framework.TestCase;

/**
 * This class is the JUnit test case for InstrumentedAlternatingTree
 * with input data of two features and binary label.
 * There are two types of splitters.
 * One is of type of equality on feature 1.
 * The other one is of type of inequality on feature 2.
 * The booster is AdaBoost.
 * @author Tassapol Athiapinya
 *
 */
public class InstrumentedAlternatingTreeTest3 extends TestCase {

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
		EqualitySplitterBuilder m_Builder1;
		InequalitySplitterBuilder m_Builder2;
		//InequalitySplitterBuilder m_IBuilder1;
		
		DataStream m_datastream = new jboost_DataStream(false, "feature1 (zero,one,two)\n feature2 number\n labels (one,two)\n");
	    exDescription = m_datastream.getExampleDescription();
	    //ExampleSet m_examples = new ExampleSet(description);
	    m_booster = new AdaBoost();
	    m_Builder1 = new EqualitySplitterBuilder(0, m_booster, new AttributeDescription[] { exDescription.getAttributeDescription(0) });
	    m_Builder2 = new InequalitySplitterBuilder(1, m_booster, new AttributeDescription[] { exDescription.getAttributeDescription(1) });
	    
	    int[] m_trainLabels = new int[] { 0, 0, 1, 0, 0, 1, 0, 0, 1, 1, 1, 0 };
	    int[] m_trainFeature1 = new int[] { 0, 2, 2, 2, 1, 2, 0, 1, 0, 0, 2, 1 };
	    int[] m_trainFeature2 = new int[] { 0, 200, 20, 200, 100000, 200, 0, 100, 0, 0, 2, 100 };
	    m_exampleIndices =  new int[m_trainLabels.length];
	    Attribute[] attributes = new Attribute[2];
	    
	    for (int i = 0; i < m_trainLabels.length; i++) {
	    	Label l = new Label(m_trainLabels[i]);
	    	attributes[0] = new DiscreteAttribute(m_trainFeature1[i]);
	    	attributes[1] = new IntegerAttribute(m_trainFeature2[i]);
	    	Example x = new Example(attributes, l);
	    	
	    	m_Builder1.addExample(i, x);
	    	m_Builder2.addExample(i, x);
	    	m_booster.addExample(i, l);
	    	m_exampleIndices[i] = i;
	    }
	    m_Builder1.finalizeData();
	    m_Builder2.finalizeData();
	    m_booster.finalizeData();
	    
	    m_builders = new Vector<SplitterBuilder>();
	    m_builders.add(m_Builder1);
	    m_builders.add(m_Builder2);
	}
	
	/**
	 * Empty constructor
	 */
	public InstrumentedAlternatingTreeTest3() {
		
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
		assertEquals(3,cands.size());
		AtreeCandidateSplit cand0 = (AtreeCandidateSplit) cands.get(0);
		AtreeCandidateSplit cand1 = (AtreeCandidateSplit) cands.get(1);
		AtreeCandidateSplit cand2 = (AtreeCandidateSplit) cands.get(2);
		assertEquals(-0.0010243850559170653,cand0.getLoss());
		assertEquals(0,cand0.getPredictorNode());
		assertNull(cand0.getSplitter());
		assertEquals(-2.88891204125599,cand1.getLoss());
		assertEquals(0,cand1.getPredictorNode());
		assertEquals(EqualitySplitter.class,cand1.getSplitter().getClass());
		assertEquals(2,((EqualitySplitter) cand1.getSplitter()).getDegree());
		assertEquals(0,((EqualitySplitter) cand1.getSplitter()).getIndex());
		assertEquals(1,((EqualitySplitter) cand1.getSplitter()).getValue());
		assertEquals(-1.7041937467631907,cand2.getLoss());
		assertEquals(0,cand2.getPredictorNode());
		assertEquals(InequalitySplitter.class,cand2.getSplitter().getClass());
		assertEquals(-1,((InequalitySplitter) cand2.getSplitter()).getDegree());
		assertEquals(1,((InequalitySplitter) cand2.getSplitter()).getIndex());
		assertEquals(60.0,((InequalitySplitter) cand2.getSplitter()).getThreshold());
		
		assertNull(cand0.getPartition());
		assertEquals(2.5690465157330253,cand1.getPartition()[0].getWeights()[0]);
		assertEquals(0.0,cand1.getPartition()[0].getWeights()[1]);
		assertEquals(3.4253953543107007,cand1.getPartition()[1].getWeights()[0]);
		assertEquals(5.8387420812114215,cand1.getPartition()[1].getWeights()[1]);
		assertEquals(1.7126976771553504,cand2.getPartition()[0].getWeights()[0]);
		assertEquals(4.670993664969138,cand2.getPartition()[0].getWeights()[1]);
		assertEquals(4.281744192888376,cand2.getPartition()[1].getWeights()[0]);
		assertEquals(1.167748416242284,cand2.getPartition()[1].getWeights()[1]);
		
		try {
			iat.addCandidate(cand1);
		} catch (InstrumentException e) {
			e.printStackTrace();
		}
		
		try {
			cands = iat.getCandidates();
		} catch (NotSupportedException e) {
			e.printStackTrace();
		}
		//System.out.println(iat);
		assertEquals(7,cands.size());
		cand0 = (AtreeCandidateSplit) cands.get(0);
		cand1 = (AtreeCandidateSplit) cands.get(1);
		cand2 = (AtreeCandidateSplit) cands.get(2);
		AtreeCandidateSplit cand3 = (AtreeCandidateSplit) cands.get(3);
		AtreeCandidateSplit cand4 = (AtreeCandidateSplit) cands.get(4);
		AtreeCandidateSplit cand5 = (AtreeCandidateSplit) cands.get(5);
		AtreeCandidateSplit cand6 = (AtreeCandidateSplit) cands.get(6);
		
		assertEquals(-0.031480427872781824,cand0.getLoss());
		assertEquals(0 ,cand0.getPredictorNode());
		assertNull(cand0.getSplitter());
		assertEquals(-1.0340687062194172 ,cand1.getLoss());
		assertEquals(0 ,cand1.getPredictorNode());
		assertEquals(EqualitySplitter.class,cand1.getSplitter().getClass());
		assertEquals(-0.8881198670207389 ,cand2.getLoss());
		assertEquals(0 ,cand2.getPredictorNode());
		assertEquals(InequalitySplitter.class,cand2.getSplitter().getClass());
		assertEquals(-1.030879170614471 ,cand3.getLoss());
		assertEquals(1 ,cand3.getPredictorNode());
		assertEquals(EqualitySplitter.class,cand3.getSplitter().getClass());
		assertEquals(-1.030879170614471 ,cand4.getLoss());
		assertEquals(1 ,cand4.getPredictorNode());
		assertEquals(InequalitySplitter.class,cand4.getSplitter().getClass());
		assertEquals(-0.04848196003774996 ,cand5.getLoss());
		assertEquals(2 ,cand5.getPredictorNode());
		assertEquals(EqualitySplitter.class,cand5.getSplitter().getClass());
		assertEquals(-0.4621800713655353 ,cand6.getLoss());
		assertEquals(2 ,cand6.getPredictorNode());
		assertEquals(InequalitySplitter.class,cand6.getSplitter().getClass());
		
		assertEquals(2,((EqualitySplitter) cand1.getSplitter()).getDegree());
		assertEquals(0,((EqualitySplitter) cand1.getSplitter()).getIndex());
		assertEquals(1,((EqualitySplitter) cand1.getSplitter()).getValue());
		assertEquals(-1,((InequalitySplitter)cand2.getSplitter()).getDegree());
		assertEquals(1,((InequalitySplitter)cand2.getSplitter()).getIndex());
		assertEquals(60.0,((InequalitySplitter)cand2.getSplitter()).getThreshold());
		assertEquals(2,((EqualitySplitter) cand3.getSplitter()).getDegree());
		assertEquals(0,((EqualitySplitter) cand3.getSplitter()).getIndex());
		assertEquals(0,((EqualitySplitter) cand3.getSplitter()).getValue());
		assertEquals(-1,((InequalitySplitter)cand4.getSplitter()).getDegree());
		assertEquals(1,((InequalitySplitter)cand4.getSplitter()).getIndex());
		assertEquals(-1.7976931348623157E308,((InequalitySplitter)cand4.getSplitter()).getThreshold());
		assertEquals(2,((EqualitySplitter) cand5.getSplitter()).getDegree());
		assertEquals(0,((EqualitySplitter) cand5.getSplitter()).getIndex());
		assertEquals(2,((EqualitySplitter) cand5.getSplitter()).getValue());
		assertEquals(-1,((InequalitySplitter)cand6.getSplitter()).getDegree());
		assertEquals(1,((InequalitySplitter)cand6.getSplitter()).getIndex());
		assertEquals(110.0,((InequalitySplitter)cand6.getSplitter()).getThreshold());
		
	}
	
	/**
	 * Learns for 100 rounds and checks the ADTree result. 
	 */
	public void testLearningResult() {
		InstrumentedAlternatingTree iat = new InstrumentedAlternatingTree(m_builders, m_booster, m_exampleIndices, new Configuration());
		int rounds = 100;
		loopLearning(rounds, iat);
		//System.out.println(iat);
		assertEquals(15,iat.getM_predictors().size());
		assertEquals(new BinaryPrediction(-0.1550774641519198 ),iat.getM_predictors().get(0).getPrediction());
		assertEquals(new BinaryPrediction(-3.328430372847054 ),iat.getM_predictors().get(1).getPrediction());
		assertEquals(new BinaryPrediction(0.39827270373275014 ),iat.getM_predictors().get(2).getPrediction());
		assertEquals(new BinaryPrediction(0.20797164092440826),iat.getM_predictors().get(3).getPrediction());
		assertEquals(new BinaryPrediction(-0.45017796804665944 ),iat.getM_predictors().get(4).getPrediction());
		assertEquals(new BinaryPrediction(-0.29907442736783274 ),iat.getM_predictors().get(5).getPrediction());
		assertEquals(new BinaryPrediction(1.6984244638315171 ),iat.getM_predictors().get(6).getPrediction());
		assertEquals(new BinaryPrediction(1.7773335190872086 ),iat.getM_predictors().get(7).getPrediction());
		assertEquals(new BinaryPrediction(-0.1027345124620859 ),iat.getM_predictors().get(8).getPrediction());
		assertEquals(new BinaryPrediction(-1.1792221140725871 ),iat.getM_predictors().get(9).getPrediction());
		assertEquals(new BinaryPrediction(-0.10676041809443705 ),iat.getM_predictors().get(10).getPrediction());
		assertEquals(new BinaryPrediction(-0.0493335914489763 ),iat.getM_predictors().get(11).getPrediction());
		assertEquals(new BinaryPrediction(1.199642018429217 ),iat.getM_predictors().get(12).getPrediction());
		assertEquals(new BinaryPrediction(-0.032517644906683865 ),iat.getM_predictors().get(13).getPrediction());
		assertEquals(new BinaryPrediction(-0.40710140763644603 ),iat.getM_predictors().get(14).getPrediction());
		
		assertEquals(7,iat.getM_splitters().size());
		SplitterNode sp = null;
		sp = iat.getM_splitters().get(0); 
		assertEquals("R.0" ,sp.getID());
		assertEquals(iat.getM_predictors().get(0),sp.getParent());
		assertEquals(iat.getM_predictors().get(1),sp.getPredictorNodes()[0]);
		assertEquals(iat.getM_predictors().get(2),sp.getPredictorNodes()[1]);
		assertEquals(0 ,sp.splitter.getIndex());
		assertEquals(2 ,sp.splitter.getDegree());
		assertEquals(1 ,((EqualitySplitter) sp.splitter).getValue());
		sp = iat.getM_splitters().get(1); 
		assertEquals("R.1" ,sp.getID());
		assertEquals(iat.getM_predictors().get(0),sp.getParent());
		assertEquals(iat.getM_predictors().get(3),sp.getPredictorNodes()[0]);
		assertEquals(iat.getM_predictors().get(4),sp.getPredictorNodes()[1]);
		assertEquals(1,sp.splitter.getIndex());
		assertEquals(-1,sp.splitter.getDegree());
		assertEquals(60.0,((InequalitySplitter) sp.splitter).getThreshold());
		sp = iat.getM_splitters().get(2); 
		assertEquals("R.1:0.0" ,sp.getID());
		assertEquals(iat.getM_predictors().get(3),sp.getParent());
		assertEquals(iat.getM_predictors().get(5),sp.getPredictorNodes()[0]);
		assertEquals(iat.getM_predictors().get(6),sp.getPredictorNodes()[1]);
		assertEquals(0,sp.splitter.getIndex());
		assertEquals(2,sp.splitter.getDegree());
		assertEquals(0,((EqualitySplitter) sp.splitter).getValue());
		sp = iat.getM_splitters().get(3); 
		assertEquals("R.1:0.1" ,sp.getID());
		assertEquals(iat.getM_predictors().get(3),sp.getParent());
		assertEquals(iat.getM_predictors().get(7),sp.getPredictorNodes()[0]);
		assertEquals(iat.getM_predictors().get(8),sp.getPredictorNodes()[1]);
		assertEquals(0,sp.splitter.getIndex());
		assertEquals(2,sp.splitter.getDegree());
		assertEquals(2,((EqualitySplitter) sp.splitter).getValue());
		sp = iat.getM_splitters().get(4); 
		assertEquals("R.1:1.0" ,sp.getID());
		assertEquals(iat.getM_predictors().get(4),sp.getParent());
		assertEquals(iat.getM_predictors().get(9),sp.getPredictorNodes()[0]);
		assertEquals(iat.getM_predictors().get(10),sp.getPredictorNodes()[1]);
		assertEquals(0 ,sp.splitter.getIndex());
		assertEquals(2 ,sp.splitter.getDegree());
		assertEquals(1 ,((EqualitySplitter) sp.splitter).getValue());
		sp = iat.getM_splitters().get(5); 
		assertEquals("R.1:0.2" ,sp.getID());
		assertEquals(iat.getM_predictors().get(3),sp.getParent());
		assertEquals(iat.getM_predictors().get(11),sp.getPredictorNodes()[0]);
		assertEquals(iat.getM_predictors().get(12),sp.getPredictorNodes()[1]);
		assertEquals(1 ,sp.splitter.getIndex());
		assertEquals(-1 ,sp.splitter.getDegree());
		assertEquals(1.0 ,((InequalitySplitter) sp.splitter).getThreshold());
		sp = iat.getM_splitters().get(6); 
		assertEquals("R.1:1.1",sp.getID());
		assertEquals(iat.getM_predictors().get(4),sp.getParent());
		assertEquals(iat.getM_predictors().get(13),sp.getPredictorNodes()[0]);
		assertEquals(iat.getM_predictors().get(14),sp.getPredictorNodes()[1]);
		assertEquals(0 ,sp.splitter.getIndex());
		assertEquals(2 ,sp.splitter.getDegree());
		assertEquals(2 ,((EqualitySplitter) sp.splitter).getValue());
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
	 * Tests add a candidate by simulating a candidate, add it and check the ADTree result.
	 */
	public void testAddCandidate() {
		double loss = -1.7041937467631907;
		BinaryBag[] partition = new BinaryBag[2];
		partition[0] = new AdaBoostBinaryBag(m_booster);
		partition[1] = new AdaBoostBinaryBag(m_booster);
		partition[0].setM_w0(1.7126976771553504);
		partition[0].setM_w1(4.670993664969138);
		partition[1].setM_w0(4.281744192888376);
		partition[1].setM_w1(1.167748416242284);
		//Splitter s = new EqualitySplitter(0, 1, 2, exDescription.getAttributeDescription(0));
		Splitter s = new InequalitySplitter(1, 60.0, exDescription.getAttributeDescription(1));
		CandidateSplit cand = new CandidateSplit(m_builders.get(1), s, partition, loss);
		
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
		assertEquals(new BinaryPrediction(0.4253267841170879),iat.getM_predictors().get(1).getPrediction());
		assertEquals(new BinaryPrediction(-0.528026337124657),iat.getM_predictors().get(2).getPrediction());
		assertEquals(1,iat.getM_splitters().size());
		SplitterNode sp = iat.getM_splitters().get(0); 
		assertEquals(iat.getM_predictors().get(0),sp.getParent());
		assertEquals(iat.getM_predictors().get(1),sp.getPredictorNodes()[0]);
		assertEquals(iat.getM_predictors().get(2),sp.getPredictorNodes()[1]);
		assertEquals(1,sp.splitter.getIndex());
		assertEquals(-1,sp.splitter.getDegree());
		assertEquals(60.0,((InequalitySplitter) sp.splitter).getThreshold());
	}
}
