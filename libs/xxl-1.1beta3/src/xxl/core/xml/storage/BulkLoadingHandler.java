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

package xxl.core.xml.storage;


import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import xxl.core.collections.containers.Container;
import xxl.core.io.converters.SizeConverter;

/**
 * Inserts the parsed XML document in bulk insertion fashion into 
 * an empty XML tree.
 */
public class BulkLoadingHandler extends org.xml.sax.helpers.DefaultHandler {

	private Stack location;

	private Container container;

	private int maxSubtreeSize;

	private EXTree tree;

	private SizeConverter subtreeConverter;

	private Object notNullId;

	/**
	 * Constructs a new BulkLoadingHandler for an EXTree.
	 * @param tree The tree which shall be bulk loaded.
	 * @param notNullId an identifyer of the tree which is not
	 * 	null (important for the calculation of the size of
	 * 	a subtree).
	 */
	public BulkLoadingHandler (EXTree tree, Object notNullId) {
		this.tree = tree;
		this.notNullId = notNullId;
		maxSubtreeSize = tree.getMaxSubtreeSize();
		container = tree.getContainer();
		subtreeConverter = tree.getSubtreeConverter(); 
		location = new Stack();
	}

	/**
	 * Handles the event of a starting tag.
	 */
	public void startElement(java.lang.String uri, java.lang.String localName, java.lang.String qName, Attributes attributes)   throws SAXException {
		Node node = new MarkupNode(qName,false);
		
		for (int i=0;i<attributes.getLength();i++) {
			Node attr = new MarkupNode(attributes.getQName(i),true);
			Node lit = new LiteralNode(attributes.getValue(i));
			attr.addChildNode(lit);
			node.addChildNode(attr);
		}

		if (!location.empty()) {
			Node father = (Node) location.peek();
			father.addChildNode(node);
		}
		location.push(node);
	}

	/**
	 * Inserts a subtree into the Container and returns a proxy node to the subtree.
	 * @param node root of the subtree
	 * @return ProxyNode ProxyNode containing a link to the subtree.
	 */
	private ProxyNode insertNode(Node node) {
		ProxyNode pn = new ProxyNode();
		Object id = container.insert(node);
		pn.setChildId(id);
		EXTree.updateParentIdsInChildSubtrees(node, id, container);
		return pn;
	}

	/**
	 * Handles the event of a closing tag.
	 */
	public void endElement(java.lang.String uri, java.lang.String localName, java.lang.String qName)  throws SAXException {
		Node node = (Node) location.pop();
		node.setParentId(notNullId);
		
		while (subtreeConverter.getSerializedSize(node)>maxSubtreeSize) {
			// save proxy nodes
			LinkedList proxies = new LinkedList();
			
			// invariant: each childnode fits into a record (!)
			ScaffoldNode sc = new ScaffoldNode();
			sc.setParentId(notNullId);
			
			// put childs into records
			Iterator it = node.getChildNodes();
			while (it.hasNext()) {
				Node child = (Node) it.next();
				
				sc.addChildNode(child);
				int size = subtreeConverter.getSerializedSize(sc);
				if (size>=maxSubtreeSize) {
					sc.removeLastChildNode(); // remove the node which was added just above
					Node in = sc;
					if (sc.getNumberOfChildren()==1) {
						in = sc.getFirstChild();
						in.setParentId(notNullId);
					}
					proxies.add(insertNode(in));
					sc = new ScaffoldNode();
					sc.addChildNode(child); // reinsert the node again
					sc.setParentId(notNullId);
				}
			}

			// Write last record (the node sc). At least the last child is inside sc.
			// This record cannot be overfull, because of the last test
			// which tested sc to fit.
			Node in = sc;
			if (sc.getNumberOfChildren()==1) {
				in = sc.getFirstChild();
				in.setParentId(notNullId);
			}
			proxies.add(insertNode(in));

			node.setChildList(proxies);
			node.setParentId(null);
			// this node with proxies can also be to big! so make it
			// with scaffold nodes again if necessarry
		}

		// Root node
		if (location.empty()) {
			Object id = container.insert(node);
			EXTree.updateParentIdsInChildSubtrees(node, id, container);
			tree.setRootId(id);
			// tree.numberOfNodes = ...
		}
	}

	/**
	 * Handles the event of characters between tags.
	 */
	public void characters(char[] ch, int start, int length) throws SAXException {
		// must have a parent node!
		Node father = (Node) location.peek();
		father.addChildNode(new LiteralNode(new String(ch, start, length)));
	}
}
