/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.xml.storage.dom;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

/**
 */
public class Text extends CharacterData implements org.w3c.dom.Text{
    
	protected Text(xxl.core.xml.storage.dom.Document ownerDoc, xxl.core.xml.storage.dom.Node parent,xxl.core.xml.storage.Node recNode){
		this.ownerDocument = ownerDoc;
		this.parentNode = parent;
		this.recNode = recNode;
		this.length=this.getData().length();
	} 
   
	protected Text(String content){
		this.recNode = xxl.core.xml.storage.Node.getNodeByType(xxl.core.xml.storage.Node.LITERAL_NODE);
		this.setData(content);    
	}
   
	protected Text(String content, xxl.core.xml.storage.dom.Node parent, xxl.core.xml.storage.dom.Document ownerDocument){
		this(content);
		this.ownerDocument = ownerDocument;
		this.parentNode = parent;
	}

	/**
     * Breaks this node into two nodes at the specified <code>offset</code>, 
     * keeping both in the tree as siblings. After being split, this node 
     * will contain all the content up to the <code>offset</code> point. A 
     * new node of the same type, which contains all the content at and 
     * after the <code>offset</code> point, is returned. If the original 
     * node had a parent node, the new node is inserted as the next sibling 
     * of the original node. When the <code>offset</code> is equal to the 
     * length of this node, the new node has no data.
     * @param offset The 16-bit unit offset at which to split, starting from 
     *   <code>0</code>.
     * @return The new node, of the same type as this node.
     * @exception DOMException
     *   INDEX_SIZE_ERR: Raised if the specified offset is negative or greater 
     *   than the number of 16-bit units in <code>data</code>.
     *   <br>NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     */
    public org.w3c.dom.Text splitText(int offset) throws org.w3c.dom.DOMException{
		throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NOT_SUPPORTED_ERR,"not Implemented");
        
    	//Zuerst die Exceptions abfangen
       /* if(offset<0 || offset>this.getLength()){
            throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.INDEX_SIZE_ERR,"Index out of Bounds");
        }
        if(readonly){
            throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NO_MODIFICATION_ALLOWED_ERR,"this Node is readonly");
        }
        
		throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NOT_SUPPORTED_ERR, "not ready implemented");
		*/
		//Neues Textnode erzeugen, inhalt setzen und Parent setzen
        /*
		Text newText = new Text(this.getData().substring(offset));
        newText.setParentNode(this.getParentNode());
        
        //the new node is inserted as the next sibling of the original node
        if(this.parentNode!=null){
            //pr�fung, ob die Childliste in Parent existiert ist �berfl�ssig, da diese Node geh�rt ja zu parent,
            //und muss also Childliste eingetragen sein. 
            //Referenz auf ChildListe holen
            java.util.List childs = ((xxl.xml.dom.NodeList)this.parentNode.getChildNodes()).getContent();  
            //index von originalen Node ermitteln
            int index = childs.indexOf(this);
            //neuen Node einf�hgen
            if(childs.size()==index){
                childs.add(newText);
            }
            else{
                childs.add(index+1,newText);
            }
        }
        this.setData(this.getData().substring(0,offset));
        
		return newText;
		 */
        
    }

    public short getNodeType() {
        return org.w3c.dom.Node.TEXT_NODE;
    }
    
    /** Returns "#text".
     */
    public String getNodeName() {
        return "#text";
    }

	/**
     * Returns a duplicate of this node, i.e., serves as a generic copy
     * constructor for nodes. The duplicate node has no parent; (
     * <code>parentNode</code> is <code>null</code>.).
     * <br>Cloning an <code>Element</code> copies all attributes and their
     * values, including those generated by the XML processor to represent
     * defaulted attributes, but this method does not copy any text it
     * contains unless it is a deep clone, since the text is contained in a
     * child <code>Text</code> node. Cloning an <code>Attribute</code>
     * directly, as opposed to be cloned as part of an <code>Element</code>
     * cloning operation, returns a specified attribute (
     * <code>specified</code> is <code>true</code>). Cloning any other type
     * of node simply returns a copy of this node.
     * <br>Note that cloning an immutable subtree results in a mutable copy,
     * but the children of an <code>EntityReference</code> clone are readonly
     * . In addition, clones of unspecified <code>Attr</code> nodes are
     * specified. And, cloning <code>Document</code>,
     * <code>DocumentType</code>, <code>Entity</code>, and
     * <code>Notation</code> nodes is implementation dependent.
     * @param param If <code>true</code>, recursively clone the subtree under
     *   the specified node; if <code>false</code>, clone only the node
     *   itself (and its attributes, if it is an <code>Element</code>).
     * @return The duplicate node.
     */
     
	public org.w3c.dom.Node cloneNode(boolean param) {
		throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NOT_SUPPORTED_ERR,"not Implemented");
		
		/*Text text = new Text(this.getData(),null,this.ownerDocument);
		return text;*/
		//return null;
	}	
	
	public Node getNextSibling() {
		Node n =  super.getNextSibling();
		if(n==null)return null;
		if(n.getNodeType()==org.w3c.dom.Node.TEXT_NODE)return n;
		return null;
	}

	
	public Node getPreviousSibling() {
		Node n = super.getPreviousSibling();
		if(n==null)return null;
		if(n.getNodeType()==org.w3c.dom.Node.TEXT_NODE)return n;
		return null;
	}
	public String getWholeText() {
		throw new UnsupportedOperationException("not implemented yet");
	}

	public boolean isElementContentWhitespace() {
		throw new UnsupportedOperationException("not implemented yet");
	}

	public org.w3c.dom.Text replaceWholeText(String content) throws DOMException {
		throw new UnsupportedOperationException("not implemented yet");
	}

}
