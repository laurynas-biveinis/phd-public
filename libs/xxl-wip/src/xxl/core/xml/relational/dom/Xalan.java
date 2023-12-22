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

import com.sun.org.apache.xpath.internal.XPathAPI;


/**
	An implementation of the Parser interface, using the apache xerces parser and apache xalan (XPath). <br>
	<br>
	This implementation supports XPath.
*/
public class Xalan implements XMLParser {
	
	/**
		Indicates whether the parser supports the XPath functionality.
	*/
	public boolean supportsXPath = true;

	/**
		Use an XPath string to select a single node.
		@param document The document containing the seeked node
		@param XPath The XPath expression that indicates the node
		@return The selected Node
	*/
	public Node selectSingleNode(Document document, String XPath) {
		try {
			return XPathAPI.selectSingleNode(document, XPath);
		}
		catch (Exception e) {
			throw new RuntimeException ("Exception during evaluation of XPath query");
		}
		
	}
}

