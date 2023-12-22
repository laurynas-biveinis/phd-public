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

import java.io.OutputStream;
import java.sql.ResultSetMetaData;
import java.util.Map;

import org.xml.sax.Attributes;

import xxl.core.cursors.MetaDataCursor;
import xxl.core.relational.metaData.ResultSetMetaDatas;
import xxl.core.relational.tuples.Tuple;
import xxl.core.util.WrappingRuntimeException;

/**
	This handler writes the data from a MetaDataCursor into an OutputStream. 
*/
public class XMLWriterHandler extends org.xml.sax.helpers.DefaultHandler {
	
	private int level;
	private MetaDataCursor cursor;
	private ResultSetMetaData rsmd;
	private OutputStream out;
	private Map identifier;

	/**
		Constructs the handler.
		@param out An OutputStream, in which this handler writes the data.
		@param cursor A Cursor that holds the data.
		@param identifier A map that contains the notation of the tag, where the data should be written.
	*/
	public XMLWriterHandler (OutputStream out, MetaDataCursor cursor,  Map identifier) {		
		this.cursor = cursor;	
		level = 0;
		
		try { 
			this.rsmd = ResultSetMetaDatas.getResultSetMetaData(cursor);
		}
		catch (Exception e) {
			throw new RuntimeException("XMLWriterHandler Constructor: ResultSet.getMetaData()");	
		}		
		this.out = out;		
		this.identifier = identifier;
	}
	
	/**
	 * Handles the startElement event (Just incresing the level here)
	 * @param uri The namespace URI
	 * @param localName The local name (without prefix), or the empty string if Namespace processing is not being performed.
	 * @param qName The qualified name (with prefix), or the empty string if qualified names are not available.
	 * @param attributes The specified or defaulted attributes.
	 */
	public void  startElement(java.lang.String uri, java.lang.String localName, java.lang.String qName, Attributes attributes) {
		level++;
	}
	
	/**
	 * Writes the data/metadata in the OutputStream, if qName equals the "datarow" value of the identifier map.
	 * The identifier map appoints also, which information will be written. If the map i.e. doesn't contain the 
	 * "meta" key/value, no metadata will be written.
	 * @param uri The namespace URI
	 * @param localName The local name (without prefix), or the empty string if Namespace processing is not being performed.
	 * @param qName The qualified name (with prefix), or the empty string if qualified names are not available.
	 */
	public void endElement(java.lang.String uri, java.lang.String localName, java.lang.String qName) {
		level--;
		
		//if (qName.equals(((String) identifier.get("datarow")))) {
		if (level==0) {
			try {
				if (identifier.containsKey("meta")) {
					out.write(("<"+((String) identifier.get("meta"))+">").getBytes());
					for (int col = 1;col<= rsmd.getColumnCount();col++) {
						out.write(("<"+((String) identifier.get("metacol"))).getBytes());	 // <name...
						// attributes
						if (identifier.containsKey("precision")) out.write(( " "+((String) identifier.get("precision"))+"=\""+rsmd.getPrecision(col)+"\" " ).getBytes());
						if (identifier.containsKey("sqltype")) out.write(( " "+((String) identifier.get("sqltype"))+"=\""+rsmd.getColumnTypeName(col)+"\" " ).getBytes());
						
						if (identifier.containsKey("scale")) out.write(( " "+((String) identifier.get("scale"))+"=\""+rsmd.getScale(col)+"\" " ).getBytes());
						if (identifier.containsKey("autoincrement")) out.write(( " "+((String) identifier.get("autoincrement"))+"=\""+rsmd.isAutoIncrement(col)+"\" " ).getBytes());
						if (identifier.containsKey("currency")) out.write(( " "+((String) identifier.get("currency"))+"=\""+rsmd.isCurrency(col)+"\" " ).getBytes());
						if (identifier.containsKey("nullable")) out.write(( " "+((String) identifier.get("nullable"))+"=\""+rsmd.isNullable(col)+"\" " ).getBytes());
						if (identifier.containsKey("signed")) out.write(( " "+((String) identifier.get("signed"))+"=\""+rsmd.isSigned(col)+"\" " ).getBytes());
						
						out.write((">").getBytes());
						
						//column name
						out.write((rsmd.getColumnName (col)).getBytes());
						out.write(("</"+((String) identifier.get("metacol"))+">").getBytes());
					}
					out.write(("</"+((String) identifier.get("meta"))+">").getBytes());
				}
				
				if (identifier.containsKey("data")) {
					Tuple t;
					String s;
					while (cursor.hasNext()) {
						t = (Tuple) cursor.next();
						out.write(("<"+((String) identifier.get("datarow"))+">").getBytes());
						for (int col = 1;col<= rsmd.getColumnCount();col++) {
							out.write(("<"+((String) identifier.get("datacol"))+">").getBytes());
							s = t.getString (col);
							if (s!=null)
								out.write(s.getBytes());
							out.write(("</"+((String) identifier.get("datacol"))+">").getBytes());
						}
						out.write(("</"+((String) identifier.get("datarow"))+">").getBytes());
					}
				}
			}
			catch (Exception e) {
				throw new WrappingRuntimeException(e);
			}
		}
	}
}
