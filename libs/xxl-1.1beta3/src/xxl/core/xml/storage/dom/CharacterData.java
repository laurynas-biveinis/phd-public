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

package xxl.core.xml.storage.dom;

/**
 *
 */
public abstract class CharacterData extends Node implements org.w3c.dom.CharacterData{
    
 
    /**
     * Length
     */
    protected int length = 0;
    
    //Methods

    /**
     * The character data of the node that implements this interface. The DOM
     * implementation may not put arbitrary limits on the amount of data
     * that may be stored in a <code>CharacterData</code> node. However,
     * implementation limits may mean that the entirety of a node's data may
     * not fit into a single <code>DOMString</code>. In such cases, the user
     * may call <code>substringData</code> to retrieve the data in
     * appropriately sized pieces.
     * @throws org.w3c.dom.DOMException
     */
    public String getData() throws org.w3c.dom.DOMException{
        return new String(((xxl.core.xml.storage.LiteralNode)this.recNode).getBytes());
    }
    
    /**
     * The character data of the node that implements this interface. The DOM
     * implementation may not put arbitrary limits on the amount of data
     * that may be stored in a <code>CharacterData</code> node. However,
     * implementation limits may mean that the entirety of a node's data may
     * not fit into a single <code>DOMString</code>. In such cases, the user
     * may call <code>substringData</code> to retrieve the data in
     * appropriately sized pieces.
     * @throws org.w3c.dom.DOMException
     *   NO_MODIFICATION_ALLOWED_ERR: Raised when the node is readonly.
    */
    public void setData(String data) throws org.w3c.dom.DOMException{
        //Zuerst die Exceptions abfangen
        if(readonly){
            throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NO_MODIFICATION_ALLOWED_ERR,"this Node is readonly");
        }
        
