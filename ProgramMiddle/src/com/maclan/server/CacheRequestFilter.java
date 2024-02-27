/*
 * Copyright 2010-2018 (C) sechance
 * 
 * Created on : 05-04-2018
 *
 */
package com.maclan.server;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

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
		
		// Cache request when input stream needed.
		ServletRequest cachedRequest = new CachedHttpServletRequest((HttpServletRequest) request);
		
		// Chain filters.
		chain.doFilter(cachedRequest, response);
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
