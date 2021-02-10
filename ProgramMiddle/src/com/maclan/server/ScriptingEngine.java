/*
 * Copyright 2010-2021 (C) vakol (see attached LICENSE file for additional info) 2010-2020 (C) sechance
 * 
 * Created on : 26-03-2020
 *
 */
package com.maclan.server;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;
import javax.script.SimpleScriptContext;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.multipage.util.Resources;
import org.multipage.util.j;

/**
 * @author user
 *
 */
public class ScriptingEngine {
	
	/**
	 * Selected scripting engine switch.
	 */
	public static final AvailableEngine selectedEngine = AvailableEngine.nashorn;
	
	/**
	 * Enumerate scripting engines.
	 */
	public enum AvailableEngine { nashorn, graalPolyglot };
	
	/**
	 * Signals signalReleased engine.
	 */
	public static boolean signalReleased = true;
	
	/**
	 * Script engine instance.
	 */
	private ScriptEngine nashorn = null;
	
	/**
	 * Graal Polyglot instance.
	 */
	private Context graalPolyglot = null;
	
	/**
	 * Currently used.
	 */
	public boolean used = false;
	
	/**
	 * CHeck the current engiine.
	 * @param scriptingEngine
	 * @return
	 */
	public static boolean is(AvailableEngine scriptingEngine) {
		
		boolean isCurrent = ScriptingEngine.selectedEngine.equals(scriptingEngine);
		return isCurrent;
	}
	
	/**
	 * Create scripting engine.
	 */
	public void create() {
		
		switch (selectedEngine) {
		
		// On Nashorn engine
		case nashorn:
			
			// Create Nashorn.
			ScriptEngineManager jsEngineManager = new javax.script.ScriptEngineManager();
			nashorn = jsEngineManager.getEngineByName("nashorn");
			
			// Check the engine.
			if (nashorn == null) {
				j.logMessage("com.maclan.server.messageCannotGetNashornScritpingEngine");
			}
			break;
		
		// On Graal Polyglot engine
		case graalPolyglot:
			
			// Create Graal Polyglot.
			graalPolyglot = org.graalvm.polyglot.Context.newBuilder("js")./*allowHostAccess(HostAccess.ALL).*/build();
			
			// Check the engine.
			if (graalPolyglot == null) {
				j.logMessage("com.maclan.server.messageCannotGetGraalPolyglotScritpingEngine");
			}
			break;
		}
	}
	
	/**
	 * Initialize scripting engine.
	 * @param scriptingEngine
	 */
	public void initialize() {
		
		switch (selectedEngine) {
		
		// On Nashorn engine
		case nashorn:
			
			// Initialize Nashorn context.
			if (nashorn != null) {
				nashorn.setContext(new SimpleScriptContext());
			}
			break;
		
			// On Graal Polyglot engine
		case graalPolyglot:
			
			// Initialize Graal Polyglot context.
			if (graalPolyglot != null) {
				// No operation.
				;
			}
			break;
		}
	}
	
	/**
	 * Bind area server with this scripting engine.
	 * @param server 
	 * @throws Exception 
	 */
	public void bindAreaServer(AreaServer server)
		throws Exception {
		
		switch (selectedEngine) {
		
		// On Nashorn engine
		case nashorn:
			
			// Check the engine.
			if (nashorn != null) {
				
				Bindings bindings = new SimpleBindings();
				
				// Put root object named server.
				try {
					bindings.put("server", new com.maclan.server.lang_elements.AreaServer(server));
				}
				catch (Exception e) {
					
					String message = String.format(
							Resources.getString("server.messagePutVariablesIntoJavaScriptError"), e.getLocalizedMessage());
					throw new Exception(message);
				}
				
				// Set Nashorn bindings.
				nashorn.setBindings(bindings, ScriptContext.GLOBAL_SCOPE);
			}
			break;
		
		// On Graal Polyglot engine
		case graalPolyglot:
			
			// Check the engine.
			if (graalPolyglot != null) {
				
				// Set Graal Polyglot
				graalPolyglot.getBindings("js").putMember("server", new com.maclan.server.lang_elements.AreaServer(server));
			}
			break;
		}
	}
	
	/**
	 * Prepare prerequisites.
	 * @param prerequisitesScript
	 */
	public void preparePrerequisites(String prerequisitesScript)
		throws Exception {
		
		switch (selectedEngine) {
		
		// On Nashorn engine
		case nashorn:
			
			// Check the engine.
			if (nashorn != null) {
				
				// Run the script with prerequisites in the Nashorn engine.
				nashorn.eval(prerequisitesScript);
			}
			break;
		
		// On Graal Polyglot engine
		case graalPolyglot:
			
			// Check the engine.
			if (graalPolyglot != null) {
				
				// Run the script with prerequisites in the Graal Polyglot engine.
				graalPolyglot.eval("js", prerequisitesScript);
			}
			break;
		}
	}
	
	/**
	 * Evaluate the input script text with a scripting engine and return resulting value.
	 * @param scriptingEngine
	 * @param scriptText
	 * @return 
	 * @throws Exception
	 */
	public Object eval(String scriptText)
		throws Exception {
		
		Object value = null;
		
		switch (selectedEngine) {
		
		// On Nashorn engine
		case nashorn:
			
			// Check the engine.
			if (nashorn != null) {
				
				// Run the script with Nashorn engine.
				value = nashorn.eval(scriptText);
			}
			break;
		
			// On Graal Polyglot engine
		case graalPolyglot:
			
			// Check the engine.
			if (graalPolyglot != null) {
				
				// Run the script with Graal Polyglot engine.
				Value valueObject = graalPolyglot.eval("js", scriptText);
				value = valueObject.as(Object.class);
			}
			break;
		}
		
		// Return the resulting value.
		return value;
	}
}
