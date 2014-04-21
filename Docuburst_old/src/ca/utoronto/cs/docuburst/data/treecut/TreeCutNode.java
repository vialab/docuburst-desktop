package ca.utoronto.cs.docuburst.data.treecut;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import prefuse.data.Node;

/**
 * A first-child, right-sibling tree node structure suiting the specific needs
 * of the tree cut calculation.
 * <br><br>
 * It provides only the methods needed by the tree cut algorithm and holds a reference
 * to the "real" node (@link {@link prefuse.data.Node}) it represents.
 * <br><br>
 * This structure is needed because the default structure is not suited for tree cut,
 * since internal nodes can have counts associated to them.
 * @author Rafa
 *
 */
public class TreeCutNode {
    private String name;
    private double frequency;
    private int nBottomLeaves; // number of leaves under this node
    private Node refNode; // the real Node represented by this, if any
    
    private TreeCutNode firstChild = null;
    private TreeCutNode rightSibling = null;
    
    public TreeCutNode(String name, double frequency, int nBottomLeaves,
            Node refNode) {

        this.name = name;
        this.frequency = frequency;
        this.nBottomLeaves = nBottomLeaves;
        this.refNode = refNode;
    }
    
    public TreeCutNode(String name, double frequency, int nBottomLeaves) {
        this.name = name;
        this.frequency = frequency;
        this.nBottomLeaves = nBottomLeaves;
    }
    
    public TreeCutNode() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getFrequency() {
        return frequency;
    }

    public void setFrequency(double frequency) {
        this.frequency = frequency;
    }

    public int getnBottomLeaves() {
        return nBottomLeaves;
    }

    public void setnBottomLeaves(int nBottomLeaves) {
        this.nBottomLeaves = nBottomLeaves;
    }

    public Node getRefNode() {
        return refNode;
    }

    public void setRefNode(Node refNode) {
        this.refNode = refNode;
    }
    
    public TreeCutNode getFirstChild() {
        return firstChild;
    }

    public void setFirstChild(TreeCutNode firstChild) {
        this.firstChild = firstChild;
    }

    public TreeCutNode getRightSibling() {
        return rightSibling;
    }

    public void setRightSibling(TreeCutNode rightSibling) {
        this.rightSibling = rightSibling;
    }

    public void addChild(TreeCutNode node){
        if (firstChild == null){
            firstChild = node;
        }
        else {
            TreeCutNode lastChild = firstChild;
            while (lastChild.getRightSibling() != null) {
                lastChild = lastChild.getRightSibling();
            }
            lastChild.setRightSibling(node);
        }
    }
    
    public void addChildren(List<TreeCutNode> nodes){
        if (firstChild == null){
            firstChild = nodes.get(0);
            nodes = nodes.subList(1, nodes.size());
        }
        
        
        TreeCutNode lastChild = firstChild;
        while (lastChild.getRightSibling() != null) {
            lastChild = lastChild.getRightSibling();
        }
        
        for (TreeCutNode n : nodes) {
            lastChild.setRightSibling(n);
            lastChild = n;
        }
    }
    
    public boolean hasChildren(){
        return firstChild != null;
    }
    
    public List<TreeCutNode> getChildren(){
        List<TreeCutNode> children = new ArrayList<TreeCutNode>();
        TreeCutNode child = firstChild;
        while (child != null) {
            children.add(child);
            child = child.getRightSibling();
        }
        return children;
    }
    
    /**
     * Because this is a first-child, right-sibling implementation,
     * this method is a better alternative to {@link #getChildren()}.
     */
    public Iterator<TreeCutNode> children(){
        return new ChildIterator(this);
    }
    
    @Override
    public String toString() {
    	return name;
    }
    
    class ChildIterator implements Iterator<TreeCutNode>{
        
        private TreeCutNode node;
        private TreeCutNode nextChild;
        
        
        public ChildIterator(TreeCutNode node) {
            this.node = node;
            this.nextChild = node.firstChild;
        }
        
        @Override
        public boolean hasNext() {
            return nextChild != null;
        }

        @Override
        public TreeCutNode next() {
            if (!hasNext()){
                throw new NoSuchElementException();
            }
            
            TreeCutNode next = nextChild;
            nextChild = next.rightSibling;
            
            return next;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
        
    }
}
