/* CVS $Id: Sector2D.java,v 1.1 2007/02/05 00:30:28 cmcollin Exp $ */
package ca.utoronto.cs.prefuseextensions.geom;

import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

/**
 * Sector2D is the class for all objects that store a 2D sector defined by a
 * outer bounding rectangle, width, start angle, and angular extent (length of
 * the arc).
 * 
 * The bounding rectangle defines the outer boundary of the full ellipse of
 * which the outer arc is a partial section. The width defines the width of the
 * sector between the inner arc and outer arc. The width is used to calculate an
 * inner bounding rectangle which defines the outer boundary of the full ellipse
 * of which the inner arc is a partial section.
 * 
 * The angles are specified relative to the non-square extents of the bounding
 * rectangles such that 45 degrees always falls on the line from the center of
 * the ellipses to the upper right corner of the bounding rectangles. As a
 * result, if the bounding rectangles are noticeably longer along one axis than
 * the other, the angles to the start and end of the arc segment will be skewed
 * farther along the longer axis of the bounds.
 * 
 * The actual storage representation of the coordinates is maintained by the
 * Area superclass and 2 Arc2D objects.
 * 
 * @author Christopher Collins
 * @version $Revision: 1.1 $
 *  
 */
public class Sector2D extends Area implements Shape, Cloneable {

    /**
     * Maintain sector width as constant when drawing elliptical sectors.
     * 
     * @see #setEllipseMethod(int)
     */
    public static final int CONSTANT_SECTOR_WIDTH = 0;
    
    /**
     * Alter sector width to be proportional between height and width of framing rectangle
     * when drawing elliptical sectors.
     * 
     * @see #setEllipseMethod(int)
     */
    public static final int PROPORTIONAL_SECTOR_WIDTH = 1;
    
    /**
     * The current drawing method for elliptical sectors.  
     * 
     * @see #setEllipseMethod(int)    
     */
    private int ellipseMethod;
    
    /**
     * The threshold for equality between <code>double</code> values x and y: if |x-y/x| > threshold
     * then they are not considered equal.
     */
    private static final double EQUALS_THRESHOLD = 0.000000001;
    
    /**
     * The outer (pie-shaped) arc.
     */
    private Arc2D outerArc;

    /**
     * The inner (pie-shaped) arc.
     */
    private Arc2D innerArc;

    /**
     * The inner ellipse to subtract (workaround for Java bug 4253189)
     */
   
    private Ellipse2D e = new Ellipse2D.Double();
    
    // CONSTRUCTORS

    /**
     * Creates an empty sector2D.
     */
    public Sector2D() {
        super();
        init();
    }

    /**
     * Creates a Sector2D with dimensions the same as a given Sector2D.
     * 
     * @param sector
     *            the Sector2D whose dimensions will be copied
     */
    public Sector2D(Sector2D sector) {
        super();
        init();
        outerArc.setArc(sector.getOuterArc());
        innerArc.setArc(sector.getInnerArc());
        update();
    }

    /**
     * Creates a Sector2D with the given inner and outer arcs as the bounds.
     * 
     * @param innerArc
     *            the inner arc which specifies the inner radius and angles of
     *            the sector
     * @param outerArc
     *            the outer arc which specifies the outer radius and angles of
     *            the sector
     * 
     * @throws IllegalArgumentException
     *             if the arcs do not span the same angles or if the innerArc
     *             radius is not less than or equal to outerArc radius.
     */
    public Sector2D(Arc2D innerArc, Arc2D outerArc)
            throws IllegalArgumentException {
        super();
        init();
        this.outerArc.setArc(outerArc);
        // confirms compatibility of arcs
        setInnerArc(innerArc);
        update();
    }

    /**
     * Creates a Sector2D within the given rectangular bounds, start angle,
     * angle extents, and width. The outer arc will fit within the bounding box
     * and the width is calculated inward.
     * 
     * @param ellipseBounds
     *            the bounded rectangle for the outer arc of this sector2D
     * @param startAngle
     *            the angle to start this Sector2D
     * @param angleExtent
     *            the angular width of this Sector2D
     * @param width
     *            the width of the Sector2D, extending inward from the outer arc
     * 
     * @throws IllegalArgumentException
     *             if the <code>width</code> exceeds the radius of the outer
     *             arc (half the width of the <code>ellipseBounds</code>
     */
    public Sector2D(Rectangle2D ellipseBounds, double startAngle,
            double angleExtent, double width) throws IllegalArgumentException {
        super();
        init();
        outerArc.setArc(ellipseBounds, startAngle, angleExtent, Arc2D.PIE);
        setSectorWidth(width);
        update();
    }

