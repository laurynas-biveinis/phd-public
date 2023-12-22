package xxl.tests.util.reflect;

import java.lang.reflect.Proxy;

import xxl.core.cursors.Cursor;
import xxl.core.util.reflect.Logger;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class Logger.
 */
public class TestLogger {

	/**
	 * Usage Example for logging.
	 * 
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 * 		submit parameters when the main method is called.
	 */
	public static void main(String args[]) {
		Cursor cursor;

		System.out.println("Start logging using the most general method");
		System.out.println();
		Proxy logger = (Proxy) Logger.newInstance(new xxl.core.cursors.sources.Enumerator(10),"xxl.core.cursors.Cursor",System.out);
		// The object is of type Proxy and fulfils the Cursor interface.
		cursor = (Cursor) logger;
		
		while (cursor.hasNext())
			System.out.println(cursor.next());
		cursor.close();

		System.out.println();
		System.out.println("Start logging using the special method getCursorLogger (without output)");
		System.out.println();

		cursor = Logger.getCursorLogger(new xxl.core.cursors.sources.Enumerator(10),xxl.core.util.XXLSystem.NULL);
		while (cursor.hasNext())
			cursor.next();
		cursor.close();
	}

}
