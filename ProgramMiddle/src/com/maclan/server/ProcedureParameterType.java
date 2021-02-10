/*
 * Copyright 2010-2021 (C) vakol (see attached LICENSE file for additional info)
 * 
 * Created on : 26-04-2017
 *
 */

package com.maclan.server;

/**
 * @author
 *
 */
public enum ProcedureParameterType {
	
	/**
	 * Procedure parameter types.
	 */
	input,
	output,
	returned,
	resultText;

	/**
	 * Returns true value if it is an output type.
	 * @return
	 */
	public boolean isOutput() {
		
		return this == ProcedureParameterType.output
				|| this == ProcedureParameterType.returned
				|| this == ProcedureParameterType.resultText;
	}
}
