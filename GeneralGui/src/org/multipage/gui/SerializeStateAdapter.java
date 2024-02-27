/*
 * Copyright 2010-2017 (C) sechance
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
	protected void onReadState(StateInputStream inputStream)
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
	protected void onWriteState(StateOutputStream outputStream)
		throws IOException {

		// Override this method.
	}
}
