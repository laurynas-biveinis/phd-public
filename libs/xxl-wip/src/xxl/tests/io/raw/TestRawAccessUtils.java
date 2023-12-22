package xxl.tests.io.raw;

import xxl.core.io.raw.RawAccessUtils;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class RawAccessUtils.
 */
public class TestRawAccessUtils {

	/**
	 * Main method. Constructs a file that can be used with the raw-classes.
	 *
	 * @param args the first parameter has to be the name of the file that 
	 *	will be constructed - the second parameter is the number of sectors.
	 *	(for example "test 20480").
	 */
	public static void main(String args[]) {
		System.out.println("createFileForRaw");
		if (args.length!=2) {
			System.out.println("The number of parameters has to be 2 (filename and number of sectors)");
			return;
		}
		
		RawAccessUtils.createFileForRaw(args[0], Long.parseLong(args[1]));
	}

}
