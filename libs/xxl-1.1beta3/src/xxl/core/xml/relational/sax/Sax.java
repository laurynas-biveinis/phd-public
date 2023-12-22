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

package xxl.core.xml.relational.sax;


import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.ContentHandler;

import xxl.core.cursors.MetaDataCursor;
import xxl.core.functions.Function;
import xxl.core.util.WrappingRuntimeException;

/**
	This class consists of static methods which can be useful in some ways.
	<br><br>
	The following table shows the structure of a <b>identifier map</b> which is used to configure
	the XML processing in some methods.<br>
	The values are editable. The methods recognize only the values according the following keys. Therefore the keys
	shouldn't be manipulated. If some method is called with a map that contains i.e. the precision key, the method tries to
	read/write a attribute according to this notation. In the write case, it's inconsiderable, but if the user calls
	i.e. the getPrecision method (in XMLResultSetMetadata) the method uses the precision key and the map to find 
	the attribute that contains the precision information.
	If the map is erroneous, informations can get lost or errors can appear.
	So it's necessary to adapt the map to the XML document, to prevent mistakes.
	<br><br><br>
	<table BORDER COLS=2  >
	<tr>
	<th CLASS="TableHeadingColor"><b>key</b></th>
	<th CLASS="TableHeadingColor"><b>value</b></th>
	<th> explanation </th>
	</tr>
	
	<tr>
	<td>meta</td>
	<td>meta</td>
	<td>notation of the meta tag in the XML Code, if no entry exists, then no metadata will be written </td>
	</tr>
	
	<tr>
	<td>data</td>
	<td>data</td>
	<td>If no entry exists, then no data will be written. The data tag doesn't occur in the XML Code.</td>
	</tr>
	
	<tr>
	<td>metacol</td>
	<td>col</td>
	<td>The notation of the column tags between the meta tag.</td>
	</tr>
	
	<tr>
	<td>datarow</td>
	<td>row</td>
	<td>The notation of the rows tags. The row tags aren't ingrained in an extra data tag.</td>
	</tr>
	
	<tr>
	<td>datacol</td>
	<td>col</td>
	<td>The notation of the column tags between the datarow tags.</td>
	</tr>
	
	<tr>
	<td>sqltype</td>
	<td>sqltype</td>
	<td>The notation of the sqltype attribute from each metacol.</td>
	</tr>
	
	<tr>
	<td>precision</td>
	<td>precision</td>
	<td>The notation of the precision attribute from each metacol.</td>
	</tr>
	
	<tr>
	<td>scale</td>
	<td>scale</td>
	<td>The notation of the scale attribute from each metacol.</td>
	</tr>
	
	<tr>
	<td>autoincrement</td>
	<td>autoincrement</td>
	<td>The notation of the autoincrement attribute from each metacol.</td>
	</tr>
	
	<tr>
	<td>currency</td>
	<td>currency</td>
	<td>The notation of the currency attribute from each metacol.</td>
	</tr>
	
	<tr>
	<td>nullable</td>
	<td>nullable</td>
	<td>The notation of the nullable attribute from each metacol.</td>
	</tr>
	
	<tr>
	<td>signed</td>
	<td>signed</td>
	<td>The notation of the signed attribute from each metacol.</td>
	</tr>
	
	</table>
*/
public class Sax {
	/** This class is not instanciable */
	private Sax() {
	}
	
	/**
	 * A static method that writes the specified cursor to an XML-OutputStream. The content
	 * of the cursor is included into another document at the position that is specified as
	 * a simple XPath expression.
	 * @param document The XML document (DOM document).
	 * @param out The output stream to which data is written.
	 * @param cursor The input Cursor.
	 * @param identifier The identifyer map.
	 * @param xpath The XPath expression to where the Cursor is written.
	 */
	public static void DBtoXML(final InputStream document, OutputStream out, MetaDataCursor cursor, Map identifier, String xpath) {
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance() ;
			final SAXParser parser = spf.newSAXParser();
	
			int[] number = new int[1];
			String[] xpaths = new String[1];
			ContentHandler[] hbs = new ContentHandler[1];
			
			xpaths[0] = xpath;
			hbs[0] = new XMLWriterHandler(out, cursor, identifier);
			number[0] = 1;
	
			// Channel termChannel = new AsynchronousChannel();
			
			final DecoratorXPathHandler xph = 
				new DecoratorXPathHandler (new MirrorHandler(out), xpaths, hbs, number, null, false, false, null);
			
			Thread t = new Thread() {
				public void run() {
					try {
						parser.parse (document, xph);
					}
					catch (Exception e) {
						throw new WrappingRuntimeException(e);
					}
				}
			};
			t.start();
			t.join();
		}
		catch (Exception e) {
			throw new WrappingRuntimeException(e);
		}
	}
	
	/**
	 * A static method that creates a function which returns a FileInputStream every time
	 * the invoke() method from this function is called.
	 * @param file The file which is used as InputStream.
	 * @return The Function.
	 */
	public static Function InputStreamFactory (final File file) {
		return new Function() {
			public Object invoke () {
				try {
					return new FileInputStream(file);
				}
				catch (Exception e) {
					return null;
				}
			}
		};
	}
	
	/**
	 * Returns a identifier map, that can be used to configure the XML processing in some methods.
	 * This should be a base to build your own maps. Note: The keys shouldn't be manipulated.
	 * @return The identifyer map.
	 */
	public static Map DEFAULT_IDENTIFIER_MAP() {
		HashMap ret = new HashMap();
		
		ret.put("meta","meta");
		ret.put("data","data");
		ret.put("metacol","col");
		ret.put("datacol","col");
		ret.put("datarow","row");
		
		ret.put("sqltype","sqltype");
		ret.put("precision","precision");
		ret.put("scale","scale");
		ret.put("autoincrement","autoincrement");
		ret.put("currency","currency");
		ret.put("nullable","nullable");
		ret.put("signed","signed");
	
		return ret;
	}
}
