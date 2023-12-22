package xxl.tests.collections.queues.io;

import java.io.File;

import xxl.core.collections.queues.io.FileStackQueue;
import xxl.core.io.converters.IntegerConverter;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class FileStackQueue.
 */
public class TestFileStackQueue {

	/**
	 * The main method contains some examples how to use a
	 * FileStackQueue. It can also be used to test the functionality
	 * of a FileStackQueue.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main (String [] args) {

		//////////////////////////////////////////////////////////////////
		//                      Usage example (1).                      //
		//////////////////////////////////////////////////////////////////

		// create a new file
		File file = new File("file.dat");
		// create a new file stack queue with ...
		FileStackQueue queue = new FileStackQueue(
			// the created file
			file,
			// an integer converter
			IntegerConverter.DEFAULT_INSTANCE
		);
		// open the queue
		queue.open();
		// create an iteration over the Integer between 0 and 19
		xxl.core.cursors.Cursor cursor = new xxl.core.cursors.sources.Enumerator(20);
		// insert all elements of the given iterator
		for (; cursor.hasNext(); queue.enqueue(cursor.next()));
		// print all elements of the queue
		while (!queue.isEmpty())
			System.out.println(queue.dequeue());
		System.out.println();
		// close open queue and cursor after use
		queue.close();
		cursor.close();
		// delete the file
		file.delete();
	}

}
