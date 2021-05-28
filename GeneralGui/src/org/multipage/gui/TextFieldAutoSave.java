/*
 * Copyright 2010-2020 (C) vakol
 * 
 * Created on : 06-01-2021
 *
 */
package org.multipage.gui;

import java.awt.Color;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusEvent.Cause;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * @author Dell
 *
 */
public class TextFieldAutoSave extends TextFieldEx {

	/**
	 * Version
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Delay in milliseconds for the SAVE TEXT operation.
	 */
	private static final int saveTimerDelay = 2000;
	
	/**
	 * Last text caret.
	 */
	private static TextCaret lastTextCaret = null;
	
	/**
	 * Identifier of this text box.
	 */
	public Object identifier = "unknown";
	
	/**
	 * User object.
	 */
	private Object userObject = null;
	
	/**
	 * Text string. When displayed, it can be edited by user.
	 */
	private String text = null;
	
	/**
	 * Message string. It cannot be edited when displayed in the text box.
	 */
	private String message = null;

	
	/**
	 * STATES AND TRANSITIONS.
	 */
	public class States {
		
		/**
		 * Content changed by user flag.
		 */
		private boolean userChanged;
		
		/**
		 * Content changed by computer flag.
		 */
		private boolean computerChanged;
		
		/**
		 * Content save flag.
		 */
		private boolean saved;

		
		/**
		 * Constructor.
		 */
		public States() {
			
			initialize();
		}		
		
		/**
		 * Initialize states.
		 */
		public void initialize() {
			
			userChanged = false;
			computerChanged = false;
			saved = true;
		}
		
		/**
		 * Set changed by user state.
		 * @param changed
		 */
		public void setUserChanged(boolean changed) {
			
			this.userChanged = changed;
		}
		
		/**
		 * Get changed by computer flag.
		 * @return
		 */
		public boolean isComputerChanged() {
			
			return this.computerChanged;
		}
		
		/**
		 * Set changed by computer state.
		 * @param changed
		 */
		public void setComputerChanged(boolean changed) {
			
			this.computerChanged = changed;
		}
		
		/**
		 * Get changed by user flag.
		 * @return
		 */
		public boolean isUserChanged() {
			
			return this.userChanged;
		}
		
		/**
		 * Set saved state.
		 * @param saved
		 */
		public void setSaved(boolean saved) {
			
			this.saved = saved;
		}
		
		/**
		 * Get saved flag.
		 * @return
		 */
		public boolean isSaved() {
			
			return this.saved;
		}
	};
	
	public States state = new States();
	
	/**
	 * Save timer.
	 */
	private Timer saveTimer;
	
	/**
	 * Lambda functions.
	 */
	public Function<TextFieldAutoSave, Function<Runnable, Consumer<Runnable>>> saveTextLambda = null;
	public Supplier<String> getGenuineTextLambda = null;
	public Runnable focusGainedLambda = null;
	public Runnable updateLambda = null;
	
	/**
	 * Constructor which takes this text field identifier of any type.
	 */
	public TextFieldAutoSave(Object identifier) {
		
		// $hide>>$
		this.identifier = identifier;
		
		// Set text box listeners and timers for SAVE operation.
		setListeners();
		setTimers();
		// $hide<<$
	}
	
