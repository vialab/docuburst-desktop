package ca.utoronto.cs.docuburst.data.treecut;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import prefuse.data.Node;
import ca.uoit.science.vialab.DescriptionLength;
import ca.uoit.science.vialab.ITreeCutNode;
import ca.uoit.science.vialab.LiAbe;
import ca.utoronto.cs.docuburst.util.Util;

/**
 * Given a tree, chooses the best tree cut based
 * on Minimum Description Length. In other words,
 * it minimizes the sum of Parameter Description Length (PDL)
 * and Data Description Length (DDL).
 * 
 * This class adapts the Prefuse tree structure
 * to the structures suitable for calling 
 * {@link ca.uoit.science.vialab.MDLTreeCut}'s methods. In
 * particular, every instance of {@link Node} is wrapped into
 * a {@link TreeCutNode} instance.
 * 
 * @see http://dl.acm.org/citation.cfm?id=972734
 * 
 * @author Rafael Veras
 */
public class MDLTreeCut extends ca.uoit.science.vialab.MDLTreeCut {
    
	
    /**
     * By default, initializes this class with Li & Abe's
     * measure of description length.
     */
    public MDLTreeCut() {
        super(new LiAbe());
    }
    
    /**
     * Initializes this class with a certain measure, to be
     * used in the tree cut calculations.
     * @param measure determines how the description length is
     * be calculated.
     */
    public MDLTreeCut(DescriptionLength measure) {
        super(measure);
    }
    
	/**
     * Given a subtree, finds the tree cut that minimizes the description
     * length. Described in Figure 7 of Li and Abe, 1998.
     * <br><br>
     * Uses the default description length calculation described in the 
     * aforementioned paper.
     * @see http://dl.acm.org/citation.cfm?id=972734
     * @param node root of the subtree
     * @return a list of {@link Node}, representing the best (uneven) horizontal cut of the subtree 
     */
    public List<Node> findcut(Node root){
        
 		float sampleSize = Util.sum((float[]) root.get("childCount"));
 		
        return findcut(root, (int)sampleSize); 
    }
	
	
    /**
     * Given a subtree, finds the tree cut that minimizes the description
     * length. Described in Figure 7 of Li and Abe, 1998.
     * <br><br>
     * Uses the description length calculation passed to this class at construction time.
     * @see http://dl.acm.org/citation.cfm?id=972734
     * @param node root of the subtree
     * @param sampleSize length of the sample (e.g., number of words)
     * @return a list of {@link Node}, representing the best (uneven) horizontal cut of the subtree 
     */
    public List<Node> findcut(Node root, int sampleSize){
        // pass an adapted replica of the tree to the tree cut algorithm
        ITreeCutNode replica = generateAdaptedTree(root);
        List<ITreeCutNode> cut = findcut(replica, sampleSize, measure);
        
        List<Node> relevantCut = new ArrayList<Node>();
        // each node of the replica (TreeCutNode) references a real node (prefuse.data.Node).
        // extract and return a list of the latter
        for (ITreeCutNode c : cut){
        	TreeCutNode tcn = (TreeCutNode)c;
            if (tcn.getRefNode()!=null)
                relevantCut.add(tcn.getRefNode());
        }
        
        return relevantCut; 
    }
    
    
    /**
     * Translates the provided tree into the data structure used by
     * the tree cut algorithm. In particular, for every node,
     * the number of leaves that can be found under it is stored.<br>
 	 *
     * @param root the root of the subtree
     * @return an object {@link TreeCutNode} storing a copy of {@code root}. 
     */
    private TreeCutNode generateAdaptedTree(Node root){
        TreeCutNode adapt = new TreeCutNode();
        
        String name = root.getString("label") + root.getString("pos") + root.getLong("offset");
        double freq = Util.sum((float[])root.get("childCount"));
        
        int nBottomLeaves = 0;
        for (Iterator it = root.children(); it.hasNext();) {
            TreeCutNode c = generateAdaptedTree((Node) it.next());
            adapt.addChild(c);
            // if this child is a leaf, increments the count by one
            if (!c.hasChildren())
            	nBottomLeaves += 1;
            else // otherwise, accumulates the value of the child
            	nBottomLeaves += c.getnBottomLeaves();
        }
        
        adapt.setnBottomLeaves(nBottomLeaves);
        adapt.setRefNode(root);
        adapt.setName(name);
        adapt.setFrequency(freq);
        
        return adapt;
    }

    
}
