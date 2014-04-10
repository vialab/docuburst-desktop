/* CVS $Id: DepthFontAction.java,v 1.1 2007/11/16 06:38:42 cmcollin Exp $ */
package ca.utoronto.cs.wordnetexplorer.prefuse.action;

import ca.utoronto.cs.wordnetexplorer.utilities.Constants;

import java.awt.Font;
import java.util.Iterator;
import java.util.logging.Logger;

import prefuse.action.assignment.FontAction;
import prefuse.data.Edge;
import prefuse.data.Node;
import prefuse.data.expression.Predicate;
import prefuse.data.expression.parser.ExpressionParser;
import prefuse.util.FontLib;
import prefuse.util.PredicateChain;
import prefuse.visual.VisualItem;

/**
 * 
 * Extension of FontAction to size the given font based on the tree depth
 * of the Node corresponding to the VisualItem.  Current formula is 
 * 18 - 1.5* depth for the font size.
 * 
 * @author Christopher Collins
 * @version Feb 24, 2006 5:17:55 PM
 */

public class DepthFontAction extends FontAction {
    private int maxSize;
    
    public DepthFontAction(String group, Font defaultFont, int maxSize) {
        super(group, defaultFont);
        this.maxSize = maxSize;
    }
 
    /**
     * Return the default font at size 18-1.5*depth of item's node in current
     * spanning tree of graph.
     *  
     * @see prefuse.action.assignment.FontAction#getFont(prefuse.visual.VisualItem)
     */
    public Font getFont(VisualItem item) {
        boolean isHyperonym = false;
        if (super.getFont(item) == defaultFont) {
            int depth = ((Node)item).getDepth();
            if (((Node)item).getInt("type") == Constants.WORD) {
                Node sNode = ((Node)item).getParent();
                if (sNode != null) {
                    Iterator edges = sNode.edges();
                    isHyperonym = true;
                    while (edges.hasNext()) {
                        Edge e = (Edge) edges.next();
                        isHyperonym = isHyperonym && (e.getInt("type") != Constants.HYPONOMY);
                    }
                }
            }
            if ((depth >= 0) && (depth < 12) && (!isHyperonym))  { 
                Font returnFont = FontLib.getFont(defaultFont.getFontName(), maxSize - 1.5*((Node)item).getDepth());
                if (item.isHover()) 
                    return FontLib.getFont("Sans", Font.BOLD, returnFont.getSize()+1);
                if (item.getBoolean("root"))
                    return FontLib.getFont("Verdana", Font.BOLD, returnFont.getSize()+1);
                if (item.isInGroup("pathToRoot"))
                    return FontLib.getFont("Verdana", Font.BOLD, returnFont.getSize());
                if (item.isInGroup("_search_"))
                    return FontLib.getFont("Verdana", returnFont.getSize()+1);
                if (item.isInGroup("_focus_"))
                    return FontLib.getFont("Verdana", Font.BOLD, returnFont.getSize()+2); // changed from 13 for Vivian
                return returnFont;
            }
            if ((depth >= 0) && (depth < 12) && (isHyperonym))  { 
                Font returnFont = FontLib.getFont(defaultFont.getFontName(), maxSize - 12 + 1.5*((Node)item).getDepth());
                if (item.isHover()) 
                    return FontLib.getFont("Sans", Font.BOLD, returnFont.getSize()+1);
                if (item.getBoolean("root"))
                    return FontLib.getFont("Verdana", Font.BOLD, returnFont.getSize()+1);
                if (item.isInGroup("pathToRoot"))
                    return FontLib.getFont("Verdana", Font.BOLD, returnFont.getSize());
                if (item.isInGroup("_search_"))
                    return FontLib.getFont("Verdana", returnFont.getSize()+1);
                if (item.isInGroup("_focus_"))
                    return FontLib.getFont("Verdana", Font.BOLD, returnFont.getSize()+2); // changed from 13 for Vivian
                return returnFont;
            }
            
            return defaultFont;
        } 
        return super.getFont(item);
    }
}
