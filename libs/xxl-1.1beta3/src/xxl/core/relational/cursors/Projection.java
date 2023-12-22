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
import java.util.Arrays;
import java.util.NoSuchElementException;

import xxl.core.cursors.MetaDataCursor;
import xxl.core.cursors.SecureDecoratorCursor;
import xxl.core.functions.Function;
import xxl.core.relational.metaData.ProjectedResultSetMetaData;
import xxl.core.relational.metaData.ResultSetMetaDatas;
import xxl.core.relational.tuples.ArrayTuple;
import xxl.core.relational.tuples.Tuple;
import xxl.core.util.metaData.CompositeMetaData;

/**
 * Straight forward implementation of the operator projection.
 * 
 * <p>In earlier versions of XXL it was possible to hand over a string array to
 * the constructor of the operators instead of an array of indices. To get this
 * functionality, use
 * {@link xxl.core.relational.resultSets.ResultSets#getColumnIndices(ResultSet, String[])}.</p>
 * 
 * <p>The example in the main method wraps an iteration containing arrays of
 * five <code>int</code> values <tt>[i,&nbsp;i+1,&nbsp;i+2,&nbsp;i+3,&nbsp;i+4]
 * to a metadata cursor using
 * {@link xxl.core.cursors.Cursors#wrapToMetaDataCursor(java.util.Iterator, Object)}.
 * Then, the column becomes projected and the cursor is printed on
 * System.out. The interesting call is: 
 * <code><pre>
 *   cursor = new Projection(
 *       cursor,
 *       ArrayTuple.FACTORY_METHOD,
 *       1,
 *       3,
 *       5
 *   );
 * </pre></code>
 */
public class Projection extends SecureDecoratorCursor<Tuple> implements MetaDataCursor<Tuple, CompositeMetaData<Object, Object>> {

	/**
	 * A function used for projecting the tuples (their columns).
	 */
	protected xxl.core.functions.Projection<Object> projection;
	
	/**
	 * A function that maps a list of objects (column values) to a tuple.
	 */
	protected Function<Object, ? extends Tuple> tupleFactory;
	
	/**
	 * The metadata provided by the projection.
	 */
	protected CompositeMetaData<Object, Object> globalMetaData;

	
	/**
	 * Creates a new instance of projection. The projection of the tuples
	 * derived from the underlying cursor is done by the given
	 * projection-function.
	 *
	 * @param cursor the input metadata cursor delivering the input elements.
	 * @param tupleFactory a function that maps a list of objects (column
	 *        values) to a new tuple. Classes implementing the Tuple interface
	 *        should provide factory methods for this task. If
	 *        <code>null</code> is passed, a factory method producing
	 *        {@link ArrayTuple array-tuples} is used.
	 * @param projection a function that maps an object array to a projected
	 *        object array.
	 */
	public Projection(MetaDataCursor<Tuple, CompositeMetaData<Object, Object>> cursor, Function<Object, ? extends Tuple> tupleFactory, xxl.core.functions.Projection<Object> projection) {
		super(cursor);
		
		globalMetaData = new CompositeMetaData<Object, Object>();
		globalMetaData.add(
			ResultSetMetaDatas.RESULTSET_METADATA_TYPE,
			new ProjectedResultSetMetaData(
				ResultSetMetaDatas.getResultSetMetaData(cursor),
				xxl.core.util.Arrays.incrementIntArray(projection.getIndices(), 1)
			)
		);
		
		this.projection = projection;
		this.tupleFactory = tupleFactory == null ? ArrayTuple.FACTORY_METHOD : tupleFactory;
	}
	
	/**
	 * Creates a new instance of projection. The tuples derived from the
	 * underlying metadata cursor are projected onto the specified columns. 
	 *
	 * @param cursor the input metadata cursor delivering the input elements.
	 * @param tupleFactory a function that maps a list of objects (column
	 *        values) to a new tuple. Classes implementing the Tuple interface
	 *        should provide factory methods for this task. If
	 *        <code>null</code> is passed, a factory method producing
	 *        {@link ArrayTuple array-tuples} is used.
	 * @param columns an array of column numbers the tuples are projected onto.
	 */
	public Projection(MetaDataCursor<Tuple, CompositeMetaData<Object, Object>> cursor, Function<Object, ? extends Tuple> tupleFactory, int... columns) {
		this(cursor, tupleFactory, new xxl.core.functions.Projection<Object>(xxl.core.util.Arrays.decrementIntArray(columns, 1)));
	}

	/**
	 * Creates a new instance of projection. The projection of the tuples
	 * derived from the underlying result set is done by the given
	 * projection-function.
	 *
	 * @param resultSet the input result set delivering the input elements.
	 * @param tupleFactory a function that maps a list of objects (column
	 *        values) to a new tuple. Classes implementing the Tuple interface
	 *        should provide factory methods for this task. If
	 *        <code>null</code> is passed, a factory method producing
	 *        {@link ArrayTuple array-tuples} is used.
	 * @param projection a function that maps an object array to a projected
	 *        object array.
	 */
	public Projection(ResultSet resultSet, Function<Object, ? extends Tuple> tupleFactory, xxl.core.functions.Projection<Object> projection) {
		this(new ResultSetMetaDataCursor(resultSet), tupleFactory, projection);
	}

