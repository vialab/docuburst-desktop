package ca.utoronto.cs.docuburst.data.treecut;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import ca.utoronto.cs.docuburst.util.Util;
import prefuse.data.Node;

/**
 * Given a tree, chooses the best tree cut based
 * on Minimum Description Length. In other words,
 * it minimizes the sum of Parameter Description Length (PDL)
 * and Data Description Length (DDL).
 * 
 * @see http://dl.acm.org/citation.cfm?id=972734
 * 
 * @author Rafael Veras
 */
public class MDLTreeCut {
    
    private DescriptionLength measure;

    /**
     * By default, initializes this class with Li & Abe's
     * measure of description length.
     */
    public MDLTreeCut() {
        this(new LiAbe());
    }
    
    /**
     * Initializes this class with a certain measure, to be
     * used in the tree cut calculations.
     * @param measure determines how the description length is
     * be calculated.
     */
    public MDLTreeCut(DescriptionLength measure) {
        this.measure = measure;
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
        TreeCutNode replica = generateAdaptedTree(root);
        List<TreeCutNode> cut = findcut(replica, sampleSize, measure);
//        List<TreeCutNode> cut = findcut(replica, sampleSize, new Wagner(0, sampleSize));
        
        List<Node> relevantCut = new ArrayList<Node>();
        // each node of the replica (TreeCutNode) references a real node (prefuse.data.Node).
        // extract and return a list of the latter
        for (TreeCutNode c : cut)
            if (c.getRefNode()!=null)
                relevantCut.add(c.getRefNode());
        
        return relevantCut; 
    }
    
    
    /**
     * Given a subtree, finds the tree cut that minimizes the description
     * length. Described in Figure 7 of Li and Abe, 1998.
     * @see http://dl.acm.org/citation.cfm?id=972734
     * @param node root of the subtree
     * @param sampleSize length of the sample (e.g., number of words)
     * @param measure a method to calculate the description length of a cut 
     * @return a list of {@link TreeCutNode}, representing the best (uneven) horizontal cut of the subtree 
     */
    public List<TreeCutNode> findcut(TreeCutNode node, int sampleSize, DescriptionLength measure){
        ArrayList<TreeCutNode> rootCut = new ArrayList<TreeCutNode>();
        rootCut.add(node);

        if (!node.hasChildren()){
            return rootCut;
        }
        else {
            ArrayList<TreeCutNode> childrenCut = new ArrayList<TreeCutNode>();
            
            for (Iterator it = node.children(); it.hasNext();) {
                TreeCutNode child = (TreeCutNode)it.next();
                childrenCut.addAll(findcut(child, sampleSize, measure));
            }
            
            if (measure.dl(rootCut, sampleSize) <= measure.dl(childrenCut, sampleSize)){
                return rootCut;
            }
            else {
                return childrenCut;
            }
        }
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

    public static void main(String[] args){
    	TreeCutNode ANIMAL  = new TreeCutNode("ANIMAL", 10, 7);
    	TreeCutNode BIRD    = new TreeCutNode("BIRD", 8, 4);
    	TreeCutNode bird    = new TreeCutNode("bird", 4, 1);
    	TreeCutNode INSECT  = new TreeCutNode("INSECT", 2, 3);
    	TreeCutNode insect  = new TreeCutNode("insect", 0, 1);
    	TreeCutNode bug     = new TreeCutNode("bug", 0, 1);
    	TreeCutNode bee     = new TreeCutNode("bee", 2, 1);
    	TreeCutNode swallow = new TreeCutNode("swallow", 0, 1);
    	TreeCutNode crow    = new TreeCutNode("crow", 2, 1);
    	TreeCutNode eagle   = new TreeCutNode("eagle", 2, 1);
    	
        List<TreeCutNode> cut1 = Arrays.asList(new TreeCutNode[]{ANIMAL});
        List<TreeCutNode> cut2 = Arrays.asList(new TreeCutNode[]{BIRD, INSECT});
        List<TreeCutNode> cut3 = Arrays.asList(new TreeCutNode[]{BIRD, bug, bee, insect});
        List<TreeCutNode> cut4 = Arrays.asList(new TreeCutNode[]{swallow, crow, eagle, bird, INSECT});
        List<TreeCutNode> cut5 = Arrays.asList(new TreeCutNode[]{swallow, crow, eagle, bird, bug, bee, insect});
        
        
        LiAbe liAbe = new LiAbe();
        for (List<TreeCutNode> c : Arrays.asList(new List[]{cut1, cut2, cut3, cut4, cut5})){
        	System.out.println(liAbe.dl(c, 10));
        }
    }
    
}
