/* CVS $Id: WordNetTree.java,v 1.2 2008/12/05 06:12:41 cmcollin Exp $ */
package ca.utoronto.cs.docuburst.data;

import static ca.utoronto.cs.wordnetexplorer.utilities.Constants.HYPERONOMY;
import static ca.utoronto.cs.wordnetexplorer.utilities.Constants.HYPONOMY;
import static ca.utoronto.cs.wordnetexplorer.utilities.Constants.L2S;
import static ca.utoronto.cs.wordnetexplorer.utilities.Constants.LEMMA;
import static ca.utoronto.cs.wordnetexplorer.utilities.Constants.S2W;
import static ca.utoronto.cs.wordnetexplorer.utilities.Constants.SENSE;
import static ca.utoronto.cs.wordnetexplorer.utilities.Constants.WORD;
import static ca.utoronto.cs.wordnetexplorer.utilities.Constants.dictionary;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.IndexWordSet;
import net.didion.jwnl.data.Pointer;
import net.didion.jwnl.data.PointerType;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Word;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Tuple;
import prefuse.util.collections.IntIterator;
import ca.utoronto.cs.wordnetexplorer.swing.WordNetSearchPanel;

public abstract class WordNetTree {

	// result of calling fillGraph with the synset entity as argument
	private static Graph fullSynsetGraphCache = null;
	// attribute of the cached graph above
	private static HashSet<PointerType> pointerTypeLabelsCache = null;
	private static Boolean countPolysemyCache = null;
	private static Boolean mergeWordsCache = null;

	private static String NODE_KEY_COLUMN = "key";
	
	/**
	 * Creates a new graph based on a cache.
	 * @param synset
	 * @return
	 */
	private static Graph getCachedFilledGraph(Synset synset){
		if (synset.getOffset() == 1740) // entity
			return fullSynsetGraphCache;
		Graph newGraph = new Graph(true);
//		setupGraph(newGraph, countPolysemyCache, mergeWordsCache);
		
		Graph cacheGraph = fullSynsetGraphCache;
				
		// find the tuple corresponding to this synset		
		Tuple synsetCachedTuple = null;
		for (Iterator<Tuple> it = cacheGraph.getNodes().tuples(); it.hasNext();) {
			Tuple tuple = it.next();
			if (tuple.getLong("offset") == synset.getOffset()){
				synsetCachedTuple = tuple;
				break;
			}
		}
		
		// add to new graph all columns found in the cache
		// this will include counts added to the graph in other parts of the code
		newGraph.addColumns(synsetCachedTuple.getSchema());
		
		// copy that tuple into a node of the new graph and make it root
		Node newRootNode = newGraph.addNode();
		setupCloneNode(newRootNode, synsetCachedTuple);
		newRootNode.setBoolean("root", true);
		
		// find the descendants of this node and copy them from the cache (Tuple instances)
		// into the new graph (Node instances)
		Queue<Tuple> cachedTupleQ = new LinkedList<Tuple>();
		Queue<Node>  nodeQ  = new LinkedList<Node>();		
		cachedTupleQ.add(synsetCachedTuple);
		nodeQ.add(newRootNode);
		
		while (!cachedTupleQ.isEmpty()){
			Tuple sourceTuple = cachedTupleQ.poll();
			Node  sourceNode  = nodeQ.poll();
			
			IntIterator outEdges = cacheGraph.edgeRows(sourceTuple.getRow(), Graph.OUTEDGES);
			while (outEdges.hasNext()) {
				Tuple targetTuple = cacheGraph.getNode(cacheGraph.getTargetNode(outEdges.nextInt()));
				Node  targetNode  = newGraph.addNode();
				setupCloneNode(targetNode, targetTuple);
				newGraph.addEdge(sourceNode, targetNode);
				cachedTupleQ.add(targetTuple);
				nodeQ.add(targetNode);
			}
		}
				
		return newGraph;
	}
	
	private static boolean isCachedGraphUseful(HashSet<PointerType> pointerTypeLabels, 
			boolean countPolysemy, boolean mergeWords){
		return countPolysemy == countPolysemyCache && mergeWords == mergeWordsCache
				&& pointerTypeLabels.equals(pointerTypeLabelsCache);
	}
	
