package xxl.tests.collections.queues.io;

import xxl.core.collections.queues.io.ListStreamQueue;
import xxl.core.functions.Constant;
import xxl.core.io.converters.IntegerConverter;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class ListStreamQueue.
 */
public class TestListStreamQueue {

	/**
	 * The main method contains some examples how to use a
	 * ListStreamQueue. It can also be used to test the functionality of
	 * a ListStreamQueue.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main (String [] args) {

		//////////////////////////////////////////////////////////////////
		//                      Usage example (1).                      //
		//////////////////////////////////////////////////////////////////

		// create a new list stream queue with ...
		ListStreamQueue queue = new ListStreamQueue(
			// an integer converter
			IntegerConverter.DEFAULT_INSTANCE,
			// an input buffer size of 4 bytes (size of an integer)
			new Constant(4),
			// an output buffer size of 4 bytes (size of an integer)
			new Constant(4)
		);
		// open the queue
		queue.open();
		// insert the integer from 0 to 9 into the queue
		for (int i = 0; i < 10; i++)
			queue.enqueue(new Integer(i));
		// print five elements of the queue
		int i = 0;
		while (i < 5 && !queue.isEmpty()) {
			i = ((Integer)queue.dequeue()).intValue();
			System.out.println(i);
		}
		// insert the integers from 20 to 29
		for (i = 20; i < 30; i++)
			queue.enqueue(new Integer(i));
		// print all elements of the queue
		while (!queue.isEmpty())
			System.out.println(queue.dequeue());
		System.out.println();
		
		// close the queue
		queue.close();
	}

}