    /**
     * Creates a Sector2D within the given rectangular bounds, start angle,
     * angle extents, and width. The outer arc will fit within the bounding box
     * and the width is calculated inward.
     * 
     * @param x
     *            the x-coordinate of the upper-left corner of the bounding
     *            rectangle
     * @param y
     *            the y-coordinate of the upper-left corner of the bounding
     *            rectangle
     * @param w
     *            the width of the bounding rectangle
     * @param h
     *            the height of the bounding rectangle
     * @param startAngle
     *            the angle to start this Sector2D
     * @param angleExtent
     *            the angular width of this Sector2D
     * @param width
     *            the width of this Sector2D, extending inward from the outer
     *            arc
     * 
     * @throws IllegalArgumentException
     *             if the <code>width</code> exceeds the radius of the outer
     *             arc (half the rectangle width <code>w</code>
     */
    public Sector2D(double x, double y, double w, double h, double startAngle,
            double angleExtent, double width) throws IllegalArgumentException {
        super();
        init();
        outerArc.setArc(x, y, w, h, startAngle, angleExtent, Arc2D.PIE);
        setSectorWidth(width);
        update();
    }

    /**
     * Creates a Sector2D centered at the given coordinates and with the
     * specified inner and outer radii and angular span.
     * 
     * @param centerX
     *            the x-coordinate of the centre of bounding box of this
     *            Sector2D
     * @param centerY
     *            the y-coordinate of the centre of bounding box of this
     *            Sector2D
     * @param innerRadius
     *            the radius of the inner arc of this Sector2D
     * @param outerRadius
     *            the radius of the outer arc of this Sector2D
     * @param startAngle
     *            the angle to start this Sector2D
     * @param angleExtent
     *            the angular width of this Sector2D
     * 
     * @throws IllegalArgumentException
     *             if the <code>innerRadius</code> is greater than the
     *             <code>outerRadius</code>
     */
    public Sector2D(double centerX, double centerY, double innerRadius,
            double outerRadius, double startAngle, double angleExtent)
            throws IllegalArgumentException {
        super();
        init();

        if (innerRadius > outerRadius)
            throw new IllegalArgumentException(
                    "Outer radius must be greater than or equal to inner radius.");

        outerArc.setArcByCenter(centerX, centerY, outerRadius, startAngle, angleExtent,
                Arc2D.PIE);
        innerArc.setArcByCenter(centerX, centerY, innerRadius, startAngle, angleExtent,
                Arc2D.PIE);
        update();
    }

    /**
     * Initialize all instance fields to empty values.
     */
    private void init() {
        innerArc = new Arc2D.Double();
        outerArc = new Arc2D.Double();
        ellipseMethod = PROPORTIONAL_SECTOR_WIDTH;
    }

    /**
     * Reset and recreate the (@link Area) which backs this Sector2D. Required
     * because objects are added to an (@link Area) by value. Changes to
     * dimensions of this Sector2D are not propagated to the (@link Area)
     * without resetting.
     * 
     * <p> FIXME Note workaround included here to expand angle of innerArc
     * to avoid outline appearing to center.  Workaround causes potential slowdown. 
     */
    private void update() {
        this.reset();
        if (outerArc.getAngleExtent() >= 360) {
            e.setFrame(outerArc.getX(), outerArc.getY(), outerArc.getWidth(), outerArc.getHeight());
            this.add(new Area(e));
        } else 
            this.add(new Area(outerArc));

        //removed to workaround java extraneous lines bug 4253189
        //innerArc.setAngleStart(outerArc.getAngleStart()-360);
        //innerArc.setAngleExtent(outerArc.getAngleExtent()+720);
        
        e.setFrame(innerArc.getX(), innerArc.getY(), innerArc.getWidth(), innerArc.getHeight());
        this.subtract(new Area(e));
        innerArc.setAngleStart(outerArc.getAngleStart());
        innerArc.setAngleExtent(outerArc.getAngleExtent());
    }

