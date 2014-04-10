package ca.utoronto.cs.prefuseextensions.lib;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;

import prefuse.Constants;
import prefuse.DisplayComponent;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.ItemAction;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.FontAction;
import prefuse.controls.ControlAdapter;
import prefuse.controls.DragControl;
import prefuse.controls.HoverActionControl;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Tuple;
import prefuse.data.event.TupleSetListener;
import prefuse.data.expression.parser.ExpressionParser;
import prefuse.data.tuple.TupleSet;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.util.io.IOLib;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.InGroupPredicate;
import ca.utoronto.cs.prefuseextensions.layout.AxisInsets;
import ca.utoronto.cs.prefuseextensions.layout.AxisInsetsLayout;

public class Colors {
	public static final Logger LOGGER = Logger.getLogger(Colors.class.getName());
	static { LOGGER.setLevel(Level.WARNING); } 
		
	private HashMap<String,Integer> colors = new HashMap<String,Integer>();
    private static Graph g = new Graph();
    private HashMap<String,String[]> palettes = new HashMap<String,String[]>();
    private static ActionListener buttonListener = new ActionListener() {
    	public void actionPerformed(ActionEvent e) {
    		if (e.getSource() instanceof JButton) {
    			String paletteName = ((JButton)e.getSource()).getText();
    			String[] focusColors = c.palettes.get(paletteName);
    			TupleSet focus = display.getVisualization().getFocusGroup(Visualization.FOCUS_ITEMS);
    			focus.clear();
    			if (focusColors == null) return;
    			for (String focusColor : focusColors) {
    				Iterator nodeIterator = display.getVisualization().items(ExpressionParser.predicate("name='"+focusColor+"'"));
    				while (nodeIterator.hasNext()) {
    					display.getVisualization().getFocusGroup(Visualization.FOCUS_ITEMS).addTuple((Tuple)nodeIterator.next());
    				}
    			}
    		}
    	}
    };
    
    private static Colors c = new Colors();
    
    static DisplayComponent display;
    
    private static boolean blackBackground = false;
	
    private void readColours(String filename) {
    	InputStream fileInputStream;
		try {
			LOGGER.info("reading colours from " + filename);
	    	fileInputStream = new FileInputStream(new File(filename));
			readColours(fileInputStream);
		} catch (FileNotFoundException e) {
			LOGGER.severe("Unable to read colours from " + filename);
			System.exit(-1);
		}
    }
    
    private void readColours(InputStream is) {
    	try {
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line = br.readLine();
			while (line != null) {
				// skip comments
				if (line.startsWith("//")) {
					line = br.readLine();
					continue;
				}
				// parse line
				String[] parts = line.split(",");
				String name = parts[0].toUpperCase();
				String type = parts[1];
				LOGGER.info("Reading: " + name);

				if (type.equals("rgbai")) {
					colors.put(name,ColorLib.rgba(Integer.parseInt(parts[2]),Integer.parseInt(parts[3]),Integer.parseInt(parts[4]),Integer.parseInt(parts[5])));
				}
				if (type.equals("rgbaf")) {
					colors.put(name,ColorLib.rgba(Float.parseFloat(parts[2]),Float.parseFloat(parts[3]),Float.parseFloat(parts[4]),Float.parseFloat(parts[5])));
				} 
				if (type.equals("i")) {
					colors.put(name,Integer.parseInt(parts[2]));
				}
				if (type.equals("p")) {
					ArrayList<String> paletteColours = new ArrayList<String>();
					for (int i = 2; i < parts.length; i++) {
						paletteColours.add(parts[i]);
					}
					palettes.put(name, paletteColours.toArray(new String[paletteColours.size()]));
				}
				line = br.readLine();
			}
		} catch (IOException e) {
			LOGGER.severe("Error reading colours from stream.");
			e.printStackTrace();
		}
    }
    
    private static JPanel createPalettePanel() {
    	JPanel panel = new JPanel();
    	panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
    	Iterator<String> paletteNames = c.palettes.keySet().iterator();
    	while (paletteNames.hasNext()) {
    		String name = paletteNames.next();
    		JButton button = new JButton(name);
    		button.addActionListener(buttonListener);
    		panel.add(button);
    		panel.add(Box.createVerticalStrut(2));
    	}
    	JButton button = new JButton("CLEAR SELECTION");
		button.addActionListener(buttonListener);
		panel.add(button);
    	return panel;
    }

