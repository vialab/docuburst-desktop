package ca.utoronto.cs.docuburst.data;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.SwingWorker.StateValue;

import ca.utoronto.cs.docuburst.preprocess.Preprocess;
import ca.utoronto.cs.wordnetexplorer.data.Document;
import ca.utoronto.cs.wordnetexplorer.data.ILoadListener;
import ca.utoronto.cs.wordnetexplorer.data.LoadEvent;

public class LoadPanel extends JDialog {

	Preprocess preprocess;
	Frame frame;

	ArrayList<ILoadListener<Document>> listeners = new ArrayList<ILoadListener<Document>>();
	
	public LoadPanel(Frame owner, Preprocess p) {
		super(owner);
		frame = owner;
		preprocess = p;
	
		setTitle("Open Document");
		setLayout(new BoxLayout(this.getContentPane(), BoxLayout.PAGE_AXIS));
		
		ButtonGroup sourceSelection = new ButtonGroup();
		
		final JRadioButton xmlSourceSelector = new JRadioButton("DocuBurst File:");
		final JRadioButton fileSourceSelector = new JRadioButton ("Plain Text File:");
		final JRadioButton pasteSourceSelector = new JRadioButton("Paste text:");
		
		sourceSelection.add(fileSourceSelector);
		sourceSelection.add(pasteSourceSelector);
		sourceSelection.add(xmlSourceSelector);

		// docuburst xml file source
		
		final JTextField docuburstFileField = new JTextField();
		final JButton docuburstSelectorButton = new JButton(new ImageIcon("data/folder.png"));
		JPanel docuburstFileSelectionPanel = new JPanel();
		docuburstFileSelectionPanel.setLayout(new BoxLayout(docuburstFileSelectionPanel, BoxLayout.LINE_AXIS));
		docuburstFileSelectionPanel.add(docuburstFileField);
		docuburstFileSelectionPanel.add(Box.createHorizontalStrut(10));
		docuburstFileSelectionPanel.add(docuburstSelectorButton);
		
		docuburstSelectorButton.addActionListener(new ActionListener() {
			JFileChooser jfc = new JFileChooser(ClassLoader.getSystemResource("./").getPath()) {
				@Override
				public boolean accept(File f) {
					return f.getName().toLowerCase().endsWith((".docuburst.xml"));
				}
			};
			
			@Override
			public void actionPerformed(ActionEvent e) {
				int returnVal = jfc.showOpenDialog((Component) e.getSource());
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					docuburstFileField.setText(jfc.getSelectedFile().getAbsolutePath());
				}
			}
		});
		
		// plain text file source
		final JTextField filenameField = new JTextField();
		final JButton fileSelectorButton = new JButton(new ImageIcon("data/folder.png"));
		JPanel fileSelectionPanel = new JPanel();
		fileSelectionPanel.setLayout(new BoxLayout(fileSelectionPanel, BoxLayout.LINE_AXIS));
		fileSelectionPanel.add(filenameField);
		fileSelectionPanel.add(Box.createHorizontalStrut(10));
		fileSelectionPanel.add(fileSelectorButton);
		