    /**
     * Create a copy of this Sector2D.
     * 
     * @return A copy of this Sector2D cast to Object.
     */
    public Object clone() {
        return new Sector2D(this);
    }

    // ARC RELATED METHODS

    /**
     * Sets the inner arc of this Sector2D to the dimensions of the given arc.  
     * 
     * @param innerArc
     *            the new arc to set this inner arc to match
     * 
     * @throws IllegalArgumentException
     *             if <code>innerArc</code> is not a PIE arc of angles the same as the
     *             outer arc and bounding box within that of the outer arc.
     */
    public void setInnerArc(Arc2D innerArc) throws IllegalArgumentException {
        if (innerArc.getArcType() != Arc2D.PIE)
            throw new IllegalArgumentException("Inner arc type must be PIE");
       if ((!doubleEquals(outerArc.getCenterX(), innerArc.getCenterX()))
                || (!doubleEquals(outerArc.getCenterY(), innerArc.getCenterY()))
                || (outerArc.getWidth() < innerArc.getWidth())
                || (outerArc.getHeight() < innerArc.getHeight()))
            throw new IllegalArgumentException(
                    "Inner arc must be subset of outer arc. "
                            + getErrorData(innerArc, outerArc));
        if ((innerArc.getAngleExtent() != outerArc.getAngleExtent())
                || (innerArc.getAngleStart() != outerArc.getAngleStart()))
            throw new IllegalArgumentException(
                    "Inner arc must span same angle as outer arc. "
                            + getErrorData(innerArc, outerArc));
        this.innerArc.setArc(innerArc);
        update();
    }

    /**
     * Get the Arc2D (PIE shaped) which forms the inner arc of this Sector2D.
     * 
     * @return the Arc2D which forms the inner arc of this Sector2D.
     */
    public Arc2D getInnerArc() {
        return innerArc;
    }

    /**
     * Sets the outer arc of this Sector2D to the dimensions of the given arc.  Sets the angular
     * dimensions of the entire sector to the those of <code>outerArc</code>.  
     * 
     * @param outerArc
     *            the new arc to set this outer arc to match
     * 
     * @throws IllegalArgumentException
     *             if <code>outerArc</code> is not a PIE arc with a bounding box which 
     * 			   contains that of the inner arc.
     */
    public void setOuterArc(Arc2D outerArc) throws IllegalArgumentException {
        if (outerArc.getArcType() != Arc2D.PIE)
            throw new IllegalArgumentException("Outer arc type must be PIE");
        if ((!doubleEquals(outerArc.getCenterX(), innerArc.getCenterX()))
                || (!doubleEquals(outerArc.getCenterY(),innerArc.getCenterY()))
                || (outerArc.getWidth() < innerArc.getWidth())
                || (outerArc.getHeight() < innerArc.getHeight()))
            throw new IllegalArgumentException(
                    "Inner arc must be subset of outer arc. "
                            + getErrorData(innerArc, outerArc));
        this.outerArc.setArc(outerArc);
        update();
    }

    /**
     * Get the Arc2D (PIE shaped) which forms the outer arc of this Sector2D
     * 
     * @return the Arc2D which forms the outer arc of this Sector2D
     */
    public Arc2D getOuterArc() {
        return outerArc;
    }

    /**
     * Sets the outer radius this Sector2D to the given radius. Resets the dimensions to square.
     * 
     * @param outerRadius
     *            the new value for the outer radius of this Sector2D
     * 
     * @throws IllegalArgumentException
     *             if <code>outerRadius</code> is not greater than or equal to the current inner radius
     */
    public void setOuterRadius(double outerRadius)
            throws IllegalArgumentException {
        if (outerRadius < getInnerRadius())
            throw new IllegalArgumentException(
                    "Outer radius must be greater than or equal to inner radius.");
        outerArc.setArcByCenter(outerArc.getCenterX(), outerArc.getCenterY(), outerRadius, outerArc.getAngleStart(), outerArc.getAngleExtent(),
                Arc2D.PIE);
        update();
    }

