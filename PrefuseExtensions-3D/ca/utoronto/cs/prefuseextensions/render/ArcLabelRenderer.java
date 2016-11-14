	/* CVS $Id: ArcLabelRenderer.java,v 1.3 2007/03/23 05:32:11 cmcollin Exp $ */
package ca.utoronto.cs.prefuseextensions.render;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.awt.geom.RoundRectangle2D;

import prefuse.Constants;
import prefuse.render.AbstractShapeRenderer;
import prefuse.render.ImageFactory;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.util.GraphicsLib;
import prefuse.util.StringLib;
import prefuse.visual.DecoratorItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;


/**
 * 
 * CMC Modified copy of LabelRenderer v 2.2
 *  
 * Renderer that draws a label, which consists of a text string,
 * an image, or both.
 * 
 * <p>When created using the default constructor, the renderer attempts
 * to use text from the "label" field. To use a different field, use the
 * appropriate constructor or use the {@link #setTextField(String)} method.
 * To perform custom String selection, subclass this Renderer and override the 
 * {@link #getText(VisualItem)} method. When the text field is
 * <code>null</code>, no text label will be shown. Labels can span multiple
 * lines of text, determined by the presence of newline characters ('\n')
 * within the text string.</p>
 * 
 * <p>By default, no image is shown. To show an image, the image field needs
 * to be set, either using the appropriate constructor or the
 * {@link #setImageField(String)} method. The value of the image field should
 * be a text string indicating the location of the image file to use. The
 * string should be either a URL, a file located on the current classpath,
 * or a file on the local filesystem. If found, the image will be managed
 * internally by an {@link ImageFactory} instance, which maintains a
 * cache of loaded images.</p>
 * 
 * <p>The position of the image relative to text can be set using the
 * {@link #setImagePosition(int)} method. Images can be placed to the
 * left, right, above, or below the text. The horizontal and vertical
 * alignments of either the text or the image can be set explicitly
 * using the appropriate methods of this class (e.g.,
 * {@link #setHorizontalTextAlignment(int)}). By default, both the
 * text and images are centered along both the horizontal and
 * vertical directions.</p>
 * 
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class ArcLabelRenderer extends AbstractShapeRenderer {

    protected ImageFactory m_images = null;
    protected String m_delim = "\n";
    
    protected String m_labelName = "label";
    protected String m_imageName = null;
    
    protected int m_xAlign = Constants.CENTER;
    protected int m_yAlign = Constants.CENTER;
    protected int m_hTextAlign = Constants.CENTER;
    protected int m_vTextAlign = Constants.CENTER;
    protected int m_hImageAlign = Constants.CENTER;
    protected int m_vImageAlign = Constants.CENTER;
    protected int m_imagePos = Constants.LEFT;
    
    protected int m_horizBorder = 2;
    protected int m_vertBorder  = 0;
    protected int m_imageMargin = 2;
    protected int m_arcWidth    = 0;
    protected int m_arcHeight   = 0;

    protected int m_maxTextWidth = -1;
    protected double minimumRenderSize;
    protected double maximumRenderSize;
    
    /** 
     * Transforms to store state of context temporarily
     */
    private AffineTransform aT1, aT2;
    
    /** Transform used to scale and position images */
    AffineTransform m_transform = new AffineTransform();
    
    /** The holder for the currently computed bounding box */
    protected RectangularShape m_bbox  = new Rectangle2D.Double();
    protected Point2D m_pt = new Point2D.Double(); // temp point
    protected Font    m_font; // temp font holder
    protected String    m_text; // label text
    protected Dimension m_textDim = new Dimension(); // text width / height

    /**
     * Create a new LabelRenderer. By default the field "label" is used
     * as the field name for looking up text, and no image is used.
     */
    public ArcLabelRenderer() {
    	minimumRenderSize = 0;
    	maximumRenderSize = Double.MAX_VALUE;
    }
    
    /**
     * Create a new LabelRenderer. Draws a text label using the given
     * text data field and does not draw an image.
     * @param textField the data field for the text label.
     */
    public ArcLabelRenderer(String textField) {
        this.setTextField(textField);
        minimumRenderSize = 0;
        maximumRenderSize = Double.MAX_VALUE;
    }

    /**
     * Create a new LabelRenderer. Draws a text label using the given
     * text data field and does not draw an image.
     * @param textField the data field for the text label.
     * @param minimumRenderSize do not render text with height smaller than the specified point size
     */
    public ArcLabelRenderer(String textField, double minimumRenderSize) {
        this.setTextField(textField);
        this.minimumRenderSize = minimumRenderSize;
        maximumRenderSize = Double.MAX_VALUE;
    }
    
    /**
     * Create a new LabelRenderer. Draws a text label using the given
     * text data field and does not draw an image.
     * @param textField the data field for the text label.
     * @param minimumRenderSize do not render text with height smaller than the specified point size
     * @param maximumRenderSize cap font height at this point size
     */
    public ArcLabelRenderer(String textField, double minimumRenderSize, double maximumRenderSize) {
        this.setTextField(textField);
        this.minimumRenderSize = minimumRenderSize;
        this.maximumRenderSize = maximumRenderSize;
    }
    
    // ------------------------------------------------------------------------
    
    /**
     * Rounds the corners of the bounding rectangle in which the text
     * string is rendered. This will only be seen if either the stroke
     * or fill color is non-transparent.
     * @param arcWidth the width of the curved corner
     * @param arcHeight the height of the curved corner
     */
    public void setRoundedCorner(int arcWidth, int arcHeight) {
        if ( (arcWidth == 0 || arcHeight == 0) && 
            !(m_bbox instanceof Rectangle2D) ) {
            m_bbox = new Rectangle2D.Double();
        } else {
            if ( !(m_bbox instanceof RoundRectangle2D) )
                m_bbox = new RoundRectangle2D.Double();
            ((RoundRectangle2D)m_bbox)
                .setRoundRect(0,0,10,10,arcWidth,arcHeight);
            m_arcWidth = arcWidth;
            m_arcHeight = arcHeight;
        }
    }

    /**
     * Get the field name to use for text labels.
     * @return the data field for text labels, or null for no text
     */
    public String getTextField() {
        return m_labelName;
    }
    
    /**
     * Set the field name to use for text labels.
     * @param textField the data field for text labels, or null for no text
     */
    public void setTextField(String textField) {
        m_labelName = textField;
    }
    
    /**
     * Sets the maximum width that should be allowed of the text label.
     * A value of -1 specifies no limit (this is the default).
     * @param maxWidth the maximum width of the text or -1 for no limit
     */
    public void setMaxTextWidth(int maxWidth) {
        m_maxTextWidth = maxWidth;
    }
    
    /**
     * Returns the text to draw. Subclasses can override this class to
     * perform custom text selection.
     * @param item the item to represent as a <code>String</code>
     * @return a <code>String</code> to draw
     */
    protected String getText(VisualItem item) {
        String s = null;
        if ( item.canGetString(m_labelName) ) {
            return item.getString(m_labelName);            
        }
        return s;
    }

    // ------------------------------------------------------------------------
    // Image Handling
    
    /**
     * Get the data field for image locations. The value stored
     * in the data field should be a URL, a file within the current classpath,
     * a file on the filesystem, or null for no image.
     * @return the data field for image locations, or null for no images
     */
    public String getImageField() {
        return m_imageName;
    }
    
    /**
     * Set the data field for image locations. The value stored
     * in the data field should be a URL, a file within the current classpath,
     * a file on the filesystem, or null for no image. If the
     * <code>imageField</code> parameter is null, no images at all will be
     * drawn.
     * @param imageField the data field for image locations, or null for
     * no images
     */
    public void setImageField(String imageField) {
        if ( imageField != null ) m_images = new ImageFactory();
        m_imageName = imageField;
    }
    
    /**
     * Sets the maximum image dimensions, used to control scaling of loaded
     * images. This scaling is enforced immediately upon loading of the image.
     * @param width the maximum width of images (-1 for no limit)
     * @param height the maximum height of images (-1 for no limit)
     */
    public void setMaxImageDimensions(int width, int height) {
        if ( m_images == null ) m_images = new ImageFactory();
        m_images.setMaxImageDimensions(width, height);
    }
    
    /**
     * Returns a location string for the image to draw. Subclasses can override 
     * this class to perform custom image selection beyond looking up the value
     * from a data field.
     * @param item the item for which to select an image to draw
     * @return the location string for the image to use, or null for no image
     */
    protected String getImageLocation(VisualItem item) {
        return item.canGetString(m_imageName)
                ? item.getString(m_imageName)
                : null;
    }
    
    /**
     * Get the image to include in the label for the given VisualItem.
     * @param item the item to get an image for
     * @return the image for the item, or null for no image
     */
    protected Image getImage(VisualItem item) {
        String imageLoc = getImageLocation(item);
        return ( imageLoc == null ? null : m_images.getImage(imageLoc) );
    }
    
    
    // ------------------------------------------------------------------------
    // Rendering
    
    private String computeTextDimensions(VisualItem item, String text,
                                         double size)
    {
        // put item font in temp member variable
        m_font = item.getFont();
        // scale the font as needed
        if ( size != 1 ) {
            m_font = FontLib.getFont(m_font.getName(), m_font.getStyle(),
                                     size*m_font.getSize());
        }
        
        FontMetrics fm = DEFAULT_GRAPHICS.getFontMetrics(m_font);
        StringBuilder str = null;
        
        // compute the number of lines and the maximum width
        int nlines = 1, w = 0, start = 0, end = text.indexOf(m_delim);
        m_textDim.width = 0;
        String line;
        for ( ; end >= 0; ++nlines ) {
            w = fm.stringWidth(line=text.substring(start,end));
            // abbreviate line as needed
            if ( m_maxTextWidth > -1 && w > m_maxTextWidth ) {
                if ( str == null )
                    str = new StringBuilder(text.substring(0,start));
                str.append(StringLib.abbreviate(line, fm, m_maxTextWidth));
                str.append(m_delim);
                w = m_maxTextWidth;
            } else if ( str != null ) {
                str.append(line).append(m_delim);
            }
            // update maximum width and substring indices
            m_textDim.width = Math.max(m_textDim.width, w);
            start = end+1;
            end = text.indexOf(m_delim, start);
        }
        w = fm.stringWidth(line=text.substring(start));
        // abbreviate line as needed
        if ( m_maxTextWidth > -1 && w > m_maxTextWidth ) {
            if ( str == null )
                str = new StringBuilder(text.substring(0,start));
            str.append(StringLib.abbreviate(line, fm, m_maxTextWidth));
            w = m_maxTextWidth;
        } else if ( str != null ) {
            str.append(line);
        }
        // update maximum width
        m_textDim.width = Math.max(m_textDim.width, w);
        
        // compute the text height
        m_textDim.height = fm.getHeight() * nlines;
        
        return str==null ? text : str.toString();
    }
    
    /**
     * @see prefuse.render.AbstractShapeRenderer#getRawShape(prefuse.visual.VisualItem)
     */
    protected Shape getRawShape(VisualItem item) {
        m_text = getText(item);
        Image  img  = getImage(item);
        double size = item.getSize();
        
        // get image dimensions
        double iw=0, ih=0;
        if ( img != null ) {
            ih = img.getHeight(null);
            iw = img.getWidth(null);    
        }
        
        // get text dimensions
        int tw=0, th=0;
        if ( m_text != null ) {
            m_text = computeTextDimensions(item, m_text, size);
            th = m_textDim.height;
            tw = m_textDim.width;   
        }
        
        // get bounding box dimensions
        double w=0, h=0;
        switch ( m_imagePos ) {
        case Constants.LEFT:
        case Constants.RIGHT:
            w = tw + size*(iw +2*m_horizBorder
                   + (tw>0 && iw>0 ? m_imageMargin : 0));
            h = Math.max(th, size*ih) + size*2*m_vertBorder;
            break;
        case Constants.TOP:
        case Constants.BOTTOM:
            w = Math.max(tw, size*iw) + size*2*m_horizBorder;
            h = th + size*(ih + 2*m_vertBorder
                   + (th>0 && ih>0 ? m_imageMargin : 0));
            break;
        default:
            throw new IllegalStateException(
                "Unrecognized image alignment setting.");
        }
        
        // get the top-left point, using the current alignment settings
        getAlignedPoint(m_pt, item, w, h, m_xAlign, m_yAlign);
        
        if ( m_bbox instanceof RoundRectangle2D ) {
            RoundRectangle2D rr = (RoundRectangle2D)m_bbox;
            rr.setRoundRect(m_pt.getX(), m_pt.getY(), w, h,
                            size*m_arcWidth, size*m_arcHeight);
        } else {
            m_bbox.setFrame(m_pt.getX(), m_pt.getY(), w, h);
        }
        
        return m_bbox;
    }
    
    /**
     * Helper method, which calculates the top-left co-ordinate of an item
     * given the item's alignment.
     */
    protected static void getAlignedPoint(Point2D p, VisualItem item, 
            double w, double h, int xAlign, int yAlign)
    {
        double x = item.getX(), y = item.getY();
        if ( Double.isNaN(x) || Double.isInfinite(x) )
            x = 0; // safety check
        if ( Double.isNaN(y) || Double.isInfinite(y) )
            y = 0; // safety check
        
        if ( xAlign == Constants.CENTER ) {
            x = x-(w/2);
        } else if ( xAlign == Constants.RIGHT ) {
            x = x-w;
        }
        if ( yAlign == Constants.CENTER ) {
            y = y-(h/2);
        } else if ( yAlign == Constants.BOTTOM ) {
            y = y-h;
        }
        p.setLocation(x,y);
    }
    
    /**
     * @see prefuse.render.Renderer#render(java.awt.Graphics2D, prefuse.visual.VisualItem)
     */
    public void render(Graphics2D g, VisualItem item) {
    	RectangularShape shape = (RectangularShape)getShape(item);
        if ( shape == null ) return;
        
        // now render the image and text
        String text = m_text;
        
        if (text == null)
            return;
                        
        boolean useInt = 1.5 > Math.max(g.getTransform().getScaleX(),
                                        g.getTransform().getScaleY());

        // fill the shape, if requested
        int type = getRenderType(item);
        if ( type==RENDER_TYPE_FILL || type==RENDER_TYPE_DRAW_AND_FILL )
            GraphicsLib.paint(g, item, shape, getStroke(item), RENDER_TYPE_FILL);
        
        // render text
        int textColor = item.getTextColor();
        double radius = (item.getDouble("outerRadius")+ item.getDouble("innerRadius")) /2;
        if ( text != null && ColorLib.alpha(textColor) > 0) {
            g.setPaint(ColorLib.getColor(textColor));
            g.setFont(m_font);
            FontMetrics fm = DEFAULT_GRAPHICS.getFontMetrics(m_font);
            
            drawString(g, fm, item, text, useInt, item.getDouble("startAngle"), radius);
        }
    
        // draw border
        if (type==RENDER_TYPE_DRAW || type==RENDER_TYPE_DRAW_AND_FILL) {
            GraphicsLib.paint(g,item,shape,getStroke(item),RENDER_TYPE_DRAW);
        }
//        
        
    }
    
    /**
     * Return the graphics space transform applied to this item's shape, based on rotation.
     * 
     * CMC test code Dec 17, 2006
     * 
     * @param item the VisualItem
     * @return the graphics space transform, or null if none
     */
    protected AffineTransform getTransform(VisualItem item) {
    	return null;
    }
    
    private final void drawString(Graphics2D g, FontMetrics fm, VisualItem item, String text,
            boolean useInt, double rotation, double radius)
    {
        double advance = fm.stringWidth(text); // we approximate the angular arc length of the text as the linear width
        double degToRadFactor = Math.PI/180;
        double startAngle = rotation * degToRadFactor;
        double endAngle = startAngle + item.getDouble("angleExtent") * degToRadFactor;
    	
    	if ((endAngle/2+startAngle/2) < Math.PI) {
	    	// TOP HALF OF CIRCLE
    		
    		// TODO make this a better calculation to position the text in the node
	    	radius -= fm.getAscent()/2;
	    	
	    	double arcLength = endAngle * radius - startAngle * radius; 
	    	double scaleFactor = arcLength / advance;
	    	
	    	// make sure it fits in height
	        if ((fm.getHeight()) * scaleFactor > item.getDouble("outerRadius")-item.getDouble("innerRadius")) 
	        	scaleFactor = (item.getDouble("outerRadius")-item.getDouble("innerRadius")) / fm.getHeight();
	        
	        // make sure font height is less that maximum
	        if (scaleFactor * fm.getHeight() > maximumRenderSize)
	        	scaleFactor = maximumRenderSize / fm.getHeight();
	        
	        // center the text by advancing the start angle
	        double bufferLength = (arcLength - (advance * scaleFactor))/2 ;
	        endAngle -= bufferLength / radius;
	        
	        // if it's large enough to see (taller than 8 pixels), then draw
	        if(fm.getHeight() * scaleFactor * g.getTransform().getScaleY() > minimumRenderSize){ 
	        	int characterCount = text.length();
	            double angleAdvance = endAngle;
	            double x1 = Math.cos(angleAdvance) * radius;
	            double y1 = Math.sin(angleAdvance) * radius;
	            double angle = Math.atan(x1/y1);
	            
	            aT1 = g.getTransform();
	            
	            // get layout origin -- need to update this if it's not the display center
	            if (item instanceof DecoratorItem)
	            	g.translate(((DecoratorItem)item).getDecoratedItem().getX(), ((DecoratorItem)item).getDecoratedItem().getY());
	            else 
	            	g.translate(item.getVisualization().getDisplay(0).getWidth()/2,
		            		item.getVisualization().getDisplay(0).getHeight()/2);
	            
	            g.scale(scaleFactor, scaleFactor);
	            for (int i = 0; i < characterCount; i++) {
	            	aT2 = g.getTransform();
	            	char character = text.charAt(i);
	                angle = Math.atan(x1/y1);
	                g.rotate(angle);
	                if (angleAdvance > Math.PI) {
	                	g.rotate(Math.PI);
	                }
	                g.translate(0, -1*radius/scaleFactor);
	                g.drawString(text.substring(i, i+1), 0, 0);
	                g.setTransform(aT2);
	                
	                advance = fm.charWidth(character) * scaleFactor;
	                angleAdvance -= advance/radius;
	                x1 = Math.cos(angleAdvance) * radius;
	                y1 = Math.sin(angleAdvance) * radius;
	            } //end for each character
	            g.setTransform(aT1);
	        }//end check if we should draw at all
	        else{
	            //in case we didn't draw a label we will draw an arc in text
	        	//g.drawArc(x, y, width, height, startAngle, arcAngle)
	        }
	    } else {	    	
	    	// BOTTOM HALF OF CIRCLE
	    	
	    	// TODO make this a better calculation to position the text in the node
	    	radius += fm.getAscent()/2;
	        
	    	double arcLength = endAngle * radius - startAngle * radius; 
	    	double scaleFactor = arcLength / advance;
	        
	    	// make sure it fits in height
	        if ((fm.getHeight()) * scaleFactor > item.getDouble("outerRadius")-item.getDouble("innerRadius")) 
	        	scaleFactor = (item.getDouble("outerRadius")-item.getDouble("innerRadius")) / fm.getHeight();
	        
	      	// make sure font height is less that maximum
	        if (scaleFactor * fm.getHeight() > maximumRenderSize)
	        	scaleFactor = maximumRenderSize / fm.getHeight();
	        
	        // center the text by advancing the start angle
	        double bufferLength = (arcLength - (advance * scaleFactor))/2 ;
	        startAngle += bufferLength/radius;
	      	
	        // if it's large enough to see (taller than 8 pixels), then draw
	        if(fm.getHeight() * scaleFactor * g.getTransform().getScaleY() > minimumRenderSize){ 
	        	int characterCount = text.length();
	            double angleAdvance = startAngle;
	            double x1 = Math.cos(angleAdvance) * radius;
	            double y1 = Math.sin(angleAdvance) * radius;
	            double angle = Math.atan(x1/y1);
	            
	            aT1 = g.getTransform();
	            
	            // get layout origin -- need to update this if it's not the display center
	            if (item instanceof DecoratorItem)
	            	g.translate(((DecoratorItem)item).getDecoratedItem().getX(), ((DecoratorItem)item).getDecoratedItem().getY());
	            else 
	            	g.translate(item.getVisualization().getDisplay(0).getWidth()/2,
		            		item.getVisualization().getDisplay(0).getHeight()/2);

	            g.scale(scaleFactor, scaleFactor);
	            for (int i = 0; i < characterCount; i++) {
	            	aT2 = g.getTransform();
	            	char character = text.charAt(i);
	                angle = Math.atan(x1/y1);
	                g.rotate(angle);
	                if (angleAdvance < Math.PI) {
	                	g.rotate(Math.PI);
	                }
	                g.translate(0, radius/scaleFactor);
	                g.drawString(text.substring(i, i+1), 0, 0);
	                g.setTransform(aT2);
	                
	                advance = fm.charWidth(character) * scaleFactor;
	                angleAdvance += advance/radius;
	                x1 = Math.cos(angleAdvance) * radius;
	                y1 = Math.sin(angleAdvance) * radius;
	            } //end for each character
	            g.setTransform(aT1);
	        }//end check if we should draw at all
	        else{
	            //in case we didn't draw a label we will draw an arc in text
	        	//g.drawArc(x, y, width, height, startAngle, arcAngle)
	        }
	    }//end of if !(text=="") 
    }
    	
    // ------------------------------------------------------------------------
    
    
} // end of class ArcLabelRenderer

