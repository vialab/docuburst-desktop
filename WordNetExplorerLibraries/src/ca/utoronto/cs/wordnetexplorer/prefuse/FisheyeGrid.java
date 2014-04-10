/**
 * Copyright (c) 2004-2006 Regents of the University of California.
 * See "license-prefuse.txt" for licensing terms.
 */
package ca.utoronto.cs.wordnetexplorer.prefuse;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JFrame;

import prefuse.Constants;
import prefuse.Display;
import prefuse.DisplayComponent;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.distortion.Distortion;
import prefuse.action.distortion.FisheyeDistortion;
import prefuse.action.layout.AxisLayout;
import prefuse.action.layout.GridLayout;
import prefuse.action.layout.Layout;
import prefuse.controls.AnchorUpdateControl;
import prefuse.controls.ControlAdapter;
import prefuse.data.Schema;
import prefuse.data.Table;
import prefuse.data.util.Sort;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.LabelRenderer;
import prefuse.render.ShapeRenderer;
import prefuse.util.ColorLib;
import prefuse.visual.VisualItem;

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
public class FisheyeGrid extends DisplayComponent {

    /** The data group name of menu items. */
    public static final String ITEMS = "items";
    /** The label data field for menu items. */
    public static final String LABEL = "label";
    public static final String ROW = "row";
    public static final String COL = "col";
    
    private int maxRow;
    private int maxCol;
    
    /**
     * This schema holds the data representation for internal storage of
     * menu items.
     */
    protected static final Schema ITEM_SCHEMA = new Schema();
    static {
        ITEM_SCHEMA.addColumn(LABEL, String.class);
        ITEM_SCHEMA.addColumn(ROW, int.class);
        ITEM_SCHEMA.addColumn(COL, int.class);
        
    }
    
    private Table m_items = ITEM_SCHEMA.instantiate(); // table of menu items
    
    private double m_maxHeight = 600; // maximum menu height in pixels
    private double m_scale = 7;       // scale parameter for fisheye distortion
    
    /**
     * Create a new, empty FisheyeMenu.
     * @see #addMenuItem(String, javax.swing.Action)
     */
    public FisheyeGrid() {
        super(new Visualization());
        m_vis.addTable(ITEMS, m_items);
        
        // set up the renderer to use
        ShapeRenderer renderer = new ShapeRenderer();
        //renderer.setHorizontalPadding(0);
        //renderer.setVerticalPadding(1);
        //renderer.setHorizontalAlignment(Constants.LEFT);
        m_vis.setRendererFactory(new DefaultRendererFactory(renderer));
        
        // set up this display
        setHighQuality(true);
        setBorder(BorderFactory.createEmptyBorder(10,10,10,5));
        
        // text color function
        // items with the mouse over printed in red, otherwise black
        ColorAction colors = new ColorAction(ITEMS, VisualItem.FILLCOLOR);
        colors.setDefaultColor(ColorLib.gray(0));
        colors.add("hover()", ColorLib.rgb(255,0,0));
        
        
        // initial layout and coloring
        ActionList init = new ActionList();
        init.add(new ScaleLayout(ITEMS));
        AxisLayout xLayout, yLayout;
        init.add(xLayout = new AxisLayout(ITEMS, ROW, Constants.Y_AXIS));
        init.add(yLayout = new AxisLayout(ITEMS, COL, Constants.X_AXIS));
        xLayout.setDataType(Constants.NUMERICAL);
        yLayout.setDataType(Constants.NUMERICAL);
        init.add(colors);
        init.add(new RepaintAction());
        m_vis.putAction("init", init);

        // fisheye distortion based on the current anchor location
        ActionList distort = new ActionList();
        Distortion feye = new FisheyeDistortion(m_scale, m_scale);
        distort.add(feye);
        distort.add(colors);
        distort.add(new RepaintAction());
        m_vis.putAction("distort", distort);
        
        // update the distortion anchor position to be the current
        // location of the mouse pointer
        addControlListener(new AnchorUpdateControl(feye, "distort"));
    }

    /**
     * Adds a menu item to the fisheye menu.
     * @param name the menu label to use
     * @param action the ActionListener to notify when the item is clicked
     * The prefuse VisualItem corresponding to this menu item will
     * be returned by the ActionEvent's getSource() method.
     */
    public void addWord(String word, int row, int col) {
        int tableRow = m_items.addRow();
        m_items.set(tableRow, LABEL, word);
        m_items.set(tableRow, ROW, row);
        m_items.set(tableRow, COL, col);
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
            
        	double w = 0, h = 0;
            Iterator iter = m_vis.getGroup(m_group).tuples(null, new Sort(new String[] {ROW, COL}));
            int curRow = 0;
            while ( iter.hasNext() ) {
                VisualItem item = (VisualItem)iter.next();
                if (item.getInt(ROW) == curRow) {
                	item.setSize(1.0);
                	w += item.getBounds().getWidth();
                } else {
                	h += item.getBounds().getHeight();
                	curRow = item.getInt(ROW);
                }
            } 
            w = w/maxRow;
            double scale = Math.min(m_maxWidth/w, m_maxHeight/h);
            scale = Math.min(scale, 1.0);
            scale = Math.max(0.1, scale);
            
            Insets ins = d.getInsets();
            System.out.println(scale);
            // second pass
            h = ins.top;
            double ih, y=0, x=ins.left;
            iter = m_vis.items();
            while ( iter.hasNext() ) {
                VisualItem item = (VisualItem)iter.next();
                item.setSize(scale); item.setEndSize(scale);
                Rectangle2D b = item.getBounds();
            }
            //    w = Math.max(w, b.getWidth());
            //    ih = b.getHeight();
             //   y = h+(ih/2);
              //  setX(item, null, x);
              //  setY(item, null, y);
              //  h += ih;
           // }
            
            // set the display size to fit text
            //d.setSize((int)Math.round(2*m_scale*w + ins.left + ins.right),
             //         (int)Math.round(h + ins.bottom));
        }
    } // end of inner class VerticalLineLayout
} // end of class FisheyeDocument

