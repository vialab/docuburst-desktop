/* CVS $Id: TreeDepthItemSorter.java,v 1.1 2008/02/21 07:45:40 cmcollin Exp $ */
package ca.utoronto.cs.prefuseextensions.sort;

import java.util.Vector;

import prefuse.Visualization;
import prefuse.visual.AggregateItem;
import prefuse.visual.DecoratorItem;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;
import prefuse.visual.sort.ItemSorter;

/**
 * ItemSorter that sorts items by tree depths. By default items deeper
 * in the tree are given lower scores, so that parent nodes are drawn
 * on top of child nodes. This ordering can be reversed using the
 * appropriate constructor arguments.
 * 
 * CMC: Allows the specification of groups which will be prioritised (ordered first). 
 * 
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class TreeDepthItemSorter extends ItemSorter {

    protected static final int AGGREGATE = 0;
    protected static final int EDGE      = 1;
    protected static final int ITEM      = 2;
    protected static final int NODE      = 3;
    protected static final int DECORATOR = 4;
    
    private int m_childrenAbove;
    private int m_hover;
    private int m_highlight;
    private int m_depth;
    private int m_groups;
    
    private Vector<String> groups;
    
    /**
     * Create a new TreeDepthItemSorter that orders nodes such that parents
     * are placed above their children.
     */
    public TreeDepthItemSorter() {
        this(false);
    }
    
    /**
     * Create a new TreeDepthItemSorter with the given sort ordering by depth.
     * @param childrenAbove true if children should be ordered above their
     * parents, false if parents should be ordered above their children.
     */
    public TreeDepthItemSorter(boolean childrenAbove) {
        if ( childrenAbove ) {
            m_childrenAbove = 1;
            m_hover = 13;
            m_highlight = 12;
            m_groups = 11;
            m_depth = 14;
        } else {
            m_childrenAbove = -1;
            m_hover = 24;
            m_highlight = 23;
            m_groups = 22;
            m_depth = 12;
        }
        groups = new Vector<String>();
    }
    
    /**
     * Score items similarly to {@link ItemSorter}, but additionally
     * ranks items based on their tree depth.
     * @see prefuse.visual.sort.ItemSorter#score(prefuse.visual.VisualItem)
     */
    public int score(VisualItem item) {
        int type = ITEM;
        if ( item instanceof EdgeItem ) {
            type = EDGE;
        } else if ( item instanceof AggregateItem ) {
            type = AGGREGATE;
        } else if ( item instanceof DecoratorItem ) {
            type = DECORATOR;
        }
        
        int score = (1<<(25+type));
        if ( item instanceof NodeItem ) {
            int depth = ((NodeItem)item).getDepth();
            score += m_childrenAbove*(depth<<m_depth);
        }
        if ( item.isHover() ) {
            score += (1<<m_hover);
        }
        if ( item.isHighlighted() ) {
            score += (1<<m_highlight);
        }
        if ( item.isInGroup(Visualization.FOCUS_ITEMS) ) {
            score += (1<<11);
        }
        if ( item.isInGroup(Visualization.SEARCH_ITEMS) ) {
            score += (1<<10);
        }
        for (String s : groups) {
            if (item.isInGroup(s))
                score += (1<<m_groups);
        }
        
        return score;
//        int score = 0;
//        if ( item.isHover() ) {
//            score += (1<<m_hover);
//        }
//        if ( item.isHighlighted() ) {
//            score += (1<<m_highlight);
//        }
//        if ( item instanceof NodeItem ) {
//            score += (1<<27); // nodes before edges
//            score += m_childrenAbove*(((NodeItem)item).getDepth()<<m_depth);
//        }
//        if ( item.isInGroup(Visualization.FOCUS_ITEMS) ) {
//            score += (1<<11);
//        }
//        if ( item.isInGroup(Visualization.SEARCH_ITEMS) ) {
//            score += (1<<10);
//        }
//        if ( item instanceof DecoratorItem ) {
//            score += (1<<9);
//        }
//        return score;
    }

    public void addGroup(String group) {
        groups.add(group);
    }
    
    public void clearGroups() {
        groups.removeAllElements();
    }
    
} // end of class TreeDepthItemSorter
