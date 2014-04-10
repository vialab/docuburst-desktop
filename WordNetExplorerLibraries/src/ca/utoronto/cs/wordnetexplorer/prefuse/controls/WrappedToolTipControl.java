package ca.utoronto.cs.wordnetexplorer.prefuse.controls;

import java.awt.event.MouseEvent;
import java.util.Vector;

import prefuse.DisplayComponent;
import prefuse.controls.ControlAdapter;
import prefuse.data.expression.Predicate;
import prefuse.visual.VisualItem;


/**
 * Control that enables a tooltip display for items based on mouse hover.  Displays
 * tool tip wrapped at given width using html.  Only applys to items that
 * match the given predicate.
 * 
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class WrappedToolTipControl extends ControlAdapter {

    private String[] label;
    private StringBuilder sbuf;
    private int width;
    private Predicate predicate;
    
    
    /**
     * Create a new ToolTipControl.
     * @param field the field name to use for the tooltip text
     * @param predicate the predicate to match; tooltip will only display for items which match predicate
     * @param width the width of the tooltip, in char
     */
    public WrappedToolTipControl(String field, Predicate predicate, int width) {
        this(new String[] {field}, predicate, width);
    }

    /**
     * Create a new ToolTipControl.
     * @param fields the field names to use for the tooltip text. The
     * values of each field will be concatenated to form the tooltip.
     * @param predicate the predicate to match; tooltip will only display for items which match predicate
     * @param width the width of the tooltip, in char
     */
    public WrappedToolTipControl(String[] fields, Predicate predicate, int width) {
        label = fields;
        if ( fields.length > 1 )
            sbuf = new StringBuilder();
        this.width = width;
        this.predicate = predicate;
    }
    
    /**
     * @see prefuse.controls.Control#itemEntered(prefuse.visual.VisualItem, java.awt.event.MouseEvent)
     */
    public void itemEntered(VisualItem item, MouseEvent e) {
        if (predicate.getBoolean(item)) {
            DisplayComponent d = (DisplayComponent)e.getSource();
            if ( label.length == 1 ) {
                // optimize the simple case
                if ( item.canGetString(label[0]) )
                    d.setToolTipText(wrap(item.getString(label[0]), width));
            } else {
                sbuf.delete(0, sbuf.length());
                for ( int i=0; i<label.length; ++i ) {
                    if ( item.canGetString(label[i]) ) {
                        if ( sbuf.length() > 0 )
                            sbuf.append("; ");
                        sbuf.append(item.getString(label[i]));
                    }
                }
            d.setToolTipText(wrap(sbuf.toString(), width));
            }   
        }
    }
    
    /**
     * @see prefuse.controls.Control#itemExited(prefuse.visual.VisualItem, java.awt.event.MouseEvent)
     */
    public void itemExited(VisualItem item, MouseEvent e) {
        DisplayComponent d = (DisplayComponent)e.getSource();
        d.setToolTipText(null);
    }
    
    public static String wrap(String startString, int width) {
        StringBuilder wrappedBuf = new StringBuilder("<html>");
        String unwrapped = new String(startString);
        int i = unwrapped.indexOf(' ', width);
        while (i != -1) {
            wrappedBuf.append(unwrapped.substring(0,i));
            wrappedBuf.append("<br>");
            unwrapped = unwrapped.substring(i+1);
            i = unwrapped.indexOf(' ', width);
        }
        if (!unwrapped.equals(""))
            wrappedBuf.append(unwrapped);
        wrappedBuf.append("</html>");
        return wrappedBuf.toString();
    }
    
    
} // end of class WrappedToolTipControl
