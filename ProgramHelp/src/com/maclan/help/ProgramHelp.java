/*
 * Copyright 2010-2020 (C) vakol
 * 
 * Created on : 13-05-2020
 *
 */
package com.maclan.help;

import java.io.BufferedInputStream;
import java.io.InputStream;

/**
 * @author user
 *
 */
public class ProgramHelp {
	
	/**
	 * Open Maclan reference template XML as input stream.
	 * @return
	 */
	public static InputStream openMaclanReferenceXml() {
		
		InputStream inputStream = ProgramHelp.class.getResourceAsStream("/com/maclan/reference_template/maclan_reference.xml");
		return new BufferedInputStream(inputStream);
	}
	
	/**
	 * Open Maclan reference template DAT as input stream.
	 * @return
	 */
	public static InputStream openMaclanReferenceDat() {
		
		InputStream inputStream = ProgramHelp.class.getResourceAsStream("/com/maclan/reference_template/maclan_reference.dat");
		return new BufferedInputStream(inputStream);
	}
}
