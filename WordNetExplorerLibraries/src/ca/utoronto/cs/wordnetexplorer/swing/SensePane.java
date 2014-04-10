/* CVS $Id: SensePane.java,v 1.1 2007/11/16 06:38:42 cmcollin Exp $ */
package ca.utoronto.cs.wordnetexplorer.swing;

import java.awt.Color;

import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import ca.utoronto.cs.wordnetexplorer.utilities.Constants;

import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.IndexWordSet;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Word;

public class SensePane extends JTextPane {
    
    static SimpleAttributeSet VERB_FORM = new SimpleAttributeSet();
    static SimpleAttributeSet TITLE = new SimpleAttributeSet();
    static SimpleAttributeSet NUMBER = new SimpleAttributeSet();
    static SimpleAttributeSet SENSE = new SimpleAttributeSet();
    static SimpleAttributeSet CONTENT = new SimpleAttributeSet();
    static SimpleAttributeSet LEMMA = new SimpleAttributeSet();
    static SimpleAttributeSet ERROR = new SimpleAttributeSet();
    static SimpleAttributeSet PART_OF_SPEECH = new SimpleAttributeSet();
    
    // Best to reuse attribute sets as much as possible.
    static {
        StyleConstants.setForeground(VERB_FORM, Color.darkGray);        
        StyleConstants.setFontFamily(VERB_FORM, "SansSerif");
        StyleConstants.setFontSize(VERB_FORM, 12);
        
        StyleConstants.setForeground(TITLE, Color.magenta);
        StyleConstants.setBold(TITLE, true);
        StyleConstants.setFontFamily(TITLE, "SansSerif");
        StyleConstants.setFontSize(TITLE, 12);    
        
        StyleConstants.setForeground(NUMBER, new Color(0.5f, 0.0f, 0.5f));
        StyleConstants.setBold(NUMBER, true);
        StyleConstants.setFontFamily(NUMBER, "SansSerif");
        StyleConstants.setFontSize(NUMBER, 12);
        
        StyleConstants.setForeground(PART_OF_SPEECH, Color.GREEN);
        StyleConstants.setBold(PART_OF_SPEECH, true);
        StyleConstants.setFontFamily(PART_OF_SPEECH, "SansSerif");
        StyleConstants.setFontSize(PART_OF_SPEECH, 12);    
        
        StyleConstants.setForeground(CONTENT, Color.black);
        StyleConstants.setFontFamily(CONTENT, "SansSerif");
        StyleConstants.setFontSize(CONTENT, 12);
        
        StyleConstants.setForeground(SENSE, Color.red);         
        StyleConstants.setFontFamily(SENSE, "SansSerif");
        StyleConstants.setFontSize(SENSE, 12);
        
        StyleConstants.setForeground(LEMMA, Color.blue);
        StyleConstants.setItalic(LEMMA, true);
        StyleConstants.setFontFamily(LEMMA, "SansSerif");
        StyleConstants.setFontSize(LEMMA, 14);
        
        StyleConstants.setForeground(ERROR, Color.orange);
        StyleConstants.setItalic(ERROR, true);
        StyleConstants.setFontFamily(ERROR, "SansSerif");
        StyleConstants.setFontSize(ERROR, 14);
    }

    public SensePane() {
        super();
        setEditable(false);
    }

    public SensePane(StyledDocument doc) {
        super(doc);
        setEditable(false);
    }
    
