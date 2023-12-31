/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.xml.storage.dom;

public class Notation extends Node implements org.w3c.dom.Notation{
    
    //private Variablen
    private String publicId;
    private String systemId;
    
    //Methoden

    /**
     * The public identifier of this notation. If the public identifier was 
     * not specified, this is <code>null</code>.
     */
    public String getPublicId(){
        return publicId;
    };

    /**
     * The system identifier of this notation. If the system identifier was 
     * not specified, this is <code>null</code>.
     */
    public String getSystemId(){
        return systemId;
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
        return org.w3c.dom.Node.NOTATION_NODE;
    }
    
		public void setNodeValue(String str) throws org.w3c.dom.DOMException {
		}
		
;

}
