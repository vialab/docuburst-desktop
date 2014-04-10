package ca.utoronto.cs.prefuseextensions.layout;

import java.util.Iterator;

import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.data.Schema;
import prefuse.data.tuple.TupleSet;
import prefuse.util.force.DragForce;
import prefuse.util.force.ForceItem;
import prefuse.util.force.ForceSimulator;
import prefuse.util.force.SpringForce;
import prefuse.visual.VisualItem;

public class AnchoredForceDirectedLayout extends ForceDirectedLayout {
	static final String ANCHORITEM = "_anchorItem";

	static final Schema ANCHORITEM_SCHEMA = new Schema();
	static {
		ANCHORITEM_SCHEMA.addColumn(ANCHORITEM, ForceItem.class);
	}

	public AnchoredForceDirectedLayout(String nodes, boolean enforceBounds, float springForce) {
		super(nodes, enforceBounds, false);

		ForceSimulator fsim = new ForceSimulator();
		fsim.addForce(new SpringForce(springForce, 0f));
		fsim.addForce(new DragForce());
		setForceSimulator(fsim);

		m_nodeGroup = nodes;
		m_edgeGroup = null;
	}

	public void clear() {
		//System.err.println("run clear");
		ForceSimulator fsim = getForceSimulator();
		fsim.clear();
		
		// make sure we have force items to work with
		TupleSet t = (TupleSet) m_vis.getGroup(m_nodeGroup);
		
		Iterator iter = m_vis.visibleItems(m_nodeGroup);
		while (iter.hasNext()) {
			VisualItem item = (VisualItem) iter.next();
			if (!item.canGet(ANCHORITEM, ForceItem.class)) break;
			// get force item
			ForceItem fitem = (ForceItem) item.get(FORCEITEM);
			if (fitem != null) {
				fitem.location[0] = (float) item.getEndX();
				fitem.location[1] = (float) item.getEndY();
				fitem.mass = getMassValue(item);
			
				// get spring anchor
				ForceItem aitem = (ForceItem) item.get(ANCHORITEM);
				// only reset if an anchor exists
				if (aitem != null) {
					aitem.location[0] = fitem.location[0];
					aitem.location[1] = fitem.location[1];
				
					fsim.addItem(fitem);
					fsim.addSpring(fitem, aitem, 0);
				}
			}
		}
    }
	
	protected void initSimulator(ForceSimulator fsim) {
		//System.err.println("run init");
		// make sure we have force items to work with
		TupleSet t = (TupleSet) m_vis.getGroup(m_nodeGroup);
		t.addColumns(ANCHORITEM_SCHEMA);
		t.addColumns(FORCEITEM_SCHEMA);
		
		Iterator iter = m_vis.visibleItems(m_nodeGroup);
		while (iter.hasNext()) {
			VisualItem item = (VisualItem) iter.next();
			// get force item
			ForceItem fitem = (ForceItem) item.get(FORCEITEM);
			if (fitem == null) {
				fitem = new ForceItem();
				item.set(FORCEITEM, fitem);
			}
			fitem.location[0] = (float) item.getEndX();
			fitem.location[1] = (float) item.getEndY();
			fitem.mass = getMassValue(item);

			// get spring anchor
			ForceItem aitem = (ForceItem) item.get(ANCHORITEM);
			if (aitem == null) {
				aitem = new ForceItem();
				item.set(ANCHORITEM, aitem);
				aitem.location[0] = fitem.location[0];
				aitem.location[1] = fitem.location[1];
			}

			fsim.addItem(fitem);
			fsim.addSpring(fitem, aitem, 0);
		}
	}
	 
} // end of class AnchoredForceLayout
