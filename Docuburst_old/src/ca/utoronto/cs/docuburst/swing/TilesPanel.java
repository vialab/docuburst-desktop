package ca.utoronto.cs.docuburst.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import ca.utoronto.cs.docuburst.Param;
import ca.utoronto.cs.docuburst.prefuse.DocuBurstActionList;
import ca.utoronto.cs.docuburst.prefuse.action.HighlightTextHoverActionControl;
import ca.utoronto.cs.prefuseextensions.swing.ValueChangedEvent;
import ca.utoronto.cs.prefuseextensions.swing.ValueListener;

public class TilesPanel extends JPanel implements ValueListener<Integer> {

	int currentTile = 0;
	JTextField tileNumber;
	HighlightTextHoverActionControl hac;
	
	
	public TilesPanel(final HighlightTextHoverActionControl hac) {
		super(new BorderLayout());
		
		this.hac = hac;
		tileNumber = new JTextField();
		tileNumber.setAlignmentX(CENTER_ALIGNMENT);
		tileNumber.setFont(new Font(Param.interfaceFont, Font.PLAIN, 20));
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(3,1));//BoxLayout(buttonPanel, BoxLayout.PAGE_AXIS));
		buttonPanel.setAlignmentX(CENTER_ALIGNMENT);
		
		JButton previousTileButton = new JButton("Previous");
		previousTileButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (currentTile > 1) {  
					currentTile--;
					hac.fillTextArea(currentTile);
					tileNumber.setText(Integer.toString(currentTile));
				}
			}
		});
		
		buttonPanel.add(previousTileButton);
		buttonPanel.add(tileNumber);
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		
		tileNumber.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int tile = Integer.parseInt(tileNumber.getText());
				
				if ((tile < hac.getTotalTiles()) && (tile > 1)) {
					currentTile = tile;
					hac.fillTextArea(tile);
				} else {
					tileNumber.setText(Integer.toString(currentTile));
				}
			}
		});
		
		JButton nextTileButton = new JButton("Next");
		nextTileButton.setSize(previousTileButton.getSize());
		nextTileButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (currentTile < hac.getTotalTiles()) {
					currentTile++;
					hac.fillTextArea(currentTile);
					tileNumber.setText(Integer.toString(currentTile));
				}
			}
		});
		
		buttonPanel.add(nextTileButton);
		
		JTextPane textPane = new JTextPane();
		
		JScrollPane textScrollPanel = new JScrollPane(textPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		textScrollPanel.setMaximumSize(new Dimension(3000, 300));
		textScrollPanel.setPreferredSize(new Dimension(3000, 100));
		textPane.setEditable(false);
		hac.setTextPane(textPane);
		nextTileButton.doClick();
		
		add(buttonPanel, BorderLayout.WEST);
		add(textScrollPanel, BorderLayout.CENTER);
	}

	@Override
	public void valueChanged(ValueChangedEvent<Integer> e) {
		int tile = e.getValue();
		if ((tile < hac.getTotalTiles()) && (tile > 1)) {
			tileNumber.setText(e.getValue().toString());
			currentTile = tile;
			hac.fillTextArea(tile);
		} 	
	}
}