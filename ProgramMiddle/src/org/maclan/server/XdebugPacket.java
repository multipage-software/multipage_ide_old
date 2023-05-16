/*
 * Copyright 2010-2023 (C) vakol
 * 
 * Created on : 09-05-2023
 *
 */
package org.maclan.server;

import java.io.StringReader;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.text.StringEscapeUtils;
import org.multipage.gui.Utility;
import org.multipage.util.Obj;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

/**
 * Xdebug packet object.
 * @author vakol
 *
 */
public class XdebugPacket {
	
	/**
	 * Field for parsed debugger URI.
	 */
	public static class XdebugClientParameters {
		
		// Computer name.
		public String computer;
		
		// Process ID
		public String pid;
		
		// Thread ID.
		public String tid;
		
		// Area ID.
		public String aid;
		
		// Area Server state hash.
		public String statehash;
	}

	/**
	 * Xdebug packet constants.
	 */
	public static final String MULTIPAGE_IDE_KEY = "MULTIPAGE_IDE";
	public static final String APPLICATION_ID = "AREA_SERVER";
	public static final String LANGUAGE_NAME = "Maclan";
	public static final String PROTOCOL_VERSION = "1.0";
	
	/**
	 * Compiled XPATH expresions
	 */
	private static XPathExpression xpathRootNodeName = null;
	private static XPathExpression xpathInitIdeKeyName = null;
	private static XPathExpression xpathInitAppIdName = null;
	private static XPathExpression xpathLanguageName = null;
	private static XPathExpression xpathProtocolVersion = null;
	private static XPathExpression xpathDebuggedUri = null;
	
	/**
	 * Create regular expression patterns.
	 */
	private static Pattern regexUriParser = null;
	
	/**
	 * One and only XML serializer.
	 */
    private static LSSerializer lsSerializer = null;
    
	/**
	 * XML Document representing the packet.
	 */
	private Document xml = null;
	
    /**
     * Static constructor.
     */
    static {
		try {
			// XML DOM document serializer. Converts DOM document to text representation of the XML.
			DOMImplementationLS domImplementationLS = (DOMImplementationLS) DOMImplementationRegistry.newInstance().getDOMImplementation("LS");
			lsSerializer = domImplementationLS.createLSSerializer();
			
			// Prerequisites needed for XPath selector.
			XPathFactory xpathFactory = XPathFactory.newInstance();
			XPath xpath = xpathFactory.newXPath();
			
			// Compile XPATH expressions used in this packet class.
			xpathRootNodeName = xpath.compile("name(/*)");
			xpathInitIdeKeyName = xpath.compile("/init/@idekey");
			xpathInitAppIdName = xpath.compile("/init/@appid");
			xpathLanguageName = xpath.compile("/init/@language");
			xpathProtocolVersion = xpath.compile("/init/@protocol_version");
			xpathDebuggedUri = xpath.compile("/init/@fileuri");
			
			// Create regex patterns.
			regexUriParser = Pattern.compile("debug:\\/\\/(?<computer>[^\\/]*)\\/\\?pid=(?<pid>\\d*)&tid=(?<tid>\\d*)&aid=(?<aid>\\d*)&statehash=(?<statehash>\\d*)", Pattern.CASE_INSENSITIVE);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
    }
    
	/**
	 * Constructor.
	 * @param xml
	 */
	public XdebugPacket(Document xml) {
		
		this.xml = xml;
	}

	/**
	 * Create INIT packet.
	 * @return
	 * @throws Exception 
	 */
	public static XdebugPacket createInitPacket(String areaServerStateLocator)
			throws Exception {
		
		// Set packet content.
		Document xml = newXmlDocument();
		Element rootElement = xml.createElement("init");
		rootElement.setAttribute("appid", APPLICATION_ID);
		rootElement.setAttribute("idekey", MULTIPAGE_IDE_KEY);
		rootElement.setAttribute("session", "");
		rootElement.setAttribute("thread", "");
		rootElement.setAttribute("parent", "");
		rootElement.setAttribute("language", LANGUAGE_NAME);
		rootElement.setAttribute("protocol_version", PROTOCOL_VERSION);
		rootElement.setAttribute("fileuri", areaServerStateLocator);
		xml.appendChild(rootElement);
        
		// Create new p cket.
		XdebugPacket initPacket = new XdebugPacket(xml);
		return initPacket;
	}
	
	/**
	 * Check if the input packet is an INIT packet.
	 * @return
	 * @throws Exception 
	 */
	public boolean isInit() 
			throws Exception {
		
		// Check packet.
		if (xml == null) {
			return false;
		}
		
		// Try to get packet root node.
		String nodeName = (String) xpathRootNodeName.evaluate(xml, XPathConstants.STRING);
		boolean isInitPacket = "init".equalsIgnoreCase(nodeName);
		return isInitPacket;
	}
	
	/**
	 * Creates new XML DOM document object.
	 * @return
	 * @throws ParserConfigurationException 
	 */
	private static Document newXmlDocument() 
			throws Exception {
		
        // Create a new DocumentBuilderFactory.
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        // Use the factory to create a new DocumentBuilder.
        DocumentBuilder builder = factory.newDocumentBuilder();

        // Create a new Document object.
        Document document = builder.newDocument();
		return document;
	}
	
	/**
	 * Get packet text.
	 * @return
	 * @throws Exception 
	 */
	public String getText() 
			throws Exception {
		
        String text = lsSerializer.writeToString(xml);
        return text;
	}

	/**
	 * Get packet bytes.
	 * @return
	 * @throws Exception 
	 */
	public byte [] getBytes() 
			throws Exception {
		
        // Delegate the call to get string buffer with XML text representation.
		String text = getText();
		byte [] bytes = text.getBytes("UTF-8");
		return bytes;
	}
	
	/**
	 * Reads new packet from the input buffer.
	 * @param buffer
	 * @return
	 */
	public static XdebugPacket readPacket(ByteBuffer buffer)
			throws Exception {
		
        // Convert the ByteBuffer to a UTF-8 encoded XML string.
		Document xml = null;
        Exception exception = null;
        String xmlString = null;
        try {
            buffer.rewind(); // Reset the buffer's position to read from the beginning
            
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes); // Read the bytes from the buffer
            
            xmlString = new String(bytes, StandardCharsets.UTF_8);
        
            // Parse the XML string into a Document
	        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder builder = factory.newDocumentBuilder();
	        xml = builder.parse(new InputSource(new StringReader(xmlString)));
        }
        catch (Exception e) {
        	exception = e;
        }
        
        // Use the resulting Document
        if (xml == null) {
        	// Throw exception.
        	Utility.throwException("org.maclan.server.messageCannotReceiveXdebugPacket", exception.getLocalizedMessage());
        }
        
        // Create new packet object.
    	XdebugPacket newPacket = new XdebugPacket(xml);
    	return newPacket;
    }
	
