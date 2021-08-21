/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.maclan.server.lang_elements;

import java.util.LinkedList;

import org.maclan.Language;
import org.maclan.server.BlockDescriptor;
import org.maclan.server.JavaScriptBlockDescriptor;
import org.maclan.server.ScriptingEngine;
//graalvm import org.graalvm.polyglot.HostAccess;
import org.multipage.gui.Utility;
import org.multipage.util.Obj;
import org.multipage.util.Resources;
import org.multipage.util.TextOutputCapturer;
import org.openjdk.nashorn.internal.runtime.Undefined;



/**
 * @author
 *
 */
public class AreaServer implements BoxedObject {
	
	/**
	 * Area server reference.
	 */
	private org.maclan.server.AreaServer server;
	
	/**
	 * Request.
	 */
	//graalvm @HostAccess.Export
	public Request request;
	
	/**
	 * Response.
	 */
	//graalvm @HostAccess.Export
	public Response response;
	
	/**
	 * Level of tags processing.
	 */
	//graalvm @HostAccess.Export
	public final long level;
	
	/**
	 * Area references.
	 */
	//graalvm @HostAccess.Export
	public       Area thisArea;
	//graalvm @HostAccess.Export
	public final Area startArea;
	//graalvm @HostAccess.Export
	public final Area homeArea;
	//graalvm @HostAccess.Export
	public final Area requestedArea;
	
	/**
	 * Constructor.
	 * @param server
	 * @throws Exception 
	 */
	public AreaServer(org.maclan.server.AreaServer server) throws Exception {
		
		this.server = server;
		
		this.request = new Request(server);
		this.response = new Response(server);
		this.level = server.state.level;
		this.thisArea = new Area(server, server.state.area);
		this.startArea = new Area(server, server.state.startArea);
		
		Obj<org.maclan.Area> area = new Obj<org.maclan.Area>();
		server.loadHomeAreaData(area);
		this.homeArea = new Area(server, area.ref);
		
		this.requestedArea = new Area(server, server.state.requestedArea);
	}
	
	/**
	 * Print parameter.
	 */
	//graalvm @HostAccess.Export
	public void print(Object object)
		throws Exception {
		
		String text = null;
		if (object == null) {
			text = "null";
		}
		else {
			text = object.toString();
		}
		
		BlockDescriptor blockDescriptor = server.state.blocks.getCurrentBlockDescriptor();
		if (blockDescriptor instanceof JavaScriptBlockDescriptor) {
			
			JavaScriptBlockDescriptor javaScriptBlockDescriptor = (JavaScriptBlockDescriptor) blockDescriptor;
			TextOutputCapturer scriptOutputCapturer = javaScriptBlockDescriptor.scriptOutputCapturer;
			scriptOutputCapturer.print(text);
		}
		else {
			Utility.throwException("server.messageMissingJavaScriptBlockDescriptor");
		}
	}
	
	/**
	 * Println parameter.
	 */
	//graalvm @HostAccess.Export
	public void println(Object object)
		throws Exception {
		
		String text = null;
		if (object == null) {
			text = "null";
		}
		else {
			text = object.toString();
		}
		
		BlockDescriptor blockDescriptor = server.state.blocks.getCurrentBlockDescriptor();
		if (blockDescriptor instanceof JavaScriptBlockDescriptor) {
			
			JavaScriptBlockDescriptor javaScriptBlockDescriptor = (JavaScriptBlockDescriptor) blockDescriptor;
			TextOutputCapturer scriptOutputCapturer = javaScriptBlockDescriptor.scriptOutputCapturer;
			scriptOutputCapturer.println(text);
		}
		else {
			Utility.throwException("server.messageMissingJavaScriptBlockDescriptor");
		}
	}
	
	/**
	 * Unbox object.
	 */
	@Override
	public Object unbox() {
		
		return server;
	}
	
	/**
	 * Box object.
	 * @param object
	 * @return
	 */
	public Object box(Object object) {
		
		if (object == null) {
			return null;
		}
		
		// Box collection items.
		if (object instanceof LinkedList) {
			
			LinkedList list = (LinkedList) object;
			for (int index = 0; index < list.size(); index++) {
				
				Object listItem = list.get(index);
				listItem = boxHelper(listItem);
				list.set(index, listItem);
			}
			return list;
		}
		
		return boxHelper(object);
	}

