package xxl.tests.collections.queues;

import xxl.core.collections.queues.ListQueue;
import xxl.core.collections.queues.UnmodifiableQueue;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class UnmodifiableQueue.
 */
public class TestUnmodifiableQueue {

	/**
	 * The main method contains some examples how to use an
	 * UnmodifiableQueue. It can also be used to test the functionality of
	 * an UnmodifiableQueue.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main(String[] args) {

		//////////////////////////////////////////////////////////////////
		//                      Usage example (1).                      //
		//////////////////////////////////////////////////////////////////

		// create a new queue
		ListQueue<Integer> inputQueue = new ListQueue<Integer>();
		// open the queue
		inputQueue.open();
		// create an iteration over 20 random Integers (between 0 and 100)
		xxl.core.cursors.Cursor<Integer> cursor = new xxl.core.cursors.sources.DiscreteRandomNumber(new xxl.core.util.random.JavaDiscreteRandomWrapper(100), 20);
		// insert all elements of the given iterator
		for (; cursor.hasNext(); inputQueue.enqueue(cursor.next()));
		// create a new unmodifiable queue with the given queue
		UnmodifiableQueue<Integer> queue = new UnmodifiableQueue<Integer>(inputQueue);
		// open the queue
		queue.open();
		// print all elements of the queue
		while (!queue.isEmpty())
			System.out.println(queue.dequeue());
		System.out.println();
		// close the open queues and cursor after use
		inputQueue.close();
		queue.close();
		cursor.close();
	}

}