    /**
     * Get the radius of the ellipse for which the outer arc is a part.
     * 
     * @return radius of the ellipse which the outer arc is part of
     */
    public double getOuterRadius() {
        if (outerArc.getWidth() != outerArc.getHeight()) 
            throw new IllegalStateException("X and Y radii different, method only valid for circular sectors."); 
        return outerArc.getWidth() / 2;
    }

    /**
     * Sets the inner and outer radii this Sector2D to the given values.
     * 
     * @param innerRadius the new value for the inner radius of this Sector2D
     * @param outerRadius
     *            the new value for the outer radius of this Sector2D
     * 
     * @throws IllegalArgumentException
     *             if the given outer radius is not greater than or equal to the given inner radius
     */    
    public void setRadii(double innerRadius, double outerRadius)
            throws IllegalArgumentException {
        if (outerRadius < innerRadius)
            throw new IllegalArgumentException(
                    "Outer radius must be greater than or equal to inner radius.");
        double x = innerArc.getCenterX();
        double y = innerArc.getCenterY();
        innerArc.setArcByCenter(x, y, innerRadius, getAngleStart(), getAngleExtent(),
                Arc2D.PIE);
        outerArc.setArcByCenter(x, y, outerRadius, getAngleStart(), getAngleExtent(),
                Arc2D.PIE);
        update();
    }

    /**
     * Sets the inner radius this Sector2D to the given value.
     * 
     * @param innerRadius
     *            the new value for the inner radius of this Sector2D
     * 
     * @throws IllegalArgumentException
     *             if <code>innerRadius</code> is greater than the current outer radius
     */
    public void setInnerRadius(double innerRadius) throws IllegalArgumentException {
        if (innerRadius > getOuterRadius())
            throw new IllegalArgumentException(
                    "Outer radius must be greater than or equal to inner radius.");
        innerArc.setArcByCenter(innerArc.getCenterX(), innerArc.getCenterY(), innerRadius, getAngleStart(), getAngleExtent(),
                Arc2D.PIE);
        update();
    }

    /**
     * Get the radius of the ellipse for which the inner arc is a part.
     *  
     * @return radius of the ellipse which the inner arc is part of
     * @throws IllegalStateException if the x and y radii are different (sector is elliptical).
     */
    public double getInnerRadius() {
        if (innerArc.getWidth() != innerArc.getHeight()) 
            throw new IllegalStateException("X and Y radii different, method only valid for circular sectors."); 
        return innerArc.getWidth() / 2;
    }

    /**
     * Scales the bounding frame up or down by the absolute value <code>scaleAmount</code>.  The sector center
     * is unchanged.
     * 
     * @param scaleAmount the amount to grow or shrink the width and height of the bounding frame
     */
    public void scale(double scaleAmount) {
        double scaleHeightAmount = scaleAmount;
        if (getEllipseMethod() == PROPORTIONAL_SECTOR_WIDTH)
            scaleHeightAmount = scaleAmount * (getHeight()/getWidth());
        setFrame(getX()-scaleAmount/2, getY()-scaleHeightAmount/2, getWidth()+scaleAmount, getHeight()+scaleHeightAmount);
    }
    
    /**
     * Scales the bounding frame up or down by the absolute value <code>scaleAmount</code>.  The sector center
     * is unchanged.  The sector width is simultaneously reset to <code>newSectorWidth</code>.
     * 
     * @param scaleAmount the amount to grow or shrink the width and height of the bounding frame
     * @param newSectorWidth the sector width for the scaled sector
     */
    public void scale(double scaleAmount, double newSectorWidth) {
        if (scaleAmount < 0) {
            setSectorWidth(newSectorWidth);
        }
        double scaleHeightAmount = scaleAmount;
        if (getEllipseMethod() == PROPORTIONAL_SECTOR_WIDTH)
            scaleHeightAmount = scaleAmount * (getHeight()/getWidth());
        setFrame(getX()-scaleAmount/2, getY()-scaleHeightAmount/2, getWidth()+scaleAmount, getHeight()+scaleHeightAmount);
        if (scaleAmount >= 0) {
            setSectorWidth(newSectorWidth);
        }
    }
    
