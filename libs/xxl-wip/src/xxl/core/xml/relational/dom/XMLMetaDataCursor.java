/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.xml.relational.dom;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import xxl.core.cursors.AbstractCursor;
import xxl.core.functions.Function;
import xxl.core.relational.metaData.ResultSetMetaDatas;
import xxl.core.util.WrappingRuntimeException;
import xxl.core.util.metaData.CompositeMetaData;
import xxl.core.xml.relational.StringToSQLTypeConverterMap;

/**
	An implementation of the MetaDataCursor interface. <br><br>
	<b> Note: </b> The constructor should not be used directly. Use Dom.DOMtoMetaDataCursor instead. <br>
	<br>
	Direct use: This class uses the Cursor.parser XMLParser, so you have to call Cursor.init to initialize the parser.
*/
public class XMLMetaDataCursor extends AbstractCursor implements xxl.core.cursors.MetaDataCursor
{
	private Element metadatabase_pointer;	// org.w3c.dom.Element
	private Element database_pointer;		// org.w3c.dom.Element
	
	private Element cursor;				//points to the actual row
	
	private int col_number=-1;
	
	private String xPathToMetadata;
	private Document doc;
	
	private boolean first=true;
	
	private StringToSQLTypeConverterMap converterMap;
	
	/**
	 * Map containing the identifyer.
	 */
	private Map identifier;
	
	private ResultSetMetaData rsmd;
	
	private CompositeMetaData<Object, Object> globalMetaData;
	
	private Function createTuple;
	
	private Object next;
	
	/**
	 * Creats an XMLMetaDataCursor.
	 * @param document The document
	 * @param xPathToData This XPath expression describes the location of the data
	 * @param xPathToMetadata This XPath expression describes the location of the metadata
	 * @param sqlTypes A string[] containing the sqltype-names which are used in the document.
	 * @param id Map containing the identifyer.
	 * @param createTuple Function that maps an Object array (column values) and a 
	 *	  	ResultSetMetaData object to a new result Tuple. xxl.relational.ArrayTuple.FACTORY_METHOD
	 *	  	can be used.
	 */
	public XMLMetaDataCursor (Document document,String xPathToData,String xPathToMetadata, String[] sqlTypes, Map id, Function createTuple) {
		// save important parameters
		this.createTuple = createTuple;
		this.doc = document; // the document
		this.xPathToMetadata = xPathToMetadata; // the XPath expression pointing to the metadata
		this.identifier = id; // the map, containing the identifier
		
		try {
			// the XPathtoData expression points to the first data row of the "table".
			// Also it's always possible to return to the first row (reset) 
			// Using the cursor without this pointer whould not allow this
			database_pointer = (Element) Dom.selectSingleNode(document, xPathToData);
			cursor = (Element) database_pointer.getFirstChild();
		}
		catch (Exception e) {
			throw new RuntimeException("XMLMetaDataCursor was not able to find data: "+e);
		}
		
		try {
			// If there's an XPath expression for the metadata, then we should calculate the number of columns here
			// Otherwise the number of colums should be equal to the number of column in the first row.
			if (xPathToMetadata != null) { // with metadata
				metadatabase_pointer = (Element) Dom.selectSingleNode(document, xPathToMetadata);
				col_number =  metadatabase_pointer.getChildNodes().getLength();  // number of rows
			}
			else { // without metadata
				// System.out.println("DB pointer: "+database_pointer.getNodeName());
				col_number = database_pointer.getChildNodes().getLength();
			}
		}
		catch (Exception e) {
			throw new RuntimeException("XMLMetaDataCursor was not able to find meta data: "+e);
		}
		
		// Create a converterMap.
		// If the user has spezified the required SQLTypes, then the map consist only of
		// converters for this types.
		// Otherwise the map contains all existing converters. (there's a table of the standard mapping. --> StringToSQLTypeConverterMap )
		// The map will be used in MetaDateResultSet, too. So it's necessary to create this map here.
		if (sqlTypes != null)
			converterMap = new StringToSQLTypeConverterMap(sqlTypes);
		else
			converterMap = new StringToSQLTypeConverterMap();
		
		// ..... also the ResultSetMetadata should be created here.
		
		globalMetaData = new CompositeMetaData<Object, Object>();
		if (metadatabase_pointer != null)
			globalMetaData.add(ResultSetMetaDatas.RESULTSET_METADATA_TYPE, rsmd = new XMLResultSetMetaData(doc, xPathToMetadata, converterMap, identifier));
		else {
			// construct default metadata
			Document docu = Dom.emptyDocument();		// new document
			Element root = docu.createElement("root");	// new element
			Element meta = docu.createElement("meta");
			Element item;
			
			for (int i=1;i<=col_number;i++) {
				item = docu.createElement("col");			// put the column description between the <col> tags
				item.setAttribute("sqltype","LONGVARCHAR"); 		// add a default SQLType
				item.setAttribute("precision","50");
				item.setAttribute("scale","50");
				item.setAttribute("autoincrement","false");
				item.setAttribute("currency","false");
				item.setAttribute("nullable","0");
				item.setAttribute("signed","false");
				
				item.appendChild(docu.createTextNode("column"+i)); 	// add default column names: column1, column2, .....
				meta.appendChild(item);
			}			
			
			root.appendChild(meta);
			docu.appendChild(root);
			
			HashMap ret = new HashMap();
			ret.put("sqltype","sqltype");
			ret.put("precision","precision");
			ret.put("scale","scale");
			ret.put("autoincrement","autoincrement");
			ret.put("currency","currency");
			ret.put("nullable","nullable");
			ret.put("signed","signed");
			
			globalMetaData.add(ResultSetMetaDatas.RESULTSET_METADATA_TYPE, rsmd = new XMLResultSetMetaData(docu, "/root/meta", converterMap, ret));
		}

	}
	
