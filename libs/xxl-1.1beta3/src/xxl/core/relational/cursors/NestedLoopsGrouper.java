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
import java.util.Map;

import xxl.core.collections.bags.Bag;
import xxl.core.collections.queues.Queue;
import xxl.core.cursors.Cursor;
import xxl.core.cursors.MetaDataCursor;
import xxl.core.functions.Function;
import xxl.core.relational.metaData.ResultSetMetaDatas;
import xxl.core.relational.tuples.Tuple;
import xxl.core.util.metaData.CompositeMetaData;

/**
 * A nested-loops grouper is an implementation of the group operator. It is
 * based on {@link xxl.core.cursors.groupers.NestedLoopsGrouper} and
 * additionally forwards the metadata.
 * 
 * <p>A call to the <code>next</code> method returns a cursor containing all
 * elements of a group.</p>
 * 
 * <p>Usually, an {@link xxl.core.relational.cursors.Aggregator} is applied on
 * the output of a grouper.</p>
 */
public class NestedLoopsGrouper extends xxl.core.cursors.groupers.NestedLoopsGrouper<Tuple> implements MetaDataCursor<Cursor<Tuple>, CompositeMetaData<Object, Object>> {

	/**
	 * An internal variable used for storing the metadata information of this
	 * group operator.
	 */
	protected CompositeMetaData<Object, Object> globalMetaData;

	/**
	 * Constructs an instance of the nested-loops grouper operator. Determines
	 * the maximum number of keys that can be stored in the main memory map
	 * <pre>
	 *   ((memSize - objectSize) / keySize) - 1
	 * </pre>
	 * This formula is based on the assumption that only the keys, i.e., the
	 * map, is stored in main memory whereas the bags storing the input
	 * cursor's elements are located in external memory.
	 *
	 * @param cursor the metadata cursor containing input elements.
	 * @param mapping an unary mapping function returning a key to a given
	 *        value.
	 * @param map the map which is used for storing the keys in main memory.
	 * @param memSize the maximum amount of available main memory (bytes) for
	 *        the map.
	 * @param objectSize the size (bytes) needed to store one element.
	 * @param keySize the size (bytes) a key needs in main memory.
	 * @param newBag a parameterless function returning an empty bag.
	 * @param newQueue a parameterless function returning an empty queue.
	 * @throws IllegalArgumentException if not enough main memory is available.
	 */
	public NestedLoopsGrouper(MetaDataCursor<? extends Tuple, CompositeMetaData<Object, Object>> cursor, Function<? super Tuple, ? extends Object> mapping, Map<Object, Bag<Tuple>> map, int memSize, int objectSize, int keySize, Function<?, ? extends Bag<Tuple>> newBag, Function<?, ? extends Queue<Tuple>> newQueue) {
		super(cursor, mapping, map, memSize, objectSize, keySize, newBag, newQueue);
		
		globalMetaData = new CompositeMetaData<Object, Object>();
		globalMetaData.add(ResultSetMetaDatas.RESULTSET_METADATA_TYPE, ResultSetMetaDatas.getResultSetMetaData(cursor));
	}

	/**
	 * Constructs an instance of the nested-loops grouper operator. Determines
	 * the maximum number of keys that can be stored in the main memory map
	 * <pre>
	 *   ((memSize - objectSize) / keySize) - 1
	 * </pre>
	 * This formula is based on the assumption that only the keys, i.e., the
	 * map, is stored in main memory whereas the bags storing the input
	 * cursor's elements are located in external memory. Uses default factory
	 * methods for list-bags and array-queues.
	 *
	 * @param cursor the metadata cursor containing input elements.
	 * @param mapping an unary mapping function returning a key to a given
	 *        value.
	 * @param map the map which is used for storing the keys in main memory.
	 * @param memSize the maximum amount of available main memory (bytes) for
	 *        the map.
	 * @param objectSize the size (bytes) needed to store one element.
	 * @param keySize the size (bytes) a key needs in main memory.
	 * @throws IllegalArgumentException if not enough main memory is available.
	 */
	public NestedLoopsGrouper(MetaDataCursor<? extends Tuple, CompositeMetaData<Object, Object>> cursor, Function<? super Tuple, ? extends Object> mapping, Map<Object, Bag<Tuple>> map, int memSize, int objectSize, int keySize) {
		super(cursor, mapping, map, memSize, objectSize, keySize);
		
		globalMetaData = new CompositeMetaData<Object, Object>();
		globalMetaData.add(ResultSetMetaDatas.RESULTSET_METADATA_TYPE, ResultSetMetaDatas.getResultSetMetaData(cursor));
	}

