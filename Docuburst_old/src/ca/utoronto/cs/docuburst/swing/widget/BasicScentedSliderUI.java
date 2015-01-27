package ca.utoronto.cs.docuburst.swing.widget;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicSliderUI;

import ca.utoronto.cs.docuburst.swing.widget.ScentedSliderModel.Point;
import ca.utoronto.cs.docuburst.util.scale.LinearScale;


/**
 * This component is based on <a href="https://today.java.net/pub/a/today/2007/02/22/how-to-write-custom-swing-component.html">this</a> tutorial.
 * It uses {@link CellRendererPane} to render a JSlider for the x values of control points, and plots the
 * y values in a chart above the slider.
 * @author rafaveguim
 *
 */
public class BasicScentedSliderUI extends ScentedSliderUI {

    protected ScentedSlider scentedSlider;
    protected ScentedSliderModel model;
    
    protected JSlider slider;
    private MySliderUI sliderUI;
    
    private CellRendererPane sliderRendererPane;
    
    // ---------------- Listeners ----------------------- ||
    private MouseListener mouseListener;
    private MouseMotionListener mouseMotionListener;
    private ChangeListener scentedSliderChangeListener;
    
    /**
     * The CellRendererPane wrapping the slider always sets the slider bounds to 0 once
     * it has finished paintint it, turning retrieval of the track info unreliable. This
     * variable stores size and position of the track always consistent with what's seen
     * on-screen.
     */
    private Rectangle trackBounds;
    
    public static ComponentUI createUI(JComponent c){
        return new BasicScentedSliderUI();
    }
    
    @Override
    /** Installs UI on the associated component. */
    public void installUI(JComponent c) {
        this.scentedSlider = (ScentedSlider)c;
        this.model = this.scentedSlider.getModel();
        installDefaults();
        installComponents();
        installListeners();
        
        c.setLayout(createLayoutManager());
        c.setBorder(new EmptyBorder(1,1,1,1));
    }
    
    /** Installs default settings for the associated component. */
    public void installDefaults(){}
    
    /** Installs listeners for the associated component. */
    public void installListeners(){
		this.mouseListener = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
			}

			@Override
			public void mouseReleased(MouseEvent e) {
			    scentedSlider.getModel().setValueIsAdjusting(false);
                double modelValue = sliderValueToModelValue(e.getX());
                // do the "magic" of snapping the slider thumb to control 
                // points of discrete ranges.
                int snappedValue = modelValueToSliderValue(modelValue);
                scentedSlider.setValue(snappedValue);
                slider.setValue(snappedValue);
			}

