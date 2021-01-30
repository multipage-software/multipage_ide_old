/*
 * Copyright 2010-2020 (C) vakol
 * 
 * Created on : 21-04-2020
 *
 */
package org.multipage.sync;

import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.http.HttpResponse;
import java.nio.CharBuffer;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.multipage.gui.HttpException;
import org.multipage.gui.Utility;
import org.multipage.util.Obj;
import org.multipage.util.Resources;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * 
 * @author user
 *
 */
public class AreaServerClient {
	
	/**
	 * Area server URL.
	 */
	private String url = null;
	
	/**
	 * Area server password
	 */
	private String password = "";
	
	/**
	 * Area server user.
	 */
	private String user = "";
	
	/**
	 * Access string
	 */
	private static String accessString = null;
	
	/**
	 * Access string format
	 */
	public static final String accessStringFormat = "%s;usr=%s;pwd=%s";
	
	/**
	 * Regular expressions.
	 */
	private static final Pattern accessStringPattern = Pattern.compile("^\\s*(.*?)\\s*;\\s*usr\\s*=\\s*(.*?)\\s*;\\s*pwd\\s*=\\s*(.*?)\\s*$");
	
	/**
	 * Connection error template.
	 */
	private static final String connectionErrorTemplate = Resources.getString("org.multipage.sync.messageConnectionErrorTemplate");
	
	/**
	 * Reload scheduler
	 */
	private static Timer reloadMenuScheduler;
	
	/**
	 * The popup menu object
	 */
	private static PopupMenu popupMenu;
	
	/**
	 * Set the access string.
	 * @param host
	 * @param port
	 * @param password 
	 * @param userDirectory 
	 */
	public static void setAccessString(String host, String user, String password) {
		
		accessString = String.format(accessStringFormat, host, user, password);
	}
	
	/**
	 * Area server client instance.
	 * @param popupMenu 
	 * @return
	 */
	public static AreaServerClient newInstance(PopupMenu popupMenu)
			throws Exception {
		
		// Remember the popup menu object
		AreaServerClient.popupMenu = popupMenu;
		
		// Check access string
		if (AreaServerClient.accessString == null) {
			Utility.throwException("org.multipage.sync.messageAccessStringNotSet");
		}
		
		AreaServerClient areaServerClient = new AreaServerClient();
		
		// Parse access string.
		Matcher matcher = accessStringPattern.matcher(accessString);
		if (matcher.matches() && matcher.groupCount() == 3) {
			areaServerClient.url = matcher.group(1);
			areaServerClient.user = matcher.group(2);
			areaServerClient.password = matcher.group(3);
		}
		else {
			areaServerClient.url = "http://localhost:8080";
		}
		
		// Create reload menu scheduler
		AreaServerClient.reloadMenuScheduler = new Timer(250, event -> {
			
			SwingUtilities.invokeLater(() -> {
				
				try {
					areaServerClient.loadMenu(true);
				}
				catch (Exception e) {
					
					// Display exception
					MessageDialog.show("org.multipage.sync.messageErrorLoadingMenu", e.getLocalizedMessage());
				}
			});
		});
		AreaServerClient.reloadMenuScheduler.setRepeats(false);
		
		// Return area server client
		return areaServerClient;
	}
	
	/**
	 * A helper function that gets URL of the area with given alias
	 * @param areaAlias - can be null for the home area of the Area Server
	 * @return
	 */
	public String getAreaUrl(String areaAlias) {
		
		String theUrl = this.url + (areaAlias == null ? "/?a" : "/?alias=" + areaAlias);
		return theUrl;
	}
	
