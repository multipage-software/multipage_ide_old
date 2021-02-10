package program.localizer;

import java.awt.*;

import javax.swing.*;

import org.multipage.gui.*;
import org.multipage.util.Resources;

import java.awt.event.*;
import java.io.*;
import java.util.*;

/**
 * 
 * @author
 *
 */
class EditorInfo {
	
	/**
	 * Properties.
	 */
	TextEditorPane editor;
	String name;
	
	/**
	 * Constructor.
	 */
	EditorInfo(TextEditorPane editor, String name) {
		
		this.editor = editor;
		this.name = name;
	}
}

/**
 * 
 * @author
 *
 */
public class PropertyEditor extends JDialog {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Bounds.
	 */
	private static Rectangle bounds;
	
	/**
	 * Splitter position.
	 */
	private static int splitterPosition;
	
	/**
	 * Set default data.
	 */
	public static void setDefaultData() {
		
		bounds = new Rectangle();
		splitterPosition = -1;
	}

	/**
	 * Load data.
	 * @param inputStream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void seriliazeData(ObjectInputStream inputStream)
			throws IOException, ClassNotFoundException {
		
		Object object = inputStream.readObject();
		if (!(object instanceof Rectangle)) {
			throw new ClassNotFoundException();
		}
		bounds = (Rectangle) object;
		splitterPosition = inputStream.readInt();
	}

	/**
	 * Save data.
	 * @param outputStream
	 * @throws IOException
	 */
	public static void serializeData(ObjectOutputStream outputStream)
			throws IOException {
		
		outputStream.writeObject(bounds);
		outputStream.writeInt(splitterPosition);
	}

	/**
	 * Confirm flag.
	 */
	private boolean confirm = false;

	/**
	 * Key name.
	 */
	private String keyName;

	/**
	 * Properties bundles reference.
	 */
	private PropertiesBundles propertiesBundles;

	/**
	 * Editor reference.
	 */
	private EditorPanel editor;

	/**
	 * Primary editor.
	 */
	private TextEditorPane primaryEditor;

	/**
	 * Created editors list.
	 */
	private LinkedList<EditorInfo> createdEditorsInfo = new LinkedList<EditorInfo>();

	// $hide<<$
	/**
	 * Components.
	 */
	private JPanel panel;
	private JButton buttonOk;
	private JButton buttonCancel;
	private JSplitPane splitPane;
	private JScrollPane scrollPanePrimary;
	private JTabbedPane tabbedPaneOther;
	private JTextField textKeyName;
	private JLabel labelKeyName;

	/**
	 * Show dialog.
	 * @param parent
	 * @param propertiesBundles 
	 * @param keyName 
	 * @param resource
	 */
	public static boolean showDialog(Component parent, String keyName, PropertiesBundles propertiesBundles, EditorPanel editor) {
		
		PropertyEditor dialog = new PropertyEditor(Utility.findWindow(parent));
		dialog.keyName = keyName;
		dialog.propertiesBundles = propertiesBundles;
		dialog.editor = editor;
		
		dialog.displayKeyName();
		dialog.loadEditors();
		
		dialog.setVisible(true);
		return dialog.confirm;
	}

