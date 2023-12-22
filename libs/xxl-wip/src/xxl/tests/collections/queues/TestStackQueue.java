package xxl.tests.collections.queues;

import xxl.core.collections.queues.Queues;
import xxl.core.collections.queues.StackQueue;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class StackQueue.
 */
public class TestStackQueue {

	/**
	 * The main method contains some examples how to use a StackQueue. It
	 * can also be used to test the functionality of a StackQueue.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main (String [] args) {

		//////////////////////////////////////////////////////////////////
		//                      Usage example (1).                      //
		//////////////////////////////////////////////////////////////////

		// create a new stack queue
		StackQueue<Integer> queue = new StackQueue<Integer>();
		// open the queue
		queue.open();
		// create an iteration over 20 random Integers (between 0 and 100)
		xxl.core.cursors.Cursor<Integer> cursor = new xxl.core.cursors.sources.DiscreteRandomNumber(new xxl.core.util.random.JavaDiscreteRandomWrapper(100), 20);
		// insert all elements of the given iterator
		for (; cursor.hasNext(); queue.enqueue(cursor.next()));
		// print all elements of the queue
		while (!queue.isEmpty())
			System.out.println(queue.dequeue());
		System.out.println();
		// close open queue and cursor after use
		queue.close();
		cursor.close();

		//////////////////////////////////////////////////////////////////
		//                      Usage example (2).                      //
		//////////////////////////////////////////////////////////////////

		// create an iteration over the Integer between 0 and 19
		cursor = new xxl.core.cursors.sources.Enumerator(20);
		// create a new stack queue that uses a new stack to store its
		// elements and that contains all elements of the given iterator
		queue = new StackQueue<Integer>();
		Queues.enqueueAll(queue, cursor);
		// open the queue
		queue.open();
		// print all elements of the queue
		while (!queue.isEmpty())
			System.out.println(queue.dequeue());
		System.out.println();
		// close open queue and cursor after use
		queue.close();
		cursor.close();
	}

}
