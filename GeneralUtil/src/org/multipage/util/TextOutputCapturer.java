/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.util;

/**
 * @author
 *
 */
public class TextOutputCapturer {
	
	/**
	 * Fields.
	 */
    private StringBuilder stringBuilder;
    
    /**
     * Constructor.
     */
	public TextOutputCapturer() {
		
		stringBuilder = new StringBuilder();
    }
	
	/**
	 * Stop.
	 * @return
	 */
    public String stop() {
        
    	String capturedValue = stringBuilder.toString();
    	stringBuilder.setLength(0);
        return capturedValue;
    }
    
    /**
     * Print text.
     * @param text
     */
	public void print(String text) {
		
		stringBuilder.append(text);
	}
	
	/**
	 * Print text line.
	 * @param text
	 */
	public void println(String text) {
		
		stringBuilder.append(text);
		stringBuilder.append('\n');
	}
}
