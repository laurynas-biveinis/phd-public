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

package xxl.core.xml.util;

import java.io.PrintStream;

import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * This class contains methods to write the XML representation of a subtree 
 * of an DOM document to a PrintStream.
 */
public class SimpleDOMNodeSerializer {
	/**
	 * No instances of this class possible.
	 */
	private SimpleDOMNodeSerializer() {
	}

	/** 
	 * Outputs the XML representation of a certain DOM node with its attributes.
	 * Child nodes are not considered. If open==true, then the opening tag is
	 * generated (with attributes), else the closing tag is generated (without
	 * the attributes).
	 *
	 * @param node the node
	 * @param level the level which determins the indentation.
	 * @param ps the PrintStream used for the output (for example System.out).
	 * @param open Outputting opening tag or closing tag?
	 */
	public static void printNode(Node node, int level, PrintStream ps, boolean open) {
		for (int i=0; i<level; i++)
			ps.print(" ");
		
		if (node.getNodeType()==Node.TEXT_NODE) {
			if (open) {
				Text tn = (Text)node;
				ps.println(tn.getData());
			}
		}
		else {
			ps.print("<");
			if (!open)
				ps.print("/");
			ps.print(node.getNodeName());
			
			if (open) {
				NamedNodeMap attrs = node.getAttributes();
				if (attrs!=null) {
					for (int i=0; i<attrs.getLength(); i++) {
						ps.print(" ");
						Attr attr = (Attr) attrs.item(i);
						ps.print(attr.getNodeName());
						ps.print("=\"");
						ps.print(attr.getValue());
						ps.print("\"");
					}
				}
			}
			
			ps.println(">");
		}
	}

	/** 
	 * Outputs the XML representation of a subtree of a DOM document.
	 *
	 * @param node the node
	 * @param level the level (height of the tree at this position).
	 * @param indent determines the number of spaces which are placed
	 *		in front of the XML nodes in each level.
	 * @param ps the PrintStream used for the output (for example System.out).
	 * @param open Outputting opening tag or closing tag?
	 */
	private static void outputNode(Node node, int level, int indent, PrintStream ps) {
		
		switch (node.getNodeType()) {
		case Node.ATTRIBUTE_NODE:
			break;
		case Node.TEXT_NODE:
			printNode(node, level, ps, true);
			break;
		default:
			printNode(node, level, ps, true);
			NodeList nl = node.getChildNodes();
			if (nl!=null) {
				for (int i=0; i<nl.getLength(); i++) {
					Node n = nl.item(i);
					outputNode(n,level+indent,indent,ps);
				}
			}
			printNode(node, level, ps, false);
		}
	}

	/** 
	 * Outputs the XML representation of a subtree of a DOM document.
	 *
	 * @param node the node
	 * @param indent determines the number of spaces which are placed
	 *		in front of the XML nodes in each level.
	 * @param ps the PrintStream used for the output (for example System.out).
	 */
	public static void outputNode(Node node, int indent, PrintStream ps) {
		outputNode(node,0,indent,ps);
	}
}
