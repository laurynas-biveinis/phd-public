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
 * Represents a CDATA section.
 */
public class CDATASection extends Text implements org.w3c.dom.CDATASection{
   
	/**
	 * Constructor
	 * @param content String content
	 */
	protected CDATASection(String content){
		super(content);
	}
   
	/**
	 * Constructor
	 * @param content String
	 * @param parent The parent node
	 * @param ownerDocument The owner document
	 */
	protected CDATASection(String content, xxl.core.xml.storage.dom.Node parent, xxl.core.xml.storage.dom.Document ownerDocument){
		this(content);
		this.parentNode = parent;
		this.ownerDocument = ownerDocument;
	}
   
	/**
	 * @return The type of the node
	 */
	public short getNodeType() {
		return org.w3c.dom.Node.CDATA_SECTION_NODE;
	}

    /** Returns "#cdata-section".
     * @return the name of the node "#cdata-section"
     */
    public String getNodeName() {
        return "#cdata-section";
    }
}
