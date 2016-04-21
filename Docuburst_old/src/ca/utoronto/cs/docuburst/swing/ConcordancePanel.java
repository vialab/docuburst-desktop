package ca.utoronto.cs.docuburst.swing;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import ca.utoronto.cs.docuburst.Param;
import ca.utoronto.cs.docuburst.prefuse.DocuBurstActionList;

public class ConcordancePanel extends JPanel {
	
	public static final String CONCORDANCE_FORMAT = "%1$3d: %2$40s     %3$s     %4$-40s\n";
	
	public ConcordancePanel(DocuBurstActionList docuburstLayout) {
		super(new BorderLayout());
		JTextPane textPane = new JTextPane();
		JScrollPane textScrollPanel = new JScrollPane(textPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		textPane.setFont(new Font(Param.interfaceFont, Font.PLAIN, 14));
		textPane.setEditable(false);
		docuburstLayout.getHighlightTextHoverActionControl().setConcordanceTextPane(textPane);

		add(textScrollPanel, BorderLayout.CENTER);
	}
	
}
