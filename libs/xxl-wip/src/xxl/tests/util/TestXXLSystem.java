package xxl.tests.util;

import xxl.core.util.WrappingRuntimeException;
import xxl.core.util.XXLSystem;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class XXLSystem.
 */
public class TestXXLSystem {

	/**
	 * The main method contains some examples for methods of this class.
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 *	submit parameters when the main method is called.
	 */
	public static void main (String args[]) {
		System.out.println("Object serialization/deserialization");
		byte b1[] = XXLSystem.serializeObject(new String("hello world"));
		byte b2[] = XXLSystem.serializeObject(new Integer(4711));
		String s = (String)XXLSystem.deserializeObject(b1);
		Integer i = (Integer)XXLSystem.deserializeObject(b2);
		
		System.out.println("String: " + s);
		System.out.println("Integer: " + i);

		System.out.println();
		System.out.println("Rootpath: " + XXLSystem.getRootPath());
		System.out.println("Outpath: " + XXLSystem.getOutPath());
		
		try {
			double d[] = new double[10];
			int length = XXLSystem.getObjectSize(d);
			System.out.println(length + " (should be 84)");
			if (length != 84)
				throw new RuntimeException("Size not correct");
			
			Double d2 = new Double(2);
			length = XXLSystem.getObjectSize(d2);
			System.out.println(length + " (should be 8)");
			if (length != 8)
				throw new RuntimeException("Size not correct");
		}
		catch (Exception e) {
			throw new WrappingRuntimeException(e);
		}
		
	}

}
