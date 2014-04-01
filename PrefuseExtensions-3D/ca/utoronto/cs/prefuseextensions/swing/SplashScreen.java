package ca.utoronto.cs.prefuseextensions.swing;

import javax.swing.*;
import java.awt.*;
import java.net.*;

public class SplashScreen extends Window {
    private Image image;

    private boolean paintCalled = false;

    /**
     * Override default update to paint only our image -- do not fill the frame with background first.
     */
    public void update(Graphics g) {
        paint(g);
    }

    /**
     * Paints the image on the window.
     */
    public void paint(Graphics g) {
        g.drawImage(image, 0, 0, this);

        // Notify method splash that the window has been painted.
        // Note: To improve performance we do not enter the synchronized block unless we have to.
        if (!paintCalled) {
            paintCalled = true;
            synchronized (this) {
                notifyAll();
            }
        }
    }

    /**
     * Create a splash screen (borderless graphic for display while other operations are taking place).
     *  
     * @param filename a class-relative path to the splash graphic
     * @param callingClass the class to which the graphic filename location is relative 
     */
    public SplashScreen(String filename, Class callingClass) {
        super(new Frame());
        URL imageURL = callingClass.getResource(filename);
        image = Toolkit.getDefaultToolkit().createImage(imageURL);
        // Load the image
        MediaTracker mt = new MediaTracker(this);
        mt.addImage(image, 0);
        try {
            mt.waitForID(0);
        } catch (InterruptedException ie) {
        }
        
        // Center the window on the screen
        int imgWidth = image.getWidth(this);
        int imgHeight = image.getHeight(this);
        setSize(imgWidth, imgHeight);
        Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenDim.width - imgWidth) / 2,
                (screenDim.height - imgHeight) / 2);
   
        setVisible(true);
        repaint();
        // if on a single processor machine, wait for painting (see Fast Java Splash Screen.pdf)
        if (!EventQueue.isDispatchThread()) { 
            synchronized (this) {
                while (!this.paintCalled) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    }
}