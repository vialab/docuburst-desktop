/* CVS $Id: DocuBurst.java,v 1.1 2008/11/30 23:29:03 cmcollin Exp $ */
package ca.utoronto.cs.docuburst.applets;

import prefuse.util.ui.JPrefuseApplet;

public class DocuBurst extends JPrefuseApplet {

	private static final long serialVersionUID = 2924724258283361515L;

	public void init() {
        this.setContentPane(
            new ca.utoronto.cs.docuburst.DocuBurst());
    }
    
}