    /**
     * Fill a tree graph starting at the provided synset and recursively following all 
     * pointers of the given type.  Only add edges once (don't loop in cycles) and
     * stops at leaves.
     * 
     * @param synset the starting point for the search
     * @param pointerTypeLabels the types of relationship to search for
     * @param countPolysemy determines whether to collect data on the number of meanings of each word
     * @param mergeWords whether to create only one instance of a given lemma and connect all relations to it (true creates a graph, false creates a tree)
     * @return a graph filled with data from WordNet given the provided specifications
     * @throws JWNLException 
     */
    public static Graph fillGraph(Synset synset, HashSet<PointerType> pointerTypeLabels, boolean countPolysemy, boolean mergeWords) throws JWNLException {
    	if (fullSynsetGraphCache != null 
    			&& isCachedGraphUseful(pointerTypeLabels, countPolysemy, mergeWords))
    		return getCachedFilledGraph(synset); 
    	
    	Graph g = new Graph(true);
        setupGraph(g, countPolysemy, false);
        
        // temporary data structures to track nodes and edges already created
        HashMap<Object, Node> nodeHashMap = new HashMap<Object, Node>();
        HashSet<String> edgeSet = new HashSet<String>();
        
        // start filling graph at root
        Node root = g.addNode();
        setupSynsetNode(root, null, synset);
        nodeHashMap.put(synset.getKey(), root);
        
        // ==>
        // Get "Word" of this Synset
        int synwordSize = 0;
        int synsetPolysemy = 0;
        synwordSize = synset.getWordsSize();
        if (synwordSize > 0) {
            Word[] synwords = synset.getWords();
            // Show each Word
            for (int i = 0; i < synwords.length; i++) {
                // Create Node for this word
                Node synwordNode = g.addNode();
                setupWordNode(synwordNode, root, synwords[i]);
                nodeHashMap.put(synwords[i].getLemma(), synwordNode);
                
                // Link This wordNode to rootNode
                Edge synwordEdge = g.addEdge(root, synwordNode);
                synwordEdge.set("label", root.getString("label") + " " + synwordNode.getString("label"));
                synwordEdge.set("type", S2W);
                edgeSet.add(makeKey(root, synwordNode, S2W));
                if (countPolysemy) synsetPolysemy += synwordNode.getInt("polysemy");
            }
        }
        if (countPolysemy) root.setInt("polysemy", synsetPolysemy);

        //  get the various types of relationships and traverse them
        for (PointerType pointerTypeLabel : pointerTypeLabels) {
            findRelations(synset, root, g, nodeHashMap, edgeSet, pointerTypeLabel, countPolysemy, mergeWords);
            edgeSet.clear(); // edges can't be the same over different type labels, so clear to save memory
        }
        
        // save in cache
        fullSynsetGraphCache   = g;
        countPolysemyCache     = countPolysemy;
        mergeWordsCache        = mergeWords;
        pointerTypeLabelsCache = pointerTypeLabels;
        
        return g;
    }

