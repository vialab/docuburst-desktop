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
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Tree;
import prefuse.data.expression.Predicate;
import prefuse.util.PrefuseLib;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.InGroupPredicate;
import ca.uoit.science.vialab.treecut.Wagner;
import ca.utoronto.cs.docuburst.data.treecut.DocuburstTreeCut;
import ca.utoronto.cs.docuburst.data.treecut.TreeCutCache;
import ca.utoronto.cs.docuburst.util.Util;

public class CachedTreeCutFilter extends FisheyeTreeFilter {

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
    
    public CachedTreeCutFilter(String group, String sources, int distance) {
        super(group, sources, distance);
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
                
        // mark current visible items as invisible and non-expanded
        Iterator<?> items = m_vis.visibleItems(m_group);
        while ( items.hasNext() ) {
            VisualItem item = (VisualItem)items.next();
//            PrefuseLib.updateVisible(item, false);
            item.setExpanded(false);
        }
        
        // try to find tree cut in cache
        float sampleSize = Util.sum((float[]) root.get("childCount"));
        Wagner measure = new Wagner(weight, sampleSize);
        List<Node> cut = new DocuburstTreeCut(measure).findcut(root);        
                
        // Marks all descendants of cut members as invisible and
        // all ancestors as visible.       
        
        // unmark all items belonging to the last cut
        for (Node n : lastCut) 
        	n.setBoolean("cut", false);
                
        // mark all items belonging to the new cut
        for (Node n : cut) 
        	n.setBoolean("cut", true);        
            
        markNode((NodeItem)root, true);
        
        
        int iterSize = 0;
        Iterator iter = m_vis.items(m_sources, m_groupP);
        while ( iter.hasNext() ){
        	NodeItem n = (NodeItem)iter.next();
        	iterSize++;
        	markExceptional(n);     	
        }
        
        lastCut = cut;
        long t2 = System.currentTimeMillis();
//        System.out.println(String.format("Tree cut has %s nodes with weight = %s", cut.size(), weight));        
        Logger.getLogger(this.getClass().getName())
        	.info(String.format("Tree cut filtering took %f seconds.", (float)(t2-t1)/1000));
    }
    
    public void markExceptional(NodeItem n){
    	PrefuseLib.updateVisible(n, true);
        n.setExpanded(n.children().hasNext());
        
    	NodeItem parent = (NodeItem)n.getParent();
    	boolean alreadyVisible = false;
    	while (parent != null){
    		alreadyVisible = parent.isVisible();
    		PrefuseLib.updateVisible(parent, true);
    		parent.setExpanded(true);
    		
    		for (Iterator<Node> it = parent.children(); it.hasNext();) {
				NodeItem c = (NodeItem)it.next();
				PrefuseLib.updateVisible(c, true);
				if (c.children().hasNext())
					c.setExpanded(true);
			}
    		
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
    public void markNode(NodeItem n, boolean isVisible){
        
        PrefuseLib.updateVisible(n, isVisible);
        
        // if n is visible and not member of the cut, its children will be visible too
        boolean isChildrenVisible = isVisible && !n.getBoolean("cut");
        
//        n.setExpanded(isChildrenVisible);
        n.setExpanded(isVisible);
        
        for (Iterator iterator = n.children(); iterator.hasNext();) {
            markNode((NodeItem) iterator.next(), isChildrenVisible);
            
        }
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
