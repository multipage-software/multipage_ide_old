/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.print.PrinterException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.EditorKit;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;
import javax.swing.text.html.HTMLDocument;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import org.multipage.util.Obj;
import org.multipage.util.Resources;
import org.multipage.util.SimpleMethodRef;
import java.awt.event.KeyAdapter;

/**
 * 
 * @author
 *
 */
public class TextEditorPane extends JPanel implements StringValueEditor {
	
	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Highlighter.
	 */
	private static final Highlighter.HighlightPainter myHighlightPainter = new FindHighlightPainter(new Color(255, 100, 100));
	
	/**
	 * Tabulator width.
	 */
	public static int tabWidth = 4;
    public static String tabWhiteSpaces = "";
	
	/**
	 * Word wrap state.
	 */
	public static boolean wordWrapState;
	
	/**
	 * Static constructor.
	 */
	static {
		
		int count = tabWidth;
		while (count > 0) {
			
			tabWhiteSpaces += ' ';
			count--;
		}
	}
	
	/**
	 * Load dialog.
	 */
	protected void loadDialog() {
		
		buttonWrap.setSelected(wordWrapState);
		wrapUnwrap();
	}
	
	/**
	 * Save dialog.
	 */
	protected void saveDialog() {
		
		stopTimers();
		
		wordWrapState = buttonWrap.isSelected();
	}

	/**
	 * Set default data.
	 */
	public static void setDefaultData() {

		wordWrapState = false;
	}

	/**
	 * Load data.
	 * @param inputStream
	 */
	public static void serializeData(ObjectInputStream inputStream)
		throws IOException, ClassNotFoundException {

		wordWrapState = inputStream.readBoolean();
	}

	public static void serializeData(ObjectOutputStream outputStream)
		throws IOException {

		outputStream.writeBoolean(wordWrapState);
	}

	/**
	 * Read only background color.
	 */
	private static final Color readOnlyBackground = new Color(230, 230, 250);
	
	/**
	 * Move line right.
	 * @param lineText
	 * @return
	 */
	public static String moveLineRight(String lineText) {
		
		StringBuffer movedText = new StringBuffer();
		
		for (int index = 0; index < tabWidth; index++) {
			movedText.append(' ');
		}
		
		movedText.append(lineText);
		
		return movedText.toString();
	
	}

	/**
	 * Move line left.
	 * @param lineText
	 * @return
	 */
	public static String moveLineLeft(String lineText) {
		
		StringBuffer movedText = new StringBuffer();
		int spacesToRemove = tabWidth;
		
		boolean isStartingWhitespace = true;
		
		for (int index = 0; index < lineText.length(); index++) {
			
			Character character = lineText.charAt(index);
			
			if (isStartingWhitespace && character != ' ') {
				isStartingWhitespace = false;
				spacesToRemove = 0;
			}
			
			if (spacesToRemove == 0) {
				movedText.append(character);
			}
			else if (spacesToRemove > 0) {
				spacesToRemove--;
			}
		}
		
		return movedText.toString();
	}

	/**
	 * Parent window.
	 */
	private Window parentWindow;
	
	/**
	 * Use HTML editor.
	 */
	private boolean useHtmlEditor;

	/**
	 * Text font.
	 */
	private Font textFont;

	/**
	 * Cut button.
	 */
	private JButton buttonCut;

	/**
	 * Undo button.
	 */
	private JButton buttonUndo;

	/**
	 * Redo button.
	 */
	private JButton buttonRedo;
	
	/**
	 * Reset button.
	 */
	private JComponent buttonReset;
	
	/**
	 * Undoable managers.
	 */
	private UndoManager undoManagerPlain;
	private UndoManager undoManagerHtml;

	/**
	 * Find replace dialogs.
	 */
	private FindReplaceDialog findPlainDialog;
	private FindReplaceDialog findHtmlDialog;

	/**
	 * Enable / disable word wrap button.
	 */
	private JToggleButton buttonWrap;
	
	/**
	 * Popup menus.
	 */
	private JPopupMenu popupMenuPlain;
	private JPopupMenu popupMenuHtml;
	
	/**
	 * Change flags.
	 */
	private boolean changing = false;

	/**
	 * Change listeners.
	 */
	private LinkedList<SimpleMethodRef> changeListeners = 
		new LinkedList<SimpleMethodRef>();
	
	/**
	 * Extract HTML body flag.
	 */
	private boolean extractBody = true;
	
	/**
	 * Highlight script commands timer.
	 */
	private javax.swing.Timer highlightScriptCommandsTimer;
	
	/**
	 * Lambda function that returns text hints.
	 */
	public Function<String, Function<Integer, Function<Caret, Consumer<JTextPane>>>> intellisenseLambda = null;

	/**
	 * Rich text buttons.
	 */
	private JButton buttonFont;
	private JButton buttonForeground;
	private JButton buttonBackground;
	private JToggleButton buttonBold;
	private JToggleButton buttonItalic;
	private JToggleButton buttonUnderline;
	private JToggleButton buttonStrike;
	private JToggleButton buttonSubscript;
	private JToggleButton buttonSuperscript;
	private JComboBox fontFamily;
	private JComboBox fontSize;
	private JComboBox textAlignment;

	// $hide<<$
	/**
	 * Components.
	 */
	private JToolBar toolBar;
	private JScrollPane htmlScrollPane;
	private JTextPane htmlTextPane;
	public JTabbedPane tabbedPane;
	private JPanel panelHtml;
	private JScrollPane plainScrollPane;
	private JTextPane plainTextPane;
	private JToolBar richTextToolBar;
	/**
	 * @wbp.nonvisual location=520,109
	 */
	private final JPanel panelNoWrapHtml = new JPanel();
	/**
	 * @wbp.nonvisual location=520,169
	 */
	private final JPanel panelNoWrapPlain = new JPanel();

	/**
	 * Constructor.
	 */
	public TextEditorPane(Window parentWindow, boolean useHtmlEditor) {
		panelNoWrapPlain.setLayout(new BorderLayout(0, 0));
		panelNoWrapHtml.setLayout(new BorderLayout(0, 0));
		
		this.parentWindow = parentWindow;
		this.useHtmlEditor = useHtmlEditor;
		// Initialize components.
		initComponents();
		// $hide>>$
		// Post creation.
		postCreation();
		// $hide<<$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		SpringLayout springLayout = new SpringLayout();
		setLayout(springLayout);
		
		toolBar = new JToolBar();
		springLayout.putConstraint(SpringLayout.EAST, toolBar, 0, SpringLayout.EAST, this);
		toolBar.setFloatable(false);
		springLayout.putConstraint(SpringLayout.NORTH, toolBar, 0, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, toolBar, 0, SpringLayout.WEST, this);
		add(toolBar);
		
		tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
		tabbedPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				onTabChanged();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, tabbedPane, 0, SpringLayout.SOUTH, toolBar);
		springLayout.putConstraint(SpringLayout.WEST, tabbedPane, 0, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, tabbedPane, 0, SpringLayout.SOUTH, this);
		springLayout.putConstraint(SpringLayout.EAST, tabbedPane, 0, SpringLayout.EAST, this);
		add(tabbedPane);
		