    /**
     * Fill a tree graph starting at the provided indexWord (all senses, one POS) and 
     * recursively following all pointers of the given type.  
     * 
     * Only add edges once (don't loop in cycles) and stops at leaves.
     * @param indexWord 
     * 
     * @param synset the starting point for the search
     * @param pointerTypeLabels the types of relationship to search for
     * @param countPolysemy determines whether to collect data on the number of meanings of each word
     * @param mergeWords whether to create only one instance of a given lemma and connect all relations to it (true creates a graph, false creates a tree)
     * @return a graph filled with data from WordNet given the provided specifications
     * 
     * @throws JWNLException if a lookup operation fails 
     */
    public static Graph fillGraph(IndexWord indexWord, HashSet<PointerType> pointerTypeLabels, boolean countPolysemy, boolean mergeWords) throws JWNLException {
        Graph g = new Graph(true);
        setupGraph(g, countPolysemy, true);
        
        // temporary data structures to track nodes and edges already created
        HashMap<Object, Node> nodeHashMap = new HashMap<Object, Node>();
        HashSet<String> edgeSet = new HashSet<String>();
        
        // start filling graph at root
        Node root = g.addNode();
        
        root.setInt("type", LEMMA);
        root.setString("label", indexWord.getLemma()); 
        root.setBoolean("root", true);
        root.setString("pos", indexWord.getPOS().getLabel());
        root.setInt("senseIndex", 0); // seseIndex = 0 means don't add to aggregate
        nodeHashMap.put(indexWord.getLemma(), root);
        
        // Get Synsets
        for (int s = 1; s <= indexWord.getSenseCount(); s++) {
            Synset synset = indexWord.getSense(s);
            Node synsetRoot = g.addNode();
            setupSynsetNode(synsetRoot, root, synset, s);
            
            nodeHashMap.put(synset.getKey(), synsetRoot);
            Edge synsetEdge = g.addEdge(root, synsetRoot);
            synsetEdge.set("type", L2S);
            synsetEdge.set("label", root.getString("label") + " " + synsetRoot.getString("label"));
            edgeSet.add(makeKey(root, synsetRoot, L2S));
            
            // Get "Word" of this Synset
            int synwordSize = 0;
            int synsetPolysemy = 0;
            synwordSize = synset.getWordsSize();
            if (synwordSize > 0) {
                Word[] synwords = synset.getWords();
                //  Show each Word
                for (int i = 0; i < synwords.length; i++) {
                    // skip word that is the root
                    if (!synwords[i].getLemma().equalsIgnoreCase(indexWord.getLemma())) {
                        //  Create Node for this word
                        Node synwordNode = g.addNode();
                        setupWordNode(synwordNode, synsetRoot, synwords[i]);
                        if (countPolysemy) synsetPolysemy += synwordNode.getInt("polysemy");
                        nodeHashMap.put(synwords[i].getLemma(), synwordNode);
                    
                        //  Link This wordNode to rootNode
                        Edge synwordEdge = g.addEdge(synsetRoot, synwordNode);
                        synwordEdge.set("label", synsetRoot.getString("label") + " " + synwordNode.getString("label"));
                        //  Set edge attribute to indicate the relationship between these two nodes
                        synwordEdge.set("type", S2W);
                        edgeSet.add(makeKey(synsetRoot, synwordNode, S2W));
                    }
                }
            }
            if (countPolysemy) synsetRoot.setInt("polysemy", synsetPolysemy);
            
            //  get the various types of relationships and traverse them
            for (PointerType pointerTypeLabel : pointerTypeLabels) {
                findRelations(synset, synsetRoot, g, nodeHashMap, edgeSet, pointerTypeLabel, countPolysemy, mergeWords);
                edgeSet.clear();  // edges can't be the same over different type labels, so clear to save memory
            }
        }
        return g;
    }

    
    /**
     * Fill Tree using the text from user input (all POS & senses), non-recursive
     * 
     * @param indexWord 
     * 
     * @param lemma the starting point for the search
     * @param countPolysemy determines whether to collect data on the number of meanings of each word
     * @return a graph filled with data from WordNet given the provided specifications
     * 
     * @throws JWNLException if a lookup operation fails 
     */
    public static Graph fillGraph(String lemma, boolean countPolysemy) throws JWNLException {
        
        // temporary data structures to track nodes and edges already created
        HashMap<Object, Node> nodeHashMap = new HashMap<Object, Node>();

        Graph g = new Graph(true);
        
        Node root = g.addNode();
        root.setInt("type", LEMMA);
        root.setString("label", lemma);
        root.setBoolean("root", true);
        root.setInt("senseIndex", 0); // seseIndex = 0 means don't add to aggregate
        root.setString("pos", "all");
        nodeHashMap.put(lemma, root);
        
        // ** For WN Search **/
        IndexWordSet idxWSet = dictionary.lookupAllIndexWords(lemma);
        IndexWord[] idxWord = idxWSet.getIndexWordArray();
        int rootPolysemy = 0;

        // Get IndexWords related to this Lemma 
        for (int i = 0; i < idxWord.length; i++) {
            // Get each synset related to this IndexWord 
            int senseCount = 0;
            senseCount = idxWord[i].getSenseCount();
            rootPolysemy += senseCount;
            if (senseCount > 0) {
                Synset[] synset = idxWord[i].getSenses();
                for (int j = 0; j < synset.length; j++) {
                    // Create WNDefaultNode object for this sense
                    Node senseNode = g.addNode();
                    setupSynsetNode(senseNode, root, synset[j], j);
                    
                    // Link this senseNode to root
                    Edge senseEdge = g.addEdge(root, senseNode);
                    senseEdge.set("label", root.getString("label") + " " + senseNode.getString("label"));
                    senseEdge.set("type", L2S);

                    // Get Word from each Sense
                    int wordSize = 0;
                    wordSize = synset[j].getWordsSize();
                    int sensePolysemy = 0;
                    if (wordSize > 0) {
                        Word[] words = synset[j].getWords();
                        for (int k = 0; k < words.length; k++) {
                            // Get rid of the word which the same as our root word
                            if (!lemma.equalsIgnoreCase(words[k].getLemma())) {
                                // Search all the current Node and check if the
                                // Node with the same word exist or not
                                Node wordNode;
                                if (!nodeHashMap.containsKey(words[k].getLemma())) {
                                    // Create WNDefaultNode object for this word
                                    wordNode = g.addNode();
                                    setupWordNode(wordNode, senseNode, words[k]);
                                    // put this node into wordHashMap
                                    nodeHashMap.put(words[k].getLemma(), wordNode);
                                } else {
                                    // get the node from wordHashMap
                                    wordNode = (Node) nodeHashMap.get(words[k].getLemma());
                                }
                                // DefaultWNMutableTreeNode wordNode = new
                                // DefaultWNMutableTreeNode(words[k]);
                                // Link this wordNode to senseNode
                                if (countPolysemy) sensePolysemy += wordNode.getInt("polysemy");
                                Edge wordEdge = g.addEdge(senseNode, wordNode);
                                wordEdge.set("label", senseNode.getString("label") + " " + wordNode.getString("label"));
                                wordEdge.set("type", S2W);
                                // System.out.println("***** "+ words[k]);
                            }
                        }
                    } // ## End=> if(wordSize > 0)
                    if (countPolysemy) senseNode.setInt("polysemy", sensePolysemy);
                }
            }
        }
        if (countPolysemy) root.setInt("polysemy", rootPolysemy);
        return g;
    }
    
