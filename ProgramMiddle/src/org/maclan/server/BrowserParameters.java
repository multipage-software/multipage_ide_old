/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.maclan.server;

import java.io.File;
import java.io.Serializable;

import org.multipage.util.Resources;

/**
 * @author
 *
 */
public class BrowserParameters implements Serializable {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Folder name.
	 */
	private String folderName;
	
	/**
	 * Home page file name.
	 */
	private String homePage;
	
	/**
	 * Browser title.
	 */
	private String title;

	/**
	 * Browser message.
	 */
	private String message;

	/**
	 * Browser window width.
	 */
	private int width;

	/**
	 * Browser window height.
	 */
	private int height;

	/**
	 * Window maximized flag.
	 */
	private boolean maximized;
	
	/**
	 * Create autorun flag.
	 */
	private boolean createAutorun;
	
	/**
	 * Browser program name.
	 */
	private String browserProgramName;

	/**
	 * Constructor.
	 */
	public BrowserParameters() {
		
		setDefault();
	}
	
	/**
	 * Set default values.
	 */
	public void setDefault() {
		
		folderName = "Pages";
		homePage = "index.htm";
		title = Resources.getString("server.textBrowserTitle");
		message = Resources.getString("server.textBrowserMessage");
		setBrowserProgramName("Browser");
		width = 800;
		height = 600;
		maximized = true;
		setCreateAutorun(true);
	}

	/**
	 * Get folder name.
	 * @return
	 */
	public String getFolder() {
		
		return folderName;
	}

	/**
	 * Get home page file name.
	 * @return
	 */
	public String getHomePage() {
		
		return homePage;
	}

	/**
	 * Get browser title.
	 * @return
	 */
	public String getTitle() {
		
		return title;
	}

	/**
	 * Get browser message.
	 * @return
	 */
	public String getMessage() {
		
		return message;
	}

	/**
	 * Get browser window width.
	 * @return
	 */
	public String getWidthText() {
		
		return String.valueOf(width);
	}

	/**
	 * Get browser window height.
	 * @return
	 */
	public String getHeightText() {
		
		return String.valueOf(height);
	}

	/**
	 * Gets true value if the browser window should be maximized.
	 * @return
	 */
	public boolean isMaximized() {
		
		return maximized;
	}

	/**
	 * Set pages folder name.
	 * @param folderName
	 */
	public void setFolder(String folderName) {
		
		this.folderName = folderName;
	}

	/**
	 * Set home page file name.
	 * @param homePage
	 */
	public void setHomePage(String homePage) {
		
		this.homePage = homePage;
	}

	/**
	 * Set browser title.
	 * @param title
	 */
	public void setTitle(String title) {
		
		this.title = title;
	}

	/**
	 * Set browser message.
	 * @param message
	 */
	public void setMessage(String message) {
		
		this.message = message;
	}

	/**
	 * Set browser window width.
	 * @param width
	 */
	public void setWidth(int width) {
		
		this.width = width;
	}

	/**
	 * Set browser window height.
	 * @param height
	 */
	public void setHeight(int height) {
		
		this.height = height;
	}

	/**
	 * Set window maximized flag.
	 * @param maximized
	 */
	public void setMaximized(boolean maximized) {
		
		this.maximized = maximized;
	}

	/**
	 * Get relative URL.
	 * @return
	 */
	public String getRelativeUrl() {
		
		return folderName + File.separatorChar + homePage;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "BrowserParameters [folderName=" + folderName + ", homePage="
				+ homePage + ", title=" + title + ", message=" + message
				+ ", width=" + width + ", height=" + height + ", maximized="
				+ maximized + "]";
	}

	/**
	 * Get window size text.
	 * @return
	 */
	public String getWindowSizeText() {
		
		return getWidthText() + ", " + getHeightText();
	}

	/**
	 * Get window maximized value text.
	 * @return
	 */
	public String getMaximizedText() {
		
		return maximized ? "True" : "False";
	}

	/**
	 * @return the createAutorun
	 */
	public boolean isCreateAutorun() {
		return createAutorun;
	}

	/**
	 * @param createAutorun the createAutorun to set
	 */
	public void setCreateAutorun(boolean createAutorun) {
		this.createAutorun = createAutorun;
	}

	/**
	 * @return the browserProgramName
	 */
	public String getBrowserProgramName() {
		return browserProgramName;
	}

	/**
	 * @param browserProgramName the browserProgramName to set
	 */
	public void setBrowserProgramName(String browserProgramName) {
		this.browserProgramName = browserProgramName;
	}
}
