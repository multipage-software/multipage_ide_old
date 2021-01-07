/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package com.maclan.server;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.Map.*;

import javax.servlet.ServletInputStream;
import javax.servlet.http.*;

/**
 * 
 * @author
 *
 */
public class Request {
	
	/**
	 * Parameters.
	 */
	private Map<String, String[]> parameters;
	
	/**
	 * Original request object reference.
	 */
	private HttpServletRequest request;

	/**
	 * Constructor.
	 * @param request 
	 * @param userObject 
	 */
	public Request(HttpServletRequest request, Map<String, String[]> parameters) {
		
		this.request = request;
		this.parameters = parameters;
	}

	/**
	 * Get original request.
	 */
	public HttpServletRequest getOriginalRequest() {
		
		return request;
	}


	/**
	 * Get parameter.
	 * @param name
	 * @return
	 */
	public String getParameter(String name) {
		
		String [] value = parameters.get(name);
		if (value == null) {
			return null;
		}
		return value[0];
	}

	/**
	 * Returns true value is a parameter exists
	 * @param name
	 * @return
	 */
	public boolean existsParameter(String name) {
		
		return parameters.get(name) != null;
	}
	
	/**
	 * Trim the encoding string and return it.
	 * @param encoding
	 * @return
	 */
	private String getEncoding() {
		
		String encoding = request.getCharacterEncoding();
		
		// Set default encoding
		if (encoding == null) {
			encoding = "UTF8";
		}
		
		return encoding;
	}
	
	/**
	 * Read all POSTed data using character encoding of the request.
	 * @return
	 * @throws Exception 
	 */
	public String post() throws Exception {
		
		String text = "";
		byte [] data;
		
		// Get input stream and its character encoding
		String encoding = getEncoding();
		InputStream inputStream = request.getInputStream();
		
		// Get cached POST data
		if (inputStream instanceof CachedInputStream) {
			
			CachedInputStream cachedInputStream = (CachedInputStream) inputStream;
			
			// Get cached data
			data = cachedInputStream.getCachedData();
		}
		else {
			
			ServletInputStream servletInputStream = request.getInputStream();
			
			// Read all data and convert it to a string with specified encoding
			data = servletInputStream.readAllBytes();
			
		}
		
		// Convert the input data to string
		text = new String(data, encoding);
		return text;
	}

	/**
	 * Get parameters.
	 */
	public Map<String, String[]> getParameters() {
		
		return parameters;
	}

	/**
	 * Trace request.
	 * @param decorated 
	 * @return
	 */
	public String trace(boolean decorated) {
		
		String trace = "";
		
		for (Entry<String, String []> entry : parameters.entrySet()) {
			
			// Get values.
			String values = "[";
			boolean firstValue = true;
			
			for (String value : entry.getValue()) {
				
				if (firstValue) {
					values += value;
					firstValue = false;
				}
				else {
					values += ", " + value;
				}
			}
			
			values += "]";
			
			trace += entry.getKey() + " = " + values + (decorated ? "<br>" : "\n");
		}
		
		return trace;
	}

	/**
	 * Get server port.
	 * @return
	 */
	public int getServerPort() {
		
		return request.getServerPort();
	}
	
	/**
	 * Get server URL.
	 * @return
	 * @throws MalformedURLException 
	 */
	public String getServerUrl() throws MalformedURLException {
		
		String serverUrl = request.getRequestURL().toString();
		
		// Replace server name with localhost.
		URL url =  new URL(serverUrl);
		serverUrl = String.format("%s://127.0.0.1:%s", url.getProtocol(), url.getPort());

		return serverUrl;
	}
	
	/**
	 * Get server root path.
	 * @return
	 */
	public String getServerRootPath() {
		
		return getOriginalRequest().getSession().getServletContext().getRealPath("/");
	}
	
	/**
	 * Get server temporary path.
	 * @return
	 */
	public String getServerTempPath() {
		
		return getOriginalRequest().getSession().getServletContext().getAttribute("javax.servlet.context.tempdir").toString();
	}

	/**
	 * Returns true value if it is area server request
	 * @return
	 */
	public boolean isAreaServerRequest() {
		
		return     request.getParameter("res_id") != null
				|| request.getParameter("flag_id") != null
				|| request.getParameter("area_id") != null
				|| request.getParameter("alias") != null;
	}
	
	/**
	 * Get header
	 * @param headerName
	 * @return
	 */
	public String getHeader(String headerName) {
		
		String headerValue = request.getHeader(headerName);
		return headerValue;
	}
	
	/**
	 * Get action name
	 */
	public String getAction() {
		
		String actionValue = request.getHeader("AreaServer-Action");
		return actionValue;
	}
}
