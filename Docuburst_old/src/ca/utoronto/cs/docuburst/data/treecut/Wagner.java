package ca.utoronto.cs.docuburst.data.treecut;

import java.util.Collection;

/**
 * Implementation of the description length calculation according to 
 * <a href="http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.35.9090">Wagner's<a>
 * paper. 
 * 
 * The only difference compared to Li and Abe is the introduction of a weighting factor.
 * 
 * @author Rafael Veras
 *
 */
public class Wagner extends LiAbe {
	
	double w = 500;
	int s = 0;
	
	public Wagner() {
	}
	
	/**
	 * 
	 * @param w C constant (here named w) in Wagner's paper. Determines
	 * the level of generalization. If 0, {@code w} will be set to a value
	 * so that the weighting factor is equals to 1, equivalent to Li and Abe's
	 * calculation.
	 * @param s Sample size. Number of occurrences in the whole subtree (total count).
	 */
	public Wagner(double w, int s) {
		if (w == 0){
			w = s / (Math.log(s)/Math.log(2)); // |S|/log2|S|
		}
		this.w = w;
		this.s = s;
		
	}
	
	/**
     * Description length. Described in Equation 8 of Wagner (2000).
     * @param cut collection of {@link TreeCutNode}.
     * @param s Sample size. Number of occurrences in the whole subtree (total count). 
     */
    public double dl(Collection<TreeCutNode> cut, int s){
    	System.out.println("DESCRIPTION LENGTH");
    	System.out.println(pdl(cut, s) + w*((Math.log(s)/Math.log(2))/(float)s)*ddl(cut, s));
        return pdl(cut, s) + w*((Math.log(s)/Math.log(2))/(float)s)*ddl(cut, s);
    }
}
