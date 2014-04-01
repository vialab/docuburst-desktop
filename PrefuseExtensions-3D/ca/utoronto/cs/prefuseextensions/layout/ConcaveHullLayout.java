package ca.utoronto.cs.prefuseextensions.layout;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import prefuse.Visualization;
import prefuse.action.ItemAction;
import prefuse.action.layout.Layout;
import prefuse.data.Schema;
import prefuse.data.Tuple;
import prefuse.data.expression.AndPredicate;
import prefuse.data.expression.Predicate;
import prefuse.data.expression.parser.ExpressionParser;
import prefuse.data.tuple.DefaultTupleSet;
import prefuse.data.tuple.TupleSet;
import prefuse.data.util.Sort;
import prefuse.render.AbstractShapeRenderer;
import prefuse.util.ColorLib;
import prefuse.visual.AggregateItem;
import prefuse.visual.AggregateTable;
import prefuse.visual.DecoratorItem;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.InGroupPredicate;
import prefuse.visual.expression.VisiblePredicate;
import ca.utoronto.cs.prefuseextensions.layout.ConcaveHullLayout.Intersection.State;
import ca.utoronto.cs.prefuseextensions.lib.MyGraphicsLib;
import ca.utoronto.cs.prefuseextensions.lib.MyMathLib;
import ca.utoronto.cs.prefuseextensions.render.RotationLabelRenderer;

/**
 * Layout algorithm that computes a convex hull surrounding
 * aggregate items and saves it in the "_polygon" field.
 */
public class ConcaveHullLayout extends Layout {
	
	private enum Direction{N,S,E,W};
	private Direction direction = Direction.S;
	
	/** Merge consecutive edges resulting from edge routing if there is no obstacle blocking merge.*/
	public boolean doMerge = true;

	/** Whether to consider blocking items when discovering nearest neighbours for edge routing. */ 
	public boolean considerBlockingItems = true;
	
	/** The logger for ConcaveHullLayout. */
	private static final Logger LOGGER = Logger.getLogger(ConcaveHullLayout.class.getName());
	
	static { Logger.getLogger(ConcaveHullLayout.class.getName()).setLevel(Level.WARNING); }

	private static final String THRESHOLD = "aggregate_threshold";
	private static final String NEIF = "aggreagate_negativeEdgeInfluenceFactor";
	private static final String NIF = "aggregate_nodeInfluenceFactor";
	private static final String NNIF = "aggreagate_negativeNodeInfluenceFactor";
	private static final String EIF = "aggregate_edgeInfluenceFactor";

	private static final float MAX_LABEL_SIZE = 30f;
	private static final float MIN_LABEL_SIZE = 14f;
	
	// data column for storing the surface points
	private static final String SURFACE = "SURFACE";
	
	public static Schema SCHEMA = new Schema();
	public static final String AGGREGATE_NUMBER = "AGGREGATE";
	
	public static long time = 0;
	public static int renderings = 0;
	
	static {
		SCHEMA.addColumn(VisualItem.POLYGON, float[].class);
        // memberDependentVisibility true means show aggregate if any members visible; false means always hide aggregates
        SCHEMA.addColumn(AGGREGATE_NUMBER, int.class);
        SCHEMA.addColumn(THRESHOLD, double.class);
        SCHEMA.addColumn(NEIF, double.class); // negative edge influence factor 
        SCHEMA.addColumn(NNIF, double.class); // negative node influence factor
        SCHEMA.addColumn(NIF, double.class); // node influence factor
        SCHEMA.addColumn(EIF, double.class); // edge influence factor
        SCHEMA.addColumn(SURFACE, Object.class);
    }

	// data column for aggregate member items which stores their distance to the aggregate centroid 
	private static final String CENTROID_DISTANCE = "CentroidDistance"; 
	
	// data column for aggregate member items which stores their virtual edge set
	private static final String VIRTUAL_EDGES = "VirtualEdges";

	/** The maximum number of passes through all nodes to attempt edge rerouting */
	private static final int MAX_ROUTING_ITERATIONS = 100;
	
	/** The maximum number of passes of marching squares while trying to ensure connectedness */
	private static final int MAX_MARCHING_ITERATIONS = 20;
	
	private static final Predicate VISIBLE_EDGE_PREDICATE = ExpressionParser.predicate("VISIBLE() AND ISEDGE()");
	
	/** The number of smoothing iterations to calculate after initializing the surface. */
	private int smoothingIterations = 0;
	
	private Predicate visibleNodePredicate = ExpressionParser.predicate("VISIBLE() AND ISNODE()");

	private static final Predicate VISIBLE_PREDICATE = ExpressionParser.predicate("VISIBLE()");
	
	String nodeGroup = "graph.nodes";
	
	// skip every N points in the marching squares when making the contour
	private int skip = 10;
	// look at super-pixel groupings of this size
	private int pixelGroup = 6; // 3
	
	/** the energy threshold for marching squares */
	private double threshold = 1;
	/** the radius for the contour around a single edge -- the point at which the energy is 1 */
	private double edgeR0 = 30;
	/** the radius at which potential reaches zero -- the extent of energy contribution by the edge */
    private double edgeR1 = 60; //100
    // (determined by quadratic equation to give the same maximum at distance 0 as the node relationship (15/50)
    // that is: 100^2/(100-30)^2 = 50^2/(50-15)^2
    
    /** the radius for the contour around a single node -- the point at which the energy is 1 */
	private double nodeR0 = 20;//15;
	/** the radius at which potential reaches zero -- the extent of energy contribution by the node */
    private double nodeR1 = 40;//50
    
    /** the distance to morph energy around a node */
    private double morphBuffer = nodeR0;
    /** whether to perform edge routing around intersections */
    private boolean doMorph = true;
    /** whether to connect neighbours left to right or to centroid */
    private boolean centroidSort = true;
    
    private double nodeInfluenceFactor = 1;
    private double edgeInfluenceFactor = 1;
    private double negativeNodeInfluenceFactor = -0.8;
    private double negativeEdgeInfluenceFactor = 0;
    
    /**  whether to have surface follow visible edges contained in aggregate, or use nearest neighbour connections */
    private boolean useStructuralEdges = true;

    private String sourceGroup;

    /** Hull used to store coordinates during smoothing */
    private float[] tempHull = new float[0]; 
    
    /** 
     * The aggregate label layout, if there are labels for the aggregates/
     */
    private AggregateLabelLayout aggregateLabelLayout;
    
    /**
     * Create a concave hull layout for a given group of aggregates and members.
     * 
     * Speedups: energy only calculated for area of possible influence of items in aggregate; only aggregates within
     * area of influence of hover items are recalcuated if there is a hover items; super-pixel sizes are used to calculate energy;
     * some points on the hull are skipped in the rendering.
     * 
     * Note that only recalculating hulls affected by the hover item, if there is one, may cause problems if 
     * items are moving but not in the region of the hover item when there is a hovered item.  When there is no hovered item
     * all hulls are recalculated.
     * 
     * @param aggrGroup the set of aggregates
     * @param sourceGroup the set of visual items which exert an influence on the aggregages (may or may not be members)
     * @param surfaceDistance the minimum distance from the aggregate member's edge to the surface of the hull 
     * @param influenceDistance the maximum influence distance
     * @param threshold the threshold for inclusion in the hull
     * @param nodeInfluenceFactor the positive influence of included nodes
     * @param edgeInfluenceFactor the positive influence of included edges
     * @param negativeNodeInfluenceFactor the negative influence of non-included nodes
     * @param negativeEdgeInfluenceFactor the negative influence of non-included edges
     * @param doMorph whether to perform edge routing around intersections (ignored if using structural edges)
     * @param useStructuralEdges whether to have surface follow visible edges contained in aggregate, or use nearest neighbour connections
     * @param centroidSort if true sort and connect items from centroid out, otherwise from left-right, top-bottom
     */
	public ConcaveHullLayout(String aggrGroup, String sourceGroup, String nodeGroup, double nodeSurfaceDistance, double nodeInfluenceDistance, 
			double edgeSurfaceDistance, double edgeInfluenceDistance,
			double nodeInfluenceFactor, double edgeInfluenceFactor, double negativeNodeInfluenceFactor, 
			double negativeEdgeInfluenceFactor, boolean doMorph, boolean useStructuralEdges, boolean centroidSort) {
        super(aggrGroup);
        this.sourceGroup = sourceGroup;
        this.nodeGroup = nodeGroup;
        visibleNodePredicate = new AndPredicate(new InGroupPredicate(nodeGroup), VisiblePredicate.TRUE);
        this.edgeR0 = edgeSurfaceDistance;
        this.edgeR1 = edgeInfluenceDistance;
        this.nodeR0 = nodeSurfaceDistance;
        this.nodeR1 = nodeInfluenceDistance;
        this.morphBuffer = nodeR0 / 2;
        this.nodeInfluenceFactor = nodeInfluenceFactor;
        this.edgeInfluenceFactor = edgeInfluenceFactor;
        this.negativeEdgeInfluenceFactor = negativeEdgeInfluenceFactor;
        this.negativeNodeInfluenceFactor = negativeNodeInfluenceFactor;
        System.err.println("nnif " + negativeNodeInfluenceFactor);
        this.doMorph = doMorph;
        this.useStructuralEdges = useStructuralEdges;
        this.centroidSort = centroidSort;
    }
    
    public ConcaveHullLayout(String aggrGroup, String sourceGroup) {
        super(aggrGroup);
        this.sourceGroup = sourceGroup;
        this.nodeGroup = sourceGroup;
        visibleNodePredicate = new AndPredicate(new InGroupPredicate(nodeGroup), VisiblePredicate.TRUE);
    }
    
    public void setAggregateLabelLayout(AggregateLabelLayout aggregateLabelLayout) {
    	this.aggregateLabelLayout = aggregateLabelLayout;
    }
    
    /**
     * Set the Visualization processed by this Action.
     * @param vis the {@link prefuse.Visualization} to process.
     */
    @Override
    public void setVisualization(Visualization vis) {
    	m_vis = vis;
        if (aggregateLabelLayout != null) aggregateLabelLayout.setVisualization(vis);
    }
    
    public void setPixelGroup(int pixelGroup) {
    	this.pixelGroup = pixelGroup;
    }
    
    public void setSkip(int skip) {
    	this.skip = skip;
    }
    
    public void setMorphBuffer(double morphBuffer) {
    	this.morphBuffer = morphBuffer;
    }
    
    /** Set the number of smoothing iterations per frame. */
    public void setSmoothing(int smoothingIterations) {
    	this.smoothingIterations = smoothingIterations;
    }
    
    public void setDoMorph(boolean doMorph) {
    	this.doMorph = doMorph;
    }
    
    /**
     * Calculate the concave hull using energy and marching squares with edge routing.
     */
    public void run(double frac) {
    	m_vis.getGroup(sourceGroup).addColumn(CENTROID_DISTANCE, double.class);
    	m_vis.getGroup(sourceGroup).addColumn(VIRTUAL_EDGES, Object.class);
    	
    	AggregateTable aggr = (AggregateTable) m_vis.getGroup(m_group);
        // do we have any  to process?
        int num = aggr.getTupleCount();
        if ( num == 0 ) return;

        // compute and assign convex hull for each visible aggregate
        Iterator aggrs = m_vis.visibleItems(m_group);
        
        if (aggregateLabelLayout != null) {
        	// add their energy to aggregates
        	TupleSet aggregateLabels = m_vis.getGroup(aggregateLabelLayout.getGroup());
        	Iterator labelIterator = aggregateLabels.tuples(VisiblePredicate.TRUE);
        	
        	while(labelIterator.hasNext()) {
        		DecoratorItem di = (DecoratorItem) labelIterator.next();
        		AggregateItem aitem = (AggregateItem) di.getDecoratedItem();
        		if (aitem.getAggregateSize() == 0) continue;
        		// find if any items in the aggregate have moved
        		Iterator itemIterator = aitem.items(VisiblePredicate.TRUE); // would VALIDATED false be better?
        		if (itemIterator.hasNext()) {
        			// something changed, recalculate aggregate
        			run(di, aitem);
        		}
        	}
        	
        	labelIterator = aggregateLabels.tuples(VisiblePredicate.FALSE);
        	
        	while(labelIterator.hasNext()) {
        		DecoratorItem di = (DecoratorItem) labelIterator.next();
        		AggregateItem aitem = (AggregateItem) di.getDecoratedItem();
        		if (aitem.getAggregateSize() == 0) continue;
        		// find if any items in the aggregate have moved
        		Iterator itemIterator = aitem.items(VisiblePredicate.TRUE); // would VALIDATED false be better?
        		if (itemIterator.hasNext()) {
        			// something changed, recalculate aggregate
        			run(aitem);
        		}
        	}
        } else {
        	while ( aggrs.hasNext() ) {
        		AggregateItem aitem = (AggregateItem) aggrs.next();
        		if (aitem.getAggregateSize() == 0) continue;
        
        		// find if any items in the aggregate have moved
        		Iterator itemIterator = aitem.items(VisiblePredicate.TRUE);
        		if (itemIterator.hasNext()) {
        			// something changed, recalculate aggregate
        			run(aitem);
        		} 
        	}
        }
    }
    
