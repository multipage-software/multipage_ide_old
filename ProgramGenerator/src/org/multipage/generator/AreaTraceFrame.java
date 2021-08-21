/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.function.Consumer;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.maclan.Area;
import org.maclan.AreasModel;
import org.multipage.gui.DefaultMutableTreeNodeDnD;
import org.multipage.gui.GraphUtility;
import org.multipage.gui.Images;
import org.multipage.gui.JTreeDnD;
import org.multipage.gui.JTreeDndCallback;
import org.multipage.gui.TextPaneEx;
import org.multipage.gui.ToolBarKit;
import org.multipage.gui.Utility;
import org.multipage.util.Obj;
import org.multipage.util.Resources;

/**
 * 
 * @author
 *
 */
public class AreaTraceFrame extends JFrame {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * States.
	 */
	private static boolean loadSubAreasState = true;
	private static boolean loadDescriptionsState = true;
	private static int selectedTabIndexState = 0;
	private static boolean inheritState = false;
	private static boolean showIdsState = false;
	private static boolean caseSensitiveState = false;
	private static boolean wholeWordsState = false;
	private static boolean exactMatchState = false;
	private static String filterState = "";
	private static String levelsState = "";
	private static Rectangle bounds = new Rectangle();
	private static int splitterPosition = -1;
	private static int splitter2Position = -1;
	
	/**
	 * Load state.
	 * @param inputStream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void serializeData(ObjectInputStream inputStream)
			throws IOException, ClassNotFoundException {
		
		loadSubAreasState = inputStream.readBoolean();
		loadDescriptionsState = inputStream.readBoolean();
		selectedTabIndexState = inputStream.readInt();
		inheritState = inputStream.readBoolean();
		showIdsState = inputStream.readBoolean();
		caseSensitiveState = inputStream.readBoolean();
		wholeWordsState = inputStream.readBoolean();
		exactMatchState = inputStream.readBoolean();
		filterState = inputStream.readUTF();
		levelsState = inputStream.readUTF();
		
		Object object = inputStream.readObject();
		if (!(object instanceof Rectangle)) {
			throw new ClassNotFoundException();
		}
		bounds = (Rectangle) object;
		
		splitterPosition = inputStream.readInt();
		splitter2Position = inputStream.readInt();
	}

	/**
	 * Save state.
	 * @param outputStream
	 * @throws IOException
	 */
	public static void serializeData(ObjectOutputStream outputStream)
			throws IOException {
		
		outputStream.writeBoolean(loadSubAreasState);
		outputStream.writeBoolean(loadDescriptionsState);
		outputStream.writeInt(selectedTabIndexState);
		outputStream.writeBoolean(inheritState);
		outputStream.writeBoolean(showIdsState);
		outputStream.writeBoolean(caseSensitiveState);
		outputStream.writeBoolean(wholeWordsState);
		outputStream.writeBoolean(exactMatchState);
		outputStream.writeUTF(filterState);
		outputStream.writeUTF(levelsState);
		
		outputStream.writeObject(bounds);
		
		outputStream.writeInt(splitterPosition);
		outputStream.writeInt(splitter2Position);
	}
	
	/**
	 * Created frames
	 */
	private static LinkedList<AreaTraceFrame> frames;
	
	/**
	 * Enable events
	 */
	private static boolean enabledEvents = true;
	
	/**
	 * List renderer.
	 * @author
	 *
	 */
	@SuppressWarnings("serial")
	class ItemRendererImpl extends JLabel {

		private boolean isSelected;
		private boolean cellHasFocus;
		private boolean isVisible = false;
		
		ItemRendererImpl() {
			
			setOpaque(true);
		}
		
		public void setProperties(String text, String subName, String superName, boolean hiddenSubareas, int index,
				boolean isSelected, boolean cellHasFocus, boolean isHomeArea) {

			setIcon(Images.getIcon(
					isHomeArea ? (isVisible ? "org/multipage/generator/images/home_icon_small.png"
							: "org/multipage/generator/images/home_icon_small_unvisible.png")
							: (isVisible ? "org/multipage/generator/images/area_node.png"
									: "org/multipage/generator/images/area_node_unvisible.png")));
			
			if (text.isEmpty()) {
				text = Resources.getString("org.multipage.generator.textUnknownAlias");
				setForeground(Color.LIGHT_GRAY);
			}
			else {
				setForeground(Color.BLACK);
			}
			
			//String outputText = "<b>" + text + "</b>";
			String outputText = text;
			String outputTextAddition = "";
			
			final boolean isBuilder = ProgramGenerator.isExtensionToBuilder();
			
			if (!subName.isEmpty()) {
				outputTextAddition += " <sup>↓</sup> <font color=gray>" + subName + "</font>";
			}
			if (!superName.isEmpty() && isBuilder) {
				outputTextAddition += " <sup>↑</sup> <font color=gray>" + superName + "</font>";
			}
			if (hiddenSubareas && isBuilder) {
				String textAux = Resources.getString("org.multipage.generator.textHasMoreInfo");
				outputTextAddition += String.format(" <font color=\"red\", style=\"font-size: 70%%\">%s</font>",
						textAux);
			}
			setText(String.format("<html>%s&nbsp;&nbsp;%s</html>", outputText, outputTextAddition));
			setBackground(Utility.itemColor(index));
			this.isSelected = isSelected;
			this.cellHasFocus = cellHasFocus;
		}
		
		public void setProperties(String text, int index,
				boolean isSelected, boolean cellHasFocus, boolean isHomeArea) {

			setIcon(Images.getIcon(isHomeArea ? "org/multipage/generator/images/home_icon_small.png" : "org/multipage/generator/images/area_node.png"));

			if (text.isEmpty()) {
				text = Resources.getString("org.multipage.generator.textUnknownAlias");
				setForeground(Color.LIGHT_GRAY);
			}
			else {
				setForeground(Color.BLACK);
			}
			setText(text);
			setBackground(Utility.itemColor(index));
			this.isSelected = isSelected;
			this.cellHasFocus = cellHasFocus;
		}
		@Override
		public void paint(Graphics g) {
			
			super.paint(g);
			GraphUtility.drawSelection(g, this, isSelected, cellHasFocus);
		}

		public void setAreaVisible(boolean isVisible) {
			
			this.isVisible = isVisible;
		}
	}
		
	/**
	 * Created frames.
	 */
	private static LinkedList<AreaTraceFrame> createdFrames = 
		new LinkedList<AreaTraceFrame>();
	
	/**
	 * Update information.
	 */
	public static void updateInformation() {
		
		for (AreaTraceFrame frame : createdFrames) {
			frame.reload();
		}
	}

	/**
	 * Area ID.
	 */
	private long areaId;

