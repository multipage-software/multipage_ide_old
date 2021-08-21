/*
 * Copyright 2010-2020 (C) vakol
 * 
 * Created on : 03-01-2020
 *
 */
package org.multipage.generator;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.SpringLayout;

import org.maclan.Middle;
import org.maclan.MiddleResult;
import org.maclan.MiddleUtility;
import org.maclan.Slot;
import org.multipage.basic.ProgramBasic;
import org.multipage.gui.Images;
import org.multipage.gui.Utility;
import org.multipage.util.Obj;

/**
 * 
 * @author user
 *
 */
public class RevertExternalProvidersDialog extends JDialog {
	
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Bounds.
	 */
	private static Rectangle bounds;
	
	/**
	 * Confirm flag.
	 */
	private boolean confirm = false;
	private JButton buttonCancel;
	private JButton buttonOk;
	private JLabel labelRevertQuestion;
	private JScrollPane scrollPane;
	private JList<ListEntry> listProviders;
	
	/**
	 * List entry.
	 */
	public static class ListEntry {
		
		/**
		 * Link to external provider.
		 */
		String link;
		
		/**
		 * Slot ID.
		 */
		long slotId;
		
		/**
		 * External provider text.
		 */
		String externalText;
		
		/**
		 * Slot text value (with possible area server command).
		 */
		String slotText;
		
		/**
		 * External provider contains processed text.
		 */
		boolean externalEqualsProcessed = false;
		
		/**
		 * External provider contains source text.
		 */
		boolean externalEqualsSource = false;
		
		/**
		 * Path to external provider
		 */
		public String path = "";
		
		/**
		 * Encoding of external provider.
		 */
		public String encoding;	
		
		/**
		 * Result of operation.
		 */
		MiddleResult result;
		
		/**
		 * Original (processed) text.
		 */
		String processedText;
	}
	
	/**
	 * Set default state.
	 */
	public static void setDefaultData() {
		
		bounds = new Rectangle();
	}

	/**
	 * Load state.
	 * @param inputStream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void serializeData(ObjectInputStream inputStream)
			throws IOException, ClassNotFoundException {
		
		Object object = inputStream.readObject();
		if (!(object instanceof Rectangle)) {
			throw new ClassNotFoundException();
		}
		bounds = (Rectangle) object;
	}

	/**
	 * Save state.
	 * @param outputStream
	 * @throws IOException
	 */
	public static void serializeData(ObjectOutputStream outputStream) 
			throws IOException {
		
		outputStream.writeObject(bounds);
	}
	
	/**
	 * Show dialog.
	 * @param parent
	 * @param oldValue 
	 * @return
	 */
	public static boolean showDialog(Component parent, LinkedList<Slot> externalSlots) {
		
		RevertExternalProvidersDialog dialog = new RevertExternalProvidersDialog(parent);
		dialog.loadList(externalSlots);
		dialog.setVisible(true);
		
		return dialog.confirm;
	}
	
