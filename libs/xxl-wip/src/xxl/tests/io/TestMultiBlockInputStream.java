package xxl.tests.io;

import java.io.InputStream;

import xxl.core.cursors.Cursor;
import xxl.core.io.Block;
import xxl.core.io.MultiBlockInputStream;
import xxl.core.io.ObjectToBlockCursor;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class MultiBlockInputStream.
 */
public class TestMultiBlockInputStream {

	/**
	 * Use case which serializes Strings to Blocks and vice versa.
	 * @param args The command line options are ignored here.
	 */
	public static void main(String args[]) {
		String s = "Hello World";
		Cursor cursor;
		// Block b = null;
		
		System.out.println("Example");
		System.out.println("=======");
		
		cursor = new ObjectToBlockCursor(
			new xxl.core.cursors.sources.Repeater(s,10),
			xxl.core.io.converters.StringConverter.DEFAULT_INSTANCE,
			5,
			4,
			100,
			Block.SET_REAL_LENGTH
		);
		
		InputStream is = new MultiBlockInputStream(cursor,4,5,Block.GET_REAL_LENGTH);
		
		cursor = new xxl.core.cursors.sources.io.InputStreamCursor(
			new java.io.DataInputStream(is),
			xxl.core.io.converters.StringConverter.DEFAULT_INSTANCE
		);
		
		xxl.core.cursors.Cursors.println(cursor);
		
		cursor.close();
	}

}
