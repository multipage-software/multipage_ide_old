package program.localizer;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.ChangeListener;

import org.multipage.gui.*;

import javax.swing.event.ChangeEvent;

/**
 * 
 * @author
 *
 */
class LocalizerItem implements Serializable {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * File path and tab name.
	 */
	String filePath;
	String tabName;
	EditorColumns columns;
	
	/**
	 * Constructor.
	 * @param filePath
	 * @param tabName
	 */
	public LocalizerItem(String filePath, String tabName, EditorColumns columns) {
		
		this.filePath = filePath;
		this.tabName = tabName;
		this.columns = columns;
	}
}

/**
 * 
 * @author
 *
 */
public class LocalizerFrame extends JFrame {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Frame states.
	 */
	private static Rectangle bounds;
	
	/**
	 * Localizer items.
	 */
	private static LinkedList<LocalizerItem> localizerItems;
	
	/**
	 * Selected tab index.
	 */
	private static int selectedTabIndex;
	
	/**
	 * Set default state.
	 */
	public static void setDefaultData() {
		
		bounds = new Rectangle();
		localizerItems = new LinkedList<LocalizerItem>();
		selectedTabIndex = -1;
	}

	/**
	 * Load state.
	 * @param inputStream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("unchecked")
	public static void seriliazeData(ObjectInputStream inputStream)
			throws IOException, ClassNotFoundException {
		
		Object object = inputStream.readObject();
		if (!(object instanceof Rectangle)) {
			throw new ClassNotFoundException();
		}
		bounds = (Rectangle) object;
		
		object = inputStream.readObject();
		if (!(object instanceof LinkedList)) {
			throw new ClassNotFoundException();
		}
		
		localizerItems = (LinkedList<LocalizerItem>) object;
		
		selectedTabIndex = inputStream.readInt();
	}

	/**
	 * Save state.
	 * @param outputStream
	 * @throws IOException
	 */
	public static void serializeData(ObjectOutputStream outputStream)
			throws IOException {
		
		outputStream.writeObject(bounds);
		outputStream.writeObject(localizerItems);
		outputStream.writeInt(selectedTabIndex);
	}

	/**
	 * State serializer.
	 */
	private StateSerializer serializer;

	// $hide<<$
	/**
	 * Components.
	 */
	private JButton buttonClose;
	private JMenuBar menuBar;
	private JMenu menuFile;
	private JMenuItem menuOpen;
	private JTextField textFileName;
	private JTabbedPane tabbedPane;
	private JButton buttonRemove;
	private JButton buttonSave;
	private JMenuItem menuRemoveDiacritics;
	private JMenuItem menuCopyFilesToClipboard;
	private JButton buttonClipboard;

	/**
	 * Create the frame.
	 * @param serializer 
	 */
	public LocalizerFrame(StateSerializer serializer) {

		initComponents();
		
		// $hide>>$
		this.serializer = serializer;
		postCreate();
		// $hide<<$
	}
	
