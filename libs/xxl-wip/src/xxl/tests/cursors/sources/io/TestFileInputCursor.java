package xxl.tests.cursors.sources.io;

import java.io.File;

import xxl.core.cursors.sources.io.FileInputCursor;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class FileInputCursor.
 */
public class TestFileInputCursor {

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

		// create a new file
		
		File file = new File("file.dat");
		// catch IOExceptions
		
		try {
			
			// create a random access file on that file
			
			java.io.RandomAccessFile output = new java.io.RandomAccessFile(file, "rw");
			
			// write some data to that file
			
			output.writeUTF("Some data.");
			output.writeUTF("More data.");
			output.writeUTF("Another bundle of data.");
			output.writeUTF("A last bundle of data.");
			
			// close the random access file
			
			output.close();
		}
		catch (Exception e) {
			System.out.println("An error occured.");
		}
		
		// create a new file input iterator with ...
		
		FileInputCursor<String> cursor = new FileInputCursor<String>(
			
			// a string converter
			
			xxl.core.io.converters.StringConverter.DEFAULT_INSTANCE,
			
			// the created file
			
			file
		);
		
		// open the cursor
		
		cursor.open();
		
		// print all elements of the iterator
		
		while (cursor.hasNext())
			System.out.println(cursor.next());
		System.out.println();
		
		// close the cursor
		
		cursor.close();
		
		// delete the file
		file.delete();
	}

}
