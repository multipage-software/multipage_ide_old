package org.multipage.generator;

import java.util.LinkedList;

import javax.swing.ButtonGroup;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SpringLayout;

import org.maclan.server.ServerUtilities;
import org.multipage.generator.RevertExternalProvidersDialog.ListEntry;
import org.multipage.gui.Utility;

import name.fraser.neil.plaintext.diff_match_patch;
import name.fraser.neil.plaintext.diff_match_patch.Diff;

/**
 * 
 * @author user
 *
 */
public class DifferPanel extends JPanel {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	private JSplitPane splitPane;
	private JScrollPane scrollPaneLeft;
	private JScrollPane scrollPaneRight;
	private JEditorPane editorPaneLeft;
	private JEditorPane editorPaneRight;
	private JLabel labelMessage;
	private JRadioButton radioRewriteExternalProviderChanges;
	private JRadioButton radiobuttonSaveNewSourceCode;
	private final ButtonGroup buttonGroup = new ButtonGroup();

	/**
	 * Create the panel.
	 */
	public DifferPanel() {
		
		// Initialize components.
		initComponents();
		// Post creation.
		postCreation(); //$hide$
	}
	
	/**
	 * Initialize components.
	 */
	private void initComponents() {
		SpringLayout springLayout = new SpringLayout();
		setLayout(springLayout);
		
		splitPane = new JSplitPane();
		springLayout.putConstraint(SpringLayout.WEST, splitPane, 10, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, splitPane, -10, SpringLayout.SOUTH, this);
		springLayout.putConstraint(SpringLayout.EAST, splitPane, -10, SpringLayout.EAST, this);
		splitPane.setResizeWeight(0.5);
		add(splitPane);
		
		scrollPaneLeft = new JScrollPane();
		splitPane.setLeftComponent(scrollPaneLeft);
		
		editorPaneLeft = new JEditorPane();
		editorPaneLeft.setEnabled(false);
		editorPaneLeft.setEditable(false);
		editorPaneLeft.setContentType("text/html");
		scrollPaneLeft.setViewportView(editorPaneLeft);
		
		scrollPaneRight = new JScrollPane();
		splitPane.setRightComponent(scrollPaneRight);
		
		editorPaneRight = new JEditorPane();
		editorPaneRight.setContentType("text/html");
		scrollPaneRight.setViewportView(editorPaneRight);
		
		labelMessage = new JLabel("org.multipage.generator.messageFoundChangedExternalProviderContent");
		springLayout.putConstraint(SpringLayout.NORTH, labelMessage, 10, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, labelMessage, 10, SpringLayout.WEST, this);
		add(labelMessage);
		
		radioRewriteExternalProviderChanges = new JRadioButton("org.multipage.generator.textRewriteExternalProviderChanges");
		springLayout.putConstraint(SpringLayout.NORTH, splitPane, 18, SpringLayout.SOUTH, radioRewriteExternalProviderChanges);
		buttonGroup.add(radioRewriteExternalProviderChanges);
		add(radioRewriteExternalProviderChanges);
		
		radiobuttonSaveNewSourceCode = new JRadioButton("org.multipage.generator.textAdaptAndSaveSlotTextValue");
		springLayout.putConstraint(SpringLayout.NORTH, radioRewriteExternalProviderChanges, 6, SpringLayout.SOUTH, radiobuttonSaveNewSourceCode);
		springLayout.putConstraint(SpringLayout.WEST, radioRewriteExternalProviderChanges, 0, SpringLayout.WEST, radiobuttonSaveNewSourceCode);
		springLayout.putConstraint(SpringLayout.NORTH, radiobuttonSaveNewSourceCode, 6, SpringLayout.SOUTH, labelMessage);
		springLayout.putConstraint(SpringLayout.WEST, radiobuttonSaveNewSourceCode, 139, SpringLayout.WEST, this);
		buttonGroup.add(radiobuttonSaveNewSourceCode);
		add(radiobuttonSaveNewSourceCode);
	}
	
	/**
	 * Post creation steps.
	 */
	private void postCreation() {
		
		localize();
	}
	
	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(labelMessage);
		Utility.localize(radiobuttonSaveNewSourceCode);
		Utility.localize(radioRewriteExternalProviderChanges);
	}

	/**
	 * Display diff.
	 * @param text1
	 * @param text2
	 */
	public void displayDiff(ListEntry entry) {
		
		diff_match_patch diff = new diff_match_patch();
		
		// Make diff depending on found external provider content.
		LinkedList<Diff> diffsResult = null;
		if (ServerUtilities.possiblyContainsTags(entry.externalText)) {
			diffsResult = diff.diff_main(entry.slotText, entry.externalText);
		}
		else {
			diffsResult = diff.diff_main(entry.processedText, entry.externalText);
		}
		String htmlDiff = diff.diff_prettyHtml(diffsResult);
		
		// Trim and display diff text.
		htmlDiff = htmlDiff.replaceAll("<del style=\"background:#ffe6e6;\">", "<span style=\"background:#ffe6e6;\">");
		htmlDiff = htmlDiff.replaceAll("</del>", "</span>");
		
		htmlDiff = htmlDiff.replaceAll("<ins style=\"background:#e6ffe6;\">", "<span style=\"background:#e6ffe6;\">");
		htmlDiff = htmlDiff.replaceAll("</ins>", "</span>");
		
		htmlDiff = htmlDiff.replaceAll("&para;", "");
		
		editorPaneLeft.setText(htmlDiff);
		
		// Trim and display slot text.
		String text = entry.slotText.replaceAll("\n", "<br>");
		editorPaneRight.setText(text);
	}
}
