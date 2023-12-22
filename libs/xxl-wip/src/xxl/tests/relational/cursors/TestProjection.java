package xxl.tests.relational.cursors;

import xxl.core.cursors.MetaDataCursor;
import xxl.core.functions.AbstractFunction;
import xxl.core.relational.cursors.Projection;
import xxl.core.relational.metaData.ResultSetMetaDatas;
import xxl.core.relational.tuples.ArrayTuple;
import xxl.core.relational.tuples.Tuple;
import xxl.core.util.metaData.CompositeMetaData;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class Projection.
 */
public class TestProjection {

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
				new AbstractFunction<Integer, ArrayTuple>() {
					@Override
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
