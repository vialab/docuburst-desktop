package ca.utoronto.cs.docuburst.swing.widget;

import java.util.List;

import javax.swing.BoundedRangeModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

public class DefaultScentedSliderModel implements ScentedSliderModel{

    protected EventListenerList listenerList = new EventListenerList();
    private List<Point> points;
    private boolean isDiscrete;
    private double value = Double.NaN;
    private double extent = Double.NaN;
    private boolean isAdjusting = false;
    
    /**
     * Data model for {@link ScentedSlider}.
     * @param points list of control points -> x,y values.
     * @param extent the gap between one value and the next when the user hits PgUp or PgDown,
     * as in {@link BoundedRangeModel#getExtent()}.
     * @param isDiscrete whether or not the user can choose a value between control points.
     */
    public DefaultScentedSliderModel(List<Point> points, double extent, boolean isDiscrete) {
        this.points = points;
        this.isDiscrete = isDiscrete;
        this.extent = extent;
    }
    
    @Override
    public void setValueIsAdjusting(boolean isAdjusting) {
    	this.isAdjusting = isAdjusting;
    }
    
    @Override
    public boolean valueIsAdjusting() {
    	return isAdjusting;
    }
    
    public void setValue(double value) {
        this.value = value;
        // TODO: Test is the value is legal. Throw IllegalArgumentException
        // in case it is not.
        this.fireStateChanged();
    }
    
    public double getValue(){
        return this.value;
    }

    public void addChangeListener(ChangeListener l){
        listenerList.add(ChangeListener.class, l);
    }
    
    public void removeChangeListener(ChangeListener l){
        listenerList.remove(ChangeListener.class, l);
    }
    
    protected void fireStateChanged(){
        ChangeEvent event = new ChangeEvent(this);
        Object[] listeners = listenerList.getListenerList();
        // 1. iterating backwards prevents the loop from breaking in case
        // one of the listeners removes itself.
        // 2. EventListenerList stores the listener and its type in the array in
        // consecutive positions.
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ChangeListener.class) {
                ((ChangeListener) listeners[i + 1]).stateChanged(event);
            }
        }
    }
    
    @Override
    public void setDiscrete(boolean isDiscrete) {
        this.isDiscrete = isDiscrete;
    }

    @Override
    public boolean isDiscrete() {
        return isDiscrete;
    }

    @Override
    public void setPoints(List<Point> points) {
        this.points = points;
    }

    @Override
    public List<Point> getPoints() {
        return points;
    }

    @Override
    public double getMaxX() {
        double max = Double.MIN_VALUE;
        for (Point p : points) {
            if (p.x > max)
                max = p.x;
        }
        return max;
    }

    @Override
    public double getMinX() {
        double min = Double.MAX_VALUE;
        for (Point p : points) {
            if (p.x < min)
                min = p.x;
        }
        return min;
    }

    @Override
    public double getMaxY() {
        double max = Double.MIN_VALUE;
        for (Point p : points) {
            if (p.y > max)
                max = p.y;
        }
        return max;
    }

    @Override
    public double getMinY() {
        double min = Double.MAX_VALUE;
        for (Point p : points) {
            if (p.y < min)
                min = p.y;
        }
        return min;
    }
    
    public double getExtentX(){
        return extent;
    }

}
