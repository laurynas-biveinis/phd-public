package xxl.tests.relational.cursors;

import xxl.core.collections.bags.Bag;
import xxl.core.cursors.MetaDataCursor;
import xxl.core.functions.AbstractFunction;
import xxl.core.relational.cursors.NestedLoopsGrouper;
import xxl.core.relational.metaData.ResultSetMetaDatas;
import xxl.core.relational.tuples.Tuple;
import xxl.core.util.metaData.CompositeMetaData;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class NestedLoopsGrouper.
 */
public class TestNestedLoopsGrouper {
	
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
			new AbstractFunction<Tuple, Object>() {
				@Override
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
