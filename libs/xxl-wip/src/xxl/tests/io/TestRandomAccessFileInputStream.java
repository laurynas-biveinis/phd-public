package xxl.tests.io;


/**
 * Some examples to demonstrate the functionality and the usage
 * of the class RandomAccessFileInputStream.
 */
public class TestRandomAccessFileInputStream {
	
	/**
	 * Example: copying a RandomAccessFile via Streams (calls the same example
	 * of RandomAccessFileInputStream). You have to specify two filenames 
	 * as arguments. Example:
	 * <pre>
	 * xxl xxl.core.io.RandomAccessFileInputStream c:\test.txt c:\copytest.txt
	 * </pre>
	 * @param args The command line options (two filenames).
	 */
	public static void main (String args[]) {
		TestRandomAccessFileOutputStream.main(args);
	}

}
