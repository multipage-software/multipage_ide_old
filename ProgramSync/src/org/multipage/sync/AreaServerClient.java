/*
 * Copyright 2010-2020 (C) Vaclav Kolarcik
 * 
 * Created on : 21-04-2020
 *
 */
package org.multipage.sync;

import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.CharBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

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
	 * Regular expressions.
	 */
	private static final Pattern accessStringPattern = Pattern.compile("^\\s*(.*?)\\s*;\\s*usr\\s*=\\s*(.*?)\\s*;\\s*pwd\\s*=\\s*(.*?)\\s*$");

	/**
	 * Area server client instance.
	 * @return
	 */
	public static AreaServerClient newInstance(String accessString)
			throws Exception {
		
		AreaServerClient client = new AreaServerClient();
		
		// Parse access string.
		Matcher matcher = accessStringPattern.matcher(accessString);
		if (matcher.matches() && matcher.groupCount() == 3) {
			client.url = matcher.group(1);
			client.user = matcher.group(2);
			client.password = matcher.group(3);
		}
		else {
			client.url = "http://localhost:8080";
		}
		
		return client;
	}
	
	/**
	 * Load menu from area server.
	 * @param popup
	 */
	public void loadMenu(PopupMenu popup)
			throws Exception {
		
		try {
			Document xml = invoke("loadMenu");
			XPathFactory xpathFactory = XPathFactory.newInstance();
			XPath path = xpathFactory.newXPath();
			
			XPathExpression expression = path.compile("/Result/*");
			NodeList nodes = (NodeList) expression.evaluate(xml, XPathConstants.NODESET);
			int length = nodes.getLength();
			
			for (int index = 0; index < length; index++) {
				Node node = nodes.item(index);
				
				Node name = node.getAttributes().getNamedItem("name");
				String request = node.getTextContent();
				
				MenuItem menu = new MenuItem(name.getTextContent());
				popup.add(menu);
				menu.addActionListener((e) -> {
					
					sendRequest(request);
				});
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Send area server request.
	 * @param request
	 */
	private void sendRequest(String request) {
		
		final int bufferLength = 2096;
		try {
			URL theUrl = new URL(url + "/" + request);
			URLConnection connection = theUrl.openConnection();
			
			InputStream inputStream = connection.getInputStream();
			InputStreamReader reader = new InputStreamReader(inputStream);
			
			CharBuffer target = CharBuffer.allocate(bufferLength);
			
			int count = 0;
			do {
				count = reader.read(target);
			}
			while (count == bufferLength);
		}
		catch (Exception e) {
		}
	}

	/**
	 * Invoke area server function.
	 * @param method
	 * @param parameters
	 * @return
	 */
	@SuppressWarnings("unused")
	private Document invoke(String method, Object ... parameters)
			throws Exception {
		
		// Make request.
		URL theUrl = new URL(url + "/?a");
		HttpURLConnection connection = (HttpURLConnection) theUrl.openConnection();
		
		connection.setRequestMethod("GET");
		
		connection.setRequestProperty("Accept-Charset", "UTF-8");
		connection.setRequestProperty("AreaServer-User", user );
		connection.setRequestProperty("AreaServer-Password", password);
		connection.setRequestProperty("AreaServer-API", method);
		
		Exception exception = null;
		InputStream inputStream = null;
		
		try {
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			
			inputStream = connection.getInputStream();
			
			Document document = builder.parse(inputStream);
			return document;
		}
		catch (Exception e) {
			
			exception = e;
		}
		finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			}
			catch (Exception e) {
			}
		}
		if (exception != null) {
			throw exception;
		}
		
		return null;
	}
}
