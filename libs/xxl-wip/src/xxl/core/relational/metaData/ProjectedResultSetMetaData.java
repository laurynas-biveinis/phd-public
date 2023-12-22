/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.relational.metaData;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import xxl.core.util.metaData.MetaDataException;

/**
 * Makes a projection of a result set metadata object to certain columns.
 */
public class ProjectedResultSetMetaData extends WrappedResultSetMetaData {
	
	/**
	 * The columns of the relational metadata that will be projected.
	 */
	protected int[] columns;
	
	/**
	 * Creates a new projected result set metadata that contains the columns
	 * identified by the specified indices.
	 * 
	 * @param metaData the result set metadata to be projected.
	 * @param columns the indices of the columns which will be available from
	 *        the projected result set metadata.
	 */
	public ProjectedResultSetMetaData(ResultSetMetaData metaData, int... columns) {
		super(metaData);
		this.columns = columns;
		
		try {
			int columnCount = metaData.getColumnCount();
			for (int column : columns)
				if (column < 1 || columnCount < column)
					throw new MetaDataException("the specified column " + column + " cannot be projected because it does not exist in underlying meta data");
		}
		catch (SQLException sqle) {
			throw new MetaDataException("meta data cannot be constructed due to the following sql exception: " + sqle.getMessage());
		}
	}

	/**
	 * Returns the column number of the original relational metadata that has
	 * been mapped to the column number value that is passed to the call.
	 *
	 * @param column column number of the mapped relational metadata.
	 * @return column number of the original relational metadata.
	 * @throws SQLException if a database access error occurs.
	 */
	@Override
	protected int originalColumnIndex(int column) throws SQLException {
		if (column < 1 || column > columns.length)
			throw new SQLException("the specified column " + column + " cannot be identified");
		return columns[column-1];
	}
	
	/**
	 * Returns the number of columns in this <code>ResultSet</code> object.
	 * 
	 * @return the number of columns.
	 * @throws SQLException if a database access error occurs.
	 */
	@Override
	public int getColumnCount() throws SQLException {
		return columns.length;
	}
}
