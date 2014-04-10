package ca.utoronto.cs.prefuseextensions.action;

import java.util.Iterator;

import prefuse.action.GroupAction;
import prefuse.data.expression.parser.ExpressionParser;
import prefuse.util.PrefuseLib;
import prefuse.visual.AggregateItem;

/** 
 * Set Aggregate to visible only iff at least one member is visible.
 */
public class AggregateVisibilityFilter extends GroupAction {
	
	public AggregateVisibilityFilter(String group) {
		super(group);
	}

	public void run(double frac) {
    	Iterator items = m_vis.items(m_group);
        while ( items.hasNext() ) {
            AggregateItem aitem = (AggregateItem)items.next();
            Iterator visibleItems = aitem.items(ExpressionParser.predicate("VISIBLE()"));
            PrefuseLib.updateVisible(aitem, visibleItems.hasNext());
        }
    }
}
