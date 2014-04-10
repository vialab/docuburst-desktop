/* CVS $Id: TwoComponentSlidingPanel.java,v 1.1 2007/11/16 06:38:42 cmcollin Exp $ */
package ca.utoronto.cs.wordnetexplorer.swing;

/*
 * try running this, move mouse to top to expose component, move away to
 * hide it, also can press the button multiple times to expose it.
 */
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import prefuse.util.ui.UILib;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

/**
 * This class experiments with the 'animation' of showing and hiding a
 * JComponent.
 * 
 * When the mouse is brought close to the top of the panel, a label 'slides'
 * down into view. When the mouse moves away from the top of the panel, the
 * label 'rolls up' out of view. Two Swing Timers are used: 1. Slides the
 * label into view. 2. Rolls the label up out of view.  Secondary timers ensure
 * view and roll up only occur if the mouse changes state for a sufficient waitTime.
 * 
 * @author Christopher Collins
 */
public class TwoComponentSlidingPanel extends JPanel implements ComponentListener {
    
    /**
     * The frequency of animation updates in millis.
     */
    
    private final static int TIMER_INTERVAL = 1;

    /**
     * The amount of time the mouse must be on/off the sliding panel before animation is activated.
     * TODO add functionality of waitTime
     */
    private int waitTime;
    
    /**
     * Set ignore to true to skip the next mouseClick for the lock open/closed function.
     */
    public boolean ignore;
    
    /**
     * Controls the speed of animation; number of pixels to move per time step.
     */
    private int pixelDelta;

    private JComponent slidingComponent;
    private JComponent complementaryComponent; 
    
    private int offset;

    private Timer showTimer;

    private Timer hideTimer;
    
    private Timer showWaitTimer;
    
    private Timer hideWaitTimer;

    /**
     * Event used to notify when component is opened or closed.
     */
    private ChangeEvent changeEvent;
    
    /**
     * Stores the original position and size of the complmentary component.
     */
    private Rectangle oldBounds;
    
    public static enum Position{TOP, BOTTOM, LEFT, RIGHT};
    
    /**
     * Indicates whether sliding panel is located at the top or the bottom of this panel.
     */
    private Position location;
    
    /**
     * Controls whether the sliding portion is locked as visible or not.
     */
    private boolean lock;
    
    /**
     * Controls whether the mouse is inside or outside the section; used for delayed hiding.
     */
    private boolean exited; 
    
    /**
     * Indicates whether the sliding portion is on screen or hidden.
     */
    private boolean isVisible;
    
    private Color backgroundColor;
    private BufferedImage pinUp;
    private BufferedImage pinDown;
    
    public TwoComponentSlidingPanel(JComponent slidingComponent, JComponent complementComponent, Position position, int pixelDelta, int waitTime) {
        super();
        setLayout(null);
        backgroundColor = UIManager.getLookAndFeel().getDefaults().getColor("JPanel.background");
    	try {
    		pinUp = ImageIO.read(this.getClass().getResource("images/pin_up.png"));
    		pinDown = ImageIO.read(this.getClass().getResource("images/pin_down.png"));
    	} catch (IOException e) {}
    		
        slidingComponent.setBackground(Color.LIGHT_GRAY);
        addSlidingComponent(slidingComponent, position);
        addComplementaryComponent(complementComponent);
        this.pixelDelta = pixelDelta;
        this.waitTime = waitTime;
        exited = true;
        showTimer = new ShowLabelTimer();
        hideTimer = new HideLabelTimer();
        showWaitTimer = new ShowWaitTimer();
        hideWaitTimer = new HideWaitTimer();
        ignore = false;
        addComponentListener(this);
    }

    public void addComplementaryComponent(JComponent complementComponent) {
        complementaryComponent = complementComponent;
        oldBounds = complementaryComponent.getBounds();
        add(complementaryComponent);
        initialize();
    }
    
