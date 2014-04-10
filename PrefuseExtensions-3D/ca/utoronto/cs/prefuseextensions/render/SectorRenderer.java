/* CVS $Id: SectorRenderer.java,v 1.2 2010/01/13 19:18:49 cmcollin Exp $ */
package ca.utoronto.cs.prefuseextensions.render;

import java.awt.Shape;

import ca.utoronto.cs.prefuseextensions.geom.Sector2D;

import prefuse.data.Schema;
import prefuse.render.AbstractShapeRenderer;
import prefuse.visual.VisualItem;

public class SectorRenderer extends AbstractShapeRenderer {

	public static final String ANGLE_EXTENT = "angleExtent";
	public static final String START_ANGLE = "startAngle";
	public static final String INNER_RADIUS = "innerRadius";
	public static final String OUTER_RADIUS = "outerRadius";
	public static final String ANGLE_FACTOR = "angleFactor";
	
    private double startAngle = 0;
    private double angleExtent = 360; 
    private double innerRadius = 10;
    private double outerRadius = 40;
    
    private Sector2D sector2D = new Sector2D();
    
    public static final Schema SECTOR_SCHEMA = new Schema();
    static {
        SECTOR_SCHEMA.addColumn(START_ANGLE, double.class);
        SECTOR_SCHEMA.addColumn(ANGLE_EXTENT, double.class);
        SECTOR_SCHEMA.addColumn(INNER_RADIUS, double.class);
        SECTOR_SCHEMA.addColumn(OUTER_RADIUS, double.class);
        SECTOR_SCHEMA.addColumn(ANGLE_FACTOR, double.class, 1.0);
    }
    
    public SectorRenderer() {
        super();
    }

    public SectorRenderer(double startAngle, double angleExtent, double innerRadius, double outerRadius) {
        this.startAngle = startAngle;
        this.angleExtent = angleExtent;
        this.innerRadius = innerRadius;
        this.outerRadius = outerRadius;
    }
    
    public void setStartAngle(double angle) {
        this.startAngle = angle;
    }
    
    public double getStartAngle() {
        return startAngle;
    }
    
    public void setAngleExtent(double angle) {
        this.angleExtent = angle;
    }

    public double getAngleExtent() {
        return angleExtent;
    }
    
    public void setInnerRadius(double innerRadius) {
        this.innerRadius = innerRadius;
    }
    
    public double getInnerRadius() {
        return innerRadius;
    }
    
    public void setOuterRadius(double outerRadius) {
        this.outerRadius = outerRadius;
    }
    
    public double getOuterRadius() {
        return outerRadius;
    }
    
    protected Shape getRawShape(VisualItem item) {
        double x = item.getX();
        if ( Double.isNaN(x) || Double.isInfinite(x) )
            x = 0;
        double y = item.getY();
        if ( Double.isNaN(y) || Double.isInfinite(y) )
            y = 0;
        
        sector2D.setSectorByCenter(x, y, 
                item.getDouble(INNER_RADIUS), 
                item.getDouble(OUTER_RADIUS),
                item.getDouble(START_ANGLE), 
                item.getDouble(ANGLE_EXTENT));
        return sector2D;
    }
}
