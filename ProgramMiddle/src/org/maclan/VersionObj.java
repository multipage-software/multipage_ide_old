/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.maclan;

import org.multipage.gui.Utility;

/**
 * @author
 *
 */
public class VersionObj implements Element {

	/**
	 * Identifier.
	 */
	private long id;
	
	/**
	 * Alias.
	 */
	private String alias;
	
	/**
	 * Description.
	 */
	private String description;
	
	/**
	 * User value.
	 */
	private Object user;
	
	/**
	 * Constructor.
	 * @param id
	 * @param alias
	 * @param description
	 */
	public VersionObj(long id, String alias, String description) {
		
		this.id = id;
		this.alias = alias;
		this.description = description;
	}

	/**
	 * Constructor.
	 */
	public VersionObj() {
		
		id = 0L;
		alias = "";
		description = "";
	}

	/**
	 * Get ID.
	 */
	@Override
	public long getId() {
		
		return id;
	}

	/**
	 * @return the alias
	 */
	public String getAlias() {
		return alias;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param alias the alias to set
	 */
	public void setAlias(String alias) {
		this.alias = alias;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}
	
	/**
	 * Check alias.
	 * @return
	 */
	public MiddleResult checkAlias() {
		
		return checkAlias(alias);
	}
	
	/**
	 * Check alias.
	 * @return
	 */
	public static MiddleResult checkAlias(String alias) {
		
		// Minimum length is 1.
		if (alias.length() < 1) {
			return new MiddleResult("middle.resultBadMinimumVersionAliasLength", null);
		}
		
		// Cannot contain white-spaces.
		if (Utility.findRegExp(alias, "\\s", false)) {
			return new MiddleResult("middle.resultVersionAliasCannotContainWhitespaces", null);
		}
		
		// Can contain only standard ASCII letters and numbers.
		if (!alias.matches("\\w+")) {
			return new MiddleResult("middle.resultVersionAliasCanContainCharNums", null);
		}
		
		return MiddleResult.OK;
	}

	/**
	 * Set user value.
	 * @param user
	 */
	public void setUser(Object user) {
		
		this.user = user;
	}
	
	/**
	 * Get user value.
	 */
	public Object getUser() {
		
		return user;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return description;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
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
		VersionObj other = (VersionObj) obj;
		if (id != other.id)
			return false;
		return true;
	}

	/**
	 * Returns true valu if the version is default.
	 * @return
	 */
	public boolean isDefault() {
		
		return id == 0L;
	}
}
