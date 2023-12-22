package xxl.tests.collections.queues;

import xxl.core.collections.queues.DistinctQueue;
import xxl.core.collections.queues.ListQueue;
import xxl.core.collections.queues.Queue;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class DistinctQueue.
 */
public class TestDistinctQueue {

	/**
	 * The main method contains some examples how to use a DistinctQueue.
	 * It can also be used to test the functionality of a DistinctQueue.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main(String[] args) {

		//////////////////////////////////////////////////////////////////
		//                      Usage example (1).                      //
		//////////////////////////////////////////////////////////////////

		// create a new queue
		Queue<Integer> q1 = new ListQueue<Integer>();
		// open the queue
		q1.open();
		// create an iteration over 10000 random Integers (between 0 and
		// 100)
		xxl.core.cursors.Cursor<Integer> cursor = new xxl.core.cursors.sources.DiscreteRandomNumber(new xxl.core.util.random.JavaDiscreteRandomWrapper(100), 10000);
		// insert all elements of the given cursor
		for (; cursor.hasNext(); q1.enqueue(cursor.next()));
		// print the size of the queue after insertion
		System.out.println("Size q1:\t" + q1.size());
		// reset the cursor, so that it can be used again
		cursor.reset();
		// create a new distinct queue
		Queue<Integer> q2 = new DistinctQueue<Integer>(new ListQueue<Integer>());
		// open the queue
		q2.open();
		// insert all elements of the given cursor
		for (; cursor.hasNext(); q2.enqueue(cursor.next()));
		// print the size of the distinct queue after insertion
		System.out.println("Size q2:\t" + q2.size());
		// close the open queues and the cursor after use
		q1.close();
		q2.close();
		cursor.close();
	}

}
