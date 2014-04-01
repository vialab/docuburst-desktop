package ca.utoronto.cs.prefuseextensions.control;

import java.awt.event.MouseEvent;

import prefuse.controls.ControlAdapter;
import prefuse.util.ui.UILib;
import prefuse.visual.AggregateItem;
import prefuse.visual.VisualItem;

public class AggregateAssignmentControl extends ControlAdapter {
	AggregateItem activeAggregate;
	String action;

	/** Add this as a focus group to put updated items in if you want to track them with a TupleSetListener */
	public static final String UPDATE_CLUSTER = "update_cluster";
	
	public AggregateAssignmentControl(String action) {
		super();
		this.action = action;
	}
			
	@Override
	public void itemClicked(VisualItem item, MouseEvent e) {
		if (UILib.isButtonPressed(e, ControlAdapter.RIGHT_MOUSE_BUTTON)) {
			if (item instanceof AggregateItem) {
				if (activeAggregate != null) { 
					activeAggregate.setHighlighted(false);
					if (activeAggregate == item) {
						activeAggregate = null;
					} else { 
						// deactivate active aggregate
						activeAggregate = (AggregateItem) item;
						activeAggregate.setHighlighted(true);
					}
				} else {
					// activate a new aggregate
					activeAggregate = (AggregateItem) item;
					activeAggregate.setHighlighted(true);
				}
			} else {
				if (activeAggregate == null) return;
				if (activeAggregate.containsItem(item)) {
					activeAggregate.removeItem(item);
				} else {
					activeAggregate.addItem(item);
				}
				// indicate the item has been updated
				if (item.getVisualization().getFocusGroup(UPDATE_CLUSTER) != null) {
					item.getVisualization().getFocusGroup(UPDATE_CLUSTER).addTuple(item);
				}
			}		
			if (action != null) item.getVisualization().run(action);
		}
	}
	
	public AggregateItem getActiveAggregate() {
		return activeAggregate;
	}
}
