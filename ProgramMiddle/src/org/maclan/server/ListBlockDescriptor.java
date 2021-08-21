/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.maclan.server;

import java.util.List;

/**
 * @author
 *
 */
public class ListBlockDescriptor extends BreakBlockDescriptor {

	/**
	 * Current index.
	 */
	protected long index;
	
	/**
	 * List reference.
	 */
	protected List<?> list;

	/**
	 * Constructor.
	 * @param startIndex
	 * @param list
	 */
	public ListBlockDescriptor(int startIndex, List<?> list) {

		this.index = startIndex;
		this.list = list;
	}

	/**
	 * @return the index
	 */
	public long getIndex() {
		return index;
	}

	/**
	 * @return the list
	 */
	public List<?> getList() {
		return list;
	}
	
	/**
	 * Get list count.
	 * @return
	 */
	public long getCount() {
		return list.size();
	}

	/**
	 * @param index the index to set
	 */
	public void setIndex(long index) {
		this.index = index;
	}

	/**
	 * Get current item.
	 * @return
	 */
	public Object getCurrentItem() {
		
		try {
			return list.get((int) index - 1);
		}
		catch (Exception e) {
			
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		return list.toString();
	}

	/**
	 * Returns true value if current item is first in the list.
	 * @return
	 */
	public boolean isFirst() {

		return index == 1;
	}

	/**
	 * Returns true value if current item is last in the list.
	 * @return
	 */
	public boolean isLast() {

		return index == list.size() || breaked;
	}

	/**
	 * Returns true value if the current item is in the middle
	 * of the list.
	 * @return
	 */
	public boolean isMiddle() {

		return !isFirst() && !isLast();
	}
}
