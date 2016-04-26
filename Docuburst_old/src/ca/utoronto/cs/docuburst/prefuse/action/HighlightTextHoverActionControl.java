package ca.utoronto.cs.docuburst.prefuse.action;

import static ca.utoronto.cs.wordnetexplorer.utilities.Constants.LEMMA;
import static ca.utoronto.cs.wordnetexplorer.utilities.Constants.SENSE;
import static ca.utoronto.cs.wordnetexplorer.utilities.Constants.WORD;
import static ca.utoronto.cs.wordnetexplorer.utilities.Constants.dictionary;

import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JTextPane;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Word;
import prefuse.Visualization;
import prefuse.controls.ControlAdapter;
import prefuse.data.Node;
import prefuse.data.Tuple;
import prefuse.data.expression.ColumnExpression;
import prefuse.data.expression.ComparisonPredicate;
import prefuse.data.expression.NumericLiteral;
import prefuse.data.tuple.TupleSet;
import prefuse.util.ColorLib;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;
import ca.utoronto.cs.docuburst.Param;
import ca.utoronto.cs.docuburst.prefuse.ColorScheme;
import ca.utoronto.cs.docuburst.prefuse.DocuBurstActionList;
import ca.utoronto.cs.docuburst.swing.ConcordancePanel;
import ca.utoronto.cs.wordnetexplorer.prefuse.FisheyeDocument;

public class HighlightTextHoverActionControl extends ControlAdapter {
	Pattern p;
	List<String> fullText;
	
	int deepest = 0;
	int count = 0;
	
	/**
	 * The field to take the number of occurrences from -- if this field is 
	 * zero for an item, then no concordances are sought.
	 */
	String countField;
	
	static int hoverColorForText = ColorScheme.highlightColor;
	static SimpleAttributeSet HIGHLIGHT_WORD = new SimpleAttributeSet();
	static SimpleAttributeSet NORMAL = new SimpleAttributeSet();
	static SimpleAttributeSet MONOSPACED = new SimpleAttributeSet();

	static {
		StyleConstants.setBackground(HIGHLIGHT_WORD, ColorLib
				.getColor(hoverColorForText));
		StyleConstants.setFontFamily(HIGHLIGHT_WORD, Param.interfaceFont);
		StyleConstants.setFontSize(NORMAL, 16);
		StyleConstants.setFontFamily(NORMAL, Param.interfaceFont);
		StyleConstants.setFontSize(HIGHLIGHT_WORD, 16);
		StyleConstants.setFontFamily(MONOSPACED, "Courier");
		StyleConstants.setFontSize(MONOSPACED, 16);
		StyleConstants.setAlignment(MONOSPACED, StyleConstants.ALIGN_CENTER);
	}
	
	private JTextPane fullTextPane;
	private JTextPane concordancePane;
	private Visualization docuburstVisualization;
	private Visualization documentVisualization;
	
	public HighlightTextHoverActionControl(JTextPane textPane, JTextPane concordancePane,
			List<String> fullText, Visualization docuburstVisualization, Visualization documentVisualization) {
		super();
		this.fullTextPane = textPane;
		this.concordancePane = concordancePane;
		this.fullText = fullText;
		this.docuburstVisualization = docuburstVisualization;
		this.documentVisualization = documentVisualization;
	}

	public HighlightTextHoverActionControl(JTextPane textPane,
			List<String> fullText, Visualization docuburstVisualization, 
			Visualization documentVisualization) {
		super();
		this.fullTextPane = textPane;
		this.concordancePane = null;
		this.fullText = fullText;
		this.docuburstVisualization = docuburstVisualization;
		this.documentVisualization = documentVisualization;
	}
	
	public HighlightTextHoverActionControl(List<String> fullText) {
		super();
		this.fullTextPane = null;
		this.concordancePane = null;
		this.fullText = fullText;
	}
	
	public void setTextPane(JTextPane textPane) {
		this.fullTextPane = textPane;
	}
	
	public void setFisheyeDocument(FisheyeDocument fisheyeDocument) {
	}
	