	/**
	 * Unbox interface.
	 * @author
	 *
	 */
	interface DoBox<T> {
		BoxedObject box(T object);
	}
	
	/**
	 * Box object helper.
	 * @param object
	 * @return
	 */
	private Object boxHelper(Object object) {
		
		class BoxOperation<T> {
			Class type;
			DoBox run;
			BoxOperation(Class type, DoBox<T> run) {
				this.type = type;
				this.run = run;
			}
			boolean matches(Object object) {
				return object.getClass().equals(type);
			}
		};
		
		// Define box operations.
		final BoxOperation [] boxOperations = {
				
				new BoxOperation<org.maclan.AreaRelation>(org.maclan.AreaRelation.class, 
						(relation)->{ return new AreaRelation(relation); }),
						
				new BoxOperation<org.maclan.Area>(org.maclan.Area.class, 
						(area)->{ return new Area(server, area); }),
						
				new BoxOperation<org.maclan.server.AreaServer>(org.maclan.server.AreaServer.class,
						(server)->{ try { return new AreaServer(server);} catch (Exception e) {return null;} }),
						
				new BoxOperation<org.maclan.EnumerationObj>(org.maclan.EnumerationObj.class,
						(enumeration)->{ return new EnumerationObj(enumeration); }),
						
				new BoxOperation<org.maclan.EnumerationValue>(org.maclan.EnumerationValue.class,
						(enumerationValue)->{ return new EnumerationValue(enumerationValue); }),
						
				new BoxOperation<org.maclan.MimeType>(org.maclan.MimeType.class,
						(mime)->{ return new MimeType(mime); }),
						
				new BoxOperation<org.maclan.server.RenderClass>(org.maclan.server.RenderClass.class,
						(mime)->{ return new RenderClass(server); }),
						
				new BoxOperation<org.maclan.server.Request>(org.maclan.server.Request.class,
						(request)->{ return new Request(server); }),
						
				new BoxOperation<org.maclan.Resource>(org.maclan.Resource.class,
						(resource)->{ try { return new Resource(server, resource); } catch (Exception e) {return null;} }),
						
				new BoxOperation<org.maclan.server.Response>(org.maclan.server.Response.class,
						(response)->{ return new Response(server); }),
						
				new BoxOperation<org.maclan.Slot>(org.maclan.Slot.class, 
						(slot)->{ return new Slot(server, slot); }),
		};
		
		// Find box operation and run it.
		for (BoxOperation boxOperation : boxOperations) {
			
			if (boxOperation.matches(object)) {
				return boxOperation.run.box(object);
			}
		}
		
		return object;
	}
	
	/**
	 * Box object.
	 * @param object
	 * @return
	 */
	public Object unbox(Object object) {
		
		if (object == null) {
			return null;
		}
		
		if (object instanceof LinkedList) {
			
			LinkedList list = (LinkedList) object;
			
			for (int index = 0; index < list.size(); index++) {
				
				Object listItem = list.get(index);
				listItem = unboxHelper(listItem);
				list.set(index, listItem);
			}
			return list;
		}
		
		return unboxHelper(object);
	}
	
	/**
	 * Box object helper.
	 * @param object
	 * @return
	 */
	private Object unboxHelper(Object object) {
		
		if (object instanceof BoxedObject) {
			return ((BoxedObject) object).unbox();
		}
		return object;
	}
	
	/**
	 * Convert to string.
	 */
	@Override
	public String toString() {
		
		return "[AreaServer object]";
	}
	
	/**
	 * Returns true value if a rendering is in progress.
	 * @return
	 */
	//graalvm @HostAccess.Export
	public boolean isRendering() {
		
		return server.isRendering();
	}
	
	/**
	 * Returns start language ID.
	 * @return
	 * @throws Exception 
	 */
	//graalvm @HostAccess.Export
	public long getStartLanguageId() throws Exception {
		
		return server.getStartLanguageId();
	}
	
	/**
	 * Returns this area.
	 * @param undefined
	 * @return
	 * @throws Exception 
	 */
	//graalvm @HostAccess.Export
	public Area area(Undefined undefined) throws Exception {
		
		return area();
	}
	
	
	/**
	 * Returns this area.
	 * @return
	 * @throws Exception 
	 */
	//graalvm @HostAccess.Export
	public Area area() throws Exception {
		
		return thisArea;
	}
	
