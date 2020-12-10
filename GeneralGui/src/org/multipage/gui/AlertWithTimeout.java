package org.multipage.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import org.multipage.util.Obj;
import org.multipage.util.Resources;

/**
 * 
 * @author user
 *
 */
public class AlertWithTimeout extends JDialog {
	
	/**
	 * Version
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * List of messages
	 */
	private static final LinkedList<String> messages = new LinkedList<String>();

	/**
	 * A dialog field.
	 */
	private static AlertWithTimeout dialog;
	
	/**
	 * Components
	 */
	private JLabel labelAlert;
	private JPanel panel;
	private JButton buttonOk;
	private int timeout;
	private String buttonTextWithTimeout;
	private String buttonText;
	private Timer timer;
	
	/**
	 * Show dialog
	 * @param parent
	 * @param timeout 
	 * @param text 
	 * @return
	 */
	public static void showDialog(Component parent, String alert, int timeout) {
		
		if (dialog == null) {
			dialog = new AlertWithTimeout(parent);
			dialog.postCreate();
		}
		dialog.set(alert);
		dialog.set(timeout);
		dialog.setVisible(true);
		dialog.start();
	}

	/**
	 * Create the dialog.
	 * @param parent 
	 */
	public AlertWithTimeout(Component parent) {
		super(Utility.findWindow(parent), ModalityType.APPLICATION_MODAL);
		
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		initComponents();
	}
	
	/**
	 * Post creation of the dialog
	 */
	private void postCreate() {
		
		Utility.centerOnScreen(this);
		localize();
	}

	/**
	 * Initialize components
	 */
	private void initComponents() {
		setTitle("org.multipage.gui.titleAlert");
		setIconImage(Toolkit.getDefaultToolkit().getImage(AlertWithTimeout.class.getResource("/org/multipage/gui/images/main_icon.png")));
		setBounds(100, 100, 367, 167);
		
		labelAlert = new JLabel("Alert text");
		labelAlert.setHorizontalAlignment(SwingConstants.CENTER);
		getContentPane().add(labelAlert, BorderLayout.CENTER);
		
		panel = new JPanel();
		panel.setPreferredSize(new Dimension(10, 35));
		getContentPane().add(panel, BorderLayout.SOUTH);

		
		buttonOk = new JButton("");
		buttonOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onOk();
			}
		});
		buttonOk.setPreferredSize(new Dimension(100, 25));
		panel.add(buttonOk);
	}

	/**
	 * Localize components
	 */
	private void localize() {
		
		Utility.localize(this);
		buttonText = Resources.getString("textOk");
		buttonTextWithTimeout = Resources.getString("org.multipage.textOkWithTimeout");
		button(timeout);
	}

	/**
	 * Set alert message
	 */
	private void set(String alert) {
		
		messages.add(alert);
		
		Obj<String> message = new Obj<String>("<html>");
		messages.stream().forEach((String text) -> {
			message.ref += text + "<br>";
		});
		message.ref += "<br>";
		labelAlert.setText(message.ref);
	}
	
	/**
	 * Set timeout value
	 * @param timeout
	 */
	private void set(int timeout) {
		
		if (timeout >= 0)
			this.timeout = timeout / 1000;
		else
			this.timeout = -1;
	}
	
	/**
	 * Start count down
	 */
	private void start() {
		
		if (timeout < 0) {
			button(-1);
			return;
		}
		
		buttonOk.setForeground(Color.GRAY);
		
		final int delay = 1000;
		timer = new Timer(delay, (e) -> {
			button(timeout);
			if (timeout >= 0) {
				timeout--;
			}
			else {
				buttonOk.setEnabled(true);
				buttonOk.setForeground(Color.BLACK);
				timer.stop();
			}
			
		});
		timer.setInitialDelay(0);
		timer.start();
	}
	
	/**
	 * Get button text
	 * @param timeout
	 * @return
	 */
	private String buttonText(int timeout) {
		
		return timeout >= 0 ? String.format(buttonTextWithTimeout, timeout) : buttonText;
	}
	
	/**
	 * Display button text with timeout value
	 * @param timeout
	 */
	private void button(int timeout) {
		
		buttonOk.setText(buttonText(timeout));
	}
	
	/**
	 * On OK
	 */
	protected void onOk() {
		
		if (timeout < 0) {
			buttonOk.setText("");
			messages.clear();
			setVisible(false);
		}	
	}
}
