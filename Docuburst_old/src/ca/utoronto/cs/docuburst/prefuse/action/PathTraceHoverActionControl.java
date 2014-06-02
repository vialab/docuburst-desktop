package ca.utoronto.cs.docuburst.prefuse.action;

import java.awt.event.MouseEvent;
import java.util.Iterator;

import prefuse.controls.HoverActionControl;
import prefuse.data.Edge;
import prefuse.data.Node;
import prefuse.data.expression.Predicate;
import prefuse.data.expression.parser.ExpressionParser;
import prefuse.data.tuple.TupleSet;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

public class PathTraceHoverActionControl extends HoverActionControl {
	Predicate p;

	public PathTraceHoverActionControl(String action) {
		super(action);
		p = ExpressionParser.predicate("type = 2");
	}

	/**
	 * Add all nodes between hover node and root to pathToRoot focus group
	 * 
	 * @param item the item under hover
	 * @param e the mouse event for itemEntered
	 */
	public void itemEntered(VisualItem item, MouseEvent e) {
		if (item instanceof NodeItem) {
			TupleSet pathToRoot = item.getVisualization().getFocusGroup("pathToRoot");
			pathToRoot.clear();
			traceToRoot((Node) item, pathToRoot, p);

			super.itemEntered(item, e);
		}
	}

	/**
	 * Remove all nodes between hover node and root to pathToRoot focus group
	 * 
	 * @param item the item under hover
	 * @param e the mouse event for itemEntered
	 */
	public void itemExited(VisualItem item, MouseEvent e) {
		if (item instanceof NodeItem) {
			TupleSet pathToRoot = item.getVisualization().getFocusGroup("pathToRoot");
			pathToRoot.clear();
			super.itemExited(item, e);
		}
	}

	/**
	 * Trace from node to root along edges in which node is target (child).
	 * Also add word nodes for all sense nodes along the way.
	 * 
	 * @param n
	 *            the starting node to trace up from
	 * @param pathToRoot
	 *            the tuple set to add the nodes to (usually a focus group)
	 * @param p
	 *            predicate to filter non child edges on
	 */
	public void traceToRoot(Node n, TupleSet pathToRoot, Predicate p) {
		if (n != null) {
			pathToRoot.addTuple(n);
			Iterator edges = n.edges();
			while (edges.hasNext()) {
				Edge edge = (Edge) edges.next();
				// add edges where current node is child (n is target)
				if (edge.getTargetNode() == n) {
					pathToRoot.addTuple(n);
					pathToRoot.addTuple(edge);
					traceToRoot(edge.getSourceNode(), pathToRoot, p);
				} else {
					// add "word" nodes along the way (n is source)
					if (p.getBoolean(edge)) {
						pathToRoot.addTuple(edge.getTargetNode());
					}
				}
			}
		}
	}
}