	public void setConcordanceTextPane(JTextPane textPane) {
		this.concordancePane = textPane;
        ((StyledDocument) concordancePane.getDocument()).setParagraphAttributes(0, 0, MONOSPACED, true);

	}
	
	public void setCountField(String countField) {
		this.countField = countField;
	}
	
	public void fillTextArea(int tile) {
		fullTextPane.setText(fullText.get(tile - 1));
		fullTextPane.setCaretPosition(0);
		updateTextArea(false);
	}
	
	public void updateTextArea(boolean updateConcordances) {
		((StyledDocument) fullTextPane.getDocument())
			.setCharacterAttributes(0, fullTextPane.getText().length(), NORMAL, true);
	
		TupleSet focusSynsets = docuburstVisualization.getFocusGroup(DocuBurstActionList.HIGHLIGHT);
		Iterator iter = focusSynsets.tuples();
		StringBuffer concordances = new StringBuffer();
		
		TupleSet allDocuments = documentVisualization.getGroup("items");
		TupleSet focusDocuments = documentVisualization.getFocusGroup(Visualization.FOCUS_ITEMS);
		if ((concordancePane != null) && updateConcordances) { // && concordancePane.isShowing()) {
			Iterator ts = focusDocuments.tuples();
			while (ts.hasNext()) {
				((Tuple)ts.next()).setInt(FisheyeDocument.VALUE, 0);
			}
			focusDocuments.clear();
		}
		// highlight instances in the text tile
		String itemsRegEx;
		StringBuffer regExBuffer = new StringBuffer("(");
		// build itemsRegEx from all selected word nodes
		boolean found = false;
		while (iter.hasNext()) {
			Tuple t = (Tuple) iter.next();
			if (t.getInt("type") == WORD) {
				if (found) 
					regExBuffer.append("|");
				else { 
					found = true;
//					regExBuffer.append("(");
				}
				regExBuffer.append(getItemRegEx(t));
//				regExBuffer.append("");
			}
		}
		if (!found) {
			// none found
			if (concordancePane != null)
				concordancePane.setText("");
			documentVisualization.run("distort");
			return;
		}
			
		// highlight matches and concordances
		regExBuffer.append(")");
		itemsRegEx = regExBuffer.toString();
		p = Pattern.compile(itemsRegEx, Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(fullTextPane.getText());
		if (m.find()) {
			// move text tile caret to the beginning of the first match
			fullTextPane.setCaretPosition(m.start());
			m.reset();
			// Replace all occurrences of pattern in input; retain case
			// Get the match result
			while (m.find()) {
				// Get the label match result only
				((StyledDocument) fullTextPane.getDocument())
					.setCharacterAttributes(m.start(), m.group().length(), HIGHLIGHT_WORD, true);
			}
		}
			
		if ((concordancePane != null) && updateConcordances) { 
			// 	get matches in all texttiles, word + up to 5 words on each side
			p = Pattern.compile("((\\S+\\s){0,5})(" + itemsRegEx + ")((\\s*\\S+\\s){0,5})", Pattern.CASE_INSENSITIVE); 
			for (int i = 0; i < fullText.size(); i++) { 
				if (fullText.get(i) != null) { 
					m = p.matcher(fullText.get(i)); 
					while (m.find()) {
						concordances.append(String.format(ConcordancePanel.CONCORDANCE_FORMAT, i+1, 
						        m.group(1).replaceAll("[\\n\\r]", " "), 
						        m.group(3).replaceAll("[\\n\\r]", " "), 
						        m.group(m.groupCount()-1).replaceAll("[\\n\\r]", " ")));
						Iterator documentIterator = allDocuments.tuples(new ComparisonPredicate(ComparisonPredicate.EQ, new NumericLiteral(i+1), new ColumnExpression("row")));
						while (documentIterator.hasNext()) {
							Tuple document = (Tuple) documentIterator.next();
							document.setInt(FisheyeDocument.VALUE, document.getInt(FisheyeDocument.VALUE)+1);
							focusDocuments.addTuple(document);
						}
					} 
				}
			}
		}
		
		// update display of focus documents
		documentVisualization.run("distort");
		if ((concordancePane != null) && updateConcordances) {  
			concordancePane.setText(concordances.toString());

			((StyledDocument) concordancePane.getDocument()).setParagraphAttributes(0, 0, MONOSPACED, true);
	 			
			concordancePane.setCaretPosition(0);
		}
	};
	
	public String getItemRegEx(VisualItem item) {
		return getItemRegEx((Node)item);
	}
	
	// matches the word OR its plural
	public String getWordRegex(String word){
		return String.format("\\b%1$s\\b|\\b%1$ss\\b", word);
	}

	public String getItemRegEx(Tuple item) {
		int type = item.getInt("type");
		StringBuffer regEx;
		Synset synset;
		try {
			if ((type == LEMMA)
					|| (type == WORD)) {
				regEx = new StringBuffer("(")
					.append(getWordRegex(item.getString("label")))
					.append(")");			
				return regEx.toString();
			}
			if (type == SENSE) {
				synset = dictionary.getSynsetAt(POS
						.getPOSForLabel(item.getString("pos")), item
						.getLong("offset"));
				Word[] words = synset.getWords();
				if (words.length == 1) {
					return "("+ getWordRegex(words[0].getLemma().replaceAll("_", " ")) + ")";
				}
				regEx = new StringBuffer("(");
				for (int i = 0; i < words.length - 1; i++) {
					regEx.append(getWordRegex(words[i].getLemma().replaceAll("_", " ")))
						 .append("|");
				}
				return regEx.append(getWordRegex(words[words.length - 1].
										getLemma().replaceAll("_", " ")))
						    .append(")\\b")
						    .toString();
			}
		} catch (JWNLException e1) {
			e1.printStackTrace();
		}
		return item.getString("label");
	}
	
	/**
	 * @see prefuse.controls.Control#itemEntered(prefuse.visual.VisualItem,
	 *      java.awt.event.MouseEvent)
	 */
	public void itemEntered(VisualItem item, MouseEvent e) {
	}

	public void itemClicked(VisualItem item, MouseEvent e) {
		if ((e.getClickCount() == 1) && (e.getButton() == MouseEvent.BUTTON1)) {
			if ((e.getModifiersEx() & MouseEvent.ALT_DOWN_MASK) != MouseEvent.ALT_DOWN_MASK) {
				if (item instanceof NodeItem) {
					Node n = (Node) item;
					if (n.getDouble(countField) != 0) {
						TupleSet focus = docuburstVisualization.getFocusGroup(DocuBurstActionList.HIGHLIGHT);
						// remove
						if (focus.containsTuple(n)) {
							focus.removeTuple(n);
							// if sense remove word children
							if (n.getInt("type") == SENSE) {  
								Iterator childrenIterator = n.children();
								while (childrenIterator.hasNext()) {
									Node child = (Node) childrenIterator.next();
									if (child.getInt("type") == WORD)
										if (child.getDouble(countField) != 0) 
											focus.removeTuple(child);
								}
							}
							// if deselected is word, deselect parent (senses should only be on if all children are on
							if (n.getInt("type") == WORD) {  
								Node parent = n.getParent();
								focus.removeTuple(parent);
		
								/* This alternate code checks if word was last selected child, and 
								  removes parent if it was
								 
								Iterator childrenIterator = parent.children();
								boolean selected = false;
								while (childrenIterator.hasNext()) {
									Node child = (Node) childrenIterator.next();
									if (child.getInt("type") == WordNetExplorer.WORD)
										if (focus.containsTuple(child))
											selected = true;
								}
								if (!selected)
									focus.removeTuple(parent);
								*/
							}
							updateTextArea(true);
							return;
						} 
						// add
						focus.addTuple(n);
						// if sense add word children
						if (n.getInt("type") == SENSE) {
							Iterator childrenIterator = ((Node)item).children();
							while (childrenIterator.hasNext()) {
								Node child = (Node) childrenIterator.next();
								if (child.getInt("type") == WORD)
									if (child.getDouble(countField) != 0)
										focus.addTuple(child);
							}
						}
						updateTextArea(true);
					}
				}
			} else {
				// write out the tree to a file
				if (item instanceof NodeItem) {
					Node n = (Node) item;
					// open a file for tree output
					try {
						File f = new File(n.getString("label") + ".csv");
						BufferedWriter w = new BufferedWriter(new FileWriter(f));
					
						// walk through tree and output <word label = "word" count = "number">
						// which type of writer outputs full lines at a time?
					
						deepest = 1;
						count = 0;
						//printXML(n, w, 1);
						printCounts(n,w,1);
						System.err.println("deepest level: " + deepest);
						System.err.println("node count: " + count);
								
						w.flush();
						w.close();
					} catch (IOException x) {
						System.err.println("Unable to output xml for " + n.getString("label"));
					}
				}
			}
		}
	}
	
	private void printXML(Node n, BufferedWriter w, int depth) throws IOException{
		if (depth > deepest) deepest = depth;
		count++;
		if (n.getInt("type") == SENSE) {  
			// don't do subtree count, only do node count
			// QUES what is the point of CACHECOUNT? ANS: CACHECOUNT values are the sum of all active tiles; regular count values return an array of tile counts
			boolean senseChild = false;
			w.write("<synset label=\"" + n.getString("label") + "\" count=\"" + n.getFloat(DocuBurstActionList.CACHECOUNT + DocuBurstActionList.NODECOUNT) + "\"");
			Iterator childrenIterator = n.children();
			while (childrenIterator.hasNext()) {
			Node child = (Node) childrenIterator.next(); 
				if (child.getInt("type") == SENSE) {
					if (!senseChild) {
						w.write(">\n");
						senseChild = true;
					}
					for (int i = 0 ; i < depth*4; i++) 
						w.write(" ");
					printXML(child, w, depth+1);
				}
			}
			if (!senseChild)
				w.write("/>\n");
			else { 
				for (int i = 0 ; i < (depth-1)*4; i++) 
					w.write(" ");
				w.write("</synset>\n");
			}
		}
	}

	private void printCounts(Node n, BufferedWriter w, int depth) throws IOException {
		if (depth > deepest) deepest = depth;
		count++;
		if ((n.getInt("type") == SENSE) || (n.getInt("type") == WORD)) {  
			// don't do subtree count, only do node count
			boolean senseChild = false;
			if (n.getInt("type") == SENSE)
				w.write("sense, ");
			else
				w.write("word, ");
			w.write(n.getString("label") + ", " + n.getFloat(DocuBurstActionList.CACHECOUNT + DocuBurstActionList.NODECOUNT) + ", " + n.getFloat(DocuBurstActionList.CACHECOUNT + DocuBurstActionList.CHILDCOUNT) + "\n");
			Iterator childrenIterator = n.children();
			while (childrenIterator.hasNext()) {
				Node child = (Node) childrenIterator.next(); 
				printCounts(child, w, depth+1);
			}
		}
	}

	public int getTotalTiles() {
		return fullText.size();
	}

	
	/**
	 * @see prefuse.controls.Control#itemExited(prefuse.visual.VisualItem,
	 *      java.awt.event.MouseEvent)
	 */
	public void itemExited(VisualItem item, MouseEvent e) {
		/*if (item instanceof NodeItem) {
			// different search regex tags needed because HTMLDocument
			// inserts extra <font> tags
			p = Pattern.compile(getItemRegEx(item),
					Pattern.CASE_INSENSITIVE);
			Matcher m = p.matcher(fullTextPane.getText());
			if (m.find()) {
				m.reset();

				// Replace all occurrences of pattern in input, retain case
				while (m.find()) {
					// Get the label match result only
					((StyledDocument) fullTextPane.getDocument())
						.setCharacterAttributes(m.start(), m.group().length(), NORMAL, true);
				}
			}
			//if (concordancePane != null)
			//	concordancePane.setText("");
		}*/
	}
}