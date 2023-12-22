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

package xxl.applications.xml.storage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import xxl.core.collections.containers.Container;
import xxl.core.collections.containers.io.BlockFileContainer;
import xxl.core.collections.containers.io.ConverterContainer;
import xxl.core.collections.containers.recordManager.FirstFitStrategy;
import xxl.core.collections.containers.recordManager.IdentityTIdManager;
import xxl.core.collections.containers.recordManager.RecordManager;
import xxl.core.collections.containers.recordManager.Strategy;
import xxl.core.collections.containers.recordManager.TId;
import xxl.core.io.converters.FixedSizeConverter;
import xxl.core.util.XXLSystem;
import xxl.core.xml.storage.EXTree;
import xxl.core.xml.storage.SimpleSplit;
import xxl.core.xml.storage.SubtreeConverter;
import xxl.core.xml.storage.dom.Attr;
import xxl.core.xml.storage.dom.Document;
import xxl.core.xml.storage.dom.DocumentBuilder;
import xxl.core.xml.storage.dom.DocumentBuilderFactory;
import xxl.core.xml.storage.dom.Element;
import xxl.core.xml.storage.dom.Node;
import xxl.core.xml.storage.dom.Text;

/**
 * This class tests some features of the DOM implementation on top
 * of our native XML storage.
 */
public class PersistentDOMTest {
	
	public static final int MODE_BUILD=1;
	public static final int MODE_USE=2;
	
	/** No instance allowed */
	private PersistentDOMTest() {
	}
	
	/**
	 * Writes the root of the Tree into a file.
	 */
	public static Object readRootID(RecordManager recordManager,String configFilename) {
		try {
			TId rootID = null;
	        
			DataInputStream d = new DataInputStream(new FileInputStream(configFilename));
			rootID = (TId) recordManager.objectIdConverter().read(d,null);
			d.close();
	
			return rootID;
		}
		catch (Exception e) {
			throw new RuntimeException("Config file could not be read");
		}
	}
    
	/**
	 * Reads the root of the Tree from a file.
	 */
	private static void writeRootID(RecordManager recordManager, Object rootID, String configFilename) {
		try {
			DataOutputStream d = new DataOutputStream(new FileOutputStream(configFilename));
			recordManager.objectIdConverter().write(d,rootID);
			
			d.close();
		}
		catch (Exception e) {
			throw new RuntimeException("Config file could not be read");
		}
	}
	
