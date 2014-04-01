/**
 * Copyright (c) 2004-2006 Regents of the University of California.
 * See "license-prefuse.txt" for licensing terms.
 */
package ca.utoronto.cs.wordnetexplorer.prefuse;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import prefuse.Constants;
import prefuse.Display;
import prefuse.DisplayComponent;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.assignment.FontAction;
import prefuse.action.distortion.Distortion;
import prefuse.action.distortion.FisheyeDistortion;
import prefuse.action.layout.AxisLayout;
import prefuse.action.layout.Layout;
import prefuse.controls.AnchorUpdateControl;
import prefuse.controls.ControlAdapter;
import prefuse.data.Schema;
import prefuse.data.Table;
import prefuse.data.util.Sort;
import prefuse.demos.RadialGraphView.NodeColorAction;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.LabelRenderer;
import prefuse.render.ShapeRenderer;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.visual.VisualItem;
import ca.utoronto.cs.prefuseextensions.lib.Colors;
import ca.utoronto.cs.prefuseextensions.swing.ValueChangedEvent;
import ca.utoronto.cs.prefuseextensions.swing.ValueListener;
import ca.utoronto.cs.wordnetexplorer.data.Document;
import ca.utoronto.cs.wordnetexplorer.data.Section;
import ca.utoronto.cs.wordnetexplorer.data.Word;

