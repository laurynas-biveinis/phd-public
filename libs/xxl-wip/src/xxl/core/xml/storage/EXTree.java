/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.xml.storage;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import xxl.core.collections.containers.Container;
import xxl.core.cursors.Cursor;
import xxl.core.cursors.sources.io.ChannelCursor;
import xxl.core.functions.AbstractFunction;
import xxl.core.functions.Function;
import xxl.core.io.converters.SizeConverter;
import xxl.core.predicates.AbstractPredicate;
import xxl.core.predicates.Predicate;
import xxl.core.util.WrappingRuntimeException;
import xxl.core.util.concurrency.AsynchronousChannel;
import xxl.core.xml.util.XPathLocation;

/**
 * This class allows the efficient storage of an XML-Tree.
 */
public class EXTree {

	/**
	 * Sets the parentId of all children records of the subtree to 
	 * the new identifyer.
	 * @param subtree The subtree which is searched for proxy nodes.
	 * @param newParentId The new identifyer of the parent
	 * @param container Container used for storing the tree.
	 */
	protected static void updateParentIdsInChildSubtrees(Node subtree, Object newParentId, Container container) {
		if (subtree!=null) {
			int type = subtree.getType();
			if (type==Node.PROXY_NODE) {
				Object id = ((ProxyNode) subtree).getChildId();
				Node root = (Node) container.get(id);
				if (!newParentId.equals(root.getParentId())) { 
					root.setParentId(newParentId);
					container.update(id, root);
				}
				// else it was not necessary to update the parentId
			}
			Iterator it = subtree.getChildNodes();
			while (it.hasNext())
				updateParentIdsInChildSubtrees((Node) it.next(), newParentId, container);
		}
	}

	/**
	 * Important return code for insert/remove opperations.
	 */
	private static final int INSERTED_IN_RECORD = -4;
	/**
	 * Important return code for insert/remove opperations.
	 */
	private static final int JUST_INSERTED_IN_LOGICAL_TREE = -3;
	/**
	 * Important return code for insert/remove opperations.
	 * Codes greater than NOTHING_IMPORTANT_HAPPENED describing the number of occurances of MarkupNodes
	 * which tag names are equal to the first item in the current xPath expression.
	 * In other words: these return codes are used to count all occurances of a kind of
	 * MarkupNode in a subtree
	 */
	private static final int NOTHING_IMPORTANT_HAPPENED = -1;

	/**
	 * For debugging purposes.
	 */
	private static int testCaseNumber = -1;

	/**
	 * The container used for storage inside.
	 */
	private Container container;

	/**
	 *  Converter which converts a subtree into a sequence of bytes
	 * (used for determination of the size, if it fits into a record
	 * or not). 
	 */
	private SizeConverter subtreeConverter;

	/**
	 * Maximum size (in bytes) which the container is able to store.
	 */
	private int maxSubtreeSize;

	/**
	 * The strategy for splits.
	 */
	private Split split;

	/**
	 * The id referencing the root of the tree inside the container.
	 */
	private Object rootId=null;

	/**
	 * Contains the number of split operations which have occured
	 */
	public int numberOfSplits=0;

	/**
	 * Contains the number of markup, literal and attribute nodes inside the tree.
	 */
	public int numberOfNodes=0;

	/**
	 * Construktor which constructs a new tree inside the Container.
	 * @param recordManger the underlying Container/RecordManager
	 * @param page the page size the recordmanager is working with
	 * @param the used split strategy
	 */
	public EXTree(Container container, int maxRecordSize, Split split, SizeConverter subtreeConverter) {
		this.container = container;
		this.maxSubtreeSize = maxRecordSize;
		this.split = split;
		this.subtreeConverter = subtreeConverter;
	}

	/**
	 * Construktor which opens a new tree which is stored inside the
	 * Container.
	 * @param recordManger the underlying Container/RecordManager
	 * @param page the page size the recordmanager is working with
	 * @param the used split strategy
	 */
	public EXTree(Container container, int maxRecordSize, Object rootId, Split split, SizeConverter subtreeConverter) {
		this.container = container;
		this.maxSubtreeSize = maxRecordSize;
		this.split = split;
		this.rootId = rootId;
		this.subtreeConverter = subtreeConverter;
	}

