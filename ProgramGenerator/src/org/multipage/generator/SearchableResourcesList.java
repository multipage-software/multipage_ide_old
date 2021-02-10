/*
 * Copyright 2010-2021 (C) vakol (see attached LICENSE file for additional info)
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.*;
import java.util.LinkedList;

import javax.swing.JList;

import com.maclan.MimeType;

/**
 * @author
 *
 */
public interface SearchableResourcesList {

	Window getWindow();

	LinkedList<MimeType> getMimeTypes();

	JList getList();

}
