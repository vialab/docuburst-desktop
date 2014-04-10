package ca.utoronto.cs.wordnetexplorer.prefuse.controls;

import static ca.utoronto.cs.wordnetexplorer.utilities.Constants.L2S;
import static ca.utoronto.cs.wordnetexplorer.utilities.Constants.S2W;

import java.awt.BasicStroke;
import java.awt.event.MouseEvent;

import prefuse.controls.ControlAdapter;
import prefuse.util.StrokeLib;
import prefuse.visual.VisualItem;
/**
 * When user double clicks "Word" or "Sense" node
 */
public class EdgeWeightControl extends ControlAdapter {
    
	private String action;
	
	public EdgeWeightControl(String action) {
		super();
		this.action = action;
	}
	
	//Invoked when the mouse button has been clicked (pressed and released) on a VisualItem. 
    public void itemClicked(VisualItem item, MouseEvent e){
        // single click edge doubles stroke width
        if ((e.getClickCount() == 1) && ((item.getInt("type") == S2W) || (item.getInt("type") == L2S))) {
            item.setStroke(StrokeLib.getStroke(item.getStroke().getLineWidth()*2,BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
            item.getVisualization().run(action);
        }
    }
}