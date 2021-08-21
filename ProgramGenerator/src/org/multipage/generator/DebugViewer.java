/*
 * Copyright 2010-2018 (C) vakol
 * 
 * Created on : 31-07-2018
 *
 */
package org.multipage.generator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.util.LinkedList;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.StyleSheet;

import org.maclan.server.XdebugListener;
import org.maclan.server.XdebugPacket;
import org.multipage.gui.AlertWithTimeout;
import org.multipage.gui.Callback;
import org.multipage.gui.Images;
import org.multipage.gui.RendererJLabel;
import org.multipage.gui.Utility;
import org.multipage.util.DOM;
import org.multipage.util.Obj;
import org.multipage.util.Resources;
import org.multipage.util.j;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This is GUI for debugging
 * @author dvchance
 *
 */
public class DebugViewer extends JFrame {

	/**
	 * Version
	 */
	private static final long serialVersionUID = 1L;
	
	// $hide>>$
	/**
	 * Window boundary
	 */
	private static Rectangle bounds;
	
	/**
	 * Set default data.
	 */
	public static void setDefaultData() {
		
		bounds = new Rectangle();
	}
	
	/**
	 * Load data.
	 * @param inputStream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void serializeData(ObjectInputStream inputStream)
			throws IOException, ClassNotFoundException {
		
		bounds = Utility.readInputStreamObject(inputStream, Rectangle.class);
	}
	
	/**
	 * Save data.
	 * @param outputStream
	 */
	public static void seriliazeData(ObjectOutputStream outputStream)
		throws IOException {
		
		outputStream.writeObject(bounds);
	}
	
	/**
	 * Header to display
	 */
	private String header;
	
	/**
	 * Lines of code to display or null if there is nothing to display
	 */
	private LinkedList<String> codeLines;
	
	/**
	 * Script file name
	 */
	private String scriptFileName = "";
	
	/**
	 * A line number of debugger step
	 */
	private int stepLineNumber = -1;
	
	/**
	 * Watch list model
	 */
	private DefaultListModel<String> listWatchModel;
	
	/**
	 * Stack list model
	 */
	private DefaultListModel<Node> listStackModel;
	
	/**
	 * Display code callbacks
	 */
	private class DisplayCodeInterface {
		
		/**
		 * Get line
		 */
		String line() {
			return "";
		}
	}
	
	/**
	 * Object status
	 */
	private Object status;

	// $hide<<$
	
	/**
	 * Controls
	 */
	private JEditorPane textCode;
	private JEditorPane textInfo;
	private JTextField textCommand;
	private JButton buttonSend;
	private JPanel panelBottom;
	private JTabbedPane tabbedPane;
	private JPanel panelWatch;
	private JPanel panelStack;
	private JPanel panelCommand;
	private JScrollPane scrollPaneOutput;
	private JScrollPane scrollPaneWatch;
	private JScrollPane scrollPaneStack;
	private JPanel panelRight;
	private JPanel panelLeft;
	private JToolBar toolBar;
	private JButton buttonRun;
	private JScrollPane scrollCodePane;
	private JPanel panelStatus;
	private JLabel labelStatus;
	private JButton buttonExit;
	private JButton buttonStepInto;
	private JButton buttonStepOut;
	private JButton buttonStepOver;
	private JList listWatch;
	private JList listStack;
	
	/**
	 * Create the frame.
	 * @param parentComponent 
	 */
	public DebugViewer(Component parentComponent) {
		
		initComponents();
		postCreate(); // $hide$
	}

