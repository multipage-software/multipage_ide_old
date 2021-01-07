/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.gui;

import java.awt.Component;

/**
 * @author
 *
 */
public interface StringValueEditor {

	/**
	 * Value meanings.
	 */
	public static final String meansInteger =              "i  ";  // - must have 3 characters according to database table column specification!
	public static final String meansReal =                 "r  ";
	public static final String meansBoolean =              "b  ";
	public static final String meansEnumeration =          "e  ";
	public static final String meansColor =                "c  ";
	public static final String meansArea =                 "a  ";
	public static final String meansText =  			   "t  ";
	public static final String meansPath =                 "p  ";
	public static final String meansCssAnimation =         "ca ";
	public static final String meansCssBorder = 		   "cb ";
	public static final String meansCssBorderImage = 	   "cbi";
	public static final String meansCssBackground = 	   "cbk";
	public static final String meansCssBorderRadius =      "cbr";
	public static final String meansCssBoxShadow = 	       "cbs";
	public static final String meansCssTextShadow =        "cbt";
	public static final String meansCssClip =              "cc ";
	public static final String meansCssCounter =           "cci";
	public static final String meansCssCursor =            "ccu";
	public static final String meansCssFont = 			   "cf ";
	public static final String meansCssFlex =              "cfx";
	public static final String meansCssKeyframes =         "ck ";
	public static final String meansCssListStyle =         "cls";
	public static final String meansCssMime =              "cm ";
	public static final String meansCssNumber =            "cn ";
	public static final String meansCssOutlines =          "co ";
	public static final String meansCssPerspectiveOrigin = "cpo";
	public static final String meansCssQuotes =            "cq ";
	public static final String meansCssResource =          "cr ";
	public static final String meansCssSpacing =           "cs ";
	public static final String meansCssTransform =         "ct ";
	public static final String meansCssTextLine =          "ctl";
	public static final String meansCssTransformOrigin =   "cto";
	public static final String meansCssTransition =        "ctr";
	public static final String meansCssUrlResource =       "cur";
	public static final String meansCssUrlsResources =     "cus";
	
	public static final String meansHtmlAnchorAreaAlias =  "haa";
	public static final String meansHtmlAnchorAreaRes =    "has";
	public static final String meansHtmlAnchorAreaRef =    "har";
	public static final String meansHtmlAnchorUrl =        "hau";
	
	public static final String meansExternalProvider =     "epr";
	
	public static final String meansFile =                 "fi ";
	public static final String meansFolder =               "fo ";
	
	public static final String meansUrl =                  "url";
	
	/**
	 * Get editor component.
	 * @return
	 */
	abstract Component getComponent();

	/**
	 * Get value.
	 * @return
	 */
	abstract String getStringValue();
	/**
	 * Get external provider specification
	 * @return
	 */
	abstract String getSpecification();
	
	/**
	 * Set string value.
	 * @param string
	 */
	abstract void setStringValue(String string);

	/**
	 * Get value meaning.
	 */
	abstract String getValueMeaning();

	/**
	 * Set grayed controls.
	 * @param isDefault
	 * @return
	 */
	abstract boolean setControlsGrayed(boolean isDefault);

	/**
	 * Returns true value if the value means a text.
	 * @param valueMeaning
	 * @return
	 */
	public static boolean meansText(String valueMeaning) {
		
		if (valueMeaning.length() != 3) {
			return false;
		}
		
		if (valueMeaning.equals(meansText)) {
			// It is a common text value.
			return true;
		}
		
		if (valueMeaning.equals(meansColor)) {
			// It is a color value.
			return false;
		}

		if (valueMeaning.charAt(0) == 'c') {
			// It is a CSS text value.
			return true;
		}
		
		return false;
	}
	
	/**
	 * Returns true value if the parameters designates a HTML anchor.
	 * @param valueMeaning
	 * @return
	 */
	public static boolean meansHtmlAnchor(String valueMeaning) {
		
		return meansHtmlAnchorAreaRef.equals(valueMeaning)
				|| meansHtmlAnchorAreaAlias.equals(valueMeaning)
				|| meansHtmlAnchorUrl.equals(valueMeaning)
				|| meansHtmlAnchorAreaRes.equals(valueMeaning);
	}
}