	/**
	 * Tests a node of out DOM layer against the DOM node of the currently used DOM parser.
	 */
	public static String testNode(xxl.core.xml.storage.dom.Node node, org.w3c.dom.Node referNode, String tag, String attr){
		String result = "\n";
		
		if (node == null || referNode == null) return "A node is null! xxl = " + node + " org = " + referNode;
		
		// Abfrage von algemeinen Funktionen
		short type = node.getNodeType();
		if (type != referNode.getNodeType())
			return "incompatible Nodes! " + type + " != " + referNode.getNodeType();
		
		switch(type){
		case Node.TEXT_NODE: 			result += "Node Type:" + replicate(" ", 20)+ "TEXT_NODE =?= TEXT_NODE\n"; break;
		case Node.ATTRIBUTE_NODE: 		result += "Node Type:" + replicate(" ", 20)+ "ATTRIBUTE_NODE =?= ATTRIBUTE_NODE\n"; break;
		case Node.ELEMENT_NODE: 		result += "Node Type:" + replicate(" ", 20)+ "ELEMENT_NODE =?= ELEMENT_NODE\n"; break;
		case Node.CDATA_SECTION_NODE: 	result += "Node Type:" + replicate(" ", 20)+ "CDATA_SECTION_NODE =?= CDATA_SECTION_NODE\n"; break;
		case Node.COMMENT_NODE: 		result += "Node Type:" + replicate(" ", 20)+ "COMMENT_NODE =?= COMMENT_NODE\n"; break;
		case Node.DOCUMENT_NODE: 		result += "Node Type:" + replicate(" ", 20)+ "DOCUMENT_NODE =?= DOCUMENT_NODE\n"; 
		}
		
		result += "Node Name:" + replicate(" ", 20) + node.getNodeName() + " =?= " + referNode.getNodeName() + "\n";  
		result += "Node Value:" + replicate(" ", 19) + node.getNodeValue() + " =?= " + referNode.getNodeValue() + "\n";
		result += "Node OwnerDoc:" + replicate(" ", 16) + node.getOwnerDocument() + " =?= " + referNode.getOwnerDocument() + "\n";
		result += "Node Parent:" + replicate(" ", 18) + getName(node.getParentNode()) + " =?= " + getName(referNode.getParentNode()) + "\n";
		result += "Node first Child:" + replicate(" ", 13) + getName(node.getFirstChild()) + " =?= " + getName(referNode.getFirstChild()) + "\n";
		result += "Node last Child:" + replicate(" ", 14) + getName(node.getLastChild()) + " =?= " + getName(referNode.getLastChild()) + "\n";
		result += "Node next Sibling:" + replicate(" ", 12) + getName(node.getNextSibling()) + " =?= " + getName(referNode.getNextSibling()) + "\n";
		result += "Node prev Sibling:" + replicate(" ", 12) + getName(node.getPreviousSibling()) + " =?= " + getName(referNode.getPreviousSibling()) + "\n";
		result += "Node has Attributes:" + replicate(" ", 10) + node.hasAttributes() + " =?= " + referNode.hasAttributes() + "\n";
		result += "Node has Childs:" + replicate(" ", 14) + node.hasChildNodes() + " =?= " + referNode.hasChildNodes() + "\n";
		result += "Node get Attributes:" + replicate(" ", 10) + namedNodeMapToString(node.getAttributes()) + "\n" + replicate(" ", 30) + namedNodeMapToString(referNode.getAttributes()) + "\n";
		result += "Node get Childs:" + replicate(" ", 14) + nodeListToString(node.getChildNodes()) + " \n" + replicate(" ", 30) + nodeListToString(referNode.getChildNodes()) + "\n";
		
		 		
		// Document spezifische Funktionen
		if (type == Node.DOCUMENT_NODE) {
			result += "\nThis is a Document Node -> specific functions:\n\n";
			Document doc 		= (Document) node; 
			org.w3c.dom.Document referDoc 	= (org.w3c.dom.Document) referNode;
			result += "Node get DocElement:" + replicate(" ", 10) + getName(doc.getDocumentElement())  + " =?= " + getName(referDoc.getDocumentElement())  + "\n";
			result += "Node get by Tag '" + tag + "':" + replicate(" ", 11-tag.length()) + nodeListToString(doc.getElementsByTagName(tag))  + " \n" + replicate(" ", 30) + nodeListToString(referDoc.getElementsByTagName(tag))  + "\n";
			
		}
		
		// Element spezifische Funktionen
		if (type == Node.ELEMENT_NODE) {
			result += "\nThis is a Element Node -> specific functions:\n\n";
			Element elem = (Element) node; 
			org.w3c.dom.Element referElem = (org.w3c.dom.Element) referNode;
			result += "Node Tag Name:" + replicate(" ", 16) + elem.getTagName() + " =?= " + referElem.getTagName() + "\n";  
			result += "Node has Attr '"+ attr +"':" + replicate(" ", 13-attr.length()) + elem.hasAttribute(attr)  + " =?= " + referElem.hasAttribute(attr)  + "\n";
			result += "Node get Attr '"+ attr +"':" + replicate(" ", 12-attr.length()) + elem.getAttribute(attr)  + " =?= " + referElem.getAttribute(attr)  + "\n";
			result += "Node get Attr Node '"+ attr +"':" + replicate(" ", 8-attr.length()) + getName(elem.getAttributeNode(attr))  + " =?= " + getName(referElem.getAttributeNode(attr))  + "\n";
			result += "Node get by Tag: '" + tag + "'" + replicate(" ", 11-tag.length()) + nodeListToString(elem.getElementsByTagName(tag))  + " \n" + replicate(" ", 30) + nodeListToString(referElem.getElementsByTagName(tag))  + "\n";
		}
		
		// Attribute spezifische Funktionen
		if (type == Node.ATTRIBUTE_NODE) {
			result += "\nThis is a Attr Node -> specific functions:\n\n";
			Attr attrNode = (Attr) node; 
			org.w3c.dom.Attr referAttrNode 	= (org.w3c.dom.Attr) referNode;
			result += "Node Name:" + replicate(" ", 20) + attrNode.getName() + " =?= " + referAttrNode.getName() + "\n";  
			result += "Node get Owner Elem:" + replicate(" ", 10) + getName(attrNode.getOwnerElement())  + " =?= " + getName(referAttrNode.getOwnerDocument())  + "\n";
			result += "Node get Specified:" + replicate(" ", 11) + attrNode.getSpecified()  + " =?= " + referAttrNode.getSpecified()  + "\n";
			result += "Node get Value:" + replicate(" ", 15) + attrNode.getValue() + " =?= " + referAttrNode.getValue()  + "\n";
		}		
		
		return result;
	}
	
