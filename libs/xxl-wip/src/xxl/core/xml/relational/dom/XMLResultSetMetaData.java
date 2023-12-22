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
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import xxl.core.xml.relational.StringToSQLTypeConverterMap;

/**
	An implemtation of the java.sql.ResultSetMetaData interface.<br> <br>
	
	An object that can be used to find out about the types and properties of the columns in a ResultSet. 
	If the XML Code  support i.e. no information about the precision of the columns, then the user has
	to remove the precision entry in the identifier map. Otherwise the method getPrecision will throw an exception.
*/
public class XMLResultSetMetaData implements ResultSetMetaData {
	
	static int columnNoNulls;
	static int columnNullable;
	static int columnNullableUnknow;
	
	private Element metadatabase_pointer;	// org.w3c.dom.Element
	private String tableName="";
	private int cols;
	
	private ArrayList<String> typeList;
	
	private StringToSQLTypeConverterMap converterMap;
	
	private Map identifier;

	/**
	 * Do not call this constructor directly. Use getMetadata in XMLResultSet!
	 * @param document The document
	 * @param xPathToMetadata This XPath expression describes the location of the metadata
	 * @param converterMap The map for conversion between strings and other types.
	 * @param id Map containing the identifyer.
	 */	
	protected XMLResultSetMetaData(Document document, String xPathToMetadata, StringToSQLTypeConverterMap converterMap, Map id) {
		try {
		 	this.converterMap = converterMap;
			metadatabase_pointer = (Element) Dom.selectSingleNode(document, xPathToMetadata);	
			
			identifier = id;
			
			cols = metadatabase_pointer.getChildNodes().getLength();
		 	
			typeList = new ArrayList<String>();
			typeList.add("");
			String S;
			for (int i=1;i<=cols;i++) {
				S = (String) id.get("sqltype");
				typeList.add(((Element) metadatabase_pointer.getChildNodes().item(i-1)).getAttribute(S));					
			}
		}
		catch (ClassCastException  e) {
			throw new RuntimeException("XMLResultSetMetaData: illegal structure");
		}
		catch (RuntimeException e) {
			throw new RuntimeException("XMLResultSetMetaData: parser error: first call Dom.init() to initialize the parser");
		}
		catch (Exception e) {
			throw new RuntimeException("Error in XMLResultSetMetaData");
		}
	}

	/**
	 * @see java.sql.ResultSetMetaData#getCatalogName(int)
	 */
	public String getCatalogName(int column) {
		return "";
	}

	/**
	 * @see java.sql.ResultSetMetaData#getColumnClassName(int)
	 */
	public String getColumnClassName(int column) {		
		if (column > cols)
			throw new RuntimeException("XMLResultSetMetaData: getColumnClassName - index out of bounds?");
		return (converterMap.getJavaClassName(typeList.get(column)));
	}
	
	/**
	 * @see java.sql.ResultSetMetaData#getColumnCount()
	 */
	public int getColumnCount() {
		return  metadatabase_pointer.getChildNodes().getLength();
	}
	
	/**
	 * @see java.sql.ResultSetMetaData#getColumnDisplaySize(int)
	 */
	public int getColumnDisplaySize(int column) {
		return 255;
	}
	
	
	/**
	 * @see java.sql.ResultSetMetaData#getColumnName(int)
	 */
	public String getColumnName(int column) {		
		if (column > cols)
			throw new RuntimeException("XMLResultSetMetaData: getColumnName - index out of bounds?");	
		return metadatabase_pointer.getChildNodes().item(column-1).getFirstChild().getNodeValue();
	}
	
	/**
	 * @see java.sql.ResultSetMetaData#getColumnLabel(int)
	 */
	public String getColumnLabel(int column) {
		return "";
	}
	
	/**
	 * @see java.sql.ResultSetMetaData#getColumnType(int)
	 */
	public int getColumnType(int column) {
		if (column > cols)
			throw new RuntimeException("XMLResultSetMetaData: getColumnType - index out of bounds?");		
		return (converterMap.getSQLType(typeList.get(column)));
	}
	
	/**
	 * @see java.sql.ResultSetMetaData#getColumnTypeName(int)
	 */
	public String getColumnTypeName(int column) {
		if (column > cols)
			throw new RuntimeException("XMLResultSetMetaData: getColumnTypeName - index out of bounds?");
		return typeList.get(column);
	}
	
	/**
	 * @see java.sql.ResultSetMetaData#getPrecision(int)
	 */
	public int getPrecision(int column) {
		if (column > cols)
			throw new RuntimeException("XMLResultSetMetaData: getPrecision - index out of bounds?");
		String s = (String) identifier.get("precision");
		if (s == null)
			throw new RuntimeException ("XMLResultSetMetaData.getPrecision: no identifier found, check the identifier map!");
		return Integer.parseInt (((Element) metadatabase_pointer.getChildNodes().item(column-1)).getAttribute(s));
	}
	
	/**
	 * @see java.sql.ResultSetMetaData#getScale(int)
	 */
	public int getScale(int column) {
		if (column > cols)
			throw new RuntimeException("XMLResultSetMetaData: getScale - index out of bounds?");
		String s = (String) identifier.get("scale");
		if (s == null)
			throw new RuntimeException ("XMLResultSetMetaData.getScale: no identifier found, check the identifier map!");
		return Integer.parseInt (((Element) metadatabase_pointer.getChildNodes().item(column-1)).getAttribute(s));
	}
	
