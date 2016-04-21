package ca.utoronto.cs.docuburst;

import prefuse.Visualization;

public class Param {

	public static String interfaceFont = "Verdana";
	
	// Determines the extent of the depth filter. 
	public static class DepthFilterScope {
		public static String SEARCH_AND_FOCUS = "searchAndFocus";
		public static String FOCUS = Visualization.FOCUS_ITEMS;
	}
	
	// Depth filter approach.
	// Treecut - uneven tree cut models selected with the minimum description 
	// length principle.
	// Fisheye - hides any node with depth beyond the threshold.	
	public enum DepthFilter { TREECUT, FISHEYE };
	
	public enum ScoringFunction { counts };
	
}
