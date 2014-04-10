package ca.utoronto.cs.wordnetexplorer.data;

import java.util.ArrayList;

import net.didion.jwnl.data.POS;

public class WordNetTaggedWord extends Word {

	String lemma, original;
	POS pos;
	ArrayList<Long> synsetNumbers; 
	
	public WordNetTaggedWord(String original, String lemma, POS pos) {
		this.original = original;
		this.lemma = lemma;
		this.pos = pos;
		synsetNumbers = new ArrayList<Long>(3);
	}
	
	public WordNetTaggedWord(String lemma) {
		this(lemma, lemma, null);
	}
	
	public boolean isTagged() {
		return (pos != null);
	}
	
	public boolean isStemmed() {
		return original.equalsIgnoreCase(lemma);
	}
	
	public POS getPOS() {
		return pos;
	}
	
	public String getLemma() {
		return lemma;
	}
	
	public String getOriginal() {
		return original;
	}
	
	public String toXML() {
		StringBuilder sb = new StringBuilder();
		sb.append("<word original=\"");
		sb.append(original);
		if (!lemma.equals(original)) {
			sb.append("\" lemma=\"");
			sb.append(lemma);
		}
		if (pos != null) {
			sb.append("\" pos=\"");
			sb.append(pos);
		} 
		if (synsetNumbers.size() > 0) {
			sb.append("\" sense=\"[");
			for (long offset : synsetNumbers) {
				sb.append(offset);
				sb.append(',');
			}
			sb.replace(sb.lastIndexOf(","), sb.length(), "]\"/>");
		}
		return sb.toString();
	}

	@Override
	public String getWord() {
		return getOriginal();
	}
	
	public void addSynset(long offset) {
		synsetNumbers.add(offset);
	}
	
	public void setSynsets(long[] offsets) {
		for (long offset: offsets)
			addSynset(offset);
	}
	
	public ArrayList<Long> getSynsets() {
		return synsetNumbers;
	}
	
}
