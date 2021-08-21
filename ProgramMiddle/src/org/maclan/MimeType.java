/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.maclan;

import java.util.ArrayList;

/**
 * @author
 *
 */
public class MimeType implements Element {
	
	/**
	 * Identifier.
	 */
	public long id;

	/**
	 * Type.
	 */
	public String type;
	
	/**
	 * Extension.
	 */
	public String extension;
	
	/**
	 * Preference.
	 */
	public boolean preference;
	
	/**
	 * Constructor.
	 */
	public MimeType(long id, String type, String extension,
			boolean preference) {
		
		this.id = id;
		this.type = type;
		this.extension =  extension;
		this.preference = preference;
	}
	
	/**
	 * Constructor.
	 */
	public MimeType() {
		this(0L, "", "", false);
	}

	/**
	 * Clone object.
	 */
	public MimeType clone() {
		
		return new MimeType(id, type, extension, preference);
	}

	/**
	 * Copy object content.
	 * @param mimeType
	 */
	public void copy(MimeType mimeType) {

		id = mimeType.id;
		type = mimeType.type;
		extension = mimeType.extension;
		preference = mimeType.preference;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return type;
	}

	/**
	 * Get default MIME type.
	 * @param mimeTypes
	 * @return
	 */
	private static MimeType getDefault(ArrayList<MimeType> mimeTypes) {

		MimeType defaultMimeType = null;
		
		for (MimeType mimeType : mimeTypes) {
			if (mimeType.id == 0) {
				return mimeType;
			}
			if (mimeType.type.equals("text/plain")) {
				defaultMimeType = mimeType;
			}
		}
		
		if (defaultMimeType != null) {
			return defaultMimeType;
		}
		if (mimeTypes.size() > 0) {
			return mimeTypes.get(0);
		}
		return null;
	}

	/**
	 * Get MIME with given extension.
	 * @param mimeTypes
	 * @param extensionParam
	 * @return
	 */
	public static MimeType getMimeWithExtension(ArrayList<MimeType> mimeTypes,
			String extension) {

		if (!extension.isEmpty()) {
			// Find given extension.
			for (MimeType mimeType : mimeTypes) {
				if (mimeType.extension.compareToIgnoreCase(extension) == 0) {
					return mimeType;
				}
			}
		}

		// Return default MIME.
		return getDefault(mimeTypes);
	}

	/**
	 * Get ID.
	 * @return
	 */
	@Override
	public long getId() {

		return id;
	}

	/**
	 * Returns true value is the object properties match.
	 * @param type
	 * @param extension
	 * @return
	 */
	public boolean equals(String type, String extension) {
		
		return this.type.equals(type) && this.extension.equals(extension);
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
		MimeType other = (MimeType) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
}
