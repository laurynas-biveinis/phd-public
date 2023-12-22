package xxl.tests.io;

import java.io.OutputStream;

import xxl.core.io.ReplaceOutputStream;
import xxl.core.util.WrappingRuntimeException;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class ReplaceOutputStream.
 */
public class TestReplaceOutputStream {
	
	/**
	 * Example filtering a file and writing the output to a second file.
	 * @param args The command line options (two filenames).
	 */
	public static void main (String args[]) {
		if (args.length!=2) {
			System.out.println("2 Parameter expected: infilename, outfilename");
			return;
		}
			
		try {
			java.io.InputStream is = new java.io.FileInputStream(args[0]);
			
			int c1[] = new int[] {'\n','\r'}; // '\r'; // this is the important case!
			int c2[] = new int[] {-1,-1}; // do not write the characters!
			
			OutputStream os = new ReplaceOutputStream(new java.io.FileOutputStream(args[1]),c1,c2);
			
			int len;
			byte b[] = new byte[4096];
			
			while ((len=is.read(b))!=-1)
				os.write(b,0,len);
			
			is.close();
			os.close();
		}
		catch (java.io.IOException e) {
			throw new WrappingRuntimeException(e);
		}
	}

}