	/**
	 * Initialize components.
	 */
	private void initComponents() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				onClose();
			}
		});
		setTitle("localizer.textMainFrameTitle");
		setBounds(100, 100, 695, 535);
		SpringLayout springLayout = new SpringLayout();
		getContentPane().setLayout(springLayout);
		
		buttonClose = new JButton("textClose");
		buttonClose.setMargin(new Insets(0, 0, 0, 0));
		buttonClose.setPreferredSize(new Dimension(80, 25));
		buttonClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onClose();
			}
		});
		springLayout.putConstraint(SpringLayout.SOUTH, buttonClose, -10, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, buttonClose, -10, SpringLayout.EAST, getContentPane());
		getContentPane().add(buttonClose);
		
		menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		menuFile = new JMenu("localizer.menuFile");
		menuBar.add(menuFile);
		
		menuOpen = new JMenuItem("localizer.menuOpen");
		menuOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onOpen();
			}
		});
		menuFile.add(menuOpen);
		
		menuRemoveDiacritics = new JMenuItem("localizer.menuRemoveDiacritics");
		menuRemoveDiacritics.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onRemoveDiacritics();
			}
		});
		menuFile.add(menuRemoveDiacritics);
		
		menuCopyFilesToClipboard = new JMenuItem("localizer.menuCopyFilesToClipboard");
		menuCopyFilesToClipboard.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onClipboard();
			}
		});
		menuFile.add(menuCopyFilesToClipboard);
		
		textFileName = new TextFieldEx();
		textFileName.setPreferredSize(new Dimension(6, 25));
		textFileName.setEditable(false);
		springLayout.putConstraint(SpringLayout.NORTH, textFileName, 10, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, textFileName, 10, SpringLayout.WEST, getContentPane());
		getContentPane().add(textFileName);
		textFileName.setColumns(10);
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				onTabChanged();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, tabbedPane, 6, SpringLayout.SOUTH, textFileName);
		springLayout.putConstraint(SpringLayout.WEST, tabbedPane, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, tabbedPane, -6, SpringLayout.NORTH, buttonClose);
		springLayout.putConstraint(SpringLayout.EAST, tabbedPane, -10, SpringLayout.EAST, getContentPane());
		getContentPane().add(tabbedPane);
		
		buttonRemove = new JButton("");
		buttonRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onRemove();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, buttonRemove, 0, SpringLayout.NORTH, textFileName);
		buttonRemove.setMargin(new Insets(0, 0, 0, 0));
		springLayout.putConstraint(SpringLayout.EAST, textFileName, 0, SpringLayout.WEST, buttonRemove);
		buttonRemove.setPreferredSize(new Dimension(25, 25));
		springLayout.putConstraint(SpringLayout.EAST, buttonRemove, 0, SpringLayout.EAST, buttonClose);
		getContentPane().add(buttonRemove);
		
		buttonSave = new JButton("textSave");
		buttonSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onSave();
			}
		});
		springLayout.putConstraint(SpringLayout.EAST, buttonSave, -20, SpringLayout.WEST, buttonClose);
		buttonSave.setMargin(new Insets(0, 0, 0, 0));
		buttonSave.setPreferredSize(new Dimension(80, 25));
		springLayout.putConstraint(SpringLayout.SOUTH, buttonSave, 0, SpringLayout.SOUTH, buttonClose);
		getContentPane().add(buttonSave);
		
		buttonClipboard = new JButton("textClipboard");
		buttonClipboard.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onClipboard();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, buttonClipboard, 0, SpringLayout.NORTH, buttonClose);
		springLayout.putConstraint(SpringLayout.WEST, buttonClipboard, 0, SpringLayout.WEST, textFileName);
		buttonClipboard.setPreferredSize(new Dimension(80, 25));
		buttonClipboard.setMargin(new Insets(0, 0, 0, 0));
		getContentPane().add(buttonClipboard);
	}
	
	/**
	 * Copy files to clipboard
	 */
	protected void onClipboard() {
		
		// Get files list for selected editor
		Component panel = tabbedPane.getSelectedComponent();
		if (!(panel instanceof EditorPanel)) {
			return;
		}
		
		EditorPanel editor = (EditorPanel) panel;
		final List<File> files = editor.getListOfFiles();
		
		if (files.isEmpty()) {
			Utility.show2(this, "No files to copy");
			return;
		}
		
		Transferable ft = new Transferable() {

			@Override
			public Object getTransferData(DataFlavor arg0) throws UnsupportedFlavorException, IOException {
				
				return files;
			}

			@Override
			public DataFlavor[] getTransferDataFlavors() {
				
				return new DataFlavor [] { DataFlavor.javaFileListFlavor };
			}

			@Override
			public boolean isDataFlavorSupported(DataFlavor flavor) {
				
				return DataFlavor.javaFileListFlavor.equals(flavor);
			}
			
		};
		
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ft, new ClipboardOwner() {
			
			@Override
			public void lostOwnership(Clipboard arg0, Transferable arg1) {

			}
		});
		
		String tabName = editor.getTabName();
		
		Utility.show2(this, String.format("Files from %s tab copied to clipboard. Use local menu to paste.", tabName));
	}

	/**
	 * Post creation.
	 */
	private void postCreate() {
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		localize();
		setIcons();
		
		loadDialog();
	}
	

	/**
	 * Localize.
	 */
	private void localize() {
		
		Utility.localize(this);
		Utility.localize(buttonClose);
		Utility.localize(buttonSave);
		Utility.localize(buttonClipboard);
		Utility.localize(menuFile);
		Utility.localize(menuOpen);
		Utility.localize(menuCopyFilesToClipboard);
		Utility.localize(menuRemoveDiacritics);
	}
	
	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		setIconImage(Images.getImage("program/localizer/images/main_icon.png"));
		buttonClose.setIcon(Images.getIcon("org/multipage/gui/images/cancel_icon.png"));
		buttonRemove.setIcon(Images.getIcon("org/multipage/gui/images/cancel_icon.png"));
		buttonSave.setIcon(Images.getIcon("program/localizer/images/save_icon.png"));
	}
	
	/**
	 * On close dialog.
	 */
	protected void onClose() {
		
		// Confirm closing.
//		if (!Utility.ask(this, "localizer.messageCloseApplicationWithoutSave")) {
//			return;
//		}
		
		saveDialog();
		
		// Save states.
		serializer.startSavingSerializedStates();
		
		dispose();
	}
	
	/**
	 * Load dialog.
	 */
	private void loadDialog() {
		
		// Set bounds.
		if (bounds.isEmpty()) {
			Utility.centerOnScreen(this);
			bounds = getBounds();
		}
		else {
			setBounds(bounds);
		}
		
		// Load files.
		loadFiles();
		
		// Set selected tab index.
		if (selectedTabIndex != -1) {
			tabbedPane.setSelectedIndex(selectedTabIndex);
		}
	}

	/**
	 * Save dialog.
	 */
	private void saveDialog() {
		
		bounds = getBounds();
		
		selectedTabIndex = tabbedPane.getSelectedIndex();
	}
	
	/**
	 * On open file.
	 */
	protected void onOpen() {
		
		// Try to open file.
		String[][] filters = {{"localizer.textPropertiesFiles", "properties"}};
		
		File file = Utility.chooseFileNameToOpen(this, filters);
		if (file == null) {
			return;
		}

		// Get tab name.
		String tabName = Utility.input(this, "localizer.messageGetTabName");
		if (tabName == null) {
			return;
		}
		
		// Add file.
		String filePath = file.getPath();
		addFile(filePath, tabName);
	}

	/**
	 * Add file.
	 * @param tabName 
	 * @param filePath
	 */
	private void addFile(String filePath, String tabName) {

		// Check if file path already exists.
		if (existsFilePath(filePath)) {
			Utility.show(this, "localizer.messageFileAlreadyExists");
			return;
		}
		
		// Create columns description object.
		EditorColumns columns = new EditorColumns(30, 100, 200);
		
		// Add localizer item.
		localizerItems.add(new LocalizerItem(filePath, tabName, columns));
		
		// Add editor.
		addEditor(filePath, tabName, columns);
	}

	/**
	 * Return true value if the path already exists.
	 * @param filePath
	 * @return
	 */
	private boolean existsFilePath(String filePath) {
		
		for (LocalizerItem localizerItem : localizerItems) {
			
			if (localizerItem.filePath.equalsIgnoreCase(filePath)) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Load files.
	 */
	private void loadFiles() {
		
		// Do loop for all localizer items.
		for (LocalizerItem localizerItem : localizerItems) {
			
			String filePath = localizerItem.filePath;
			String tabName = localizerItem.tabName;
			EditorColumns columns = localizerItem.columns;

			// Add editor.
			addEditor(filePath, tabName, columns);
		}
	}

	/**
	 * Add editor.
	 * @param filePath
	 * @param tabName
	 * @param columns 
	 */
	private void addEditor(String filePath, String tabName, EditorColumns columns) {
		
		// Create new editor.
		EditorPanel editor = new EditorPanel(tabName);
		
		// Add editor to a tab.
		tabbedPane.addTab(tabName, editor);
		
		// Load editor.
		editor.load(filePath, columns);
	}

	/**
	 * On tab changed.
	 */
	protected void onTabChanged() {

		// Get selected tab.
		int selectedIndex = tabbedPane.getSelectedIndex();
		if (selectedIndex == -1) {
			textFileName.setText("");
			return;
		}
		
		// Get localizer item.
		LocalizerItem localizerItem = localizerItems.get(selectedIndex);
		
		// Display file path.
		textFileName.setText(localizerItem.filePath);
	}

	/**
	 * On remove file.
	 */
	protected void onRemove() {
		
		// Get selected index.
		int selectedIndex = tabbedPane.getSelectedIndex();
		if (selectedIndex == -1) {
			return;
		}
		
		// Ask user.
		if (!Utility.ask(this, "localizer.messageDeleteFileReference")) {
			return;
		}
		
		// Remove tab.
		tabbedPane.remove(selectedIndex);
		
		// Remove localize item.
		localizerItems.remove(selectedIndex);
	}
	
	/**
	 * On save.
	 */
	protected void onSave() {
		
		// Confirm saving.
		if (!Utility.ask(this, "localizer.messaeSaveAllPropertyFiles")) {
			return;
		}

		// Do loop for all editors.
		for (int index = 0;  index < tabbedPane.getTabCount(); index++) {
			EditorPanel editor = (EditorPanel) tabbedPane.getComponent(index);

			// Save editor files.
			editor.saveFiles();
		}
	}
	
	/**
	 * On remove diacritics.
	 */
	protected void onRemoveDiacritics() {
		
		// Get selected tab index.
		int index = tabbedPane.getSelectedIndex();
		if (index == -1) {
			return;
		}
		
		// Ask user.
		if (!Utility.ask(this, "localizer.messageRemoveDiacritics")) {
			return;
		}
		
		// Get editor and remove its diacritics.
		EditorPanel editor = (EditorPanel) tabbedPane.getComponentAt(index);
		editor.removeDiacritics();
	}
}
