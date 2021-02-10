/**
 * 
 */
package org.multipage.util;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Document object model utilities
 * @author user
 *
 */
public class DOM {
	
	/**
	 * A reference to DOM node
	 */
	private Node node;
	
	/**
	 * Object factory
	 * @param node
	 * @return
	 */
	public static DOM use(Node node) {
		
		DOM dom = new DOM();
		dom.node = node;
		return dom;
	}
	
	public String attribute(String name) {
		
		if (node != null) {
			NamedNodeMap attributes = node.getAttributes();
			if (attributes != null) {
				
				Node attribute = attributes.getNamedItem(name);
				if (attribute != null) {
					
					String value = attribute.getTextContent();
					if (value != null) {
						return value;
					}
				}
			}
		}
		return "";
	};
}