	/**
	 * Initialize components
	 */
	private void initComponents() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onClose();
			}
			@Override
			public void windowOpened(WindowEvent arg0) {
				onOpen();
			}
		});
		
		setBounds(100, 100, 909, 654);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		JSplitPane splitPane = new JSplitPane();
		splitPane.setOneTouchExpandable(true);
		splitPane.setResizeWeight(0.7);
		getContentPane().add(splitPane);
		
		panelRight = new JPanel();
		splitPane.setRightComponent(panelRight);
		panelRight.setLayout(new BorderLayout(0, 0));
		
		tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		panelRight.add(tabbedPane, BorderLayout.CENTER);
		
		panelWatch = new JPanel();
		tabbedPane.addTab("Watch", null, panelWatch, null);
		panelWatch.setLayout(new BorderLayout(0, 0));
		
		scrollPaneWatch = new JScrollPane();
		panelWatch.add(scrollPaneWatch);
		
		listWatch = new JList();
		scrollPaneWatch.setViewportView(listWatch);
		
		panelStack = new JPanel();
		tabbedPane.addTab("Stack", null, panelStack, null);
		panelStack.setLayout(new BorderLayout(0, 0));
		
		scrollPaneStack = new JScrollPane();
		panelStack.add(scrollPaneStack);
		
		listStack = new JList();
		scrollPaneStack.setViewportView(listStack);
		
		panelOutput = new JPanel();
		tabbedPane.addTab("Output", null, panelOutput, null);
		panelOutput.setLayout(new BorderLayout(0, 0));
		
		scrollPane = new JScrollPane();
		panelOutput.add(scrollPane);
		
		textOutput = new JTextArea();
		textOutput.setFont(new Font("Monospaced", Font.PLAIN, 13));
		textOutput.setEditable(false);
		scrollPane.setViewportView(textOutput);
		
		panelCommand = new JPanel();
		tabbedPane.addTab("Command", null, panelCommand, null);
		panelCommand.setLayout(new BorderLayout(0, 0));
		
		scrollPaneOutput = new JScrollPane();
		scrollPaneOutput.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		panelCommand.add(scrollPaneOutput);
		
		textInfo = new JEditorPane();
		textInfo.setPreferredSize(new Dimension(30, 30));
		scrollPaneOutput.setViewportView(textInfo);
		textInfo.setEditable(false);
		
		panelBottom = new JPanel();
		panelCommand.add(panelBottom, BorderLayout.SOUTH);
		panelBottom.setPreferredSize(new Dimension(10, 19));
		panelBottom.setLayout(new BorderLayout(0, 0));
		
		textCommand = new JTextField();
		textCommand.setPreferredSize(new Dimension(6, 24));
		panelBottom.add(textCommand, BorderLayout.CENTER);
		textCommand.setColumns(10);
		
		buttonSend = new JButton("Send");
		panelBottom.add(buttonSend, BorderLayout.EAST);
		buttonSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onSendCommand();
			}
		});
		
		panelLeft = new JPanel();
		splitPane.setLeftComponent(panelLeft);
		panelLeft.setLayout(new BorderLayout(0, 0));
		
		scrollCodePane = new JScrollPane();
		scrollCodePane.setPreferredSize(new Dimension(30, 30));
		panelLeft.add(scrollCodePane, BorderLayout.CENTER);
		
		textCode = new JEditorPane();
		scrollCodePane.setViewportView(textCode);
		textCode.setPreferredSize(new Dimension(20, 20));
		textCode.setContentType("text/html");
		textCode.setEditable(false);
		
		toolBar = new JToolBar();
		getContentPane().add(toolBar, BorderLayout.NORTH);
		
		buttonRun = new JButton("run");
		buttonRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onRun();
			}
		});
		
		buttonStepInto = new JButton("step into");
		buttonStepInto.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onStepInto();
			}
		});
		buttonStepInto.setPreferredSize(new Dimension(20, 20));
		toolBar.add(buttonStepInto);
		
		buttonStepOver = new JButton("step over");
		buttonStepOver.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onStepOver();
			}
		});
		buttonStepOver.setPreferredSize(new Dimension(20, 20));
		toolBar.add(buttonStepOver);
		
		buttonStepOut = new JButton("step out");
		buttonStepOut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onStepOut();
			}
		});
		buttonStepOut.setPreferredSize(new Dimension(20, 20));
		toolBar.add(buttonStepOut);
		buttonRun.setPreferredSize(new Dimension(20, 20));
		toolBar.add(buttonRun);
		
		buttonExit = new JButton("exit");
		buttonExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onExit();
			}
		});
		buttonExit.setPreferredSize(new Dimension(20, 20));
		toolBar.add(buttonExit);
				
		panelStatus = new JPanel();
		panelStatus.setPreferredSize(new Dimension(10, 18));
		getContentPane().add(panelStatus, BorderLayout.SOUTH);
		FlowLayout fl_panelStatus = new FlowLayout(FlowLayout.RIGHT, 5, 5);
		panelStatus.setLayout(fl_panelStatus);
		
		labelStatus = new JLabel("status");
		panelStatus.add(labelStatus);
	}
	
	/**
	 * Post creation
	 */
	private void postCreate() {
		
		localize();
		setIcons();
		setCallbacks();
		establishWatchDog();
		
		initStackDump();
		initWatch();
		
		loadDialog();
	}

	/**
	 * Called when the windows is opened
	 */
	protected void onOpen() {
		
		// Start debugger
		XdebugListener.getSingleton().startDebugging();
	}
	
	/**
	 * Called when a user closes this window with click on window close button
	 */
	protected void onClose() {
		
		// Stop debugger
		XdebugListener.getSingleton().stopDebugging();
		
		saveDialog();
		dispose();
	}

	/**
	 * Load dialog
	 */
	private void loadDialog() {
		
		// Set window boundary
		if (bounds.isEmpty()) {
			Utility.centerOnScreen(this);
		}
		else {
			setBounds(bounds);
		}
	}
	
	/**
	 * Save dialog
	 */
	private void saveDialog() {
		
		// Save window boundary
		bounds = getBounds();
	}
	
	/**
	 * Localize components
	 */
	private void localize() {
		
		// Set window title
		setTitle(Resources.getString("org.multipage.generator.textApplicationDebug"));
	}

	/**
	 * Set icons
	 */
	private void setIcons() {
		
		// Set main icon
		setIconImage(Images.getImage("org/multipage/basic/images/main_icon.png"));
	}

	/**
	 * Opens file for debugging using Xdebug command
	 * @param fileUri
	 * @throws Exception 
	 */
	public int openFile(String fileUri) {
		
		if (fileUri.isEmpty()) {
			return -1;
		}
		
		try {
			
			URI uri = new URI(fileUri);
			File sourceFile = new File(uri);
			if (sourceFile.exists() && sourceFile.canRead()) {
				
				BufferedReader br = new BufferedReader(new FileReader(sourceFile));
				Obj<String> sourceCode = new Obj<String>("");
				
				Obj<Integer> lines = new Obj<Integer>(0);
				
				br.lines().forEach((String line) -> {
					sourceCode.ref += line + "\n";
					lines.ref++;
				});
				br.close();
				
				this.header = fileUri;
				
				openSource(header, sourceCode.ref);
				scriptFileName = fileUri;
				
				return lines.ref;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	/**
	 * Open source code
	 * @param header
	 * @param sourceCode
	 */
	public void openSource(String header, String sourceCode) {
		
		// Split the source code into lines
		String [] inputLines = sourceCode.split("\n");
		
		final Obj<Integer> lineNumber = new Obj<Integer>(0);
		final int lines = inputLines.length;

		codeLines = new LinkedList<String>();
		
		// Display source
		displaySourceCode(header, new DisplayCodeInterface() {
			
			@Override
			public String line() {
				
				if (lineNumber.ref >= lines) {
					return null;
				}
				String inputLine = inputLines[lineNumber.ref++];
				codeLines.addLast(inputLine);
				return inputLine;
			}
		});
		
		// Show the window
		setVisible(true);
	}

	/**
	 * Displays source code. The callback is utilized for miscellaneous code sources
	 * @param header 
	 * @param callbacks
	 */
	private void displaySourceCode(String header, DisplayCodeInterface callbacks) {
		
		final String tabulator = "&nbsp;&nbsp;&nbsp;&nbsp;";
		
		String code = "<html>"
				+ "<head>"
				+ "<style>"
				+ "body {"
				+ "		white-space:nowrap;"
				+ "}"
				+ "#header {"
				+ "		font-family: Monospaced;"
				+ "		background-color: #DDDDDD;"
				+ "		color: #FFFFFF;"
				+ "}"
				+ ".lino {"
				+ "		font-family: Monospaced;"
				+ "		background-color: #DDDDDD;"
				+ "		color: #FFFFFF;"
				+ "}"
				+ ".code {"
				+ "		font-family: Monospaced;"
				+ "}"
				+ "</style>"
				+ "</head>"
				+ "<body>";
		
		// Display header
		if (header != null) {
			code += String.format("<div id='header'><center>%s</center></div>", header);
		}
		
		// Display lines
		String inputLine;
		int lineNumber = 1;
		
        for (;;) {
        	
        	Object returned = callbacks.line();
        	if (returned == null) {
        		break;
        	}
        	
        	inputLine = returned.toString();
        	inputLine = Utility.htmlSpecialChars(inputLine);
        	
			inputLine = inputLine.replaceAll("\\t", tabulator);
        	inputLine = inputLine.replaceAll("\\s", "&nbsp;");
        	String linoText = String.format("% 3d ", lineNumber);
        	linoText = linoText.replaceAll("\\s", "&nbsp;");
        	
    		code += String.format("<span class='lino'>%s</span>&nbsp;<span id='line%d' class='code'>%s</span><br>", linoText, lineNumber, inputLine);
    		lineNumber++;
        }

		code += "</body>"
				+ "</html>";
		
		// Display code (preserve scroll position)
		Point viewPosition = scrollCodePane.getViewport().getViewPosition();
		textCode.setText(code);
		SwingUtilities.invokeLater(() -> {
			scrollCodePane.getViewport().setViewPosition(viewPosition);
		});
	}
	
	/**
	 * Print message on console
	 * @param message
	 */
	private void consolePrint(String message) {
		
		String content = textInfo.getText() + message + "\r\n\r\n";
		textInfo.setText(content);
	}

	/**
	 * On send command to Xdebug server
	 */
	protected void onSendCommand() {
		
		String command = textCommand.getText();
		if (command.isEmpty()) {
			return;
		}
		try {
			XdebugPacket responsePacket = XdebugListener.getSingleton().postCommand(command);
			String resultText = responsePacket.getPacketText();
			consolePrint(resultText);
		}
		catch (Exception e) {
			consolePrint("");
			Utility.show2(this, e.getMessage());
		}
	}
	
	/**
	 * Shows reload alert depending on input parameter
	 * @param show
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	private void pageReloadException(boolean show) throws Exception {
		
		if (show) {
			Utility.show(this, "org.multipage.generator.messageReloadPageToStartDebugger");
			return;
		}
	}
	
	/**
	 * Starts debugging
	 */
	private void startDebugging() throws Exception {
		
		// Start debug viewer.
		boolean accepted = XdebugListener.getSingleton().startDebugging();
		//pageReloadException(!accepted);
	}
	
	/**
	 * Set viewer state
	 * @param debugging
	 * @throws Exception 
	 */
	private void setState(EditorState debugging) {
		
		try {
			setEditorCss(debugging.cssRule);
		}
		catch (Exception e) {
		}
	}

	/**
	 * On run till the end of the script
	 */
	protected void onRun() {
		
		// Process run command
		try {
			startDebugging();
			XdebugPacket responsePacket = XdebugListener.getSingleton().postCommand("run");
			String resultText = responsePacket.getPacketText();
			consolePrint(resultText);
		}
		catch (Exception e) {
		}
	}
	
	static int jlog = 1;
	private JPanel panelOutput;
	private JScrollPane scrollPane;
	private JTextArea textOutput;

	
	/**
	 * Do debugging step
	 */
	protected void step(String command) {
		
		// Process step into command
		try {
			startDebugging();
			XdebugPacket responsePacket = XdebugListener.getSingleton().postCommand(command);
			if (responsePacket.isEmpty()) {
				return;
			}
			String resultText = responsePacket.getPacketText();
			consolePrint(resultText);
			
			String filename = responsePacket.getString("/response/message/@filename");
			if (!filename.isEmpty() && !filename.equals(scriptFileName)) {
				openFile(filename);
			}
			
			String lineNumber = responsePacket.getString("/response/message/@lineno");
			if (!lineNumber.isEmpty()) {
				resetStepHighlight();
				
				final Color color = new Color(255, 255, 255);
				final Color bkColor = new Color(255, 0, 0);
				
				highlightCurrentStep(Integer.parseInt(lineNumber),  color, bkColor);
				
				listWatchModel.clear();
			}
			
			// Show output buffer content, stack dump and breakpoint context
			showOutput();
			showStackDump();
			processContext(3);
			processContext(1);
			
		}
		catch (Exception e) {
			j.log("Xdebug: " + e.getLocalizedMessage());
		}
	}
	
	/**
	 * Show output buffer
	 */
	private void showOutput() {
		
		try {
			XdebugPacket responsePacket = XdebugListener.getSingleton().postCommandT("eval", "ob_get_contents()");
			if (!responsePacket.isEmpty()) {
				String base64 = responsePacket.getString("/response/property/text()");
				if (base64 != null) {
					byte [] bytes = Utility.decodeBase64(base64);
					String valueText = new String(bytes);
					textOutput.setText(valueText);
				}
			}
		}
		catch (Exception e) {
			j.log(e.getMessage());
		}
	}

	/**
	 * Show stack dump
	 */
	private void showStackDump() {
		
		// Process run command
		try {
			XdebugPacket responsePacket = XdebugListener.getSingleton().postCommand("stack_get");
			if (!responsePacket.isEmpty()) {
				
				String resultText = responsePacket.getPacketText();
				consolePrint(resultText);
				
				// Clear output window
				listStackModel.clear();
				
				// Get stack items
				NodeList nodes = responsePacket.getNodes("/response/stack");
				if (nodes != null) {
					
					int count = nodes.getLength();
					for (int index = 0; index < count; index++) {
						
						Node node = nodes.item(index);
						if (node != null) {
							listStackModel.addElement(node);
						}
					}
				}
			}
		}
		catch (Exception e) {
			j.log(e.getMessage());
		}
	}
	
	/**
	 * Initialize stack dump window
	 */
	private void initStackDump() {
		
		listStackModel = new DefaultListModel<Node>();
		listStack.setModel(listStackModel);
		
		listStack.setCellRenderer(new ListCellRenderer<Node>() {
			
			// Set renderer.
			final RendererJLabel label = new RendererJLabel();

			@Override
			public Component getListCellRendererComponent(JList<? extends Node> list, Node node, int index,
					boolean isSelected, boolean cellHasFocus) {
				
				label.set(isSelected, cellHasFocus, index);
				
				DOM dom = DOM.use(node);
				
				String level = dom.attribute("level");
				String where = dom.attribute("where");
				String lineno = dom.attribute("lineno");
				String type = dom.attribute("type");
				String filename = dom.attribute("filename");
				
				label.setText(String.format(
						  "<html>"
						+ "<div style='margin: 3;'>"
						+ "<font size='16px'>"
						+ "%s <b>%s</b> on line <font color='#FF0000'><b>%s</b></font> in <font color='#FF0000'><b>%s</b></font> <font color='#CCCCCC'>(type %s)</font>"
						+ "</font>"
						+ "</div>"
						+ "</html>", level, where, lineno, filename, type));
				return label;
			}
		});
	}
	
	/**
	 * Initialize watch window
	 */
	private void initWatch() {
		
		listWatchModel = new DefaultListModel<String>();
		listWatch.setModel(listWatchModel);
		
		listWatch.setCellRenderer(new ListCellRenderer<String>() {
			
			// Set renderer.
			final RendererJLabel label = new RendererJLabel();

			@Override
			public Component getListCellRendererComponent(JList<? extends String> list, String text, int index,
					boolean isSelected, boolean cellHasFocus) {
				
				label.set(isSelected, cellHasFocus, index);
				
				label.setText(String.format(
						  "<html>"
						+ "<div style='margin: 3;'>"
						+ "<font size='16px'>"
						+ "%s"
						+ "</font>"
						+ "</div>"
						+ "</html>", text));
				return label;
			}
		});
	}

	/**
	 * Process context
	 */
	protected void processContext(int number) {
		
		// Process context_get command
		try {
			String command = String.format("context_get -c %s", number);
			XdebugPacket responsePacket = XdebugListener.getSingleton().postCommand(command);
			if (responsePacket.isEmpty()) {
				return;
			}
			
			NodeList properties = responsePacket.getNodes("/response/*");
			if (properties == null) {
				return;
			}
			
			for (int index = 0; index < properties.getLength(); index++) {
				
				Node property = properties.item(index);
				if (property == null) {
					return;
				}
				
				NamedNodeMap attributes = property.getAttributes();
				if (attributes == null) {
					return;
				}
				
				Node nameAttribute = attributes.getNamedItem("fullname");
				if (nameAttribute == null) {
					return;
				}
				
				String variableName = nameAttribute.getNodeValue();
				if (variableName == null) {
					return;
				}
				
				String type = responsePacket.getString(String.format("/response/property/@type", variableName));
				if (type != null) {
					listWatchModel.addElement(String.format("<b>%s</b> (%s)", variableName, type));
				}
				
				String value = "";
				if ("array".contentEquals(type)) {
					
					NodeList nodes = responsePacket.getNodes(String.format("/response/property[@fullname='%s']/*", variableName));
					if (nodes != null) {
						int length = nodes.getLength();
						
						for (int item = 0; item < length; item++) {
							
							Node node = nodes.item(item);
							if (node != null) {
								
								String valueText = node.getTextContent();
								valueText = new String(Utility.decodeBase64(valueText));
								
								Node nameNode = node.getAttributes().getNamedItem("name");
								if (nameNode != null) {
									String itemName = nameNode.getTextContent();
									
									listWatchModel.addElement(String.format("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<b>%s</b> => <font color='#FF0000'><b>%s</b></font>", itemName, valueText));
								}
							}
						}
					}
				}
				else if (!"uninitialized".contentEquals(type)) {
					
					value = responsePacket.getString(String.format("/response/property[@fullname='%s']/text()", variableName));
					listWatchModel.addElement(String.format("     = <font color='#FF0000'><b>%s</b></font>", value));
				}
			}
		}
		catch (Exception e) {
		}
	}

	/**
	 * On step into
	 */
	protected void onStepInto() {
		
		// Process step into command
		step("step_into");
	}

	/**
	 * On step out
	 */
	protected void onStepOut() {
		
		// Process step into command
		step("step_out");
	}

	/**
	 * On step over
	 */
	protected void onStepOver() {
		
		// Process step into command
		step("step_over");
	}
	
	/**
	 * Reset highlights
	 * @throws Exception 
	 */
	private void resetLastStepHighlight() {
		
		// TODO: test
		showOutput();
		resetStepHighlight();
		
	}
	
	/**
	 * Reset highlights
	 * @throws Exception 
	 */
	private void resetStepHighlight() {
		
		try {
			
			HTMLDocument document = (HTMLDocument) textCode.getDocument();
			StyleSheet documentCss = document.getStyleSheet();
			
			if (stepLineNumber >= 0) {
				String cssRule = String.format("#line%d {color: #000000; background-color: #FFFFFF;}", stepLineNumber);
				documentCss.addRule(cssRule);
				
				stepLineNumber = -1;
			}
		}
		catch (Exception e) {
		}
	}
	
	/**
	 * Highlight given code line
	 * @param lineNumber
	 * @param bkColor 
	 */
	private void highlightCurrentStep(int lineNumber, Color color, Color bkColor) throws Exception {
		
		resetStepHighlight();
		
		if (lineNumber > 0 && color != null && bkColor != null) {
			
			String elementId = String.format("line%s", lineNumber);
			
			String foregroundColor = Utility.getCssColor(color);
			String backgroundColor = Utility.getCssColor(bkColor);
			
			String cssRule = String.format("#%s {color: %s; background-color: %s;}", elementId, foregroundColor, backgroundColor);
			setEditorCss(cssRule);
		}
				
		stepLineNumber = lineNumber;
	}
	
	/**
	 * Sets CSS rule for code editor
	 * @param rule
	 */
	private void setEditorCss(String rule) throws Exception {
		
		HTMLDocument document = (HTMLDocument) textCode.getDocument();
		StyleSheet documentCss = document.getStyleSheet();
		documentCss.addRule(rule);
	}

	/**
	 * Updates source code view
	 */
	private void updateSourceCodeView() {
		
		if (codeLines == null || codeLines.isEmpty()) {
			return;
		}
		
		final Obj<Integer> lineIdex = new Obj<Integer>(0);
		final int count = codeLines.size();
		
		displaySourceCode(header, new DisplayCodeInterface() {
			@Override
			public String line() {
				
				if (lineIdex.ref >= count) {
					return null;
				}
				return codeLines.get(lineIdex.ref++);
			}
		});
	}

	/**
	 * On exit
	 */
	protected void onExit() {
		
		XdebugListener client = XdebugListener.getSingleton();
		if (client == null) {
			return;
		}
		
		// Process stop command
		client.stopSession();
	}

	/**
	 * A watch dog that scans Xdebug status
	 */
	private void establishWatchDog() {
		
		XdebugListener client = XdebugListener.getSingleton();
		if (client == null) {
			return;
		}

		client.setWatchDogCallback(new Callback() {
			@Override
			public Object run(Object status) {
				
				// Process Xdebug status
				SwingUtilities.invokeLater(() -> {
					
					processStatus(status);
					labelStatus.setText(status.toString());
				});
				return null;
			}
		});
	}
	
	/**
	 * Sets callbacks
	 */
	private void setCallbacks() {
		
		XdebugListener client = XdebugListener.getSingleton();
		if (client == null) {
			return;
		}
		
		client.setNewSessionCallback(new Callback() {
			@Override
			public Object run(Object input) {
				SwingUtilities.invokeLater(() -> {
					step("step_into");
				});
				return null;
			}
			
		});
	}
	
	/**
	 * Processes Xdebug server status
	 * @param status 
	 */
	protected void processStatus(Object status) {
		
		// If the status doesn't change, do nothing
		if (this.status == status) {
			return;
		}
		
		// Status actions
		if ("stopping".equals(status) || "no connection".equals(status) ||
			"disconnected".equals(status) || "connection breakdown".equals(status)) {
			resetLastStepHighlight();
		}
		else {
			// Set editor state
			setState(EditorState.debugging);
		}
		
		this.status = status;
		
		setViewerState();
	}
	
	/**
	 * Set viewer state
	 */
	private void setViewerState() {
		
		if ("no connection".equals(status) ||
			"disconnected".equals(status) || 
			"connection breakdown".equals(status)) {
			setState(EditorState.initial);
		}
		else {
			setState(EditorState.debugging);
		}
	}

	/**
	 * Show user alert
	 * @param message
	 * @param timeout 
	 */
	public void showUserAlert(String message, int timeout) {
		
		SwingUtilities.invokeLater(() -> {
			AlertWithTimeout.showDialog(this, message, timeout);
		});
	}
}
