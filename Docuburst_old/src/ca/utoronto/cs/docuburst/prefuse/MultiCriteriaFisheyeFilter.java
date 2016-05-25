package ca.utoronto.cs.docuburst.prefuse;

import prefuse.Visualization;
import prefuse.action.filter.FisheyeTreeFilter;
import prefuse.data.Graph;
import prefuse.data.expression.Predicate;
import prefuse.util.PrefuseLib;
import prefuse.visual.NodeItem;
import prefuse.visual.expression.InGroupPredicate;

/**
 * Extends {@link FisheyeTreeFilter} by allowing multiple predicates determine the
 * visibility of a visual item.
 * @author rafa
 *
 */
public class MultiCriteriaFisheyeFilter extends FisheyeTreeFilter {
	
	Predicate[] predicates = null;
	
    public MultiCriteriaFisheyeFilter(String group) {
        this(group, 1);
    }

    public MultiCriteriaFisheyeFilter(String group, int distance) {
        this(group, Visualization.FOCUS_ITEMS, distance);
    }
    
    public MultiCriteriaFisheyeFilter(String group, String sources, int distance)
    {
        super(group);
        m_sources = sources;
        m_threshold = -distance;
        m_groupP = new InGroupPredicate(
                PrefuseLib.getGroupName(group, Graph.NODES));
    }

    public MultiCriteriaFisheyeFilter(String group, String sources, int distance, Predicate... predicates)
    {
        super(group);
        this.predicates = predicates;
        m_sources = sources;
        m_threshold = -distance;
        m_groupP = new InGroupPredicate(
                PrefuseLib.getGroupName(group, Graph.NODES));
    }
    
    protected boolean matchOtherVisibilityCriteria(NodeItem item){    	
//		return item.getFloat("cacheCountchildCount") > 0 && !wordPredicate.getBoolean(item);
    	for (int i = 0; i < predicates.length; i++) {
			Predicate p = predicates[i];
			if (!p.getBoolean(item))
				return false;
		}
    	return true;
    }
    
    protected void visit(NodeItem n, NodeItem c, int doi, int ldist) {
    	if (matchOtherVisibilityCriteria(n))
    		super.visit(n, c, doi, ldist);
    }
    
    public void setPredicates(Predicate[] predicates){
    	if (predicates != null)
    		this.predicates = predicates;
    }
    
    
}
