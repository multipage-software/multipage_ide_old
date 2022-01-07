package program.builder.dynamic;

import general.gui.*;
import general.util.*;

import javax.swing.*;

import program.basic.*;
import program.builder.*;
import program.generator.AreasProperties;
import program.generator.GeneratorMainFrame;
import program.middle.*;
import program.middle.dynamic.AreaDynamic;
import program.middle.dynamic.AreasModelDynamic;
import program.middle.dynamic.MiddleDynamic;
import program.middle.dynamic.Program;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.util.*;

/**
 * 
 * @author
 *
 */
public class ProgramListPanel extends JPanel {
	
	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * List model.
	 */
	private DefaultListModel programsModel = new DefaultListModel();

	/**
	 * Area nodes.
	 */
	private LinkedList<AreaDynamic> areas;

	/**
	 * Areas properties dialog reference.
	 */
	private AreasProperties areasProperties;

	// $hide<<$
	/**
	 * Components.
	 */
	private JLabel labelPrograms;
	private JScrollPane scrollPane;
	private JToolBar toolbar;
	private JList listPrograms;

	/**
	 * Create the panel.
	 * @param areasProperties 
	 */
	public ProgramListPanel(AreasProperties areasProperties) {

		initComponents();
		
		postCreate(areasProperties); // $hide$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		setLayout(new BorderLayout(0, 0));
		
		labelPrograms = new JLabel("textAreaPrograms");
		add(labelPrograms, BorderLayout.NORTH);
		
		scrollPane = new JScrollPane();
		add(scrollPane, BorderLayout.CENTER);
		
		listPrograms = new JList();
		scrollPane.setViewportView(listPrograms);
		
		toolbar = new JToolBar();
		add(toolbar, BorderLayout.SOUTH);
	}
	
	/**
	 * Post creation.
	 * @param areasProperties 
	 */
	private void postCreate(AreasProperties areasProperties) {
		
		this.areasProperties = areasProperties;
		// Create tool bar.
		createToolbar();
		// Localize components.
		localize();
		// Set drag and drop.
		listPrograms.setDragEnabled(true);
		// Set list model.
		listPrograms.setModel(programsModel);
		// Enable drag'n drop.
		setTransferHandles();
	}

