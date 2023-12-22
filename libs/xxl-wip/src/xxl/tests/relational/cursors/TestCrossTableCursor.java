package xxl.tests.relational.cursors;

import java.sql.ResultSetMetaData;
import java.sql.Types;

import xxl.core.cursors.MetaDataCursor;
import xxl.core.functions.AbstractMetaDataFunction;
import xxl.core.math.functions.AggregationFunction;
import xxl.core.relational.cursors.CrossTableCursor;
import xxl.core.relational.cursors.InputStreamMetaDataCursor;
import xxl.core.relational.metaData.ColumnMetaDataResultSetMetaData;
import xxl.core.relational.metaData.ProjectedResultSetMetaData;
import xxl.core.relational.metaData.ResultSetMetaDatas;
import xxl.core.relational.metaData.StoredColumnMetaData;
import xxl.core.relational.tuples.Tuple;
import xxl.core.util.metaData.CompositeMetaData;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class CrossTableCursor.
 */
public class TestCrossTableCursor {

	/**
	 * The main method of this class showing a default use case.
	 * 
	 * @param args the arguments to the use case. The arguments are optional
	 *        and specify the name of the file containing the data, the name of
	 *        the column defining the first dimension, the name of the column
	 *        defining the second dimension, the name of the column tuples are
	 *        reduced to before storing them into the internally used data
	 *        structure, a boolean value specified whether the stored objects
	 *        should be reduced to an average value or to the last object seen,
	 *        a boolean value specifing whether the cross table should be
	 *        printed to <code>System.out</code> or to a file, that's name is
	 *        specified thereafter. 
	 * @throws Exception if an exception occurs.
	 */
	public static void main(String args[]) throws Exception {
		MetaDataCursor<Tuple, CompositeMetaData<Object, Object>> cursor = args.length < 1 ?
			new InputStreamMetaDataCursor(new java.net.URL("http://dbs.mathematik.uni-marburg.de/downloads/data/FileMDCtest.txt")) :
			new InputStreamMetaDataCursor(args[0]);
		final ResultSetMetaData resultSetMetaData = ResultSetMetaDatas.getResultSetMetaData(cursor);
		
		String firstDimensionName = args.length < 1 ? "name" : args[1];
		String secondDimensionName = args.length < 1 ? "first name" : args[2];
		String thirdDimensionName = args.length < 1 ? "year of birth" : args[3];
		boolean computeAverage = args.length < 5 ? true : Boolean.parseBoolean(args[4]);
		boolean writeToSystemOut = args.length < 7 ? true : Boolean.parseBoolean(args[5]);
		
		final int resultColumnIndex = ResultSetMetaDatas.getColumnIndex(resultSetMetaData, thirdDimensionName);

		if (computeAverage)
			cursor = new CrossTableCursor<xxl.core.collections.bags.Bag<Object>>(
				cursor,
				firstDimensionName,
				secondDimensionName,
				new AggregationFunction<Tuple, xxl.core.collections.bags.Bag<Object>>() {
					@Override
					public xxl.core.collections.bags.Bag<Object> invoke(xxl.core.collections.bags.Bag<Object> bag, Tuple tuple) {
						if (bag == null)
							bag = new xxl.core.collections.bags.ListBag<Object>();
						bag.insert(tuple.getObject(resultColumnIndex));
						return bag;
					}
				},
				new AbstractMetaDataFunction<xxl.core.collections.bags.Bag<Object>, Object, CompositeMetaData<Object, Object>>() {
					protected CompositeMetaData<Object, Object> globalMetaData = new CompositeMetaData<Object, Object>();
					{
						globalMetaData.add(ResultSetMetaDatas.RESULTSET_METADATA_TYPE, new ColumnMetaDataResultSetMetaData(new StoredColumnMetaData( false, false, true, false, ResultSetMetaData.columnNoNulls, true, 52, "AVG", "AVG", "", 52, 0, "", "", Types.DOUBLE, true, false, false)));
					}
					
					@Override
					public Object invoke(xxl.core.collections.bags.Bag<Object> bag) {
						if (bag == null)
							return null;
						double sum = 0;
						for (xxl.core.cursors.Cursor<Object> objects = bag.cursor(); objects.hasNext(); objects.next())
							//System.out.println("number is null");
							sum += objects.peek() == null ?
								0 :
								((Number)objects.peek()).doubleValue();
						return sum / bag.size();
					}
					
					@Override
					public CompositeMetaData<Object, Object> getMetaData() {
						return globalMetaData;
					}
					
				},
				xxl.core.relational.tuples.ArrayTuple.FACTORY_METHOD
			);
		else
			cursor = new CrossTableCursor<Object>(
				cursor, 
				firstDimensionName,
				secondDimensionName,
				new AggregationFunction<Tuple, Object>() {
					@Override
					public Object invoke(Object object, Tuple tuple) {
						return tuple.getObject(resultColumnIndex);
					}
				},
				new AbstractMetaDataFunction<Object, Object, CompositeMetaData<Object, Object>>() {
					CompositeMetaData<Object, Object> globalMetaData = new CompositeMetaData<Object, Object>();
					{
						globalMetaData.add(ResultSetMetaDatas.RESULTSET_METADATA_TYPE, new ProjectedResultSetMetaData(resultSetMetaData, resultColumnIndex));
					}
					
					@Override
					public Object invoke(Object object) {
						return object;
					}
					
					@Override
					public CompositeMetaData<Object, Object> getMetaData() {
						return globalMetaData;
					}
				},
				xxl.core.relational.tuples.ArrayTuple.FACTORY_METHOD
			);
		
		java.io.PrintStream stream = null;
		if (writeToSystemOut)
			stream = System.out;
		else
			stream = new java.io.PrintStream(new java.io.FileOutputStream(args[6]));
		xxl.core.relational.resultSets.ResultSets.writeToPrintStream(cursor, stream, true, "\t");
		if (!writeToSystemOut)
			System.out.println("Cross table has been written to " + args[6]);
	}

}