	/**
	 * Create the dialog.
	 * @param parentWindow 
	 */
	public PropertyEditor(Window parentWindow) {
		super(parentWindow, ModalityType.DOCUMENT_MODAL);

		initComponents();
		
		// $hide>>$
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
				onCancel();
			}
		});
		setTitle("localizer.textPropertyEditorDialog");
		
		setBounds(100, 100, 707, 502);
		
		panel = new JPanel();
		panel.setPreferredSize(new Dimension(10, 50));
		getContentPane().add(panel, BorderLayout.SOUTH);
		SpringLayout sl_panel = new SpringLayout();
		panel.setLayout(sl_panel);
		
		buttonOk = new JButton("textOk");
		buttonOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onOk();
			}
		});
		buttonOk.setMargin(new Insets(0, 0, 0, 0));
		buttonOk.setPreferredSize(new Dimension(80, 25));
		panel.add(buttonOk);
		
		buttonCancel = new JButton("textCancel");
		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCancel();
			}
		});
		sl_panel.putConstraint(SpringLayout.SOUTH, buttonCancel, -10, SpringLayout.SOUTH, panel);
		sl_panel.putConstraint(SpringLayout.NORTH, buttonOk, 0, SpringLayout.NORTH, buttonCancel);
		sl_panel.putConstraint(SpringLayout.EAST, buttonOk, -6, SpringLayout.WEST, buttonCancel);
		sl_panel.putConstraint(SpringLayout.EAST, buttonCancel, -10, SpringLayout.EAST, panel);
		buttonCancel.setPreferredSize(new Dimension(80, 25));
		buttonCancel.setMargin(new Insets(0, 0, 0, 0));
		panel.add(buttonCancel);
		
		textKeyName = new TextFieldEx();
		textKeyName.setFont(new Font("Tahoma", Font.BOLD, 11));
		textKeyName.setForeground(Color.RED);
		sl_panel.putConstraint(SpringLayout.EAST, textKeyName, -50, SpringLayout.WEST, buttonOk);
		textKeyName.setPreferredSize(new Dimension(6, 25));
		sl_panel.putConstraint(SpringLayout.NORTH, textKeyName, 15, SpringLayout.NORTH, panel);
		panel.add(textKeyName);
		textKeyName.setColumns(10);
		
		labelKeyName = new JLabel("localizer.textKeyName");
		sl_panel.putConstraint(SpringLayout.WEST, textKeyName, 6, SpringLayout.EAST, labelKeyName);
		sl_panel.putConstraint(SpringLayout.SOUTH, labelKeyName, 0, SpringLayout.SOUTH, buttonOk);
		sl_panel.putConstraint(SpringLayout.NORTH, labelKeyName, 0, SpringLayout.NORTH, buttonOk);
		sl_panel.putConstraint(SpringLayout.WEST, labelKeyName, 10, SpringLayout.WEST, panel);
		panel.add(labelKeyName);
		
		splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.4);
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		getContentPane().add(splitPane, BorderLayout.CENTER);
		
		scrollPanePrimary = new JScrollPane();
		splitPane.setLeftComponent(scrollPanePrimary);
		
		tabbedPaneOther = new JTabbedPane(JTabbedPane.BOTTOM);
		splitPane.setRightComponent(tabbedPaneOther);
	}

	/**
	 * Post creation.
	 */
	private void postCreate() {
		
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		
		createPrimaryEditor();
		localize();
		setIcons();
		
		loadDialog();
	}

	/**
	 * Create primary editor.
	 */
	private void createPrimaryEditor() {
		
		primaryEditor = new TextEditorPane(this, true);
		splitPane.setLeftComponent(primaryEditor);
	}

	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(this);
		Utility.localize(buttonOk);
		Utility.localize(buttonCancel);
		Utility.localize(labelKeyName);
	}
	
	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		buttonOk.setIcon(Images.getIcon("org/multipage/gui/images/ok_icon.png"));
		buttonCancel.setIcon(Images.getIcon("org/multipage/gui/images/cancel_icon.png"));
	}

	/**
	 * On cancel.
	 */
	protected void onCancel() {
		
		saveDialog();
		
		confirm = false;
		dispose();
	}

	/**
	 * On OK.
	 */
	protected void onOk() {
		
		setValues();
		
		saveDialog();
		
		confirm = true;
		dispose();
	}

	/**
	 * Display key name.
	 */
	private void displayKeyName() {
		
		textKeyName.setText(keyName);
	}

	/**
	 * Load editors.
	 */
	private void loadEditors() {
		
		// Create other values editors.
		for (int index = 0; index < propertiesBundles.size(); index++) {
			
			// Get value.
			String value = propertiesBundles.getValue(keyName, index);
			
			// Set primary value.
			if (index == 0) {
				primaryEditor.setText(value);
				continue;
			}
			
			// Add value editor.
			String tabName = propertiesBundles.getName(index);
			TextEditorPane editor = addEditor(tabName, value);
			
			// Set focus.
			if (index == 1) {
				final TextEditorPane focusEditor = editor;
				SwingUtilities.invokeLater(new Runnable() {
					
					@Override
					public void run() {
						focusEditor.getCurrentEditor().grabFocus();
					}
				});
				
			}
		}
	}

	/**
	 * Add editor.
	 * @param tabName
	 * @param value
	 */
	private TextEditorPane addEditor(String tabName, String value) {
		
		// Create new editor and add it to the tab panel.
		TextEditorPane editor = new TextEditorPane(this, true);
		tabbedPaneOther.addTab(tabName, editor);
		
		// Set text value.
		editor.setText(value);
		
		// Save editor reference.
		createdEditorsInfo.add(new EditorInfo(editor, tabName));
		
		return editor;
	}
	
	/**
	 * Load dialog.
	 */
	private void loadDialog() {
		
		if (bounds.isEmpty()) {
			Utility.centerOnScreen(this);
			bounds = getBounds();
		}
		else {
			setBounds(bounds);
		}
		if (splitterPosition >= 0) {
			splitPane.setDividerLocation(splitterPosition);
		}
		else {
			splitterPosition = splitPane.getDividerLocation();
		}
	}
	
	/**
	 * Save dialog.
	 */
	private void saveDialog() {
		
		bounds = getBounds();
		splitterPosition = splitPane.getDividerLocation();
	}

	/**
	 * Set values.
	 */
	private void setValues() {
		
		// Get key index.
		int keyIndex = propertiesBundles.getPrimaryKeyIndex(keyName);
		
		// If key name changes, ask user.
		String newKeyName = textKeyName.getText();
		if (!newKeyName.equals(keyName)) {
			
			int row = editor.getPropertyRow(newKeyName);
			if (row >= 0) {
				
				editor.selectTableRow(row);
				return;
			}
			
			if (Utility.ask(this, "localizer.messageChangeKeyName")) {
				
				// Check if key name already exists.
				if (propertiesBundles.exists(newKeyName)) {
					Utility.show2(this, String.format(Resources.getString("localizer.messageKeyNameAlreadyExists"), newKeyName));
					return;
				}
				else {
					// Remove old and add new key name.
					propertiesBundles.removeProperty(keyName);
					propertiesBundles.addNew(newKeyName, keyIndex);
					propertiesBundles.initKeyList();
					
					keyName = newKeyName;
					
					// Reload table.
					editor.updateTable();
				}
			}
		}
		
		// Set primary value.
		String value = primaryEditor.getText();
		propertiesBundles.setPrimaryValue(keyName, value);
		
		// Set other values.
		for (EditorInfo editorInfo : createdEditorsInfo) {
			
			String name = editorInfo.name;
			value = editorInfo.editor.getText();
			
			// Set value.
			propertiesBundles.setValue(name, keyName, value, keyIndex);
		}
	}
}