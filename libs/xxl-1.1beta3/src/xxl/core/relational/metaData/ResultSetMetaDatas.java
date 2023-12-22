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

	http://www.mathematik.uni-marburg.de/DBS/xxl

bugs, requests for enhancements: xxl@mathematik.uni-marburg.de

If you want to be informed on new versions of XXL you can 
subscribe to our mailing-list. Send an email to 
	
	xxl-request@lists.uni-marburg.de

without subject and the word "subscribe" in the message body. 
*/

package xxl.core.relational.metaData;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Comparator;

import xxl.core.functions.Function;
import xxl.core.util.Arrays;
import xxl.core.util.metaData.CompositeMetaData;
import xxl.core.util.metaData.MetaDataException;
import xxl.core.util.metaData.MetaDataProvider;
import xxl.core.util.metaData.MetaDataProviders;

/**
 * This class provides static methods for dealing with instances implementing
 * the interface {@link java.sql.ResultSetMetaData result set metadata}. Beside
 * this methods, it contains constants for identifying columns storing data
 * that is rather important for query processing and optimization.
 * 
 * @see java.sql.ResultSetMetaData
 */
public class ResultSetMetaDatas {
	
	/**
	 * This constant provides a comparator for relational metadata (result set
	 * metadata). The given relational metadata is compared for the number of
	 * columns, the column names, the column types and a possible loss of
	 * precision.
	 */
	public static final Comparator<ResultSetMetaData> RESULTSET_METADATA_COMPARATOR = new Comparator<ResultSetMetaData>() {
		public int compare(ResultSetMetaData rsmd1, ResultSetMetaData rsmd2) {
			try {
				int compare = ((Integer)rsmd1.getColumnCount()).compareTo(rsmd2.getColumnCount());
				if (compare != 0)
					return compare;
				
				for (int column = 1; column <= rsmd1.getColumnCount(); column++) {
					compare = rsmd1.getColumnName(column).compareToIgnoreCase(rsmd2.getColumnName(column));
					if (compare != 0)
						return compare;
					
					compare = ((Integer)rsmd1.getColumnType(column)).compareTo(rsmd2.getColumnType(column));
					if (compare != 0)
						return compare;
					
					compare = ((Integer)rsmd1.getPrecision(column)).compareTo(rsmd2.getPrecision(column));
					if (compare != 0)
						return compare;
				}
				return 0;
			}
			catch (SQLException sqle) {
				throw new MetaDataException("relational metadata information cannot be compared because of the following SQL exception : " + sqle.getMessage());
			}
		}
	};
	
	/**
	 * This constant provides a hash function for relational metadata (result
	 * set metadata). The hash-code of a given relational metadata is defined as
	 * the sum of number of columns, the hash-codes of the column names, the
	 * column types and a the precision.
	 */
	public static final Function<ResultSetMetaData, Integer> RESULTSET_METADATA_HASH_FUNCTION = new Function<ResultSetMetaData, Integer>() {
		public Integer invoke(ResultSetMetaData rsmd) {
			try {
				int hashCode = 0;
				for (int column = 1; column <= rsmd.getColumnCount(); column++)
					hashCode += 1 + rsmd.getColumnName(column).hashCode() + rsmd.getColumnType(column) + rsmd.getPrecision(column);
				return hashCode;
			}
			catch (SQLException sqle) {
				throw new MetaDataException("relational metadata information cannot be accessed because of the following SQL exception : " + sqle.getMessage());
			}
		}
	};
	
	/**
	 * This constant can be used to indicate that a column of the described
	 * result set stores the start timestamp of the tuple's time interval.
	 */
	public static final String TIME_INTERVAL_START_TIMESTAMP = "TIME_INTERVAL_START_TIMESTAMP";
	
	/**
	 * This constant can be used to indicate that a column of the described
	 * result set stores the end timestamp of the tuple's time interval.
	 */
	public static final String TIME_INTERVAL_END_TIMESTAMP = "TIME_INTERVAL_END_TIMESTAMP";

	/**
	 * This constant can be used to identify relational metadata inside a
	 * composite metadata.
	 */
	public static final String RESULTSET_METADATA_TYPE = "RESULT_SET_METADATA";
	
	/**
	 * Returns the metadata fragment of the given metadata provider's metadata
	 * representing its relational metadata.
	 * 
	 * @param metaDataProvider the metadata provider containing the desired
	 *        relational metadata fragment.
	 * @return the relational metadata fragment of the given metadata provider's
	 *         metadata.
	 * @throws MetaDataException when the given metadata provider's metadata
	 *         does not contain any relational metadata fragment.
	 */
	public static ResultSetMetaData getResultSetMetaData(MetaDataProvider<? extends CompositeMetaData<? super String, ? extends Object>> metaDataProvider) throws MetaDataException {
		return (ResultSetMetaData)MetaDataProviders.getMetaDataFragment(metaDataProvider, RESULTSET_METADATA_TYPE);
	}
	