	/**
	 * Constructs an instance of the nested-loops grouper operator. Determines
	 * the maximum number of keys that can be stored in the main memory map
	 * <pre>
	 *   ((memSize - objectSize) / keySize) - 1
	 * </pre>
	 * This formula is based on the assumption that only the keys, i.e., the
	 * map, is stored in main memory whereas the bags storing the input
	 * result set's elements are located in external memory.
	 *
	 * @param resultSet the result set containing input elements.
	 * @param mapping an unary mapping function returning a key to a given
	 *        value.
	 * @param map the map which is used for storing the keys in main memory.
	 * @param memSize the maximum amount of available main memory (bytes) for
	 *        the map.
	 * @param objectSize the size (bytes) needed to store one element.
	 * @param keySize the size (bytes) a key needs in main memory.
	 * @param newBag a parameterless function returning an empty bag.
	 * @param newQueue a parameterless function returning an empty queue.
	 * @throws IllegalArgumentException if not enough main memory is available.
	 */
	public NestedLoopsGrouper(ResultSet resultSet, Function<? super Tuple, ? extends Object> mapping, Map<Object, Bag<Tuple>> map, int memSize, int objectSize, int keySize, Function<?, ? extends Bag<Tuple>> newBag, Function<?, ? extends Queue<Tuple>> newQueue) {
		this(new ResultSetMetaDataCursor(resultSet), mapping, map, memSize, objectSize, keySize, newBag, newQueue);
	}

	/**
	 * Constructs an instance of the nested-loops grouper operator. Determines
	 * the maximum number of keys that can be stored in the main memory map
	 * <pre>
	 *   ((memSize - objectSize) / keySize) - 1
	 * </pre>
	 * This formula is based on the assumption that only the keys, i.e., the
	 * map, is stored in main memory whereas the bags storing the input
	 * result set's elements are located in external memory. Uses default
	 * factory methods for list-bags and array-queues.
	 *
	 * @param resultSet the result set containing input elements.
	 * @param mapping an unary mapping function returning a key to a given
	 *        value.
	 * @param map the map which is used for storing the keys in main memory.
	 * @param memSize the maximum amount of available main memory (bytes) for
	 *        the map.
	 * @param objectSize the size (bytes) needed to store one element.
	 * @param keySize the size (bytes) a key needs in main memory.
	 * @throws IllegalArgumentException if not enough main memory is available.
	 */
	public NestedLoopsGrouper(ResultSet resultSet, Function<? super Tuple, ? extends Object> mapping, Map<Object, Bag<Tuple>> map, int memSize, int objectSize, int keySize) {
		this(new ResultSetMetaDataCursor(resultSet), mapping, map, memSize, objectSize, keySize);
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
	 * @param args the arguments the the use case.
	 */
	public static void main(String[] args){

		/*********************************************************************/
		/*                            Example 1                              */
		/*********************************************************************/
		
		// Wraps a Enumerator cursor (integers 0 to 99)
		// to a MetaDataCursor using Cursors.wrapToMetaDataCursor().
		// A NestedLoopsGrouper is used to group the objects according
		// to their last digit. Then, the first group is sent to System.out.
		
		System.out.println("Example 1: Grouping sorted Integers");
		
		CompositeMetaData<Object, Object> metadata = new CompositeMetaData<Object, Object>();
		metadata.add(ResultSetMetaDatas.RESULTSET_METADATA_TYPE, new xxl.core.relational.metaData.ColumnMetaDataResultSetMetaData(new xxl.core.relational.metaData.StoredColumnMetaData(false, false, true, false, java.sql.ResultSetMetaData.columnNoNulls, true, 9, "", "", "", 9, 0, "", "", xxl.core.relational.Types.INTEGER, true, false, false)));
		
		MetaDataCursor<Tuple, CompositeMetaData<Object, Object>> cursor = xxl.core.cursors.Cursors.wrapToMetaDataCursor(xxl.core.relational.tuples.Tuples.mapObjectsToTuples(new xxl.core.cursors.sources.Enumerator(0,100)), metadata);

		NestedLoopsGrouper grouper = new NestedLoopsGrouper(
			cursor,
			new Function<Tuple, Object>() {
				public Object invoke(Tuple tuple) {
					return new Integer(tuple.getInt(1) % 10);
				}
			},
			new java.util.HashMap<Object, Bag<Tuple>>(),
			10000,
			4,
			4
		);
		
		System.out.println("Printing the elements of the first group that is returned.");
		if (grouper.hasNext())
			xxl.core.cursors.Cursors.println(grouper.next());
		else
			throw new RuntimeException("Error in NestedLoopsGrouper (first group)!!!");
		
		int groupsLeft = xxl.core.cursors.Cursors.count(grouper);
		System.out.println("Groups left (9 is ok): " + groupsLeft);
		if (groupsLeft != 9)
			throw new RuntimeException("Error in NestedLoopsGrouper (number of groups)!!!");		
	}
}
