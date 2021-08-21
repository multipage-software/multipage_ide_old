package org.multipage.generator;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.function.Consumer;

import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.maclan.Middle;
import org.maclan.MiddleResult;
import org.maclan.Revision;
import org.maclan.Slot;
import org.multipage.basic.ProgramBasic;
import org.multipage.gui.RendererJLabel;
import org.multipage.gui.Utility;

/**
 * 
 * @author user
 *
 */
public class RevisionsDialog extends JDialog {

	/**
	 * Version
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Window position
	 */
	private static Rectangle bounds;
	
	/**
	 * Set default state
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
		
		bounds = Utility.readInputStreamObject(inputStream, Rectangle.class);
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
	 * Reference to last revision
	 */
	private Revision last;
	
	/**
	 * List model
	 */
	private DefaultListModel<Revision> model;
	
	/**
	 * Slot reference
	 */
	private Slot slot;
	
	/**
	 * Confirmation flag
	 */
	private boolean confirmed = false;
	private JLabel labelRevisions;
	private JList<Revision> list;
	private Consumer<Revision> fireSelection;
	private JButton buttonCancel;
	private JButton buttonOk;
	private JButton buttonDelete;
	
	/**
	 * Show dialog.
	 * @param parent
	 * @param slot 
	 * @param area 
	 * @return
	 */
	public static Revision showDialog(Component parent, Slot slot, Consumer<Revision> fireSelection) {
		
		RevisionsDialog dialog = new RevisionsDialog(parent);
		dialog.fireSelection = fireSelection;
		dialog.loadRevisions(slot);
		dialog.setVisible(true);
		
		if (!dialog.confirmed) {
			return null;
		}
		
		return dialog.getRevision();
	}
	
	/**
	 * Constructor
	 * @param parent
	 */
	public RevisionsDialog(Component parent) {
		super(Utility.findWindow(parent), ModalityType.APPLICATION_MODAL);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		initComponents();
		initList();
		localize();
		loadDialog();
	}

