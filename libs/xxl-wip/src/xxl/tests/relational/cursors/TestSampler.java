package xxl.tests.relational.cursors;

import xxl.core.cursors.MetaDataCursor;
import xxl.core.relational.cursors.Sampler;
import xxl.core.relational.metaData.ResultSetMetaDatas;
import xxl.core.relational.tuples.Tuple;
import xxl.core.util.metaData.CompositeMetaData;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class Sampler.
 */
public class TestSampler {

	/**
	 * The main method contains some examples to demonstrate the usage and the
	 * functionality of this class.
	 * 
     * @param args the arguments to the main method.
	 */
	public static void main(String[] args){

		/*********************************************************************/
		/*                            Example 1                              */
		/*********************************************************************/
		
		// Wraps an Enumerator cursor (integers 0 to 99)
		// to a MetaDataCursor using Cursors.wrapToMetaDataCursor(). 
		// Then, a sample of approximatly 10% of the original size is produced.

		System.out.println("Example 1: Generating a sample of approximately 10% of size");
		
		CompositeMetaData<Object, Object> metadata = new CompositeMetaData<Object, Object>();
		metadata.add(ResultSetMetaDatas.RESULTSET_METADATA_TYPE, new xxl.core.relational.metaData.ColumnMetaDataResultSetMetaData(new xxl.core.relational.metaData.StoredColumnMetaData(false, false, true, false, java.sql.ResultSetMetaData.columnNoNulls, true, 9, "", "", "", 9, 0, "", "", java.sql.Types.INTEGER, true, false, false)));
		
		MetaDataCursor<Tuple, CompositeMetaData<Object, Object>> cursor = xxl.core.cursors.Cursors.wrapToMetaDataCursor(xxl.core.relational.tuples.Tuples.mapObjectsToTuples(new xxl.core.cursors.sources.Enumerator(0,100)), metadata);
		
		cursor = new Sampler(cursor, 0.1);
		
		cursor.open();
		
		while (cursor.hasNext())
			System.out.println(cursor.next());
		
		cursor.close();
	}

}
