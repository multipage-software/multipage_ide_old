/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.basic;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.Properties;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.maclan.MiddleResult;
import org.maclan.server.ProgramHttpServer;
import org.multipage.gui.Images;
import org.multipage.gui.Utility;
import org.multipage.util.Resources;

/**
 * 
 * @author
 *
 */
public class LoginDialog extends JDialog {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Dialog normal size.
	 */
	private static final Dimension normalSize = new Dimension(350, 310);
	
	/**
	 * Dialog extended size
	 */
	private static final Dimension extendedSize = new Dimension(350, 420);

	/**
	 * Bad login sleep in milliseconds.
	 */
	private static final long loginSleep = 3000;
	
	/**
	 * Database names list.
	 */
	private static LinkedList<String> databaseNames;
	
	/**
	 * Login values.
	 */
	private static String userName;
	private static String serverName;
	private static int portNumber;
	private static boolean sslFlag;
	private static String databaseName;

	private static int defaultPortNumber = 5432;

	/**
	 * Is true if login button pressed else false if dialog canceled.
	 */
	private boolean loginFlag = false;

	/**
	 * Attempts.
	 */
	private int attempts = 3;
	
	/**
	 * Focus.
	 */
	private Component focus;

	/**
	 * Result of the login process.
	 */
	public MiddleResult result = MiddleResult.UNKNOWN_ERROR;

	/**
	 * Sleep progress bar.
	 */
	@SuppressWarnings("serial")
	private JProgressBar progress = new JProgressBar(0, 100) {

		/* (non-Javadoc)
		 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
		 */
		@Override
		protected void paintComponent(Graphics g) {
			// Paint component.
			super.paintComponent(g);
			// Paint text.
			String text = Resources.getString("org.multipage.basic.textSecurityDelay");
			Rectangle2D rectangle = g.getFontMetrics().getStringBounds(text, g);
			g.setColor(Color.GRAY);
			g.drawString(text, getWidth() / 2 - (int)rectangle.getCenterX(), 11);
		}
	};

	/**
	 * Check port number.
	 * @param number
	 * @return
	 */
	private static boolean isPortValid(int number) {

		return number >=0 && number <= 0xFFFF;
	}

	/**
	 * Save dialog data.
	 * @param outputStream 
	 */
	public static void serializeData(ObjectOutputStream outputStream)
		throws IOException {
		
		// Save user name.
		outputStream.writeUTF(userName);
		// Save serverText name.
		outputStream.writeUTF(serverName);
		// Save portText number.
		outputStream.writeInt(
				isPortValid(portNumber) ? portNumber : defaultPortNumber );
		// Save SSL flag.
		outputStream.writeBoolean(sslFlag);
		// Save database name.
		outputStream.writeUTF(databaseName);
		// Save database names.
		outputStream.writeObject(databaseNames);
	}

	/**
	 * Load dialog data.
	 * @param inputStream
	 */
	public static void serializeData(ObjectInputStream inputStream)
		throws IOException, ClassNotFoundException {
		
		// Load user name.
		userName = inputStream.readUTF();
		// Load serverText name.
		serverName = inputStream.readUTF();
		// Load portText number.
		portNumber = inputStream.readInt();
		// Load SSL flag.
		sslFlag = inputStream.readBoolean();
		// Load database name.
		databaseName = inputStream.readUTF();
		// Load database names.
		Object object = inputStream.readObject();
		if (!(object instanceof LinkedList<?>)) {
			throw new ClassNotFoundException();
		}
		databaseNames = (LinkedList) object;
		for (Object name : databaseNames) {
			if (!(name instanceof String)) {
				throw new ClassNotFoundException();
			}
		}
	}

	/**
	 * Set default data.
	 */
	public static void setDefaultData() {

		userName   = "administrator";
		serverName = "localhost";
		portNumber = 5432;
		sslFlag   = false;
		databaseName = "programs";
		
		databaseNames = new LinkedList<String>();
		databaseNames.add(databaseName);
	}