	/**
	 * Moves the cursor down one row from its current position and computes the next tuple.
	 * A cursor is initially positioned before the first row.
	 * The first call to next makes the first row the current row.
	 * The second call makes the second row the current row, and so on.
	 * <p> 
	 * Implementation: This methode is implemented using "cursor = cursor.getNextSibling()"
	 * @return true, iff there is a next Object inside the iteration.
	 */
	public boolean hasNextObject() {

		if (!first)
			cursor = (Element) cursor.getNextSibling();
		else {
			// first call
			cursor = database_pointer;
			first = false;
		}
		
		if (cursor==null)
			return false;

		// System.out.println("Nodename: "+cursor.getNodeName());
		// System.out.println("Prefix: "+cursor.getPrefix());
		
		try {
			int columns = rsmd.getColumnCount();
			List<Object> ol = new ArrayList<Object>(columns);
			for (int columnIndex=1 ; columnIndex<=columns ; columnIndex++) {
				Node colNode=cursor.getChildNodes().item(columnIndex-1);
				// if table is uncomplete!
				if (colNode!=null) {
					Node node=colNode.getFirstChild();
					if (node!=null)
						ol.add(converterMap.getObject(node.getNodeValue(), rsmd.getColumnTypeName(columnIndex) ));
				}
				else
					ol.add(null);
			}
			next = createTuple.invoke(ol);
			return true;
		}
		catch (SQLException e) {
			throw new WrappingRuntimeException(e);
		}
	}
	
	/**
	 * Returns the next Object of the iteration.
	 * @see xxl.core.cursors.AbstractCursor#nextObject()
	 */
	public Object nextObject() {
		return next;
	}
	
	/**
		Moves the cursor to the first row.
	*/
	public void reset() {
		super.reset();
		cursor = (Element) database_pointer.getFirstChild();
		first = true;
	}
	
	/**
	 * @see xxl.core.cursors.Cursor#supportsReset()
	 */
	public boolean supportsReset() {
		return true;
	}
	
	/**
		Retrieves the number, types and properties of a ResultSet's columns.
		
		Return a default description if no metadata is available.
		@return the description of a ResultSet's columns	
	*/
	public Object getMetaData() {	
		return globalMetaData;
	}	
}
