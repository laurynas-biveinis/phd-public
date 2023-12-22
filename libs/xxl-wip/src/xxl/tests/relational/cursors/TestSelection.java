package xxl.tests.relational.cursors;

import xxl.core.cursors.MetaDataCursor;
import xxl.core.predicates.AbstractPredicate;
import xxl.core.relational.cursors.Selection;
import xxl.core.relational.metaData.ResultSetMetaDatas;
import xxl.core.relational.tuples.Tuple;
import xxl.core.util.metaData.CompositeMetaData;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class Selection.
 */
public class TestSelection {
	
	/**
	 * The main method contains some examples to demonstrate the usage and the
	 * functionality of this class.
	 * 
     * @param args the arguments to the main method.
	 */
	public static void main(String[] args) {

		// ********************************************************************
		// *                            Example 1                             *
		// ********************************************************************

		System.out.println("Example 1: Performing a selection >=100 and <=200 on random integers (100 integers up to 1000)");
		
		CompositeMetaData<Object, Object> metadata = new CompositeMetaData<Object, Object>();
		metadata.add(ResultSetMetaDatas.RESULTSET_METADATA_TYPE, new xxl.core.relational.metaData.ColumnMetaDataResultSetMetaData(new xxl.core.relational.metaData.StoredColumnMetaData(false, false, true, false, java.sql.ResultSetMetaData.columnNoNulls, true, 9, "", "", "", 9, 0, "", "", java.sql.Types.INTEGER, true, false, false)));
		
		MetaDataCursor<Tuple, CompositeMetaData<Object, Object>> cursor = xxl.core.cursors.Cursors.wrapToMetaDataCursor(xxl.core.relational.tuples.Tuples.mapObjectsToTuples(new xxl.core.cursors.sources.DiscreteRandomNumber(new xxl.core.util.random.JavaDiscreteRandomWrapper(1000),100)), metadata);
		
		cursor = new Selection(
			cursor,
			new AbstractPredicate<Tuple>() {
				@Override
				public boolean invoke(Tuple tuple) {
					int value = tuple.getInt(1);
					return (value>=100) && (value<=200);
				}
			}
		);
		
		cursor.open();
		
		xxl.core.cursors.Cursors.println(cursor);
		
		cursor.close();
	}

}