	/**
	 * Inserts an attribute in the markup with the given xPath.
	 * @param xPath the xPath that points to the markup
	 * @param name the name of the attribute
	 * @param value the value of the attribute
	 * @return true if the attribute has been successfully inserted into the tree, false otherwise
	 */
	public boolean insertAttribute(XPathLocation xPath, String name, String value) {
		MarkupNode markup = new MarkupNode((String) name, true);
		LiteralNode literal = new LiteralNode(value.getBytes(), LiteralNode.STRING);
		markup.addChildNode(literal);
		return insert(xPath, markup);
	}

	/**
	 * Inserts a literal. The given String will be treated as a String. If it has a other
	 * type, i.e. Byte, Short, Integer, use the other insert method to specify a type.
	 * The getObject() method in LiteralNode return an Object according to this tpye. Default is String.
	 * @param xPath the xPath that points to the location (to the markup) where the literal has to be inserted.
	 * @param value the value of the literal
	 * @return true if the attribute has been successfully inserted into the tree, false otherwise
	 */
	public boolean insertLiteralNode(XPathLocation xPath, String value) {
		return insertLiteralNode(xPath, value, LiteralNode.STRING);
	}

	/**
	 * Inserts a literal. The bytes from the given String will be treated as i.e.
	 * Byte, Short, Integer, use the type parameter to specify a type, see the class LiteralNode for these types.
	 * The getObject() method in LiteralNode return an Object according to this tpye.
	 * @param xPath the xPath that points to the location (to the markup) where the literal has to be inserted.
	 * @param value the value of the literal
	 * @param type an int that indicates the type of the literal
	 * @return true if the attribute has successfully inserted into the tree, false otherwise
	 */
	public boolean insertLiteralNode(XPathLocation xPath, String value, int type) {
		LiteralNode node = new LiteralNode(value.getBytes(), type);
		return insert(xPath, node);
	}

	/**
	 * Inserts a markup-node into the tree.
	 * @param xPath the location (the parent-node) where to insert the markup
	 * @param tagName the tagName of this markup-node
	 * @return true if the markup-node has been successfully inserted into the tree, false otherwise
	 */
	public boolean insertMarkupNode(XPathLocation xPath, String tagName) {
		MarkupNode node = new MarkupNode((String) tagName);
		return insert(xPath, node);
	}

	/**
	 * Inserts the given node with the given xPath into this tree.
	 * If this tree is empty, the xPath will be ignored.
	 */
	private boolean insert(XPathLocation xPath, Node node) {
		//if the tree is emtpy, insert the serialized subtree into the container
		if (rootId==null) {
			rootId = container.insert(node);
			numberOfNodes=1;
			return true;
		}
		else {
			if (numberOfNodes==testCaseNumber) {
				System.out.println("Test case reached!");
				System.out.println(xPath);
				System.out.println(node); 
			}
			xPath.setMatchLevel(0);
			boolean b = insert(rootId, xPath, node, 1)==INSERTED_IN_RECORD;
			if (b)
				numberOfNodes++;
			return b;
		}
	}

	/**
	 * Writes the root Tree into a record and writes it into the Container.
	 * If necessary, a split is performed.
	 */
	private void writeAndSplitRoot(Node root) {
		int currentSize = subtreeConverter.getSerializedSize(root);
		
		while (true) {
			if (currentSize <= maxSubtreeSize) {
				container.update(rootId, root);
				break;
			}
			else {
				root = split.split(root, rootId); // now root is hopefully much smaller separator
				int newSize = subtreeConverter.getSerializedSize(root);
				if (newSize>=currentSize)
					throw new RuntimeException("root record does not become smaller after multiple splits");
				currentSize = newSize;
			}
		}
	}

