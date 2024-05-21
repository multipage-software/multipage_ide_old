/*
 * Copyright 2010-2024 (C) vakol
 * 
 * Created on : 09-05-2023
 *
 */
package org.maclan.server;

import java.io.StringReader;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;
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
import org.multipage.util.Obj;
import org.multipage.util.Resources;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

/**
 * Xdebug client response.
 * @author vakol
 *
 */
public class XdebugClientResponse {
	
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
	public static final int NULL_SIZE = XdebugClientResponse.NULL_SYMBOL.length;
	
	/**
	 * Xdebug type names.
	 */
	private static final String INTEGER_TYPE_NAME = "int";
	private static final String STRING_TYPE_NAME = "string";
	
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
	private static XPathExpression xpathSuccessResponse = null;
	private static XPathExpression xpathSourceResponse = null;
	private static XPathExpression xpathEvalResponse = null;
	private static XPathExpression xpathStateSlotId = null;
	private static XPathExpression xpathStateResourceId = null;
	private static XPathExpression xpathStateProcessId = null;
	private static XPathExpression xpathStateThreadId = null;
	private static XPathExpression xpathStateProcessName = null;
	private static XPathExpression xpathStateThreadName = null;
	private static XPathExpression xpathErrorCode = null;
	private static XPathExpression xpathErrorMessage = null;
	private static XPathExpression xpathContextsResponse = null;
	
	/**
	 * Regular expression.
	 */
	private static Pattern regexUriParser = null;
	
	/**
	 * XML serializer.
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
			xpathSuccessResponse = xpath.compile("/response/@success");
			xpathSourceResponse = xpath.compile("/response/text()");
			xpathEvalResponse = xpath.compile("/response/property/text()");
			xpathStateSlotId = xpath.compile("/response/property[@classname='AreaServerState']/property[@classname='DebuggedCodeDescriptor']/property[@classname='TagsSource']/property[@name='slotId']/text()");
			xpathStateResourceId = xpath.compile("/response/property[@classname='AreaServerState']/property[@classname='DebuggedCodeDescriptor']/property[@classname='TagsSource']/property[@name='resourceId']/text()");
			xpathStateProcessId = xpath.compile("/response/property[@classname='AreaServerState']/property[@name='processId']/text()");
			xpathStateThreadId = xpath.compile("/response/property[@classname='AreaServerState']/property[@name='threadId']/text()");
			xpathStateProcessName = xpath.compile("/response/property[@classname='AreaServerState']/property[@name='processName']/text()");
			xpathStateThreadName = xpath.compile("/response/property[@classname='AreaServerState']/property[@name='threadName']/text()");
			xpathErrorCode = xpath.compile("/response/error/@code");
			xpathErrorMessage = xpath.compile("/response/error/message/text()");
			xpathContextsResponse = xpath.compile("/response/*");
			
			// Create regex patterns.
			regexUriParser = Pattern.compile("debug:\\/\\/(?<computer>[^\\/]*)\\/\\?pid=(?<pid>\\d*)&tid=(?<tid>\\d*)&aid=(?<aid>\\d*)&statehash=(?<statehash>\\d*)", Pattern.CASE_INSENSITIVE);
		}
		catch (Exception e) {
			onException(e);
		}
    }
    
	/**
	 * Constructor.
	 * @param xml
	 */
	public XdebugClientResponse(Document xml) {
		
		this.xml = xml;
	}

