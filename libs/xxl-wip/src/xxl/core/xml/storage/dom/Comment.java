/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.xml.storage.dom;

/**
 * Folgendes ausw�hlen, um die Schablone f�r den erstellten Typenkommentar zu �ndern:
 * Fenster&gt;Benutzervorgaben&gt;Java&gt;Codegenerierung&gt;Code und Kommentare
 */
public class Comment extends CharacterData implements org.w3c.dom.Comment{
    
    //Konstruktoren
	protected Comment(){
        this.recNode = xxl.core.xml.storage.Node.getNodeByType(xxl.core.xml.storage.Node.LITERAL_NODE);
        this.setData("");
    }
    
    protected Comment(String content) {
        this.recNode = xxl.core.xml.storage.Node.getNodeByType(xxl.core.xml.storage.Node.LITERAL_NODE);
        this.setData(content);
    }
    
    protected Comment(xxl.core.xml.storage.LiteralNode recNode){
        this.recNode=recNode;
        this.length=this.getData().length();
    }
    
    protected Comment(String content, xxl.core.xml.storage.dom.Node parent, xxl.core.xml.storage.dom.Document ownerDocument){
        this(content);
        this.parentNode = parent;
        this.ownerDocument = ownerDocument;
    }
    
    //Methoden
    public short getNodeType() {
        return org.w3c.dom.Node.COMMENT_NODE;
    }
    
    /** Returns "#comment".
     */
    public String getNodeName() {
        return "#comment";
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
		throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NOT_SUPPORTED_ERR,"not implemented");
        
		//Comment comment = new Comment(this.getData(),null,this.ownerDocument);
		//return comment;
	}
}