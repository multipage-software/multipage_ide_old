/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.maclan.Area;
import org.maclan.AreaRelation;
import org.maclan.AreaTreeData;
import org.maclan.Language;
import org.maclan.Middle;
import org.maclan.MiddleListener;
import org.maclan.MiddleResult;
import org.maclan.MiddleUtility;
import org.maclan.Slot;
import org.maclan.VersionObj;
import org.maclan.server.BrowserParameters;
import org.maclan.server.DebugListener;
import org.maclan.server.DebugViewerCallback;
import org.maclan.server.ProgramServlet;
import org.maclan.server.TextRenderer;
import org.multipage.basic.ProgramBasic;
import org.multipage.gui.BareBonesBrowserLaunch;
import org.multipage.gui.Images;
import org.multipage.gui.Progress2Dialog;
import org.multipage.gui.ProgressDialog;
import org.multipage.gui.ToolBarKit;
import org.multipage.gui.Utility;
import org.multipage.sync.SyncMain;
import org.multipage.util.Obj;
import org.multipage.util.ProgressResult;
import org.multipage.util.Resources;
import org.multipage.util.SimpleMethodRef;
import org.multipage.util.SwingWorkerHelper;
import org.multipage.util.j;

/**
 * 
 * @author
 *
 */
public class GeneratorMainFrame extends JFrame {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Menu size.
	 */
	private static final Dimension preferredMenuSize = new Dimension(240, 22);
	
	/**
	 * A flag that indicates reactivation of GUI.
	 */
	private static Obj<Boolean> guiReactivationInProgress = new Obj<Boolean>(false);

	/**
	 * Close window callback.
	 */
	private SimpleMethodRef closeCallback;
	
	/**
	 * Main frame object.
	 */
	protected static GeneratorMainFrame mainFrame;
	
	/**
	 * Boundary.
	 */
	protected static Rectangle bounds;

	/**
	 * Extended state.
	 */
	private static int extendedState;
	
	/**
	 * Show ID button state.
	 */
	private static boolean showIdButtonState;
	
	/**
	 * Main diagram name.
	 */
	private static String mainDiagramName;
	
	/**
	 * Contents of tabs.
	 */
	private static LinkedList<TabState> tabsStates;

	/**
	 * Selected tab index.
	 */
	private static int selectedTabIndex;
	
	/**
	 * Debug viewer reference
	 */
	private static DebugViewer debugViewer;
	
	/**
	 * Load data.
	 * @param inputStream
	 */
	public static void serializeData(ObjectInputStream inputStream)
		throws IOException, ClassNotFoundException {
		
		// Load dialog bounds.
		Object object = inputStream.readObject();
		if (!(object instanceof Rectangle)) {
			throw new ClassNotFoundException();
		}
		bounds = (Rectangle) object;
		// Load dialog state.
		extendedState = inputStream.readInt();
		// Load "show ID button" state.
		showIdButtonState = inputStream.readBoolean();
		// Load editor states.
		AreasDiagramPanel.splitPositionStateMain = inputStream.readInt();
		AreasDiagramPanel.splitPositionStateSecondary = inputStream.readInt();
		
		int count = inputStream.readInt();
		while (count > 0) {
			AreasDiagramPanel.selectedAreasIdsState.add(inputStream.readLong());
			count--;
		}
		// Load text.
		SelectAreaDialog.oldText = inputStream.readUTF();
		// Load text.
		SelectAreaResource.oldText = inputStream.readUTF();
		// Load export data.
		ExportDialog.exportFolder = inputStream.readUTF();
		// Load main diagram name.
		mainDiagramName = inputStream.readUTF();
		// Load states of tabs.
		tabsStates = readTabStates(inputStream);
		// Load selected tab index.
		selectedTabIndex = inputStream.readInt();
	}

	/**
	 * Save data.
	 * @param outputStream
	 */
	public static void serializeData(ObjectOutputStream outputStream)
		throws IOException {

		// Save dialog boundaries.
		outputStream.writeObject(bounds);
		// Save dialog state.
		outputStream.writeInt(extendedState);
		// Save "show ID button" state.
		outputStream.writeBoolean(showIdButtonState);
		// Save editor states.
		outputStream.writeInt(AreasDiagramPanel.splitPositionStateMain);
		outputStream.writeInt(AreasDiagramPanel.splitPositionStateSecondary);
		
		outputStream.writeInt(AreasDiagramPanel.selectedAreasIdsState.size());
		for (long areaId : AreasDiagramPanel.selectedAreasIdsState) {
			outputStream.writeLong(areaId);
		}
		// Save text.
		outputStream.writeUTF(SelectAreaDialog.oldText);
		// Save text.
		outputStream.writeUTF(SelectAreaResource.oldText);
		// Save export data.
		outputStream.writeUTF(ExportDialog.exportFolder);
		// Save main diagram name.
		outputStream.writeUTF(mainDiagramName);
		// Save cloned diagrams.
		outputStream.writeObject(tabsStates);
		// Save selected tab index.
		outputStream.writeInt(selectedTabIndex);
	}

	/**
	 * Set default data.
	 */
	public static void setDefaultData() {

		bounds = null;
		extendedState = NORMAL;
		showIdButtonState = true;
		mainDiagramName = Resources.getString("org.multipage.generator.textMainAreasTab");
		tabsStates = new LinkedList<TabState>();
		selectedTabIndex = 0;
	}

	/**
	 * Read tabs states.
	 * @param inputStream
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private static LinkedList<TabState> readTabStates(
			ObjectInputStream inputStream)
					throws IOException, ClassNotFoundException {
		
		LinkedList<TabState> list = new LinkedList<TabState>();
		
		// Read list object.
		Object object = inputStream.readObject();
		if (object instanceof LinkedList) {
			
			// Do loop for all list items.
			for (Object item : ((LinkedList) object)) {
				
				// Add item to the output list.
				if (item instanceof TabState) {
					list.add((TabState) item);
				}
			}
		}
		
		return list;
	}

	/**
	 * Set close listener.
	 * @param closeCallback
	 */
	public void setCloseListener(SimpleMethodRef closeCallback) {

		this.closeCallback = closeCallback;
	}

	/**
	 * Load tab states and create appropriate tab panels.
	 */
	private void loadTabPanels() {
		
		// Reset flag.
		boolean cloned = false;
		
		// Do loop for all diagram states.
		for (TabState tabState : tabsStates) {
			
			// Get tab type
			TabType type = tabState.type;
			
			// On areas diagram
			if (TabType.areasDiagram.equals(type) && tabState instanceof AreasDiagramTabState) {
				
				// Get extended tab state
				AreasDiagramTabState extendedTabState = (AreasDiagramTabState) tabState;
				
				// Create areas diagram panel
				AreasDiagramPanel diagramPanel = createAreasDiagram(extendedTabState.title, extendedTabState.areaId);
				if (diagramPanel == null) {
					continue;
				}
				
				// Set flag.
				cloned = true;
			
				// Get inner areas diagram.
				AreasDiagram innerAreasDiagram = diagramPanel.getDiagram();
				
				// Set position of areas in the inner panel.
				innerAreasDiagram.setDiagramPosition(extendedTabState.translationx,
						extendedTabState.translationy, extendedTabState.zoom);
			}
			
			// On areas tree view
			else if (TabType.areasTree.equals(type) && tabState instanceof AreasTreeTabState) {
				
				// Get extended tab state
				AreasTreeTabState extendedTabState = (AreasTreeTabState) tabState;
				
				// Create new areas tree view
				createAreasTreeView(extendedTabState.title, extendedTabState.areaId, extendedTabState.displayedArea);
				
				// Set flag.
				cloned = true;
			}
			
			// On HTML browser
			else if (TabType.monitor.equals(type) && tabState instanceof MonitorTabState) {
				
				// Get extended tab state
				MonitorTabState extendedTabState = (MonitorTabState) tabState;
				
				// Add new monitor to the tab panel
				tabPanel.addMonitor(extendedTabState.url, false);
				
				// Set flag.
				cloned = true;
			}
		}
		
		// If nothing cloned, trim selection.
		if (!cloned) {
			selectedTabIndex = 0;
		}
		
		// Select main tab.
		try {
			tabPanel.setSelectedIndex(selectedTabIndex);
		}
		catch (Exception e) {
		}
		
		// Update window selection trayMenu.
		updateWindowSelectionMenu();
		
		// Propagate event.
		SwingUtilities.invokeLater(() -> {
			ConditionalEvents.transmit(GeneratorMainFrame.this, Signal.loadDiagrams);
		});
	}

	/**
	 * Load dialog.
	 */
	protected void loadDialog() {

		// Set bounds.
		if (bounds == null) {
			bounds = new Rectangle(0, 0, 1040, 730);
			setBounds(bounds);
			Utility.centerOnScreen(this);
		}
		else {
			setBounds(bounds);
		}
		// Load extended state.
		setExtendedState(extendedState);
		// Show ID state.
		showIdButton.setSelected(showIdButtonState);
		onShowHideIds();
		// Lighten read only elements state.
		exposeReadOnly.setSelected(!AreaShapes.readOnlyLighter);
		// Create cloned diagrams.
		loadTabPanels();
	}

	/**
	 * Save dialog.
	 */
	protected void saveDialog() {

		// Save bounds.
		bounds = getBounds();
		// Save extended state.
		extendedState = getExtendedState();
		// Save show ID button state.
		showIdButtonState = showIdButton.isSelected();
		// Save main dialog title.
		mainDiagramName = tabPanel.getTabTitle(0);
		// Save selected tab state.
		selectedTabIndex = tabPanel.getSelectedIndex();
		// Save tabs states.
		saveTabsStates();
	}

	/**
	 * Save tabs states.
	 */
	private void saveTabsStates() {
		
		// Load tab states
		tabsStates = tabPanel.getTabsStates();
	}

	/**
	 * Tool bar.
	 */
	protected JToolBar toolBar;

	/**
	 * AreasDiagram.
	 */
	private AreasDiagramPanel mainAreaDiagramEditor;

	/**
	 * Status bar.
	 */
	private MainStatusBar statusBar;

	/**
	 * Connection watch dog.
	 */
	private ActionListener watchdog;

	/**
	 * Connection watch dog interval in milliseconds.
	 */
	private int watchdogMs = 60000;
	
	/**
	 * Elements properties container panel.
	 */
	private PropertiesPanel propertiesPanel;
	
	/**
	 * AreasDiagram and properties split panel.
	 */
	private SplitProperties splitDiagramProperties;

	/**
	 * Timer watch dog.
	 */
	private javax.swing.Timer timerWatchDog;
	
	/**
	 * Tab panel.
	 */
	private TabPanel tabPanel;
	
	/**
	 * Customize colors.
	 */
	private CustomizedColors customizeColors;
	
	/**
	 * Customize controls.
	 */
	private CustomizedControls cutomizeControls;

	/**
	 * Show id toggle button.
	 */
	private JToggleButton showIdButton;

	/**
	 * Light read only elements.
	 */
	private JToggleButton exposeReadOnly;
	
	/**
	 * Search dialog.
	 */
	private SearchDialog searchDialog;

	/**
	 * User value.
	 */
	private String userValue = "";

	/**
	 * Undo redo buttons.
	 */
	private JButton undoButton;
	private JButton redoButton;

	/**
	 * Window selection trayMenu.
	 */
	private JMenu windowSelectionMenu;

	/**
	 * Reset area tree copy timer.
	 */
	private javax.swing.Timer resetAreaTreeCopyTimer;
	
	/**
	 * Toggle debug button.
	 */
	private JToggleButton toggleDebug;

	/**
	 * Area tree data to copy.
	 */
	private static AreaTreeData areaTreeDataToCopy;

	/**
	 * Constructor.
	 */
	public GeneratorMainFrame() {

		// Set static member.
		mainFrame = this;
		
		//RestartApplication.runRestartWathdog();
		
		// Set close action.
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		// Set window description and icon.
		setTitle(ProgramGenerator.getApplicationTitle());
		setIconImage(Images.getImage("org/multipage/generator/images/main_icon.png"));

		// Set middle layer login listener.
		setMiddleListener();
		// Create components.
		createComponents();
		// Create trayMenu.
		createMenu();
		// Create tool bar.
		createToolBar();
		// Create status bar.
		createStatusBar();
		// Set connection watch dog.
		createConnectionWatchDog();
		// Set callback functions.
		setCallbacks();
		// Set listeners.
		setListeners();
		// Set timers.
		setTimers();
		// Initialize log window.
		LoggingDialog.initialize(this);
		// Load dialog.
		loadDialog();
		
		// Reload areas model.
		MiddleResult result = ProgramGenerator.reloadModel();
		result.showError(this);
	}

	/**
	 * Set timers.
	 */
	private void setTimers() {
		
		// Reset area tree copy timer.
		resetAreaTreeCopyTimer = new javax.swing.Timer(60000, (e) -> {
			
			areaTreeDataToCopy = null;
		});
		resetAreaTreeCopyTimer.setRepeats(false);
	}
	
