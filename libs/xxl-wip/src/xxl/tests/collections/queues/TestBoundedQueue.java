package xxl.tests.collections.queues;

import java.util.Iterator;

import xxl.core.collections.queues.BoundedQueue;
import xxl.core.collections.queues.Queue;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class BoundedQueue.
 */
public class TestBoundedQueue {

	/**
	 * The main method contains an example to demonstrate the usage
	 * of the BoundedQueue. An emumerator produces a sequence of
	 * Integers that are inserted into the bounded queue. When the queue
	 * is full, an exception is thrown, which is caught by this main
	 * method. Then, all elements are consumed and counted.
	 * 
	 * @param args the arguments for the <tt>main</tt> method.
	 */
	public static void main(String[] args) {

		//////////////////////////////////////////////////////////////////
		//                      Usage example (1).                      //
		//////////////////////////////////////////////////////////////////
		System.out.println("Example for the BoundedQueue");
		// create a new bounded queue using an ArrayQueue
		Queue<Integer> queue = new BoundedQueue<Integer>(new xxl.core.collections.queues.ArrayQueue<Integer>(), 100);
		
		// open the queue
		queue.open();

		try {
			Iterator<Integer> iterator = new xxl.core.cursors.sources.Enumerator(200);
			for (; iterator.hasNext(); queue.enqueue(iterator.next()));
		}
		catch (IndexOutOfBoundsException e) {}

		int count = queue.size();
		System.out.println("The queue contained "+count+" elements (100 is correct!)");

		if (count != 100) {
			// for main maker
			throw new RuntimeException("Error in BoundedQueue!!!");
		}
		
		// close the queue
		queue.close();
	}

}
