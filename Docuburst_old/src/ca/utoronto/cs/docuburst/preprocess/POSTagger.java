package ca.utoronto.cs.docuburst.preprocess;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class POSTagger {

	public static List<List<TaggedWord>> tagTiles(List<String> tiles) throws Exception{
        MaxentTagger tagger = new MaxentTagger("taggers/english-left3words-distsim.tagger");
        
        List<List<CoreLabel>> tokenizedTiles = new ArrayList<List<CoreLabel>>();
        for (String tile : tiles) {
    	    PTBTokenizer<CoreLabel> tokenizer = new PTBTokenizer<CoreLabel>(
    	    		new StringReader(tile), new CoreLabelTokenFactory(), "");
    	    List<CoreLabel> tokens = tokenizer.tokenize();
    	    tokenizedTiles.add(tokens);
		}
	    
	    return tagger.process(tokenizedTiles);
	}
	
	public static void main(String[] args) throws Exception {
		tagTiles(Tiling.tile("/Users/rafa/Dropbox/Dev/docuburst/Docuburst_old/texts/hellobarbie_lines_v2.txt"));

	}

}
