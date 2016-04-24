/* CVS $Id: LanguageLib.java,v 1.8 2008/12/10 23:20:04 cmcollin Exp $ */
package ca.utoronto.cs.wordnetexplorer.utilities;

import static ca.utoronto.cs.wordnetexplorer.utilities.Constants.dictionary;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Synset;
import prefuse.util.io.IOLib;

public abstract class LanguageLib {

	private static final Logger logger
    	= Logger.getLogger(LanguageLib.class.getName());

	public enum CountMethod {EVEN, RANK, FIRST, FIRSTEVEN};
	
	/**
	 * Read in a text file, parse and stem the words, and count their occurrences, per tile. 
	 *  
	 * @param wordMap the word occurrence map to fill; key is word, stored object is an array of counts
	 * @param wordsFilename the filename containing the stemmed words, one per line
	 * @param fullTextFileName the related full-text filename
	 * @return the full document text, as an array of tile text
	 */
    public static String [] fillWordCountsMap(HashMap<String, float[]> wordMap, String wordsFilename, String fullTextFileName, CountMethod countMethod) {
        int tileNumber = 0;
        int totalTiles = 0;
        InputStreamReader fR;
        String [] fullText = new String[0];
        
        try {
            // count tiles
            fR = new InputStreamReader(IOLib.streamFromString(wordsFilename));
            BufferedReader bR = new BufferedReader(fR);
            String word = new String();
            while (word != null) {
                word = bR.readLine();
                if (word == null) break;
                if (word.equals("==========") || word.equals("==========*")) 
                    tileNumber++;
            }
            totalTiles = tileNumber;
            logger.warning("Read " + totalTiles + " tiles from " + wordsFilename);
            tileNumber = 0;
            fR.close();
            // read statistics
            fR = new InputStreamReader(IOLib.streamFromString(wordsFilename));
            bR = new BufferedReader(fR);
            word = new String();
            // initialize loop variables
            List baseForms = null;
            POS pos;
            String posLabel = new String();
            float numSenses;
            while (word != null) {
                word = bR.readLine();
                if (word == null) break;
                if (word.equals("==========")|| word.equals("==========*")) {
                    tileNumber++;
                } else {
                    try {
                    	posLabel = (word.substring(word.indexOf(' ')+1)).trim();
                    	word = (word.substring(0, word.indexOf(' '))).trim();
                    	
                    	if ((word.length() != 0) && (posLabel.length() != 0)) {
                        	baseForms = null;
                        	if (posLabel.equals(POS.NOUN.getLabel())) {
                        		pos = POS.NOUN;
                        	} else { 
                        		if (posLabel.equals(POS.VERB.getLabel())) 
                        			pos = POS.VERB;
                        		else
                        			pos = null;
                        	}
                        	
                        	// check to ensure it was one of the allowed POS
                        	if (pos != null) {
                        		// note that not all base forms reported by the MorphologicalProcessor are necessarily valid (i.e. they may not have any IndexWord for the POS. e.g. "is", NOUN)
                        		baseForms = dictionary.getMorphologicalProcessor().lookupAllBaseForms(pos, word);
                        		Iterator formIterator = baseForms.iterator();
                        		logger.info("word: " + word);
                        		String newWord;
                        		String key;
                        		
                        		while(formIterator.hasNext()) {
                        			newWord = (String)formIterator.next();
                        			key = new String(newWord.concat(posLabel));

                        			
                        			logger.info("form: "+ newWord + " ");
                        			// for each base for and POS, count numSenses
                        			try {
                        				numSenses = dictionary.getIndexWord(pos, newWord).getSenseCount();
                        			} catch (NullPointerException e) {
                        				// most errors generated here are problems with the tagging or morphological processing (WordNet exception list)
                        				logger.warning("Base form \'" + newWord + "\' has no index words for part of speech " + pos.getLabel() + ".");
                        				continue;
                        			}
                        			logger.info(" numSenses " + numSenses);
                        			
                        			Synset[] senses = dictionary.getIndexWord(pos, newWord).getSenses();
                        			
                        			switch (countMethod) {
                        			case EVEN:
                        				// evenly distributed amongst all
                            			for (int i = 0; i < 1; i++) {
                            				String fullKey = key.concat(""+senses[i].getOffset());
                            				if (wordMap.containsKey(fullKey)) { 
                            					float[] wordCounts = (float[]) wordMap.get(fullKey);
                            					wordCounts[tileNumber] += 1/numSenses;
                            				} else {	
                            					float[] wordCounts = new float[totalTiles];
                            					wordCounts[tileNumber] += 1/numSenses;
                            					wordMap.put(fullKey, wordCounts);
                            				}
                            			}
                            			break;
                        			case RANK:
                        				// linearly distributed by rank
                        				int totalSumRanks = (int) (numSenses * (numSenses+1) / 2);
                            			for (int i = 0; i < numSenses; i++) {
                        					String fullKey = key.concat(""+senses[i].getOffset());
                        					if (wordMap.containsKey(fullKey)) { 
                        						float[] wordCounts = (float[]) wordMap.get(fullKey);
                        						wordCounts[tileNumber] += (numSenses-i)/totalSumRanks;
                        					} else {	
                        						float[] wordCounts = new float[totalTiles];
                        						wordCounts[tileNumber] += (numSenses-i)/totalSumRanks;
                        						wordMap.put(fullKey, wordCounts);
                        					}
                        				}
                        				break;
                        			case FIRST:
                        				// all given to first
                        				if (numSenses > 0) {
                        					String fullKey = key.concat(""+senses[0].getOffset());
                        					if (wordMap.containsKey(fullKey)) { 
                        						float[] wordCounts = (float[]) wordMap.get(fullKey);
                        						wordCounts[tileNumber] += 1;
                        					} else {	
                        						float[] wordCounts = new float[totalTiles];
                        						wordCounts[tileNumber] += 1;
                        						wordMap.put(fullKey, wordCounts);
                        					}
                        				}
                        				break;
                        			case FIRSTEVEN:
                        				// evenly distributed then only the first is given a portion; lowers affect of polysemous words
                        				if (numSenses > 0) {
                        					String fullKey = key.concat(""+senses[0].getOffset());
                        					if (wordMap.containsKey(fullKey)) { 
                        						float[] wordCounts = (float[]) wordMap.get(fullKey);
                        						wordCounts[tileNumber] += 1/numSenses;
                        					} else {	
                        						float[] wordCounts = new float[totalTiles];
                        						wordCounts[tileNumber] += 1/numSenses;
                        						wordMap.put(fullKey, wordCounts);
                        					}
                        				}
                        				break;
                        			}
                        		}
                        	}
                        }
                    } catch (JWNLException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            fR.close();
            fR = new InputStreamReader(IOLib.streamFromString(fullTextFileName));
            bR = new BufferedReader(fR);
            fullText = new String[tileNumber];
            String line = new String();
            StringBuffer tile = new StringBuffer();
            tileNumber = 0;
            while (line != null) {
                line = bR.readLine();
                if (line == null) break;
                if (line.equals("==========")|| line.equals("==========*")) {
                    fullText[tileNumber] = tile.toString();
                    //System.out.println("TILE: " + tileNumber);
                    //System.out.println(fullText[tileNumber]);
                    //System.out.println("******************");
                    tileNumber++;
                    tile = new StringBuffer();
                } else {
                    tile.append(line);
                }
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return fullText;
    }

    /**
     * Clean a tagged, tiled file to create the format for StarBurst input.
     * 
     * @param inFilename
     * @param outFilename
     * @throws IOException
     */
    public static void extract(String inFilename, String outFilename) throws IOException {
        File in = new File(inFilename);
        File out = new File(outFilename);
        try {
            FileReader fR = new FileReader(in);
            FileWriter fW = new FileWriter(out);
            BufferedReader bR = new BufferedReader(fR);
            BufferedWriter bW = new BufferedWriter(fW);
            String line = new String();
            String word = new String();
            while (line != null) {
                line = bR.readLine();
                if (line == null) break;
                StringTokenizer sT = new StringTokenizer(line);
                while (sT.hasMoreTokens()) {
                    String token = sT.nextToken();
                    String pos = token.substring(token.indexOf('/')+1);
                    
                    if (pos.equals("NN") || pos.equals("NNS")) {
                        word = token.substring(0, token.indexOf('/'));
                        if (word.indexOf('\'') > 0)
                            word = word.substring(0, word.indexOf('\''));
                        if (word.indexOf('.') >0)
                            word = word.substring(0, word.indexOf('.'));
                        if (word.indexOf('\"') > 0)
                            word = word.substring(0, word.indexOf('\"'));
                        if (word.indexOf('?') > 0)
                            word = word.substring(0, word.indexOf('?'));
                        if (word.indexOf('!') > 0)
                            word = word.substring(0, word.indexOf('!'));
                        if (word.indexOf(')') > 0)
                            word = word.substring(0, word.indexOf(')'));
                        if (word.indexOf(':') > 0)
                            word = word.substring(0, word.indexOf(':'));
                        if (word.indexOf(';') > 0)
                            word = word.substring(0, word.indexOf(';'));
                        if (word.indexOf(',') > 0)
                            word = word.substring(0, word.indexOf(','));
                        if (word.indexOf('[') > 0)
                            word = word.substring(word.indexOf('[') +1);
                        if (word.indexOf('(') > 0)
                            word = word.substring(word.indexOf('[') +1);
                        if (word.equals("==========") || word.equals("==========*") )
                            bW.write(word.toLowerCase().trim());
                        else
                            bW.write(word.toLowerCase().trim() + " " + POS.NOUN.getLabel());
                        
                        bW.newLine();
                    }
                    if (pos.equals("NNP")) {
                        word = token.substring(0, token.indexOf('/'));
                        if (word.indexOf('\'') > 0)
                            word = word.substring(0, word.indexOf('\''));
                        if (word.indexOf('.') >0)
                            word = word.substring(0, word.indexOf('.'));
                        if (word.indexOf('\"') > 0)
                            word = word.substring(0, word.indexOf('\"'));
                        if (word.indexOf('?') > 0)
                            word = word.substring(0, word.indexOf('?'));
                        if (word.indexOf('!') > 0)
                            word = word.substring(0, word.indexOf('!'));
                        if (word.indexOf(')') > 0)
                            word = word.substring(0, word.indexOf(')'));
                        if (word.indexOf(':') > 0)
                            word = word.substring(0, word.indexOf(':'));
                        if (word.indexOf(';') > 0)
                            word = word.substring(0, word.indexOf(';'));
                        if (word.indexOf(',') > 0)
                            word = word.substring(0, word.indexOf(','));
                        if (word.indexOf('[') > 0)
                            word = word.substring(word.indexOf('[') +1);
                        if (word.indexOf('(') > 0)
                            word = word.substring(word.indexOf('[') +1);
                        if (word.equals("==========") || word.equals("==========*") )
                            bW.write(word.toLowerCase().trim());
                        else
                            bW.write(word.toLowerCase().trim() + " " + "noun");
                        
                        bW.newLine();
                    }
                    if (pos.equals("VBD") || pos.equals("VBG") || pos.equals("VBN") ||
                        pos.equals("VBP") || pos.equals("VBZ")) {
                            
                        word = token.substring(0, token.indexOf('/'));
                        if (word.indexOf('\'') > 0)
                            word = word.substring(0, word.indexOf('\''));
                        if (word.indexOf('.') > 0)
                            word = word.substring(0, word.indexOf('.'));
                        if (word.indexOf(':') > 0)
                            word = word.substring(0, word.indexOf(':'));
                        if (word.indexOf(';') > 0)
                            word = word.substring(0, word.indexOf(';'));
                        if (word.indexOf(',') > 0)
                            word = word.substring(0, word.indexOf(','));
                        if (word.indexOf('[') > 0)
                            word = word.substring(word.indexOf('[') +1);
                        bW.write(word.toLowerCase().trim() + " " + POS.VERB.getLabel());
                        bW.newLine();
                    }
                }
            }
            bW.close();
            bR.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    class TaggedWord {
    	public String word;
    	public String tag;
    }
    
    public static String [] fillWordCountsMap(HashMap<String, float[]> wordMap, List<String> tiledFile, CountMethod countMethod) {
    	
    	return null;
    }
    
    /**
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        extract("c:/workspace/docuburst_old/texts/practice_and_science_of_drawing.tiled.tagged.txt", "c:/workspace/docuburst_old/texts/practice_and_science_of_drawing.tiled.tagged.cleaned.txt");
    }
}
