package ca.utoronto.cs.docuburst.data.treecut;

import java.util.Collection;
import ca.utoronto.cs.docuburst.data.treecut.TreeCutNode;

/**
 * Implementation of the MDL (Minimum Description Length) model,
 * as described in <a href=http://dl.acm.org/citation.cfm?id=972734>Hang Li and Naoki Abe (1998)</a>.
 * <br>
 * <br>
 * Given a tree, determines a horizontal uneven tree cut, as represented by
 * a list of nodes, that has the best balance between conciseness (abstraction)
 * and goodness of fit to the data (probabilities).  
 * 
 * @author Rafael Veras
 */
public class LiAbe implements DescriptionLength{
    
    /**
     * ^P(C) - probability of a category to occur in the sample. 
     * Equation 12 in Li & Abe (1998).
     * 
     * @param f f(C), the total frequency of words in class C in the sample S
     * @param s |S|, # of words in the sample
     */
    private double pc(double f, int s) {
        return f / s;
    }
    
    /**
     * ^P(n) - probability of a category to occur in the sample, normalized
     * by its number of children.
     * Equation 11 in Li & Abe (1998).
     * 
     * @param f f(C), the total frequency of words in class C in the sample S
     * @param s |S|, # of words in the sample
     * @param c |C|,  # of children of a class (1 for leaves)
     */
    private double pn(double f, int s, int c){
        return pc(f, s) / c;
    }
    
    /**
     * L(S|T,teta) - data description length.
     * Equation 10 in Li & Abe (1998).
     *     
     * @param cut collection of {@link TreeCutNode}
     * @param sampleSize number of occurrences in the whole subtree (total count)
     */
    public double ddl(Collection<TreeCutNode> cut, int sampleSize){
        double ddl = 0;
        
        for (TreeCutNode c : cut) {
            double f = c.getFrequency(); 
            // this is the number of leaves under this class. 1 in case it's leaf
            int nChildren = c.getnBottomLeaves() > 0 ? c.getnBottomLeaves() : 1;
            
            double pn = pn(f, sampleSize, nChildren);
            
            if (pn > 0)
                ddl -= (Math.log(pn)/Math.log(2)) * f;  
        } 
        
        return ddl;
    }
    
    /**
     * L(teta|T) - parameter description length.
     * Equation 9 in Li & Abe (1998).
     * 
     * @param cut collection of {@link TreeCutNode} objects
     * @param sampleSize number of occurrences in the whole subtree (total count)
     */
    public double pdl(Collection<TreeCutNode> cut, int sampleSize){
        // We can use k or k+1. See Appendix A of Li and Abe (1998)
        float k = cut.size() -1;
        
        // TODO: Watch this. The Python code has a try catch block enclosing it.
        return  k * (Math.log(sampleSize)/Math.log(2)) / 2;
    }
    
    /**
     * Description length. The sum of PDL and DDL.
     * @param cut collection of {@link TreeCutNode}
     * @param sampleSize number of occurrences in the whole subtree (total count) 
     */
    public double dl(Collection<TreeCutNode> cut, int sampleSize){
        return ddl(cut, sampleSize) + pdl(cut, sampleSize);
    }
    
}
