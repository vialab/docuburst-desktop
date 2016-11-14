package ca.utoronto.cs.prefuseextensions.render;

import java.awt.Shape;

import prefuse.visual.DecoratorItem;
import prefuse.visual.VisualItem;

public class HandlerRenderer extends SectorRenderer {

	int HANDLER_WIDTH = 5;
	
	public HandlerRenderer() {
	
	}	
	
	
	@Override
	protected Shape getRawShape(VisualItem item) {
		VisualItem decoratedItem = ((DecoratorItem)item).getDecoratedItem();
        double x = decoratedItem.getX();
        if ( Double.isNaN(x) || Double.isInfinite(x) )
            x = 0;
        double y = decoratedItem.getY();
        if ( Double.isNaN(y) || Double.isInfinite(y) )
            y = 0;        
        
        sector2D.setSectorByCenter(x, y, 
                decoratedItem.getDouble(OUTER_RADIUS) + .5d, // compensate for default border overflow (see StrokeAction) 
                decoratedItem.getDouble(OUTER_RADIUS) + HANDLER_WIDTH,
                decoratedItem.getDouble(START_ANGLE), 
                decoratedItem.getDouble(ANGLE_EXTENT));
        
        return sector2D;
	}
	
}