		fileSelectorButton.addActionListener(new ActionListener() {
			JFileChooser jfc = new JFileChooser(ClassLoader.getSystemResource("./").getPath());
			
			@Override
			public void actionPerformed(ActionEvent e) {
				int returnVal = jfc.showOpenDialog((Component) e.getSource());
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					filenameField.setText(jfc.getSelectedFile().getAbsolutePath());
				}
			}
		});
		
		// paste text
		final JTextArea pasteTextArea = new JTextArea();
		final JScrollPane pasteTextAreaScroll = new JScrollPane(pasteTextArea);
		
		JPanel sourcePanel = new JPanel();
		sourcePanel.setBorder(BorderFactory.createTitledBorder("Source"));
		GroupLayout sourcePanelLayout = new GroupLayout(sourcePanel);
		sourcePanelLayout.setAutoCreateContainerGaps(true);
		sourcePanelLayout.setAutoCreateGaps(true);
		sourcePanel.setLayout(sourcePanelLayout);
		
		sourcePanelLayout.setHorizontalGroup(
				sourcePanelLayout.createSequentialGroup()
				.addGroup(sourcePanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
						.addComponent(xmlSourceSelector)
						.addComponent(fileSourceSelector)
						.addComponent(pasteSourceSelector))
				.addGroup(sourcePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(docuburstFileSelectionPanel)
						.addComponent(fileSelectionPanel)
						.addComponent(pasteTextAreaScroll)));
		
		sourcePanelLayout.setVerticalGroup(
				sourcePanelLayout.createSequentialGroup()
				.addGroup(sourcePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(xmlSourceSelector)
						.addComponent(docuburstFileSelectionPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGroup(sourcePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(fileSourceSelector)
						.addComponent(fileSelectionPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGroup(sourcePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(pasteSourceSelector)
						.addComponent(pasteTextAreaScroll)));
		
		// output
		
		JLabel titleLabel = new JLabel("Title:");
		final JTextField titleField = new JTextField();
		
		JLabel outputFileLabel = new JLabel("Output File:");
		final JTextField outputFileTextField = new JTextField();
		final JButton outputFileSelectorButton = new JButton(new ImageIcon("data/folder.png"));
		
		outputFileSelectorButton.addActionListener(new ActionListener() {
			JFileChooser jfc = new JFileChooser(ClassLoader.getSystemResource("./").getPath());
			
			@Override
			public void actionPerformed(ActionEvent e) {
				int returnVal = jfc.showOpenDialog((Component) e.getSource());
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					outputFileTextField.setText(jfc.getSelectedFile().getAbsolutePath());
				}
			}
		});
		
		JPanel outputFilePanel = new JPanel();
		outputFilePanel.setLayout(new BoxLayout(outputFilePanel, BoxLayout.LINE_AXIS));
		outputFilePanel.add(outputFileTextField);
		outputFilePanel.add(Box.createHorizontalStrut(10));
		outputFilePanel.add(outputFileSelectorButton);
		
		final JPanel outputPanel = new JPanel();
		outputPanel.setBorder(BorderFactory.createTitledBorder("Output"));
		GroupLayout outputPanelLayout = new GroupLayout(outputPanel);
		outputPanelLayout.setAutoCreateContainerGaps(true);
		outputPanelLayout.setAutoCreateGaps(true);
		outputPanel.setLayout(outputPanelLayout);
		
		outputPanelLayout.setHorizontalGroup(
				outputPanelLayout.createSequentialGroup()
				.addGroup(outputPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
						.addComponent(titleLabel)
						.addComponent(outputFileLabel))
				.addGroup(outputPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(titleField)
						.addComponent(outputFilePanel)));
		
		outputPanelLayout.setVerticalGroup(
				outputPanelLayout.createSequentialGroup()
				.addGroup(outputPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(titleLabel)
						.addComponent(titleField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGroup(outputPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(outputFileLabel)
						.addComponent(outputFilePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)));
		
		outputPanelLayout.linkSize(SwingConstants.VERTICAL, titleField, outputFilePanel);
		
		// control the input fields 
		ActionListener choiceListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				docuburstFileField.setEnabled(xmlSourceSelector.isSelected());
				docuburstSelectorButton.setEnabled(xmlSourceSelector.isSelected());
				titleField.setEnabled(!xmlSourceSelector.isSelected());
				outputFileTextField.setEnabled(!xmlSourceSelector.isSelected());
				outputFileSelectorButton.setEnabled(!xmlSourceSelector.isSelected());
				
				filenameField.setEnabled(fileSourceSelector.isSelected());
				fileSelectorButton.setEnabled(fileSourceSelector.isSelected());
			
				pasteTextArea.setEnabled(pasteSourceSelector.isSelected());
			}
		};
			
		fileSourceSelector.addActionListener(choiceListener);
		pasteSourceSelector.addActionListener(choiceListener);
		xmlSourceSelector.addActionListener(choiceListener);
		
		// ok/cancel
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));
		
		JButton okButton = new JButton("OK");
		JButton cancelButton = new JButton("Cancel");
		
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				titleField.setText("");
				pasteTextArea.setText("");
				outputFileTextField.setText("");
				filenameField.setText("");
				setVisible(false);
			};
		});
		
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if (fileSourceSelector.isSelected()) {
						preprocessAndLoad(new File(filenameField.getText()), titleField.getText(), new File(outputFileTextField.getText()));
					}
				} catch (IOException ex) {
					JOptionPane.showMessageDialog((Component) e.getSource(), "Unable to process file.", "Error", JOptionPane.ERROR_MESSAGE);
					ex.printStackTrace();
				}
				
				if (pasteSourceSelector.isSelected()) {
					preprocess.preprocess(pasteTextArea.getText(), titleField.getText(), new File(outputFileTextField.getText()));
					JOptionPane.showConfirmDialog((Component) e.getSource(), "Text processed successfully.");
				}
				
				if (xmlSourceSelector.isSelected()) {
					// load xml file
				}
				titleField.setText("");
				pasteTextArea.setText("");
				outputFileTextField.setText("");
				filenameField.setText("");
				setVisible(false);
			}
		});
		
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(okButton);
		buttonPanel.add(Box.createHorizontalStrut(5));
		buttonPanel.add(cancelButton);
		buttonPanel.add(Box.createHorizontalStrut(5));
		
		// put it all together
		
		add(sourcePanel);
		add(Box.createVerticalStrut(5));
		add(outputPanel);
		add(Box.createVerticalStrut(5));
		add(buttonPanel);
		
		xmlSourceSelector.doClick();
		
		setSize(500,500);
		setLocationRelativeTo(getParent());
	}

	public void preprocessAndLoad(File inFile, String title, File outFile) throws IOException {
		long documentSize = inFile.length();
		
		BufferedReader br = new BufferedReader(new FileReader(inFile));
		BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
		
		final JProgressBar readProgress = new JProgressBar(0, 100);
		final JDialog progressDialog = new JDialog(this, true);
		progressDialog.setTitle("Processing Progress");
		progressDialog.add(readProgress);
		progressDialog.setSize(400, 100);
		progressDialog.setLocationRelativeTo(this);
		
		final SwingWorker<Document, Void> worker = preprocess.preprocess(br, title, documentSize, bw);
		
		worker.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals("progress")) {
					// check if done because SwingWorker was oddly sending progress events after the done event (reported as Sun Java bug 6493680)
					if (!worker.isDone()) {
						readProgress.setValue((Integer) evt.getNewValue());
					} 
				}
				// watch for worker finishing (done or cancelled)
				if (evt.getPropertyName().equals("state")) {
					if (evt.getNewValue().equals(StateValue.DONE)) {
						progressDialog.setVisible(false);
						readProgress.setValue(0);
						progressDialog.dispose();
						try {
							if (worker.isCancelled()) {
								JOptionPane.showMessageDialog(frame, "Processing cancelled.", "Error", JOptionPane.ERROR_MESSAGE);
							} else {
								try {
									Document doc = worker.get();
									JOptionPane.showMessageDialog(frame, "File processed successfully.", "Completed", JOptionPane.INFORMATION_MESSAGE);
									fireLoadEvent(doc);
								} catch (Exception e) {
									JOptionPane.showMessageDialog(frame, "Unable to process file.", "Error", JOptionPane.ERROR_MESSAGE);
									return;
								} 
							}
						} catch (HeadlessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} 
					}
				}
			}
		});
		
		worker.execute();
		
		progressDialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (!worker.isDone())
					worker.cancel(true);
			}
		});
		
		progressDialog.setVisible(true);
	}
	
	// needs to be synchronized so listeners aren't added (as documents are loading) at the same time as listeners are being notified
	public synchronized void addSelectionListener(ILoadListener<Document> listener) {
		listeners.add(listener);
	}
	
	// needs to be synchronized so listeners aren't added (as documents are loading) at the same time as listeners are being notified
	public synchronized void removeSelectionListener(ILoadListener<Document> listener) {
		listeners.remove(listener);
	}
	
	// needs to be synchronized so listeners aren't added (as documents are loading) at the same time as listeners are being notified
	private synchronized void fireLoadEvent(Document doc) {
		LoadEvent<Document> e = new LoadEvent<Document>(this, doc);
		for (ILoadListener<Document> listener : listeners) {
			listener.dataLoaded(e);
		}
	}	
}
