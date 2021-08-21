/*
 * Copyright 2010-2018 (C) vakol
 * 
 * Created on : 16-08-2018
 *
 */
package org.maclan.server;

import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Properties;

import org.maclan.server.XdebugListener.ChannelBreakDownException;

/**
 * Holds negatiated Xdebug features
 * @author user
 *
 */
public class XdebugFeatures {
	
	/**
	 * Exceptions
	 */
	private static final Exception makeInitialLoad = new Exception("First make initial features load");
	
	/**
	 * References to prerequisites
	 */
	private XdebugListener xdebugClient;
	private SocketChannel socketChannel;

	/**
	 * Features loaded
	 */
	private Properties features = new Properties();

	/**
	 * Loads Xdebug features
	 * @param xdebugClient 
	 * @param socketChannel
	 * @throws ChannelBreakDownException 
	 * @throws Exception 
	 */
	public void initialLoad(XdebugListener xdebugClient, SocketChannel socketChannel) throws ChannelBreakDownException {
		
		// Set prerequisites
		this.xdebugClient = xdebugClient;
		this.socketChannel = socketChannel;
		
		// Reset features
		features.clear();
		
		// Transact features
		xdebugClient.beginTransaction(XdebugListener.BREAK_ON_FIRST_EXCEPTION);
		
		loadStringFeature("language_supports_threads");
		loadStringFeature("language_name");
		loadStringFeature("language_version");
		loadStringFeature("encoding");
		loadStringFeature("protocol_version");
		loadStringFeature("supports_async");
		loadStringFeature("breakpoint_types");
		loadStringFeature("resolved_breakpoints");
		loadStringFeature("multiple_sessions");
		loadStringFeature("max_children");
		loadStringFeature("max_data");
		loadStringFeature("max_depth");
		loadStringFeature("extended_properties");
		loadStringFeature("supported_encodings");
		loadStringFeature("supports_postmortem");
		loadStringFeature("show_hidden");
		loadStringFeature("notify_ok");
		
		LinkedList<Exception> exceptions = xdebugClient.endTransaction();
	}
	
	/**
	 * Sets a new value of given Xdebug server feature
	 * @param feature
	 * @param newValue
	 * @throws Exception
	 */
	public void set(String feature, String newValue) throws Exception {
		
		// Check prerequisites
		if (xdebugClient == null || socketChannel == null) {
			throw makeInitialLoad;
		}
		
		// Check if feature exists
		if (!features.containsKey(feature)) {
			throw new Exception(String.format("Feature '%s' is not supported by Xdebug server", feature));
		}
		
		// Post a command to Xdebug server using new transaction
		xdebugClient.beginTransaction(XdebugListener.BREAK_ON_FIRST_EXCEPTION);
		
		String command = String.format("feature_set -n %s -v %s", feature, newValue);
		XdebugPacket packet = xdebugClient.command(socketChannel, command);
		String sValue = packet.getString("/response/@success");
		
		LinkedList<Exception> exceptions = xdebugClient.endTransaction();
		
		// On exceptions throw the first one
		if (!exceptions.isEmpty()) {
			throw exceptions.getFirst();
		}
		
		// On failure throw an exception
		if ("0".equals(sValue)) {
			throw new Exception(String.format("Setting feature '%s' with value '%s' failed", feature, newValue));
		}
		
		// Otherwise reload the feature using communication link to Xdebug
		loadStringFeature(feature);
		
		// Check new value
		if (!newValue.equals(features.getProperty(feature))) {
			throw new Exception(String.format("Feature '%s' cannot be negotiated with a value '%s'", feature, newValue));
		}
	}

	/**
	 * Load string feature
	 * @param feature
	 * @throws ChannelBreakDownException 
	 */
	private void loadStringFeature(String feature) throws ChannelBreakDownException {
		
		String command = String.format("feature_get -n %s", feature);
		XdebugPacket packet = xdebugClient.command(socketChannel, command);
		String sValue =  packet.getString("/response/text()");
		features.put(feature, sValue);
	}
}
