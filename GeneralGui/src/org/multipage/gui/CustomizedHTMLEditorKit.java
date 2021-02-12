/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.gui;

import java.io.*;

import javax.swing.text.*;
import javax.swing.text.html.*;

/**
 * @author
 *
 */
public class CustomizedHTMLEditorKit extends HTMLEditorKit {
	
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1;

	/**
	 * Write method.
	 */
	@Override
	public void write(Writer out, Document doc, int pos, int len)
			throws IOException, BadLocationException {
		
		if (!(doc instanceof HTMLDocument)) {
			return;
		}
		HTMLDocument document = (HTMLDocument) doc;
		
		// Write document content.
        HTMLWriter w = new HTMLWriter(out, document, pos, len);
        w.write();
	}
}
