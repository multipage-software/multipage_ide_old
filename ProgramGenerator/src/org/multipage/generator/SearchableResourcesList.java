/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.Window;
import java.util.LinkedList;

import javax.swing.JList;

import org.maclan.MimeType;

/**
 * @author
 *
 */
public interface SearchableResourcesList {

	Window getWindow();

	LinkedList<MimeType> getMimeTypes();

	JList getList();

}