		plainScrollPane = new JScrollPane();
		plainScrollPane.setBorder(null);
		tabbedPane.addTab("org.multipage.gui.messageHtmlText", null, plainScrollPane, null);
		
		plainTextPane = new JTextPane();
		plainTextPane.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				onKeyReleased(e);
			}
		});
		plainTextPane.setBorder(null);
		plainTextPane.setDragEnabled(true);
		plainScrollPane.setViewportView(plainTextPane);
		
		panelHtml = new JPanel();
		tabbedPane.addTab("org.multipage.gui.messageHtmlDesign", null, panelHtml, null);
		springLayout.putConstraint(SpringLayout.NORTH, panelHtml, 24, SpringLayout.SOUTH, tabbedPane);
		springLayout.putConstraint(SpringLayout.SOUTH, panelHtml, 176, SpringLayout.SOUTH, tabbedPane);
		panelHtml.setLayout(new BorderLayout(0, 0));
		
		htmlScrollPane = new JScrollPane();
		htmlScrollPane.setBorder(null);
		panelHtml.add(htmlScrollPane, BorderLayout.CENTER);
		springLayout.putConstraint(SpringLayout.NORTH, htmlScrollPane, 48, SpringLayout.SOUTH, tabbedPane);
		springLayout.putConstraint(SpringLayout.WEST, htmlScrollPane, 0, SpringLayout.WEST, tabbedPane);
		springLayout.putConstraint(SpringLayout.SOUTH, htmlScrollPane, -20, SpringLayout.SOUTH, this);
		springLayout.putConstraint(SpringLayout.EAST, htmlScrollPane, 223, SpringLayout.WEST, this);
		
		htmlTextPane = new JTextPane();
		htmlTextPane.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				onKeyReleased(e);
			}
		});
		htmlTextPane.setBorder(null);
		htmlTextPane.addCaretListener(new CaretListener() {
			public void caretUpdate(CaretEvent e) {
				setToolBarControls();
			}
		});
		htmlTextPane.setContentType("text/html;charset=UTF-8");
		addAncestorListener(new AncestorListener() {
			public void ancestorAdded(AncestorEvent event) {
				loadDialog();
			}
			public void ancestorMoved(AncestorEvent event) {
			}
			public void ancestorRemoved(AncestorEvent event) {
				saveDialog();
			}
		});
		
		htmlTextPane.setDragEnabled(true);
		htmlScrollPane.setViewportView(htmlTextPane);
		springLayout.putConstraint(SpringLayout.WEST, panelHtml, 34, SpringLayout.EAST, htmlScrollPane);
		springLayout.putConstraint(SpringLayout.EAST, panelHtml, 193, SpringLayout.EAST, htmlScrollPane);
		
		richTextToolBar = new JToolBar();
		panelHtml.add(richTextToolBar, BorderLayout.NORTH);
		richTextToolBar.setFloatable(false);
	}
	
	/**
	 * Post creation.
	 */
	private void postCreation() {

		// Initialize key strokes.
		initKeyStrokes();
		// Localize components.
		localize();
		// Set icons.
		setIcons();
		// Set tool bar.
		createToolBars();
		// Set editors.
		setEditors();
		// Set documents filter.
		setDocuments();
		// Set listeners.
		setListeners();
		// Set undoable edit.
		setUndoableEdit();
		// Create find dialog.
		createFindDialog();
		// Create popup menus.
		popupMenuPlain = new TextPopupMenu(plainTextPane);
		popupMenuHtml = new TextPopupMenu(htmlTextPane);
		// Create timers.
		createTimers();
	}

	/**
	 * Initialize key strokes.
	 */
	@SuppressWarnings("serial")
	private void initKeyStrokes() {
		
		// Escape key.
		Action removeHighlights = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Utility.removeFindHighlights(plainTextPane);
				Utility.removeFindHighlights(htmlTextPane);
			}};
		KeyStroke ecsapeKey = KeyStroke.getKeyStroke("ESCAPE");
		
		
		plainTextPane.getInputMap().put(ecsapeKey, "removeHighlights");
		plainTextPane.getActionMap().put("removeHighlights", removeHighlights);
		htmlTextPane.getInputMap().put(ecsapeKey, "removeHighlights");
		htmlTextPane.getActionMap().put("removeHighlights", removeHighlights);
		
		// CTRL + F key.
		Action find = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				findText();
			}};
		KeyStroke findKey = KeyStroke.getKeyStroke("control F");
		
		plainTextPane.getInputMap().put(findKey, "find");
		plainTextPane.getActionMap().put("find", find);
		htmlTextPane.getInputMap().put(findKey, "find");
		htmlTextPane.getActionMap().put("find", find);
		
		// Shift + TAB key.
		Action moveRight = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				moveTextRight();
			}
		};
		KeyStroke moveRightKey = KeyStroke.getKeyStroke("shift TAB");
		
		plainTextPane.getInputMap().put(moveRightKey, "moveRight");
		plainTextPane.getActionMap().put("moveRight", moveRight);
		htmlTextPane.getInputMap().put(moveRightKey, "moveRight");
		htmlTextPane.getActionMap().put("moveRight", moveRight);
		
		
		// CTRL + B key.
		Action moveLeft = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				moveTextLeft();
			}
		};
		KeyStroke moveLeftKey = KeyStroke.getKeyStroke("control B");
		
		plainTextPane.getInputMap().put(moveLeftKey, "moveLeft");
		plainTextPane.getActionMap().put("moveLeft", moveLeft);
		htmlTextPane.getInputMap().put(moveLeftKey, "moveLeft");
		htmlTextPane.getActionMap().put("moveLeft", moveLeft);
		
		// Shift + bacspace.
		Action backLeft = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				backTextLeft();
			}
		};
		KeyStroke backLeftKey = KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, InputEvent.SHIFT_DOWN_MASK);
		
		plainTextPane.getInputMap().put(backLeftKey, "backLeft");
		plainTextPane.getActionMap().put("backLeft", backLeft);
		htmlTextPane.getInputMap().put(backLeftKey, "backLeft");
		htmlTextPane.getActionMap().put("backLeft", backLeft);
		
		
		// CTRL + Z key.
		Action undo = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				undoText();
			}
		};
		KeyStroke undoKey = KeyStroke.getKeyStroke("control Z");
		
		plainTextPane.getInputMap().put(undoKey, "undo");
		plainTextPane.getActionMap().put("undo", undo);
		htmlTextPane.getInputMap().put(undoKey, "undo");
		htmlTextPane.getActionMap().put("undo", undo);
		
		
		// CTRL + Y key.
		Action redo = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				redoText();
			}
		};
		KeyStroke redoKey = KeyStroke.getKeyStroke("control Y");
		
		plainTextPane.getInputMap().put(redoKey, "redo");
		plainTextPane.getActionMap().put("redo", redo);
		htmlTextPane.getInputMap().put(redoKey, "redo");
		htmlTextPane.getActionMap().put("redo", redo);
	}

	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(tabbedPane);
	}

	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		// Set tab icons.
		tabbedPane.setIconAt(0, Images.getIcon("org/multipage/gui/images/html_icon.png"));
		tabbedPane.setIconAt(1, Images.getIcon("org/multipage/gui/images/text_icon.png"));
	}

	/**
	 * Set tool bars.
	 */
	private void createToolBars() {

		buttonCut = ToolBarKit.addToolBarButton(toolBar, "org/multipage/gui/images/cut_icon.png",
				this, "cutText", "org.multipage.gui.tooltipCutText");
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/gui/images/copy_icon.png",
				this, "copyText", "org.multipage.gui.tooltipCopySelectedText");
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/gui/images/paste_icon.png",
				this, "pasteText", "org.multipage.gui.tooltipPasteText");
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/gui/images/select_all.png",
				this, "selectAll", "org.multipage.gui.tooltipSelectAll");

		ToolBarKit.addToolBarButton(toolBar, "org/multipage/gui/images/copy_all.png",
				this, "copyAll", "org.multipage.gui.tooltipCopyAllText");
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/gui/images/replace_icon.png",
				this, "replaceText", "org.multipage.gui.tooltipReplaceText");
		toolBar.addSeparator();
		buttonFont = ToolBarKit.addToolBarButton(toolBar, "org/multipage/gui/images/font_icon.png",
				this, "setFont", "org.multipage.gui.tooltipSetFont");
		toolBar.addSeparator();
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/gui/images/indent.png",
				this, "moveTextRight", "org.multipage.gui.tooltipMoveTextRight");
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/gui/images/unindent.png",
				this, "moveTextLeft", "org.multipage.gui.tooltipMoveTextLeft");
		toolBar.addSeparator();
		buttonUndo = ToolBarKit.addToolBarButton(toolBar, "org/multipage/gui/images/undo_icon.png",
				this, "undoText", "org.multipage.gui.tooltipUndoAction");
		buttonRedo = ToolBarKit.addToolBarButton(toolBar, "org/multipage/gui/images/redo_icon.png",
				this, "redoText", "org.multipage.gui.tooltipRedoAction");
		toolBar.addSeparator();
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/gui/images/search_icon.png",
				this, "findText", "org.multipage.gui.tooltipFindText");
		toolBar.addSeparator();
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/gui/images/print_icon.png",
				this, "print", "org.multipage.gui.tooltipPrintText");
		buttonWrap = ToolBarKit.addToggleButton(toolBar, "org/multipage/gui/images/word_wrap.png",
				this, "wrapUnwrap", "org.multipage.gui.tooltipWrapUnwrap");
		buttonReset = ToolBarKit.addToolBarButton(toolBar, "org/multipage/gui/images/cancel_icon.png",
				this, "resetText", "org.multipage.gui.tooltipResetContent");
		
		// If the HTML editor is used.
		if (useHtmlEditor) {

			buttonBold = ToolBarKit.addToggleButton(richTextToolBar,
					"org/multipage/gui/images/bold.png",
					this, "boldText", "org.multipage.gui.tooltipSetBoldText");
			buttonItalic = ToolBarKit.addToggleButton(richTextToolBar,
					"org/multipage/gui/images/italic.png",
					this, "italicText", "org.multipage.gui.tooltipSetItalicText");
			buttonUnderline = ToolBarKit.addToggleButton(richTextToolBar,
					"org/multipage/gui/images/underline.png",
					this, "underlineText", "org.multipage.gui.tooltipSetUnderlinedText");
			buttonStrike = ToolBarKit.addToggleButton(richTextToolBar,
					"org/multipage/gui/images/strike.png",
					this, "strikeText", "org.multipage.gui.tooltipSetStrikedText");
			buttonSubscript = ToolBarKit.addToggleButton(richTextToolBar,
					"org/multipage/gui/images/subscript.png",
					this, "subscriptText", "org.multipage.gui.tooltipSetSubscriptText");
			buttonSuperscript = ToolBarKit.addToggleButton(richTextToolBar,
					"org/multipage/gui/images/superscript.png",
					this, "superscriptText", "org.multipage.gui.tooltipSetSuperscriptText");
			
			setFontNames(richTextToolBar);
			setFontSizes(richTextToolBar);
			setAlignment(richTextToolBar);
			
			buttonForeground = ToolBarKit.addToolBarButton(richTextToolBar,
					"org/multipage/gui/images/foreground.png",
					this, "foregroundText", "org.multipage.gui.tooltipSetTextForegroundColor");
			buttonBackground = ToolBarKit.addToolBarButton(richTextToolBar,
					"org/multipage/gui/images/background.png",
					this, "backgroundText", "org.multipage.gui.tooltipSetTextBackgroundColor");
			buttonBackground.setVisible(false);// hide it
		}
	}

	/**
	 * Set paragraph alignment.
	 * @param toolBar
	 */
	private void setAlignment(JToolBar toolBar) {
		
		// Get current paragraph alignment.
		AttributeSet attributes = Utility.getInputAttributes(htmlTextPane);
		int alignment = StyleConstants.getAlignment(attributes);
			
		textAlignment = new JComboBox();
		textAlignment.setEnabled(false);
		textAlignment.setMaximumSize(new Dimension(40, 26));
		
		Utility.setParagraphAlignments(textAlignment, alignment);
		
		toolBar.add(textAlignment);
		
		// Set listener.
		textAlignment.addItemListener(new ItemListener() {
			// On selection.
			@Override
			public void itemStateChanged(ItemEvent e) {
				Object selected = textAlignment.getSelectedItem();
				if (selected instanceof Object []) {
					Object [] item = (Object []) selected;
					textAlign((Integer) item[1]);
				}
			}
		});
		
		textAlignment.setVisible(false); // hide it
	}

	/**
	 * Set font sizes.
	 * @param toolBar
	 */
	private void setFontSizes(JToolBar toolBar) {
		
		// Get current font size.
		AttributeSet attributes = Utility.getInputAttributes(htmlTextPane);
		int size = StyleConstants.getFontSize(attributes);
				
		fontSize = new JComboBox();
		fontSize.setMaximumSize(new Dimension(45, 26));
		
		Utility.loadFontSizes(fontSize, size);
		toolBar.add(fontSize);
		
		final JPanel thisPanel = this;
		
		// Set listeners.
		fontSize.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {

				Object selected = fontSize.getSelectedItem();
				if (selected instanceof Integer) {
					textSize((Integer) selected);
				}
			}
		});
		fontSize.getEditor().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Check value.
				String value = fontSize.getSelectedItem().toString();
				try {
					textSize(Integer.parseInt(value));
				}
				catch (NumberFormatException err) {
					Utility.show(thisPanel, "org.multipage.gui.messageInputValueIsNotNumber");
				}
			}
		});
	}

	/**
	 * Set font names
	 * @param toolBar
	 */
	private void setFontNames(JToolBar toolBar) {
		
		// Get current font family.
		AttributeSet attributes = Utility.getInputAttributes(htmlTextPane);
		String name = StyleConstants.getFontFamily(attributes);
		
		// Add font names.
		fontFamily = new JComboBox();
		fontFamily.setMaximumSize(new Dimension(150, 26));
		Utility.loadFontFamilies(fontFamily, name);
		toolBar.add(fontFamily);
		
		// Set listener.
		fontFamily.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				
				Object selected = fontFamily.getSelectedItem();
				if (selected instanceof String) {
					textFont((String) selected);
				}
			}
		});
	}

	/**
	 * Tab changed.
	 */
	protected void onTabChanged() {
		
		if (buttonFont == null) {
			return;
		}
		boolean htmlEditorSelected = (tabbedPane.getSelectedIndex() == 1);
		buttonFont.setEnabled(!htmlEditorSelected);
		// Reset undo managers.
		undoManagerPlain.discardAllEdits();
		undoManagerHtml.discardAllEdits();
		onDocumentChanged();
		// Close find dialogs.
		if (findPlainDialog.isVisible()) {
			findPlainDialog.closeWindow();
		}
		if (findHtmlDialog.isVisible()) {
			findHtmlDialog.closeWindow();
		}
		
		// Highlight script commands.
		highlightScriptCommands();
	}

	/**
	 * Get current editor.
	 */
	public JEditorPane getCurrentEditor() {
		
		return tabbedPane.getSelectedIndex() == 0 ? plainTextPane : htmlTextPane;
	}
	
	/**
	 * Cut text.
	 */
	public void cutText() {
		
		JEditorPane editor = getCurrentEditor();
		editor.grabFocus();
		editor.cut();
	}

	/**
	 * Copy text.
	 */
	public void copyText() {

		JEditorPane editor = getCurrentEditor();
		editor.grabFocus();
		editor.copy();
	}

	/**
	 * Paste text.
	 */
	public void pasteText() {

		JEditorPane editor = getCurrentEditor();
		editor.grabFocus();
		editor.paste();
	}
	
	/**
	 * Select all.
	 */
	public void selectAll() {
		
		JEditorPane editor = getCurrentEditor();
		editor.grabFocus();
		editor.selectAll();
	}
	
	/**
	 * Replace text.
	 */
	public void replaceText() {
		
		selectAll();
		pasteText();
	}
	
	/**
	 * Copy all.
	 */
	public void copyAll() {
		
		selectAll();
		copyText();
	}
	
	/**
	 * Get undo manager.
	 * @return
	 */
	private UndoManager getUndoManager() {
		
		return tabbedPane.getSelectedIndex() == 0 ? undoManagerPlain : undoManagerHtml;
	}
	
	/**
	 * Set undoable edit.
	 */
	private void setUndoableEdit() {

		// Create undo manager.
		undoManagerPlain = new UndoManager();
		undoManagerHtml = new UndoManager();
		// Get documents.
		Document documentPlain = plainTextPane.getDocument();
		Document documentHtml = htmlTextPane.getDocument();
		// Set listeners.
		documentPlain.addUndoableEditListener(undoManagerPlain);
		documentHtml.addUndoableEditListener(undoManagerHtml);
		// Add listener.
		documentPlain.addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				onDocumentChanged();
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				onDocumentChanged();
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				onDocumentChanged();
			}
		});
	}
	
	/**
	 * On document change.
	 */
	protected void onDocumentChanged() {

		// Enable / disable buttons.
		UndoManager undoManager = getUndoManager();
		buttonUndo.setEnabled(undoManager.canUndo());
		buttonRedo.setEnabled(undoManager.canRedo());
	}

	/**
	 * Enable / disable editor.
	 * @param editable
	 */
	public void setEditable(boolean editable) {

		plainTextPane.setEditable(editable);
		plainTextPane.setBackground(editable ? Color.WHITE : readOnlyBackground);
		htmlTextPane.setEditable(editable);
		htmlTextPane.setBackground(editable ? Color.WHITE : readOnlyBackground);
		buttonCut.setEnabled(editable);
		buttonUndo.setEnabled(editable);
		buttonRedo.setEnabled(editable);
		buttonReset.setEnabled(editable);
	}
	
	/**
	 * Set font.
	 */
	public void setFont() {
		
		FontChooser dialog = new FontChooser();
		Font font = dialog.showDialog(this, textFont);
		
		if (font != null) {
			// Set font.
			plainTextPane.setFont(font);
			textFont = font;
		}
		
		plainTextPane.grabFocus();
	}

	/**
	 * Create find dialog.
	 */
	private void createFindDialog() {

		findPlainDialog = new FindReplaceDialog(parentWindow, plainTextPane);
		findHtmlDialog = new FindReplaceDialog(parentWindow, htmlTextPane);
	}

	/**
	 * Set text.
	 * @param text
	 */
	public void setText(String text) {

		final Point point = new Point();
		
		plainTextPane.setText(text);
		// Set caret position.
		plainTextPane.setCaretPosition(0);
		// Reset undo manager.
		undoManagerPlain.discardAllEdits();
		undoManagerHtml.discardAllEdits();
		// Scroll to the old position.
		Utility.scrollToPosition(htmlScrollPane, point);
		
		// Highlight script commands.
		highlightScriptCommands();
	}

	/**
	 * Get text fond.
	 * @return
	 */
	public Font getTextFont() {

		return plainTextPane.getFont();
	}

	/**
	 * Set text font.
	 * @param font
	 */
	public void setTextFont(Font font) {

		this.textFont = font;
		plainTextPane.setFont(font);
	}
	
	/**
	 * Undo text.
	 */
	public void undoText() {

		try {
			getUndoManager().undo();
		}
		catch (CannotUndoException e) {
			
		}
		getCurrentEditor().grabFocus();
	}
	
	/**
	 * Redo text.
	 */
	public void redoText() {
		
		try {
			getUndoManager().redo();
		}
		catch (CannotRedoException e) {
			
		}
		getCurrentEditor().grabFocus();
	}
	
	/**
	 * Find text.
	 */
	public void findText() {
		
		// Show find dialog.
		if (tabbedPane.getSelectedIndex() == 0) {
			
			// Get selected text.
			String selectedText = plainTextPane.getSelectedText();
			findPlainDialog.setVisible(true);
			findPlainDialog.setFindText(selectedText);
		}
		else {
			
			// Get selected text.
			String selectedText = htmlTextPane.getSelectedText();
			findHtmlDialog.setVisible(true);
			findHtmlDialog.setFindText(selectedText);
		}
	}

	/**
	 * Get text.
	 * @return
	 */
	public String getText() {

		String text = plainTextPane.getText();
		return text;
	}

	/**
	 * Get scroll position.
	 * @return
	 */
	public Point getScrollPosition() {
		
		JViewport viewport = htmlScrollPane.getViewport();
		return viewport.getViewPosition();
	}

	/**
	 * Scroll to given position.
	 * @param position
	 */
	public void scrollToPosition(final Point position) {
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// Scroll to the start.
				JViewport viewport = htmlScrollPane.getViewport();
				if (viewport != null) {
					viewport.setViewPosition(position);
				}
			}
		});
	}
	
	/**
	 * Print text.
	 */
	public void print() {
		
		try {
			getCurrentEditor().print();
		}
		catch (PrinterException e) {
			// Inform user.
			Utility.show2(this, e.getLocalizedMessage());
		}
	}
	
	/**
	 * Enable / disable word wrap.
	 */
	public void wrapUnwrap() {

		boolean wrapText = buttonWrap.isSelected();
		
		if (wrapText) {
			htmlScrollPane.setViewportView(htmlTextPane);
			
			plainScrollPane.setViewportView(plainTextPane);
		}
		else {
			htmlScrollPane.setViewportView(panelNoWrapHtml);
			panelNoWrapHtml.add(htmlTextPane);
			
			plainScrollPane.setViewportView(panelNoWrapPlain);
			panelNoWrapPlain.add(plainTextPane);
		}
		htmlTextPane.revalidate();
		plainTextPane.revalidate();
	}

	/**
	 * @return the plainTextPane
	 */
	public JEditorPane getTextPane() {
		return plainTextPane;
	}
	
	/**
	 * Add popup menu.
	 * @param popupAddIn
	 */
	public void addPopupMenusPlain(TextPopupMenuAddIn popupAddIn) {
		
		popupAddIn.addMenu(popupMenuPlain, plainTextPane);
	}
	
	/**
	 * Add popup menu.
	 * @param popupAddIn
	 */
	public void addPopupMenusHtml(TextPopupMenuAddIn popupAddIn) {
		
		popupAddIn.addMenu(popupMenuHtml, htmlTextPane);
	}
	
	/**
	 * Set listerers.
	 */
	private void setListeners() {
		
		htmlTextPane.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				onChangeDesign();
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				onChangeDesign();
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				onChangeDesign();
			}
		});
		plainTextPane.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				onChangeSource();
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				onChangeSource();
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				onChangeSource();
			}
		});
	}

	/**
	 * On change design window content.
	 */
	protected void onChangeDesign() {

		if (changing) {
			return;
		}
		
		// Disable deadlock.
		changing = true;
		
		// Read text from the design text component and insert it to the source text component.
		HTMLDocument htmlDocument = (HTMLDocument) htmlTextPane.getDocument();
		
		EditorKit kit = htmlTextPane.getEditorKit();
		StringWriter writer = new StringWriter();
		try {
			kit.write(writer, htmlDocument, 0, htmlDocument.getLength());
		}
		catch (Exception e1) {
			e1.printStackTrace();
		}
		
		String htmlText = writer.toString();

		htmlText = adaptTextToSource(htmlText);
		
		// Rearrange <p> paragraphs.
		if (extractBody) {
			
			Obj<Boolean> modified = new Obj<Boolean>(false);
			htmlText = rearrangeParagraphs(htmlText, modified);
			
			final String newHtmlText = htmlText;
			
			if (modified.ref) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
	
						// Update HTML editor text.
						changing = true;
						htmlTextPane.setText(newHtmlText);
						changing = false;
					}
				});
			}
		}
		
		plainTextPane.setText(htmlText);
		
		changing = false;
		
		fireChange();
	}
	
	/**
	 * On key released event.
	 * @param event
	 */
	protected void onKeyReleased(KeyEvent event) {
		
		// Get plain text and insert it to the design text component.
		Document plainDocument = plainTextPane.getDocument();
		int plainLength = plainDocument.getLength();
		
		final Obj<String> plainText = new Obj<String>("");
		try {
			// Get text content.
			plainText.ref = plainDocument.getText(0, plainLength);
			
			// Get caret position.
			int selection = plainTextPane.getSelectionStart();
			Caret caret = plainTextPane.getCaret();
			
			// If the intellisense exists, get text hints.
			if (intellisenseLambda != null) {
				
				SwingUtilities.invokeLater(() -> {
					intellisenseLambda.apply(plainText.ref).apply(selection).apply(caret).accept(plainTextPane);
				});
				
			}
		}
		catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * On change source.
	 */
	protected void onChangeSource() {

		if (changing) {
			return;
		}
		
		// Disable deadlock.
		changing = true;
		
		// Get plain text and insert it to the design text component.
		Document plainDocument = plainTextPane.getDocument();
		int plainLength = plainDocument.getLength();
		
		final Obj<String> plainText = new Obj<String>("");
		try {
			// Get text content.
			plainText.ref = plainDocument.getText(0, plainLength);
		}
		catch (BadLocationException e) {
			e.printStackTrace();
		}
			
		if (useHtmlEditor) {
			htmlTextPane.setText(plainText.ref);

			
		}
		changing = false;
		
		// Highlight script commands.
		highlightScriptCommands();
		
		fireChange();
	}

	/**
	 * Add listener.
	 * @param simpleMethodRef
	 */
	protected void addChangeListener(SimpleMethodRef simpleMethodRef) {
		
		changeListeners.add(simpleMethodRef);
	}
	
	/**
	 * Fire change.
	 */
	private void fireChange() {
		
		for (SimpleMethodRef listener : changeListeners) {
			listener.run();
		}
	}

	/**
	 * Adapt text.
	 * @param text
	 * @return
	 */
	private String adaptTextToSource(String text) {
		
		// Extract body.
		if (extractBody) {
			text = extractBody(text);
		}

		// Remove character escapes.
		Pattern escapePattern = Pattern.compile("&#[0-9]+;");
		while (true) {
			
			Matcher escapeMatcher = escapePattern.matcher(text);
			// Find escape sequence.
			if (!escapeMatcher.find(0)) {
				break;
			}
			// Replace escape sequence.
			int escapeStart = escapeMatcher.start();
			int escapeEnd = escapeMatcher.end();
			String numberString = text.substring(escapeStart + 2, escapeEnd - 1);
			int number = 0;
			try {
				number = Integer.parseInt(numberString);
			}
			catch (NumberFormatException e) {
			}
			char character = (char) number;
			
			int length = text.length();
			text = text.substring(0, escapeStart) + character + text.substring(escapeEnd, length);
		}
		
		// Replace &amp;
		text = text.replace("&amp;", "&");
		// Replace &quot;
		text = text.replace("&quot;", "\"");

		return text;
	}

	/**
	 * Rearrange paragraphs.
	 * @param text
	 * @param modified
	 * @return
	 */
	private String rearrangeParagraphs(String text, Obj<Boolean> modified) {
		
		modified.ref = false;
		
		Pattern paragraphStartPattern = Pattern.compile("<\\s*p\\s*[^>]*");
		Pattern paragraphEndPattern = Pattern.compile("<\\s*/\\s*p\\s*>");
		
		// Simplify end of lines.
		text = text.replace("\r\n", "\n");
		text = text.replace('\r', '\n');
		
		String resultText = "";
		
		int length = text.length();
		int lineBegin = 0;
		
		for (int index = 0; index < length; index++) {
			
			// If a paragraph start is on current position.
			if (Utility.patternMatches(text, index, paragraphStartPattern)) {
				
				// Find end of the paragraph.
				Integer paragraphEnd = Utility.findPattarnEnd(text, index, paragraphEndPattern);
				if (paragraphEnd == null) {
					
					paragraphEnd = text.length();
				}
				
				// Save paragraph.
				resultText += text.substring(index, paragraphEnd) + '\n';
				index = paragraphEnd;
				lineBegin = index;
				
				continue;
			}
			
			// If end of text is reached.
			if (index == length - 1) {
				
				boolean isNewLineEnd = text.charAt(index) == '\n';
				String line = "";
				
				if (length > lineBegin) {
					line = text.substring(lineBegin, isNewLineEnd ? length - 1 : length);
				}
				
				// Create paragraph.
				String paragraph = String.format("<p style=\"margin-top: 0\">\n    %s\n</p>\n", line);
				resultText += paragraph;
				
				// Add possible end new line.
				if (isNewLineEnd && !line.isEmpty()) {
					resultText += "<p style=\"margin-top: 0\">\n    \n</p>\n";
				}
				
				modified.ref = true;
				
				break;
			}
			
			// If the end of line is reached, add new paragraph.
			if (text.charAt(index) == '\n') {
				
				String line = "";
				
				if (index > lineBegin) {
					line = text.substring(lineBegin, index);
				}
				
				// Create paragraph.
				String paragraph = String.format("<p style=\"margin-top: 0\">\n    %s\n</p>\n", line);
				resultText += paragraph;
				
				lineBegin = index + 1;
				
				modified.ref = true;
			}
		}
		
		return resultText;
	}

	/**
	 * Extract HTML body.
	 * @param text
	 * @return
	 */
	private String extractBody(String text) {
		
		// Find body start and end.
		final Pattern bodyStartPattern = Pattern.compile("<\\s*body\\s*>", Pattern.CASE_INSENSITIVE);
		final Pattern bodyEndPattern = Pattern.compile("<\\s*/\\s*body\\s*>", Pattern.CASE_INSENSITIVE);
		
		// Find first body start.
		Matcher bodyStartMatcher = bodyStartPattern.matcher(text);
		if (bodyStartMatcher.find()) {
			
			int bodyStart = bodyStartMatcher.end();
			int bodyEnd = text.length();
			
			// Find last body end.
			Matcher bodyEndMatcher = bodyEndPattern.matcher(text);
			while (bodyEndMatcher.find()) {
				
				bodyEnd = bodyEndMatcher.start();
			}
			
			// Extract body.
			text = text.substring(bodyStart, bodyEnd);
			
			// Move text lines left.
			String [] textLines = text.split("\n");
			String movedText = "";
			
			for (String textLine : textLines) {
				
				textLine = moveLineLeft(textLine);
				movedText += textLine + '\n';
			}
			
			// Trim text.
			text = movedText.trim();
		}
		
		return text;
	}

	/**
	 * Set editors.
	 */
	private void setEditors() {

		if (!useHtmlEditor) {
			tabbedPane.removeTabAt(1);
		}
		else {
			// Customized HTML editor kit.
			CustomizedHTMLEditorKit editorKit = new CustomizedHTMLEditorKit();
			htmlTextPane.setEditorKit(editorKit);
		}
	}
	
	/**
	 * Select HTML editor.
	 */
	public void selectHtmlEditor(boolean select) {
		
		if (useHtmlEditor) {
			tabbedPane.setSelectedIndex(select ? 1 : 0);
		}
	}

	/**
	 * Reset text.
	 */
	public void resetText() {
		
		if (JOptionPane.showConfirmDialog(this,
				Resources.getString("org.multipage.gui.messageResetContent"))
				!= JOptionPane.YES_OPTION) {
			return;
		}
		
		changing = true;
		htmlTextPane.setText("");
		plainTextPane.setText("");
		changing = false;
	}

	/**
	 * Toggle bold text.
	 */
	public void boldText() {
		
		htmlTextPane.grabFocus();
		
		boolean flag = StyleConstants.isBold(Utility.getInputAttributes(htmlTextPane));

		SimpleAttributeSet attributes = new SimpleAttributeSet();
		StyleConstants.setBold(attributes, !flag);
		Utility.setCharacterAttributes(htmlTextPane, attributes, false);
	}

	/**
	 * Toggle italic text.
	 */
	public void italicText() {
		
		htmlTextPane.grabFocus();
		
		boolean flag = StyleConstants.isItalic(Utility.getInputAttributes(htmlTextPane));

		SimpleAttributeSet attributes = new SimpleAttributeSet();
		StyleConstants.setItalic(attributes, !flag);
		Utility.setCharacterAttributes(htmlTextPane, attributes, false);
	}
	
	/**
	 * Toggle underlined text.
	 */
	public void underlineText() {
		
		htmlTextPane.grabFocus();
		
		boolean flag = StyleConstants.isUnderline(Utility.getInputAttributes(htmlTextPane));

		SimpleAttributeSet attributes = new SimpleAttributeSet();
		StyleConstants.setUnderline(attributes, !flag);
		Utility.setCharacterAttributes(htmlTextPane, attributes, false);
	}
	
	/**
	 * Toggle strike text.
	 */
	public void strikeText() {
		
		htmlTextPane.grabFocus();
		
		boolean flag = StyleConstants.isStrikeThrough(Utility.getInputAttributes(htmlTextPane));

		SimpleAttributeSet attributes = new SimpleAttributeSet();
		StyleConstants.setStrikeThrough(attributes, !flag);
		Utility.setCharacterAttributes(htmlTextPane, attributes, false);
	}
	
	/**
	 * Toggle subscript text.
	 */
	public void subscriptText() {
		
		htmlTextPane.grabFocus();
		
		boolean flag = StyleConstants.isSubscript(Utility.getInputAttributes(htmlTextPane));

		SimpleAttributeSet attributes = new SimpleAttributeSet();
		StyleConstants.setSubscript(attributes, !flag);
		Utility.setCharacterAttributes(htmlTextPane, attributes, false);
	}
	
	/**
	 * Toggle superscript text.
	 */
	public void superscriptText() {
		
		htmlTextPane.grabFocus();
		
		boolean flag = StyleConstants.isSuperscript(Utility.getInputAttributes(htmlTextPane));

		SimpleAttributeSet attributes = new SimpleAttributeSet();
		StyleConstants.setSuperscript(attributes, !flag);
		Utility.setCharacterAttributes(htmlTextPane, attributes, false);
	}

	/**
	 * Set font family.
	 * @param fontFamily
	 */
	protected void textFont(String fontFamily) {

		htmlTextPane.grabFocus();
		
		SimpleAttributeSet attributes = new SimpleAttributeSet();
		StyleConstants.setFontFamily(attributes, fontFamily);
		Utility.setCharacterAttributes(htmlTextPane, attributes, false);		
	}

	/**
	 * Set text size.
	 * @param size
	 */
	protected void textSize(int size) {

		htmlTextPane.grabFocus();
		
		SimpleAttributeSet attributes = new SimpleAttributeSet();
		StyleConstants.setFontSize(attributes, size);
		Utility.setCharacterAttributes(htmlTextPane, attributes, false);		
	}

	/**
	 * Set text align.
	 * @param align
	 */
	protected void textAlign(int align) {

		htmlTextPane.grabFocus();

		MutableAttributeSet attributes = new SimpleAttributeSet();
		StyleConstants.setAlignment(attributes, align);
		htmlTextPane.setParagraphAttributes(attributes, false);
	}
	
	/**
	 * Set tool bar controls.
	 */
	protected void setToolBarControls() {
		
		if (buttonBold == null) {
			return;
		}
		
		AttributeSet textAttributes = Utility.getInputAttributes(htmlTextPane);
		AttributeSet paragraphAttributes = htmlTextPane.getParagraphAttributes();
		
		Color color = StyleConstants.getForeground(textAttributes);
		buttonForeground.setBackground(color);
		color = (Color) textAttributes.getAttribute(StyleConstants.Background);
		if (color == null) {
			color = Color.WHITE;
		}
		buttonBackground.setBackground(color);
		buttonBold.setSelected(StyleConstants.isBold(textAttributes));
		buttonItalic.setSelected(StyleConstants.isItalic(textAttributes));
		buttonUnderline.setSelected(StyleConstants.isUnderline(textAttributes));
		buttonStrike.setSelected(StyleConstants.isStrikeThrough(textAttributes));
		buttonSubscript.setSelected(StyleConstants.isSubscript(textAttributes));
		buttonSuperscript.setSelected(StyleConstants.isSuperscript(textAttributes));
		

		String name = StyleConstants.getFontFamily(textAttributes);
		Utility.selectComboItem(fontFamily, name);
		
		Integer size = StyleConstants.getFontSize(textAttributes);
		fontSize.getEditor().setItem(size.toString());
		Utility.selectComboItem(fontSize, size);

		int align = StyleConstants.getAlignment(paragraphAttributes);
		Utility.selectComboAlign(textAlignment, align);
	}
	
	/**
	 * Set foreground color.
	 */
	public void foregroundText() {
		
		Color color = StyleConstants.getForeground(Utility.getInputAttributes(htmlTextPane));

		// Choose color.
		color = Utility.chooseColor(this, color);
		buttonForeground.setBackground(color);
		
		// Set foreground color.
		htmlTextPane.grabFocus();
		
		SimpleAttributeSet attributes = new SimpleAttributeSet();
		StyleConstants.setForeground(attributes, color);
		Utility.setCharacterAttributes(htmlTextPane, attributes, false);
	}
	
	/**
	 * Set background text.
	 */
	public void backgroundText() {
		
		Color color = StyleConstants.getBackground(Utility.getInputAttributes(htmlTextPane));

		// Choose color.
		color = Utility.chooseColor(this, color);
		buttonBackground.setBackground(color);
		
		// Set foreground color.
		htmlTextPane.grabFocus();
		
		SimpleAttributeSet attributes = new SimpleAttributeSet();
		StyleConstants.setBackground(attributes, color);
		Utility.setCharacterAttributes(htmlTextPane, attributes, false);	
	}

	/**
	 * Highlight found.
	 * @param foundAttr
	 */
	public void highlightFound(final FoundAttr foundAttr) {
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				
				Utility.highlight(plainTextPane, foundAttr, myHighlightPainter);
				if (useHtmlEditor) {
					Utility.highlight(htmlTextPane, foundAttr, myHighlightPainter);
				}				
			}
		});
	}

	/**
	 * Gets true value if a HTML body is extracted.
	 * @param extractBody the extractBody to set
	 */
	public void setExtractBody(boolean extractBody) {
		
		this.extractBody = extractBody;
	}
	
	/**
	 * Set documents.
	 */
	private void setDocuments() {

		// Create document filter.
		DocumentFilter filter = new DocumentFilter() {

			@Override
			public void replace(FilterBypass fb, int offset, int length,
					String text, AttributeSet attrs)
					throws BadLocationException {
				
				// Replace tabulator with spaces.
				text = text.replace("\t", tabWhiteSpaces);
				
				if (text.equals("\n")) {
					// On new line keep leading white spaces.
					String previousText = fb.getDocument().getText(0, offset);
					String spaces = getLeadingSpacesFromPreviousText(previousText);
					text += spaces;
				}
				super.replace(fb, offset, length, text, attrs);
			}
		};
		
		// Set document filters.
		AbstractDocument document = (AbstractDocument) plainTextPane.getDocument();
		document.setDocumentFilter(filter);
	}
	
	/**
	 * Get leading spaces from previous text.
	 * @param previousText
	 * @return
	 */
	protected String getLeadingSpacesFromPreviousText(String previousText) {
		
		// Get last line.
		String[] lines = previousText.split("\n");
		int length = lines.length;
		
		String lastLine = null;
		if (length > 0) {
			lastLine = lines[length - 1];
		}
		else {
			lastLine = previousText.replace("\n", "");
		}
		
		String spaces = "";
		
		// Get last line leading spaces.
		for (int index = 0; index < lastLine.length(); index++) {
			
			char character = lastLine.charAt(index);
			if (Character.isWhitespace(character)) {
				spaces += character;
			}
			else {
				break;
			}
		}

		return spaces;
	}

	/**
	 * Set tab size.
	 * @param pane
	 * @param size
	 */
	public static void setTabSize(JTextPane pane, int size) {
		String tab = "";
		for (int i = 0; i < size; i++) {
			tab += " ";
		}
		float f = (float) pane.getFontMetrics(pane.getFont()).stringWidth(tab);
		TabStop[] tabs = new TabStop[500]; // this sucks

		for (int i = 0; i < tabs.length; i++) {
			tabs[i] = new TabStop(f * (i + 1), TabStop.ALIGN_LEFT,
					TabStop.LEAD_NONE);
		}

		TabSet tabset = new TabSet(tabs);
		StyleContext sc = StyleContext.getDefaultStyleContext();
		AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY,
				StyleConstants.TabSet, tabset);
		pane.setParagraphAttributes(aset, false);
	}

	/**
	 * Get line from the text.
	 * @param text
	 * @param lineIndex
	 * @return
	 */
	private static String getLine(String text, int lineIndex) {
		
		int length = text.length();
		int currentLine = 0;
		StringBuffer lineText = new StringBuffer();
		
		for (int charIndex = 0; charIndex < length; charIndex++) {
			
			Character character = text.charAt(charIndex);
			
			if (currentLine == lineIndex) {
				lineText.append(character);
			}
			if (currentLine > lineIndex) {
				break;
			}

			// Recognize line end.
			if (character == '\n') {
				currentLine++;
			}
			else if (character == '\r') {
				Integer nextIndex = charIndex + 1;
				if (nextIndex >= length) {
					nextIndex = null;
				}
				if (nextIndex == null) {
					currentLine++;
				}
				else {
					Character nextCharacter = text.charAt(nextIndex);
					if (nextCharacter != '\n') {
						currentLine++;
					}
					else {
						charIndex++;
						currentLine++;
					}
				}
			}
		}
		
		return lineText.toString();
	}

	/**
	 * Get line indices.
	 * @param textComponent 
	 * @return
	 */
	public static void moveLines(boolean left, JTextComponent textComponent) {
		
		// Get selection.
		Integer start = textComponent.getSelectionStart();
		Integer end = textComponent.getSelectionEnd();
		// Get text.
		String text = textComponent.getText();

		int lineIndex = 0;
		int length = text.length();
		Integer startLine = null;
		Integer endLine = null;
		boolean isLineStart = true;
		int position = 0;
		
		for (int charIndex = 0; charIndex < length; charIndex++) {

			// Get start line.
			if (position >= start) {
				if (startLine == null) {
					startLine = lineIndex;
				}
			}
			
			// Get stop line.
			if (position >= end) {
				if (endLine == null) {
					if (isLineStart && start == end || !isLineStart) {
						endLine = lineIndex;
					}
					else {
						endLine = lineIndex - 1;
						if (endLine < 0) {
							endLine = 0;
						}
 					}
				}
			}
			
			isLineStart = false;
			
			// Recognize line end.
			Character character = text.charAt(charIndex);
			if (character == '\n') {
				lineIndex++;
				isLineStart = true;
			}
			else if (character == '\r') {
				Integer nextIndex = charIndex + 1;
				if (nextIndex >= length) {
					nextIndex = null;
				}
				if (nextIndex == null) {
					lineIndex++;
					isLineStart = true;					
				}
				else {
					Character nextCharacter = text.charAt(nextIndex);
					if (nextCharacter != '\n') {
						lineIndex++;
						isLineStart = true;							
					}
					else {
						charIndex++;
						lineIndex++;
						isLineStart = true;
					}
				}
			}
			
			position++;
		}
		
		if (startLine == null) {
			startLine = lineIndex;
		}
		if (endLine == null) {
			if (isLineStart && start == end || !isLineStart) {
				endLine = lineIndex;
			}
			else {
				endLine = lineIndex - 1;
				if (endLine < 0) {
					endLine = 0;
				}
			}
		}
		
		// Move lines left or right.
		position = 0;
		String newText = "";
		int lineCount = lineIndex;
		start = null;
		end = null;
		
		for (int index = 0; index <= lineCount; index++) {
			
			String lineText = getLine(text, index);
			if (index >= startLine && index <= endLine) {
				if (start == null) {
					start = newText.length();
				}
				newText += left ? TextEditorPane.moveLineLeft(lineText) : TextEditorPane.moveLineRight(lineText);
				
				if (index == lineCount && start != null && end == null) {
					end = newText.length();
				}
			}
			else {
				if (start != null && end == null) {
					end = newText.length();
				}
				newText += lineText;
			}
		}
		
		textComponent.setText(newText);
		textComponent.grabFocus();

		if (start != null && end != null) {
					
			textComponent.setSelectionStart(start);
			textComponent.setSelectionEnd(end);
		}
	}

	/**
	 * Shift text back.
	 * @param textComponent
	 */
	private static void backText(JTextComponent textComponent) {
		
		int position = textComponent.getCaretPosition();

		// Get component text.
		int length = textComponent.getDocument().getLength();
		String text = "";
		try {
			text = textComponent.getDocument().getText(0, length);
		}
		catch (BadLocationException e) {
		}
		
		// Find previous new line character.
		int lineStart = position - 1;
		while (true) {
			
			if (lineStart < 0) {
				lineStart = 0;
				break;
			}
			
			char character = text.charAt(lineStart);
			if (character == '\n') {

				lineStart++;
				break;
			}
			
			lineStart--;
		}
		
		// Remove leading spaces.
		int removeCount = 0;
		int index = lineStart;
		
		while (true) {
			
			char character = text.charAt(index);
			if (character == ' ') {
				index++;
				
				removeCount++;
				if (removeCount < tabWidth) {
					continue;
				}
			}
			
			break;
		}
		
		// Remove leading spaces.
		if (removeCount > 0) {

			try {
				textComponent.getDocument().remove(lineStart, removeCount);
			}
			catch (BadLocationException e) {
				e.printStackTrace();
			}
			
			// Set new caret position.
			textComponent.setCaretPosition(position - removeCount);
		}
	}

	/**
	 * Move text right.
	 */
	public void moveTextRight() {
		
		if (getCurrentEditor().isEditable()) {
			TextEditorPane.moveLines(false, getCurrentEditor());
		}
	}
	
	/**
	 * Move text left.
	 */
	public void moveTextLeft() {
		
		if (getCurrentEditor().isEditable()) {
			TextEditorPane.moveLines(true, getCurrentEditor());
		}
	}
	
	/**
	 * Shift text back.
	 */
	protected void backTextLeft() {
		
		if (getCurrentEditor().isEditable()) {
			TextEditorPane.backText(getCurrentEditor());
		}
	}

	/**
	 * Create timers.
	 */
	private void createTimers() {
		
		// Create Swing timer.
		highlightScriptCommandsTimer = new Timer(100, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				// Highlight script commands.
				if (plainTextPane.isShowing()) {
					highlightScriptCommands(plainTextPane);
				}
				
				if (htmlTextPane.isShowing()) {
					highlightScriptCommands(htmlTextPane);
				}
			}
		});
		
		// Timer action is invoked only once.
		highlightScriptCommandsTimer.setRepeats(false);
		
		highlightScriptCommandsTimer.setCoalesce(true);
	}
	
	/**
	 * Callback method which highlights script commands.
	 * @param textPane
	 */
	protected void highlightScriptCommands(JTextPane textPane) {
		
		// Override this method.
	}

	/**
	 * Stop timers.
	 */
	private void stopTimers() {
		
		highlightScriptCommandsTimer.stop();
	}
	
	/**
	 * Highlight script commands.
	 */
	protected void highlightScriptCommands() {
		
		// Start timer.
		if (!highlightScriptCommandsTimer.isRunning()) {
			highlightScriptCommandsTimer.start();
		}
	}

	/**
	 * Get component.
	 */
	@Override
	public Component getComponent() {
		
		return this;
	}

	/**
	 * Get text value.
	 */
	@Override
	public String getStringValue() {
		
		return getText();
	}
	
	/**
	 * Get specification of value
	 */
	@Override
	public String getSpecification() {
		
		return getText();
	}
	
	/**
	 * Set string value.
	 */
	@Override
	public void setStringValue(String string) {
		
		setText(string);
	}

	/**
	 * Get value meaning.
	 */
	@Override
	public String getValueMeaning() {
		
		return meansText;
	}

	/**
	 * Text editor grabs focus.
	 */
	public void grabFocusText() {
		
		JEditorPane editorPane = getCurrentEditor();
		if (editorPane != null) {
			
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					
					editorPane.grabFocus();
				}
			});
		}
	}

	/**
	 * Set grayed controls. If a false value is returned, program will use its own method
	 * to gray this panel controls.
	 */
	@Override
	public boolean setControlsGrayed(boolean isDefault) {
		
		return false;
	}
}