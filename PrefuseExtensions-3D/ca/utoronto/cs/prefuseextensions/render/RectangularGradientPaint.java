package ca.utoronto.cs.prefuseextensions.render;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.ColorModel;
 
public class RectangularGradientPaint implements Paint {
  protected Point2D mPoint;
  protected Point2D mRadius, mInnerRadius;
  protected Color mPointColor, mBackgroundColor;
  
  public RectangularGradientPaint(double x, double y, Color pointColor,
      Point2D radius, Point2D innerRadius, Color backgroundColor) {
    if (radius.distance(0, 0) <= 0)
      throw new IllegalArgumentException("Radius must be greater than 0.");
    mPoint = new Point2D.Double(x, y);
    mPointColor = pointColor;
    mRadius = radius;
    mInnerRadius = innerRadius;
    mBackgroundColor = backgroundColor;
  }
  
  public PaintContext createContext(ColorModel cm,
      Rectangle deviceBounds, Rectangle2D userBounds,
      AffineTransform xform, RenderingHints hints) {
    Point2D transformedPoint = xform.transform(mPoint, null);
    Point2D transformedRadius = xform.deltaTransform(mRadius, null);
    Point2D transformedInnerRadius = xform.deltaTransform(mInnerRadius, null);
    return new RectangularGradientContext(transformedPoint, mPointColor,
        transformedRadius, transformedInnerRadius, mBackgroundColor);
  }
  
  public int getTransparency() {
    int a1 = mPointColor.getAlpha();
    int a2 = mBackgroundColor.getAlpha();
    return (((a1 & a2) == 0xff) ? OPAQUE : TRANSLUCENT);
  }
}
