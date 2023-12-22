package xxl.tests.relational.cursors;

import java.sql.ResultSetMetaData;

import xxl.core.cursors.MetaDataCursor;
import xxl.core.relational.cursors.Union;
import xxl.core.relational.metaData.ResultSetMetaDatas;
import xxl.core.relational.tuples.Tuple;
import xxl.core.util.metaData.CompositeMetaData;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class Union.
 */
public class TestUnion {
	
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
