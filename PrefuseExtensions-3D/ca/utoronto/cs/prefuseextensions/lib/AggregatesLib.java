package ca.utoronto.cs.prefuseextensions.lib;

import java.util.Iterator;

import prefuse.data.Tuple;
import prefuse.data.tuple.TupleSet;
import prefuse.util.collections.IntIterator;
import prefuse.visual.AggregateItem;
import prefuse.visual.AggregateTable;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;
import ca.utoronto.cs.prefuseextensions.layout.ConcaveHullLayout;

public class AggregatesLib {

    private AggregatesLib() {
        // prevent instantiation
    }
	
    /**
	 * Test if a given cluster number already exists.
	 *  
	 * @param at the aggregate table
	 * @param clusterNumber the cluster number to search for
	 * @param field the field in the aggregate table where the cluster numbers are stored
	 * @return true if and only if the cluster number has already been initialized
	 */
	public static boolean clusterExists(AggregateTable at, int clusterNumber, String field) {
		IntIterator rows = at.rows();
		while (rows.hasNext()) {
			int row = rows.nextInt();
			if (at.getInt(row, field) == clusterNumber)
				return true;
		}
		return false;
	}
	
	/**
	 * Get a pre-existing aggregate corresponding to the given cluster number. 
	 * Create a new one if necessary.
	 * 
	 * @param at the aggregate table
	 * @param clusterNumber the number of the cluster to retrieve
	 * @param field the field in the aggregate table where the cluster numbers are stored
	 * @return an AggregateItem with the specified cluster number
	 */
	public static AggregateItem getAItem(AggregateTable at, int clusterNumber, String field) {
		IntIterator rows = at.rows();
		while (rows.hasNext()) {
			int row = rows.nextInt();
			if (at.getInt(row, field) == clusterNumber)
				return (AggregateItem) at.getItem(row);
		}
		AggregateItem aItem = (AggregateItem) at.addItem();
		// initialize aggregate properties which don't have a fixed default
		aItem.setInt(field, clusterNumber);
		return aItem;
	}

	/**
	 * Get the current maximum cluster number in the table.
	 * 
	 * @return the max cluster number in the table
	 */
	public static int getMaxClusterNumber(AggregateTable at, String field) {
		IntIterator rows = at.rows();
		int maxClusterNumber = Integer.MIN_VALUE;
		while (rows.hasNext()) {
			int row = rows.nextInt();
			if (at.getInt(row, field) > maxClusterNumber) {
				maxClusterNumber = at.getInt(row, field);
			}
		}
		return maxClusterNumber;
	}
	
	/**
	 * Starting with a given node, recursively fill a cluster with all nodes having the same
	 * cluster number.
	 * 
	 * @param aItem the aggregate item to fill
	 * @param nodes the nodes to check
	 * @param field the field storing the aggregate number
	 * @param n the starting node
	 */
	public static void fillCluster(AggregateItem aItem, VisualItem n, TupleSet tupleSet, String field) {
		/*int clusterNumber = aItem.getInt(field);
		
		// check attached nodes and edges too
		Iterator tuples = tupleSet.tuples();
		while (tuples.hasNext()) {
			VisualItem neighbourTuple = (VisualItem) tuples.next();
			int[] cluster = (int[]) neighbourTuple.get(field);
			boolean contains = false;
			for (int c: cluster) {
				if (c == clusterNumber) {
					contains = true;
					break;
				}
			}
			if (contains) {
				if (!aItem.containsItem(neighbourTuple)) {
					aItem.addItem(neighbourTuple);
					// recurse
					fillCluster(aItem, neighbourTuple, tupleSet, field);
				}
			}
		}*/
		if (!aItem.containsItem(n))
			aItem.addItem(n);
		
		
	}

	/**
	 * Add the cluster number to the array stored in the given field.
	 *  
	 * @param t
	 * @param clusterNumber
	 * @param field
	 * @return whether the first clusternumber in the array has changed
	 */
	public static boolean addTupleToCluster(Tuple t, int clusterNumber, String field) {
		int[] clusters = new int[((int[])t.get(field)).length+1];
		clusters[clusters.length-1] = clusterNumber;
		t.set(field, clusters);
		return (clusters.length == 1);
	}
	
	/**
	 * Remove the cluster number from the array stored in the given field.  
	 * 
	 * @param t
	 * @param clusterNumber
	 * @param field
	 * @return whether the first clusternumber in the array has changed
	 */
	public static boolean removeTupleFromCluster(Tuple t, int clusterNumber, String field) {
		int[] oldClusters = (int[])t.get(field);
		int[] newClusters = new int[oldClusters.length-1];
		
		boolean updateFirstCluster = false;
		for (int i = 0, j = 0; i < oldClusters.length; i++) {
			if (oldClusters[i] != clusterNumber) {
				newClusters[j++] = oldClusters[i];
			} else {
				// first cluster changed
				if (i==0) {
					updateFirstCluster = true;
				}
			}
		}
		t.set(field, newClusters);
		return updateFirstCluster;
	}
}
