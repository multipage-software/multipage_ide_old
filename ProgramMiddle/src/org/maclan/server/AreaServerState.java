/*
 * Copyright 2010-2020 (C) vakol
 * 
 * Created on : 10-03-2020
 *
 */
package org.maclan.server;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeSet;

import org.maclan.Area;
import org.maclan.AreaVersion;
import org.maclan.Language;
import org.maclan.MiddleLight;
import org.multipage.util.Obj;
import org.multipage.util.j;

/**
 * @author user
 *
 */
public class AreaServerState {
	
	/**
	 * Default timeout value in milliseconds.
	 */
	public static final Long defaultTimeoutValue = 60000L;
	
	/**
	 * Constants used in the area server state properties.
	 */
	public static final int metaInfoFalse = 0;
	public static final int metaInfoTrue = 1;
	public static final int metaInfoTemporary = 2;
	
	/**
	 * State level index.
	 */
	public int stateLevelIndex = 0;
	
	/**
	 * Reference to parent state.
	 */
	public AreaServerState parentState = null;
	
	/**
	 * Process and thread information.
	 */
	public long processId = -1;
	public long threadId = -1;
	public String processName = null;
	public String threadName = null;

	/**
	 * Area server response timeout in milliseconds.
	 */
	public long responseTimeoutMilliseconds = defaultTimeoutValue;
	
	/**
	 * Response start time.
	 */
	public long responseStartTime = 0L;	
	
	/**
	 * Rendering flag.
	 */
	public boolean rendering = false;
	
	/**
	 * Rendering resources.
	 */
	public Map<Long, LinkedList<RenderedResource>> renderingResources;
	
	/**
	 * Common resource file names flag.
	 */
	public boolean commonResourceFileNames = false;

	/**
	 * Middle layer reference.
	 */
	public MiddleLight middle;
	
	/**
	 * Area not visible flag
	 */
	public boolean areaNotVisible = false;
	
	/**
	 *  Blocks descriptor stack.
	 */
	public BlockDescriptorsStack blocks;
	
	/**
	 * Request reference.
	 */
	public Request request;
	
	/**
	 * Response reference.
	 */
	public Response response;
	
	/**
	 * Languages.
	 */
	public LinkedList<Language> languages = new LinkedList<Language>();
	/**
	 * Rendering flags.
	 */
	public HashMap<Long, RenderedFlag> renderingFlags;

	/**
	 * Area text.
	 */
	public StringBuilder text;
	
	/**
	 * Supported encoding.
	 */
	public String encoding;
	
	/**
	 * CSS lookup table.
	 */
	public CssLookupTable cssLookupTable = new CssLookupTable();
	
	/**
	 * Rules for CSS.
	 */
	public AreasMediasRules cssRulesCache = new AreasMediasRules();
	
	/**
	 * Current level of tag processing.
	 */
	public long level;
	
	/**
	 * Area reference.
	 */
	public Area area;
	
	/**
	 * Requested area reference.
	 */
	public Area requestedArea;
	
	/**
	 * Found area.
	 */
	public Area startArea;
	
	/**
	 * Position.
	 */
	public int position;
	
	/**
	 * Tag start position.
	 */
	public int tagStartPosition;
	
	/**
	 * Current language.
	 */
	public Language currentLanguage;
	
	/**
	 * Analysis object reference.
	 */
	public Analysis analysis;
	
	/**
	 * Process properties flag.
	 */
	public boolean processProperties = false;
	
	/**
	 * Break point name.
	 */
	public String breakPointName = "";
	
	/**
	 * Show localized text IDs.
	 */
	public boolean showLocalizedTextIds = false;
	
	/**
	 * Area server listener.
	 */
	public AreaServerListener listener;
	
	/**
	 * Bookmark replace map.
	 */
	public HashMap<String, String> bookmarkReplacement = new HashMap<String, String>();
	
	/**
	 * Found include identifiers.
	 */
	public HashSet<String> foundIncludeIdentifiers = new HashSet<String>();
	
	/**
	 * Related area versions set.
	 */
	public HashSet<AreaVersion> relatedAreaVersions;
	
	/**
	 * Enable PHP flag.
	 */
	public Obj<Boolean> enablePhp = new Obj<Boolean>(true);
	
	/**
	 * JavaScript engine.
	 */
	public ScriptingEngine scriptingEngine;
	
	/**
	 * Tabulator used in indentation.
	 */
	public Obj<String> tabulator = new Obj<String>();
	
	/**
	 * CSS lookup table.
	 */
	public TreeSet<Long> unzippedResourceIds = new TreeSet<Long>();
	
	/**
	 * Web interface directory
	 */
	public Obj<String> webInterfaceDirectory = new Obj<String>("");
	
	/**
	 * Redirection object includes information about redirection after
	 * the area server returns control to servlet.
	 */
	public Redirection redirection = new Redirection();
	
	/**
	 * Resources render folder.
	 */
	public String resourcesRenderFolder = "";
	
	/**
	 * Slots updated by area server.
	 */
	public LinkedList<Long> updatedSlots = null;
	
	/**
	 * This flag determines if <meta http-equiv="Content-Type" content="text/html; charset=..."> 
	 * should be included into <head> section.
	 */
	public boolean useMetaCharset = true;
	
