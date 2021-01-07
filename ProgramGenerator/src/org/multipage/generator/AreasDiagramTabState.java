/*
 * Copyright 2010-2020 (C) vakol
 * 
 * Created on : 14-12-2020
 *
 */
package org.multipage.generator;

import java.io.Serializable;

/**
 * 
 * @author sechance
 *
 */
public class AreasDiagramTabState extends AreasTabState implements Serializable {

	/**
	 * Version
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Initial X translation of the diagram.
	 */
	public double translationx = 0.0;
	
	/**
	 * Initial Y translation of the diagram.
	 */
	public double translationy = 0.0;
	
	/**
	 * Initial zoom of the diagram.
	 */
	public double zoom = 1.0;
	
	/**
	 * Constructor
	 */
	public AreasDiagramTabState() {
		
		type = TabType.areasDiagram;
	}
		
	/**
	 * Set this tab state from the input tab state
	 * @param tabState
	 */
	public void setTabStateFrom(AreasDiagramTabState tabState) {
		
		super.setTabStateFrom(tabState);
		
		type = TabType.areasDiagram;
		
		translationx = tabState.translationx;
		translationx = tabState.translationx;
		zoom = tabState.zoom;
	}
}
