package xxl.tests.relational.cursors;

import xxl.core.cursors.MetaDataCursor;
import xxl.core.relational.cursors.MergeSorter;
import xxl.core.relational.metaData.ResultSetMetaDatas;
import xxl.core.relational.tuples.Tuple;
import xxl.core.relational.tuples.Tuples;
import xxl.core.util.metaData.CompositeMetaData;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class MergeSorter.
 */
public class TestMergeSorter {

	/**
	 * The main method contains some examples to demonstrate the usage and the
	 * functionality of this class.
	 * 
	 * @param args the arguments to the use case.
	 */
	public static void main(String[] args) {

		/*********************************************************************/
		/*                            Example 1                              */
		/*********************************************************************/
		
		// Wraps a RandomIntegers cursor (100 integers up to 1000)
		// to a MetaDataCursor using Cursors.wrapToMetaDataCursor().
		// Then, the cursor becomes sorted.
		
		System.out.println("Example 1: Sorting randomly generated Integers");
		
		CompositeMetaData<Object, Object> metadata = new CompositeMetaData<Object, Object>();
		metadata.add(ResultSetMetaDatas.RESULTSET_METADATA_TYPE, new xxl.core.relational.metaData.ColumnMetaDataResultSetMetaData(new xxl.core.relational.metaData.StoredColumnMetaData(false, false, true, false, java.sql.ResultSetMetaData.columnNoNulls, true, 9, "", "", "", 9, 0, "", "", xxl.core.relational.Types.INTEGER, true, false, false)));
		
		MetaDataCursor<Tuple, CompositeMetaData<Object, Object>> cursor = xxl.core.cursors.Cursors.wrapToMetaDataCursor(Tuples.mapObjectsToTuples(new xxl.core.cursors.sources.DiscreteRandomNumber(new xxl.core.util.random.JavaDiscreteRandomWrapper(1000),100)), metadata);
		
		cursor = new MergeSorter(cursor, new int[] {1}, new boolean[] {true});
		
		xxl.core.cursors.Cursors.println(cursor);
	}

}
