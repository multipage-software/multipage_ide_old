/**
 * 
 */
package com.maclan.server;

import java.io.*;
import java.util.Enumeration;

import javax.servlet.*;
import javax.servlet.http.*;

/**
 * @author user
 *
 */
public class CachedHttpServletRequest extends HttpServletRequestWrapper {
	
	/**
	 * Cached input stream reference.
	 */
	private CachedInputStream cachedInputStream;
	
	/**
	 * Thrown exception.
	 */
	private IOException exception;
	
	/**
	 * Constructor.
	 * @param request
	 */
	public CachedHttpServletRequest(HttpServletRequest request) {
		
		super (request);
		
		Enumeration<String> headers = request.getHeaderNames();
		String header = request.getHeader("Content-Length");
		header = request.getHeader("Content-Type");
		
		// Cache input stream.
		try {
			ServletInputStream servletInputStream = request.getInputStream();
			cachedInputStream = new CachedInputStream(servletInputStream);
		}
		catch (IOException e) {
			exception = e;
		}
	}
	
	/**
	 * cached input stream.
	 */
	@Override
	public ServletInputStream getInputStream() throws IOException {
		
		if (cachedInputStream == null) {
			throw exception;
		}
		return cachedInputStream;
	}
}
