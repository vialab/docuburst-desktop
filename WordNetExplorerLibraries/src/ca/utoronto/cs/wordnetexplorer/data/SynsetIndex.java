package ca.utoronto.cs.wordnetexplorer.data;

import java.util.ArrayList;
import java.util.HashMap;

import net.didion.jwnl.data.POS;

public class SynsetIndex {

	private HashMap<POS, HashMap<Long, ArrayList<Integer>>> synsetIndex = 
		new HashMap<POS, HashMap<Long, ArrayList<Integer>>>(); 
	
	public void add(POS pos, long offset, int wordIndex) {
		HashMap<Long, ArrayList<Integer>> posIndex;
		if (synsetIndex.containsKey(pos)) {
			posIndex = synsetIndex.get(pos);
		} else {
			posIndex = new HashMap<Long, ArrayList<Integer>>();
		}
		ArrayList<Integer> indices;
		if (posIndex.containsKey(offset)) {
			indices = posIndex.get(offset);
		} else {
			indices = new ArrayList<Integer>(3);
		}
		indices.add(wordIndex);
		posIndex.put(offset, indices);
		synsetIndex.put(pos, posIndex);
	}

	public ArrayList<Integer> getIndices(POS pos, long offset) {
		if (synsetIndex.containsKey(pos)) {
			return synsetIndex.get(pos).get(offset);
		} else
			return null;
	}
	
	public int count(POS pos, long offset) {
		if (synsetIndex.containsKey(pos)) {
			HashMap<Long, ArrayList<Integer>> posIndex = synsetIndex.get(pos);
		 	if (posIndex.containsKey(offset)) {
				return posIndex.get(offset).size();
		 	}
		}
		return 0;
	}

	public void add(WordNetTaggedWord dt, int index) {
		POS pos = dt.getPOS();
		ArrayList<Long> offsets = dt.getSynsets();
		for (long offset : offsets)
			add(pos, offset, index);
	}
	
}
