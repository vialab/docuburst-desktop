package ca.utoronto.cs.docuburst.data;

import ca.utoronto.cs.wordnetexplorer.data.Word;
import net.didion.jwnl.data.POS;

public class DocuBurstTaggedWord extends Word {

	String lemma, original;
	POS pos;
	int synsetNumber = -1;
	
	public DocuBurstTaggedWord(String original, String lemma, POS pos) {
		this.original = original;
		this.lemma = lemma;
		this.pos = pos;
	}
	
	public DocuBurstTaggedWord(String lemma) {
		this.original = lemma;
		this.lemma = lemma;
		this.pos = null;
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
		if (synsetNumber != -1) {
			sb.append("\" sense=\"");
			sb.append(synsetNumber);
		}
		sb.append("\"/>");
		return sb.toString();
	}

	@Override
	public String getWord() {
		return getOriginal();
	}
	
}