	/**
	 * Load list of external providers.
	 * @param externalSlots
	 */
	private void loadList(LinkedList<Slot> externalSlots) {
		
		// Create list model.
		DefaultListModel<ListEntry> model = new DefaultListModel<ListEntry>();
		listProviders.setModel(model);
		
		// Set list items renderer.
		listProviders.setCellRenderer(new ListCellRenderer<ListEntry>() {
			
			// Create renderer.
			ExternalProviderRevertPanel renderer = new ExternalProviderRevertPanel();
			Dimension preferredSize = new Dimension();
			{
				preferredSize.height = 24;
			}
			
			// Set renderer for list item.
			@Override
			public Component getListCellRendererComponent(JList<? extends ListEntry> list, ListEntry item, int index,
					boolean isSelected, boolean cellHasFocus) {
				
				if (item != null) {
					renderer.setContent(item, isSelected);
				}
				preferredSize.width = list.getWidth();
				renderer.setPreferredSize(preferredSize);
				return renderer;
			}
		});
		
		// Load links.
		MiddleResult result = MiddleResult.OK;
		try {
			
			Middle middle = ProgramBasic.loginMiddle();
			for (Slot externalSlot : externalSlots) {
				
				// Create new entry.
				ListEntry entry = new ListEntry();
				
				// Set slot ID.
				long slotId = externalSlot.getId();
				entry.slotId = slotId;
				
				// Load slot text.
				entry.slotText = ProgramBasic.getSlotText(slotId);
				
				// Get current external provider link and output text.
				result = middle.loadSlotExternalLinkAndOutputText(externalSlot);
				if (result.isNotOK()) {
					break;
				}
				String externalProviderLink = externalSlot.getExternalProvider();
				
				// Set external provider text.
				Obj<String> text = new Obj<String>();
				Obj<String> path = new Obj<String>();
				Obj<String> encoding = new Obj<String>();
				result = MiddleUtility.loadExternalProviderText(externalProviderLink, text, path, encoding);
				if (result.isOK()) {
					
					// Set entry.
					entry.externalText = text.ref;
					entry.path = path.ref;
					entry.encoding = encoding.ref;
					entry.processedText = externalSlot.getOutputText();
					
					// Slot must be processed.
					if (entry.processedText != null) {
						
						// Check if the external provider contains slot text value.
						entry.externalEqualsSource = entry.externalText.equals(entry.slotText);
						
						// Check if the external provider contains processed text.
						entry.externalEqualsProcessed = entry.externalText.equals(entry.processedText);
					}
					else {
						entry.result = MiddleResult.NOT_PROCESSED;
					}
				}
				else {
					entry.result = result;
				}
				
				model.addElement(entry);
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
	
	/**
	 * On list clicked.
	 */
	protected void onListClicked(MouseEvent e) {
		
		// If it is a double click.
		if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
			
			// Get selected list entry.
			ListEntry entry = listProviders.getSelectedValue();
			if (entry == null) {
				return;
			}
			
			// If it is an error entry, inform user about it.
			if (entry.result != null) {
				entry.result.show(this);
				return;
			}
			
			// If content is not equal, display differ window.
			if (!entry.externalEqualsProcessed && !entry.externalEqualsSource) {
				DifferDialog.showDialog(this, entry);
				return;
			}
			
			// Display OK message.
			Utility.show(this, "org.multipage.generator.messageProviderIsOkNoActionNeeded");
		}
	}
	
	/**
	 * Constructor.
	 * @param parent 
	 */
	public RevertExternalProvidersDialog(Component parent) {
		super(Utility.findWindow(parent), ModalityType.APPLICATION_MODAL);
		
		// initialize dialog components.
		initComponents();
		
		// Do post creation functions.
		postCreation(); //$hide$
	}
	
	/**
	 * Initialize components.
	 */
	private void initComponents() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});
		setTitle("org.multipage.generator.titleListOfExternalProviders");
		SpringLayout springLayout = new SpringLayout();
		getContentPane().setLayout(springLayout);
		
		buttonOk = new JButton("textOk");
		buttonOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onOk();
			}
		});
		getContentPane().add(buttonOk);
		buttonOk.setPreferredSize(new Dimension(89, 25));
		buttonOk.setMargin(new Insets(0, 0, 0, 0));
		
		buttonCancel = new JButton("textCancel");
		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCancel();
			}
		});
		springLayout.putConstraint(SpringLayout.SOUTH, buttonCancel, -10, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, buttonOk, 0, SpringLayout.SOUTH, buttonCancel);
		springLayout.putConstraint(SpringLayout.EAST, buttonOk, -6, SpringLayout.WEST, buttonCancel);
		springLayout.putConstraint(SpringLayout.EAST, buttonCancel, -10, SpringLayout.EAST, getContentPane());
		getContentPane().add(buttonCancel);
		buttonCancel.setMargin(new Insets(0, 0, 0, 0));
		buttonCancel.setPreferredSize(new Dimension(89, 25));
		
		labelRevertQuestion = new JLabel("org.multipage.generator.textReverExternalProviders");
		springLayout.putConstraint(SpringLayout.NORTH, labelRevertQuestion, 10, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, labelRevertQuestion, 10, SpringLayout.WEST, getContentPane());
		getContentPane().add(labelRevertQuestion);
		
		scrollPane = new JScrollPane();
		springLayout.putConstraint(SpringLayout.NORTH, scrollPane, 6, SpringLayout.SOUTH, labelRevertQuestion);
		springLayout.putConstraint(SpringLayout.WEST, scrollPane, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, scrollPane, -10, SpringLayout.NORTH, buttonCancel);
		springLayout.putConstraint(SpringLayout.EAST, scrollPane, -10, SpringLayout.EAST, getContentPane());
		getContentPane().add(scrollPane);
		
		listProviders = new JList();
		listProviders.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				onListClicked(e);
			}
		});
		scrollPane.setViewportView(listProviders);
	}
	
	/**
	 * Post creation of dialog.
	 */
	private void postCreation() {
		
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		
		localize();
		setIcons();
		
		loadDialog();
	}
	
	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		buttonOk.setIcon(Images.getIcon("org/multipage/gui/images/ok_icon.png"));
		buttonCancel.setIcon(Images.getIcon("org/multipage/gui/images/cancel_icon.png"));
	}

	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(this);
		Utility.localize(buttonOk);
		Utility.localize(buttonCancel);
		Utility.localize(labelRevertQuestion);
	}
	
	/**
	 * Load dialog.
	 */
	private void loadDialog() {
		
		if (bounds.isEmpty()) {
			Utility.centerOnScreen(this);
		}
		else {
			setBounds(bounds);
		}
	}
	
	/**
	 * Save dialog.
	 */
	private void saveDialog() {
		
		bounds = getBounds();
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
		
		saveDialog();
		
		confirm = true;
		dispose();
	}
}
