package xxl.tests.relational.cursors;

import java.sql.ResultSetMetaData;
import java.sql.Types;

import xxl.core.cursors.Cursor;
import xxl.core.cursors.MetaDataCursor;
import xxl.core.predicates.AbstractPredicate;
import xxl.core.relational.cursors.SortBasedGrouper;
import xxl.core.relational.metaData.ColumnMetaDataResultSetMetaData;
import xxl.core.relational.metaData.StoredColumnMetaData;
import xxl.core.relational.tuples.Tuple;
import xxl.core.relational.tuples.Tuples;
import xxl.core.util.metaData.CompositeMetaData;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class SortBasedGrouper.
 */
public class TestSortBasedGrouper {

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
			new AbstractPredicate<Tuple>() {
				@Override
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
