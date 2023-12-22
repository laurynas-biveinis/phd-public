/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.xml.relational.dom;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
	An object that supports the functionality which is used in the xxl.xml package.
	This interface enables the usage of different XML-parsers such as crimson or xerces.
*/
public interface XMLParser {

	/**
		Indicates whether the parser supports the XPath functionality.
	*/
	boolean supportsXPath = false;	
	
	/**
		Use an XPath string to select a single node.
		@param document The document containing the seeked node
		@param XPath The XPath expression that indicates the node
		@return The selected Node
	*/
	public Node selectSingleNode(Document document, String XPath);
}
