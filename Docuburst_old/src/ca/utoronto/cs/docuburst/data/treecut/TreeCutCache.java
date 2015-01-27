package ca.utoronto.cs.docuburst.data.treecut;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import prefuse.data.Node;

public class TreeCutCache {

	// for each weight, a cut
	private Map<Double, List<Node>> cache;
	
	public TreeCutCache() {
		cache = new HashMap<Double, List<Node>>();
	}
	
	public void add(double w, List<Node> cut){
		cache.put(w, cut);
	}
	
	public List<Node> get(double w){
		return cache.get(w);
	}
	
	public List<Double> getSortedWeights(){
	    List<Double> keys = new ArrayList<Double>(cache.keySet());
	    Collections.sort(keys);
	    return keys;
	}

}
