/* CVS $Id: DocuBurstActionList.java,v 1.5 2008/12/10 23:19:43 cmcollin Exp $ */
package ca.utoronto.cs.docuburst.prefuse;

import static ca.utoronto.cs.wordnetexplorer.utilities.Constants.LEMMA;
import static ca.utoronto.cs.wordnetexplorer.utilities.Constants.S2W;
import static ca.utoronto.cs.wordnetexplorer.utilities.Constants.WORD;
import static ca.utoronto.cs.wordnetexplorer.utilities.Constants.dictionary;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;

import prefuse.Constants;
import prefuse.Display;
import prefuse.DisplayComponent;
import prefuse.Visualization;
import prefuse.action.Action;
import prefuse.action.ActionList;
import prefuse.action.GroupAction;
import prefuse.action.ItemAction;
import prefuse.action.RepaintAction;
import prefuse.action.animate.ColorAnimator;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.FontAction;
import prefuse.action.filter.FisheyeTreeFilter;
import prefuse.action.filter.VisibilityFilter;
import prefuse.action.layout.Layout;
import prefuse.controls.ControlAdapter;
import prefuse.controls.HoverActionControl;
import prefuse.controls.PanControl;
import prefuse.controls.ZoomControl;
import prefuse.controls.ZoomToFitControl;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Schema;
import prefuse.data.Tree;
import prefuse.data.Tuple;
import prefuse.data.event.TupleSetListener;
import prefuse.data.expression.AndPredicate;
import prefuse.data.expression.OrPredicate;
import prefuse.data.expression.Predicate;
import prefuse.data.expression.parser.ExpressionParser;
import prefuse.data.search.PrefixSearchTupleSet;
import prefuse.data.search.SearchTupleSet;
import prefuse.data.tuple.CompositeTupleSet;
import prefuse.data.tuple.DefaultTupleSet;
import prefuse.data.tuple.TupleSet;
import prefuse.render.AbstractShapeRenderer;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.EdgeRenderer;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.util.GraphicsLib;
import prefuse.util.PrefuseLib;
import prefuse.util.display.DisplayLib;
import prefuse.visual.DecoratorItem;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.HoverPredicate;
import prefuse.visual.expression.VisiblePredicate;
import prefuse.visual.sort.ItemSorter;
import ca.utoronto.cs.docuburst.DocuBurst;
import ca.utoronto.cs.docuburst.Param;
import ca.utoronto.cs.docuburst.Param.DepthFilter;
import ca.utoronto.cs.docuburst.prefuse.action.HighlightTextHoverActionControl;
import ca.utoronto.cs.docuburst.prefuse.action.NodeColorAction;
import ca.utoronto.cs.docuburst.prefuse.action.NodeStrokeColorAction;
import ca.utoronto.cs.docuburst.prefuse.action.PathTraceHoverActionControl;
import ca.utoronto.cs.docuburst.prefuse.action.StarburstScaleFontAction;
import ca.utoronto.cs.docuburst.preprocess.POSTagger;
import ca.utoronto.cs.docuburst.preprocess.Tiling;
import ca.utoronto.cs.docuburst.preprocess.WordMap;
import ca.utoronto.cs.prefuseextensions.layout.StarburstLayout;
import ca.utoronto.cs.prefuseextensions.layout.StarburstLayout.WidthType;
import ca.utoronto.cs.prefuseextensions.lib.Colors;
import ca.utoronto.cs.prefuseextensions.render.ArcLabelRenderer;
import ca.utoronto.cs.prefuseextensions.render.DecoratorLabelRenderer;
import ca.utoronto.cs.prefuseextensions.render.HandlerRenderer;
import ca.utoronto.cs.prefuseextensions.render.SectorRenderer;
import ca.utoronto.cs.prefuseextensions.sort.TreeDepthItemSorter;
import ca.utoronto.cs.wordnetexplorer.prefuse.FisheyeDocument;
import ca.utoronto.cs.wordnetexplorer.prefuse.action.WordNetExplorerActionList;
import ca.utoronto.cs.wordnetexplorer.prefuse.controls.DisplaySenseMouseOverControl;
import ca.utoronto.cs.wordnetexplorer.utilities.LanguageLib.CountMethod;
import edu.stanford.nlp.ling.TaggedWord;

public class DocuBurstActionList extends WordNetExplorerActionList {

	private static final boolean DIVIDE_BY_CHILDREN = false;
	
	/**
	 * Counts stored as CACHECOUNT + COUNTTYPE are sums of counts of active sections of the document (the CACHERANGE) 
	 */
	public static final String CACHECOUNT = "cacheCount";
	public static final String CACHERANGE = "cacheRange";
	
	// This column indicates whether or not a node belongs to the tree cut.
	// 1 = True; 0 = False. For some reason the Node structure does not support Boolean.
	public static final String CUT = "cut";
	
	// Marks nodes that are in the visibility boundary and have invisible children.
	public static final String AGGREGATE = "agg";
	

	/**
	 * Counts are stored as arrays of counts for each section or tile of the document.
	 */
	public static final String CHILDCOUNT = "childCount"; // accumulated frequency of a node's subtree
	public static final String NOCOUNT = "noCount";
	public static final String NODECOUNT = "nodeCount"; // frequency directly associated with a node
	public static final String LEAFCOUNT = "leafCount"; // number of leaves dominated by a node
	// specific conditional entropy. see http://www.autonlab.org/tutorials/infogain11.pdf
	public static final String CONDENTROPY = "condEntropy"; 

	// node field names
	public static final String HIGHLIGHT = "highlight";
	public static final String LABELS = "labels";
	public static final String HANDLERS = "handlers";
	
	public static boolean omitWords = true;
	public static boolean omitZeros = true;
	
	// create data description of labels, setting colors, fonts ahead of time
	private static final Schema LABEL_SCHEMA = PrefuseLib.getVisualItemSchema();
	private static final Schema AGG_SCHEMA = PrefuseLib.getVisualItemSchema();