    /**
     * Sets the dimensions of this sector as given.
     * 
     * @param x
     *            the x-coordinate of the upper-left corner of the bounding
     *            rectangle
     * @param y
     *            the y-coordinate of the upper-left corner of the bounding
     *            rectangle
     * @param w
     *            the width of the bounding rectangle
     * @param h
     *            the height of the bounding rectangle
     * @param startAngle
     *            the angle to start this Sector2D
     * @param angleExtent
     *            the angular width of this Sector2D
     * @param width
     *            the width of this Sector2D, extending inward from the outer
     *            arc
     * 
     * @throws IllegalArgumentException
     *             if double (sector) <code>width</code> would exceed outer bounding box or if
     *             <code>width</code> is less than zero.
     * 
     * @see java.awt.geom.Arc2D#setArc(double, double, double, double, double,
     *      double, int)
     */
    public void setSector(double x, double y, double w, double h,
            double startAngle, double angleExtent, double width) throws IllegalArgumentException {
        outerArc.setArc(x, y, w, h, startAngle, angleExtent, Arc2D.PIE);
        setSectorWidth(width);
    }

    /**
     * Set this sector centered at the given coordinates and with the
     * specified inner and outer radii and angular span.  Sector forms part of a circle (bounding 
     * box is a square).
     * 
     * 
     * @param centerX
     *            the x-coordinate of the centre of bounding box of this
     *            Sector2D
     * @param centerY
     *            the y-coordinate of the centre of bounding box of this
     *            Sector2D
     * @param innerRadius
     *            the radius of the inner arc of this Sector2D
     * @param outerRadius
     *            the radius of the outer arc of this Sector2D
     * @param startAngle
     *            the angle to start this Sector2D
     * @param angleExtent
     *            the angular width of this Sector2D
     * 
     * @throws IllegalArgumentException
     *             if the <code>innerRadius</code> is greater than the
     *             <code>outerRadius</code>     
     * 
     * @see java.awt.geom.Arc2D#setArc(double, double, double, double, double,
     *      double, int)
     */
    public void setSectorByCenter(double centerX, double centerY, double innerRadius,
            double outerRadius, double startAngle, double angleExtent)
            throws IllegalArgumentException {
        if (innerRadius > outerRadius) {
            throw new IllegalArgumentException(
                    "Inner arc must be subset of outer arc.");
        }
        
        innerArc.setArcByCenter(centerX, centerY, innerRadius, startAngle, angleExtent,
                Arc2D.PIE);
        outerArc.setArcByCenter(centerX, centerY, outerRadius, startAngle, angleExtent,
                Arc2D.PIE);
        update();
    }

    /**
     * Sets the angle start to the given value.  0 is measured as horizontal (to the right).
     *
     * @param startAngle the new starting angle
     *  
     * @see java.awt.geom.Arc2D#setAngleStart(double)
     */
    public void setAngleStart(double startAngle) {
        outerArc.setAngleStart(startAngle);
        update();
    }

    /**
     * Sets this sectors angular span to the given value. 
     * 
     * @param angleExtent the new angular span for this sector
     * 
     * @see java.awt.geom.Arc2D#setAngleExtent(double)
     */
    public void setAngleExtent(double angleExtent) {
        outerArc.setAngleExtent(angleExtent);
        update();
    }

    /**
     * Set the width of this sector as specified. Outer radius and bounding box remain
     * unchanged.
     * 
     * @param width
     *            the width (inward from outer radius) to make this sector
     * @throws IllegalArgumentException
     *             if double sector width would exceed outer bounding box and bounding box width is > 0
     */
    public void setSectorWidth(double width) throws IllegalArgumentException {
        // 2 methods of displaying elliptical sectors; see notes 2 September
        // can have a constant sector width, requiring subtraction of entire inner ellipse
        // because angles will differ OR can have proportional sector width maintaining
        // angles
        double height = Integer.MAX_VALUE;
        switch (ellipseMethod) {
        	case (CONSTANT_SECTOR_WIDTH): { 
        	    height = width;
        	    break;
        	}
        	case (PROPORTIONAL_SECTOR_WIDTH): {
        	    height = (width/getWidth())*getHeight();
        	    break;
        	}
        }
        
        // QUES do we need this restriction?  i.e. must inner arc radius be >= 0?  Yes, sector width must be >= 0 and inner radius must be <= outer, but can't inner be < 0?  Arc2D allows this.
        if ((getWidth() > 0) && (width * 2 > getWidth()))
            throw new IllegalArgumentException("Specified width too large: " + (width) + "; maximum: " + (getWidth()/2) + ".");
        if ((getHeight() > 0) && (height * 2 > getHeight()))
            throw new IllegalArgumentException("Specified width too large: " + (width) + "; maximum: " + (getHeight()/2) + ".");
        
        // QUES removed this restriction because TreeMaps have some negative node widths -- why?
        //if (width < 0)
        //    throw new IllegalArgumentException(
        //           "Width must be greater than or equal to 0.");
        
        innerArc.setArc(getX() + width, getY() + height , getWidth() - width * 2,
                getHeight() - height * 2, getAngleStart(), getAngleExtent(), Arc2D.PIE);
        update();
    }