	/**
	 * Create tool bar.
	 */
	private void createToolbar() {

        // Add tool bar buttons.
        ToolBarKit.addToolBarButton(toolbar, "program/generator/images/add_item_icon.png", this, "onNewProgram", "tooltipNewProgram");
        ToolBarKit.addToolBarButton(toolbar, "program/generator/images/rename_node.png", this, "onRenameProgram", "tooltipRenameProgram");
        ToolBarKit.addToolBarButton(toolbar, "program/generator/images/remove_node.png", this, "onRemovePrograms", "tooltipRemovePrograms");
        toolbar.addSeparator();
        ToolBarKit.addToolBarButton(toolbar, "program/generator/images/update_icon.png", this, "onUpdatePrograms", "tooltipUpdatePrograms");
        ToolBarKit.addToolBarButton(toolbar, "program/generator/images/program_icon.png", this, "onOpenPrograms", "tooltipOpenPrograms");
        toolbar.addSeparator();
        ToolBarKit.addToolBarButton(toolbar, "program/generator/images/select_all.png", this, "onSelectAllPrograms", "tooltipSelectAllPrograms");
        ToolBarKit.addToolBarButton(toolbar, "program/generator/images/deselect_all.png", this, "onDeselectAllPrograms", "tooltipDeselectAllPrograms");
	}

	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(labelPrograms);
	}

	/**
	 * Load programs.
	 */
	public void loadPrograms(LinkedList<Area> areas) {
		
		// Clear list.
		programsModel.clear();
		
		if (!areas.isEmpty()) {
			
			HashSet<Program> programs = new HashSet<Program>();
			
			for (Area area : areas) {
				for (Program program : ((AreaDynamic) area).getPrograms()) {
					programs.add(program);
				}
			}
			
			LinkedList<Program> sortedPrograms = new LinkedList<Program>();
			// Copy references.
			for (Program program : programs) {
				sortedPrograms.add(program);
			}
			
			// Sort list of programs.
			Collections.sort(sortedPrograms, new Comparator<Program>() {
				@Override
				public int compare(Program o1, Program o2) {

					return o1.getDescription().compareToIgnoreCase(o2.getDescription());
				}
			});
			
			for (Program sortedProgram : sortedPrograms) {
				programsModel.addElement(sortedProgram);
			}
		}
	}

	/**
	 * On new program.
	 */
	public void onNewProgram() {
		
		// If multiple areas are selected, inform user and exit.
		if (areas.size() != 1) {
			JOptionPane.showMessageDialog(this, Resources.getString("messageSelectSingleArea"));
			return;
		}
		
		// Get program description.
		String description = JOptionPane.showInputDialog(this,
				Resources.getString("messageInputProgramDescription"),
				Resources.getString("textNewProgram"));
		if (description == null) {
			return;
		}
		if (description.isEmpty()) {
			// Inform user.
			JOptionPane.showMessageDialog(this, Resources.getString("messageProgramNameEmpty"));
			return;
		}

		Properties login = ProgramBasic.getLoginProperties();
		AreaDynamic area = areas.getFirst();
		
		// Create new program.
		Program program = new Program();
		Obj<Long> programId = new Obj<Long>();
		
		// Try to save new program.
		MiddleResult result = ProgramBuilderDynamic.getMiddle().insertProgram(login, area, description,
				programId);
		if (result != MiddleResult.OK) {
			// Inform user.
			result.show(this);
			return;
		}
		
		// Set new program properties.
		program.setId(programId.ref);
		program.setDescription(description);
		
		// Change model.
		AreasModelDynamic model = (AreasModelDynamic) ProgramBuilder.getAreasModel();
		model.add(program);
		area.addProgram(program);

		GeneratorMainFrame.getFrame().repaint();
		
		// Update information.
		areasProperties.updateInformation();
	}
	
	/**
	 * On rename program.
	 */
	public void onRenameProgram() {
		
		// Get selected programs.
		Object [] selected = listPrograms.getSelectedValues();
		
		// If nothing selected, inform user.
		if (selected.length == 0) {
			JOptionPane.showMessageDialog(this, Resources.getString("messageSelectProgram"));
			return;
		}
		
		// If more than one selected, inform user.
		if (selected.length > 1) {
			JOptionPane.showMessageDialog(this, Resources.getString("messageSelectSingleProgram"));
			return;
		}
		
		// If selected object is not program, inform user.
		if (!(selected[0] instanceof Program)) {
			JOptionPane.showInputDialog(this, Resources.getString("messageSelectedIsNotProgram"));
		}
		
		Program program = (Program) selected[0];
		
		// Get new program name.
		String description = JOptionPane.showInputDialog(this,
				Resources.getString("messageInputProgramDescription"), program.getDescription());
		
		// If canceled, exit.
		if (description == null) {
			return;
		}
		
		// If is empty, inform user.
		if (description.isEmpty()) {
			// Inform user.
			JOptionPane.showMessageDialog(this, Resources.getString("messageProgramNameEmpty"));
			return;
		}

		// Try to rename program.
		MiddleResult result = ProgramBuilderDynamic.getMiddle().updateProgramDescription(
				ProgramBasic.getLoginProperties(), program.getId(), description);
		
		if (result != MiddleResult.OK) {
			result.show(this);
			return;
		}
		
		Program changedProgram = ((AreasModelDynamic) ProgramBuilder.getAreasModel()).getProgram(program.getId());
		changedProgram.setDescription(description);
		
		GeneratorMainFrame.getFrame().updateInformation();
	}

	/**
	 * On remove programs.
	 */
	public void onRemovePrograms() {
		
		// Get selected programs.
		LinkedList<Program> programs = getSelectedPrograms();
		
		// If nothing selected, inform user and exit.
		if (programs.size() == 0) {
			JOptionPane.showMessageDialog(this,
					Resources.getString("messageSelectPrograms"));
			return;
		}
		
		GeneratorMainFrame mainFrame = GeneratorMainFrame.getFrame();
		
		// Confirm the deletion.
		int confirmation = ConfirmProgramDeletion.showConfirmDialog(mainFrame,
				areas, programs);
		if (confirmation == ConfirmProgramDeletion.CANCEL) {
			return;
		}
		
		Properties login = ProgramBasic.getLoginProperties();
		MiddleDynamic middle = ProgramBuilderDynamic.getMiddle();
		MiddleResult result;
		
		if (confirmation == ConfirmProgramDeletion.SELECTED_AREAS) {
			// Remove selected programs from selected areas.
			result = middle.removeProgramsFromAreas(login, programs,
					areas);
		}
		else {
			// Remove programs.
			result = middle.removePrograms(login, programs);
		}
		
		// Redraw the application UI.
		mainFrame.repaint();
		
		if (result.isNotOK()) {
			result.show(this);
		}
		
		// Update information.
		areasProperties.updateInformation();
	}

	/**
	 * Get selected programs.
	 * @return
	 */
	private LinkedList<Program> getSelectedPrograms() {

		LinkedList<Program> programs = new LinkedList<Program>();
		
		// Get selected list items.
		Object [] selected = listPrograms.getSelectedValues();
		
		// Do loop for all selected items.
		for (Object item : selected) {
			if (item instanceof Program) {
				
				Program program = (Program) item;
				programs.add(program);
			}
		}
		
		return programs;
	}

	/**
	 * Open programs.
	 */
	public void onOpenPrograms() {
		
		if (areas.size() == 0) {
			return;
		}
		
		// Get selected programs.
		Object [] selectedArray = listPrograms.getSelectedValues();
		
		// If nothing selected, inform user.
		if (selectedArray.length == 0) {
			JOptionPane.showMessageDialog(this, Resources.getString("messageSelectPrograms"));
			return;
		}

		// Set program diagrams.
		for (Object object : selectedArray) {
			
			if (!(object instanceof Program)) {
				JOptionPane.showMessageDialog(this, Resources.getString("messageIncorrectObjectClass"));
				return;
			}
			
			Program program = (Program) object;
			((TabPanelDynamic)GeneratorMainFrame.getFrame().getTabPanel()).set(program);
		}
	}

	/**
	 * On list clicked.
	 * @param e
	 */
	protected void onListClicked(MouseEvent e) {

		if (e.getClickCount() == 2) {
	    	// Open programs.
	    	onOpenPrograms();
		}
	}

	/**
	 * On select all programs.
	 */
	public void onSelectAllPrograms() {
		
		listPrograms.setSelectionInterval(0, programsModel.size() - 1);
	}
	
	/**
	 * Deselect all programs.
	 */
	public void onDeselectAllPrograms() {
		
		listPrograms.clearSelection();
	}

	/**
	 * Set transfer handles.
	 */
	@SuppressWarnings("serial")
	private void setTransferHandles() {

		listPrograms.setTransferHandler(new TransferHandler() {

			/* (non-Javadoc)
			 * @see javax.swing.TransferHandler#getSourceActions(javax.swing.JComponent)
			 */
			@Override
			public int getSourceActions(JComponent c) {
				
				return LINK;
			}

			/* (non-Javadoc)
			 * @see javax.swing.TransferHandler#createTransferable(javax.swing.JComponent)
			 */
			@Override
			protected Transferable createTransferable(JComponent c) {

				Object [] values = listPrograms.getSelectedValues();
				StringBuffer stringBuffer = new StringBuffer();
				
				// Do loop for all selected values.
				for (Object value : values) {
					if (value instanceof Program) {
						Program program = (Program) value;
						stringBuffer.append(String.valueOf(program.getId()));
						stringBuffer.append(';');
					}
				}
				return new StringSelection(stringBuffer.toString());
			}
		});
	}

}
