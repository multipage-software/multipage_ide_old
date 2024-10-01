/**
 * 
 */
package org.maclan.server;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequestWrapper;

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
	public CachedHttpServletRequest(javax.servlet.http.HttpServletRequest request) {
		
		super(request);
		
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
	public ServletInputStream getInputStream() throws IOException {
		
		if (cachedInputStream == null) {
			throw exception;
		}
		return cachedInputStream;
	}
}