	/**
	 * List renderer.
	 */
	private ItemRendererImpl itemRenderer;

	/**
	 * List model.
	 */
	private DefaultListModel listModel;
	
	/**
	 * Tree model.
	 */
	private DefaultTreeModel treeModel;
	
	/**
	 * Areas properties reference.
	 */
	private AreasPropertiesBase areasPropertiesPanel;
	
	/**
	 * MessagePanel label.
	 */
	private MessagePanel panelMessage;
	
	/**
	 * Toggle debug.
	 */
	private JToggleButton toggleDebug;
		
	// $hide<<$
	/**
	 * Components.
	 */
	private JRadioButton radioSubAreas;
	private JRadioButton radioSuperAreas;
	private JRadioButton radioDescriptions;
	private JRadioButton radioAliases;
	private JCheckBox checkInherits;
	private JButton buttonClose;
	private final ButtonGroup buttonGroupAreas = new ButtonGroup();
	private final ButtonGroup buttonGroupText = new ButtonGroup();
	private JTabbedPane tabbedPane;
	private JPanel panelList;
	private JPanel panelTree;
	private JButton buttonReload;
	private JLabel labelFilter;
	private JTextField textFilter;
	private JCheckBox checkCaseSensitive;
	private JCheckBox checkWholeWords;
	private JCheckBox checkExactMatch;
	private JScrollPane scrollList;
	private JList list;
	private JScrollPane scrollTree;
	private JTreeDnD tree;
	private JLabel labelLevels;
	private JTextField textLevels;
	private JLabel labelFoundAreasCount;
	private JToolBar toolBarTree;
	private JPopupMenu popupMenuList;
	private JPopupMenu popupMenuTree;
	private JCheckBox checkShowIds;
	private JMenuItem menuSelectSubNodes;
	private JSplitPane splitPane;
	private JMenuItem menuAddSubArea;
	private JMenuItem menuRemoveArea;
	private JToolBar toolBarMain;
	private JSplitPane splitPaneProviders;
	private JScrollPane scrollPane;
	private JEditorPane editorSlotPreview;

	/**
	 * Show new frame.
	 * @param areaId
	 */
	public static void showNewFrame(long areaId) {
		
		AreaTraceFrame frame = new AreaTraceFrame(areaId);
		
		createdFrames.add(frame);

		frame.reload();
		frame.setVisible(true);
	}

