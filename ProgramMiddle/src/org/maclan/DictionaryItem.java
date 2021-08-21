/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.maclan;

/**
 * @author
 *
 */
public class DictionaryItem {

	/**
	 * Localized text ID.
	 */
	private long id;
	
	/**
	 * Default text.
	 */
	private String defaultText;
	
	/**
	 * Localized text.
	 */
	private String localizedText;
	
	/**
	 * Localized text holderId.
	 */
	private long holderId;
	
	/**
	 * Holder type.
	 */
	private TextHolderType holderType;
	
	/**
	 * Holder text.
	 */
	private String holderText;
	
	/**
	 * Hide flag.
	 */
	private boolean hide;

	/**
	 * @param id
	 * @param defaultText
	 * @param localizedText
	 * @param holderId
	 * @param holderType
	 */
	public DictionaryItem(long id, String defaultText, String localizedText,
			long holder, TextHolderType holderType) {

		this.id = id;
		this.defaultText = defaultText;
		this.localizedText = localizedText;
		this.holderId = holder;
		this.setHolderType(holderType);
		this.hide = false;
	}

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @return the defaultText
	 */
	public String getDefaultText() {
		return defaultText;
	}

	/**
	 * @return the localizedText
	 */
	public String getLocalizedText() {
		return localizedText;
	}

	/**
	 * @return the holderId
	 */
	public long getHolderId() {
		return holderId;
	}

	/**
	 * Get holderId text.
	 * @return
	 */
	public String getHolderText() {
		return holderText;
	}

	/**
	 * @param holderText the holderText to set
	 */
	public void setHolderText(String holderText) {
		this.holderText = holderText;
	}

	/**
	 * @return the hide
	 */
	public boolean isHidden() {
		return hide;
	}

	/**
	 * @param hide the hide to set
	 */
	public void setHide(boolean hide) {
		this.hide = hide;
	}

	/**
	 * @param holderType the holderType to set
	 */
	public void setHolderType(TextHolderType holderType) {
		this.holderType = holderType;
	}

	/**
	 * @return the holderType
	 */
	public TextHolderType getHolderType() {
		return holderType;
	}
}