    /**
     * Sets the location and size of the outer bounds of this Sector2D to the specified rectangular values.
     * Sector width, angular start and angular extents are unchanged.  New width must be greater than
     * sector width.
     * 
     * @param x the coordinates to which to set the location of the upper left corner of the outer bounds of this Rectangle2D
     * @param y the coordinates to which to set the location of the upper left corner of the outer bounds of this Rectangle2D
     * @param w the width to set the outer bounding box of this Sector2D
     * @param h the height to set the outer bounding box of this Sector2D
     * 
     * @throws IllegalArgumentException if <code>w</code> is less than twice the sector width
     */
    public void setFrame(double x, double y, double w, double h) throws IllegalArgumentException {
        double sectorWidth = getSectorWidth();
        if ((w>0) && (sectorWidth > w/2)) 
            throw new IllegalArgumentException("New width (" + w + ") must be greater than twice the current sector width (" + sectorWidth*2 + ").");
        if ((h>0) && (sectorWidth > h/2)) 
            throw new IllegalArgumentException("New height (" + h + ") must be greater than twice the current sector width (" + sectorWidth*2 + ").");
        outerArc.setFrame(x, y, w, h);
        setSectorWidth(sectorWidth); // implicit reset of inner arc
        update();
    }
    
    /**
     * Sets this Sector to be the same as the specified Sector.
     * 
     * @param sector
     *            the sector to copy
     */
    public void setSector(Sector2D sector) {
        innerArc.setArc(sector.getInnerArc());
        outerArc.setArc(sector.getOuterArc());
        update();
    }

    // query methods all based on outer arc

    /**
     * Returns the angular extent of this sector. 
     * 
     * @return the angular span of this sector, in degrees
     */
    public double getAngleExtent() {
        return outerArc.getAngleExtent();
    }

    /**
     * Returns the angular start of this sector.  0 is measured as horizontal,
     * to the right.
     * 
     * @return the start angle of this sector, in degrees
     */
    public double getAngleStart() {
        return outerArc.getAngleStart();
    }

    /**
     * Returns the X coordinate of the upper left corner of the framing
     * rectangle in double precision.
     * 
     * @return the X coordinate of the upper left corner of the framing
     *         rectangle
     */
    public double getX() {
        return outerArc.getX();
    }

    /**
     * Returns the Y coordinate of the upper left corner of the framing
     * rectangle in double precision.
     * 
     * @return the Y coordinate of the upper left corner of the framing
     *         rectangle
     */
    public double getY() {
        return outerArc.getY();
    }

    /**
     * Returns the width of the framing rectangle in double precision.
     * 
     * @return the width of the framing rectangle
     */
    public double getWidth() {
        return outerArc.getWidth();
    }

    /**
     * Returns the height of the framing rectangle in double precision.
     * 
     * @return the height of the framing rectangle
     */
    public double getHeight() {
        return outerArc.getHeight();
    }

    /**
     * Returns the maximum x value (the right corner) of the bounding box which 
     * contains the ellipse which the outer arc of this sector is part.
     *  
     * @return the maximum x value, determined by the outer arc
     */
    public double getMaxX() {
        return outerArc.getMaxX();
    }

    /**
     * Returns the maximum y value (the lower corner) of the bounding box which 
     * contains the ellipse which the outer arc of this sector is part.
     *  
     * @return the maximum y value, determined by the outer arc
     */
    public double getMaxY() {
        return outerArc.getMaxX();
    }

    /**
     * Returns the x-coordinate of the center of the framing rectangle of the sector in <code>double</code> precision.
     * 
     * @return the x-coordinate of center of the framing rectangle of the sector
     */
    public double getCenterX() {
        return outerArc.getCenterX();
    }

