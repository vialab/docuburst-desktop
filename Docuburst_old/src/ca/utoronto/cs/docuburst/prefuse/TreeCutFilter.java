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
import ca.uoit.science.vialab.Wagner;
import ca.utoronto.cs.docuburst.data.treecut.MDLTreeCut;
import ca.utoronto.cs.docuburst.util.Util;

public class TreeCutFilter extends GroupAction {
    
    private double distance;
    
    private String sources;
    private Predicate group;
    
    List<Node> lastCut = new ArrayList<Node>();
    
    /**
    * @param group the data group to process. This should resolve to
    * a Graph instance, otherwise exceptions will result when this
    * Action is run.
    **/
    public TreeCutFilter(String group, String sources) {
        super(group);
        this.distance = 0;
        this.sources  = sources;
        this.group    = new InGroupPredicate(
                PrefuseLib.getGroupName(group, Graph.NODES));
    }
    
    /**
     * Increments distance by one step.
     */
    public void incDistance(){
        distance++;
    }
    
    /**
     * Decrements distance by one step.
     */
    public void decDistance(){
        if (distance > 0)
            distance--;
    }
    
    public void updateDistance(int delta){
        if (delta < 0 && distance == 0)
            return;
        
        distance += delta;
        
    }
    
    @Override
    public void run(double frac) {
        
        Tree tree = ((Graph)m_vis.getGroup(m_group)).getSpanningTree();
        Node root = tree.getRoot();
        float s = Util.sum((float[]) root.get("childCount")); // sample size
        
        double w = distance * 350;
        
        Wagner measure = new Wagner(w, Math.round(s));
        MDLTreeCut treeCutter = new MDLTreeCut(measure);
        
        // mark current visible items as invisible and non-expanded
        Iterator items = m_vis.visibleItems(m_group);
        while ( items.hasNext() ) {
            VisualItem item = (VisualItem)items.next();
            PrefuseLib.updateVisible(item, false);
            item.setExpanded(false);
        }
        
        
        // Finds tree cut
        List<Node> cut = treeCutter.findcut(root);
        
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
        System.out.println(String.format("Tree cut has %s nodes", cut.size()));
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


//// Initialize a queue with children of cut members
//Queue<Node> queue = new LinkedList<Node>();
//for (Node n : cut) {
//  n.setBoolean("cut", true);
//  ((NodeItem)n).setExpanded(false);
//  for (Iterator iterator = n.children(); iterator.hasNext();) {
//      queue.add((Node) iterator.next());
//  }
//}
//
//while (!queue.isEmpty()){
//  Node head = queue.poll();
//  
//  PrefuseLib.updateVisible((NodeItem)head, false);
//  
//  for (Iterator iterator = head.children(); iterator.hasNext();) {
//      queue.add((Node) iterator.next());
//  }
//}