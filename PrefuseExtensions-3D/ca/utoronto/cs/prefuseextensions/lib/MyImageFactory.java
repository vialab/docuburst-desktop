/* CVS $Id: MyImageFactory.java,v 1.1 2006/12/05 06:38:42 cmcollin Exp $ */
package ca.utoronto.cs.prefuseextensions.lib;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;

import prefuse.data.Tuple;
import prefuse.render.ImageFactory;
import prefuse.render.ImageFactory.LoadMapEntry;
import prefuse.util.io.IOLib;

public class MyImageFactory extends ImageFactory {

    public MyImageFactory() {
        super();
        // TODO Auto-generated constructor stub
    }

    public MyImageFactory(int maxImageWidth, int maxImageHeight) {
        super(maxImageWidth, maxImageHeight);
        // TODO Auto-generated constructor stub
    }

    
    /**
     * <p>Get the image associated with the given location string. If the image
     * has already been loaded, it simply will return the image, otherwise it
     * will load it from the specified location.</p>
     * 
     * <p>The imageLocation argument must be a valid resource string pointing
     * to either (a) a valid URL, (b) a file on the classpath, or (c) a file
     * on the local filesystem. The location will be resolved in that order.
     * </p>
     * 
     * @param imageLocation the image location as a resource string.
     * @return the corresponding image, if available
     * @throws IOException 
     */
    public Image getTwoByTwoImage(String imageLocation) {
        Image image = (Image) imageCache.get(imageLocation);
        if (image == null && !loadMap.containsKey(imageLocation)) {
            StringTokenizer sT = new StringTokenizer(imageLocation);
            if (sT.countTokens() != 4) return null;
            URL[] urls = {
                    IOLib.urlFromString(sT.nextToken()),
                    IOLib.urlFromString(sT.nextToken()),
                    IOLib.urlFromString(sT.nextToken()),
                    IOLib.urlFromString(sT.nextToken())
            };
            
            try {
            BufferedImage[] images = {
                    ImageIO.read(urls[0]),
                    ImageIO.read(urls[1]),
                    ImageIO.read(urls[2]),
                    ImageIO.read(urls[3])
                };
            
             int w = images[0].getWidth();
             int h = images[0].getHeight();
             BufferedImage total = new BufferedImage(2*w, 2*h, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = total.createGraphics();
                g.drawImage(images[0], 0, 0, null);
                g.drawImage(images[1], w, 0, null);
                g.drawImage(images[2], 0, h, null);
                g.drawImage(images[3], w, h, null);
            
                image = total;
            } catch (IOException e) {
                System.err.println("IO Exception reading images.");
                return null;
            } catch (IllegalArgumentException e) {
            	System.err.println("Problem getting image.");
            }
                // if set for synchronous mode, block for image to load.
            if ( !m_asynch ) {
                waitForImage(image);
                addImage(imageLocation, image);
            } else {
                int id = ++nextTrackerID;
                tracker.addImage(image, id);
                loadMap.put(imageLocation, new LoadMapEntry(id,image));    
            }
        } else if ( image == null && loadMap.containsKey(imageLocation) ) {
            LoadMapEntry entry = (LoadMapEntry)loadMap.get(imageLocation);
            if ( tracker.checkID(entry.id, true) ) {
                addImage(imageLocation, entry.image);
                loadMap.remove(imageLocation);
                tracker.removeImage(entry.image, entry.id);
            }
        } else {
            return image;
        }
        return (Image) imageCache.get(imageLocation);
    }
    
    /**
     * <p>Preloads images for use in a visualization. Images to load are
     * determined by taking objects from the given iterator and retrieving
     * the value of the specified field. The items in the iterator must
     * be instances of the {@link prefuse.data.Tuple} class.</p>
     * 
     * <p>Images are loaded in the order specified by the iterator until the
     * the iterator is empty or the maximum image cache size is met. Thus
     * higher priority images should appear sooner in the iteration.</p>
     * 
     * @param iter an Iterator of {@link prefuse.data.Tuple} instances
     * @param field the data field that contains the image location
     */
    public void preloadImages(Iterator iter, String field) {
        boolean synch = m_asynch;
        m_asynch = false;
        
        String loc = null;
        while ( iter.hasNext() && imageCache.size() <= m_imageCacheSize ) {
            // get the string describing the image location
            Tuple t = (Tuple)iter.next();
            loc = t.getString(field);
            if ( loc != null ) {
            	getTwoByTwoImage(loc);
            }
        }
        m_asynch = synch;
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub

    }

}
