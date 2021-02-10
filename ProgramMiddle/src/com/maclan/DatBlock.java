/*
 * Copyright 2010-2021 (C) vakol (see attached LICENSE file for additional info)
 * 
 * Created on : 15-05-2020
 *
 */

package com.maclan;

/**
 * 
 * @author user
 *
 */
public class DatBlock {
	
	/**
	 * Minimum number of bytes in large blocks.
	 */
	public static final int largeBlockBytes = 104857600;
	
	/**
	 * Block type.
	 */
	public enum Type { languageIcon, resourceBlob }
	public Type type;
	
	/**
	 * Record ID.
	 */
	public long recordId;
	
	/**
	 * Block start offset.
	 */
	public long dataStart;
	
	/**
	 * Block end offset.
	 */
	public long dataEnd;
	
	/**
	 * Create new language icon block.
	 * @param recordId
	 * @param dataStart
	 * @param dataEnd
	 * @return
	 */
	public static DatBlock newLanguageIcon(long recordId, long dataStart, long dataEnd) {
		
		DatBlock block = new DatBlock();
		
		block.type = Type.languageIcon;
		block.recordId = recordId;
		block.dataStart = dataStart;
		block.dataEnd = dataEnd;
		
		return block;
	}
	
	/**
	 * Create new language icon block.
	 * @param recordId
	 * @param dataStart
	 * @param dataEnd
	 * @return
	 */
	public static DatBlock newResoureBlob(long recordId, long dataStart, long dataEnd) {
		
		DatBlock block = new DatBlock();
		
		block.type = Type.resourceBlob;
		block.recordId = recordId;
		block.dataStart = dataStart;
		block.dataEnd = dataEnd;
		
		return block;
	}
	
	/**
	 * Dump object.
	 */
	@Override
	public String toString() {
		return "DatBlock [type=" + type.name() + ", recordId=" + recordId + ", dataStart=" + dataStart + ", dataEnd=" + dataEnd
				+ "]";
	}
}
