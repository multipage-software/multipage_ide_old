/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.maclan.server;

import java.util.*;
import java.util.Map.Entry;

import org.maclan.MiddleUtility;
import org.multipage.util.Resources;

/**
 * @author
 *
 */
public class BlockDescriptor {
	
	/**
	 * Block identifier or class name.
	 */
	public String name = null;
	
	/**
	 * Areas areasVariables list.
	 */
	public HashMap<String, Variable> variables = new HashMap<String, Variable>();
	
	/**
	 * Procedures set.
	 */
	public HashMap<String, Procedure> procedures = new HashMap<String, Procedure>();
	
	/**
	 * Create block variable.
	 * @param name
	 * @param value
	 */
	public Variable createBlockVariable(String name, Object value)
		throws Exception {

		// Split name.
		String [] nameParts = name.split("\\.");
		// Check name.
		if (nameParts.length < 1) {
			throw new Exception(String.format(
					Resources.getString("server.messageCannotRecognizeVariableName"),
					name));
		}
		// Get variable name.
		String variableName = nameParts[0];
		
		// If the variable already exists, throw exception.
		if (variables.containsKey(variableName)) {
			throw new Exception(String.format(
					Resources.getString("server.messageVariableAlreadyExists"),
					name));
		}

		// Create new variable and add it to the hash map.
		Variable newVariable = new Variable(variableName, null);

		variables.put(variableName, newVariable);
		
		// Create possible sub objects.
		Variable currentVariable = newVariable;
		for (int index = 1; index < nameParts.length; index++) {
			
			String partName = nameParts[index];
			
			// Create current object sub object.
			Variable subVariable = new Variable(partName, null);
			
			HashMap<String, Variable> subVariables = new HashMap<String, Variable>();
			subVariables.put(partName, subVariable);
			
			currentVariable.value = subVariables;
			currentVariable = subVariable;
		}
		
		// Set value.
		currentVariable.value = value;
		
		return newVariable;
	}

	/**
	 * Get block variable.
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public Variable getBlockVariable(String name) {
		
		return variables.get(name);
	}
	
	/**
	 * On block leave.
	 */
	public void afterBlockRemoved() {
		
		// Override this method.
	}

	/**
	 * Trace block.
	 * @param decorated
	 * @return
	 */
	public String trace(boolean decorated) {

		// Trace procedures.
		String [][] proceduresDescriptor = new String[procedures.size()][2];
		
		int index = 0;
		
		for (Entry<String, Procedure> entry : procedures.entrySet()) {
			
			proceduresDescriptor[index][0] = entry.getKey() + ":";
			proceduresDescriptor[index][1] = entry.getValue().trace(decorated);

			index++;
		}
		
		final String newLine = decorated ? "<br>" : "\n";
		String proceduresText = Resources.getString("server.textProceduresTrace");
		
		String trace = (decorated ? "<u>" + proceduresText + "</u>" : proceduresText) + ": " + newLine
				+ MiddleUtility.createTraceTable(proceduresDescriptor, decorated) + (decorated ? "" : "\n");
		
		// Trace variables.
		String [][] variablesDescriptor = new String [variables.size()][2];
		
		index = 0;
		
		for (Entry<String, Variable> entry : variables.entrySet()) {
			
			String name = entry.getKey();
			Object value = entry.getValue().value;
			
			variablesDescriptor[index][0] = name + ":";
			
			if (value == null) {
				variablesDescriptor[index][1] = null;
			}
			else {
				String valueTextSimple = MiddleUtility.trimTextWithTags(value.toString(), decorated);
				String valueText = null;
				
				if (decorated) {
					valueText = "<style> .TraceVariableValue { background-color: lightgreen; color: black; } </style>";
					valueText += String.format("<span class='TraceVariableValue'>%s</span>", valueTextSimple);
				}
				else {
					valueText = valueTextSimple;
				}
				
				variablesDescriptor[index][1] = valueText;
			}
			
			index++;
		}
		
		String variablesText = Resources.getString("server.textVariablesTrace");
		
		trace += (decorated ? "<u>" + variablesText + "</u>" : variablesText) + ": " + newLine
				+ MiddleUtility.createTraceTable(variablesDescriptor, decorated);
		
		return trace;
	}

	/**
	 * Returns true if this descriptor has a procedure with specific name
	 * @param procedureName
	 * @return
	 */
	public boolean hasProcedure(String procedureName) {
		
		return procedures.containsKey(procedureName);
	}
}