	/**
	 * Returns area with given ID.
	 * @param areaId
	 * @return
	 * @throws Exception 
	 */
	//graalvm @HostAccess.Export
	public Area area(Long areaId) throws Exception {
		
		return new Area(server, server.getArea(areaId));
	}
	
	/**
	 * Returns area with given alias.
	 * @param alias
	 * @return
	 * @throws Exception
	 */
	//graalvm @HostAccess.Export
	public Area area(String alias) throws Exception {
		
		return new Area(server, server.getArea(alias));
	}
	
	/**
	 * Returns list of sub areas of this area.
	 * @param areaObject
	 * @return
	 * @throws Exception 
	 */
	//graalvm @HostAccess.Export
	public LinkedList<Area> subareas(Object areaObject) throws Exception {
		
		org.maclan.Area area = getAreaJsParameter(areaObject);
		server.loadSubAreasData(area);
		
		LinkedList<org.maclan.Area> middleSubareas = area.getSubareas();
		LinkedList<Area> subareas = new LinkedList<Area>();
		
		for (org.maclan.Area middleSubarea : middleSubareas) {
			subareas.add(new Area(server, middleSubarea));
		}
		
		return subareas;
	}

	/**
	 * Returns list of super areas of this area.
	 * @param areaObject
	 * @return
	 * @throws Exception 
	 */
	//graalvm @HostAccess.Export
	public LinkedList<Area> superareas(Object areaObject) throws Exception {
		
		org.maclan.Area area = getAreaJsParameter(areaObject);
		server.loadSuperAreasData(area);
		
		LinkedList<org.maclan.Area> middleSuperareas = area.getSuperareas();
		LinkedList<Area> superareas = new LinkedList<Area>();
		
		for (org.maclan.Area middleSuperarea : middleSuperareas) {
			superareas.add(new Area(server, middleSuperarea));
		}
		
		return superareas;
	}
	
	/**
	 * Input slot value using external provider.
	 * @param slot
	 * @throws Exception
	 */
	//graalvm @HostAccess.Export
	public void input(Slot slot) throws Exception {
		
		server.input((org.maclan.Slot) slot.unbox());
	}
	
	/**
	 * Watch external provider changes.
	 * @param slot
	 * @throws Exception
	 */
	//graalvm @HostAccess.Export
	public void watch(Slot slot) throws Exception {
		
		server.input((org.maclan.Slot) slot.unbox());
	}
	
	/**
	 * Stop watching all.
	 * @throws Exception
	 */
	//graalvm @HostAccess.Export
	public void stopWatchingAll() throws Exception {
		
		server.stopWatchingAll();
	}
	
	/**
	 * Get area slot.
	 * @param name
	 * @param secondParameter
	 * @param skipDefault
	 * @param parent 
	 * @param inheritanceLevel
	 * @return
	 * @throws Exception 
	 */
	//graalvm @HostAccess.Export
	public Object slot(String slotAlias, Object areaObjectOrType, Object skipDefaultOrArea, Object parent, Object inheritanceLevel) throws Exception {
		
		if (areaObjectOrType instanceof String) {
			
			// Delegate call.
			return slot(slotAlias, skipDefaultOrArea, (String) areaObjectOrType);
		}
		
		// Trim parameters.
		org.maclan.Area area = getAreaJsParameter(areaObjectOrType);
		boolean skipDefaultFlag = ScriptingEngine.getParameter(skipDefaultOrArea, false);
		boolean parentFlag = ScriptingEngine.getParameter(parent, false);
		Long inheritanceLevelValue = ScriptingEngine.getLongParameter(inheritanceLevel, null);
		
		org.maclan.Slot middleSlot = server.slot(slotAlias,	area, skipDefaultFlag, parentFlag, inheritanceLevelValue, false);
		
		if (middleSlot == null) {
			return null;
		}
		
		return new Slot(server, middleSlot);
	}
	
	/**
	 * Get area slot.
	 * @param slotAlias
	 * @param areaObject
	 * @param type - "d" skip default values, "v" return slot value, "i" or "iXXX" inherit (from XXX level), "p" only slots from parents
	 * @return
	 * @throws Exception
	 */
	//graalvm @HostAccess.Export
	public Object slot(String slotAlias, Object areaObject, String type) throws Exception {
		
		Object slotOrValue = server.slot(
				slotAlias,
				getAreaJsParameter(areaObject),
				type);
		
		if (slotOrValue == null) {
			return null;
		}
		
		return box(slotOrValue);
	}
	
