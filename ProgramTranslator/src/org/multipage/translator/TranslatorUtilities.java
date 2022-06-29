/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.translator;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

import javax.swing.*;

import org.apache.commons.imaging.Imaging;
import org.multipage.gui.Utility;

/**
 * @author
 *
 */
public class TranslatorUtilities {

	/**
	 * Current path names.
	 */
	public static String currentImagePathName = "";

	/**
	 * Load image from disk.
	 * @param parentComponent
	 * @return
	 */
	public static BufferedImage loadImageFromDisk(Component parentComponent) {
		
		// Select resource file.
		JFileChooser dialog = new JFileChooser(currentImagePathName);
		
		// List filters.
		String [][] filters = {{"org.multipage.translator.textPngFile", "png"}};
		
		// Add filters.
		Utility.addFileChooserFilters(dialog, currentImagePathName, filters, true);
						
		// Open dialog.
	    if(dialog.showOpenDialog(parentComponent) != JFileChooser.APPROVE_OPTION) {
	       return null;
	    }
	    
	    // Get selected file.
	    File file = dialog.getSelectedFile();
	    
	    // Set current path name.
	    currentImagePathName = file.getPath();
	    
	    BufferedImage image;
		try {
			image = Imaging.getBufferedImage(file);
		}
		catch (Exception e) {
			return null;
		}
	    
	    return image;
	}
}
