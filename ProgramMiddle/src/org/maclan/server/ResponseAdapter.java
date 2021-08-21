/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.maclan.server;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

/**
 * 
 * @author
 *
 */
public class ResponseAdapter {
	
	/**
	 * Response reference.
	 */
	public Response response;

	/**
	 * Set content type.
	 * @param contentType
	 */
	public void setContentType(String contentType) {
		
		// Override this method.
	}

	/**
	 * Set content extension.
	 * @param extension
	 */
	public void setContentExtension(String extension) {
		
		// Override this method.
	}

	/**
	 * Set header.
	 * @param headerName
	 * @param headerContent
	 */
	public void setHeader(String headerName, String headerContent) {
		
		// Override this method.
	}

	/**
	 * Set character encoding.
	 * @param encoding
	 */
	public void setCharacterEncoding(String encoding) {
		
		// Override this method.
	}

	/**
	 * Get writer.
	 * @param outputStream
	 * @return
	 */
	public Writer getWriter(OutputStream outputStream) {
		
		// Override this method.
		return null;
	}

	/**
	 * Post process text.
	 * @param text
	 * @return
	 * @throws Exception 
	 */
	public String postProcessText(String text, RenderClass renderClass) throws Exception {
		
		// Override this method.
		return text;
	}

	/**
	 * Get output stream.
	 * @return
	 * @throws IOException 
	 */
	public OutputStream getOutputStream() {
		
		// Override this method.
		return null;
	}

	/**
	 * Set output not localized.
	 */
	public void setOutputNotLocalized() {
		
		// Override this method.
	}
}
