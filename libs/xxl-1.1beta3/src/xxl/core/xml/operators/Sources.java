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

package xxl.core.xml.operators;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;

import org.xml.sax.SAXException;

import xxl.core.cursors.Cursor;
import xxl.core.cursors.mappers.Mapper;
import xxl.core.functions.Function;
import xxl.core.util.WrappingRuntimeException;

/**
 * This class contains sources for stream processing
 * of XML documents.
 */
public class Sources {
	/** No instances allowed from this class */
	private Sources() {
	}

	/**
	 * Reads XML files and returns a cursor, which contains
	 * all the XML documents in DOM representation (type XMLObject).
	 * @param filenames Iterator which contains the filenames which are used.
	 * @param docB DocumentBuilder which is used to construct the DOM representation
	 * 	of the XML files.
	 * @return Cursor containing XMLObjects.
	 */
	public static Cursor readXMLFiles(Iterator filenames, final DocumentBuilder docB) {
		return
			new Mapper(
				new Function() {
					public Object invoke (Object filename) {
						try {
							XMLObject o = new XMLObject(
								docB.parse( new File((String) filename))
							);
							o.putMetaDataEntry("filename",filename);
							return o;
						}
						catch (SAXException e) {
							throw new WrappingRuntimeException(e);
						}
						catch (IOException e) {
							throw new WrappingRuntimeException(e);
						}
						catch (IllegalArgumentException e) {
							throw new WrappingRuntimeException(e);
						}
					}
				},
				filenames
			);
	}

}