	/**
	 * Resolves the number of the column with the specified name in the given
	 * relational metadata. When no column name matches the given name
	 * (ignoring its case) 0 is returned.
	 * 
	 * @param resultSetMetaData the relational metadata that is searched for
	 *        the given column name.
	 * @param columnName the column name which column number in the given
	 *        relational metadata should be resolved.
	 * @return the number of the column with the specified name in the given
	 *         relational metadata.
	 * @throws SQLException if a database access error occurs.
	 */
	public static int getColumnIndex(ResultSetMetaData resultSetMetaData, String columnName) throws SQLException {
		for (int column = 1; column <= resultSetMetaData.getColumnCount(); column++)
			if (columnName.equalsIgnoreCase(resultSetMetaData.getColumnName(column)))
				return column;
		return 0;
	}
	
	/**
	 * Transfers an array of column names into an array of indices. For every
	 * column, a partner is searched in the
	 * {@link java.sql.ResultSetMetaData result set's metadata}. When no column
	 * name matches the given name (ignoring its case) 0 is inserted into the
	 * array to be returned.
	 *
	 * @param resultSetMetaData the relational metadata that is searched for
	 *        the given column names.
	 * @param columnNames an array of strings that contains the names of some
	 *        of the result set's columns.
	 * @return an array of int values containing the indices of the given
	 *         columns.
	 * @throws SQLException if a database access error occurs.
	 */
	public static int[] getColumnIndices(ResultSetMetaData resultSetMetaData, String... columnNames) throws SQLException {
		int[] columnIndices = new int[columnNames.length];
		for (int i = 0; i < columnIndices.length; i++)
			columnIndices[i] = getColumnIndex(resultSetMetaData, columnNames[i]);
		return columnIndices;
	}

	/**
	 * Resolves the number of the column with the specified table and column
	 * name in the given relational metadata. When no column matches the given
	 * names (ignoring its case) 0 is returned.
	 * 
	 * @param resultSetMetaData the relational metadata that is searched for
	 *        the given column name.
	 * @param tableName the table name of the column whose column number in the
	 *        given relational metadata should be resolved.
	 * @param columnName the name of the column whose column number in the
	 *        given relational metadata should be resolved.
	 * @return the number of the column with the specified table and column
	 *         name in the given relational metadata.
	 * @throws SQLException if a database access error occurs.
	 */
	public static int getColumnIndex(ResultSetMetaData resultSetMetaData, String tableName, String columnName) throws SQLException {
		for (int column = 1; column <= resultSetMetaData.getColumnCount(); column++)
			if (tableName.equalsIgnoreCase(resultSetMetaData.getTableName(column)) && columnName.equalsIgnoreCase(resultSetMetaData.getColumnName(column)))
				return column;
		return 0;
	}
	
	/**
	 * Resolves the number of the column with the specified schema, table and
	 * column name in the given relational metadata. When no column matches the
	 * given names (ignoring its case) 0 is returned.
	 * 
	 * @param resultSetMetaData the relational metadata that is searched for
	 *        the given column name.
	 * @param schemaName the schema name of the column whose column number in
	 *        the given relational metadata should be resolved.
	 * @param tableName the table name of the column whose column number in the
	 *        given relational metadata should be resolved.
	 * @param columnName the name of the column whose column number in the
	 *        given relational metadata should be resolved.
	 * @return the number of the column with the specified schema, table and
	 *         column name in the given relational metadata.
	 * @throws SQLException if a database access error occurs.
	 */
	public static int getColumnIndex(ResultSetMetaData resultSetMetaData, String schemaName, String tableName, String columnName) throws SQLException {
		for (int column = 1; column <= resultSetMetaData.getColumnCount(); column++)
			if (schemaName.equalsIgnoreCase(resultSetMetaData.getSchemaName(column)) && tableName.equalsIgnoreCase(resultSetMetaData.getTableName(column)) && columnName.equalsIgnoreCase(resultSetMetaData.getColumnName(column)))
				return column;
		return 0;
	}
	
	/**
	 * Transfers an array of indices into an array of column names. To get the
	 * column names, this method uses a result set 's metadata that is passed
	 * to the method call.
	 *
	 * @param resultSetMetaData the relational metadata that is searched for
	 *        the given column indices.
	 * @param columnIndices an array of int values that contains indices of
	 *        some of the result set's columns.
	 * @return an array of string objects containing the names of the given
	 *         columns.
	 * @throws SQLException if a database access error occurs.
	 */
	public static String[] getColumnNames(ResultSetMetaData resultSetMetaData, int... columnIndices) throws SQLException {
		String[] columnNames = new String[columnIndices.length];
		for (int i = 0; i < columnNames.length; i++)
			columnNames[i] = resultSetMetaData.getColumnName(columnIndices[i]);
		return columnNames;
	}

	/**
	 * Get the columns numbers of strings in a metadata object.
	 *
	 * @param resultSetMetaData the relational metadata that is searched for
	 *        string columns.
	 * @return a new array of the appropriate length.
	 */
	public static int[] getStringColumns(ResultSetMetaData resultSetMetaData) {
		int[] columns = null;
		int count = 0;
		try {
			columns = new int[resultSetMetaData.getColumnCount()];

			for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++)
				if (resultSetMetaData.getColumnClassName(i).equals("java.lang.String"))
					columns[count++] = i;
		}
		catch (SQLException e) {}

		// construct a new array of the appropriate length
		return Arrays.copy(columns, 0, count);
	}

	/**
	 * The default constructor has private access in order to ensure
	 * non-instantiability.
	 */
	private ResultSetMetaDatas() {
		// private access in order to ensure non-instantiability
	}

}