	/**
	 * Set callback functions.
	 */
	private void setCallbacks() {
		
		SyncMain.setReactivateGuiCallback(() -> {
			
			reactivateGui();
		});
	}

	/**
	 * Set listeners.
	 */
	private void setListeners() {

		tabPanel.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						onChangeTab();
					}
				});
			}
		});
		
		tabPanel.setRemoveListener(() -> {
				
			// Update window selection trayMenu.
			updateWindowSelectionMenu();
		});
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent e) {
				init();
			}
			public void windowClosing(WindowEvent e) {
				closeWindow();
			}
		});
		
		addWindowStateListener(new WindowStateListener() {
			@Override
			public void windowStateChanged(WindowEvent e) {
				// If window state changed repaint properties.
				splitDiagramProperties.redraw();
			}
		});
		
		// Close splash windows.
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				
				Utility.closeSplash();
			}
		});
		
		// Area Server events from servlet.
		ProgramServlet.setUpdatedSlotsListener(slotIds -> {
			
			if (!slotIds.isEmpty()) {
				SwingUtilities.invokeLater(() -> {
					ConditionalEvents.transmit(ProgramServlet.class, Signal.updatedSlotsWithServlet, slotIds);
				});
			}
		});
		
		// Create debug viewer
		debugViewer = new DebugViewer(this);
		
		// Set debug viewer listener
		DebugListener debugger = ProgramBasic.getHttpServer().getDebugger();
		if (debugger != null) {
			debugger.setDebugViewerListener(new DebugViewerCallback() {

				@Override
				public int openFile(String fileUri) {
					
					// Delegates the call to debug viewer
					return debugViewer.openFile(fileUri);
				}

				@Override
				public void showUserAlert(String message, int timeout) {
					
					// Delegates the call to debug viewer
					debugViewer.showUserAlert(message, timeout);
				}
			});
		}
		
		// Set Sync lambda functions.
		SyncMain.setCloseEvent(() -> {
			
			closeWindow();
		});
		
		// "Show areas' properties" event receiver.
		ConditionalEvents.receiver(this, Signal.showAreasProperties, message -> {
			
			HashSet<Long> selectedAreaIds = null;
					
			// Retrieve selected areas' IDs from the event object or get currently selected areas.
			if (message.relatedInfo instanceof HashSet<?>) {
				selectedAreaIds = (HashSet<Long>) message.relatedInfo;
			}
			else {
				selectedAreaIds = getAreaDiagram().getSelectedAreaIds();
			}
			
			// Show selected areas' properties.
			showProperties(selectedAreaIds);
		});
		
		// "Monitor home page" event receiver.
		ConditionalEvents.receiver(this, Signal.monitorHomePage, message -> {
			
			monitorHomePage();
		});
		
		// "Reactivate GUI" event receiver.
		ConditionalEvents.receiver(this, Signal.reactivateGui, message -> {
			
			// Initialize focused component.
			Component focusedComponent = null;
			
			// Try to get focused component from the event information.
			Object relatedInfo = message.relatedInfo;
			if (relatedInfo instanceof Component) {
				focusedComponent = (Component) relatedInfo;
			}
			
			// Do reactivation.
			invokeReactivationOfGui(focusedComponent);
		});
		
		// "Update all request" event receiver.
		ConditionalEvents.receiver(this, Signal.updateAll, ConditionalEvents.HIGH_PRIORITY, message -> {
			
			// Disable the signal temporarily.
			Signal.updateAll.disable();
			
			// Reload areas model.
			ProgramGenerator.reloadModel();
			
			// Enable the signal.
			SwingUtilities.invokeLater(() -> {
				Signal.updateAll.enable();
			});
		});
		
		// "Expose read only areas" event receiver.
		ConditionalEvents.receiver(this, Signal.exposeReadOnlyAreas, message -> {
			AreaShapes.readOnlyLighter = !exposeReadOnly.isSelected();
		});
		
		// "Focus area" event receiver.
		ConditionalEvents.receiver(this, Signal.focusArea, message -> {
			
			// Get diagram panel.
			AreasDiagramPanel areasDiagramPanel = getFrame().getVisibleAreasEditor();
			
			// Try to focus area using coordinates.
			try {
				// Get coordinates.
				AreaCoordinatesTableItem coordinatesItem = message.getRelatedInfo();
				
				// Focus coordinates.
				if (coordinatesItem != null) {
					areasDiagramPanel.getDiagram().focus(coordinatesItem.coordinate, null);
				}
			}
			catch (Exception e) {
			}
			
			// Try to focus area using area identifier.
			try {
				// Get area ID.
				Long areaId = message.getRelatedInfo();
				
				// Focus coordinates.
				if (areaId != null) {
					areasDiagramPanel.focusArea(areaId);
				}
			}
			catch (Exception e) {
			}
		});
 	}
	
	/**
	 * Remove listeners.
	 */
	protected void removeListeners() {
		
		// Remove event receivers.
		ConditionalEvents.removeReceivers(this);
	}
	
	/**
	 * Reactivate GUI.
	 */
	public static void reactivateGui() {
		
		// Get current focused control.
		Component focusedControl = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
		
		// Propagate the event with focused control.
		ConditionalEvents.transmit(GeneratorMainFrame.class, Signal.reactivateGui, focusedControl);
	}
	
	/**
	 * Invoke reactivation of the GUI.
	 * @param focusedComponent - currently focused GUI component.
	 */
	public void invokeReactivationOfGui(Component focusedComponent) {
		
		// Coalesce reactivation events.
		if (guiReactivationInProgress.ref) {
			return;
		}
		
		// Set the flag.
		guiReactivationInProgress.ref = true;
		
		// Reactivate GUI.
		
		// 1. Enable the frame window.
		SwingUtilities.invokeLater(() -> {
			getFrame().setEnabled(true);
		});
		
		// 2. Set always on top.
		SwingUtilities.invokeLater(() -> {
			getFrame().setAlwaysOnTop(true);
		});
		
		// 3. Sleep for a while.
		SwingUtilities.invokeLater(() -> {
			try {
				Thread.sleep(100);
			}
			catch (Exception e) {
			}
		});
		
		// 4. Reset always on top.
		SwingUtilities.invokeLater(() -> {
			getFrame().setAlwaysOnTop(false);
		});
		
		// 5. Ensure the frame is visible.
		SwingUtilities.invokeLater(() -> {
			getFrame().setVisible(true);
		});
		
		// 6. Sleep for a while.
		SwingUtilities.invokeLater(() -> {
			try {
				Thread.sleep(100);
			}
			catch (Exception e) {
			}
		});
		
		// 7. Bring the frame to front.
		SwingUtilities.invokeLater(() -> {
			getFrame().toFront();
		});
		
		// 8. Restore focus.
		SwingUtilities.invokeLater(() -> {
			
			if (focusedComponent != null) {
				focusedComponent.requestFocusInWindow();
			}
		});
		
		// 9. Repaint the frame.
		SwingUtilities.invokeLater(() -> {
			getFrame().repaint();
		});
		
		// Reset the flag.
		SwingUtilities.invokeLater(() -> {
			guiReactivationInProgress.ref = false;
		});
	}

	/**
	 * Create connection watch dog.
	 */
	private void createConnectionWatchDog() {

		// Schedule timer.
		watchdog = new ActionListener() {
			// Middle.
			private Middle middle = MiddleUtility.newMiddleInstance();
			@Override
			public void actionPerformed(ActionEvent event) {
				// Check login and get number of connections.
				try {
					if (GeneratorMainFrame.getFrame() != null) {
						
						Properties login = ProgramBasic.getLoginProperties();
						
						boolean isConnection = middle.checkLogin(login).isOK();
						statusBar.setConnection(isConnection);
						
						// Get number of connections.
						Obj<Integer> number = new Obj<Integer>(0);
						middle.loadNumberConnections(login, number);
						statusBar.setNumberConnections(number.ref);
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		timerWatchDog = new javax.swing.Timer(watchdogMs, watchdog);
		timerWatchDog.setInitialDelay(0);
		timerWatchDog.setRepeats(true);
		timerWatchDog.start();
	}

	/**
	 * Create status bar.
	 */
	private void createStatusBar() {

		statusBar = new MainStatusBar();
		statusBar.setLoginProperties(ProgramBasic.getLoginProperties());
		this.add(statusBar, BorderLayout.PAGE_END);
	}

	/**
	 * Create components.
	 */
	private void createComponents() {
		
		mainAreaDiagramEditor = newAreasDiagramEditor();
		tabPanel = newTabPanel(mainAreaDiagramEditor);
		propertiesPanel = newElementProperties();
		splitDiagramProperties = new SplitProperties(tabPanel, propertiesPanel);
		customizeColors = new CustomizedColors(this);
		cutomizeControls = newCustomizedControls(this);
		searchDialog = new SearchDialog(this, ProgramGenerator.getAreasModel());
		
		add(splitDiagramProperties, BorderLayout.CENTER);
	}
	
	/**
	 * Create new areas diagram editor object.
	 * @return
	 */
	protected AreasDiagramPanel newAreasDiagramEditor() {
		
		// Create new panel and initialize it.
		AreasDiagramPanel diagramPanel = new AreasDiagramPanel();
		diagramPanel.init();
		
		return diagramPanel;
	}
	
	/**
	 * Create new customized controls object.
	 * @param owner
	 * @return
	 */
	protected CustomizedControls newCustomizedControls(Window owner) {
		
		return new CustomizedControls(owner);
	}
	
	/**
	 * Create new tab panel object.
	 * @param panel
	 * @return
	 */
	protected TabPanel newTabPanel(JPanel panel) {
		
		return new TabPanel(panel);
	}
	
	/**
	 * Create new element properties object.
	 * @return
	 */
	protected PropertiesPanel newElementProperties() {
		
		return new PropertiesPanel();
	}

	/**
	 * Set middle listener.
	 */
	private void setMiddleListener() {

		ProgramBasic.getMiddle().addListener(new MiddleListener(){
			@Override
			public void onLogin(boolean ok) {
				// Delegate call.
				onLoginCheck(ok);
			}
		});
	}

	/**
	 * Close main frame window.
	 */
	public void closeWindow() {
		
		// Call on close method.
		onClose();
		
		// Close properties.
		propertiesPanel.setNoProperties();
		splitDiagramProperties.minimize();
		
		// Invoke dialog saving later.
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				
				// Save dialog.
				saveDialog();
				// Dispose dialog.
				mainAreaDiagramEditor.dispose();
				// Dispose splitter.
				splitDiagramProperties.dispose();
				// Dispose dialog.
				cutomizeControls.disposeDialog();
				// Dispose dialog.
				customizeColors.disposeDialog();
				// Dispose properties.
				propertiesPanel.dispose();
				// Run callback.
				if (closeCallback != null) {
					closeCallback.run();
				}
				removeListeners();
				// Dispose window.
				dispose();
				// Exit application.
				System.exit(0);
			}
		});
	}
	
	/**
	 * On change tab.
	 */
	private void onChangeTab() {
		
		// Get visible area editor.
		AreasDiagramPanel editor = getVisibleAreasEditor();
		if (editor != null) {
			editor.getDiagram().updateUndoRedo();
		}
		
		// Update window selection trayMenu.
		updateWindowSelectionMenu();
		
		// Get current tab panel.
		int tabIndex = tabPanel.getSelectedIndex();
		if (tabIndex >= 0) {
			
			Component tabComponent = tabPanel.getComponentAt(tabIndex);
			if (tabComponent instanceof TabItemInterface) {
				
				TabItemInterface tabItem = (TabItemInterface) tabComponent;
				HashSet<Long> selectedAreaIds = tabItem.getSelectedAreaIds();
				
				// Transmit event.
				if (selectedAreaIds != null) {
					j.log("TRANSMITTED 6 showAreasProperties %s", selectedAreaIds.toString());
					ConditionalEvents.transmit(GeneratorMainFrame.this, Signal.showAreasProperties, selectedAreaIds);
				}
			}
		}
	}

	/**
	 * Initialization.
	 */
	public void init() {
		
		splitDiagramProperties.init();
	}
	
	/**
	 * Create trayMenu.
	 */
	protected void createMenu() {

		// Create trayMenu components.
		JMenuBar menuBar = new JMenuBar();
		
		JMenu file = new JMenu(Resources.getString("org.multipage.generator.menuMainFile"));
		JMenu edit = new JMenu(Resources.getString("org.multipage.generator.menuEditors"));
		JMenu tools = new JMenu(Resources.getString("org.multipage.generator.menuTools"));
		JMenu window = new JMenu(Resources.getString("org.multipage.generator.menuWindow"));
		windowSelectionMenu = new JMenu(Resources.getString("org.multipage.generator.menuWindowSelection"));
		JMenu help = new JMenu(Resources.getString("org.multipage.generator.menuMainHelp"));

		// Create trayMenu tree.
		menuBar.add(file);
		menuBar.add(edit);
		menuBar.add(tools);
		menuBar.add(window);
		menuBar.add(help);
		
		this.setJMenuBar(menuBar);
		
		// Create optional debug trayMenu.
		if (ProgramGenerator.isDebug()) {
			
			JMenu debugMenu = new JMenu(Resources.getString("org.multipage.generator.menuDebug"));
			menuBar.add(debugMenu);
			
			JMenuItem test = new JMenuItem(Resources.getString("org.multipage.generator.menuTest"));
			JMenuItem setUserValue = new JMenuItem(Resources.getString("org.multipage.generator.menuSetUserValue"));
			JMenuItem loggingDialog = new JMenuItem(Resources.getString("org.multipage.generator.menuLoggingDialog"));
			
			debugMenu.add(setUserValue);
			debugMenu.add(test);
			debugMenu.add(loggingDialog);
			
			setUserValue.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					onSetUserValue();
				}});
			test.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					onTest();
				}});
			loggingDialog.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					onLogging();
				}});
		}
		
		// Conditionally created login trayMenu item.
		JMenuItem loginDialog = null;
		if (ProgramBasic.isUsedLogin()) {
			loginDialog = new JMenuItem(Resources.getString("org.multipage.generator.menuLoginDialog"));
				loginDialog.setAccelerator(KeyStroke.getKeyStroke("control L"));
				loginDialog.setIcon(Images.getIcon("org/multipage/generator/images/login_small.png"));
		}
		
	
		// Add trayMenu items.
		JMenuItem fileMenuExit = new JMenuItem(Resources.getString("org.multipage.generator.menuMainFileExit"));
			fileMenuExit.setAccelerator(KeyStroke.getKeyStroke("control X"));
			fileMenuExit.setIcon(Images.getIcon("org/multipage/generator/images/exit_icon.png"));
			
		JMenuItem  helpMenuAbout = new JMenuItem(Resources.getString("org.multipage.generator.menuMainHelpAbout"));
			helpMenuAbout.setAccelerator(KeyStroke.getKeyStroke("control H"));
			helpMenuAbout.setIcon(Images.getIcon("org/multipage/generator/images/about_small.png"));
			
		JMenuItem updateData = new JMenuItem(Resources.getString("org.multipage.generator.menuUpdateData"));
			updateData.setAccelerator(KeyStroke.getKeyStroke("control U"));
			updateData.setIcon(Images.getIcon("org/multipage/generator/images/update2_icon.png"));
			
		JMenuItem render = new JMenuItem(Resources.getString("org.multipage.generator.menuRender"));
			render.setAccelerator(KeyStroke.getKeyStroke("control R"));
			render.setIcon(Images.getIcon("org/multipage/generator/images/render_small.png"));
			
		JMenuItem closeAllWindows = new JMenuItem(Resources.getString("org.multipage.generator.menuCloseAllWindows"));
			closeAllWindows.setAccelerator(KeyStroke.getKeyStroke("control shift A"));
			closeAllWindows.setIcon(Images.getIcon("org/multipage/generator/images/close_all.png"));
			
		JMenuItem toolsSearch = new JMenuItem(Resources.getString("org.multipage.generator.menuFind"));
			toolsSearch.setAccelerator(KeyStroke.getKeyStroke("control F"));
			toolsSearch.setIcon(Images.getIcon("org/multipage/generator/images/search2_icon.png"));
			
		JMenuItem toolsCustomizeColors = new JMenuItem(Resources.getString("org.multipage.generator.menuToolsCustomizeColors"));
			toolsCustomizeColors.setAccelerator(KeyStroke.getKeyStroke("alt C"));
			toolsCustomizeColors.setIcon(Images.getIcon("org/multipage/generator/images/colors_icon.png"));
			
		JMenuItem toolsCustomizeControls = new JMenuItem(Resources.getString("org.multipage.generator.menuToolsCustomizeConstrols"));
			toolsCustomizeControls.setAccelerator(KeyStroke.getKeyStroke("shift alt C"));
			toolsCustomizeControls.setIcon(Images.getIcon("org/multipage/generator/images/controls_icon.png"));
			
		JMenuItem toolsMimeTypes = new JMenuItem(Resources.getString("org.multipage.generator.menuToolsMimeTypes"));
			toolsMimeTypes.setAccelerator(KeyStroke.getKeyStroke("control M"));
			toolsMimeTypes.setIcon(Images.getIcon("org/multipage/generator/images/mime_icon.png"));
			
		JMenuItem toolsSettings = new JMenuItem(Resources.getString("org.multipage.generator.menuToolsSettings"));
			toolsSettings.setAccelerator(KeyStroke.getKeyStroke("alt S"));
			toolsSettings.setIcon(Images.getIcon("org/multipage/generator/images/settings_icon.png"));
			
		JMenuItem editResources = new JMenuItem(Resources.getString("org.multipage.generator.menuEditResources"));
			editResources.setAccelerator(KeyStroke.getKeyStroke("alt R"));
			editResources.setIcon(Images.getIcon("org/multipage/generator/images/resources_icon.png"));
			
		JMenuItem editTranslators = new JMenuItem(Resources.getString("org.multipage.generator.menuEditTranslations"));
			editTranslators.setAccelerator(KeyStroke.getKeyStroke("control T"));
			editTranslators.setIcon(Images.getIcon("org/multipage/generator/images/translator_icon.png"));
			
		JMenuItem editFileNames = new JMenuItem(Resources.getString("org.multipage.generator.menuEditFileNames"));
			editFileNames.setAccelerator(KeyStroke.getKeyStroke("alt F"));
			editFileNames.setIcon(Images.getIcon("org/multipage/generator/images/filenames_icon.png"));
			
		JMenuItem exportArea = new JMenuItem(Resources.getString("org.multipage.generator.menuFileExport"));
			exportArea.setAccelerator(KeyStroke.getKeyStroke("control E"));
			exportArea.setIcon(Images.getIcon("org/multipage/generator/images/export2_icon.png"));
			
		JMenuItem importArea = new JMenuItem(Resources.getString("org.multipage.generator.menuFileImport"));
			importArea.setAccelerator(KeyStroke.getKeyStroke("control I"));
			importArea.setIcon(Images.getIcon("org/multipage/generator/images/import2_icon.png"));

		JMenuItem toolsCheckRenderedFiles = new JMenuItem(Resources.getString("org.multipage.generator.menuToolsCheckRenderedFiles"));
			toolsCheckRenderedFiles.setAccelerator(KeyStroke.getKeyStroke("control alt C"));
			toolsCheckRenderedFiles.setIcon(Images.getIcon("org/multipage/generator/images/check_ambiguity_icon.png"));
		
		JMenuItem  helpMenuManualGui = new JMenuItem(Resources.getString("org.multipage.generator.menuMainHelpManualGui"));
			helpMenuManualGui.setAccelerator(KeyStroke.getKeyStroke("alt shift M"));
			helpMenuManualGui.setIcon(Images.getIcon("org/multipage/generator/images/manual.png"));
			
		JMenuItem  helpMenuManualGmpl = new JMenuItem(Resources.getString("org.multipage.generator.menuMainHelpManualGmpl"));
		helpMenuManualGmpl.setAccelerator(KeyStroke.getKeyStroke("alt shift G"));
		helpMenuManualGmpl.setIcon(Images.getIcon("org/multipage/generator/images/manual.png"));
			
		JMenuItem  helpMenuVideo = new JMenuItem(Resources.getString("org.multipage.generator.menuMainHelpVideo"));
			helpMenuVideo.setAccelerator(KeyStroke.getKeyStroke("alt shift V"));
			helpMenuVideo.setIcon(Images.getIcon("org/multipage/generator/images/video.png"));
			
		// Set size.
		fileMenuExit.setPreferredSize(preferredMenuSize);
		helpMenuAbout.setPreferredSize(preferredMenuSize);
		closeAllWindows.setPreferredSize(GeneratorMainFrame.preferredMenuSize);
		toolsCustomizeColors.setPreferredSize(GeneratorMainFrame.preferredMenuSize);
		editResources.setPreferredSize(GeneratorMainFrame.preferredMenuSize);
		
		// Create trayMenu tree.
		if (loginDialog != null) {
			file.add(loginDialog);
		}

		file.add(importArea);
		file.add(exportArea);
		file.add(render);
		file.add(updateData);
		file.add(fileMenuExit);
		
		help.add(helpMenuVideo);
		help.add(helpMenuManualGui);
		help.add(helpMenuManualGmpl);
		help.add(helpMenuAbout);
		
		tools.add(toolsSearch);
		addSearchInTextResourcesMenuItem(tools);
		tools.add(toolsCheckRenderedFiles);
		tools.add(toolsCustomizeColors);
		tools.add(toolsCustomizeControls);
		tools.add(toolsSettings);
		
		window.add(closeAllWindows);
		window.add(windowSelectionMenu);
		
		edit.add(editTranslators);
		edit.add(editFileNames);
		edit.add(editResources);
		addEditEnumerationsMenuItem(edit);
		addEditVersionsMenuItem(edit);
		edit.add(toolsMimeTypes);
		
		// Set listeners.
		if (loginDialog != null) {
			loginDialog.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					onLoginProperties();
				}});
		}
		// Add action listeners.
		fileMenuExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onFileExitMenu();
			}});
		helpMenuAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onHelpAboutMenu();
			}});
		updateData.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ConditionalEvents.transmit(GeneratorMainFrame.this, Signal.updateAreasModel);
			}});
		closeAllWindows.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onCloseAllWindows();
			}});
		toolsCustomizeColors.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onCustomizeColors();
			}});
		toolsCustomizeControls.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onCustomizeControls();
			}});
		toolsMimeTypes.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onMimeTypesEditor();
			}
		});
		toolsSettings.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onSettings();
			}
		});
		editResources.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onResources();
			}
		});
		editTranslators.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onTranslator();
			}
		});
		final Component thisComponent = this;
		render.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onRender(thisComponent);
			}
		});
		exportArea.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onExport();
			}
		});
		importArea.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onImport();
			}
		});
		editFileNames.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onEditFileNames();
			}
		});
		toolsSearch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onSearch();
			}
		});

		toolsCheckRenderedFiles.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onCheckRenderedFiles();
			}
		});
		helpMenuManualGui.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onManualGui();
			}
		});
		helpMenuManualGmpl.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onManualMaclan();
			}
		});
		helpMenuVideo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onVideo();
			}
		});
	}
	
	/**
	 * On logging dialog.
	 */
	protected void onLogging() {
		
		LoggingDialog.showDialog(GeneratorMainFrame.getFrame());
	}

	/**
	 * Add search in text resources trayMenu item.
	 * @param trayMenu
	 */
	protected void addSearchInTextResourcesMenuItem(JMenu menu) {
		
		// Override this method.
	}

	/**
	 * Add edit versions trayMenu item.
	 * @param trayMenu
	 */
	protected void addEditVersionsMenuItem(JMenuItem menu) {
		
		// Override this method.
	}

	/*
	 * Add edit enumerations trayMenu item.
	 */
	protected void addEditEnumerationsMenuItem(JMenu menu) {
		
		// Override this method.
	}

	/**
	 * On mime types editor.
	 */
	protected void onMimeTypesEditor() {

		MimeTypesEditor.showEditor(this);
	}

	/**
	 * On set user value.
	 */
	protected void onSetUserValue() {

		String message = String.format(Resources.getString("org.multipage.generator.messageInputUserValue"),
				userValue);
		String text = JOptionPane.showInputDialog(this, message, userValue);
		if (text == null) {
			return;
		}
		
		userValue = text;
		
		// Repaint diagram.
		repaint();
	}

	/**
	 * Customize controls.
	 */
	protected void onCustomizeControls() {

		// Open control customize dialog.
		cutomizeControls.setVisible(true);
	}

	/**
	 * On customize colors.
	 */
	public void onCustomizeColors() {
		
		// Open customize color dialog.
		customizeColors.setVisible(true);
	}

	/**
	 * On close all windows.
	 */
	protected void onCloseAllWindows() {

		tabPanel.closeAll();
	}

	/**
	 * Create tool bar..
	 */
	protected void createToolBar() {
		
		// Create StatusBar.
		toolBar = new JToolBar(Resources.getString("org.multipage.generator.textMainToolBar"));
		this.add(toolBar, BorderLayout.PAGE_START);
		
		toolBar.setFloatable(false);
		
		// Add buttons. 24 x 24 icons
		toolBar.addSeparator();
		if (ProgramBasic.isUsedLogin()) {
			ToolBarKit.addToolBarButton(toolBar, "org/multipage/generator/images/login_icon.png", this, "onLoginProperties", "org.multipage.generator.tooltipLoginWindow");
			toolBar.addSeparator();
		}
		showIdButton = ToolBarKit.addToggleButton(toolBar, "org/multipage/generator/images/show_hide_id.png", this, "onShowHideIds", "org.multipage.generator.tooltipShowHideIds");
		addHideSlotsButton(toolBar);
		toolBar.addSeparator();
		exposeReadOnly = ToolBarKit.addToggleButton(toolBar, "org/multipage/generator/images/enable_remove.png", this, "onExposeReadOnly", "org.multipage.generator.tooltipAreasUnprotected");
		toolBar.addSeparator();
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/generator/images/reload_icon.png", this, "onUpdate", "org.multipage.generator.tooltipUpdate");
		toolBar.addSeparator();
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/generator/images/center_icon.png", this, "onFocusBasicArea", "org.multipage.generator.tooltipFocusWhole");
		toolBar.addSeparator();
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/generator/images/focus_tab_big.png", this, "onFocusTabArea", "org.multipage.generator.tooltipFocus");
		toolBar.addSeparator();
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/generator/images/home_icon.png", this, "onFocusHome", "org.multipage.generator.tooltipFocusHome");
		toolBar.addSeparator();
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/generator/images/binoculars.png", this, "onSearch", "org.multipage.generator.tooltipSearch");
		toolBar.addSeparator();
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/generator/images/select_all_large.png", this, "onSelectAll", "org.multipage.generator.tooltipSelectAll");
		toolBar.addSeparator();
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/generator/images/deselect_all_large.png", this, "onUnselectAll", "org.multipage.generator.tooltipUnselectAll");
		toolBar.addSeparator();
		undoButton = ToolBarKit.addToolBarButton(toolBar, "org/multipage/generator/images/undo_focus.png", this, "onUndoFocus", "org.multipage.generator.tooltipUndoFocus");
		toolBar.addSeparator();
		redoButton = ToolBarKit.addToolBarButton(toolBar, "org/multipage/generator/images/redo_focus.png", this, "onRedoFocus", "org.multipage.generator.tooltipRedoFocus");
		toolBar.addSeparator();
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/generator/images/render.png", this, "onRenderTool", "org.multipage.generator.tooltipRenderHtmlPages");
		toolBar.addSeparator();
		toggleDebug = ToolBarKit.addToggleButton(toolBar,  "org/multipage/generator/images/debug.png", this, "onToggleDebug", "org.multipage.generator.tooltipEnableDisplaySourceCode");
		toolBar.addSeparator();
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/generator/images/revert.png", this, "onRevert", "org.multipage.generator.tooltipRevertExternalSourceCodes");
		toolBar.addSeparator();
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/generator/images/display_home_page.png", this, "onMonitorHomePage", "org.multipage.generator.tooltipMonitorHomePage");
		toolBar.addSeparator();
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/generator/images/about_icon.png", this, "onHelpAboutMenu", "org.multipage.generator.tooltipAbout");
		
		// Set undo and redo references.
		getAreaDiagram().setUndoRedoComponents(undoButton, redoButton);
	}
	
	/**
	 * On switch on or off debugging
	 */
	public void onToggleDebug() {
		
		final boolean selected = toggleDebug.isSelected();
		
		// Switch on or off debugging of PHP code
		Settings.setEnableDebugging(selected);
		
		// Refresh slot editors buttons
		SlotEditorHelper.refreshAll((SlotEditorHelper helper) -> {
			JToggleButton toggleDebug = helper.editor.getToggleDebug();
			if (toggleDebug != null) {
				toggleDebug.setSelected(selected);
			}
		});
	}

	/**
	 * Add hide slots button.
	 * @param toolBar
	 */
	protected void addHideSlotsButton(JToolBar toolBar) {
		
		// Do nothing.
	}

	/**
	 * On file exit trayMenu item.
	 */
	public void onFileExitMenu() {
		
		// Call events.
		closeWindow();
	}

	/**
	 * On help about trayMenu item.
	 */
	public void onHelpAboutMenu() {
	
		// Open about dialog window.
		AboutDialogBase aboutDlg = ProgramGenerator.newAboutDialog(this);
		aboutDlg.setVisible(true);
	}

	/**
	 * Focus on the Basic Area.
	 */
	public void onFocusBasicArea() {
		
		ConditionalEvents.transmit(this, AreasDiagram.class, Signal.focusBasicArea);
	}

	/**
	 * On login.
	 * @param ok
	 */
	public void onLoginCheck(boolean ok) {

		if (statusBar != null) {
			statusBar.setConnection(ok);
		}
	}

	/* (non-Javadoc)
	 * @see org.multipage.gui.MainFrame#onClose()
	 */
	protected void onClose() {
		
		// Stop dispatching events.
		ConditionalEvents.stopDispatching();
		
		// Cancel watch dog.
		if (timerWatchDog != null) {
			timerWatchDog.stop();
		}
		
		// Save windows data.
		customizeColors.saveIfDirty(false);
	}
	
	/**
	 * Set main tool bar text.
	 */
	public void setMainToolBarText(String text) {
		
		if (statusBar != null) {
			statusBar.setMainText(text);
		}
	}
	
	/**
	 * On login dialog.
	 */
	public void onLoginProperties() {
		
		// If the database changes, reset diagram position and zoom.
		Properties loginProperties = ProgramBasic.getLoginProperties();
		String oldDatabaseName = loginProperties.getProperty("database");
		
		ProgramBasic.setAttempts(3);
		
		String title = Resources.getString(
				ProgramGenerator.isExtensionToBuilder() ? "builder.textLoginDialog" :
					"org.multipage.generator.textLoginDialog");
		
		ProgramBasic.loginDialog(this, title);
		statusBar.setLoginProperties(ProgramBasic.getLoginProperties());
		ConditionalEvents.transmit(GeneratorMainFrame.this, Signal.newBasicArea);
		
		loginProperties = ProgramBasic.getLoginProperties();
		String newDatabaseName = loginProperties.getProperty("database");
		
		if (!newDatabaseName.equals(oldDatabaseName)) {
			getAreaDiagram().resetDiagramPosition();
		}
	}
	
	/**
	 * @return the tabPanel
	 */
	public TabPanel getTabPanel() {
		return tabPanel;
	}

	/**
	 * Get showing tab area ID or null if the area doesn't exist
	 * @return
	 */
	public static Long getTabAreaId() {
		
		Long tabAreaId = getFrame().tabPanel.getTopAreaIdOfSelectedTab();
		return tabAreaId;
	}
	
	/**
	 * On show/hide IDs.
	 */
	public void onShowHideIds() {
		
		// Toggle show IDs flags.
		boolean showIds = showIdButton.isSelected();
		showIDs(showIds);
	}
	
	/**
	 * Show hide IDs.
	 */
	public void showIDs(boolean show) {
		
		Area.setShowId(show);
		Slot.setShowId(show);
		
		showIDsExtended(show);
		
		ConditionalEvents.transmit(GeneratorMainFrame.this, Signal.showOrHideIds, show);
	}

	/**
	 * Show IDs extended.
	 */
	protected void showIDsExtended(boolean show) {
		
	}

	/**
	 * @return the areaDiagramEditor
	 */
	public AreasDiagram getAreaDiagram() {
		return mainAreaDiagramEditor.getDiagram();
	}
	
	/**
	 * Area diagram editor.
	 */
	public AreasDiagramPanel getAreaDiagramEditor() {
		
		AreasDiagramPanel editor = getVisibleAreasEditor();
		if (editor != null) {
			return editor;
		}
		return mainAreaDiagramEditor;
	}
	
	/**
	 * On search.
	 */
	public void onSearch() {
		
		// Do modeless dialog.
		searchDialog.setVisible(true);
	}
	
	/**
	 * Set properties visible.
	 * @param visible
	 */
	public void setPropertiesVisible(boolean visible) {
		
		if (visible) {
			splitDiagramProperties.maximize();
		}
		else {
			splitDiagramProperties.minimize();
		}
	}
	
	/**
	 * Select all areas.
	 */
	public void onSelectAll() {
		
		// Select all areas.
		ConditionalEvents.transmit(this, Signal.selectAll);
		j.log("TRANSMITTED 7 showAreasProperties %s", "");
		// Show areas' properties.
		ConditionalEvents.transmit(this, Signal.showAreasProperties);
	}
	
	/**
	 * Unselect all afreas.
	 */
	public void onUnselectAll() {
		
		j.log("TRANSMITTED 8 showAreasProperties %s", "");
		// Unselect all areas.
		ConditionalEvents.transmit(this, Signal.unselectAll);
		// Show areas' properties.
		ConditionalEvents.transmit(this, Signal.showAreasProperties);
	}

	/**
	 * Gets user value
	 * @return
	 */
	public String getUserValue() {

		return userValue;
	}

	/**
	 * On settings.
	 */
	protected void onSettings() {
		
		// Show settings dialog.
		Settings.showDialog(this);
	}

	/**
	 * On resources.
	 */
	protected void onResources() {

		// Edit resources.
		ResourcesEditorDialog.showDialog(this);
	}

	/**
	 * On dictionary.
	 */
	protected void onTranslator() {
		
		// Get selected areas.
		LinkedList<Area> areas = mainFrame.getAreaDiagram().getSelectedAreas();
		// Show dialog.
		GeneratorTranslatorDialog.showDialog(this, areas);
	}

	/**
	 * On update data.
	 */
	public void onUpdate() {
		
		ConditionalEvents.transmit(GeneratorMainFrame.this, Signal.updateAll);
	}

	/**
	 * Main frame getter method.
	 * @return
	 */
	public static GeneratorMainFrame getFrame() {
		return mainFrame;
	}

	/**
	 * Get selected areas.
	 * @param areas
	 */
	public LinkedList<Area> getSelectedAreas() {

		return mainAreaDiagramEditor.getSelectedAreas();
	}
	
	/**
	 * Focus on home area.
	 */
	public void onFocusHome() {
		
		// Propagate event.
		ConditionalEvents.transmit(this, Signal.focusHomeArea);
	}

	/**
	 * Get area properties editor.
	 * @return
	 */
	public AreasPropertiesBase getAreasProperties() {

		return propertiesPanel.getPropertiesEditor();
	}

	/**
	 * On undo focus.
	 */
	public void onUndoFocus() {
		
		AreasDiagramPanel editor = getVisibleAreasEditor();
		if (editor != null) {
			// Delegate the call.
			editor.getDiagram().undoFocus();
		}
	}
	
	/**
	 * On redo focus.
	 */
	public void onRedoFocus() {
		
		AreasDiagramPanel editor = getVisibleAreasEditor();
		if (editor != null) {
			// Delegate the call.
			editor.getDiagram().redoFocus();
		}
	}
	
	/**
	 * On expose read only areas.
	 */
	public void onExposeReadOnly() {
		
		// Transmit "expose read only areas" signal.
		ConditionalEvents.transmit(GeneratorMainFrame.this, AreasDiagram.class, Signal.exposeReadOnlyAreas);
	}

	/**
	 * Get areas locked flag.
	 * @return
	 */
	public static boolean areasLocked() {
		
		return !mainFrame.exposeReadOnly.isSelected();
	}
	
	/**
	 * Clone areas diagram.
	 * @param diagramName
	 * @param topAreaId
	 */
	public AreasDiagramPanel createAreasDiagram(String diagramName, Long topAreaId) {
		
		// Create new areas editor.
		AreasDiagramPanel newAreasEditor = new AreasDiagramPanel();
		AreasDiagramPanel currentAreasEditor = getVisibleAreasEditor();
		
		newAreasEditor.initDiagramEditor(currentAreasEditor);
		
		// Set undo and redo references.
		AreasDiagram diagram = newAreasEditor.getDiagram();
		
		// Get current diagram.
		AreasDiagram currentDiagram = getVisibleAreasEditor().getDiagram();
		double translationX = currentDiagram.getTranslatingX();
		double translationY = currentDiagram.getTranslatingY();
		double zoom = currentDiagram.getZoom();
		
		// Add new tab.
		tabPanel.addAreasEditor(newAreasEditor, TabType.areasDiagram, diagramName, topAreaId, false);
		diagram.setDiagramPosition(translationX, translationY, zoom);
		diagram.setUndoRedoComponents(undoButton, redoButton);
		
		// Select the new tab.
		int count = tabPanel.getTabCount();
		tabPanel.setSelectedIndex(count - 1);
		
		// Update window selection trayMenu.
		updateWindowSelectionMenu();
		
		return newAreasEditor;
	}
	
	/**
	 * Create area tree view.
	 * @param title
	 * @param displayedAreas 
	 * @param areaId
	 */
	private AreasTreeEditorPanel createAreasTreeView(String title, Long rootAreaId, Long [] displayedAreas) {
		
		// Trim input.
		if (rootAreaId == null) {
			rootAreaId = 0L;
		}
		
		// Add new tree view.
		AreasTreeEditorPanel areasTreePanel = new AreasTreeEditorPanel(rootAreaId);
		areasTreePanel.displayAreaIds(displayedAreas);
		tabPanel.addAreasEditor(areasTreePanel, TabType.areasTree, title, rootAreaId, true);
		
		// Select the new tab.
		int count = tabPanel.getTabCount();
		tabPanel.setSelectedIndex(count - 1);
		
		return areasTreePanel;
	}
	
	/**
	 * Clone areas diagram.
	 */
	public void cloneAreasDiagram(Area focusArea) {
		
		// Get current areas diagram.
		AreasDiagram diagram = GeneratorMainFrame.getVisibleAreasDiagram();
		
		// If the area connection is in progress, inform user and exit.
		if (diagram.isAreaConnection()) {
			diagram.escapeDiagramModes();
		}
		
		// Get selected area.
		LinkedList<Area> selectedAreas = getSelectedAreas();
		
		// Get area to clone.
		boolean useSelectedArea = selectedAreas.size() == 1;
		final Area area = focusArea != null ? focusArea : 
			(useSelectedArea ? selectedAreas.getFirst() : getBiggestVisibleArea());
		
		// Get biggest visible area shape.
		String areaName = area != null ? area.getDescription() : "";
		
		// Get diagram description.
		Obj<TabType> type = new Obj<TabType>();
		String title = ClonedDiagramDialog.showDialog(this, areaName, type);
		if (title == null) {
			return;
		}
		
		Long areaId = area == null ? null : area.getId();
		
		// On diagram.
		if (TabType.areasDiagram.equals(type.ref)) {
			
			// Clone diagram.
			final AreasDiagramPanel areasDiagramEditor = createAreasDiagram(title, areaId);
			
			// Focus area.
			if (areasDiagramEditor != null) {
				SwingUtilities.invokeLater(() -> {
						areasDiagramEditor.focusAreaNear(area.getId());
				});
			}
		}
		// On tree view.
		else if (TabType.areasTree.equals(type.ref)) {
			
			// Clone tree view.
			createAreasTreeView(title, areaId, new Long [] {});
		}
	}
	
	/**
	 * Add monitor tab.
	 * @param url
	 */
	public void addMonitor(String url) {
		
		tabPanel.addMonitor(url, true);
	}
	
	/**
	 * Get biggest visible area.
	 * @return
	 */
	private Area getBiggestVisibleArea() {
		
		return getVisibleAreasEditor().getDiagram().getBiggestVisibleArea();
	}
	
	/**
	 * Get visible area editor.
	 * @return
	 */
	public AreasDiagramPanel getVisibleAreasEditor() {
		
		int index = tabPanel.getSelectedIndex();
		if (index == -1) {
			return null;
		}
		
		Component component = tabPanel.getComponentAt(index);
		if (component instanceof AreasDiagramPanel) {
			
			AreasDiagramPanel editor = (AreasDiagramPanel) component;
			return editor;
		}
		
		// Otherwise return main editor.
		component = tabPanel.getComponentAt(0);
		if (component instanceof AreasDiagramPanel) {
			
			AreasDiagramPanel editor = (AreasDiagramPanel) component;
			return editor;
		}
		
		return null;
	}

	/**
	 * Find and view area help.
	 * @param area
	 */
	public void findViewAreaHelp(Area area) {
		
		LinkedList<Long> foundAreaIds = new LinkedList<Long>();
		MiddleResult result = ProgramBasic.getMiddle().findSuperAreaWithHelp(
				ProgramBasic.getLoginProperties(), area.getId(), foundAreaIds);
		if (result.isNotOK()) {
			result.show(this);
			return;
		}
		
		LinkedList<Area> foundAreas = new LinkedList<Area>();
		// Load areas.
		for (long foundAreaId : foundAreaIds) {
			
			Area foundArea = ProgramGenerator.getAreasModel().getArea(foundAreaId);
			foundAreas.add(foundArea);
		}
		
		// Add constructor area to the list.
		Long constructorId = area.getConstructorHolderId();
		if (constructorId != null) {
			
			// Load constructor area.
			Properties login = ProgramBasic.getLoginProperties();
			Middle middle = ProgramBasic.getMiddle();
			
			Obj<Long> constructorAreaId = new Obj<Long>();
			
			result = middle.loadConstructorHolderAreaId(login, constructorId, constructorAreaId);
			if (result.isOK() && constructorAreaId.ref != null) {
			
				Area constructorArea = ProgramGenerator.getArea(constructorAreaId.ref);
				foundAreas.add(constructorArea);
			}
			
			// Logout and possibly show an error
			MiddleResult logoutResult = middle.logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
			
			if (result.isNotOK()) {
				result.show(this);
			}
		}
		
		if (!foundAreas.isEmpty()) {
			// View help.
			AreaHelpViewer.showDialog(this, foundAreas);
		}
		else {
			Utility.show(this, "org.multipage.generator.messageHelpAreaNotFound");
		}
	}
	
	/**
	 * On render HTML pages.
	 */
	public void onRenderTool() {
		
		onRender(this);
	}
	
	/**
	 * On render HTML pages.
	 */
	public void onRender(Component parentComponent) {
		
		final LinkedList<Area> areasToRender = new LinkedList<Area>();
		
		// Get selected areas.
		final LinkedList<Area> areas = mainAreaDiagramEditor.getSelectedAreas();
		if (!areas.isEmpty()) {
			areasToRender.addAll(areas);
		}
		else {
			//Utility.show(parentComponent, "org.multipage.generator.messagePleaseSelectAreasToRender");
			//return;
		
			// Get home area.
			Area homeArea = ProgramGenerator.getHomeArea();
			if (!homeArea.isVisible()) {
				Utility.show(parentComponent, "org.multipage.generator.messageHomeAreaIsNotVisible");
				return;
			}
			areasToRender.add(homeArea);
		}

		final LinkedList<Language> languages = new LinkedList<Language>();
		final Obj<String> target = new Obj<String>(TextRenderer.serializedTarget);
		final Obj<String> coding = new Obj<String>("UTF-8");
		final Obj<Boolean> showTextIds = new Obj<Boolean>(false);
		final Obj<BrowserParameters> browserParameters = new Obj<BrowserParameters>();
		final Obj<Boolean> generateList = new Obj<Boolean>();
		final Obj<Boolean> generateIndex = new Obj<Boolean>();
	    Obj<Boolean> runBrowser = new Obj<Boolean>(true);
	    Obj<Boolean> removeOldFiles = new Obj<Boolean>(false);
	    final LinkedList<VersionObj> versions = new LinkedList<VersionObj>();
	    final Obj<Boolean> renderRelatedAreas = new Obj<Boolean>(true);
		
		if (!RenderDialog.showDialog(parentComponent, languages, false, target, coding, showTextIds,
				browserParameters, generateList, generateIndex, runBrowser, removeOldFiles,
				versions, renderRelatedAreas)) {
			return;
		}
		
		// Check ambiguous file names.
		LinkedList<AmbiguousFileName> ambiguousFileNames = new LinkedList<AmbiguousFileName>();
		CheckRenderedFiles.getAmbiguousFileNames(areasToRender, versions, ambiguousFileNames);
		
		if (!ambiguousFileNames.isEmpty()) {
			
			// Ask user.
			if (Utility.ask(parentComponent, "org.multipage.generator.messageFileNameAmbiguitiesFound")) {
				if (!CheckRenderedFiles.showDialogModal(parentComponent, areasToRender)) {
					return;
				}
			}
		}
		
		// Save target.
		TextRenderer.serializedTarget = target.ref;
		
		// Remove old files.
		if (removeOldFiles.ref) {
			if (!Utility.deleteFolderContent(target.ref)) {
				return;
			}
		}
		
		String _pagesTarget = target.ref;
		
		// Correct target.
		if (browserParameters.ref != null) {
			
			String pagesFolder = browserParameters.ref.getFolder();
			if (!pagesFolder.isEmpty()) {
				
				_pagesTarget += File.separatorChar + pagesFolder;
				if (!makeFolder(_pagesTarget)) {
					return;
				}
			}
		}

		final String pagesTarget = _pagesTarget;
		final LinkedList<String> pageFileNames = new LinkedList<String>();

		// Create progress dialog.
		ProgressDialog<Object> progressDialog = new ProgressDialog<Object>(
				parentComponent, Resources.getString("org.multipage.generator.textRenderingHtmlPages"),
				_pagesTarget);
		
		ProgressResult progressResult = progressDialog.execute(new SwingWorkerHelper<Object>() {
			// Background process.
			@Override
			protected Object doBackgroundProcess() throws Exception {
				
				// Render HTML pages.
				TextRenderer renderer = new TextRenderer(ProgramBasic.getLoginProperties());
				renderer.setSkipErrorFiles(false);
				renderer.setCommonResourceFileNamesFlag(Settings.getCommonResourceFileNamesFlag());
				renderer.setResourcesFolder(Settings.getResourcesRenderFolder());
				
				try {
					renderer.render(areasToRender, languages, versions, coding.ref, showTextIds.ref, generateList.ref,
							generateIndex.ref, Settings.getExtractedCharacters(),
							pagesTarget, pageFileNames, renderRelatedAreas, this);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				
				renderer.dispose();
				
				// On error throw exception.
				if (renderer.isError()) {
					throw new Exception();
				}
				
				return null;
			}
		});
		
		// On error delete created files and exit the method.
		if (progressResult != ProgressResult.OK) {
			
			if (Settings.isRemovePartiallyGenerated()) {
				
				// Ask user and delete files.
				if (!Utility.deleteFolderContent(target.ref)) {
					return;
				}
			}
		}

		if (browserParameters.ref != null) {
			
			if (!pageFileNames.isEmpty()) {
				
				// Check if the home page has been generated.
				if (!checkHomePageExists(browserParameters.ref, pageFileNames)) {
					
					String message = String.format(
							Resources.getString("org.multipage.generator.textHomePageNotGeneratedSelectNew"),
							browserParameters.ref.getHomePage());
					
					String pageName = SelectStringDialog.showDialog(parentComponent, pageFileNames,
							"org/multipage/generator/images/home_page.png", "org.multipage.generator.textSelectHomePage",
							message);
					if (pageName != null) {
						browserParameters.ref.setHomePage(pageName);
						BrowserParametersDialog.serializedBrowserParameters.setHomePage(pageName);
					}
					else {
						return;
					}
				}
				// Create and run browser.
				if (generateBrowser(target.ref, browserParameters.ref) && runBrowser.ref) {
					
					runBrowser(target.ref, browserParameters.ref);
				}
			}
			else {
				Utility.show(parentComponent, "org.multipage.generator.textNoFilesGeneratedNoVisibleAreas");
			}
		}
	}

	/**
	 * Check if the home page has been generated.
	 * @param browserParameters
	 * @param pageFileNames
	 */
	private boolean checkHomePageExists(BrowserParameters browserParameters,
			LinkedList<String> pageFileNames) {
		
		for (String pageFileName : pageFileNames) {
			
			if (browserParameters.getHomePage().equals(pageFileName)) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Makes folder.
	 * @param folderPath
	 * @return
	 */
	private boolean makeFolder(String folderPath) {
		
		File folder = new File(folderPath);
		
		try {
			folder.mkdirs();
		}
		catch (Exception e) {
			Utility.show2(this, String.format(
					Resources.getString("org.multipage.generator.messageCannotCreateHtmlDirectory"), e.getMessage()));
			return false;
		}
		
		return true;
	}
	
	/**
	 * Generate browser.
	 * @param target
	 * @param browserParameters
	 * @return
	 */
	private boolean generateBrowser(String target,
			BrowserParameters browserParameters) {
		
		// If the browser name is empty, ask user.
		if (browserParameters.getBrowserProgramName().isEmpty()) {
			String name = Utility.input(this, "org.multipage.generator.messageInsertBrowserProgramName");
			if (name == null) {
				return false;
			}
			name = name.trim();
			if (name.isEmpty()) {
				return false;
			}
			BrowserParametersDialog.getParameters().setBrowserProgramName(name);
			browserParameters.setBrowserProgramName(name);
		}
		
		// Save browser EXE file.
		if (!saveBrowser(target, browserParameters)) {
			return false;
		}
		
		// Save browser properties.
		if (!saveBrowserProperties(target, browserParameters)) {
			return false;
		}
		
		// Save autorun file.
		if (!saveAutorunFile(target, browserParameters)) {
			return false;
		}
		
		return true;
	}

	/**
	 * Run browser.
	 * @param target
	 * @param browserParameters 
	 * @param browserParameters
	 */
	protected void runBrowser(String target, BrowserParameters browserParameters) {
		
		String exeFileName = browserParameters.getBrowserProgramName() + ".exe";
		String browserFullName = target + File.separatorChar + exeFileName;
		
		try {
			Runtime.getRuntime().exec(browserFullName);
		}
		catch (IOException e) {
			
			Utility.show(this, String.format(
					Resources.getString("org.multipage.generator.messageCannotRunBrowser"), exeFileName,
					e.getMessage()));
		}
	}

	/**
	 * Save browser properies.
	 * @param target
	 * @param browserParameters
	 * @return
	 */
	private boolean saveBrowserProperties(String target,
			BrowserParameters browserParameters) {
		
		FileOutputStream outputStream = null;
		OutputStreamWriter writer = null;
		
		try {
			outputStream = new FileOutputStream(target + File.separatorChar + "Properties.ini");
			writer = new OutputStreamWriter(outputStream, "UTF-16LE");
			
			writer.write("[Settings]\r\n");
			writer.write("\r\nRelativeUrl = " + browserParameters.getRelativeUrl());
			writer.write("\r\nTitle = " + browserParameters.getTitle());
			writer.write("\r\nDefaultMessage = " + browserParameters.getMessage());
			writer.write("\r\nWindowSize = " + browserParameters.getWindowSizeText());
			writer.write("\r\nWindowMaximized = " + browserParameters.getMaximizedText());
		}
		catch (Exception e) {
			
			String message = String.format(
					Resources.getString("org.multipage.generator.messageCannotCreateBrowserProperties"),
					e.getMessage());
			
			Utility.show2(this, message);
			return false;
		}
		finally {
			try {
				if (writer != null) {
					writer.close();
				}
				if (outputStream != null) {
					outputStream.close();
				}
			}
			catch (Exception e) {
			}
		}
		
		return true;
	}
	
	/**
	 * Save autorun file.
	 * @param target
	 * @param browserParameters
	 * @return
	 */
	private boolean saveAutorunFile(String target,
			BrowserParameters browserParameters) {
		
		if (browserParameters.isCreateAutorun()) {

			FileOutputStream outputStream = null;
			OutputStreamWriter writer = null;
			
			try {
				outputStream = new FileOutputStream(target + File.separatorChar + "Autorun.inf");
				writer = new OutputStreamWriter(outputStream, "UTF-16LE");
				
				writer.write("[autorun]\r\n");
				writer.write("\r\nopen = " + browserParameters.getBrowserProgramName() + ".exe");
			}
			catch (Exception e) {
				
				String message = String.format(
						Resources.getString("org.multipage.generator.messageCannotCreateBrowserProperties"),
						e.getMessage());
				
				Utility.show2(this, message);
				return false;
			}
			finally {
				try {
					if (writer != null) {
						writer.close();
					}
					if (outputStream != null) {
						outputStream.close();
					}
				}
				catch (Exception e) {
				}
			}
		}
		
		return true;
	}

	/**
	 * Save browser.
	 * @param target
	 * @param browserParameters
	 * @return
	 */
	private boolean saveBrowser(String target,
			BrowserParameters browserParameters) {
		
		FileOutputStream browserOutputStream = null;
		InputStream inputStream = null;
		
		try {
			browserOutputStream = new FileOutputStream(
					target + File.separatorChar + browserParameters.getBrowserProgramName() + ".exe");
			
			inputStream = getClass().getResourceAsStream("/org/multipage/generator/browser/Browser.exe");
			
			// Coopy data.
			final int bufferLength = 2^16;
			byte [] buffer = new byte [bufferLength];
			
			while (true) {
				
				int bytesRead = inputStream.read(buffer);
				
				if (bytesRead == -1) {
					break;
				}
				
				browserOutputStream.write(buffer, 0, bytesRead);
			}
		}
		catch (Exception e) {
			Utility.show2(this, String.format(
					Resources.getString("org.multipage.generator.messageCannotGenerateBrowser"), e.getMessage()));
			return false;
		}
		finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
				if (browserOutputStream != null) {
					browserOutputStream.close();
				}
			}
			catch (Exception e) {
			}
		}

		return true;
	}
	
	/**
	 * Edit start resource.
	 * @param area
	 * @param inherits 
	 */
	public static void editStartResource(Area area, boolean inherits) {
		
		Component parentComponent = GeneratorMainFrame.getFrame();
		
		Obj<Long> versionId = new Obj<Long>(0L);
		
		if (inherits) {
			
			// Select version.
			Obj<VersionObj> version = new Obj<VersionObj>();
			
			if (!SelectVersionDialog.showDialog(parentComponent, version)) {
				return;
			}
			
			// Get selected version ID.
			versionId.ref = version.ref.getId();
			
			// Get inherited area.
			Area inheritedArea = ProgramGenerator.getAreasModel().getStartArea(area, versionId.ref);
			if (inheritedArea != null) {
				area = inheritedArea;
			}
		}
		
		// Load area source.
		Middle middle = ProgramBasic.getMiddle();
		Properties login = ProgramBasic.getLoginProperties();
		Obj<Long> resourceId = new Obj<Long>(0L);
		
		MiddleResult result = middle.loadAreaSource(login, area.getId(), versionId.ref, resourceId);
		if (result.isNotOK()) {
			result.show(null);
			return;
		}
		
		// Load old style start resource if not loaded.
		if (resourceId.ref == null) {
			result = middle.loadContainerStartResource(login, area, resourceId, versionId, null);
			if (result.isNotOK()) {
				result.show(null);
				return;
			}
		}
		
		if (resourceId.ref == 0L) {
			Utility.show(null, "org.multipage.generator.messageAreaHasNoStartResource");
			return;
		}
		
		// Get saving method.
		Obj<Boolean> savedAsText = new Obj<Boolean>();
		result = middle.loadResourceSavingMethod(login, resourceId.ref, savedAsText);
		if (result.isNotOK()) {
			result.show(null);
			return;
		}

		// Edit text resource.
		TextResourceEditor.showDialog(parentComponent, resourceId.ref,
				savedAsText.ref, false);
	}

	/**
	 * Display area.
	 * @param area
	 */
	public static void displayRenderedArea(Area area) {
		
		// If the area is not visible exit the method.
		if (!area.isVisible()) {
			Utility.show(getFrame(), "org.multipage.generator.messageAreaNotVisible");
			return;
		}
		
		final LinkedList<Area> areas = new LinkedList<Area>();
		final LinkedList<Language> languages = new LinkedList<Language>();
		final LinkedList<VersionObj> versions = new LinkedList<VersionObj>();
		
		areas.add(area);
		
		// Show rendering dialog.
		final Obj<String> target = new Obj<String>(TextRenderer.serializedTarget);
		final Obj<String> coding = new Obj<String>("UTF-8");
		final Obj<Boolean> showTextIds = new Obj<Boolean>(false);
		Obj<Boolean> removeOldFiles = new Obj<Boolean>(false);
		final Obj<Boolean> renderRelatedAreas = new Obj<Boolean>(true);
		
		if (!RenderDialog.showDialog(getFrame(), languages, false, target, coding, showTextIds,
				null, null, null, null, removeOldFiles, versions, renderRelatedAreas)) {
			return;
		}
		
		if (languages.isEmpty()) {
			Utility.show(getFrame(), "org.multipage.generator.messageErrorLanguageListIsEmpty");
			return;
		}
		
		TextRenderer.serializedTarget = target.ref;
		
		// Remove old files.
		if (removeOldFiles.ref) {
			if (!Utility.deleteFolderContent(target.ref)) {
				return;
			}
		}
		
		final LinkedList<String> fileNames = new LinkedList<String>();
		
		// Create progress dialog.
		ProgressDialog<MiddleResult> progressDialog = new ProgressDialog<MiddleResult>(
				null, Resources.getString("org.multipage.generator.textRenderingHtmlPages"),
				target.ref);
		
		ProgressResult progressResult = progressDialog.execute(new SwingWorkerHelper<MiddleResult>() {
			// Background process.
			@Override
			protected MiddleResult doBackgroundProcess() throws Exception {
				
				// Render HTML pages.
				TextRenderer renderer = null;
				
				try {
					renderer = new TextRenderer(ProgramBasic.getLoginProperties());
					
					renderer.setSkipErrorFiles(false);
					renderer.setCommonResourceFileNamesFlag(Settings.getCommonResourceFileNamesFlag());
					renderer.setResourcesFolder(Settings.getResourcesRenderFolder());
					
					renderer.render(areas, languages, versions, coding.ref, showTextIds.ref, false,
							false, 0, target.ref, fileNames, renderRelatedAreas, this);
					renderer.dispose();
				}
				catch (Exception e) {
					if (renderer != null) {
						renderer.dispose();
					}
					return new MiddleResult(null, e.getMessage());
				}
				return MiddleResult.OK;
			}});
		
		// On progress dialog error exit the method.
		if (progressResult != ProgressResult.OK) {
			return;
		}
		
		// On output error.
		MiddleResult result = progressDialog.getOutput();
		if (result.isNotOK()) {
			Utility.show2(null, result.getMessage());
			return;
		}
		
		// If no files generated, inform user.
		if (fileNames.isEmpty()) {
			Utility.show(getFrame(), "org.multipage.generator.messageNoFilesRendered");
			return;
		}
		
		// Open the rendered HTML in the browser.
		if (Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();
			if (desktop.isSupported(Desktop.Action.BROWSE)) {
				try {
					long startLanguageId = getStartLanguage();
					
					Language language = null;
					
					for (Language languageItem : languages) {
						if (languageItem.id == startLanguageId) {
							language = languageItem;
						}
					}
											
					if (language == null) {
						language = languages.getFirst();
					}
					
					String fileName = fileNames.getFirst();

					String pathName = TextRenderer.serializedTarget + "/" + fileName;
					pathName = pathName.replaceAll("\\\\", "/");
					
					// Open URL.
					BareBonesBrowserLaunch.openURL(pathName);
				}
				catch(Exception e) {
					Utility.show2(getFrame(), e.getMessage());
				}
			}
			else {
				Utility.show(getFrame(), "org.multipage.generator.messagePlatformDesktopBrowseNotSupported");
			}
		}
		else {
			Utility.show(getFrame(), "org.multipage.generator.messagePlatformDesktopClassNotSupported");
		}
	}
	

	/**
	 * Display online area.
	 * @param area
	 */
	public void displayOnlineArea(Area area, Language language, VersionObj version, Boolean showTextIds, String parametersOrUrl, Boolean externalBrowser) {
		
		if (Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();
			if (desktop.isSupported(Desktop.Action.BROWSE)) {
				
				try {
					
					// Try to open URL.
					String url = null;
					URL urlObject = null;
					try {
						urlObject = Utility.tryUrl(parametersOrUrl);
					}
					catch (MalformedURLException e1) {
						urlObject = null;
					}
					catch (IOException e3) {
						Utility.show2(this, e3.getLocalizedMessage());
						return;
					}
					
					// Use parameters instead.
					if (urlObject == null) {
						
						long areaId = area.getId();
						Long languageId = language != null ? language.id : 0L;
						long versionId = version != null ? version.getId() : 0L;
						
						// Load start language.
						Properties login = ProgramBasic.getLoginProperties();
						Obj<Long> startLanguageId = new Obj<Long>(0L);
						ProgramBasic.getMiddle().loadStartLanguageId(login, startLanguageId);
						
						// Get home area ID.
						Area homeArea = ProgramGenerator.getHomeArea();
						long homeAreaId = 0;
						
						if (homeArea != null) {
							homeAreaId = homeArea.getId();
						}
						
						if (parametersOrUrl == null) {
							parametersOrUrl = "";
						}
						if (!parametersOrUrl.isEmpty()) {
							parametersOrUrl = '&' + parametersOrUrl;
						}
						
						if (areaId == homeAreaId && versionId == 0L && languageId == startLanguageId.ref) {
							url = String.format("http://localhost:%d/?%s%s", Settings.getHttpPortNumber(), ProgramServlet.displayHomeArea, parametersOrUrl);
						}
						else {
							url = String.format("http://localhost:%d/?%s&area_id=%d&lang_id=%d&ver_id=%d%s",
									Settings.getHttpPortNumber(), ProgramServlet.displayHomeArea, areaId, languageId, versionId, parametersOrUrl);
						}
						
						// Display localized text.
						if (showTextIds != null && showTextIds) {
							url += "&l";
						}
					}
					else {
						url = urlObject.toString();
					}
					
					if (externalBrowser != null & externalBrowser) {
						// Show external browser.
						BareBonesBrowserLaunch.openURL(url);
					}
					else {
						// Add monitor (internal browser).
						addMonitor(url);
					}
				}
				catch(Exception e) {
					Utility.show2(getFrame(), e.getMessage());
				}
			}
			else {
				Utility.show(getFrame(), "org.multipage.generator.messagePlatformDesktopBrowseNotSupported");
			}
		}
		else {
			Utility.show(getFrame(), "org.multipage.generator.messagePlatformDesktopClassNotSupported");
		}
	}
	
	/**
	 * Display online area.
	 * @param area
	 */
	public void displayOnlineArea(Area area) {
		
		if (Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();
			if (desktop.isSupported(Desktop.Action.BROWSE)) {
					
				// Get online display parameters.
				Obj<Language> language = new Obj<Language>();
				Obj<VersionObj> version = new Obj<VersionObj>();
				Obj<Boolean> showTextIds = new Obj<Boolean>();
				Obj<String> parametersOrUrl = new Obj<String>();
				Obj<Boolean> externalBrowser = new Obj<Boolean>();
				
				if (!DisplayOnlineDialog.showDialog(getFrame(), language, version, showTextIds, parametersOrUrl, externalBrowser)) {
					return;
				}
				
				// Delegate the call.
				displayOnlineArea(area, language.ref, version.ref, showTextIds.ref, parametersOrUrl.ref, externalBrowser.ref);
			}
		}
	}

	/**
	 * Display IDE helper area.
	 * @param area
	 */
	public void displayIdeHelperArea(Area area) {
		
		// Get current IDE language alias.
		String languageAlias = GeneratorMain.defaultLanguage;
		
		Language pageLanguage = null;
		
		try {
			// Login middle layer.
			Middle middle = ProgramBasic.loginMiddle();
			
			// Load languages.
			LinkedList<Language> languages = new LinkedList<Language>();
			
			MiddleResult result = middle.loadLanguages(languages);
			result.throwPossibleException();
			
			// Find language with given alias.
			for (Language language : languages) {
				
				// Return found language
				if (language.alias.equals(languageAlias)) {
					pageLanguage = language;
					break;
				}
			}
		}
		catch (Exception e) {
			// Display error message.
			Utility.show2(getFrame(), e.getLocalizedMessage());
		}
		finally {
			// Logout middle layer.
			ProgramBasic.logoutMiddle();
		}
		
		// Delegate the call.
		displayOnlineArea(area, pageLanguage, null, null, null, false);
	}
	
	/**
	 * Get start language.
	 * @return
	 */
	private static long getStartLanguage() {
		
		Obj<Long> startLanguageId = new Obj<Long>();
		
		// Login to the database.
		Middle middle = ProgramBasic.getMiddle();
		Properties login = ProgramBasic.getLoginProperties();
		MiddleResult result;

		// Load start language ID.
		result = middle.loadStartLanguageId(login, startLanguageId);

		if (result.isNotOK()) {
			result.show(getFrame());
			return 0L;
		}
		
		return startLanguageId.ref;
	}

	/**
	 * On test.
	 */
	protected void onTest() {
		
		// Get selected areas.
		LinkedList<Area> areas = getSelectedAreas();
		if (areas.size() != 1) {
			Utility.show(this, "org.multipage.generator.messageSelectSingleArea");
			return;
		}
		
		// Generate areas.
		GenerateAreasDialog.showDialog(this, areas.getFirst());
	}

	/**
	 * On export data.
	 */
	protected void onExport() {
		
		// Get selected areas.
		LinkedList<Area> areas = getSelectedAreas();
		if (areas.size() != 1) {
			Utility.show(this, "org.multipage.generator.messageSelectSingleArea");
			return;
		}
		
		// Get super area.
		Area area = areas.getFirst();
		exportArea(area, this);
	}
	
	/**
	 * Export area.
	 * @param area
	 */
	public static void exportArea(Area area, Component parent) {
		
		LinkedList<Area> superAreas = area.getSuperareas();
		Area parentArea = null;
		
		if (superAreas.size() == 1) {
			parentArea = superAreas.getFirst();
		}
		else if (superAreas.size() > 1) {
			
			LinkedList<AreaRelation> uniqueSuperAereaRelations = area.getUniqueSuperAreaRelations();
			if (uniqueSuperAereaRelations.size() == 1) {
				
				parentArea = superAreas.getFirst();
			}
			else {
				
				// Try to find selected area shape super area.
				AreaCoordinates lastSelectedCoordinates = getVisibleAreasDiagram().getLastSelectedAreaCoordinates();
				if (lastSelectedCoordinates != null) {
					
					parentArea = lastSelectedCoordinates.getParentArea();
				}
				
				if (parentArea == null) {
					
					// Select super area edge.
					parentArea = SelectSuperAreaDialog.showDialog(parent, area);
					if (parentArea == null) {
						return;
					}
				}
			}
		}
		
		// Open export dialog.
		ExportDialog.showDialog(parent, area, parentArea);
	}

	/**
	 * On import data.
	 */
	protected void onImport() {
		
		// Get selected areas.
		LinkedList<Area> areas = getSelectedAreas();
		if (areas.size() != 1) {
			Utility.show(this, "org.multipage.generator.messageSelectSingleArea");
			return;
		}
		
		Area area = areas.getFirst();
		importArea(area, this, false, true, true);
		
		long areaId = area.getId();
		ConditionalEvents.transmit(GeneratorMainFrame.this, Signal.importToArea, areaId);
	}
	
	/**
	 * Import area.
	 * @param area
	 * @param parent
	 */
	public static Long importArea(Area area, Component parent, boolean askSubName,
			boolean askImportLanguage, boolean askImportHome) {
		
		// Disable import to not allowed area.
		if (!area.canImport()) {
			Utility.show(parent, "org.multipage.generator.messageCannotImportToArea", area.getDescriptionForced());
			return null;
		}
		
		// Disable import to all hidden sub areas of given area.
		LinkedList<Area> superAreas = area.getSuperareas();
		boolean allHidden = true;
		
		if (!superAreas.isEmpty()) {
			for (Area areaAux : superAreas) {
				
				if (!area.isHideSubUseSuper(areaAux.getId())) {
					allHidden = false;
					break;
				}
			}
		}
		else {
			allHidden = false;
		}
		
		if (allHidden) {
			Utility.show(parent, "org.multipage.generator.messageCannotImportAllHidden");
			return null;
		}
		
		return ImportDialog.showDialog(parent, area, askImportLanguage, askImportHome);
	}

	/**
	 * Repaint after tools width changed.
	 */
	public void repaintAfterToolsChanged() {
		
		GeneralDiagram.updateDiagramsControls();
	}
	
	/**
	 * On edit file names.
	 */
	public void onEditFileNames() {
		
		// Get selected areas.
		LinkedList<Area> areas = getSelectedAreas();
		// Show editor.
		FileNamesEditor.showDialog(this, areas);
	}

	/**
	 * Check rendered files.
	 */
	protected void onCheckRenderedFiles() {
		
		// Get selected areas.
		LinkedList<Area> areas = getSelectedAreas();
		// Show dialog.
		CheckRenderedFiles.showDialog(this, areas);
	}
	
	/**
	 * On display home page.
	 */
	public void onRevert() {
		
		// Get selected areas.
		final LinkedList<Area> selectedAreas = mainAreaDiagramEditor.getSelectedAndEnabledAreas();
		
		// Check if the list contains some selected areas. If not, display a dialog with message.
		if (selectedAreas.isEmpty()) {
			
			Utility.show(this, "org.multipage.generator.messageSelectAreasWithExternalProviders");
			return;
		}
		
		// Get found external slots.
		try {
			LinkedList<Slot> externalSlots = ProgramGenerator.getExternalSlots(selectedAreas);
			
			// Let user confirm external providers' list.
			boolean confirmed = RevertExternalProvidersDialog.showDialog(this, externalSlots);
			if (!confirmed) {
				return;
			}
			
			// Ask user if he/she wants to rewrite external sources.
			if (Utility.ask(this, "org.multipage.generator.messageConfirmRewritingOfExternalSources")) {
			
				// Save slots' text values to external providers of code.
				String externalProviderLink = null;
				String outputText = null;
				
				for (Slot externalSlot : externalSlots) {
					
					externalProviderLink = externalSlot.getExternalProvider();
					outputText = externalSlot.getTextValue();
					
					MiddleUtility.saveValueToExternalProvider(null, externalProviderLink, outputText);
				}
			}
			
			// Ask user if he/she wants to unlock slots.
			if (Utility.ask(this, "org.multipage.generator.messageUnlockExternalSlots")) {
				
				MiddleResult result = MiddleResult.OK;
				try {
					Middle middle = ProgramBasic.loginMiddle();
					for (Slot externalSlot : externalSlots) {
						
						long slotId = externalSlot.getId();
						result = middle.updateSlotUnlock(slotId);
					}
				}
				catch (Exception e) {
					result = MiddleResult.exceptionToResult(e);
				}
				finally {
					ProgramBasic.logoutMiddle();
				}
				if (result.isNotOK()) {
					result.show(this);
				}
			}
		}
		catch (Exception e) {
			Utility.show(this, e.getLocalizedMessage());
		}
	}
	
	/**
	 * MOnitor home page on-line in the web browser.
	 */
	private void monitorHomePage() {
		
		// Get home area.
		Area homeArea = ProgramGenerator.getHomeArea();
		
		// Display it.
		displayOnlineArea(homeArea);
	}
	
	/**
	 * On monitor home page.
	 */
	public void onMonitorHomePage() {
		
		ConditionalEvents.transmit(this, Signal.monitorHomePage);
	}

	/**
	 * Get visible area diagram.
	 * @return
	 */
	public static AreasDiagram getVisibleAreasDiagram() {
		
		if (mainFrame != null) {
			AreasDiagramPanel editor = mainFrame.getVisibleAreasEditor();
			
			if (editor != null) {
				return editor.getDiagram();
			}
			else {
				mainFrame.getAreaDiagram();
			}
		}
		return null;
	}
	
	/**
	 * Update window selection trayMenu.
	 */
	public void updateWindowSelectionMenu() {
		
		// Create action class.
		class Action implements ActionListener {
			
			// Tab index.
			int index;
			
			// Constructor.
			Action(int index) {
				this.index = index;
			}

			// ActionGroup.
			@Override
			public void actionPerformed(ActionEvent e) {
				
				// Select tab item.
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						
						tabPanel.setSelectedIndex(index);
					}
				});
			}
		}
		
		// Load windows to select.
		windowSelectionMenu.removeAll();
		
		int selectedIndex = tabPanel.getSelectedIndex();
		
		// Setup trayMenu items.
		for (int index = 0; index < tabPanel.getTabCount(); index++) {
						
			// Get tab name and create trayMenu item.
			String title = tabPanel.getTabTitle(index);
			JMenuItem menuItem = new JMenuItem(title);
			
			// Set check image.
			if (index == selectedIndex) {
				menuItem.setIcon(Images.getIcon("org/multipage/gui/images/true_icon.png"));
			}
			
			// Set trayMenu listener.
			menuItem.addActionListener(new Action(index));
			
			// Insert trayMenu item into the trayMenu.
			windowSelectionMenu.add(menuItem);
		}
	}

	/**
	 * Set home area.
	 * @param area
	 */
	public void setHomeArea(Component parentComponent, Area area) {
		
		String areaDescription = area.getDescriptionForced();
		
		// If an area is not visible, ask user.
		if (!area.isVisible()) {
			if (!Utility.ask(parentComponent, "org.multipage.generator.messageAreaInvisibleSetHome", areaDescription)) {
				return;
			}
		}
		
		// Ask user.
		if (!Utility.ask2(parentComponent, String.format(
				Resources.getString("org.multipage.generator.messageSetHomeArea"), areaDescription))) {
			return;
		}
		
		// Set start area.
		long areaId = area.getId();
		
		MiddleResult result = ProgramBasic.getMiddle().setStartArea(
				ProgramBasic.getLoginProperties(), areaId);
		
		if (result.isNotOK()) {
			result.show(parentComponent);
		}
		
		ConditionalEvents.transmit(GeneratorMainFrame.this, Signal.updateHomeArea, areaId);
	}
	
	/**
	 * Focus on the tab area.
	 */
	@SuppressWarnings("unused")
	private void onFocusTabArea() {
		
		Long tabAreaId = tabPanel.getTopAreaIdOfSelectedTab();
		
		ConditionalEvents.transmit(this, Signal.focusTabArea, tabAreaId);

	}
	
	/**
	 * On manual GUI.
	 */
	protected void onManualGui() {
		
		// Get directory.
		String manualDirectory = MiddleUtility.getManualDirectory() + File.separatorChar + "GUI";
		if (manualDirectory.isEmpty()) {
			
			manualDirectory = System.getProperty("user.dir") + File.separatorChar + "Manual" + File.separatorChar + "GUI";
		}
		
		// Check if index.htm exists.
		String indexFilePath = manualDirectory + File.separatorChar + "index.htm";
		
		String url = String.format("file://%s", indexFilePath);
		//Utility.show2(this, url);
		
		File indexFile = new File(indexFilePath);
		
		if (!indexFile.exists()) {
			Utility.show(this, "org.multipage.generator.messageManualIndexNotFound", indexFilePath);
			return;
		}

		BareBonesBrowserLaunch.openURL(url);
	}
	
	/**
	 * On Maclan manual event.
	 */
	protected void onManualMaclan() {
		
		// Get directory.
		String manualDirectory = MiddleUtility.getManualDirectory() + File.separatorChar + "Maclan";
		if (manualDirectory.isEmpty()) {
			
			manualDirectory = System.getProperty("user.dir") + File.separatorChar + "Manual" + File.separatorChar + "Maclan";
		}
		
		// Check if index.htm exists.
		String indexFilePath = manualDirectory + File.separatorChar + "index.htm";
		
		String url = String.format("file://%s", indexFilePath);
		//Utility.show2(this, url);
		
		File indexFile = new File(indexFilePath);
		
		if (!indexFile.exists()) {
			Utility.show(this, "org.multipage.generator.messageManualIndexNotFound", indexFilePath);
			return;
		}

		BareBonesBrowserLaunch.openURL(url);
	}
	
	/**
	 * On video.
	 */
	protected void onVideo() {
		
		// Get directory.
		String manualDirectory = MiddleUtility.getManualDirectory() + File.separatorChar + "Video";
		if (manualDirectory.isEmpty()) {
			
			manualDirectory = System.getProperty("user.dir") + File.separatorChar + "Manual" + File.separatorChar + "Video";
		}
		
		// Check if index.htm exists.
		String indexFilePath = manualDirectory + File.separatorChar + "tutorial.htm";
		
		String url = String.format("file://%s", indexFilePath);
		//Utility.show2(this, url);
		
		File indexFile = new File(indexFilePath);
		
		if (!indexFile.exists()) {
			//Utility.show(this, "org.multipage.generator.messageVideoNotFound", indexFilePath);
			
			url = MiddleUtility.getWebVideoUrl();
			if (url.isEmpty()) {
				url = "http://www.multipage-software.org/video_alpha";
			}
		}

		BareBonesBrowserLaunch.openURL(url);
	}

	/**
	 * Copy area tree.
	 * @param area
	 * @param parentArea 
	 */
	public void copyAreaTree(Area area, Area parentAreaParam) {
		
		if (area == null) {
			Utility.show(this, "org.multipage.generator.messageSelectSingleAreaToCopy");
			return;
		}
		
		if (parentAreaParam == null) {
			
			// Let user select parent area.
			parentAreaParam = SelectSuperAreaDialog.showDialog(this, area);
			if (parentAreaParam == null) {
				return;
			}
		}
		
		final Area parentArea = parentAreaParam;
		
		// Create and execute progress dialog.
		ProgressDialog<MiddleResult> progressDlg = new ProgressDialog<MiddleResult>(this,
				Resources.getString("org.multipage.generator.textCopyAreaProgressTitle"),
				Resources.getString("org.multipage.generator.textCopyAreaLoadingData"));
		
		// Load area tree data.
		progressDlg.execute(new SwingWorkerHelper<MiddleResult>() {
			@Override
			protected MiddleResult doBackgroundProcess() throws Exception {
				
				Middle middle = ProgramBasic.getMiddle();
				Properties login = ProgramBasic.getLoginProperties();
				
				// Create area tree object with callback methods.
				areaTreeDataToCopy = new AreaTreeData() {

					@Override
					public boolean existsAreaOutside(Long areaId) {
						
						// Check in model if an area exists.
						if (areaId == null) {
							return false;
						}
						Area area = ProgramGenerator.getArea(areaId);
						return area != null;
					}
				};
				areaTreeDataToCopy.setCloned(true);
			
				Long parentAreaId = parentArea != null ? parentArea.getId() : null;
				
				MiddleResult result = middle.loadAreaTreeData(login, area.getId(), parentAreaId,
						areaTreeDataToCopy, this);
				return result;
			}
		});
		
		// Reset copied data after delay.
		resetAreaTreeCopyTimer.restart();
			
		MiddleResult result = progressDlg.getOutput();
		// On error inform user.
		if (result.isNotOK()) {
			result.show(this);
		}

		// Set message information.
		String message = areaTreeDataToCopy.getExportMessage();
		Utility.showHtml(this, message);
	}

	/**
	 * Paste area tree.
	 * @param area
	 */
	public void pasteAreaTree(Area area) {
		
		// Check data.
		if (areaTreeDataToCopy == null) {
			Utility.show(this, "org.multipage.generator.messageCopyAreaFirst");
			return;
		}
		
		// Check input area.
		if (area == null) {
			Utility.show(this, "org.multipage.generator.messageSelectSingleAreaToPaste");
			return;
		}
		
		if (!area.canImport()) {
			Utility.show(this, "org.multipage.generator.messageCannotImportToArea", area.getDescriptionForced());
			return;
		}

		Progress2Dialog<MiddleResult> progressDlg = null;
			
		// Reset copied data after delay.
		resetAreaTreeCopyTimer.restart();
		
		// Create and execute progress dialog.
		progressDlg = new Progress2Dialog<MiddleResult>(this,
				Resources.getString("org.multipage.generator.textPasteAreaProgressTitle"),
				Resources.getString("org.multipage.generator.textPasteAreaData"));
		
		progressDlg.execute(new SwingWorkerHelper<MiddleResult>() {
			@Override
			protected MiddleResult doBackgroundProcess() throws Exception {

				Middle middle = ProgramBasic.getMiddle();
				Properties login = ProgramBasic.getLoginProperties();
				
				// Import data.
				MiddleResult result = areaTreeDataToCopy.saveToDatabase(middle, login, area, null, false, this);

				return result;
			}
		});
					
		MiddleResult result = progressDlg.getOutput();
		// On error inform user.
		if (result != null && result.isNotOK() && result != MiddleResult.CANCELLATION) {
			result.show(this);
			return;
		}
		
		long areaId = area.getId();
		ConditionalEvents.transmit(GeneratorMainFrame.this, Signal.importToArea, areaId);
	}

	/**
	 * Returns true value if there area area tree data to copy.
	 * @return
	 */
	public boolean isAreaTreeDataCopy() {
		
		return areaTreeDataToCopy != null;
	}

	/**
	 * Transfer area.
	 * @param transferredArea
	 * @param transferredParentArea 
	 * @param droppedArea
	 * @param droppedParentArea 
	 * @param action 
	 * @param parentComponent
	 */
	public static void transferArea(Area transferredArea, Area transferredParentArea,
			Area droppedArea, Area droppedParentArea, int action, Component parentComponent) {
		
		// If the transferred and dropped areas are same, inform user and exit.
		if (droppedArea.equals(transferredArea)) {
			Utility.show(parentComponent, "org.multipage.generator.messageCannotDropAreaToItself");
			return;
		}

		// Check action. 1 means link, 2 means move
		if (!(action == 1 || action == 2)) {
			Utility.show(parentComponent, "org.multipage.generator.messageUnknownTransferAreaAction");
			return;
		}
		
		// Ask method.
		int method = SelectTransferMethodDialog.showDialog(parentComponent, 
				action, droppedParentArea == null);
		
		if (method == SelectTransferMethodDialog.CANCELLED) {
			return;
		}
		
		// Set parent area depending on the selected method.
		Area parentArea = null;
		Area subArea = transferredArea;
		
		switch (method) {
		case SelectTransferMethodDialog.BEFORE:
		case SelectTransferMethodDialog.AFTER:
			
			if (droppedParentArea == null) {
				return;
			}
			parentArea = droppedParentArea;
			break;
			
		case SelectTransferMethodDialog.SUBAREA:
			
			parentArea = droppedArea;
			break;
			
		default:
			return;
		}
		
		// When linked, check if a cycle exists in the new areas diagram.
		if (action == 1) {
			if (AreasDiagram.existsCircle(parentArea, subArea)) {
			
				Utility.show(parentComponent, "org.multipage.generator.messageCycleInAreaDiagramExists");
				return;
			}
		}
		else {

			// If the transferred area contains the dropped area, inform user and exit.
			if (AreasDiagram.containsSubarea(transferredArea, droppedArea)) {
				
				Utility.show(parentComponent, "org.multipage.generator.messageAreaCannotMoveToItself");
				return;
			}
		}
		
		boolean sameParent = parentArea.equals(transferredParentArea);
		
		Obj<Boolean> inheritance = new Obj<Boolean>(true);
		Obj<String> relationNameSub = new Obj<String>();
		Obj<String> relationNameSuper = new Obj<String>();
		Obj<Boolean> hideSub = new Obj<Boolean>(true);
		
		if (!sameParent) {
			
			// Ask user for sub area edge definition.
			boolean confirmed = AreasDiagram.askNewSubAreaEdge(parentArea, subArea, inheritance, relationNameSub, relationNameSuper,
					hideSub, parentComponent);
				
			if (!confirmed) {
				return;
			}
		}
		
		// Prepare prerequisites and update database.
		Properties login = ProgramBasic.getLoginProperties();
		Middle middle = ProgramBasic.getMiddle();
		
		MiddleResult result = middle.login(login);
		if (result.isNotOK()) {
			result.show(parentComponent);
			return;
		}
		
		boolean error = false;
		
		if (!sameParent) {
			
			// On move delete old edge.
			if (action == 2 && transferredParentArea != null) {
				
				result = middle.removeIsSubareaEdge(transferredParentArea, transferredArea);
				if (result.isNotOK()) {
					error = true;
				}
			}
			
			// Connect parent area with sub area.
			if (!error) {
				result = middle.connectSimplyAreas(parentArea, subArea, inheritance.ref,
						relationNameSub.ref, relationNameSuper.ref, hideSub.ref);
				
				if (result.isNotOK()) {
					error = true;
				}
			}
		}
		
		// On BEFORE and AFTER update sub areas order.
		if (!error && (method == SelectTransferMethodDialog.BEFORE || method == SelectTransferMethodDialog.AFTER)) {
			
			// Place new area in sub areas and save priorities.
			LinkedList<Area> subAreas = parentArea.getSubareas();
			long droppedAreaId = droppedArea.getId();
			long transferredAreaId = transferredArea.getId();
			
			// Insert new sub area.			
			LinkedList<Long> subAreasIds = new LinkedList<Long>();
			for (Area area : subAreas) {
				
				long areaId = area.getId();
				
				// Skip if it is the transferred area.
				if (areaId == transferredAreaId) {
					continue;
				}
				
				// Add the area before the dropped area.
				if (areaId == droppedAreaId && method == SelectTransferMethodDialog.BEFORE) {
					subAreasIds.add(transferredAreaId);
				}
				
				subAreasIds.add(areaId);
				
				// Add the area after the dropped area.
				if (areaId == droppedAreaId && method == SelectTransferMethodDialog.AFTER) {
					subAreasIds.add(transferredAreaId);
				}
			}
			
			// Update priorities.
			result = middle.initAreaSubareasPriorities(parentArea.getId(), subAreasIds);
			if (result.isNotOK()) {
				error = true;
			}
		}
		
		// Logout from the database.
		MiddleResult logoutResult = middle.logout(result);
		if (result.isOK()) {
			result = logoutResult;
		}
		
		// Display error message.
		if (result.isNotOK()) {
			result.show(parentComponent);
		}

		// Update data.
		ConditionalEvents.transmit(getFrame(), Signal.transferToArea);
	}
	
	/**
	 * Hide properties.
	 */
	public static void hideProperties() {
		
		// Delegate call.
		showProperties(null);
	}
	
	/**
	 * Display area properties.
	 * @param areas
	 */
	public static void showProperties(Collection<Long> areaIds) {
		
		// If there are no areas, do not display panel with properties.
		if (areaIds == null || areaIds.isEmpty()) {
			mainFrame.propertiesPanel.setNoProperties();
			mainFrame.setPropertiesVisible(false);
			return;
		}
		
		// Load areas from IDs.
		LinkedList<Area> areas = new LinkedList<Area>();
		areaIds.stream().forEach(areaId -> {
			Area area = ProgramGenerator.getArea(areaId);
			if (area != null) {
				areas.add(area);
			}
		});
		
		// Display editor.
		mainFrame.propertiesPanel.setAreas(areas);
		mainFrame.setPropertiesVisible(true);
	}
	
	/**
	 * Removes area or a link to the area.
	 * @param area
	 * @param parentArea
	 * @param parentComponent
	 */
	public static void removeArea(Area area, Area parentArea, Component parentComponent) {
		
		// Areas deletion dialog.
		HashSet<Area> areas = new HashSet<Area>();
		areas.add(area);
		
		AreasDeletionDialog dialog = new AreasDeletionDialog(parentComponent, areas,
				parentArea);
		dialog.setVisible(true);
		
		// Propagate update all event.
		ConditionalEvents.transmit(parentComponent, Signal.updateAll);
		
	}
	
	/**
	 * Display help page for Maclan item designated by ID.
	 * @param maclanHelpId
	 */
	public static void displayMaclanHelp(String maclanHelpId) {
		
		// Try to find area with alias set to Maclan help ID.
		Area maclanHelpArea = ProgramGenerator.getAreasModel().getArea(maclanHelpId);
		
		// Check the area.
		if (maclanHelpArea == null) {
			Utility.showHtml(getFrame().getContentPane(), "org.multipage.generator.messageStatementDescriptionIsNotAvailable", maclanHelpId);
			return;
		}
		
		// Add Maclan help page.
		getFrame().displayIdeHelperArea(maclanHelpArea);
	}
}
