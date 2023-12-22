/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.xml.util;

import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import xxl.core.functions.AbstractFunction;
import xxl.core.functions.Function;


/**
 * A handler for XML Parsing with SAX. It extends the org.xml.sax.helpers.DefaultHandler
 * and is decorated with two Functions which will be called at the beginning of a tag
 * and at the beginning of the characters between a tag.
 */
public class XPathFunctionCallerHandler extends XPathCalculatorHandler {

	/**
	 * A simple Function for the XPathCalculatorHandler that 
	 * writes the XPath and the Tag-Name to the standard output.
	 */
	public static Function<Object,Object> SIMPLE_PRINTLN_FUNCTION_T() {
		return new AbstractFunction<Object,Object>() {
			public Object invoke(List<? extends Object> oa) {
				String xPath = (String) oa.get(0);
				String value = (String) oa.get(1);
				System.out.println("Tag, xPath: "+xPath+", value: "+value);
				return null;
			}
		};
	}

	/**
	 * A simple Function for the XPathCalculatorHandler that 
	 * writes the XPath and the Literal String to the standard output.
	 */
	public static Function<Object,Object> SIMPLE_PRINTLN_FUNCTION_C() {
		return new AbstractFunction<Object,Object>() {
			public Object invoke(List<? extends Object> oa) {
				String xPath = (String) oa.get(0);
				String value = (String) oa.get(1);
				System.out.println("Literal, xPath: "+xPath+", value: "+value);
				return null;
			}
		};
	}

	/**
	 * Function which is called for each attribute.
	 */
	protected Function functionA; //ATTRIBUTE

	/**
	 * Function which is called for each literal node (character node).
	 */
	protected Function functionC; //LITERAL

	/**
	 * Function which is called for each tag (markup).
	 */
	protected Function functionT; //MARKUP

	/**
	 * Constructs an XPathFunctionCallerHandler which uses the given Functions to handle the events.
	 * The tag and character functions will be called with two String arguments. The first one is always the
	 * current XPath, the second argument is the Tagname (in the case of functionT) or the Literal-String (in the case of functionC).
	 * The functionA for the attributes will be called with three parameters, the first one is the XPath-String
	 * the second is the name if the attribute and the third is the value of the attribute. Note, that
	 * the functionA will be called one time for every attribute in a tag!!
	 * @param functionC this function will be called at the beginning of a Literal (String)
	 * @param functionT this function will be called at the beginning of a Markup (Tag)
	 * @param functionA this function will be called at the beginning of a Markup and gets the attributes name and value
	 *
	 */
	public XPathFunctionCallerHandler (Function functionC, Function functionT, Function functionA) {
		super();
		this.functionC = functionC;
		this.functionT = functionT;
		this.functionA = functionA;
	}

	/**
	 * The default constructor. Calls the second constructor with the SIMPLE_PRINTLN_FUNCTION s and null as the functionA
	 * See SIMPLE_PRINTLN_FUNCTION_T() and SIMPLE_PRINTLN_FUNCTION_C() for more informations about these
	 * functions.
	 */
	public XPathFunctionCallerHandler() {
		this(SIMPLE_PRINTLN_FUNCTION_C(), SIMPLE_PRINTLN_FUNCTION_T(), null);
	}

	/**
	 * Handles the event of a starting tag.
	 * Calls the Function functionT.
	 */
	 public void startElement(java.lang.String uri, java.lang.String localName, java.lang.String qName, Attributes attributes)   throws SAXException {
		functionT.invoke(currentXPathLocation, new String(qName));

		super.startElement(uri, localName, qName, attributes);
		
		if (functionA!=null) {
			Object[] array;
			for (int i=0;i<attributes.getLength();i++) {
				array=new Object[3];
				array[0] = currentXPathLocation;
				array[1] = new String(attributes.getQName(i));
				array[2] = new String(attributes.getValue(i));
				functionA.invoke(array);	
			}
		}
	}

	/**
	 * Handles the event of a closing tag.
	 */
	public void endElement(java.lang.String uri, java.lang.String localName, java.lang.String qName)  throws SAXException {
		super.endElement(uri, localName, qName);
	}

	/**
	 * Handles the event of characters between tags.
	 * Calls the Function functionC.
	 */
	public void characters(char[] ch, int start, int length) throws SAXException {
		super.characters(ch, start, length);
		if (currentXPathLocation!=null && currentXPathLocation.getNumberOfParts()!=0)
			functionC.invoke(currentXPathLocation, new String(ch, start, length));
	}
}
