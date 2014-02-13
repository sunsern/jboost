package jboost.visualization;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.MarkerChangeListener;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYIntervalSeries;
import org.jfree.data.xy.XYIntervalSeriesCollection;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.Layer;

/**
 * This code was edited or generated using CloudGarden's Jigloo
 * SWT/Swing GUI Builder, which is free for non-commercial
 * use. If Jigloo is being used commercially (ie, by a corporation,
 * company or business for any purpose whatever) then you
 * should purchase a license for each developer using Jigloo.
 * Please visit www.cloudgarden.com for details.
 * Use of Jigloo implies acceptance of these licensing terms.
 * A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
 * THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
 * LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
 */
/**
 * @author yoavfreund (Rewritten by Sunsern Cheamanunkul) A class based on swing
 *         and jFreeChart that implements a frame for visualizing an ROC and a
 *         histogram for a two-class distribution or multi-class. Used to
 *         visualize the scores distribution generated by boosting.
 */
public class HistogramFrame extends javax.swing.JFrame {

  private static final long serialVersionUID = 2L;

  private JSplitPane jSplitPane1;
  private JSplitPane jSplitPane2;

  private JPanel jPanel1;
  private JPanel jPanel2;
  private JPanel jPanel3;

  private JSlider jSlider1;
  private JSlider jSlider2;

  private JMenu jMenu1;
  private JMenu jMenu2;
  private JMenuBar jMenuBar1;
  private JMenuItem jMenuItem6;
  private JMenuItem jMenuItem5;
  private JMenuItem jMenuItem3;
  private JMenuItem jMenuItem2;
  private JMenuItem jMenuItem1;

  private JScrollPane jScrollPane1;
  private JList jList1;

  private final int posLabel = +1;
  private final int negLabel = -1;

  private final int noOfBins = 80; // number of bins in the histogram

  private XYIntervalSeriesCollection histogramDataset;
  private XYIntervalSeriesCollection fluctDataset;
  private XYSeriesCollection weightDataset;
  private XYSeriesCollection potentialDataset;

  private JFreeChart histogramChart;
  private ChartPanel histogramPanel;
  private XYSeriesCollection rocDataset;
  private JFreeChart rocChart;
  private ChartPanel rocPanel;

  private int iter; // number of current iteration

  private double upper_limit, lower_limit;
  private double upperMarkerScore, lowerMarkerScore;
  private IntervalMarker histMarker;

  private ValueMarker lower_tprMarker, lower_fprMarker; // markers for ROC graph
  private ValueMarker upper_tprMarker, upper_fprMarker; // markers for ROC graph

  private DataSet rawData; // current dataset that is showing

  private boolean showPotential = false;
  private boolean showWeight = false;

  private static InfoParser infoParser;

  public static void main(String[] args) {

    boolean carryOver = false;

    if (args.length == 0) {
      System.out.println("Please call this from the python wrapper instead");
      System.exit(-1);
    }

    // check carryOver flag
    if (Integer.parseInt(args[0]) != 0) {
      carryOver = true;
    }
    int offset = 1;

    // get test files
    int numTestFiles = Integer.parseInt(args[offset++]);
    String[] testFiles = new String[numTestFiles];
    if (numTestFiles == 0) {
      System.out.println("Error: Cannot find *.test.boosting.info");
      System.exit(-1);
    }
    for (int i = 0; i < numTestFiles; i++)
      testFiles[i] = args[offset + i];

    // get train files
    offset = offset + numTestFiles;
    int numTrainFiles = Integer.parseInt(args[offset++]);
    String[] trainFiles = new String[numTrainFiles];
    for (int i = 0; i < numTrainFiles; i++)
      trainFiles[i] = args[offset + i];

    // get info files
    offset = offset + numTrainFiles;
    int numInfoFiles = Integer.parseInt(args[offset++]);
    String[] infoFiles = new String[numInfoFiles];

    for (int i = 0; i < numInfoFiles; i++)
      infoFiles[i] = args[offset + i];

    // create the info parser
    infoParser = new InfoParser(testFiles, trainFiles, infoFiles, carryOver);

    SwingUtilities.invokeLater(new Runnable() {

      public void run() {
        try {
          HistogramFrame inst = new HistogramFrame();
          inst.setLocationRelativeTo(null);
          inst.setVisible(true);
        }
        catch (IOException e) {
          e.printStackTrace();
        }
        catch (RuntimeException e) {
          e.printStackTrace();
          System.exit(-1);
        }
      }
    });
  }

