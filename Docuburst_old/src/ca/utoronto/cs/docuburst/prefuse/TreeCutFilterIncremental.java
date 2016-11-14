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
import ca.uoit.science.vialab.treecut.ITreeCutNode;
import ca.uoit.science.vialab.treecut.LiAbe;
import ca.uoit.science.vialab.treecut.MDLTreeCut;
import ca.uoit.science.vialab.treecut.Wagner;
import ca.utoronto.cs.docuburst.data.treecut.DocuburstTreeCut;
import ca.utoronto.cs.docuburst.util.Util;

public class TreeCutFilterIncremental extends GroupAction {
    
    private double distanceFromOrigin;
    // TODO: remove this, it doesn't serve any purpose anymore
    private double distanceFromLast;
    
    private double nextDelta;
    
    private String sources;
    private Predicate group;
    
    List<ITreeCutNode> lastCut = new ArrayList<ITreeCutNode>();
    List<Node> lastPrefuseCut  = new ArrayList<Node>();
    
    /**
    * @param group the data group to process. This should resolve to
    * a Graph instance, otherwise exceptions will result when this
    * Action is run.
    **/
    public TreeCutFilterIncremental(String group, String sources) {
        super(group);
        this.distanceFromLast   = 0;
        this.distanceFromOrigin = 0;
        this.nextDelta      = 0;
        this.sources  = sources;
        this.group    = new InGroupPredicate(
                PrefuseLib.getGroupName(group, Graph.NODES));
    }
    
    /**
     * Increments distance by one step.
     */
    public void incDistance(){
        nextDelta++;
    }
    
    /**
     * Decrements distance by one step.
     */
    public void decDistance(){
        if (nextDelta + distanceFromOrigin > 0)
            nextDelta--;
    }
    
    
    private void consolidateIncrement(){
    	distanceFromLast   += nextDelta;
        distanceFromOrigin += nextDelta;
        nextDelta = 0;
    }
    
    
    public void updateDistance(int delta){
        if (distanceFromOrigin + delta < 0)
            return;
        
        nextDelta += delta;
        
    }
    
    @Override
    public void run(double frac) {
        
//    	// TODO: CACHE THE SPANNING TREE?
//    	System.out.println("Retrieving spanning tree...");
//        Tree tree = ((Graph)m_vis.getGroup(m_group)).getSpanningTree();
//        System.out.println("Spanning tree ready...");
//        Node root = tree.getRoot();
//        float s = Util.sum((float[]) root.get("childCount")); // sample size
//        
//        double w = distanceFromLast * 500;
//        
//        // the first tree cut uses Li and Abe's default
//        LiAbe measure = new LiAbe();
//        
//        // dcTreeCut is a bridge between the canonical representation
//        // of the tree cut and the Prefuse classes
//        DocuburstTreeCut dcTreeCutter = new DocuburstTreeCut(measure);
//        
//        
//        // mark current visible items as invisible and non-expanded
//        Iterator items = m_vis.visibleItems(m_group);
//        while ( items.hasNext() ) {
//            VisualItem item = (VisualItem)items.next();
//            PrefuseLib.updateVisible(item, false);
//            item.setExpanded(false);
//        }
//        
//        /*
//         * Find the tree cut
//         */
//        
//        MDLTreeCut mdlTreeCutter = new MDLTreeCut();
//        
//        // There are two representations for the same cut.
//        // The cut package uses ITreeCutNode, while the rest of the application uses prefuse.data.Node
//        List<ITreeCutNode> cut = null;
//        List<Node>  prefuseCut = null;
//        
//        if (distanceFromOrigin + nextDelta == 0){
//
//            ITreeCutNode r = dcTreeCutter.generateAdaptedTree(root);
//            
//            cut        = mdlTreeCutter.findcut(r, s, measure);
//            prefuseCut = dcTreeCutter.extractPrefuseNodes(cut);
//            
//        } else {
//            
////            // TODO: Check this out. It will break. It will cause an infinite loop at some point.
////            int inc = 0;
////            while (cut == null){
////                cut = mdlTreeCutter.getBestSubcut(lastCut, (int)nextDelta + inc + lastCut.size(), null, s);
////                inc++;
////            }
////            nextDelta += inc;
//        	
////            int i = 0;
////        	cut = lastCut;
////            mdlTreeCutter = new MDLTreeCut(new Wagner(1, s));
////        	while (i < 20){
////        		cut = mdlTreeCutter.bestDeeperCut(cut, s);
////            	i++;
////        	}
//            
//            int i =0;
//            cut = lastCut;
//            mdlTreeCutter = new MDLTreeCut(new Wagner(1, s));
//            
//            while (i<10){
//                cut = mdlTreeCutter.previousBestCut(cut, s);
//                i++;
//            }
//            
//            consolidateIncrement();
//            prefuseCut = dcTreeCutter.extractPrefuseNodes(cut);
//        }
//        
//        /*
//         * Mark all descendants of cut members as invisible and
//         * all ancestors as visible.
//         */
//
//        // unmark all items belonging to the last cut
//        for (Node n : lastPrefuseCut) {
//            n.setBoolean("cut", false);
//        }
//        
//        // mark all items belonging to the new cut
//        for (Node n : prefuseCut) {
//            n.setBoolean("cut", true);
//        }
//        
//        markNode((NodeItem)root, true);
//
//        lastCut = cut;
//        lastPrefuseCut = prefuseCut;
//        System.out.println(String.format("Tree cut has %s nodes", prefuseCut.size()));
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
        if (!isVisible)
            n.setDOI(-Constants.MINIMUM_DOI);
        
        // if n is visible and not member of the cut, its children will be visible too
        boolean isChildrenVisible = isVisible && !n.getBoolean("cut");
        
        n.setExpanded(isChildrenVisible);
        
        for (Iterator iterator = n.children(); iterator.hasNext();) {
            markNode((NodeItem) iterator.next(), isChildrenVisible);
            
        }
    }
}
