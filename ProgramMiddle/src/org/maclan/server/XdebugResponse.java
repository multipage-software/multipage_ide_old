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
public class XdebugResponse {
	
	/**
	 * Xdebug packet constants.
	 */
	public static final String MULTIPAGE_IDE_KEY = "MULTIPAGE_IDE";
	public static final String APPLICATION_ID = "AREA_SERVER";
	public static final String LANGUAGE_NAME = "Maclan";
	public static final String PROTOCOL_VERSION = "1.0";
	
	/**
     * Xdebug NULL symbol.
     */
	public static final byte [] NULL_SYMBOL = new byte [] { 0 };
	public static final int NULL_SIZE = XdebugResponse.NULL_SYMBOL.length;
	
	/**
	 * Compiled XPATH expresions
	 */
	private static XPathExpression xpathRootNodeName = null;
	private static XPathExpression xpathInitIdeKeyName = null;
	private static XPathExpression xpathInitAppIdName = null;
	private static XPathExpression xpathLanguageName = null;
	private static XPathExpression xpathProtocolVersion = null;
	private static XPathExpression xpathDebuggedUri = null;
	private static XPathExpression xpathResponseTransactionId = null;
	private static XPathExpression xpathResponseCommandName = null;
	private static XPathExpression xpathResponseFeatureName = null;
	private static XPathExpression xpathResponseFeatureSupported = null;
	private static XPathExpression xpathResponseFeatureValue = null;	
	
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
			xpathResponseTransactionId = xpath.compile("/response/@transaction_id");
			xpathResponseCommandName = xpath.compile("/response/@command");
			xpathResponseFeatureName = xpath.compile("/response/@feature_name");
			xpathResponseFeatureSupported = xpath.compile("/response/@supported");
			xpathResponseFeatureValue = xpath.compile("/response/text()");
			
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
	public XdebugResponse(Document xml) {
		
		this.xml = xml;
	}

	/**
	 * Create INIT packet.
	 * @return
	 * @throws Exception 
	 */
	public static XdebugResponse createInitPacket(String areaServerStateLocator)
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
        
		// Create new packet.
		XdebugResponse initPacket = new XdebugResponse(xml);
		return initPacket;
	}
	
	
	/**
     * Create feature packet.
     * @param command
     * @param featureValue
     * @return
	 * @throws Exception 
     */
	public static XdebugResponse createFeaturePacket(XdebugCommand command, Object featureValue)
			throws Exception {
		
		// Get the feature name from the input command.
		String featureName = command.getArgument("-n");
		
		// Check if the feature name is supported.
		boolean supported = !(featureValue instanceof Exception);
		
		// Get transaction ID from the input command.
		int transactionId = command.transactionId;
        if (transactionId < 1) {
        	Utility.throwException("org.maclan.server.messageXdebugBadTransactionId", transactionId);
        }
        
        // Convert feature value to string.
        String featureValueString = supported ? String.valueOf(featureValue) : "";
        
        // Create new XML DOM document object.
        Document xml = newXmlDocument();
        Element rootElement = xml.createElement("response");
        rootElement.setAttribute("feature_name", featureName);
        rootElement.setAttribute("supported", supported ? "1" : "0");
        rootElement.setAttribute("transaction_id", String.valueOf(transactionId));
        rootElement.setTextContent(featureValueString);
        xml.appendChild(rootElement);
        
        // Create new packet.
		XdebugResponse featurePacket = new XdebugResponse(xml);
		return featurePacket;
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
	
	/**
	 * Get transaction ID.
	 * @return
	 */
	public int getTransactionId() throws Exception {
		
		try {
			// Get transaction ID from XML Document.
			String transactionIdText = (String) xpathResponseTransactionId.evaluate(xml, XPathConstants.STRING);
			int transactionId = Integer.parseInt(transactionIdText);
			return transactionId;
		}
		catch (Exception e) {
			Utility.throwException("org.maclan.server.messageBadXdebugTransactionId");
		}
		return -1;
	}
	
	/**
	 * Get Xdebug feature.
	 * @return
	 * @throws Exception 
	 */
	public XdebugFeature getFeature() throws Exception {
		
		// Get feature attributes.
		String commandName = (String) xpathResponseCommandName.evaluate(xml, XPathConstants.STRING);
		if ("feature_get".equals(commandName)) {
			Utility.throwException("org.maclan.server.messageBadXdebugFeatureCommandName");
		}
		String featureName = (String) xpathResponseFeatureName.evaluate(xml, XPathConstants.STRING);
		String supportedString = (String) xpathResponseFeatureSupported.evaluate(xml, XPathConstants.STRING);
		String featureValue = (String) xpathResponseFeatureValue.evaluate(xml, XPathConstants.STRING);
		
		// Create feature object from this packet.
		XdebugFeature feature = XdebugFeature.createFeature(featureName, supportedString, featureValue);
		return feature;
	}
	
	/**
	 * Create error packet.
	 * @param command
	 * @param exception
	 * @return
	 */
	public static XdebugResponse createErrorPacket(XdebugCommand command, Exception exception) {
		// TODO Auto-generated method stub
		return null;
	}
}