    /**
     * Find all synsets related to the given synset through the specified relationship.
     * 
     * @param synset the starting (source) synset
     * @param parent the graph node representing the source synset
     * @param g the graph in which to place the discovered relationships
     * @param nodeHashMap the temporary storage of nodes, for easy lookup of JWNL index if they have already been created and cached
     */
    private static void findRelations(Synset synset, Node parent,
            Graph g, HashMap nodeHashMap, HashSet edgeSet, PointerType pointerType, 
            boolean countPolysemy, boolean mergeWords) throws JWNLException {
        // ** Create a HashMap to put all the related "Word" objects we found
        Pointer[] pointers = synset.getPointers();
        if (pointers.length < 1)
        	return;
            
        // Use pointer to get all related synset
        for (int k = 0; k < pointers.length; k++) {
            // Pointer Type
            PointerType pType = pointers[k].getType();
            if (pType == null) 
                throw (new NullPointerException("pType null exception: " + synset.toString() + " pointer: " + pointers[k].toString()));

            if (!pType.equals(pointerType))
            	continue;

            Synset targetSynset = pointers[k].getTargetSynset();
            // Create synsetNode for this sense
            Node targetNode;
            // if already encountered synset, use it, unless mergeWords not selected
            if (nodeHashMap.containsKey(targetSynset.getKey()) && (mergeWords)){
                targetNode = (Node) nodeHashMap.get(targetSynset.getKey());
            }
            else {
                targetNode = g.addNode();
                setupSynsetNode(targetNode, parent, targetSynset);
                nodeHashMap.put(targetSynset.getKey(), targetNode);

                // Get Word from each Pointer Synset (new node guaranteed not to have word edges already)
                int wordSize = 0;
                int synsetPolysemy = 0;
                wordSize = targetSynset.getWordsSize();
                if (wordSize > 0) {
                    Word[] words = targetSynset.getWords();
                    
                    for (int j = 0; j < words.length; j++) {
                        // Search all nodes to see if word node exists already
                        // do not duplicate word node if mergetWords is true
                        Node wordNode;
                        if ((!mergeWords) || (!nodeHashMap.containsKey(words[j].getLemma()))) {
                            // Create WNDefaultNode object for this word
                            wordNode = g.addNode();
                            setupWordNode(wordNode, targetNode, words[j]);
                            // put this node into wordHashMap
                            nodeHashMap.put(words[j].getLemma(), wordNode);
                        } else {
                            // get the node from wordHashMap
                            wordNode = (Node) nodeHashMap.get((String) words[j].getLemma());
                        }
                        // Link wordNode to senseNode
                        Edge wordEdge = g.addEdge(targetNode, wordNode);
                        wordEdge.set("label", targetNode.getString("label") + " " + wordNode.getString("label"));
                        if (countPolysemy) synsetPolysemy += wordNode.getInt("polysemy"); 
                        // Set edge attribute to indicate the
                        // relationship between these two node
                        wordEdge.set("type", S2W);
                    }
                }
                if (countPolysemy) targetNode.setInt("polysemy", synsetPolysemy);
            }
            
            // link targetNode to parent node if not added already
            // don't processes edges already added (i.e. only process cycles once)
            if (!edgeSet.contains(makeKey(parent, targetNode, pType))) {
                edgeSet.add(makeKey(parent, targetNode, pType));
                Edge psynEdge = g.addEdge(parent, targetNode);
                psynEdge.set("label", parent.getString("label") + " " + targetNode.getString("label"));
                // Set edge attribute to indicate the relationship between them
                if (pType == PointerType.HYPONYM)
                    psynEdge.set("type", HYPONOMY);
                if (pType == PointerType.HYPERNYM) 
                    psynEdge.set("type", HYPERONOMY);
                psynEdge.setString("linktype", pType.getLabel());
                findRelations(targetSynset, targetNode, g, nodeHashMap, edgeSet, pointerType, countPolysemy, mergeWords);
            }
        }                
        
        
        // End of findLinkSyn
    }

