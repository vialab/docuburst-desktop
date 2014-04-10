package ca.utoronto.cs.docuburst.data.treecut;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class TreeCutTest {
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
	LiAbe liAbe = new LiAbe();
	MDLTreeCut treeCutter = new MDLTreeCut(); 
	
	@Test
	/**
	 * Comparing the values of Description length with Table 4 of Li & Abe (1998).
	 */
	public void testDl() {
    	
        List<TreeCutNode> cut1 = Arrays.asList(new TreeCutNode[]{ANIMAL});
        List<TreeCutNode> cut2 = Arrays.asList(new TreeCutNode[]{BIRD, INSECT});
        List<TreeCutNode> cut3 = Arrays.asList(new TreeCutNode[]{BIRD, bug, bee, insect});
        List<TreeCutNode> cut4 = Arrays.asList(new TreeCutNode[]{swallow, crow, eagle, bird, INSECT});
        List<TreeCutNode> cut5 = Arrays.asList(new TreeCutNode[]{swallow, crow, eagle, bird, bug, bee, insect});
        
        
        assertEquals(28.07, liAbe.dl(cut1, 10), 0.01);
        assertEquals(28.05, liAbe.dl(cut2, 10), 0.01);
        assertEquals(28.20, liAbe.dl(cut3, 10), 0.01);
        assertEquals(29.03, liAbe.dl(cut4, 10), 0.01);
        assertEquals(29.19, liAbe.dl(cut5, 10), 0.01);
	}
	
	@Test
	/**
	 * Tests if the code finds the best cut with the input described
	 * in Li and Abe (1998).
	 */
	public void testFindCut(){
		ANIMAL.addChild(BIRD);
		BIRD.addChildren(Arrays.asList(new TreeCutNode[]{swallow, crow, eagle, bird}));
		ANIMAL.addChild(INSECT);
		INSECT.addChildren(Arrays.asList(new TreeCutNode[]{bug, bee, insect}));
		
		List<TreeCutNode> bestCut = Arrays.asList(new TreeCutNode[]{BIRD, INSECT});
		
		
		assertEquals(bestCut, treeCutter.findcut(ANIMAL, 10, liAbe));
	}

}
