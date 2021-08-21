/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.SpringLayout;
import javax.swing.TransferHandler;

import org.maclan.Middle;
import org.maclan.MiddleResult;
import org.maclan.Namespace;
import org.maclan.Resource;
import org.multipage.basic.ProgramBasic;
import org.multipage.gui.Images;
import org.multipage.gui.Utility;

/**
 * 
 * @author
 *
 */
public class ResourcesEditorDialog extends JDialog {

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
	 * Load dialog data.
	 */
	private void loadDialog() {
		
		if (bounds.isEmpty()) {
			// Center dialog.
			Utility.centerOnScreen(this);
		}
		else {
			setBounds(bounds);
		}
	}
	
	/**
	 * Save dialog data.
	 */
	private void saveDialog() {
		
		bounds = getBounds();
	}
	
	/**
	 * Set default data.
	 */
	public static void setDefaultData() {

		bounds = new Rectangle();
	}

	/**
	 * Load data.
	 * @param inputStream
	 */
	public static void seriliazeData(ObjectInputStream inputStream)
		throws IOException, ClassNotFoundException {

		Object data = inputStream.readObject();
		if (!(data instanceof Rectangle)) {
			throw new ClassNotFoundException();
		}
		bounds = (Rectangle) data;
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
	 * Confirmation.
	 */
	private boolean confirm;

	/**
	 * Resource reference.
	 */
	private java.util.List<Resource> resources;
	
	// $hide<<$
	/**
	 * Dialog components.
	 */
	private JButton buttonCancel;
	private JButton buttonOk;
	private JSplitPane splitPane;
	private JPanel panelLeft;
	private JPanel panelRight;
	private JLabel labelNamespace;
	private NamespaceTreePanel panelNamespaces;
	private JLabel labelResources;
	private NamespaceResourcesEditor panelResources;

	/**
	 * Launch the dialog.
	 */
	public static void showDialog(JFrame parentFrame) {

		ResourcesEditorDialog dialog = new ResourcesEditorDialog(parentFrame,
				null);
		dialog.setVisible(true);		
	}

	/**
	 * Launch the dialog.
	 * @param resources 
	 */
	public static boolean showDialog(Component parentComponent,
			java.util.List<Resource> resources) {

		ResourcesEditorDialog dialog = new ResourcesEditorDialog(
				Utility.findWindow(parentComponent), resources);
		dialog.setVisible(true);

		return dialog.confirm;
	}

	/**
	 * Create the dialog.
	 * @param parentFrame 
	 * @param resources 
	 */
	public ResourcesEditorDialog(Window parentWindow, java.util.List<Resource> resources) {
		super(resources == null ?  null : parentWindow,
				resources == null ? ModalityType.MODELESS : ModalityType.APPLICATION_MODAL);
		
		this.resources = resources;
		// Initialize components.
		initComponents();
		// Post creation.
		// $hide>>$
		postCreate();
		// $hide<<$
	}

	/**
	 * Initialize components.
	 */
	@SuppressWarnings("serial")
	private void initComponents() {
		setMinimumSize(new Dimension(470, 280));
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setTitle("org.multipage.generator.textOpenResourceDialog");
		setBounds(100, 100, 768, 521);
		SpringLayout springLayout = new SpringLayout();
		getContentPane().setLayout(springLayout);
		
		buttonCancel = new JButton("textCancel");
		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCancel();
			}
		});
		buttonCancel.setMargin(new Insets(2, 2, 2, 2));
		buttonCancel.setPreferredSize(new Dimension(80, 25));
		springLayout.putConstraint(SpringLayout.SOUTH, buttonCancel, -10, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, buttonCancel, -10, SpringLayout.EAST, getContentPane());
		getContentPane().add(buttonCancel);
		
		buttonOk = new JButton("textOk");
		buttonOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onOk();
			}
		});
		buttonOk.setMargin(new Insets(2, 2, 2, 2));
		buttonOk.setPreferredSize(new Dimension(80, 25));
		springLayout.putConstraint(SpringLayout.SOUTH, buttonOk, 0, SpringLayout.SOUTH, buttonCancel);
		springLayout.putConstraint(SpringLayout.EAST, buttonOk, -6, SpringLayout.WEST, buttonCancel);
		getContentPane().add(buttonOk);
		
		splitPane = new JSplitPane();
		splitPane.setOneTouchExpandable(true);
		springLayout.putConstraint(SpringLayout.NORTH, splitPane, 0, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, splitPane, 0, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, splitPane, -10, SpringLayout.NORTH, buttonOk);
		springLayout.putConstraint(SpringLayout.EAST, splitPane, 0, SpringLayout.EAST, getContentPane());
		getContentPane().add(splitPane);
		
		panelLeft = new JPanel();
		splitPane.setLeftComponent(panelLeft);
		SpringLayout sl_panelLeft = new SpringLayout();
		panelLeft.setLayout(sl_panelLeft);
		
		labelNamespace = new JLabel("org.multipage.generator.textNameSpace");
		sl_panelLeft.putConstraint(SpringLayout.NORTH, labelNamespace, 10, SpringLayout.NORTH, panelLeft);
		sl_panelLeft.putConstraint(SpringLayout.WEST, labelNamespace, 3, SpringLayout.WEST, panelLeft);
		panelLeft.add(labelNamespace);
		
		panelNamespaces = new NamespaceTreePanel();
		sl_panelLeft.putConstraint(SpringLayout.NORTH, panelNamespaces, 6, SpringLayout.SOUTH, labelNamespace);
		sl_panelLeft.putConstraint(SpringLayout.WEST, panelNamespaces, 0, SpringLayout.WEST, panelLeft);
		sl_panelLeft.putConstraint(SpringLayout.SOUTH, panelNamespaces, 0, SpringLayout.SOUTH, panelLeft);
		sl_panelLeft.putConstraint(SpringLayout.EAST, panelNamespaces, 0, SpringLayout.EAST, panelLeft);
		panelLeft.add(panelNamespaces);
		
		panelRight = new JPanel();
		splitPane.setRightComponent(panelRight);
		SpringLayout sl_panelRight = new SpringLayout();
		panelRight.setLayout(sl_panelRight);
		
		labelResources = new JLabel("org.multipage.generator.textResources");
		sl_panelRight.putConstraint(SpringLayout.NORTH, labelResources, 10, SpringLayout.NORTH, panelRight);
		sl_panelRight.putConstraint(SpringLayout.WEST, labelResources, 3, SpringLayout.WEST, panelRight);
		panelRight.add(labelResources);
		
		panelResources = new NamespaceResourcesEditor() {
			@Override
			protected void onCloseDialog() {
				onOk();
			}
		};
		sl_panelRight.putConstraint(SpringLayout.NORTH, panelResources, 6, SpringLayout.SOUTH, labelResources);
		sl_panelRight.putConstraint(SpringLayout.WEST, panelResources, 0, SpringLayout.WEST, panelRight);
		sl_panelRight.putConstraint(SpringLayout.SOUTH, panelResources, 0, SpringLayout.SOUTH, panelRight);
		sl_panelRight.putConstraint(SpringLayout.EAST, panelResources, 0, SpringLayout.EAST, panelRight);
		panelRight.add(panelResources);
		splitPane.setDividerLocation(210);
	}

	/**
	 * Post creation.
	 */
	private void postCreate() {

		// Localize.
		localize();
		// Set icons.
		setIcons();
		// Set name space tree listener.
		panelNamespaces.setTreeListener(new NamespaceTreeListener() {
			// On name space selected.
			@Override
			public void onNamespaceSelectedEvent(Namespace namespace) {
				onNamespaceSelected(namespace);
			}
		});
		// Load name spaces tree.
		panelNamespaces.updateInformation();
		panelNamespaces.selectRoot();
		// Set drag and drop.
		setDragAndDrop();
		// Load dialog.
		loadDialog();
	}

	/**
	 * Localize components.
	 */
	private void localize() {

		Utility.localize(this);
		Utility.localize(buttonOk);
		Utility.localize(buttonCancel);
		Utility.localize(labelNamespace);
		Utility.localize(labelResources);
	}

	/**
	 * Set icons.
	 */
	private void setIcons() {

		setIconImage(Images.getImage("org/multipage/generator/images/main_icon.png"));
		
		buttonOk.setIcon(Images.getIcon("org/multipage/generator/images/ok_icon.png"));
		buttonCancel.setIcon(Images.getIcon("org/multipage/generator/images/cancel_icon.png"));
	}

	/**
	 * On cancel.
	 */
	protected void onCancel() {

		confirm = false;
		panelResources.close();
		saveDialog();
		dispose();
	}
	
	/**
	 * On OK.
	 */
	protected void onOk() {

		if (resources == null) {
			confirm = false;
		}
		else {
			// Get first selected resource.
			java.util.List<Resource> selectedResources = panelResources.getSelectedResources();
			
			// If nothing selected, return false value.
			if (selectedResources == null) {
				confirm = false;
			}
			else {
				for (Resource selectedResource : selectedResources) {
					
					// Reset resource image data and add it to the list.
					selectedResource.setImage(null);
					resources.add(selectedResource);
				}
				
				confirm = true;
			}
		}
		
		panelResources.close();
		saveDialog();
		dispose();
	}

	/**
	 * On name space selected.
	 * @param namespace
	 */
	protected void onNamespaceSelected(Namespace namespace) {

		// Load list.
		panelResources.loadNamespaceContent(namespace.getId());
	}

	/**
	 * Set drag and drop.
	 */
	@SuppressWarnings("serial")
	private void setDragAndDrop() {

		// Set drag and drop source.
		final JList list = panelResources.getList();
		list.setDragEnabled(true);
		list.setTransferHandler(new TransferHandler() {
			// Source actions.
			@Override
			public int getSourceActions(JComponent c) {
				// Move component.
				return MOVE;
			}
			// Create transferable.
			@Override
			protected Transferable createTransferable(JComponent c) {
				
				Object [] values = list.getSelectedValues();
				StringBuffer stringBuffer = new StringBuffer();
				
				// Do loop for all selected values.
				for (Object value : values) {
					if (value instanceof Resource) {
						Resource resource = (Resource) value;
						stringBuffer.append(String.valueOf(resource.getId()));
						stringBuffer.append(';');
					}
				}
				return new StringSelection(stringBuffer.toString());
			}
			// Can import.
			@Override
			public boolean canImport(TransferSupport support) {
				return true;
			}
			// Import data.
			@Override
			public boolean importData(TransferSupport support) {
				
				// If it is not a drop operation, exit the method with
				// false value.
				if (!support.isDrop()) {
					return false;
				}
				
				// Get the string that is dropped.
		        Transferable transferable = support.getTransferable();
				java.util.List<File> fileList;
		        try {
		            fileList = (java.util.List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
		        } 
		        catch (Exception e) {
		        	return false;
		        }
		        
				// Do loop for all files.
				for (File file : fileList) {
					
					if (!panelResources.loadFile(file)) {
						break;
					}
				}

		        return true;
			}
		});

		// Set drag and drop target.
		final NamespaceTreePanel namespacesPanel = panelNamespaces;
		final JTree tree = panelNamespaces.getTree();
		tree.setTransferHandler(new TransferHandler() {
			// Can import.
			@Override
			public boolean canImport(TransferSupport support) {
				return true;
			}
			// Import data.
			@Override
			public boolean importData(TransferSupport support) {
				
				// If it is not a drop operation, exit the method with
				// false value.
				if (!support.isDrop()) {
					return false;
				}
				
				// Get the string that is dropped.
		        Transferable transferable = support.getTransferable();
				String data;
		        try {
		            data = (String) transferable.getTransferData(DataFlavor.stringFlavor);
		        } 
		        catch (Exception e) { return false; }

				// Get selected tree node.
				Namespace namespace = namespacesPanel.getSelectedNamespace();
			
				// Split resources IDs.
				String [] resourceIds = data.split(";");
				LinkedList<Long> resourcesIds = new LinkedList<Long>();
				for (String idString : resourceIds) {
					resourcesIds.add(Long.parseLong(idString));
				}
								
				// Change resources namespace.
				Middle middle = ProgramBasic.getMiddle();
				MiddleResult result;
				Properties login = ProgramBasic.getLoginProperties();
				
				result = middle.changeResourcesNamespace(login, resourcesIds, namespace);
				if (result.isNotOK()) {
					result.show(null);
					return false;
				}
				
				// Load list.
				panelResources.loadNamespaceContent(namespace.getId());
				// Select resources.
				panelResources.selectResources(resourcesIds);
				
		        return true;
			}
		});
	}
}