	/**
	 * This method starts inserting at the beginning of a record. If the rootNode of
	 * this record is a attribut or a literal it isn't possible to continue, else
	 * this method calls the second private insert method.
	 */
	private int insert(Object tid, XPathLocation xPath, Node insertNode, int count) {
		Node root = (Node) container.get(tid);
		
		// what kind of node is root
		switch (root.getType()) {
		case Node.MARKUP_NODE:
			// if (!xPath.matchPart(((MarkupNode) root).getTagName()))
			//	return NOTHING_IMPORTANT_HAPPENED;
			// Walk further to the SCAFFOLD_NODE case to do the rest ... (no break here)
		case Node.SCAFFOLD_NODE:
			int ttemp = insert2(root, xPath, insertNode, count);

			if (ttemp==INSERTED_IN_RECORD)
				return INSERTED_IN_RECORD;
			if (ttemp==JUST_INSERTED_IN_LOGICAL_TREE) { //node was added into the logical tree
				int newSize = subtreeConverter.getSerializedSize(root);
				
				if (numberOfNodes==testCaseNumber)
					debugXMLOutput(testCaseNumber+"_","");
				
				if (newSize > maxSubtreeSize) { // we need a split
					if (tid.equals(rootId)) {
						// Splitting the root
						root = split.split(root, rootId);
						root.setParentId(null); // root is no longer a root of a subtree
						writeAndSplitRoot(root);
					}
					else {
						// Splitting an inner node
						Node test=null;
						Object parentId = root.getParentId();
						root = split.split(root, parentId);
						root.setParentId(null); // root is no longer a root of a subtree
						
						// The splited parts have new TIds, the separator itself will be inserted into the
						// father node. So the tid is no longer used.
						container.remove(tid);

						// insert the separator node (and so the complete subtree beginning with the separator)
						// into the parent-record.
						// in the logical tree it's simple, because we just need to overwrite
						// the proxy, that leads to this record, by the separator
						up(root, parentId, tid);
					}
					numberOfSplits++;
				}
				else
					container.update(tid, root);

				return INSERTED_IN_RECORD;
			}
			return ttemp;
			
		case Node.PROXY_NODE:
			// leads to another record
			return insert(((ProxyNode) root).getChildId(), xPath, insertNode, count);
		case Node.LITERAL_NODE:
			// cannot insert a child into a literal node, but a different node
			// can be found due to the fact that a scaffold node can be above the
			// literal node.
			return NOTHING_IMPORTANT_HAPPENED;
		default:
			throw new RuntimeException("Unknown node type inside the tree");
		}
	}

	/**
	 * Inserts a node into a subtree in main memory.
	 */
	private int insert2 (Node root, XPathLocation xPath, Node insertNode, int count) {
		int storedMatchLevel = xPath.getMatchLevel();
		switch (root.getType()) {
		case Node.MARKUP_NODE:
			if ( ((MarkupNode) root).isAttribute())
				return 0;

			int matchValue = xPath.matchPart( ((MarkupNode) root).getTagName(), count );
			
			if (matchValue>=0)
				return matchValue; // this is the number of matched nodes!
			
			if (matchValue==-1) {
				// the tag name is equal and the count matches
				if (xPath.isMatchingComplete()) {
					// the xPath location is reached, insert the new node
					root.addChildNode(insertNode);
					return JUST_INSERTED_IN_LOGICAL_TREE;
				}
			}
			
			// the xPath location isn't reached yet
			// matchValue ==-1 and not completly matched or
			// matchValue == -2 (\\ in XPath expression)
			// ==> go to all child nodes.
			count=1; // it is the first node below a MarkupNode
			// use the code of the scaffold node! (no break here!)
		case Node.SCAFFOLD_NODE:

			Iterator children = root.getChildNodes();
			int counter=count;
			while (children.hasNext()) {
				Node child = (Node) children.next();

				int retType = insert2(child, xPath, insertNode, counter);

				if (retType==INSERTED_IN_RECORD)
					return INSERTED_IN_RECORD;
				if (retType==JUST_INSERTED_IN_LOGICAL_TREE)
					return JUST_INSERTED_IN_LOGICAL_TREE;
				if (retType>=0)
					counter+=retType;
			}
			// Restore the original match level
			if (root.getType()==Node.MARKUP_NODE)
				xPath.setMatchLevel(storedMatchLevel);
			return (counter-count);

		case Node.PROXY_NODE: // leads to another record
			return insert(((ProxyNode) root).getChildId(), xPath, insertNode, count);

		case Node.LITERAL_NODE:
			return NOTHING_IMPORTANT_HAPPENED;
		default:
			throw new RuntimeException("Unknown node type inside the tree");
		}
	}

	/**
	 * Removes the Node with the given xPath. That includes removing
	 * of the complete subtree of node. Returns true if the remove
	 * was succesfull, false otherwise.
	 * @param xPath the xPath of the node that should be removed by this method
	 * @return true if the remove was successfull, false otherwise
	 */
	public boolean removeMarkup(XPathLocation xPath) {
		Node root = (Node) container.get(rootId);
		xPath.setMatchLevel(0);
		return removeMarkupInsideSubtree(root, null, root, -1, xPath, 1, rootId) ==INSERTED_IN_RECORD;
	}

