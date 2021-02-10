/*
 * Copyright 2010-2021 (C) vakol (see attached LICENSE file for additional info)
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.gui;

import javax.swing.*;

import org.multipage.util.Resources;

/**
 * @author
 *
 */
public class ToolBarKit {

	/**
	 * Adds StatusBar button
	 */
	public static JButton addToolBarButton(JToolBar toolBarObject,
			String iconPictureName, Object notifyObject,
			String methodToInvoke, String toolTipResoure) {
	
		// Add StatusBar Button and set action adapter.
		JButton toolBarButton;
		toolBarButton = toolBarObject.add(new ActionAdapter(notifyObject, methodToInvoke, (Class<?>[])null));
		toolBarButton.setToolTipText(Resources.getString(toolTipResoure));
		
		ImageIcon icon = Images.getIcon(iconPictureName);
		if (icon != null) {
			toolBarButton.setIcon(icon);
		}
		else {
			String format = Resources.getString("org.multipage.gui.errorCannotLoadToolbarIcon");
			String message = String.format(format, iconPictureName);
			JOptionPane.showMessageDialog(null, message);
		}
		
		return toolBarButton;
	}

	/**
	 * Add toggle button.
	 */
	public static JToggleButton addToggleButton(JToolBar toolBarObject,
			String iconPictureName, Object notifyObject,
			String methodToInvoke, String toolTipResoure) {
		
		// Add toggle button and set action adapter.
		JToggleButton toggleButton = new JToggleButton();
		toggleButton.setAction(new ActionAdapter(notifyObject, methodToInvoke, (Class<?>[])null));
		toolBarObject.add(toggleButton);
		toggleButton.setToolTipText(Resources.getString(toolTipResoure));

		ImageIcon icon = Images.getIcon(iconPictureName);
		if (icon != null) {
			toggleButton.setIcon(icon);
		}
		else {
			String format = Resources.getString("org.multipage.gui.errorCannotLoadToolbarIcon");
			String message = String.format(format, iconPictureName);
			JOptionPane.showMessageDialog(null, message);
		}
		
		return toggleButton;
	}
}
