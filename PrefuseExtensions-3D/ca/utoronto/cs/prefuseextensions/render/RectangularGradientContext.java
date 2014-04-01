package ca.utoronto.cs.prefuseextensions.render;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;

import prefuse.util.GraphicsLib;

 
public class RectangularGradientContext
    implements PaintContext {
  protected Point2D mPoint;
  protected Point2D mRadius;
  protected Point2D mInnerRadius;
  protected Color mC1, mC2;
  public RectangularGradientContext(Point2D p, Color c1, Point2D r, Point2D ir, Color c2) {
    mInnerRadius = ir;
	mPoint = p;
    mC1 = c1;
    mRadius = r;
    mC2 = c2;
  }
  
  public void dispose() {}
  
  public ColorModel getColorModel() { return ColorModel.getRGBdefault(); }
  
  public Raster getRaster(int x, int y, int w, int h) {
    WritableRaster raster =
        getColorModel().createCompatibleWritableRaster(w, h);
    
    Rectangle2D.Double innerRectangle = new Rectangle2D.Double(mPoint.getX() - mInnerRadius.getX(), mPoint.getY() - mInnerRadius.getY(), 2*mInnerRadius.getX(), 2*mInnerRadius.getY());
    Point2D[] intersections = new Point2D[2];
    
    int[] data = new int[w * h * 4];
    for (int j = 0; j < h; j++) {
      for (int i = 0; i < w; i++) {
    	double ratio = 1.0;
    	if (GraphicsLib.intersectLineRectangle(mPoint, new Point2D.Double(x+i, y+j), innerRectangle, intersections) != GraphicsLib.NO_INTERSECTION) {
    		double radius = mRadius.distance(mInnerRadius);
    		double distance = intersections[0].distance(x+i, y+j);
    		ratio = distance/radius;
    		/*System.err.println("point one: " + mPoint.toString());
    		System.err.println("point two: " + (x+i) + ", " + (y+j));
    		System.err.println("inner rec: " + innerRectangle.toString());
    		System.err.println("intersection " + intersections[0].toString());
    		System.err.println("intersection " + intersections[1].toString());
    		System.err.println("radius " + radius);
    		System.err.println("distance " + distance);
    		System.err.println("ratio " + ratio);
    		System.err.println("coincident");*/
    	}	
    	
    	if(innerRectangle.contains(new Point2D.Double(x+i, y+j))) {
    		ratio = 0.0;
    	}
    	
        if (ratio > 1.0)
          ratio = 1.0;
        
        int base = (j * w + i) * 4;
        data[base + 0] = (int)(mC1.getRed() + ratio *
            (mC2.getRed() - mC1.getRed()));
        data[base + 1] = (int)(mC1.getGreen() + ratio *
            (mC2.getGreen() - mC1.getGreen()));
        data[base + 2] = (int)(mC1.getBlue() + ratio *
            (mC2.getBlue() - mC1.getBlue()));
        data[base + 3] = (int)(mC1.getAlpha() + ratio *
            (mC2.getAlpha() - mC1.getAlpha()));
      }
    }
    raster.setPixels(0, 0, w, h, data);
    
    return raster;
  }
}