	// $hide<<$
	/**
	 * Components.
	 */
	private JLabel padlock;
	private JLabel usernameLabel;
	private JTextField usernameText;
	private JLabel passwordLabel;
	private JPasswordField passwordText;
	private JButton loginButton;
	private JButton cancelButton;
	private JCheckBox checkboxMore;
	private JPanel morePanel;
	private JPanel progressPanel;
	private JLabel serverLabel;
	private JTextField serverText;
	private JLabel portLabel;
	private JComboBox comboDatabaseNames;
	private JFormattedTextField portText;
	private JLabel databaseLabel;
	private JCheckBox sslCheckBox;
	private JButton buttonLoadDatabaseNames;
	private JMenuBar menuBar;
	private JMenu menuDatabase;
	private JMenuItem menuCreateDatabase;
	private JMenuItem menuRemoveDatabase;
	
	/**
	 * Create the dialog.
	 */
	public LoginDialog(Window owner, String title, ModalityType modality) {
		super(owner, title, modality);

		// Set application main window.
		if (owner != null) {
			Utility.setApplicationMainWindow(owner);
		}
		else {
			Utility.setApplicationMainWindow(this);
		}
		
		initComponents();
		// $hide>>$
		postCreate();
		// $hide<<$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				onComponentShown();
			}
		});

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setResizable(false);
		setBounds(100, 100, 350, 442);
		SpringLayout springLayout = new SpringLayout();
		getContentPane().setLayout(springLayout);
		
		padlock = new JLabel("");
		springLayout.putConstraint(SpringLayout.SOUTH, padlock, 148, SpringLayout.NORTH, getContentPane());
		padlock.setHorizontalAlignment(SwingConstants.CENTER);
		springLayout.putConstraint(SpringLayout.WEST, padlock, 0, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, padlock, 122, SpringLayout.WEST, getContentPane());
		getContentPane().add(padlock);
		
		usernameLabel = new JLabel("org.multipage.basic.textUserName");
		springLayout.putConstraint(SpringLayout.NORTH, usernameLabel, 36, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, usernameLabel, 6, SpringLayout.EAST, padlock);
		getContentPane().add(usernameLabel);
		
		usernameText = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, usernameText, 6, SpringLayout.SOUTH, usernameLabel);
		springLayout.putConstraint(SpringLayout.WEST, usernameText, 6, SpringLayout.EAST, padlock);
		springLayout.putConstraint(SpringLayout.EAST, usernameText, -20, SpringLayout.EAST, getContentPane());
		getContentPane().add(usernameText);
		usernameText.setColumns(10);
		
		passwordLabel = new JLabel("org.multipage.basic.textPassword");
		springLayout.putConstraint(SpringLayout.NORTH, passwordLabel, 6, SpringLayout.SOUTH, usernameText);
		springLayout.putConstraint(SpringLayout.WEST, passwordLabel, 6, SpringLayout.EAST, padlock);
		getContentPane().add(passwordLabel);
		
		passwordText = new JPasswordField();
		springLayout.putConstraint(SpringLayout.NORTH, passwordText, 6, SpringLayout.SOUTH, passwordLabel);
		springLayout.putConstraint(SpringLayout.WEST, passwordText, 6, SpringLayout.EAST, padlock);
		springLayout.putConstraint(SpringLayout.EAST, passwordText, -20, SpringLayout.EAST, getContentPane());
		getContentPane().add(passwordText);
		
		loginButton = new JButton("org.multipage.basic.textLoginButton");
		springLayout.putConstraint(SpringLayout.WEST, loginButton, 6, SpringLayout.EAST, padlock);
		loginButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onLogin();
			}
		});
		loginButton.setHorizontalTextPosition(SwingConstants.LEADING);
		loginButton.setMargin(new Insets(0, 0, 0, 0));
		loginButton.setPreferredSize(new Dimension(80, 25));
		getContentPane().add(loginButton);
		
		cancelButton = new JButton("org.multipage.basic.textCancelButton");
		springLayout.putConstraint(SpringLayout.NORTH, loginButton, 0, SpringLayout.NORTH, cancelButton);
		springLayout.putConstraint(SpringLayout.NORTH, cancelButton, 24, SpringLayout.SOUTH, passwordText);
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCancel();
			}
		});
		springLayout.putConstraint(SpringLayout.EAST, cancelButton, 0, SpringLayout.EAST, usernameText);
		cancelButton.setPreferredSize(new Dimension(80, 25));
		cancelButton.setMargin(new Insets(0, 0, 0, 0));
		cancelButton.setHorizontalTextPosition(SwingConstants.LEADING);
		getContentPane().add(cancelButton);
		
		checkboxMore = new JCheckBox("org.multipage.basic.textMoreCheckbox");
		checkboxMore.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// On action.
				showMore(checkboxMore.getSelectedObjects() != null);
			}
		});
		springLayout.putConstraint(SpringLayout.WEST, checkboxMore, 0, SpringLayout.WEST, usernameLabel);
		getContentPane().add(checkboxMore);
		
		morePanel = new JPanel();
		springLayout.putConstraint(SpringLayout.NORTH, morePanel, 6, SpringLayout.SOUTH, checkboxMore);
		springLayout.putConstraint(SpringLayout.WEST, morePanel, 0, SpringLayout.WEST, usernameLabel);
		springLayout.putConstraint(SpringLayout.EAST, morePanel, -20, SpringLayout.EAST, getContentPane());
		getContentPane().add(morePanel);
		
		progressPanel = new JPanel();
		springLayout.putConstraint(SpringLayout.NORTH, progressPanel, -16, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, morePanel, 0, SpringLayout.NORTH, progressPanel);
		GridBagLayout gbl_morePanel = new GridBagLayout();
		gbl_morePanel.columnWidths = new int[]{0, 0, 0};
		gbl_morePanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gbl_morePanel.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gbl_morePanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		morePanel.setLayout(gbl_morePanel);
		
		serverLabel = new JLabel("org.multipage.basic.textServerLabel");
		GridBagConstraints gbc_serverLabel = new GridBagConstraints();
		gbc_serverLabel.gridwidth = 2;
		gbc_serverLabel.anchor = GridBagConstraints.LINE_START;
		gbc_serverLabel.insets = new Insets(0, 0, 5, 0);
		gbc_serverLabel.gridx = 0;
		gbc_serverLabel.gridy = 0;
		morePanel.add(serverLabel, gbc_serverLabel);
		
		serverText = new JTextField();
		GridBagConstraints gbc_serverText = new GridBagConstraints();
		gbc_serverText.gridwidth = 2;
		gbc_serverText.insets = new Insets(0, 0, 5, 0);
		gbc_serverText.fill = GridBagConstraints.HORIZONTAL;
		gbc_serverText.gridx = 0;
		gbc_serverText.gridy = 1;
		morePanel.add(serverText, gbc_serverText);
		serverText.setColumns(10);
		
		portLabel = new JLabel("org.multipage.basic.textPortLabel");
		GridBagConstraints gbc_portLabel = new GridBagConstraints();
		gbc_portLabel.gridwidth = 2;
		gbc_portLabel.insets = new Insets(0, 0, 5, 5);
		gbc_portLabel.anchor = GridBagConstraints.LINE_START;
		gbc_portLabel.gridx = 0;
		gbc_portLabel.gridy = 2;
		morePanel.add(portLabel, gbc_portLabel);
		
		portText = new JFormattedTextField(new DecimalFormat("#####"));
		GridBagConstraints gbc_portText = new GridBagConstraints();
		gbc_portText.insets = new Insets(0, 0, 5, 5);
		gbc_portText.fill = GridBagConstraints.HORIZONTAL;
		gbc_portText.gridx = 0;
		gbc_portText.gridy = 3;
		morePanel.add(portText, gbc_portText);
		portText.setColumns(10);
		
		sslCheckBox = new JCheckBox("org.multipage.basic.textUseSsl");
		GridBagConstraints gbc_sslCheckBox = new GridBagConstraints();
		gbc_sslCheckBox.anchor = GridBagConstraints.WEST;
		gbc_sslCheckBox.gridwidth = 2;
		gbc_sslCheckBox.insets = new Insets(0, 0, 0, 5);
		gbc_sslCheckBox.gridx = 0;
		gbc_sslCheckBox.gridy = 4;
		morePanel.add(sslCheckBox, gbc_sslCheckBox);
		springLayout.putConstraint(SpringLayout.WEST, progressPanel, 0, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, progressPanel, 0, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, progressPanel, 344, SpringLayout.WEST, getContentPane());
		getContentPane().add(progressPanel);
		progressPanel.setLayout(new BorderLayout(0, 0));
		
		databaseLabel = new JLabel("org.multipage.basic.textDatabaseName");
		springLayout.putConstraint(SpringLayout.NORTH, databaseLabel, 17, SpringLayout.SOUTH, loginButton);
		springLayout.putConstraint(SpringLayout.WEST, databaseLabel, 0, SpringLayout.WEST, usernameLabel);
		getContentPane().add(databaseLabel);
		
		comboDatabaseNames = new JComboBox();
		springLayout.putConstraint(SpringLayout.EAST, comboDatabaseNames, -44, SpringLayout.EAST, getContentPane());
		comboDatabaseNames.setPreferredSize(new Dimension(28, 24));
		springLayout.putConstraint(SpringLayout.NORTH, checkboxMore, 10, SpringLayout.SOUTH, comboDatabaseNames);
		springLayout.putConstraint(SpringLayout.NORTH, comboDatabaseNames, 6, SpringLayout.SOUTH, databaseLabel);
		springLayout.putConstraint(SpringLayout.WEST, comboDatabaseNames, 0, SpringLayout.WEST, usernameLabel);
		getContentPane().add(comboDatabaseNames);
		comboDatabaseNames.setEditable(true);
		
		buttonLoadDatabaseNames = new JButton("");
		buttonLoadDatabaseNames.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onUpdateDatabaseNames();
			}
		});
		buttonLoadDatabaseNames.setMargin(new Insets(0, 0, 0, 0));
		springLayout.putConstraint(SpringLayout.NORTH, buttonLoadDatabaseNames, 0, SpringLayout.NORTH, comboDatabaseNames);
		springLayout.putConstraint(SpringLayout.WEST, buttonLoadDatabaseNames, 0, SpringLayout.EAST, comboDatabaseNames);
		buttonLoadDatabaseNames.setPreferredSize(new Dimension(24, 24));
		getContentPane().add(buttonLoadDatabaseNames);
		
		menuBar = new JMenuBar();
		springLayout.putConstraint(SpringLayout.NORTH, padlock, 0, SpringLayout.SOUTH, menuBar);
		springLayout.putConstraint(SpringLayout.WEST, menuBar, 0, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, menuBar, 0, SpringLayout.EAST, getContentPane());
		getContentPane().add(menuBar);
		
		menuDatabase = new JMenu("org.multipage.basic.textDatabaseMenu");
		menuBar.add(menuDatabase);
		
		menuCreateDatabase = new JMenuItem("org.multipage.basic.textCreateDatabaseMenuItem");
		menuCreateDatabase.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onNewDatabase();
			}
		});
		menuDatabase.add(menuCreateDatabase);
		
		menuRemoveDatabase = new JMenuItem("org.multipage.basic.textRemoveDatabaseMenuItem");
		menuRemoveDatabase.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onRemoveDatabase();
			}
		});
		menuDatabase.add(menuRemoveDatabase);
	}

	/**
	 * On cancel.
	 */
	protected void onCancel() {
		
		// On action.
		loginFlag = true;
		saveDialog();
		dispose();
	}

	/**
	 * Post creation.
	 */
	private void postCreate() {
		
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		// Set progress bar.
		progress.setVisible(false);
		progressPanel.add(progress);
		morePanel.setVisible(false);
		setSize(normalSize);
		Utility.centerOnScreen(this);
		localize();
		setIcons();
		setToolTips();
		setListeners();
		// Set ENTER key.
		loginButton.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "pressed");
		// Load dialog.
		loadDialog();
	}

	/**
	 * On component shown.
	 */
	protected void onComponentShown() {
		
		Utility.closeSplash();
		passwordText.requestFocusInWindow();
	}

	/**
	 * Set listeners.
	 */
	private void setListeners() {
		
		// Port number validation.
		portText.setInputVerifier(new InputVerifier() {
			@Override
			public boolean verify(JComponent input) {
				return isPortValid();
			}
		});
		
		// Action on ENTER key.
		KeyAdapter keyAdapter = new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				super.keyReleased(e);
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					onLogin();
				}
			}
		};
		
		// Set adapters.
		usernameText.addKeyListener(keyAdapter);
		passwordText.addKeyListener(keyAdapter);
		serverText.addKeyListener(keyAdapter);
		portText.addKeyListener(keyAdapter);
		sslCheckBox.addKeyListener(keyAdapter);
		loginButton.addKeyListener(keyAdapter);
		cancelButton.addKeyListener(keyAdapter);
		comboDatabaseNames.getEditor().addActionListener((e) -> { SwingUtilities.invokeLater(() -> { onLogin(); }); });
	}

	/**
	 * Load dialog.
	 */
	private void loadDialog() {
		
		// Load user name.
		usernameText.setText(userName);
		// Load serverText name.
		serverText.setText(serverName);
		// Load portText number.
		portText.setText(String.valueOf(portNumber));
		// Load SSL flag.
		sslCheckBox.setSelected(sslFlag);
		// Load database names.
		comboDatabaseNames.removeAllItems();
		for (String name : databaseNames) {
			addDatabaseName(name);
		}
		// Set database name.
		if (!databaseName.isEmpty()) {
			comboDatabaseNames.getEditor().setItem(databaseName);
		}
		else {
			updateDatabaseNamesComboBox();
		}
	}
	
	/**
	 * Save dialog.
	 */
	private void saveDialog() {
		
		userName = usernameText.getText();
		serverName = serverText.getText();
		portNumber = isPortValid() ? Integer.parseInt(portText.getText()) : defaultPortNumber;
		sslFlag = sslCheckBox.isSelected();
		databaseName = comboDatabaseNames.getEditor().getItem().toString();
		
		// Clear database names.
		databaseNames.clear();
		
		// Save new database names.
		ComboBoxModel<String> model = comboDatabaseNames.getModel();
		for (int index = 0; index < model.getSize(); index++) {
			
			String item = model.getElementAt(index);
			databaseNames.add(item);
		}
	}
	
	/**
	 * Adds a database name into combobox if it is not already present.
	 * @param databaseName
	 */
	private void addDatabaseName(String databaseName) {
		
		// Check if the database name is already in list.
		DefaultComboBoxModel model = (DefaultComboBoxModel) comboDatabaseNames.getModel();
		for (int index = 0; index < model.getSize(); index ++) {
			Object item = model.getElementAt(index);
			if (item != null && item.toString().equals(databaseName)) {
				return;
			}
		}
		
		// Add new database name into the list.
		comboDatabaseNames.addItem(databaseName);
	}

	/**
	 * Adds database name into combobox if they are not already present
	 * @param databaseNames
	 */
	private void addNewDatabaseNames(LinkedList<String> databaseNames) {
		
		for (String databaseName : databaseNames) {
			addDatabaseName(databaseName);
		}
	}

	/**
	 * Checks portText number.
	 * @return
	 */
	protected boolean isPortValid() {

		// Verify input.
		int number;
		boolean valid;
		
		try {
			number = Integer.parseInt(portText.getText());
			valid = number >=0 && number <= 0xFFFF;
		}
		catch (NumberFormatException e) {
			valid = false;
		}
		
		// If not valid inform user.
		if (!valid) {
			JOptionPane.showMessageDialog(this, Resources.getString("org.multipage.basic.messagePortNumberFormat"));
			portText.setText(portText.getValue().toString());
			return false;
		}
		return valid;
	}

	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(usernameLabel);
		Utility.localize(passwordLabel);
		Utility.localize(loginButton);
		Utility.localize(cancelButton);
		Utility.localize(checkboxMore);
		Utility.localize(serverLabel);
		Utility.localize(portLabel);
		Utility.localize(databaseLabel);
		Utility.localize(sslCheckBox);
		Utility.localize(menuDatabase);
		Utility.localize(menuCreateDatabase);
		Utility.localize(menuRemoveDatabase);
	}

	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		setIconImage(Images.getImage("org/multipage/basic/images/main_icon.png"));
		padlock.setIcon(Images.getIcon("org/multipage/basic/images/padlock.png"));
		loginButton.setIcon(Images.getIcon("org/multipage/basic/images/ok_icon.png"));
		cancelButton.setIcon(Images.getIcon("org/multipage/basic/images/cancel_icon.png"));
		buttonLoadDatabaseNames.setIcon(Images.getIcon("org/multipage/basic/images/reload.png"));
		menuCreateDatabase.setIcon(Images.getIcon("org/multipage/basic/images/new_database.png"));
		menuRemoveDatabase.setIcon(Images.getIcon("org/multipage/basic/images/remove_database.png"));
	}
	
	/**
	 * Set tool tips.
	 */
	private void setToolTips() {
		
		buttonLoadDatabaseNames.setToolTipText(Resources.getString("org.multipage.basic.tooltipUpdateDatabaseNames"));
	}

	/**
	 * On login button pressed.
	 */
	protected void onLogin() {
		
		// Initialize.
		boolean exit = false;
		
		// Get HTTP server object.
		ProgramHttpServer server = ProgramBasic.getHttpServer();
		
		// Get focused component.
		focus = getFocusOwner();
		
		// Try to login.
		if (attempts > 0) {
			
			start_login: while (!exit) {
			
				// Check login properties.
				Properties properties = getLoginPropertiesPrivate();
				result = ProgramBasic.getMiddle().checkLogin(properties);
				
				if (result != MiddleResult.OK) {
					
					// Inform user.
					result.show(this);
					
					// On bad database.
					if (result == MiddleResult.DATABASE_NOT_FOUND) {
						
						// Get database name.
						String databaseName = properties.getProperty("database");
						
						// Confirm database.
						if (Utility.ask("org.multipage.basic.messageCreateNewDatabase", databaseName)) {
							
							// create new database.
							result = createNewDatabase(databaseName);
							if (result.isNotOK()) {
								result.show(null);
							}
							else {
								continue;
							}
						}
					}
					
					// On bad credentials.
					else if (result == MiddleResult.BAD_USERNAME || result == MiddleResult.BAD_PASSWORD) {
						
						// Decrement attempts counter.
						attempts--;
						if (attempts == 0) {

							maximumAttemptsReached();
							return;
						}
						
						SwingWorker<Void, Void> thread = new SwingWorker<Void, Void>() {
							// Do background.
							@Override
							protected Void doInBackground() throws Exception {

								try {
									setEnabled(false);
									// Set progress.
									setProgress(0);
									// Do loop and update progress bar.
									int interval = 20; // milliseconds
									for (int milliseconds = 0; milliseconds <= loginSleep; milliseconds += interval) {
										Thread.sleep(interval);
										setProgress(milliseconds * 100 / (int) loginSleep);
									}
									// Set progress.
									setProgress(100);
									setEnabled(true);
									
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								return null;
							}
							// On done.
							@Override
							protected void done() {

								// On thread end.
								setEnabled(true);
								progress.setValue(0);
								passwordText.setText("");
								progress.setVisible(false);
								
								// Request old focus.
								if (focus != null) {
									focus.requestFocusInWindow();
								}
							}
						};
						// Set properties listener.
						thread.addPropertyChangeListener(new PropertyChangeListener() {
							// Property change listener.
							@Override
							public void propertyChange(PropertyChangeEvent evt) {
								// Get property name.
								String propertyName = evt.getPropertyName();
								// If it is state property.
								if (propertyName == "state") {
									
									SwingWorker.StateValue state = (SwingWorker.StateValue) evt.getNewValue();
									
									if (state == SwingWorker.StateValue.STARTED) {
										progress.setVisible(true);
										setEnabled(false);
									}
								}
								// If it is progress property.
								else if (propertyName == "progress") {
									
									int progressValue = (Integer) evt.getNewValue();
									// Set progress bar.
									progress.setValue(progressValue);
								}
							}
						});
						// Start the thread.
						thread.execute();
					}
					else if (result == MiddleResult.SSL_NOT_SUPPORTED_BY_SERVER) {
						
						// Ask user if should use not not encrypted connection.
						if (JOptionPane.showConfirmDialog(this, Resources.getString("org.multipage.basic.messageUseNotEncryptedConection")) == JOptionPane.YES_OPTION) {
							sslCheckBox.setSelected(false);
							continue start_login;
						}
					}
				}
				else {
					// If login is OK.
					progress.setToolTipText("");
					loginFlag = true;
					result = MiddleResult.OK;
					
					addComboEditorValueToList();
					saveDialog();
					dispose();
					
					// Set HTTP server login information.
					if (server != null) {
						server.setLogin(ProgramBasic.getLoginProperties());
					}
				}
				
				exit = true;
			}
		}
		else {
			maximumAttemptsReached();
		}
	}
	
	/**
	 * Add editor combo box value to the list.
	 */
	private void addComboEditorValueToList() {
		
		// Get editor value.
		String editorValue = (String) comboDatabaseNames.getEditor().getItem();
		
		// Add editor value to the list.
		addDatabaseName(editorValue);
	}

	/**
	 * On maximum attempts reached.
	 */
	private void maximumAttemptsReached() {

		// Inform user.
		JOptionPane.showMessageDialog(this, Resources.getString("org.multipage.basic.messageMaximumAttempts"));
		// Cancel login.
		loginFlag = false;
		saveDialog();
		dispose();
	}

	/**
	 * Gets login properties.
	 * @return
	 */
	public synchronized Properties getLoginProperties() {
		
		Properties properties = new Properties();
		
		char [] passwordArea = passwordText.getPassword();
		
		properties.setProperty("username", userName);
		properties.setProperty("password", String.valueOf(passwordArea));
		properties.setProperty("server", serverName);
		properties.setProperty("port", String.valueOf(portNumber));
		properties.setProperty("ssl", sslFlag ? "true" : "false");
		
		if (databaseName.isEmpty()) {
			databaseName = "empty";
		}
		properties.setProperty("database", databaseName);
		
		// Remove passwordText.
		for (int index = 0; index < passwordArea.length; index++) {
			passwordArea[index] = 0; 
		}
		
		return properties;
	}
	
	/**
	 * Get login properties.
	 * @return
	 */
	private Properties getLoginPropertiesPrivate() {
		
		Properties properties = new Properties();
		
		StringBuilder password = new StringBuilder();
		password.append(passwordText.getPassword());
		
		// Inform about empty password.
		if (password.length() <= 0) {
			Utility.show(this, "org.multipage.basic.messageEmptyPassword");
		}
		
		properties.setProperty("username", usernameText.getText());
		properties.setProperty("password", password.toString());
		properties.setProperty("server", serverText.getText());
		properties.setProperty("port", portText.getText());
		properties.setProperty("ssl", sslCheckBox.isSelected() ? "true" : "false");
		
		String databaseName = comboDatabaseNames.getEditor().getItem().toString();
		if (databaseName.isEmpty()) {
			databaseName = "empty";
		}
				
		properties.setProperty("database", databaseName);
		
		// Remove passwordText.
		for (int index = 0; index < password.length(); index++) {
			password.setCharAt(index, '\00');
		}
		
		
		return properties;
	}
	
	/**
	 * Show more.
	 */
	private void showMore(boolean show) {
		
		// Set dialog size.
		setSize(show ? extendedSize : normalSize);
		// Set more panel visible / not visible.
		morePanel.setVisible(show);
	}

	/**
	 * @return the loginFlag
	 */
	public boolean isReturnedFlag() {
		return loginFlag;
	}

	/**
	 * @param attempts the attempts to set
	 */
	public void setAttempts(int attempts) {
		this.attempts = attempts;
	}

	/**
	 * @return the success
	 */
	public boolean isSuccess() {
		return result.isOK();
	}

	/**
	 * On remove database name.
	 */
	protected void onRemoveDatabaseName() {
		
		// Get editor text value.
		String editorValue = (String) comboDatabaseNames.getEditor().getItem();
		if (editorValue.isEmpty()) {
			return;
		}
		
		// Ask user.
		if (!Utility.ask(this, "org.multipage.basic.messageRemoveCurrentDatabaseNameFromList")) {
			return;
		}
		
		// Try to remove given list item.
		removeDatabaseName(editorValue);
	}
	
	/**
	 * Removes database name from combo box list.
	 */
	protected void removeDatabaseName(String databaseName) {
		
		ComboBoxModel<String> model = comboDatabaseNames.getModel();
		
		// Try to remove given list item.
		for (int index = 0; index < model.getSize(); index++) {
			String itemValue = model.getElementAt(index);
			
			if (itemValue.contains(databaseName)) {
				
				// Remove the item, initialize combo box editor and exit the loop.
				comboDatabaseNames.removeItemAt(index);
				updateDatabaseNamesComboBox();
				break;
			}
		}
	}
	
	/**
	 * Get list of available databases.
	 * @return
	 */
	private MiddleResult getAvailableDatabases(LinkedList<String> databaseNames) {
		
		// Get available database names.
		Properties loginProperties = getLoginPropertiesPrivate();
		
		MiddleResult result = ProgramBasic.getMiddle().getDatabaseNames(loginProperties, databaseNames);
		return result;
	}
	
	/**
	 * Update database names combo box.
	 */
	private void updateDatabaseNamesComboBox() {
		
		// Get available databases.
		LinkedList<String> databaseNames = new LinkedList<String>();
		MiddleResult result = getAvailableDatabases(databaseNames);
		
		// Delegate the call.
		if (result.isOK()) {
			updateDatabaseNamesComboBox(databaseNames);
		}
	}
	
	/**
	 * Update database names combo box.
	 */
	private void updateDatabaseNamesComboBox(LinkedList<String> databaseNames) {
		
		// Add new database names.
		addNewDatabaseNames(databaseNames);
		
		// Set editor text
		String editorValue = "";
		
		ComboBoxModel<String> model = comboDatabaseNames.getModel();
		if (model.getSize() > 0) {
			editorValue = model.getElementAt(0);
		}
		
		// Reset editor value.
		comboDatabaseNames.getEditor().setItem(editorValue);
	}
	
	/**
	 * On update database names.
	 */
	protected void onUpdateDatabaseNames() {
		
		// Get available databases.
		LinkedList<String> databaseNames = new LinkedList<String>();
		MiddleResult result = getAvailableDatabases(databaseNames);
		if (result.isNotOK()) {
			Utility.show(this, "org.multipage.basic.messageCannotGetAvailableDatabases", result.getMessage());
			return;
		}
		
		// Let user select current database
		String currentDatabase = SelectDatabaseDialog.showDialog(this, databaseNames);
		
		// Update combo box.
		updateDatabaseNamesComboBox(databaseNames);
		
		// Select current database.
		comboDatabaseNames.setSelectedItem(currentDatabase);
	}
	
	/**
	 * On new database.
	 */
	protected void onNewDatabase() {
		
		// Get database name.
		String databaseName = Utility.input(this, "org.multipage.basic.textInsertNewDatabaseName");
		if (databaseName == null) {
			return;
		}
		
		// Create new database.
		createNewDatabase(databaseName);
	}
	
	/**
	 * Create new database.
	 * @param databaseName
	 */
	public MiddleResult createNewDatabase(String databaseName) {
		
		// Get connection properties.		
		String userName = usernameText.getText();
		char [] passwordArea = passwordText.getPassword();
		String password =  String.valueOf(passwordArea);
		String server = serverText.getText();
		int port = -1;
		try {
			port = Integer.parseInt(portText.getText());
		}
		catch (Exception e) {
		}
		boolean useSsl = sslCheckBox.isSelected();
		
		// Try to create new database.
		MiddleResult result = ProgramBasic.getMiddle().createDatabase(server, port, useSsl, userName, password, databaseName);
		if (result.isNotOK()) {
			result.show(this);
			
			return result;
		}
		
		// Set new database name.
		LoginDialog.databaseName = databaseName;
		addDatabaseName(databaseName);
		comboDatabaseNames.setSelectedItem(databaseName);
		
		return MiddleResult.OK;
	}
	
	/**
	 * Remove old database.
	 */
	protected void onRemoveDatabase() {
		
		// Get database names list.
		LinkedList<String> databaseNames = new LinkedList<String>();
		MiddleResult result = getAvailableDatabases(databaseNames);
		
		if (result.isNotOK()) {
			Utility.show(this, "org.multipage.basic.messageCannotGetAvailableDatabases", result.getMessage());
			return;
		}
		
		// Select database.
		String databaseName = SelectDatabaseDialog.showDialog(this, databaseNames);
		if (databaseName == null) {
			return;
		}
		
		// Confirm deletion.
		if (!Utility.ask(this, "org.multipage.basic.messageDropDatabase", databaseName)) {
			return;
		}
		
		// Get connection properties.	
		Properties loginProperties = getLoginPropertiesPrivate();
		String userName = loginProperties.getProperty("username");
		String password =  loginProperties.getProperty("password");
		String server = loginProperties.getProperty("server");
		String portString = loginProperties.getProperty("port");
		int port = Integer.parseInt(portString);
		String useSslString = loginProperties.getProperty("useSsl");
		boolean useSsl = Boolean.getBoolean(useSslString);
		
		// Try to remove selected database.
		result = ProgramBasic.getMiddle().dropDatabase(server, port, useSsl, userName, password, databaseName);
		if (result.isNotOK()) {
			result.show(this);
		}
		
		// Remove database name from combobox list
		removeDatabaseName(databaseName);
	}
}
