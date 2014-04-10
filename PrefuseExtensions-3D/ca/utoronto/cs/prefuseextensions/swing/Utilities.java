/* CVS $Id: Utilities.java,v 1.1 2007/02/09 06:40:55 cmcollin Exp $ */
package ca.utoronto.cs.prefuseextensions.swing;

import java.awt.GridBagConstraints;

public abstract class Utilities {
    
    /**
     * Utility method to assist setting the parameters of a GridBagConstraints object.
     * 
     * @param c the constraints object to set
     * @param gridx Specifies the cell containing the leading edge of the component's display area, where the first cell in a row has gridx=0. 
     * @param gridy Specifies the cell at the top of the component's display area, where the topmost cell has gridy=0. The value RELATIVE specifies that the component be placed just below the component that was added to the container just before this component was added.
     * @param weightx Specifies how to distribute extra horizontal space.
     * @param weighty Specifies how to distribute extra vertical space.
     * @param gridwidth Specifies the number of cells in a row for the component's display area.
     * @param gridheight Specifies the number of cells in a row for the component's display area.
     * @param fill This field is used when the component's display area is larger than the component's requested size. It determines whether to resize the component, and if so, how.
     * 
     * @see GridBagConstraints
     */
    public static void setGBC(GridBagConstraints c, int gridx, int gridy, double weightx, double weighty, int gridwidth,
            int gridheight, int fill) {
        c.gridx = gridx;
        c.gridy = gridy;
        c.weightx = weightx;
        c.weighty = weighty;
        c.gridwidth = gridwidth;
        c.gridheight = gridheight;
        c.fill = fill;
    }
}
