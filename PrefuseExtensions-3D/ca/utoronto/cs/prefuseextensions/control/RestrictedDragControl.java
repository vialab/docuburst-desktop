package ca.utoronto.cs.prefuseextensions.control;

import java.awt.Cursor;
import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;

import prefuse.Display;
import prefuse.controls.DragControl;
import prefuse.data.event.TableListener;
import prefuse.visual.AggregateItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;


/**
 * Changes a node's location when dragged on screen. Other effects
 * include fixing a node's position when the mouse if over it, and
 * changing the mouse cursor to a hand when the mouse passes over an
 * item.
 *
 * Modified by CMC: can restrict movement in X or Y axis.
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class RestrictedDragControl extends DragControl implements TableListener {

	public enum Axis {X,Y,NONE};
	
    protected Axis axis = Axis.NONE;
    
    private String group;
    
    /**
     * Creates a new drag control that issues repaint requests as an item
     * is dragged.
     */
    public RestrictedDragControl() {
    	super();
    }
    
    /**
     * Creates a new drag control that optionally issues repaint requests
     * as an item is dragged.
     * @param repaint indicates whether or not repaint requests are issued
     *  as drag events occur. This can be set to false if other activities
     *  (for example, a continuously running force simulation) are already
     *  issuing repaint events.
     */
    public RestrictedDragControl(boolean repaint) {
        super(repaint);
    }
    
    /**
     * Creates a new drag control that optionally issues repaint requests
     * as an item is dragged.
     * @param repaint indicates whether or not repaint requests are issued
     *  as drag events occur. This can be set to false if other activities
     *  (for example, a continuously running force simulation) are already
     *  issuing repaint events.
     * @param fixOnMouseOver indicates if object positions should become
     * fixed (made stationary) when the mouse pointer is over an item.
     */
    public RestrictedDragControl(boolean repaint, boolean fixOnMouseOver) {
       super(repaint, fixOnMouseOver);
    }
    
    /**
     * Creates a new drag control that invokes an action upon drag events.
     * @param action the action to run when drag events occur.
     */
    public RestrictedDragControl(String action) {
        super(action);
    }
    
    /**
     * Creates a new drag control that invokes an action upon drag events.
     * @param action the action to run when drag events occur
     * @param fixOnMouseOver indicates if object positions should become
     * fixed (made stationary) when the mouse pointer is over an item.
     */
    public RestrictedDragControl(String action, boolean fixOnMouseOver) {
        super(action, fixOnMouseOver);
    }
    
    public void setRestrictedAxis(Axis a) {
    	axis = a;
    }
  
    public void setGroup(String group) {
    	this.group = group;
    }
    
    /**
     * @see prefuse.controls.Control#itemDragged(prefuse.visual.VisualItem, java.awt.event.MouseEvent)
     */
    @Override
    public void itemDragged(VisualItem item, MouseEvent e) {
        if ((group != null)  && (!item.getVisualization().getGroup(group).containsTuple(item)))
        	return;

    	if (item instanceof AggregateItem) return;
    	if (!SwingUtilities.isLeftMouseButton(e)) return;
        dragged = true;
        Display d = (Display)e.getComponent();
        d.getAbsoluteCoordinate(e.getPoint(), temp);
        double dx = temp.getX()-down.getX();
        double dy = temp.getY()-down.getY();
        double x = item.getX();
        double y = item.getY();

        if (axis != Axis.X) { 
        	item.setStartX(x);  
            item.setX(x+dx);    
            item.setEndX(x+dx);
            item.setValidated(false);
        }
        
        if (axis != Axis.Y) {
            item.setStartY(y);
            item.setY(y+dy);
        	item.setEndY(y+dy);
        }
        
        if ( repaint )
            item.getVisualization().repaint();
        
        down.setLocation(temp);
        if ( action != null )
            d.getVisualization().run(action);
    }
    
    /**
     * @see prefuse.controls.Control#itemEntered(prefuse.visual.VisualItem, java.awt.event.MouseEvent)
     */
    public void itemEntered(VisualItem item, MouseEvent e) {
        if ((group == null) || (item.getVisualization().getGroup(group).containsTuple(item)))
        	super.itemEntered(item, e);
    }
    
    /**
     * @see prefuse.controls.Control#itemExited(prefuse.visual.VisualItem, java.awt.event.MouseEvent)
     */
    public void itemExited(VisualItem item, MouseEvent e) {
    	if ((group == null) || (item.getVisualization().getGroup(group).containsTuple(item)))
    		super.itemExited(item, e);
    } //
    
    /**
     * @see prefuse.controls.Control#itemPressed(prefuse.visual.VisualItem, java.awt.event.MouseEvent)
     */
    public void itemPressed(VisualItem item, MouseEvent e) {
    	if ((group == null) || (item.getVisualization().getGroup(group).containsTuple(item)))
    		super.itemPressed(item, e);
    }
    
    /**
     * @see prefuse.controls.Control#itemReleased(prefuse.visual.VisualItem, java.awt.event.MouseEvent)
     */
    public void itemReleased(VisualItem item, MouseEvent e) {
    	if ((group == null) || (item.getVisualization().getGroup(group).containsTuple(item)))
        	super.itemReleased(item, e);
    }
    
} // end of class DragControl