    private void run(AggregateItem aitem) {
    	this.run(null, aitem);
    }
    
    /**
     * Perform adjacent point smoothing on the curve.  Geometric drop off. 
     * 
     * http://en.wikipedia.org/wiki/Gaussian_smoothing
     * 
     * @param hull a list of x,y values to be smoothed - x1, y1, x2, y2 etc.   
     * @param iterations
     * @return smoothed coordinated
     */
    private float[] adjacentPointsSmooth(float[] hull, int size, int iterations) {
    	if (tempHull.length < size) {
    		tempHull = new float[size];
    	}
    	System.arraycopy(hull, 0, tempHull, 0, size);
    	
    	for (int j = 0; j < iterations; j++) {
        	
    		// NOTE smoothing first and last points resulted in these points crossing each other
    		// first point
    		//tempHull[0] = (hull[size-2]/2 + hull[0] + hull[2]/2) / 2f;
    		//tempHull[1] = (hull[size-1]/2 + hull[1] + hull[3]/2) / 2f;
   			
    		for (int i = 2; i < (size-2); i++) {
    			tempHull[i] = (hull[i-2]/2 + hull[i] + hull[i+2]/2) / 2f;
    		}
    		
    		// last point
    		//tempHull[size-4] = (hull[size-4]/2 + hull[size-2] + hull[0]/2) / 2f;
    		//tempHull[size-3] = (hull[size-3]/2 + hull[size-1] + hull[1]/2) / 2f;
    		
    		System.arraycopy(tempHull, 0, hull, 0, size);
    	}
    	return hull;
    }
    
    private void run(DecoratorItem di, AggregateItem aitem) {
    	long localtime = System.currentTimeMillis();
    	
    	Rectangle2D bounds = null;
    	VisualItem item = null;
        Iterator aggregateMembersIterator = aitem.items();
        
        // if no aggregate members are visible, do nothing
        if (!aggregateMembersIterator.hasNext()) {
        	return;
        }
        
        // calculate and store virtual edges
        calculateVirtualEdges(aitem);
        
        // cycle through members of aggregate adding to bounds of influence
        while (aggregateMembersIterator.hasNext() ) {
        	item = (VisualItem) aggregateMembersIterator.next();
        	if (bounds == null) {
        		// clone because we don't want to change bounds of items, but we 
        		// need to start with item bounds (not empty bounds because 0,0 may not be
        		// in area of influence
        		bounds = (Rectangle2D) item.getBounds().clone(); 
        	} else { 
        		bounds.add(item.getBounds());
        	} 
        	
        	// add the bounds of the virtual edges to the active area
        	if (item.get(VIRTUAL_EDGES) != null) {
        		Deque<Line2D> virtualEdges = (Deque<Line2D>) item.get(VIRTUAL_EDGES);
        		Iterator<Line2D> lines = virtualEdges.iterator();
        		while (lines.hasNext()) {
        			bounds.add(lines.next().getBounds2D());
        		}
        	}
        }
        
        if (di != null) {
        	// position the label
        	aggregateLabelLayout.process(di, 1.0);
        
        	// add label bounds
        	bounds.add(di.getBounds());
        }
        
        aggregateMembersIterator = null;
        
        // bounds contains a rectangle with all the nodes in the aggregate within it (convex hull)
        // expand bounds by the maximum radius on all sides
        bounds.setRect(bounds.getX()-Math.max(edgeR1, nodeR1)-morphBuffer, bounds.getY()-Math.max(edgeR1, nodeR1)-morphBuffer, 
        		bounds.getWidth()+2*Math.max(edgeR1, nodeR1)+2*morphBuffer, bounds.getHeight()+2*Math.max(edgeR1,nodeR1)+2*morphBuffer);
        double [][] potentialArea = new double[(int)(Math.ceil(bounds.getWidth()/pixelGroup))][(int)(Math.ceil(bounds.getHeight()/pixelGroup))];
        
        // estimate length of contour to be the perimeter of the rectangular aggregate bounds (tested, it's a good approx)
        ArrayList<Point2D>surface = (ArrayList<Point2D>) aitem.get(SURFACE);
        
        if (surface == null) {
        	surface = new ArrayList<Point2D>((int)bounds.getWidth() * 2 + (int)bounds.getHeight() * 2);
        } else {
        	surface.clear();
        }
            
        // store defaults and adjust globals so that changes are visible to calculateSurface method
        double tempThreshold = threshold;
        double tempNegativeNodeInfluenceFactor = negativeNodeInfluenceFactor;
        double tempNegativeEdgeInfluenceFactor = negativeEdgeInfluenceFactor;
        double tempNodeInfluenceFactor = nodeInfluenceFactor;
        double tempEdgeInfluenceFactor = edgeInfluenceFactor;
            
        int iterations = 0;
        
        // add the aggregate and all it's members and virtual edges
        fillPotentialArea(bounds, aitem, potentialArea);
    	// add the label item
        if (di != null) fillPotentialArea(bounds, di, potentialArea);
        
        // try to march, check if surface contains all items
        while ((!calculateSurface(surface, bounds, aitem, potentialArea)) && (iterations < MAX_MARCHING_ITERATIONS)) {
            surface.clear();
            iterations++;
	            
            // reduce negative influences first; this will allow the surface to pass without making it fatter all around (which raising the threshold does)
            if (iterations <= MAX_MARCHING_ITERATIONS / 2) {
            	threshold *= 0.95f;
            	nodeInfluenceFactor *= 1.2;
        		edgeInfluenceFactor *= 1.2;		
        	 	fillPotentialArea(bounds, aitem, potentialArea);
        	}
            	
        	// after half the iterations, start increasing positive energy and lowering the threshold
        	
            if (iterations > MAX_MARCHING_ITERATIONS / 2) {
            	if ((negativeEdgeInfluenceFactor != 0) || (negativeNodeInfluenceFactor != 0)) {
            		threshold *= 0.95f;
            		negativeNodeInfluenceFactor *= 0.8;
            		negativeEdgeInfluenceFactor *= 0.8;
            	 	fillPotentialArea(bounds, aitem, potentialArea);
            	}
            }
        	
            // expand bounds to all items on last iteration in case marching is actually going out of bounds (should not occur)
            /*if (iterations == MAX_MARCHING_ITERATIONS-1) {
            	LOGGER.warning("EXPANDING BOUNDS");
            	Iterator visibleItems = m_vis.getGroup(sourceGroup).tuples(VISIBLE_PREDICATE);
            	while (visibleItems.hasNext()) { 
            		item = (VisualItem) visibleItems.next();
            		bounds.add(item.getBounds());
            	}
            	bounds.setRect(bounds.getX()-Math.max(edgeR1, nodeR1)-morphBuffer, bounds.getY()-Math.max(edgeR1, nodeR1)-morphBuffer, 
                	bounds.getWidth()+2*Math.max(edgeR1, nodeR1)+2*morphBuffer, bounds.getHeight()+2*Math.max(edgeR1,nodeR1)+2*morphBuffer);
                    
            	potentialArea = new double[(int)(Math.ceil(bounds.getWidth()/pixelGroup))][(int)(Math.ceil(bounds.getHeight()/pixelGroup))];
            	fillPotentialArea(bounds, aitem, potentialArea);
            }*/
        }
            
        //System.err.println("threshold: " + threshold);
        //System.err.println(aitem.getInt(AGGREGATE_NUMBER) + " iterations: " + iterations);
            
        // store final attributes for visualizing energy later
        aitem.setDouble(THRESHOLD, threshold);
        aitem.setDouble(NEIF, negativeEdgeInfluenceFactor);
        aitem.setDouble(NNIF, negativeNodeInfluenceFactor);
        aitem.setDouble(NIF, nodeInfluenceFactor);
        aitem.setDouble(EIF, edgeInfluenceFactor);
            
        threshold = tempThreshold;
        negativeEdgeInfluenceFactor = tempNegativeEdgeInfluenceFactor;
        negativeNodeInfluenceFactor = tempNegativeNodeInfluenceFactor;
        nodeInfluenceFactor = tempNodeInfluenceFactor;
        edgeInfluenceFactor = tempEdgeInfluenceFactor;
            
        // finalize the surface by adding bounds to positions and set into the aitem

        // start with global SKIP value, but decrease skip amount if there aren't enough points in the surface
        int thisSkip = skip;
        // prepare viz attribute array
        int size = surface.size();
 		          
        if (thisSkip > 1) {
          	size = surface.size() / thisSkip;
          	// if we reduced too much (fewer than three points in reduced surface) reduce skip and try again
           	while ((size < 3) && (thisSkip > 1)) { 
           		thisSkip--;
           		size = surface.size() / thisSkip;
           	}
        }
        
        size *= 2; // double to store x,y pairs in an array
        float xcorner = (float)bounds.getX();
        float ycorner = (float)bounds.getY();
            
        float[] fhull = (float[]) aitem.get(VisualItem.POLYGON);
        if (fhull == null || fhull.length < size)
            fhull = new float[size];
        else if (fhull.length > size)
            fhull[size+1] = Float.NaN;
            
        // copy hull values
        for (int i=0,j=0; j < size-1; j+=2,i+=thisSkip) {
        	fhull[j] = (float)surface.get(i).getX() + xcorner;
            fhull[j+1] = (float)surface.get(i).getY() + ycorner;
        }
            
        // Use this to see regions of influence
        /*
        fhull = new float[9];
        fhull[0] = (float) bounds.getX();
        fhull[1] = (float) bounds.getY();
        fhull[2] = fhull[0] + (float)bounds.getWidth();
        fhull[3] = fhull[1];
        fhull[4] = fhull[2];
        fhull[5] = fhull[1] + (float)bounds.getHeight();
        fhull[6] = fhull[0];
        fhull[7] = fhull[5];
        fhull[8] = Float.NaN;
        */
        
        fhull = adjacentPointsSmooth(fhull, size, smoothingIterations);

        aitem.set(VisualItem.POLYGON, fhull);
        aitem.set(SURFACE, surface);
        aitem.setValidated(false); // force invalidation
        time+=(System.currentTimeMillis()-localtime);
        renderings++;
    }
    
    /**
     * Fill the surface using marching squares, return true if and only if all items in the given aggregate are contained
     * inside rectangle specified by the extents of the surface.  This does not guarantee the surface will contain all items, but
     * it is a fast approximation.
     * 
     * @param surface the surface to fill
     * @param bounds the bounds of the space being calculated, in screen coordinates
     * @param aitem the aggregate item for which a surface is being calculated
     * @param potentialArea the energy field corresponding to the given aggregate and bounds
     * @return true if and only if marching squares successfully found a surface containing all elements in the aggregate
     */
    public boolean calculateSurface(ArrayList<Point2D> surface, Rectangle2D bounds, AggregateItem aitem, double[][] potentialArea) {
      
    	// find a first point on the contour
        boolean marched = false;
        // set starting direction for conditional states (6 & 9)
        direction = Direction.S;
        for (int x = 0; x < potentialArea.length && !marched; x++) {
        	for (int y = 0; y < potentialArea[x].length && !marched; y++) {
        		if (test(potentialArea[x][y])) {
        			// check invalid state condition
        			if (getState(potentialArea,x,y) != 15) {
        				marched = march(surface, potentialArea, x, y);
        			}
        		}
        	}
        }

        // if no surface could be found stop
        if (!marched) return false;
       
        boolean[] containment = testContainment(surface, bounds, aitem);
    
        return containment[0];
    }

