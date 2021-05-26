/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Properties;

import org.multipage.gui.Images;
import org.multipage.gui.StatusBar;
import org.multipage.util.Resources;

/**
 * @author
 *
 */
public class MainStatusBar extends StatusBar {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Main text.
	 */
	private String mainText;
	
	/**
	 * Login properties.
	 */
	private Properties login;

	/**
	 * Connection flag.
	 */
	private boolean connection;

	/**
	 * Number of connections.
	 */
	private int numberConnections;

	/**
	 * Set login properties.
	 */
	public void setLoginProperties(Properties loginProperties) {
		
		login = loginProperties;
	}

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		
		if (login != null) {
			drawWithLogin(g);
		}
		else {
			drawSimple(g);
		}
	}

	/**
	 * Draw simple status bar.
	 * @param g
	 */
	private void drawSimple(Graphics g) {
		
		final int space = 5;
		int y = (height + fontSize) / 2;
		
		// Use font.
		g.setFont(font);
				
		// Draw main text.
		if (mainText != null) {
			g.drawString(mainText, space, y);
		}
	}

	/**
	 * Draw status bar with login information.
	 * @param g
	 */
	private void drawWithLogin(Graphics g) {
				
		BufferedImage connectedImage = Images.getImage("org/multipage/generator/images/connected.png");
		BufferedImage disconnectedImage = Images.getImage("org/multipage/generator/images/disconnected.png");
		BufferedImage securedImage = Images.getImage("org/multipage/generator/images/secure.png");
		BufferedImage notsecuredImage = Images.getImage("org/multipage/generator/images/not_secure.png");
		BufferedImage hostImage = Images.getImage("org/multipage/generator/images/host.png");
		BufferedImage databaseImage = Images.getImage("org/multipage/generator/images/database.png");
		BufferedImage userImage = Images.getImage("org/multipage/generator/images/user.png");
		
		final int space = 5;
		int y = (height + fontSize) / 2,
		    yimage = (height - securedImage.getHeight()) / 2,
		    x = getWidth() - space;
		
		// Use font.
		g.setFont(font);
		FontMetrics metrics = g.getFontMetrics();
		
		// Draw strings.
		String text;
		
		if (connection) {
			text = Resources.getString("org.multipage.generator.textConnected");
			x -= metrics.stringWidth(text);
			g.drawString(text, x, y);
			x -= connectedImage.getWidth() + space;
			g.drawImage(connectedImage, x, yimage, null);
			x -= space;
			g.drawLine(x, 0, x, height);
			x -= space;
			
			text = Resources.getString("org.multipage.generator.textNumberConnections");
			text = String.format(text, numberConnections);
			x -= metrics.stringWidth(text);
			g.drawString(text, x, y);
			//x -= space;
			//g.drawImage(connectedImage, x, yimage, null);
		}
		else {
			text = Resources.getString("org.multipage.generator.textDisconnected");
			x -= metrics.stringWidth(text);
			g.drawString(text, x, y);
			x -= disconnectedImage.getWidth() + space;
			g.drawImage(disconnectedImage, x, yimage, null);
		}
		x -= space;
		g.drawLine(x, 0, x, height);
		x -= space;
		if (login.getProperty("ssl") == "true") {
			text = Resources.getString("org.multipage.generator.textSecured");
			x -= metrics.stringWidth(text);
			g.drawString(text, x, y);
			x -= securedImage.getWidth() + space;
			g.drawImage(securedImage, x, yimage, null);
		}
		else {
			text = Resources.getString("org.multipage.generator.textNotSecured");
			x -= metrics.stringWidth(text);
			g.drawString(text, x, y);
			x -= notsecuredImage.getWidth() + space;
			g.drawImage(notsecuredImage, x, yimage, null);
		}
		x -= space;
		g.drawLine(x, 0, x, height);
		x -= space;
		text = login.getProperty("server") + ":" + login.getProperty("port");
		x -= metrics.stringWidth(text);
		g.drawString(text, x, y);
		x -= hostImage.getWidth() + space;
		g.drawImage(hostImage, x, yimage, null);
		x -= space;
		g.drawLine(x, 0, x, height);
		x -= space;
		
		text = login.getProperty("database");
		x -= metrics.stringWidth(text);
		g.drawString(text, x, y);
		x -= databaseImage.getWidth() + space;
		g.drawImage(databaseImage, x, yimage, null);
		x -= space;
		g.drawLine(x, 0, x, height);
		x -= space;
		
		text = login.getProperty("username");
		x -= metrics.stringWidth(text);
		g.drawString(text, x, y);
		x -= userImage.getWidth() + space;
		g.drawImage(userImage, x, yimage, null);
		x -= space;
		g.drawLine(x, 0, x, height);
		x -= space;
		
		// Draw main text.
		if (mainText != null) {
			g.setClip(space, 0, x, height);
			g.drawString(mainText, space, y);
		}
	}

	/**
	 * Set connection state
	 * @param connection
	 */
	public void setConnection(boolean connection) {

		this.connection = connection;
		repaint();
	}

	/**
	 * @param mainText the mainText to set
	 */
	public void setMainText(String mainText) {
		
		this.mainText = mainText;
		repaint();
	}

	/**
	 * Set number of connections.
	 * @param number
	 */
	public void setNumberConnections(int number) {
		
		this.numberConnections = number;
		repaint();
	}
}
