package ca.utoronto.cs.prefuseextensions.control;

import java.awt.event.MouseEvent;
import java.util.Iterator;

import prefuse.controls.ControlAdapter;
import prefuse.visual.AggregateItem;
import prefuse.visual.AggregateTable;
import prefuse.visual.VisualItem;

 public class AggregateFadeHoverControl extends ControlAdapter {
		
	 String AGGREGATES = "AGGREGATES";
	 String NODES = "NODES";
	 String FADE = "FADE";
	 String ACTION = null;
	 
	 public AggregateFadeHoverControl(String aggregateGroup, String nodeGroup, String fadeGroup, String action) {
		 super();
		 this.AGGREGATES = aggregateGroup;
		 this.NODES = nodeGroup;
		 this.FADE = fadeGroup;
		 this.ACTION = action;
	 }
	 
	 boolean aggregateDragged = false;
		
		@Override
		public void itemEntered(VisualItem item, MouseEvent evt) {
            if ( item.isInGroup(NODES) ) {
              AggregateTable at = (AggregateTable) item.getVisualization().getGroup(AGGREGATES);
              Iterator iterator = at.getAggregates(item);
              AggregateItem aggr;
              
              // cycle through aggregates containing this node, when a hover aggregate is found, set it
              while (iterator.hasNext()) {
            	  aggr = ((AggregateItem) iterator.next()); 
            	  if (aggr.containsItem(item)) {
            		  aggr.setHover(true);
            	  }
              }
              // cycle through all aggregates
              iterator = at.tuples();
              while (iterator.hasNext()) {
            	  aggr = ((AggregateItem) iterator.next()); 
            	  // aggregate not hovered
            	  if (!aggr.isHover()) {
            		  item.getVisualization().getFocusGroup(FADE).addTuple(aggr);
            		  Iterator aggregateItems = aggr.items();
            		  while (aggregateItems.hasNext()) {
            			  VisualItem aggregatedItem = (VisualItem) aggregateItems.next();
            			  // if item is not also in a hover aggregate, fade it
            			  Iterator itemAggregates = at.getAggregates(aggregatedItem);
            			  boolean hover = false;
            			  while (itemAggregates.hasNext()) {
            				  if (((AggregateItem) itemAggregates.next()).isHover())
            				  	hover = true;
            			  }
            			  if (!hover)
            			    aggr.getVisualization().getGroup(FADE).addTuple(aggregatedItem);
            		  }
            	  } else {
            		  Iterator aggregateItems = aggr.items();
            		  while (aggregateItems.hasNext()) {
            			  VisualItem aggregatedItem = (VisualItem) aggregateItems.next();
            			  aggregatedItem.setHover(true);
            		  }
            	  }
              }
              
              item.getVisualization().run(ACTION);
          	   
            }  
            
            if (item.isInGroup(AGGREGATES)) {
            	item.setHover(true);
            	AggregateItem aItem = (AggregateItem) item;
        		Iterator it = aItem.items();
        		while (it.hasNext()) {
        			((VisualItem)it.next()).setHover(true);
        		}
            	
        		item.getVisualization().run(ACTION); 
            }
            
            item.setFixed(false);
        }
        
        @Override
		public void itemExited(VisualItem item, MouseEvent evt) {
            if ( item.isInGroup(NODES) ) {
            	item.setFillColor(item.getEndFillColor());
                item.setStrokeColor(item.getEndStrokeColor());
                AggregateTable at = (AggregateTable) item.getVisualization().getGroup(AGGREGATES);
                Iterator iterator = at.tuples(); // item
                AggregateItem aggr;
                while (iterator.hasNext()) {
              	  aggr = ((AggregateItem) iterator.next());
              	  if (aggr.containsItem(item)) {
              		  aggr.setHover(false);
              		  Iterator aggregateItems = aggr.items();
              		  while (aggregateItems.hasNext()) {
              			  VisualItem aggregatedItem = (VisualItem) aggregateItems.next();
              			  aggregatedItem.setHover(false);
              		  }
              	  }
              	  else { 
            		  item.getVisualization().getFocusGroup(FADE).removeTuple(aggr);
            		  Iterator aggregateItems = aggr.items();
            		  while (aggregateItems.hasNext()) {
            			  VisualItem aggregatedItem = (VisualItem) aggregateItems.next();
            			  aggr.getVisualization().getGroup(FADE).removeTuple(aggregatedItem);
            		  }
              	  }
                }
            }
            
            if (item.isInGroup(AGGREGATES)) {
            	item.setHover(false);
            	AggregateItem aItem = (AggregateItem) item;
        		Iterator it = aItem.items();
        		while (it.hasNext()) {
        			((VisualItem)it.next()).setHover(false);
        		}
            }
            item.getVisualization().run(ACTION);
        }
	}    