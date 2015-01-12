package ca.utoronto.cs.docuburst.swing.widget;

import java.util.List;

public interface ScentedSliderModel {

    public static class Point {
        double x;
        double y;
        
        public Point(double x, double y){
            this.x = x;
            this.y = y;
        }
    }
    
    public void setDiscrete(boolean isDiscrete);
    public boolean isDiscrete();
    public void setPoints(List<Point> points);
    public List<Point> getPoints();
    public double getMaxX();
    public double getMinX();
    public double getMaxY();
    public double getMinY();
    public double getExtentX();
}