  public HistogramFrame() throws IOException {

    super();

    infoParser.getHasIndexAndNumClasses();
    infoParser.getMaxNumIndices();
    infoParser.getMaxNumIter();
    infoParser.getBoosterInfo();
    rawData = infoParser.createDataSet();
    lower_limit = rawData.getMin(0);
    upper_limit = rawData.getMax(0);
    initGUI();

  }

  private void initGUI() {

    post("Initializing GUI...");

    try {
      setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
      {
        jMenuBar1 = new JMenuBar();
        setJMenuBar(jMenuBar1);
        jMenuBar1.add(getJMenu1());
        jMenuBar1.add(getJMenu2());
      }
      {
        jSplitPane2 = new JSplitPane();
        jSplitPane2.setBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED));
        jSplitPane2.setDividerLocation(850);
        jSplitPane2.setFocusCycleRoot(true);
        jSplitPane2.add(getJSplitPane1(), JSplitPane.LEFT);
        jSplitPane2.add(getJPanel3(), JSplitPane.RIGHT);
      }

      this.add(jSplitPane2);
      pack();
      this.setSize(1000, 500);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  private JSplitPane getJSplitPane1() {
    if (jSplitPane1 == null) {
      jSplitPane1 = new JSplitPane();
      jSplitPane1.setPreferredSize(new Dimension(546, 400));
      jSplitPane1.setDividerLocation(400);
      jSplitPane1.setDoubleBuffered(true);
      jSplitPane1.setLastDividerLocation(100);
      {
        jPanel1 = new JPanel();
        jSplitPane1.add(jPanel1, JSplitPane.RIGHT);
        jPanel1.setPreferredSize(new Dimension(10, 406));

        BoxLayout jPanel1Layout = new BoxLayout(jPanel1, javax.swing.BoxLayout.Y_AXIS);
        jPanel1.setLayout(jPanel1Layout);
        {
          histogramDataset = new XYIntervalSeriesCollection();
          fluctDataset = new XYIntervalSeriesCollection();
          weightDataset = new XYSeriesCollection();
          potentialDataset = new XYSeriesCollection();

          updateHistogramDatasets();
          histogramChart = createHistogramChart();

          histogramPanel = new ChartPanel(histogramChart);
          jPanel1.add(histogramPanel);
          histogramPanel.setPopupMenu(null);
          histogramPanel.setPreferredSize(new Dimension(433, 374));
        }
      }
      {
        jPanel2 = new JPanel();
        jSplitPane1.add(jPanel2, JSplitPane.LEFT);
        jPanel2.setPreferredSize(new Dimension(10, 393));

        BoxLayout jPanel2Layout = new BoxLayout(jPanel2, javax.swing.BoxLayout.Y_AXIS);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2.setOpaque(false);

        rocDataset = new XYSeriesCollection();
        XYSeries rocSeries = rawData.generateRoC(negLabel, posLabel);
        rocDataset.addSeries(rocSeries);
        rocChart = createRocChart(rocDataset);
        rocPanel = new ChartPanel(rocChart);
        jPanel2.add(rocPanel);
        rocPanel.setPopupMenu(null);
        rocPanel.setPreferredSize(new Dimension(10, 406));
      }
      {
        jSlider1 = new JSlider();
        jPanel1.add(jSlider1);
        jSlider1.setLayout(null);
        jSlider1.setPreferredSize(new Dimension(10, 16));
        jSlider1.addChangeListener(new ChangeListener() {

          public void stateChanged(ChangeEvent evt) {
            updateUpperMarker();
          }
        });
      }
      {
        jSlider2 = new JSlider();
        jPanel1.add(jSlider2);
        jSlider2.setLayout(null);
        jSlider2.setPreferredSize(new Dimension(10, 16));
        jSlider2.addChangeListener(new ChangeListener() {

          public void stateChanged(ChangeEvent evt) {
            updateLowerMarker();
          }
        });

      }
    }
    return jSplitPane1;
  }