	/**
	 * Load menu from area server.
	 * @param userInvoked 
	 * @param popup
	 */
	public void loadMenu(boolean userInvoked)
			throws Exception {
		
		// Create new thread
		Thread thread = new Thread(() -> {

			try {
				
				Obj<HttpResponse<InputStream>> response = new Obj<HttpResponse<InputStream>>();
				Optional<String> optionalException = null;
				
				// Remove all menu items
				SwingUtilities.invokeLater(() -> {
					popupMenu.removeAll();
				});
				
				// Get home area URL
				String homeAreaUrl = getAreaUrl(null);
				
				// Request menu descriptor from area server using the home area
				Document xml = apiRequestDocument(homeAreaUrl, "loadMenu", response);
				
				// Check the XML result
				if (xml == null) {
					
					// Add reload menu item
					addefaultMenuItems();
					return;
				}
				
				// Prepare XPATH prerequisites
				XPathFactory xpathFactory = XPathFactory.newInstance();
				XPath path = xpathFactory.newXPath();
				
				// Get optional Area Server response exception from the header field
				optionalException = response.ref.headers().firstValue("AreaServer-Exception");
				
				// If the response contain a document with an exception, throw the exception
				if (optionalException != null && optionalException.isPresent()) {
					
					// Create XPATH query
					XPathExpression exceptionXPath = path.compile("/Result/MenuException/text()");
					
					// Read exception body and unescape special characters
					String exceptionBody = (String) exceptionXPath.evaluate(xml, XPathConstants.STRING);
					exceptionBody = org.apache.commons.text.StringEscapeUtils.unescapeXml(exceptionBody);
					
					// Add URL info to the error message
					String errorMessage = String.format(connectionErrorTemplate, Utility.removeLastPunctuation(optionalException.get()), homeAreaUrl);
					
					// And throw the exception
					Utility.throwHttpException("org.multipage.gui.messageHttpAreaServerException", exceptionBody, errorMessage);
				}
				
				// Load menu from the XML result
				XPathExpression menuXPath = path.compile("/Result/*");
				NodeList nodes = (NodeList) menuXPath.evaluate(xml, XPathConstants.NODESET);
				int length = nodes.getLength();
				
				SwingUtilities.invokeLater(() -> {
	
					// Go through child nodes
					for (int index = 0; index < length; index++) {
						
						// Get request text
						Node node = nodes.item(index);
						Node name = node.getAttributes().getNamedItem("name");
						String request = node.getTextContent();
						
						// Create new menu item
						MenuItem menu = new MenuItem(name.getTextContent());
						popupMenu.add(menu);
						menu.addActionListener((event) -> {
							
							String answer = null;
							try {
								// Send request
								answer = sendSimpleRequest(request);
							
								// Display answer
								answer = answer.trim();
							}
							catch (Exception e) {
								answer = e.getLocalizedMessage();
							}
							
							// Display possible answer
							if (!answer.isEmpty()) {
								MessageDialog.showDialog(answer);
							}
							
						});
					}
					
					// If user is reloading the menu and there are no menu items, inform user about it
					if (userInvoked && length <= 0) {
						MessageDialog.show("org.multipage.sync.messageNoMenuItemsLoaded");
					}
					
					// Add default menu items.
					addefaultMenuItems();
				});
				
			}
			catch (Exception e) {
				
				// Add reload menu item
				addefaultMenuItems();
				
				Exception exception = null;
				String errorMessage = e.getLocalizedMessage();
				
				if (e instanceof HttpException) {
					
					// On HTTP exception
					HttpException httpException = (HttpException) e;
					String exceptionBody = httpException.getExceptionBody();
					exception = new HttpException(errorMessage, exceptionBody);
				}
				else {
					
					// On common exception
					exception = new Exception(errorMessage);
				}
				
				// Display exception
				MessageDialog.showException("org.multipage.sync.messageErrorLoadingMenu", exception);
			}
			
		});
		
		// Start thread
		thread.start();
	}

