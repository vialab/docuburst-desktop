package ca.utoronto.cs.prefuseextensions.layout;

import java.util.Iterator;

import ca.utoronto.cs.prefuseextensions.render.SectorRenderer;
import prefuse.action.layout.Layout;
import prefuse.data.Tuple;
import prefuse.data.tuple.TupleSet;
import prefuse.visual.DecoratorItem;
import prefuse.visual.VisualItem;

public class NodeHandlerLayout extends Layout {

	int HANDLER_WIDTH = 5;
	
	public NodeHandlerLayout(String group) {
		super(group);
	}
	
	@Override
	public void run(double frac) {
		TupleSet items = m_vis.getGroup(m_group);
				
		for (Iterator<Tuple> it = items.tuples(); it.hasNext();){
			DecoratorItem handler = (DecoratorItem)it.next();
			VisualItem node = handler.getDecoratedItem();
			
			handler.setX(node.getX());
			handler.setY(node.getY());
			handler.setDouble(SectorRenderer.INNER_RADIUS, 
					node.getDouble(SectorRenderer.OUTER_RADIUS));
			handler.setDouble(SectorRenderer.OUTER_RADIUS, 
					node.getDouble(SectorRenderer.OUTER_RADIUS) + HANDLER_WIDTH);
			handler.setDouble(SectorRenderer.ANGLE_EXTENT, 
					node.getDouble(SectorRenderer.ANGLE_EXTENT));
			handler.setDouble(SectorRenderer.START_ANGLE, 
					node.getDouble(SectorRenderer.START_ANGLE));
		}
		

	}

}
