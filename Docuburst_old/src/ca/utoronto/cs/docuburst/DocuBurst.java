/* CVS: $Id: DocuBurst.java,v 1.6 2008/12/06 00:14:22 cmcollin Exp $ */

package ca.utoronto.cs.docuburst;

import static ca.utoronto.cs.wordnetexplorer.utilities.Constants.HIGH_QUALITY;
import static ca.utoronto.cs.wordnetexplorer.utilities.Constants.S2W;
import static ca.utoronto.cs.wordnetexplorer.utilities.Constants.WORD;
import static ca.utoronto.cs.wordnetexplorer.utilities.Constants.dictionary;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Paint;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.PointerType;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Word;
import net.didion.jwnl.dictionary.Dictionary;
import prefuse.Display;
import prefuse.DisplayComponent;
import prefuse.Visualization;
import prefuse.action.filter.FisheyeTreeFilter;
import prefuse.activity.Activity;
import prefuse.activity.ActivityListener;
import prefuse.data.Graph;
import prefuse.data.Table;
import prefuse.data.Tree;
import prefuse.data.expression.Predicate;
import prefuse.data.expression.parser.ExpressionParser;
import prefuse.data.query.SearchQueryBinding;
import prefuse.data.search.SearchTupleSet;
import prefuse.util.FontLib;
import prefuse.util.display.ExportDisplayAction;
import prefuse.util.display.PaintListener;
import prefuse.util.io.IOLib;
import prefuse.util.ui.JSearchPanel;
import prefuse.util.ui.UILib;
import prefuse.visual.VisualGraph;
import ca.utoronto.cs.docuburst.data.WordNetTree;
import ca.utoronto.cs.docuburst.data.treecut.TreeCutCache;
import ca.utoronto.cs.docuburst.prefuse.CachedTreeCutFilter;
import ca.utoronto.cs.docuburst.prefuse.DocuBurstActionList;
import ca.utoronto.cs.docuburst.swing.ConcordancePanel;
import ca.utoronto.cs.docuburst.swing.TilesPanel;
import ca.utoronto.cs.docuburst.swing.widget.ScentedSlider;
import ca.utoronto.cs.docuburst.swing.widget.ScentedSliderModel;
import ca.utoronto.cs.docuburst.swing.widget.ScentedSliderModel.Point;
import ca.utoronto.cs.prefuseextensions.layout.StarburstLayout;
import ca.utoronto.cs.prefuseextensions.render.SectorRenderer;
import ca.utoronto.cs.prefuseextensions.swing.Utilities;
import ca.utoronto.cs.prefuseextensions.swing.ValueChangedEvent;
import ca.utoronto.cs.prefuseextensions.swing.ValueListener;
import ca.utoronto.cs.wordnetexplorer.jwnl.LoadData;
import ca.utoronto.cs.wordnetexplorer.prefuse.FisheyeDocument;
import ca.utoronto.cs.wordnetexplorer.prefuse.controls.LoadDataControl;
import ca.utoronto.cs.wordnetexplorer.swing.SensePane;
import ca.utoronto.cs.wordnetexplorer.swing.TwoComponentSlidingPanel;
import ca.utoronto.cs.wordnetexplorer.swing.WordNetSearchPanel;
import ca.utoronto.cs.wordnetexplorer.utilities.LanguageLib;

/**
 * Runnable main class, initiates the GUI for the Radial WordNet Visualization.
 * Extend JPanel instead of JFrame to allow it to be plugged into other
 * applications.
 * 
 * @version $Revision: 1.6 $
 * @author Christopher Collins
 */
@SuppressWarnings("serial")
public class DocuBurst extends JPanel implements LoadData {

    /**
     * The <code>APP_NAME</code> is the title of this JFrame.
     */
    public static final String APP_NAME = "DocuBurst";

    /**
     * The pane in which sense information is listed on filter
     */
    public static SensePane filterPane = new SensePane();

    // -- Swing --

    /**
     * If we are running on the table, display in large window.
     */
    public static boolean isTableDisplay = false;

    // Predicates
    public static final Predicate NOT_LEAF_PREDICATE = (Predicate) ExpressionParser.parse("childcount() > 0");
    
    public static final Predicate NOT_S2W_PREDICATE = (Predicate) ExpressionParser.parse("type != " + S2W);

    public static final Predicate OMIT_WORDS_PREDICATE = (Predicate) ExpressionParser.parse("(type != " + WORD + ") and (type != " + S2W + ")");

    /**
     * The pane in which sense information is listed on search
     */
    public static SensePane sensePane = new SensePane();

    /**
     * Filter for words and sense-to-word edges.
     */
    public static final Predicate WORDS_PREDICATE = (Predicate) ExpressionParser.parse("(type = " + WORD + ") or (type = " + S2W + ")");

    /**
     * If the application is run as an applet, some features will be disabled
     * (such as saving images to the local file system).
     */
    private static boolean isApplet = true;

    private static final Logger LOGGER = Logger.getLogger(DocuBurst.class.getName());

    private static String documentFile;

    private TwoComponentSlidingPanel contentPane;

