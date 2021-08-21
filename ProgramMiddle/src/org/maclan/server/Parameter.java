/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.maclan.server;

/**
 * @author
 *
 */
public class Parameter {
	
	/**
	 * Parameter type.
	 */
	private ProcedureParameterType parameterType;

	/**
	 * Constructor.
	 * @param parameterType
	 */
	public Parameter(ProcedureParameterType parameterType) {

		this.parameterType = parameterType;
	}

	/**
	 * @return the isReference
	 */
	public boolean isOutput() {
		
		return parameterType.isOutput();
	}

	/**
	 * Get parameter type.
	 */
	public ProcedureParameterType getParameterType() {
		
		return parameterType;
	}
}
