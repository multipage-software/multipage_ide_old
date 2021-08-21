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
public class Procedure {
	
	/**
	 * Inner text.
	 */
	private String innerText;
	
	/**
	 * Parameters.
	 */
	private HashMap<String, Parameter> parameters;
	
	/**
	 * When users CALL procedure with this flag declared, them must use complex tags such:
	 * [@CALL name]...inner text...[/@CALL] or [@Name]...inner text...[/@Name].
	 * Inner text of CALL tags is passed as an argument of the call.
	 */
	private boolean fullCall = false;
	
	/**
	 * Transparency of the procedure for defined inner procedures
	 */
	private boolean transparent = false;

	/**
	 * Return text flag.
	 */
	private boolean isReturnText = false;

	/**
	 * Create procedure.
	 * @param parameters
	 * @param innerText
	 * @param nameForced 
	 * @param fullCall 
	 * @param transparent 
	 */
	public Procedure(Properties parameters, String innerText, boolean nameForced, boolean fullCall, boolean transparent)
		throws Exception {
		
		this.innerText = innerText;
		this.parameters = new HashMap<String, Parameter>();
		this.fullCall = fullCall;
		this.transparent = transparent;
		
		int retCount = 0;
		
		// Create parameters set.
		for (Entry<Object, Object> parameterEntry : parameters.entrySet()) {
			
			// Get parameter name.
			Object keyObject = parameterEntry.getKey();
			if (!(keyObject instanceof String)) {
				throw new Exception(Resources.getString("server.messageParameterNameIsNotString"));
			}
			String parameterName = (String) keyObject;
			if (parameterName.equals(nameForced ? "$name" : "name")) {
				throw new Exception(Resources.getString("server.messageUnexpectedNameParameter"));
			}
			
			// If parameter already exists.
			if (this.parameters.containsKey(parameterName)) {
				throw new Exception(Resources.getString("server.messageParameterAlreadyExists"));
			}
			
			// Get parameter type.
			ProcedureParameterType parameterType = ProcedureParameterType.input;
			
			Object valueObject = parameterEntry.getValue();
			if (valueObject instanceof String) {
				
				String specification = (String) valueObject;
				if (!specification.isEmpty()) {
					
					if (specification.equals("out")) {
						parameterType = ProcedureParameterType.output;
					}
					else if (specification.equals("ret")) {
						parameterType = ProcedureParameterType.returned;
						retCount++;
					}
					else if (specification.equals("txt")) {
						parameterType = ProcedureParameterType.resultText;
					}
					else {
						throw new Exception(Resources.getString("server.messageExpectingOutOrRetOrTextSpecification"));
					}
				}
			}
			
			// Check ret and txt count.
			if (retCount > 1) {
				throw new Exception(Resources.getString("server.messageReturnValueSpecificationAmbiguity"));
			}
			
			// Create new parameter and add it to the set.
			Parameter parameter = new Parameter(parameterType);
			this.parameters.put(parameterName, parameter);
		}
	}

	/**
	 * @return the innerText
	 */
	public String getInnerText() {
		return innerText;
	}

	/**
	 * @return the parameters
	 */
	public HashMap<String, Parameter> getParameters() {
		return parameters;
	}

	/**
	 * Get procedure trace.
	 * @return
	 */
	public String getTraceText() {
		
		return parameters.toString();
	}

	/**
	 * Trace procedure.
	 * @param decorated
	 * @return
	 */
	public String trace(boolean decorated) {
		
		// Trace parameters.
		String [][] descriptor = new String [parameters.size()][2];
		int index = 0;
		
		for (Entry<String, Parameter> entry : parameters.entrySet()) {
			
			descriptor[index][0] = Resources.getString("server.textParameterTrace") + ": " + entry.getKey();
			descriptor[index][1] = entry.getValue().isOutput()
					? Resources.getString("server.textParameterOutputTrace") : "";
			
			index++;
		}
		
		String trace = MiddleUtility.createTraceTable(descriptor, decorated);
		
		// Trace inner text of the procedure.
		final int maximumCharacters = 128;
		
		int numberCharacters = innerText.length();
		
		if (numberCharacters > maximumCharacters) {
			numberCharacters = maximumCharacters;
		}
		
		String body = MiddleUtility.trimTextWithTags(innerText.substring(0, numberCharacters), decorated).trim();
		
		if (decorated) {
			trace += "<style> .TraceProcedureBody {font-style: italic; background-color: lightgreen; color: black;}</style>";
			return trace +
					Resources.getString("server.textProcedureBodyTrace") + ": " +
					"<span class='TraceProcedureBody'>" + body + "</span>";
		}
		else {
			return trace + Resources.getString("server.textProcedureBodyTrace") + ": " + body;
		}
	}
	
	/**
	 * Returns true if the procedure call is a complex tag.
	 * @return
	 */
	public boolean isFullCall() {
		
		return fullCall;
	}
	
	/**
	 * Returns true if the procedure is transparent for inner procedures.
	 * @return
	 */
	public boolean isTransparent() {
		
		return transparent;
	}
	
	/**
	 * Set return text flag.
	 * @param isReturnText
	 */
	public void setReturnText(boolean isReturnText) {
		
		this.isReturnText = isReturnText;
	}
	
	/**
	 * Get return text flag.
	 */
	public boolean isReturnText() {
		
		return isReturnText;
	}
}
