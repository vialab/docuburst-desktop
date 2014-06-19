package ca.utoronto.cs.docuburst.data.treecut;

import prefuse.data.Node;
import ca.uoit.science.vialab.BasicTreeCutNode;
import ca.uoit.science.vialab.ITreeCutNode;

/**
 * This class encapsulates an instance of {@link prefuse.data.Node},
 * providing the methods defined in {@link ITreeCutNode}, which are
 * necessary for computing the tree cut.
 *
 * @author Rafa
 */

public class TreeCutNode extends BasicTreeCutNode {
   
	private Node refNode;
    
	public TreeCutNode() {
		// TODO Auto-generated constructor stub
	}
	
    public TreeCutNode(String name, double frequency, int nBottomLeaves,
            Node refNode) {
    	super(name, frequency, nBottomLeaves);

    	this.refNode = refNode;
    }
    
    public Node getRefNode() {
        return refNode;
    }

    public void setRefNode(Node refNode) {
        this.refNode = refNode;
    }
        
}