  private void updateUpperMarker() {
    int pos = (int) jSlider1.getValue();
    upperMarkerScore = lower_limit + pos * (upper_limit - lower_limit) / 100.0;
    histMarker.setEndValue(upperMarkerScore);
    if (!jSlider1.getValueIsAdjusting()) {
      double[] FPTP = rawData.getFPTP(upperMarkerScore);
      upper_fprMarker.setValue(FPTP[0]);
      upper_tprMarker.setValue(FPTP[1]);
    }
  }

  private void updateLowerMarker() {
    int pos = (int) jSlider2.getValue();
    lowerMarkerScore = lower_limit + pos * (upper_limit - lower_limit) / 100.0;
    histMarker.setStartValue(lowerMarkerScore);
    if (!jSlider2.getValueIsAdjusting()) {
      double[] FPTP = rawData.getFPTP(lowerMarkerScore);
      lower_fprMarker.setValue(FPTP[0]);
      lower_tprMarker.setValue(FPTP[1]);
    }
  }

  /**
   * @param listener
   * @see org.jfree.chart.plot.Marker#addChangeListener(org.jfree.chart.event.MarkerChangeListener)
   */
  public void addChangeListener(MarkerChangeListener listener) {
    histMarker.addChangeListener(listener);
  }

  private void updateHistogramDatasets() {

    if (histogramDataset.getSeriesCount() > 0) {
      histogramDataset.removeAllSeries();
      fluctDataset.removeAllSeries();
      weightDataset.removeAllSeries();
      potentialDataset.removeAllSeries();
    }

    XYIntervalSeries posSeries = new XYIntervalSeries("positive");
    XYIntervalSeries negSeries = new XYIntervalSeries("negative");

    double[] posHist = rawData.computeHistogram(1, noOfBins);
    double[] negHist = rawData.computeHistogram(-1, noOfBins);

    double min = rawData.getMin(iter);
    double max = rawData.getMax(iter);

    double x = min;
    double step = (max - min) / noOfBins;
    double height = 1.0;
    for (int i = 0; i < noOfBins; i++) {
      posSeries.add(x, x, x + (step / 2), posHist[i], 0, posHist[i]);
      negSeries.add(x + (step / 2), x + (step / 2), x + step, negHist[i], 0, negHist[i]);
      x = x + step;
      if (posHist[i] > height) height = posHist[i];
      if (negHist[i] > height) height = negHist[i];
    }

    histogramDataset.addSeries(posSeries);
    histogramDataset.addSeries(negSeries);
    fluctDataset.addSeries(rawData.getFluctBins());
    fluctDataset.addSeries(rawData.getFluctRanges());

    if (histogramChart != null) {
      ValueAxis axis = new NumberAxis();
      axis.setRange(rawData.getMinRange(iter), rawData.getMaxRange(iter));
      histogramChart.getXYPlot().setDomainAxis(axis);
    }

    if (infoParser.isRobustBoost) {
      double t = infoParser.averageTime[iter];
      double rho = infoParser.rho;
      boolean confRated = infoParser.confRated;
      double sigma_f = infoParser.sigma_f;
      double epsilon = infoParser.epsilon;
      double theta = infoParser.theta;

      rho = RobustBoostHelper.calculateRho(sigma_f, epsilon, theta);

      if (showWeight) {
        weightDataset.addSeries(RobustBoostHelper.getPosWeightPlot(sigma_f, epsilon, theta, rho, t, height, min, max, step / 2));
        weightDataset.addSeries(RobustBoostHelper.getNegWeightPlot(sigma_f, epsilon, theta, rho, t, height, min, max, step / 2));
      }

      if (showPotential) {
        potentialDataset.addSeries(RobustBoostHelper.getPosPotentialPlot(sigma_f, epsilon, theta, rho, t, height, min, max, step / 2));
        potentialDataset.addSeries(RobustBoostHelper.getNegPotentialPlot(sigma_f, epsilon, theta, rho, t, height, min, max, step / 2));
      }
    }
    else if (infoParser.isAdaBoost) {
      if (showWeight) {
        weightDataset.addSeries(AdaBoostHelper.getPosWeightPlot(height, min, max, step / 2));
        weightDataset.addSeries(AdaBoostHelper.getNegWeightPlot(height, min, max, step / 2));
      }

      if (showPotential) {
        potentialDataset.addSeries(AdaBoostHelper.getPosPotentialPlot(height, min, max, step / 2));
        potentialDataset.addSeries(AdaBoostHelper.getNegPotentialPlot(height, min, max, step / 2));
      }
    }
    else if (infoParser.isLogLossBoost) {
      if (showWeight) {
        weightDataset.addSeries(LogLossBoostHelper.getPosWeightPlot(height, min, max, step / 2));
        weightDataset.addSeries(LogLossBoostHelper.getNegWeightPlot(height, min, max, step / 2));
      }

      if (showPotential) {
        potentialDataset.addSeries(LogLossBoostHelper.getPosPotentialPlot(height, min, max, step / 2));
        potentialDataset.addSeries(LogLossBoostHelper.getNegPotentialPlot(height, min, max, step / 2));
      }
    }
  }

