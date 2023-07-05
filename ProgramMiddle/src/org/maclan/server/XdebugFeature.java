/*
 * Copyright 2010-2023 (C) vakol
 * 
 * Created on : 07-06-2023
 *
 */
package org.maclan.server;

import org.multipage.gui.Utility;

/**
 * Xdebug feature object.
 * @author vakol
 *
 */
public class XdebugFeature {
	
	/**
	 * Feature name.
	 */
	public String name = null;
	
	/**
	 * Feature supported or not supported (a flag).
	 */
	public boolean supported = false;
	
	/**
	 * Feature value.
	 */
	public String value = null;

	/**
	 * Create feature object.
	 * @param featureName
	 * @param supportedString
	 * @param featureValue
	 * @return
	 */
	public static XdebugFeature createFeature(String featureName, String supportedString, String featureValue)
			throws Exception {
		
		XdebugFeature feature = new XdebugFeature();
		feature.name = featureName;
		feature.supported = "1".equals(supportedString);
		feature.value = featureValue;
		
		if (!feature.supported && !"0".equals(supportedString)) {
			Utility.throwException("org.maclan.server.messageXdebugFeatureSupportedNotNull", supportedString);
		}
		return feature;
	}
}
