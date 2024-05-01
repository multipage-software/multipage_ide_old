/*
 * Copyright 2010-2023 (C) vakol
 * 
 * Created on : 16-05-2023
 *
 */
package org.maclan.server;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Base64;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.multipage.gui.Utility;
import org.multipage.util.j;

/**
 * Xdebug commands that sends the IDE.
 * @author vakol
 *
 */
public class XdebugCommand {
	
	/**
	 * Xdebug command names.
	 */
	public static final String FEATURE_GET = "feature_get";
	public static final String FEATURE_SET = "feature_set";
	
	/**
     * Xdebug NULL symbol.
     */
	public static byte [] NULL_SYMBOL = new byte [] { 0 };
	
	/**
	 * Xdebug constants.
	 */
	private static final int COMMAND_BUFFER_INITIAL_CAPACITY = 128;
	private static final int COMMAND_BUFFER_INITIAL_CAPACITY_WITH_DATA = 1024;
	
	/**
	 * Regular expression for Xdebug commands.
	 */
	private static Pattern regexCommand;
	
	/**
	 * Static constructor.
	 */
	static {
		regexCommand = Pattern.compile("^(?<name>[^\\s]+)+\\s+(?<param>.+?)(\\s+--(?<data>.+))?$");
	}

	/**
	 * Command name.
	 */
	public String name = null;
	
	/**
	 * Arguments for the above command.
	 */
	public String[][] arguments = null;
	
	/**
	 * Data attached to the command.
	 */
	public byte [] data = null;
	
	/**
	 * Unique transaction ID.
	 */
	public int transactionId = -1;
	
	/**
	 * Create new Xdebug command.
	 * @param commandName
	 * @param arguments
	 * @return
	 */
	public static XdebugCommand create(String commandName, String[][] arguments, byte [] data) {
		
		XdebugCommand command = new XdebugCommand();
		command.name = commandName;
		command.arguments = arguments;
		command.data = data;
		command.transactionId = -1;
		return command;
	}
	
	/**
	 * Compiles Xdebug command string that will be sent to the Xdebug client.
	 * @param transactionId
	 * @return
	 */
	public CharBuffer compile(int transactionId) {
		
		// Initialization.
		boolean hasData = data != null;
		boolean hasArguments = arguments != null && arguments.length > 0;
		CharBuffer commandChars = CharBuffer.allocate(hasData ? COMMAND_BUFFER_INITIAL_CAPACITY_WITH_DATA : COMMAND_BUFFER_INITIAL_CAPACITY);
		
		// Insert command name.
		commandChars.append(name);
		
		// Insert transaction ID.
		commandChars.append(" -i " + String.valueOf(transactionId));
		
		// Compile arguments string.
		if (hasArguments) {
			for (String [] argument : arguments) {
				commandChars.append(' ' + argument[0] + ' ' + argument[1]);
			}
		}
		
		// Optionaly insert Base64 encoded command data.
		if (hasData) {
			 String base64Data = Base64.getEncoder().encodeToString(data);
			 commandChars.append(" --" + base64Data);
		}
		
		// Append trailing NULL characters and rewind the buffer to the beginning..
		commandChars.append('\0');
		commandChars.flip();
		return commandChars;
	}
	
	/**
	 * Parses Xdebug command bytes and creates new Xdebug command.
	 * @param commandBytes
	 * @return
	 * @throws Exception 
	 */
	public static XdebugCommand parseCommand(ByteBuffer commandBytes) 
			throws Exception {
		
		// Decode command bytes using UTF-8 encoding.
		commandBytes.flip();
		
		int limit = commandBytes.limit();
		byte [] bytes = new byte[limit];
		
		commandBytes.get(bytes);
		String statementText = new String(bytes, "UTF-8");
		
		// Reset the command buffer to reuse it next time.
		commandBytes.clear();
		
		// Delegate the function.
		XdebugCommand command = parseCommand(statementText);
		return command;
	}
	
	/**
	 * Parses Xdebug statement text and creates new Xdebug command.
	 * @param statementText
	 * @return
	 * @throws Exception 
	 */
	public static XdebugCommand parseCommand(String statementText)
			throws Exception {
		
        // Use regex to parse the command name and arguments.
		Matcher matcher = regexCommand.matcher(statementText);
		
        boolean success = matcher.find();
        int groupCount = matcher.groupCount();
        
        if (success && groupCount >= 4) {
        	
        	// Create new command.
        	XdebugCommand command = new XdebugCommand();
            
            // Get command name.
            command.name = matcher.group("name").trim();
            
            // Get command arguments.
            LinkedList<String[]> argumentList = new LinkedList<String[]>();
            String paramString = matcher.group("param");
           	String [] splittedParams = paramString.split("\\s+");
           	
           	int count = splittedParams.length;
           	for (int index = 0; index < count; index++) {
           		
           		String [] parameter = new String[2];
           		parameter[0] = splittedParams[index++].trim();
           		if (index < count) {
           			
           			// Set transaction ID.
           			if ("-i".equals(parameter[0])) {
           				command.transactionId = Integer.parseInt(splittedParams[index].trim());
           			}
           			else {
	           			parameter[1] = splittedParams[index].trim();
	           			argumentList.add(parameter);
           			}
           		}
           	}
           	// Check if the transaction ID has been set.
           	if (command.transactionId == -1) {
               Utility.throwException("org.maclan.server.messageCannotParseXdebugCommandTransactionId", statementText);
           	}
           	command.arguments = argumentList.toArray(new String[argumentList.size()][]);
           	
           	// Get command data.
            String data = matcher.group("data");
            if ( data != null) {
            	command.data = Base64.getDecoder().decode(data);
            }
            
            // Return command.
            return command;
        }
		
        // Throw exception.
        Utility.throwException("org.maclan.server.messageCannotParseXdebugCommand", statementText);
		return null;
	}
	
	/**
	 * Returns the command argument by its key name.
	 * @param argumentKey
	 * @return
	 */
	public String getArgument(String argumentKey) {
		
		// Find argument by key name.
		for (String [] argument : arguments) {
			
			if (argument[0].equals(argumentKey)) {
                return argument[1];
            }
		}
		return null;
	}

	/**
	 * Get text representation of the command.
	 * @return
	 */
	public String getText() {
		
		CharBuffer characters = compile(-1);
		String text = characters.toString();
		return text;
	}
}