	/**
	 * Set text editor listeners.
	 */
	private void setListeners() {
		
		// Set text boxes content change callback function.
        getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				
				// Delegate call.
				if (!state.isComputerChanged()) {
					onUserChangedText();
				}
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				
				// Delegate call.
				if (!state.isComputerChanged()) {
					onUserChangedText();
				}
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				
				// Delegate call.
				if (!state.isComputerChanged()) {
					onUserChangedText();
				}
			}
        });
        
        // Add key listener.
        addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				
				// Delegate the call.
				super.keyPressed(e);
				
				// On ENTER save the text box text.
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					saveTextInternal();
				}
			}
		});
		
        // Add focus listener.
		addFocusListener(new FocusAdapter() {
			
			// Wrap the call.
			@Override
			public void focusGained(FocusEvent e) {
				
				super.focusGained(e);
				
				// Get focus event cause.
				Cause cause = e.getCause();
				
				// Check focus event cause.
				if (FocusEvent.Cause.UNKNOWN.equals(cause)
					|| FocusEvent.Cause.UNEXPECTED.equals(cause)
					|| FocusEvent.Cause.ROLLBACK.equals(cause)) {
					return;
				}
				
				// Invoke input listener.
				if (focusGainedLambda != null) {
					focusGainedLambda.run();
				}
			}
			
			// Wrap the call.
			@Override
			public void focusLost(FocusEvent e) {
				
				super.focusLost(e);
				
				// Try to save unsaved text content.
				if (!state.isSaved()) {
					saveTextInternal();
				}
			}
		});
		
		// Add a listener that saves the text box caret.
		addCaretListener(e -> {
			
			saveCaret();
		});
    }
	
	/**
	 * Set user object reference.
	 * @param userObject
	 */
	public void setUserObject(Object userObject) {
		
		this.userObject = userObject;
	}
	
	/**
	 * Get user object.
	 * @return
	 */
	public Object getUserObject() {
		
		return this.userObject;
	}
	
	/**
	 * Set save timer.
	 */
	private void setTimers() {

		// Create timer firing one event.
		saveTimer = new Timer(saveTimerDelay, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				// Save box text.
				saveTextInternal();
			}
		});
				
		saveTimer.setInitialDelay(saveTimerDelay);
		saveTimer.setRepeats(false);
	}
	
	/**
	 * Set 
	 * @param text
	 */
	public void setText(String text) {
		
		// Trim text.
		if (text == null) {
			text = "";
		}
		
		// Set text reference.
		this.text = text;
		
		// Remove message text.
		this.message = null;
		
		// Save caret.
		boolean hasFocus = hasFocus();
		if (hasFocus) {
			saveCaret();
		}
		
		// Get genuine text.
		String genuineText = getText();
		
		// Delegate the call.
		if (genuineText != null && !genuineText.equals(text)) {
			
			state.setComputerChanged(true);
			
			super.setText(text);
			
			// Reset flag.
			SwingUtilities.invokeLater(() -> state.setComputerChanged(false));
		}
		
		// Request end of update signals.
		SwingUtilities.invokeLater(() -> {
			
			// Restore caret.
			if (hasFocus) {
				restoreCaret();
			}
		});
	}
	
	/**
	 * Get text string.
	 */
	@Override
	public String getText() {
		
		// Check presence of editable text.
		if (this.text == null) {
			return null;
		}
		
		// Retrieve current text.
		this.text = super.getText();
		return this.text;
	}
	
	/**
	 * Set message.
	 * @param message
	 */
	public void setMessage(String message) {
		
		// First remove editable text.
		this.text = null;
		
		// Save the reference to a message.
		this.message = message;
		
		// Set the text box message.
		super.setText(message);
	}
	
	/**
	 * Save text.
	 */
	public void saveText() {
		
		// Check text presence.
		if (this.text == null) {
			return;
		}
		
		// Invoke event.
		if (saveTextLambda != null) {
			
			// Save text with on finished callback.
			saveTextLambda.apply(this)
				
				// On save finished.
				.apply(() -> {
				
					// Set the flag.
					state.setSaved(true);
				})
				
				// On request update.
				.accept(() -> {
					
					// Do nothing
				});
		}
	}
	
	/**
	 * Save text. An internal procedure.
	 */
	private void saveTextInternal() {
		
		// Check text presence.
		if (this.text == null) {
			return;
		}
		
		// If the description changes...
		if (state.isUserChanged()) {
			
			// Invoke event.
			if (saveTextLambda != null) {
				
				// Save text with on finished callback.
				saveTextLambda.apply(this)
					
					// On save finished.
					.apply(() -> {
						
						// Set the flag.
						state.setSaved(true);
						// Reset change flag.
						state.setUserChanged(false);
					})
					
					// On request update.
					.accept(() -> {
						
						// Call update event.
						if (updateLambda != null) {
							updateLambda.run();
						}
					});
			}
		
			// Remove highlight
			setForeground(Color.BLACK);
		}
	}
	
	/**
	 * Returns true if the text box ha genuine content.
	 * @return
	 */
	public boolean isGenuine() {
		
		// Check the callback.
		if (getGenuineTextLambda == null) {
			return false;
		}
		
		// Find out description changes
		try {
			
			String genuineText = getGenuineTextLambda.get();	
			String text = super.getText();
			
			if (text == null || genuineText == null) {
				return false;
			}
			
			return text.compareTo(genuineText) != 0;
		}
		catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * Check if the text content has been changed by user.
	 * @return
	 */
	public boolean isTextChangedByUser() {
		
		return state.isUserChanged();
	}
	
	/**
	 * On new text.
	 */
	public void onUserChangedText() {
		
		// Set flag.
		state.setUserChanged(true);
		
		Color color;
		boolean isMessage = this.message != null;
		
		// Enable/disable the text box.
		setEnabled(!isMessage);
		
		// On message.
		if (isMessage) {
			color = Color.lightGray;
		}
		// On simple text.
		else {
	
			// If the current text is not equal to loaded area
			// description set red text color.
			color = Color.RED;
			
			// Start save timer.
			saveTimer.restart();
		}
		
		// Set the color.
		setForeground(color);
	}
	
	/**
	 * Returns true if this text box has focus.
	 */
	public boolean hasFocus() {
		
		Object owner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
		boolean hasFocus = this.equals(owner);
		return hasFocus;
	}
	
	/**
	 * Remember caret position.
	 */
	public void saveCaret() {
		
		TextFieldAutoSave.lastTextCaret = new TextCaret(this);
	}
	
	/**
	 * Restore text box caret.
	 */
	public void restoreCaret() {
		
		// Try to restore the caret position.
		SwingUtilities.invokeLater(() -> {
			
			// Check last caret component.
			if (lastTextCaret != null && lastTextCaret.isFor(this)) {
				
				// Set focus.
				requestFocusInWindow();
				
				try {
					setCaretPosition(lastTextCaret.position);
				}
				catch (Exception e) {
				}
				
				// Reset caret.
				TextFieldAutoSave.lastTextCaret = null;
			}
		});
	}
}
