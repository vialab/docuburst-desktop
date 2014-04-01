/* CVS $Id: SlidingPanel.java,v 1.1 2007/11/16 06:38:42 cmcollin Exp $ */
package ca.utoronto.cs.wordnetexplorer.swing;

/*
 * try running this, move mouse to top to expose component, move away to
 * hide it, also can press the button multiple times to expose it.
 */
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

/**
 * This class experiments with the 'animation' of showing and hiding a
 * JComponent.
 * 
 * When the mouse is brought close to the top of the panel, a label 'slides'
 * down into view. When the mouse moves away from the top of the panel, the
 * label 'rolls up' out of view. Two Swing Timers are used: 1. Slides the
 * label into view. 2. Rolls the label up out of view.
 * 
 * Note that a null layout is used with absolute positioning. Null layout
 * should be fine for the ImageViewer panel where the image is essentially
 * painted as the background. That is, when the toolbar slides down, it
 * won't push the image down, it will slide over and cover the top of the
 * image.
 * 
 * @author Ted Hill
 * @author Christopher Collins
 */
public class SlidingPanel extends JPanel {
    private static final int TIMER_INTERVAL = 1;

    private static final int PIXEL_DELTA = 2;

    private JButton button;
    public JComponent slidingComponent;
    
    private int vertOffset;

    private Timer showTimer = new ShowLabelTimer();

    private Timer hideTimer = new HideLabelTimer();

    private int slidingComponentHeight;

    private int slidingComponentWidth;
    
    public static final int TOP = 0;
    public static final int BOTTOM = 1;
    private int location;
    private boolean lock;
    
    public SlidingPanel() {
        super();
    }
    
    public SlidingPanel(LayoutManager layout) {
        super(layout);
    }
    
    public void addSlidingComponent(JComponent _slidingComponent, int position) {
        slidingComponent = _slidingComponent; 
        slidingComponentHeight = slidingComponent.getHeight();
        location = position;

        add(slidingComponent);
        slidingComponent.setVisible(false);
        lock = false;
        
        if (position == TOP) {
            positionComponentTop(-1*slidingComponent.getHeight());
        } if (position == BOTTOM) { 
            positionComponentBottom(slidingComponent.getHeight());
        }

        addMouseMotionListener(new MouseMotionListener() {
            public void mouseDragged(MouseEvent e) {
                // TODO: add an implementation
            }

            public void mouseMoved(MouseEvent e) {
                final int height = e.getY();

                if (!lock) {
                    if (location == TOP) {
                        if (height < 10 && !showTimer.isRunning() && !slidingComponent.isVisible()) {
                            showTimer.start();
                        } else if (height > slidingComponent.getHeight() + 5 && !hideTimer.isRunning()
                                && slidingComponent.isVisible()) {
                            hideTimer.start();
                        }
                    } else {
                        if (height > (getHeight()-getInsets().bottom-10) && !showTimer.isRunning() && !slidingComponent.isVisible()) {
                            showTimer.start();
                        } else if (height < (getHeight()-getInsets().bottom-slidingComponent.getHeight() - 5) && !hideTimer.isRunning()
                                && slidingComponent.isVisible()) {
                            hideTimer.start();
                        }
                    }
                }
            }
        });
        
    }
    
    private void lock(boolean lock) {
        this.lock = lock;
        if (lock) {
            if (location == TOP) {
                positionComponentTop(-slidingComponent.getHeight());
            } else {
                positionComponentBottom(-slidingComponent.getHeight());
            }
            slidingComponent.setVisible(true);
        } 
    }
    
    private void positionComponentTop(final int offset) {
        int panelWidth = getWidth();
        Insets insets = getInsets();
        Dimension size = slidingComponent.getPreferredSize();

        slidingComponent.setBounds(insets.left, offset + insets.top, size.width,
                size.height);
    }
    
    private void positionComponentBottom(final int offset) {
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        Insets insets = getInsets();
        Dimension slidingComponentSize = slidingComponent.getPreferredSize();
        
        slidingComponent.setBounds(insets.left, panelHeight - insets.bottom - offset, 
                slidingComponentSize.width, slidingComponentSize.height);
    }
    
    private JLabel initLabel() {
        JLabel label = new JLabel("Test Label");

        label.setBackground(Color.YELLOW);
        label.setOpaque(true);

        label.setVisible(false);

        return label;
    }

    /**
     * Timer class that is called back at intervals.
     */
    private class ShowLabelTimer extends Timer implements ActionListener {
        ShowLabelTimer() {
            // first param is callback interval in milliseconds
            super(TIMER_INTERVAL, null); // call back in millis
            addActionListener(this);
        }

        /**
         * Starts the <code>Timer</code>, causing it to start sending
         * action events to its listeners.
         * 
         * @see #stop
         */
        public void start() {
            if (location == TOP)
                vertOffset = -slidingComponent.getHeight();
            else 
                vertOffset = 0;
            slidingComponent.setVisible(true);
            super.start();
        }

        /**
         * update the time display
         */
        public void actionPerformed(ActionEvent e) {
            
            if (location == TOP) {
                if (vertOffset <= 0) {
                    positionComponentTop(vertOffset);
                } else {
                    showTimer.stop();
                }
                vertOffset += PIXEL_DELTA;
            } else {
                if (vertOffset <= slidingComponent.getHeight()) {
                    positionComponentBottom(vertOffset);
                } else {
                    showTimer.stop();
                }
                vertOffset += PIXEL_DELTA;
            }
        }
    }

    /**
     * Timer class that is called back at intervals.
     */
    private class HideLabelTimer extends Timer implements ActionListener {
        HideLabelTimer() {
            // first param is callback interval in milliseconds
            super(TIMER_INTERVAL, null); // call back in millis
            addActionListener(this);
        }

        /**
         * Stops the <code>Timer</code>, causing it to stop sending
         * action events to its listeners.
         * 
         * @see #start
         */
        public void stop() {
            slidingComponent.setVisible(false);
            super.stop(); // TODO: add an implementation
        }

        /**
         * update the time display
         */
        public void actionPerformed(ActionEvent e) {
            if (location == TOP) {
                if (vertOffset >= (-slidingComponentHeight)) {
                    vertOffset -= PIXEL_DELTA;
                    positionComponentBottom(vertOffset);
                } else {
                    hideTimer.stop();
                }
            } else {
                if (vertOffset >= 4) {
                    vertOffset -= PIXEL_DELTA;
                    positionComponentBottom(vertOffset);
                } else {
                    hideTimer.stop();
                }
            }
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("SlidingLabelTest");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        SlidingPanel slidingPanel = new SlidingPanel();
        slidingPanel.addSlidingComponent(new WordNetSearchPanel(null), SlidingPanel.BOTTOM);
        frame.getContentPane().add(slidingPanel, BorderLayout.CENTER);
        
        // frame.pack( );
        frame.setSize(400, 200);
        frame.setVisible(true);
    }
}
