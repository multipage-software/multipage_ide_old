/*
 * Copyright 2010-2021 (C) vakol (see attached LICENSE file for additional info)
 * 
 * Created on : 26-03-2020
 *
 */
package com.maclan.server;

import org.multipage.util.TextOutputCapturer;

/**
 * @author user
 *
 */
public class JavaScriptBlockDescriptor extends BlockDescriptor {
	
	/**
	 * Script output capturer.
	 */
	public TextOutputCapturer scriptOutputCapturer;

	/**
	 * JavaScript block descriptor.
	 */
	public JavaScriptBlockDescriptor() {
		
		scriptOutputCapturer = new TextOutputCapturer();
	}
}
