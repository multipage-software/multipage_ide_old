/*
 * Copyright 2010-2018 (C) vakol
 * 
 * Created on : 05-04-2018
 *
 */
package com.maclan.server;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * @author user
 *
 */
public class CacheRequestFilter implements Filter {

	/**
	 * Does request filter.
	 */
	@Override
	public void doFilter(final ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
			
		// Chain filters for Jetty and Apache
		if (request instanceof org.eclipse.jetty.server.Request) {
			
			// Cache request when input stream needed.
			CachedHttpServletRequest cachedRequest = new CachedHttpServletRequest((org.eclipse.jetty.server.Request) request);
			chain.doFilter(cachedRequest, response);
		}
		else {
			// Do not filter the request
			chain.doFilter(request, response);
		}
	}
	
	/**
	 * Initialize filter.
	 */
	@Override
	public void init(FilterConfig filterConfiguration) throws ServletException {
		
		// Nothing to do.
	}

	/**
	 * Destroy filter.
	 */
	@Override
	public void destroy() {
		
		// Nothing to do.
	}
}
