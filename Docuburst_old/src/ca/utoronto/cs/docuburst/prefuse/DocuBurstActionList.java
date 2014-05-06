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
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;

import prefuse.Constants;
import prefuse.Display;
import prefuse.DisplayComponent;
import prefuse.Visualization;
import prefuse.action.Action;
import prefuse.action.ActionList;
import prefuse.action.ItemAction;
import prefuse.action.RepaintAction;
import prefuse.action.animate.ColorAnimator;
import prefuse.action.animate.QualityControlAnimator;
import prefuse.action.animate.VisibilityAnimator;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.FontAction;
import prefuse.action.filter.FisheyeTreeFilter;
import prefuse.action.filter.VisibilityFilter;
import prefuse.action.layout.CollapsedSubtreeLayout;
import prefuse.action.layout.Layout;
import prefuse.activity.SlowInSlowOutPacer;
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
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.HoverPredicate;
import prefuse.visual.expression.StartVisiblePredicate;
import prefuse.visual.expression.VisiblePredicate;
import ca.utoronto.cs.docuburst.DocuBurst;
import ca.utoronto.cs.prefuseextensions.layout.StarburstLayout;
import ca.utoronto.cs.prefuseextensions.layout.StarburstLayout.WidthType;
import ca.utoronto.cs.prefuseextensions.lib.Colors;
import ca.utoronto.cs.prefuseextensions.render.ArcLabelRenderer;
import ca.utoronto.cs.prefuseextensions.render.DecoratorLabelRenderer;
import ca.utoronto.cs.prefuseextensions.render.SectorRenderer;
import ca.utoronto.cs.prefuseextensions.sort.TreeDepthItemSorter;
import ca.utoronto.cs.wordnetexplorer.prefuse.FisheyeDocument;
import ca.utoronto.cs.wordnetexplorer.prefuse.action.WordNetExplorerActionList;
import ca.utoronto.cs.wordnetexplorer.prefuse.controls.DisplaySenseMouseOverControl;
import ca.utoronto.cs.wordnetexplorer.utilities.LanguageLib;
import ca.utoronto.cs.wordnetexplorer.utilities.LanguageLib.CountMethod;

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

	/**
	 * Counts are stored as arrays of counts for each section or tile of the document.
	 */
	public static final String CHILDCOUNT = "childCount";
	public static final String NOCOUNT = "noCount";
	public static final String NODECOUNT = "nodeCount";

	// node field names
	public static final String HIGHLIGHT = "highlight";
	public static final String LABELS = "labels";
	
	public static boolean omitWords = true;
	public static boolean omitZeros = false;
	
	// create data description of labels, setting colors, fonts ahead of time
	private static final Schema LABEL_SCHEMA = PrefuseLib.getVisualItemSchema();

	// predicate filtering which nodes should be labelled 
	private static Predicate labelPredicate = new AndPredicate(ExpressionParser.predicate("(type = 1 or type = 0)"), 
			new OrPredicate(new VisiblePredicate(), new StartVisiblePredicate()));

	private static final boolean FONTFROMDIAGONAL = false;
	
	private static final double MAXFONTHEIGHT = 40.0;

	private static final double MINFONTHEIGHT = 6.0;
	
	private float childMaxTotal;
	private float nodeMaxTotal;

	// currently selected type of count 
	private String countType = NODECOUNT;

	// text document
	private String[] fullText;
	private String[] fullText2;
	int startTile, endTile, maxTiles;

	/** 
	 * Counts are stored per word, not per synset; only non zero counts are stored.
	 */
	HashMap<String, float[]> wordMap;
	HashMap<String, float[]> wordMap2;

	// prefuse controls and actions 

	private FisheyeTreeFilter fisheyeTreeFilter;
	private NodeColorAction nodeColor;

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
		
		String fullTextFile = "texts/" + filename + ".tiled.txt";
		String wordsFile = "texts/" + filename + ".tiled.tagged.cleaned.txt";
		
		// do this on Event processing thread because need this to continue setup
		if (dictionary == null)
			System.err.println("WordNet dictionary not initialized.");
		fullText = LanguageLib.fillWordCountsMap(wordMap = new HashMap<String, float[]>(), wordsFile, fullTextFile, CountMethod.RANK);
		
		// uncomment to subtract second file
		/*fullTextFile = "texts/2008_third_presidential_debate.tiled.txt";
		wordsFile = "texts/2008_third_presidential_debate.tiled.tagged.cleaned.txt";
		
		fullText2 = LanguageLib.fillWordCountsMap(wordMap2 = new HashMap<String, float[]>(), wordsFile, fullTextFile);
		
		Iterator it = wordMap.keySet().iterator();
		
		int size = 0;
		while (it.hasNext()) {
			String key = (String) it.next();
			if (wordMap2.containsKey(key)) {
				float sum = 0;
				float[] counts = wordMap2.get(key);
				size = wordMap.get(key).length;
				
				for (float count : counts) {
					sum += count;
				}
				
				wordMap.get(key)[0] -= sum;
				wordMap2.remove(key);
			}
		}
		
		it = wordMap2.keySet().iterator();
		
		while (it.hasNext()) {
			String key = (String) it.next();
			if (wordMap2.containsKey(key)) {
				float sum = 0;
				float[] counts = wordMap2.get(key);
				
				float[] newCounts = new float[size];
				
				for (float count : counts) {
					sum += count;
				}
				
				newCounts[0] -= sum;
				wordMap.put(key, newCounts);
			}

		}
		*/
		fisheyeDocument.initializeText(fullText);
		fisheyeDocument.getVisualization().run("init");

		// -- set up renderers --

		m_vis.setRendererFactory(createRenderers());
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
		nodeColor = new NodeColorAction("graph.nodes", true);
		ColorAction nodeStrokeColor = new NodeStrokeColorAction("graph.nodes");
		// Color by POS
		// DataColorAction dca = new DataColorAction("graph.nodes", "pos",
		// Constants.NOMINAL, VisualItem.FILLCOLOR);
		// nodeColor.add("type = 1", dca);
		ActionList nodeColorList = new ActionList();
		// nodeColorList.add(dca);
		nodeColorList.add(nodeColor);
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

		CollapsedSubtreeLayout subLayout = new CollapsedSubtreeLayout("graph");

		CompositeTupleSet searchAndFocus = new CompositeTupleSet();
		searchAndFocus.addSet(Visualization.FOCUS_ITEMS, m_vis.getFocusGroup(Visualization.FOCUS_ITEMS));
		searchAndFocus.addSet(Visualization.SEARCH_ITEMS, m_vis.getFocusGroup(Visualization.SEARCH_ITEMS));
		m_vis.addFocusGroup("searchAndFocus", searchAndFocus);

		VisibilityFilter vF = createVisibilityFilter();
		m_vis.putAction("visibility", vF);

		// create the filtering and layout for initial layout (no animation;
		// workaround for problem caused by MutableFisheyeTreeFilter on initial animation.
		ActionList filter = new ActionList();
		filter.add(new ItemAction("graph.nodes") {
			public void process(VisualItem item, double frac) {
				item.setString("label", wrap(item.getString("label"), 30));
				item.setString("gloss", wrap(item.getString("gloss"), 30));
			};
		});
		fisheyeTreeFilter = new FisheyeTreeFilter("graph", "searchAndFocus", 6);

		// recentre and rezoom on reload
		Action resizeAction = new Action() {
			public void run(double frac) {
				// animate reset zoom to fit the data (must run only AFTER layout)
				Rectangle2D bounds = m_vis.getBounds("graph");
				if (bounds.getWidth() == 0)
					return;
				GraphicsLib.expand(bounds, (int) (1 / m_vis.getDisplay(0).getScale()));
				DisplayLib.fitViewToBounds(m_vis.getDisplay(0), bounds, (long) 2000);
			}
		};
		m_vis.putAction("resize", resizeAction);

		// create the filtering and layout
		this.add(fisheyeTreeFilter);
		this.add(vF);
		this.add(treeLayout);
		this.add(new LabelLayout(LABELS));
		this.add(decoratorFonts);
		this.add(lemmaFont);
		this.add(subLayout);
		this.add(recolor);
		m_vis.putAction("layout", this);

		// animated transition
		ActionList animate = new ActionList(500);
		animate.setPacingFunction(new SlowInSlowOutPacer());
		animate.add(new QualityControlAnimator());
		animate.add(new VisibilityAnimator(LABELS));
		animate.add(new VisibilityAnimator("graph"));
		animate.add(new ColorAnimator("graph"));
		animate.add(new RepaintAction());
		m_vis.putAction("animate", animate);
		m_vis.alwaysRunAfter("layout", "animate");

		// add listeners to displays, for "click" and "hover"
		for (int i = 0; i < m_vis.getDisplayCount(); i++) {
			Display display = m_vis.getDisplay(i);
			TreeDepthItemSorter tdis = new TreeDepthItemSorter();
			tdis.addGroup("pathToRoot");
			display.setItemSorter(tdis);
			display.addControlListener(zoomToFitControl = new ZoomToFitControl());
			zoomToFitControl.setZoomOverItem(false);

			display.addControlListener(hoverActionControl = new PathTraceHoverActionControl("repaint"));
			display.addControlListener(highlightTextHAC = new HighlightTextHoverActionControl(null, null, fullText, m_vis, fisheyeDocument.getVisualization()));
			highlightTextHAC.setCountField(CACHECOUNT + NODECOUNT);
			display.addControlListener(panControl = new PanControl(true));
			display.addControlListener(zoomControl = new ZoomControl());
			display.addControlListener(mouseWheelControl = new ControlAdapter() {
				public void itemWheelMoved(VisualItem item, MouseWheelEvent e) {
					if (e.getWheelRotation() < 0)
						item.setDouble("angleFactor", item.getDouble("angleFactor") + 0.1);
					else
						item.setDouble("angleFactor", (item.getDouble("angleFactor") > 0.2 ? item.getDouble("angleFactor") - 0.1 : 0.1));
					m_vis.cancel("layout");
					m_vis.cancel("animate");
					m_vis.run("layout");
				}
			});
			display.addControlListener(displaySenseMouseOverControl = new DisplaySenseMouseOverControl(DocuBurst.filterPane));
		}
	}

	public void addLabels() {
		m_vis.addDecorators(LABELS, "graph.nodes", labelPredicate, LABEL_SCHEMA);
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
		Schema countsSchema = new Schema();
		countsSchema.addColumn(NODECOUNT, float[].class, null);
		countsSchema.addColumn(CHILDCOUNT, float[].class, null);
		countsSchema.addColumn(CACHERANGE, String.class, null);
		countsSchema.addColumn(CACHECOUNT + CHILDCOUNT, float.class, null);
		countsSchema.addColumn(CACHECOUNT + NODECOUNT, float.class, null);
		// if true, the nodes belongs to the tree cut
		countsSchema.addColumn(CUT, boolean.class, false);
		
		// Graph graph = (Graph) m_vis.getGroup(m_group);
		graph.addColumns(countsSchema);
		Tree t = graph.getSpanningTree();
		addCounts(t.getRoot());
		cacheTotals(graph);
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
			start = new float[fullText.length];
		// cycle through tiles, adding to counts for each
		for (int i = 0; i < fullText.length; i++) {
			start[i] += toAdd[i];
		}
		return start;
	}

	public float[] divide(float[] start, float divisor) {
		// cycle through tiles, adding to counts for each
		if (start == null)
			start = new float[fullText.length];
		if (divisor == 1)
			return start;
		for (int i = 0; i < fullText.length; i++) {
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
					}
					// words propagate nodeCount and childCount to their parent
					n.set("nodeCount", sumCounts((float[]) n.get("nodeCount"), (float[]) c.get("nodeCount")));
					n.set("childCount", sumCounts((float[]) n.get("childCount"), (float[]) c.get("childCount")));
				} else {
					addCounts(c);
					// lemmas and senses only propagate childCount to parent
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

	private DefaultRendererFactory createRenderers() {
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

		// for angular rotating of non-curved labels
		DecoratorLabelRenderer decoratorLabelRenderer = new DecoratorLabelRenderer("label", false, MINFONTHEIGHT);
		// decoratorLabelRenderer.setHorizontalAlignment(Constants.LEFT);
		// for arching of labels
		ArcLabelRenderer arcLabelRenderer = new ArcLabelRenderer("label", MINFONTHEIGHT, MAXFONTHEIGHT);
		rf.add("ingroup('labels') and rotation == 0", arcLabelRenderer); // all sector labels
		rf.add("ingroup('labels') and rotation != 0", decoratorLabelRenderer); // all sector labels
		rf.add("type = 4", labelRenderer); // central lemmas
		rf.add("type = 1 or type = 0", shapeRenderer);
		rf.add("type = 2", S2WedgeRenderer);
		rf.add("type = 5", S2WedgeRenderer);
		return rf;
	}

	private VisibilityFilter createVisibilityFilter() {
		VisibilityFilter vF = new VisibilityFilter("graph", DocuBurst.WORDS_PREDICATE) {
			public void run(double frac) {
				// only check visible items: fisheye filter already set
				// everything visible
				Iterator items = m_vis.visibleItems(m_group);
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
					if (omitZeros && !countType.equals(NOCOUNT)) {
						// only check childCount so parents of nodes with counts are always visible
						float total = item.getFloat(CACHECOUNT + CHILDCOUNT);
						if (total == 0) {
							// set DOI to minimum -- only those items (and their children) that 
							// are visible are checked by the fishEyeFilter; since we may have
							// no visible items after the zeros-off step, we set the DOI to 
							// circumvent the first step of the fishEyeFilter and force re-check
							// on any nodes which we turn off.
							// have minimum DOI are checked by fishEyeFilter
							item.setDOI(Constants.MINIMUM_DOI);
							item.setVisible(false);
							item.setEndVisible(false);
						}
					}
				}
			}
		};
		return vF;
	}

	private float getMaxTotal(String type) {
		if (type.equals(CHILDCOUNT))
			return childMaxTotal;
		if (type.equals(NODECOUNT))
			return nodeMaxTotal;
		return 0;
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

	/**
	 * Set node fill colors
	 */
	class NodeColorAction extends ColorAction {

		public NodeColorAction(String group, boolean trackSenseIndex) {
			super(group, VisualItem.FILLCOLOR, ColorLib.rgba(100, 100, 100, 0));
			add("type = 1", new WordCountColorAction(group, VisualItem.FILLCOLOR));
		}

		class SenseColorAction extends ColorAction {

			public SenseColorAction(String group, String field) {
				super(group, field);
			}

			public int getColor(VisualItem item) {
				if (item.getInt("searchDepth") == 1)
					return ColorScheme.searchColor;

				if (item.canGetInt("senseIndex"))
					return ColorScheme.sensesPalette[item.getInt("senseIndex") % ColorScheme.sensesPalette.length];
				else
					return ColorScheme.resultsColor;
			}
		}

		class WordCountColorAction extends ColorAction {
			SenseColorAction sca;

			public WordCountColorAction(String group, String field) {
				super(group, field);
				sca = new SenseColorAction(group, VisualItem.FILLCOLOR);
			}

			public int getColor(VisualItem item) {
				// test 
				if (item.getBoolean("cut")){
					return new Color(222,13,107).getRGB();
				}
				
				int color = sca.getColor(item);
				
				// lemmas and senses in the pathToRoot
				if (item.isInGroup("pathToRoot") && ((item.getInt("type") == 1) || (item.getInt("type") == 4)))
					color = ColorScheme.pathToRootColor;

				// count off -- just return color
				if (countType.equals(NOCOUNT))
					return color;

				float total = (float) (item.getFloat(CACHECOUNT + countType));
				float maxTotal = getMaxTotal(countType);

				if (total < 0) color = ColorScheme.negativeColor;
				
				if (total == 0) {
					if (color != ColorScheme.pathToRootColor)
						// zero senses get grey, zero lemmas and words get clear
						return ColorLib.setAlpha(ColorScheme.zeroOccurrenceSenseColor, ColorScheme.zeroAlpha);
					else
						// modulate path to root color 
						return ColorLib.setAlpha(color, ColorScheme.zeroAlpha);
				}

				// initial alpha for zero and one count
				int alpha = ColorScheme.zeroAlpha;

				alpha += (int) (((float) total / (float) maxTotal) * (float) (255 - alpha));

				if (total < 0)
					System.err.println("total: " + total + " node: " + item.getString("label") + " alpha: " + alpha);

				// this should not occur except for rounding errors
				if (alpha > 255)
					alpha = 255;

				return ColorLib.setAlpha(color, alpha);
			}
		}
	} // end of inner class NodeColorAction

	/**
	 * Set node fill colors
	 */
	class NodeStrokeColorAction extends ColorAction {

		public NodeStrokeColorAction(String group) {
			// default is invisible stroke (for word and lemma nodes)
			super(group, VisualItem.STROKECOLOR, ColorLib.gray(0, 0));
			add("ingroup('highlight')", ColorScheme.highlightColor);
			add("_hover", ColorScheme.hoverColor);
			add("type = 1 or ingroup('_search_')", new WordCountColorAction(group, VisualItem.STROKECOLOR));
			// if it's a sense node, run the count-based action
		}

		class SenseColorAction extends ColorAction {

			public SenseColorAction(String group, String field) {
				super(group, field);
			}

			public int getColor(VisualItem item) {
				// search results always pink, alpha changes based on count
				if (item.getInt("searchDepth") == 1)
					return ColorScheme.searchColor;

				if (item.canGetInt("senseIndex"))
					return ColorScheme.sensesPalette[item.getInt("senseIndex") % ColorScheme.sensesPalette.length];
				else
					return ColorScheme.zeroOccurrenceSenseColor;
			}
		}

		class WordCountColorAction extends ColorAction {
			SenseColorAction sca;

			public WordCountColorAction(String group, String field) {
				super(group, field);
				sca = new SenseColorAction(group, VisualItem.FILLCOLOR);
			}

			public int getColor(VisualItem item) {
				int color = ColorLib.darker(item.getFillColor());//ColorLib.darker(sca.getColor(item));
				int alpha = ColorScheme.zeroAlpha + 10;

				// return search color if match; don't modulate based on occurrence or omit due to size
				if (item.getInt("searchDepth") == 1)
					return ColorScheme.searchColor;

				// for small nodes, omit border
				if (item.getDouble("angleExtent") < 2)
					alpha = 0;

				// if we aren't counting, just return border colour
				if (countType.equals(NOCOUNT))
					return color;

				float maxTotal = getMaxTotal(countType);
				//float total = (float) Math.log(item.getFloat(CACHECOUNT + countType));
				float total = (float) (item.getFloat(CACHECOUNT + countType));

				// no search results, return senseColor, with 0 alpha if < 2 degrees
				if (total == 0)
					return ColorLib.setAlpha(ColorLib.darker(sca.getColor(item)), alpha);

				// if we are have a single sense and count is non-zero, set color to results colour
				// otherwise keep it as fill color 
				if (color == ColorScheme.zeroOccurrenceSenseColor)
					color = ColorScheme.resultsColor;

				// base border alpha on count; alpha starts darker than for fill
				alpha = 80;
				alpha += (int) ((total / maxTotal) * (float) (255 - alpha));

				if (alpha > 255)
					alpha = 255;

				if (item.getDouble("angleExtent") < 2)
					alpha = 0;
				return ColorLib.setAlpha(color, alpha);
			}

		}
	} // end of inner class NodeColorAction

	class PathTraceHoverActionControl extends HoverActionControl {
		Predicate p;

		public PathTraceHoverActionControl(String action) {
			super(action);
			p = ExpressionParser.predicate("type = 2");
		}

		/**
		 * Add all nodes between hover node and root to pathToRoot focus group
		 * 
		 * @param item the item under hover
		 * @param e the mouse event for itemEntered
		 */
		public void itemEntered(VisualItem item, MouseEvent e) {
			if (item instanceof NodeItem) {
				TupleSet pathToRoot = item.getVisualization().getFocusGroup("pathToRoot");
				pathToRoot.clear();
				traceToRoot((Node) item, pathToRoot, p);

				super.itemEntered(item, e);
			}
		}

		/**
		 * Remove all nodes between hover node and root to pathToRoot focus group
		 * 
		 * @param item the item under hover
		 * @param e the mouse event for itemEntered
		 */
		public void itemExited(VisualItem item, MouseEvent e) {
			if (item instanceof NodeItem) {
				TupleSet pathToRoot = item.getVisualization().getFocusGroup("pathToRoot");
				pathToRoot.clear();
				super.itemExited(item, e);
			}
		}

		/**
		 * Trace from node to root along edges in which node is target (child).
		 * Also add word nodes for all sense nodes along the way.
		 * 
		 * @param n
		 *            the starting node to trace up from
		 * @param pathToRoot
		 *            the tuple set to add the nodes to (usually a focus group)
		 * @param p
		 *            predicate to filter non child edges on
		 */
		public void traceToRoot(Node n, TupleSet pathToRoot, Predicate p) {
			if (n != null) {
				pathToRoot.addTuple(n);
				Iterator edges = n.edges();
				while (edges.hasNext()) {
					Edge edge = (Edge) edges.next();
					// add edges where current node is child (n is target)
					if (edge.getTargetNode() == n) {
						pathToRoot.addTuple(n);
						pathToRoot.addTuple(edge);
						traceToRoot(edge.getSourceNode(), pathToRoot, p);
					} else {
						// add "word" nodes along the way (n is source)
						if (p.getBoolean(edge)) {
							pathToRoot.addTuple(edge.getTargetNode());
						}
					}
				}
			}
		}
	}

	class StarburstScaleFontAction extends FontAction {
		public StarburstScaleFontAction(String labels) {
			super(labels);
		}

		public double getArcHeight(VisualItem item) {
			// the outer-inner distance between rings minus 2 for borders
			if (item.getDouble("angleExtent") == 360)
				return 2 * (item.getDouble("outerRadius") - item.getDouble("innerRadius") - 4);
			else
				return (item.getDouble("outerRadius") - item.getDouble("innerRadius") - 4);

		}

		public double getArcWidth(VisualItem item) {
			// the chord length between two points at midpoint of circle
			double R = (item.getDouble("outerRadius") + item.getDouble("innerRadius")) / 2;
			if (item.getDouble("innerRadius") == 0)
				return 2 * R; // render across middle of circle
			else
				return R * Math.toRadians(item.getDouble("angleExtent")); // length along arc
		}

		public double getDiagonal(Rectangle2D bounds) {
			// set font based on diagonal not width to make more even around the circle
			return Math.sqrt(bounds.getWidth() * bounds.getHeight());
		}

		public Font getFont(VisualItem item) {
			if (FONTFROMDIAGONAL)
				return getFontDiagonal(item);
			else
				return getFontPrecise(item);
		}

		public Font getFontDiagonal(VisualItem item) {
			DecoratorItem dItem = (DecoratorItem) item;
			Font currentFont = (Font) item.getSchema().getDefault(VisualItem.FONT);
			FontMetrics fm = LabelRenderer.DEFAULT_GRAPHICS.getFontMetrics(currentFont);
			// too bigDouble("innerRadius")

			while (((fm.stringWidth(item.getString("label")) > getDiagonal(dItem.getDecoratedItem().getBounds())) || (fm.getHeight() > getArcHeight(dItem)))
					&& (currentFont.getSize() > 0)) {
				currentFont = FontLib.getFont(currentFont.getFontName(), currentFont.getStyle(), currentFont.getSize() - 1);
				fm = LabelRenderer.DEFAULT_GRAPHICS.getFontMetrics(currentFont);
			}
			while ((fm.stringWidth(item.getString("label")) < getDiagonal(dItem.getDecoratedItem().getBounds())) && (fm.getHeight() < getArcHeight(dItem))) {
				Font testFont = FontLib.getFont(currentFont.getFontName(), currentFont.getStyle(), currentFont.getSize() + 1);
				FontMetrics fmTest = LabelRenderer.DEFAULT_GRAPHICS.getFontMetrics(testFont);
				if (fmTest.stringWidth(item.getString("label")) < getDiagonal(dItem.getDecoratedItem().getBounds()) * 0.75) {
					currentFont = testFont;
					fm = LabelRenderer.DEFAULT_GRAPHICS.getFontMetrics(currentFont);
				} else {
					break;
				}
			}
			dItem.setFont(currentFont);
			return currentFont;
		}

		public Font getFontPrecise(VisualItem item) {
			DecoratorItem dItem = (DecoratorItem) item;
			Font currentFont = (Font) item.getSchema().getDefault(VisualItem.FONT);
			FontMetrics fm = LabelRenderer.DEFAULT_GRAPHICS.getFontMetrics(currentFont);

			if (item.getDouble("rotation") != 0) {
				// scale based on string width and difference between arc inner and out radii
				double scaleFactor = getArcHeight(dItem.getDecoratedItem()) / fm.stringWidth(dItem.getString("label"));
				// ensure scaled height doesn't exceed median arc width
				if (fm.getHeight() * scaleFactor > getArcWidth(dItem))
					scaleFactor = getArcWidth(dItem) / fm.getHeight();
				currentFont = FontLib.getFont(currentFont.getFontName(), currentFont.getStyle(), Math.min(currentFont.getSize() * scaleFactor, MAXFONTHEIGHT));
			} else {
				// scale based on string height and difference between arc inner and out radii
				double scaleFactor = getArcHeight(dItem.getDecoratedItem()) / fm.getHeight();
				// ensure scaled height doesn't exceed median arc width
				if (fm.stringWidth(dItem.getString("label")) * scaleFactor > getArcWidth(dItem))
					scaleFactor = getArcWidth(dItem) / fm.stringWidth(dItem.getString("label"));
				// scale is later refined by the renderer
				currentFont = FontLib.getFont(currentFont.getFontName(), currentFont.getStyle(), Math.min(currentFont.getSize() * scaleFactor, MAXFONTHEIGHT));
			}

			return currentFont;
		}
	}
}