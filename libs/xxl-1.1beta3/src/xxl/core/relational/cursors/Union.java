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

package xxl.core.relational.cursors;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import xxl.core.cursors.MetaDataCursor;
import xxl.core.cursors.unions.Sequentializer;
import xxl.core.functions.Function;
import xxl.core.relational.metaData.ResultSetMetaDatas;
import xxl.core.relational.tuples.Tuple;
import xxl.core.util.metaData.CompositeMetaData;
import xxl.core.util.metaData.MetaDataException;

/**
 * Union is a straight forward implementation of the operator union. It is
 * based on {@link Sequentializer}. The elements of both input relations become
 * appended. The metadata of both inputs has to be compatible.
 * 
 * <p>The example in the main method wraps two cursors containing tuples
 * representing the elements of enumerations (integers 0 to 9 and integers 10
 * to 19) to metadata cursors using
 * {@link xxl.core.cursors.Cursors#wrapToMetaDataCursor(java.util.Iterator, Object)}. 
 * Then, the union operator is applied.</p>
 */
public class Union extends Sequentializer<Tuple> implements MetaDataCursor<Tuple, CompositeMetaData<Object, Object>> {

	/**
	 * The metadata provided by the selection.
	 */
	protected CompositeMetaData<Object, Object> globalMetaData;

	/**
	 * Constructs an instance of the union operator.
	 *
	 * @param cursor1 the first metadata cursor containing the input elements.
	 * @param cursor2 the second metadata cursor containing the input elements.
	 */
	public Union(MetaDataCursor<Tuple, CompositeMetaData<Object, Object>> cursor1, MetaDataCursor<Tuple, CompositeMetaData<Object, Object>> cursor2) {
		super(cursor1, cursor2);
		
		ResultSetMetaData metaData1 = ResultSetMetaDatas.getResultSetMetaData(cursor1);
		ResultSetMetaData metaData2 = ResultSetMetaDatas.getResultSetMetaData(cursor2);

		if (ResultSetMetaDatas.RESULTSET_METADATA_COMPARATOR.compare(metaData1, metaData2) != 0)
			throw new MetaDataException("the difference of the given cursors cannot be computed because they differ in their relational metadata");
		
		globalMetaData = new CompositeMetaData<Object, Object>();
		globalMetaData.add(ResultSetMetaDatas.RESULTSET_METADATA_TYPE, metaData1);
	}

	/**
	 * Constructs an instance of the union operator.
	 *
	 * @param resultSet1 the first input result set delivering the elements.
	 *        The result set is wrapped internally to a metadata cursor using
	 *        {@link ResultSetMetaDataCursor}.
	 * @param resultSet2 the second input result set delivering the elements.
	 *        The result set is wrapped internally to a metadata cursor using
	 *        {@link ResultSetMetaDataCursor}.
	 * @param createTuple a function that maps a (row of the) result set to a
	 *        tuple. The function gets a result set and maps the current row to
	 *        a tuple. If <code>null</code> is passed, the factory method of
	 *        array-tuple is used. It is forbidden to call the
	 *        <code>next</code>, <code>update</code> and similar methods of the
	 *        result set from inside the function!
	 */
	public Union(ResultSet resultSet1, ResultSet resultSet2, Function<? super ResultSet, ? extends Tuple> createTuple ) {
		this(new ResultSetMetaDataCursor(resultSet1, createTuple), new ResultSetMetaDataCursor(resultSet2, createTuple));
	}

	/**
	 * Returns the metadata information for this metadata-cursor as a composite
	 * metadata ({@link CompositeMetaData}).
	 *
	 * @return the metadata information for this metadata-cursor as a composite
	 *         metadata ({@link CompositeMetaData}).
	 */
	public CompositeMetaData<Object, Object> getMetaData() {
		return globalMetaData;
	}
	
	/**
	 * The main method contains some examples to demonstrate the usage and the
	 * functionality of this class.
	 * 
     * @param args the arguments to the main method.
	 */
	public static void main(String[] args) {

		/*********************************************************************/
		/*                            Example 1                              */
		/*********************************************************************/
		
		System.out.println("Example 1: Generating a sample of approximately 10% of size");
		
		CompositeMetaData<Object, Object> metadata = new CompositeMetaData<Object, Object>();
		metadata.add(ResultSetMetaDatas.RESULTSET_METADATA_TYPE, new xxl.core.relational.metaData.ColumnMetaDataResultSetMetaData(new xxl.core.relational.metaData.StoredColumnMetaData(false, false, true, false, ResultSetMetaData.columnNoNulls, true, 9, "", "", "", 9, 0, "", "", java.sql.Types.INTEGER, true, false, false)));
		
		MetaDataCursor<Tuple, CompositeMetaData<Object, Object>> cursor1 = xxl.core.cursors.Cursors.wrapToMetaDataCursor(xxl.core.relational.tuples.Tuples.mapObjectsToTuples(new xxl.core.cursors.sources.Enumerator(0, 10)), metadata);
		MetaDataCursor<Tuple, CompositeMetaData<Object, Object>> cursor2 = xxl.core.cursors.Cursors.wrapToMetaDataCursor(xxl.core.relational.tuples.Tuples.mapObjectsToTuples(new xxl.core.cursors.sources.Enumerator(10, 20)), metadata);

		MetaDataCursor<Tuple, CompositeMetaData<Object, Object>> cursor = new Union(cursor1, cursor2);
		
		cursor.open();
		
		xxl.core.cursors.Cursors.println(cursor);
		
		cursor.close();
	}

}