	/**
	 * Returns true value if a slot is defined.
	 * @param slotAlias
	 * @param areaObject
	 * @param skipDefault
	 * @param parent
	 * @return
	 * @throws Exception 
	 */
	//graalvm @HostAccess.Export
	public boolean slotDefined(String slotAlias, Object areaObject, Boolean skipDefault, Boolean parent) throws Exception {
		
		return server.slotDefined(
				slotAlias,
				getAreaJsParameter(areaObject),
				skipDefault != null ? skipDefault : false, 
				parent != null ? parent : false);
	}
		
	/**
	 * Get resource with given ID.
	 * @param resourceId
	 * @return
	 * @throws Exception 
	 */
	//graalvm @HostAccess.Export
	public Resource resource(Long resourceId) throws Exception {
		
		if (resourceId == null) {
			return null;
		}
		
		org.maclan.Resource middleResource = server.resource(resourceId);
		return new Resource(server, middleResource);
	}
	
	/**
	 * Get area resource.
	 * @param name
	 * @param area
	 * @return
	 * @throws Exception 
	 */
	//graalvm @HostAccess.Export
	public Resource areaResource(String name, Object areaObject) throws Exception {
		
		org.maclan.Resource middleResource = server.resource(name, getAreaJsParameter(areaObject));
		if (middleResource == null) {
			return null;
		}
		
		return new Resource(server, middleResource);
	}
	
	/**
	 * Get resource URL.
	 * @param resourceName
	 * @param area
	 * @return
	 * @throws Exception
	 */
	//graalvm @HostAccess.Export
	public String getResourceUrl(String resourceName, Object areaObject) throws Exception {
		
		org.maclan.Area area = getAreaJsParameter(areaObject);
		return server.getResourceUrl(area.getId(), resourceName, null, false);
	}
	
	/**
	 * Get area JavaScript parameter.
	 * @param areaObject
	 * @return
	 */
	org.maclan.Area getAreaJsParameter(Object areaObject) throws Exception {
		
		if (areaObject instanceof Area) {
			return ((Area) areaObject).area;
		}
		else if (areaObject == null) {
			
			if (ScriptingEngine.is(ScriptingEngine.AvailableEngine.graalPolyglot)) {
				return server.state.area;
			}
			throw new Exception(Resources.getString("server.messageExpectedAreaParameterIsNull"));
		}
		// If an input area parameter is JavaScript undefined, use thisArea.
		else if (areaObject.getClass().getSimpleName().equals("Undefined")) {
			return server.state.area;
		}

		else {
			throw new Exception(String.format(
					Resources.getString("server.messageExpectingAreaParameter"),
					Area.class.getSimpleName(), areaObject.getClass().getSimpleName()));
		}
	}
	
	/**
	 * Get area URL.
	 * @param areaObject
	 * @param languageId
	 * @param versionId
	 * @return
	 * @throws Exception
	 */
	//graalvm @HostAccess.Export
	public String getAreaUrl(Object areaObject, Long languageId, Long versionId, Boolean localhost) throws Exception {
		
		return server.getAreaUrl(getAreaJsParameter(areaObject).getId(), languageId, versionId, localhost, null);
	}
	
	/**
	 * Create tree descriptor.
	 * @param rootArea
	 * @param relationName
	 * @return
	 * @throws Exception
	 */
	//graalvm @HostAccess.Export
	public String createTree(Object rootAreaObject, String relationName) throws Exception {

		return server.createTree(getAreaJsParameter(rootAreaObject), relationName).toString();
	}
	
	/**
	 * Get enumeration object.
	 * @param description
	 * @return
	 * @throws Exception
	 */
	//graalvm @HostAccess.Export
	public EnumerationObj getEnumeration(String description) throws Exception {
		
		org.maclan.EnumerationObj middleEnum = server.getEnumeration(description);
		if (middleEnum == null) {
			return null;
		}
		
		return new EnumerationObj(middleEnum);
	}
	
	/**
	 * Get enumeration value object.
	 * @param description
	 * @param valueName
	 * @return
	 * @throws Exception
	 */
	//graalvm @HostAccess.Export
	public EnumerationValue getEnumeration(String description, String valueName) throws Exception {
		
		org.maclan.EnumerationValue middleEnumValue = server.getEnumeration(description, valueName);
		if (middleEnumValue == null) {
			return null;
		}
		
		return new EnumerationValue(middleEnumValue);
	}
	
