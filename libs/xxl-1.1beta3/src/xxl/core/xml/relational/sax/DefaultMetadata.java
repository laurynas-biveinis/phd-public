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


/**
	An implementation of the java.sql.ResultSetMetaData interface.
	<br>This class consists of methods, which return only <b>default values</b>.<br>
	i.e. the method getColumnTypeName() returns always "LONGVARCHAR".<br><br>
	<b>Note:</b> The getColumnCount() method is the only method, that returns a value, which isn't fixed.
*/
public  class DefaultMetadata implements java.sql.ResultSetMetaData {
	private int cols;
	
	/**
		Constructor.
		@param cols the number of columns
	*/
	protected DefaultMetadata(int cols) {
		this.cols = cols;
	}

	/**
	 * Returns null.
	 * @param column The column index starting with 1.
	 * @return null.
	 */
	public String getCatalogName(int column) {
		return null;
	}

	/**
	 * Return "java.lang.String".
	 * @param column The column index starting with 1.
	 * @return "java.lang.String".
	 */
	public String getColumnClassName(int column) {
		return "java.lang.String";
	}
	
	/**
	 * Returns the number of columns.
	 * @return the number of columns.
	 */
	public int getColumnCount() {
		return  cols;
	}
	
	/**
	 * Return 255.
	 * @param column The column index starting with 1.
	 * @return 255
	 */
	public int getColumnDisplaySize(int column) {
		return 255;
	}

	/**
	 * Returns "columnx", where x is the given int value.
	 * @param column The column index starting with 1.
	 * @return "columnx", where x is the given int value.
	 */
	public String getColumnName(int column) {
		return "column"+column;
	}
	
	/**
	 * Returns null.
	 * @param column The column index starting with 1.
	 * @return null.
	 */	
	public String getColumnLabel(int column) {
		return null;
	}
	
	/**
	 * Returns 12.
	 * @param column The column index starting with 1.
	 * @return 12
	 */
	public int getColumnType(int column) {
		return 12;
	}
	
	/**
	 * Returns "LONGVARCHAR".
	 * @param column The column index starting with 1.
	 * @return "LONGVARCHAR"
	 */
	public String getColumnTypeName(int column) {
		return "LONGVARCHAR";
	}
	
	/**
	 * Returns 255.
	 * @param column The column index starting with 1.
	 * @return 255
	 */	
	public int getPrecision(int column) {
		return 255;
	}
	
	/**
	 * Returns 1.
	 * @param column The column index starting with 1.
	 * @return 1
	 */	
	public int getScale(int column) {
		return 1;
	}
	
	/**
	 * Returns null.
	 * @param column The column index starting with 1.
	 * @return null
	 */
	public String getSchemaName(int column) {
		return null;		
	}

	/**
	 * Returns null.
	 * @param column The column index starting with 1.
	 * @return null
	 */	
	public String getTableName(int column) {
		return null;	
	}

	/**
	 * Returns false.
	 * @param column The column index starting with 1.
	 * @return false
	 */	
	public boolean isAutoIncrement(int column) {
		return false;
	}
	
	/**
	 * Returns false.
	 * @param column The column index starting with 1.
	 * @return false
	 */
	public boolean isCaseSensitive(int column) {
		return false;
	}
	
	/**
	 * Returns false.
	 * @param column The column index starting with 1.
	 * @return false
	 */	
	public boolean isCurrency(int column) {
		return false;
	}

	/**
	 * Returns false.
	 * @param column The column index starting with 1.
	 * @return false
	 */		
	public boolean isDefinitelyWritable(int column) {
		return false;	
	}
	
	/**
	 * Returns 0.
	 * @param column The column index starting with 1.
	 * @return 0
	 */		
	public int isNullable(int column) {
		return 0;
	}

	/**
	 * Returns true.
	 * @param column The column index starting with 1.
	 * @return true
	 */		
	public boolean isReadOnly(int column) {
		return true;	
	}

	/**
	 * Returns false.
	 * @param column The column index starting with 1.
	 * @return false
	 */		
	public boolean isSearchable(int column) {
		return false;	
	}

	/**
	 * Returns false.
	 * @param column The column index starting with 1.
	 * @return false
	 */
	public boolean isSigned(int column) {
		return false;
	}
	
	/**
	 * Returns false.
	 * @param column The column index starting with 1.
	 * @return false
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