    /**
     * Test containment of items in the bubble set.  
     * @param surface the points on the surface
     * @param bounds the bounds of influence used to calculate the surface
     * @param aitem the aggregate item to test
     * @return an array where the first element indicates if the set contains all required items and the second element indicates if the set contains extra items
     */
    public boolean[] testContainment(ArrayList<Point2D> surface, Rectangle2D bounds, AggregateItem aitem) {
        // precise bounds checking 
        // copy hull values
        Path2D g = new Path2D.Double();
        // start with global SKIP value, but decrease skip amount if there aren't enough points in the surface
        int thisSkip = skip;
        // prepare viz attribute array
        int size = surface.size();
        if (thisSkip > 1) {
        	size = surface.size() / thisSkip;
        	// if we reduced too much (fewer than three points in reduced surface) reduce skip and try again
        	while ((size < 3) && (thisSkip > 1)) { 
        		thisSkip--;
        		size = surface.size() / thisSkip;
        	}
        }
        
        float xcorner = (float)bounds.getX();
        float ycorner = (float)bounds.getY();
        
        // simulate the surface we will eventually draw, using straight segments (approximate, but fast) 
        for ( int i=0; i < size-1; i++) {
        	if (i==0) 
        		g.moveTo((float)surface.get(i*thisSkip).getX()+xcorner, (float)surface.get(i*thisSkip).getY()+ycorner);
        	else
        		g.lineTo((float)surface.get(i*thisSkip).getX()+xcorner, (float)surface.get(i*thisSkip).getY()+ycorner);
        }
		
        g.closePath();
        
        boolean containsAll = true;
        boolean containsExtra = false;
        Iterator items = m_vis.items(visibleNodePredicate);
        while (items.hasNext()) {
        	VisualItem item = (VisualItem) items.next();
			if (aitem.isHover()) item.setHighlighted(false);
        	if (aitem.containsItem(item)) {
        		// check rough bounds
        		containsAll = (containsAll) && (g.getBounds().contains(item.getBounds().getCenterX(), item.getBounds().getCenterY()));
        		// 	check precise bounds if rough passes
        		containsAll = (containsAll) && (g.contains(item.getBounds().getCenterX(), item.getBounds().getCenterY()));
        		
        	} else {
        		// check rough bounds
        		if (g.getBounds().contains(item.getBounds().getCenterX(), item.getBounds().getCenterY())) {
        			// 	check precise bounds if rough passes
        			if (g.contains(item.getBounds().getCenterX(), item.getBounds().getCenterY())) {
        				if (aitem.isHover()) item.setHighlighted(true);
        	        		containsExtra = true;
        			}
        		} 
        	}
        }
    	return new boolean[] {containsAll, containsExtra};
    }
    
    /**
     * Fill the given area with energy, with values modulated by the preset energy function parameters (radial extent, postive
     * and negative influences for included and excluded nodes and edges).
     *  
     * @param activeArea the screen coordinates of the region to fill
     * @param aitem the aggregate item to calculate energy for
     * @param potentialArea the energy field to fill in
     */
    public void fillPotentialArea(Rectangle2D activeArea, AggregateItem aitem, double[][] potentialArea) {
        double influenceFactor = 0;
        
        // add all positive energy (included items) first, as negative energy morphing 
        // requires all positives to be already set

		if (nodeInfluenceFactor != 0) {
			Iterator aNodeItems = aitem.items(visibleNodePredicate);
			while (aNodeItems.hasNext()) {
				VisualItem item = (VisualItem) aNodeItems.next();
				
				// add node energy
				influenceFactor = nodeInfluenceFactor;
				double a = 1/(Math.pow(nodeR0-nodeR1,2));
				calculateRectangleInfluence(potentialArea, a*influenceFactor, nodeR1,
    				new Rectangle2D.Double(item.getBounds().getX()-activeArea.getX(), item.getBounds().getY()-activeArea.getY(), item.getBounds().getWidth(), item.getBounds().getHeight()));
        		
				// add the influence of all the virtual edges
				Deque<Line2D> scannedLines = (Deque<Line2D>) item.get(VIRTUAL_EDGES);
				influenceFactor = edgeInfluenceFactor;
				a = 1/((edgeR0-edgeR1)*(edgeR0-edgeR1));
	    				
				// only count distance from point on surface to nearest segment, not all segments
				if (scannedLines.size() > 0) calculateLinesInfluence(potentialArea, a*influenceFactor, edgeR1, scannedLines, activeArea); 
			} // end processing node items of this aggregate
		} // end processing positive node energy
		
		// calculate positive edge energy
		Iterator aEdgeItems = aitem.items(VISIBLE_EDGE_PREDICATE);
		
		while (aEdgeItems.hasNext()) {
			EdgeItem edge = (EdgeItem) aEdgeItems.next();
        	if (edgeInfluenceFactor != 0) { 
    			influenceFactor = edgeInfluenceFactor;
    			double a = 1/((edgeR0-edgeR1)*(edgeR0-edgeR1));
    			calculateLineInfluence(potentialArea, a*influenceFactor, edgeR1, 
    					new Line2D.Double(edge.getSourceItem().getX()-activeArea.getX(), edge.getSourceItem().getY()-activeArea.getY(), 
    							edge.getTargetItem().getX()-activeArea.getX(), edge.getTargetItem().getY()-activeArea.getY()));
    		}
        }
        
        // calculate negative energy contribution for all other visible items within bounds 
        if ((negativeNodeInfluenceFactor != 0) || (negativeEdgeInfluenceFactor != 0)) {
        	Iterator sourceItems = m_vis.visibleItems(sourceGroup);
            while (sourceItems.hasNext()) {
	        	VisualItem item = (VisualItem) sourceItems.next();
	        	// check for items in the aggregate
	        	if (aitem.containsItem(item)) { 
	        		continue;
	        	} else {
	        		// if item is within influence bounds, add potential
		        	if (activeArea.intersects(item.getBounds())) {
		        		if ((item.isInGroup(nodeGroup)) && (negativeNodeInfluenceFactor != 0)) {
		        			// subtract influence
		        			influenceFactor = negativeNodeInfluenceFactor;
		        			double a = 1/Math.pow(nodeR0-nodeR1, 2);
		        			calculateRectangleInfluence(potentialArea, a*influenceFactor, nodeR1,
		    					new Rectangle2D.Double(item.getBounds().getX()-activeArea.getX(), item.getBounds().getY()-activeArea.getY(), item.getBounds().getWidth(), item.getBounds().getHeight()));
		        		}
		        		// subtract edges normally
		        		if ((item instanceof EdgeItem) && (negativeEdgeInfluenceFactor != 0)) { 
		        			influenceFactor = negativeEdgeInfluenceFactor;
		    				EdgeItem edge = (EdgeItem) item;
		    				double a = 1/Math.pow(edgeR0-edgeR1, 2);
		    				calculateLineInfluence(potentialArea, a*influenceFactor, edgeR1,
		    						new Line2D.Double(edge.getSourceItem().getX()-activeArea.getX(), edge.getSourceItem().getY()-activeArea.getY(), 
		    								edge.getTargetItem().getX()-activeArea.getX(), edge.getTargetItem().getY()-activeArea.getY()));
		        		}        			
		    		}
		        }
            }
        }
    }

    /**
     * Fill the given area with energy, with values modulated by the preset energy function parameters (radial extent, positive
     * and negative influences for included and excluded nodes and edges).
     *  
     * @param activeArea the screen coordinates of the region to fill
     * @param aitem the aggregate item to calculate energy for
     * @param potentialArea the energy field to fill in
     */
    public void fillPotentialArea(Rectangle2D activeArea, DecoratorItem aggregateDecorator, double[][] potentialArea) {
        double influenceFactor = 0;
        
        // add all positive energy (included items) first, as negative energy morphing 
        // requires all positives to be already set

		if (nodeInfluenceFactor != 0) {
			// add decorator energy
			influenceFactor = nodeInfluenceFactor;
			double a = 1/(Math.pow(nodeR0-nodeR1,2));
			
			Area labelArea = new Area(((AbstractShapeRenderer)aggregateDecorator.getRenderer()).getShape(aggregateDecorator));
			calculateAreaInfluence(potentialArea, a*influenceFactor, nodeR1, labelArea, activeArea);
   				
		} // end processing positive node energy
    }
    
	private Iterator getSortedNodeIterator(AggregateItem aitem, boolean centroidSort) {
		Sort positionSort = null;
		
		if (centroidSort) { 
			// find the centroid
			double totalx = 0;
			double totaly = 0;
			double nodeCount = 0;
		
			Iterator aNodeItems = aitem.items(visibleNodePredicate);
			while (aNodeItems.hasNext()) {
				VisualItem item = (VisualItem) aNodeItems.next();
				totalx += item.getX();
				totaly += item.getY();
				nodeCount++;
			}
		
			totalx /= nodeCount;
			totaly /= nodeCount;
		
			aNodeItems = aitem.items(visibleNodePredicate);
			
			// select centroid or ordered sort
			while (aNodeItems.hasNext()) {
				VisualItem item = (VisualItem) aNodeItems.next();
				item.setDouble(CENTROID_DISTANCE, Math.sqrt(Math.pow(totalx-item.getX(),2) + Math.pow(totaly-item.getY(),2)));
			}
			positionSort = Sort.parse(CENTROID_DISTANCE + " ASC");
		} else { 
			positionSort = Sort.parse("_x ASC, _y ASC");
		}
        
		TupleSet set = new DefaultTupleSet();
		Iterator aItems = aitem.items();
		while (aItems.hasNext()) {
			set.addTuple((Tuple)aItems.next());
		}
			
		return set.tuples(visibleNodePredicate, positionSort);
	}
    
	private void calculateVirtualEdges(AggregateItem aitem) {
		Deque<VisualItem> visited = new ArrayDeque<VisualItem>();
		
		Iterator aNodeItems = getSortedNodeIterator(aitem, centroidSort);
		
		while (aNodeItems.hasNext()) {
			VisualItem item = (VisualItem) aNodeItems.next();
			boolean itemConnected = false;
			
			// check for visible edge structure connecting this node to others 
			if ((useStructuralEdges) && (item instanceof NodeItem)) {
				NodeItem nodeItem = (NodeItem) item;
				Iterator edgeIterator = aitem.items(VISIBLE_EDGE_PREDICATE);
				while (edgeIterator.hasNext()) {
					EdgeItem edge = (EdgeItem) edgeIterator.next();
					// check to see if this edge connects the current node
					if ((edge.getTargetItem() == item) || (edge.getSourceItem() == nodeItem)) {
						if (aitem.containsItem(edge.getAdjacentItem(nodeItem))) {
							// edge connects this item to another item in aggregate
							itemConnected = true;
						}
					}
				}		
			} 
			
			if (!itemConnected) {
				item.set(VIRTUAL_EDGES, connectItem(aitem, item, visited));
			} else {
				item.set(VIRTUAL_EDGES, new ArrayDeque<Line2D>());
			}
			visited.add(item);
		}
	}
	
