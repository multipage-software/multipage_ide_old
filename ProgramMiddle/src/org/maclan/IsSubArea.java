/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.maclan;

/**
 * 
 * @author
 *
 */
public class IsSubArea {

	public Long id;
	public Long subAreaId;
	public Boolean inheritance;
	public Integer prioritySub;
	public Integer prioritySuper;
	public String nameSub;
	public String nameSuper;
	public Long positionId;
	public Boolean hideSub;
	public Boolean recursion;

	// Auxiliry fileds.
	public boolean mark = false;

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "IsSubArea [id=" + id + ", subAreaId=" + subAreaId
				+ ", inheritance=" + inheritance + ", prioritySub="
				+ prioritySub + ", prioritySuper=" + prioritySuper
				+ ", nameSub=" + nameSub + ", nameSuper=" + nameSuper
				+ ", positionId=" + positionId + ", hideSub=" + hideSub
				+ ", recursion=" + recursion + ", mark=" + mark + "]";
	}
}