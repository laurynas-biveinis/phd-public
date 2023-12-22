package xxl.tests.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import xxl.core.io.RandomAccessFileInputStream;
import xxl.core.io.RandomAccessFileOutputStream;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class RandomAccessFileOutputStream.
 */
public class TestRandomAccessFileOutputStream {
	
	/**
	 * Example: copying a RandomAccessFile via Streams. You have to 
	 * specify two filenames as arguments. Example:
	 * <pre>
	 * xxl xxl.core.io.RandomAccessFileOutputStream c:\test.txt c:\copytest.txt
	 * </pre>
	 * @param args The command line options (two filenames).
	 */
	public static void main (String args[]) {
		if (args.length==2) {
			try {
				RandomAccessFile source = new RandomAccessFile(args[0],"r");
				RandomAccessFile sink = new RandomAccessFile(args[1],"rw");
				
				InputStream inputStream = new java.io.BufferedInputStream(new RandomAccessFileInputStream(source));
				OutputStream outputStream = new java.io.BufferedOutputStream(new RandomAccessFileOutputStream(sink));
				for (int b; (b = inputStream.read())!=-1;)
					outputStream.write(b);
				outputStream.close();
				inputStream.close();
			}
			catch (IOException e) {
				System.out.println("Error while copying:");
				e.printStackTrace();
			}
		}
	}

}
