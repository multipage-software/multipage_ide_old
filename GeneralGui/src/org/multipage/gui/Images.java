/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.gui;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.imageio.*;
import javax.swing.*;

/**
 * @author
 *
 */
public class Images {
	
	/**
	 * List of loaded icons.
	 */
	private static Hashtable<String, ImageIcon> icons = new Hashtable<String, ImageIcon>();
	
	/**
	 * List if loaded images.
	 */
	private static Hashtable<String, BufferedImage> images = new Hashtable<String, BufferedImage>();
	
	/**
	 * Get icon.
	 */
	public static ImageIcon getIcon(String urlString) {
		
		ImageIcon icon = icons.get(urlString);
		
		// If icon does't exist load it.
		if (icon == null) {
			URL url = ClassLoader.getSystemResource(urlString);
			if (url != null) {
				ImageIcon newIcon = new ImageIcon(url);
				if (newIcon != null) {
					icon = newIcon;
					icons.put(urlString, icon);
				}
			}
		}
		
		return icon;
	}
	
	/**
	 * Get image.
	 */
	public static BufferedImage getImage(String urlString) {
		
		BufferedImage image = images.get(urlString);
		
		// If image doesn't exist load it.
		if (image == null) {
			URL url = ClassLoader.getSystemResource(urlString);
			if (url != null) {
				try {
					image = ImageIO.read(url);
					images.put(urlString, image);
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return image;
	}

	/**
	 * Get cursor.
	 */
	public static Cursor loadCursor(String file, Point hotspot) {
		
		// Try to get an image.
		BufferedImage image = getImage(file);
		// Create cursor object.
		return Toolkit.getDefaultToolkit().createCustomCursor(image, hotspot, "img");
	}
}
