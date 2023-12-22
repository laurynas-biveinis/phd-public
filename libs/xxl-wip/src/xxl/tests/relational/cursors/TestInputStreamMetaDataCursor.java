package xxl.tests.relational.cursors;

import java.io.IOException;
import java.net.URL;

import xxl.core.cursors.MetaDataCursor;
import xxl.core.relational.cursors.InputStreamMetaDataCursor;
import xxl.core.relational.cursors.NestedLoopsJoin;
import xxl.core.relational.metaData.ResultSetMetaDatas;
import xxl.core.relational.tuples.ArrayTuple;
import xxl.core.relational.tuples.Tuple;
import xxl.core.util.metaData.CompositeMetaData;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class InputStreamMetaDataCursor.
 */
public class TestInputStreamMetaDataCursor {
	
	/**
	 * Use case that reads the specified URL (first command line argument) and
	 * outputs all tuples. This method can be used to test an URL if it can be
	 * used with a input stream metadata cursor. When two URLs are specified a
	 * nested-loops join of them is calculated.
	 * 
	 * @param args the arguments to the main method. The arguments can be one
	 *        or two URLs.
	 */
	public static void main(String[] args) {
		try {
			MetaDataCursor<Tuple, CompositeMetaData<Object, Object>> cursor = args.length == 0 ?
				new InputStreamMetaDataCursor(new URL("http://dbs.mathematik.uni-marburg.de/downloads/data/FileMDCtest.txt")) :
				args.length == 1 ?
					new InputStreamMetaDataCursor(new URL(args[0])) :
					new NestedLoopsJoin(
						new InputStreamMetaDataCursor(new URL(args[0])),
						new InputStreamMetaDataCursor(new URL(args[1])),
						null,
						ArrayTuple.FACTORY_METHOD,
						NestedLoopsJoin.Type.NATURAL_JOIN
					);
			
			System.out.println(ResultSetMetaDatas.getResultSetMetaData(cursor));
					
			while (cursor.hasNext())
				System.out.println(cursor.next());
			
			cursor.reset();
			System.out.println("Number of tuples: " + xxl.core.cursors.Cursors.count(cursor));
			
			cursor.close();
		}
		catch (java.net.MalformedURLException mue) {
			System.out.println("The use case cannot be executed because of malformed URLs : " + mue.getMessage());
		}
		catch (IOException ioe) {
			System.out.println("The use case cannot be executed because of I/O exceptions : " + ioe.getMessage());
		}
	}

}
