/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.xml.operators;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;

import org.xml.sax.SAXException;

import xxl.core.cursors.Cursor;
import xxl.core.cursors.mappers.Mapper;
import xxl.core.functions.AbstractFunction;
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
				new AbstractFunction() {
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