    /**
     * Display sense Information on JTextPane.  Displays sense of entire synset.
     */
    public void displaySense(Synset synset){        
        String posString;
        POS pos = synset.getPOS();
        posString = pos.getLabel();
        String synonyms;        
        if(synset.getWordsSize()>0){
            StringBuilder words = new StringBuilder();
            for (int i = 0; i < synset.getWordsSize(); ++i) {
                if (i > 0) words.append(", ");
                words.append(synset.getWord(i).getLemma().replace('_',' '));
            }
            synonyms = words.toString();
        }else{
            synonyms="";
        }
        String sense = synset.getGloss();
        String sense_pre="";
        String sense_post="";
        if(sense != null && sense.length()>0){
            if(sense.indexOf('"')>0){
                sense_pre = sense.substring(0,sense.indexOf('"'));
                sense_post = sense.substring(sense.indexOf('"'));
            }else{
                sense_pre = sense;
            }
        }
        
        String[] verbforms = synset.getVerbFrames();
        String verbform="";
        if(verbforms.length>0){
            for(int i=0; i < verbforms.length; i++){
                if (i > 0) 
                    verbform = verbform + ("; ");
                verbform = verbform + verbforms[i];
            }
        }           
        
        this.setText("");       
        insertText(this, "POS: ", TITLE);
        insertText(this, posString + "\n", CONTENT);
        insertText(this, "Synonyms: ", TITLE);
        insertText(this, synonyms + "\n", CONTENT);
        insertText(this, "Sense: ", TITLE);
        insertText(this, sense_pre, SENSE);
        insertText(this, sense_post + "\n", CONTENT);
        if(verbforms.length>0){ //Print out VerbForm information only it exist for this synset
            insertText(this, "VerbForm: ", TITLE);
            insertText(this, verbform + "\n", VERB_FORM);
        }
        this.setCaretPosition(0);
    }
    
