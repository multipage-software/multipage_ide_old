/*
 * Copyright 2010-2023 (C) vakol
 * 
 * Created on : 16-05-2023
 *
 */
package org.maclan.server;

import java.util.function.Consumer;

import org.multipage.util.Obj;

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
	 * Lambda function that can receive command result.
	 */
	public Consumer<XdebugPacket> responseLambda = null;
	
	/**
	 * Create new transaction for the command.
	 * @param command
	 * @param responseLambda 
	 * @return
	 */
	public static XdebugTransaction create(XdebugCommand command, Consumer<XdebugPacket> responseLambda) {
		
		XdebugTransaction transaction = new XdebugTransaction();
		transaction.id = generateNewTransactionId();
		transaction.command = command;
		transaction.responseLambda = responseLambda;
		return transaction;
	}

}
