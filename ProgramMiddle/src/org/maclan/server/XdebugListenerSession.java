/*
 * Copyright 2010-2023 (C) vakol
 * 
 * Created on : 03-05-2023
 *
 */
package org.maclan.server;

import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Hashtable;
import java.util.function.Consumer;

import org.maclan.server.XdebugPacket.XdebugClientParameters;
import org.multipage.util.Obj;

/**
 * Xdebug listener session object that stores session states.
 * @author vakol
 *
 */
public class XdebugListenerSession extends DebugListenerSession {
	
	/**
	 * List of debugged URIs.
	 */
	public String debuggedUri = null;
	
	/**
	 * Xdebug listener.
	 */
	public XdebugListener listener = null;
	
	/**
	 * Client parameters.
	 */
	public XdebugClientParameters clientParameters = null;
	
	/**
	 * Xdebug transactions. These commands are either waiting for sending via Xdebug protocol or waiting for
	 * response from debugging client (the debugging probe).
	 */
	private Hashtable<Integer, XdebugTransaction> transactions = new Hashtable<Integer, XdebugTransaction>();
	
	/**
	 * Cosntructor.
	 * @param server 
	 * @param client
	 * @throws Exception 
	 */
	public XdebugListenerSession(AsynchronousServerSocketChannel server, AsynchronousSocketChannel client)
			throws Exception {
		
		// Delegate the call.
		super(server, client);
	}
	
	/**
	 * initialize session using input packet.
	 * @param inputPacket
	 * @throws Exception 
	 */
	public void initialize(XdebugPacket inputPacket) 
			throws Exception {
		
		debuggedUri = inputPacket.GetDebuggedUri();
		clientParameters = XdebugPacket.parseDebuggedUri(debuggedUri);
	}
	
	/**
	 * Get process ID.
	 * @return
	 */
	@Override
	public String getPid() {
		
		if (clientParameters == null) {
			return "";
		}
		return clientParameters.pid;
	}
	
	/**
	 * Send Xdebug command.
	 * @param commandName
	 * @param arguments
	 * @param responseLambda
	 * @return
	 */
	public int prepareCommand(String commandName, String [][] arguments, Consumer<XdebugPacket> responseLambda) {
		

		// Create new Xdebug command.
		XdebugCommand command = XdebugCommand.create(commandName, arguments);
		
		// Prepare new transaction in the current session.
		XdebugTransaction newTransaction = XdebugTransaction.create(command, responseLambda);
		int transactionId = newTransaction.id;
		transactions.put(transactionId, newTransaction);
		return transactionId;
	}

	/**
	 * Load specific features from the client and save  them in session state.
	 * @param featureNames
	 */
	public void loadFeaturesFromClient(String ... featureNames) {
		
		// Prepare commands that will be sent to the debug client.
		for (String featureName : featureNames) {
			prepareCommand(XdebugCommand.FEATURE_GET, new String [][] {{"-n", featureName}}, responsePacket -> {
				
			});
		}
	}
	
	/**
	 * Send IDE features to the debugging client (the debugging probe).
	 * @param ideFeatureValues
	 */
	public void sendIdeFeaturesToClient(String[][] ideFeatureValues) {
		// TODO Auto-generated method stub
		
	}
}
