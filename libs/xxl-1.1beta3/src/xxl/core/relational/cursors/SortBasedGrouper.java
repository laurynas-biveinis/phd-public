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
import java.sql.Types;

import xxl.core.cursors.Cursor;
import xxl.core.cursors.MetaDataCursor;
import xxl.core.functions.Function;
import xxl.core.predicates.ComparatorBasedEqual;
import xxl.core.predicates.Not;
import xxl.core.predicates.Predicate;
import xxl.core.relational.metaData.ColumnMetaDataResultSetMetaData;
import xxl.core.relational.metaData.ResultSetMetaDatas;
import xxl.core.relational.metaData.StoredColumnMetaData;
import xxl.core.relational.tuples.Tuple;
import xxl.core.relational.tuples.Tuples;
import xxl.core.util.metaData.CompositeMetaData;

/**
 * The sort-based grouper is an implementation of the group operator. It is
 * based on {@link xxl.core.cursors.groupers.SortBasedGrouper}. The input
 * relation has to be sorted so that all elements belonging to the same group
 * have to be in sequence. Then, a group is defined by a predicate that returns
 * false at the end of such a sequence.
 * 
 * <p>A call to the <code>next</code> method returns a group (cursor)
 * containing all elements of a group.</p>
 * 
 * <p>Usually, a
 * {@link xxl.core.relational.cursors.GroupAggregator group-aggregator} is
 * applied on the output of a grouper.</p>
 */
public class SortBasedGrouper extends xxl.core.cursors.groupers.SortBasedGrouper<Tuple> implements MetaDataCursor<Cursor<Tuple>, CompositeMetaData<Object, Object>> {

	/**
	 * The metadata provided by the sort-based grouper.
	 */
	protected CompositeMetaData<Object, Object> globalMetaData;

	/**
	 * Constructs an instance of the sort-based grouper.
	 *
	 * @param sortedCursor the sorted metadata cursor containing elements.
	 * @param predicate the predicate that determines the borders of the groups.
	 */
	public SortBasedGrouper(MetaDataCursor<Tuple, CompositeMetaData<Object, Object>> sortedCursor, Predicate<? super Tuple> predicate) {
		super(sortedCursor, predicate);
		
		globalMetaData = new CompositeMetaData<Object, Object>();
		globalMetaData.add(ResultSetMetaDatas.RESULTSET_METADATA_TYPE, ResultSetMetaDatas.getResultSetMetaData(sortedCursor));
	}

	/**
	 * Constructs an instance of the sort-based grouper.
	 *
	 * @param sortedCursor the sorted metadata cursor containing elements.
	 * @param columns if the values in the passed column numbers differ, a new
	 *        group is created. The first column is 1, the second is 2, ...
	 */
	public SortBasedGrouper(MetaDataCursor<Tuple, CompositeMetaData<Object, Object>> sortedCursor, int[] columns) {
		this(
			sortedCursor,
			new Not<Tuple>(
				new ComparatorBasedEqual<Tuple>(
					Tuples.getTupleComparator(columns)
				)
			)
		);
	}

	/**
	 * Constructs an instance of the sort-based grouper.
	 *
	 * @param sortedResultSet the sorted result set containing elements.
	 * @param createTuple a function that maps a (row of the) result set to a
	 *        tuple. The function gets a result set and maps the current row to
	 *        a tuple. If <code>null</code> is passed, the factory method of
	 *        array-tuple is used. It is forbidden to call the
	 *        <code>next</code>, <code>update</code> and similar methods of the
	 *        result set from inside the function!
	 * @param predicate the predicate that determines the borders of the
	 *        groups.
	 */
	public SortBasedGrouper(ResultSet sortedResultSet, Function<? super ResultSet, ? extends Tuple> createTuple, Predicate<? super Tuple> predicate) {
		this(
			createTuple == null ?
				new ResultSetMetaDataCursor(sortedResultSet) :
				new ResultSetMetaDataCursor(sortedResultSet, createTuple),
			predicate
		);
	}

