/**
 * 
 */
package program.builder.dynamic;

import java.awt.event.*;
import java.util.*;

import general.gui.*;

import javax.swing.*;

import program.middle.dynamic.*;


/**
 * @author
 *
 */
public class ConfirmStepDeletion extends JDialog {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Confirmed flag.
	 */
	private boolean confirmed = false;

	/**
	 * Step to delete.
	 */
	private LinkedList<Step> steps;
	
	/**
	 * Show confirm dialog.
	 * @param frame
	 * @param step
	 * @return
	 */
	public static boolean showConfirmDialog(JFrame frame, LinkedList<Step> steps) {
		
		// Create and show the confirm dialog.
		ConfirmStepDeletion dialog = new ConfirmStepDeletion(frame, steps);
		dialog.setVisible(true);
		
		return dialog.confirmed;
	}
	
	/**
	 * Dialog elements.
	 */
    private javax.swing.JButton cancelButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel labelConfirmMessage;
    private javax.swing.JLabel labelStepsToDelete;
    private javax.swing.JList listSteps;
    private javax.swing.JButton noButton;
    private javax.swing.JButton yesButton;

    /**
     * 
     */
	private DefaultListModel stepsModel = new DefaultListModel();
    
	/**
	 * Initialize components.
	 */
    private void initComponents() {

        yesButton = new javax.swing.JButton();
        noButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        labelConfirmMessage = new javax.swing.JLabel();
        labelStepsToDelete = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        listSteps = new javax.swing.JList();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("textConfirmStepDeletion");
        setAlwaysOnTop(true);
        setModal(true);

        yesButton.setText("textYes");
        yesButton.setPreferredSize(new java.awt.Dimension(80, 25));

        noButton.setText("textNo");
        noButton.setPreferredSize(new java.awt.Dimension(80, 25));

        cancelButton.setText("textCancel");
        cancelButton.setPreferredSize(new java.awt.Dimension(80, 25));

        labelConfirmMessage.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        labelConfirmMessage.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelConfirmMessage.setText("textStepDeletionConfirmMessage");

        labelStepsToDelete.setText("textStepsToDelete");

        listSteps.setModel(stepsModel);
        jScrollPane1.setViewportView(listSteps);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(58, 58, 58)
                        .addComponent(yesButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(noButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(labelConfirmMessage, javax.swing.GroupLayout.DEFAULT_SIZE, 341, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(labelStepsToDelete)))
                .addGap(49, 49, 49))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelConfirmMessage)
                .addGap(18, 18, 18)
                .addComponent(labelStepsToDelete)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 97, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(yesButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(noButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
    }

    /**
     * Localize components.
     */
	private void localizeComponents() {

		Utility.localize(this);
        Utility.localize(yesButton);
        Utility.localize(noButton);
        Utility.localize(cancelButton);
        Utility.localize(labelConfirmMessage);
        Utility.localize(labelStepsToDelete);
	}

	/**
	 * Sets icons.
	 */
    private void setIcons() {

		yesButton.setIcon(Images.getIcon("general/gui/images/ok_icon.png"));
		noButton.setIcon(Images.getIcon("general/gui/images/cancel_icon.png"));
	}
	
    /**
     * Constructor.
     */
    public ConfirmStepDeletion(JFrame frame, LinkedList<Step> steps) {
    	super(frame);
		setIconImage(Images.getImage("program/basic/images/main_icon.png"));

    	this.steps = steps;
    	
    	// Initialize components.
    	initComponents();
    	// Localize components.
    	localizeComponents();
    	// Set icons.
    	setIcons();
    	// Load data.
    	loadData();
    	// Center dialog.
    	Utility.centerOnScreen(this);
    	// Set listeners.
    	setListeners();
    }

    /**
     * Load data.
     */
    private void loadData() {

    	// Load steps.
    	for (Step step : steps) {
    		
	    	stepsModel.addElement(step);
    	}
	}

	/**
     * Set listeners.
     */
	private void setListeners() {

		addWindowListener(new WindowAdapter() {
			// On window closing.
			@Override
			public void windowClosing(WindowEvent e) {
				closeDialog(false);
			}
		});
		ActionListener cancelAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				closeDialog(false);
			}
		};
		noButton.addActionListener(cancelAction);
		cancelButton.addActionListener(cancelAction);
		yesButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				closeDialog(true);
			}
		});
	}

	/**
	 * Closes the dialog.
	 * @param confirm
	 */
	protected void closeDialog(boolean confirm) {

		this.confirmed = confirm;
		dispose();
	}
}
