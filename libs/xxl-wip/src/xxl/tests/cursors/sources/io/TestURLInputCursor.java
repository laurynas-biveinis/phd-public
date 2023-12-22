package xxl.tests.cursors.sources.io;

import java.io.IOException;
import java.net.URL;

import xxl.core.cursors.sources.io.URLInputCursor;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class URLInputCursor.
 */
public class TestURLInputCursor {

	/**
	 * The main method contains some examples to demonstrate the usage and the
	 * functionality of this class.
	 *
	 * @param args array of <code>String</code> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main(String[] args) {

		//////////////////////////////////////////////////////////////////
		//                      Usage example (1).                      //
		//////////////////////////////////////////////////////////////////

		// catch MalformedURLExceptions and IOExceptions
		
		try {
			
			// create a new URLInputIterator with ...
			
			URLInputCursor<xxl.core.spatial.KPE> cursor = new URLInputCursor<xxl.core.spatial.KPE>(
				
				// a converter for convertable objects
				
				new xxl.core.io.converters.ConvertableConverter<xxl.core.spatial.KPE>(
					
					// a factory method that created KPEs with 2 dimensions
						
					new xxl.core.functions.AbstractFunction<Object, xxl.core.spatial.KPE>() {
						@Override
						public xxl.core.spatial.KPE invoke () {
							return new xxl.core.spatial.KPE(2);
						}
					}
				),
				
				// a given URL
				
				new URL("http://dbs.mathematik.uni-marburg.de/research/projects/xxl/rr_small.bin")
			);
			
			// open the cursor
			
			cursor.open();
			
			// print all elements of the cursor
			
			while(cursor.hasNext())
				System.out.println(cursor.next());
			
			// close the cursor
			
			cursor.close();
			
		}
		catch (java.net.MalformedURLException mue) {
			System.out.println("The given URL is malformed.");
		}
		catch (IOException ioe) {
			System.out.println("The given URL cannot be opened for reading.");
		}
		System.out.println();
	}

}
