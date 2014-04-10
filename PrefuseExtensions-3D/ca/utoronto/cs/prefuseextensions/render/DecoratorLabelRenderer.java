/* CVS $Id: DecoratorLabelRenderer.java,v 1.2 2009/08/29 06:15:54 cmcollin Exp $ */
package ca.utoronto.cs.prefuseextensions.render;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;


import prefuse.render.LabelRenderer;
import prefuse.visual.DecoratorItem;
import prefuse.visual.VisualItem;

public class DecoratorLabelRenderer extends RotationLabelRenderer {
    
    boolean restrictToBounds;
    double minimumRenderSize;
    
    public DecoratorLabelRenderer() {
        super();
    }

    /**
     * An extension of RotationLabelRenderer that only displays labels if they fit within
     * node boundaries.  Labels that are too large are omitted.
     * 
     * @param string the label string to try
     * @param restrictToBounds
     */
    public DecoratorLabelRenderer(String string, boolean restrictToBounds, double minimumRenderSize) {
        super(string);
        this.restrictToBounds = restrictToBounds;
        this.minimumRenderSize = minimumRenderSize;
    }
    
    /**
     * An extension of RotationLabelRenderer that only displays labels if they fit within
     * node boundries.  Labels that are too large are ommitted.
     * 
     * @param string the label string to try
     * @param restrictToBounds
     * @param minimumRenderSize do not render text with height lower than this point size
     */
    public DecoratorLabelRenderer(String string, boolean restrictToBounds) {
        super(string);
        this.restrictToBounds = restrictToBounds;
        this.minimumRenderSize = 8.0;
    }

    /**
     * Only render labels that fit within their assigned shape.
     */
    public void render(Graphics2D g, VisualItem item) {
        DecoratorItem dItem = (DecoratorItem) item;
        Rectangle2D itemBounds = dItem.getDecoratedItem().getBounds();
        if (((itemBounds.getWidth() > getRawShape(item).getBounds2D().getWidth()) ||
           (itemBounds.getHeight() > getRawShape(item).getBounds2D().getHeight()) ||
           (!restrictToBounds)) && (g.getFontMetrics(item.getFont()).getHeight() * g.getTransform().getScaleX() > minimumRenderSize))
            super.render(g, item);
    }
}
