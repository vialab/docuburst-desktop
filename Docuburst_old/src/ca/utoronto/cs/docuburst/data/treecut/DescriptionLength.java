package ca.utoronto.cs.docuburst.data.treecut;

import java.util.Collection;

import prefuse.data.Node;


/**
 * Defines the methods that an implementation of the measure
 * Description Length needs to offer. 
 * 
 * @author Rafael Veras
 *
 */
public interface DescriptionLength {
    /**
     * Description length.
     * @param cut collection of {@link Node}
     * @param sampleSize number of occurrences in the whole subtree (total count) 
     */
    public double dl(Collection<TreeCutNode> cut, int sampleSize);

}
