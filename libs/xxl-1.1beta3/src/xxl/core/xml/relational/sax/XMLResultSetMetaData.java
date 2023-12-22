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

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import xxl.core.xml.relational.StringToSQLTypeConverterMap;

/**
	An implementation of the ResultSetMetaData interface.
*/
public class XMLResultSetMetaData implements java.sql.ResultSetMetaData {

	private int cols;
	
	private String columns[], columnTypeNames[], columnClassName[];
	private int columnTypes[], precision[], scale[], nullable[];
	private boolean autoincrement[], currency[], signed[];

	/**
	 * Do not create an Object directly. Use getMetadata in XMLResultSet instead.
	 * @param columnList A list with the column names.
	 * @param attributes A list with attributes.
	 * @param convertermap a map with String to Object converters.
	 * @param identifier the map with the xml-identifyer.
	 */	
	protected XMLResultSetMetaData(List columnList, List attributes, StringToSQLTypeConverterMap convertermap, Map identifier) {
		this.cols = columnList.size();
		
		columns = new String[cols];
		columnTypeNames = new String[cols];
		columnClassName = new String[cols];
		columnTypes = new int[cols];
		precision = new int[cols];
		scale = new int[cols];
		autoincrement = new boolean[cols];
		currency = new boolean[cols];
		nullable = new int[cols];
		signed = new boolean[cols];
		
		String sqlTypeId = (String) identifier.get("sqltype");
		String precisionTypeId = (String) identifier.get("precision");
		String scaleTypeId = (String) identifier.get("scale");
		String autoincrementTypeId = (String) identifier.get("autoincrement");	
		String currencyTypeId = (String) identifier.get("currency");	
		String nullableTypeId = (String) identifier.get("nullable");	
		String signedTypeId = (String) identifier.get("signed");	
		
		for (int i=0; i<cols; i++) {
			columns[i] = (String) columnList.get(i);

			Map m = (Map) attributes.get(i);

			if (sqlTypeId!=null) {
				columnTypeNames[i] = (String) m.get (sqlTypeId);
				columnTypes[i] = convertermap.getSQLType(columnTypeNames[i]);
			}
			
			if (precisionTypeId!=null)
				precision[i] = Integer.parseInt ((String) m.get(precisionTypeId));
			if (scaleTypeId!=null)
				scale[i] = Integer.parseInt ((String) m.get(scaleTypeId));
			if (autoincrementTypeId!=null)
				autoincrement[i] = Boolean.getBoolean ((String) m.get(autoincrementTypeId));
			if (currencyTypeId!=null)
				currency[i] = Boolean.getBoolean ((String) m.get(currencyTypeId));
			if (nullableTypeId!=null)
				nullable[i] = Integer.parseInt ((String) m.get(nullableTypeId));
			if (signedTypeId!=null)
				signed[i] = Boolean.getBoolean ((String) m.get(signedTypeId));

			columnClassName[i] = convertermap.getJavaClassName(columnTypeNames[i]);
		}
	}

	/**
	 * Returns null.
	 * @param column The column index starting with 1.
	 * @return null
	 */
	public String getCatalogName(int column) {
		return null;
	}

	/**
	 * @param column The column index starting with 1.
	 * @return The column class name.
	 */
	public String getColumnClassName(int column) {		
		if (column > cols)
			throw new RuntimeException("getColumnClassName - index out of bounds?");			
		return columnClassName[column-1];		
	}
	
	/**
	 * @see java.sql.ResultSetMetaData#getColumnCount()
	 */
	public int getColumnCount() {
		return  cols;
	}
	
	/**
	 * @param column The column index starting with 1.
	 * @see java.sql.ResultSetMetaData#getColumnDisplaySize(int)
	 */
	public int getColumnDisplaySize(int column) {
		return 255;	
	}
	
	/**
	 * @param column The column index starting with 1.
	 * @see java.sql.ResultSetMetaData#getColumnName(int)
	 */
	public String getColumnName(int column) {		
		if (column > cols) throw new RuntimeException("getColumnName - index out of bounds");
		return columns[column-1];
	}
	
