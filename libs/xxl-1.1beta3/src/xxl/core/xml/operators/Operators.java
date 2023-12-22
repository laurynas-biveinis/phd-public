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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import xxl.core.cursors.Cursor;
import xxl.core.cursors.mappers.Mapper;
import xxl.core.cursors.sources.io.FileNameCursor;
import xxl.core.functions.Function;
import xxl.core.predicates.Predicate;
import xxl.core.util.WrappingRuntimeException;

/**
 * Contains a lot of useful methods for processing a sequence (Iterator)
 * of XML documents. The documents have to be wrapped into an
 * XMLObject. So each document can posess meta data.
 */
public class Operators {
	/**
	 * This class cannot become instantiated.
	 */
	private Operators() {
	}

	/**
	 * TransformerFactory which is used by all operators inside this
	 * package. If a different factory is wanted, then overwrite
	 * the reference.
	 */
	public static TransformerFactory factory = TransformerFactory.newInstance();

	/**
	 * This method returns a Function which performs an XSLT transformation 
	 * of an XML document which is held inside an XMLObject. This function
	 * can be used inside {@link xxl.core.cursors.mappers.Mapper}.
	 * @param xsltSource Source which defines the XSLT transformation
	 * 	(for example from an XSLT file using {@link org.xml.sax.InputSource}).
	 * @return Function which performs an XSLT transformation on an XMLObject.
	 */
	public static Function getXSLTMapFunction (final Source xsltSource) {
		return new Function () {
			Transformer trans=null;
			public Object invoke (Object o) {
				XMLObject xmlo = (XMLObject) o;
				Node n = xmlo.getDocument();
				if (trans==null) {
					try {
						trans = factory.newTransformer(xsltSource);
					}
					catch (TransformerConfigurationException e) {
						throw new WrappingRuntimeException(e);
					}
				}
				DOMResult dr = new DOMResult();
				try {
					DOMSource ds = new DOMSource(n);
					
					trans.transform(ds, dr);
					// recycle the old XMLObject!
					xmlo.setDocument((Document)dr.getNode());
					return o;
				}
				catch (TransformerException e) {
					throw new WrappingRuntimeException(e);
				}
			}
		};
	}

	/**
	 * This method returns a Function which performs an XSLT transformation 
	 * of an XML document which is held inside an XMLObject. This function
	 * can be used inside {@link xxl.core.cursors.mappers.Mapper}.
	 * @param filename XSLT file which defines the XSLT transformation.
	 * @return Function which performs an XSLT transformation on an XMLObject.
	 */
	public static Function getXSLTMapFunction (String filename) {
		try {
			return getXSLTMapFunction(
				new SAXSource(new org.xml.sax.InputSource(new FileInputStream(filename)))
			);
		}
		catch (FileNotFoundException e) {
			throw new WrappingRuntimeException(e);
		}
	}

	/**
	 * Maps the stream of XMLObjects and updates the metadata with the identifyer
	 * "simplexpathlocation" to a new value that is computed by the updateXPathFunction.
	 * @param xmlIterator Input iterator delivering XMLObjects.
	 * @param updateXPathFunction Function which maps the meta data entry for
	 * 	"simplexpathlocation" to a new String value.
	 * @return The Cursor.
	 */
	public static Cursor updateXPathLocation (Iterator xmlIterator, final Function updateXPathFunction) {
		return 
			new Mapper(
				new Function() {
					public Object invoke(Object o) {
						XMLObject xmlo = (XMLObject) o;
						xmlo.putMetaDataEntry(
							"simplexpathlocation",
							updateXPathFunction.invoke(xmlo.getMetaDataEntry("simplexpathlocation"))
						);
						return xmlo;
					}
				},
				xmlIterator
			);
	}

	//////////////////////////////////////////////////////////////////////
	// Some Functions concerning Strings.
	//////////////////////////////////////////////////////////////////////

	/**
	 * Returns a Predicate which returns true iff the object matches the
	 * String which is given.
	 * @param matcher Regular expression.
	 * @see java.lang.String#matches(String)
	 * @return The Predicate.
	 */
	public static Predicate getStringMatchingPredicate(final String matcher) {
		return new xxl.core.predicates.Predicate() {
			public boolean invoke(Object o) {
				return ((String) o).matches(matcher);
			}
		};
	}

	/**
	 * Returns a Function which expects a String parameter s and returns 
	 * prefix+s+suffix as a String.
	 * @param prefix Prefix String.
	 * @param suffix Suffix String.
	 * @return The Function.
	 */
	public static Function getPrefixSuffixMapStringFunction(final String prefix, final String suffix) {
		return new Function() {
			public Object invoke(Object o) {
				return prefix+o+suffix;
			}
		};
	}

	/**
	 * Function which expects a String parameter that
	 * represents an absolute path to a file and returns the
	 * filename without the path. 
	 */
	public static final Function FILENAME_FROM_PATH_FUNCTION = new Function() {
		public Object invoke(Object o) {
			java.io.File f = new java.io.File((String)o);
			return f.getName();
		}
	};

	/**
	 * Function which gets an object and returns the String 
	 * representation of the object (uses toString() inside).
	 */
	public static final Function TO_STRING_FUNCTION = new Function() {
		public Object invoke(Object o) {
			return o.toString();
		}
	};

	/**
	 * Test case which transforms the documents inside data/xml/weather
	 * according to a xslt-description data/xml/weather/weather.xsl.
	 * The output files are written to output/xml/weather.
	 * @param args Command line arguments are ignored here.
	 */
	public static void main(String args[]) {
		DocumentBuilder dbuild;
		
		try {
			dbuild = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		}
		catch (ParserConfigurationException e) {
			throw new WrappingRuntimeException(e);
		}
		
		String inPath = xxl.core.util.XXLSystem.getDataPath(new String[]{"xml","weather"});
		String outPath = xxl.core.util.XXLSystem.getOutPath(new String[]{"output","xml","weather"});
		System.out.println("inPath: "+inPath);
		System.out.println("outPath: "+outPath);
		
		// String path=".";
		Cursor filenamesCursor =
			new Mapper(
				getPrefixSuffixMapStringFunction(inPath,""),
				new xxl.core.cursors.filters.Filter(
					new FileNameCursor(inPath, false),
					getStringMatchingPredicate(".*[xX][mM][lL]$") // xml at the end
				)
			);

		// xxl.core.cursors.Cursors.println(filenamesCursor);
		// System.out.println(xxl.core.cursors.Cursors.count(filenamesCursor));

		Cursor c = Sources.readXMLFiles(filenamesCursor,dbuild);

		c = new Mapper(
			getXSLTMapFunction(inPath+java.io.File.separatorChar+"weather.xsl"),
			c
		);
		
		// xxl.cursors.Cursors.count(c);
		Sinks.writeToFiles(
			c,
			getPrefixSuffixMapStringFunction(outPath+java.io.File.separatorChar,".html").compose(
				FILENAME_FROM_PATH_FUNCTION
			)
		);

		// different possibility: write into files with increasing number as file name.
		// writeToFiles (
		//	c,
		//	new Mapper(
		//		new xxl.cursors.Enumerator(),
		//		getPrefixSuffixMapStringFunction(".\\weather",".html")
		//	)
		// );
	}
}
