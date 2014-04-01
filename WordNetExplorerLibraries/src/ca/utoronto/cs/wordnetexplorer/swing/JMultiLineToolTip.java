/* CVS $Id: JMultiLineToolTip.java,v 1.1 2007/11/16 06:38:42 cmcollin Exp $ */
package ca.utoronto.cs.wordnetexplorer.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JComponent;

/**
 * Component for displaying tooltips. Presents multiple lines of text in a light
 * yellow box with a black border. Adapted from DefaultToolTipper in ToolTipManager 
 * class.  Had to edit ToolTipManager to accept custom tool tip.
 */
public class JMultiLineToolTip extends JComponent {
    
    private static final long serialVersionUID = -7047235970561862566L;
    
    private String text = null;
    
    private int wrapWidth;
    
    private Vector<String> textLines;
    
    public static final int DEFAULTTOOLTIPWIDTH = 20;
    
    public JMultiLineToolTip() {
        this(DEFAULTTOOLTIPWIDTH);
    } //
    
    public JMultiLineToolTip(int wrapWidth) {
        this.setBackground(new Color(255, 255, 225));
        this.setForeground(Color.BLACK);
        this.wrapWidth = wrapWidth;
    } //
    
    public Dimension getPreferredSize() {
        if (text == null)
            return new Dimension(0, 0);
        Graphics g = getParent().getParent().getGraphics();
        FontMetrics fm = g.getFontMetrics();
        int maxWidth = 0;
        // width is lesser of string width or wrap limit
        for (String line: textLines) {
            int width = fm.stringWidth(line);
            maxWidth = (width > maxWidth) ? width : maxWidth;
        }
        // set size
        int w = 8;
        w += maxWidth;
        int h = 4 + (textLines.size() * fm.getHeight());
        return new Dimension(w, h);
    } //
    
    public void paintComponent(Graphics g) {
        Rectangle r = this.getBounds();
        g.setColor(getBackground());
        g.fillRect(0, 0, r.width - 1, r.height - 1);
        g.setColor(getForeground());
        g.drawRect(0, 0, r.width - 1, r.height - 1);
        FontMetrics fm = g.getFontMetrics();
        if (text != null) {
            for (int line = 0; line < textLines.size(); line++) {
                g.drawString(textLines.get(line), 4, fm.getAscent() + (line * fm.getHeight()));
            }
        }
    } //
    
    public String getText() {
        return text;
    } //
    
    public void setText(String text) {
        if (text != null) {
            if ((this.text == null) || (!this.text.equals(text))) {
                this.text = text;
                textLines = wrap(text, wrapWidth);
            }
        } else // text is null
            this.text = text;
    } //
    
    public static Vector<String> wrap(String startString, int width) {
        Vector<String> linesVector = new Vector<String>();
        String unwrapped = new String(startString);
        int i = unwrapped.indexOf(' ', width);
        while (i != -1) {
            linesVector.add(unwrapped.substring(0,i));
            unwrapped = unwrapped.substring(i+1);
            i = unwrapped.indexOf(' ', width);
        }
        if (!unwrapped.equals(""))
            linesVector.add(unwrapped);
        return linesVector;
    }
    
} // end of class JMultiLineToolTip
