/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.xml.operators;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import xxl.core.util.WrappingRuntimeException;
import xxl.core.util.concurrency.Channel;

/**
 * This class implements a SAX Handler to receive the messages 
 * created by parsing XML document first, all the nodes with 
 * the given name will be selected. Then several smaller 
 * XML documents (in comparison with the one input XML document) 
 * will be created (as DOM documents), whose root is the previously 
 * selected node.
 * The resulting documents are written to a channel.
 * It might be necessary to run the parsing in its own thread
 * (depends on the channel used).
 */
public class DOMTreeContentHandler extends DefaultHandler  {

	/** DOMDocument(treeModel)to be created */
	protected Document doc;

	/** Current node of the input XML-document(the document,which is being parsed) */
	protected Element currentNode;

	/** name of current node */
	protected String elementName;

	/** channel, which can contain the result documents after parsing the input XML-document */
	protected Channel channel;

	/** to fix the URI for prefixMapping */
	protected Map namespaceMappings;

	/** to memorise the deep of the subtree*/
	protected int level;

	/** to create Transformer objects*/
	protected static TransformerFactory factory = TransformerFactory.newInstance();

	/** to create  DOM Document Objects*/
	protected DocumentBuilder dbuild = null;

	/**
	 * create a new DOMTreeContentHandler
	 * @param elementName the name of the elements, which will be selected
	 * @param channel channel, to which the resultdocuments will be put 
	 */
	public DOMTreeContentHandler(String elementName, Channel channel) { 
		this.elementName = elementName;
		this.namespaceMappings = new HashMap();
		this.channel = channel;
		this.level = -1;
		try {
			dbuild = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		}
		catch (ParserConfigurationException e) {
			throw new WrappingRuntimeException(e); 
		}
		doc = dbuild.newDocument();
		// this.currentNode = doc.createElement(elementName);
	}

	/**
	 * Receive notification of the beginning of a document
	 * but there are no visible events in our implementation.
	 * @throws SAXException not thrown inside. 
	 */
	public void startDocument()throws SAXException {
	}

	/**
	 * Receive notification of the end of a document.
	 * @throws SAXException not thrown inside. 
	 */   
	public void endDocument()throws SAXException { 
		channel.put(null);
	}

	/**
	 * Begin the scope of a prefix-URI Namespace mapping 
	 * @param uri key of the prefix
	 * @param prefix Namensraum-Pr�fix
	 */
	public void startPrefixMapping(String prefix, String uri) {
		if(level !=-1)
			namespaceMappings.put(uri,prefix);
	}

	/**
	 * End the scope of a prefix-URI Namespace mapping.
	 * In this implementation the parameter prefix will be removed 
	 * from the namespaceMappings.
	 * @param prefix Namespace prefix.
	 */ 
	public void endPrefixMapping(String prefix) {
		if( level != -1){
			for (Iterator i = namespaceMappings.keySet().iterator(); i.hasNext();) {
				String uri = (String)i.next();
				String thisPrefix = (String)namespaceMappings.get(uri);
					if(prefix.equals(thisPrefix)){
					namespaceMappings.remove(uri);
					break;
				}
			}
		}
	 }

	/**
	 * Receive notification of the beginning of an element.
	 * If the qName of this node is identical with the given 
	 * name (see parameter in constructor) and its ancestor is not 
	 * selected then this element will be selected
	 * and all its properties (such as namespaceURI, attributes...)
	 * will be added as its children.
	 * @param namespaceURI the URI of namespace of the current node
	 * @param localName the local name of the current node
	 * @param qName the name of the current node 
	 * @param atts the attributes of the current node 
	 * @throws SAXException not thrown inside. 
	 */
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
							throws SAXException {

		Element element = doc.createElement(qName);
		
		if(level==-1 && qName.equals(elementName)) {
			level = 0;
			currentNode = element;
			doc.appendChild(currentNode);
		}
		else if (level>=0) {
			currentNode.appendChild(element);
			currentNode = element;
			level++;
		}
		else
			return;

		//namespace processing 
		if (namespaceURI.length()>0){ 
		String prefix = (String)namespaceMappings.get(namespaceURI);
		if (prefix.equals("")) 
			prefix = ""; 
			Element namespace = doc.createElementNS(namespaceURI,prefix); 
			currentNode.appendChild(namespace);
		}

		//attributes processing
		if (atts != null){
			for(int i=0;i<atts.getLength();i++){
				String attURI = atts.getURI(i);
				String attNamespace="";
				if (attURI.length()>0){
					String attPrefix=(String)namespaceMappings.get(attURI);
					if(attPrefix.equals(""))
						attPrefix = "";   
					attNamespace = attURI+attPrefix;
				}  
				currentNode.setAttributeNS(attNamespace, atts.getQName(i),atts.getValue(i));   
			}
		}
	}

	/** 
	 * Receive notification of the end of an element.
	 * If the end, which corresponds to the start of a selected node is 
	 * being searched then the whole selected node will be put to the 
	 * channel (see parameter in constructor).
	 * @param namespaceURI the URI of namespace of the current node
	 * @param localName the local name of the current node
	 * @param qName the name of the current node 
	 * @throws SAXException not thrown inside. 
	 */
	public void endElement(String namespaceURI, String localName, String qName) throws SAXException{
		if(level==0) {
			channel.put(new XMLObject(doc));
			doc = dbuild.newDocument();
			currentNode = null;
			level = -1;
		}
		else if (level>0) {
			currentNode = (Element)currentNode.getParentNode();
			level--;
		}
	}

	/**
	 * Receive notification of character data.
	 * The data will be added to the current node as a child.
	 * @param ch Array,which contains the characters
	 * @param start the start of the characters in the array
	 * @param length the length of the characters in the array
	 * @throws SAXException not thrown inside. 
	 */
	public void characters(char[] ch, int start, int length) throws SAXException {
		if(level != -1){
			String s = new String(ch,start,length);
			currentNode.appendChild(doc.createTextNode(s));
		}
	}

	/**
	 * Receive notification of a skipped entity.
	 * In this implementation we will create a new node,
	 * which contains the skipped entity
	 * and add this to the currentNode
	 * @param name name of the skipped entity
	 * @throws SAXException not thrown inside. 
	 */
	public void skippedEntity(String name) throws SAXException{
		if (level != -1){
			Element skippedEnt = doc.createElement(name);
			currentNode.appendChild(skippedEnt);
		}
	}
}
