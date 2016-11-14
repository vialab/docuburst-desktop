package ca.utoronto.cs.docuburst.prefuse;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import prefuse.Constants;
import prefuse.action.GroupAction;
import prefuse.action.filter.FisheyeTreeFilter;
import prefuse.data.CascadedTable;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;
import prefuse.data.Tree;
import prefuse.data.Tuple;
import prefuse.data.column.Column;
import prefuse.data.event.ProjectionListener;
import prefuse.data.expression.AndPredicate;
import prefuse.data.expression.Predicate;
import prefuse.data.expression.parser.ExpressionParser;
import prefuse.data.util.ColumnProjection;
import prefuse.data.util.TableIterator;
import prefuse.util.PrefuseLib;
import prefuse.util.collections.IntIterator;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.InGroupPredicate;
import prefuse.visual.expression.VisiblePredicate;
import ca.uoit.science.vialab.treecut.Wagner;
import ca.utoronto.cs.docuburst.data.treecut.DocuburstTreeCut;
import ca.utoronto.cs.docuburst.data.treecut.TreeCutCache;
import ca.utoronto.cs.docuburst.util.Util;

public class CachedTreeCutFilter extends MultiCriteriaFisheyeFilter {

    private TreeCutCache cache; 
    
    private double weightDelta = 250;
    
    private double weight = weightDelta;
    
    List<Node> lastCut = new ArrayList<Node>();
    
    Node currentRoot;
    
    /**
    * @param group the data group to process. This should resolve to
    * a Graph instance, otherwise exceptions will result when this
    * Action is run.
    **/
    public CachedTreeCutFilter(String group, String sources, TreeCutCache cache) {
        super(group, sources, 1);
    }
    
    public CachedTreeCutFilter(String group, String sources, int distance, 
    		Predicate... predicates) {
        super(group, sources, distance, predicates);
    }
    
    @Override
    public void setDistance(int distance) {
    	super.setDistance(distance);
    	setWeight(getDistance()*weightDelta);
    }
    
    public void setSources(String sources) {
    	super.setSources(sources);
    	buildTreeCutCache((Graph)m_vis.getGroup(m_group));
    };
    
    @Override
    public void run(double frac) {
    	long t1 = System.currentTimeMillis();
    	
    	final Graph graph = (Graph)m_vis.getGroup(m_group);
        Tree tree = graph.getSpanningTree();
        Node root = tree.getRoot();
                
        // mark current visible items as non-expanded        
        Iterator<?> items = m_vis.visibleItems(m_group);
        while ( items.hasNext() ) {
            VisualItem item = (VisualItem)items.next();
            // Never call updateVisible. Don't know why this operation is SO COSTLY!
            // Instead, use DOI as a visibility flag. Later, call updateVisible only
            // for visible items that have DOI == MINIMUM_DOI.
//            PrefuseLib.updateVisible(item, false);
            item.setDOI(Constants.MINIMUM_DOI); //
            item.setExpanded(false);
            item.setBoolean("cut", false);
            item.setBoolean("agg", false);
        }
        
        long t3 = System.currentTimeMillis();
        float sampleSize = root.getFloat("cacheCountchildCount");
        Wagner measure = new Wagner(weight, sampleSize);
        List<Node> cut = new DocuburstTreeCut(measure).findcut(root);
        long t4 = System.currentTimeMillis();
        Logger.getLogger(this.getClass().getName())
    		.info(String.format("Find cut took %f seconds.", (float)(t4-t3)/1000));        
        
        // Marks all descendants of cut members as invisible and
        // all ancestors as visible.       
                
        // mark all nodes that belong to the new cut
        for (Node n : cut)
        	n.setBoolean("cut", true);                	
        
        markNodeVisible((NodeItem)root);
        
        // make visible items that were queried through search
        Iterator iter = m_vis.items(m_sources, m_groupP);
        while ( iter.hasNext() ){
        	NodeItem n = (NodeItem)iter.next();
        	markExceptional(n);     	
        }
        
        long t5 = System.currentTimeMillis();        
        // mark currently visible unreached items as invisible
        Iterator<VisualItem> visible = m_vis.visibleItems(m_group);
        while ( visible.hasNext() ){
        	VisualItem item = visible.next();
        	if (item.getDOI() == Constants.MINIMUM_DOI)
        		PrefuseLib.updateVisible(item, false);
        }
        long t6 = System.currentTimeMillis();
        Logger.getLogger(this.getClass().getName())
			.info(String.format("Marking invisible took %f seconds.", (float)(t6-t5)/1000));
      
        
        lastCut = cut;
        long t2 = System.currentTimeMillis();
        Logger.getLogger(this.getClass().getName())
        	.info(String.format("Tree cut filtering took %f seconds.", (float)(t2-t1)/1000));
    }
    
