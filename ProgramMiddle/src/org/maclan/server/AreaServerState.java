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
import org.multipage.util.Lock;
import org.multipage.util.Obj;

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
	 * Xdebug client.
	 */
	public XdebugClient debugClient = null;
	
	/**
	 * Xdebug opration.
	 */
	public XdebugOperation debuggerOperation = XdebugOperation.no_operation;

	/**
	 * Debugger lock.
	 */
	public Lock debuggerLock = null;
	
	/**
	 * Debugged code descriptor.
	 */
	public DebuggedCodeDescriptor debuggedCodeDescriptor = null;
	
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
	 * Update server state.
	 * @param server
	 */
	public void progateFromSubstate(AreaServerState subState) {
		
		breakPointName = subState.breakPointName;
		exceptionThrown = subState.exceptionThrown;
		trayMenu = subState.trayMenu;
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
}
