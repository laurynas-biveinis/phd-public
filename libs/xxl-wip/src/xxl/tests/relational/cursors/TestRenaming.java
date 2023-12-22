package xxl.tests.relational.cursors;

import xxl.core.cursors.MetaDataCursor;
import xxl.core.relational.cursors.Renaming;
import xxl.core.relational.metaData.ResultSetMetaDatas;
import xxl.core.relational.tuples.Tuple;
import xxl.core.util.metaData.CompositeMetaData;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class Renaming.
 */
public class TestRenaming {

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

		System.out.println("Example 1: Performing a renaming of a column");
		
		CompositeMetaData<Object, Object> metadata = new CompositeMetaData<Object, Object>();
		metadata.add(ResultSetMetaDatas.RESULTSET_METADATA_TYPE, new xxl.core.relational.metaData.ColumnMetaDataResultSetMetaData(new xxl.core.relational.metaData.StoredColumnMetaData(false, false, true, false, java.sql.ResultSetMetaData.columnNoNulls, true, 9, "", "", "", 9, 0, "", "", java.sql.Types.INTEGER, true, false, false)));
		
		MetaDataCursor<Tuple, CompositeMetaData<Object, Object>> cursor = xxl.core.cursors.Cursors.wrapToMetaDataCursor(xxl.core.relational.tuples.Tuples.mapObjectsToTuples(new xxl.core.cursors.sources.Enumerator(0,10)), metadata);
		
		cursor = new Renaming(
			cursor,
			"NewName"
		);
		
		cursor.open();
		
		xxl.core.relational.resultSets.ResultSets.writeToPrintStream(cursor, System.out, true, "\t");
		
		cursor.close();
	}

}
