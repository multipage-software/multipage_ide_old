/*
 * Copyright 2010-2024 (C) vakol
 * 
 * Created on : 16-05-2023
 *
 */
package org.maclan.server;

import java.util.function.Consumer;

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
	 * Current transaction ID.
	 */
	public int id = -1;
	
	/**
	 * Xdebug command that will be executed by the debugging client (the probe).
	 */
	public XdebugCommand command = null;
	
	/**
	 * Transaction state.
	 */
	public XdebugTransactionState state = XdebugTransactionState.created;
	
	/**
	 * Lambda function that can receive command result.
	 */
	public Consumer<XdebugClientResponse> responseLambda = null;

	/**
	 * Set exception thrown when sending this command to the Xdebug client.
	 */
	private Throwable writeException = null;
	
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
		transaction.id = command.transactionId = generateNewTransactionId();
		transaction.command = command;
		transaction.responseLambda = responseLambda;
		transaction.state = XdebugTransactionState.created;
		return transaction;
	}
	
	/**
	 * Check completed bytes.
	 * @param bytesWritten
	 */
	public void checkWrittenBytes(int bytesWritten) {
		
		if (bytesWritten != bytesToWrite) {
			String errorMessage = String.format(Resources.getString("org.maclan.server.messageXdebugBytesNotWritten"), bytesToWrite, bytesWritten);
			this.writeException = new Exception(errorMessage);
		}
		else {
			this.writeException = null;
		};
	}
	
	/**
	 * Set sending error.
	 * @param writeException
	 */
	public void setWriteException(Throwable writeException) {
		
		this.writeException = writeException;
	}
	
	/**
	 * Set number of bytes sent.
	 * @param bytesToWrite
	 */
	public void setBytesToWrite(int bytesToWrite) {
		
		this.bytesToWrite  = bytesToWrite;
	}
}
