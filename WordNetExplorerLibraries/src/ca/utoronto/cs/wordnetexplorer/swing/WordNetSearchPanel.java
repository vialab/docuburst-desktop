package ca.utoronto.cs.wordnetexplorer.swing;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.EtchedBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import ca.utoronto.cs.prefuseextensions.swing.Utilities;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.IndexWordSet;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Word;
import net.didion.jwnl.dictionary.Dictionary;

/** 
 * 
 * Provides a search panel for specifying a WordNet synset by lemma, part of speech
 * and sense index.  Provides output of sense information into a pane specified by calling
 * class (sense pane not provided by default so that user can place sense information in 
 * pane of their choice).
 * 
 * @author Christopher Collins
 * @version $Id: WordNetSearchPanel.java,v 1.3 2008/06/23 03:39:02 cmcollin Exp $
 *
 */

public class WordNetSearchPanel extends JPanel implements FocusListener {
    
    private static final long serialVersionUID = 7407688748622624547L;
    private static final String ALL_STRING = "All";
 
    private Dictionary dictionary;
    public JTextField wordTextField;
    protected JComboBox senseComboBox;
    protected JComboBox posComboBox;
    protected JButton searchButton;
    private JTextPane sensePane;
    
    //TODO take WordNetPanel style constants to their own mutable class rather than static, 
    //initialize with these defaults (empty WordNetStyleConstants constructor).
    
    public WordNetSearchPanel(Dictionary dictionary, LayoutManager layout, boolean isDoubleBuffered) {
        super(layout, isDoubleBuffered);
        this.dictionary = dictionary;
        setup();
    }

    public WordNetSearchPanel(Dictionary dictionary, JTextPane sensePane, LayoutManager layout, boolean isDoubleBuffered) {
        super(layout, isDoubleBuffered);
        this.dictionary = dictionary;
        this.sensePane = sensePane;
        setup();
    }
    
    public WordNetSearchPanel(Dictionary dictionary, JTextPane sensePane, LayoutManager layout) {
        super(layout);
        this.dictionary = dictionary;
        this.sensePane = sensePane;
        setup();
    }
    
    public WordNetSearchPanel(Dictionary dictionary, LayoutManager layout) {
        super(layout);
        this.dictionary = dictionary;
        setup();
    }


    public WordNetSearchPanel(Dictionary dictionary, JTextPane sensePane, boolean isDoubleBuffered) {
        super(isDoubleBuffered);
        this.dictionary = dictionary;
        this.sensePane = sensePane;
        setup();
    }
    
    public WordNetSearchPanel(Dictionary dictionary, boolean isDoubleBuffered) {
        super(isDoubleBuffered);
        this.dictionary = dictionary;
        setup();
    }

    public WordNetSearchPanel(Dictionary dictionary, JTextPane sensePane) {
        super();
        this.dictionary = dictionary;
        this.sensePane = sensePane;
        setup();
    }

    public WordNetSearchPanel(Dictionary dictionary) {
        super();
        this.dictionary = dictionary;
        setup();
    }
   
    protected void setEndSelection(JTextPane editor){
        editor.setSelectionStart(editor.getDocument().getLength());
        editor.setSelectionEnd(editor.getDocument().getLength());
    }
    