        ((xxl.core.xml.storage.LiteralNode)this.recNode).setContent(data.getBytes());
        length = data.length();
    }
    
    /**
     * The number of 16-bit units that are available through <code>data</code>
     * and the <code>substringData</code> method below. This may have the
     * value zero, i.e., <code>CharacterData</code> nodes may be empty.
     */
    public int getLength(){
        return length;
    }
    
    /**
     * Extracts a range of data from the node.
     * @param offset Start offset of substring to extract.
     * @param count The number of 16-bit units to extract.
     * @return The specified substring. If the sum of <code>offset</code> and
     *   <code>count</code> exceeds the <code>length</code>, then all 16-bit
     *   units to the end of the data are returned.
     * @throws org.w3c.dom.DOMException
     *   INDEX_SIZE_ERR: Raised if the specified <code>offset</code> is
     *   negative or greater than the number of 16-bit units in
     *   <code>data</code>, or if the specified <code>count</code> is
     *   negative.
     */
    public String substringData(int offset, int count) throws org.w3c.dom.DOMException{
        //Zuerst die Exceptions abfangen
        if(offset<0 || offset>this.getLength() || count<0){
            throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.INDEX_SIZE_ERR,"Index out of Bounds");
        }
        
        String str = new String(((xxl.core.xml.storage.LiteralNode)this.recNode).getBytes());       
        
        if((offset+count)>this.getLength()){
            return str.substring(offset);
        }
        else{   
            return str.substring(offset,offset+count+1);
        }
    }
    
    /**
     * Append the string to the end of the character data of the node. Upon
     * success, <code>data</code> provides access to the concatenation of
     * <code>data</code> and the <code>DOMString</code> specified.
     * @param arg The <code>DOMString</code> to append.
     * @throws org.w3c.dom.DOMException
     *   NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     */
    public void appendData(String arg) throws org.w3c.dom.DOMException{
        //Zuerst die Exceptions abfangen
        if(readonly){
            throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NO_MODIFICATION_ALLOWED_ERR,"this Node is readonly");
        }
        
        String str = new String(((xxl.core.xml.storage.LiteralNode)this.recNode).getBytes());
        str += arg;
        ((xxl.core.xml.storage.LiteralNode)this.recNode).setContent(str.getBytes());
        length = str.length();
    }
    
    /**
     * Insert a string at the specified 16-bit unit offset.
     * @param offset The character offset at which to insert.
     * @param arg The <code>DOMString</code> to insert.
     * @throws org.w3c.dom.DOMException
     *   INDEX_SIZE_ERR: Raised if the specified <code>offset</code> is
     *   negative or greater than the number of 16-bit units in
     *   <code>data</code>.
     *   <br>NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     */
    public void insertData(int offset, String arg) throws org.w3c.dom.DOMException{
        //Zuerst die Exceptions abfangen
        if(readonly){
            throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NO_MODIFICATION_ALLOWED_ERR,"this Node is readonly");
        }
        if(offset<0 || offset>this.getLength()){
            throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.INDEX_SIZE_ERR,"Index out of Bounds");
        }

        String str = new String(((xxl.core.xml.storage.LiteralNode)this.recNode).getBytes());
        String strAnfang = str.substring(0,offset);
        String strEnde = str.substring(offset);
        String newString = strAnfang+arg+strEnde;
        ((xxl.core.xml.storage.LiteralNode)this.recNode).setContent(newString.getBytes());
        length = newString.length();
    }
    
    /**
     * Remove a range of 16-bit units from the node. Upon success,
     * <code>data</code> and <code>length</code> reflect the change.
     * @param offset The offset from which to start removing.
     * @param count The number of 16-bit units to delete. If the sum of
     *   <code>offset</code> and <code>count</code> exceeds
     *   <code>length</code> then all 16-bit units from <code>offset</code>
     *   to the end of the data are deleted.
     * @throws org.w3c.dom.DOMException
     *   INDEX_SIZE_ERR: Raised if the specified <code>offset</code> is
     *   negative or greater than the number of 16-bit units in
     *   <code>data</code>, or if the specified <code>count</code> is
     *   negative.
     *   <br>NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     */
    public void deleteData(int offset, int count) throws org.w3c.dom.DOMException{
        //Zuerst die Exceptions abfangen
        if(offset<0 || offset>this.getLength() || count<0){
            throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.INDEX_SIZE_ERR,"Index out of Bounds");
        }

        String str = new String(((xxl.core.xml.storage.LiteralNode)this.recNode).getBytes());
        String strAnfang = str.substring(0,offset);
        String strEnde = str.substring(offset+count);
        String newString = strAnfang + strEnde;
        ((xxl.core.xml.storage.LiteralNode)this.recNode).setContent(newString.getBytes());
        length = newString.length();
    }
    
    /**
     * Replace the characters starting at the specified 16-bit unit offset
     * with the specified string.
     * @param offset The offset from which to start replacing.
     * @param count The number of 16-bit units to replace. If the sum of
     *   <code>offset</code> and <code>count</code> exceeds
     *   <code>length</code>, then all 16-bit units to the end of the data
     *   are replaced; (i.e., the effect is the same as a <code>remove</code>
     *    method call with the same range, followed by an <code>append</code>
     *    method invocation).
     * @param arg The <code>DOMString</code> with which the range must be
     *   replaced.
     * @throws org.w3c.dom.DOMException
     *   INDEX_SIZE_ERR: Raised if the specified <code>offset</code> is
     *   negative or greater than the number of 16-bit units in
     *   <code>data</code>, or if the specified <code>count</code> is
     *   negative.
     *   <br>NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     */
    public void replaceData(int offset, int count, String arg) throws org.w3c.dom.DOMException{
        //Zuerst die Exceptions abfangen
        if(readonly){
            throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NO_MODIFICATION_ALLOWED_ERR,"this Node is readonly");
        }

        if(offset<0 || offset>this.getLength() || count<0){
            throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.INDEX_SIZE_ERR,"Index out of Bounds");
        }
        
        String str = new String(((xxl.core.xml.storage.LiteralNode)this.recNode).getBytes());
        String strAnfang = str.substring(0,offset);
        String strEnde = str.substring(offset+count);
        String newString = strAnfang+arg+strEnde;
        ((xxl.core.xml.storage.LiteralNode)this.recNode).setContent(newString.getBytes());
        length = newString.length();
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
    public abstract org.w3c.dom.Node cloneNode(boolean param);
    
    public void setParentNode(org.w3c.dom.Node parentNode) {
        this.parentNode = (Node)parentNode;
    }    
    
   /**
     * The value of this node, depending on its type; see the table above.
     * When it is defined to be <code>null</code>, setting it has no effect.
     * @throws DOMException
     *   NO_MODIFICATION_ALLOWED_ERR: Raised when the node is readonly.
     * @throws org.w3c.dom.DOMException
     *   DOMSTRING_SIZE_ERR: Raised when it would return more characters than
     *   fit in a <code>DOMString</code> variable on the implementation
     *   platform.
     */
    public void setNodeValue(String str) throws org.w3c.dom.DOMException {
        this.setData(str);
    }
   
     /** The value of this node, depending on its type; see the table above.
     * When it is defined to be <code>null</code>, setting it has no effect.
     * @throws org.w3c.dom.DOMException
     *   NO_MODIFICATION_ALLOWED_ERR: Raised when the node is readonly.
     * @throws org.w3c.dom.DOMException
     *   DOMSTRING_SIZE_ERR: Raised when it would return more characters than
     *   fit in a <code>DOMString</code> variable on the implementation
     *   platform.
     */
    public String getNodeValue() throws org.w3c.dom.DOMException {
        
        return this.getData();
    }
}