    /**
     * ActionListener to keep the visual filtering up to date with the status of
     * the related swing controls.
     */
    private ActionListener depthFilterAction = new ActionListener() {
        public void actionPerformed(ActionEvent event) {
            String actionCommand = event.getActionCommand();

            if (actionCommand.equals("Search and Focus")) {
                if (!docuburstLayout.depthFilterScope.equals(Param.DepthFilterScope.SEARCH_AND_FOCUS)) {
                	docuburstLayout.depthFilterScope = Param.DepthFilterScope.SEARCH_AND_FOCUS;
                    // update filter sources to search and focus
                    docuburstLayout.getFisheyeTreeFilter().setSources(Param.DepthFilterScope.SEARCH_AND_FOCUS);
                    if (docuburstVisualization.getGroup("graph").getTupleCount() > 0) {
                        docuburstVisualization.run("layout");
                        docuburstVisualization.run("resize");
                    }
                }
            }
            if (actionCommand.equals("Focus")) {
                if (!docuburstLayout.depthFilterScope.equals(Param.DepthFilterScope.FOCUS)) {
                	docuburstLayout.depthFilterScope = Param.DepthFilterScope.FOCUS;
                    // update filter sources to only focus
                    docuburstLayout.getFisheyeTreeFilter().setSources(Param.DepthFilterScope.FOCUS);
                    if (docuburstVisualization.getGroup("graph").getTupleCount() > 0) {
                        docuburstVisualization.run("layout");
                        docuburstVisualization.run("resize");
                    }
                }
            }
        }
    };
    
    private ActionListener unevenAction = new ActionListener() {
        
        @Override
        public void actionPerformed(ActionEvent e) {
            if (((JCheckBox)e.getSource()).isSelected())
                docuburstLayout.setDepthFilterApproach(Param.DepthFilter.TREECUT);
            else
                docuburstLayout.setDepthFilterApproach(Param.DepthFilter.FISHEYE);
        }
    };

    /**
     * The tree depth filter spinner
     */
    private JSpinner depthSpinner;

    // -- prefuse --

    private JPanel displayPanel;

    /**
     * The display pane for DocuBurst drawing
     */
    private DisplayComponent docuburstDisplay;

    /**
     * The docuburst layout sequence.
     */
    private DocuBurstActionList docuburstLayout;
    /**
     * The docuburst visualization
     */
    private Visualization docuburstVisualization;

    /**
     * The search panel for filtering a visible graph.
     */
    private JPanel filterPanel;

    /**
     * The full text fish eye coordinated view
     */
    private FisheyeDocument fishEyeDocument;

    /**
     * The backing storage of the graph.
     */
    private Graph graph;

    /**
     * The interface controls panel.
     */
    private JTabbedPane interfacePane;

    // Option variables
    private boolean mergeWords;

    private ScentedSlider scentedSlider;
    
    /**
     * Action which resets the graph and updates the sense panel when the search
     * button is clicked.
     */
    private ActionListener searchAction = new ActionListener() {
        public void actionPerformed(ActionEvent event) {
            if (searchPanel.isSearchEnabled()) {
                if ((searchPanel.getSelectedPOS() == null) || (searchPanel.getSelectedSense() == 0)) {
                    // select filter pane
                    interfacePane.setSelectedIndex(1);
                    // stop layout while making changes to underlying graph
                    // structure
                    cancelLayouts();
                    reset(searchPanel.getIndexWord());
                } else {
                    // select filter pane
                    interfacePane.setSelectedIndex(1);
                    // stop layout while making changes to underlying graph
                    // structure
                    cancelLayouts();
                    if (searchPanel.hasWord()) {
                        reset(searchPanel.getWord());
                    }
                }
            }
        }
    };

    /**
     * The search interface to find specific concepts in the WordNet
     */
    private WordNetSearchPanel searchPanel;
    
    /*
     * Docuburst node weight action
     */
    private ActionListener weightAction = new ActionListener() {
        public void actionPerformed(ActionEvent event) {
            String actionCommand = event.getActionCommand();

            if (actionCommand.equals("children")) {
                docuburstLayout.setNodeWeight(StarburstLayout.WidthType.CHILDCOUNT);
            }
            if (actionCommand.equals("count")) {
                // remove zeros in this view
                if (zerosCheckBox.isSelected())
                    zerosCheckBox.doClick();
                docuburstLayout.setNodeWeight(StarburstLayout.WidthType.FIELD);
            }
        }
    };

    private JCheckBox zerosCheckBox;

    // initialize loggers
    // 
    static {
        Logger.getLogger(LanguageLib.class.getName()).setLevel(Level.OFF);
        //Logger.getLogger(DocuBurst.class.getName()).setLevel(Level.SEVERE);
        Logger.getLogger(DocuBurst.class.getName()).setLevel(Level.OFF);
        
        // from http://stackoverflow.com/questions/470430/java-util-logging-logger-doesnt-respect-java-util-logging-level
        
        //get the top Logger:
        Logger topLogger = java.util.logging.Logger.getLogger("");

        // Handler for console (reuse it if it already exists)
        Handler consoleHandler = null;
        //see if there is already a console handler
        for (Handler handler : topLogger.getHandlers()) {
            if (handler instanceof ConsoleHandler) {
                //found the console handler
                consoleHandler = handler;
                break;
            }
        }


        if (consoleHandler == null) {
            //there was no console handler found, create a new one
            consoleHandler = new ConsoleHandler();
            topLogger.addHandler(consoleHandler);
        }
        //set the console handler to fine:
        consoleHandler.setLevel(java.util.logging.Level.SEVERE);
    }