	/**
	 * @see java.sql.ResultSetMetaData#getSchemaName(int)
	 */
	public String getSchemaName(int column) {
		return "unknown";
	}
	
	/**
	 * @see java.sql.ResultSetMetaData#getTableName(int)
	 */
	public String getTableName(int column) {
		return tableName;
	}
	
	/**
	 * @see java.sql.ResultSetMetaData#isAutoIncrement(int)
	 */
	public boolean isAutoIncrement(int column) {
		if (column > cols)
			throw new RuntimeException("XMLResultSetMetaData: isAutoIncrement - index out of bounds?");	
		String s = (String) identifier.get("autoincrement");	
		if (s == null)
			throw new RuntimeException ("XMLResultSetMetaData.isAutoIncrement: no identifier found, check the identifier map!");
		return Boolean.getBoolean (((Element) metadatabase_pointer.getChildNodes().item(column-1)).getAttribute(s));
	}
	
	/**
	 * @see java.sql.ResultSetMetaData#isCaseSensitive(int)
	 */
	public boolean isCaseSensitive(int column) {
		return false;
	}
	
	/**
	 * @see java.sql.ResultSetMetaData#isCurrency(int)
	 */
	public boolean isCurrency(int column) {
		if (column > cols)
			throw new RuntimeException("XMLResultSetMetaData: isCurrency - index out of bounds?");	
		String s = (String) identifier.get("currency");	
		if (s == null)
			throw new RuntimeException ("XMLResultSetMetaData.isCurrency: no identifier found, check the identifier map!");
		return Boolean.getBoolean (((Element) metadatabase_pointer.getChildNodes().item(column-1)).getAttribute(s));
	}
	
	/**
	 * @see java.sql.ResultSetMetaData#isDefinitelyWritable(int)
	 */
	public boolean isDefinitelyWritable(int column) {
		return false;
	}
	
	/**
	 * @see java.sql.ResultSetMetaData#isNullable(int)
	 */
	public int isNullable(int column) {
		if (column > cols)
			throw new RuntimeException("XMLResultSetMetaData: isNullable - index out of bounds?");	
		String s = (String) identifier.get("nullable");	
		if (s == null)
			throw new RuntimeException ("XMLResultSetMetaData.isNullable: no identifier found, check the identifier map!");
		return Integer.parseInt (((Element) metadatabase_pointer.getChildNodes().item(column-1)).getAttribute(s));
	}
	
	/**
	 * @see java.sql.ResultSetMetaData#isReadOnly(int)
	 */
	public boolean isReadOnly(int column) {
		return true;
	}
	
	/**
	 * @see java.sql.ResultSetMetaData#isSearchable(int)
	 */
	public boolean isSearchable(int column) {
		return false;
	}
	
	/**
	 * @see java.sql.ResultSetMetaData#isSigned(int)
	 */
	public boolean isSigned(int column) {
		if (column > cols)
			throw new RuntimeException("XMLResultSetMetaData: isSigned - index out of bounds?");	
		String s = (String) identifier.get("signed");	
		if (s == null)
			throw new RuntimeException ("XMLResultSetMetaData.isSigned: no identifier found, check the identifier map!");
		return Boolean.getBoolean (((Element) metadatabase_pointer.getChildNodes().item(column-1)).getAttribute(s));
	}
	
	/**
	 * @see java.sql.ResultSetMetaData#isWritable(int)
	 */
	public boolean isWritable(int column) {
		return false;
	}
	
	/**
	 * Returns an object that implements the given interface to allow access to
	 * non-standard methods, or standard methods not exposed by the proxy. The
	 * result may be either the object found to implement the interface or a
	 * proxy for that object. If the receiver implements the interface then
	 * that is the object. If the receiver is a wrapper and the wrapped object
	 * implements the interface then that is the object. Otherwise the object
	 * is the result of calling <code>unwrap</code> recursively on the wrapped
	 * object. If the receiver is not a wrapper and does not implement the
	 * interface, then an <code>SQLException</code> is thrown.
	 *
	 * @param iface a class defining an interface that the result must
	 *        implement.
	 * @return an object that implements the interface. May be a proxy for the
	 *         actual implementing object.
	 * @throws SQLException if no object found that implements the interface.
	 */
	public <T> T unwrap(Class<T> iface) throws SQLException {
		throw new UnsupportedOperationException("this method is not implemented yet.");
	}
	
	/**
	 * Returns true if this either implements the interface argument or is
	 * directly or indirectly a wrapper for an object that does. Returns false
	 * otherwise. If this implements the interface then return true, else if
	 * this is a wrapper then return the result of recursively calling
	 * <code>isWrapperFor</code> on the wrapped object. If this does not
	 * implement the interface and is not a wrapper, return false. This method
	 * should be implemented as a low-cost operation compared to
	 * <code>unwrap</code> so that callers can use this method to avoid
	 * expensive <code>unwrap</code> calls that may fail. If this method
	 * returns true then calling <code>unwrap</code> with the same argument
	 * should succeed.
	 *
	 * @param iface a class defining an interface.
	 * @return true if this implements the interface or directly or indirectly
	 *         wraps an object that does.
	 * @throws SQLException if an error occurs while determining whether this
	 *         is a wrapper for an object with the given interface.
	 */
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		throw new UnsupportedOperationException("this method is not implemented yet.");
	}
	
}
