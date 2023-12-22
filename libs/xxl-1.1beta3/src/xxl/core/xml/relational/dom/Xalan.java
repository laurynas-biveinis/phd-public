/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2006 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307,
USA

	http://www.xxl-library.de

bugs, requests for enhancements: request@xxl-library.de

If you want to be informed on new versions of XXL you can
subscribe to our mailing-list. Send an email to

	xxl-request@lists.uni-marburg.de

without subject and the word "subscribe" in the message body.
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

