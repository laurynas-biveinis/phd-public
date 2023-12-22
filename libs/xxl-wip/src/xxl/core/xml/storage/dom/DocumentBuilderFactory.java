/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.xml.storage.dom;

import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

/**
 */
public class DocumentBuilderFactory extends javax.xml.parsers.DocumentBuilderFactory {
    //Variablen
    protected Map attribute = new HashMap();
    
    /** Creates a new instance of DocumentBuilderFactory */
    public DocumentBuilderFactory() {
    
    }
    
    //Methoden
    public static javax.xml.parsers.DocumentBuilderFactory newInstance(){
    	return new DocumentBuilderFactory(); 
    }
    
    public javax.xml.parsers.DocumentBuilder newDocumentBuilder() throws javax.xml.parsers.ParserConfigurationException {
        return new DocumentBuilder(this);
    }
    
    public Object getAttribute(String str) throws java.lang.IllegalArgumentException {
        return attribute.get(str);
    }
    
    public void setAttribute(String str, Object obj) throws java.lang.IllegalArgumentException {
        attribute.put(str,obj);
    }

	@Override
	public boolean getFeature(String name) throws ParserConfigurationException {
		throw new UnsupportedOperationException("not implemented yet");
	}

	@Override
	public void setFeature(String name, boolean value) throws ParserConfigurationException {
		throw new UnsupportedOperationException("not implemented yet");
	}
    
}