	/**
	 * Constructs an instance of the sort-based grouper.
	 *
	 * @param sortedResultSet the sorted result set containing elements.
	 * @param createTuple a function that maps a (row of the) result set to a
	 *        tuple. The function gets a result set and maps the current row to
	 *        a tuple. If <code>null</code> is passed, the factory method of
	 *        array-tuple is used. It is forbidden to call the
	 *        <code>next</code>, <code>update</code> and similar methods of the
	 *        result set from inside the function!
	 * @param columns if the values in the passed column numbers differ, a new
	 *        group is created. The first column is 1, the second is 2, ...
	 */
	public SortBasedGrouper(ResultSet sortedResultSet, Function<? super ResultSet, ? extends Tuple> createTuple, int[] columns) {
		this(
			sortedResultSet,
			createTuple,
			new Not<Tuple>(
					new ComparatorBasedEqual<Tuple>(
						Tuples.getTupleComparator(columns)
					)
				)
			);
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
     * @param args the arguments.
	 */
	public static void main(String[] args) {

		// ********************************************************************
		// *                           Example 1                              *
		// ********************************************************************
		
		// Wraps an Enumerator cursor (integers 0 to 9) to a MetaDataCursor
		// using Cursors.wrapToMetaDataCursor. A SortBasedGrouper is used to
		// group the objects according to their first digit. Then, the first
		// group is sent to System.out.
		System.out.println("Example 1: Grouping 00, 01, ..., 99 after the first digit");
		
		CompositeMetaData<Object, Object> globalMetaData = new CompositeMetaData<Object, Object>();
		globalMetaData.add(
			xxl.core.relational.metaData.ResultSetMetaDatas.RESULTSET_METADATA_TYPE,
			new ColumnMetaDataResultSetMetaData(new StoredColumnMetaData(false, false, true, false, ResultSetMetaData.columnNoNulls, true, 9, "", "", "", 9, 0, "", "", Types.INTEGER, true, false, false))
		);
		MetaDataCursor<Tuple, CompositeMetaData<Object, Object>> cursor = xxl.core.cursors.Cursors.wrapToMetaDataCursor(
			Tuples.mapObjectsToTuples(
				new xxl.core.cursors.sources.Enumerator(0,100)
			),
			globalMetaData
		);
		
		SortBasedGrouper grouper = new SortBasedGrouper(
			cursor,
			new Predicate<Tuple>() {
				public boolean invoke(Tuple previous, Tuple next) {
					return previous.getInt(1) / 10 != next.getInt(1) / 10;
				}
			}
		);
		
		System.out.println("Printing the elements of the first group that is returned.");
		Cursor<Tuple> firstGroup = null;
		if (grouper.hasNext()) {
			firstGroup = grouper.next();
			xxl.core.cursors.Cursors.println(firstGroup);
		}
		else
			throw new RuntimeException("Error in SortBasedGrouper (first group)!!!");
	
		System.out.println("Accessing two more groups");
		Cursor<Tuple> secondGroup = grouper.next();
		Cursor<Tuple> thirdGroup = grouper.next();
		
		if (secondGroup.hasNext())
			throw new RuntimeException("Error in SortBasedGrouper (could access erlier groups)");
	
		System.out.println("Testing a group 3");
		if (thirdGroup.hasNext()) {
			Tuple t = thirdGroup.next();
			if (t.getInt(1) / 10 != 2) 
				throw new RuntimeException("Error in SortBasedGrouper (object of third cursor does not belong to this group)!!!");
		}
		
		System.out.println("Counting the groups...");
		int groupsLeft = xxl.core.cursors.Cursors.count(grouper);
		System.out.println("Groups left (7 is ok): " + groupsLeft);
		if (groupsLeft != 7)
			throw new RuntimeException("Error in SortBasedGrouper (number of groups)!!!");
			
	}
}