	/**
	 * Default version ID.
	 */
	protected long defaultVersionId = 0L;
	
	/**
	 * Current version ID.
	 */
	public long currentVersionId =  0L;
	
	/**
	 * Exception flag.
	 */
	public MaclanException exceptionThrown = null;
	
	/**
	 * Tray trayMenu result.
	 */
	protected TrayMenuResult trayMenu = new TrayMenuResult();
	
	/**
	 * New line characters
	 */
	public String newLine = "\n";
	
	/**
	 * Enable or disable meta information in resulting texts.
	 */
	public int enableMetaTags = AreaServerState.metaInfoFalse;
	
	/**
	 * Process META tags or not.
	 */
	public boolean processMetaTags = false;
	
	/**
	 * Debugged tag information.
	 */
	public DebugInfo debugInfo = null;
	
	/**
	 * Constructor.
	 */
	public AreaServerState() {
		
		// Save process ID and name.
    	processId = ProcessHandle.current().pid();
    	ProcessHandle processHandle = ProcessHandle.of(processId).orElseThrow();
    	processName = processHandle.info().command().orElseThrow();
    	
    	// Save thread ID and name.
        Thread currentThread = Thread.currentThread();
        threadId = currentThread.getId();
        threadName = currentThread.getName();
	}
	
	/**
	 * Clone this Area Server state.
	 * @param area
	 * @param textValue
	 * @return
	 */
	public AreaServerState cloneState(Area area, String textValue) {
		
		AreaServerState clonedState = new AreaServerState();
		
		clonedState.stateLevelIndex = stateLevelIndex + 1;
		
		clonedState.parentState = parentState;
		clonedState.responseTimeoutMilliseconds = responseTimeoutMilliseconds;
		clonedState.responseStartTime = responseStartTime;
		clonedState.rendering = rendering;
		clonedState.renderingFlags = renderingFlags;
		clonedState.renderingResources = renderingResources;
		clonedState.commonResourceFileNames = commonResourceFileNames;
		clonedState.middle = middle;
		clonedState.blocks = blocks;
		clonedState.request = request;
		clonedState.response = response;
		clonedState.languages = languages;
		clonedState.analysis = analysis;
		clonedState.text = new StringBuilder(textValue);
		clonedState.encoding = encoding;
		clonedState.level = level;
		clonedState.area = area;
		clonedState.requestedArea = requestedArea;
		clonedState.startArea = startArea;
		clonedState.position = 0;
		clonedState.tagStartPosition = 0;
		clonedState.currentLanguage = currentLanguage;
		clonedState.currentVersionId = currentVersionId;
		clonedState.processProperties = processProperties;
		clonedState.breakPointName = breakPointName;
		clonedState.showLocalizedTextIds = showLocalizedTextIds;
		clonedState.listener = listener;
		clonedState.bookmarkReplacement = bookmarkReplacement;
		clonedState.foundIncludeIdentifiers = foundIncludeIdentifiers;
		clonedState.relatedAreaVersions = relatedAreaVersions;
		clonedState.enablePhp = enablePhp;
		clonedState.scriptingEngine = scriptingEngine;
		clonedState.tabulator = tabulator;
		clonedState.cssRulesCache = cssRulesCache;
		clonedState.cssLookupTable = cssLookupTable;
		clonedState.unzippedResourceIds = unzippedResourceIds;
		clonedState.webInterfaceDirectory = webInterfaceDirectory;
		clonedState.redirection = redirection;
		clonedState.resourcesRenderFolder = resourcesRenderFolder;
		clonedState.updatedSlots = updatedSlots;
		clonedState.defaultVersionId = defaultVersionId;
		clonedState.trayMenu = trayMenu;
		clonedState.newLine = newLine;
		clonedState.enableMetaTags = enableMetaTags;
		
		if (debugInfo != null) {
			clonedState.debugInfo = debugInfo.cloneDebugInfo();
		}
		
		return clonedState;
	}	

	/**
	 * Update server state.
	 * @param server
	 */
	public void progateFromSubState(AreaServerState subState) {
		
		breakPointName = subState.breakPointName;
		exceptionThrown = subState.exceptionThrown;
		trayMenu = subState.trayMenu;
		
		if (debugInfo != null) {
			debugInfo.progateFromSubInfo(subState.debugInfo);
		}
	}
	
	/**
	 * Set exception thrown.
	 * @param exception
	 */
	public void setExceptionThrown(Exception exception) {
		
		if (exceptionThrown == null) {
			exceptionThrown = new MaclanException();
		}
		exceptionThrown.exception = exception;
	}
	
	/**
	 * Set debugger information.
	 * @param debugInfo
	 */
	public void setDebugInfo(DebugInfo debugInfo) {	
		this.debugInfo = debugInfo;
	}
	
	/**
	 * Get debug client.
	 * @return
	 */
	public XdebugClient getDebugClient() {
		
		if (debugInfo == null) {
			return null;
		}
		
		XdebugClient debugClient = debugInfo.getDebugClient();
		return debugClient;
	}
	
	/**
	 * Get debugger information.
	 */
	public DebugInfo getDebugInfo() {
		return debugInfo;
	}
}
