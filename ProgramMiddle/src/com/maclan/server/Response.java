/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package com.maclan.server;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletResponse;

import org.multipage.util.Resources;

/**
 * 
 * @author
 *
 */
public class Response {
	
	/**
	 * Original response.
	 */
	private HttpServletResponse response;
	
	/**
	 * Response adapter.
	 */
	private ResponseAdapter responseAdapter;
	
	/**
	 * Output stream reference.
	 */
	private OutputStream outputStream;

	/**
	 * Area error flag.
	 */
	private String error = null;

	/**
	 * Rendered class.
	 */
	public RenderClass renderClass = null;

	/**
	 * PHP command exists flag.
	 */
	private boolean phpCommandExists = false;
	
	/**
	 * Area server reference.
	 */
	private AreaServer areaServer;
	
	/**
	 * Constructor.
	 * @param response 
	 * @param responseAdapter 
	 */
	public Response(HttpServletResponse response, ResponseAdapter responseAdapter) {
		
		this.response = response;
		
		responseAdapter.response = this;
		this.responseAdapter = responseAdapter;
	}
	
	/**
	 * Get original response.
	 */
	public HttpServletResponse getOriginalResponse() {
		
		return response;
	}

	/**
	 * Set content type.
	 * @param contentType
	 */
	public void setContentType(String contentType) {
		
		if (responseAdapter != null) {
			responseAdapter.setContentType(contentType);
		}
	}

	/**
	 * Set content extension.
	 * @param extension
	 */
	public void setContentExtension(String extension) {
		
		if (responseAdapter != null) {
			responseAdapter.setContentExtension(extension);
		}
	}
	
	/**
	 * Set header.
	 * @param headerName
	 * @param headerContent
	 */
	public void setHeader(String headerName, String headerContent) {
		
		if (responseAdapter != null) {
			responseAdapter.setHeader(headerName, headerContent);
		}
	}

	/**
	 * Set character encoding.
	 * @param encoding
	 */
	public void setCharacterEncoding(String encoding) {
		
		if (responseAdapter != null) {
			responseAdapter.setCharacterEncoding(encoding);
		}
	}

	/**
	 * Get output stream.
	 * @return
	 * @throws Exception 
	 */
	public OutputStream getOutputStream() {
		
		if (outputStream == null) {
		
			if (responseAdapter != null) {
				outputStream = responseAdapter.getOutputStream();
			}
		}
		
		return outputStream;
	}
	
	/**
	 * Write text to output stream specified by response object
	 * @param text
	 * @param encoding
	 */
	public void writeText(String text, String charset)
		throws Exception {
		
		OutputStream outputStream = getOutputStream();
		outputStream.write(text.getBytes(charset));
	}

	/**
	 * Close output stream.
	 */
	public void closeOutputStream() {
		
		if (outputStream != null) {
			
			try {
				outputStream.close();
			}
			catch (IOException e) {
			}
			
			outputStream = null;
		}
	}

	/**
	 * Get writer.
	 * @param outputStream
	 * @return
	 */
	public Writer getWriter(OutputStream outputStream) {
		
		Writer writer = null;
		
		if (responseAdapter != null) {
			
			// Try to get writer
			writer = responseAdapter.getWriter(outputStream);
			if (writer == null) {
				
				// Create new writer
				writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
			}
		}
		return writer;
	}
	
	/**
	 * Set error header using a string resource
	 * @param error
	 */
	public void setErrorHeader(String error) {
		
		this.error = Resources.getString(error);
	}
	
	/**
	 * Set error header.
	 * @param errorMessage
	 */
	public void setErrorHeader2(String errorMessage) {
		
		this.error = errorMessage;
	}

	/**
	 * Returns true if an error occurred
	 * @return
	 */
	public boolean isError() {
		return error != null;
	}

	/**
	 * Post process text.
	 * @param text
	 * @return
	 * @throws Exception 
	 */
	public String postProcessText(String text) throws Exception {
		
		if (responseAdapter != null) {
			return responseAdapter.postProcessText(text, renderClass);
		}
		return text;
	}

	/**
	 * Set render class.
	 * @param renderClassName
	 * @param renderClassText 
	 */
	public void setRenderClass(String renderClassName, String renderClassText) {
		
		this.renderClass = new RenderClass(renderClassName, renderClassText);
	}

	/**
	 * Set output not localized.
	 */
	public void setOutputNotLocalized() {
		
		if (responseAdapter != null) {
			responseAdapter.setOutputNotLocalized();
		}
	}

	/**
	 * Set PHP command found flag.
	 * @param exists
	 */
	public void setPhpCommandExists(boolean exists) {
		
		phpCommandExists = exists;
	}
	
	/**
	 * Get PHP command found flag.
	 * @param exists
	 */
	public boolean phpCommandExists() {
		
		return phpCommandExists;
	}
	
	/**
	 * Set area server reference.
	 */
	public void setAreaServer(AreaServer areaServer) {
		
		this.areaServer = areaServer;
	}
	
	/**
	 * Get area server reference.
	 */
	public AreaServer getAreaServer() {
		
		return areaServer;
	}

	/**
	 * Response to bad password
	 */
	public void badPassword() {
		
		String message = Resources.getString("com.maclan.server.messageBadPassword");
		
		try {
			setErrorHeader2(message);
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
		}
		catch (Exception e) {
		}
	}
	
	/**
	 * Response to bad operation
	 */
	public void badOperation() {
		
		String message = Resources.getString("com.maclan.server.messageBadOperation");
		
		try {
			setErrorHeader2(message);
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, message);
		}
		catch (Exception e) {
		}
	}
	
	/**
	 * Response to bad parameter
	 */
	public void badParameter() {
		
		String message = Resources.getString("com.maclan.server.messageBadParameter");
		
		try {
			setErrorHeader2(message);
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, message);
		}
		catch (Exception e) {
		}
	}
	
	/**
	 * Bad request
	 */
	public void badRequest() {
		
		String message = Resources.getString("com.maclan.server.messageBadRequest");
		
		try {
			setErrorHeader2(message);
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, message);
		}
		catch (Exception e) {
		}
	}
	
	/**
	 * Bad file
	 */
	public void badFile() {
		
		String message = Resources.getString("com.maclan.server.messageBadFile");
		
		try {
			setErrorHeader2(message);
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, message);
		}
		catch (Exception e) {
		}
	}
	
	/**
	 * Finalize headers
	 */
	public void finalizeHeaders() {
		
		if (this.error != null) {
			response.addHeader("AreaServer-Exception", this.error);
		}
	}
}