    private Deque<Line2D> connectItem(AggregateItem aitem, VisualItem item, Collection<VisualItem> visited) {
		VisualItem closestNeighbour = null;
		Deque<Line2D> scannedLines = new ArrayDeque<Line2D>();
		Deque<Line2D> linesToCheck = new ArrayDeque<Line2D>();
		
		// if item is not connected within the aggregate by a visible edge
		// find the closest visited node neighbour in same aggregate
		closestNeighbour = null;
		
		Iterator neighbourIterator = visited.iterator();
		double minLength = Double.MAX_VALUE;
		while (neighbourIterator.hasNext()) {
			double numberInterferenceItems = 0;
			VisualItem neighbourItem = (VisualItem) neighbourIterator.next();
			double distance = Point2D.distance(item.getX(), item.getY(), neighbourItem.getX(), neighbourItem.getY());

			// move virtual edges around nodes, not other edges (routing around edges would be too difficult)
			
			// discover the nearest neighbour
			
			if (considerBlockingItems) {
				// augment distance by number of interfering items
				Line2D completeLine = new Line2D.Double(item.getX(), item.getY(), neighbourItem.getX(), neighbourItem.getY());
				Iterator interferenceItems = m_vis.getVisualGroup(sourceGroup).tuples(visibleNodePredicate);
				numberInterferenceItems = countInterferenceItems(interferenceItems, aitem, completeLine);
				// add all non interference edges
				/*if (numberInterferenceItems == 0) {
					scannedLines.push(completeLine);
				}*/
			} 
			// TODO is there a better function to consider interference in nearest-neighbour checking?  This is hacky
			if ((distance * (numberInterferenceItems+1) < minLength)) {
				closestNeighbour = neighbourItem;
				minLength = distance * (numberInterferenceItems+1);
			}
		}
			
		// if there is a visited closest neighbour, add straight line between them to the positive energy to 
		// ensure connected clusters
		if ((closestNeighbour != null) && (edgeInfluenceFactor != 0)) {
			Line2D completeLine = new Line2D.Double(item.getX(), item.getY(), closestNeighbour.getX(), closestNeighbour.getY());
			
			// route the edge around intersecting nodes not in set
			if (doMorph) {
				linesToCheck.push(completeLine);
				
				boolean hasIntersection = true;
				int iterations = 0;
				Intersection[] intersections = new Intersection[4];
				int numIntersections = 0;
				while (hasIntersection && iterations < MAX_ROUTING_ITERATIONS) {
					hasIntersection = false;
					while (!hasIntersection && !linesToCheck.isEmpty()) {
						Line2D line = linesToCheck.pop();

						// move virtual edges around nodes, not other edges (routing around edges would be too difficult)
						Iterator interferenceItems = m_vis.getVisualGroup(sourceGroup).tuples(visibleNodePredicate);
						
						// resolve intersections in order along edge
						VisualItem closestItem = getCenterItem(interferenceItems, aitem, line);
						
						if (closestItem != null) {
							numIntersections = testIntersection(line, closestItem.getBounds(), intersections);
							
							// reroute if an endpoint is internal to a node
							/*if (numIntersections == 1) {
								Point2D startingPoint = line.getP1();
								int point = 1;
								
								// which point is inside
								if (closestItem.getBounds().contains(line.getP2())) {
									point = 2;
									startingPoint = line.getP2();
								}
								
								// try far corner first (close corner is likely blocked, that's why we have a point inside this item)
								Point2D movePoints = moveEndPoint(startingPoint, closestItem.getBounds(), 1, true);
								boolean pointInside = isPointInsideNode(movePoints, m_vis.getVisualGroup(sourceGroup).tuples(visibleNodePredicate), aitem);
								boolean foundFirst = pointExists(movePoints, linesToCheck.iterator());
								
								// try the close corner
								if ((pointInside) || (foundFirst)) {
									movePoints = moveEndPoint(startingPoint, closestItem.getBounds(), 1, false);
									pointInside = isPointInsideNode(movePoints, m_vis.getVisualGroup(sourceGroup).tuples(visibleNodePredicate), aitem);
									foundFirst = pointExists(movePoints, linesToCheck.iterator());
								}
								
								// if not inside and not already found
								if ((!pointInside) && (!foundFirst)) {
									if (point == 1) {
										line.setLine(movePoints, line.getP2());
									} else {
										line.setLine(line.getP1(), movePoints);
									} 

									// move all other lines that are connected
									for (Line2D moveEndPointLine : linesToCheck) { 
										if (MyMathLib.doublePointsEqual(moveEndPointLine.getP1(), startingPoint)) {
											moveEndPointLine.setLine(movePoints, moveEndPointLine.getP2());
										} 
										if (MyMathLib.doublePointsEqual(moveEndPointLine.getP2(), startingPoint)) {
											moveEndPointLine.setLine(moveEndPointLine.getP1(), movePoints);
										}
									}
									hasIntersection = true;
									// put it back to check again -- should have two intersections next time
									linesToCheck.push(line);
								}
							} */
							
							if (numIntersections == 2) {
								double tempMorphBuffer = morphBuffer;
								
								Point2D movePoint = rerouteLine(line, closestItem.getBounds(), tempMorphBuffer, intersections, true);
								// test the movePoint already exists
								
								boolean foundFirst = (pointExists(movePoint, linesToCheck.iterator()) || pointExists(movePoint, scannedLines.iterator()));
								boolean pointInside = isPointInsideNode(movePoint, m_vis.getVisualGroup(sourceGroup).tuples(visibleNodePredicate), aitem);
								
								// prefer first corner, even if buffer becomes very small
								while ((!foundFirst) && (pointInside) && (tempMorphBuffer >= 1)) {
									// try a smaller buffer
									tempMorphBuffer /= 1.5; 
									movePoint = rerouteLine(line, closestItem.getBounds(), tempMorphBuffer, intersections, true);
									foundFirst = (pointExists(movePoint, linesToCheck.iterator()) || pointExists(movePoint, scannedLines.iterator()));
									pointInside = isPointInsideNode(movePoint, m_vis.getVisualGroup(sourceGroup).tuples(visibleNodePredicate), aitem);
								}
								
								if ((movePoint != null) && (!foundFirst) && (!pointInside)) {
									// add 2 rerouted lines to check 
									linesToCheck.push(new Line2D.Double(line.getP1(), movePoint));
									linesToCheck.push(new Line2D.Double(movePoint, line.getP2()));
									// indicate intersection found
									hasIntersection = true;
								}
								
								// if we didn't find a valid point around the first corner, try the second
								if (!hasIntersection) {
									tempMorphBuffer = morphBuffer;
								
									movePoint = rerouteLine(line, closestItem.getBounds(), tempMorphBuffer, intersections, false);
									boolean foundSecond = (pointExists(movePoint, linesToCheck.iterator()) || pointExists(movePoint, scannedLines.iterator()));
									pointInside = isPointInsideNode(movePoint, m_vis.getVisualGroup(sourceGroup).tuples(visibleNodePredicate), aitem);
									
									// if both corners have been used, stop; otherwise gradually reduce buffer and try second corner
									while ((!foundSecond) && (pointInside) && (tempMorphBuffer >= 1)) {
										// try a smaller buffer
										tempMorphBuffer /= 1.5; 
										movePoint = rerouteLine(line, closestItem.getBounds(), tempMorphBuffer, intersections, false);
										foundSecond = (pointExists(movePoint, linesToCheck.iterator()) || pointExists(movePoint, scannedLines.iterator()));
										pointInside = isPointInsideNode(movePoint, m_vis.getVisualGroup(sourceGroup).tuples(visibleNodePredicate), aitem);
									}
									
									if ((movePoint != null) && (!foundSecond)) {
										// add 2 rerouted lines to check 
										linesToCheck.push(new Line2D.Double(line.getP1(), movePoint));
										linesToCheck.push(new Line2D.Double(movePoint, line.getP2()));
										// indicate intersection found
										hasIntersection = true;
									} 
								}
							}
						} // end check of closest item
						
						// no intersection found, mark this line as completed
						if (!hasIntersection) scannedLines.push(line);
						
						iterations++;
					} // end inner loop - out of lines or found an intersection 
				} // end outer loop - no more intersections or out of iterations
				
				if (iterations >= MAX_ROUTING_ITERATIONS) 
					LOGGER.warning("Warning: exceeding routing iterations.");
			
				// finalize any that were not rerouted (due to running out of iterations) or if we aren't morphing
				while (!linesToCheck.isEmpty()) {
					scannedLines.push(linesToCheck.pop());
				}
				
				// try to merge consecutive lines if possible (see reviewer #3 comments regarding 'sideways 7' in video)
				if (doMerge) {
					while (!scannedLines.isEmpty()) {
					Line2D line1 = scannedLines.pop();
					
					if (!scannedLines.isEmpty()) {
						Line2D line2 = scannedLines.pop();
						
						Line2D mergeLine = new Line2D.Double(line1.getP1(), line2.getP2());
						
						// move virtual edges around nodes, not other edges (routing around edges would be too difficult)
						Iterator interferenceItems = m_vis.getVisualGroup(sourceGroup).tuples(visibleNodePredicate);
						
						// resolve intersections in order along edge
						VisualItem closestItem = getCenterItem(interferenceItems, aitem, mergeLine);
						
						// merge most recent line and previous line
						if (closestItem == null) {
							scannedLines.push(mergeLine);
						} else {
							linesToCheck.push(line1);
							scannedLines.push(line2);
						}
					} else {
						linesToCheck.push(line1);
					}
				}
				scannedLines = linesToCheck;
				}
			} else {
				scannedLines.push(completeLine);
			}
		}
		return scannedLines;
	}
    
    
    
    /**
     * Check if a point is inside the rectangular bounds of any of the given tuples.  Ignores members
     * of the given aggregate item if one is specified. 
     * 
     * @param movePoint the point to check, in screen coordinates
     * @param tuples the tuples to scan
     * @param exceptionItem the aggregate whose items should be ignored
     * @return true if this point is within the rectangular bounding box of at least one tuple; false otherwise
     */
    private boolean isPointInsideNode(Point2D movePoint, Iterator tuples, AggregateItem exceptionItem) {
    	while (tuples.hasNext()) {
    		VisualItem item = (VisualItem) tuples.next();
    		if ((exceptionItem != null) && (exceptionItem.containsItem(item))) {
    			continue;
    		}
    		if (item.getBounds().contains(movePoint)) { 
    			return true;
    		}
    	}
    	return false;
    }

    /**
     * Checks whether a given point is already an endpoint of any of the given lines.
     * 
     * @see MyMathLib#doublePointsEqual(Point2D, Point2D) 
     * 
     * @param pointToCheck the point to check
     * @param linesIterator the lines to scan the endpoints of 
     * @return true if the given point is the endpoint of at least one line; false otherwise
     */
    public boolean pointExists(Point2D pointToCheck, Iterator<Line2D> linesIterator) {
		boolean found = false;
		
		while ((linesIterator.hasNext()) && (!found)) {
			Line2D checkEndPointsLine = (Line2D) linesIterator.next();
			if (MyMathLib.doublePointsEqual(checkEndPointsLine.getP1(), pointToCheck)) {
				found = true;
			} 
			if (MyMathLib.doublePointsEqual(checkEndPointsLine.getP2(), pointToCheck)) {
				found = true;
			} 
		}
		return found;
	}

	/**
     * Add radial (circular) contribution of a point source to all points in a given area.
     *  
     * @param potentialArea the area to fill with influence values
     * @param factor the influence factor of this point source
     * @param pointx the x-coordinate of the point source
     * @param pointy the y-coordinate of the point source
     */
    public void calculatePointInfluence(double[][]potentialArea, double factor, double r1, double pointx, double pointy) {
    	double tempX = 0, tempY = 0, distance = 0;
    	
        // for every point in potentialArea, calculate distance to center of item and add influence
    	for (int x = 0; x < potentialArea.length; x++) {
			for (int y = 0; y < potentialArea[x].length; y++) {
				tempX = x*pixelGroup;
				tempY = y*pixelGroup;
				distance = Point2D.distance(tempX, tempY, pointx, pointy);
				// only influence if less than r1  
				if (distance < r1) {
					potentialArea[x][y] += factor * Math.pow(distance-r1, 2);
				}
			}
		}
    }
 
    /**
     * Add a contribution of a line source to all points in a given area.  For every point 
     * in the given area, the distance to the closest point on the line is calculated 
     * and this distance is input into the gradient influence function, then added to the 
     * potentialArea.
     * 
     * @param potentialArea the area to fill with influence values
     * @param influenceFactor the influence factor of the line in the area
     * @param line the line source 
     */
    public void calculateLineInfluence(double[][]potentialArea, double influenceFactor, double r1, Line2D line) {
		double tempX, tempY, distance = 0;
		
		Rectangle2D r = line.getBounds2D();
		int startX = Math.min(Math.max(0, (int)((r.getX()-r1)/pixelGroup)), potentialArea.length-1);
    	int startY = Math.min(Math.max(0, (int)((r.getY()-r1)/pixelGroup)), potentialArea[startX].length-1);
    	int endX = Math.min(potentialArea.length-1, Math.max(0, (int)((r.getX()+r.getWidth()+r1)/pixelGroup)));
    	int endY = Math.min(potentialArea[startX].length, Math.max(0,(int)((r.getY()+r.getHeight()+r1)/pixelGroup)));
    	
		// for every point in potentialArea, calculate distance to nearest point on line and add influence
    	for (int x = startX; x < endX; x++) {
			for (int y = startY; y < endY; y++) {
				tempX = x*pixelGroup;
				tempY = y*pixelGroup;
		
				distance = line.ptSegDist(tempX, tempY);
				
				// only influence if less than r1 
				if (distance <= r1) {
					potentialArea[x][y] += influenceFactor * Math.pow(distance-r1, 2);
				}
			}
    	}
    }
    