	private static String getName(org.w3c.dom.Node node) {
		return ((node == null) ? "null" : node.getNodeName());
	}
	
	private static String namedNodeMapToString (org.w3c.dom.NamedNodeMap map) {
		if (map == null)
			return "null";
		
		String result = "";
		int count = map.getLength();
		for (int i = 0; i < count; i++)
			result += map.item(i).getNodeName()+  ( (i==count-1) ? "" : ", "); 
		
		return result;
	}
	
	private static String nodeListToString (org.w3c.dom.NodeList list) {
		if (list == null) 
			return "null";
		
		String result = "";
		int count = list.getLength();
		for (int i = 0; i < count; i++)
			result += list.item(i).getNodeName() + ( (i==count-1) ? "" : ", "); 
		
		return result;
	}
	
	private static String replicate(String str, int count){
		String result = "";
		for (int i = 0; i < count; i++ )
			result += str; 
		return result;
	}

	/**
	 * Starts the test.
	 * @param args the command line arguments
	 */
	public static void main(String[] args)throws Exception {
		
		System.out.println("DOM for EXTree test");
		
		String sourceFilename =
			XXLSystem.getDataPath(new String[]{"xml"})+System.getProperty("file.separator")+"database.xml";
		String containerFilename = 
			XXLSystem.getOutPath(new String[]{"xml"})+System.getProperty("file.separator")+"xmlContainer";
		String configFilename = 
			XXLSystem.getOutPath(new String[]{"xml"})+System.getProperty("file.separator")+"xmlRootDescriptor.mtd";
		
		Container container, bfc;
		Strategy strategy;
		RecordManager recordManager;
		EXTree tree;
		
		int pageSize = 4096;

		int mode = MODE_BUILD;
		if (args.length==1)
			mode = Integer.parseInt(args[0]);
		
		if (mode==MODE_BUILD) {
			System.out.println("Mode: build EXTree");
			bfc = new BlockFileContainer(containerFilename, pageSize);
		}
		else {
			System.out.println("Mode: use EXTree inside record manager");
			bfc = new BlockFileContainer(containerFilename);
		}
		
		FixedSizeConverter idConverter = bfc.objectIdConverter(); 
		SubtreeConverter converter = xxl.core.xml.storage.Node.getSubtreeConverter(TId.getConverter(idConverter));
		strategy = new FirstFitStrategy();
		recordManager = new RecordManager(bfc, pageSize, strategy, new IdentityTIdManager(idConverter), 0);
		int maxObjectSize = recordManager.getMaxObjectSize();
		container = new ConverterContainer(recordManager, converter);
		
		if (mode==MODE_BUILD)
			tree = new EXTree (container, maxObjectSize, new SimpleSplit(recordManager, converter, pageSize), converter);
		else
			tree = new EXTree (container, maxObjectSize, readRootID(recordManager,configFilename), new SimpleSplit(recordManager, converter, pageSize), converter);

		tree.toXML(
			new DataOutputStream(
				new FileOutputStream(
					XXLSystem.getOutPath()+System.getProperty("file.separator")+"debugOutput.xml"
				)
			),
			false
		);

		DocumentBuilderFactory dBFactory = new DocumentBuilderFactory();
		dBFactory.setAttribute("EXTree",tree);
		dBFactory.setAttribute("recordManager",recordManager);
		dBFactory.setAttribute("configFilename",configFilename);
		
		DocumentBuilder dBuilder = (xxl.core.xml.storage.dom.DocumentBuilder)dBFactory.newDocumentBuilder();
		xxl.core.xml.storage.dom.Document doc;
		
		if (mode==MODE_BUILD)
			doc = (xxl.core.xml.storage.dom.Document) dBuilder.parse(sourceFilename);
		else
			doc = (xxl.core.xml.storage.dom.Document) dBuilder.parse(
				new xxl.core.xml.storage.dom.StorageInputSource()
			);
		
		// instance standart parser
		javax.xml.parsers.DocumentBuilderFactory testFactory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
		javax.xml.parsers.DocumentBuilder testBuilder = testFactory.newDocumentBuilder();
		org.w3c.dom.Document testDoc = testBuilder.parse(sourceFilename);
		
		
		/***************** testing ********************/
		
		// Document methods
		System.out.println("\n================== Document Test  ==================");
		
		System.out.println ( testNode(doc, testDoc, "web",  ""));
		
				
		// ROOT Element methods
		System.out.println("\n================== ROOT Elememt Test  ===================");
		
		org.w3c.dom.Element rootElement = (Element)doc.getDocumentElement();
		org.w3c.dom.Element testRootElement = testDoc.getDocumentElement();
		
		System.out.println ( testNode((Node)rootElement, (org.w3c.dom.Node)testRootElement, "row",  "url"));
		
			
		// Element methods
		System.out.println("\n================== Elememt Test  ===================");
		

		org.w3c.dom.Element element = (Element)rootElement.getFirstChild();
		org.w3c.dom.Element testElement = (org.w3c.dom.Element) testRootElement.getFirstChild();
		
		System.out.println ( testNode((Node)element, (org.w3c.dom.Node)testElement, "*",  "sqltype"));
				
	
		// Attr methods
		System.out.println("\n================== Attribut Test  ===================");
		
		org.w3c.dom.Attr attr = ((Element) element.getFirstChild().getFirstChild()).getAttributeNode("precision");
		org.w3c.dom.Attr testAttr = (org.w3c.dom.Attr) ( (org.w3c.dom.Element) testElement.getFirstChild().getFirstChild()).getAttributeNode("precision");
		
		System.out.println ( testNode((Node)attr, (org.w3c.dom.Node)testAttr, "",  "signed"));
		
				
        // Text methods
		System.out.println("\n================== Text Test  ===================");
		
		org.w3c.dom.Text text = (Text)element.getFirstChild().getFirstChild().getFirstChild();
		org.w3c.dom.Text testText = (org.w3c.dom.Text) testElement.getFirstChild().getFirstChild().getFirstChild();
		
		System.out.println ( testNode((Node)text, (org.w3c.dom.Node)testText, "",  ""));
		/*			
		System.out.println("Text getNodeName() : " + text.getNodeName() + " =?= " + testText.getNodeName());
		System.out.println("Attr getNodeType() : " + text.getNodeType() + " =?= " + testText.getNodeType());
		System.out.println("Attr getNodeValue() : " + text.getNodeValue() + " =?= " + testText.getNodeValue());
		System.out.println("Attr getFirstChild() : " + text.getFirstChild() + " =?= " + testText.getFirstChild());
		System.out.println("Attr getLastChild() : " + text.getLastChild() + " =?= " + testText.getLastChild());
		System.out.println("Attr getNextSibling().getNodeName : " +  ((text.getNextSibling()==null)?null:text.getNextSibling().getNodeName())
											+ " =?= " +  ((testText.getNextSibling()==null)?null:testText.getNextSibling().getNodeName()));
		System.out.println("Attr getPreviousSibling() : " + text.getPreviousSibling() + " =?= " + testText.getPreviousSibling());
		System.out.println("Attr getOwnerDocument() : " + text.getOwnerDocument() + " =?= " + testText.getOwnerDocument());
		System.out.println("Attr getParentNode().getNodeName() : " + ((text.getParentNode()==null)?null:text.getParentNode().getNodeName())
															+ " =?= " + ((testText.getParentNode()==null)?null:testText.getParentNode().getNodeName()));
		System.out.println("Attr hasAttributes() : " + text.hasAttributes() + " =?= " + testText.hasAttributes());
		System.out.println("Attr hasChildNodes() : " + text.hasChildNodes() + " =?= " + testText.hasChildNodes());
		*/  
		
		writeRootID(recordManager,tree.getRootId(),configFilename);
		recordManager.close();
		container.close();
		
		System.out.println("\nTest completed");
	}
}