	/**
	 * @param column The column index starting with 1.
	 * @see java.sql.ResultSetMetaData#getColumnLabel(int)
	 */
	public String getColumnLabel(int column) {
		return null;
	}
	
	/**
	 * @param column The column index starting with 1.
	 * @see java.sql.ResultSetMetaData#getColumnType(int)
	 */
	public int getColumnType(int column) {
		if (column > cols) throw new RuntimeException("getColumnType - index out of bounds");
		return columnTypes[column-1];
	}
	
	/**
	 * @param column The column index starting with 1.
	 * @see java.sql.ResultSetMetaData#getColumnTypeName(int)
	 */
	public String getColumnTypeName(int column) {
		if (column > cols) throw new RuntimeException("getColumnTypeName - index out of bounds");
		return columnTypeNames[column-1];
	}
	
	/**
	 * @param column The column index starting with 1.
	 * @see java.sql.ResultSetMetaData#getPrecision(int)
	 */
	public int getPrecision(int column) {
		if (column > cols) throw new RuntimeException("getPrecision - index out of bounds");
		return precision[column-1];
	}
	
	/**
	 * @param column The column index starting with 1.
	 * @see java.sql.ResultSetMetaData#getScale(int)
	 */
	public int getScale(int column) {
		if (column > cols) throw new RuntimeException("getScale - index out of bounds");
		return scale[column-1];
	}
	
	/**
	 * @param column The column index starting with 1.
	 * @see java.sql.ResultSetMetaData#getSchemaName(int)
	 */
	public String getSchemaName(int column) {
		return null;		
	}
	
	/**
	 * @param column The column index starting with 1.
	 * @see java.sql.ResultSetMetaData#getTableName(int)
	 */
	public String getTableName(int column) {
		return null;	
	}
	
	/**
	 * @param column The column index starting with 1.
	 * @see java.sql.ResultSetMetaData#isAutoIncrement(int)
	 */
	public boolean isAutoIncrement(int column) {
		if (column > cols) throw new RuntimeException("isAutoIncrement - index out of bounds");	
		return autoincrement[column-1];
	}
	
	/**
	 * @param column The column index starting with 1.
	 * @see java.sql.ResultSetMetaData#isCaseSensitive(int)
	 */
	public boolean isCaseSensitive(int column) {
		return false;	
	}
	
	/**
	 * @param column The column index starting with 1.
	 * @see java.sql.ResultSetMetaData#isCurrency(int)
	 */
	public boolean isCurrency(int column) {
		if (column > cols) throw new RuntimeException("isCurrency - index out of bounds");	
		return currency[column-1];
	}
	
	/**
	 * @param column The column index starting with 1.
	 * @see java.sql.ResultSetMetaData#isDefinitelyWritable(int)
	 */
	public boolean isDefinitelyWritable(int column) {
		return false;	
	}
	
	/**
	 * @param column The column index starting with 1.
	 * @see java.sql.ResultSetMetaData#isNullable(int)
	 */
	public int isNullable(int column) {
		if (column > cols) throw new RuntimeException("isNullable - index out of bounds");	
		return nullable[column-1];
	}
	
	/**
	 * @param column The column index starting with 1.
	 * @see java.sql.ResultSetMetaData#isReadOnly(int)
	 */
	public boolean isReadOnly(int column) {
		return true;	
	}
	
	/**
	 * @param column The column index starting with 1.
	 * @see java.sql.ResultSetMetaData#isSearchable(int)
	 */
	public boolean isSearchable(int column) {
		return false;	
	}
	
	/**
	 * @param column The column index starting with 1.
	 * @see java.sql.ResultSetMetaData#isSigned(int)
	 */
	public boolean isSigned(int column) {
		if (column > cols) throw new RuntimeException("isSigned - index out of bounds");
		return signed[column-1];
	}
	
	/**
	 * @param column The column index starting with 1.
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
	public Object unwrap(java.lang.Class<?> iface) throws SQLException {
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
	public boolean isWrapperFor(java.lang.Class<?> iface) throws SQLException {
		throw new UnsupportedOperationException("this method is not implemented yet.");
	}
	
}
