/* CVS $Id: SplashWindow.java,v 1.1 2007/11/26 21:24:13 cmcollin Exp $ */

package ca.utoronto.cs.prefuseextensions.swing;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

/*
 * @(#)SplashWindow.java  2.2  2005-04-03
 *
 * Copyright (c) 2003-2005 Werner Randelshofer
 * http://www.randelshofer.ch/oop/javasplash/javasplash.html
 * Modified by Christopher Collins
 * Staldenmattweg 2, Immensee, CH-6405, Switzerland.
 * All rights reserved.
 *
 * This software is in the public domain.
 */
public class SplashWindow extends Window {
    public Image image;
    private static SplashWindow instance;
    private boolean paintCalled = false;

    public static void invokeMain(String className, String[] args) {
        try {
            Class.forName(className).getMethod("main",
                    new Class[] { String[].class }).invoke(null,
                    new Object[] { args });
        } catch (Exception e) {
            InternalError error = new InternalError(
                    "Failed to invoke main method");
            error.initCause(e);
            throw error;
        }
    }

    public static void splash(URL imageURL) {
        if (imageURL != null) {
            splash(Toolkit.getDefaultToolkit().createImage(imageURL));
        }
    }

    public static void splash(Image image) {
        if (instance == null && image != null) {

            Frame f = new Frame();

            instance = new SplashWindow(f, image);
            instance.setVisible(true);

            if (!EventQueue.isDispatchThread()
                    && Runtime.getRuntime().availableProcessors() == 1) {
                synchronized (instance) {
                    while (!instance.paintCalled) {
                        try {
                            instance.wait();
                        } catch (InterruptedException e) {
                        }
                    }
                }
            }
        }

    }

    private SplashWindow(Frame parent, Image image) {
        super(parent);
        this.image = image;

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

        // Users shall be able to close the splash window by
        // clicking on its display area. This mouse listener
        // listens for mouse clicks and disposes the splash window.
        MouseAdapter disposeOnClick = new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                // Note: To avoid that method splash hangs, we
                // must set paintCalled to true and call notifyAll.
                // This is necessary because the mouse click may
                // occur before the contents of the window
                // has been painted.
                synchronized (SplashWindow.this) {
                    SplashWindow.this.paintCalled = true;
                    SplashWindow.this.notifyAll();
                }
                dispose();
            }
        };
        addMouseListener(disposeOnClick);
    }

    /**
     * Updates the display area of the window.
     */
    public void update(Graphics g) {
        // Note: Since the paint method is going to draw an
        // image that covers the complete area of the component we
        // do not fill the component with its background color
        // here. This avoids flickering.
        paint(g);
    }

    /**
     * Paints the image on the window.
     */
    public void paint(Graphics g) {
        g.drawImage(image, 0, 0, this);

        // Notify method splash that the window
        // has been painted.
        // Note: To improve performance we do not enter
        // the synchronized block unless we have to.
        if (!paintCalled) {
            paintCalled = true;
            synchronized (this) {
                notifyAll();
            }
        }
    }

    public static void main(String[] args) {
        splash(SplashWindow.class.getResource("images/splash.jpg"));
        invokeMain("ca.utoronto.cs.radialwordnet.RadialWordNet", args);
        instance.dispose();
    }
}