	/**
	 * Removes all subtrees below the given node from the container.
	 * @param root node which children should be removed. 
	 */
	private void removeAllChildRecords(Node root) {
		// idea: search for PROXIES and go to the following subtrees and delete them,
		//       then go to the following subtrees of these subtrees, ...
		int type = root.getType();
		if ((type==Node.MARKUP_NODE)||(type==Node.SCAFFOLD_NODE)) {
			// markup and scaffold nodes are able to store children
			Iterator it = root.getChildNodes();
			while (it.hasNext())
				// call this method on every child-node (rekursion)
				removeAllChildRecords((Node) it.next()); 
		}
		else if (type==Node.PROXY_NODE) {
			// reconstruct the logical subtree
			Object newId = ((ProxyNode) root).getChildId();
			Node newRoot = (Node) container.get(newId);

			// ... and remove this element from the container
			container.remove(newId);
			// ... then call this method on the new root-node (rekursion)
			removeAllChildRecords(newRoot);
		}
	}

	/**
	 * Removes a markup inside a subtree.
	 */
	private int removeMarkupInsideSubtree(Node node, Node fatherNode, Node rootOfSubtree, int childIndex, XPathLocation xPath, int count, Object tid) {
		int storedMatchLevel = xPath.getMatchLevel();
		
		switch (node.getType()) {
		case Node.MARKUP_NODE:
			if ( ((MarkupNode) node).isAttribute())
				return 0;

			int matchValue = xPath.matchPart( ((MarkupNode) node).getTagName(), count );
			
			if (matchValue>=0)
				return matchValue; // this is the number of matched nodes!
			
			if (matchValue==-1) {
				if (xPath.isMatchingComplete()) {
					// the xPath location is reached, insert the new node
					// Remove subtree inside a (sub-)tree.
					removeAllChildRecords(node);
					
					if (node == rootOfSubtree) {
						if (tid.equals(rootId))
							// root of the whole tree
							rootId = null;
						else {
							Object parentId = rootOfSubtree.getParentId();
							// remove the whole subtree from the parent's subtree
							Node rootOfParentRecord = (Node) container.get(parentId);
							rootOfParentRecord.removeProxy(tid);
							container.update(parentId, rootOfParentRecord);
						}
						container.remove(tid);
					}
					else {
						fatherNode.getChildList().remove(childIndex);
						// and store the new subtree
						container.update(tid, rootOfSubtree);
					}

					return INSERTED_IN_RECORD;
				}
			}

			// the xPath location isn't reached yet
			// matchValue ==-1 and not completly matched or
			// matchValue == -2 (\\ in XPath expression)
			// ==> go to all child nodes.
			count=1; // it is the first node below a MarkupNode
			// use the code of the scaffold node! (no break here!)

		case Node.SCAFFOLD_NODE: 
			// a scaffold is like a markup node just without a tagname

			Iterator children = node.getChildNodes();
			int counter=count;
			int k=0;
			while (children.hasNext()) {
				Node child = (Node) children.next();
				int retType = removeMarkupInsideSubtree(child, node, rootOfSubtree, k, xPath, counter, tid);
				if (retType==INSERTED_IN_RECORD)
					return INSERTED_IN_RECORD;
				if (retType==JUST_INSERTED_IN_LOGICAL_TREE)
					return JUST_INSERTED_IN_LOGICAL_TREE;
				if (retType>=0) counter+=retType;
				k++;
			}
			// Restore the original match level
			if (node.getType()==Node.MARKUP_NODE)
				xPath.setMatchLevel(storedMatchLevel);
			return (counter-count);

		case Node.PROXY_NODE: // leads to another record
			Object newId = ((ProxyNode) node).getChildId();
			Node newRoot = (Node) container.get(newId);
			return removeMarkupInsideSubtree(newRoot, null, newRoot, -1, xPath, count, newId); 

		case Node.LITERAL_NODE:
			return NOTHING_IMPORTANT_HAPPENED;
		default:
			throw new RuntimeException("Unknown node type inside the tree");
	}
	}

