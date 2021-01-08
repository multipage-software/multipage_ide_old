/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.Window;

import javax.swing.JDialog;

/**
 * @author
 *
 */
public class AboutDialogBase extends JDialog {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * @param owner
	 * @param applicationModal
	 */
	public AboutDialogBase(Window owner, ModalityType applicationModal) {
		super(owner, applicationModal);
	}

	/**
	 * Delegate call.
	 */
	public void setVisible(boolean visible) {
		
		super.setVisible(visible);
	}
}