	/**
	 * Initialize components
	 */
	private void initComponents() {
		
		setTitle("org.multipage.generator.titleSelectRevision");
		setBounds(100, 100, 381, 482);
		getContentPane().setLayout(new BorderLayout());
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				buttonOk = new JButton("textOk");
				buttonOk.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						onOk();
					}
				});
				{
					buttonDelete = new JButton("org.multipage.generator.textDelete");
					buttonDelete.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							onDelete();
						}
					});
					buttonDelete.setPreferredSize(new Dimension(80, 25));
					buttonDelete.setActionCommand("OK");
					buttonPane.add(buttonDelete);
				}
				{
					Component horizontalStrut = Box.createHorizontalStrut(40);
					buttonPane.add(horizontalStrut);
				}
				buttonOk.setPreferredSize(new Dimension(80, 25));
				buttonOk.setActionCommand("OK");
				buttonPane.add(buttonOk);
				getRootPane().setDefaultButton(buttonOk);
			}
			{
				buttonCancel = new JButton("textCancel");
				buttonCancel.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						onCancel();
					}
				});
				buttonCancel.setPreferredSize(new Dimension(80, 25));
				buttonCancel.setActionCommand("Cancel");
				buttonPane.add(buttonCancel);
			}
		}
		{
			labelRevisions = new JLabel("org.multipage.generator.titleAvailableRevisions");
			labelRevisions.setBounds(new Rectangle(2, 2, 2, 2));
			getContentPane().add(labelRevisions, BorderLayout.NORTH);
		}
		{
			JScrollPane scrollPane = new JScrollPane();
			getContentPane().add(scrollPane, BorderLayout.CENTER);
			{
				list = new JList();
				list.addListSelectionListener(new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {
						onListSelectionChanged(e);
					}
				});
				scrollPane.setViewportView(list);
			}
		}
	}
	
	/**
	 * On delete revision
	 */
	protected void onDelete() {
		
		// Get selected revision
		Revision revision = (Revision) list.getSelectedValue();
		if (revision == null) {
			Utility.show(this, "org.multipage.generator.messageSelectRevisionToDelete");
			return;
		}
		
		// At least one revision must exist
		if (model.size() == 1) {
			Utility.show(this, "org.multipage.generator.messageAtLeastOneRevision");
			return;
		}
		
		// Ask user if delete the revision
		if (!Utility.ask(this, "org.multipage.generator.messageDeleteRevisionNumber", revision.toString(last.equals(revision)))) {
			return;
		}
		
		// Delete revision
		MiddleResult result = MiddleResult.UNKNOWN_ERROR;
		try {
			Middle middle = ProgramBasic.loginMiddle();
			result = middle.removeSlotRevision(slot, revision);
		}
		catch (Exception e) {
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			ProgramBasic.logoutMiddle();
		}
		if (result.isNotOK()) {
			result.show(this);
			return;
		}
		
		// Reload list
		loadRevisions(slot);
	}
	
	/**
	 * On OK
	 */
	protected void onOk() {
		
		Revision revision = getRevision();
		
		// Confirm revision
		confirmed = revision != null;
		
		saveDialog();
		dispose();
	}
	
	/**
	 * On cancel
	 */
	protected void onCancel() {
		
		saveDialog();
		dispose();
	}
	
	/**
	 * Localize components
	 */
	private void localize() {
		
		Utility.localize(this);
		Utility.localize(labelRevisions);
		Utility.localize(buttonOk);
		Utility.localize(buttonCancel);
		Utility.localize(buttonDelete);
	}
	
	/**
	 * 
	 */
	private void initList() {
		
		model = new DefaultListModel();
		list.setModel(model);
		
		list.setCellRenderer(new ListCellRenderer<Revision>() {
			
			RendererJLabel renderer = new RendererJLabel();
			private Font monospaced = new Font(Font.MONOSPACED, Font.PLAIN, 12);
			
			@Override
			public Component getListCellRendererComponent(JList<? extends Revision> list, Revision revision, int index,
					boolean isSelected, boolean cellHasFocus) {
				
				renderer.setFont(monospaced );
				renderer.setText(revision.toString(last.equals(revision)));
				renderer.set(isSelected, cellHasFocus, index);
				return renderer;
			}
		});
	}
	
	/**
	 * Helper function that delegates event action
	 * @param e
	 */
	protected void onListSelectionChanged(ListSelectionEvent e) {
		
		if (!list.getValueIsAdjusting()) {
			onSelected(list.getSelectedIndex());
		}
	}
	
	/**
	 * On selected list item
	 * @param index
	 */
	private void onSelected(int index) {
		
		if (fireSelection != null && index >= 0) {
			fireSelection.accept(model.getElementAt(index));
		}
	}

	/**
	 * Load revisions
	 * @param slot 
	 */
	private void loadRevisions(Slot slot) {
		
		this.slot = slot;
		
		MiddleResult result;
		try {
			Middle middle = ProgramBasic.loginMiddle();
			LinkedList<Revision> revisions = new LinkedList<Revision>();
			result = middle.loadRevisions(slot, revisions);
			if (result.isOK()) {
				display(revisions);
			}
		}
		catch (Exception e) {
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			ProgramBasic.logoutMiddle();
		}
		if (result.isNotOK()) {
			result.show(this);
		}
	}
	
	/**
	 * Displays revisions
	 * @param revisions
	 */
	private void display(LinkedList<Revision> revisions) {
		
		model.clear();
		
		last = revisions.getLast();
		for (Revision revision : revisions) {
			model.addElement(revision);
		}
	}
	
	/**
	 * Get revisions
	 * @return
	 */
	private Revision getRevision() {
		
		Object value = (Revision) list.getSelectedValue();
		return value != null ? (Revision) value : null;
	}
	
	/**
	 * Load dialog
	 */
	private void loadDialog() {
		
		if (!bounds.isEmpty()) {
			setBounds(bounds);
		}
		else {
			Utility.centerOnScreen(this);
		}
	}
	
	/**
	 * Save dialog
	 */
	private void saveDialog() {
		
		bounds = getBounds();
	}
}
