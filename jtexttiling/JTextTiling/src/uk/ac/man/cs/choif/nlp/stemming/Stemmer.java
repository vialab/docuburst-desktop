package uk.ac.man.cs.choif.nlp.stemming;

/**
 * <b>Purpose/Function</b>
 * <li>Defines an interface for stemming algorithms
 * 
 */
public interface Stemmer {
/**
 * <li>Get the stem of a word.
 * @return java.lang.String  Stem
 * @param word java.lang.String  Word
 */
String stemOf (String word);
}