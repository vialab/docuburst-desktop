package ca.utoronto.cs.docuburst.swing.widget;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import ca.utoronto.cs.docuburst.swing.widget.ScentedSliderModel.Point;

public class ScentedSlider extends JComponent {

    private static final long serialVersionUID = -8764161070769338099L;
    
    /**
     * The UI class ID string.
     */
    private static final String uiClassID = "ScentedSliderUI";
    
    private ScentedSliderModel model;
    
    public ScentedSlider(List<Point> points, double extent, boolean isDiscrete) throws NullPointerException, IllegalArgumentException{
        if (points == null)
            throw new NullPointerException();
        if (points.size()<2)
            throw new IllegalArgumentException("ScentedSlider needs at least two points");
        
        this.model = new DefaultScentedSliderModel(points, extent, isDiscrete);
        
        this.updateUI();
    }
    
    public void setUI(ScentedSliderUI ui){
        super.setUI(ui);
    }
    
    @Override
    public String getUIClassID() {
        return uiClassID;
    }
    
    public void updateUI(){
        if (UIManager.get(getUIClassID()) != null){
            setUI((ScentedSliderUI) UIManager.getUI(this));
        } else {
            setUI(new BasicScentedSliderUI());
        }
    }
    
    public ScentedSliderModel getModel() {
        return model;
    }
    
    public List<Point> getControlPoints(){
        return model.getPoints();
    }
    
    public int getControlPointCount(){
        return getControlPoints().size();
    }
    
    public static void main(String[] args){
        
        SwingUtilities.invokeLater(new Runnable() {
            
            @Override
            public void run() {
                JFrame frame = new JFrame();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(200, 300);
                
                Point p1 = new Point(10, 50);
                Point p2 = new Point(30, 100);
                Point p3 = new Point(60, 20);
                
                List<Point> points = new ArrayList<ScentedSliderModel.Point>();
                points.add(p1);
                points.add(p2);
                points.add(p3);
                
                ScentedSlider slider = new ScentedSlider(points, 10, true);
                frame.getContentPane().add(slider, BorderLayout.CENTER);
                
                frame.pack();
                frame.setVisible(true);
                
            }
        });
        
        
        
    }
}