			@Override
			public void mousePressed(MouseEvent e) {
				scentedSlider.getModel().setValueIsAdjusting(true);
				double modelValue = sliderValueToModelValue(e.getX());
				// do the "magic" of snapping the slider thumb to control 
				// points of discrete ranges.
				int snappedValue = modelValueToSliderValue(modelValue);
				scentedSlider.setValue(snappedValue);
				slider.setValue(snappedValue);
			}
		};   
		this.scentedSlider.addMouseListener(mouseListener);
		
		this.scentedSliderChangeListener = new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				scentedSlider.repaint();
			}
		};
		this.scentedSlider.getModel().addChangeListener(this.scentedSliderChangeListener);
		
		this.mouseMotionListener = new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				scentedSlider.getModel().setValueIsAdjusting(true);
				double modelValue = sliderValueToModelValue(e.getX());
				scentedSlider.setValue(modelValue);
				
				slider.setValue(px(modelValue));
			}
		};
		this.scentedSlider.addMouseMotionListener(this.mouseMotionListener);
    };
    
    /** Installs components for the associated component. */
    public void installComponents(){
        slider = new JSlider(JSlider.HORIZONTAL, (int)model.getMinX(), (int)model.getMaxX(), (int)model.getMinX());
        slider.setFocusable(false);
        slider.setPaintTicks(false);
        slider.setPaintLabels(true);
        slider.setSnapToTicks(false);
        // creates labelTable
        Dictionary<Integer, JComponent> labelTable = new Hashtable<Integer, JComponent>();
        List<Point> points  = model.getPoints();
        for (Point point : points) {
			labelTable.put((int)point.x, new JLabel(Integer.toString((int)point.x)));
		}
        slider.setLabelTable(labelTable);
        
        sliderUI = new MySliderUI(slider);
        slider.setUI(sliderUI);
        
//        scentedSlider.add(slider);
        
        sliderRendererPane = new CellRendererPane();
        this.scentedSlider.add(sliderRendererPane);
        
    };
 
    @Override
    /** Uninstalls UI on the associated component. */
    public void uninstallUI(JComponent c) {
        c.setLayout(null);
        uninstallListeners();
        uninstallComponents();
        uninstallDefaults();
        
        this.scentedSlider = null;
    }

    /** Uninstalls default settings for the associated component. */
    public void uninstallDefaults(){};

    /** Uninstalls listeners for the associated component. */
    public void uninstallListeners(){};

    /** Uninstalls components for the associated component. */
    public void uninstallComponents(){};

    @Override
    public void paint(Graphics g, JComponent c) {
        super.paint(g, c);
        paintChart(g);
        Rectangle bounds = sliderRendererPane.getBounds();
        sliderRendererPane.paintComponent(g, this.slider, this.scentedSlider, bounds.x, bounds.y,
				bounds.width, bounds.height, true);
//        this.slider.paintComponents(g);
    }
   
    
    private void paintChart(Graphics g){
        Insets ins = scentedSlider.getInsets();
        Rectangle sliderBounds = sliderRendererPane.getBounds();
        
        Rectangle chartBounds = new Rectangle();
        chartBounds.setLocation(trackBounds.x, ins.top);
        chartBounds.setSize(trackBounds.width, 
                scentedSlider.getHeight()-ins.top-sliderBounds.height);
        
        
        // painting the chart within the chart bounds
        List<Point> points = model.getPoints();
        
        LinearScale xScale = new LinearScale(model.getMinX(), model.getMaxX(), chartBounds.getMinX(), chartBounds.getMaxX());
        LinearScale yScale = new LinearScale(model.getMinY(), model.getMaxY(), chartBounds.getMaxY(), chartBounds.getMinY());
        
        Graphics2D g2d = (Graphics2D)g;
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        Point lastP = points.get(0);
        for (int i = 1; i < points.size(); i++) {
            Point p = points.get(i);
            g.drawLine(px(xScale.scale(lastP.x)), 
                    px(yScale.scale(lastP.y)), 
                    px(xScale.scale(p.x)), 
                    px(yScale.scale(p.y)));
            lastP = p;
        }
    }
    
    protected double sliderValueToModelValue(int sliderValue) {
    	LinearScale xScale = new LinearScale(trackBounds.getMinX(), trackBounds.getMaxX(), model.getMinX(), model.getMaxX());
    	return xScale.scale(sliderValue);
    }
    
    protected int modelValueToSliderValue(double modelValue) {   	
    	// assuming list of points is ordered, uses binary search to find the closest point to modelValue
    	List<Point> points = model.getPoints();
    	
    	int a = 0, b = points.size() - 1; 
    	int pivot;
    	
    	while (b-a > 1){
    		pivot = (b+a)/2;
    		if (modelValue > points.get(pivot).x){
    			a = pivot;
    		} else {
    			b = pivot;
    		}
    	}
    	
    	int controlPoint;
    	
    	if (modelValue - points.get(a).x < points.get(b).x - modelValue)
    		controlPoint = a;
    	else
    		controlPoint = b;
    	
//    	LinearScale xScale = new LinearScale(model.getMinX(), model.getMaxX(), trackBounds.getMinX(), trackBounds.getMaxX());
    	
    	return (int)points.get(controlPoint).x;
    }
    
    private int px(double number){
        return (int)Math.round(number);
    }
    
    
    /**
     * Invoked by <code>installUI</code> to create a layout manager object to
     * manage the {@link ScentedSlider}.
     * 
     * @return a layout manager object
     */

    protected LayoutManager createLayoutManager() {
        return new ScentedSliderLayout();
    }
    
    protected class ScentedSliderLayout implements LayoutManager{

        // gap between slider control points, in px
        int gap = 4;
        // the preferred height of the chart
        int chartHeight = 80;
        
        int minWidth = 200;
        
        @Override
        public void addLayoutComponent(String name, Component comp) {}

        @Override
        public void removeLayoutComponent(Component comp) {}

        @Override
        public Dimension preferredLayoutSize(Container c) {
            Insets ins = c.getInsets();
            ScentedSlider scentSlider = (ScentedSlider) c;
            int width  = Math.max(minWidth, (scentSlider.getControlPointCount()-1) * gap);
            int height = slider.getPreferredSize().height + chartHeight;
            
            return new Dimension(width + ins.left + ins.right, height + ins.top + ins.bottom);
        }

        @Override
        public Dimension minimumLayoutSize(Container c) {
            return preferredLayoutSize(c);
        }
        

        @Override
        public void layoutContainer(Container c) {
            Insets ins = c.getInsets();
            int h = c.getHeight();
            int w = c.getWidth();
//            c.setBounds(x, y, width, height);
            
            // lay out slider in the bottom of the container
            int sliderX = ins.left;
            int sliderY = Math.max(h - slider.getPreferredSize().height, ins.bottom);
            int sliderW = Math.max(slider.getPreferredSize().width,  w - ins.left - ins.right);
            int sliderH = Math.min(slider.getPreferredSize().height, h - ins.top - ins.bottom);
            sliderRendererPane.setBounds(sliderX, sliderY, sliderW, sliderH);
            slider.setBounds(sliderX, sliderY, sliderW, sliderH);
            sliderUI.calculateGeometry();
            trackBounds = sliderUI.getTrackBounds();
        }
        
    }
    
    /**
     * In order to interpolate between model and slider values,
     * we need to know the exact coordinates of the slider track,
     * we are available as protected methods of the BasicSliderUI.
     * This classes works merely as a proxy to these methods.
     * @author rafaveguim
     *
     */
    protected class MySliderUI extends BasicSliderUI{

        public MySliderUI(JSlider b) {
            super(b);
        }
        
        
        public void calculateGeometry(){
        	super.calculateGeometry();
        }
        
        public Rectangle getTrackBounds(){
        	return new Rectangle(super.trackRect);
        }
        
        public int getTrackX(){
            return trackRect.x;
        }
        
        public int getTrackWidth(){
            return trackRect.width;
        }
    }
}
