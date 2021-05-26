package org.multipage.generator;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.multipage.gui.StringValueEditor;
import org.multipage.gui.TextFieldEx;
import org.multipage.util.Resources;

/**
 * 
 * @author
 *
 */
public class IntegerEditorPanel extends JPanel implements SlotValueEditorPanelInterface {
	
	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Value
	 */
	private Long number = null;

	// $hide<<$
	/**
	 * Components.
	 */
	private JTextField textInteger;
	private JLabel labelMessage;

	/**
	 * Create the panel.
	 */
	public IntegerEditorPanel() {
		
		// Initialize components.
		initComponents();
		// $hide>>$
		// Post creation.
		postCreation();
		// $hide<<$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		SpringLayout springLayout = new SpringLayout();
		setLayout(springLayout);
		
		textInteger = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textInteger, 16, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, textInteger, 0, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, textInteger, 0, SpringLayout.EAST, this);
		add(textInteger);
		textInteger.setColumns(10);
		
		labelMessage = new JLabel("");
		springLayout.putConstraint(SpringLayout.EAST, labelMessage, 0, SpringLayout.EAST, textInteger);
		labelMessage.setFont(new Font("Tahoma", Font.ITALIC, 11));
		labelMessage.setForeground(Color.RED);
		labelMessage.setHorizontalAlignment(SwingConstants.CENTER);
		springLayout.putConstraint(SpringLayout.NORTH, labelMessage, 6, SpringLayout.SOUTH, textInteger);
		springLayout.putConstraint(SpringLayout.WEST, labelMessage, 0, SpringLayout.WEST, textInteger);
		add(labelMessage);
	}

	/**
	 * Get value.
	 */
	@Override
	public Object getValue() {

		if (number == null && !ProgramGenerator.isExtensionToBuilder()) {
			number = 0L;
		}
		return number;
	}

	/**
	 * Set value.
	 */
	@Override
	public void setValue(Object value) {
		
		if (value instanceof Long) {
			number = (Long) value;
		}
		else if (value instanceof Integer) {
			number = (Long) value;
		}
		else {
			number = null;
		}
		
		if (number != null) {
			textInteger.setText(String.valueOf(number));
		}
		else {
			textInteger.setText("");
		}
	}
	
	/**
	 * Post creation.
	 */
	private void postCreation() {

		// Set editor listener.
		setEditorListener();
	}

	/**
	 * Set editor listener.
	 */
	private void setEditorListener() {

		textInteger.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				onEditorChange();
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				onEditorChange();
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				onEditorChange();
			}
		});
	}

	/**
	 * On editor change.
	 */
	protected void onEditorChange() {

		// Try to convert the value.
		String text = textInteger.getText();
		String message = "";
		
		try {
			if (!text.isEmpty()) {
				number = Long.parseLong(text);
			}
			else {
				number = null;
			}
		}
		catch (NumberFormatException e) {
			
			number = null;
			message = Resources.getString("org.multipage.generator.messageErrorIntegerNumber");
		}
		
		labelMessage.setText(message);
	}

	/**
	 * Clear editor.
	 */
	public void clear() {

		number = null;
		textInteger.setText("");
	}

	/**
	 * @return the textInteger
	 */
	public JTextField getTextInteger() {
		return textInteger;
	}

	/**
	 * Set default value state.
	 */
	@Override
	public void setDefault(boolean isDefault) {
		
		// Nothing to do.
	}

	/**
	 * Get value meaning.
	 */
	@Override
	public String getValueMeaning() {
		
		return StringValueEditor.meansInteger;
	}
}