	/**
	 * Create INIT packet.
	 * @return
	 * @throws Exception 
	 */
	public static XdebugClientResponse createInitPacket(String areaServerStateLocator)
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
		XdebugClientResponse initPacket = new XdebugClientResponse(xml);
		return initPacket;
	}
	
	/**
	 * Create error packet.
	 * @param command
	 * @param xdebugError
	 * @param exception 
	 * @return
	 * @throws Exception 
	 */
	public static XdebugClientResponse createErrorPacket(XdebugCommand command, XdebugError xdebugError, Exception exception)
			throws Exception {
		
		// Get Xdebug command name.
		String commandName = command.getName();
		
		// Get transaction ID from the input command.
		int transactionId = command.getTransactionId();
        if (transactionId < 1) {
        	onThrownException("org.maclan.server.messageXdebugBadTransactionId", transactionId);
        }
        
        // Get error code and error message.
        int errorCode = xdebugError.getErrorCode();
        String errorCodeText = String.valueOf(errorCode);
        
        // Create new XML DOM document object.
        Document xml = newXmlDocument();
        Element rootElement = xml.createElement("response");
        rootElement.setAttribute("command", commandName);
        rootElement.setAttribute("transaction_id", String.valueOf(transactionId));
        xml.appendChild(rootElement);
        Element errorElement = xml.createElement("error");
        errorElement.setAttribute("code", errorCodeText);
        rootElement.appendChild(errorElement);
        
        if (exception != null) {
        	Element messageElement = xml.createElement("message");
        	String errorText = exception.getLocalizedMessage();
        	messageElement.setTextContent(errorText);
        	errorElement.appendChild(messageElement);
        }

        // Create new packet.
		XdebugClientResponse errorPacket = new XdebugClientResponse(xml);
        return errorPacket;
	}

	/**
     * Create feature packet.
     * @param command
     * @param featureValue
     * @return
	 * @throws Exception 
     */
	public static XdebugClientResponse createGetFeatureResult(XdebugCommand command, Object featureValue)
			throws Exception {
		
		// Get the feature name from the input command.
		String featureName = command.getArgument("-n");
		
		// Check if the feature name is supported.
		boolean supported = !(featureValue instanceof Exception);
		
		// Get transaction ID from the input command.
		int transactionId = command.getTransactionId();
        if (transactionId < 1) {
        	onThrownException("org.maclan.server.messageXdebugBadTransactionId", transactionId);
        }
        
        // Convert feature value to string.
        String featureValueString = supported ? String.valueOf(featureValue) : "";
        
        // Create new XML DOM document object.
        Document xml = newXmlDocument();
        Element rootElement = xml.createElement("response");
        rootElement.setAttribute("command", "feature_get");
        rootElement.setAttribute("feature_name", featureName);
        rootElement.setAttribute("supported", supported ? "1" : "0");
        rootElement.setAttribute("transaction_id", String.valueOf(transactionId));
        rootElement.setTextContent(featureValueString);
        xml.appendChild(rootElement);
        
        // Create and return new packet.
		XdebugClientResponse featurePacket = new XdebugClientResponse(xml);
		return featurePacket;
	}
	
	/**
	 * Creates response packet to the set featrue command.
	 * @param command
	 * @param success
	 * @return
	 * @throws Exception 
	 */
	public static XdebugClientResponse createSetFeatureResult(XdebugCommand command, boolean success)
			throws Exception {
		
		// Get the feature name from the input command.
		String featureName = command.getArgument("-n");

		// Get transaction ID from the input command.
		int transactionId = command.getTransactionId();
        if (transactionId < 1) {
        	onThrownException("org.maclan.server.messageXdebugBadTransactionId", transactionId);
        }
        
        // Create new XML DOM document object.
        Document xml = newXmlDocument();
        Element rootElement = xml.createElement("response");
        rootElement.setAttribute("command", "feature_set");
        rootElement.setAttribute("feature", featureName);
        rootElement.setAttribute("success", success ? "1" : "0");
        rootElement.setAttribute("transaction_id", String.valueOf(transactionId));
        xml.appendChild(rootElement);
		
        // Create and return  new packet.
		XdebugClientResponse featurePacket = new XdebugClientResponse(xml);
		return featurePacket;
	}
	
	/**
	 * Creates response to Xdebug source command.
	 * @param command
	 * @param sourceCode
	 * @return
	 * @throws Exception 
	 */
	public static XdebugClientResponse createSourceResult(XdebugCommand command, String sourceCode)
			throws Exception {
		
		// Get transaction ID from the input command.
		int transactionId = command.getTransactionId();
        if (transactionId < 1) {
        	onThrownException("org.maclan.server.messageXdebugBadTransactionId", transactionId);
        }

        // Create new XML DOM document object.
        Document xml = newXmlDocument();
        Element rootElement = xml.createElement("response");
        rootElement.setAttribute("command", "source");
        boolean success = sourceCode != null && !sourceCode.isEmpty();
		rootElement.setAttribute("success", success ? "1" : "0");
		rootElement.setAttribute("transaction_id", String.valueOf(transactionId));
		if (success) {
			rootElement.setTextContent(sourceCode);
		}
		xml.appendChild(rootElement);
        
        // Create and return new packet.
		XdebugClientResponse featurePacket = new XdebugClientResponse(xml);
		return featurePacket;
	}
	
	/**
	 * Creates response packet with context names.
	 * @param areaServer 
	 * @param command 
	 * @return
	 */
	public static XdebugClientResponse createContextNamesResult(XdebugCommand command, AreaServer areaServer)
			throws Exception {
		
		// Get transaction ID from the input command.
		int transactionId = command.getTransactionId();
        if (transactionId < 1) {
        	onThrownException("org.maclan.server.messageXdebugBadTransactionId", transactionId);
        }
        
        // Create new XML DOM document object.
        Document xml = newXmlDocument();
        Element rootElement = xml.createElement("response");
        rootElement.setAttribute("command", "context_names");
        rootElement.setAttribute("transaction_id", String.valueOf(transactionId));
        xml.appendChild(rootElement);
        
        // Append child node For all Xdebug client contexts.
        Map<String, Integer> contexts = XdebugClient.getContexts();
        for (Entry<String, Integer> entry : contexts.entrySet()) {
        	
        	// Get context name and ID.
        	String contextName = entry.getKey();
        	int contextId = entry.getValue();
        	String contextIdText = String.valueOf(contextId);
        	
            // Add context node.
            Element contextElement = xml.createElement("context");
            contextElement.setAttribute("name", contextName);
	        contextElement.setAttribute("id", contextIdText);
	        rootElement.appendChild(contextElement);
        }
        
        // Create and return new packet.
		XdebugClientResponse propertyPacket = new XdebugClientResponse(xml);
		return propertyPacket;
	}
	
	/**
	 * Creates response packet with area server state.
	 * @param command
	 * @param state
	 * @return
	 * @throws Exception 
	 */
	public static XdebugClientResponse createAreaServerStateResult(XdebugCommand command, AreaServerState state)
			throws Exception {
		
		// Get transaction ID from the input command.
		int transactionId = command.getTransactionId();
        if (transactionId < 1) {
        	onThrownException("org.maclan.server.messageXdebugBadTransactionId", transactionId);
        }
        
        // Create new XML DOM document object.
        Document xml = newXmlDocument();
        Element rootElement = xml.createElement("response");
        rootElement.setAttribute("command", "property_get");
        rootElement.setAttribute("transaction_id", String.valueOf(transactionId));
        xml.appendChild(rootElement);
        
        // Create property tree.
        if (state != null) {
	        createPropertyElement(xml, state, "areaServerState", stateElement -> {
	        	rootElement.appendChild(stateElement);

	        	// Add process ID.
        		createPropertyValueElement(xml, stateElement, "processId", INTEGER_TYPE_NAME, processIdValue -> {
	        		
		        	long processId = state.processId;
		        	String processIdText = String.valueOf(processId);
		        	
		        	processIdValue.setTextContent(processIdText);
		            stateElement.appendChild(processIdValue);

	        	});
	        	
	        	// Add process name.
        		createPropertyValueElement(xml, stateElement, "processName", STRING_TYPE_NAME, processNameValue -> {
	        		
		        	String processName = state.processName;
		        	
		        	processNameValue.setTextContent(processName);
		            stateElement.appendChild(processNameValue);

	        	});
        		
	        	// Add thread ID.
        		createPropertyValueElement(xml, stateElement, "threadId", INTEGER_TYPE_NAME, threadIdValue -> {
	        		
    	            long threadId = state.threadId;
    	            String threadIdText = String.valueOf(threadId);
		        	
    	            threadIdValue.setTextContent(threadIdText);
		            stateElement.appendChild(threadIdValue);

	        	});
        		
	        	// Add thread name.
        		createPropertyValueElement(xml, stateElement, "threadName", STRING_TYPE_NAME, threadNameValue -> {
	        		
		        	String threadName = state.threadName;
		        	
		        	threadNameValue.setTextContent(threadName);
		            stateElement.appendChild(threadNameValue);

	        	});        		
	            
	        	DebuggedCodeDescriptor descriptor = state.debuggedCodeDescriptor;
	            if (descriptor == null) {
	            	return false;
	            }
	            
            	createPropertyElement(xml, descriptor, "debuggedCodeDescriptor", descriptorElement -> {
            		stateElement.appendChild(descriptorElement);
            		
            		TagsSource tagsSource = descriptor.tagsSource;
                	if (descriptor.tagsSource == null) {
                		return false;
                	}
                		
    	        	createPropertyElement(xml, tagsSource, "tagsSource", sourceElement -> {
    	        		descriptorElement.appendChild(sourceElement);

    	        		Long resourceId = tagsSource.resourceId;
    	        		Long slotId = tagsSource.slotId;
    	        		if (resourceId == null && slotId == null) {
    	        			return false;
    	        		}
    	        		
    	        		if (resourceId != null) {
    	        			createPropertyValueElement(xml, sourceElement, "resourceId", INTEGER_TYPE_NAME, resourceIdValue -> {
    	        				String resourceIdText = String.valueOf(resourceId);
    	        				resourceIdValue.setTextContent(resourceIdText);
    	        				sourceElement.appendChild(resourceIdValue);
    	        			});
    	        		}
    	        		
       	        		if (slotId != null) {
    	        			createPropertyValueElement(xml, sourceElement, "slotId", INTEGER_TYPE_NAME, slotIdValue -> {
    	        				String slotIdText = String.valueOf(slotId);
    	        				slotIdValue.setTextContent(slotIdText);
    	        				sourceElement.appendChild(slotIdValue);
    	        			});
    	        		}
    	        		return true;
    	        	});             			
            		return true;
            	});
            	return true;
	        });
        }

        // Create and return new packet.
		XdebugClientResponse propertyPacket = new XdebugClientResponse(xml);
		return propertyPacket;
	}

	/**
	 * Create property XML element.
	 * @param xml
	 * @param theObject
	 * @param propertyName
	 * @param elementLambda
	 * @return
	 */
	private static void createPropertyElement(Document xml, Object theObject, String propertyName, Function<Element, Boolean> elementLambda) {
		
		Element propertyElement = xml.createElement("property");
		propertyElement.setAttribute("name", propertyName);
		propertyElement.setAttribute("type", "object");
		String className = theObject.getClass().getSimpleName();
		propertyElement.setAttribute("classname", className);
		propertyElement.setAttribute("encoding", "none");
		
		boolean hasChildren = elementLambda.apply(propertyElement);
		propertyElement.setAttribute("children", hasChildren ? "1" : "0");
	}	
	
	/**
	 * Create property value XML element.
	 * @param xml
	 * @param value
	 * @param propertyName
	 * @param typeName
	 */
	private static void createPropertyValueElement(Document xml, Object value, String propertyName, String typeName, Consumer<Element> elementLambda) {
		
		Element propertyElement = xml.createElement("property");
		propertyElement.setAttribute("name", propertyName);
		propertyElement.setAttribute("type", typeName);
		propertyElement.setAttribute("classname", "none");
		propertyElement.setAttribute("encoding", "none");
		
		elementLambda.accept(propertyElement);
		propertyElement.setAttribute("children", "0");		
	}

	/**
	 * Creates response packet to the expr command.
	 * @param command
	 * @param exprResultText
	 * @return
	 * @throws Exception 
	 */
	public static XdebugClientResponse createExprResult(XdebugCommand command, String exprResultText)
			throws Exception {
		
		// Get transaction ID from the input command.
		int transactionId = command.getTransactionId();
        if (transactionId < 1) {
        	onThrownException("org.maclan.server.messageXdebugBadTransactionId", transactionId);
        }

        // Create new XML DOM document object.
        Document xml = newXmlDocument();
        Element rootElement = xml.createElement("response");
        rootElement.setAttribute("command", "expr");
        rootElement.setAttribute("transaction_id", String.valueOf(transactionId));
        xml.appendChild(rootElement);
        Element propertyElement = xml.createElement("property");
        propertyElement.setTextContent(exprResultText);
        rootElement.appendChild(propertyElement);
		
        // Create and return  new packet.
		XdebugClientResponse exprPacket = new XdebugClientResponse(xml);
		return exprPacket;
	}
	
	/**
	 * Create continuation command result.
	 * @param command
	 * @return
	 * @throws Exception 
	 */
	public static XdebugClientResponse createContinuationCommandResult(XdebugCommand command)
			throws Exception {
		
		// Get transaction ID from the input command.
		int transactionId = command.getTransactionId();
        if (transactionId < 1) {
        	onThrownException("org.maclan.server.messageXdebugBadTransactionId", transactionId);
        }
        
        // Get command name.
        String commandName = command.getName();

        // Create new XML DOM document object.
        Document xml = newXmlDocument();
        Element rootElement = xml.createElement("response");
        rootElement.setAttribute("command", commandName);
        rootElement.setAttribute("transaction_id", String.valueOf(transactionId));
        rootElement.setAttribute("status", "running");
        rootElement.setAttribute("reason", "ok");
        xml.appendChild(rootElement);
		
        // Create and return new packet.
		XdebugClientResponse runPacket = new XdebugClientResponse(xml);
		return runPacket;
	}
	
	/**
	 * Check if the input packet is an INIT packet.
	 * @return
	 * @throws Exception 
	 */
	public boolean isInitPacket() throws Exception {
		
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
	 * Check if the input packet is an error packet.
	 * @return
	 * @throws Exception 
	 */
	public boolean isErrorPacket() throws Exception {
		
		// Check packet.
		if (xml == null) {
			return false;
		}
		
		// Try to get packet root node.
		String codeText = (String) xpathErrorCode.evaluate(xml, XPathConstants.STRING);
		boolean isErrorPacket = !codeText.isEmpty();
		return isErrorPacket;
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
	 * Get Xdebug XML response.
	 * @param xmlBuffer
	 * @return
	 * @throws Exception 
	 */
	private static XdebugClientResponse getXmlContent(ByteBuffer xmlBuffer)
				throws Exception {
		
		// Prepare the XML buffer for reading the XML content.
		xmlBuffer.flip();
		
		// Get the length of the XML buffer. Create byte array to hold the buffer contents.
		int arrayLength = xmlBuffer.limit();
		byte [] bytes = new byte [arrayLength];
		
		// Read buffer contents into the byte array.
		xmlBuffer.get(bytes);
		
		// Convert bytes into UTF-8 encoded string, the XML.
		String xmlText = new String(bytes, "UTF-8");
		
		// Dalgate the call.
		XdebugClientResponse clientResponse = getXmlContent(xmlText);
		
    	// Reset the XML buffer.
    	xmlBuffer.clear();
    	
    	return clientResponse;
	}
	
	/**
	 * Get Xdebug XML response.
	 * @param xmlText
	 * @return
	 * @throws Exception 
	 */
	static XdebugClientResponse getXmlContent(String xmlText)
				throws Exception {
		
        // Parse the XML string into a Document.
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        
        Document xml = builder.parse(new InputSource(new StringReader(xmlText)));
		
        // Create new packet object.
    	XdebugClientResponse clientResponse = new XdebugClientResponse(xml);
    	
    	// Return XML response.
    	return clientResponse;
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
	public String getDebuggedUri()
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
			
			XdebugClientParameters clientParameters = new XdebugClientParameters();
			String computer = matcher.group("computer");
		    clientParameters.setComputer(computer);
		    String processIdText = matcher.group("pid");
		    Long processId = Long.parseLong(processIdText);
		    clientParameters.setProcessId(processId);
		    String threadIdText = matcher.group("tid");
		    Long thredId = Long.parseLong(threadIdText);
		    clientParameters.setThreadId(thredId);
		    String areaIdText = matcher.group("aid");
		    Long areaId = Long.parseLong(areaIdText);
		    clientParameters.setAreaId(areaId);
		    String hashText = matcher.group("statehash");
		    Integer stateHash = Integer.parseInt(hashText);
		    clientParameters.setStatehash(stateHash);
		    return clientParameters;
		}
		
		onThrownException("org.maclan.server.messageBadDebuggerUri", debuggerUri);
		return null;
	}
	
	/**
	 * Get transaction ID.
	 * @return
	 */
	public int getTransactionId()
			throws Exception {
		
		try {
			// Get transaction ID from XML Document.
			String transactionIdText = (String) xpathResponseTransactionId.evaluate(xml, XPathConstants.STRING);
			int transactionId = Integer.parseInt(transactionIdText);
			return transactionId;
		}
		catch (Exception e) {
			onThrownException("org.maclan.server.messageBadXdebugTransactionId");
		}
		return -1;
	}
	
	/**
	 * Get Xdebug feature.
	 * @return
	 * @throws Exception 
	 */
	public XdebugFeature getFeatureValue()
			throws Exception {
		
		// Get feature attributes.
		String commandName = (String) xpathResponseCommandName.evaluate(xml, XPathConstants.STRING);
		if (!"feature_get".equals(commandName)) {
			onThrownException("org.maclan.server.messageBadXdebugCommandName", "feature_get", commandName);
		}
		String featureName = (String) xpathResponseFeatureName.evaluate(xml, XPathConstants.STRING);
		String supportedString = (String) xpathResponseFeatureSupported.evaluate(xml, XPathConstants.STRING);
		String featureValue = (String) xpathResponseFeatureValue.evaluate(xml, XPathConstants.STRING);
		
		// Create feature object from this packet.
		XdebugFeature feature = XdebugFeature.createFeature(featureName, supportedString, featureValue);
		return feature;
	}
	
	/**
	 * Get feature result. 
	 * @return
	 * @throws Exception 
	 */
	public boolean getSettingFeatureResult()
			throws Exception {
		
		// Check command name.
		String commandName = (String) xpathResponseCommandName.evaluate(xml, XPathConstants.STRING);
		if (!"feature_set".equals(commandName)) {
			onThrownException("org.maclan.server.messageBadXdebugCommandName", "feature_set", commandName);
		}
		
		boolean success = (boolean) xpathSuccessResponse.evaluate(xml, XPathConstants.BOOLEAN);
		return success;
	}
	
	/**
	 * Get source result.
	 * @return
	 * @throws Exception
	 */
	public String getSourceResult()
			throws Exception {
		
		// Check command name.
		String commandName = (String) xpathResponseCommandName.evaluate(xml, XPathConstants.STRING);
		if (!"source".equals(commandName)) {
			onThrownException("org.maclan.server.messageBadXdebugCommandName", "source", commandName);
		}
		
		// Get source code.
		String sourceCode = (String) xpathSourceResponse.evaluate(xml, XPathConstants.STRING);
		return sourceCode;
	}

	/**
	 * Get expr result.
	 * @throws Exception
	 */
	public String getExprResult()
			throws Exception {
		
		// Check command name.
		String commandName = (String) xpathResponseCommandName.evaluate(xml, XPathConstants.STRING);
		if (!"expr".equals(commandName)) {
			onThrownException("org.maclan.server.messageBadXdebugCommandName", "expr", commandName);
		}
		
		// Get expression result.
		String evalResult = (String) xpathEvalResponse.evaluate(xml, XPathConstants.STRING);
		return evalResult;
	}
	
	/**
	 * Get context names.
	 * @return
	 * @throws Exception 
	 */
	public LinkedHashMap<String, Integer> getContextNames()
			throws Exception {
		
		// Check command name.
		String commandName = (String) xpathResponseCommandName.evaluate(xml, XPathConstants.STRING);
		if (!"context_names".equals(commandName)) {
			onThrownException("org.maclan.server.messageBadXdebugCommandName", "context_names", commandName);
		}
		
		// Initialize context map.
		LinkedHashMap<String, Integer> contextMap = null;
		
		// Load context names and IDs.
		NodeList nodeList = (NodeList) xpathContextsResponse.evaluate(xml, XPathConstants.NODESET);
		int length = nodeList.getLength();
		
		for (int index = 0; index < length; index++) {
			
			Node node = nodeList.item(index);
			String nodeName = node.getNodeName();
			
			// Check node.
			if (!"context".equals(nodeName)) {
				continue;
			}
			
			// Get context name and ID.
			NamedNodeMap nodeAttributes = node.getAttributes();
			if (nodeAttributes == null) {
				continue;
			}
			
			node = nodeAttributes.getNamedItem("name");
			String contextName = node.getNodeValue();
			
			node = nodeAttributes.getNamedItem("id");
			String contextIdText = node.getNodeValue();
			Integer contextId = null;
			try {
				contextId = Integer.parseInt(contextIdText);
			}
			catch (Exception e) {
				continue;
			}
			
			// Add context to map.
			if (contextMap == null) {
				contextMap = new LinkedHashMap<String, Integer>();
			}
			contextMap.put(contextName, contextId);
		}
		
		return contextMap;
	}
	
	/**
	 * Get area state available for Xdebug.
	 * @return
	 * @throws Exception 
	 */
	public XdebugAreaServerState getXdebugAreaState()
			throws Exception {
		
		// Check command name.
		String commandName = (String) xpathResponseCommandName.evaluate(xml, XPathConstants.STRING);
		if (!"property_get".equals(commandName)) {
			onThrownException("org.maclan.server.messageBadXdebugCommandName", "property_get", commandName);
		}
		
		XdebugAreaServerState state = new XdebugAreaServerState();
		try {
			// Get ID of slot that supplied the source code. 
			String valueText = (String) xpathStateSlotId.evaluate(xml, XPathConstants.STRING);
			if (!valueText.isEmpty()) {
				state.setSourceTagId(Long.parseLong(valueText));
			}
			
			// Get ID of resource that supplied the source code. 
			valueText = (String) xpathStateResourceId.evaluate(xml, XPathConstants.STRING);
			if (!valueText.isEmpty()) {
				state.setTagResourceId(Long.parseLong(valueText));
			}
			
			// Get process ID.
			valueText = (String) xpathStateProcessId.evaluate(xml, XPathConstants.STRING);
			if (!valueText.isEmpty()) {
				state.setProcessId(Long.parseLong(valueText));
			}
			
			// Get process name.
			valueText = (String) xpathStateProcessName.evaluate(xml, XPathConstants.STRING);
			if (!valueText.isEmpty()) {
				state.setProcessName(valueText);
			}	
			
			// Get thread ID.
			valueText = (String) xpathStateThreadId.evaluate(xml, XPathConstants.STRING);
			if (!valueText.isEmpty()) {
				state.setThreadId(Long.parseLong(valueText));
			}
			
			// Get thread name.
			valueText = (String) xpathStateThreadName.evaluate(xml, XPathConstants.STRING);
			if (!valueText.isEmpty()) {
				state.setThreadName(valueText);
			}	
		}
		catch (Exception e) {
			onThrownException(e);
		}
		return state;
	}

	/**
	 * Get error message.
	 * @return
	 */
	public String getErrorMessage()
			throws Exception {
		
		// Check command name, error code with description and message text.
		String commandName = (String) xpathResponseCommandName.evaluate(xml, XPathConstants.STRING);
		
		String codeText = (String) xpathErrorCode.evaluate(xml, XPathConstants.STRING);
		if (codeText.isEmpty()) {
			onThrowException("org.maclan.server.messageMissingErrorCode");
		}
		
		String errorCodeDescription = "";
		try {
			int errorCode = Integer.parseInt(codeText);
			errorCodeDescription = XdebugError.getErrorDescription(errorCode);
		}
		catch (Exception e) {
			onException(e);
		}
		
		String messageText = (String) xpathErrorMessage.evaluate(xml, XPathConstants.STRING);
		String errorMessage = String.format("ERROR %s, Xdebug command \"%s\". %s %s", codeText, commandName, errorCodeDescription, messageText);
		return errorMessage;
	}

	/**
	 * Get string value.
	 * @param xpathExpression
	 * @return
	 */
	public String getString(String xpathExpression) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * Throws exception created from the response packet.
	 */
	public void throwPacketException()
			throws Exception {
		
		String errorMessage = getErrorMessage();
		Exception exception = new Exception(errorMessage);
		throw exception;
	}
	
	/**
	 * Called on exception.
	 * @param messageId
	 * @throws Exception
	 */
	private void onThrowException(String messageId) throws Exception {
		
		String message = Resources.getString(messageId);
		Exception exception = new Exception(message);
		onException(exception);
		throw exception;
	}
	
	/**
	 * On exception.
	 * @param messageFormatId
	 * @param parameters
	 */
	private static void onThrownException(String messageFormatId, Object ... parameters)
			throws Exception {
		
		String messageFormat = Resources.getString(messageFormatId);
		String message = String.format(messageFormat, parameters);
		Exception exception = new Exception(message);
		onException(exception);
		throw exception;		
	}
	
	/**
	 * On exception.
	 * @param e
	 * @throws Exception 
	 */
	private void onThrownException(Exception e)
			throws Exception {
		
		onException(e);
		throw e;
	}
	
	/**
	 * On exception.
	 * @param e
	 */
	private static void onException(Throwable e) {
		
		// Override this method.
		e.printStackTrace();
	}
}