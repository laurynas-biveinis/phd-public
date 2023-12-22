/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.xml.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * This is a handler for XML Parsing with SAX which computes
 * the current position inside the document. It can be used
 * by extending this class. All methods then have to call
 * the same method of the superclass (this class)
 * (for example: call super.startElement in startElement).
 * Look for further instructions below.
 * <p>
 * The current XPath location can be taken from the protected
 * field currentXPathLocation. But be careful,
 * becaues this field will change its content!
 */
public class XPathCalculatorHandler extends DefaultHandler {

	/**
	 * Contains the XPath expression which leeds to the current
	 * position inside the XML source.
	 */
	protected XPathLocation currentXPathLocation;
	
	/**
	 * Contains the current level (starting with 0) for the
	 * root tag. Character data does not change the level.
	 */
	protected int level;

	/**
	 * Contains a map for each level of the XML tree.
	 */
	private Stack mapStack;

	/**
	 * Contains the map which is currently used at a specific level 
	 * in the tree.
	 */
	private Map status;

	/**
	 * Constructs an XPathCalculatorHandler.
	 */
	public XPathCalculatorHandler() {
		super();
		level = -1;
		mapStack = new Stack();
		status = new HashMap();
		currentXPathLocation = new XPathLocation();
	}

	/**
	 * Handles the event of a starting tag. Call this method from a
	 * subclass as the first instruction!
	 */
	 public void startElement(java.lang.String uri, java.lang.String localName, java.lang.String qName, Attributes attributes)   throws SAXException {
		int number = 1;
		level++;
		
		currentXPathLocation.append(qName, number, false);

		if (currentXPathLocation.getNumberOfParts()>0) {
			String s = currentXPathLocation.toString();
	
			if (status.containsKey(s)) {
				Integer iv = (Integer) status.get(s);
				if (iv!=null)
					number = iv.intValue()+1;
			}
			currentXPathLocation.setLastCount(number);
			status.put(s, new Integer(number));
			
			mapStack.push(status);
			status = new HashMap();
		}
	}

	/**
	 * Handles the event of a closing tag. Call this method from a
	 * subclass as the last instruction!
	 */
	public void endElement(java.lang.String uri, java.lang.String localName, java.lang.String qName)  throws SAXException {
		if (level>0) 
			status = (Map) mapStack.pop();
		else
			level--;
		currentXPathLocation.removeLast();
	}
}