  private JFreeChart createHistogramChart() {

    XYBarRenderer renderer1 = new XYBarRenderer();
    renderer1.setSeriesPaint(0, Color.cyan);
    renderer1.setSeriesPaint(1, Color.pink);

    XYPlot histPlot = new XYPlot(histogramDataset, null, new NumberAxis("count"), renderer1);

    XYBarRenderer renderer2 = new XYBarRenderer();
    renderer2.setSeriesPaint(0, Color.green);
    renderer2.setSeriesPaint(1, Color.orange);
    renderer2.setUseYInterval(true);

    // weight and potential
    if (infoParser.isRobustBoost || infoParser.isAdaBoost || infoParser.isLogLossBoost) {
      StandardXYItemRenderer renderer3 = new StandardXYItemRenderer();
      renderer3.setSeriesPaint(0, Color.blue);
      renderer3.setSeriesPaint(1, Color.red);
      renderer3.setBaseStroke(new BasicStroke(2));

      StandardXYItemRenderer renderer4 = new StandardXYItemRenderer();
      renderer4.setSeriesPaint(0, Color.blue);
      renderer4.setSeriesPaint(1, Color.red);
      renderer4.setBaseStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 2, new float[] { 2 }, 0));

      histPlot.setDataset(1, weightDataset);
      histPlot.setRenderer(1, renderer3);

      histPlot.setDataset(2, potentialDataset);
      histPlot.setRenderer(2, renderer4);