    /**
     * Finds the item in the iterator whose rectangular bounds intersect the line closest to the P1 endpoint and the item is 
     * not in the given aggregate.  Note that despite the shape of the rendered VisualItem, the rectangular bounds are used as
     * for this check.
     *  
     * @param interferenceItems
     * @param currentAggregate
     * @param testLine
     * @return the closest item or null if there are no intersections.
     */
    public VisualItem getClosestItem(Iterator interferenceItems, AggregateItem currentAggregate, Line2D testLine) {
    	double minDistance = Double.MAX_VALUE;
    	VisualItem closestItem = null;
    	
    	while (interferenceItems.hasNext()) {
			VisualItem interferenceItem = (VisualItem) interferenceItems.next();
			if (!currentAggregate.containsItem(interferenceItem)) {
				
				// only test if overlap is possible (QUES not sure if this is faster b/c it adds some tests to every item)
				if ((interferenceItem.getBounds().getMinX() <= (Math.max(testLine.getX1(),testLine.getX2()))
				 	&& (interferenceItem.getBounds().getMinY() <= Math.max(testLine.getY1(), testLine.getY2())) &&
				 	((interferenceItem.getBounds().getMaxX() >= (Math.min(testLine.getX1(),testLine.getX2()))
						 	&& (interferenceItem.getBounds().getMaxY() >= Math.min(testLine.getY1(), testLine.getY2())))))) {
					
					double distance = fractionToLineEnd(interferenceItem.getBounds(), testLine);
					// find closest intersection
					if ((distance != -1) && (distance < minDistance)) {
						closestItem = interferenceItem;
						minDistance = distance;
					}
				}
			}
		}
    	return closestItem;
    }
    
    /**
     * Finds the item in the iterator whose rectangular bounds intersect the line closest to the center and the item is 
     * not in the given aggregate.  Note that despite the shape of the rendered VisualItem, the rectangular bounds are used as
     * for this check.
     *  
     * @param interferenceItems
     * @param currentAggregate
     * @param testLine
     * @return the closest item or null if there are no intersections.
     */
    public VisualItem getCenterItem(Iterator interferenceItems, AggregateItem currentAggregate, Line2D testLine) {
    	double minDistance = Double.MAX_VALUE;
    	VisualItem closestItem = null;
    	
    	while (interferenceItems.hasNext()) {
			VisualItem interferenceItem = (VisualItem) interferenceItems.next();
			if (!currentAggregate.containsItem(interferenceItem)) {
				
				// only test if overlap is possible (QUES not sure if this is faster b/c it adds some tests to every item)
				if ((interferenceItem.getBounds().getMinX() <= (Math.max(testLine.getX1(),testLine.getX2()))
				 	&& (interferenceItem.getBounds().getMinY() <= Math.max(testLine.getY1(), testLine.getY2())) &&
				 	((interferenceItem.getBounds().getMaxX() >= (Math.min(testLine.getX1(),testLine.getX2()))
						 	&& (interferenceItem.getBounds().getMaxY() >= Math.min(testLine.getY1(), testLine.getY2())))))) {
					
					double distance = fractionToLineCenter(interferenceItem.getBounds(), testLine);
					// find closest intersection
					if ((distance != -1) && (distance < minDistance)) {
						closestItem = interferenceItem;
						minDistance = distance;
					}
				}
			}
		}
    	return closestItem;
    }
    
    public int countInterferenceItems(Iterator interferenceItems, AggregateItem currentAggregate, Line2D testLine) {
    	int count = 0;
    	while (interferenceItems.hasNext()) {
			VisualItem interferenceItem = (VisualItem) interferenceItems.next();
			if (!currentAggregate.containsItem(interferenceItem)) {
				// only test if overlap is possible (QUES not sure if this is faster b/c it adds some tests to every item)
				// only test if overlap is possible (QUES not sure if this is faster b/c it adds some tests to every item)
				if ((interferenceItem.getBounds().getMinX() <= (Math.max(testLine.getX1(),testLine.getX2()))
				 	&& (interferenceItem.getBounds().getMinY() <= Math.max(testLine.getY1(), testLine.getY2())) &&
				 	((interferenceItem.getBounds().getMaxX() >= (Math.min(testLine.getX1(),testLine.getX2()))
						 	&& (interferenceItem.getBounds().getMaxY() >= Math.min(testLine.getY1(), testLine.getY2())))))) {
					if (fractionToLineCenter(interferenceItem.getBounds(), testLine) != -1) count++;
				}
			}
		}
    	return count;
    }
    
    
    /**
     * Add a contribution of a line source to all points in a given area.  For every point 
     * in the given area, the distance to the closest point on the line is calculated 
     * and this distance is input into the gradient influence function, then added to the 
     * potentialArea.
     * 
     * @param potentialArea the area to fill with influence values
     * @param influenceFactor the influence factor of the line in the area
     * @param line the line source 
     */
    public void calculateLinesInfluence(double[][]potentialArea, double influenceFactor, double r1, Deque<Line2D> lines, Rectangle2D bounds) {
		double tempX, tempY, distance = 0;
		double minDistance = Double.MAX_VALUE;

		Rectangle2D r = null;
		
		// calculate active region
		for (Line2D line : lines) {
			if (r==null) 
				r = (Rectangle2D) line.getBounds2D().clone();
			else 
				r.add(line.getBounds2D());
		}
		r.setFrame(r.getX()-bounds.getX(), r.getY()-bounds.getY(), r.getWidth(), r.getHeight()); 

		int startX = Math.min(Math.max(0, (int)((r.getX()-r1)/pixelGroup)), potentialArea.length-1);
    	int startY = Math.min(Math.max(0, (int)((r.getY()-r1)/pixelGroup)), potentialArea[startX].length-1);
    	int endX = Math.min(potentialArea.length-1, Math.max(0, (int)((r.getX()+r.getWidth()+r1)/pixelGroup)));
    	int endY = Math.min(potentialArea[startX].length, Math.max(0,(int)((r.getY()+r.getHeight()+r1)/pixelGroup)));
    	
		// for every point in potentialArea, calculate distance to nearest point on line and add influence
    	for (int x = startX; x < endX; x++) {
			for (int y = startY; y < endY; y++) {
				
				// if we are adding negative energy, skip if not already positive 
				// positives have already been added first, and adding negative to <=0 will have no affect on surface
				if ((influenceFactor < 0) && (potentialArea[x][y] <= 0)) continue;
				
				tempX = x*pixelGroup + bounds.getX();
				tempY = y*pixelGroup + bounds.getY();
		
				minDistance = Double.MAX_VALUE;
				for (Line2D line : lines) {
					distance = line.ptSegDist(tempX, tempY);
					if (distance < minDistance) {
						minDistance = distance;
					}
				}
					
				// only influence if less than r1 
				if (minDistance <= r1) {
					potentialArea[x][y] += influenceFactor * Math.pow(minDistance-r1, 2);
				}
			}
    	}
    }
    
    /**
     * Add a contribution of a rectangle source to all points in a given area.  For every point 
     * in the given area, the distance to the closest point on the rectangle is calculated 
     * and this distance is input into the gradient influence function, then added to the 
     * potentialArea.
     * 
     * @param potentialArea the area to fill with influence values
     * @param influenceFactor the influence factor of the line in the area
     * @param line the line source 
     */
    public void calculateRectangleInfluence(double[][]potentialArea, double influenceFactor, double r1, Rectangle2D r) {
    	double tempX, tempY, distance = 0;
    	Line2D line;
    	
    	int startX = Math.min(Math.max(0, (int)((r.getX()-r1)/pixelGroup)), potentialArea.length-1);
    	int startY = Math.min(Math.max(0, (int)((r.getY()-r1)/pixelGroup)), potentialArea[startX].length-1);
    	int endX = Math.min(potentialArea.length-1, Math.max(0, (int)((r.getX()+r.getWidth()+r1)/pixelGroup)));
    	int endY = Math.min(potentialArea[startX].length, Math.max(0,(int)((r.getY()+r.getHeight()+r1)/pixelGroup)));
    	
    	// for every point in potentialArea, calculate distance to nearest point on rectangle
    	// and add influence
    	for (int x = startX; x < endX; x++) {
			for (int y = startY; y < endY; y++) {
				// if we are adding negative energy, skip if not already positive 
				// positives have already been added first, and adding negative to <=0 will have no affect on surface
				
				if ((influenceFactor < 0) && (potentialArea[x][y] <= 0)) continue;
				
				tempX = x*pixelGroup;
				tempY = y*pixelGroup;
				
				// inside 
				if (r.contains(tempX, tempY)) {
					distance = 0;
				} else {
					int outcode = r.outcode(tempX, tempY);
					// top
					if ((outcode & Rectangle2D.OUT_TOP) == Rectangle2D.OUT_TOP) {
						// and left
						if ((outcode & Rectangle2D.OUT_LEFT) == Rectangle2D.OUT_LEFT) {
							//linear distance from upper left corner
							distance = Point2D.distance(tempX, tempY, r.getMinX(), r.getMinY());
						} else {
							// and right
							if ((outcode & Rectangle2D.OUT_RIGHT) == Rectangle2D.OUT_RIGHT) {
								//linear distance from upper right corner
								distance = Point2D.distance(tempX, tempY, r.getMaxX(), r.getMinY());
							} else {
								// distance from top line segment
								line = new Line2D.Double(r.getMinX(), r.getMinY(), r.getMaxX(), r.getMinY());
								distance = line.ptSegDist(tempX, tempY);
							}
						}
					} else {
						// bottom
						if ((outcode & Rectangle2D.OUT_BOTTOM) == Rectangle2D.OUT_BOTTOM) {
							// and left
							if ((outcode & Rectangle2D.OUT_LEFT) == Rectangle2D.OUT_LEFT) {
								//linear distance from lower left corner
								distance = Point2D.distance(tempX, tempY, r.getMinX(), r.getMaxY());
							} else {
								// and right
								if ((outcode & Rectangle2D.OUT_RIGHT) == Rectangle2D.OUT_RIGHT) {
									//linear distance from lower right corner
									distance = Point2D.distance(tempX, tempY, r.getMaxX(), r.getMaxY());
								} else {
									// distance from bottom line segment
									line = new Line2D.Double(r.getMinX(), r.getMaxY(), r.getMaxX(), r.getMaxY());
									distance = line.ptSegDist(tempX, tempY);
								}
							}
						} else {
							// left only
							if ((outcode & Rectangle2D.OUT_LEFT) == Rectangle2D.OUT_LEFT) {
								//linear distance from left edge
								line = new Line2D.Double(r.getMinX(), r.getMinY(), r.getMinX(), r.getMaxY());
								distance = line.ptSegDist(tempX, tempY);
							} else {
								// right only 
								if ((outcode & Rectangle2D.OUT_RIGHT) == Rectangle2D.OUT_RIGHT) {
									//linear distance from right edge
									line = new Line2D.Double(r.getMaxX(), r.getMinY(), r.getMaxX(), r.getMaxY());
									distance = line.ptSegDist(tempX, tempY);
								}
							}
						}
					}
				}
				// only influence if less than r1 
				if (distance <= r1) {
					potentialArea[x][y] += influenceFactor * Math.pow(distance-r1, 2);
				}
			}
		}
    }
    
    
    /**
     * Add a contribution of an arbitrary area made of straight line segments to all points in a given area.  
     * For every point in the given area, the distance to the closest point on the area boundary is calculated 
     * and this distance is input into the gradient influence function, then added to the potentialArea.
     * 
     * @param potentialArea the area to fill with influence values
     * @param influenceFactor the influence factor of the line in the area
     * @param r1 the radius at which energy drops to zero
     * @param a the area to add positive influence (in world coordinates)
     * @param activeArea the bounds of the calculation region
     */
    public void calculateAreaInfluence(double[][]potentialArea, double influenceFactor, double r1, Area a, Rectangle2D activeArea) {
    	double tempX, tempY, distance = 0;
    	
    	// create a deque of the lines
    	Deque<Line2D> lines = new ArrayDeque<Line2D>();
    	PathIterator pathIterator = a.getPathIterator(null);
    	
    	double[] current = new double[6];
    	double[] previous =  new double[6];
    	double[] start = null;
    	
    	int segmentType;
    	
    	int i = 0;
    	while (!pathIterator.isDone()) {
    		segmentType = pathIterator.currentSegment(current);
        	if (segmentType == PathIterator.SEG_LINETO) {
        		if (previous != null) {
        			lines.add(new Line2D.Double(previous[0], previous[1], current[0], current[1]));
        		}
        	}
        	if (segmentType == PathIterator.SEG_CLOSE) {
        		if (previous != null) {
        			lines.add(new Line2D.Double(previous[0], previous[1], start[0], start[1]));
        		}
        	}
        	System.arraycopy(current, 0, previous, 0, current.length);
        	
        	if (start == null) {
        		start = new double[6];
        		System.arraycopy(current, 0, start, 0, current.length);
        	}
        	
    		pathIterator.next();
    	}
    	
    	// go around edges
    	calculateLinesInfluence(potentialArea, influenceFactor, r1, lines, activeArea);
    	
    	int startX = Math.min(Math.max(0, (int)((activeArea.getX()-r1)/pixelGroup)), potentialArea.length-1);
    	int startY = Math.min(Math.max(0, (int)((activeArea.getY()-r1)/pixelGroup)), potentialArea[startX].length-1);
    	int endX = Math.min(potentialArea.length-1, Math.max(0, (int)((activeArea.getX()+activeArea.getWidth()+r1)/pixelGroup)));
    	int endY = Math.min(potentialArea[startX].length, Math.max(0,(int)((activeArea.getY()+activeArea.getHeight()+r1)/pixelGroup)));
    	
    	// for every point in potentialArea, calculate distance to nearest point on rectangle
    	// and add influence
    	for (int x = startX; x < endX; x++) {
			for (int y = startY; y < endY; y++) {
				// if we are adding negative energy, skip if not already positive 
				// positives have already been added first, and adding negative to <=0 will have no affect on surface
				
				if ((influenceFactor < 0) && (potentialArea[x][y] <= 0)) continue;
				
				tempX = x*pixelGroup + activeArea.getX();
				tempY = y*pixelGroup + activeArea.getY();
				
				// inside 
				if (a.contains(tempX, tempY)) {
					distance = 0;
				} else {
					distance = Double.MAX_VALUE;
				}
				// only influence if less than r1 
				if (distance <= r1) {
					potentialArea[x][y] += influenceFactor * Math.pow(distance-r1, 2);
				}
			}
		}
    }
    
