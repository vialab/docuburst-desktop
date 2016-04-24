package uk.ac.man.cs.choif.nlp.stopword;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.Hashtable;
/**
 * Implements a stopword list
 * Creation date: (07/12/99 23:45:20)
 * @author: Freddy Choi
 */
public class Stopword {
	protected Hashtable wordSet = new Hashtable(); // Set of stopwords
/**
 * Stopword constructor comment.
 */
public Stopword() {
	super();
}
/**
 * Load a collection from input stream
 * Creation date: (07/12/99 00:37:40)
 * @param file java.lang.String
 */
public Stopword(InputStream in) {
	try {
		/* Open the file and attach stream tokenizer */
		Reader r = new BufferedReader(new InputStreamReader(in));
		parse(r);
		r.close();
	}
	catch (Exception e) {}
}
/**
 * Load a collection from disk
 * Creation date: (07/12/99 00:37:40)
 * @param file java.lang.String
 */
public Stopword(String file) {
	try {
		/* Open the file and attach stream tokenizer */
		Reader r = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		parse(r);
		r.close();
	}
	catch (Exception e) {}
}
/**
 * Test if word is a stopword.
 * Creation date: (07/12/99 23:46:08)
 * @return boolean
 * @param word java.lang.String
 */
public boolean isStopword(final String word) {
	return (wordSet.get(word) != null);
}
/**
 * Parse a stopword list file. Expecting one stopword per line format.
 * Creation date: (07/12/99 23:49:38)
 * @param r java.io.Reader
 */
protected void parse(Reader r) {
	
	/* 1. Setup syntax table for the tokeniser */
	StreamTokenizer tk = new StreamTokenizer(r);
	tk.resetSyntax();
	tk.wordChars('\u0021', '\u00FF');
	tk.whitespaceChars('\u0000', '\u0020');
	tk.lowerCaseMode(false);

	/* 2. Parse stream */
	try {
		while (tk.nextToken() != tk.TT_EOF) wordSet.put(tk.sval, new Boolean(true));
	}
	catch (IOException e) {}

}
}
