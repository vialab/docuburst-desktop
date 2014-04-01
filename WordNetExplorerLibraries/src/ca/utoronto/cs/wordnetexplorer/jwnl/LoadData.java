package ca.utoronto.cs.wordnetexplorer.jwnl;

import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Word;

public interface LoadData {

	public void cancelLayouts();
	public void reset(Synset synset);
	public void reset(Word word);
	public void reset(IndexWord indexWord);
	
}