    /**
     * 2-D Marching squares algorithm.  March around a given area to find an iso-energy surface.
     * 
     * @param surface the surface to fill with iso-energy points
     * @param potentialArea the area, filled with potential values
     * @param x the current x-position in the area 
     * @param y the current y-position in the area
     */
    public boolean march(ArrayList<Point2D> surface, double[][]potentialArea, int x, int y) {
    	Point2D p = new Point2D.Float(x * pixelGroup, y * pixelGroup);
    	
    	// check if we're back where we started
    	if (surface.contains(p)) {
			if (!surface.get(0).equals(p)) {
				// encountered a loop but haven't returned to start; will change direction using conditionals and continue
			} else {
				// back to start
				return true;
			} 
    	} else { 
			surface.add(p);
		}
    	
    	int state = getState(potentialArea, x, y);
    	// x, y are upper left of 2X2 marching square
    	
    	switch(state) {
    		case -1: LOGGER.warning("Marched out of bounds"); 
    			return false; // marched out of bounds (shouldn't happen)
    		case 0:
    		case 3:
    		case 2:
    		case 7: direction = Direction.E; break;
    		case 12:
    		case 14:
    		case 4: direction = Direction.W; break;
    		case 6: direction = (direction == Direction.N) ? Direction.W : Direction.E; break;   
    		case 1:
    		case 13:
    		case 5: direction = Direction.N; break;
    		case 9: direction = (direction == Direction.E) ? Direction.N : Direction.S; break;
    		case 10:
    		case 8:
    		case 11: direction = Direction.S; break;
			default:
				throw new IllegalStateException("Marching squares invalid state: " + state);
    	}
    	
    	switch (direction) {
    		case N: return march(surface, potentialArea, x, y-1); // up
    		case S: return march(surface, potentialArea, x, y+1); // down
    		case W: return march(surface, potentialArea, x-1, y); // left
    		case E: return march(surface, potentialArea, x+1, y); // right
    		default: 
    			throw new IllegalStateException("Marching squares invalid state: " + state);
    	}
    }
    
    public void setThreshold(double threshold) {
    	this.threshold = threshold;
    }
    
    public void setNodeInfluence(double positive, double negative) {
    	nodeInfluenceFactor = positive;
    	negativeNodeInfluenceFactor = negative;
    }
    
    public void setEdgeInfluenceFactor(double positive, double negative) {
    	edgeInfluenceFactor = positive;
    	negativeEdgeInfluenceFactor = negative;
    }
   
    /**
     * Tests whether a given value meets the threshold specified for marching squares.
     * 
     * @param test the value to test
     * @return whether the test value passes
     */
    public boolean test(double test) { 
    	return (test > threshold);
    }

    /**
     * 2-D Marching Squares algorithm.  Given a position and an area of potential energy 
     * values, calculate the current marching squares state by testing neighbouring squares.
     * 
     * @param potentialArea the area filled with potential energy values
     * @param x the current x-position in the area
     * @param y the current y-position in the area
     * @return an int value representing a marching squares state
     */
    public int getState(double[][] potentialArea, int x, int y) {
		int dir = 0;    	
    	try {
		dir+= (test(potentialArea[x][y]) ? 1<<0 : 0);
    	dir+= (test(potentialArea[x+1][y]) ? 1<<1 : 0);
    	dir+= (test(potentialArea[x][y+1]) ? 1<<2 : 0);
    	dir+= (test(potentialArea[x+1][y+1]) ? 1<<3 : 0);
    	} catch (ArrayIndexOutOfBoundsException e) {
    		LOGGER.severe("Marched out of bounds: " + x + " " + y + " bounds: " + potentialArea.length + " " + potentialArea[0].length);
    		return -1;
    	}
    	return dir;
    }
    
    /**
     * Calculate the energy function of any hovered aggregates and visualize it in the given graphics context.
     * 
     * @param g2d the graphics context into which to visualize the energy function
     */
    public void paintPotentialArea(Graphics2D g2d) {
        AggregateTable aggr = (AggregateTable) m_vis.getGroup(m_group);
        if (aggr == null) return;
        
        // do we have any  to process?
        int num = aggr.getTupleCount();
        if ( num == 0 ) return;
        
        if (aggregateLabelLayout != null) {
        	// add their energy to aggregates
        	TupleSet aggregateLabels = m_vis.getGroup(aggregateLabelLayout.getGroup());
        	Iterator labelIterator = aggregateLabels.tuples();
        	
        	while(labelIterator.hasNext()) {
        		DecoratorItem di = (DecoratorItem) labelIterator.next();
        		AggregateItem aitem = (AggregateItem) di.getDecoratedItem();
    			if (di.isVisible()) {
        			if ((aitem.isHover()) && ( aitem.getAggregateSize() > 0 ))  {
        				paintPotential(aitem, di, g2d);
        			}
        		} else {
        			if (aitem.isVisible()) {
        				if ((aitem.isHover()) && ( aitem.getAggregateSize() > 0 ))  {
        					paintPotential(aitem, null, g2d);
        				}
        			}
        		}
        	}
        } else {
        	// compute and assign convex hull for each aggregate
            Iterator aggrs = m_vis.visibleItems(m_group);
            while ( aggrs.hasNext() ) {
              	AggregateItem aitem = (AggregateItem)aggrs.next();
              	if ((aitem.isHover()) && ( aitem.getAggregateSize() > 0 ))  {
               		paintPotential(aitem, null, g2d);
               	}
            }
        }
    }
    
    public void paintPotential(AggregateItem aitem, DecoratorItem di, Graphics2D g2d) {
    	// calculate bounds of influence
    	Rectangle2D bounds = null;
    	VisualItem item = null;
    	
    	bounds = aitem.getBounds();
    	
    	// expand bounds by the maximum radius on all sides 
    	bounds.setRect(bounds.getX()-Math.max(edgeR1, nodeR1), bounds.getY()-Math.max(edgeR1, nodeR1), 
    			bounds.getWidth()+2*Math.max(edgeR1, nodeR1), bounds.getHeight()+2*Math.max(edgeR1,nodeR1));
	            
    	// 	calculates potential for all points within bounds
    	double [][] potentialArea = new double[(int)Math.ceil(bounds.getWidth()/pixelGroup)][(int)Math.ceil(bounds.getHeight()/pixelGroup)];
	            
    	// save old energy parameters
    	double tempThreshold = threshold;
    	double tempNegativeNodeInfluenceFactor = negativeNodeInfluenceFactor;
    	double tempNegativeEdgeInfluenceFactor = negativeEdgeInfluenceFactor;
    	double tempNodeInfluenceFactor = nodeInfluenceFactor;
    	double tempEdgeInfluenceFactor = edgeInfluenceFactor;	            
    	
    	// set energy parameters from node cache
    	threshold = aitem.getDouble(THRESHOLD);
    	negativeNodeInfluenceFactor = aitem.getDouble(NNIF);
    	negativeEdgeInfluenceFactor = aitem.getDouble(NEIF);
    	nodeInfluenceFactor = aitem.getDouble(NIF);
    	edgeInfluenceFactor = aitem.getDouble(EIF);
    	
    	System.err.println("Aggregate info: threshold: " + threshold + " NNIF: " + negativeNodeInfluenceFactor + " NEIF: " + negativeEdgeInfluenceFactor + " NIF: " + nodeInfluenceFactor + " EIF: " + edgeInfluenceFactor);
    	
    	fillPotentialArea(bounds, aitem, potentialArea);
    	if (di != null) {
        	fillPotentialArea(bounds, di, potentialArea);
    	}
    	
        //draw energy field
        int tempX, tempY;
        for (int x = 0; x < potentialArea.length-1; x++) {
        	for (int y = 0; y < potentialArea[x].length-1; y++) {
        		tempX = x*pixelGroup + (int) bounds.getX();
        		tempY = y*pixelGroup + (int) bounds.getY();
						
        		if (potentialArea[x][y] < 0)
        			g2d.setColor(ColorLib.getColor(ColorLib.rgba(20,20,150,(int)Math.min(255,Math.abs((potentialArea[x][y] * 40))))));
        		else
        			g2d.setColor(ColorLib.getColor(ColorLib.rgba(150,20,20,(int)Math.min(255,Math.abs((potentialArea[x][y] * 40))))));
        		if (potentialArea[x][y] == threshold)
        			g2d.setColor(ColorLib.getColor(ColorLib.gray(0, 120)));
        		g2d.fillRect(tempX, tempY, pixelGroup, pixelGroup);
        	}
        }
        
        // reset parameters
        threshold = tempThreshold;
        negativeEdgeInfluenceFactor = tempNegativeEdgeInfluenceFactor;
        negativeNodeInfluenceFactor = tempNegativeNodeInfluenceFactor;
        nodeInfluenceFactor = tempNodeInfluenceFactor;
        edgeInfluenceFactor = tempEdgeInfluenceFactor;
    }
    
