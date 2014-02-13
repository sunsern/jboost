package test.jboost.atree;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
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
import jboost.examples.attributes.Label;
import jboost.examples.attributes.descriptions.AttributeDescription;
import jboost.exceptions.InstrumentException;
import jboost.exceptions.NotSupportedException;
import jboost.learner.splitter_builders.EqualitySplitterBuilder;
import jboost.learner.splitter_builders.SplitterBuilder;
import jboost.learner.splitters.EqualitySplitter;
import jboost.learner.splitters.Splitter;
import jboost.tokenizer.*;
import junit.framework.TestCase;

/**
 * This class is the JUnit test case for InstrumentedAlternatingTree
 * with input data of one feature and binary label.
 * Splitters are of type of equality on its feature.
 * The booster is AdaBoost.
 * @author Tassapol Athiapinya
 *
 */
public class InstrumentedAlternatingTreeTest1 extends TestCase {

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
		EqualitySplitterBuilder m_EBuilder1;
		//InequalitySplitterBuilder m_IBuilder1;
		
		DataStream m_datastream = new jboost_DataStream(false, "feature1 (zero,one,two)\n labels (one,two)\n");
	    exDescription = m_datastream.getExampleDescription();
	    //ExampleSet m_examples = new ExampleSet(description);
	    m_booster = new AdaBoost();
	    m_EBuilder1 = new EqualitySplitterBuilder(0, m_booster, new AttributeDescription[] { exDescription.getAttributeDescription(0) });
	    
	    int[] m_trainLabels = new int[] { 0, 0, 1, 0, 0, 1, 0, 0, 1, 1, 1, 0 };
	    int[] m_trainFeature1 = new int[] { 0, 2, 2, 2, 1, 2, 0, 1, 0, 0, 2, 1 };
	    m_exampleIndices =  new int[m_trainLabels.length];
	    Attribute[] attributes = new Attribute[1];
	    
	    for (int i = 0; i < m_trainLabels.length; i++) {
	    	Label l = new Label(m_trainLabels[i]);
	    	attributes[0] = new DiscreteAttribute(m_trainFeature1[i]);
	    	Example x = new Example(attributes, l);
	    	
	    	m_EBuilder1.addExample(i, x);
	    	m_booster.addExample(i, l);
	    	m_exampleIndices[i] = i;
	    }
	    m_EBuilder1.finalizeData();
	    m_booster.finalizeData();
	    