    /**
     * Returns the y-coordinate of the center of the framing rectangle of the sector in <code>double</code> precision.
     * 
     * @return the y-coordinate of center of the framing rectangle of the sector
     */
    public double getCenterY() {
        return outerArc.getCenterY();
    }

    /**
     * Returns the framing {@link Rectangle2D} that defines the overall shape of this object.
     * 
     * @return a <code>Rectangle2D</code>, specified in double coordinates
     */
    public Rectangle2D getFrame() {
        return outerArc.getFrame();
    }

    /**
     * Returns the difference in radius between the outer and inner arcs forming
     * this sector.
     * 
     * @return the width of the sector
     */
    public double getSectorWidth() {
        return (outerArc.getWidth()-innerArc.getWidth())/2;
    }
    
    /**
     * Sets the ellipse drawing method.  
     * 
     * <p> Arcs forming Sectors contained in (non-square) rectangles are parts of ellipses.
     * Inner arc can be designated to maintain constant sector width (inner and outer angles
     * will not match because w/h proportions will differ) or proportional (changing) sector
     * width (default).  This behaviour is managed by the value of <code>ellipseMethod</code> and
     * (@link #setSectorWidth(double)).
     * 
     * @param ellipseMethod Can be either <code>CONSTANT_SECTOR_WIDTH</code> or <code>PROPORTIONAL_SECTOR_WIDTH</code>
     * @throws IllegalArgumentException if <code>ellipseMethod</code> is not one of <code>CONSTANT_SECTOR_WIDTH</code> or <code>PROPORTIONAL_SECTOR_WIDTH</code>
     */
    public void setEllipseMethod(int ellipseMethod) {
        if ((ellipseMethod != CONSTANT_SECTOR_WIDTH) && (ellipseMethod != PROPORTIONAL_SECTOR_WIDTH))
        	throw new IllegalArgumentException("Invalid ellipse method.  Value must be CONSTANT_SECTOR_WIDTH or PROPORTIONAL_SECTOR_WIDTH.");
        if (this.ellipseMethod != ellipseMethod) {
            this.ellipseMethod = ellipseMethod;
            setSectorWidth(getSectorWidth());
            update();
        }
    }
    
    /**
     * Gets the current method of drawing elliptical sectors.
     *   
     * @return the current method of drawing elliptical sectors, either <code>CONSTANT_SECTOR_WIDTH</code> or <code>PROPORTIONAL_SECTOR_WIDTH</code>
     */
    public int getEllipseMethod() {
        return ellipseMethod;
    }
    
    // UTILITY METHODS

    /**
     * Create debugging information about this Sector2D.
     * 
     * @param innerArc
     *            the inner Arc2D to debug
     * @param outerArc
     *            the outer Arc2D to debug
     * 
     * @return a String containing debugging information about the given arcs
     */
    private String getErrorData(Arc2D innerArc, Arc2D outerArc) {
        String message = new String();
        message = message.concat("Inner: (" + innerArc.getCenterX() + ", "
                + innerArc.getCenterY() + ", " + innerArc.getAngleStart()
                + ", " + innerArc.getAngleExtent() + ", " + innerArc.getWidth()
                / 2);
        message = message.concat(" Outer: (" + outerArc.getCenterX() + ", "
                + outerArc.getCenterY() + ", " + outerArc.getAngleStart()
                + ", " + outerArc.getAngleExtent() + ", " + outerArc.getWidth()
                / 2);
        return message;
    }
    
    /**
     * Performs an equals test for floting point numbers with some flexibility
     * (the <code>EQUALS_THRESHOLD</code>).  Required because resizing radial geometry
     * leads to rounding problems.
     * 
     * @param x the first number to compare
     * @param y the second number to compare
     * @return true if <code>x</code> and <code>y</code> are equal within the (absolute) <code>EQUALS_THRESHOLD</code>
     */
    private boolean doubleEquals(double x, double y) {
        // a faster version of Math.abs(x-y) <= EQUALS_THRESHOLD (does not call Math library):
        return ((x >= y-EQUALS_THRESHOLD) && (x <= y+EQUALS_THRESHOLD));
    }
}