    private void setup() {
        ActionListener posListener = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                try{ 
                    String searchLemma = wordTextField.getText().replace(' ', '_');
                    // treat no selection as "all"
                    if (posComboBox.getSelectedItem() != null)
                        if (!((String)posComboBox.getSelectedItem()).equals(ALL_STRING)) {
                            POS pos = POS.getPOSForLabel(truncatePOS((String) posComboBox.getSelectedItem()));
                            IndexWord indexWord = dictionary.lookupIndexWord(pos, searchLemma);
                            updateSenseComboBox(indexWord);
                        }
                } catch (JWNLException ex) {
                    ex.printStackTrace();
                    System.exit(-1);
                }}
        };
        
        CaretListener wordCaretListener = new CaretListener() {
            @SuppressWarnings("unchecked")
            public void caretUpdate(CaretEvent e) {
                if (sensePane != null) sensePane.setText("");
                wordTextField.setForeground(Color.BLACK);
                try {
                    String searchLemma = wordTextField.getText().replace(' ', '_');
                    IndexWordSet indexWordSet = dictionary.lookupAllIndexWords(searchLemma);
                    if (indexWordSet.size() == 0){ 
                        senseComboBox.setEnabled(false);
                        posComboBox.setEnabled(false);
                        searchButton.setEnabled(false);                        
                    } else {
                        updatePOSComboBox(indexWordSet);
                    }
                } catch (JWNLException ex) {
                    ex.printStackTrace();
                    System.exit(-1);
                }
            }
        };
        
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        
        wordTextField = new JTextField("", 20);
        wordTextField.addCaretListener(wordCaretListener);
        wordTextField.addFocusListener(this);
        wordTextField.setFocusAccelerator('W');
        
        posComboBox = new JComboBox(new String[] {POS.ADJECTIVE.getLabel(), POS.ADVERB.getLabel(), POS.NOUN.getLabel(), POS.VERB.getLabel()});
        posComboBox.setSelectedIndex(0);
        posComboBox.addActionListener(posListener);
        
        senseComboBox = new JComboBox(new String[] {ALL_STRING});
        senseComboBox.setToolTipText("Find a sense number by first searching for a word.");
        senseComboBox.setSelectedIndex(0);
        
        setBorder(new EtchedBorder());
        
        Utilities.setGBC(c, 0, 0, 0.1, 0, 1, 1, c.NONE);
        c.insets = new Insets(0,10,0,10);
        add(new JLabel("<html><u>W</u>ord:</html>"), c);
        Utilities.setGBC(c, 1, 0, 1, 0, 1, 1, c.HORIZONTAL);
        add(wordTextField, c);
        
        Utilities.setGBC(c, 2, 0, 0.1, 0, 1, 1, c.NONE);
        add(posComboBox, c);
        Utilities.setGBC(c, 3, 0, 0, 0, 1, 1, c.NONE);
        JPanel sense = new JPanel();
        sense.add(new JLabel("Sense:"));
        sense.add(senseComboBox);
        add(sense, c);
        Utilities.setGBC(c, 4, 0, 0, 0, 1, 1, c.NONE);
        searchButton = new JButton("Search");
        add(searchButton, c);
        searchButton.setEnabled(false);
        //TODO P3 fix display for case when window width < minimum panel width
    }
    
    public boolean hasWord() {
        return searchButton.isEnabled();
    }
    
    private void updatePOSComboBox(Set<POS> posSet, String searchLemma) {
        posComboBox.removeAllItems();
        Vector<String> sortedPOS = new Vector<String>();
        for (POS pos : posSet) {
            String label = pos.getLabel();
            // put noun first
            if (label.equals(POS.NOUN.getLabel()))
                sortedPOS.add(0, label);
            else
                sortedPOS.add(label);
        }
        for (String label : sortedPOS) 
            posComboBox.addItem(label);
        posComboBox.setSelectedIndex(0);
        posComboBox.setEnabled(true);
        POS pos = POS.getPOSForLabel((String) posComboBox.getSelectedItem());
        try {
            updateSenseComboBox(dictionary.lookupIndexWord(pos, searchLemma));
        } catch (JWNLException e) {
            e.printStackTrace();
        }
    }
    
    private void updatePOSComboBox(IndexWordSet indexWordSet) {
        Set<POS> posSet = indexWordSet.getValidPOSSet();
        posComboBox.removeAllItems();
        Vector<String> sortedPOS = new Vector<String>();
        for (POS pos : posSet) {
            String label = pos.getLabel();
            // put noun first
            if (label.equals(POS.NOUN.getLabel()))
                sortedPOS.add(0, label + ": " + indexWordSet.getIndexWord(pos).getLemma());
            else
                sortedPOS.add(label + ": " + indexWordSet.getIndexWord(pos).getLemma());
        }
        for (String label : sortedPOS) 
            posComboBox.addItem(label);
        posComboBox.setSelectedIndex(0);
        posComboBox.setEnabled(true);
        POS pos = POS.getPOSForLabel(truncatePOS((String)posComboBox.getSelectedItem()));
        try { 
            updateSenseComboBox(dictionary.lookupIndexWord(pos, indexWordSet.getIndexWord(pos).getLemma()));
        } catch (JWNLException e) {
            e.printStackTrace();
        }
    }
    
    private void updateSenseComboBox(IndexWord indexWord) {
        senseComboBox.removeAllItems();
        if (indexWord == null) {
            senseComboBox.setEnabled(false);
            searchButton.setEnabled(false);
        } else {
            if (indexWord.getSenseCount() > 1) senseComboBox.addItem(ALL_STRING);
            for (int i = 0; i < indexWord.getSenseCount(); i++) {
                // senses indexed from 1
                senseComboBox.addItem(Integer.toString(i+1));
            }
            senseComboBox.setSelectedIndex(0);
            senseComboBox.setEnabled(true);
            searchButton.setEnabled(true);            
        }
    }
    
    public void requestFocus() {
        wordTextField.requestFocusInWindow();
    }
    
    public void setSearchAction(ActionListener actionListener) {
        searchButton.addActionListener(actionListener);
        wordTextField.addActionListener(actionListener);
    }
    
    /**
     * Get the part of speech selected by the user.  If "All" is selected, returns null.
     * 
     * @return the part of speech selected by the user, or null if no selection or "All" is selected.
     */
    public POS getSelectedPOS() {
        return POS.getPOSForLabel(truncatePOS((String) posComboBox.getSelectedItem()));
    }
    
    public String getLemma() {
        return wordTextField.getText();
    }
    
    public int getSelectedSense() {
        if (((String)senseComboBox.getSelectedItem()).equals(ALL_STRING))
            return 0;
        return Integer.parseInt((String)senseComboBox.getSelectedItem());
    }
    
    public Word getWord() {
        return getWord(getLemma(), getSelectedPOS(), getSelectedSense());
    }
    
    public IndexWord getIndexWord() {
        IndexWord indexWord = null;
        try {
            String searchLemma = getLemma().replace(' ','_');
            indexWord = dictionary.lookupIndexWord(getSelectedPOS(), searchLemma);
        } catch (JWNLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return indexWord;
    }
    
    public Word getWord(String lemma, POS pos, int senseNumber) {
        try {
            String searchLemma = lemma.replace(' ','_');
            IndexWord indexWord = dictionary.lookupIndexWord(pos, searchLemma);
            // senses displayed are indexed from 1 but JWNL indexes senses from 0
            // stem searchLemma with morphological processor
            if (!searchLemma.equals(indexWord.getLemma().replace(' ','_'))) {
                searchLemma = indexWord.getLemma().replace(' ','_');
                int posTemp = posComboBox.getSelectedIndex();
                int senseTemp = senseComboBox.getSelectedIndex();
                wordTextField.setText(searchLemma);
                wordTextField.setForeground(Color.RED);
                posComboBox.setSelectedIndex(posTemp);
                senseComboBox.setSelectedIndex(senseTemp);
            }
            Synset synset = indexWord.getSense(senseNumber);
            for (int i = 0; i < synset.getWordsSize(); i++) {
                if (synset.getWord(i).getLemma().toLowerCase().equals(searchLemma.toLowerCase())) {
                    return synset.getWord(i);
                }
            }
        } catch (JWNLException ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
        return null;
    }
    
    public static String truncatePOS(String startPOSLabel) {
        return startPOSLabel.substring(0, startPOSLabel.indexOf(':'));
    }
    
    public void displayIndexWord(SensePane sensePane) {
        if (getSelectedPOS().getLabel().equals(ALL_STRING)) {
            sensePane.displayIndexWordSet(getLemma());
        } else {
            sensePane.displayIndexWord(getLemma(), getSelectedPOS());
        }
    }
    
    public static String removeExamplesFromGloss(String sense) {
        String sense_pre="";
        
        if(sense != null && sense.length()>0){
            if(sense.indexOf('"')>0){
                sense_pre = (sense.substring(0,sense.indexOf('"'))).trim();
            }else{
                sense_pre = sense.trim();
            }
            if (sense_pre.lastIndexOf(';') == sense_pre.length()-1) {
                sense_pre = sense_pre.substring(0, sense_pre.lastIndexOf(';'));
            }                
        }
        // REMOVED by CMC March 24 -- no need to remove after ;
        //if(sense_pre != null && sense_pre.length()>0){
        //    if(sense_pre.indexOf(';')>0)
        //        sense_pre = sense_pre.substring(0,sense_pre.indexOf(';'));
        //}
        return sense_pre;
    }
    
    public static String wrap(String startString, int width) {
        String wrapped = new String ();
        String unwrapped = new String(startString);
        int i = unwrapped.indexOf(' ', width);
        while (i != -1) {
                wrapped = wrapped.concat(unwrapped.substring(0,i));
                wrapped = wrapped.concat("\n");
                unwrapped = unwrapped.substring(i+1);
                i = unwrapped.indexOf(' ', width);
        }
        wrapped = wrapped.concat(unwrapped);
        return wrapped;
    }
 
    public boolean isSearchEnabled() {
    	return searchButton.isEnabled();
    }

    public void focusGained(FocusEvent e) {
        if(e.getSource() instanceof JTextField)
            ((JTextField)e.getSource()).selectAll();
    }

    public void focusLost(FocusEvent e) {
        if(e.getSource() instanceof JTextField)
            ((JTextField)e.getSource()).select(0,0);
    }
}
