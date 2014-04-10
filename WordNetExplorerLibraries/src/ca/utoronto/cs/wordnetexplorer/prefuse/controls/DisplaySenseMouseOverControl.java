/* CVS $Id: DisplaySenseMouseOverControl.java,v 1.1 2008/02/21 07:45:50 cmcollin Exp $ */
package ca.utoronto.cs.wordnetexplorer.prefuse.controls;

import static ca.utoronto.cs.wordnetexplorer.utilities.Constants.*;

import java.awt.Cursor;
import java.awt.event.MouseEvent;

import prefuse.Display;
import prefuse.controls.ControlAdapter;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Word;

import ca.utoronto.cs.wordnetexplorer.swing.SensePane;

/**
 * 
 * Place sense information for a given visual item into a SensePane.
 * Locks position of node under mouse cursor and changes cursor to hand, indicating
 * item can be clicked to reload visualization.
 * 
 * @author Christopher Collins
 * @version Feb 25, 2006 1:56:51 PM
 */
public class DisplaySenseMouseOverControl extends ControlAdapter {
    
    private SensePane sensePane;
    
    public DisplaySenseMouseOverControl(SensePane sensePanel) {
        super();
        this.sensePane = sensePanel;
    }
    
    /**
     * Tags and fixes the node under the mouse pointer.
     */
    public void itemEntered(VisualItem item, MouseEvent e) {            
        ((Display)e.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        item.setFixed(true);
        int type = item.getInt("type");
        Synset synset;
        try {
            if (type == LEMMA)
                sensePane.displayIndexWord(dictionary.lookupIndexWord(POS.getPOSForLabel(item.getString("pos")), item.getString("label").replace(' ','_')));
            if ((type == SENSE) || (type == WORD)) {
                synset = dictionary.getSynsetAt(POS.getPOSForLabel(item.getString("pos")), item.getLong("offset"));
                if (type == WORD) {
                    Word word = synset.getWord(item.getInt("wordIndex"));
                    sensePane.displayWord(word);
                    if (item.canGetInt("polysemy")) System.out.println(item.getString("label") + "  " + item.getInt("polysemy"));
                } else {
                    sensePane.displaySense(synset);
                }
            }
        } catch (JWNLException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        
        // Show Relationship between two Sense nodes
        /*
         * if(item instanceof EdgeItem){ String nodeRelation =
         * item.getAttribute("linktype");//Get origin "linktype" information
         * from VisualItem String type = item.getAttribute("type"); //eithter
         * "SynsetToSynset" or "WordToSynset" String pos =
         * item.getAttribute("pos"); // noun,verb,adverb, adjective
         * if(type.equalsIgnoreCase("SynsetToSynset")){ // Get Two Node
         * connected by this Edge NodeItem sourceNode =
         * (NodeItem)((EdgeItem)item).getFirstNode(); Synset sourceSynset =
         * (Synset) getSynset(sourceNode); NodeItem targetNode =
         * (NodeItem)((EdgeItem)item).getSecondNode(); Synset targetSynset =
         * (Synset) getSynset(targetNode); // get POS type, use "+" indicate
         * Parent Node and "-" and Child Node
         * if(pos.equals(POS.NOUN.getLabel())){
         * if(nodeRelation.equalsIgnoreCase("hypernym")){ nodeRelation="+ is a
         * kind of -"; } else if(nodeRelation.equalsIgnoreCase("hyponym")){
         * nodeRelation="- is a kind of +"; } else
         * if(nodeRelation.equalsIgnoreCase("holonym")){ nodeRelation="is a part
         * of"; } else if(nodeRelation.equalsIgnoreCase("meronym")){
         * nodeRelation="is a part of"; } else
         * if(nodeRelation.equalsIgnoreCase("part holonym")){ nodeRelation="+ is
         * a part of -"; } else if(nodeRelation.equalsIgnoreCase("part
         * meronym")){ nodeRelation="- is a part of +"; } else
         * if(nodeRelation.equalsIgnoreCase("member meronym")){ nodeRelation="-
         * is a member of +"; } else if(nodeRelation.equalsIgnoreCase("member
         * holonym")){ nodeRelation="+ is a member of -"; } else
         * if(nodeRelation.equalsIgnoreCase("attribute")){ nodeRelation="- is
         * attribute of +"; } else if(nodeRelation.equalsIgnoreCase("substance
         * meronym")){ nodeRelation="+ is made of -"; } else
         * if(nodeRelation.equalsIgnoreCase("substance holonym")){
         * nodeRelation="- is made of +"; }
         * 
         * }else if(pos.equals(POS.VERB.getLabel())){
         * if(nodeRelation.equalsIgnoreCase("hypernym")){ nodeRelation="+ is one
         * way to -"; } else if(nodeRelation.equalsIgnoreCase("troponym")){
         * nodeRelation="is particular way to"; } else
         * if(nodeRelation.equalsIgnoreCase("hyponym")){ nodeRelation="- is
         * particular way to +"; } else
         * if(nodeRelation.equalsIgnoreCase("entailment")){ nodeRelation="+
         * can't be done unless - is done"; } else
         * if(nodeRelation.equalsIgnoreCase("cause")){ nodeRelation="+ causes
         * -"; } }else if(pos.equals(POS.ADJECTIVE.getLabel())){
         * if(nodeRelation.equalsIgnoreCase("attribute")){ nodeRelation="+ is
         * attribute of -"; } else if(nodeRelation.equalsIgnoreCase("similar")){
         * nodeRelation="+ is similar to -"; } }else
         * if(pos.equals(POS.ADVERB.getLabel())){
         * if(nodeRelation.equalsIgnoreCase("derived")){ nodeRelation="+ is
         * derived from -"; } }
         * m_wnforceFrame.displayRelation(sourceSynset,targetSynset,nodeRelation);
         * }//==>End if(type.equalsIgnoreCase("SynsetToSynset")) ..
         * ((Display)e.getSource()).setToolTipText(nodeRelation); }
         */       
    } //
    
    public void itemExited(VisualItem item, MouseEvent e) {
        ((Display)e.getSource()).setCursor(Cursor.getDefaultCursor());
        item.setFixed(false);
    } //
    
    public void itemReleased(VisualItem item, MouseEvent e) {
        item.setFixed(false);
    }                 
}