	/**
	 * Writes the current XML-Document to the PrintStream (in correct XML syntax).
	 * @param out the PrintStream to which the XML-Code will be written.
	 * @param withStructure determines if the proxy and scaffold nodes
	 * 		are also written to XML.
	 */
	public void toXML(PrintStream out, boolean withStructure) {
		out.println("<?xml version=\"1.0\"?>");
		if (rootId!=null)
			getRootNode().toXML(
				rootId, out, withStructure,
				new AbstractFunction() {
					public Object invoke(Object o) {
						return (Node) container.get(o);
					}
				}
			);
	}

	/**
	 * Writes the current XML-Document to the OutputStream (in correct XML syntax).
	 * @param out the OutputStream to which the XML-Code will be written.
	 * @param withStructure determines if the proxy and scaffold nodes
	 * 		are also written to XML.
	 */
	public void toXML(OutputStream out, boolean withStructure) {
		toXML(new PrintStream(out), withStructure);
	}

	/* Important for DOM */
	public Iterator getXMLChildren(Node node) {
		
		List list = node.getChildList();
		if (list==null)
			return null; // EmptyCursor.DEFAULT_INSTANCE;
		else {
			java.util.Vector v = new java.util.Vector(list); 
			Iterator it = v.iterator();
			
			while(it.hasNext()) {
				Node child =  (Node) it.next();
				int type = child.getType(); 
				
				if (type == Node.PROXY_NODE)
					replaceProxy(child, list);
			}
			
			return list.iterator();
		}
	}

	/* Called from getXMLChildren */
	private void replaceProxy(Node proxy, List list) {
		// Knoten aus der liste entfernen:
		int pos = list.indexOf(proxy);
		
		list.remove(proxy);
		// Durch Proxy Verlinkte Knoten einfügen:
		Object nextRecordId = ((ProxyNode)proxy).getChildId();
		
		Function getNodeInRecordFunction = 
			new AbstractFunction() {
				public Object invoke(Object nextRecordId) {
					return (Node) container.get(nextRecordId);
				}
			};
		
		Node nextNode = (Node) getNodeInRecordFunction.invoke(nextRecordId);
		
		if (nextNode.getType() == Node.SCAFFOLD_NODE) {
						 
			Iterator it = nextNode.getChildNodes();
		
			while(it.hasNext()) {
		
				Node child =  (Node) it.next();
				int type = child.getType(); 
		
				if (type == Node.PROXY_NODE) {
					int size = list.size();
					list.add(pos, child);
					replaceProxy(child, list);
					pos = pos + list.size() - size;
				}
				else {
					list.add(pos, child);
					pos++;
				}
			}
		}
		else if (nextNode.getType() == Node.PROXY_NODE) {
			int size = list.size();
			list.add(pos, nextNode);
			replaceProxy(nextNode, list);	
			pos = pos + list.size() - size;		
		}
		else {
			list.add(pos, nextNode);
			pos++;
		}
	}

	/**
	 * Tries to insert the separator subtree into the record given by pTid.
	 * This is done by overwriting the proxy, that leads to the record with oldTid, by the
	 * separator node.
	 */
	private void up(Node separator, Object pTId, Object oldTId) {
		Node root = (Node) container.get(pTId);
		//update the internal Ids of the subtree beginning with root

		// Put the separator tree into the tree instead of the proxy
		if (root.replaceProxyByNode(oldTId, separator)) { 

			// if (root.containsProxyTId(oldTId)) System.out.println("merkwürdig!!!");
			
			if (pTId.equals(rootId))
				writeAndSplitRoot(root);
			else { 
				// it is not the root-record, just a normal record in the "tree"
				int currentSize = subtreeConverter.getSerializedSize(root);
				
				if (currentSize <= maxSubtreeSize)
					container.update(pTId, root);
				else {
					// the new Record is to big, we need a split
					// The splited parts have new TIds, the separator itself will be inserted into the
					// father node. So the pTId is no longer used.
					container.remove(pTId);

					Node newSeparator = split.split(root, root.getParentId());
					up(newSeparator, root.getParentId(), pTId);
				}
			}
		}
		else
			throw new RuntimeException("Proxy could not be found. This case should never occur!!!");
	}

