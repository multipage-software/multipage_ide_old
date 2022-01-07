package program.builder.dynamic;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import general.gui.Images;
import general.gui.Utility;
import general.util.*;

import javax.swing.*;

import program.middle.dynamic.MacroElement;
import program.middle.dynamic.Procedure;


/**
 * Confirm macroElement content deletion dialog.
 * @author
 *
 */
public class ConfirmMacroContentDeletion extends JDialog {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Confirmed flag.
	 */
	private boolean confirmed = false;

	/**
	 * Macro element.
	 */
	private MacroElement macroElement;
	
	/**
	 * Show confirm dialog.
	 * @param frame
	 * @param step
	 * @return
	 */
	public static boolean showConfirmDialog(JFrame frame, MacroElement macroElement) {
		
		// Create and show the confirm dialog.
		ConfirmMacroContentDeletion dialog = new ConfirmMacroContentDeletion(frame, macroElement);
		dialog.setVisible(true);
		
		return dialog.confirmed;
	}
	
	/**
	 * Dialog controls.
	 */
    private javax.swing.JButton cancelButton;
    private javax.swing.JEditorPane messagePane;
    private javax.swing.JButton noButton;
    private javax.swing.JButton yesButton;
    
	/**
	 * Initialize components.
	 */
    private void initComponents() {

        javax.swing.JScrollPane jScrollPane1 = new javax.swing.JScrollPane();
        messagePane = new javax.swing.JEditorPane();
        yesButton = new javax.swing.JButton();
        noButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("textProcedureContentDeletionConfirmation");
        setModal(true);
        setResizable(false);

        messagePane.setContentType("text/html");
        messagePane.setEditable(false);
        jScrollPane1.setViewportView(messagePane);

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
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(60, 60, 60)
                        .addComponent(yesButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(noButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(yesButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(noButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }
	
    /**
     * Localizes components.
     */
	private void localizeComponents() {

        setTitle(Resources.getString(
        		macroElement instanceof Procedure ? "textProcedureContentDeletionConfirmation"
        				: "textProgramContentDeletionConfirmation"));
        Utility.localize(yesButton);
        Utility.localize(noButton);
        Utility.localize(cancelButton);
	}
	
	/**
	 * Set icons.
	 */
	private void setIcons() {

		yesButton.setIcon(Images.getIcon("general/gui/images/ok_icon.png"));
		noButton.setIcon(Images.getIcon("general/gui/images/cancel_icon.png"));		
	}

    /**
     * Constructor.
     */
    public ConfirmMacroContentDeletion(JFrame frame, MacroElement macroElement) {
    	super(frame);
		setIconImage(Images.getImage("program/basic/images/main_icon.png"));

    	this.macroElement = macroElement;
    	
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

		// Compile message string.
		String message = String.format(
				Resources.getString(macroElement instanceof Procedure ?
						"messageConfirmProcedureContentDeletion"
						: "messageConfirmProgramContentDeletion"),
				macroElement.toString());
		
		// Set message text.
		messagePane.setText(message);
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
