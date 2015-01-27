package ca.utoronto.cs.docuburst.swing.widget;

import java.util.List;

import javax.swing.event.ChangeListener;

public interface ScentedSliderModel {

    public static class Point {
        double x;
        double y;
        
        public Point(double x, double y){
            this.x = x;
            this.y = y;
        }
    }
    
    public double getValue();
    public void setValue(double v);
    public boolean valueIsAdjusting();
    public void setValueIsAdjusting(boolean isAdjusting);
    public void setDiscrete(boolean isDiscrete);
    public boolean isDiscrete();
    public void setPoints(List<Point> points);
    public List<Point> getPoints();
    public double getMaxX();
    public double getMinX();
    public double getMaxY();
    public double getMinY();
    public double getExtentX();
    public void addChangeListener(ChangeListener l);    
    public void removeChangeListener(ChangeListener l);
}