	/**
	 * Returns a predicate that outputs the structure of the tree
	 * in a short form to a PrintStream (to be used in conjunction with treeTraversal).
	 * @param ps The PrintStream used for the output.
	 */
	public static Predicate<Object> getStructureOutputPredicate(final PrintStream ps) {
		return new AbstractPredicate<Object>() {
			public boolean invoke(List<? extends Object> list) {
				if (list.size()<=2)
					return true;
				String s="";
				if (!((Boolean) list.get(1)).booleanValue())
					s = "/";
				Node node = (Node) list.get(2);
				switch (node.getType()) {
				case Node.LITERAL_NODE:
					ps.print("L");
					break;
				case Node.MARKUP_NODE: 
					if (((MarkupNode) node).isAttribute())
						ps.print("<"+s+"A>");
					else
						ps.print("<"+s+"M>");
					break;
				case Node.PROXY_NODE:
					ps.print("<"+s+"P>");
					break;
				case Node.SCAFFOLD_NODE:
					ps.print("<"+s+"S>");
					break;
				}
				return true;
			}
		};
	}

	/**
	 * Traverses the tree beginning at root and
	 * calls a predicate with 2-4 parameters.
	 * 
	 * The function is called with two parameter, when a new
	 * subtree is entered. The first parameter is the
	 * identifyer inside the container, the second parameter 
	 * is the root of the new subtree which is entered.
	 * 
	 * The function is called with three or four parameters, when
	 * a node is detected. The parameters are:
	 * <ul>
	 * <li>The identifyer of the subtree in the container</li>
	 * <li>Boolean.TRUE/Boolean.FALSE: is it the start or the end of a tag
	 *		(literal nodes only one function call with Boolean.TRUE)</li>
	 * <li>The current node.</li>
	 * <li>For the start tag of proxy nodes only: the subtree to which the proxy points.</li>
	 * </ul>
	 * 
	 * If the predicate returns false, then the subtree of the current node is not entered.
	 *
	 * @param root root of the current subtree.
	 * @param predicate Predicate which is called during tree traversal.
	 */
	public void treeTraversal(Object id, Node root, Predicate predicate) {
		switch (root.getType()) {
		case Node.LITERAL_NODE:
			predicate.invoke(Arrays.asList(id, Boolean.TRUE, root));
			return;
		case Node.MARKUP_NODE:
		case Node.SCAFFOLD_NODE:
			if (predicate.invoke(Arrays.asList(id, Boolean.TRUE, root))) {
				Iterator it = root.getChildNodes();
				while (it.hasNext())
					treeTraversal(id, (Node) it.next(), predicate);
			}
			predicate.invoke(Arrays.asList(id, Boolean.FALSE, root));
			return;
		case Node.PROXY_NODE:
			Object childId = ((ProxyNode)root).getChildId();
			Node childSubtree = (Node) container.get(childId);
			if (predicate.invoke(Arrays.asList(id, Boolean.TRUE, root, childSubtree)))
				if (predicate.invoke(id, childSubtree))
					treeTraversal(childId, childSubtree, predicate);
			predicate.invoke(Arrays.asList(id, Boolean.FALSE, root));
			return;
		}
	}

	/**
	 * Traverses the whole tree beginning at the root and
	 * calls a predicate with 2-4 parameters.
	 * 
	 * The function is called with two parameter, when a new
	 * subtree is entered. The first parameter is the
	 * identifyer inside the container, the second parameter 
	 * is the root of the new subtree which is entered.
	 * 
	 * The function is called with three or four parameters, when
	 * a node is detected. The parameters are:
	 * <ul>
	 * <li>The identifyer of the subtree in the container</li>
	 * <li>Boolean.TRUE/Boolean.FALSE: is it the start or the end of a tag
	 *		(literal nodes only one function call with Boolean.TRUE)</li>
	 * <li>The current node.</li>
	 * <li>For the start tag of proxy nodes only: the subtree to which the proxy points.</li>
	 * </ul>
	 * 
	 * If the predicate returns false, then the subtree of the current node is not entered.
	 *
	 * @param predicate Predicate which is called during tree traversal.
	 */
	public void treeTraversal(Predicate predicate) {
		if (rootId!=null) {
			Node node = (Node) container.get(rootId);
			if (predicate.invoke(rootId, node))
				treeTraversal(rootId, node, predicate);
		}
	}

