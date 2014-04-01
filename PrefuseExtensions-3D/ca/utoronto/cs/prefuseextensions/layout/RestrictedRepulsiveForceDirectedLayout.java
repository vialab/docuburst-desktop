package ca.utoronto.cs.prefuseextensions.layout;

import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.util.force.DragForce;
import prefuse.util.force.ForceSimulator;
import prefuse.util.force.NBodyForce;
import prefuse.util.force.SpringForce;
import prefuse.visual.EdgeItem;


/**
 * Creates a force directed layout where items are generally pushed apart, but held together by variable length
 * springs.  Spring length is dependent on the difference in item's value of YINDEX and the item widths.
 * 
 * @author Christopher Collins
 *
 */
public class RestrictedRepulsiveForceDirectedLayout extends RestrictedForceDirectedLayout {

	public static String YINDEX = new String("y_index");
	public float x_sep = 0f;
	public float y_sep = 0f;

	private AxisInsetsLayout yLayout;

	public RestrictedRepulsiveForceDirectedLayout(String graph) {
		this(graph, true, true);
	}

	public RestrictedRepulsiveForceDirectedLayout(String graph, boolean enforceBounds, boolean dragForce) {
		super(graph, enforceBounds);

		ForceSimulator fSim = new ForceSimulator();
		fSim.addForce(new NBodyForce(-0.8f, 20f, NBodyForce.DEFAULT_THETA));
		fSim.addForce(new SpringForce(5e-5f, 10f));
		if (dragForce) fSim.addForce(new DragForce());

		this.setForceSimulator(fSim);
		yLayout = null;
	}

	public RestrictedRepulsiveForceDirectedLayout(String graph, boolean enforceBounds, boolean dragForce, AxisInsetsLayout y) {
		this (graph, enforceBounds, dragForce);
		yLayout = y;
	}

	public RestrictedRepulsiveForceDirectedLayout(String graph, boolean enforceBounds, boolean dragForce, AxisInsetsLayout y, float x_sep, float y_sep) {
		this (graph, enforceBounds, dragForce);
		yLayout = y;
		this.x_sep = x_sep;
		this.y_sep = y_sep;
	}


	/**
	 * Get the minimum horizontal space between nodes. 
	 * 
	 * @return the minimum horizontal spacing between nodes
	 */
	public float getXSep() {
		return x_sep;
	}


	/**
	 * Get the minimum vertical space between nodes. 
	 * 
	 * @return the minimum vertical spacing between nodes
	 */
	public float getYSep() {
		return y_sep;
	}

	/**
	 * Set the minimum horizonal space between nodes. 
	 * 
	 * @param x_sep the minimum horizontal spacing between nodes
	 */
	public void setXSet(float x_sep) {
		this.x_sep = x_sep;
	}

	/**
	 * Set the minimum vertical space between nodes. 
	 * 
	 * @param x_sep the minimum vertical spacing between nodes
	 */
	public void setYSet(float y_sep) {
		this.y_sep = y_sep;
	}


	/**
	 * Spring length based on level (YINDEX) difference between source and target
	 * and width of each item.
	 * 
	 * @param e
	 *            the edge for which to compute the spring length
	 * @return the spring length for the edge. A return value of -1 means to
	 *         ignore this method and use the global default.
	 */
	protected float getSpringLength(EdgeItem e) {
		// y-factor in the spring length determined by difference in level using axis layout
		/*float levelDifferenceFactor = (Math.abs(e.getSourceItem().getInt(YINDEX) - e
				.getTargetItem().getInt(YINDEX)) * ((yLayout == null) ? y_sep : (yLayout.getRange() == 0) ? 0 : ((float)yLayout.getLayoutBounds().getHeight()/(float) yLayout.getRange())));
		// if there is a difference in level, check to make sure the combined height of the items isn't larger than the axis layout determined separation 
		if (levelDifferenceFactor != 0)
			levelDifferenceFactor = (float) Math.max(levelDifferenceFactor, (e.getSourceItem().getBounds().getHeight()/2 + e.getTargetItem().getBounds().getHeight() / 2));
*/
		return (float)(Math.max(e.getSourceItem().getBounds().getWidth(), e.getSourceItem().getBounds().getHeight())/2 + Math.max(e.getTargetItem().getBounds().getWidth(), e.getTargetItem().getBounds().getHeight())/2); 
		
//		return (float) Math.sqrt(Math.pow(levelDifferenceFactor, 2) + Math.pow((e.getSourceItem().getBounds().getWidth()/2 + e.getTargetItem()
//				.getBounds().getWidth()/2), 2)) + x_sep;
	}
}