	    m_builders = new Vector<SplitterBuilder>();
	    m_builders.add(m_EBuilder1);
	}
	
	/**
	 * Empty constructor
	 */
	public InstrumentedAlternatingTreeTest1() {
		
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
		assertEquals(-2.88891204125599,cand1.getLoss());
		assertEquals(0,cand1.getPredictorNode());
		assertEquals(cand1.getSplitter().getClass(),EqualitySplitter.class);
		assertEquals(2,((EqualitySplitter) cand1.getSplitter()).getDegree());
		assertEquals(0,((EqualitySplitter) cand1.getSplitter()).getIndex());
		assertEquals(1,((EqualitySplitter) cand1.getSplitter()).getValue());
		
		assertNull(cand0.getPartition());
		assertEquals(2.5690465157330253,cand1.getPartition()[0].getWeights()[0]);
		assertEquals(0.0,cand1.getPartition()[0].getWeights()[1]);
		assertEquals(3.4253953543107007,cand1.getPartition()[1].getWeights()[0]);
		assertEquals(5.8387420812114215,cand1.getPartition()[1].getWeights()[1]);
		try {
			iat.addCandidate(cand1);
		} catch (InstrumentException e) {
			e.printStackTrace();
		}
		/**
		 * Here, iat should be
		 * 0	[R] prediction = BinaryPrediction. p(1)= -0.1550774641519198
		 * 1	[R.0] Splitter = EqualitySplit: 2 feature1 = one
		 * 1	[R.0:0] prediction = BinaryPrediction. p(1)= -0.913122822649612
		 * 1	[R.0:1] prediction = BinaryPrediction. p(1)= 0.2399441942391387
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
		
		
		assertEquals(-0.031480427872781824 ,cand0.getLoss());
		assertEquals(0 ,cand0.getPredictorNode());
		assertEquals(-1.0340687062194172 ,cand1.getLoss());
		assertEquals(0 ,cand1.getPredictorNode());
		assertEquals(cand1.getSplitter().getClass(),EqualitySplitter.class);
		assertEquals(-1.030879170614471 ,cand2.getLoss());
		assertEquals(1 ,cand2.getPredictorNode());
		assertEquals(cand2.getSplitter().getClass(),EqualitySplitter.class);
		assertEquals(-0.04848196003774996 ,cand3.getLoss());
		assertEquals(2 ,cand3.getPredictorNode());
		assertEquals(cand3.getSplitter().getClass(),EqualitySplitter.class);
		
		assertEquals(2,((EqualitySplitter) cand1.getSplitter()).getDegree());
		assertEquals(0,((EqualitySplitter) cand1.getSplitter()).getIndex());
		assertEquals(1,((EqualitySplitter) cand1.getSplitter()).getValue());
		assertEquals(2,((EqualitySplitter) cand2.getSplitter()).getDegree());
		assertEquals(0,((EqualitySplitter) cand2.getSplitter()).getIndex());
		assertEquals(0,((EqualitySplitter) cand2.getSplitter()).getValue());
		assertEquals(2,((EqualitySplitter) cand3.getSplitter()).getDegree());
		assertEquals(0,((EqualitySplitter) cand3.getSplitter()).getIndex());
		assertEquals(2,((EqualitySplitter) cand3.getSplitter()).getValue());
		
		assertNull(cand0.getPartition());
		assertEquals(1.030879170614471 ,cand1.getPartition()[0].getWeights()[0]);
		assertEquals(0.0 ,cand1.getPartition()[0].getWeights()[1]);
		assertEquals(4.354287932550912 ,cand1.getPartition()[1].getWeights()[0]);
		assertEquals(4.593173513053193 ,cand1.getPartition()[1].getWeights()[1]);
		assertEquals(0.0 ,cand2.getPartition()[0].getWeights()[0]);
		assertEquals(0.0 ,cand2.getPartition()[0].getWeights()[1]);
		assertEquals(1.030879170614471 ,cand2.getPartition()[1].getWeights()[0]);
		assertEquals(0.0 ,cand2.getPartition()[1].getWeights()[1]);
		assertEquals(2.177143966275456 ,cand3.getPartition()[0].getWeights()[0]);
		assertEquals(2.755904107831916 ,cand3.getPartition()[0].getWeights()[1]);
		assertEquals(2.177143966275456 ,cand3.getPartition()[1].getWeights()[0]);
		assertEquals(1.837269405221277 ,cand3.getPartition()[1].getWeights()[1]);
		
	}
	
	/**
	 * Tests add a candidate by simulating a candidate, add it and check the ADTree result.
	 */
	public void testAddCandidate() {
		double loss = -2.88891204125599;
		BinaryBag[] partition = new BinaryBag[2];
		partition[0] = new AdaBoostBinaryBag(m_booster);
		partition[1] = new AdaBoostBinaryBag(m_booster);
		partition[0].setM_w0(2.5690465157330253);
		partition[0].setM_w1(0.0);
		partition[1].setM_w0(3.4253953543107007);
		partition[1].setM_w1(5.8387420812114215);
		Splitter s = new EqualitySplitter(0, 1, 2, exDescription.getAttributeDescription(0));
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
		assertEquals(new BinaryPrediction(-0.913122822649612),iat.getM_predictors().get(1).getPrediction());
		assertEquals(new BinaryPrediction(0.2399441942391387),iat.getM_predictors().get(2).getPrediction());
		assertEquals(1,iat.getM_splitters().size());
		SplitterNode sp = iat.getM_splitters().get(0); 
		assertEquals(iat.getM_predictors().get(0),sp.getParent());
		assertEquals(iat.getM_predictors().get(1),sp.getPredictorNodes()[0]);
		assertEquals(iat.getM_predictors().get(2),sp.getPredictorNodes()[1]);
		assertEquals(0,sp.splitter.getIndex());
		assertEquals(2,sp.splitter.getDegree());
		assertEquals(1,((EqualitySplitter) sp.splitter).getValue());
	}
	
	/**
	 * Learns for 100 rounds and checks the ADTree result. 
	 */
	public void testLearningResult() {
		InstrumentedAlternatingTree iat = new InstrumentedAlternatingTree(m_builders, m_booster, m_exampleIndices, new Configuration());
		int rounds = 100;
		loopLearning(rounds, iat);
		//System.out.println(iat);
		assertEquals(11,iat.getM_predictors().size());
		assertEquals(new BinaryPrediction(-0.1550774641519198 ),iat.getM_predictors().get(0).getPrediction());
		assertEquals(new BinaryPrediction(-4.627311942575493 ),iat.getM_predictors().get(1).getPrediction());
		assertEquals(new BinaryPrediction(0.2787563203139271 ),iat.getM_predictors().get(2).getPrediction());
		assertEquals(new BinaryPrediction(0.07905369272194795 ),iat.getM_predictors().get(3).getPrediction());
		assertEquals(new BinaryPrediction(-0.10341566531131641 ),iat.getM_predictors().get(4).getPrediction());
		assertEquals(new BinaryPrediction(-0.5290188352402914 ),iat.getM_predictors().get(5).getPrediction());
		assertEquals(new BinaryPrediction(-0.01914443728907655 ),iat.getM_predictors().get(6).getPrediction());
		assertEquals(new BinaryPrediction(-0.001118753537654138 ),iat.getM_predictors().get(7).getPrediction());
		assertEquals(new BinaryPrediction(-0.48679484081551744 ),iat.getM_predictors().get(8).getPrediction());
		assertEquals(new BinaryPrediction(0.0 ),iat.getM_predictors().get(9).getPrediction());
		assertEquals(new BinaryPrediction(-0.07511091739754386 ),iat.getM_predictors().get(10).getPrediction());
		assertEquals(5,iat.getM_splitters().size());
		SplitterNode sp = null;
		sp = iat.getM_splitters().get(0); 
		assertEquals(iat.getM_predictors().get(0),sp.getParent());
		assertEquals(iat.getM_predictors().get(1),sp.getPredictorNodes()[0]);
		assertEquals(iat.getM_predictors().get(2),sp.getPredictorNodes()[1]);
		assertEquals(0,sp.splitter.getIndex());
		assertEquals(2,sp.splitter.getDegree());
		assertEquals(1,((EqualitySplitter) sp.splitter).getValue());
		sp = iat.getM_splitters().get(1);
		assertEquals(iat.getM_predictors().get(0),sp.getParent());
		assertEquals(iat.getM_predictors().get(3),sp.getPredictorNodes()[0]);
		assertEquals(iat.getM_predictors().get(4),sp.getPredictorNodes()[1]);
		assertEquals(0,sp.splitter.getIndex());
		assertEquals(2,sp.splitter.getDegree());
		assertEquals(2,((EqualitySplitter) sp.splitter).getValue());
		sp = iat.getM_splitters().get(2); 
		assertEquals(iat.getM_predictors().get(4),sp.getParent());
		assertEquals(iat.getM_predictors().get(5),sp.getPredictorNodes()[0]);
		assertEquals(iat.getM_predictors().get(6),sp.getPredictorNodes()[1]);
		assertEquals(0,sp.splitter.getIndex());
		assertEquals(2,sp.splitter.getDegree());
		assertEquals(1,((EqualitySplitter) sp.splitter).getValue());
		sp = iat.getM_splitters().get(3); 
		assertEquals(iat.getM_predictors().get(4),sp.getParent());
		assertEquals(iat.getM_predictors().get(7),sp.getPredictorNodes()[0]);
		assertEquals(iat.getM_predictors().get(8),sp.getPredictorNodes()[1]);
		assertEquals(0,sp.splitter.getIndex());
		assertEquals(2,sp.splitter.getDegree());
		assertEquals(0,((EqualitySplitter) sp.splitter).getValue());
		sp = iat.getM_splitters().get(4); 
		assertEquals(iat.getM_predictors().get(1),sp.getParent());
		assertEquals(iat.getM_predictors().get(9),sp.getPredictorNodes()[0]);
		assertEquals(iat.getM_predictors().get(10),sp.getPredictorNodes()[1]);
		assertEquals(0,sp.splitter.getIndex());
		assertEquals(2,sp.splitter.getDegree());
		assertEquals(0,((EqualitySplitter) sp.splitter).getValue());
	}
	
	/**
	 * Tests whether {@code getCombinedPredictor()} generates an AlternatingTree which is
	 * equal to InstrumentedAlternatingTree.
	 * @see InstrumentedAlternatingTree#getCombinedPredictor()
	 */
	public void testGetCombinedPredictor() {
		InstrumentedAlternatingTree iat = new InstrumentedAlternatingTree(m_builders, m_booster, m_exampleIndices, new Configuration());
		int rounds = 100;
		loopLearning(rounds, iat);
		AlternatingTree tree = (AlternatingTree) iat.getCombinedPredictor();
		testEqualByRef(iat, tree);
	}
	
	/**
	 * Learns for 100 rounds, serialize the tree, read it back and compare two ADTrees. 
	 */
	public void testSerialize() {
		InstrumentedAlternatingTree iat = new InstrumentedAlternatingTree(m_builders, m_booster, m_exampleIndices, new Configuration());
		int rounds = 100;
		loopLearning(rounds, iat);
		AlternatingTree tree = (AlternatingTree) iat.getCombinedPredictor();
		// serialize the tree
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream os = null;
		try {
			os = new ObjectOutputStream(bos);
			os.writeObject(tree);
			os.flush();
			os.close();
		} catch (IOException ioEx) {
			ioEx.printStackTrace();
		}
		
		// de-serialize
		ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
		ObjectInputStream is = null;
		AlternatingTree newTree = null;

		try {
			is = new ObjectInputStream(bis);
			newTree = (AlternatingTree) is.readObject();
			is.close();
		} catch (IOException ioEx) {
			ioEx.printStackTrace();
		} catch (ClassNotFoundException cEx) {
			cEx.printStackTrace();
		}
		assertEquals(tree.toString(),newTree.toString());
	    
	}
	
	/**
	 * Learns for 20 rounds with ADD_ALL flag and check the ADTree result.
	 */
	public void testBuildSplittersAddAll() {
		InstrumentedAlternatingTree iat = new InstrumentedAlternatingTree(m_builders, m_booster, m_exampleIndices, new Configuration());
		int rounds = 20;
		iat.setM_treeType(AtreeType.ADD_ALL);
		loopLearning(rounds, iat);
		/**
		 * iat should be
		 	0	[R] prediction = BinaryPrediction. p(1)= -0.1550774641519198
		 	1	[R.0] Splitter = EqualitySplit: 2 feature1 = one
			1	[R.0:0] prediction = BinaryPrediction. p(1)= -3.994697090407493
			1	[R.0:1] prediction = BinaryPrediction. p(1)= 0.2722679771453203
			2	[R.1] Splitter = EqualitySplit: 2 feature1 = two
			2	[R.1:0] prediction = BinaryPrediction. p(1)= 0.07905369272194795
			2	[R.1:1] prediction = BinaryPrediction. p(1)= -0.10341566531131641
			3	[R.1:1.0] Splitter = EqualitySplit: 2 feature1 = one
			3	[R.1:1.0:0] prediction = BinaryPrediction. p(1)= -0.057026849077307734
			3	[R.1:1.0:1] prediction = BinaryPrediction. p(1)= -0.006874989699742461
		 */	
		ArrayList<PredictorNode> m_p = iat.getM_predictors();
		ArrayList<SplitterNode> m_s = iat.getM_splitters();
		assertEquals(7,m_p.size());
		assertEquals(3,m_s.size());
		assertEquals(new BinaryPrediction(-0.1550774641519198),iat.getM_predictors().get(0).getPrediction());
		assertEquals(new BinaryPrediction(-3.994697090407493),iat.getM_predictors().get(1).getPrediction());
		assertEquals(new BinaryPrediction(0.2722679771453203),iat.getM_predictors().get(2).getPrediction());
		assertEquals(new BinaryPrediction(0.07905369272194795),iat.getM_predictors().get(3).getPrediction());
		assertEquals(new BinaryPrediction(-0.10341566531131641),iat.getM_predictors().get(4).getPrediction());
		assertEquals(new BinaryPrediction(-0.057026849077307734),iat.getM_predictors().get(5).getPrediction());
		assertEquals(new BinaryPrediction(-0.006874989699742461),iat.getM_predictors().get(6).getPrediction());
		SplitterNode sp = null;
		sp = m_s.get(0); 
		assertEquals("R.0",sp.getID());
		assertEquals(iat.getM_predictors().get(0),sp.getParent());
		assertEquals(iat.getM_predictors().get(1),sp.getPredictorNodes()[0]);
		assertEquals(iat.getM_predictors().get(2),sp.getPredictorNodes()[1]);
		assertEquals(0,sp.splitter.getIndex());
		assertEquals(2,sp.splitter.getDegree());
		assertEquals(1,((EqualitySplitter) sp.splitter).getValue());
		sp = m_s.get(1); 
		assertEquals("R.1",sp.getID());
		assertEquals(iat.getM_predictors().get(0),sp.getParent());
		assertEquals(iat.getM_predictors().get(3),sp.getPredictorNodes()[0]);
		assertEquals(iat.getM_predictors().get(4),sp.getPredictorNodes()[1]);
		assertEquals(0,sp.splitter.getIndex());
		assertEquals(2,sp.splitter.getDegree());
		assertEquals(2,((EqualitySplitter) sp.splitter).getValue());
		sp = m_s.get(2); 
		assertEquals("R.1:1.0",sp.getID());
		assertEquals(iat.getM_predictors().get(4),sp.getParent());
		assertEquals(iat.getM_predictors().get(5),sp.getPredictorNodes()[0]);
		assertEquals(iat.getM_predictors().get(6),sp.getPredictorNodes()[1]);
		assertEquals(0,sp.splitter.getIndex());
		assertEquals(2,sp.splitter.getDegree());
		assertEquals(1,((EqualitySplitter) sp.splitter).getValue());
	}
	
	/**
	 * Learns for 20 rounds with ADD_ROOT flag and check the ADTree result.
	 */
	public void testBuildSplittersAddRoot() {
		InstrumentedAlternatingTree iat = new InstrumentedAlternatingTree(m_builders, m_booster, m_exampleIndices, new Configuration());
		int rounds = 20;
		iat.setM_treeType(AtreeType.ADD_ROOT);
		loopLearning(rounds, iat);
		/**
		 * iat should be
		 	0	[R] prediction = BinaryPrediction. p(1)= -0.1550774641519198
			1	[R.0] Splitter = EqualitySplit: 2 feature1 = one
			1	[R.0:0] prediction = BinaryPrediction. p(1)= -4.051723423992754
			1	[R.0:1] prediction = BinaryPrediction. p(1)= 0.2696302852229931
			2	[R.1] Splitter = EqualitySplit: 2 feature1 = two
			2	[R.1:0] prediction = BinaryPrediction. p(1)= 0.07905369272194795
			2	[R.1:1] prediction = BinaryPrediction. p(1)= -0.10341566531131641
		 */	
		ArrayList<PredictorNode> m_p = iat.getM_predictors();
		ArrayList<SplitterNode> m_s = iat.getM_splitters();
		assertEquals(5,m_p.size());
		assertEquals(2,m_s.size());
		assertEquals(new BinaryPrediction(-0.1550774641519198),iat.getM_predictors().get(0).getPrediction());
		assertEquals(new BinaryPrediction(-4.051723423992754),iat.getM_predictors().get(1).getPrediction());
		assertEquals(new BinaryPrediction(0.2696302852229931),iat.getM_predictors().get(2).getPrediction());
		assertEquals(new BinaryPrediction(0.07905369272194795),iat.getM_predictors().get(3).getPrediction());
		assertEquals(new BinaryPrediction(-0.10341566531131641),iat.getM_predictors().get(4).getPrediction());
		SplitterNode sp = null;
		sp = m_s.get(0); 
		assertEquals("R.0",sp.getID());
		assertEquals(iat.getM_predictors().get(0),sp.getParent());
		assertEquals(iat.getM_predictors().get(1),sp.getPredictorNodes()[0]);
		assertEquals(iat.getM_predictors().get(2),sp.getPredictorNodes()[1]);
		assertEquals(0,sp.splitter.getIndex());
		assertEquals(2,sp.splitter.getDegree());
		assertEquals(1,((EqualitySplitter) sp.splitter).getValue());
		sp = m_s.get(1); 
		assertEquals("R.1",sp.getID());
		assertEquals(iat.getM_predictors().get(0),sp.getParent());
		assertEquals(iat.getM_predictors().get(3),sp.getPredictorNodes()[0]);
		assertEquals(iat.getM_predictors().get(4),sp.getPredictorNodes()[1]);
		assertEquals(0,sp.splitter.getIndex());
		assertEquals(2,sp.splitter.getDegree());
		assertEquals(2,((EqualitySplitter) sp.splitter).getValue());
	}
	
	/**
	 * Learns for 3 rounds with ADD_SINGLES flag and check the ADTree result.
	 */
	public void testBuildSplittersAddSingles() {
		InstrumentedAlternatingTree iat = new InstrumentedAlternatingTree(m_builders, m_booster, m_exampleIndices, new Configuration());
		int rounds = 3;
		iat.setM_treeType(AtreeType.ADD_SINGLES);
		loopLearning(rounds, iat);
		/**
		 * iat should be
		   	0	[R] prediction = BinaryPrediction. p(1)= -0.1550774641519198
			1	[R.0] Splitter = EqualitySplit: 2 feature1 = one
			1	[R.0:0] prediction = BinaryPrediction. p(1)= -0.913122822649612
			2	[R.0:0.0] Splitter = EqualitySplit: 2 feature1 = zero
			2	[R.0:0.0:0] prediction = BinaryPrediction. p(1)= 0.0
			2	[R.0:0.0:1] prediction = BinaryPrediction. p(1)= -0.6234414877642134
			3	[R.0:0.0:1.0] Splitter = EqualitySplit: 2 feature1 = zero
			3	[R.0:0.0:1.0:0] prediction = BinaryPrediction. p(1)= 0.0
			3	[R.0:0.0:1.0:1] prediction = BinaryPrediction. p(1)= -0.43693231540780036
			1	[R.0:1] prediction = BinaryPrediction. p(1)= 0.2399441942391387
		 */
		ArrayList<PredictorNode> m_p = iat.getM_predictors();
		ArrayList<SplitterNode> m_s = iat.getM_splitters();
		assertEquals(7,m_p.size());
		assertEquals(3,m_s.size());
		assertEquals(new BinaryPrediction(-0.1550774641519198),iat.getM_predictors().get(0).getPrediction());
		assertEquals(new BinaryPrediction(-0.913122822649612),iat.getM_predictors().get(1).getPrediction());
		assertEquals(new BinaryPrediction(0.2399441942391387),iat.getM_predictors().get(2).getPrediction());
		assertEquals(new BinaryPrediction(0.0),iat.getM_predictors().get(3).getPrediction());
		assertEquals(new BinaryPrediction(-0.6234414877642134),iat.getM_predictors().get(4).getPrediction());
		assertEquals(new BinaryPrediction(0.0),iat.getM_predictors().get(5).getPrediction());
		assertEquals(new BinaryPrediction(-0.43693231540780036),iat.getM_predictors().get(6).getPrediction());
		SplitterNode sp = null;
		sp = m_s.get(0); 
		assertEquals("R.0",sp.getID());
		assertEquals(iat.getM_predictors().get(0),sp.getParent());
		assertEquals(iat.getM_predictors().get(1),sp.getPredictorNodes()[0]);
		assertEquals(iat.getM_predictors().get(2),sp.getPredictorNodes()[1]);
		assertEquals(0,sp.splitter.getIndex());
		assertEquals(2,sp.splitter.getDegree());
		assertEquals(1,((EqualitySplitter) sp.splitter).getValue());
		sp = m_s.get(1); 
		assertEquals("R.0:0.0",sp.getID());
		assertEquals(iat.getM_predictors().get(1),sp.getParent());
		assertEquals(iat.getM_predictors().get(3),sp.getPredictorNodes()[0]);
		assertEquals(iat.getM_predictors().get(4),sp.getPredictorNodes()[1]);
		assertEquals(0,sp.splitter.getIndex());
		assertEquals(2,sp.splitter.getDegree());
		assertEquals(0,((EqualitySplitter) sp.splitter).getValue());
		sp = m_s.get(2); 
		assertEquals("R.0:0.0:1.0",sp.getID());
		assertEquals(iat.getM_predictors().get(4),sp.getParent());
		assertEquals(iat.getM_predictors().get(5),sp.getPredictorNodes()[0]);
		assertEquals(iat.getM_predictors().get(6),sp.getPredictorNodes()[1]);
		assertEquals(0,sp.splitter.getIndex());
		assertEquals(2,sp.splitter.getDegree());
		assertEquals(0,((EqualitySplitter) sp.splitter).getValue());
	}
	
	/**
	 * Learns for 20 rounds with ADD_ROOT_OR_SINGLES flag and check the ADTree result.
	 */
	public void testBuildSplittersAddRootOrSingles() {
		InstrumentedAlternatingTree iat = new InstrumentedAlternatingTree(m_builders, m_booster, m_exampleIndices, new Configuration());
		int rounds = 20;
		iat.setM_treeType(AtreeType.ADD_ROOT_OR_SINGLES);
		loopLearning(rounds, iat);
		/**
		 * iat should be
		   	0	[R] prediction = BinaryPrediction. p(1)= -0.1550774641519198
			1	[R.0] Splitter = EqualitySplit: 2 feature1 = one
			1	[R.0:0] prediction = BinaryPrediction. p(1)= -3.994697090407493
			1	[R.0:1] prediction = BinaryPrediction. p(1)= 0.2722679771453203
			2	[R.1] Splitter = EqualitySplit: 2 feature1 = two
			2	[R.1:0] prediction = BinaryPrediction. p(1)= 0.07905369272194795
			2	[R.1:1] prediction = BinaryPrediction. p(1)= -0.10341566531131641
			3	[R.1:1.0] Splitter = EqualitySplit: 2 feature1 = one
			3	[R.1:1.0:0] prediction = BinaryPrediction. p(1)= -0.057026849077307734
			3	[R.1:1.0:1] prediction = BinaryPrediction. p(1)= -0.006874989699742461

		 */
		ArrayList<PredictorNode> m_p = iat.getM_predictors();
		ArrayList<SplitterNode> m_s = iat.getM_splitters();
		assertEquals(7,m_p.size());
		assertEquals(3,m_s.size());
		assertEquals(new BinaryPrediction(-0.1550774641519198),iat.getM_predictors().get(0).getPrediction());
		assertEquals(new BinaryPrediction(-3.994697090407493),iat.getM_predictors().get(1).getPrediction());
		assertEquals(new BinaryPrediction(0.2722679771453203),iat.getM_predictors().get(2).getPrediction());
		assertEquals(new BinaryPrediction(0.07905369272194795),iat.getM_predictors().get(3).getPrediction());
		assertEquals(new BinaryPrediction(-0.10341566531131641),iat.getM_predictors().get(4).getPrediction());
		assertEquals(new BinaryPrediction(-0.057026849077307734),iat.getM_predictors().get(5).getPrediction());
		assertEquals(new BinaryPrediction(-0.006874989699742461),iat.getM_predictors().get(6).getPrediction());
		SplitterNode sp = null;
		sp = m_s.get(0); 
		assertEquals("R.0",sp.getID());
		assertEquals(iat.getM_predictors().get(0),sp.getParent());
		assertEquals(iat.getM_predictors().get(1),sp.getPredictorNodes()[0]);
		assertEquals(iat.getM_predictors().get(2),sp.getPredictorNodes()[1]);
		assertEquals(0,sp.splitter.getIndex());
		assertEquals(2,sp.splitter.getDegree());
		assertEquals(1,((EqualitySplitter) sp.splitter).getValue());
		sp = m_s.get(1); 
		assertEquals("R.1",sp.getID());
		assertEquals(iat.getM_predictors().get(0),sp.getParent());
		assertEquals(iat.getM_predictors().get(3),sp.getPredictorNodes()[0]);
		assertEquals(iat.getM_predictors().get(4),sp.getPredictorNodes()[1]);
		assertEquals(0,sp.splitter.getIndex());
		assertEquals(2,sp.splitter.getDegree());
		assertEquals(2,((EqualitySplitter) sp.splitter).getValue());
		sp = m_s.get(2); 
		assertEquals("R.1:1.0",sp.getID());
		assertEquals(iat.getM_predictors().get(4),sp.getParent());
		assertEquals(iat.getM_predictors().get(5),sp.getPredictorNodes()[0]);
		assertEquals(iat.getM_predictors().get(6),sp.getPredictorNodes()[1]);
		assertEquals(0,sp.splitter.getIndex());
		assertEquals(2,sp.splitter.getDegree());
		assertEquals(1,((EqualitySplitter) sp.splitter).getValue());
	}
	
	/**
	 * This test does not work because addSplitterNode(sNode) does not add predictor node with it.
	 * It is not clear whether how this method is being used.
	 */
	/*
	public void testAddSplitterNode() {
		InstrumentedAlternatingTree iat = new InstrumentedAlternatingTree(m_builders, m_booster, m_exampleIndices, new Configuration());
		//System.out.println(iat);
		PredictorNode root = iat.getM_predictors().get(0);
		Vector<SplitterNode> children = new Vector<SplitterNode>();
		Splitter sp = new EqualitySplitter(0, 1, 2, exDescription.getAttributeDescription(0));
		
		SplitterNode sNode = new SplitterNode(sp, "R.0", 1, null, root);
		PredictorNode pNode0 = new PredictorNode(new BinaryPrediction(-4.627311942575493), "R.0:0", 1, children, sNode, 0);
		PredictorNode pNode1 = new PredictorNode(new BinaryPrediction(0.2787563203139271), "R.0:1", 1, children, sNode, 1);
		PredictorNode[] pNodes = new PredictorNode[2];
		pNodes[0] = pNode0;
		pNodes[1] = pNode1;
		sNode.setPredictorNodes(pNodes);
		iat.addSplitterNode(sNode);
		//System.out.println(iat);
	}
	*/
	
	/**
	 * Generates a list of PredictorNode recursively. 
	 */
	private void genPredictorNodeList(PredictorNode p,ArrayList<PredictorNode> list) {
		if (p != null) {
			list.add(p);
			Vector<SplitterNode> sNodes = p.getSplitterNodes();
			for (Iterator<SplitterNode> it = sNodes.iterator(); it.hasNext();) {
				SplitterNode sNode = it.next();
				PredictorNode[] pNodes = sNode.getPredictorNodes();
				for (int i=0; i < pNodes.length; i++) {
					genPredictorNodeList(pNodes[i], list);
				}
			}
		}
	}
	
	/**
	 * Generates a list of SplitterNode recursively. 
	 */
	private void genSplitterNodeList(PredictorNode p,ArrayList<SplitterNode> list) {
		if (p != null) {
			Vector<SplitterNode> sNodes = p.getSplitterNodes();
			for (Iterator<SplitterNode> it = sNodes.iterator(); it.hasNext();) {
				SplitterNode sNode = it.next();
				if (sNode != null) {
					list.add(sNode);
					PredictorNode[] pNodes = sNode.getPredictorNodes();
					for (int i=0; i < pNodes.length; i++) {
						genSplitterNodeList(pNodes[i], list);
					}
				}
				
			}
		}
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
	 * Compares InstrumentedAlternatingTree and AlternatingTree by
	 * checking these conditions.<ol>
	 * <li> Is number of predictor node equal? </li>
	 * <li> Is every predictor node in iat in tree? </li>
	 * <li> Is number of splitter node equal? </li>
	 * <li> Is every splitter node in iat in tree?</li>
	 * </ol>
	 * @param iat ADTree of class InstrumentedAlternatingTree
	 * @param tree ADTree of class AlternatingTree
	 */
	private void testEqualByRef(InstrumentedAlternatingTree iat,AlternatingTree tree) {
		ArrayList<PredictorNode> pNodes = new ArrayList<PredictorNode>();
		genPredictorNodeList(tree.getRoot(), pNodes);
		assertEquals(iat.getM_predictors().size(),pNodes.size());
		for (int i=0; i < iat.getM_predictors().size(); i++) {
			assertTrue(pNodes.contains(iat.getM_predictors().get(i)));
		}
		ArrayList<SplitterNode> sNodes = new ArrayList<SplitterNode>();
		genSplitterNodeList(tree.getRoot(), sNodes);
		assertEquals(iat.getM_splitters().size(),sNodes.size());
		for (int i=0; i < iat.getM_splitters().size(); i++) {
			assertTrue(sNodes.contains(iat.getM_splitters().get(i)));
		}
	}
}