	/**
	 * Queries the tree for an XPathLocation.
	 * @param xp The XPathLocation which is tried to match.
	 */
	public Cursor query (final XPathLocation xp) {
		final AsynchronousChannel channel = new AsynchronousChannel();
		
		Thread t = new Thread() {
			public void run() {
				xp.setMatchLevel(0);
				final Stack numberStack = new Stack();
				numberStack.push(new HashMap());
				
				treeTraversal(
					new AbstractPredicate<Object>() {
						XPathLocation currentXPL = new XPathLocation();
						public boolean invoke(Object o1, Object o2) {
							return true;
						}
						public boolean invoke(List<? extends Object> list) {
							Node node = (Node) list.get(2);
							if (list.get(1).equals(Boolean.TRUE)) {
								
								if (node.getType()==Node.MARKUP_NODE) {
									MarkupNode mnode = (MarkupNode) node;
									String tagName = mnode.getTagName();
		
									HashMap map = (HashMap) numberStack.peek();
									int number=1;
									Integer intNumber = (Integer) map.get(tagName);
									if (intNumber!=null)
										number = intNumber.intValue();
		
									currentXPL.append(tagName, number, false);
									map.put(tagName, new Integer(number+1));
									numberStack.push(new Integer(xp.getMatchLevel()));
									numberStack.push(new HashMap());
									
									if (xp.getMatchLevel()<xp.getNumberOfParts()) {
										int matchValue = xp.matchPart(tagName, number); 
										if (matchValue==-1) {
											if (xp.isMatchingComplete())
												channel.put(currentXPL.clone());
										}
										else
											return (matchValue==-2);
									}
								}
							}
							else {
								if (node.getType()==Node.MARKUP_NODE) {
									currentXPL.removeLast();
									numberStack.pop();
									xp.setMatchLevel(((Integer) numberStack.pop()).intValue());
								}
							}
							return true;
						}
					}
				);
				numberStack.pop(); // not necessary, but it should always work!
				channel.put(null);
			}
		};
		t.start();
		
		return new ChannelCursor(channel);
	}

	/**
	 * Returns the identifyer of the root inside the container.
	 * @return Object the root identifyer.
	 */
	public final Object getRootId() {
		return rootId;
	}

	/**
	 * Sets a new root identifyer inside the container.
	 * @param id the new identifyer
	 */
	public final void setRootId(Object id) {
		rootId = id;
	}

	/**
	 * Returns the absolute root node of the tree.
	 * @return Node the root node.
	 */
	public final Node getRootNode() {
		return (Node) container.get(rootId);
	}
	
	/**
	 * Returns the container which is used for the tree.
	 * @return Container the container.
	 */
	public final Container getContainer() {
		return container;
	}

	/**
	 * Returns the converter which is used for determining the
	 * size of subtrees.
	 * @return SizeConverter the subtree converter
	 */
	public final SizeConverter getSubtreeConverter() {
		return subtreeConverter;
	}

	/**
	 * Returns the maximum size of a subtree inside the container
	 * in bytes.
	 * @return the maximum subtree size in bytes.
	 */
	public final int getMaxSubtreeSize() {
		return maxSubtreeSize;
	}

	/**
	 * Outputs the current tree to a file with a name before+numberOfSplits+after+".xml".
	 * @param before String used for creation of the filename. 
	 * @param after String used for creation of the filename.
	 */
	private void debugXMLOutput(String before, String after) {
		System.out.println("Debug output");
		System.out.println("numberOfSplits="+numberOfSplits);
		System.out.print("Writing XML with toXML()...");
		try {
			FileOutputStream fos = new FileOutputStream(before+numberOfSplits+after+".xml"); 
			toXML(fos,true);
			fos.close();
		}
		catch (Exception e) {
			throw new WrappingRuntimeException(e);
		}
		System.out.println("done.");
	}

	/**
	 * Result type for checkTreeQuality. To get the results, 
	 * check the fields directly or call the toString method.
	 */	
	public static class TreeQuality {
		int maxContainerHeight;	// in number of container entries
		int maxTreeHeight;			// in number of nodes
		int countUnderfills;
		int countProxyBeginningOfSubtree;
		int countLiteralBeginningOfSubtree;
		int countScaffoldWithoutChildren;
		int countScaffoldWithOneChild;
		int countScaffoldBelowScaffold;
		int countScaffoldBelowMarkup;
		int nodeCounter[] = new int[4];
		int attributeCounter; 		// attributes are also counted as MarkupNodes
		