    protected void insertText(JTextPane editor, String text, AttributeSet set) {
        try {
            editor.getDocument().insertString(editor.getDocument().getLength(), text, set); 
        }catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Display Word information on JTextPane.  Displays a specific sense of a lemma.
     */
    public void displayWord(Word word){     
        String lemma = word.getLemma().replace('_',' ');
        String posString;
        POS pos = word.getPOS();
        posString = pos.getLabel();
        
        String synonyms;
        Synset synset = word.getSynset();       
        if(synset.getWordsSize()>0){
            StringBuilder words = new StringBuilder();
            for (int i = 0; i < synset.getWordsSize(); ++i) {
                if (i > 0) words.append(", ");
                words.append(synset.getWord(i).getLemma().replace('_',' '));
            }
            synonyms = words.toString();
        }else{
            synonyms="";
        }
        
        String sense = synset.getGloss();
        String sense_pre="";
        String sense_post="";
        if(sense != null && sense.length()>0){
            if(sense.indexOf('"')>0){
                sense_pre = sense.substring(0,sense.indexOf('"'));
                sense_post = sense.substring(sense.indexOf('"'));
            }else{
                sense_pre = sense;
            }
        }
        
        String[] verbforms = synset.getVerbFrames();
        String verbform="";
        if(verbforms.length>0){
            for(int i=0; i < verbforms.length; i++){
                if (i > 0) verbform = verbform + ("; ");
                verbform = verbform + verbforms[i];
            }
        }           
        
        this.setText("");       
        insertText(this, "Lemma: ", TITLE);
        insertText(this, lemma + "\n", LEMMA);
        insertText(this, "POS: ", TITLE);
        insertText(this, posString + "\n", CONTENT);
        insertText(this, "Synonyms: ", TITLE);
        insertText(this, synonyms + "\n", CONTENT);
        insertText(this, "Sense: ", TITLE);
        insertText(this, sense_pre, SENSE);
        insertText(this, sense_post + "\n", CONTENT);
        if(verbforms.length>0){ //Print out VerbForm information only it exist for this synset
            insertText(this, "VerbForm: ", TITLE);
            insertText(this, verbform + "\n", VERB_FORM);
        }
        this.setCaretPosition(0);
    }
    
    /**
     * Display IndexWord information (for all POS) on this JTextPane.  Displays all senses of 
     * a lemma.
     */    
    public void displayIndexWordSet(String lemma) {
        // QUES should restrict to just nouns?
        // put in WN format
        String searchLemma = lemma.replace(' ','_');
        this.setText("");
        try {
            IndexWordSet indexWordSet = Constants.dictionary.lookupAllIndexWords(searchLemma);
            if (indexWordSet == null) {
                insertText(this, lemma + " not found.", ERROR);
            }
            else {
                IndexWord [] indexWords = indexWordSet.getIndexWordArray();
                insertText(this, "Lemma: " + lemma + "\n", LEMMA); 
                // cycle through POS
                for (int i = 0; i < indexWords.length; i++) {
                    if (i > 0) insertText(this, "\n", PART_OF_SPEECH);
                    insertText(this, "POS: ", PART_OF_SPEECH);
                    insertText(this, indexWords[i].getPOS().getLabel()+ " (" + indexWords[i].getSenseCount() + ") \n", CONTENT);
                    // cycle through senses for POS
                    for (int j = 1; j <= indexWords[i].getSenseCount(); j++) {
                        // synonyms
                        Synset synset = indexWords[i].getSense(j);
                        String synonyms;
                        if(synset.getWordsSize()>0){
                            StringBuffer words = new StringBuffer();
                            for (int k = 0; k < synset.getWordsSize(); k++) {
                                if (k > 0) words.append(", ");
                                words.append(synset.getWord(k).getLemma().replace('_',' '));
                            }
                            synonyms = words.toString();
                        }else{
                            synonyms="";
                        }
                        // gloss
                        String gloss = synset.getGloss();
                        String gloss_pre="";
                        String gloss_post="";
                        if(gloss != null && gloss.length()>0){
                            if(gloss.indexOf('"')>0){
                                gloss_pre = gloss.substring(0,gloss.indexOf('"'));
                                gloss_post = gloss.substring(gloss.indexOf('"'));
                            }else{
                                gloss_pre = gloss;
                            }
                        }
                        
                        // verb forms
                        String[] verbforms = synset.getVerbFrames();
                        String verbform="";
                        if(verbforms.length>0){
                            for(int k=0; k < verbforms.length; k++){
                                if (k > 0) verbform = verbform + ("; ");
                                verbform = verbform + verbforms[k];
                            }
                        }
                        /* Old code for setting text color based on sense index
                        if (indexWords[i].getPOS() == POS.NOUN) {
                            StyleConstants.setForeground(CONTENT, rcf.getColor(j));
                        } else {
                            StyleConstants.setForeground(CONTENT, Color.black);
                        }
                        */
                        
                        insertText (this, "" + indexWords[i].getPOS().getLabel() + "."+ j + " ", NUMBER);
                        insertText(this, "Synonyms: ", TITLE);
                        insertText(this, synonyms + "\n", CONTENT);
                        
                        insertText(this, "Sense: ", TITLE);
                        insertText(this, gloss_pre, SENSE);
                        StyleConstants.setForeground(CONTENT, Color.black);
                        insertText(this, gloss_post + "\n", CONTENT);
                        if(verbforms.length>0){ //Print out VerbForm information only it exist for this synset
                            insertText(this, "VerbForm: ", TITLE);
                            insertText(this, verbform + "\n", VERB_FORM);
                        }
                    }
                }
            }
            this.setCaretPosition(0);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        } 
    }
    
    /**
     * Display IndexWord information on this JTextPane.  Displays all senses of 
     * a lemma.
     */    
    public void displayIndexWord(IndexWord indexWord) {
        // QUES should restrict to just nouns?
        // put in WN format
        this.setText("");
        try {
            insertText(this, "Lemma: " + indexWord.getLemma() + "\n", LEMMA); 
            // cycle through POS
            insertText(this, "POS: ", PART_OF_SPEECH);
            insertText(this, indexWord.getPOS().getLabel()+ " (" + indexWord.getSenseCount() + ") \n", CONTENT);
            // cycle through senses for POS
            for (int j = 1; j <= indexWord.getSenseCount(); j++) {
                // synonyms
                Synset synset = indexWord.getSense(j);
                String synonyms;
                if(synset.getWordsSize()>0){
                    StringBuffer words = new StringBuffer();
                    for (int k = 0; k < synset.getWordsSize(); k++) {
                        if (k > 0) words.append(", ");
                        words.append(synset.getWord(k).getLemma().replace('_',' '));
                    }
                    synonyms = words.toString();
                }else{
                    synonyms="";
                }
                // gloss
                String gloss = synset.getGloss();
                String gloss_pre="";
                String gloss_post="";
                if(gloss != null && gloss.length()>0){
                    if(gloss.indexOf('"')>0){
                        gloss_pre = gloss.substring(0,gloss.indexOf('"'));
                        gloss_post = gloss.substring(gloss.indexOf('"'));
                    }else{
                        gloss_pre = gloss;
                    }
                }
                    
                // verb forms
                String[] verbforms = synset.getVerbFrames();
                String verbform="";
                if(verbforms.length>0){
                    for(int k=0; k < verbforms.length; k++){
                        if (k > 0) verbform = verbform + ("; ");
                        verbform = verbform + verbforms[k];
                    }
                }
                /* Old code for setting text color based on sense index
                if (indexWords[i].getPOS() == POS.NOUN) {
                    StyleConstants.setForeground(CONTENT, rcf.getColor(j));
                } else {
                    StyleConstants.setForeground(CONTENT, Color.black);
                }
                */
                
                insertText (this, "" + indexWord.getPOS().getLabel() + "."+ j + " ", NUMBER);
                insertText(this, "Synonyms: ", TITLE);
                insertText(this, synonyms + "\n", CONTENT);
                    
                insertText(this, "Sense: ", TITLE);
                insertText(this, gloss_pre, SENSE);
                StyleConstants.setForeground(CONTENT, Color.black);
                insertText(this, gloss_post + "\n", CONTENT);
                if(verbforms.length>0){ //Print out VerbForm information only it exist for this synset
                    insertText(this, "VerbForm: ", TITLE);
                    insertText(this, verbform + "\n", VERB_FORM);
                }
            }
            this.setCaretPosition(0);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        } 
    }
    
    /**
     * Display IndexWord information (for given POS) on JTextPane.  Displays all senses of 
     * a lemma for a POS.
     */    
    public void displayIndexWord(String lemma, POS pos) {
        String searchLemma = lemma.replace(' ','_');
        this.setText("");
        try {
            IndexWord indexWord = Constants.dictionary.lookupIndexWord(pos, searchLemma);
            if (indexWord == null) {
                insertText(this, lemma + " not found.", ERROR);
            }
            else {
                insertText(this, "Lemma: " + lemma + "\n", LEMMA); 
                // cycle through senses for POS

                for (int j = 1; j <= indexWord.getSenseCount(); j++) {
                    // synonyms
                    Synset synset = indexWord.getSense(j);
                    String synonyms;
                    if(synset.getWordsSize()>0){
                        StringBuffer words = new StringBuffer();
                        for (int k = 0; k < synset.getWordsSize(); k++) {
                            if (k > 0) words.append(", ");
                            words.append(synset.getWord(k).getLemma().replace('_',' '));
                        }
                        synonyms = words.toString();
                    }else{
                        synonyms="";
                    }
                    // gloss
                    String gloss = synset.getGloss();
                    String gloss_pre="";
                    String gloss_post="";
                    if(gloss != null && gloss.length()>0){
                        if(gloss.indexOf('"')>0){
                            gloss_pre = gloss.substring(0,gloss.indexOf('"'));
                            gloss_post = gloss.substring(gloss.indexOf('"'));
                        }else{
                            gloss_pre = gloss;
                        }
                    }
                    
                    // verb forms
                    String[] verbforms = synset.getVerbFrames();
                    String verbform="";
                    if(verbforms.length>0){
                        for(int k=0; k < verbforms.length; k++){
                            if (k > 0) verbform = verbform + ("; ");
                            verbform = verbform + verbforms[k];
                        }
                    }
                    
                    /* Old code for setting font color based on sense index
                    if (pos == POS.NOUN) {
                        StyleConstants.setForeground(CONTENT, rcf.getColor(j));
                    } else {
                        StyleConstants.setForeground(CONTENT, Color.black);
                    }
                    */
                    
                    insertText (this, "" + j + " ", NUMBER);
                    insertText(this, "Synonyms: ", TITLE);
                    insertText(this, synonyms + "\n", CONTENT);
                    
                    insertText(this, "Sense: ", TITLE);
                    insertText(this, gloss_pre, SENSE);
                    StyleConstants.setForeground(CONTENT, Color.black);
                    insertText(this, gloss_post + "\n", CONTENT);
                    if(verbforms.length>0){ //Print out VerbForm information only it exist for this synset
                        insertText(this, "VerbForm: ", TITLE);
                        insertText(this, verbform + "\n", VERB_FORM);
                    }
                }
            }
            this.setCaretPosition(0);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        } 
    }
    
    public void clear() {
        this.setText("");
    }
    
}