    public void fireStateChanged() {
    	// Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        // Go by 2s because list is (Class, listener)
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==ChangeListener.class) {
                // Lazily create the event:
                if (changeEvent == null)
                    changeEvent = new ChangeEvent(this);
                ((ChangeListener)listeners[i+1]).stateChanged(changeEvent);
            }
        }
    }
    
    public void addSlidingComponent(JComponent _slidingComponent, Position position) {
        slidingComponent = _slidingComponent; 
        
        location = position;
        lock = false;
    
        slidingComponent.addMouseListener(new MouseListener() {
            public void mouseEntered(MouseEvent e) {
                exited = false;
                if (!lock && !hideTimer.isRunning() && !isVisible){
                    if ((!hideWaitTimer.isRunning()) && (!showWaitTimer.isRunning())) {
                        showWaitTimer.start();
                    }
                }
            }

            public void mouseExited(MouseEvent e) {
                //System.out.println("E: (" +e.getX() + ", " + e.getY() + ") SC: (" + slidingComponent.getWidth() + ", " + slidingComponent.getHeight() + ")");
                if ((e.getY() <= 0) ||
                        (e.getY() >= slidingComponent.getHeight()) ||
                        (e.getX() <= 0) ||
                        (e.getX() >= slidingComponent.getWidth())) { 
                    if (!lock && !hideTimer.isRunning() && isVisible) {
                        exited = true;
                        if ((!hideWaitTimer.isRunning()) && (!showWaitTimer.isRunning())) {
                            hideWaitTimer.start();
                        }
                    }
                }
            }
            
            public void mousePressed(MouseEvent e) {
            }
            
            public void mouseReleased(MouseEvent e) {
            }

            public void mouseClicked(MouseEvent e) {
                // ignore mouse clicks that activate elements of the slidingComponent
                if (!ignore) {
                    lock = !lock;
                    slidingComponent.getBackground();
                    if (lock) 
                        slidingComponent.setBackground(Color.GRAY);
                    else 
                        slidingComponent.setBackground(Color.LIGHT_GRAY);
                } else {
                    //reset ignore
                    ignore = false;
                }
            }
        });
        add(slidingComponent);
        initialize();
    }
    
    private void lock(boolean lock) {
        this.lock = lock;
        if (lock) {
            if (location == Position.TOP) {
                positionComponentTop(-slidingComponent.getHeight());
            } 
            if (location == Position.BOTTOM) {
                positionComponentBottom(-slidingComponent.getHeight());
            }
            if (location == Position.LEFT) {
                positionComponentLeft(-slidingComponent.getHeight());
            }
            if (location == Position.RIGHT) {
                positionComponentRight(-slidingComponent.getHeight());
            }
        } 
    }
    
    private void positionComponentTop(final int offset) {
        int panelWidth = getWidth();
        Insets insets = getInsets();
        Dimension size = slidingComponent.getPreferredSize();
        if (complementaryComponent != null) {
            complementaryComponent.setBounds(oldBounds.x, oldBounds.y + slidingComponent.getHeight() + offset, oldBounds.width, oldBounds.height - (offset + slidingComponent.getHeight()));
            complementaryComponent.validate();
        }
        
        slidingComponent.setBounds(insets.left, offset + insets.top, size.width,
                size.height);
    }
    
    private void positionComponentBottom(final int offset) {
        int panelHeight = getHeight();
        Insets insets = getInsets();
        Dimension slidingComponentSize = slidingComponent.getPreferredSize();
        if (complementaryComponent != null) {
            complementaryComponent.setBounds(oldBounds.x, oldBounds.y, oldBounds.width, oldBounds.height-offset);
            complementaryComponent.validate();
        }
        
        slidingComponent.setBounds(insets.left, panelHeight - insets.bottom - offset, 
                slidingComponentSize.width, slidingComponentSize.height);
    }
    
    private void positionComponentLeft(final int offset) {
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        Insets insets = getInsets();
        Dimension slidingComponentSize = slidingComponent.getPreferredSize();
        if (complementaryComponent != null) {
            complementaryComponent.setBounds(oldBounds.x + offset + slidingComponent.getWidth(), oldBounds.y, oldBounds.width - (offset+slidingComponent.getWidth()), oldBounds.height);
            complementaryComponent.validate();
        }
        slidingComponent.setBounds(insets.left + offset, insets.top, slidingComponentSize.width,
                slidingComponentSize.height);
    }

    private void positionComponentRight(final int offset) {
        int panelWidth = getWidth();
        Insets insets = getInsets();
        Dimension slidingComponentSize = slidingComponent.getPreferredSize();
        if (complementaryComponent != null) {
            complementaryComponent.setBounds(oldBounds.x, oldBounds.y, oldBounds.width-offset, oldBounds.height);
            complementaryComponent.validate();
        }
        
        slidingComponent.setBounds(panelWidth - insets.left - offset, insets.bottom, 
                slidingComponentSize.width, slidingComponentSize.height);
    }
    
    private JLabel initLabel() {
        JLabel label = new JLabel("Test Label");

        label.setBackground(Color.YELLOW);
        label.setOpaque(true);

        label.setVisible(false);

        return label;
    }

    public void initialize() {
        if (location == Position.TOP) 
            positionComponentTop(-slidingComponent.getHeight() + 5);
        if (location == Position.BOTTOM) 
            positionComponentBottom(5);
        if (location == Position.LEFT)
            positionComponentLeft(-slidingComponent.getWidth() + 5);
        if (location == Position.RIGHT)
            positionComponentRight(5);
        isVisible = false;
        lock = false;
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
            if (location == Position.TOP)
                offset = -slidingComponent.getHeight();
            if (location == Position.BOTTOM) 
                offset = 0;
            if (location == Position.LEFT) 
                offset = -slidingComponent.getWidth();
            if (location == Position.RIGHT)
                offset = 0;
            super.start();
        }

        /**
         * Stops the <code>Timer</code>, causing it to stop sending
         * action events to its listeners.
         * 
         * @see #start
         */
        public void stop() {
            isVisible = true;
            super.stop(); 
            fireStateChanged();
        }
        
        /**
         * timer update
         */
        public void actionPerformed(ActionEvent e) {
            if (location == Position.TOP) {
                positionComponentTop(offset);
                if (offset == 0) {
                    showTimer.stop();
                } else {
                    offset += pixelDelta;
                    if (offset > 0) offset = 0;
                }
            }
            if (location == Position.BOTTOM) {
                positionComponentBottom(offset);        
                if (offset == slidingComponent.getHeight()) {
                    showTimer.stop();
                } else {
                    offset += pixelDelta;
                    if (offset > slidingComponent.getHeight()) offset = slidingComponent.getHeight();
                }
            }
            if (location == Position.LEFT) {
                positionComponentLeft(offset);
                if (offset == 0) {
                    showTimer.stop();
                } else {
                    offset += pixelDelta;
                    if (offset > 0) offset = 0;
                }
            }
            if (location == Position.RIGHT) {
                positionComponentRight(offset);
                if (offset == slidingComponent.getWidth()) {
                    showTimer.stop();
                } else {
                    offset += pixelDelta;
                    if (offset > slidingComponent.getWidth()) offset = slidingComponent.getWidth();
                } 
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
            isVisible = false;
            super.stop(); 
            fireStateChanged();
        }

        /**
         * timer update
         */
        public void actionPerformed(ActionEvent e) {
            if (location == Position.TOP) {
                if (offset >= (-slidingComponent.getHeight())+pixelDelta) {
                    offset -= pixelDelta;
                    positionComponentTop(offset);
                } else {
                    initialize();
                    hideTimer.stop();
                }
            } 
            if (location == Position.BOTTOM) {
                if (offset >= pixelDelta) {
                    offset -= pixelDelta;
                    positionComponentBottom(offset);
                } else {
                    initialize();
                    hideTimer.stop();
                }
            }
            if (location == Position.LEFT) {
                if (offset >= (-slidingComponent.getWidth()) + pixelDelta) {
                    offset -= pixelDelta;
                    positionComponentLeft(offset);
                } else {
                    initialize();
                    hideTimer.stop();
                }
            }
            if (location == Position.RIGHT) {
                if (offset >= pixelDelta) {
                    offset -= pixelDelta;
                    positionComponentRight(offset);
                } else {
                    initialize();
                    hideTimer.stop();
                }
            }
        }
    }

    /**
     * Timer class that is called back at intervals.
     */
    private class HideWaitTimer extends Timer implements ActionListener {
        HideWaitTimer() {
            // first param is callback interval in milliseconds
            super(waitTime, null); // call back in millis
            setRepeats(false);
            addActionListener(this);
        }

        /**
         * if still exited, hide
         */
        public void actionPerformed(ActionEvent e) {
            if (exited) {
                hideTimer.start();
            }
        }
    }

    /**
     * Timer class that is called back at intervals.
     */
    private class ShowWaitTimer extends Timer implements ActionListener {
        ShowWaitTimer() {
            // first param is callback interval in milliseconds
            super(waitTime, null); // call back in millis
            setRepeats(false);
            addActionListener(this);
        }

        /**
         * if still entered, show
         */
        public void actionPerformed(ActionEvent e) {
            if (!exited) {
                showTimer.start();
            } 
        }
    }
 
    public void componentResized(ComponentEvent e) {
        if ((location == Position.TOP) || (location == Position.BOTTOM)) {
            // make complementary component full size of resized panel
            Dimension size = this.getSize();
            Insets insets = this.getInsets();
            complementaryComponent.setBounds(insets.left, insets.top, size.width, size.height);
            oldBounds = complementaryComponent.getBounds();
            // resize width of sliding component to match panel
            slidingComponent.setPreferredSize(new Dimension((int)size.getWidth(), (int)slidingComponent.getSize().getHeight()));
            initialize();
         }
        if ((location == Position.LEFT) || (location == Position.RIGHT)) {
            // make complementary component full size of resized panel
            Dimension size = this.getSize();
            Insets insets = this.getInsets();
            complementaryComponent.setBounds(insets.left, insets.top, size.width, size.height);
            oldBounds = complementaryComponent.getBounds();
            // resize width of sliding component to match panel
            slidingComponent.setPreferredSize(new Dimension((int)slidingComponent.getSize().getWidth(), (int)getSize().getHeight()));
            initialize();
         }
    }
    
    public void componentHidden(ComponentEvent e) {
    }

    public void componentMoved(ComponentEvent e) {
    }
    
    public void componentShown(ComponentEvent e) {
        complementaryComponent.setPreferredSize(this.getSize());
        oldBounds = complementaryComponent.getBounds();
        slidingComponent.setPreferredSize(new Dimension((int)this.getSize().getWidth(), (int)slidingComponent.getSize().getHeight()));
    }
    
    public static void main(String[] args) {
        JFrame frame = new JFrame("SlidingLabelTest");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        TwoComponentSlidingPanel slidingPanel = new TwoComponentSlidingPanel(new WordNetSearchPanel(null), new JPanel(), TwoComponentSlidingPanel.Position.LEFT, 4, 1000);
        frame.getContentPane().add(slidingPanel, BorderLayout.CENTER);
        
        // frame.pack( );
        frame.setSize(400, 200);
        frame.setVisible(true);
    }

	public void addChangeListener(ChangeListener listener) {
		this.listenerList.add(ChangeListener.class, listener);
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		if ((pinDown != null) && (pinUp != null)) {
			if (lock)
				g.drawImage(pinDown, slidingComponent.getWidth()-slidingComponent.getInsets().right-pinUp.getWidth(), slidingComponent.getY()+slidingComponent.getInsets().top, backgroundColor, slidingComponent);
			else
				g.drawImage(pinUp, slidingComponent.getWidth()-slidingComponent.getInsets().right-pinUp.getWidth(), slidingComponent.getY()+slidingComponent.getInsets().top, backgroundColor, slidingComponent);
		}
 	}
}
