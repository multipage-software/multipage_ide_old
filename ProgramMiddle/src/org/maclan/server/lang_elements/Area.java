/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.maclan.server.lang_elements;

import java.util.LinkedList;

import org.maclan.MiddleResult;
//graalvm import org.graalvm.polyglot.HostAccess;
import org.multipage.util.Obj;

/**
 * @author
 *
 */
public class Area implements BoxedObject {

	/**
	 * Server reference,
	 */
	private org.maclan.server.AreaServer server;
	
	/**
	 * Middle layer area object reference.
	 */
	org.maclan.Area area;
	
	/**
	 * Public properties.
	 */
	//graalvm @HostAccess.Export
	public final String name;
	//graalvm @HostAccess.Export
	public final long id;
	//graalvm @HostAccess.Export
	public final String alias;

	/**
	 * Constructor.
	 * @param server 
	 * @param area
	 */
	public Area(org.maclan.server.AreaServer server, org.maclan.Area area) {
		
		this.server = server;
		this.area = area;
		
		area.getDescription();
		String text = area.getDescriptionForced();
		this.name = server.state.showLocalizedTextIds && area.isLocalized() ? org.maclan.server.AreaServer.getIdHtml("A", area.getId()) + text : text;
		
		this.id = area.getId();
		this.alias = area.getAlias();
	}

	/**
	 * Unbox object.
	 */
	@Override
	public Object unbox() {
		
		return area;
	}

	/**
	 * Convert to string.
	 */
	@Override
	public String toString() {
		
		return String.format("[Area object id = %d]", id);
	}
	
	/**
	 * Returns true value if this area inherits from given area.
	 * @param inheritedArea
	 * @param level
	 * @return
	 */
	//graalvm @HostAccess.Export
	public boolean inherits(Area inheritedArea, Long level) throws Exception {
		
		return server.inherits(area, inheritedArea.area, level);
	}
	
	/**
	 * Returns true value if this area inherits from given area.
	 * @param inheritedArea
	 * @return
	 */
	//graalvm @HostAccess.Export
	public boolean inherits(Area inheritedArea) throws Exception {
		
		return server.inherits(area, inheritedArea.area, null);
	}
	
	/**
	 * Get sub area relation.
	 * @param subArea
	 * @return
	 * @throws Exception 
	 */
	//graalvm @HostAccess.Export
	public AreaRelation getSubRelation(Area subArea) throws Exception {
		
		// Check parameter.
		if (subArea == null) {
			return null;
		}
		
		server.loadSubAreasData(area);
		
		org.maclan.AreaRelation relation = area.getSubRelation(subArea.area.getId());
		return new AreaRelation(relation);
	}
	
	/**
	 * Get super area relation.
	 * @param superArea
	 * @return
	 * @throws Exception
	 */
	//graalvm @HostAccess.Export
	public AreaRelation getSuperRelation(Area superArea) throws Exception {
		
		// Check parameter.
		if (superArea == null) {
			return null;
		}
		
		server.loadSuperAreasData(area);
		
		org.maclan.AreaRelation relation = area.getSuperRelation(superArea.area.getId());
		return new AreaRelation(relation);
	}
	
	/**
	 * Get pure name without possible localization info.
	 * @return
	 */
	//graalvm @HostAccess.Export
	public String getPureName() {
		
		return area.getDescription();
	}
	
	/**
	 * Returns true value if this area is current (server.thisArea).
	 */
	//graalvm @HostAccess.Export
	public boolean isCurrent() {
		
		return area.getId() == server.state.middle.getCurrentRootArea().getId();
	}
	
	/**
	 * Returns true value if this area is a home area.
	 * @return
	 * @throws Exception 
	 */
	//graalvm @HostAccess.Export
	public boolean isHome() throws Exception {
		
		Obj<org.maclan.Area> homeArea = new Obj<org.maclan.Area>();
		
		MiddleResult result = server.state.middle.loadHomeAreaData(homeArea);
		if (result.isNotOK()) {
			throw new Exception(result.getMessage());
		}
		
		return area.getId() == homeArea.ref.getId();
	}
	
	/**
	 * Returns true value if this area is visible.
	 * @return
	 */
	//graalvm @HostAccess.Export
	public boolean isVisible() {
		
		return area.isVisible();
	}
	
	/**
	 * Returns true value if this area is requested.
	 * @return
	 */
	//graalvm @HostAccess.Export
	public boolean isRequested() {
		
		return area.getId() == server.state.requestedArea.getId();
	}
	
	/**
	 * Returns true value if this area is a constructor.
	 * @return
	 */
	//graalvm @HostAccess.Export
	public boolean isConstructor() {
		
		return area.isConstructorArea();
	}

	/**
	 * Returns true value if this area is a constructor.
	 * @return
	 */
	//graalvm @HostAccess.Export
	public boolean isStartArea() {
		
		return area.isStartArea();
	}
	
	/**
	 * Returns area file name.
	 * @return
	 */
	//graalvm @HostAccess.Export
	public String getFileName() {
		
		return area.getFileName();
	}
	
	/**
	 * Gets related area.
	 * @return
	 * @throws Exception 
	 */
	//graalvm @HostAccess.Export
	public Area getRelatedArea() throws Exception {
		
		server.loadRelatedAreaData(area);
		
		org.maclan.Area middleArea = area.getRelatedArea();
		if (middleArea == null) {
			return null;
		}
		
		return new Area(server, middleArea);
	}
	
	/**
	 * Gets list of slots connected to this area.
	 * @return
	 * @throws Exception 
	 */
	//graalvm @HostAccess.Export
	public LinkedList<Slot> getSlots() throws Exception {
		
		server.loadAreaSlotsData(area);
		
		LinkedList<org.maclan.Slot> middleSlots = area.getSlots();
		if (middleSlots == null) {
			return null;
		}
		
		LinkedList<Slot> slots = new LinkedList<Slot>();
		
		for (org.maclan.Slot middleSlot : middleSlots) {
			slots.add(new Slot(server, middleSlot));
		}
		
		return slots;
	}
	
	/**
	 * Gets this area constructor area.
	 * @return
	 * @throws Exception 
	 */
	//graalvm @HostAccess.Export
	public Area getConstructorArea() throws Exception {
		
		org.maclan.Area middleArea  = server.getConstructorArea(area);
		if (middleArea == null) {
			return null;
		}
		
		return new Area(server, middleArea);
	}
	
	/**
	 * Gets first visible super area.
	 * @return
	 * @throws Exception 
	 */
	//graalvm @HostAccess.Export
	public Area getFirstVisibleSuperArea() throws Exception {
		
		org.maclan.Area middleArea = server.getFirstVisibleSuperArea(area.getId());
		if (middleArea == null) {
			return null;
		}
		
		return new Area(server, middleArea);
	}
}
