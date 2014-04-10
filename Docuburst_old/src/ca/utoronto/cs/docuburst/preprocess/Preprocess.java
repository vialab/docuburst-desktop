package ca.utoronto.cs.docuburst.preprocess;

import static ca.utoronto.cs.wordnetexplorer.utilities.Constants.dictionary;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JDialog;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.SwingWorker.StateValue;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.dictionary.Dictionary;
import prefuse.util.io.IOLib;
import ca.utoronto.cs.docuburst.data.DocuBurstTaggedWord;
import ca.utoronto.cs.docuburst.data.MultiWordDetectionQueue;

import ca.utoronto.cs.wordnetexplorer.data.Document;
import ca.utoronto.cs.wordnetexplorer.data.Section;
import ca.utoronto.cs.wordnetexplorer.utilities.MathUtilities;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class Preprocess {

	private static final int MAX_LENGTH = 3;
	private final MultiWordDetectionQueue wordQueue = new MultiWordDetectionQueue(MAX_LENGTH);

	public Preprocess() throws InterruptedException, ExecutionException {
		initializeJWNL();
		try {
			MaxentTagger tagger2 = new MaxentTagger("lib/left3words-wsj-0-18.tagger");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Initialize the JWNL off the event-dispatching thread, to provide for continued construction of the GUI.
	 * When everything else is done, GUI will call "get()" which blocks until dictionary is ready.
	 * 
	 * @return a SwingWorker which will initialize the JWNL with a file-backed dictionary 
	 */
	private void initializeJWNL() {
		// initialize JWNL using properties file; must be done before use
		String propsFile = "/home/rafa/Dev/workspace/docuburst/jwnl-sf/src/file_properties.xml"; // jwnl properties
		try {
			InputStream propsStream = IOLib.streamFromString(propsFile);
			JWNL.initialize(propsStream);
		} catch (JWNLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		// get a JWNL pointer to a WordNet dictionary
		dictionary = Dictionary.getInstance();
		if (dictionary == null) {
			System.err.println("Dictionary instance is null: confirm dictionary is located as specified in "
							+ propsFile + ".");
			System.exit(-1);
		}
	}

	/**
	 * Process a given input plain-text string and output a tagged set of 
	 * tokens and stems.
	 *  
	 * @param s the plain text string to process
	 * @return an array of tagged words along with their stems
	 * @throws JWNLException 
	 */
	public Section processText(String s) throws JWNLException {
		POS pos;
		Section section = new Section(0);
		
		List<Sentence<? extends HasWord>> sentences = MaxentTagger.tokenizeText(new StringReader(s));
		
		for (Sentence<? extends HasWord> sentence : sentences) {
			Sentence<TaggedWord> tSentence = MaxentTagger.tagSentence(sentence);
			for (TaggedWord word : tSentence) {
			pos = null;
				String tag;
				// 	skip non-word characters 
				if (word.word().matches("\\W*(^\\W+)\\W*"))
					continue;
				
				// deal with the case when multiple words tagged together
				if (word.tag().length() != 0) {
					tag = word.tag();
					if (tag.charAt(0) == 'V') {
						pos = POS.VERB;
					}
					if (tag.charAt(0) == 'N') {
						pos = POS.NOUN;
					} 
					if ("JJ".equals(tag)) {
						pos = POS.ADJECTIVE;
					}
					if ("ADV".equals(tag)) {
						pos = POS.ADVERB;
					}
					
					// if we have a POS of interest, look it up
					if (pos != null) {
						// test lemmatization
						IndexWord indexWord = dictionary.lookupIndexWord(pos, word.word());
						if (indexWord != null) {
							DocuBurstTaggedWord dt = wordQueue.addWord(new DocuBurstTaggedWord(word.word(), indexWord.getLemma(), pos));
							// if a word was popped when we added the most recent, record it
							if (dt != null) {
								section.addWord(dt);
							}
							continue;
						}
					}
					// 	not found in dictionary, so we can't find a lemma
					DocuBurstTaggedWord dt = wordQueue.addWord(new DocuBurstTaggedWord(word.word(), word.word(), pos));
					if (dt != null) { 
						section.addWord(dt);
					}
				}
			}
			// add remaining words
			List<DocuBurstTaggedWord> dtList = wordQueue.clear();
			for (DocuBurstTaggedWord w : dtList) {
				section.addWord(w);
			}
		}
		return section;
	}

	public boolean preprocess(String text, String title, File outFile) {
		return false;
	}
	
	public SwingWorker<Document, Void> preprocess(final BufferedReader br, final String title, final long size, final BufferedWriter bw) {
		
		final SwingWorker<Document, Void> worker = new SwingWorker<Document, Void>() {
			@Override
			protected Document doInBackground() throws Exception {
				
				long read = 0;
				Document doc = new Document(title);
		
				// process by paragraphs
				StringBuilder paragraph = new StringBuilder();
				String line = br.readLine();
				while ((line != null) && (!isCancelled())) {
					while ((line != null) && (line.trim().length() > 0) && (!isCancelled())) {
						read += line.getBytes().length;
						setProgress((int) MathUtilities.clamp(0, (100 * ((float)read/(float)size)), 100));
						paragraph.append(line);
						line = br.readLine();
					}
					doc.addSection(processText(paragraph.toString()));
					paragraph.delete(0, paragraph.length());
					line = br.readLine();
				}
				br.close();
		
				bw.write(doc.toXML());
				bw.close();
				
				return doc;
			}
		};
		return worker;
	}
	
	public static void main(String [] args) throws JWNLException, ExecutionException, InterruptedException, IOException {
	}
}
