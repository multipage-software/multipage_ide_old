/*
 * Copyright 2010-2024 (C) vakol
 * 
 * Created on : 16-05-2023
 *
 */
package org.maclan.server;

import java.util.function.Consumer;

import org.multipage.util.Lock;
import org.multipage.util.Obj;
import org.multipage.util.Resources;

/**
 * Xdebug transaction object. The transaction sends a command and waits for response.
 * @author vacla
 *
 */
public class XdebugTransaction {

	/**
	 * Generated transaction ID.
	 */
	private static Obj<Integer> generatedTransactionId = new Obj<Integer>(0);
	
	/**
	 * Current transaction ID.
	 */
	private int id = -1;
	
	/**
	 * Xdebug command that will be executed by the debugging client (the probe).
	 */
	private XdebugCommand command = null;
	
	/**
	 * Transaction state.
	 */
	private XdebugTransactionState state = XdebugTransactionState.created;
	
	/**
	 * Lambda function that receives command responses.
	 */
	private Consumer<XdebugClientResponse> responseLambda = null;
	
	/**
	 * A lock that can wait for response completion.
	 */
	public Lock responseLock = null;
	
	/**
	 * Lock timout in milliseconds.
	 */
	public int responseLockTimeoutMs = 0;
	
	/**
     * Number of bytes to send.
     */
	private int bytesToWrite = 0;
	
	/**
	 * Create new transaction for the command.
	 * @param command
	 * @param responseLambda 
	 * @return
	 */
	public static XdebugTransaction create(XdebugCommand command, Consumer<XdebugClientResponse> responseLambda) {
		
		XdebugTransaction transaction = new XdebugTransaction();
		int transactionId = generateNewTransactionId();
		command.setTransactionId(transactionId);
		transaction.id = transactionId;
		transaction.command = command;
		transaction.responseLambda = responseLambda;
		transaction.state = XdebugTransactionState.created;
		return transaction;
	}
	
	/**
	 * Generate new transaction ID.
	 */
	public static int generateNewTransactionId() {
		synchronized (generatedTransactionId) {
			
			if (generatedTransactionId.ref < Integer.MAX_VALUE) {
				generatedTransactionId.ref++;
			}
			else {
				generatedTransactionId.ref = 1;
			}
			return generatedTransactionId.ref;
		}
	}
	
	/**
	 * Get transaction ID.
	 * @return
	 */
	public int getId() {
		
		return id;
	}
	
	/**
	 * Get Xdebugu command.
	 * @return
	 */
	public XdebugCommand getCommand() {
		
		return command;
	}
	
	/**
	 * Set transaction state.
	 * @param state
	 */
	public void setState(XdebugTransactionState state) {
		// 
		this.state = state;
	}
	
	/**
	 * Get transaction state.
	 * @return
	 */
	public XdebugTransactionState getState() {
		
		return state;
	}
	
	/**
	 * Get response lambda.
	 * @return
	 */
	public Consumer<XdebugClientResponse> getResponseLambda() {
		
		return responseLambda;
	}
	
	/**
	 * Check completed bytes.
	 * @param bytesWritten
	 */
	public void checkWrittenBytes(int bytesWritten) {
		
		if (bytesWritten != bytesToWrite) {
			String errorMessage = String.format(Resources.getString("org.maclan.server.messageXdebugBytesNotWritten"), bytesToWrite, bytesWritten);
			Exception exception = new Exception(errorMessage);
			onThrownException(exception);
		}
	}
	
	/**
	 * Set number of bytes sent.
	 * @param bytesToWrite
	 */
	public void setBytesToWrite(int bytesToWrite) {
		
		this.bytesToWrite  = bytesToWrite;
	}
	
	/**
	 * On exception.
	 * @param e
	 */
	protected void onThrownException(Throwable e) {
		
		// Override this method.
		e.printStackTrace();
	}
}
