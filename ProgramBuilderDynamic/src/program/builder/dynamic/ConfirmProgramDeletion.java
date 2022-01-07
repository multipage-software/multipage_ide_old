/**
 * 
 */
package program.builder.dynamic;

import java.awt.event.*;
import java.util.LinkedList;

import general.gui.*;

import javax.swing.*;

import program.builder.ProgramBuilder;
import program.middle.Area;
import program.middle.AreasModel;
import program.middle.dynamic.AreaDynamic;
import program.middle.dynamic.Program;


/**
 * @author
 *
 */
public class ConfirmProgramDeletion extends JDialog {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Returned values.
	 */
	public final static int CANCEL = 0;
	public final static int SELECTED_AREAS = 1;
	public final static int FORCE_DELETION = 2;
	
	/**
	 * Selected areas.
	 */
	private LinkedList<AreaDynamic> selectedAreas = new LinkedList<AreaDynamic>();
	
	/**
	 * Selected programs.
	 */
	private LinkedList<Program> selectedPrograms = new LinkedList<Program>();
	
	/**
	 * Confirmation.
	 */
	private int confirmation = CANCEL;

	/**
	 * Areas model.
	 */
	private DefaultListModel areasModel = new DefaultListModel();
	
	/**
	 * Dialog components.
	 */
    private javax.swing.JLabel affectedAreasLabel;
    private javax.swing.JList areasList;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton cancelButton;
    private javax.swing.JRadioButton forceProgramRemoving;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel labelProgramDeletion;
    private javax.swing.JButton noButton;
    private javax.swing.JRadioButton removeProgramsSelectedAreas;
    private javax.swing.JButton yesButton;

	/**
	 * Show confirm dialog.
	 * @param parent
	 * @param selectedAreas 
	 * @param programs 
	 * @return
	 */
	public static int showConfirmDialog(JFrame parent, LinkedList<AreaDynamic> areas,
			LinkedList<Program> programs) {
		
		ConfirmProgramDeletion dialog = new ConfirmProgramDeletion(parent,
				areas, programs);
		
		dialog.setVisible(true);
		return dialog.confirmation;
	}
	
	/**
	 * Initialize components.
	 */
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        labelProgramDeletion = new javax.swing.JLabel();
        removeProgramsSelectedAreas = new javax.swing.JRadioButton();
        forceProgramRemoving = new javax.swing.JRadioButton();
        yesButton = new javax.swing.JButton();
        noButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        areasList = new javax.swing.JList();
        affectedAreasLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("textConfirmProgramDeletion");
        setModal(true);
        setResizable(false);

        labelProgramDeletion.setText("textSelectProgramDeletion");

        buttonGroup1.add(removeProgramsSelectedAreas);
        removeProgramsSelectedAreas.setSelected(true);
        removeProgramsSelectedAreas.setText("textRemoveProgramsSelectedAreas");

        buttonGroup1.add(forceProgramRemoving);
        forceProgramRemoving.setText("textForceProgramRemoving");

        yesButton.setText("textYes");
        yesButton.setPreferredSize(new java.awt.Dimension(80, 25));

        noButton.setText("textNo");
        noButton.setPreferredSize(new java.awt.Dimension(80, 25));

        cancelButton.setText("textCancel");
        cancelButton.setPreferredSize(new java.awt.Dimension(80, 25));

        areasList.setModel(areasModel);
        jScrollPane1.setViewportView(areasList);

        affectedAreasLabel.setText("textAffectedAreas");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(labelProgramDeletion))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(removeProgramsSelectedAreas)
                            .addComponent(forceProgramRemoving)))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jScrollPane1, 0, 0, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(yesButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(noButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(affectedAreasLabel)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelProgramDeletion)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(removeProgramsSelectedAreas)
                .addGap(3, 3, 3)
                .addComponent(forceProgramRemoving)
                .addGap(18, 18, 18)
                .addComponent(affectedAreasLabel)
                .addGap(3, 3, 3)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 21, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(noButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(yesButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
    }

	/**
     * Localize components texts.
     */
    private void localizeComponentsTexts() {
    	
    	Utility.localize(this);
        Utility.localize(labelProgramDeletion);
        Utility.localize(removeProgramsSelectedAreas);
        Utility.localize(forceProgramRemoving);
        Utility.localize(yesButton);
        Utility.localize(noButton);
        Utility.localize(cancelButton);
        Utility.localize(affectedAreasLabel);
    }
    
	/**
	 * Constructor.
	 * @param parent
	 * @param selectedAreas 
	 * @param programs 
	 */
	public ConfirmProgramDeletion(JFrame parent, LinkedList<AreaDynamic> areas,
			LinkedList<Program> programs) {
		super(parent, true);
		setIconImage(Images.getImage("program/basic/images/main_icon.png"));

		// Set members.
		selectedAreas = areas;
		selectedPrograms = programs;

		// Initialize components.
		initComponents();
		// Localize components.
		localizeComponentsTexts();
		// Set icons.
		setIcons();
		// Center the dialog.
		Utility.centerOnScreen(this);
		// Set listeners.
		setListeners();
		// Load dialog content.
		loadDialog(areas, programs);
	}

	/**
	 * Load dialog content.
	 * @param areas 
	 * @param programs 
	 */
	protected void loadDialog(LinkedList<AreaDynamic> areas,
			LinkedList<Program> programs) {

		// Load areas list model.
		areasModel.clear();
		
		for (AreaDynamic area : areas) {
			if (area.contains(programs)) {
				
				areasModel.addElement(area);
			}
		}
	}

	/**
	 * Load dialog content.
	 * @param areas
	 * @param programs
	 */
	protected void loadDialogSimple(LinkedList<Area> areas,
			LinkedList<Program> programs) {
		
		// Load areas list model.
		areasModel.clear();
		
		for (Area area : areas) {
			AreaDynamic areaDynamic = (AreaDynamic) area;
			if (areaDynamic.contains(programs)) {
				
				areasModel.addElement(area);
			}
		}
	}

	/**
	 * Set icons.
	 */
	private void setIcons() {

		yesButton.setIcon(Images.getIcon("general/gui/images/ok_icon.png"));
		noButton.setIcon(Images.getIcon("general/gui/images/cancel_icon.png"));
	}

	/**
	 * Sets listeners.
	 */
	private void setListeners() {

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});
		
		ActionListener action = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onCancel();
			}
		};
		noButton.addActionListener(action);
		cancelButton.addActionListener(action);
		
		yesButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onOk();
			}
		});
		
		removeProgramsSelectedAreas.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Load areas.
				loadDialog(selectedAreas, selectedPrograms);
			}
		});
		
		forceProgramRemoving.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Load areas.
				AreasModel model = ProgramBuilder.getAreasModel();
				LinkedList<Area> allAreas = model.getAreas();
				loadDialogSimple(allAreas, selectedPrograms);
			}
		});
	}

	/**
	 * On OK.
	 */
	protected void onOk() {
		
		confirmation = forceProgramRemoving.isSelected() ? FORCE_DELETION
				: SELECTED_AREAS;
		
		dispose();
	}

	/**
	 * On cancel.
	 */
	protected void onCancel() {
		
		confirmation = CANCEL;
		dispose();
	}
}
