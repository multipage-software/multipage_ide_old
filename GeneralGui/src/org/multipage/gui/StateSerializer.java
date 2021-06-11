/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.gui;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.util.LinkedList;

import javax.swing.JOptionPane;

import org.multipage.util.Resources;
import org.multipage.util.j;

/**
 * @author
 *
 */
public class StateSerializer {
	
	/**
	 * Output and input stream that saves or loads application state.
	 */
	public String             settingsFileName;
	public FileOutputStream   saveStateOutputFile;
	public ObjectOutputStream saveStateOutputStream;
	public FileInputStream    loadStateInputFile;
	public ObjectInputStream  loadStateInputStream;
	
	/**
	 * Serialize state listeners list reference. 
	 */
	public LinkedList<SerializeStateAdapter> serializeStateListenersRef =
		new LinkedList<SerializeStateAdapter>();

	/**
	 * Constructor.
	 * @param settingsFileName
	 */
	public StateSerializer(String settingsFileName) {

		this.settingsFileName = settingsFileName;
	}
	
	/**
	 * Constructor.
	 * @param settingsFilePath
	 */
	public StateSerializer(Path settingsFilePath) {
		
		this.settingsFileName = settingsFilePath.toString();
	}

	/**
	 * Constructor.
	 */
	public StateSerializer() {
		this("");
	}

	/**
	 * Add listener.
	 */
	public void add(SerializeStateAdapter listener) {

		serializeStateListenersRef.add(listener);
	}

	/**
	 * Opens output stream that saves application state.
	 * @return - true if file found, else false
	 */
	public boolean openStateOutputStream() {
		
		// Check if the output file is already opened. Return true if the file descriptor is valid.
		try {
			
			saveStateOutputFile.getFD();
			return true;
		}
		catch (Exception e) {
			
		}
		try {
			
			// Open settings file and create object output stream.
			saveStateOutputFile = new FileOutputStream(settingsFileName);
			saveStateOutputStream = new ObjectOutputStream(saveStateOutputFile);
		}
		catch (Exception e) {
			
			// Inform user about error and return false.
			JOptionPane.showMessageDialog(null, Resources.getString("org.multipage.gui.errorCannotSaveApplicationState"));
			return false;
		}
		return true;
	}
	
	/**
	 * Open state input stream.
	 */
	public boolean openStateInputStream() {
		
		// Check if the input file is already opened. Return true if the file descriptor is valid.
		try {
			
			loadStateInputFile.getFD();
			return true;
		}
		catch (Exception e) {
			
		}
		try {
			
			// Open settings file and create object input stream.
			loadStateInputFile   = new FileInputStream(settingsFileName);
			loadStateInputStream = new ObjectInputStream(loadStateInputFile);
		}
		catch (Exception e) {
			
			// Inform user about error and return false.
			//JOptionPane.showMessageDialog(null, Resources.getString("org.multipage.gui.errorCannotFindApplicationStateUsingDef"));
			return false;
		}
		return true;
	}

	/**
	 * Closes current state output stream.
	 * @return - return true if the stream is closed, else returns false.
	 */
	public boolean closeStateOutputStream() {
		
		// Check if the file is opened. if not return true;
		try {
			
			saveStateOutputFile.getFD();
		}
		catch (Exception e) {
			
			return true;
		}
		try {
			// Try to close file and stream.
			saveStateOutputStream.close();
			saveStateOutputFile.close();
			return true;
		}
		catch (Exception e) {
			
			// Inform user about error.
			JOptionPane.showMessageDialog(null, e.getMessage());
		}
		return false;
	}
	
	/**
	 * Close state input stream.
	 */
	public boolean closeStateInputStream() {
		
		// Check if the file is opened. if not return true;
		try {
			
			loadStateInputFile.getFD();
		}
		catch (Exception e) {
			
			return true;
		}
		try {
			// Try to close file and stream.
			loadStateInputStream.close();
			loadStateInputFile.close();
			return true;
		}
		catch (Exception e) {
			
			// Inform user about error.
			JOptionPane.showMessageDialog(null, e.getMessage());
		}
		return false;
	}

	/**
	 * Load default states.
	 */
	public void loadDefaultStates() {

		// Do loop for all listeners. Invoke set default events.
		for (SerializeStateAdapter listener : serializeStateListenersRef) {
			// Invoke lister.
			listener.onSetDefaultState();
		}

	}
	
	/**
	 * On load windows state.
	 */
	public void startLoadingSerializedStates() {
		
		// Load states.
		if (openStateInputStream()) {

			try {
				// Do loop for all listeners. Invoke read state event.
				for (SerializeStateAdapter listener : serializeStateListenersRef) {
					
					// Invoke lister.
					listener.onReadState(loadStateInputStream);
				}
			}
			catch (Exception e) {
				//JOptionPane.showMessageDialog(null, Resources.getString("org.multipage.gui.errorCannotFindApplicationStateUsingDef"));
				// Load default data.
				loadDefaultStates();
			}
			closeStateInputStream();
		}
		else {
			// Load default data.
			loadDefaultStates();
		}
	}

	/**
	 * On save windows state.
	 */
	public void startSavingSerializedStates() {
		
		// Save states.
		if (openStateOutputStream()) {
			
			try {
				// Do loop for all listeners. Invoke write state events.
				for (SerializeStateAdapter listener : serializeStateListenersRef) {
					// Invoke lister.
					listener.onWriteState(saveStateOutputStream);
				}
			}
			catch (IOException e) {
				// Inform user.
				JOptionPane.showMessageDialog(null, Resources.getString("org.multipage.gui.errorCannotSaveApplicationState"));
			}
			closeStateOutputStream();
		}
	}
}
