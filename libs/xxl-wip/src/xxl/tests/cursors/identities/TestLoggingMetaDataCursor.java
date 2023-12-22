package xxl.tests.cursors.identities;

import xxl.core.cursors.identities.LoggingMetaDataCursor;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class LoggingMetaDataCursor.
 */
public class TestLoggingMetaDataCursor {

	/**
	 * The main method contains some examples to demonstrate the usage and the
	 * functionality of this class.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to submit
	 *        parameters when the main method is called.
	 */
	public static void main(String[] args) {
		System.out.println("Example using a LoggingMetaDataCursor");
		System.out.println();

		xxl.core.cursors.Cursor cursor = new LoggingMetaDataCursor(
			new xxl.core.cursors.sources.Enumerator(10),
			System.out,
			"Enumerator",
			false
		);
		
		cursor.open();
		
		System.out.println("Number of elements in the cursor: " + xxl.core.cursors.Cursors.count(cursor));
		
		cursor.close();
	}

}