    private static void setupWordNode(Node wordNode, Node synsetParentNode, Word word) throws JWNLException{
    	wordNode.setInt("type", WORD);
        wordNode.setString("label", word.getLemma().replace('_', ' '));
        wordNode.setBoolean("root", false);
        wordNode.setInt("wordIndex", word.getIndex()); // put WordNet "Word" with node
        wordNode.setString("pos", word.getPOS().getLabel());
        wordNode.setLong("offset", word.getSynset().getOffset());
        if (wordNode.canSetInt("polysemy")) {
        	IndexWordSet iws = dictionary.lookupAllIndexWords(word.getLemma().replace(' ', '_'));
        	IndexWord[] iw = iws.getIndexWordArray();
            int size = 0;
            for (int i = 0; i < iw.length; i++) {
                size += iw[i].getSenseCount();
            }
            wordNode.setInt("polysemy", size); // use getAllIndexWords as we do not want morphological processing 
        }
        if (synsetParentNode.canGet("senseIndex", int.class)) 
            wordNode.set("senseIndex", synsetParentNode.getInt("senseIndex"));
    }
    	
    private static void setupSynsetNode(Node synsetNode, Node synsetParentNode, Synset synset) throws JWNLException {
    	if (synsetNode.canSetInt("senseIndex")) {
    		if (synsetParentNode.canGetInt("senseIndex")) {
    			setupSynsetNode(synsetNode, synsetParentNode, synset, synsetParentNode.getInt("senseIndex"));
    		} else 
    			throw new JWNLException("Sense Index missing from synset: " + synset.toString());
    	} else {
    		setupSynsetNode(synsetNode, synsetParentNode, synset, -1);
    	}
    }
    
    	
    private static void setupSynsetNode(Node synsetNode, Node synsetParentNode, Synset synset, int senseIndex) throws JWNLException {
    	synsetNode.setInt("type", SENSE);
    	if (synset.getWordsSize() > 0) 
    		synsetNode.setString("label", synset.getWord(0).getLemma().replace('_', ' '));
    	else 
    		synsetNode.setString("label", WordNetSearchPanel
                .removeExamplesFromGloss(synset.getGloss()));
    	synsetNode.setString("gloss", WordNetSearchPanel
                .removeExamplesFromGloss(synset.getGloss()));                            
   		synsetNode.setBoolean("root", synsetParentNode == null);
        synsetNode.setLong("offset", synset.getOffset()); // put this synset reference to node
    	synsetNode.setString("pos", synset.getPOS().getLabel());
    	synsetNode.setInt("wordChildren", synset.getWordsSize());
        if (synsetNode.canSetInt("senseIndex"))
        	synsetNode.setInt("senseIndex", senseIndex);
    }
    
	private static void setupCloneNode(Node newNode, Tuple ref){
		for (int i = 0; i < ref.getColumnCount(); i++)
			newNode.set(i, ref.get(i));		
	}
   
	
    private static void setupGraph(Graph g, boolean countPolysemy, boolean trackSenseIndex) {
	   g.addColumn("type", int.class);
       g.addColumn("label", String.class);
       g.addColumn("root", boolean.class);
       g.addColumn("pos", String.class);
       g.addColumn("offset", long.class);
       g.addColumn("wordIndex", int.class);
       g.addColumn("linktype", String.class);
       g.addColumn("wordChildren", int.class);
       g.addColumn("gloss", String.class);
       if (trackSenseIndex) g.addColumn("senseIndex", int.class);
       if (countPolysemy) g.addColumn("polysemy", int.class);
    }
    
    private static String makeKey(Node parent, Node target, PointerType pType) {
        return new String(parent.toString() + target.toString() + pType.getLabel());
    }
    
    private static String makeKey(Node parent, Node target, int type) {
        return new String(parent.toString() + target.toString() + type);
    }
    
}