	/**
	 * Create the frame.
	 * @param areaId 
	 */
	public AreaTraceFrame(long areaId) {
		setMinimumSize(new Dimension(400, 350));

		// Initialize components.
		initComponents();
		// $hide>>$
		this.areaId = areaId;
		postCreate();
		// $hide<<$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onCloseWindow();
			}
		});
		setBounds(100, 100, 693, 540);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		SpringLayout springLayout = new SpringLayout();
		getContentPane().setLayout(springLayout);
		
		radioSubAreas = new JRadioButton("org.multipage.generator.textSubareas");
		springLayout.putConstraint(SpringLayout.NORTH, radioSubAreas, 6, SpringLayout.NORTH, getContentPane());
		radioSubAreas.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reload();
			}
		});
		buttonGroupText.add(radioSubAreas);
		springLayout.putConstraint(SpringLayout.WEST, radioSubAreas, 10, SpringLayout.WEST, getContentPane());
		radioSubAreas.setSelected(true);
		getContentPane().add(radioSubAreas);
		
		radioSuperAreas = new JRadioButton("org.multipage.generator.textSuperareas");
		springLayout.putConstraint(SpringLayout.NORTH, radioSuperAreas, 0, SpringLayout.NORTH, radioSubAreas);
		springLayout.putConstraint(SpringLayout.WEST, radioSuperAreas, 10, SpringLayout.EAST, radioSubAreas);
		radioSuperAreas.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reload();
			}
		});
		buttonGroupText.add(radioSuperAreas);
		getContentPane().add(radioSuperAreas);
		
		radioDescriptions = new JRadioButton("org.multipage.generator.textDescriptions");
		springLayout.putConstraint(SpringLayout.NORTH, radioDescriptions, 0, SpringLayout.SOUTH, radioSubAreas);
		radioDescriptions.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reload();
			}
		});
		buttonGroupAreas.add(radioDescriptions);
		springLayout.putConstraint(SpringLayout.WEST, radioDescriptions, 0, SpringLayout.WEST, radioSubAreas);
		radioDescriptions.setSelected(true);
		radioDescriptions.setOpaque(false);
		getContentPane().add(radioDescriptions);
		
		radioAliases = new JRadioButton("org.multipage.generator.textAliases");
		springLayout.putConstraint(SpringLayout.NORTH, radioAliases, 0, SpringLayout.SOUTH, radioSuperAreas);
		springLayout.putConstraint(SpringLayout.WEST, radioAliases, 0, SpringLayout.WEST, radioSuperAreas);
		radioAliases.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reload();
			}
		});
		buttonGroupAreas.add(radioAliases);
		radioAliases.setOpaque(false);
		getContentPane().add(radioAliases);
		
		checkInherits = new JCheckBox("org.multipage.generator.textInherits");
		springLayout.putConstraint(SpringLayout.WEST, checkInherits, 10, SpringLayout.EAST, radioSuperAreas);
		checkInherits.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reload();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, checkInherits, 0, SpringLayout.NORTH, radioSubAreas);
		checkInherits.setOpaque(false);
		getContentPane().add(checkInherits);
		
		buttonClose = new JButton("textClose");
		springLayout.putConstraint(SpringLayout.SOUTH, buttonClose, -6, SpringLayout.SOUTH, getContentPane());
		buttonClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCloseWindow();
			}
		});
		springLayout.putConstraint(SpringLayout.EAST, buttonClose, -10, SpringLayout.EAST, getContentPane());
		buttonClose.setPreferredSize(new Dimension(80, 25));
		buttonClose.setMargin(new Insets(0, 0, 0, 0));
		getContentPane().add(buttonClose);
		
		buttonReload = new JButton("org.multipage.generator.textReload");
		buttonReload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reload();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, buttonReload, 0, SpringLayout.NORTH, buttonClose);
		springLayout.putConstraint(SpringLayout.WEST, buttonReload, 0, SpringLayout.WEST, radioSubAreas);
		buttonReload.setPreferredSize(new Dimension(80, 25));
		buttonReload.setMargin(new Insets(0, 0, 0, 0));
		getContentPane().add(buttonReload);
		
		checkShowIds = new JCheckBox("org.multipage.generator.textShowIds");
		checkShowIds.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reload();
			}
		});
		springLayout.putConstraint(SpringLayout.WEST, checkShowIds, 0, SpringLayout.WEST, checkInherits);
		springLayout.putConstraint(SpringLayout.SOUTH, checkShowIds, 0, SpringLayout.SOUTH, radioDescriptions);
		getContentPane().add(checkShowIds);
		
		splitPane = new JSplitPane();
		splitPane.setBorder(null);
		springLayout.putConstraint(SpringLayout.NORTH, splitPane, 6, SpringLayout.SOUTH, checkShowIds);
		springLayout.putConstraint(SpringLayout.WEST, splitPane, 0, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, splitPane, -6, SpringLayout.NORTH, buttonClose);
		springLayout.putConstraint(SpringLayout.EAST, splitPane, 0, SpringLayout.EAST, getContentPane());
		splitPane.setResizeWeight(0.5);
		splitPane.setOneTouchExpandable(true);
		getContentPane().add(splitPane);
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		splitPane.setLeftComponent(tabbedPane);
		springLayout.putConstraint(SpringLayout.NORTH, tabbedPane, 6, SpringLayout.SOUTH, radioDescriptions);
		springLayout.putConstraint(SpringLayout.EAST, tabbedPane, 0, SpringLayout.EAST, radioAliases);
		tabbedPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						reload();
					}
				});
			}
		});
		springLayout.putConstraint(SpringLayout.WEST, tabbedPane, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, tabbedPane, -6, SpringLayout.NORTH, buttonClose);
		
		panelTree = new JPanel();
		tabbedPane.addTab("tree", null, panelTree, null);
		SpringLayout sl_panelTree = new SpringLayout();
		panelTree.setLayout(sl_panelTree);
		
		scrollTree = new JScrollPane();
		scrollTree.setBorder(null);
		sl_panelTree.putConstraint(SpringLayout.NORTH, scrollTree, 0, SpringLayout.NORTH, panelTree);
		sl_panelTree.putConstraint(SpringLayout.WEST, scrollTree, 0, SpringLayout.WEST, panelTree);
		sl_panelTree.putConstraint(SpringLayout.EAST, scrollTree, 0, SpringLayout.EAST, panelTree);
		panelTree.add(scrollTree);
		
		tree = new JTreeDnD();
		tree.setBorder(null);
		scrollTree.setViewportView(tree);
		
		popupMenuTree = new JPopupMenu();
		addPopup(tree, popupMenuTree);
		
		menuSelectSubNodes = new JMenuItem("org.multipage.generator.menuSelectSubNodes");
		menuSelectSubNodes.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectNodeWithSubNodes();
			}
		});
		popupMenuTree.add(menuSelectSubNodes);
		
		menuAddSubArea = new JMenuItem("org.multipage.generator.menuAddSubArea");
		menuAddSubArea.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onAddSubArea();
			}
		});
		popupMenuTree.add(menuAddSubArea);
		
		menuRemoveArea = new JMenuItem("org.multipage.generator.menuRemoveArea");
		menuRemoveArea.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onRemoveArea();
			}
		});
		popupMenuTree.add(menuRemoveArea);
		
		popupMenuTree.addSeparator();
		
		toolBarTree = new JToolBar();
		sl_panelTree.putConstraint(SpringLayout.SOUTH, scrollTree, 0, SpringLayout.NORTH, toolBarTree);
		sl_panelTree.putConstraint(SpringLayout.WEST, toolBarTree, 0, SpringLayout.WEST, panelTree);
		sl_panelTree.putConstraint(SpringLayout.SOUTH, toolBarTree, 0, SpringLayout.SOUTH, panelTree);
		toolBarTree.setFloatable(false);
		panelTree.add(toolBarTree);
		
		panelList = new JPanel();
		tabbedPane.addTab("org.multipage.generator.textSearchAreas", null, panelList, null);
		SpringLayout sl_panelList = new SpringLayout();
		panelList.setLayout(sl_panelList);
		
		labelFilter = new JLabel("org.multipage.generator.textFilter");
		sl_panelList.putConstraint(SpringLayout.NORTH, labelFilter, 10, SpringLayout.NORTH, panelList);
		sl_panelList.putConstraint(SpringLayout.WEST, labelFilter, 10, SpringLayout.WEST, panelList);
		panelList.add(labelFilter);
		
		textFilter = new JTextField();
		sl_panelList.putConstraint(SpringLayout.NORTH, textFilter, 10, SpringLayout.NORTH, panelList);
		sl_panelList.putConstraint(SpringLayout.WEST, textFilter, 6, SpringLayout.EAST, labelFilter);
		sl_panelList.putConstraint(SpringLayout.EAST, textFilter, 197, SpringLayout.EAST, labelFilter);
		panelList.add(textFilter);
		textFilter.setColumns(10);
		
		checkCaseSensitive = new JCheckBox("org.multipage.generator.textCaseSensitive");
		sl_panelList.putConstraint(SpringLayout.NORTH, checkCaseSensitive, 0, SpringLayout.SOUTH, textFilter);
		checkCaseSensitive.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reload();
			}
		});
		sl_panelList.putConstraint(SpringLayout.WEST, checkCaseSensitive, 0, SpringLayout.WEST, labelFilter);
		panelList.add(checkCaseSensitive);
		
		checkWholeWords = new JCheckBox("org.multipage.generator.textWholeWords");
		sl_panelList.putConstraint(SpringLayout.NORTH, checkWholeWords, 0, SpringLayout.NORTH, checkCaseSensitive);
		checkWholeWords.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reload();
			}
		});
		sl_panelList.putConstraint(SpringLayout.WEST, checkWholeWords, 6, SpringLayout.EAST, checkCaseSensitive);
		panelList.add(checkWholeWords);
		
		checkExactMatch = new JCheckBox("org.multipage.generator.textExactMatch");
		sl_panelList.putConstraint(SpringLayout.NORTH, checkExactMatch, 0, SpringLayout.NORTH, checkCaseSensitive);
		checkExactMatch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reload();
			}
		});
		sl_panelList.putConstraint(SpringLayout.WEST, checkExactMatch, 6, SpringLayout.EAST, checkWholeWords);
		panelList.add(checkExactMatch);
		
		scrollList = new JScrollPane();
		sl_panelList.putConstraint(SpringLayout.NORTH, scrollList, 0, SpringLayout.SOUTH, checkCaseSensitive);
		sl_panelList.putConstraint(SpringLayout.WEST, scrollList, 0, SpringLayout.WEST, panelList);
		sl_panelList.putConstraint(SpringLayout.EAST, scrollList, 0, SpringLayout.EAST, panelList);
		panelList.add(scrollList);
		
		list = new JList();
		scrollList.setViewportView(list);
		
		popupMenuList = new JPopupMenu();
		addPopup(list, popupMenuList);
		
		labelLevels = new JLabel("org.multipage.generator.textLevels");
		sl_panelList.putConstraint(SpringLayout.NORTH, labelLevels, 0, SpringLayout.NORTH, labelFilter);
		sl_panelList.putConstraint(SpringLayout.WEST, labelLevels, 6, SpringLayout.EAST, textFilter);
		panelList.add(labelLevels);
		
		textLevels = new JTextField();
		sl_panelList.putConstraint(SpringLayout.NORTH, textLevels, 0, SpringLayout.NORTH, labelFilter);
		sl_panelList.putConstraint(SpringLayout.WEST, textLevels, 6, SpringLayout.EAST, labelLevels);
		sl_panelList.putConstraint(SpringLayout.EAST, textLevels, 51, SpringLayout.EAST, labelLevels);
		textLevels.setColumns(10);
		panelList.add(textLevels);
		
		labelFoundAreasCount = new JLabel("org.multipage.generator.textFoundAreasCount");
		sl_panelList.putConstraint(SpringLayout.SOUTH, scrollList, 0, SpringLayout.NORTH, labelFoundAreasCount);
		sl_panelList.putConstraint(SpringLayout.WEST, labelFoundAreasCount, 0, SpringLayout.WEST, panelList);
		sl_panelList.putConstraint(SpringLayout.SOUTH, labelFoundAreasCount, 0, SpringLayout.SOUTH, panelList);
		panelList.add(labelFoundAreasCount);
		{
			splitPaneProviders = new JSplitPane();
			splitPaneProviders.setBorder(null);
			splitPaneProviders.setResizeWeight(0.6);
			splitPaneProviders.setOrientation(JSplitPane.VERTICAL_SPLIT);
			splitPane.setRightComponent(splitPaneProviders);
			{
				scrollPane = new JScrollPane();
				scrollPane.setBorder(null);
				splitPaneProviders.setRightComponent(scrollPane);
				{
					editorSlotPreview = new TextPaneEx();
					editorSlotPreview.setBorder(null);
					editorSlotPreview.setFont(new Font("DialogInput", Font.PLAIN, 12));
					editorSlotPreview.setEditable(false);
					scrollPane.setViewportView(editorSlotPreview);
				}
			}
		}
		
		toolBarMain = new JToolBar();
		springLayout.putConstraint(SpringLayout.NORTH, toolBarMain, 12, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, toolBarMain, -6, SpringLayout.EAST, getContentPane());
		toolBarMain.setFloatable(false);
		getContentPane().add(toolBarMain);
	}

	/**
	 * On remove area.
	 */
	protected void onRemoveArea() {
		
		// Get selected area.
		TreePath [] selectedPaths = tree.getSelectionPaths();
		if (selectedPaths.length != 1) {
			Utility.show(this, "org.multipage.generator.messageSelectSingleArea");
			return;
		}
		
		// Get parent area.
		TreePath path = selectedPaths[0];
		int elementsCount = path.getPathCount();
		if (elementsCount < 2) {
			Utility.show(this, "org.multipage.generator.messageCannotRemoveRootArea");
			return;
		}
		
		DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) path.getPathComponent(elementsCount - 2);
		Area parentArea = (Area) parentNode.getUserObject();
		
		// Get selected area.
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
		Area area = (Area) node.getUserObject();
		
		AreaShapes areaShapes = (AreaShapes) area.getUser();
		HashSet<AreaShapes> shapesSet = new HashSet<AreaShapes>();
		shapesSet.add(areaShapes);
		
		// Remove area.
		GeneratorMainFrame.getVisibleAreasDiagram().removeDiagramArea(shapesSet, parentArea, this);
	}

	/**
	 * On add sub area.
	 */
	protected void onAddSubArea() {
		
		// Get selected area.
		TreePath [] selectedPaths = tree.getSelectionPaths();
		if (selectedPaths.length != 1) {
			Utility.show(this, "org.multipage.generator.messageSelectSingleArea");
			return;
		}
		
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
		Area parentArea = (Area) node.getUserObject();
		
		// Add new area.
		Obj<Area> newArea = new Obj<Area>();
		if (GeneratorMainFrame.getVisibleAreasDiagram().addNewArea(parentArea, this, newArea, false)) {
		
			// Select and expand area item.
			if (newArea.ref != null) {
				SwingUtilities.invokeLater(() -> {
					
					AreaTreeState.addSelectedAndExpanded(tree, selectedPaths);
					reload();
				});
			}
		}
	}
	
	/**
	 * Select area with sub nodes.
	 */
	protected void selectNodeWithSubNodes() {
		
		// Get selected area.
		TreePath [] selectedPaths = tree.getSelectionPaths();
		if (selectedPaths.length != 1) {
			Utility.show(this, "org.multipage.generator.messageSelectSingleArea");
			return;
		}
		
		TreePath treePath = selectedPaths[0];
		LinkedList<TreePath> treePaths = new LinkedList<TreePath>();
		
		getSubPaths((DefaultMutableTreeNode) treePath.getLastPathComponent(), treePaths);
		
		// Select sub nodes.
		tree.setSelectionPaths(treePaths.toArray(new TreePath [0]));
	}

	/**
	 * Get sun paths.
	 * @param node
	 * @param treePaths
	 */
	private void getSubPaths(DefaultMutableTreeNode node,
			LinkedList<TreePath> treePaths) {
		
		// Add this node path.
		TreeNode [] nodePath = node.getPath();
		TreePath treePath = new TreePath(nodePath);
		treePaths.add(treePath);
		
		// Do loop for all sub nodes.		
		Enumeration<? super TreeNode> childNodes = node.children();
		while (childNodes.hasMoreElements()) {
			
			DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) childNodes.nextElement();
			
			// Call this method recursively.
			getSubPaths(childNode, treePaths);
		}
	}

	/**
	 * Returns true value if the IDs have to be visible.
	 * @return
	 */
	private boolean showIds() {
		
		return checkShowIds.isSelected();
	}
	
	/**
	 * Post create.
	 */
	private void postCreate() {
		
		// Create and set areas properties panel.
		areasPropertiesPanel = ProgramGenerator.newAreasProperties(true);
		panelMessage = new MessagePanel();
		
		setNoAreasSelectedMessage();
		
		// Load dialog.
		loadDialog();
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		// Localize components.
		localize();
		// Set icons.
		setIcons();
		// Create tool bar.
		createToolBars();
		// Create list.
		createList();
		// Create tree.
		createTree();
		// Set listeners.
		setListeners();
		// Create popup menus.
		createPopupMenus();
		// Switch on or off debugging of PHP code
		boolean selected = Settings.getEnableDebugging();
		toggleDebug.setSelected(selected);
		
		setSlotSelectionListener();
		
		// Remember this frame
		if (frames == null) {
			frames = new LinkedList<AreaTraceFrame>();
		}
		frames.add(this);
	}

	/**
	 * Set slot selection listener
	 */
	private void setSlotSelectionListener() {
		
		areasPropertiesPanel.setSlotSelectedEvent((slot) -> {
			
			editorSlotPreview.setText(slot.getTextValue());
			editorSlotPreview.setCaretPosition(0);
		});
	}

	/**
	 * Set no areas selected message.
	 */
	private void setNoAreasSelectedMessage() {
		
		panelMessage.setText(Resources.getString("org.multipage.generator.textNoAreaSelected"));
		splitPaneProviders.setLeftComponent(panelMessage);
		editorSlotPreview.setText("");
	}

	/**
	 * Load dialog.
	 */
	private void loadDialog() {
		
		if (loadDescriptionsState) {
			radioDescriptions.setSelected(true);
		}
		else {
			radioAliases.setSelected(true);
		}
		
		if (loadSubAreasState) {
			radioSubAreas.setSelected(true);
		}
		else {
			radioSuperAreas.setSelected(true);
		}
		
		tabbedPane.setSelectedIndex(selectedTabIndexState);
		
		checkInherits.setSelected(inheritState);
		checkShowIds.setSelected(showIdsState);
		checkCaseSensitive.setSelected(caseSensitiveState);
		checkWholeWords.setSelected(wholeWordsState);
		checkExactMatch.setSelected(exactMatchState);
		textFilter.setText(filterState);
		textLevels.setText(levelsState);
		
		if (bounds.isEmpty()) {
			// Center dialog.
			Utility.centerOnScreen(this);
			bounds = getBounds();
		}
		else {
			setBounds(bounds);
		}
		
		if (splitterPosition != -1) {
			splitPane.setDividerLocation(splitterPosition);
		}
		if (splitter2Position != -1) {
			splitPaneProviders.setDividerLocation(splitter2Position);
		}
	}

	/**
	 * Create popup menus.
	 */
	private void createPopupMenus() {
		
		// Create new area trayMenu.
		final Component thisComponent = this;
		
		// Create new area trayMenu.
		AreaLocalMenu areaMenuList = ProgramGenerator.newAreaLocalMenu(new AreaLocalMenuListener() {
			@Override
			protected Area getCurrentArea() {
				// Get selected area.
				Object [] selected = list.getSelectedValues();
				if (selected.length != 1) {
					return null;
				}
				Area area = (Area) selected[0];
				return ProgramGenerator.getAreasModel().getArea(area.getId());
			}

			@Override
			public Component getComponent() {
				// Get this component.
				return thisComponent;
			}
		});
		
		AreaLocalMenu areaMenuTree = ProgramGenerator.newAreaLocalMenu(new AreaLocalMenuListener() {
			@Override
			protected Area getCurrentArea() {
				// Get selected area.
				TreePath [] selectedPaths = tree.getSelectionPaths();
				if (selectedPaths.length != 1) {
					return null;
				}
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectedPaths[0].getLastPathComponent();
				Area area = (Area) node.getUserObject();
				return ProgramGenerator.getAreasModel().getArea(area.getId());
			}

			@Override
			public Area getCurrentParentArea() {
				
				// Get selected area and its parent.
				TreePath [] selectedPaths = tree.getSelectionPaths();
				if (selectedPaths.length != 1) {
					return null;
				}
				
				TreePath selectedPath = selectedPaths[0];
				int elementsCount = selectedPath.getPathCount();
				
				if (elementsCount < 2) {
					return null;
				}

				DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) selectedPath.getPathComponent(elementsCount - 2);
				Area parentArea = (Area) parentNode.getUserObject();
				return ProgramGenerator.getAreasModel().getArea(parentArea.getId());
			}

			@Override
			public Component getComponent() {
				// Get this component.
				return thisComponent;
			}

			@Override
			public void onNewArea(Long newAreaId) {
				
				if (newAreaId == null) {
					return;
				}

				// Select new area (imported).
				GeneratorMainFrame.getVisibleAreasDiagram().removeSelection();
				GeneratorMainFrame.getVisibleAreasDiagram().select(newAreaId, true, false);
				
				reload();
			}
		});
		// Add new trayMenu items.
		areaMenuList.addTo(this, popupMenuList);
		areaMenuTree.addTo(this, popupMenuTree);
	}

	/**
	 * Create tool bar.
	 */
	private void createToolBars() {
		
		// Main tool bar.
		toggleDebug = ToolBarKit.addToggleButton(toolBarMain,  "org/multipage/generator/images/debug.png", this, "onToggleDebug", "org.multipage.generator.tooltipEnableDisplaySourceCode");
		ToolBarKit.addToolBarButton(toolBarMain, "org/multipage/generator/images/reload_icon.png", this, "onUpdate", "org.multipage.generator.tooltipUpdateData");
		toolBarMain.addSeparator();
		ToolBarKit.addToolBarButton(toolBarMain, "org/multipage/generator/images/render.png", this, "onRender", "org.multipage.generator.tooltipRenderHtmlPages");
		toolBarMain.addSeparator();
		ToolBarKit.addToolBarButton(toolBarMain, "org/multipage/generator/images/display_home_page.png", this, "onDisplayHomePage", "org.multipage.generator.tooltipDisplayHomePage");
		
		// Area tree tool bar.
		ToolBarKit.addToolBarButton(toolBarTree, "org/multipage/generator/images/expand_icon.png", this, "onExpandTree", "org.multipage.generator.tooltipExpandTree");
		ToolBarKit.addToolBarButton(toolBarTree, "org/multipage/generator/images/collapse_icon.png", this, "onCollapseTree", "org.multipage.generator.tooltipCollapseTree");
	}
	
	/**
	 * On update data.
	 */
	@SuppressWarnings("unused")
	private void onUpdate() {
		
		ConditionalEvents.transmit(AreaTraceFrame.this, Signal.updateAll);
	}
	
	/**
	 * On render HTML pages.
	 */
	@SuppressWarnings("unused")
	private void onRender() {
		
		GeneratorMainFrame.getFrame().onRender(this);
	}
	
	/**
	 * On display home page.
	 */
	@SuppressWarnings("unused")
	private void onDisplayHomePage() {
		
		ConditionalEvents.transmit(this, Signal.monitorHomePage);
	}
	
	/**
	 * On expand all.
	 */
	public void onExpandTree() {
		
		Utility.expandSelected(tree, true);
	}
	
	/**
	 * On collapse all.
	 */
	public void onCollapseTree() {
		
		Utility.expandSelected(tree, false);
	}
	
	/**
	 * On switch on or off debugging
	 */
	public void onToggleDebug() {
		
		if (!enabledEvents) {
			return;
		}
		if (toggleDebug == null) {
			return;
		}
		final boolean selected = toggleDebug.isSelected();
		
		// Switch on or off debugging of PHP code
		Settings.setEnableDebugging(selected);
		
		// Refresh buttons in other frames
		refresh((AreaTraceFrame frame) -> {
			if (frame != this) {
				frame.setEnableDebugging(selected);
			}
		});
	
		// Refresh slot editors buttons
		SlotEditorHelper.refreshAll((SlotEditorHelper helper) -> {
			JToggleButton toggleDebug = helper.editor.getToggleDebug();
			if (toggleDebug != null) {
				toggleDebug.setSelected(selected);
			}
		});
	}
	
	/**
	 * Set button that enables debugging
	 * @param selected
	 */
	public void setEnableDebugging(boolean selected) {
		
		if (toggleDebug == null) {
			return;
		}
		toggleDebug.setSelected(selected);
	}

	/**
	 * Refresh created frames
	 * @param callback
	 */
	public void refresh(Consumer<AreaTraceFrame> callback) {
		
		enabledEvents = false;
		
		for (AreaTraceFrame frame : frames) {
			if (frame != this) {
				callback.accept(frame);
			}
		}
		
		enabledEvents = true;
	}

	/**
	 * Refresh created frames
	 * @param callback
	 */
	public static void refreshAll(Consumer<AreaTraceFrame> callback) {
		
		if (frames == null) {
			return;
		}
		
		enabledEvents = false;
		
		for (AreaTraceFrame frame : frames) {
			callback.accept(frame);
		}
		
		enabledEvents = true;
	}
	
	/**
	 * Set listeners.
	 */
	private void setListeners() {
		
		DocumentListener listener = new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				onChange();
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				onChange();
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				onChange();
			}
			private void onChange() {
				reload();
			}
		};
		
		textFilter.getDocument().addDocumentListener(listener);
		textLevels.getDocument().addDocumentListener(listener);
		
		// On tree item selection.
		tree.addTreeSelectionListener(new TreeSelectionListener() {
		    public void valueChanged(TreeSelectionEvent e) {

		    	// Get selected areas.
		    	LinkedList<Area> areas = new LinkedList<Area>();
		    	TreePath [] paths = tree.getSelectionPaths();
		    	
		    	if (paths == null) {
		    		return;
		    	}
		    	
		    	// Do loop for all paths.
		    	for (TreePath path : paths) {
		    		DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
		    		Area area = (Area) node.getUserObject();
		    		
		    		// If the area is already in list, continue loop.
		    		boolean isNewArea = true;
		    		
		    		for (Area item : areas) {
		    			if (item.getId() == area.getId()) {
		    				isNewArea = false;
		    			}
		    		}
		    		
		    		if (isNewArea) {
		    			areas.add(area);
		    		}
		    	}
		    	
		    	int location = splitPane.getDividerLocation();
		    	int location2 = splitPaneProviders.getDividerLocation();
		    	
		    	// Set areas properties panel.
		    	areasPropertiesPanel.setAreas(areas);
		    	splitPaneProviders.setLeftComponent(areasPropertiesPanel);

				// Clear slot preview
				editorSlotPreview.setText("");
		    	
		    	splitPane.setDividerLocation(location);
		    	splitPaneProviders.setDividerLocation(location2);
		    }
		});
	}

	/**
	 * Create tree.
	 */
	private void createTree() {

		tree.setExpandsSelectedPaths(true);
		
		// Set model.
		treeModel = new DefaultTreeModel(null);
		tree.setModel(treeModel);
		// Set renderer.
		tree.setCellRenderer(new TreeCellRenderer() {
			@Override
			public Component getTreeCellRendererComponent(JTree tree, Object value,
					boolean selected, boolean expanded, boolean leaf, int row,
					boolean hasFocus) {

				itemRenderer.setForeground(Color.BLACK);
				
				if (!(value instanceof DefaultMutableTreeNode)) {
					itemRenderer.setProperties("#renderer error#", 0, selected, hasFocus, false);
					return itemRenderer;
				}
				
				// Get tree nodes.
				DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) value;
				DefaultMutableTreeNode parentTreeNode = (DefaultMutableTreeNode) treeNode.getParent();
				
				// Get DnD mark.
				boolean dndMark = false;
				if (value instanceof DefaultMutableTreeNodeDnD) {
					
					DefaultMutableTreeNodeDnD dndNode = (DefaultMutableTreeNodeDnD) value;
					dndMark = dndNode.isMarked();
				}
				selected = selected || dndMark;
				
				// Get area.
				Area area = (Area) treeNode.getUserObject();
				boolean isHomeArea = ProgramGenerator.getAreasModel().isHomeArea(area);
				boolean isVisible = area.isVisible();
				boolean isDisabled = !area.isEnabled();
				
				itemRenderer.setAreaVisible(isVisible);
				
				// Get area text.
				String text  = radioAliases.isSelected() ? area.getAlias()
						: area.getDescriptionForced(showIds());
				
				// Get sub relation names.
				if (parentTreeNode != null) {
					Area parentArea = (Area) parentTreeNode.getUserObject();
	
					String subName = parentArea.getSubRelationName(area.getId());
					String superName = area.getSuperRelationName(parentArea.getId());
					boolean hiddenSubareas = parentArea.isSubareasHidden(area);
					
					itemRenderer.setProperties(text, subName, superName, hiddenSubareas, 0, selected, hasFocus, isHomeArea);
				}
				else {
					itemRenderer.setProperties(text, 0, selected, hasFocus, isHomeArea);
				}
				
				// If the area is disabled, gray its name
				if (isDisabled) {
					itemRenderer.setForeground(Color.GRAY);
				}
				
				Object userObject = area.getUser();
				if (userObject instanceof AreaShapes) {
					
					boolean isSelected = ((AreaShapes) userObject).isSelected();
					if (isSelected) {
						
						itemRenderer.setForeground(Color.RED);
					}
				}
				return itemRenderer;
			}
		});
		
		final Component thisComponent = this;
		
		// Set Drag and Drop callback.
		tree.setDragAndDropCallback(new JTreeDndCallback() {

			@Override
			public void onNodeDropped(
					DefaultMutableTreeNodeDnD droppedDndNode,
					TreeNode droppedNodeParent,
					DefaultMutableTreeNodeDnD transferedDndNode,
					TreeNode transferredNodeParent,
					DropTargetDropEvent e) {
				
				// Get transferred area, target area, parent areas and action number and do an action.
				Object transferredObject = transferedDndNode.getUserObject();
				Object droppedObject = droppedDndNode.getUserObject();
				
				if (!(transferredObject instanceof Area && droppedObject instanceof Area)) {
					e.rejectDrop();
					return;
				}
				
				Area transferredParentArea = null;
				if (transferredNodeParent instanceof DefaultMutableTreeNode) {
					
					DefaultMutableTreeNode transferredMutableNodeParent = (DefaultMutableTreeNode) transferredNodeParent;
					Object userObject = transferredMutableNodeParent.getUserObject();
					
					if (userObject instanceof Area) {
						transferredParentArea = (Area) userObject;
					}
				}
				
				Area droppedParentArea = null;
				if (droppedNodeParent instanceof DefaultMutableTreeNode) {
					
					DefaultMutableTreeNode droppedMutableNodeParent = (DefaultMutableTreeNode) droppedNodeParent;
					Object userObject = droppedMutableNodeParent.getUserObject();
					
					if (userObject instanceof Area) {
						droppedParentArea = (Area) userObject;
					}
				}
				
				int action = e.getDropAction();
				
				GeneratorMainFrame.transferArea(
						(Area)transferredObject, transferredParentArea,
						(Area)droppedObject, droppedParentArea,
						action, thisComponent);
			}
		});
	}

	/**
	 * Create list.
	 */
	private void createList() {
		
		// Set renderer.
		itemRenderer = new ItemRendererImpl();
		list.setCellRenderer(new ListCellRenderer() {
			// Get list renderer.
			@Override
			public Component getListCellRendererComponent(JList list, Object value,
					int index, boolean isSelected, boolean cellHasFocus) {
				
				itemRenderer.setForeground(Color.BLACK);
				
				if (!(value instanceof Area)) {
					return null;
				}
				Area area = (Area) value;
				String text  = radioAliases.isSelected() ? area.getAlias()
						: area.getDescriptionForced(showIds());
				
				boolean isHomeArea = ProgramGenerator.getAreasModel().isHomeArea(area);
				
				itemRenderer.setProperties(text, index, isSelected, cellHasFocus, isHomeArea);
				
				Object userObject = area.getUser();
				if (userObject instanceof AreaShapes) {
					
					boolean isAreaSelected = ((AreaShapes) userObject).isSelected();
					if (isAreaSelected) {
						
						itemRenderer.setForeground(Color.RED);
					}
				}
				return itemRenderer;
			}
		});
		// Set model.
		listModel = new DefaultListModel();
		list.setModel(listModel);
		
		// Set list selection listener.
		list.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				
				// Get selected areas.
				LinkedList<Area> areas = new LinkedList<Area>();
				
				for (Object item : list.getSelectedValuesList()) {
					areas.add((Area) item);
				}
				
				int location = splitPane.getDividerLocation();
				int location2 = splitPaneProviders.getDividerLocation();
				
				// Set areas properties panel.
				areasPropertiesPanel.setAreas(areas);
				splitPaneProviders.setLeftComponent(areasPropertiesPanel);
				
				// Clear slot preview
				editorSlotPreview.setText("");
				
				splitPane.setDividerLocation(location);
				splitPaneProviders.setDividerLocation(location2);
			}
		});
	}

	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		tabbedPane.setIconAt(1, Images.getIcon("org/multipage/generator/images/list.png"));
		buttonClose.setIcon(Images.getIcon("org/multipage/generator/images/cancel_icon.png"));
		buttonReload.setIcon(Images.getIcon("org/multipage/generator/images/update_icon.png"));
		menuSelectSubNodes.setIcon(Images.getIcon("org/multipage/generator/images/select_subnodes.png"));
		menuAddSubArea.setIcon(Images.getIcon("org/multipage/generator/images/area_node.png"));
		menuRemoveArea.setIcon(Images.getIcon("org/multipage/generator/images/cancel_icon.png"));
	}

	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(buttonClose);
		Utility.localize(checkInherits);
		Utility.localize(labelLevels);
		Utility.localize(radioAliases);
		Utility.localize(radioDescriptions);
		Utility.localize(radioSubAreas);
		Utility.localize(radioSuperAreas);
		Utility.localize(tabbedPane);
		Utility.localize(buttonReload);
		Utility.localize(labelFilter);
		Utility.localize(checkCaseSensitive);
		Utility.localize(checkWholeWords);
		Utility.localize(checkExactMatch);
		Utility.localize(checkShowIds);
		Utility.localize(menuSelectSubNodes);
		Utility.localize(menuAddSubArea);
		Utility.localize(menuRemoveArea);
	}

	/**
	 * On close window.
	 */
	protected void onCloseWindow() {
		
		saveDialog();
		
		dispose();
		createdFrames.remove(this);
	}

	/**
	 * Save dialog.
	 */
	private void saveDialog() {
		
		loadDescriptionsState = radioDescriptions.isSelected();
		loadSubAreasState = radioSubAreas.isSelected();
		selectedTabIndexState = tabbedPane.getSelectedIndex();
		inheritState = checkInherits.isSelected();
		showIdsState = checkShowIds.isSelected();
		caseSensitiveState = checkCaseSensitive.isSelected();
		wholeWordsState = checkWholeWords.isSelected();
		exactMatchState = checkExactMatch.isSelected();
		filterState = textFilter.getText();
		levelsState = textLevels.getText();
		
		bounds = getBounds();
		splitterPosition = splitPane.getDividerLocation();
		splitter2Position = splitPaneProviders.getDividerLocation();
	}

	/**
	 * Reload dialog.
	 */
	private void reload() {
		
		// Get split pane divider position.
		int splitPaneDividerLocation = splitPane.getDividerLocation();
		int splitPane2DividerLocation = splitPaneProviders.getDividerLocation();
		
		// Reset area editor.
		areasPropertiesPanel.setAreas(null);
		setNoAreasSelectedMessage();
		
		// Clear slot preview
		editorSlotPreview.setText("");
		
		// Get selected area.
		Area selectedArea = (Area) list.getSelectedValue();
		Long selectedAreaId = null;
		
		if (selectedArea != null) {
			selectedAreaId = selectedArea.getId();
		}
		
		// Set tab icon and text.
		boolean isSubareas = radioSubAreas.isSelected();
		String iconPath = "org/multipage/generator/images/" + (isSubareas ? "subareas" 
				: "superareas") + ".png";
		
		tabbedPane.setIconAt(0, Images.getIcon(iconPath));
		tabbedPane.setTitleAt(0, Resources.getString(
				isSubareas ? "org.multipage.generator.textSubAreasTree" : "org.multipage.generator.textSuperAreasTree"));
		setIconImage(Images.getImage(iconPath));

		// Set title.
		Area area = ProgramGenerator.getAreasModel().getArea(areaId);
		String areaName = area != null ? area.getDescriptionForced(showIds())
				: Resources.getString("org.multipage.generator.textUnknownArea");
		
		setTitle(String.format(Resources.getString("org.multipage.generator.textAreaTitle"), areaName));

		// Get selected tab.
		boolean isList = tabbedPane.getSelectedIndex() == 1;
		// Get selected text type.
		boolean isDescription = radioDescriptions.isSelected();
		// Get inheritance.
		boolean inheritance = checkInherits.isSelected();
		// Get number of levels.
		String levelsText = textLevels.getText();
		int levels = 0;
		try {
			levels = Integer.parseInt(levelsText);
		}
		catch (Exception e) {
		}
		// Get filter.
		String filterText = textFilter.getText();
		boolean caseSensitive = checkCaseSensitive.isSelected();
		boolean wholeWord = checkWholeWords.isSelected();
		boolean exactMatch = checkExactMatch.isSelected();

		// Set inheritance and Drag and Drop.
		checkInherits.setEnabled(!isSubareas);
		tree.enableDragAndDrop(isSubareas);
		
		// Update tree.
		if (!isList) {
			
			// Get tree state.
			AreaTreeState treeState = AreaTreeState.getTreeState(tree);
			
			// Load tree.
			updateTreeModel(treeModel, areaId, isSubareas, inheritance);
						
			// Apply tree state.
			AreaTreeState.applyTreeState(treeState, tree);
			
			// Expand tree root.
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					Utility.expandTop(tree, true);
				}
			});
			
		}
		else {
			// Update list.
			listModel.clear();
			// Get areas.
			AreasModel areasModel = ProgramGenerator.getAreasModel();
			LinkedList<Area> areas = isSubareas ? areasModel.getAreaAndSubAreas(areaId, levels) :
				areasModel.getAreaAndSuperAreas(areaId, levels, inheritance);
			LinkedList<Area> areasSorted = new LinkedList<Area>();
			
			// Load texts.
			for (Area areaItem : areas) {
				
				String alias = areaItem.getAlias();
				String description = areaItem.getDescriptionForced(showIds());
				
				String text = isDescription ? description : alias;
				if (!text.isEmpty()) {
					if (!filterText.isEmpty() && !Utility.matches(text, filterText,
							caseSensitive, wholeWord, exactMatch)) {
						continue;
					}
					areasSorted.add(areaItem);
				}
			}
			
			// Sort texts.
			class AreasComparator implements Comparator<Area> {
				
				boolean isAliases;
				
				public AreasComparator(boolean isAliases) {
					this.isAliases = isAliases;
				}

				@Override
				public int compare(Area area1, Area area2) {
					
					String area1Text;
					String area2Text;
					
					if (isAliases) {
						area1Text = area1.getAlias();
						area2Text = area2.getAlias();
					}
					else {
						area1Text = area1.getDescriptionForced(showIds());
						area2Text = area2.getDescriptionForced(showIds());
					}
					return area1Text.compareTo(area2Text);
				}
			}
			
			Collections.sort(areasSorted, new AreasComparator(!isDescription));
			
			// Load list.
			for (Area areaSorted : areasSorted) {
				listModel.addElement(areaSorted);
			}
			
			// Set areas count.
			labelFoundAreasCount.setText(String.format(
					Resources.getString("org.multipage.generator.textFoundAreasCount"), areasSorted.size()));
			
			// Select area.
			if (selectedAreaId != null) {
				selectArea(selectedAreaId);
			}
		}
		
		// Set split pane location.
		splitPane.setDividerLocation(splitPaneDividerLocation);
		splitPaneProviders.setDividerLocation(splitPane2DividerLocation);
	}
	
	/**
	 * Update tree model.
	 * @param treeModel
	 * @param isSubareas
	 * @param inheritance
	 */
	private void updateTreeModel(DefaultTreeModel treeModel, Long rootAreaId, boolean isSubareas,
			boolean inheritance) {
		
		if (rootAreaId == null) {
			// Clear tree model.
			treeModel.setRoot(null);
			return;
		}
		
		// Get root area.
		Area rootArea = ProgramGenerator.getArea(rootAreaId);
		if (rootArea == null) {
			treeModel.setRoot(null);
			return;
		}
		
		// Create root node.
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNodeDnD(rootArea);
		
		// Create nodes.
		createNodes(rootNode, isSubareas, inheritance);
		
		// Set root node.
		treeModel.setRoot(rootNode);
	}

	/**
	 * Create nodes.
	 * @param parentNode
	 * @param inheritance 
	 */
	private void createNodes(DefaultMutableTreeNode parentNode, boolean isSubareas, boolean inheritance) {
		
		Object userObject = parentNode.getUserObject();
		if (!(userObject instanceof Area)) {
			return;
		}
		
		Area area = (Area) userObject;
		
		// Do loop for all sub or super areas.
		LinkedList<Area> areas = null;
		
		if (isSubareas) {
			
			// If the area is disabled, hide its sub areas
			if (!area.isEnabled()) {
				return;
			}
			
			areas = area.getSubareas();
		}
		else {
			if (!inheritance) {
				areas = area.getSuperareas();
			}
			else {
				areas = area.getInheritsFrom();
			}
		}
		
		for (Area areaItem : areas) {
			
			// Create new node.
			DefaultMutableTreeNode childNode = new DefaultMutableTreeNodeDnD(areaItem);
			// Add it to the parent node.
			parentNode.add(childNode);

			if (isSubareas) {
				// If area item sub areas are hidden, continue the loop.
				if (area.isSubareasHidden(areaItem)) {
					continue;
				}
			}
			
			// Call this method recursively.
			createNodes(childNode, isSubareas, inheritance);
		}
	}

	/**
	 * Select area.
	 * @param areaId
	 */
	private void selectArea(long areaId) {
		
		list.clearSelection();
		
		int count = listModel.getSize();
		for (int index = 0; index < count; index++) {
			
			Area area = (Area) listModel.get(index);
			if (area.getId() == areaId) {
				
				list.setSelectedIndex(index);
				break;
			}
		}
	}
	
	/**
	 * Show trayMenu.
	 * @param e
	 * @param popup 
	 */
	protected void showMenu(MouseEvent e, JPopupMenu popup) {
		
		if (popup.equals(popupMenuTree)) {
			
			boolean isSubAreas = radioSubAreas.isSelected();
			
			menuAddSubArea.setEnabled(isSubAreas);
			menuRemoveArea.setEnabled(isSubAreas);
		}
		popup.show(e.getComponent(), e.getX(), e.getY());
	}
	
	/**
	 * Add popup window.
	 * @param component
	 * @param popup
	 */
	private void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e, popup);
				}
			}
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e, popup);
				}
			}
			
		});
	}

	/**
	 * Redraw information.
	 */
	public static void redrawInformation() {
		
		Utility.traverseUI((component) -> {
			
			if (component instanceof AreaTraceFrame) {
				AreaTraceFrame traceFrame = (AreaTraceFrame) component;
				
				traceFrame.tree.repaint();
				traceFrame.list.repaint();
			}
			return true;
		});
	}
}
