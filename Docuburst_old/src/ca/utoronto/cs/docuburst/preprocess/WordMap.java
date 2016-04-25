package ca.utoronto.cs.docuburst.preprocess;

import static ca.utoronto.cs.wordnetexplorer.utilities.Constants.dictionary;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import prefuse.util.io.IOLib;
import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.dictionary.Dictionary;
import ca.utoronto.cs.wordnetexplorer.utilities.LanguageLib;
import ca.utoronto.cs.wordnetexplorer.utilities.LanguageLib.CountMethod;
import edu.stanford.nlp.ling.TaggedWord;

public class WordMap {

	private static final Logger logger
	= Logger.getLogger(LanguageLib.class.getName());
	
	@SuppressWarnings("unchecked")
	public static void fillWordCountsMap(HashMap<String, float[]> wordMap,
			List<List<TaggedWord>> tiledTaggedFile, CountMethod countMethod) {

		int tileNumber = -1;

		for (List<TaggedWord> tile : tiledTaggedFile) {
			tileNumber++;
			for (TaggedWord taggedWord : tile) {
				
				try {
					String posLabel = taggedWord.tag();
					String word     = taggedWord.word();

					if (word.length() == 0 || posLabel.length() == 0)
						continue;
					
					POS pos;
					
					if (posLabel.startsWith("N")){
						pos = POS.NOUN;
						posLabel = POS.NOUN.getLabel();
					}
					else if (posLabel.startsWith("V")){
						pos = POS.VERB;
						posLabel = POS.VERB.getLabel();
					}
					else
						continue;

					// note that not all base forms reported by the
					// MorphologicalProcessor are necessarily valid
					// (i.e. they may not have any IndexWord for the
					// POS. e.g. "is", NOUN)
					List<String> baseForms = dictionary.getMorphologicalProcessor()
							.lookupAllBaseForms(pos, word);
					Iterator<String> formIterator = baseForms.iterator();
					
					logger.info("word: " + word);
					
					String newWord, key;

					while (formIterator.hasNext()) {
						newWord = formIterator.next();
						key = new String(newWord.concat(posLabel));

						// logger.info("form: "+ newWord + " ");
						// for each base for and POS, count numSenses
						Synset[] senses = null;
						try {
							 senses = dictionary.getIndexWord(pos, newWord).getSenses();
						} catch (NullPointerException e) {
							// most errors generated here are problems
							// with the tagging or morphological
							// processing (WordNet exception list)
							// logger.warning("Base form \'" + newWord +
							// "\' has no index words for part of speech "
							// + pos.getLabel() + ".");
							continue;
						}
						logger.info(" numSenses " + senses.length);

						count(key, senses, countMethod, tileNumber, wordMap, tiledTaggedFile.size());
					}
				} catch (JWNLException e) {	e.printStackTrace(); }
			}
		}
	}

	private static void incCount(HashMap<String, float[]> wordMap, String key,
			int tileNumber, float value, int totalTiles) {
		if (!wordMap.containsKey(key)) {
			wordMap.put(key, new float[totalTiles]);
		}
		float[] wordCounts = (float[]) wordMap.get(key);
		wordCounts[tileNumber] += value;
	}

	private static void count(String key, Synset[] senses,
			CountMethod countMethod, int tileNumber, HashMap<String, float[]> wordMap, 
			int totalTiles) {
		float numSenses = senses.length;
		switch (countMethod) {
		case EVEN:
			// evenly distributed amongst all
			for (int i = 0; i < 1; i++) {
				String fullKey = key.concat("" + senses[i].getOffset());
				incCount(wordMap, fullKey, tileNumber, 1 / numSenses,
						totalTiles);
			}
			break;
		case RANK:
			// linearly distributed by rank
			int totalSumRanks = (int) (numSenses * (numSenses + 1) / 2);
			for (int i = 0; i < numSenses; i++) {
				String fullKey = key.concat("" + senses[i].getOffset());
				incCount(wordMap, fullKey, tileNumber, (numSenses - i) / totalSumRanks, 
						totalTiles);
			}
			break;
		case FIRST:
			// all given to first
		    if (key.equals("fashionnoun")){
		        String fullKey = key.concat("" + senses[2].getOffset());
		        incCount(wordMap, fullKey, tileNumber, 1, totalTiles);
		    } else if (numSenses > 0) {
				String fullKey = key.concat("" + senses[0].getOffset());
				incCount(wordMap, fullKey, tileNumber, 1, totalTiles);
			}
			break;
		case FIRSTEVEN:
			// evenly distributed then only the first is
			// given a portion; lowers affect of polysemous words
			if (numSenses > 0) {
				String fullKey = key.concat("" + senses[0].getOffset());
				incCount(wordMap, fullKey, tileNumber, 1 / numSenses, totalTiles);
			}
			break;
		}
	}

	public static void main(String[] args) throws Exception {
		// initialize JWNL using properties file; must be done before
        // use
        String propsFile = "jwnl_file_properties.xml"; // jwnl properties
        try {
            InputStream propsStream = IOLib.streamFromString(propsFile);
            JWNL.initialize(propsStream);
        } catch (JWNLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // get a JWNL pointer to a WordNet dictionary
        dictionary = Dictionary.getInstance();

		List<String> tiles = Tiling.tile("/Users/rafa/Dropbox/Dev/docuburst/Docuburst_old/texts/hellobarbie_lines_v2.txt");
		List<List<TaggedWord>> tiledTaggedFile = POSTagger.tagTiles(tiles);
		HashMap<String, float[]> map = new HashMap<String, float[]>();
		fillWordCountsMap(map, tiledTaggedFile, CountMethod.FIRST);
	}

}