    public Colors(String filename) {
    	super();
    	readColours(filename);
    }
    
    public Colors() {
    	super();
    	// load from local colours repository
        readColours(getClass().getResourceAsStream("/data/colours.csv"));
    }
    
    public Set getColorNames() {
    	return colors.keySet();
    }
    
    public int getColor(String name) {
    	return colors.get(name);
    }
    
    public static Colors getInstance() {
    	return c;
    }
    
    public int[] getPalette(String name) {
    	String[] colorNames = palettes.get(name);
    	int[] colors = new int[colorNames.length];
    	for (int i = 0; i < colorNames.length; i++) {
    		colors[i] = getColor(colorNames[i]);
    	}
    	return colors;
    }
    
    public void addColor(String name, int color) {
    	colors.put(name, color);
    }
    
    public int getSize() {
    	return colors.size();
    }
   
    
    // program to allow cycling through colours 
    public static void main(String[] args) {
    	final JFrame jFrame = new JFrame();
    	// if we are running the color demo, show info
    	LOGGER.setLevel(Level.INFO);
    	
    	display = new DisplayComponent(new Visualization());
    	
    	if (args.length > 0) {
    		URL imageURL = IOLib.urlFromString(args[0]); 
    		if ( imageURL == null ) {
    			LOGGER.warning("Null background image");
    		} else {
    			Image image = Toolkit.getDefaultToolkit().createImage(imageURL);
    			display.setBackgroundImage(image, true, true);
    		}
    	}

    	g.addColumn("color", int.class);
    	g.addColumn("x", int.class);
    	g.addColumn("y", int.class);
    	g.addColumn("name", String.class);
		
    	TreeSet<String> sortedNames = new TreeSet<String>();
    	Iterator<String> nameIterator = c.getColorNames().iterator();
    	while (nameIterator.hasNext()) {
        	sortedNames.add(nameIterator.next());
    	}
    	nameIterator = sortedNames.iterator();
    		
    	int i = 0;
    	String name = new String();
    	while (nameIterator.hasNext()) {
        		Node n = g.addNode();
    		name = nameIterator.next();
    		n.setInt("color", c.getColor(name));
    		n.setInt("x", i % 10);
    		n.setInt("y", i / 10);
    		n.setString("name", name);
    		i++;
    	}
    	
    	// add full color chooser on ctrl-n(ew)
    	display.addKeyListener(new KeyAdapter() {
    		@Override
    		public void keyPressed(KeyEvent e) {
    			if ((e.getKeyCode() == KeyEvent.VK_N) && ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) == (KeyEvent.CTRL_DOWN_MASK))) {
    				Color selectedColor = JColorChooser.showDialog(display, "Custom Color", Color.BLACK);
    				if (selectedColor != null) {
    					// output selection
    					LOGGER.info("Custom color = " + selectedColor.toString() + " " + ColorLib.color(selectedColor));
    					// add color
    					String name = "Custom Colour" + c.getSize();
    					c.addColor(name,ColorLib.color(selectedColor));
    					Node n = g.addNode();
    		    		n.setInt("color", ColorLib.color(selectedColor));
    		    		n.setInt("x", (c.getSize()-1) % 10);
    		    		n.setInt("y", (c.getSize()-1) / 10);
    		    		n.setString("name", name);
    		    		display.getVisualization().run("layout");
    				}
    			}
    		}
    	});
    	
    	LabelRenderer l = new LabelRenderer("name");
    	l.setHorizontalPadding(20);
    	l.setVerticalPadding(20);
    	display.getVisualization().setRendererFactory(new DefaultRendererFactory(l));
    	display.getVisualization().addGraph("graph", g);
    	
    	ColorAction c = new ColorAction("graph", VisualItem.FILLCOLOR) {
    		public int getColor(VisualItem item) {
    			if ((display.getVisualization().getFocusGroup(Visualization.FOCUS_ITEMS).getTupleCount() == 0) ||
    				(item.isInGroup(Visualization.FOCUS_ITEMS))) {
    				return item.getInt("color");
    			} else {
    				return ColorLib.setAlpha(item.getInt("color"), 30);
    			}
    		}
    	};
    	c.add(new InGroupPredicate(Visualization.FOCUS_ITEMS), ColorLib.blue(100));
    	
    	ColorAction s = new ColorAction("graph", VisualItem.STROKECOLOR) {
    		public int getColor(VisualItem item) {
    			if ((display.getVisualization().getFocusGroup(Visualization.FOCUS_ITEMS).getTupleCount() == 0) ||
       				(item.isInGroup(Visualization.FOCUS_ITEMS))) {
    				return item.getInt("color");
        		} else {
        			return ColorLib.setAlpha(item.getInt("color"), 30);
        		}
    		}
    	};
    	
    	
    	ColorAction t = new ColorAction("graph", VisualItem.TEXTCOLOR) {
    		public int getColor(VisualItem item) {
    			if ((display.getVisualization().getFocusGroup(Visualization.FOCUS_ITEMS).getTupleCount() == 0) ||
        				(item.isInGroup(Visualization.FOCUS_ITEMS))) {
        			return ColorLib.gray(50, 200);
       			} else {
        			return ColorLib.gray(50, 30);
       			}
    		}
    	};
    	
    	FontAction font = new FontAction("graph", FontLib.getFont("Tahoma",12));
    	
    	ItemAction printIntAction = new ItemAction() {
    	@Override
    		public void process(VisualItem item, double frac) {
    		if (item.isHover())	
    			display.setToolTipText(ColorLib.getColor(item.getInt("color")).toString() + " " + item.getInt("color"));
    			LOGGER.info(item.getString("name") + ": " + ColorLib.getColor(item.getInt("color")).toString() + " " + item.getInt("color"));
    		}	
    	};
    	display.getVisualization().putAction("printInt", printIntAction);
    	
    	display.addControlListener(new HoverActionControl("printInt"));
    	
    	ActionList recolor = new ActionList();
    	recolor.add(c);
    	recolor.add(s);
    	recolor.add(t);
    	recolor.add(new RepaintAction());
    	display.getVisualization().putAction("recolor", recolor);
    	
    	ActionList layout = new ActionList();
    	AxisInsetsLayout x,y;
    	layout.add(x = new AxisInsetsLayout("graph", "x", Constants.X_AXIS));
    	layout.add(y = new AxisInsetsLayout("graph", "y", Constants.Y_AXIS));
    	x.setInsets(new AxisInsets(50,50,50,50));
    	y.setInsets(new AxisInsets(50,50,50,50));
    	layout.add(recolor);
    	layout.add(font);
    	layout.add(new RepaintAction());
    	
    	RepaintAction repaint = new RepaintAction();
    	display.getVisualization().putAction("repaint", repaint);
    	
    	// create palette buttons
    	JPanel buttonPanel = createPalettePanel();
    	
    	display.addControlListener(new DragControl());
    	display.getVisualization().putAction("layout", layout);
    	Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
    	jFrame.setPreferredSize(screenDim);
    	jFrame.setLayout(new BorderLayout());
    	jFrame.add(display, BorderLayout.CENTER);
    	jFrame.add(buttonPanel, BorderLayout.EAST);
    	jFrame.setTitle("Christopher Collins' Favourite Visualization Colours");
    	jFrame.pack();
    	jFrame.setVisible(true);
    	display.getVisualization().run("layout");
    	
    	display.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				display.getVisualization().run("layout");
			}
		});
    	
    	display.addMouseListener(new MouseAdapter() {
    		public void mouseClicked(MouseEvent e) {
    			blackBackground = !blackBackground;
    			display.setBackground(blackBackground ? Color.BLACK : Color.WHITE);
    			display.damageReport();
    		}    			
    	});
    	
    	display.addControlListener(new ControlAdapter() {
			public void itemWheelMoved(VisualItem item, MouseWheelEvent e) {
				if (e.getWheelRotation() < 0)
					item.setSize(item.getSize()+0.2);
				if (e.getWheelRotation() > 0)
					item.setSize(Math.max(0.2, item.getSize()-0.2));
				display.getVisualization().run("repaint");
			}
		});
    	

        display.getVisualization().getFocusGroup(Visualization.FOCUS_ITEMS).addTupleSetListener(new TupleSetListener() {
        	@Override
        	public void tupleSetChanged(TupleSet tset, Tuple[] added,
        			Tuple[] removed) {
        		display.getVisualization().run("recolor");
        	}
        });
    	
    	jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
