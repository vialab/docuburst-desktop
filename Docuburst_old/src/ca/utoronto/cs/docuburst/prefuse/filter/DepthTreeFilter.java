package ca.utoronto.cs.docuburst.prefuse.filter;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import prefuse.action.GroupAction;
import prefuse.data.Graph;
import prefuse.data.Tree;
import prefuse.data.expression.Predicate;
import prefuse.util.PrefuseLib;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.InGroupPredicate;

/**
 * This Filter Action sets to False the visibility of all
 * nodes beyond a certain distance from the root.
 * @author rafa
 */
public class DepthTreeFilter extends GroupAction {

    private Predicate group;
    private int distance;
    
    /**
     * Create a new instance of {@link DepthTreeFilter} that processes
     * the given group.
     * @param group the  data group to process. This should resolve to
     * a Graph instance,  otherwise exceptions  will result  when this
     * Action is run.
     */
    public DepthTreeFilter(String group) {
       this(group, 1); 
    }
    
    /**
     * Create a new instance of {@link DepthTreeFilter} that processes
     * the given group.
     * @param group  the data group to process. This should resolve to
     * a Graph instance,  otherwise exceptions  will result  when this
     * Action is run.
     * @param distance the distance threshold from the root past which 
     * nodes will not be visible nor expanded.
     */
    public DepthTreeFilter(String group, int distance) {
        super(group);
        this.group = new InGroupPredicate(
                PrefuseLib.getGroupName(group, Graph.NODES));
        this.distance = distance;
    }
    
    @Override
    public void run(double frac) {
        
        Tree tree = ((Graph)m_vis.getGroup(m_group)).getSpanningTree();
        NodeItem root = (NodeItem)tree.getRoot();
        
        // mark current visible items as invisible and non-expanded
        Iterator items = m_vis.visibleItems(m_group);
        while ( items.hasNext() ) {
            VisualItem item = (VisualItem)items.next();
            PrefuseLib.updateVisible(item, false);
            item.setExpanded(false);
        }
        
        // Breadth-First search
        Queue<BFSEntry> q = new LinkedList<DepthTreeFilter.BFSEntry>();
        q.add(new BFSEntry(root, 0));
        
        while (!q.isEmpty()){
            BFSEntry n = q.poll();
            
            if (n.depth > distance)
                break;
            
            // update visibility and expandability as a function of depth
            PrefuseLib.updateVisible(n.node, true);
            n.node.setExpanded(n.depth < distance && n.node.children().hasNext());

            for (Iterator it = n.node.children(); it.hasNext();) {
                NodeItem child = (NodeItem) it.next();
                q.add(new BFSEntry(child, n.depth+1));
            }
        }
    }
    
    public int getDistance() {
        return distance;
    }
    
    public void setDistance(int distance) {
        this.distance = distance;
    }
    
    class BFSEntry{
        public NodeItem node;
        public int depth;
        
        public BFSEntry(NodeItem node, int depth) {
            this.node = node;
            this.depth = depth;
        }
    }

}
