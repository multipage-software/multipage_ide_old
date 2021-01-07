/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package com.maclan;

/**
 * @author
 *
 */
public class EnumerationValue implements Cloneable {
	
	/**
	 * Empty enumeration value.
	 */
	public static final EnumerationValue emptyValue;
	
	/**
	 * Static constructor.
	 */
	static {
		emptyValue = new EnumerationValue();
		emptyValue.value = "";
		emptyValue.enumeration = new EnumerationObj(0, "");
	}

	/**
	 * Enumeration reference.
	 */
	private EnumerationObj enumeration;
	
	/**
	 * Identifier.
	 */
	private long id;
	
	/**
	 * Value.
	 */
	private String value;

	/**
	 * Description.
	 */
	private String description;
	
	/**
	 * Set enumeration.
	 * @param enumeration
	 */
	public void setEnumeration(EnumerationObj enumeration) {
		
		this.enumeration = enumeration;
	}

	/**
	 * Set identifier.
	 * @param id
	 */
	public void setId(long id) {
		
		this.id = id;
	}

	/**
	 * Set value.
	 * @param value
	 */
	public void setValue(String value) {
		
		this.value = value;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return value;
	}

	/**
	 * Get value.
	 * @return
	 */
	public String getValue() {
		
		return value;
	}
	
	/**
	 * Get description.
	 * @return
	 */
	public String getDescription() {
		
		return description;
	}

	/**
	 * Get identifier.
	 * @return
	 */
	public long getId() {
		
		return id;
	}

	/**
	 * Get enumeration type ID.
	 * @return
	 */
	public long getEnumerationId() {
		
		if (enumeration != null) {
			return enumeration.getId();
		}
		return 0L;
	}

	/**
	 * Get value full name.
	 * @return
	 */
	public String getValueFullNameDecorated() {
		
		if (enumeration == null && value == null) {
			return "";
		}
		
		String valueText = value != null ? value : "";
		String descriptionText = description != null ? description : "";
		
		String fullName = "<html>";
		
		if (enumeration != null) {
			fullName += "<i>" + enumeration.getDescription() + ":</i> ";
		}
		
		String valueAndDescription = valueText;
		if (!descriptionText.isEmpty()) {
			valueAndDescription += " (" + descriptionText + ")";
		}
		
		fullName += "<b>" + valueAndDescription + "</b><html>";
		return fullName;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EnumerationValue other = (EnumerationValue) obj;
		if (id != other.id)
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	/**
	 * Get enumeration.
	 * @return
	 */
	public EnumerationObj getEnumeration() {
		
		return enumeration;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected Object clone() throws CloneNotSupportedException {
		
		return super.clone();
	}

	/**
	 * Set description.
	 * @param description
	 */
	public void setDescription(String description) {
		
		if (description == null) {
			this.description = "";
		}
		else {
			this.description = description;
		}
	}

	/**
	 * Get value description for the Builder.
	 * @return
	 */
	public String getValueDescriptionBuilder() {

		if (description == null || description.isEmpty()) {
			return value;
		}
		return String.format("%s (%s)", value, description);
	}

	/**
	 * Get description for generator.
	 * @return
	 */
	public String getValueDescriptionGenerator() {
		
		if (description == null || description.isEmpty()) {
			return value;
		}
		return description;
	}
}