    private void setVisible(NodeItem n){
    	PrefuseLib.updateVisible(n, true);
    	n.setDOI(0);
        n.setExpanded(n.children().hasNext());
    }
    
    public void markExceptional(NodeItem n){
    	if (!matchOtherVisibilityCriteria(n))
    		return;
    	    	
    	setVisible(n);
    	
        // climb up hierarchy marking ancestors visible        
    	NodeItem parent = (NodeItem)n.getParent();
    	NodeItem skip   = n; // last parent (has been visited)
    	
    	while (parent != null){
    	    
    		// make siblings visible
    		for (Iterator<NodeItem> it = parent.children(); it.hasNext();) {
				NodeItem c = it.next();
				
				if (c == skip || !matchOtherVisibilityCriteria(c)) continue;
				
				setVisible(c);
			}
    		
    		if (parent.getDOI()==0){ // if parent is already visible, then we can return
    		    // since we have made parent's children visible, parent can no longer be an aggregate
                parent.setBoolean("agg", false);
    			return;
    		}

    		setVisible(parent);
    		
    		skip = parent;
    		parent = (NodeItem)parent.getParent();    		
    	}
    }
    
    /**
     * Marks a node and its children as visible or invisible.
     * The children will be invisible if their parent is invisible, or visible and
     * member of the tree cut.
     * @param n node
     * @param isVisible determines if the node is visible/invisible
     */
    public void markNodeVisible(NodeItem n){
        setVisible(n);
        
        EdgeItem parentEdge = (EdgeItem)n.getParentEdge();
        if (parentEdge != null)
        	PrefuseLib.updateVisible(parentEdge, true);
        
        // if n is member of the cut, its children won't be visible
        boolean onCut = n.getBoolean("cut");
        
        Iterator<NodeItem> children = n.children();
        n.setExpanded(children.hasNext());
        
        // a. count # of valid children
        // b. set visible valid children if they are on or above the cut
        int validChildren = 0;
        while (children.hasNext()) {
        	NodeItem c = children.next();
        	if (matchOtherVisibilityCriteria(c)){
        		validChildren++;
        		if (!onCut)
        			markNodeVisible(c);            
        	}
        }
        
        // if n is on cut and has valid children, then mark it as an aggregate
        if (onCut && validChildren > 0)
        	n.setBoolean("agg", true);
        
    }
    
    public TreeCutCache getTreeCutCache(){
    	return null;
    }
    
    private TreeCutCache buildTreeCutCache(Graph graph){
        Tree tree = graph.getSpanningTree();
        Node root = tree.getRoot();
        float s = Util.sum((float[]) root.get("childCount")); // sample size
        
        Wagner measure = new Wagner(Math.round(s));
        DocuburstTreeCut treeCutter = new DocuburstTreeCut(measure);
        
        TreeCutCache treeCutCache = new TreeCutCache();
        
        // add tree cut for initial weight
        treeCutCache.add(measure.getWeight(), treeCutter.findcut(root));
        
        for (int w=250; w<5000; w+=250){
            measure.setWeight(w);
            treeCutCache.add(w, treeCutter.findcut(root));
        }        
        
        return treeCutCache;
    }
    
    public void setWeight(double weight) {
        this.weight = weight;
    }
    
    public double getWeight() {
        return weight;
    }
    
    public void reset(){
        this.cache = null;
    }
    
    public List<Double> getSortedWeights(){
        return cache.getSortedWeights();
    }
    
}
