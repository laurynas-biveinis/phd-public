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

import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;

import xxl.core.functions.Function;
import xxl.core.util.WrappingRuntimeException;

/**
 * This class contains sinks for stream processing of XML
 * documents.
 */
public class Sinks {
	/** No instances allowed from this class */
	private Sinks() {
	}

	/*	public static void insertIntoEXTree(Iterator xmlIterator, EXTree tree) {
			while (xmlIterator.hasNext()) {
				XMLObject xmlo = (XMLObject) xmlIterator.next();
				String xpath = (String) xmlo.getMetaDataEntry("simplexpathlocation");
				// tree.insert(node,xpath);
			}
		}
	*/

	/**
	 * Writes all documents from the input Iterator to {@link javax.xml.transform Result}
	 * objects which are produced by the Function createResult. 
	 * @param xmlIterator Input Iterator containing XMLObjects.
	 * @param createResult Function that gets an XMLObject as input parameter
	 * 	and returns a new Result object.
	 */
	public static void writeToResult(Iterator xmlIterator, Function createResult) {
		Transformer trans;
		try {
			trans = Operators.factory.newTransformer();
		}
		catch (TransformerConfigurationException e) {
			throw new WrappingRuntimeException(e);
		}
		
		while (xmlIterator.hasNext()) {
			XMLObject o = (XMLObject) xmlIterator.next();
			Node node = o.getDocument();
			Result result = (Result) createResult.invoke(o);
			try {
				trans.transform(new DOMSource(node),result);
			}
			catch (TransformerException e) {
				throw new WrappingRuntimeException(e);
			}
		}
	}

	/**
	 * Function which returns StreamResult objects that writes the
	 * data into temporary files.
	 * @see File#createTempFile(String, String)
	 */
	public static Function createStreamResult = new Function() {
		public Object invoke(Object xmlo) {
			try {
				return new StreamResult(File.createTempFile("documents",""));
			}
			catch (IOException e) {
				throw new WrappingRuntimeException(e);
			}
		}
	};

	/**
	 * Writes all documents from the input Iterator to files.
	 * The name of the files are computed by calling a mapFilename
	 * Function with the meta data "filename" from each document.
	 * @param xmlIterator Input Iterator containing XMLObjects.
	 * @param mapFilename Function which maps the filename meta data
	 * 	to a real file name.
	 */
	public static void writeToFiles(Iterator xmlIterator, final Function mapFilename) {
		writeToResult(
			xmlIterator,
			new Function () {
				public Object invoke(Object o) {
					XMLObject xmlo = (XMLObject) o;
					return new StreamResult(
						new File(
							(String) mapFilename.invoke(xmlo.getMetaDataEntry("filename"))
						)
					);
				}
			}
		);
	}

	/**
	 * Writes all documents from the input Iterator to files with
	 * filenames given from the filenameIterator.
	 * @param xmlIterator Input Iterator containing XMLObjects.
	 * @param filenameIterator Cursor containing filenames.
	 */
	public static void writeToFiles(Iterator xmlIterator, final Iterator filenameIterator) {
		writeToResult(
			xmlIterator,
			new Function () {
				public Object invoke(Object xmlo) {
					return new StreamResult((String) filenameIterator.next());
				}
			}
		);
	}

	/**
	 * This method sends each document from the input iterator 
	 * (wrapped inside XMLObjects) to SaxHandlers which are 
	 * returned by the Function getSAXContentHandler.
	 * @param xmlIterator Input Iterator containing XMLObjects.
	 * @param getSAXContentHandler Function which returns new 
	 * 	Objects of the type ContentHandler.
	 */
	public static void sendToSAXHandler(Iterator xmlIterator, Function getSAXContentHandler) {
		while (xmlIterator.hasNext()) {
			XMLObject xmlo = (XMLObject) xmlIterator.next();
			ContentHandler ch = (ContentHandler) getSAXContentHandler.invoke(xmlo);
			
			Transformer trans;
			try {
				trans = Operators.factory.newTransformer();
			}
			catch (TransformerConfigurationException e) {
				throw new WrappingRuntimeException(e);
			}
			
			SAXResult sr = new SAXResult(ch);
			try {
				DOMSource ds = new DOMSource(xmlo.getDocument());
				trans.transform(ds, sr);
			}
			catch (TransformerException e) {
				throw new WrappingRuntimeException(e);
			}
		}
	}
}
