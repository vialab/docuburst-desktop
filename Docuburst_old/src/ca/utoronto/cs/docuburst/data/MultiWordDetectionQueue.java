package ca.utoronto.cs.docuburst.data;

import static ca.utoronto.cs.wordnetexplorer.utilities.Constants.dictionary;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWordSet;
import net.didion.jwnl.data.POS;

public class MultiWordDetectionQueue {

	LinkedList<DocuBurstTaggedWord> words = new LinkedList<DocuBurstTaggedWord>();
	
	String[] lemmas;
	String[] originals;

	StringBuilder lemmaBuilder = new StringBuilder();
	StringBuilder originalBuilder = new StringBuilder();

	int size = 0;
	
	public MultiWordDetectionQueue(int size) {
		lemmas = new String[size];
		originals = new String[size];
		words = new LinkedList<DocuBurstTaggedWord>();
		this.size = size;
	}
	
	public List<DocuBurstTaggedWord> clear() {
		ArrayList<DocuBurstTaggedWord> remainder = new ArrayList<DocuBurstTaggedWord>();
		while (!words.isEmpty())
			remainder.add(pop());
		words.clear();
		return remainder;
	}
	
	public DocuBurstTaggedWord addWord(DocuBurstTaggedWord word) {
		DocuBurstTaggedWord returnWord = null;
		if (words.size() == size) {
			returnWord = pop();
		}
		
		words.add(word);
		
		return returnWord;
	}
	
	public DocuBurstTaggedWord pop() {
		try {
			String original;
			String lemma;
			String foundOriginal = null;
			
			lemmaBuilder.delete(0, lemmaBuilder.length());
			originalBuilder.delete(0, originalBuilder.length());
			
			int found = 0;
			IndexWordSet set = null;
			IndexWordSet foundSet = null;
			
			for (int j = 0; j < size; j++) {
				lemmas[j] = null;
				originals[j] = null;
			}

			Iterator<DocuBurstTaggedWord> wordsIterator = words.iterator(); 
			
			int j = 0;
			while (wordsIterator.hasNext()) {
				j++;
				DocuBurstTaggedWord word = wordsIterator.next();
				originalBuilder.append(word.original);
				originalBuilder.append(' ');
				lemmaBuilder.append(word.lemma);
				lemmaBuilder.append(' ');
					
				// only test if the sequence of this length was built
				original = originalBuilder.toString().trim();
				set = dictionary.getAllIndexWords(original);	
				if ((set.size() > 0) && (set.getLemma().equalsIgnoreCase(original))) {
					found = j;
					foundSet = set;
					foundOriginal = original;
				} else {
					lemma = lemmaBuilder.toString().trim();
					set = dictionary.getAllIndexWords(lemma);
					if ((set.size() > 0) && (set.getLemma().equalsIgnoreCase(lemma))) {
						found = j;
						foundSet = set;
						foundOriginal = original;
					}
				}
			}
		
			if (found > 1) {
				// for multi-word compounds, just use the first POS found
				Iterator it = foundSet.getValidPOSSet().iterator();
				
				DocuBurstTaggedWord newWord = new DocuBurstTaggedWord(foundOriginal, foundSet.getLemma(), (POS) it.next());
				for (int remove = 0; remove < found; remove++)  
					words.remove();
				return newWord;
			}
		} catch (JWNLException e) {
			System.err.println("Unable to check for multiwords due to JWNL Exception.  Exiting.");
			e.printStackTrace();
			System.exit(-1);
		}
		return words.remove();
	}	
}
