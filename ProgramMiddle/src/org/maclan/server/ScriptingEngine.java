/*
 * Copyright 2010-2020 (C) vakol
 * 
 * Created on : 26-03-2020
 *
 */
package org.maclan.server;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.SimpleBindings;
import javax.script.SimpleScriptContext;

//graalvm import org.graalvm.polyglot.Context;
//graalvm import org.graalvm.polyglot.Value;
import org.multipage.util.Resources;
import org.multipage.util.j;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;

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
//graalvm private Context graalPolyglot = null;
	
	/**
	 * Currently used.
	 */
	private boolean used = false;
	
	/**
	 * Try to get parameter.
	 * @param <T>
	 * @param parameter
	 * @param defaultValue 
	 * @return
	 */
	public static <T> T getParameter(Object parameter, T defaultValue) {
		
		// Check null value.
		if (parameter == null) {
			return null;
		}
		
		// Check undefined parameter sent by Nashorn.
		if (parameter.toString().equalsIgnoreCase("undefined")) {
			return defaultValue;
		}
		
		// Convert the input parameter.
		T typedParameter = (T) parameter;
		return typedParameter;
	}
	
	/**
	 * Try to get parameter of type Long.
	 * @param parameter
	 * @param object
	 * @return
	 */
	public static Long getLongParameter(Object parameter, Long defaultValue) {
		
		// Check null value.
		if (parameter == null) {
			return null;
		}
		
		// Check undefined parameter sent by Nashorn.
		if (parameter.toString().equalsIgnoreCase("undefined")) {
			return defaultValue;
		}
		
		// Convert the input parameter.
		Long typedParameter = null;
		if (parameter instanceof Integer) {
			typedParameter = Long.valueOf((int) parameter);
		}
		else {
			typedParameter = (Long) parameter;
		}
		return typedParameter;
	}
	
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
	 * Returns true if the scripting engine is currently used
	 * @return
	 */
	public boolean isUsed() {
		
		return this.used;
	}
	
	/**
	 * Set a flag that indicates whether this scripting engine is used
	 * @param used
	 */
	public void setUsed(boolean used) {
		
		this.used = used;
	}
	
	/**
	 * Create scripting engine.
	 */
	public void create() {
		
		switch (selectedEngine) {
		
		// On Nashorn engine
		case nashorn:
			
			// Create Nashorn.
			try {
				NashornScriptEngineFactory jsEngineManager = new NashornScriptEngineFactory();
				nashorn = jsEngineManager.getScriptEngine("nashorn");
			}
			catch (Throwable e) {
				e.printStackTrace();
			}
				
			// Check the engine.
			if (nashorn == null) {
				j.logMessage("org.maclan.server.messageCannotGetNashornScritpingEngine");
			}
			break;
		
		// On Graal Polyglot engine
		case graalPolyglot:
			
//graalvm			// Create Graal Polyglot.
//graalvm			graalPolyglot = org.graalvm.polyglot.Context.newBuilder("js")./*allowHostAccess(HostAccess.ALL).*/build();
//graalvm			
//graalvm			// Check the engine.
//graalvm			if (graalPolyglot == null) {
//graalvm				j.logMessage("org.maclan.server.messageCannotGetGraalPolyglotScritpingEngine");
//graalvm			}
//graalvm			break;
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
			
//graalvm			// Initialize Graal Polyglot context.
//graalvm			if (graalPolyglot != null) {
//graalvm				// No operation.
//graalvm				;
//graalvm			}
//graalvm			break;
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
				
				// Put area server objects.
				try {
					org.maclan.server.lang_elements.AreaServer areaServer = new org.maclan.server.lang_elements.AreaServer(server);
					
					bindings.put("server", areaServer);
					
					// New style bindings.
					bindings.put("_", areaServer);
					
					bindings.put("_thisArea", areaServer.thisArea);
					bindings.put("_startArea", areaServer.startArea);
					bindings.put("_requestedArea", areaServer.requestedArea);
					bindings.put("_homeArea", areaServer.homeArea);
					
					bindings.put("_level", areaServer.level);
					
					bindings.put("_request", areaServer.request);
					bindings.put("_response", areaServer.response);
					
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
			
//graalvm			// Check the engine.
//graalvm			if (graalPolyglot != null) {
//graalvm				
//graalvm				// Set Graal Polyglot
//graalvm				graalPolyglot.getBindings("js").putMember("server", new org.maclan.server.lang_elements.AreaServer(server));
//graalvm			}
//graalvm			break;
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
			
//graalvm			// Check the engine.
//graalvm			if (graalPolyglot != null) {
//graalvm				
//graalvm				// Run the script with prerequisites in the Graal Polyglot engine.
//graalvm				graalPolyglot.eval("js", prerequisitesScript);
//graalvm			}
//graalvm			break;
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
			
//graalvm			// Check the engine.
//graalvm			if (graalPolyglot != null) {
//graalvm				
//graalvm				// Run the script with Graal Polyglot engine.
//graalvm				Value valueObject = graalPolyglot.eval("js", scriptText);
//graalvm				value = valueObject.as(Object.class);
//graalvm			}
//graalvm			break;
		}
		
		// Return the resulting value.
		return value;
	}
}
