/**
 * 
 */
package com.maclan.expression;

/**
 * @author
 *
 */
public class ProcedureParameter {

	/**
	 * Name.
	 */
	private String name;
	
	/**
	 * Output object identifier.
	 */
	private String outputObjectIdentifier;

	/**
	 * Set input value.
	 */
	private Object inputValue;

	/**
	 * Set name.
	 * @param name
	 */
	public void setName(String name) {
		
		this.name = name;
	}
	
	/**
	 * Get name.
	 * @return
	 */
	public String getName() {
		
		return name;
	}

	/**
	 * Set output object identifier.
	 * @param outputObjectIdentifier
	 */
	public void setOutputObjectIdentifier(String outputObjectIdentifier) {
		
		this.outputObjectIdentifier = outputObjectIdentifier;
	}
	
	/**
	 * Get output object identifier.
	 * @return
	 */
	public String getOutputObjectIdentifier() {
		
		return outputObjectIdentifier;
	}

	/**
	 * Set input value.
	 * @param inputValue
	 */
	public void setInputValue(Object inputValue) {
		
		this.inputValue = inputValue;
	}
	
	/**
	 * Get input value.
	 * @return
	 */
	public Object getInputValue() {
		
		return inputValue;
	}
}
