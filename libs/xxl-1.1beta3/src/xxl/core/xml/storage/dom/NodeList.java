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

import java.util.Vector;

/**
 */
public class NodeList implements org.w3c.dom.NodeList{
    
    //private Variablen
	protected xxl.core.xml.storage.Node recNode = null;
	protected xxl.core.xml.storage.dom.Node parent = null;
	protected xxl.core.xml.storage.dom.Document ownerDoc = null;
	
	private Vector vector = null;
	//Konstruktoren
    protected NodeList(xxl.core.xml.storage.dom.Document ownerDoc, xxl.core.xml.storage.dom.Node parent, xxl.core.xml.storage.Node node){
    this.recNode = node;
		this.ownerDoc = ownerDoc;
		this.parent = parent;
		this.vector = null;
	}
    
	protected NodeList(){
		
		this.vector = new Vector();
	}
    
	protected void append(org.w3c.dom.Node node){
		//System.out.println("nodeList() " + vector.toString() );
		//System.out.println("append : " + node.getNodeName());
		if(this.vector != null){
			this.vector.add(node);
		}
	}
	
	/**
     * Returns the <code>index</code>th item in the collection. If 
     * <code>index</code> is greater than or equal to the number of nodes in 
     * the list, this returns <code>null</code>.
     * @param index Index into the collection.
     * @return The node at the <code>index</code>th position in the 
     *   <code>NodeList</code>, or <code>null</code> if that is not a valid 
     *   index.
     */
    public org.w3c.dom.Node item(int index){
		
		
		if(this.vector!=null){
			return (Node)this.vector.elementAt(index);
		}
		
						
		int i = 0;
		
		java.util.Iterator listIterator = this.ownerDoc.tree.getXMLChildren(this.recNode); 
		while (listIterator.hasNext()) {
			xxl.core.xml.storage.Node node = (xxl.core.xml.storage.Node) listIterator.next();
			if (!(node.getType() == xxl.core.xml.storage.Node.MARKUP_NODE && ((xxl.core.xml.storage.MarkupNode)node).isAttribute())) {
				//System.out.println("i : " +i + " index : " + index);
				if (i == index){
					switch(node.getType()){
						case xxl.core.xml.storage.Node.LITERAL_NODE : return new Text(this.ownerDoc,this.parent,node);
						case xxl.core.xml.storage.Node.MARKUP_NODE :{
							/*if (((xxl.xml.storage.MarkupNode)node).isAttribute())
								return new Attr(this.ownerDoc,this.parent,node);
							else*/ return new Element(this.ownerDoc,this.parent,node);
						}
					}
				}
				else i++;
			}
		}
		return null;
    };

    /**
     * The number of nodes in the list. The range of valid child node indices 
     * is 0 to <code>length-1</code> inclusive.
     */
    public int getLength(){
    /*    
		if(this.vector!=null){
			return this.vector.size();
		}*/
		
		int laenge=0;
		
		java.util.Iterator listIterator = null;//this.recNode.getChildNodes(); 
		if(this.vector!=null) {
			Vector newVector = new Vector();
			for(int i=0; i < this.vector.size(); i++){
				newVector.addElement(((Node)this.vector.elementAt(i)).recNode);
			}
			listIterator = newVector.iterator();
		}
		else {
			listIterator = this.ownerDoc.tree.getXMLChildren(this.recNode); 
		}
		if (listIterator!=null) {
			while (listIterator.hasNext()) {
				xxl.core.xml.storage.Node tmpNode = (xxl.core.xml.storage.Node) listIterator.next();
					
				if( (tmpNode.getType()==xxl.core.xml.storage.Node.LITERAL_NODE)
					|| (tmpNode.getType()==xxl.core.xml.storage.Node.MARKUP_NODE && 
							!((xxl.core.xml.storage.MarkupNode)tmpNode).isAttribute())
					) 
					laenge++;
			}
		}
		return laenge;
    }
	
	protected Node next(Node node){
		
		if(this.parent==null)return null;
		if(node.recNode == this.parent.recNode)return null;
			
		if(this.vector!=null){
			int i = this.vector.indexOf(node);
			if(i<this.vector.size()-1)return (Node)this.vector.elementAt(i+1);
			return null;
		}
		
		boolean found=false;
		
		java.util.Iterator listIterator = this.ownerDoc.tree.getXMLChildren(this.recNode); 
		
		while (listIterator.hasNext()) {
		  xxl.core.xml.storage.Node tmpNode = (xxl.core.xml.storage.Node) listIterator.next();
			if (!(tmpNode.getType() == xxl.core.xml.storage.Node.MARKUP_NODE && (((xxl.core.xml.storage.MarkupNode)tmpNode).isAttribute()))) {
				if(found){
					switch(tmpNode.getType()){
						case xxl.core.xml.storage.Node.LITERAL_NODE : return new Text(this.ownerDoc,this.parent,tmpNode);
						case xxl.core.xml.storage.Node.MARKUP_NODE :{
							/*if (((xxl.xml.storage.MarkupNode)tmpNode).isAttribute())
								return new Attr(this.ownerDoc,this.parent,tmpNode);
							else*/ return new Element(this.ownerDoc,this.parent,tmpNode);
						}
					}
				}
				if(node.recNode == tmpNode){
					found=true;
				}
			}
		}
		return null;
	}

	protected Node before(Node node){
		//if(node!=null && this.parent!=null)System.out.println("recNode = "+node.recNode + " parent.RecNode= " + this.parent.recNode);
		if(this.parent==null)return null;
		if(node.recNode == this.parent.recNode)return null;

		if(this.vector!=null){
			int i = this.vector.indexOf(node);
			if(i>0)return (Node)this.vector.elementAt(i-1);
			return null;
		}
		
		xxl.core.xml.storage.Node tmpRecNode = null;
		
		java.util.Iterator listIterator = null;// this.recNode.getChildNodes(); 
			if(this.vector!=null){
				Vector newVector = new Vector();
				for(int i=0; i < this.vector.size(); i++){
					newVector.addElement(((Node)this.vector.elementAt(i)).recNode);
				}
				listIterator = newVector.iterator();
			}else{
				listIterator = this.ownerDoc.tree.getXMLChildren(this.recNode); 
			}
		
		while (listIterator.hasNext()) {
			
		    xxl.core.xml.storage.Node tmpNode = (xxl.core.xml.storage.Node) listIterator.next();
			if (!(tmpNode.getType() == xxl.core.xml.storage.Node.MARKUP_NODE && (((xxl.core.xml.storage.MarkupNode)tmpNode).isAttribute()))) {
				if(node.recNode==tmpNode){
					if(tmpRecNode==null)return null;
					switch(tmpRecNode.getType()){
						case xxl.core.xml.storage.Node.LITERAL_NODE : return new Text(this.ownerDoc,this.parent,tmpRecNode);
						case xxl.core.xml.storage.Node.MARKUP_NODE :{
							/*if (((xxl.xml.storage.MarkupNode)tmpNode).isAttribute())
								return new Attr(this.ownerDoc,this.parent,tmpRecNode);
							else*/ return new Element(this.ownerDoc,this.parent,tmpRecNode);
						}
					}
				}
				else{
					tmpRecNode=tmpNode;
				}
			}
		}
		return null;
	}
}
