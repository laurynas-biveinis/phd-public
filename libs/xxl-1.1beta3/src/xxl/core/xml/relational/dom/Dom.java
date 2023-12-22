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

package xxl.core.xml.relational.dom;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.ResultSetMetaData;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import xxl.core.cursors.MetaDataCursor;
import xxl.core.relational.metaData.ResultSetMetaDatas;
import xxl.core.relational.tuples.ArrayTuple;
import xxl.core.relational.tuples.Tuple;
import xxl.core.util.WrappingRuntimeException;

/**
	This class consists of static methods that support some database functionality,
	such as XML to MetaDataCursor and vice versa, on XML. <br> <br>
	<b> Note: </b> The XMLParser has to be inititialized before the first usage.
	<br>
	<br>
	<br>
	The following table shows the structure of a identifier map which is used to configure the XML processing in some methods.<br>
	The values are editable. The methods recognize only the values according the following keys. Therefore the keys
	shouldn't be manipulated. <br>
	By the way, this map is equal to the map created by the method Dom.DEFAULT_IDENTIFIER_MAP().
	<br><br>
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
public abstract class Dom {
	
	/**
		The XMLParser offering the funcionality which is used in this package.
	*/	
	private static XMLParser parser;
		
	/**
		Initializes the XMLParser.
		@param parser an XMLParser object
	*/
	public static void init (XMLParser parser) {
		Dom.parser = parser;	
	}

	/**
		Cannot be used.
	*/	
	private Dom () {}
		
	/**
		Inserts data from a java.sql.MetaDataCursor into a document.
		An Xpath Expression indicates where the data has to be inserted. A default identifier map will be used.
		Writes the metadata at the same place as the data. (use a new identifier map and remove the meta entry to
		write no metadata).	
		@param cursor MetaDataCursor
		@param document document in which the data should be inserted
		@param dataXPath A XPath Expression
	*/
	public static void DBtoDOM(MetaDataCursor cursor, Document document, String dataXPath) { 
		DBtoDOM (cursor, document, dataXPath, dataXPath, Dom.DEFAULT_IDENTIFIER_MAP());	
	}
	
	/**
		Inserts data from a java.sql.MetaDataCursor into a document.
		An Xpath Expression indicates where the data has to be inserted.
		Writes the metadata at the same place as the data. (remove the meta entry from the identifier map to
		write no metadata).	
		@param cursor MetaDataCursor
		@param document document in which the data should be inserted
		@param dataXPath A XPath Expression
		@param identifierMap A user specified identifier map
	*/	
	public static void DBtoDOM(MetaDataCursor cursor, Document document, String dataXPath, Map identifierMap) { 
		DBtoDOM (cursor, document, dataXPath, dataXPath, identifierMap);	
	}
	
	/**
		Inserts data from a java.sql.MetaDataCursor into a document.
		An Xpath Expression indicates where the data has to be inserted.
		Writes the metadata at the same place as the data. (remove the meta entry from the identifier map to
		write no metadata).	
		@param cursor MetaDataCursor
		@param document document in which the data should be inserted
		@param dataXPath A XPath Expression
		@param metadataXPath A XPath Expression
		@param identifierMap A user specified identifier map
	*/	
	public static void DBtoDOM (MetaDataCursor cursor, Document document, String dataXPath, String metadataXPath, Map identifierMap) {
		DBtoDOM (cursor, document, dataXPath, metadataXPath, identifierMap, true);	
	}
	
	/**
		Inserts data from a java.sql.MetaDataCursor into a document.	
		@param cursor a MetaDataCursor 
		@param document a DOM document
		@param dataXPath an Xpath expression, which describes the location where to put the data 
		@param metadataXPath an Xpath expression, which describes the location where to put the metadata 
		@param identifierMap a map which describes the layout of the XML code
		@param nullable if nullable is set false, then instead of SQL null values, an empty string will be added to the document
	*/
	public static void DBtoDOM (MetaDataCursor cursor, Document document, String dataXPath, String metadataXPath, Map identifierMap, boolean nullable) {
		
		if (parser==null) throw new RuntimeException("first call Dom.init() to initialize the parser");
		try {
			
			ResultSetMetaData rsmd = ResultSetMetaDatas.getResultSetMetaData(cursor); // the Metadaten
		
			Element item,tu;
			String metacol = (String) identifierMap.get("metacol");	// the notation of the column tag in the metadata
			String datacol = (String) identifierMap.get("datacol");
			String datarow = (String) identifierMap.get("datarow");

			if (identifierMap.get("meta") != null)  {
		
				Element metadata = document.createElement((String) identifierMap.get("meta"));     // Create Element
				
					
				/* for each column..... */
				for (int i=1;i<=rsmd.getColumnCount() ;i++) {
	
					item = document.createElement(metacol); /* add a column tag  */
							
					/* insert some attributes, that describe the column properties */
					
					String s = (String) identifierMap.get("sqltype");
					if (s != null) item.setAttribute(s,rsmd.getColumnTypeName(i));
					
					s = (String) identifierMap.get("scale");
					if (s != null) item.setAttribute(s,""+rsmd.getScale(i));	
	
					s = (String) identifierMap.get("precision");
					if (s != null) item.setAttribute(s,""+rsmd.getPrecision(i));					
					
					s = (String) identifierMap.get("autoincrement");
					if (s != null) item.setAttribute(s,String.valueOf(rsmd.isAutoIncrement(i)));	
					
					s = (String) identifierMap.get("currency");
					if (s != null) item.setAttribute(s,String.valueOf(rsmd.isCurrency(i)));								
					
					s = (String) identifierMap.get("nullable");
					if (s != null) item.setAttribute(s,String.valueOf(rsmd.isNullable(i)));				
	
					s = (String) identifierMap.get("signed");
					if (s != null) item.setAttribute(s,String.valueOf(rsmd.isSigned(i)));									
					
					item.appendChild( document.createTextNode( rsmd.getColumnName(i)) );
					metadata.appendChild(item);
				}	
				
				Node n = selectSingleNode(document, metadataXPath);
				if (n==null)
					throw new RuntimeException("Path for meta data not found");
				n.appendChild(metadata);
			}
		
			/* the data */
			
			if (identifierMap.get("data") !=null) {
				Element data = (Element) selectSingleNode(document, dataXPath);

				String s="";			
				while (cursor.hasNext()) { /* for each row */
					Tuple t = (Tuple) cursor.next();
					item = document.createElement(datarow); // create a new element
					for (int i=1;i<=rsmd.getColumnCount() ;i++) { // for each column
						tu = document.createElement(datacol); // create a new <col> tag
						s = t.getString(i); // get the value
						/* if s = null, and the user has set nullable==false, then an empty string will be inserted instead of s */
						if (s==null) {
							if (nullable)
								tu.appendChild( document.createTextNode(""));
							else
								throw new RuntimeException("Column "+i+" is not nullable!");
						}
						else
							tu.appendChild( document.createTextNode(s));
						item.appendChild(tu);
					}
					data.appendChild(item);
				}	
			}
		}
		catch (Exception e) { 
			throw new WrappingRuntimeException(e);
		}
	} 
	
	/**
		Writes a document in an OutputStream.
		@param document the document 
		@param out a java.util.OutputStream in which the document should be written
	 * @throws IOException
	*/
	public static void DOMtoOutputStream(Document document, OutputStream out) throws IOException  {

		Transformer trans;
		try {
			trans = TransformerFactory.newInstance().newTransformer();
		}
		catch (TransformerConfigurationException e) {
			throw new WrappingRuntimeException(e);
		}

		try {
			trans.transform(new DOMSource(document),new StreamResult(out));
		}
		catch (TransformerException e) {
			throw new WrappingRuntimeException(e);
		}
	}

	/**
	 * This Method builds a document
	 * @param in a java.util.InputStream containing the information about the document
	 * @return a document
	 * @throws Exception
	 */
	public static Document InputStreamtoDOM(InputStream in) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		return dbf.newDocumentBuilder().parse(in);
	}
	
	/**
		Tries to extract data from the document. The XPath expression describes the location of the data/metadata.
		Returns a MetaDataCursor. Default metadata and the default identifier map will be used.
		@param document The document
		@param XPathtoData This XPath expression describes the location of the data
		@return A MetaDataCursor containing the data
	*/		
	public static XMLMetaDataCursor DOMtoMetaDataCursor(Document document, String XPathtoData) {
		return new XMLMetaDataCursor(document, XPathtoData, null, null, Dom.DEFAULT_IDENTIFIER_MAP(), ArrayTuple.FACTORY_METHOD);
	}	
	
	/**
		Tries to extract data from the document. The XPath expression describes the location of the data/metadata.
		Returns a MetaDataCursor. The dafault identifier map will be used.
		@param document The document
		@param XPathtoData This XPath expression describes the location of the data
		@param XPathtoMetadata This XPath expression describes the location of the metadata
		@return A MetaDataCursor containing the data
	*/		
	public static XMLMetaDataCursor DOMtoMetaDataCursor(Document document, String XPathtoData, String XPathtoMetadata) {
		return new XMLMetaDataCursor(document, XPathtoData, XPathtoMetadata, null, Dom.DEFAULT_IDENTIFIER_MAP(), ArrayTuple.FACTORY_METHOD);
	}	
	
	/**
	Tries to extract data from the document. The XPath expression describes the location of the data/metadata.
	Returns a MetaDataCursor.
	@param document The document
	@param XPathtoData This XPath expression describes the location of the data
	@param XPathtoMetadata This XPath expression describes the location of the metadata
	@param identifier A map containing the notation of attributes and tags	
	@return A MetaDataCursor containing the data
	*/		
	public static XMLMetaDataCursor DOMtoMetaDataCursor(Document document, String XPathtoData, String XPathtoMetadata,  Map identifier) {
		return new XMLMetaDataCursor(document, XPathtoData, XPathtoMetadata, null, identifier, ArrayTuple.FACTORY_METHOD);
	}	
	
	/**
		Tries to extract data from the document. The XPath expression describes the location of the data/metadata.
		Returns a MetaDataCursor.
		@param document The document
		@param XPathtoData This XPath expression describes the location of the data
		@param XPathtoMetadata This XPath expression describes the location of the metadata
		@param SQLTypes A stringlist containing the sqltype-names which are used in the document
		@param identifier A map containing the notation of attributes and tags
		@return A MetaDataCursor containing the data
	*/	
	public static XMLMetaDataCursor DOMtoMetaDataCursor(Document document, String XPathtoData, String XPathtoMetadata, String[] SQLTypes, Map identifier) {
		return new XMLMetaDataCursor(document, XPathtoData, XPathtoMetadata, SQLTypes, identifier, ArrayTuple.FACTORY_METHOD);
	}		
	
	/**
		Creats a new DOM document
		@return a new document
	*/
	public static Document emptyDocument() {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setValidating(true);
			dbf.setIgnoringComments(true);
			dbf.setIgnoringElementContentWhitespace(true);
			
			return dbf.newDocumentBuilder().newDocument();
		}
		catch(Exception e) {
			throw new RuntimeException("emptyDocument");	
		}	
	}	
	
	/**
		Creats a new document
		@param documentElement the notation of the first node
		@return a document with one node (DocumentElement)
	*/
	public static Document emptyDocument(String documentElement) {
		try {
			Document document = emptyDocument();  // new document
			document.appendChild(document.createElement( documentElement )); // append one node 
			return document;
		}
		catch(Exception e) {
			throw new RuntimeException("emptyDocument");	
		}	
	}
	
	/**
	 * Use an XPath string to select a single node of an XML document.
	 * @param document The document.
	 * @param XPath The XPath expression.
	 * @return The node which matches the XPath query.
	 */
	public static Node selectSingleNode(Document document, String XPath) {
		if (parser==null)
			throw new RuntimeException("first call Dom.init() to initialize the parser");
		return parser.selectSingleNode(document, XPath);
	}	
	
	/**
		This Method creates a (Hash)Map which can be used to configure the XML generation. It can also 
		be used to configure the generation of XMLResultSets.
		<ul>
		<li>&lt;meta&gt; indicates the metadata</li>
		<li>&lt;col&gt;  indicates the columns, occurs in &lt;row&gt; an in &lt;meta&gt;</li>
		<li>&lt;row&gt;  indicates a row </li>
		</ul>
		The attributes:
		<ul>
		<li>sqltype</li>
		<li>precision</li>
		<li>scale</li>
		<li>autoincrement</li>
		<li>currency</li>
		<li>nullable</li>
		<li>signed</li>
		</ul>
		<br>
		@return Map
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

	/**
	This is a configuration Map which provides the generation of html code.
	@return Map
	*/	
	public static Map DEFAULT_HTML_IDENTIFIER_MAP() {

		HashMap ret = new HashMap();
		
		ret.put("meta","tr");
		ret.put("data","tr");
		ret.put("metacol","th");
		ret.put("datacol","td");
		ret.put("datarow","tr");
		ret.put("sqltype","title");		
	
		return ret;		
	}	
}

