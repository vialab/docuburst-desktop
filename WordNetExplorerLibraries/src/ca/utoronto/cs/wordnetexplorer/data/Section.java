package ca.utoronto.cs.wordnetexplorer.data;

import java.util.ArrayList;

public class Section {

	private int index;
	
	private ArrayList<Word> words = new ArrayList<Word>();
	
	public Section(int index) {
		this.index = index;
	}
	
	public void addWord(Word word) {
		words.add(word);
	}
	
	public int length() {
		return words.size();
	}
	
	public String toXML() {
		StringBuilder sb = new StringBuilder();
		if (words.size() > 0) {
			sb.append("<section>");
			for (Word w: words) {
				sb.append(w.toXML());
			}
			sb.append("</section>\n");
		}
		return sb.toString();
	}

	public Word getWord(int i) {
		return words.get(i);
	}
}