	/**
	 * Add reload menu item
	 */
	private void addefaultMenuItems() {
		
		SwingUtilities.invokeLater(() -> {
			
			// Add separator.
			int itemCount = popupMenu.getItemCount();
			if (itemCount > 0) {
				popupMenu.addSeparator();
			}
			
			// A maintenance sub menu.
			Menu menuMaintenance = new Menu(Resources.getString("org.multipage.sync.menuMaintenance"));
			popupMenu.add(menuMaintenance);
			
			// Update menu.
			Utility.addSubMenu(menuMaintenance, "org.multipage.sync.menuReloadMenu", e -> {
				reloadMenuScheduler.start();
			});
			
			// Reactivate GUI.
			Utility.addSubMenu(menuMaintenance, "org.multipage.sync.menuReactivateGui", e -> {
				SyncMain.reactivateGui();
			});
			
			// Menu item for program termination.
			final String confirmationMessage = String.format(
					Resources.getString("org.multipage.sync.messageConfirmQuit"), SyncMain.getMainApplicationTitle());
			
			if (SyncMain.isStandalone) {
				Utility.addSubMenu(menuMaintenance, "org.multipage.sync.menuQuitApplication", e -> {
					
					if (Utility.ask2Top(confirmationMessage)) {
						SyncMain.stop();
					}
				});
			}
			else {
				Utility.addPopupMenuItem(popupMenu, "org.multipage.sync.menuQuitApplication", e -> {
					
					if (Utility.ask2Top(confirmationMessage)) {
						SyncMain.closeMainApplication();
					}
				});
			}
		});
	}
	
	/**
	 * Send simple area server request.
	 * @param request
	 */
	private String sendSimpleRequest(String request) throws Exception {
		
		final int bufferLength = 2096;
		
		// Initialize objects
		Obj<String> resultString = new Obj<String>("");
		Obj<Exception> exception = new Obj<Exception>(null);
		
		// Create new thread
		Thread thread = new Thread (() -> {
			
			try {
				
				URL theUrl = new URL(url + "/" + request);
				URLConnection connection = theUrl.openConnection();
				
				String error = connection.getHeaderField("AreaServer-Exception");
				if (error != null) {
					
					error = Resources.getString("org.multipage.sync.messageConnectionError");
					Utility.throwException(String.format(error, url ));
				}
				
				InputStream inputStream = connection.getInputStream();
				InputStreamReader reader = new InputStreamReader(inputStream);
				
				CharBuffer target = CharBuffer.allocate(bufferLength);
				
				int count = 0;
				do {
					count = reader.read(target);
					
					resultString.ref += target;
				}
				while (count == bufferLength);
	
			}
			catch (Exception e) {
				String errorMessage = String.format(connectionErrorTemplate, Utility.removeLastPunctuation(e.getMessage()), url);
				exception.ref = new Exception(errorMessage);
			}
			
		});
		
		// Start the thread
		thread.start();
		thread.join();
		
		// Possibly throw exception
		if (exception.ref != null) {
			throw exception.ref;
		}
		
		return resultString.ref;
	}

	/**
	 * Request document using API method.
	 * @param url
	 * @param apiMethod
	 * @param response
	 * @param parameters
	 * @return
	 */
	private Document apiRequestDocument(String url, String apiMethod, Obj<HttpResponse<InputStream>> response, Object ... parameters)
			throws Exception {
		
		final long apiRequestTimoutMs = 15000;	// In milliseconds
		
		// Initialization
		InputStream inputStream = null;
		Integer statusCode = null;
		Document document = null;
		Exception exception = null;
		
		try {
			
			// Make HTTP request for the home area.
			inputStream = Utility.getHttpStream(url, apiRequestTimoutMs, response,
					// Set request headers
					"Accept-Charset", "UTF-8",
					"AreaServer-User", user,
					"AreaServer-Password", password,
					"AreaServer-API", apiMethod,
					"AreaServer-Action", "LoadMenu"
					);
			
			// Check the HTTP response status code
			statusCode = response.ref.statusCode();
			if (statusCode != 200) {
				Utility.throwException("org.multipage.gui.messageHttpConnectionError", statusCode);
			}
			
			// Parse XML document
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.parse(inputStream);
		}
		// Process exception
		catch (Exception e) {
			
			// Get error message, add URL info and create exception
			String errorMessage = String.format(connectionErrorTemplate, e.getLocalizedMessage(), url);
			exception = new Exception(errorMessage);
		}
		finally {
			Utility.close(inputStream);
		}
	
		// Throw possible exception
		if (exception != null) {
			throw exception;
		}
		
		// Return document
		return document;
	}
}
