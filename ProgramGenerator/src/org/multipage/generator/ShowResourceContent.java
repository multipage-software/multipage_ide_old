/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.image.BufferedImage;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import org.multipage.basic.ProgramBasic;
import org.multipage.gui.Utility;
import org.multipage.util.Obj;
import org.multipage.util.Resources;

import com.maclan.Middle;
import com.maclan.MiddleResult;
import com.maclan.Resource;

/**
 * @author
 *
 */
public class ShowResourceContent {
	
	/**
	 * Show dialog.
	 * @param parent
	 * @param resource
	 */
	public static void showDialog(Component parent, Resource resource) {
		
		Properties login = ProgramBasic.getLoginProperties();
		Middle middle = ProgramBasic.getMiddle();
		MiddleResult result;
		
		// Try to load resource image.
		Obj<BufferedImage> image = new Obj<BufferedImage>();
		
		result = middle.loadResourceFullImage(login, resource.getId(), image);
		if (result.isNotOK()) {
			result.show(parent);
			return;
		}
		
		// If it is not an image, inform user.
		if (image.ref == null) {
			Utility.show(parent, "org.multipage.generator.messageResourceContentIsNotImageOrText");
			return;
		}
		
		showDialog(parent, resource, image.ref);
	}
	
	/**
	 * Show dialog.
	 * @param parent
	 * @param resource
	 * @param image
	 */
	public static void showDialog(Component parent, Resource resource, BufferedImage image) {
		
		JDialog dialog = new JDialog(Utility.findWindow(parent), ModalityType.DOCUMENT_MODAL);
		dialog.setTitle(Resources.getString("org.multipage.generator.textImage") + ": " + resource.getDescription());
		
		// Get frame width.
		int width = image.getWidth();
		if (width < 300) {
			width = 300;
		}
		if (width > 800) {
			width = 800;
		}
		width += 50;

		
		int height = image.getHeight();
		if (height < 100) {
			height = 100;
		}
		if (height > 600) {
			height = 600;
		}
		height += 80;
		
		dialog.setSize(width, height);
		
		Utility.centerOnScreen(dialog);

		JLabel label = new JLabel("", new ImageIcon(image), JLabel.CENTER);
		
		JScrollPane scroll = new JScrollPane();
		scroll.setViewportView(label);
		
		dialog.getContentPane().add(scroll);
		
		dialog.setVisible(true);
	}

}