    public static void main(String[] args) {
        UILib.setPlatformLookAndFeel();
        
        if (args.length == 0)
            System.err.println("Usage: DocuBurst inputFileStem");
        else
            documentFile = args[0];

        /*
         * Schedule a job for the event-dispatching thread: creating and showing
         * the GUI ensures thread safety: operations on the GUI, unless excepted
         * by SwingWorker will run on event-dispatching thread
         */
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowFrame();
            }
        });
    }

    /**
     * Creates Radial WordNet Visualization GUI in a JFrame and adds default
     * elements. For thread safety, this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowFrame() {
        // Create and set up the window.
        final JFrame jFrame = new JFrame(String.format("%s - %s", APP_NAME, documentFile));
        jFrame.setIconImage(new ImageIcon("wne.gif").getImage());

        isApplet = false;
        isTableDisplay = false;

        final DocuBurst docuburst = new DocuBurst();

        jFrame.setContentPane(docuburst);
        jFrame.pack();

        jFrame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                docuburst.cancelLayouts();
                docuburst.run();
            }
        });

        jFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                new Thread(new Runnable() {
                    public void run() {
                        jFrame.dispose();
                        System.exit(0);
                    }
                }).start();
            }

            public void windowStateChanged(WindowEvent e) {
                docuburst.cancelLayouts();
                docuburst.run();
            }
        });

        // Display the window; move to screen center
        Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
        if (isTableDisplay)
            screenDim = new Dimension(2800, 2100);
        jFrame.setLocation((screenDim.width - jFrame.getWidth()) / 2, ((screenDim.height - jFrame.getHeight()) / 2) - (isTableDisplay ? 50 : 0));
        // make search box the default focus
        docuburst.searchPanel.requestFocusInWindow();
        jFrame.setVisible(true);

    }

    /**
     * Constructs a WordNetExplorer instance and reads in the WordNet database.
     * 
     * @param isTableDisplay
     *            tells whether we are on the table, if so, default to large
     *            window size
     */
    @SuppressWarnings("unchecked")
    public DocuBurst() {
        // create the main panel and initialize the Swing layout
        super();

        /**
         * JWNL initialization
         */

        // initialize JWNL in a separate thread; continue on Event Dispatching
        // Thread
        final SwingWorker<Dictionary, Void> JWNLWorker = initializeJWNL();
        JWNLWorker.execute();

        /**
         * prefuse initialization
         */

        // load default graph
        graph = new Tree();

        docuburstVisualization = new Visualization() {
            public String toString() {
                return "DocuBurst";
            }
        };
        docuburstVisualization.addGraph("graph", graph);

        // create displays and layouts
        LoadDataControl LDC = new LoadDataControl(this);

        displayPanel = new JPanel(new GridBagLayout());
        docuburstDisplay = new DisplayComponent(docuburstVisualization);
        GridBagConstraints docuburstGBC = new GridBagConstraints();
        Utilities.setGBC(docuburstGBC, 0, 0, 1, 1, 1, 1, GridBagConstraints.BOTH);
        displayPanel.add(docuburstDisplay, docuburstGBC);
        docuburstDisplay.setHighQuality(HIGH_QUALITY);
        docuburstDisplay.addControlListener(LDC);

        fishEyeDocument = new FisheyeDocument(FisheyeDocument.SectionMarker.ROW_NUMBER);

        docuburstDisplay.addPaintListener(new PaintListener() {
            AffineTransform nullTransform = new AffineTransform();

            // draw the legend after the docuburstDisplay is painted
            public void postPaint(Display d, Graphics2D g) {
                Color c = g.getColor();
                Paint p = g.getPaint();
                AffineTransform old = d.getTransform();
                g.setTransform(nullTransform);
                if (graph.getNodeCount() > 0)
                    docuburstLayout.drawLegend(d, g);
                g.setColor(c);
                g.setPaint(p);
                g.setTransform(old);
            }

            public void prePaint(Display d, Graphics2D g) {
            }
        });

        fishEyeDocument.setPreferredSize(new Dimension(150, 20));
        Utilities.setGBC(docuburstGBC, 1, 0, 0, 1, 1, 1, GridBagConstraints.VERTICAL);
        displayPanel.add(fishEyeDocument, docuburstGBC);

        // now check for dictionary initialization (need to to initialize
        // DocuBurstActionList)
        // look up the dictionary (hangs EDT until JWNL initialized, so put this
        // here rather than in done() method)
        try {
            dictionary = ((Dictionary) JWNLWorker.get());
        } catch (InterruptedException ignore) {
            // ignore interrupted exception
        } catch (ExecutionException e1) {
            // print error indicating task did not finish
            e1.printStackTrace();
        }

        docuburstLayout = new DocuBurstActionList(docuburstVisualization, fishEyeDocument, documentFile);
//        if (docuburstLayout.TREECUT)
//            docuburstLayout.addActivityListener(getDocuBurstActivityListener());
        /**
         * Setup Swing layout and content pane
         */
        TwoComponentSlidingPanel contentPane = setupContentPane();
        contentPane.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                cancelLayouts();
                run();
                docuburstVisualization.run("resize");
            }
        });

        this.setLayout(new BorderLayout());
        this.add(contentPane);
        
        
    }
    
    public ActivityListener getDocuBurstActivityListener(){
        
        return new ActivityListener() {
            
            @Override
            public void activityStepped(Activity a) {}
            
            @Override
            public void activityStarted(Activity a) {}
            
            @Override
            public void activityScheduled(Activity a) {}
            
            @Override
            public void activityFinished(Activity a) {
//                if (scentedSlider == null){
//                    interfacePane.addTab("Tree Cut", createTreeCutPanel());
//                }
            }
            
            @Override
            public void activityCancelled(Activity a) {}
        };
    }
    

    public void cancelLayouts() {
        docuburstLayout.cancel();
    }

    /**
     * Display graph for more than one sense of search word
     * 
     * @param indexWord
     *            the indexWord to display all senses of
     */
    public void reset(IndexWord indexWord) {
        try {
            LOGGER.info("INDEXWORD SEARCH");
            sensePane.displayIndexWord(indexWord);
            filterPane.displayIndexWord(indexWord);
            searchPanel.wordTextField.setText(indexWord.getLemma().replace('_', ' '));
            HashSet<PointerType> relationshipTypes = new HashSet<PointerType>();
            relationshipTypes.add(PointerType.HYPONYM);
            // load on worker thread
            SwingWorker<Graph, Void> loadDataWorker = loadDataWorker(indexWord, relationshipTypes, false, mergeWords);
            loadDataWorker.execute();
        } catch (JWNLException e) {
            e.printStackTrace();
        }
    }

    /***************************************************************************
     * Listeners and Actions
     **************************************************************************/

    /**
     * Resets the DocuBurst to a new root. Updates the information panels.
     * 
     * @param synset
     *            the synset to set as the root of DocuBurst
     */
    @SuppressWarnings("unchecked")
    public void reset(Synset synset) {
        try {
            LOGGER.info("SYNSET SEARCH");
            // get new graph
            sensePane.displaySense(synset);
            filterPane.displaySense(synset);
            searchPanel.wordTextField.setText(synset.getWord(0).getLemma().replace('_', ' '));
            HashSet relationshipTypes = new HashSet();
            relationshipTypes.add(PointerType.HYPONYM);
            // load on worker thread
            SwingWorker<Graph, Void> loadDataWorker = loadDataWorker(synset, relationshipTypes, false, mergeWords);
            loadDataWorker.execute();
        } catch (JWNLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Resets the DocuBurst to a new root. Updates the information panels.
     * 
     * @param word
     *            the word to set as the root of DocuBurst
     */
    @SuppressWarnings("unchecked")
    public void reset(Word word) {
        try {
            LOGGER.info("WORD SEARCH");
            sensePane.displayWord(word);
            filterPane.displayWord(word);
            searchPanel.wordTextField.setText(word.getLemma().replace('_', ' '));
            HashSet relationshipTypes = new HashSet();
            relationshipTypes.add(PointerType.HYPONYM);
            // load on worker thread
            SwingWorker<Graph, Void> loadDataWorker = loadDataWorker(word.getSynset(), relationshipTypes, false, mergeWords);
            loadDataWorker.execute();
        } catch (JWNLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Run DocuBurst layout
     */
    public void run() {
        if (docuburstVisualization.getGroup("graph") != null)
            if (docuburstVisualization.getGroup("graph").getTupleCount() > 0)
                docuburstVisualization.run("layout");
    }

    /***************************************************************************
     * prefuse visualization
     **************************************************************************/
    
    
    /**
     * Display the given graph in DocuBurst.
     * 
     * @param graph
     *            the graph to visualize
     */
    public void visualize(final Graph graph) {
    	

        // DOCUBURST
        // this processing takes long, so do it in a separate thread
        new SwingWorker<Graph, Void>() {
			@Override
			protected Graph doInBackground() throws Exception {
				docuburstVisualization.reset();
				LOGGER.info("Nodes: " + graph.getNodeCount() + " Edges: " + graph.getEdgeCount());
				fishEyeDocument.getVisualization().getFocusGroup(Visualization.FOCUS_ITEMS).clear();
		        docuburstLayout.getHighlightTextHoverActionControl().updateTextArea(true);
		        VisualGraph docuburstVG = docuburstVisualization.addGraph("graph", graph);
		    
		        try {
		            docuburstVG.addColumns(SectorRenderer.SECTOR_SCHEMA);
		        } catch (IllegalArgumentException e) {
		        	// duplicate columns exception
		        	e.printStackTrace();
		        }       
		        docuburstLayout.processCounts(graph);
		        
		        // clear search sets
		        docuburstVisualization.getFocusGroup(Visualization.SEARCH_ITEMS).clear();
		        docuburstLayout.addLabels();
		        
		        //Search depth tracks search results: 0 -- no result; 1 -- result; d >
                // 1 -- a descendant at depth d is a result
                graph.addColumn("searchDepth", int.class, 0);
                // create a column with _ instead of " " for searching multi-word entries
                graph.addColumn("multiWordSearchKey", "REPLACE(label,\" \", \"_\")");
                // create a column for limiting search to a word, instead of a prefix
                graph.addColumn("limitedSearchKey", "CONCAT(label, \"|\", \" \", \"|\", label, \"|\")");
                // create a column for limiting multi-word searches to exact match, not prefix
                graph.addColumn("multiWordLimitedSearchKey", "CONCAT(multiWordSearchKey, \"|\", \" \", \"|\", multiWordSearchKey, \"|\")");
                 
                // create a search panel and index radial search set 		        
 		        ((SearchTupleSet) docuburstVisualization.getFocusGroup(Visualization.SEARCH_ITEMS)).index(docuburstVG.getNodeTable().tuples(), "multiWordSearchKey");
 		        ((SearchTupleSet) docuburstVisualization.getFocusGroup(Visualization.SEARCH_ITEMS)).index(docuburstVG.getNodeTable().tuples(), "limitedSearchKey");
 		        ((SearchTupleSet) docuburstVisualization.getFocusGroup(Visualization.SEARCH_ITEMS)).index(docuburstVG.getNodeTable().tuples(),
 		                "multiWordLimitedSearchKey");
                 
                 
                 
				return graph;
			}
			
			@Override
			protected void done() {		        
		        // RUNS LAYOUT
		        docuburstVisualization.setInteractive("graph.edges", null, false);
		        docuburstVisualization.run("layout");
		        docuburstVisualization.run("resize");
		        long t1 = System.currentTimeMillis();
		        SearchQueryBinding sq = new SearchQueryBinding((Table) docuburstVisualization.getGroup("graph.nodes"), "label", (SearchTupleSet) docuburstVisualization
 		                .getFocusGroup(Visualization.SEARCH_ITEMS));
//		        JSearchPanel search = new JSearchPanel(sq.getSearchSet(), "label", true) {
//		            public String getQuery() {
//		                String query = super.getQuery();
//		                if (query.equals("|"))
//		                    return new String();
//		                else
//		                    return query;
//		            }
//		        };
		        JSearchPanel search = sq.createSearchPanel();
		        long t2 = System.currentTimeMillis();
                System.out.println("Is EDT: " + SwingUtilities.isEventDispatchThread());
                System.out.println(String.format("visualize() took %d seconds.", (t2-t1)/1000));
		    
		        search.setShowBorder(false);
		        search.setShowResultCount(true);
		        search.setLabelText("Focus:");
		        search.setFont(FontLib.getFont(Param.interfaceFont, Font.PLAIN, 11));
		        search.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
		        
		        filterPanel.remove(0);
		        filterPanel.add(search, 0);
		        filterPanel.validate();
		        search.requestFocusInWindow();
			}
        
        }.run();
 
        
        

    }



    /***************************************************************************
     * prefuse visualization
     **************************************************************************/

    /**
     * Initialize the JWNL off the event-dispatching thread, to provide for
     * continued construction of the GUI. When everything else is done, GUI will
     * call "get()" which blocks until dictionary is ready.
     * 
     * @return a SwingWorker which will initialize the JWNL with a file-backed
     *         dictionary
     */
    private SwingWorker<Dictionary, Void> initializeJWNL() {
        final SwingWorker<Dictionary, Void> worker = new SwingWorker<Dictionary, Void>() {
            @Override
            protected Dictionary doInBackground() throws Exception {
                // initialize JWNL using properties file; must be done before
                // use
                String propsFile = "jwnl_file_properties.xml"; // jwnl properties
                try {
                    InputStream propsStream = IOLib.streamFromString(propsFile);
                    JWNL.initialize(propsStream);
                } catch (JWNLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // get a JWNL pointer to a WordNet dictionary
                Dictionary tempDict = Dictionary.getInstance();
                if (tempDict == null) {
                    System.err.println("Dictionary instance is null: confirm dictionary is located as specified in " + propsFile + ".");
                    System.exit(-1);
                }

                return tempDict; // not used -- get this with get() instead
            };
        };
        return worker;
    }
    
    
    private SwingWorker<Graph, Void> loadDataWorker(final IndexWord indexWord, final HashSet<PointerType> relationshipTypes, final boolean countPolysemy,
            final boolean mergeWords) throws JWNLException {
        final SwingWorker<Graph, Void> worker = new SwingWorker<Graph, Void>() {
            protected Graph doInBackground() throws InterruptedException, ExecutionException, JWNLException {
                Graph tempgraph = WordNetTree.fillGraph(indexWord, relationshipTypes, countPolysemy, mergeWords);
                return tempgraph;
            }

            protected void done() {
                try {
                    graph = get();
                    if (graph.getNodeCount() > 0)
                        visualize(graph);
                } catch (InterruptedException ignore) {
                } catch (ExecutionException e) {
                    // could not load the data
                    e.printStackTrace();
                }
            }
        };
        return worker;
    }

    private SwingWorker<Graph, Void> loadDataWorker(final Synset synset, final HashSet<PointerType> relationshipTypes, final boolean countPolysemy,
            final boolean mergeWords) throws JWNLException {
        final SwingWorker<Graph, Void> worker = new SwingWorker<Graph, Void>() {
            protected Graph doInBackground() throws InterruptedException, ExecutionException, JWNLException {
                Graph tempgraph = WordNetTree.fillGraph(synset, relationshipTypes, countPolysemy, mergeWords);
                return tempgraph;
            }

            protected void done() {
                try {
                    graph = get();
                    if (graph.getNodeCount() > 0)
                        visualize(graph);
                } catch (InterruptedException ignore) {
                    ignore.printStackTrace();
                } catch (ExecutionException e) {
                    // could not load the data
                    e.printStackTrace();
                }
            }
        };
        return worker;
    }

    private JPanel createFilterPanel() {
        GridBagConstraints c = new GridBagConstraints();
        JPanel filterTab = new JPanel(new GridBagLayout());
        filterPanel = new JPanel();

        // create a search panel for the tree map
        SearchQueryBinding sq = new SearchQueryBinding((Table) docuburstVisualization.getGroup("graph.nodes"), "label", (SearchTupleSet) docuburstVisualization
                .getGroup(Visualization.SEARCH_ITEMS));
        JSearchPanel search = sq.createSearchPanel();
        
        search.setShowBorder(false);
        search.setShowResultCount(true);
        search.setLabelText("Focus:");
        search.setFont(FontLib.getFont(Param.interfaceFont, Font.PLAIN, 11));
        search.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        
        Utilities.setGBC(c, 0, 0, 1, 0, 1, 1, GridBagConstraints.BOTH);
        filterPanel.add(search);
        filterTab.add(filterPanel, c);

        // sense detail panel
        JScrollPane scrollPane = new JScrollPane(filterPane);
        JPanel sensePanel = new JPanel(new BorderLayout());
        sensePanel.setBorder(new TitledBorder("Word/Sense Details:"));
        sensePanel.add(scrollPane);

        Utilities.setGBC(c, 0, 1, 1, 0.25, 1, 1, GridBagConstraints.BOTH);
        filterTab.add(sensePanel, c);
        return filterTab;

    }
    
    private JPanel createTreeCutPanel(){
        
        JPanel panel = new JPanel(new BorderLayout());
        TreeCutCache cache = ((CachedTreeCutFilter)docuburstLayout.getFisheyeTreeFilter()).getTreeCutCache();
        List<Double> weights = cache.getSortedWeights();
        ArrayList<Point> points = new ArrayList<ScentedSliderModel.Point>();
        for (Double w : weights) {
            points.add(new Point(w, cache.get(w).size()));
        }
        this.scentedSlider = new ScentedSlider(points, 0, true);
        this.scentedSlider.getModel().addChangeListener(new ChangeListener() {
            
            @Override
            public void stateChanged(ChangeEvent e) {
                if (!scentedSlider.getModel().valueIsAdjusting())
                    docuburstLayout.setTreeCutWeight(scentedSlider.getModel().getValue());
                
            }
        });
        panel.add(scentedSlider, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createOptionsPanel() {
        GridBagConstraints c = new GridBagConstraints();

        JPanel optionsPanel = new JPanel(new BorderLayout());

        JPanel topPanel = new JPanel(new GridBagLayout());
        JPanel bottomPanel = new JPanel(new GridBagLayout());

        // FILTER

        JPanel depthFilterPanel = new JPanel();
        depthFilterPanel.setBorder(new TitledBorder("Depth Filter"));

        JLabel depthLabel = new JLabel("Maximum tree depth:");
        FisheyeTreeFilter treeFilter = docuburstLayout.getFisheyeTreeFilter();
        depthSpinner = new JSpinner(new SpinnerNumberModel(docuburstLayout.getDepthFilterDistance(), 1, 20, 1));
//        depthSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 20, 1));
        depthLabel.setDisplayedMnemonic('d');
        depthFilterPanel.add(depthLabel);
        depthFilterPanel.add(depthSpinner);
        depthLabel.setLabelFor(depthSpinner);
        depthSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JSpinner spinner = (JSpinner) e.getSource();
                SpinnerNumberModel model = (SpinnerNumberModel) spinner.getModel();
                docuburstLayout.setDepthFilterDistance(model.getNumber().intValue());
                run();
                docuburstVisualization.run("resize"); // zoom to fit because
                                                        // layout doesn't
            }
        });

        // add keyboard mappings to UP and DOWN for tree depth filter

        Action upAction = new AbstractAction() {
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    depthSpinner.getModel().setValue(depthSpinner.getModel().getNextValue());
                } catch (IllegalArgumentException illegalArgumentException) {
                    // if action performed when top of spinner already reached
                }
            }
        };

        Action downAction = new AbstractAction() {
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    depthSpinner.getModel().setValue(depthSpinner.getModel().getPreviousValue());
                } catch (IllegalArgumentException illegalArgumentException) {
                    // if action performed when bottom of spinner already
                    // reached
                }
            }
        };

        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "up");
        this.getActionMap().put("up", upAction);
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "down");
        this.getActionMap().put("down", downAction);

        ButtonGroup depthFilterBG = new ButtonGroup();
        JRadioButton searchAndFocusButton = new JRadioButton("Search and Focus");
        searchAndFocusButton.addActionListener(depthFilterAction);
        searchAndFocusButton.setActionCommand("Search and Focus"); // keep
                                                                    // command
                                                                    // even if
                                                                    // change
                                                                    // button
                                                                    // text //
                                                                    // action
        JRadioButton focusButton = new JRadioButton("Focus");
        focusButton.addActionListener(depthFilterAction);
        focusButton.setActionCommand("Focus");
        
        JCheckBox unevenCheckBox = new JCheckBox("Uneven");
        unevenCheckBox.setSelected(docuburstLayout.getDepthFilterApproach().equals(Param.DepthFilter.TREECUT));
        unevenCheckBox.addActionListener(unevenAction);
        
        depthFilterBG.add(searchAndFocusButton);
        depthFilterBG.add(focusButton);
        depthFilterPanel.add(searchAndFocusButton);
        depthFilterPanel.add(focusButton);
        depthFilterPanel.add(unevenCheckBox);
        searchAndFocusButton.setSelected(true);

        Utilities.setGBC(c, 0, 0, 2, 0, 2, 1, GridBagConstraints.HORIZONTAL);
        topPanel.add(depthFilterPanel, c);

        // SAVE BUTTONS

        Action saveAction = new AbstractAction() {
            public void actionPerformed(ActionEvent actionEvent) {
                ExportDisplayAction eda = new ExportDisplayAction(docuburstDisplay);
                eda.actionPerformed(actionEvent);
            }
        };

        if (!isApplet) {
            JPanel savePanel = new JPanel();
            JButton saveButton;
//          JButton pdfButton;
            savePanel.add(saveButton = new JButton("Save"));
//          savePanel.add(pdfButton = new JButton("PDF"));
            saveButton.addActionListener(saveAction);
            Utilities.setGBC(c, 4, 0, 0, 0, 1, 1, GridBagConstraints.NONE);
            topPanel.add(savePanel, c);
            this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_MASK), "e");
            this.getActionMap().put("e", saveAction);
        }

        // DOCUBURST OPTIONS

        JPanel docuBurstOptionsPanel = new JPanel();
        docuBurstOptionsPanel.setBorder(new TitledBorder("DocuBurst Options"));
        JLabel startTileLabel = new JLabel("Start tile:");
        JSpinner startSpinner = new JSpinner(new SpinnerNumberModel(1, 1, docuburstLayout.getHighlightTextHoverActionControl().getTotalTiles(), 1));
        docuburstLayout.setStartTile(((SpinnerNumberModel) startSpinner.getModel()).getNumber().intValue());
        docuBurstOptionsPanel.add(startTileLabel);
        docuBurstOptionsPanel.add(startSpinner);

        JLabel endTileLabel = new JLabel("End tile:");
        final JSpinner endSpinner = new JSpinner(new SpinnerNumberModel(docuburstLayout.getHighlightTextHoverActionControl().getTotalTiles(), 1, docuburstLayout.getHighlightTextHoverActionControl().getTotalTiles(), 1));
        docuburstLayout.setEndTile(((SpinnerNumberModel) endSpinner.getModel()).getNumber().intValue());
        docuBurstOptionsPanel.add(endTileLabel);
        docuBurstOptionsPanel.add(endSpinner);

        startSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JSpinner spinner = (JSpinner) e.getSource();
                SpinnerNumberModel model = (SpinnerNumberModel) spinner.getModel();
                docuburstLayout.setStartTile(model.getNumber().intValue());
                SpinnerNumberModel endModel = (SpinnerNumberModel) endSpinner.getModel();
                if (model.getNumber().intValue() - 1 == endModel.getNumber().intValue())
                    endModel.setValue(endModel.getNextValue());
                else if (model.getNumber().intValue() + 1 == endModel.getNumber().intValue())
                    endModel.setValue(endModel.getPreviousValue());
            }
        });

        endSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JSpinner spinner = (JSpinner) e.getSource();
                SpinnerNumberModel model = (SpinnerNumberModel) spinner.getModel();
                docuburstLayout.setEndTile(model.getNumber().intValue());
            }
        });

        JRadioButton childCountsButton = new JRadioButton("Cumulative");
        childCountsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                docuburstLayout.setCountType(DocuBurstActionList.CHILDCOUNT);
            }
        });
        JRadioButton nodeCountsButton = new JRadioButton("Single Node");
        nodeCountsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                docuburstLayout.setCountType(DocuBurstActionList.NODECOUNT);
            }
        });

        JRadioButton noCountsButton = new JRadioButton("None");
        noCountsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                docuburstLayout.setCountType(DocuBurstActionList.NOCOUNT);
            }
        });

        final JCheckBox wordsCheckBox = new JCheckBox("Words");
        wordsCheckBox.setSelected(!DocuBurstActionList.omitWords);
        wordsCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                DocuBurstActionList.omitWords = !wordsCheckBox.isSelected();
                run();
            }
        });

        zerosCheckBox = new JCheckBox("Zeros");
        zerosCheckBox.setSelected(!DocuBurstActionList.omitZeros);
        zerosCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                DocuBurstActionList.omitZeros = !zerosCheckBox.isSelected();
                run();
            }
        });

        docuBurstOptionsPanel.add(noCountsButton);
        docuBurstOptionsPanel.add(nodeCountsButton);
        docuBurstOptionsPanel.add(childCountsButton);
        docuBurstOptionsPanel.add(wordsCheckBox);
        docuBurstOptionsPanel.add(zerosCheckBox);
        ButtonGroup cButtonGroup = new ButtonGroup();
        cButtonGroup.add(noCountsButton);
        cButtonGroup.add(nodeCountsButton);
        cButtonGroup.add(childCountsButton);
        childCountsButton.doClick();

        Utilities.setGBC(c, 0, 0, 3, 0, 3, 1, GridBagConstraints.HORIZONTAL);
        bottomPanel.add(docuBurstOptionsPanel, c);

        // WEIGHT

        JPanel weightPanel = new JPanel();
        weightPanel.setBorder(new TitledBorder("DocuBurst Node Size"));
        ButtonGroup weightBG = new ButtonGroup();
        JRadioButton childrenButton = new JRadioButton("Number of Children");
        childrenButton.addActionListener(weightAction);
        childrenButton.setActionCommand("children"); // keep action command
                                                        // even if change button
                                                        // text
        childrenButton.setSelected(true);
        JRadioButton countButton = new JRadioButton("Word Count");
        countButton.addActionListener(weightAction);
        countButton.setActionCommand("count");
        weightBG.add(childrenButton);
        weightBG.add(countButton);
        if (docuburstLayout.getLayout().getWidthType() == StarburstLayout.WidthType.CHILDCOUNT)
            childrenButton.setSelected(true);
        else
            countButton.setSelected(true);
        weightPanel.add(childrenButton);
        weightPanel.add(countButton);
        searchAndFocusButton.setSelected(true);

        Utilities.setGBC(c, 3, 0, 2, 0, 2, 1, GridBagConstraints.HORIZONTAL);
        bottomPanel.add(weightPanel, c);

        optionsPanel.add(topPanel, BorderLayout.NORTH);
        optionsPanel.add(bottomPanel, BorderLayout.SOUTH);
        return optionsPanel;
    }

    private JPanel createSearchPanel() {
        GridBagConstraints c = new GridBagConstraints();
        JPanel searchTab = new JPanel(new GridBagLayout());
        searchPanel = new WordNetSearchPanel(dictionary);
        // associate search action with search button
        searchPanel.setSearchAction(searchAction);

        Utilities.setGBC(c, 0, 0, 1, 0.1, 1, 1, GridBagConstraints.BOTH);
        searchTab.add(searchPanel, c);

        // sense detail panel
        JScrollPane scrollPane = new JScrollPane(sensePane);
        JPanel sensePanel = new JPanel(new BorderLayout());
        sensePanel.setBorder(new TitledBorder("Word Senses:"));
        sensePanel.add(scrollPane);

        Utilities.setGBC(c, 0, 1, 1, 1, 1, 1, GridBagConstraints.BOTH);
        searchTab.add(sensePanel, c);
        return searchTab;

    }

    private TwoComponentSlidingPanel setupContentPane() {

        // search word panel
        interfacePane = new JTabbedPane();

        JPanel searchTab = createSearchPanel();
        interfacePane.addTab("Search", searchTab);
        interfacePane.setMnemonicAt(interfacePane.getTabCount() - 1, KeyEvent.VK_S);

        // filter graph panel
        JPanel filterPanel = createFilterPanel();
        interfacePane.addTab("Filter", filterPanel);
        interfacePane.setMnemonicAt(interfacePane.getTabCount() - 1, KeyEvent.VK_F);

        // relationship and options panel
        JPanel optionsPanel = createOptionsPanel();
        interfacePane.addTab("Options", optionsPanel);
        interfacePane.setMnemonicAt(interfacePane.getTabCount() - 1, KeyEvent.VK_O);
        
        // text tiles panel
        final TilesPanel tilesPanel = new TilesPanel(docuburstLayout.getHighlightTextHoverActionControl());
        fishEyeDocument.addValueListener(tilesPanel);
        fishEyeDocument.addValueListener(new ValueListener<Integer>() {
            @Override
            public void valueChanged(ValueChangedEvent<Integer> e) {
                interfacePane.setSelectedComponent(tilesPanel);
            }
        });
        
        interfacePane.addTab("Text Segments", tilesPanel);
        interfacePane.setMnemonicAt(interfacePane.getTabCount() - 1, KeyEvent.VK_T);

        JPanel concordancesPanel = new ConcordancePanel(docuburstLayout);
        interfacePane.addTab("Concordance Lines", concordancesPanel);
        interfacePane.setMnemonicAt(interfacePane.getTabCount() - 1, KeyEvent.VK_C);

        if (isTableDisplay)
            interfacePane.setPreferredSize(new Dimension(600, 300));

        contentPane = new TwoComponentSlidingPanel(interfacePane, displayPanel, TwoComponentSlidingPanel.Position.BOTTOM, 100, 2000);

        interfacePane.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                contentPane.ignore = true;
            };
        });

        if (isTableDisplay)
            contentPane.setPreferredSize(new Dimension(2800, 2150));
        else
            contentPane.setPreferredSize(new Dimension(1400, 1000));
        

        return contentPane;
    }
}