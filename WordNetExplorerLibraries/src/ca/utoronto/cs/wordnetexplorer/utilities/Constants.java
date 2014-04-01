package ca.utoronto.cs.wordnetexplorer.utilities;

import net.didion.jwnl.dictionary.Dictionary;

public abstract class Constants {

	// Node types
	public static final int WORD = 0;

	public static final int SENSE = 1;

	public static final int LEMMA = 4;

	// Edge types
	public static final int S2W = 2; // for sense to its multiple words

	public static final int L2S = 5; // for central word to its multiple
										// senses

	public static final int HYPONOMY = 3; // for hyponomy relationship (from
											// root/focus)

	public static final int HYPERONOMY = 6; // for hyponomy relationship (from
											// root/focus)

	// Maximum distance to highlight that a descendent is a search result
	public static final int MAX_SEARCH_DEPTH = 20;

	// draw with anti-aliasing or not
	public static final boolean HIGH_QUALITY = true;

	/**
	 * The default
	 * <code>Dictionary<\code> object which is created by JWNL for use.
	 */
	public static Dictionary dictionary;
	
	public enum SimilarityMeasure{ALPHABETIC, LESK, JCN, PATH};

}
