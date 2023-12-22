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