		public String toString() {
			return
				"maxContainerHeight:\t"+maxContainerHeight+"\t"+
				"maxTreeHeight:\t"+maxTreeHeight+"\t"+
				"MarkupNodes:\t"+nodeCounter[Node.MARKUP_NODE-1]+"\t"+
				"Attributes:\t"+attributeCounter+"\t"+
				"LiteralNodes:\t"+nodeCounter[Node.LITERAL_NODE-1]+"\t"+
				"ProxyNodes:\t"+nodeCounter[Node.PROXY_NODE-1]+"\t"+
				"ScaffoldNodes:\t"+nodeCounter[Node.SCAFFOLD_NODE-1]+"\t"+
				"Underfills:\t"+countUnderfills+"\t"+
				"ProxyNode is beginning of subtree:\t"+countProxyBeginningOfSubtree+"\t"+
				"LiteralNode is beginning of subtree:\t"+countLiteralBeginningOfSubtree+"\t"+
				"ScaffoldNode without children:\t"+countScaffoldWithoutChildren+"\t"+
				"ScaffoldNode with only one child node:\t"+countScaffoldWithOneChild+"\t"+
				"A ScaffoldNode below a ScaffoldNode:\t"+countScaffoldBelowScaffold+"\t"+
				"A ScaffoldNode below a MarkupNode:\t"+countScaffoldBelowMarkup;
		}
	}

	/**
	 * Checks the quality of the EXTree. To output the result, it is 
	 * convenient to call the toString method of the result.
	 */
	public TreeQuality checkTreeQuality() {
		// sizeHistogram
		final Stack path = new Stack();
		final TreeQuality quality = new TreeQuality();
		
		treeTraversal(
			new AbstractPredicate<Object>() {
				int currentTreeHeight=0;
				int currentContainerHeight=0;
				
				public boolean invoke (Object id, Object o) {
					Node node = (Node)o;
					if (subtreeConverter.getSerializedSize(node)<maxSubtreeSize/5)
						quality.countUnderfills++;
					if (node.getType()==Node.PROXY_NODE)
						quality.countProxyBeginningOfSubtree++;
					if (node.getType()==Node.LITERAL_NODE)
						quality.countLiteralBeginningOfSubtree++;
					return true;
				}
				public boolean invoke (List<? extends Object> list) {
					Node node = (Node) list.get(2);

					if (list.get(1).equals(Boolean.TRUE)) {
						currentTreeHeight++;
						if (currentTreeHeight>quality.maxTreeHeight)
							quality.maxTreeHeight = currentTreeHeight;
						
						quality.nodeCounter[node.getType()-1]++;
						
						switch (node.getType()) {
						case Node.MARKUP_NODE:
							if (((MarkupNode)node).isAttribute())
								quality.attributeCounter++;
							break;
						case Node.SCAFFOLD_NODE:
							if (node.getNumberOfChildren()==0)
								quality.countScaffoldWithoutChildren++;
							if (node.getNumberOfChildren()==1)
								quality.countScaffoldWithOneChild++;
							
							Node lastNodeInPath = (Node) path.peek(); 
							if (lastNodeInPath.getType()==Node.SCAFFOLD_NODE)
								quality.countScaffoldBelowScaffold++;
							if (lastNodeInPath.getType()==Node.MARKUP_NODE)
								quality.countScaffoldBelowMarkup++;
							break;
						case Node.PROXY_NODE:
							if (list.size()==4) {
								currentContainerHeight++;
								if (currentContainerHeight>quality.maxContainerHeight)
									quality.maxContainerHeight = currentContainerHeight;
								
								Node subtree = (Node) list.get(3);
								if (subtree.parentId==null || !subtree.parentId.equals(list.get(0)))
									throw new RuntimeException("Tree is invalid, parentId is wrong."); 
							}
							else
								throw new RuntimeException("Should never occur");
							break;
						}
						
						if (node.getType()==Node.LITERAL_NODE) {
							currentTreeHeight--;
						}
						else
							path.push(node);
					}
					else {
						currentTreeHeight--;
						if (node.getType()==Node.PROXY_NODE)
							currentContainerHeight--;
						path.pop();
					}
					return true;
				}
			}
		);
		
		if (path.isEmpty())
			return quality;
		else
			throw new RuntimeException("Path is not empty after traversal");
	}
// execute SAX events on a DefaultHandler from a XPathLocation

}
