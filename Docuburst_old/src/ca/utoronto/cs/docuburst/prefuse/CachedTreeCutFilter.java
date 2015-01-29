package ca.utoronto.cs.docuburst.prefuse;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import prefuse.Constants;
import prefuse.action.GroupAction;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Tree;
import prefuse.data.expression.Predicate;
import prefuse.util.PrefuseLib;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.InGroupPredicate;
import ca.uoit.science.vialab.treecut.Wagner;
import ca.utoronto.cs.docuburst.data.treecut.DocuburstTreeCut;
import ca.utoronto.cs.docuburst.data.treecut.TreeCutCache;
import ca.utoronto.cs.docuburst.util.Util;

public class CachedTreeCutFilter extends GroupAction {

    private TreeCutCache cache; 
    
    private double weight = Double.NaN;
    
    List<Node> lastCut = new ArrayList<Node>();
    
    /**
    * @param group the data group to process. This should resolve to
    * a Graph instance, otherwise exceptions will result when this
    * Action is run.
    **/
    public CachedTreeCutFilter(String group, TreeCutCache cache) {
        super(group);
    }
    
    @Override
    public void run(double frac) {
        Tree tree = ((Graph)m_vis.getGroup(m_group)).getSpanningTree();
        Node root = tree.getRoot();
        
        // mark current visible items as invisible and non-expanded
        Iterator items = m_vis.visibleItems(m_group);
        while ( items.hasNext() ) {
            VisualItem item = (VisualItem)items.next();
            PrefuseLib.updateVisible(item, false);
//            item.setVisible(false);
            item.setExpanded(false);
        }
        
        // Finds tree cut
        TreeCutCache cache = getTreeCutCache();
        if (Double.isNaN(weight))
            weight = cache.getSortedWeights().get(0);
        List<Node> cut = cache.get(weight);
        
        /*
         * Marks all descendants of cut members as invisible and
         * all ancestors as visible.
         */

        // unmark all items belonging to the last cut
        for (Node n : lastCut) {
            n.setBoolean("cut", false);
        }
        
        // mark all items belonging to the new cut
        for (Node n : cut) {
            n.setBoolean("cut", true);
        }
        
        markNode((NodeItem)root, true);

        lastCut = cut;
        System.out.println(String.format("Tree cut has %s nodes with weight = %s", cut.size(), weight));
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
        
        n.setExpanded(isChildrenVisible);
        
        for (Iterator iterator = n.children(); iterator.hasNext();) {
            markNode((NodeItem) iterator.next(), isChildrenVisible);
            
        }
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
    
    public TreeCutCache getTreeCutCache(){
        if (this.cache == null){
            this.cache = buildTreeCutCache((Graph)m_vis.getGroup(m_group));
        }
        return this.cache;
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
