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
	 * Feature supported or not supported.
	 */
	private boolean supported = false;
	
	/**
	 * Feature name.
	 */
	private String name = null;	
	/**
	 * Feature value.
	 */
	private String value = null;

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
	
	/**
	 * Returns true value when the feature is supported.
	 * @return
	 */
	public boolean isSupported() {
		
		return supported;
	}
	
	/**
	 * Get feature name.
	 * @return
	 */
	public String getName() {
		
		return name;
	}

	/**
	 * Returns feature value.
	 * @return
	 */
	public String getValue() {
		
		return value;
	}
}