      histPlot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
    }

    XYPlot fluctPlot = new XYPlot(fluctDataset, null, new NumberAxis("bin"), renderer2);

    double initialLocation = (upper_limit + lower_limit) / 2.0;
    histMarker = new IntervalMarker(initialLocation, initialLocation);
    histPlot.addDomainMarker(histMarker, Layer.BACKGROUND);
    fluctPlot.addDomainMarker(histMarker, Layer.BACKGROUND);

    // plot.setBackgroundPaint(Color.lightGray);
    // plot.setDomainGridlinePaint(Color.white);
    // plot.setRangeGridlinePaint(Color.white);

    CombinedDomainXYPlot combinedPlot = new CombinedDomainXYPlot(new NumberAxis("score"));
    combinedPlot.setGap(10.0);

    // add the subplots...
    ValueAxis axis = new NumberAxis();
    axis.setRange(rawData.getMinRange(iter), rawData.getMaxRange(iter));
    combinedPlot.add(histPlot, 3);
    combinedPlot.add(fluctPlot, 1);
    combinedPlot.setOrientation(PlotOrientation.VERTICAL);
    combinedPlot.setDomainAxis(axis);

    JFreeChart chart = new JFreeChart("Histogram", JFreeChart.DEFAULT_TITLE_FONT, combinedPlot, false // legend
        );

    return chart;
  }

  private JFreeChart createRocChart(XYDataset dataset) {
    JFreeChart chart = ChartFactory.createXYLineChart("ROC", // chart title
                                                      "False positive rate", // x axis label
                                                      "True positive rate", // y axis label
                                                      dataset, // data
                                                      PlotOrientation.VERTICAL, false, // include
                                                      // legend
                                                      true, // tooltips
                                                      false // urls
                                   );

    chart.setAntiAlias(false);
    RenderingHints hints = new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
    chart.setRenderingHints(hints);

    XYPlot plot = (XYPlot) chart.getPlot();
    plot.setBackgroundPaint(Color.lightGray);
    plot.setDomainGridlinePaint(Color.white);
    plot.setRangeGridlinePaint(Color.white);

    lower_tprMarker = new ValueMarker(0.5);
    lower_tprMarker.setPaint(Color.blue);
    lower_fprMarker = new ValueMarker(0.5);
    lower_fprMarker.setPaint(Color.blue);
    plot.addRangeMarker(lower_tprMarker);
    plot.addDomainMarker(lower_fprMarker);

    upper_tprMarker = new ValueMarker(0.5);
    upper_tprMarker.setPaint(Color.red);
    upper_fprMarker = new ValueMarker(0.5);
    upper_fprMarker.setPaint(Color.red);
    plot.addRangeMarker(upper_tprMarker);
    plot.addDomainMarker(upper_fprMarker);

    return chart;

  }

  private JPanel getJPanel3() {
    if (jPanel3 == null) {
      jPanel3 = new JPanel();
      BoxLayout jPanel3Layout = new BoxLayout(jPanel3, BoxLayout.Y_AXIS);
      jPanel3.setLayout(jPanel3Layout);
      {
        jPanel3.add(getJScrollPane1());
        jPanel3.setPreferredSize(new java.awt.Dimension(80, 400));
      }
    }
    return jPanel3;
  }

  private JScrollPane getJScrollPane1() {
    if (jScrollPane1 == null) {
      jScrollPane1 = new JScrollPane();
      jList1 = new JList(infoParser.iterNoList);
      jList1.setLayout(new FlowLayout());
      jList1.setFocusable(false);
      jList1.setIgnoreRepaint(false);
      jList1.addListSelectionListener(new ListSelectionListener() {

        public void valueChanged(ListSelectionEvent evt) {
          if (!evt.getValueIsAdjusting()) {
            JList list = (JList) evt.getSource();
            iter = list.getSelectedIndex();
            loadIteration(iter);
          }
        }
      });
      jScrollPane1.getViewport().add(jList1);
    }
    return jScrollPane1;
  }

  private JMenu getJMenu1() {
    if (jMenu1 == null) {
      jMenu1 = new JMenu();
      jMenu1.setText("Tools");
      jMenu1.add(getJMenuItem1());
      jMenu1.add(getJMenuItem2());
    }
    return jMenu1;
  }

  private JMenu getJMenu2() {
    if (jMenu2 == null) {
      jMenu2 = new JMenu();
      jMenu2.setText("View");
      jMenu2.add(getJMenuItem3());
      jMenu2.add(getJMenuItem5());
      jMenu2.add(getJMenuItem6());
    }
    return jMenu2;
  }

  private JMenuItem getJMenuItem1() {
    if (jMenuItem1 == null) {
      jMenuItem1 = new JMenuItem();
      jMenuItem1.setText("Print Indices");
      if (infoParser.useCV && !infoParser.hasIndex) jMenuItem1.setEnabled(false);
      jMenuItem1.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent evt) {
          // System.out.println("jMenuItem1.actionPerformed, event="+evt);
          dumpExamples();
        }
      });
    }
    return jMenuItem1;
  }

  private JMenuItem getJMenuItem2() {
    if (jMenuItem2 == null) {
      jMenuItem2 = new JMenuItem();
      jMenuItem2.setText("Print PDF");
      jMenuItem2.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent evt) {
          // System.out.println("jMenuItem2.actionPerformed, event="+evt);
          toPDF();
        }
      });
    }
    return jMenuItem2;
  }

  private JMenuItem getJMenuItem3() {
    if (jMenuItem3 == null) {
      jMenuItem3 = new JMenuItem();
      jMenuItem3.setText("Switch Label");
      if (infoParser.numClasses != 2) jMenuItem3.setEnabled(false);
      jMenuItem3.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent evt) {
          // System.out.println("jMenuItem3.actionPerformed, event="+evt);
          infoParser.switchLabel *= -1;
          try {
            rawData = infoParser.createDataSet();
          }
          catch (IOException e) {
            e.printStackTrace();
          }
        }
      });
    }
    return jMenuItem3;
  }

  private JMenuItem getJMenuItem5() {
    if (jMenuItem5 == null) {
      jMenuItem5 = new JMenuItem();
      if (showWeight) jMenuItem5.setText("Hide Weight");
      else jMenuItem5.setText("Show Weight");
      jMenuItem5.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent evt) {

          showWeight = !showWeight;

          if (showWeight) jMenuItem5.setText("Hide Weight");
          else jMenuItem5.setText("Show Weight");

          loadIteration(iter);
        }
      });
    }
    return jMenuItem5;
  }

  private JMenuItem getJMenuItem6() {
    if (jMenuItem6 == null) {
      jMenuItem6 = new JMenuItem();
      if (showPotential) jMenuItem6.setText("Hide Potential");
      else jMenuItem6.setText("Show Potential");
      jMenuItem6.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent evt) {

          showPotential = !showPotential;

          if (showPotential) jMenuItem6.setText("Hide Potential");
          else jMenuItem6.setText("Show Potential");

          loadIteration(iter);
        }
      });
    }
    return jMenuItem6;
  }

  private void toPDF() {

    File pdf = selectPDFFile();

    if (pdf != null) {

      JComponent toDraw = this.jSplitPane2;

      File[] tmpFiles = new File[infoParser.maxNumIter];

      for (int i = 0; i < infoParser.maxNumIter; i++) {

        post("Printing " + infoParser.iterNoList[i] + "...");

        jList1.setSelectedIndex(i);
        jList1.scrollRectToVisible(jList1.getCellBounds(i, i));
        loadIteration(i);

        BufferedImage bimg = new BufferedImage(toDraw.getWidth(), toDraw.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bimg.createGraphics();
        toDraw.paint(g);
        g.dispose();

        // add leading zeros
        String file_idx = Integer.toString(i);
        while (file_idx.length() < 3)
          file_idx = "0" + file_idx;

        try {
          tmpFiles[i] = new File("scorevistmp" + file_idx + ".png");
          javax.imageio.ImageIO.write(bimg, "png", tmpFiles[i]);
        }
        catch (Exception e) {
          e.printStackTrace();
        }

      }

      // restore plots
      jList1.setSelectedIndex(iter);
      loadIteration(iter);

      boolean success = true;

      // call convert to make pdf
      try {
        Process p = Runtime.getRuntime().exec("convert scorevistmp*.png " + pdf.getAbsolutePath());
        p.waitFor();
      }
      catch (Exception e) {
        success = false;
      }

      if (!success) {
        System.out.println("'convert' is missing. You need to have ImageMagick installed.");
        System.out.println("PDF is not generated but you can find PNG files in the current directory.");
      }
      else {
        /*
         * for (int i=0;i<maxNumIter-1;i++) { tmpFiles[i].delete(); }
         */

        if (infoParser.maxNumIter > 0) {
          tmpFiles[infoParser.maxNumIter - 1].renameTo(new File(pdf.getAbsolutePath() + ".png"));
        }

        JOptionPane.showMessageDialog(this, "PDF file is generated!", "Done", JOptionPane.INFORMATION_MESSAGE);
      }
    }
  }

  private void dumpExamples() {
    File txt = selectDumpFile();
    if (txt != null) {
      rawData.setOutputFilename(txt.getAbsolutePath());
      rawData.printScores(infoParser.iterNoList[iter], lowerMarkerScore, upperMarkerScore);
      JOptionPane.showMessageDialog(this, "Operation completed successfully!", "Done", JOptionPane.INFORMATION_MESSAGE);
    }
  }

  private File selectPDFFile() {

    File fFile = new File("default.pdf");
    JFileChooser fc = new JFileChooser();

    // Start in current directory
    fc.setCurrentDirectory(new File("."));

    // Set filter for Java source files.
    fc.setFileFilter(new FileFilter() {

      public boolean accept(File f) {
        String path = f.getAbsolutePath();
        if (f.isDirectory() || path.endsWith(".pdf")) return true;
        else return false;
      }

      public String getDescription() {
        return "PDF Files";
      }
    });

    // Set to a default name for save.
    fc.setSelectedFile(fFile);

    // Open chooser dialog
    int result = fc.showSaveDialog(this);

    if (result == JFileChooser.CANCEL_OPTION) {
      return null;
    }
    else if (result == JFileChooser.APPROVE_OPTION) {
      fFile = fc.getSelectedFile();
      if (fFile.exists()) {
        int response =
            JOptionPane.showConfirmDialog(null, "Overwrite existing file?", "Confirm Overwrite", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (response == JOptionPane.CANCEL_OPTION) return null;
      }
      return fFile;
    }
    else {
      return null;
    }
  }

  private File selectDumpFile() {

    File fFile = new File("ExamplesDumpFile.txt");
    JFileChooser fc = new JFileChooser();

    // Start in current directory
    fc.setCurrentDirectory(new File("."));

    // Set filter for Java source files.
    fc.setFileFilter(new FileFilter() {

      public boolean accept(File f) {
        String path = f.getAbsolutePath();
        if (f.isDirectory() || path.endsWith(".txt")) return true;
        else return false;
      }

      public String getDescription() {
        return "Text Files";
      }
    });

    // Set to a default name for save.
    fc.setSelectedFile(fFile);

    // Open chooser dialog
    int result = fc.showSaveDialog(this);

    if (result == JFileChooser.CANCEL_OPTION) {
      return null;
    }
    else if (result == JFileChooser.APPROVE_OPTION) {
      fFile = fc.getSelectedFile();
      if (fFile.exists()) {
        int response =
            JOptionPane.showConfirmDialog(null, "Overwrite existing file?", "Confirm Overwrite", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (response == JOptionPane.CANCEL_OPTION) return null;
      }
      return fFile;
    }
    else {
      return null;
    }
  }

  private void loadIteration(int iter) {
    rawData.setIteration(iter);

    post("updating histogram...");
    updateHistogramDatasets();

    post("updating RoC...");
    XYSeries rocSeries = rawData.generateRoC(negLabel, posLabel);
    rocDataset.removeSeries(0);
    rocDataset.addSeries(rocSeries);

    post("updating marker...");
    updateUpperMarker();
    updateLowerMarker();

    post("done");
  }

  private void post(String s) {
    System.out.println(s);
  }

}