    /**
     * Calculate the intersection of two line segments.
     * @param a
     * @param b
     * @return an Intersection item storing the type of intersection and the exact point if any was found
     */
    public Intersection intersectLineLine(Line2D a, Line2D b) {
    	Intersection result;
    	
    	double ua_t = (b.getX2()-b.getX1()) * (a.getY1()-b.getY1()) - (b.getY2()-b.getY1()) * (a.getX1()-b.getX1());
        double ub_t = (a.getX2()-a.getX1()) * (a.getY1()-b.getY1()) - (a.getY2()-a.getY1()) * (a.getX1()-b.getX1());
        double u_b  = (b.getY2()-b.getY1()) * (a.getX2()-a.getX1()) - (b.getX2()-b.getX1()) * (a.getY2()-a.getY1());

        if ( u_b != 0 ) {
            double ua = ua_t / u_b;
            double ub = ub_t / u_b;

            if ( 0 <= ua && ua <= 1 && 0 <= ub && ub <= 1 ) {
                result = new Intersection(
                        a.getX1() + ua * (a.getX2() - a.getX1()),
                        a.getY1() + ua * (a.getY2() - a.getY1()));
            } else {
                result = new Intersection(State.None);
            }
        } else {
            if ( ua_t == 0 || ub_t == 0 ) {
                result = new Intersection(State.Coincident);
            } else {
                result = new Intersection(State.Parallel);
            }
        }

        return result;
    };

    /**
     * Find the fraction along the line a that line b intersects, closest to P1 on line a.  This is slightly faster than
     * determining the actual intersection coordinates.
     * 
     * @param bounds
     * @param line
     * @return the smallest fraction along the line that indicates an intersection point
     */
    public double fractionAlongLineA(Line2D a, Line2D b) {
    	double ua_t = (b.getX2()-b.getX1()) * (a.getY1()-b.getY1()) - (b.getY2()-b.getY1()) * (a.getX1()-b.getX1());
        double ub_t = (a.getX2()-a.getX1()) * (a.getY1()-b.getY1()) - (a.getY2()-a.getY1()) * (a.getX1()-b.getX1());
        double u_b  = (b.getY2()-b.getY1()) * (a.getX2()-a.getX1()) - (b.getX2()-b.getX1()) * (a.getY2()-a.getY1());

        if ( u_b != 0 ) {
            double ua = ua_t / u_b;
            double ub = ub_t / u_b;

            if ( 0 <= ua && ua <= 1 && 0 <= ub && ub <= 1 ) { 
            	return ua;
            }
        }
        return Double.MAX_VALUE;
    };

    /**
     * Find the fraction along the given line that the rectangle intersects, closest to the center of the line. 
     * This is slightly faster than determining the actual intersection coordinates.
     * 
     * @param bounds
     * @param line
     * @return the smallest fraction along the line that indicates an intersection point
     */
    public double fractionToLineCenter(Rectangle2D bounds, Line2D line) {
    	double minDistance = Double.MAX_VALUE;
    	double testDistance = 0;
    	int countIntersections = 0;
    	
    	// top
		testDistance = fractionAlongLineA(line, new Line2D.Double(bounds.getMinX(), bounds.getMinY(), bounds.getMaxX(), bounds.getMinY()));
		testDistance -= 0.5;
		testDistance = Math.abs(testDistance);
		if ((testDistance >= 0) && (testDistance <= 1)) {
			countIntersections++;
			if (testDistance < minDistance) {
				minDistance = testDistance;
			}
		}
		
		// left
		testDistance = fractionAlongLineA(line, new Line2D.Double(bounds.getMinX(), bounds.getMinY(), bounds.getMinX(), bounds.getMaxY()));
		testDistance -= 0.5;
		testDistance = Math.abs(testDistance);
		if ((testDistance >= 0) && (testDistance <= 1)) {
			countIntersections++;
			if (testDistance < minDistance) {
				minDistance = testDistance;
			}
		}
		if (countIntersections == 2) return minDistance; // max 2 intersections
		
		// bottom
		testDistance = fractionAlongLineA(line, new Line2D.Double(bounds.getMinX(), bounds.getMaxY(), bounds.getMaxX(), bounds.getMaxY()));
		testDistance -= 0.5;
		testDistance = Math.abs(testDistance);
		if ((testDistance >= 0) && (testDistance <= 1)) {
			countIntersections++;
			if (testDistance < minDistance) {
				minDistance = testDistance;
			}
		}
		if (countIntersections == 2) return minDistance; // max 2 intersections
		
		// right
		testDistance = fractionAlongLineA(line, new Line2D.Double(bounds.getMaxX(), bounds.getMinY(), bounds.getMaxX(), bounds.getMaxY()));
		testDistance -= 0.5;
		testDistance = Math.abs(testDistance);
		if ((testDistance >= 0) && (testDistance <= 1)) {
			countIntersections++;
			if (testDistance < minDistance) {
				minDistance = testDistance;
			}
		}
		
		// if no intersection, return -1
		if (countIntersections == 0) {
			return -1;
		}
		
		return minDistance; 
	}
    
    /**
     * Find the fraction along the given line that the rectangle intersects, closest to P1 on the line. 
     * This is slightly faster than determining the actual intersection coordinates.
     * 
     * @param bounds
     * @param line
     * @return the smallest fraction along the line that indicates an intersection point
     */
    public double fractionToLineEnd(Rectangle2D bounds, Line2D line) {
    	double minDistance = Double.MAX_VALUE;
    	double testDistance = 0;
    	int countIntersections = 0;
    	
    	// top
		testDistance = fractionAlongLineA(line, new Line2D.Double(bounds.getMinX(), bounds.getMinY(), bounds.getMaxX(), bounds.getMinY()));
		if ((testDistance >= 0) && (testDistance <= 1)) {
			countIntersections++;
			if (testDistance < minDistance) {
				minDistance = testDistance;
			}
		}
		
		// left
		testDistance = fractionAlongLineA(line, new Line2D.Double(bounds.getMinX(), bounds.getMinY(), bounds.getMinX(), bounds.getMaxY()));
		if ((testDistance >= 0) && (testDistance <= 1)) {
			countIntersections++;
			if (testDistance < minDistance) {
				minDistance = testDistance;
			}
		}
		if (countIntersections == 2) return minDistance; // max 2 intersections
		
		// bottom
		testDistance = fractionAlongLineA(line, new Line2D.Double(bounds.getMinX(), bounds.getMaxY(), bounds.getMaxX(), bounds.getMaxY()));
		if ((testDistance >= 0) && (testDistance <= 1)) {
			countIntersections++;
			if (testDistance < minDistance) {
				minDistance = testDistance;
			}
		}
		if (countIntersections == 2) return minDistance; // max 2 intersections
		
		// right
		testDistance = fractionAlongLineA(line, new Line2D.Double(bounds.getMaxX(), bounds.getMinY(), bounds.getMaxX(), bounds.getMaxY()));
		if ((testDistance >= 0) && (testDistance <= 1)) {
			countIntersections++;
			if (testDistance < minDistance) {
				minDistance = testDistance;
			}
		}
		
		// if no intersection, return -1
		if (countIntersections == 0) {
			return -1;
		}
		
		return minDistance; 
	}
    
    /**
     * Tests intersection of the given line segment with all sides of the given rectangle.
     * 
     * @param line the line to test
     * @param rectangle the rectangular bounds to test each side of
     * @param intersections an array of at least 4 intersections where the intersections will be stored as top, left, bottom, right
     * @return the number of intersection points found (doesn't count coincidental lines)
     */
    public int testIntersection(Line2D line, Rectangle2D bounds, Intersection[] intersections) {
    	
    	int countIntersections = 0;
    	
    	// top
		intersections[0] = intersectLineLine(line, new Line2D.Double(bounds.getMinX(), bounds.getMinY(), bounds.getMaxX(), bounds.getMinY()));
		if (intersections[0].state == State.Point) countIntersections++;
		
		// left
		intersections[1] = intersectLineLine(line, new Line2D.Double(bounds.getMinX(), bounds.getMinY(), bounds.getMinX(), bounds.getMaxY()));
		if (intersections[1].state == State.Point) countIntersections++;
		
		// CAN'T STOP HERE: NEED ALL INTERSECTIONS TO BE FILLED IN
		//if (countIntersections == 2) return countIntersections; // max 2 intersections
		
		// bottom
		intersections[2] = intersectLineLine(line, new Line2D.Double(bounds.getMinX(), bounds.getMaxY(), bounds.getMaxX(), bounds.getMaxY()));
		if (intersections[2].state == State.Point) countIntersections++;
		
		// right
		intersections[3] = intersectLineLine(line, new Line2D.Double(bounds.getMaxX(), bounds.getMinY(), bounds.getMaxX(), bounds.getMaxY()));
		if (intersections[3].state == State.Point) countIntersections++;
		
		return countIntersections; 
	}
	

    /**
     * Move an endpoint of a line to outside the given bounds if it is within them.  Moves it to the nearest corner if wrapNormal is true, 
     * otherwise to the corner opposite the nearest.
     * 
     * @param line the line to test 
     * @param rectangle the rectangular bounds to move the line out of
     * @param rerouteBuffer the buffer to place between the rectangle corner and the new point position 
     * @param wrapNormal whether to wrap around the closest corner to the endpoint (if true) or the opposite corner (if false)
     * @return an array of two points: the original point and the new location 
     */
    public Point2D moveEndPoint(Point2D oldPoint, Rectangle2D rectangle, double rerouteBuffer, boolean wrapNormal) {
    	Rectangle2D bounds = rectangle.getBounds2D();
    	
 
    	// only add 1, if we add morph buffer could get thrashing by routing the endpoint back into the item it came from

    	Point2D newPoint;
    	
		if (wrapNormal) {
	    	// reroute around the opposite corner of the bounds from where the endpoint currently sits in the bounds
			// top
			if (oldPoint.getY() < bounds.getCenterY()) {
	    		// left
				if (oldPoint.getX() < bounds.getCenterX()) {
	    			// bottom right
	    			newPoint  = new Point2D.Double(bounds.getMaxX()+rerouteBuffer, bounds.getMaxY()+rerouteBuffer);
	    		} else {
	    			// bottom left
	    			newPoint = new Point2D.Double(bounds.getMinX()-rerouteBuffer, bounds.getMaxY()+rerouteBuffer);
	    		}
			} else {
				// bottom
				if (oldPoint.getX() < bounds.getCenterX()) {
					// top right
					newPoint = new Point2D.Double(bounds.getMaxX()+rerouteBuffer, bounds.getMinY()-rerouteBuffer);    				
				} else {
					// top left
					newPoint = new Point2D.Double(bounds.getMinX()-rerouteBuffer, bounds.getMinY()-rerouteBuffer);
				}
			}
		} else {
	    	// reroute around the closest corner of the bounds from where the endpoint currently sits in the bounds
			// top
			if (oldPoint.getY() < bounds.getCenterY()) {
	    		// left
				if (oldPoint.getX() < bounds.getCenterX()) {
					// top left
					newPoint = new Point2D.Double(bounds.getMinX()-rerouteBuffer, bounds.getMinY()-rerouteBuffer);
	    		} else {
					// top right
					newPoint = new Point2D.Double(bounds.getMaxX()+rerouteBuffer, bounds.getMinY()-rerouteBuffer);    				
	    		}
			} else {
				// bottom
				if (oldPoint.getX() < bounds.getCenterX()) {
	    			// bottom left
	    			newPoint = new Point2D.Double(bounds.getMinX()-rerouteBuffer, bounds.getMaxY()+rerouteBuffer);
				} else {
					// bottom right
	    			newPoint = new Point2D.Double(bounds.getMaxX()+rerouteBuffer, bounds.getMaxY()+rerouteBuffer);
				}
			}
		}
		
		return newPoint;
    }
    