	// predicate filtering which nodes should be labeled 
	private static Predicate labelPredicate = new AndPredicate(ExpressionParser.predicate("(type = 1 or type = 0)"), 
			new OrPredicate(new VisiblePredicate()));
	private static Predicate aggregatePredicate = ExpressionParser.predicate(
			String.format("%s = true",AGGREGATE));

	public static final boolean FONTFROMDIAGONAL = false;
	
	private Param.DepthFilter depthFilter = Param.DepthFilter.TREECUT;
	private int depthFilterDistance = 1;
	
	public static final double MAXFONTHEIGHT = 40.0;

	private static final double MINFONTHEIGHT = 6.0;
	
	public String depthFilterScope = Param.DepthFilterScope.SEARCH_AND_FOCUS;
	
	// sum of all values childCount and nodeCount, respectively, over the active document region
	private float childMaxTotal;
	private float nodeMaxTotal;
	private float condEntropyMaxTotal;
	private float condEntropyMinTotal = Float.MAX_VALUE;

	// currently selected type of count 
	public String countType = NODECOUNT;

	// text document
	private List<String> tiledText;
	int startTile, endTile, maxTiles;

	/** 
	 * Counts are stored per word, not per synset; only non zero counts are stored.
	 */
	HashMap<String, float[]> wordMap;
	HashMap<String, float[]> wordMap2;
	
	// prefuse controls and actions 
	private FisheyeTreeFilter fisheyeTreeFilter; 
	public DefaultTreeCutFilter cachedTreeCutFilter;
	private NodeColorAction nodeColor;
	private ColorAction handlerFillColor;
	
	StarburstLayout treeLayout;
	
	private PanControl panControl;
	private HoverActionControl hoverActionControl;
	private HighlightTextHoverActionControl highlightTextHAC;
	private ControlAdapter mouseWheelControl;
	private DisplaySenseMouseOverControl displaySenseMouseOverControl;
	private ZoomControl zoomControl;
	private ZoomToFitControl zoomToFitControl;

	static {
		LABEL_SCHEMA.addColumn("rotation", double.class, 0.0);
		LABEL_SCHEMA.setDefault(VisualItem.INTERACTIVE, false);
		LABEL_SCHEMA.setDefault(VisualItem.TEXTCOLOR, ColorLib.gray(10));
		LABEL_SCHEMA.setDefault(VisualItem.FONT, FontLib.getFont("Verdana", Font.PLAIN, 4));
	}