/**
 * <p>A prefuse-based implementation of Fisheye Menus, showcasing the use of
 * visual distortion to provide access to a large number of data items
 * without scrolling.</p>
 * 
 * <p>This implementation is inspired by the Fisheye Menu research conducted
 * by Ben Bederson at the University of Maryland. See the
 * <a href="http://www.cs.umd.edu/hcil/fisheyemenu/">Fisheye Menu project
 * web site</a> for more details.</p>
 * 
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
@SuppressWarnings("serial")
public class FisheyeDocument extends DisplayComponent {

	public enum SectionMarker{ FIRST_WORD, ROW_NUMBER };
	
    /** The data group name of menu items. */
    public static final String ITEMS = "items";
    /** The label data field for menu items. */
    public static final String LABEL = "label";
    public static final String ROW = "row";
    public static final String INVERSE_ROW = "inverse row";
    public static final String COL = "col";
    public static final String VALUE = "_fisheyevalue_";
    
    private int maxRow;
    private int maxCol;
    
    private SectionMarker sectionMarker;
    
    /**
     * This schema holds the data representation for internal storage of
     * menu items.
     */
    protected static final Schema ITEM_SCHEMA = new Schema();
    static {
        ITEM_SCHEMA.addColumn(LABEL, String.class);
        ITEM_SCHEMA.addColumn(ROW, int.class);
        ITEM_SCHEMA.addColumn(COL, int.class);
        ITEM_SCHEMA.addColumn(INVERSE_ROW, float.class);
        ITEM_SCHEMA.addColumn(VALUE, int.class, 0);
    }
    
    private Table m_items = ITEM_SCHEMA.instantiate(); // table of menu items
    
    private double m_scale = 2;   // TABLETOP RESET to 3   // scale parameter for fisheye distortion
    
    private ArrayList<ValueListener> listeners = new ArrayList<ValueListener>();
    
    /**
     * Create a default FisheyeDocument with row numbers as section labels.
     */
    public FisheyeDocument() {
    	this(SectionMarker.ROW_NUMBER);
    }
    
    /**
     * Create a new, empty FisheyeMenu.
     * @see #addMenuItem(String, javax.swing.Action)
     */
    public FisheyeDocument(SectionMarker sectionMarker) {
        super(new Visualization());
        m_vis.addTable(ITEMS, m_items);
        this.sectionMarker=sectionMarker;
        
        // set up the renderer to use
        ShapeRenderer renderer = new ShapeRenderer() {
        	@Override
        	protected java.awt.Shape getRawShape(VisualItem item) {
        		double width = 20 * item.getSize();
        		double height = item.getSize() * (double) m_vis.getDisplay(0).getHeight()/ (double) m_vis.getGroup(ITEMS).getTupleCount();
        		
        		return super.rectangle(item.getX()-width/2, item.getY()-height/2, width, height);
        	}
        	
        };//Renderer(LABEL);
        
        //renderer.setHorizontalAlignment(Constants.CENTER);
        //renderer.setHorizontalPadding(0);
        //renderer.setVerticalPadding(1);
        //renderer.setHorizontalAlignment(Constants.LEFT);
        m_vis.setRendererFactory(new DefaultRendererFactory(renderer));
        
        // set up this display
        setHighQuality(true);
        //setBorder(BorderFactory.createEmptyBorder(10,10,10,5));
        
        // text color function
        // items with the mouse over printed in red, otherwise black
        ColorAction colors = new ColorAction(ITEMS, VisualItem.FILLCOLOR);
        colors.setDefaultColor(ColorLib.setAlpha(Colors.getInstance().getColor("GRAY4"), 50));
        DataColorAction nodeColorAction = new DataColorAction(ITEMS, VALUE, Constants.NUMERICAL, VisualItem.FILLCOLOR, ColorLib.getInterpolatedPalette(ColorLib.setAlpha(Colors.getInstance().getColor("ORANGE1"), 100), Colors.getInstance().getColor("ORANGE1")));
        colors.add("ingroup('_focus_')", nodeColorAction);
        
        FontAction font = new FontAction();
        font.add("ingroup('_focus_')", FontLib.getFont("SansSerif",Font.BOLD,10));
        
        // initial layout and coloring
        ActionList init = new ActionList();
        init.add(new ScaleLayout(ITEMS));
        final AxisLayout yLayout;
        init.add(yLayout = new AxisLayout(ITEMS, INVERSE_ROW, Constants.Y_AXIS));
        yLayout.setDataType(Constants.NOMINAL);
        init.add(colors);
        init.add(font);
        init.add(new RepaintAction());
        m_vis.putAction("init", init);

        // fisheye distortion based on the current anchor location
        ActionList distort = new ActionList();
        Distortion feye = new FisheyeDistortion(0, m_scale);
        distort.add(feye);
        distort.add(colors);
        distort.add(font);
        distort.add(new RepaintAction());
        m_vis.putAction("distort", distort);
        
        addComponentListener(new ComponentAdapter() {
        	public void componentResized(ComponentEvent e) {
        		yLayout.setLayoutBounds(((DisplayComponent)e.getSource()).getBounds());
        		m_vis.run("init");
        	}
        });
        
        // update the distortion anchor position to be the current
        // location of the mouse pointer
        addControlListener(new AnchorUpdateControl(feye, "distort"));
        
        addControlListener(new ControlAdapter() {
        	public void itemClicked(VisualItem item, MouseEvent e) {
        		for (ValueListener vl : listeners) {
        			vl.valueChanged(new ValueChangedEvent<Integer>(this, item.getInt(ROW)));
        		}
        	}
        });
    }

    public void initializeText(String [] fullText) {
    	// label sections with the row number
    	if (sectionMarker == SectionMarker.ROW_NUMBER) {
    		for (int i = 0; i < fullText.length; i++) {
        		this.addWord("" + (i + 1), i + 1, 0);
    		}
    	}
    	if (sectionMarker == SectionMarker.FIRST_WORD) {
    		for (int i = 0; i < fullText.length; i++) {
        		StringTokenizer st = new StringTokenizer(fullText[i]);
    			this.addWord(st.nextToken(), i + 1, 0);
    		}
    	}
    }
    
    public void initializeText(Document document) {
    	// label sections with the row number
    	if (sectionMarker == SectionMarker.ROW_NUMBER) {
    		int i = 0;
    		for (Section s : document.sections) {
    			this.addWord("" + (i + 1), i + 1, 0);
    			i++;
    		}
    	}
    	if (sectionMarker == SectionMarker.FIRST_WORD) {
    		int i = 0;
    		for (Section s : document.sections) {
    			Word w = s.getWord(0);
    			this.addWord(w.getWord(), i + 1, 0);
    			i++;
    		}
    	}
    }
    
    
    public void addValueListener(ValueListener v) {
    	listeners.add(v);
    }
    
    /**
     * Adds a menu item to the fisheye menu.
     * @param col the column number
     * @param word the string to associate with this node
     * @param row the row number
     * The prefuse VisualItem corresponding to this menu item will
     * be returned by the ActionEvent's getSource() method.
     */
    public void addWord(String word, int row, int col) {
        int tableRow = m_items.addRow();
        m_items.set(tableRow, LABEL, word);
        m_items.set(tableRow, ROW, row);
        m_items.set(tableRow, COL, col);
        m_items.set(tableRow, INVERSE_ROW, 1.0f/(float)row);
        m_items.set(tableRow, VALUE, 0);
        if (row > maxRow) maxRow = row;
        if (col > maxCol) maxCol = col;
    }
    
    /**
     * Lines up all VisualItems vertically. Also scales the size such that
     * all items fit within the maximum layout size, and updates the
     * Display to the final computed size.
     */
    public class ScaleLayout extends Layout {
        String m_group;
    	
        public ScaleLayout(String group) {
        	m_group = group;
        }
        
        public void run(double frac) {
            Display d = m_vis.getDisplay(0);
        	
            // first pass
            double m_maxHeight = d.getHeight();
            double m_maxWidth = d.getWidth();
            
            Iterator iter = m_vis.getGroup(m_group).tuples(null, new Sort(new String[] {ROW, COL}));
            int curRow = 1;
            double maxW = 0;
            double w = 0, h = 0;
            
            while ( iter.hasNext() ) {
                VisualItem item = (VisualItem)iter.next();
                item.setSize(1.0);
                if (item.getInt(ROW) == curRow) {
                	w += item.getBounds().getWidth();
                } else {
                	// measure widest row
                	if (maxW < w) maxW = w;
                	w = 0;
                	h += item.getBounds().getHeight();
                	curRow = item.getInt(ROW);
                }
            } 
            double scale = Math.min(m_maxWidth/maxW, m_maxHeight/h);
            scale = Math.max(scale, 0.6); // TABLETOP reset to 0.8
            
            iter = m_vis.items();
            while ( iter.hasNext() ) {
                VisualItem item = (VisualItem)iter.next();
                item.setSize(scale); item.setEndSize(scale);
                setX(item, null, m_maxWidth/2);
            }
        }
    } // end of inner class ScaleLayout
} // end of class FisheyeDocument

