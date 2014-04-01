/* CVS $Id: WordNetExplorerActionList.java,v 1.1 2007/11/16 06:38:42 cmcollin Exp $ */
package ca.utoronto.cs.wordnetexplorer.prefuse.action;

import prefuse.Visualization;
import prefuse.action.ActionList;

public abstract class WordNetExplorerActionList extends ActionList {

    public WordNetExplorerActionList(Visualization visualization) {
        super(visualization);
    }

    public WordNetExplorerActionList() {
        // TODO Auto-generated constructor stub
    }

    /**
     * Remove any additions or changes made to the registry or display registered to this layout.
     * Cancel and remove this ActionList from the ActivityManager's schedule.
     */
    public abstract void remove(); 
}
