package ca.utoronto.cs.prefuseextensions.lib;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.awt.geom.RoundRectangle2D;

import ca.utoronto.cs.prefuseextensions.render.RectangularGradientPaint;


import prefuse.render.AbstractShapeRenderer;
import prefuse.util.ArrayLib;
import prefuse.util.ColorLib;
import prefuse.visual.VisualItem;

/**
 * Library of useful computer graphics routines such as geometry routines
 * for computing the intersection of different shapes and rendering methods
 * for computing bounds and performing optimized drawing.
 * 
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public abstract class MyGraphicsLib {

    public static String OUTERFILLCOLOR = new String("outerFill");

    /**
     * Render a shape associated with a VisualItem into a graphics context. This
     * method uses the {@link java.awt.Graphics} interface methods when it can,
     * as opposed to the {@link java.awt.Graphics2D} methods such as
     * {@link java.awt.Graphics2D#draw(java.awt.Shape)} and
     * {@link java.awt.Graphics2D#fill(java.awt.Shape)}, resulting in a
     * significant performance increase on the Windows platform, particularly
     * for rectangle and line drawing calls.
     * 
     * CMC modified 4 Dec 2006: for rectangular shapes, paint fill with a radial
     * gradient from r/2 to r. Does not work for non-rectangular shapes and lines.  
     * 
     * @param g the graphics context to render to
     * @param item the item being represented by the shape, this instance is
     * used to get the correct color values for the drawing
     * @param shape the shape to render
     * @param stroke the stroke type to use for drawing the object.
     * @param type the rendering type indicating if the shape should be drawn,
     * filled, or both. One of
     * {@link prefuse.render.AbstractShapeRenderer#RENDER_TYPE_DRAW},
     * {@link prefuse.render.AbstractShapeRenderer#RENDER_TYPE_FILL},
     * {@link prefuse.render.AbstractShapeRenderer#RENDER_TYPE_DRAW_AND_FILL}, or
     * {@link prefuse.render.AbstractShapeRenderer#RENDER_TYPE_NONE}.
     */
    public static void gradientPaint(Graphics2D g, VisualItem item,
                             Shape shape, BasicStroke stroke, int type)
    {
        // if render type is NONE, then there is nothing to do
        if ( type == AbstractShapeRenderer.RENDER_TYPE_NONE )
            return;
        
        // set up colors
        Color strokeColor = ColorLib.getColor(item.getStrokeColor());
        Color fillColor = ColorLib.getColor(item.getFillColor());
        Color outerFillColor = ColorLib.getColor(item.getInt(OUTERFILLCOLOR));
        
        boolean sdraw = (type == AbstractShapeRenderer.RENDER_TYPE_DRAW ||
                         type == AbstractShapeRenderer.RENDER_TYPE_DRAW_AND_FILL) &&
                        strokeColor.getAlpha() != 0;
        boolean fdraw = (type == AbstractShapeRenderer.RENDER_TYPE_FILL ||
                         type == AbstractShapeRenderer.RENDER_TYPE_DRAW_AND_FILL) &&
                        fillColor.getAlpha() != 0;
        if ( !(sdraw || fdraw) ) return;
        
        Stroke origStroke = null;
        if ( sdraw ) {
            origStroke = g.getStroke();
            g.setStroke(stroke);
        }
        
        int x, y, w, h, aw, ah;
        double xx, yy, ww, hh;

        // see if an optimized (non-shape) rendering call is available for us
        // these can speed things up significantly on the windows JRE
        // it is stupid we have to do this, but we do what we must
        // if we are zoomed in, we have no choice but to use
        // full precision rendering methods.
        AffineTransform at = g.getTransform();
        double scale = Math.max(at.getScaleX(), at.getScaleY());
        // CMC remove scale factor rendering difference
        //if ( scale > 1.5 ) {
        //    if (fdraw) { g.setPaint(fillColor);   g.fill(shape); }
        //    if (sdraw) { g.setPaint(strokeColor); g.draw(shape); }
        //}
        if ( shape instanceof RectangularShape )
        {
            RectangularShape r = (RectangularShape)shape;
            xx = r.getX(); ww = r.getWidth(); 
            yy = r.getY(); hh = r.getHeight();
            
            x = (int)xx;
            y = (int)yy;
            w = (int)(ww+xx-x);
            h = (int)(hh+yy-y);
            
            if ( shape instanceof Rectangle2D ) {
                if (fdraw) {
                	RectangularGradientPaint rgp = new RectangularGradientPaint(x+w/2, y+h/2, fillColor, new Point2D.Double(w/2, h/2), new Point2D.Double(w/4, h/4), outerFillColor);
                	g.setPaint(rgp);//fillColor);
                    g.fillRect(x, y, w, h);
                }
                if (sdraw) {
                    g.setPaint(strokeColor);
                    g.drawRect(x, y, w, h);
                }
            } else if ( shape instanceof RoundRectangle2D ) {
                RoundRectangle2D rr = (RoundRectangle2D)shape;
                aw = (int)rr.getArcWidth();
                ah = (int)rr.getArcHeight();
                if (fdraw) {
                	RectangularGradientPaint rgp = new RectangularGradientPaint(x+w/2, y+h/2, fillColor, new Point2D.Double(w/2, h/2), new Point2D.Double(w/4, h/4), outerFillColor);
                	g.setPaint(rgp);//fillColor);
                    g.fillRoundRect(x, y, w, h, aw, ah);
                }
                if (sdraw) {
                	g.setPaint(strokeColor);
                    g.drawRoundRect(x, y, w, h, aw, ah);
                }
            } else if ( shape instanceof Ellipse2D ) {
                if (fdraw) {
                	RectangularGradientPaint rgp = new RectangularGradientPaint(x+w/2, y+h/2, fillColor, new Point2D.Double(w/2, h/2), new Point2D.Double(w/4, h/4), outerFillColor);
                	g.setPaint(rgp);//fillColor);
                	g.fillOval(x, y, w, h);
                }
                if (sdraw) {
                    g.setPaint(strokeColor);
                    g.drawOval(x, y, w, h);
                }
            } else {
                if (fdraw) {
                	RectangularGradientPaint rgp = new RectangularGradientPaint(x+w/2, y+h/2, fillColor, new Point2D.Double(w/2, h/2), new Point2D.Double(w/4, h/4), outerFillColor);
                	g.setPaint(rgp);//fillColor);
                	g.fill(shape); 
                }
                if (sdraw) { g.setPaint(strokeColor); g.draw(shape); }
            }
        } else if ( shape instanceof Line2D ) {
            if (sdraw) {
                Line2D l = (Line2D)shape;
                x = (int)(l.getX1()+0.5);
                y = (int)(l.getY1()+0.5);
                w = (int)(l.getX2()+0.5);
                h = (int)(l.getY2()+0.5);
                g.setPaint(strokeColor);
                g.drawLine(x, y, w, h);
            }
        } else {
            if (fdraw) { g.setPaint(fillColor);   g.fill(shape); }
            if (sdraw) { g.setPaint(strokeColor); g.draw(shape); }
        }
        if ( sdraw ) {
            g.setStroke(origStroke);
        }
    }
    
	/**
	 * Perform precise bounds checking using method one from
	 * http://local.wasp.uwa.edu.au/~pbourke/geometry/insidepoly/
	 */
	public boolean locatePoint(Point2D p, Shape s, boolean ss) {
        
		if ( s.getBounds().contains(p) ) {
            // if within bounds, check within shape outline
            if (s == null) return false;
            
            PathIterator iterator = s.getPathIterator(null, 2);
            
            int countIntersections = 0;
            
            double[] coords = new double[6];
            double x1 = 0, y1 = 0, x2 = 0, y2 = 0, xintersect = 0;
            
            while (!iterator.isDone()) {
            	iterator.next();
            	int type = iterator.currentSegment(coords);
            	
            	x1 = coords[0];
        		y1 = coords[1];
        		
            	switch (type) {
            	case PathIterator.SEG_MOVETO:
            		// do nothing
            		break;
            	case PathIterator.SEG_LINETO:
            		// check line
            		if (p.getY() > Math.min(y1, y2)) {
            			if (p.getY() <= Math.max(y1, y2)) {
            				if (p.getX() <= Math.max(x1, x2)) {
            					if (y1 != y2) {
            						xintersect = (p.getY()-y1)*(x2-x1)/(y2-y1)+x1;
            						if (x1 == x2 || p.getX() < xintersect) {
            							countIntersections++;
            						}
            					}
            				}
            			}
            		}
            		x2 = x1;
            		y2 = y1;
            		break;
            	case PathIterator.SEG_CLOSE:
            		// do nothing
            		break;
            	}
            }
            return (countIntersections % 2 != 0);
        } else {
            return false;
        }
    }
        
	/** Return the point that is frac along the segment from P1, where frac is [0,1] */
	public static Point2D interpolateLine2D(Line2D line, double frac) {
		double newX = line.getP1().getX() * (1-frac) + line.getP2().getX() * frac;
		double newY = line.getP1().getY() * (1-frac) + line.getP2().getY() * frac;
		return new Point2D.Double(newX, newY);
	}
	
} // end of class GraphicsLib
