/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.xml.storage.dom;

public class DocumentType extends Node implements org.w3c.dom.DocumentType{

    //Methoden

    /**
     * The name of DTD; i.e., the name immediately following the 
     * <code>DOCTYPE</code> keyword.
     */
    public String getName(){
        return null;
    }

    /**
     * A <code>NamedNodeMap</code> containing the general entities, both 
     * external and internal, declared in the DTD. Parameter entities are 
     * not contained. Duplicates are discarded. For example in: 
     * <pre>&lt;!DOCTYPE 
     * ex SYSTEM "ex.dtd" [ &lt;!ENTITY foo "foo"&gt; &lt;!ENTITY bar 
     * "bar"&gt; &lt;!ENTITY bar "bar2"&gt; &lt;!ENTITY % baz "baz"&gt; 
     * ]&gt; &lt;ex/&gt;</pre>
     *  the interface provides access to <code>foo</code> 
     * and the first declaration of <code>bar</code> but not the second 
     * declaration of <code>bar</code> or <code>baz</code>. Every node in 
     * this map also implements the <code>Entity</code> interface.
     * <br>The DOM Level 2 does not support editing entities, therefore 
     * <code>entities</code> cannot be altered in any way.
     */
    public org.w3c.dom.NamedNodeMap getEntities(){
        return null;
    }

    /**
     * A <code>NamedNodeMap</code> containing the notations declared in the 
     * DTD. Duplicates are discarded. Every node in this map also implements 
     * the <code>Notation</code> interface.
     * <br>The DOM Level 2 does not support editing notations, therefore 
     * <code>notations</code> cannot be altered in any way.
     */
    public org.w3c.dom.NamedNodeMap getNotations(){
        return null;
    }

    /**
     * The public identifier of the external subset.
     * @since DOM Level 2
     */
    public String getPublicId(){
        return null;
    }

    /**
     * The system identifier of the external subset.
     * @since DOM Level 2
     */
    public String getSystemId(){
        return null;
    }

    /**
     * The internal subset as a string, or <code>null</code> if there is none. 
     * This is does not contain the delimiting square brackets.The actual 
     * content returned depends on how much information is available to the 
     * implementation. This may vary depending on various parameters, 
     * including the XML processor used to build the document.
     * @since DOM Level 2
     */
    public String getInternalSubset(){
        return null;
    }
    
    /** Returns a duplicate of this node, i.e., serves as a generic copy
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
        
    }
    
    public short getNodeType() {
        return org.w3c.dom.Node.DOCUMENT_TYPE_NODE;
    }
    
	public void setNodeValue(String str) throws org.w3c.dom.DOMException {
	}
}