	/**
	 * Creates a new instance of projection. The tuples derived from the
	 * underlying result set are projected onto the specified columns. 
	 *
	 * @param resultSet the input result set delivering the input elements.
	 * @param tupleFactory a function that maps a list of objects (column
	 *        values) to a new tuple. Classes implementing the Tuple interface
	 *        should provide factory methods for this task. If
	 *        <code>null</code> is passed, a factory method producing
	 *        {@link ArrayTuple array-tuples} is used.
	 * @param columns an array of column numbers the tuples are projected
	 *        onto.
	 */
	public Projection(ResultSet resultSet, Function<Object, ? extends Tuple> tupleFactory, int... columns) {
		this(new ResultSetMetaDataCursor(resultSet), tupleFactory, columns);
	}

	/**
	 * Shows the next element in the iteration without proceeding the iteration
	 * (optional operation). After calling <tt>peek</tt> the returned element is
	 * still the cursor's next one such that a call to <tt>next</tt> would be
	 * the only way to proceed the iteration. But be aware that an
	 * implementation of this method uses a kind of buffer-strategy, therefore
	 * it is possible that the returned element will be removed from the
	 * <i>underlying</i> iteration, e.g., the caller can use an instance of a
	 * cursor depending on an iterator, so the next element returned by a call
	 * to <tt>peek</tt> will be removed from the underlying iterator which does
	 * not support the <tt>peek</tt> operation and therefore the iterator has to
	 * be wrapped and buffered.
	 * 
	 * <p>Note, that this operation is optional and might not work for all
	 * cursors. After calling the <tt>peek</tt> method a call to <tt>next</tt>
	 * is strongly recommended.</p> 
	 *
	 * @return the next element in the iteration.
	 * @throws IllegalStateException if the cursor is already closed when this
	 *         method is called.
	 * @throws NoSuchElementException iteration has no more elements.
	 * @throws UnsupportedOperationException if the <tt>peek</tt> operation is
	 *         not supported by the cursor.
	 */
	public Tuple peek() throws IllegalStateException, NoSuchElementException, UnsupportedOperationException {
		return tupleFactory.invoke(Arrays.asList(projection.invoke(super.peek().toArray())));
	}

	/**
	 * Returns the next element in the iteration. This element will be
	 * accessible by some of the cursor's methods, e.g., <tt>update</tt> or
	 * <tt>remove</tt>, until a call to <tt>next</tt> or <tt>peek</tt> occurs.
	 * This is calling <tt>next</tt> or <tt>peek</tt> proceeds the iteration and
	 * therefore its previous element will not be accessible any more.
	 *
	 * @return the next element in the iteration.
	 * @throws IllegalStateException if the cursor is already closed when this
	 *         method is called.
	 * @throws NoSuchElementException if the iteration has no more elements.
	 */
	public Tuple next() throws IllegalStateException, NoSuchElementException {
		return tupleFactory.invoke(Arrays.asList(projection.invoke(super.next().toArray())));
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
     * @throws Exception if an error occurs.
	 */
	public static void main(String[] args) throws Exception {

		// ********************************************************************
		// *                            Example 1                             *
		// ********************************************************************

		System.out.println("Example 1: Performing a projection of three columns");
		
		CompositeMetaData<Object, Object> metadata = new CompositeMetaData<Object, Object>();
		metadata.add(
			ResultSetMetaDatas.RESULTSET_METADATA_TYPE,
			new xxl.core.relational.metaData.ColumnMetaDataResultSetMetaData(
				new xxl.core.relational.metaData.StoredColumnMetaData(false, false, true, false, java.sql.ResultSetMetaData.columnNoNulls, true, 9, "", "FIRST", "", 9, 0, "", "", java.sql.Types.INTEGER, true, false, false),
				new xxl.core.relational.metaData.StoredColumnMetaData(false, false, true, false, java.sql.ResultSetMetaData.columnNoNulls, true, 9, "", "SECOND", "", 9, 0, "", "", java.sql.Types.INTEGER, true, false, false),
				new xxl.core.relational.metaData.StoredColumnMetaData(false, false, true, false, java.sql.ResultSetMetaData.columnNoNulls, true, 9, "", "THIRD", "", 9, 0, "", "", java.sql.Types.INTEGER, true, false, false),
				new xxl.core.relational.metaData.StoredColumnMetaData(false, false, true, false, java.sql.ResultSetMetaData.columnNoNulls, true, 9, "", "FOURTH", "", 9, 0, "", "", java.sql.Types.INTEGER, true, false, false),
				new xxl.core.relational.metaData.StoredColumnMetaData(false, false, true, false, java.sql.ResultSetMetaData.columnNoNulls, true, 9, "", "FIFTH", "", 9, 0, "", "", java.sql.Types.INTEGER, true, false, false)
			)
		);
		
		MetaDataCursor<Tuple, CompositeMetaData<Object, Object>> cursor = xxl.core.cursors.Cursors.wrapToMetaDataCursor(
			new xxl.core.cursors.mappers.Mapper<Integer, Tuple>(
				new Function<Integer, ArrayTuple>() {
					public ArrayTuple invoke(Integer integer) {
						return new ArrayTuple(integer, integer+1, integer+2, integer+3, integer+4);
					}
				},
				new xxl.core.cursors.sources.Enumerator(0, 10)
			),
			metadata
		);
		
		cursor = new Projection(
			cursor,
			ArrayTuple.FACTORY_METHOD,
			1,
			3,
			5
		);
		
		cursor.open();
		
		xxl.core.relational.resultSets.ResultSets.writeToPrintStream(cursor, System.out, true, "\t");
		
		cursor.close();
	}
}
