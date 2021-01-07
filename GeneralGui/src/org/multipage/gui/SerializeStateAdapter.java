/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.gui;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @author
 *
 */
public class SerializeStateAdapter {

	/**
	 * On read state.
	 */
	protected void onReadState(ObjectInputStream inputStream)
		throws IOException, ClassNotFoundException {
		
		// Override this method.
	}

	/**
	 * On set default state.
	 */
	protected void onSetDefaultState() {
		
		// Override this method.
	}

	/**
	 * On write state.
	 * @param saveStateOutputStream 
	 */
	protected void onWriteState(ObjectOutputStream outputStream)
		throws IOException {

		// Override this method.
	}
}
