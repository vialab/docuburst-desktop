package prefuse.action.filter;

import java.util.Iterator;
import java.util.logging.Logger;

import prefuse.Constants;
import prefuse.Visualization;
import prefuse.action.GroupAction;
import prefuse.data.Graph;
import prefuse.data.Tree;
import prefuse.data.expression.Predicate;
import prefuse.data.expression.parser.ExpressionParser;
import prefuse.util.PrefuseLib;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.InGroupPredicate;

/**
 * <p>Filter Action that computes a fisheye degree-of-interest function over
 * a tree structure (or the spanning tree of a graph structure). Visibility
 * and DOI (degree-of-interest) values are set for the nodes in the
 * structure. This function includes current focus nodes, and includes 
 * neighbors only in a limited window around these foci. The size of this
 * window is determined by the distance value set for this action. All
 * ancestors of a focus up to the root of the tree are considered foci as well.
 * By convention, DOI values start at zero for focus nodes, with decreasing
 * negative numbers for each hop away from a focus.</p>
 * 
 * <p>This form of filtering was described by George Furnas as early as 1981.
 * For more information about Furnas' fisheye view calculation and DOI values,
 * take a look at G.W. Furnas, "The FISHEYE View: A New Look at Structured 
 * Files," Bell Laboratories Tech. Report, Murray Hill, New Jersey, 1981. 
 * Available online at <a href="http://citeseer.nj.nec.com/furnas81fisheye.html">
 * http://citeseer.nj.nec.com/furnas81fisheye.html</a>.</p>
 *  
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class FisheyeTreeFilter extends GroupAction {

    protected String m_sources;
    protected Predicate m_groupP;
    
    protected int m_threshold;
    
    protected NodeItem m_root;
    protected double m_divisor;
    
    /**
     * Create a new FisheyeTreeFilter that processes the given group.
     * @param group the data group to process. This should resolve to
     * a Graph instance, otherwise exceptions will result when this
     * Action is run.
     */
    public FisheyeTreeFilter(String group) {
        this(group, 1);
    }

    /**
     * Create a new FisheyeTreeFilter that processes the given group.
     * @param group the data group to process. This should resolve to
     * a Graph instance, otherwise exceptions will result when this
     * Action is run.
     * @param distance the graph distance threshold from high-interest
     * nodes past which nodes will not be visible nor expanded.
     */
    public FisheyeTreeFilter(String group, int distance) {
        this(group, Visualization.FOCUS_ITEMS, distance);
    }
    
    /**
     * Create a new FisheyeTreeFilter that processes the given group.
     * @param group the data group to process. This should resolve to
     * a Graph instance, otherwise exceptions will result when this
     * Action is run.
     * @param sources the group to use as source nodes, representing the
     * nodes of highest degree-of-interest.
     * @param distance the graph distance threshold from high-interest
     * nodes past which nodes will not be visible nor expanded.
     */
    public FisheyeTreeFilter(String group, String sources, int distance)
    {
        super(group);
        m_sources = sources;
        m_threshold = -distance;
        m_groupP = new InGroupPredicate(
                PrefuseLib.getGroupName(group, Graph.NODES));
    }
    
    /**
     * Get the graph distance threshold used by this filter. This
     * is the threshold for high-interest nodes, past which nodes will
     * not be visible nor expanded.
     * @return the graph distance threshold
     */
    public int getDistance() {
        return -m_threshold;
    }

    /**
     * Set the graph distance threshold used by this filter. This
     * is the threshold for high-interest nodes, past which nodes will
     * not be visible nor expanded.
     * @param distance the graph distance threshold to use
     */
    public void setDistance(int distance) {
        m_threshold = -distance;
    }
    
    /**
     * Get the name of the group to use as source nodes for measuring
     * graph distance. These form the roots from which the graph distance
     * is measured.
     * @return the source data group
     */
    public String getSources() {
        return m_sources;
    }
    
    /**
     * Set the name of the group to use as source nodes for measuring
     * graph distance. These form the roots from which the graph distance
     * is measured.
     * @param sources the source data group
     */
    public void setSources(String sources) {
        m_sources = sources;
    }
    
    /**
     * @see prefuse.action.GroupAction#run(double)
     */
    public void run(double frac) {
    	long t1 = System.currentTimeMillis();
        Tree tree = ((Graph)m_vis.getGroup(m_group)).getSpanningTree();
        m_divisor = tree.getNodeCount();
        //System.out.println("Tree node count: " + m_divisor); // debug
        m_root = (NodeItem)tree.getRoot();
        
        // mark visible items as non-expanded and minimum interest
        Iterator items = m_vis.visibleItems(m_group);
        while ( items.hasNext() ) {
            VisualItem item = (VisualItem)items.next();
            item.setDOI(Constants.MINIMUM_DOI);
            item.setExpanded(false);
        }
        
        // compute the fisheye over nodes
        Iterator iter = m_vis.items(m_sources, m_groupP);
        while ( iter.hasNext() ){
        	NodeItem n = (NodeItem)iter.next();
        	// by default, each of these m_groupP items (usually search) used to be a focus
        	// as a result, we would expose as many of their descendants as depth allowed:
            // visitFocus(n, null);
        	
        	// now we only expose n and its siblings
        	if (visit(n, null, 0, 0)) // set focus DOI to 0
            	visitAncestors(n); // make siblings visible       
        }
        visitFocus(m_root, null);

        // mark unreached items (might include edge or node items)
        items = m_vis.visibleItems(m_group);
        while ( items.hasNext() ) {
            VisualItem item = (VisualItem)items.next();
            if ( item.getDOI() == Constants.MINIMUM_DOI )
                PrefuseLib.updateVisible(item, false);
        }
        long t2 = System.currentTimeMillis();
		Logger.getLogger(this.getClass().getName())
			.info(String.format("FisheyeTreeFilter filtering took %f seconds.", (float)(t2-t1)/1000));
    }
    
    /**
     * Visit a focus node.
     */
    protected void visitFocus(NodeItem n, NodeItem c) {
    	if (n == null)
    		return;
    	// all nodes have minimum DOI in the beginning
        if ( n.getDOI() <= -1 ) {
            visit(n, c, 0, 0); // set focus DOI to 0
            if ( m_threshold < 0 )                 
                visitDescendants(n, c);
            visitAncestors(n); // make siblings visible
        }
    }
    
    /**
     * Visit a specific node, make it visible and update its degree-of-interest.
     */
    protected boolean visit(NodeItem n, NodeItem c, int doi, int ldist) {    	
        PrefuseLib.updateVisible(n, true);
        double localDOI = -ldist / Math.min(1000.0, m_divisor);
        n.setDOI(doi+localDOI);
        
        if ( c != null ) {
            EdgeItem e = (EdgeItem)c.getParentEdge();
        	e.setDOI(c.getDOI());
        	PrefuseLib.updateVisible(e, true);
        }
        
        return true;
    }
    
    /**
     * Visit tree ancestors and their other descendants.
     */
    private void visitAncestors(NodeItem n) {
        if ( n == m_root ) return;
        // this is the reason why Fisheye shows so many more items than tree cut when there's a search
        visitFocus((NodeItem)n.getParent(), n);
    }
    
    /**
     * Traverse (and make visible) tree descendants, except skip.
     */
    private void visitDescendants(NodeItem p, NodeItem skip) {
        int lidx = ( skip == null ? 0 : p.getChildIndex(skip) );
        
        Iterator children = p.children();
        
        p.setExpanded(children.hasNext()); // if a node has children, set it as expanded
        
        // then visit every children setting doi to current DOI - 1
        for ( int i=0; children.hasNext(); ++i ) {
            NodeItem c = (NodeItem)children.next();
            if ( c == skip ) { continue; }
            
            int doi = (int)(p.getDOI()-1);            
            visit(c, c, doi, Math.abs(lidx-i));      
            if ( doi > m_threshold) // stopping condition
                visitDescendants(c, null);   
        }
    }
    
} // end of class FisheyeTreeFilter