	/**
	 * Check if the IDE key in current packet macthes the input value.
	 * @param ideKey
	 * @throws Exception 
	 */
	public boolean checkIdeKey(String ideKey, Obj<String> packetIdeKey)
			throws Exception {
		
		// Check input value.
		if (ideKey == null) {
			packetIdeKey.ref = "null";
			return false;
		}
		
		// Get packet IDE key.
		packetIdeKey.ref = (String) xpathInitIdeKeyName.evaluate(xml, XPathConstants.STRING);
		boolean matches = ideKey.equalsIgnoreCase(packetIdeKey.ref);
		return matches;
	}
	
	/**
	 * Check if the application ID in current packet macthes the input value.
	 * @param appId
	 * @param foundAppId
	 * @return
	 * @throws Exception 
	 */
	public boolean checkAppId(String appId, Obj<String> foundAppId)
			throws Exception {
		
		// Check input value.
		if (appId == null) {
			foundAppId.ref = "null";
			return false;
		}
		
		// Get packet IDE key.
		foundAppId.ref = (String) xpathInitAppIdName.evaluate(xml, XPathConstants.STRING);
		boolean matches = appId.equalsIgnoreCase(foundAppId.ref);
		return matches;
	}
	
	/**
	 * Check if the debugged language in current packet macthes the input value.
	 * @param languageName
	 * @param foundLanguageName
	 * @return
	 */
	public boolean checkLanguage(String languageName, Obj<String> foundLanguageName)
			throws Exception {
		
		// Check input value.
		if (languageName == null) {
			foundLanguageName.ref = "null";
			return false;
		}
		
		// Get language name from the packet data.
		foundLanguageName.ref = (String) xpathLanguageName.evaluate(xml, XPathConstants.STRING);
		boolean matches = languageName.equalsIgnoreCase(foundLanguageName.ref);
		return matches;		
	}
	
	/**
	 * Check if protocol version in current packet macthes the input value.
	 * @param protocolVersion
	 * @param foundProtocolVersion
	 * @return
	 */
	public boolean checkProtocolVersion(String protocolVersion, Obj<String> foundProtocolVersion)
			throws Exception {
		
		// Check input value.
		if (protocolVersion == null) {
			foundProtocolVersion.ref = "null";
			return false;
		}
		
		// Get protocol version from the packet data.
		foundProtocolVersion.ref = (String) xpathProtocolVersion.evaluate(xml, XPathConstants.STRING);
		boolean matches = protocolVersion.equalsIgnoreCase(foundProtocolVersion.ref);
		return matches;	
	}
	
	/**
	 * Get debugged process URI.
	 * @return
	 */
	public String GetDebuggedUri()
		throws Exception {
		
		// Try to get URI from current packet.
		String debuggedUri = (String) xpathDebuggedUri.evaluate(xml, XPathConstants.STRING);
		debuggedUri = URLDecoder.decode(debuggedUri, "UTF-8");
		debuggedUri = StringEscapeUtils.unescapeHtml4(debuggedUri);
		return debuggedUri;
	}
	
	/**
	 * Parse debugger URI.
	 * @param debuggerUri
	 * @return
	 */
	public static XdebugClientParameters parseDebuggedUri(String debuggerUri) 
			throws Exception {
		
		// Create URI matcher with regular expression.
		Matcher matcher = regexUriParser.matcher(debuggerUri);
		
		boolean success = matcher.find();
		int groupCount = matcher.groupCount();
		if (success && groupCount == 5) {
			
			XdebugClientParameters parsedUri = new XdebugClientParameters();
		    parsedUri.computer = matcher.group("computer");
		    parsedUri.pid = matcher.group("pid");
		    parsedUri.tid = matcher.group("tid");
		    parsedUri.aid = matcher.group("aid");
		    parsedUri.statehash = matcher.group("statehash");
		    return parsedUri;
		}
		
		Utility.throwException("org.maclan.server.messageBadDebuggerUri", debuggerUri);
		return null;
	}
}