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
	 * STATES AND TRANSITIONS.
	 */
	public class States {

		/**
		 * Locked flag.
		 */
		private boolean locked;
		
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
			
			locked = false;
			saved = true;
		}

		/**
		 * Set locked.
		 * @param locked
		 */
		public void setLocked(boolean locked) {
			
			this.locked = locked;
		}
		
		/**
		 * Get locked flag.
		 * @return
		 */
		public boolean isLocked() {
			
			return this.locked;
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
	
	public States states = new States();
	
	/**
	 * Save timer.
	 */
	private Timer saveTimer;
	
	/**
	 * Events.
	 */
	public Function<TextFieldAutoSave, Function<Runnable, Consumer<Runnable>>> saveTextEvent = null;
	public Consumer<Boolean> enableEvent = null;
	public Supplier<String> getGenuineTextEvent = null;
	public Runnable focusGainedEvent = null;
	public Runnable updateEvent = null;
	
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
				onChangeText();
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				onChangeText();
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				onChangeText();
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
				if (focusGainedEvent != null) {
					focusGainedEvent.run();
				}
			}
			
			// Wrap the call.
			@Override
			public void focusLost(FocusEvent e) {
				
				super.focusLost(e);
				
				// Try to save unsaved text content.
				if (!states.isSaved()) {
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
	public void setUser(Object userObject) {
		
		this.userObject = userObject;
	}
	
	/**
	 * Get user object.
	 * @return
	 */
	public Object getUser() {
		
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
	public void setTextEx(String text) {
		
		// Check lock.
		if (states.isLocked()) {
			return;
		}
		
		// Save caret.
		boolean hasFocus = hasFocus();
		if (hasFocus) {
			saveCaret();
		}
		
		// Get genuine text.
		String genuineText = getText();
		
		// Delegate the call.
		if (!genuineText.equals(text)) {
			
			super.setText(text);
			
			// Set unsaved state.
			states.setSaved(false);
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
	 * Save text.
	 */
	public void saveText() {
		
		// Invoke event.
		if (saveTextEvent != null) {
			
			// Save text with on finished callback.
			saveTextEvent.apply(this)
				
				// On save finished.
				.apply(() -> {
				
					// Set the flag.
					states.setSaved(true);
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
		
		// If the description changes...
		if (isTextChange()) {
			
			// Lock the text box
			lockInput(true);
			
			// Invoke event.
			if (saveTextEvent != null) {
				
				// Save text with on finished callback.
				saveTextEvent.apply(this)
					
					// On save finished.
					.apply(() -> {
						
						// Set the flag.
						states.setSaved(true);
						
						// Unlock the text box
						lockInput(false);
					})
					
					// On request update.
					.accept(() -> {
						
						// Call update event.
						if (updateEvent != null) {
							updateEvent.run();
						}
					});
			}
			else {
				// Unlock the text box
				lockInput(false);
			}
		
			// Remove highlight
			setForeground(Color.BLACK);
		}
	}
	
	/**
	 * Returns true if the text has been.
	 * @return
	 */
	public boolean isTextChange() {
		
		// Check the callback.
		if (getGenuineTextEvent == null) {
			return false;
		}
		
		// Find out description changes
		try {
			
			String genuineText = getGenuineTextEvent.get();	
			String text = getText();
			
			return text.compareTo(genuineText) != 0;
		}
		catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * On change text.
	 */
	public void onChangeText() {
		
		Color color;
	
		// If the current area description is not equal to loaded area
		// description set red text color.
		if (isTextChange()) {
			color = Color.RED;
			
			// Start save timer.
			saveTimer.restart();
		}
		else {
			color = Color.black;
		}
		
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
	 * Lock text box input
	 * @param lock
	 */
	public void lockInput(boolean lock) {
		
		// Save caret on lock.
		if (lock) {
			saveCaret();
		}
		
		// Enable/disable the text box
		setEnabled(!lock);
		setForeground(lock ? Color.DARK_GRAY : Color.BLACK);
		setBackground(Color.WHITE);
		
		// Set state.
		states.setLocked(lock);
		
		// Invoke enable event.
		if (enableEvent != null) {
			enableEvent.accept(lock);
		}
		
		// Restore caret on unlock.
		if (!lock) {
			restoreCaret();
		}
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
