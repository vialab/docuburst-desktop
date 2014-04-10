package ca.utoronto.cs.wordnetexplorer.prefuse.controls;

import java.awt.event.MouseEvent;

import static ca.utoronto.cs.wordnetexplorer.utilities.Constants.*;

import ca.utoronto.cs.wordnetexplorer.jwnl.LoadData;

import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Pointer;
import net.didion.jwnl.data.PointerType;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Word;
import net.didion.jwnl.dictionary.Dictionary;
import prefuse.controls.ControlAdapter;
import prefuse.visual.VisualItem;

public class LoadDataControl extends ControlAdapter {

	LoadData visualization;
	
	public LoadDataControl(LoadData visualization) {
		super();
		this.visualization = visualization;
	}
	
	public void itemClicked(VisualItem item, MouseEvent e) {
        
        // ctrl double left click -- load all senses of current word
        if ((e.getClickCount() == 2) && ((e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) == MouseEvent.CTRL_DOWN_MASK) && (e.getButton() == MouseEvent.BUTTON1)) {
            try {
                // check the type of this VisualItem, either "Word" or "Sense"
                int type = item.getInt("type");
                if(type == WORD) {
                    visualization.cancelLayouts();
                    IndexWord indexWord = dictionary.getIndexWord(POS.getPOSForLabel(item.getString("pos")), item.getString("label"));
                    visualization.reset(indexWord);
                }
                // SYNSET & LEMMA can't reload IndexWord
            } catch (JWNLException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
        
        // double left click -- load graph rooted at clicked item (word or sense)
        if ((e.getClickCount() == 2) && ((e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) != MouseEvent.CTRL_DOWN_MASK) && (e.getButton() == MouseEvent.BUTTON1)) {
            try {
                // don't reload on click of current root (won't do anything)
                if (!item.getBoolean("root")) {
                    int type = item.getInt("type");
                    //check the type of this VisualItem, either "Word" or "Sense"
                    if (type == WORD) {
                        visualization.cancelLayouts();
                        Synset synset = dictionary.getSynsetAt(POS.getPOSForLabel(item.getString("pos")), item.getLong("offset"));
                        Word word = synset.getWord(item.getInt("wordIndex"));
                        visualization.reset(word);
                    } else if (type == SENSE){
                        visualization.cancelLayouts();
                        Synset synset = dictionary.getSynsetAt(POS.getPOSForLabel(item.getString("pos")), item.getLong("offset"));
                        visualization.reset(synset);
                    }   
                    //  LEMMA can't reload
                }
            } catch (JWNLException e1) {
                //  TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
        
        // double right click to go up one step to hyperonym of clicked item and reload
        if ((e.getClickCount() == 2) && (e.getButton()==MouseEvent.BUTTON3)){
            try {
                //check the type of this VisualItem, either "Word" or "Sense"
                int type = item.getInt("type");
                if(type == WORD){
                    visualization.cancelLayouts();
                    Synset synset = dictionary.getSynsetAt(POS.getPOSForLabel(item.getString("pos")), item.getLong("offset"));
                    // look for hyperonym
                    Pointer[] pointers = synset.getPointers(PointerType.HYPERNYM);
                    visualization.reset(pointers[0].getTargetSynset());
                }else if(type == SENSE){
                    visualization.cancelLayouts();
                    Synset synset = dictionary.getSynsetAt(POS.getPOSForLabel(item.getString("pos")), item.getLong("offset"));
                    // look for hyperonym
                    Pointer[] pointers = synset.getPointers(PointerType.HYPERNYM);
                    if (pointers.length > 0)
                        visualization.reset(pointers[0].getTargetSynset());
                }                   
            } catch (JWNLException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
	}
}