	public static String wrap(String startString, int width) {
		if (startString == null)
			return null;
		StringBuilder wrappedBuf = new StringBuilder();
		String unwrapped = new String(startString);
		int i = unwrapped.indexOf(' ', width);
		while (i != -1) {
			wrappedBuf.append(unwrapped.substring(0, i));
			wrappedBuf.append("\n");
			unwrapped = unwrapped.substring(i + 1);
			i = unwrapped.indexOf(' ', width);
		}
		if (!unwrapped.equals(""))
			wrappedBuf.append(unwrapped);
		return wrappedBuf.toString();
	}
	protected static void insertText(JTextPane editor, String text, AttributeSet set) {
		try {
			editor.getDocument().insertString(editor.getDocument().getLength(), text, set);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	public DocuBurstActionList(Visualization visualization, FisheyeDocument fisheyeDocument, String filename) {
		super(visualization);
		
//		String fullTextFile = "texts/" + filename + ".tiled.txt";
//		String wordsFile = "texts/" + filename + ".tiled.tagged.cleaned.txt";
//		String fullTextFile = "texts/" + filename + ".txt";
		String fullTextFile = filename;
		
		// do this on Event processing thread because need this to continue setup
		if (dictionary == null)
			System.err.println("WordNet dictionary not initialized.");
//		String[] fullText = LanguageLib.fillWordCountsMap(wordMap = new HashMap<String, float[]>(), wordsFile, fullTextFile, CountMethod.FIRST);
//		List<String> tiledText = new ArrayList<String>();
//		for (int i = 0; i < fullText.length; i++) {
//			tiledText.add(fullText[i]);
//		}
		
		tiledText = Tiling.tile(fullTextFile);
		List<List<TaggedWord>> tiledTaggedFile = null;
		try {
			tiledTaggedFile = POSTagger.tagTiles(tiledText);
		} catch (Exception e1) { e1.printStackTrace();}
		WordMap.fillWordCountsMap(wordMap = new HashMap<String, float[]>(), tiledTaggedFile, CountMethod.FIRST);
		
		
		fisheyeDocument.initializeText(tiledText);
		fisheyeDocument.getVisualization().run("init");

		// -- set up renderers --

		m_vis.setRendererFactory(createRenderers(isTreeCutEnabled()));
		
		// -- set up processing actions --

		// search
		SearchTupleSet search = new PrefixSearchTupleSet();
		m_vis.addFocusGroup(Visualization.SEARCH_ITEMS, search);
		search.addTupleSetListener(new TupleSetListener() {
			public void tupleSetChanged(@SuppressWarnings("unused")
			TupleSet t, Tuple[] add, Tuple[] rem) {
				for (Tuple element : rem) {
					Node n = (Node) element;
					// do not highlight search results to root as in
					// RadialNodeLink
					n.setInt("searchDepth", 0);
				}

				for (Tuple element : add) {
					Node n = (Node) element;
					// do not highlight search results to root as in RadialNodeLink
					n.setInt("searchDepth", 1);
				}

				if ((add.length > 0) || (rem.length > 0)) {
					m_vis.cancel("animatePaint");
					m_vis.cancel("resize");
					m_vis.cancel("layout");
					m_vis.cancel("repaint");
					m_vis.cancel("recolor");
					m_vis.cancel("visibility");
					m_vis.cancel("labelVisibility");
					m_vis.cancel("labelFilter");
					if (getFisheyeTreeFilter().getSources().equals("searchAndFocus")) {
						m_vis.run("layout"); // rerun everything because want to show related to search results
						m_vis.runAfter("layout", "resize");
					} else {
						m_vis.run("recolor");
						m_vis.run("labelVisibility");
						m_vis.run("labelFilter");
						m_vis.run("animatePaint");
					}
				}
			}
		});

		// colors
		nodeColor = new NodeColorAction(this, "graph.nodes", true);
		ColorAction nodeStrokeColor = new NodeStrokeColorAction(this, "graph.nodes");
		handlerFillColor = new ColorAction(HANDLERS, VisualItem.FILLCOLOR, ColorLib.hex("#52576A"));
		
		// Color by POS	
		ActionList nodeColorList = new ActionList();
		nodeColorList.add(nodeColor);
		nodeColorList.add(handlerFillColor);
		nodeColorList.add(nodeStrokeColor);

		// edge colors
		ColorAction edgeColorAction = new ColorAction("graph.edges", VisualItem.STROKECOLOR) {
			public int getColor(VisualItem item) {
				Edge eItem = (Edge) item;
				// only sense-word edges are visible
				if (eItem.getInt("type") == S2W) {
					if ((m_vis.getFocusGroup("pathToRoot").containsTuple(eItem.getTargetNode()) && eItem.getTargetNode().getInt("type") == 0))
						return ColorLib.setAlpha(ColorScheme.wordRootColor, ColorScheme.zeroAlpha);
					else
						return ColorScheme.edgeColor;
				} else
					return ColorLib.rgba(0, 0, 0, 0);
			}
		};

		m_vis.addFocusGroup("pathToRoot", new DefaultTupleSet());
		m_vis.addFocusGroup(HIGHLIGHT, new DefaultTupleSet());

		ColorAction labelColor = new ColorAction(LABELS, VisualItem.TEXTCOLOR) {
			public int getColor(VisualItem item) {
				DecoratorItem dItem = (DecoratorItem) item;
				int color = ((Integer) dItem.getSchema().getDefault(VisualItem.TEXTCOLOR)).intValue();
				int alpha = ColorScheme.zeroAlpha;

				if (dItem.getDecoratedItem().isHover())
					return ColorLib.gray(0, 255);
				if (dItem.getDecoratedItem().isInGroup("pathToRoot") && dItem.getDecoratedItem().getInt("type") == 0)
					return ColorScheme.wordRootColor;
				if (dItem.getDecoratedItem().isInGroup("pathToRoot"))
					return ColorLib.gray(0, 255);
				if (dItem.canGetFloat(CACHECOUNT + countType))
					if (dItem.getFloat(CACHECOUNT + countType) == 0) // zero counts
						return ColorLib.setAlpha(color, alpha);
				return ColorLib.setAlpha(color, 180);
			}
		};

		ColorAction lemmaColor = new ColorAction("graph.nodes", VisualItem.TEXTCOLOR) {
			public int getColor(VisualItem item) {
				if (item.isHover())
					return ColorLib.gray(10, 255);
				if (item.isInGroup("pathToRoot"))
					return ColorLib.gray(10, 255);
				if (item.canGetFloat(CACHECOUNT + countType))
					if (item.getFloat(CACHECOUNT + countType) == 0) // zero counts
						return ColorLib.gray(10, ColorScheme.zeroAlpha);
				return ColorLib.gray(10, 180);
			}
		};

		// font settings
		FontAction decoratorFonts = new StarburstScaleFontAction(LABELS);
		FontAction lemmaFont = new FontAction("graph.nodes", FontLib.getFont("Verdana", Font.BOLD, 12)); // changed from 13
		// plain for Vivian

		// recolor
		ActionList recolor = new ActionList();
		recolor.add(nodeColorList);
		recolor.add(edgeColorAction);
		recolor.add(labelColor);
		recolor.add(lemmaColor);
		m_vis.putAction("recolor", recolor);

		// repaint
		ActionList repaint = new ActionList();
		repaint.add(recolor);
		repaint.add(decoratorFonts);
		repaint.add(lemmaFont);
		repaint.add(new RepaintAction());
		m_vis.putAction("repaint", repaint);

		// animate paint change
		ActionList animatePaint = new ActionList(400, 100);
		animatePaint.add(new ColorAnimator("graph.nodes"));
		animatePaint.add(new ColorAnimator(LABELS));
		animatePaint.add(new RepaintAction());
		m_vis.putAction("animatePaint", animatePaint);

		treeLayout = new StarburstLayout("graph");
		if (countType == NODECOUNT){
		    treeLayout.setWidthType(WidthType.FIELD, CACHECOUNT + NODECOUNT);
		} else {
		    treeLayout.setWidthType(WidthType.CHILDCOUNT, null);
		}
//		treeLayout.setWidthType(widthType, widthField);
		// set layout anchor so it doesn't reset on reload new data

		// move display to have (0,0) at center
		m_vis.getDisplay(0).zoomAbs(new Point2D.Double(), 1 / m_vis.getDisplay(0).getScale());
		// set layout center as (0,0)
		treeLayout.setLayoutAnchor(new Point2D.Double());//m_vis.getDisplay(0).getAbsoluteCoordinate(new Point2D.Double(m_vis.getDisplay(0).getWidth()/2, m_vis.getDisplay(0).getHeight()/2), null));
		// set autoscale off so if display is zoomed on repaint, the layout isn't resized to fit in full display 
		treeLayout.setAutoScale(false);

//		CollapsedSubtreeLayout subLayout = new CollapsedSubtreeLayout("graph");

		CompositeTupleSet searchAndFocus = new CompositeTupleSet();
		searchAndFocus.addSet(Visualization.FOCUS_ITEMS, m_vis.getFocusGroup(Visualization.FOCUS_ITEMS));
		searchAndFocus.addSet(Visualization.SEARCH_ITEMS, m_vis.getFocusGroup(Visualization.SEARCH_ITEMS));
		m_vis.addFocusGroup("searchAndFocus", searchAndFocus);

//		VisibilityFilter vF = createVisibilityFilter();
//		m_vis.putAction("visibility", vF);

		// create the filtering and layout for initial layout (no animation;
		// workaround for problem caused by MutableFisheyeTreeFilter on initial animation.
		ActionList filter = new ActionList();
		filter.add(new ItemAction("graph.nodes") {
			public void process(VisualItem item, double frac) {
				item.setString("label", wrap(item.getString("label"), 30));
				item.setString("gloss", wrap(item.getString("gloss"), 30));
			};
		});

		fisheyeTreeFilter = depthFilter.equals(Param.DepthFilter.TREECUT) ? 
				new DefaultTreeCutFilter("graph", depthFilterScope, getDepthFilterDistance(),
						getTreeFilterPredicates()) :
				new MultiCriteriaFisheyeFilter("graph", depthFilterScope, 
						getDepthFilterDistance(), getTreeFilterPredicates());
		

		// recentre and rezoom on reload
		Action resizeAction = new Action() {
			public void run(double frac) {
				// animate reset zoom to fit the data (must run only AFTER layout)
				Rectangle2D bounds = null;
				try{
					bounds = m_vis.getBounds("graph");
				} catch(IndexOutOfBoundsException e){
					return;
				}
				if (bounds.getWidth() == 0)
					return;
				GraphicsLib.expand(bounds, (int) (1 / m_vis.getDisplay(0).getScale()));
				DisplayLib.fitViewToBounds(m_vis.getDisplay(0), bounds, (long) 1000);
			}
		};
		m_vis.putAction("resize", resizeAction);

		// create the filtering and layout
		
		this.add(fisheyeTreeFilter);
		this.add(treeLayout);
		this.add(new GroupAction() {			
			@Override
			public void run(double frac) {
				addLabels();
			    addHandlers();
			}
		});
//		this.add(new NodeHandlerLayout(HANDLERS));
		this.add(new LabelLayout(LABELS));
		this.add(decoratorFonts);
		this.add(lemmaFont);
		this.add(recolor);
		m_vis.putAction("layout", this);

		
		// animated transition
//		ActionList animate = new ActionList(100);
//		animate.setPacingFunction(new SlowInSlowOutPacer());
////		animate.add(new QualityControlAnimator());
//		animate.add(new VisibilityAnimator(LABELS));
//		animate.add(new VisibilityAnimator("graph"));
////		animate.add(new ColorAnimator("graph"));
//		animate.add(new RepaintAction());
//		
//		m_vis.putAction("animate", animate);
//		m_vis.alwaysRunAfter("layout", "animate");

		// add listeners to displays, for "click" and "hover"
		for (int i = 0; i < m_vis.getDisplayCount(); i++) {
			Display display = m_vis.getDisplay(i);
			final TreeDepthItemSorter tdis = new TreeDepthItemSorter();
			tdis.addGroup("pathToRoot");
			display.setItemSorter(new ItemSorter(){
				@Override
				public int score(VisualItem item) {
					int score = tdis.score(item);
					if (item.getGroup().equals(HANDLERS))
						score = -1000;
					return score;
				}
			});
			display.addControlListener(zoomToFitControl = new ZoomToFitControl());
			zoomToFitControl.setZoomOverItem(false);

			display.addControlListener(hoverActionControl = new PathTraceHoverActionControl("repaint"));
			display.addControlListener(highlightTextHAC = new HighlightTextHoverActionControl(null, null, tiledText, m_vis, fisheyeDocument.getVisualization()));
			highlightTextHAC.setCountField(CACHECOUNT + NODECOUNT);
			display.addControlListener(panControl = new PanControl(true));
			display.addControlListener(zoomControl = new ZoomControl());
			
			display.addControlListener(mouseWheelControl = new ControlAdapter() {
			    public void itemWheelMoved(VisualItem item, MouseWheelEvent e) {
			        setDepthFilterDistance(getDepthFilterDistance() - e.getWheelRotation());
                    m_vis.cancel("layout");
                    m_vis.cancel("animate");
                    m_vis.run("layout");
			    }
			});

			display.addControlListener(displaySenseMouseOverControl = new DisplaySenseMouseOverControl(DocuBurst.filterPane));
		}
		
//		this.treeCutCache = buildTreeCutCache();
	}
	
	public boolean isTreeCutEnabled(){
	    return getDepthFilterApproach().equals(DepthFilter.TREECUT);
	}
	
	public void setDepthFilterApproach(Param.DepthFilter a){
	    if (a.equals(depthFilter)) return;
	    
	    this.remove(fisheyeTreeFilter);
	    
	    depthFilter = a;
	    if (depthFilter.equals(Param.DepthFilter.TREECUT))
	        fisheyeTreeFilter = new DefaultTreeCutFilter("graph", depthFilterScope, 
	        		getDepthFilterDistance(), getTreeFilterPredicates());
	    else{
	        fisheyeTreeFilter = new MultiCriteriaFisheyeFilter("graph", depthFilterScope, 
	        		getDepthFilterDistance(), getTreeFilterPredicates());
	        m_vis.setValue(HANDLERS, aggregatePredicate, AGGREGATE, false);
	    }
	        

	    // reset renderer and toggle action that paints the handler
	    m_vis.setRendererFactory(createRenderers(isTreeCutEnabled()));
//	    handlerFillColor.setDefaultColor(ColorLib.rgba(242, 183, 56, isTreeCutEnabled() ? 255 : 0));
	    
	    fisheyeTreeFilter.setDistance(getDepthFilterDistance());
	    this.add(0, fisheyeTreeFilter);
	    m_vis.putAction("layout", this);
        m_vis.cancel("layout");
        m_vis.cancel("animate");
        m_vis.run("layout");
        m_vis.run("resize");
	}
	
	private void resetTreeFilterPredicates(){
		MultiCriteriaFisheyeFilter f = (MultiCriteriaFisheyeFilter)fisheyeTreeFilter;
		f.setPredicates(getTreeFilterPredicates());
	}
	
	private Predicate[] getTreeFilterPredicates(){
		List<Predicate> predicates = new ArrayList<Predicate>();
		if (omitWords) predicates.add(DocuBurst.OMIT_WORDS_PREDICATE);
		if (omitZeros && !countType.equals(NOCOUNT)) 
			predicates.add((Predicate) ExpressionParser.parse(
					String.format("%s > 0", CACHECOUNT + CHILDCOUNT)));
		return predicates.toArray(new Predicate[0]);
	}

	public void setOmitWords(boolean omitWords){
		DocuBurstActionList.omitWords = omitWords;
		resetTreeFilterPredicates();
        m_vis.cancel("layout");
        m_vis.cancel("animate");
        m_vis.run("layout");
        m_vis.run("resize");
	}
	
	public void setOmitZeros(boolean omitZeros){
		DocuBurstActionList.omitZeros = omitZeros;
		resetTreeFilterPredicates();
        m_vis.cancel("layout");
        m_vis.cancel("animate");
        m_vis.run("layout");
        m_vis.run("resize");
	}
	
	public Param.DepthFilter getDepthFilterApproach(){
        return depthFilter;
    }
	
	public int getDepthFilterDistance() {
        return depthFilterDistance;
    }
	
    public void setDepthFilterDistance(int d) {
        this.depthFilterDistance = d;
        fisheyeTreeFilter.setDistance(d);
       
    }
    
    public void setTreeCutWeight(double w){
	    ((DefaultTreeCutFilter)fisheyeTreeFilter).setWeight(w);
        m_vis.cancel("layout");
        m_vis.cancel("animate");
        m_vis.run("layout");
        m_vis.run("resize");
	}

    
	public void addHandlers() {
		try {
			m_vis.addDecorators(HANDLERS, "graph.nodes", aggregatePredicate, AGG_SCHEMA);
		}
		catch (IllegalArgumentException e) {
			
		}
	}
	

	public void addLabels() {
		try {
			m_vis.addDecorators(LABELS, "graph.nodes", labelPredicate, LABEL_SCHEMA);
		}
		catch (IllegalArgumentException e) {
			
		}
	}

	public void cancel() {
		// cancel any running layouts
		m_vis.cancel("animate");
		m_vis.cancel("animatePaint");
		super.cancel();
		// indicate damage to the entire display (force redraw)
		for (int i = 0; i < m_vis.getDisplayCount(); i++)
			m_vis.getDisplay(i).damageReport();
	}

	public void drawLegend(Display d, Graphics2D g) {
		if (getMaxTotal(countType) == 0)
			return;

		// get hover item if any
		TupleSet t = m_vis.getVisualGroup("graph");
		Iterator i = t.tuples();
		int fillColor = ColorScheme.resultsColor;
		// check if we have more than one sense
		float target = -1;
		if (i.hasNext()) {
			if (((Tuple) i.next()).canGetInt("senseIndex")) {
				fillColor = Colors.getInstance().getColor("GRAY1");
			}
		}

		// check if we have any hover items, if so, find the sense
		// color of the hover item; if not, leave fillColor as gray to 
		// work for all senses or green for single sense
		i = t.tuples(new HoverPredicate());
		while (i.hasNext()) {
			Tuple tuple = (Tuple) i.next();
			// set where we want the indicator to appear
			// don't count lemmas as they skew the scale yet have no fill
			if (tuple.getInt("type") != LEMMA) {
				//target = (float) Math.log(tuple.getFloat(CACHECOUNT + countType));
				target = (float) (tuple.getFloat(CACHECOUNT + countType));
				if (tuple.canGetInt("senseIndex"))
					fillColor = ColorScheme.sensesPalette[tuple.getInt("senseIndex") % ColorScheme.sensesPalette.length];
			}
		}

		int h = 25;
		int w = 52;
		int ulx = d.getWidth() - 25 - w;
		int uly = d.getHeight() - 25 - h;
		Font f = FontLib.getFont("Arial", 20);
		g.setFont(f);
		String min = new String("0 ");
		String max = String.format(" %1$.2f", getMaxTotal(countType));
		FontMetrics fm = g.getFontMetrics();
		Rectangle2D minBounds = fm.getStringBounds(min, g);
		Rectangle2D maxBounds = fm.getStringBounds(max, g);
		ulx -= maxBounds.getWidth();
		// draw zero to 1 gradiant
		GradientPaint p1 = new GradientPaint(ulx, uly + h / 2, ColorLib.getColor(ColorLib.setAlpha(ColorScheme.zeroOccurrenceSenseColor, ColorScheme.zeroAlpha)), ulx + 2, uly + h / 2,
				ColorLib.getColor(ColorLib.setAlpha(fillColor, ColorScheme.zeroAlpha)));
		g.setPaint(p1);
		g.fillRect(ulx, uly, 2, h);
		// draw 1 to max gradiant
		GradientPaint p2 = new GradientPaint(ulx + 2, uly + h / 2, ColorLib.getColor(ColorLib.setAlpha(fillColor, ColorScheme.zeroAlpha)), ulx + w, uly + h / 2, 
				ColorLib.getColor(fillColor));
		g.setPaint(p2);
		g.fillRect(ulx + 2, uly, w - 2, h);
		g.setColor(ColorLib.getColor(ColorLib.setAlpha(Colors.getInstance().getColor("GRAY1"), 200)));
		g.drawString(min, (int) (ulx - minBounds.getWidth()), (int) (uly + h / 2 + (minBounds.getHeight() / 2)));
		g.drawString(max, (int) (ulx + w), (int) (uly + h / 2 + (maxBounds.getHeight() / 2)));
		// draw marker
		if (target != -1) {
			int xpos = (int) (ulx + ((float) target / (float) getMaxTotal(countType) * (w - 3)));
			g.setColor(ColorLib.getColor(ColorScheme.hoverColor));
			g.fillRect(xpos, uly, 3, h);
			f = FontLib.getFont("Arial", 10);
			g.setFont(f);
			fm = g.getFontMetrics();
			String targetString = String.format("%1$.2f", target);
			Rectangle2D targetBounds = fm.getStringBounds(targetString, g);
			g.drawString(targetString, xpos - (int) targetBounds.getWidth() / 2, uly - 2);
		}
		g.setColor(Color.black);
		g.drawRect(ulx, uly, w, h);
	}

	public FisheyeTreeFilter getFisheyeTreeFilter() {
		return fisheyeTreeFilter;
	}

	public HighlightTextHoverActionControl getHighlightTextHoverActionControl() {
		return highlightTextHAC;
	}

	public StarburstLayout getLayout() {
		return treeLayout;
	}

	/**
	 * Add nodeCount and recursive childCount field to the graph schema.  Calls addCounts(Node)
	 * which populates these new columns with counts from the source document.
	 * 
	 * @param graph the graph to add occurrence count columns to
	 */
	public void processCounts(Graph graph) {
		// check if this graph already has the count schema
		// if so, assume the counts are cached and skip it
		boolean cache = graph.getNodeTable().getColumn(NODECOUNT) != null;
		
		if (!cache){
			Schema countsSchema = new Schema();
			countsSchema.addColumn(NODECOUNT, float[].class, null);
			countsSchema.addColumn(CHILDCOUNT, float[].class, null);
			countsSchema.addColumn(CACHERANGE, String.class, null);
			countsSchema.addColumn(CACHECOUNT + CHILDCOUNT, float.class, null);
			countsSchema.addColumn(CACHECOUNT + NODECOUNT, float.class, null);
			countsSchema.addColumn(LEAFCOUNT, float.class, null);
//			countsSchema.addColumn(CONDENTROPY, float.class, null);
			// if true, the nodes belongs to the tree cut
			countsSchema.addColumn(CUT, boolean.class, false);
			countsSchema.addColumn(AGGREGATE, boolean.class, false);
			
			
			// Graph graph = (Graph) m_vis.getGroup(m_group);
			graph.addColumns(countsSchema);
			Tree t = graph.getSpanningTree();
			addCounts(t.getRoot());
		}

		cacheTotals(graph);
//		addCondEntropy(t.getRoot());
	}

	/**
	 * Remove all control listeners and visualization items.
	 */
	public void remove() {
		cancel();
		m_vis.reset();
		for (int i = 0; i < m_vis.getDisplayCount(); i++) {
			DisplayComponent display = (DisplayComponent) m_vis.getDisplay(i);
			display.removeControlListener(zoomToFitControl);
			display.removeControlListener(hoverActionControl);
			display.removeControlListener(panControl);
			display.removeControlListener(zoomControl);
			display.removeControlListener(mouseWheelControl);
			display.removeControlListener(displaySenseMouseOverControl);
			display.removeControlListener(highlightTextHAC);
		}
	}

	/**
	 * Add all control listeners back to the visualization.
	 */
	public void restart() {
		for (int i = 0; i < m_vis.getDisplayCount(); i++) {
			DisplayComponent display = (DisplayComponent) m_vis.getDisplay(i);
			display.addControlListener(zoomToFitControl);
			display.addControlListener(hoverActionControl);
			display.addControlListener(panControl);
			display.addControlListener(zoomControl);
			display.addControlListener(mouseWheelControl);
			display.addControlListener(displaySenseMouseOverControl);
			display.addControlListener(highlightTextHAC);
		}
	}

	/**
	 * Choose between node count and child (subtree) count.  Repaint the graph.
	 * 
	 * @param countType the new occurrence count mode, either node or child (subtree)
	 */
	public void setCountType(String countType) {
		this.countType = countType;
		m_vis.run("recolor");
		m_vis.run("animatePaint");
	}

	public void setEndTile(int tile) {
		endTile = tile;
		cacheTotals((Graph) m_vis.getGroup("graph"));
		m_vis.run("recolor");
		m_vis.run("animatePaint");
	}

	public void setNodeWeight(StarburstLayout.WidthType widthType) {
		if (treeLayout.getWidthType() != widthType) {
			if (widthType == StarburstLayout.WidthType.CHILDCOUNT)
				treeLayout.setWidthType(StarburstLayout.WidthType.CHILDCOUNT, null);
			else
				treeLayout.setWidthType(StarburstLayout.WidthType.FIELD, CACHECOUNT + NODECOUNT);
			if (m_vis.getGroup("graph").getTupleCount() > 0) {
				m_vis.run("layout");
				m_vis.run("resize");
			}
		}
	}

	public void setStartTile(int tile) {
		startTile = tile - 1;
		cacheTotals((Graph) m_vis.getGroup("graph"));
		m_vis.run("recolor");
		m_vis.run("animatePaint");
	}

	public float[] sumCounts(float[] start, float[] toAdd) {
		if (toAdd == null)
			return start;
		if (start == null)
			start = new float[tiledText.size()];
		// cycle through tiles, adding to counts for each
		for (int i = 0; i < tiledText.size(); i++) {
			start[i] += toAdd[i];
		}
		return start;
	}

	public float[] divide(float[] start, float divisor) {
		// cycle through tiles, adding to counts for each
		if (start == null)
			start = new float[tiledText.size()];
		if (divisor == 1)
			return start;
		for (int i = 0; i < tiledText.size(); i++) {
			start[i] /= divisor;
		}
		return start;
	}

	
	public Vector<String> vectorWrap(String startString, int width) {
		Vector<String> linesVector = new Vector<String>();
		if (startString == null)
			return linesVector;
		String unwrapped = new String(startString);
		int i = unwrapped.indexOf(' ', width);
		while (i != -1) {
			linesVector.add(unwrapped.substring(0, i));
			unwrapped = unwrapped.substring(i + 1);
			i = unwrapped.indexOf(' ', width);
		}
		if (!unwrapped.equals(""))
			linesVector.add(unwrapped);
		return linesVector;
	}

	private void addCounts(Node n) {
		if (n.getChildCount() == 0) {
			// leave counts null to save memory
		} else {
			Iterator it = n.children();
			Node c;
			while (it.hasNext()) {
				c = (Node) it.next();
				if (c.getInt("type") == WORD) {
					String key = c.getString("label") + (c.getString("pos")) + (c.getLong("offset"));
					if (wordMap.get(key) == null) {
						// leave counts null to save memory
					} else {
						c.set("nodeCount", (float[]) wordMap.get(key));
						c.set("childCount", (float[]) wordMap.get(key));
						c.set(LEAFCOUNT, 0);
					}
					// words propagate nodeCount and childCount to their parent
					n.set(LEAFCOUNT, (Float)n.get(LEAFCOUNT) + 1);
					n.set("nodeCount", sumCounts((float[]) n.get("nodeCount"), (float[]) c.get("nodeCount")));
					n.set("childCount", sumCounts((float[]) n.get("childCount"), (float[]) c.get("childCount")));
				} else {
					addCounts(c);
					// lemmas and senses only propagate childCount to parent
					n.set(LEAFCOUNT, (Float)n.get(LEAFCOUNT) + (Float)c.get(LEAFCOUNT));
					if (DIVIDE_BY_CHILDREN) {
						n.set("childCount", sumCounts((float[]) n.get("childCount"), divide((float[]) c.get("childCount"), (float) n.getChildCount())));
					} else {
						n.set("childCount", sumCounts((float[]) n.get("childCount"), (float[]) c.get("childCount")));
					}
				}
			}
		}
	}
	
	/**
	 * Compute conditional entropy all nodes within the subtree rooted by this node.
	 * @param node root
	 */
	private List<Node> addCondEntropy(Node node){
		
		List<Node> leaves = new ArrayList<Node>();
		
		if (node.getChildCount() < 1){
			leaves.add(node);
		}
		
		for (Iterator it = node.children(); it.hasNext();) {
			Node child = (Node) it.next();
			leaves.addAll(addCondEntropy(child));
		}
		
		float condEntropy = 0; // specific conditional entropy
		float nodeFreq = node.getFloat(CACHECOUNT + CHILDCOUNT);
		
		// $ H(Y|X=x) = p(x) \sum{y \in Y} p(y|x) log_2 p(y|x) $ latex formula
		
		float total = 0; // debug
		
		for (Node leaf : leaves) {
			float leafFreq = leaf.getFloat(CACHECOUNT + CHILDCOUNT);
			float leafProb = leafFreq / nodeFreq;
			total += leafFreq;
			condEntropy -= leafProb > 0 ? leafProb * Math.log(leafProb)/Math.log(2) : 0;
			// include CONDENTROPYVALUE as a type of value
			// update coloring to respond to the above change
			// use childMaxTotal for conditional entropy
			// for the probabilities, use float total = (float) (item.getFloat(DocuBurstActionList.CACHECOUNT + NodeColorAction.this.docuBurstActionList.countType))
		}
		
		if (Math.abs(total - nodeFreq) > 0.001f)
			System.err.println("Sum of child counts doesn't total node's childCount.");
		
		condEntropy *= nodeFreq/childMaxTotal;
		
		if (condEntropy > 0 && condEntropy < condEntropyMinTotal)
			condEntropyMinTotal = condEntropy;
		
		node.set(CONDENTROPY, condEntropy);
		
		condEntropyMaxTotal = condEntropy;
		
		return leaves;
	}
	

	/**
	 * For the currently selected tile range, sum all the counts of those tiles
	 * and store result in the CACHECOUNT+count_type field of the node. 
	 * @param g the graph to scan and cache
	 */
	private void cacheTotals(Graph g) {
		Iterator iterator = g.nodes();
		Node n;
		nodeMaxTotal = 0;
		childMaxTotal = 0;

		String label = makeLabel(startTile, endTile);
		while (iterator.hasNext()) {
			n = (Node) iterator.next();
			float[] nodeCounts = (float[]) n.get(NODECOUNT);
			float[] childCounts = (float[]) n.get(CHILDCOUNT);
			float nodeTotal = 0;
			float childTotal = 0;
			// only count non-null; all zeros are null to save memory
			for (int i = startTile; i < endTile; i++) {
				if (nodeCounts != null)
					nodeTotal += nodeCounts[i];
				if (childCounts != null)
					childTotal += childCounts[i];
			}

			// don't count Lemma as max
			if (n.getInt("type") != LEMMA) {
				if (Math.abs(childTotal) > childMaxTotal) 
					childMaxTotal = Math.abs(childTotal);
				if (Math.abs(nodeTotal) > nodeMaxTotal)
					nodeMaxTotal = Math.abs(nodeTotal);
			}
			n.set(CACHECOUNT + NODECOUNT, nodeTotal);
			n.set("cacheRange", label);
			n.set(CACHECOUNT + CHILDCOUNT, childTotal);
		}

		//childMaxTotal = (float) Math.log(childMaxTotal);
		//nodeMaxTotal = (float) Math.log(nodeMaxTotal);
	}

	private DefaultRendererFactory createRenderers(boolean addHandlerRenderer) {
		EdgeRenderer S2WedgeRenderer = new EdgeRenderer(Constants.EDGE_TYPE_LINE, Constants.EDGE_ARROW_NONE);

		// renderer to draw labels for words, not senses
		LabelRenderer labelRenderer = new LabelRenderer("label");
		labelRenderer.setRoundedCorner(8, 8);
		labelRenderer.setRenderType(AbstractShapeRenderer.RENDER_TYPE_DRAW_AND_FILL);
		labelRenderer.setHorizontalAlignment(Constants.CENTER);

		// renderer to draw shapes for senses, not words
		SectorRenderer shapeRenderer = new SectorRenderer();
		shapeRenderer.setRenderType(AbstractShapeRenderer.RENDER_TYPE_DRAW_AND_FILL);
		DefaultRendererFactory rf = new DefaultRendererFactory();

		HandlerRenderer handlerRenderer = new HandlerRenderer();
		handlerRenderer.setRenderType(AbstractShapeRenderer.RENDER_TYPE_FILL);
		
		// for angular rotating of non-curved labels
		DecoratorLabelRenderer decoratorLabelRenderer = new DecoratorLabelRenderer("label", false, MINFONTHEIGHT);
		// decoratorLabelRenderer.setHorizontalAlignment(Constants.LEFT);
		// for arching of labels
		
		ArcLabelRenderer arcLabelRenderer = new ArcLabelRenderer("label", MINFONTHEIGHT, MAXFONTHEIGHT);
		rf.add("ingroup('labels') and rotation == 0", arcLabelRenderer); // all sector labels
		rf.add("ingroup('labels') and rotation != 0", decoratorLabelRenderer); // all sector labels
		if (addHandlerRenderer) 
		    rf.add(String.format("ingroup('%s')", HANDLERS), handlerRenderer);
		rf.add("type = 4", labelRenderer); // central lemmas
		rf.add("type = 1 or type = 0", shapeRenderer);
		rf.add("type = 2", S2WedgeRenderer);
		rf.add("type = 5", S2WedgeRenderer);
		
		return rf;
	}

	private VisibilityFilter createVisibilityFilter() {
		VisibilityFilter vF = new VisibilityFilter("graph", DocuBurst.WORDS_PREDICATE) {
			public void run(double frac) {
				long t1 = System.currentTimeMillis();
				// only check visible items: fisheye filter already set
				// everything visible
//				Iterator items = m_vis.visibleItems(m_group);
				Iterator items = m_vis.visibleItems(m_group);
				int visibleCount = 0;
				while (items.hasNext()) {
					VisualItem item = (VisualItem) items.next();
					// fisheye filter already moved current visibility to
					// starting, we just update current & ending (not starting)
					if (omitWords && DocuBurst.WORDS_PREDICATE.getBoolean(item)) {
						item.setDOI(Constants.MINIMUM_DOI);
						item.setVisible(false);
						item.setEndVisible(false);
					}
					// omit zero count subtrees if omitting zeros and counting
//					if (omitZeros && !countType.equals(NOCOUNT)) {
//						// only check childCount so parents of nodes with counts are always visible
//						float total = item.getFloat(CACHECOUNT + CHILDCOUNT);
//						if (total == 0) {
//							// set DOI to minimum -- only those items (and their children) that 
//							// are visible are checked by the fishEyeFilter; since we may have
//							// no visible items after the zeros-off step, we set the DOI to 
//							// circumvent the first step of the fishEyeFilter and force re-check
//							// on any nodes which we turn off.
//							// have minimum DOI are checked by fishEyeFilter
//							item.setDOI(Constants.MINIMUM_DOI);
//							item.setVisible(false);
//							item.setEndVisible(false);
//						}
//					}
//					if (item instanceof NodeItem && item.isVisible())
						visibleCount++;
				}
				long t2 = System.currentTimeMillis();
				Logger.getLogger(this.getClass().getName())
					.info(String.format("Visibility filtering took %f seconds.", (float)(t2-t1)/1000));
				System.out.println(visibleCount + " items processed by VisibilityFilter.");
			}
		};
		return vF;
	}

	public float getMaxTotal(String type) {
		if (type.equals(CHILDCOUNT))
			return childMaxTotal;
		if (type.equals(NODECOUNT))
			return nodeMaxTotal;
		if (type.equals(CONDENTROPY))
			return condEntropyMaxTotal;
		return 0;
	}
	
	public float getCondEntropyMinTotal() {
		return condEntropyMinTotal;
	}

	private String makeLabel(int startTile, int endTile) {
		return new String(startTile + " " + endTile);
	}

	private void setMaxTotal(String type, int maxTotal) {
		if (type.equals("childCount"))
			childMaxTotal = maxTotal;
		else
			nodeMaxTotal = maxTotal;
	};
	
	public DefaultTreeCutFilter getTreeCutFilter(){
	    return this.cachedTreeCutFilter;
	}

	/**
	 * Set label positions. Labels are assumed to be DecoratorItem instances,
	 * decorating their respective nodes. The layout simply gets the bounds of
	 * the decorated node and assigns the label coordinates to the center of
	 * those bounds.
	 */
	class LabelLayout extends Layout {
		public LabelLayout(String group) {
			super(group);
		}

		public void run(double frac) {
			Iterator iter = m_vis.items(m_group);

			while (iter.hasNext()) {
				DecoratorItem item = (DecoratorItem) iter.next();
				VisualItem node = item.getDecoratedItem();
				Rectangle2D bounds = node.getBounds();
				double angle = 360 - item.getDouble("startAngle") - 0.5 * item.getDouble("angleExtent");
				// render arched
				if ((item.getDouble("angleExtent") > 30) && (item.getDouble("innerRadius") != 0)) { // was check for angleExtent != 360, but 360 arcs can be arched
					item.setDouble("rotation", 0);
				} else {
					// 	render straight
					item.setDouble("rotation", angle);
				}
				setX(item, null, bounds.getCenterX());
				setY(item, null, bounds.getCenterY());
			}
		}
	} // end of inner class LabelLayout
}