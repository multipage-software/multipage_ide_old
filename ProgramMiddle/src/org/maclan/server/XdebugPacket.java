/**
 * 
 */
package org.maclan.server;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.multipage.gui.Utility;
import org.multipage.util.Obj;
import org.multipage.util.j;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author user
 *
 */
public class XdebugPacket {
	
	/**
	 * Empty packet
	 */
	public final static XdebugPacket empty = new XdebugPacket();

	/**
	 * XML Document
	 */
	private Document xml;
	
	/**
	 * Original message
	 */
	private String packetText;
	
	/**
	 * XPATH object
	 */
	private final XPath xpath = XPathFactory.newInstance().newXPath();
	
	/**
	 * Constructs empty packet
	 */
	public XdebugPacket() {
		
		xml = null;
		packetText = "";
	}

	/**
	 * Constructor of the packet object
	 * @param packetText
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public XdebugPacket(String packetText)
			throws Exception {
		
		// Prepare XML DOM parser prerequisites
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		
		// Remember Packet text
		this.packetText = packetText;
		
		// Parse packet text
		InputSource source = new InputSource(new StringReader(packetText));
		xml = builder.parse(source);
		
		// TODO: <---DEBUG
		j.log("PARSED XML PACKET: %s", packetText);
	}

	/**
	 * Gets DOM type based on XPath expression
	 * @param xpathExpression
	 * @return
	 */
	public String getString(String xpathExpression) {
		
		if (xml == null) {
			return null;
		}
		try {
			Object object = xpath.evaluate(xpathExpression, xml, XPathConstants.STRING);
			return (String) object;
		}
		catch (Exception e) {
		}
		return null;
	}
	
	/**
	 * Get DOM type based on XPath expression
	 * @param xpathExpression
	 * @return
	 */
	public String getBase64String(String xpathExpression) {
		
		String base64 = getString(xpathExpression);
		if (base64 == null) {
			return null;
		}
		byte [] bytes = Base64.getDecoder().decode(base64);
		return new String(bytes);
	}
	
	/**
	 * Gets DOM type based on XPath expression
	 * @param xpathExpression
	 * @return
	 */
	public NodeList getNodes(String xpathExpression) {
		
		if (xml == null) {
			return null;
		}
		try {
			Object object = xpath.evaluate(xpathExpression, xml, XPathConstants.NODESET);
			return (NodeList) object;
		}
		catch (Exception e) {
		}
		return null;
	}
	
	/**
	 * Gets a number
	 */
	public Double getNumber(String xpathExpression) {
		
		if (xml == null) {
			return null;
		}
		try {
		}
		catch (Exception e) {
		}
		return null;	
	}
	
	/**
	 * Get boolean value
	 * @param xpathExpression
	 * @return
	 */
	public Boolean getBoolean(String xpathExpression) {
		
		if (xml == null) {
			return null;
		}
		try {
			return (Boolean) xpath.evaluate(xpathExpression, xml, XPathConstants.BOOLEAN);
		}
		catch (Exception e) {
		}
		return null;
	}

	/**
	 * Returns packet text
	 * @return
	 */
	public String getPacketText() {
		
		return packetText;
	}
	
	/**
	 * Convert packet to text line
	 */
	@Override
	public String toString() {
		return packetText.replaceAll("\n"," ");
	}
	
	/**
	 * Checks if the packet is empty
	 * @return
	 */
	public boolean isEmpty() {
		
		return equals(empty);
	}
	
	/**
	 * Gets result status
	 * @return
	 */
	public String status() {
		
		return getString("/response/@status");
	}
}
