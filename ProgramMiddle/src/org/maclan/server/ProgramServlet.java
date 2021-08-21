/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.maclan.server;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;

import javax.servlet.*;
import javax.servlet.http.*;

import org.maclan.*;
import org.multipage.gui.CallbackNoArg;
import org.multipage.gui.Utility;
import org.multipage.util.*;

import php.java.bridge.Util;
import php.java.bridge.util.ILogger;
import php.java.bridge.util.Logger;
import php.java.servlet.ServletUtil;
import php.java.servlet.fastcgi.*;

/**
 * @author
 *
 */
public class ProgramServlet extends FastCGIServlet {

	/**
	 * Version 1.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * URL parameter for displaying home area
	 */
	public static final String displayHomeArea = "a";
	
	/**
	 * Login parameters.
	 */
	static Properties login;
	
	/**
	 * Set login.
	 * @param login
	 */
	public static void setLogin(Properties login) {
		
		ProgramServlet.login = login;
	}

	/**
	 * Listener that determines if PHP should be interpreted
	 */
	private static CallbackNoArg interpretPhp = null;

	/**
	 * Listener that determines if the servlet is used for programming web application
	 */
	private static CallbackNoArg developingWebApp = null;
	
	/**
	 * Listener that determines if the servlet is used for running web application
	 */
	private static CallbackNoArg runningWebApp = null;
	
	/**
	 * Listener that is invoked whenever some of the slots are updated from external code providers.
	 */
	private static Consumer<LinkedList<Long>> updatedSlots = null;

	/**
	 * Script data socket channel (nonblocking)
	 */
	private static ServerSocketChannel scriptDataSocketChannel;

	
	/**
	 * Sets listener that determines if the servlet has to interpret PHP
	 * @param listener
	 */
	public static void setInterpretPhpListener(CallbackNoArg listener) {
		
		interpretPhp = listener;
	}
	
	/**
	 * Sets listener that determines if the servlet is used for developing web application
	 * @param listener
	 */
	public static void setDevelopingWebAppListener(CallbackNoArg listener) {
		
		developingWebApp = listener;
	}
	
	/**
	 * Sets listener that determines if the servlet is used for running web application
	 * @param listener
	 */
	public static void setRunningWebAppListener(CallbackNoArg listener) {
		
		runningWebApp = listener;
	}
	
	/**
	 * Set listener that is invoked whenever some of the slots are updated from external code providers.
	 * @param slotIds
	 */
	public static void setUpdatedSlotsListener(Consumer<LinkedList<Long>> listener) {
		
		updatedSlots = listener;
	}
	
	/**
	 * Returns true value if the PHP should be interpreted
	 */
	public static boolean interpretPhp() {
		
		if (interpretPhp != null) {
			Object returned =  interpretPhp.run();
			
			if (returned instanceof Boolean) {
				return (Boolean) returned;
			}
		}
		
		return true;
	}
	
	/**
	 * Create listening script data socket
	 * @param port
	 */
	public static void createScriptDataSocket(int port) {
		
		try {
			scriptDataSocketChannel = ServerSocketChannel.open();
			scriptDataSocketChannel.socket().bind(new InetSocketAddress(port));
			scriptDataSocketChannel.configureBlocking(false);
		}
		catch (Exception e) {
		}
	}
	
	/**
	 * Temporary PHP file reference.
	 */
	private File temporaryPhpFile;

	/**
	 * PHP error
	 */
	private String pageError;

	/**
	 * Script extensions
	 */
	private String[] scriptExtensions;

	/**
	 * Servlet 
	 */
	private ServletConfig config;
	
	/**
	 * Area server reference.
	 */
	private AreaServer areaServer;
	
	/**
	 * Initialize servlet configuration
	 */
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		this.config = config;
		
		// Get accepted script file extensions
		String scriptExtensionsFromConfig = config.getServletContext().getInitParameter("script_extensions");
		if (scriptExtensionsFromConfig == null) {
			return;
		}
		scriptExtensions = scriptExtensionsFromConfig.split("[,;\\s]+");
		
