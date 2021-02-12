/*
 * Copyright 2010-2018 (C) vakol
 * 
 * Created on : 05-04-2018
 *
 */
package com.maclan.server;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;

/**
 * @author user
 *
 */
public class CachedInputStream extends ServletInputStream {
	
	/**
	 * Cache with the data from the servlet input stream.
	 */
	private byte [] cache = new byte [] {};
	
	/**
	 * Position in cache.
	 */
	private int position = 0;
	
	/**
	 * Listener to invoke.
	 */
	private ReadListener listener;
	
	/**
	 * Constructor.
	 * @param stream
	 */
	public CachedInputStream(InputStream inputStream) throws IOException {
		
		// Get posted data
		cache = inputStream.readAllBytes();
		
		// Invoke listener method.
		onDataAvailable();
	}

	/**
	 * Read data from blocks in the cache.
	 */
	@Override
	public int read() throws IOException {
		
		// If there is nothing to read, return negative value.
		if (position >= cache.length) {
			onAllDataRead();
			return -1;
		}
		
		// Get single byte from the cache.
		byte singleByte = cache[position];
		
		// Increment position.
		position++;
		
		return singleByte;
	}
	
	/**
	 * Invoked when all data for the current request has been read.
	 * @throws IOException 
	 */
	private void onAllDataRead() throws IOException {
		
		if (listener != null) {
			listener.onAllDataRead();
		}
	}
	
	/**
	 * Invoked the first time when it is possible to read data.
	 * @throws IOException 
	 */
	private void onDataAvailable() throws IOException {
		
		if (listener != null) {
			listener.onDataAvailable();
		}
	}

	/**
	 * Returns true when all the data from the stream has been read else it returns false.
	 */
	@Override
	public boolean isFinished() {
		
		return position >= cache.length;
	}
	
	/**
	 * Data can be read without blocking, so this method returns true value.
	 */
	@Override
	public boolean isReady() {
		
		return true;
	}

	/**
	 * Instructs the ServletInputStream to invoke the provided ReadListener when it is possible to read.
	 */
	@Override
	public void setReadListener(ReadListener listener) {
		
		this.listener = listener;
	}

	/**
	 * Sets position to the beginning of the cache.
	 */
	public synchronized void rewind() throws IOException {
		
		position = 0;
	}
	
	/**
	 * Get cached data
	 * @param encoding
	 * @return
	 * @throws Exception 
	 */
	public byte [] getCachedData() throws Exception {
		
		// Get cache data.
		return cache;
	}
}
