package program.builder.dynamic;
/**
 * 
 */

import java.awt.event.*;
import java.util.*;

import general.gui.*;

import javax.swing.*;

import program.middle.*;
import program.middle.dynamic.Program;


/**
 * @author
 *
 */
public class ConfirmProgramLink extends JDialog {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Dialog elements.
	 */
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel confirmLabel;
    private javax.swing.JTextArea destinationAreaDescription;
    private javax.swing.JLabel labelDestinationArea;
    private javax.swing.JLabel labelPrograms;
    private javax.swing.JList programList;
    private javax.swing.JButton noButton;
    private javax.swing.JButton yesButton;

    /**
     * Program list model.
     */
	private DefaultListModel programListModel;

	private boolean confirmed = false;
    
	/**
	 * Show confirm dialog.
	 * @param parent
	 * @param area
	 * @param programs
	 * @return
	 */
	public static boolean showConfirmDialog(JFrame parent, Area area,
			LinkedList<Program> programs) {

        ConfirmProgramLink confirmDialog
    		= new ConfirmProgramLink(parent, area, programs);
        
        confirmDialog.setVisible(true);
        
        return confirmDialog.confirmed;
	}
	
	/**
	 * Initialize components.
	 */
    private void initComponents() {

        labelPrograms = new javax.swing.JLabel();
        javax.swing.JScrollPane jScrollPane1 = new javax.swing.JScrollPane();
        programList = new javax.swing.JList();
        labelDestinationArea = new javax.swing.JLabel();
        javax.swing.JScrollPane jScrollPane2 = new javax.swing.JScrollPane();
        destinationAreaDescription = new javax.swing.JTextArea();
        confirmLabel = new javax.swing.JLabel();
        yesButton = new javax.swing.JButton();
        noButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("textConfirmProgramLink");
        setIconImages(null);
        setModal(true);
        setResizable(false);

        labelPrograms.setText("textPrograms");

        programList.setModel(programListModel);
        jScrollPane1.setViewportView(programList);

        labelDestinationArea.setText("textDestinationArea");

        destinationAreaDescription.setColumns(20);
        destinationAreaDescription.setEditable(false);
        destinationAreaDescription.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        destinationAreaDescription.setRows(5);
        jScrollPane2.setViewportView(destinationAreaDescription);

        confirmLabel.setFont(new java.awt.Font("Tahoma", 1, 12));
        confirmLabel.setText("textConfirmProgramsLink");

        yesButton.setText("textYes");
        yesButton.setPreferredSize(new java.awt.Dimension(80, 25));

        noButton.setText("textNo");
        noButton.setPreferredSize(new java.awt.Dimension(80, 25));

        cancelButton.setText("textCancel");
        cancelButton.setPreferredSize(new java.awt.Dimension(80, 25));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                            .addComponent(labelPrograms)
                            .addComponent(labelDestinationArea)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                            .addComponent(confirmLabel))
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(yesButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(noButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(54, 54, 54))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelPrograms)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(labelDestinationArea)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(confirmLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 30, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(noButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(yesButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-416)/2, (screenSize.height-325)/2, 416, 325);
    }
    
	/**
	 * Localize component texts.
	 */
	private void localizeComponentsTexts() {
		
		Utility.localize(this);
		Utility.localize(labelPrograms);
		Utility.localize(labelDestinationArea);
		Utility.localize(confirmLabel);
		Utility.localize(yesButton);
		Utility.localize(noButton);
		Utility.localize(cancelButton);
	}

	/**
	 * Load programs model.
	 * @param programs
	 */
	private void loadListModel(LinkedList<Program> programs) {

		programListModel = new DefaultListModel();
		
		// Do loop for all programs.
		for (Program program : programs) {
			// Add the program to the list model.
			programListModel.addElement(program);
		}
	}

	/**
	 * Constructor.
	 * @param parent 
	 * @param programs 
	 * @param area 
	 */
	public ConfirmProgramLink(JFrame parent, Area area,
			LinkedList<Program> programs) {
		super(parent, true);
		setIconImage(Images.getImage("ptogram/builder/images/main_icon.png"));

		// Create list model.
		loadListModel(programs);
		// Initialize components.
		initComponents();
		// Localize components.
		localizeComponentsTexts();
		// Set area description.
		destinationAreaDescription.setText(area.toString());
		// Set icons.
		setIcons();
		// Center the dialog on the screen.
		Utility.centerOnScreen(this);
		// Set listeners.
		setListeners();
	}

	/**
	 * Sets icons.
	 */
	private void setIcons() {

		yesButton.setIcon(Images.getIcon("general/gui/images/ok_icon.png"));
		noButton.setIcon(Images.getIcon("general/gui/images/cancel_icon.png"));
	}

	/**
	 * Sets listeners.
	 */
	private void setListeners() {

		// Set cancel listeners.
		ActionListener cancelListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onCancel();
			}
		};
		noButton.addActionListener(cancelListener);
		cancelButton.addActionListener(cancelListener);
		
		// Set YES listener.
		yesButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onOK();
			}
		});
		
		// Closing window listener.
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});
	}

	/**
	 * On OK.
	 */
	protected void onOK() {

		confirmed = true;
		dispose();
	}

	/**
	 * On dialog cancel.
	 */
	protected void onCancel() {

		confirmed = false;
		dispose();
	}
}
