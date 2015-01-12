package ca.utoronto.cs.docuburst.swing.widget;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.util.List;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ComponentUI;

import ca.utoronto.cs.docuburst.swing.widget.ScentedSliderModel.Point;
import ca.utoronto.cs.docuburst.util.scale.LinearScale;

public class BasicScentedSliderUI extends ScentedSliderUI {

    protected ScentedSlider scentedSlider;
    protected ScentedSliderModel model;
    
    protected JSlider slider;
    
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
    public void installListeners(){};

    /** Installs components for the associated component. */
    public void installComponents(){
        slider = new JSlider(JSlider.HORIZONTAL, (int)model.getMinX(), (int)model.getMaxX(), (int)model.getMinX());
        slider.getPreferredSize();
        slider.setFocusable(false);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setMajorTickSpacing(15);
        slider.setMajorTickSpacing(5);
        
        scentedSlider.add(slider);
        
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
        this.slider.paintComponents(g);
        paintChart(g);
    }
    
    private void paintChart(Graphics g){
        Insets ins             = scentedSlider.getInsets();
        Rectangle sliderBounds = this.slider.getBounds();
        

        
        Rectangle chartBounds  = new Rectangle();
        chartBounds.setLocation(ins.left, ins.top);
        chartBounds.setSize(sliderBounds.width, 
                scentedSlider.getHeight()-ins.top-sliderBounds.height);
        
        
        // painting the chart within the chart bounds
        List<Point> points = model.getPoints();
        
        LinearScale xScale = new LinearScale(model.getMinX(), model.getMaxX(), chartBounds.getMinX(), chartBounds.getMaxX());
        LinearScale yScale = new LinearScale(model.getMinY(), model.getMaxY(), chartBounds.getMinY(), chartBounds.getMaxY());
        
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
            int sliderW = Math.min(slider.getPreferredSize().width,  w - ins.left - ins.right);
            int sliderH = Math.min(slider.getPreferredSize().height, h - ins.top - ins.bottom);
            slider.setBounds(sliderX, sliderY, sliderW, sliderH);
        }
        
    }
}