    /**
     * Find an appropriate split point in the line to wrap the line around the given rectangle.
     * 
     * @param line the line to split
     * @param rectangle the rectangle which intersects the line exactly twice
     * @param rerouteBuffer the buffer to place between the selected reroute corner and the new point
     * @param intersections the intersections of the line with each of the rectangle edges
     * @param wrapNormal whether to wrap around the closest corner (if true) or the opposite corner (if false)
     * @return the position of the new endpoint 
     */
    public Point2D rerouteLine(Line2D line, Rectangle2D rectangle, double rerouteBuffer, Intersection[] intersections, boolean wrapNormal) {
    	Rectangle2D bounds = rectangle.getBounds2D();
    	
    	Intersection topIntersect = intersections[0];
    	Intersection leftIntersect = intersections[1];
    	Intersection bottomIntersect = intersections[2];
    	Intersection rightIntersect = intersections[3];

    	// wrap around the most efficient way
		if (wrapNormal) {
	    	if (leftIntersect.state == State.Point) {
				if (topIntersect.state == State.Point) {
					// triangle, must go around top left
					return new Point2D.Double(rectangle.getMinX()-rerouteBuffer, rectangle.getMinY()-rerouteBuffer);
				}
				if (bottomIntersect.state == State.Point) {
					// triangle, must go around bottom left
					return new Point2D.Double(rectangle.getMinX()-rerouteBuffer, rectangle.getMaxY()+rerouteBuffer);
				}
				// else through left to right, calculate areas
				double totalArea = bounds.getHeight() * bounds.getWidth();
				// top area
				double topArea = bounds.getWidth() * (((leftIntersect.getY()-bounds.getY()) + (rightIntersect.getY()-bounds.getY()))/2);
				if (topArea < totalArea/2) {
					// go around top (the side which would make a greater movement)
					if (leftIntersect.getY() > rightIntersect.getY()) {
						// top left
						return new Point2D.Double(rectangle.getMinX()-rerouteBuffer, rectangle.getMinY()-rerouteBuffer);
					} else {
						// top right
						return new Point2D.Double(rectangle.getMaxX()+rerouteBuffer, rectangle.getMinY()-rerouteBuffer);
					}
				} else {
					// go around bottom
					if (leftIntersect.getY() < rightIntersect.getY()) {
						// bottom left
						return new Point2D.Double(rectangle.getMinX()-rerouteBuffer, rectangle.getMaxY()+rerouteBuffer);
					} else {
						// bottom right
						return new Point2D.Double(rectangle.getMaxX()+rerouteBuffer, rectangle.getMaxY()+rerouteBuffer);
					}
				}
			} else {
				if (rightIntersect.state == State.Point) {
					if (topIntersect.state == State.Point) {
						// triangle, must go around top right
						return new Point2D.Double(rectangle.getMaxX()+rerouteBuffer, rectangle.getMinY()-rerouteBuffer);
					}
					if (bottomIntersect.state == State.Point) {
						// triangle, must go around bottom right
						return new Point2D.Double(rectangle.getMaxX()+rerouteBuffer, rectangle.getMaxY()+rerouteBuffer);
					}
				} else {
					// else through top to bottom, calculate areas
					double totalArea = bounds.getHeight() * bounds.getWidth();
					// top area
					double leftArea = bounds.getHeight() * (((topIntersect.getX()-bounds.getX()) + (rightIntersect.getX()-bounds.getX()))/2);
					if (leftArea < totalArea/2) {
						// go around left
						if (topIntersect.getX() > bottomIntersect.getX()) {
							// top left
							return new Point2D.Double(rectangle.getMinX()-rerouteBuffer, rectangle.getMinY()-rerouteBuffer);
						} else {
							// bottom left
							return new Point2D.Double(rectangle.getMinX()-rerouteBuffer, rectangle.getMaxY()+rerouteBuffer);
						}
					} else {
						// go around right
						if (topIntersect.getX() < bottomIntersect.getX()) {
							// top right
							return new Point2D.Double(rectangle.getMaxX()+rerouteBuffer, rectangle.getMinY()-rerouteBuffer);
						} else {
							// bottom right
							return new Point2D.Double(rectangle.getMaxX()+rerouteBuffer, rectangle.getMaxY()+rerouteBuffer);
						}
						
					}
				}
			}
		} else {
			// wrap around opposite (usually because the first move caused a problem
	    	if (leftIntersect.state == State.Point) {
				if (topIntersect.state == State.Point) {
					// triangle, must go around bottom right
					return new Point2D.Double(rectangle.getMaxX()+rerouteBuffer, rectangle.getMaxY()+rerouteBuffer);
				}
				if (bottomIntersect.state == State.Point) {
					// triangle, must go around top right
					return new Point2D.Double(rectangle.getMaxX()+rerouteBuffer, rectangle.getMinY()-rerouteBuffer);
				}
				// else through left to right, calculate areas
				double totalArea = bounds.getHeight() * bounds.getWidth();
				// top area
				double topArea = bounds.getWidth() * (((leftIntersect.getY()-bounds.getY()) + (rightIntersect.getY()-bounds.getY()))/2);
				if (topArea < totalArea/2) {
					// go around bottom (the side which would make a lesser movement)
					if (leftIntersect.getY() > rightIntersect.getY()) {
						// bottom right
						return new Point2D.Double(rectangle.getMaxX()+rerouteBuffer, rectangle.getMaxY()+rerouteBuffer);
					} else {
						// bottom left
						return new Point2D.Double(rectangle.getMinX()-rerouteBuffer, rectangle.getMaxY()+rerouteBuffer);
					}
				} else {
					// go around top
					if (leftIntersect.getY() < rightIntersect.getY()) {
						// top right
						return new Point2D.Double(rectangle.getMaxX()+rerouteBuffer, rectangle.getMinY()-rerouteBuffer);						
					} else {
						// top left
						return new Point2D.Double(rectangle.getMinX()-rerouteBuffer, rectangle.getMinY()-rerouteBuffer);
					}
				}
			} else {
				if (rightIntersect.state == State.Point) {
					if (topIntersect.state == State.Point) {
						// triangle, must go around bottom left
						return new Point2D.Double(rectangle.getMinX()-rerouteBuffer, rectangle.getMaxY()+rerouteBuffer);
					}
					if (bottomIntersect.state == State.Point) {
						// triangle, must go around top left
						return new Point2D.Double(rectangle.getMinX()-rerouteBuffer, rectangle.getMinY()-rerouteBuffer);
					}
				} else {
					// else through top to bottom, calculate areas
					double totalArea = bounds.getHeight() * bounds.getWidth();
					// left area
					double leftArea = bounds.getHeight() * (((topIntersect.getX()-bounds.getX()) + (rightIntersect.getX()-bounds.getX()))/2);
					if (leftArea < totalArea/2) {
						// go around right
						if (topIntersect.getX() > bottomIntersect.getX()) {
							// bottom right
							return new Point2D.Double(rectangle.getMaxX()+rerouteBuffer, rectangle.getMaxY()+rerouteBuffer);
						} else {
							// top right
							return new Point2D.Double(rectangle.getMaxX()+rerouteBuffer, rectangle.getMinY()-rerouteBuffer);
						}
					} else {
						// go around left
						if (topIntersect.getX() < bottomIntersect.getX()) {
							// bottom left
							return new Point2D.Double(rectangle.getMinX()-rerouteBuffer, rectangle.getMaxY()+rerouteBuffer);
						} else {
							// top left
							return new Point2D.Double(rectangle.getMinX()-rerouteBuffer, rectangle.getMinY()-rerouteBuffer);
						}
					}
				}
			}
	    }
		
		// will only get here if intersection was along edge (parallel) or at a corner
		return null;
    }
    
    static class Intersection extends Point2D.Double {
    	enum State {Point, Parallel, Coincident, None};

    	State state = State.Point;
    	
    	public Intersection(double x, double y) {
    		super(x, y);
    		state = State.Point;
    	}
    	
    	public Intersection(State state) {
    		this.state = state;
    	}
    }
   
    public AggregateLabelLayout createLabelLayout(String group, String labelField) {
    	aggregateLabelLayout = new AggregateLabelLayout(group, labelField);
    	return aggregateLabelLayout;
    }
   
    public AggregateLabelLayout getLabelLayout() {
    	return aggregateLabelLayout;
    }
    
    /**
     * Positions aggregate labels (DecoratorItems) along the longest virtual edge in the aggregate.  Rotates
     * label up to +-45 degress to follow the edge.  Label size is modulated to fit within the length of the edge.
     * @author Christopher
     */    
    public class AggregateLabelLayout extends ItemAction {

    	private Font labelFont = new Font("Verdana", Font.BOLD, 10);
    	private String titleField;
    		
    	public AggregateLabelLayout(String group, String titleField) {
    		super(group);
    		this.titleField = titleField;
		}
    	
    	public void setFont(Font labelFont) {
    		this.labelFont = labelFont;
    	}

		@Override
		public void process(VisualItem visualItem, double frac) {
			DecoratorItem di = (DecoratorItem) visualItem;
			AggregateItem aItem = (AggregateItem) di.getDecoratedItem();

			Iterator aNodeItems = aItem.items();
			Line2D longestLine = new Line2D.Double();
			double maxLength = -1 * Double.MAX_VALUE;
			
			// set the font size according to the available space
			// get a graphics context
			Graphics g = m_vis.getDisplay(0).getGraphics();
			if (g==null) {
				g = (new BufferedImage(2, 2, BufferedImage.TYPE_4BYTE_ABGR_PRE)).getGraphics();
			}

			// min label length
			labelFont = labelFont.deriveFont(MIN_LABEL_SIZE);
			FontMetrics fm = g.getFontMetrics(labelFont);
			
			// START WITH A LINE ACROSS THE TOP OF THE FIRST ITEM;  keep this if no virtual edge is longer than the top of the first item
			if (aNodeItems.hasNext()) {
				VisualItem item = (VisualItem) aNodeItems.next();
				longestLine = new Line2D.Double(item.getBounds().getX(), item.getBounds().getY()-nodeR0/2, item.getBounds().getMaxX(), item.getBounds().getY()-nodeR0/2);
				maxLength = longestLine.getP1().distance(longestLine.getP2());
			}
			
			double length = 0;
			double labelWidth = fm.stringWidth(di.getString(titleField));
			
			// increase to max font size that would fit across size across top
			while ((fm.stringWidth(di.getString(titleField)) < maxLength) && (labelFont.getSize() < MAX_LABEL_SIZE)) {
				// enlarge as much as possible
				labelFont = labelFont.deriveFont(labelFont.getSize2D()+0.5f);
				fm = g.getFontMetrics(labelFont);
				labelWidth = fm.stringWidth(di.getString(titleField));
			}
			
			// raise by descent of final font
			longestLine.setLine(longestLine.getP1().getX(), longestLine.getP1().getY()-fm.getDescent(), longestLine.getP2().getX(), longestLine.getP2().getY()-fm.getDescent());
			
			// get the items in this aggregate
			aNodeItems = aItem.items();
			
			while (aNodeItems.hasNext()) {
				// for each item in the aggregate
				VisualItem item = (VisualItem) aNodeItems.next();
				// get the virtual edges of this item
				Deque<Line2D> edges = (Deque<Line2D>) item.get(VIRTUAL_EDGES);
				// for all virtual edges of this item
				for (Line2D edge : edges) {
					// calculate length of edge
					length = edge.getP1().distance(edge.getP2());  
					// if half this edge is > current max, test further
					if (length/2 >= maxLength) {
						//check if starting point of label at current font size would be within item bounds
						Point2D testPoint = MyGraphicsLib.interpolateLine2D(edge, 0.5 - ((labelWidth / length) / 2.0));
						if (!item.getBounds().contains(testPoint)) {
							while (!item.getBounds().contains(testPoint)  && (labelFont.getSize() < MAX_LABEL_SIZE)) {
								// enlarge as much as possible
								labelFont = labelFont.deriveFont(labelFont.getSize2D()+0.5f);
								fm = g.getFontMetrics(labelFont);
								labelWidth = fm.stringWidth(di.getString(titleField));
								testPoint = MyGraphicsLib.interpolateLine2D(edge, 0.5 - ((labelWidth / length) / 2.0));
							}
							// step down by one from the largest size found 
							labelFont = labelFont.deriveFont(labelFont.getSize2D()-3f);
							fm = g.getFontMetrics(labelFont);
							labelWidth = fm.stringWidth(di.getString(titleField));
							maxLength = length/2;
							longestLine = edge;
						}
					}
				}
			}
		
			// set decorator at middle
			di.setX((longestLine.getP1().getX() + longestLine.getP2().getX()) / 2);
			di.setY((longestLine.getP1().getY() + longestLine.getP2().getY()) / 2);
			// rotate along line
			double rotation = Math.toDegrees(Math.atan2(longestLine.getY2()-longestLine.getY1(), longestLine.getX2()-longestLine.getX1()));
		
			di.setDouble(RotationLabelRenderer.ROTATION, rotation);
			di.setFont(labelFont);
		}
    }
    
} // end of class ConcaveHullLayout