		// Set bridge PHP directory.
		setPhpDirectoryForJavaBridge();
	}

	/**
	 * Returns true if the servlet is used for web application development
	 * @param request 
	 * @return
	 */
	private boolean developingWebApp(Request request) {
		
		boolean defaultValue = false;
		
		if (request.existsParameter(displayHomeArea) || request.isAreaServerRequest()) {
			return true;
		}
		
		if (developingWebApp == null) {
			return defaultValue;
		}
		
		Object returned = developingWebApp.run();
		if (returned instanceof Boolean) {
			return (Boolean) returned;
		}
		return defaultValue;
	}
	
	/**
	 * Returns true if the servlet is used only for running web application
	 * @param request 
	 */
	private boolean runWebApp(Request request) {
		
		boolean defaultValue = true;
		
		if (runningWebApp == null) {
			return defaultValue;
		}
		
		Object returned = runningWebApp.run();
		if (returned instanceof Boolean) {
			return (Boolean) returned;
		}
		return defaultValue;
	}
	
	/**
	 * Send updated slots to listener.
	 * @param slotIds
	 * @return
	 */
	private static void invokeUpdatedSlots(LinkedList<Long> slotIds) {
		
		if (updatedSlots != null) {
			updatedSlots.accept(slotIds);
		}
	}

	/**
	 * Handle GET, PUT, POST, DELETE
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		handleArea(req, res);
	}
	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
		handleArea(req, res);
	}
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
		handleArea(req, res);
	}
	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		handleArea(req, res);
	}
	
	/**
	 * Handle area
	 * @param _request
	 * @param _response
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void handleArea(final HttpServletRequest _request, final HttpServletResponse _response)
			throws ServletException, IOException {
		
		String uri = _request.getRequestURI();
		
		// Create private request object.
		org.maclan.server.Request request = new org.maclan.server.Request(_request, _request.getParameterMap());
		
		/***************************/
		/***** RUN AREA SERVER *****/
		/***************************/
		if (developingWebApp(request)) {
			
			// Create memory output stream.
			final ByteArrayOutputStream memoryOutputStream = new ByteArrayOutputStream();
			
			final Obj<Boolean> isProgramError = new Obj<Boolean>(false);
			
			/**
	    	 * If there is a request to clear server directory, do that and inform user.
	    	 */
			if (MiddleUtility.clearServer("", request, _response)) {
				return;
			}
	    	
			// Create private response object.
			Response response = new Response(_response, new ResponseAdapter() {
				@Override
				public void setContentType(String contentType) {
					_response.setContentType(contentType);
					_response.setHeader("Content-Type", contentType + "; charset=utf-8");
				}
				@Override
				public void setHeader(String headerName, String headerContent) {
					_response.setHeader(headerName, headerContent);
				}
				@Override
				public void setCharacterEncoding(String encoding) {
					_response.setCharacterEncoding(encoding);
				}
				@Override
				public OutputStream getOutputStream() {
					return memoryOutputStream;
				}
			});
			
			// Initialize flags
			boolean skipResponse = false;
			boolean processResponse = true;
			boolean isError = false;
			
			// Login to the database.
			if (login != null) {
				
				// Create middle layer.
				MiddleLight middle = MiddleUtility.newMiddleLightInstance();
				middle.enableCache(true);
				
				MiddleResult result = middle.login(login);
				if (result.isOK()) {
					
					// Set current language.
					LanguageServer.setCurrentLanguage(middle, request);
			
					boolean loaded = false;
					
					// Load resource.
					if (ResourceServer.loadResource(middle, request, response)) {
						loaded = true;
					}
					// Load language flag.
					if (!loaded && LanguageServer.loadFlag(middle, request, response)) {
						loaded = true;
					}
					
					// Load area user interface.
					if (!loaded) {
		
						// Create blocks stack.
						BlockDescriptorsStack blocks = new BlockDescriptorsStack();
						// Create analysis object.
						Analysis analysis = new Analysis();
						
						// Create area server.
						areaServer = new AreaServer();
						
						synchronized (areaServer) {
							
							// Initialize server state.
							areaServer.initServerState();
							
							// Set listener.
							areaServer.setListener(new AreaServerListener() {
								
								@Override
								public void onError(String message) {
									isProgramError.ref = true;
								}
	
								@Override
								public void updatedSlots(LinkedList<Long> slotIds) {
									invokeUpdatedSlots(slotIds);
								}
							});
							
							// Show possible text IDs.
							areaServer.setShowLocalizedTextIds(request.getParameter("l") != null);
							
							// Load page.
							processResponse = areaServer.loadAreaPage(middle, blocks, analysis, request, response);
						}
						
						// Set error flag
						isError = isProgramError.ref;
						
						// Set URI for running web application
						if (!isError) {
							
							Redirection redirection = areaServer.getRedirection();
							if (redirection.isActive()) {
								uri = "/" + redirection.getUri();
								
								// If this is an area server redirection (or rather forwarding), make it.
								if (!redirection.isDirect()) {
									
									RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(uri);
									if (dispatcher != null) {
										dispatcher.forward(_request, _response);
										
										skipResponse = true;
									}
									else {
										isError = true;
										memoryOutputStream.write(Resources.getString("server.messageErrorForwardingToNeUri").getBytes("UTF-8"));
									}
								}
							}
						}
					}
					
					// Logout from the database
					MiddleResult logoutResult = middle.logout(result);
					if (result.isOK()) {
						result = logoutResult;
					}
					
					// On error inform user
					if (result.isNotOK()) {
						isError = true;
						memoryOutputStream.write(result.getMessage().getBytes("UTF-8"));
					}
				}
			}
			else {
	
				// Output an error
				isError = true;
				memoryOutputStream.write(Resources.getString("server.messageNoLoginInformation").getBytes("UTF-8"));
			}
			
			// If the response must be skipped, do so.
			if (skipResponse) {
				
				_response.getOutputStream().close();
				return;
			}
			
			// If a PHP commands exist and should be interpreted, use the PHP/JavaBridge.
			if (processResponse && interpretPhp() && !isError && response.phpCommandExists()) {
				
				pageError = "";
				
				// Remove temporary PHP file
				temporaryPhpFile = createTemporaryPhpScript(memoryOutputStream);
				
				// Redirect system error stream
				StringBuilder error = new StringBuilder();
				
				// Set PHP script file for further processing
				setScriptFile(temporaryPhpFile);
				
				Logger.setLogger(new ILogger() {
					
					@Override
					public void warn(String msg) {
						
						synchronized (error) {
							if (msg != null && !msg.isEmpty()) {
								error.append(msg);
							}
						}
					}
					
					@Override
					public void printStackTrace(Throwable t) {
						
						synchronized (error) {
							String msg = t.toString();
							if (msg != null && !msg.isEmpty()) {
								error.append(msg);
							}
						}
					}
					
					@Override
					public void log(int level, String msg) {
						
						synchronized (error) {
							if (msg != null && !msg.isEmpty()) {
								error.append(msg);
							}
						}
					}
				});
				
				try {
					
					// Process PHP script
					handlePhp(_request, _response);
					
					// Send script
					sendScript(memoryOutputStream);
				}
				catch (Exception e) {
					pageError += e.getMessage() + " ";
				}
				
				// On error do output
				pageError += error.toString();
				
				if (!pageError.isEmpty()) {
					isError = true;
					_response.getOutputStream().write(String.format("<html><body><span style=\"color: red\">%s</span></body></html>", pageError).getBytes("UTF-8"));
				}
				
				// Remove the script
				removeTemporaryPhpScript();
			}
			// If no PHP commands
			else {
				
				// Output memory stream and close it
				byte [] output = memoryOutputStream.toByteArray();
				_response.getOutputStream().write(output);
			}
			
			// Close output streams
			memoryOutputStream.close();
			_response.getOutputStream().close();
		}
		else {
			
			/*******************************/
			/***** RUN EXTERNAL WEBAPP *****/
			/*******************************/
			if (runWebApp(request)) {
				
				pageError = "";
				StringBuilder error = new StringBuilder();
				
				// Set PHP script file
				try {
					String webInterface = MiddleUtility.getWebInterfaceDirectory();
					Path path = Paths.get(webInterface, uri);
					scriptFile = path.toFile();
					
					synchronized (scriptFile) {
						
						if (scriptFile.isDirectory()) {
							scriptFile = findIndexFile(scriptFile);
						}
						
						if (Utility.isFileExtension(scriptFile, scriptExtensions)) {
							
							// Set Java/PHP bridge script file.
							setScriptFile(scriptFile);
						
							// If an URI is specified send appropriate file using output stream
							Logger.setLogger(new ILogger() {
								@Override
								public void warn(String msg) {
									error.append(msg);
								}
								@Override
								public void printStackTrace(Throwable t) {
									error.append(t.toString());
								}
								@Override
								public void log(int level, String msg) {
									error.append(msg);
								}
							});
							
							// Handle PHP request using Java/PHP Bridge.
							handlePhp(_request, _response);
						}
						else {
							
							// Send file content. Do not interpret it.
							FileInputStream fileInputStream = null;
							FileChannel fileChannel = null;
							ServletOutputStream servletOutputStream = null;
							WritableByteChannel httpChannel = null;
							
							Exception exception = null;
							
							try {
								// Get file input channel.
								fileInputStream = new FileInputStream(scriptFile);
								fileChannel = fileInputStream.getChannel();
								
								// Get servlet output channel
								servletOutputStream = _response.getOutputStream();
								httpChannel = Channels.newChannel(servletOutputStream);
								
								// Transfer data from file input channel to HTTP output channel.
								fileChannel.transferTo(0, scriptFile.length(), httpChannel);
							}
							catch (Exception e) {
								exception = e;
								System.err.println(e.getLocalizedMessage());
							}
							finally {
								
								// Close resources.
								if (fileChannel != null) {
									try { fileChannel.close(); } catch (Exception e) {};
								}
								if (fileInputStream != null) {
									try { fileInputStream.close(); } catch (Exception e) {};
								}
								if (httpChannel != null) {
									try { httpChannel.close(); } catch (Exception e) {};
								}
								if (servletOutputStream != null) {
									try { servletOutputStream.close(); } catch (Exception e) {};
								}
							}
							
							// If an exception exists, send it to client via HTTP.
							if (exception != null) {
								pageError = exception.getLocalizedMessage();
							}
						}
					}
				}
				catch (Exception e) {
					System.err.println(e.getLocalizedMessage());
					pageError += e.getMessage() + " ";
				}
				
				// On error do output
				pageError += error.toString();
				
				// On error do output
				pageError += error;
				
				if (!pageError.isEmpty()) {
					_response.getOutputStream().write(String.format("<html><body><span style=\"color: red\">%s</span></body></html>", pageError).getBytes("UTF-8"));
				}
			}
			
			// Close output stream and exit
			_response.getOutputStream().close();
		}
	}

	/**
	 * Sends script to PHP engine
	 * @param scriptMemoryStream
	 */
	private void sendScript(ByteArrayOutputStream scriptMemoryStream) {
		
		final long timeoutMs = 200;
		final int numberOfAttempts = 10;

		// Make N attempts to accept incoming connection
		new Thread(() -> {
			try {
				int attempts = numberOfAttempts;
				
				do {
					SocketChannel socketChannel = scriptDataSocketChannel.accept();
					if (socketChannel != null) {
						
						// Send script data saved in memory stream
						byte [] scriptBytes = scriptMemoryStream.toByteArray();
						int scriptLength = scriptBytes.length;
						
						ByteBuffer scriptBuffer = ByteBuffer.allocate(1024);
						scriptBuffer.putInt(scriptLength);
						scriptBuffer.put(scriptBytes);
						
						socketChannel.write(scriptBuffer);
						socketChannel.close();
					}
					
					Thread.sleep(timeoutMs);
				}
				while (attempts-- > 0);
			}
			catch (Exception e) {	
			}
		});
	}

	/**
	 * 
	 */
	@Override
	protected void setupRequestVariables(HttpServletRequest req, Environment env) {
				
		// Do setup
		super.setupRequestVariables(req, env);
			
		// Set debugger
		//env.includedDebugger = true;
	}
	
	
	/////////////////////////////////////
	// PHP/Java Bridge FOSS contribution
	// VVVVV
	private File scriptFile = new File("");
	
	// @Override
	public void setScriptFile(File scriptFile) {
		
		this.scriptFile = scriptFile;
	}
	
	/**
	 * Finds index file
	 * @param directory
	 * @return
	 */
	private File findIndexFile(File directory) {
		
		String contextValue = config.getServletContext().getInitParameter("index_files");
		String [] indexFileNames = contextValue.split("[,;]*\\s+");
		
		// Try to find index file
		for (String indexFileName : indexFileNames) {
			File indexFile = new File(directory, indexFileName);
			if (indexFile.exists() && indexFile.isFile()) {
				return indexFile;
			}
		}
		
		// Return default value
		return new File(directory, "index.htm");
	}
	
	/**
	 * Set PHP script filename using SCRIPT_FILENAME property set in overridden method
	 * @throws IOException 
	 */
	@Override
	public void setScriptName(HttpServletRequest activeReq, Environment env)  {
		
		String scriptRealPath = null;
		try {
			if (scriptFile != null && scriptFile.exists()) {
				if (scriptFile.isFile()) {
					scriptRealPath = scriptFile.getCanonicalPath();
				}
			}
		} catch (IOException e) {
		}
		
		if (scriptRealPath == null) {
			scriptRealPath = ServletUtil.getRealPath(context, env.servletPath);
		}
		
		ServletUtil.getRealPath(context, env.servletPath);
		
		HashMap envp = env.environment;
		boolean includeDebugger = env.includedDebugger &&"1".equals(activeReq.getParameter("start_debug")) && null != activeReq.getParameter("debug_port") && null != activeReq.getParameter("original_url");
		boolean includeJavaInc = env.includedJava;
		if (includeDebugger && includeJavaInc) {
		    envp.put("X_JAVABRIDGE_INCLUDE_ONLY", "@");
		    envp.put("X_JAVABRIDGE_INCLUDE", scriptRealPath);
		    envp.put("SCRIPT_FILENAME", ServletUtil.getRealPath(context, "java/PHPDebugger.php"));
		} else if (includeDebugger && !includeJavaInc) {
		    envp.put("X_JAVABRIDGE_INCLUDE", scriptRealPath);
		    envp.put("SCRIPT_FILENAME", ServletUtil.getRealPath(context, "java/PHPDebugger.php"));
		} else if (!includeDebugger && includeJavaInc) {
		    envp.put("X_JAVABRIDGE_INCLUDE_ONLY", ServletUtil.getRealPath(context, "java/Java.inc"));
		    envp.put("X_JAVABRIDGE_INCLUDE", scriptRealPath);
		    envp.put("SCRIPT_FILENAME", ServletUtil.getRealPath(context, "java/PHPDebugger.php"));
		} else 
		{
		    envp.put("SCRIPT_FILENAME", scriptRealPath);
		}
	}
	
	/**
	 * Set PHP directory for PHP/Java bridge.
	 */
	private void setPhpDirectoryForJavaBridge() {
		
		String phpDirectory = MiddleUtility.getPhpDirectory();
		if (phpDirectory.isEmpty()) {
			return;
		}
		
		Util.DEFAULT_CGI_LOCATIONS = new String [] { phpDirectory + File.separator + "php-cgi.exe" };
	}
	
	// AAAA
	///////////////////////////


	/**
	 * Create temporary PHP script
	 * @param memoryOutputStream
	 * @throws IOException 
	 */
	protected File createTemporaryPhpScript(ByteArrayOutputStream memoryOutputStream) throws IOException {
		
		FileOutputStream outputStream = null;
		
		try {

			// Try to get requested area name.
			String requestedAreaName = "unknown_area";
			if (areaServer != null) {
				
				String areaName = areaServer.state.requestedArea.getDescription();
				long areaId = areaServer.state.requestedArea.getId();
				
				// Replace bad characters and set area name.
				areaName = Utility.replaceNonAsciiChars(areaName);
				areaName = areaName.replaceAll("[\\W]", "_");
				
				// Truncate the name.
				final int maxLength = 30;
				if (areaName.length() > maxLength) {
					areaName = areaName.substring(0, maxLength);
				}
				
				requestedAreaName = String.format("%s_%d", areaName, areaId);
			}
			
			// Create temporary PHP page
			File webInterfaceDirectory = new File(MiddleUtility.getWebInterfaceDirectory());
			
			File temporaryPhpFile = File.createTempFile(requestedAreaName + "-", ".php", webInterfaceDirectory);
			temporaryPhpFile.deleteOnExit();
			
			setScriptFile(temporaryPhpFile);
			
			// Write output data.
			outputStream = new FileOutputStream(temporaryPhpFile);
			
			if (memoryOutputStream != null) {
				outputStream.write(memoryOutputStream.toByteArray());
			}
			
			return temporaryPhpFile;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				if (outputStream != null) {
					outputStream.close();
				}
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return File.createTempFile("unknown", ".php");
	}

	/**
	 * Remove temporary PHP file.
	 */
	protected void removeTemporaryPhpScript() {
		
		// Try to remove temporary PHP file.
		if (temporaryPhpFile != null && temporaryPhpFile.exists()) {
			temporaryPhpFile.delete();
		}
	}

	/**
	 * Super handle delegate.
	 * @param _request
	 * @param _response
	 * @throws IOException 
	 * @throws ServletException 
	 */
	protected void handlePhp(HttpServletRequest _request,
			HttpServletResponse _response) throws ServletException, IOException {
		
		// Rewind input stream.
		InputStream inputStream = _request.getInputStream();
		if (inputStream instanceof CachedInputStream) {
			((CachedInputStream) inputStream).rewind();
		}
		
		// Handle PHP request.
		super.handle(_request, _response);
	}
}
