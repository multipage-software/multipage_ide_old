/*
 * Copyright 2010-2018 (C) sechance
 * 
 * Created on : 05-04-2018
 *
 */
package com.maclan.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;

/**
 * @author user
 *
 */
public class CachedInputStream extends ServletInputStream {
	
	/**
	 * Cache with blocks of data from servlet input stream.
	 */
	private static final int blockLength = 1024;
	private LinkedList<byte []> cache = new LinkedList<byte []>();
	
	/**
	 * Number of bytes in cache.
	 */
	private int bytesInCache = 0;
	
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
		
		// Read bytes from input stream to blocks of data in the cache
		int bytesRead = 0;
		
		do {
			byte [] block = new byte [blockLength];
			
			bytesRead = inputStream.read(block);
			if (bytesRead > 0) {
				
				cache.add(block);
				bytesInCache += bytesRead;
			}
		}
		while (bytesRead > 0 && bytesRead < blockLength);
		
		// Invoke listener method.
		onDataAvailable();
	}

	/**
	 * Read data from blocks in the cache.
	 */
	@Override
	public int read() throws IOException {
				
		// If there is nothing to read, return negative value.
		if (position >= bytesInCache) {
			onAllDataRead();
			return -1;
		}
		
		// Get block number and position in block.
		int blockNumber = position / blockLength;
		int positionInBlock = position % blockLength;
		
		// Get block and a byte on given position.
		byte [] block = cache.get(blockNumber);
		byte singleByte = block[positionInBlock];
		
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
		
		return position >= bytesInCache;
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
}