	/**
	 * Returns true value if a variable with given name is defined.
	 * @param variableName
	 * @return
	 */
	//graalvm @HostAccess.Export
	public boolean defined(String variableName) {
		
		return server.state.blocks.findVariable(variableName) != null;
	}
	
	/**
	 * Process text.
	 * @param textToProcess
	 * @param area
	 * @return
	 * @throws Exception
	 */
	//graalvm @HostAccess.Export
	public String process(String textToProcess, Object areaObject) throws Exception {
		
		org.maclan.Area area = getAreaJsParameter(areaObject);
		String resultText = server.processTextCloned(area, textToProcess);
		return resultText;
	}
	
	/**
	 * Process text.
	 * @param textToProcess
	 * @return
	 * @throws Exception
	 */
	//graalvm @HostAccess.Export
	public String process(String textToProcess) throws Exception {
		
		return server.processTextCloned(server.state.area, textToProcess);
	}
	
	/**
	 * Process text.
	 * @param textToProcess
	 * @param area
	 * @return
	 * @throws Exception
	 */
	//graalvm @HostAccess.Export
	public String processWithErrors(String textToProcess, Object areaObject) throws Exception {
		
		org.maclan.Area area = getAreaJsParameter(areaObject);
		String resultText = server.processTextClonedWithErrors(area, textToProcess);
		return resultText;
	}
	
	/**
	 * Process text.
	 * @param textToProcess
	 * @return
	 * @throws Exception
	 */
	//graalvm @HostAccess.Export
	public String processWithErrors(String textToProcess) throws Exception {
		
		return server.processTextClonedWithErrors(server.state.area, textToProcess);
	}
	/**
	 * Get variable value.
	 * @param variableName
	 * @return
	 * @throws Exception 
	 */
	//graalvm @HostAccess.Export
	public Object get(String variableName) throws Exception {
		
		// Get variable value, box it and return the value.
		Object value = server.state.blocks.findVariableValue(variableName);
		value = box(value);
		
		return value;
	}
	
	/**
	 * Set variable value.
	 * @param variableName
	 * @param variableValue
	 * @throws Exception
	 */
	//graalvm @HostAccess.Export
	public void set(String variableName, Object variableValue) throws Exception {
		
		// Unbox the value and set it.
		variableValue = unbox(variableValue);
		server.state.blocks.setVariable(variableName, variableValue);
	}
	
	/**
	 * Create server variable.
	 * @param variableName
	 * @param variableValue
	 * @throws Exception
	 */
	//graalvm @HostAccess.Export
	public void variable(String variableName, Object variableValue) throws Exception {
		
		// Unbox variable value and create variable that holds this value.
		variableValue = unbox(variableValue);
		server.state.blocks.createVariable(variableName, variableValue);
	}
	
	/**
	 * Get current language ID.
	 * @return
	 */
	//graalvm @HostAccess.Export
	public long getCurrentLangId() {
		
		Language language = server.getCurrentLanguage();
		if (language == null) {
			return 0;
		}
		
		return language.id;
	}
	
	/**
	 * Clear CSS rules.
	 */
	//graalvm @HostAccess.Export
	public void clearCssRules() {
		
		server.clearCssRules();
	}
	
	/**
	 * Insert CSS rules.
	 * @param area
	 * @param selector
	 * @param mediaSlotName
	 * @param importantSlotName
	 */
	//graalvm @HostAccess.Export
	public void insertCssRules(Area area, String selector, String mediaSlotName, String importantSlotName) throws Exception {
		
		server.insertCssRules(area.area, selector, mediaSlotName, importantSlotName);
	}
	
	/**
	 * Deprecated function name.
	 * @param selector
	 * @param mediaSlotName
	 * @param importantSlotName
	 * @throws Exception
	 */
	//graalvm @HostAccess.Export
	public void insertGlobalCss(String selector, String mediaSlotName, String importantSlotName) throws Exception {
		
		server.insertCssRules(selector, mediaSlotName, importantSlotName);
	}
	
	/**
	 * Get CSS rules.
	 * @return
	 */
	//graalvm @HostAccess.Export
	public String getCssRules() {
		
		String cssRules = server.getCssRules();
		return cssRules;
	}
	
	/**
	 * Deprecated function name.
	 * @return
	 */
	//graalvm @HostAccess.Export
	public String getGlobalCss() {
		
		return getCssRules();
	}
}
