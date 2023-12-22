package xxl.tests.collections.queues;

import xxl.core.collections.queues.ArrayQueue;
import xxl.core.collections.queues.BoundedQueue;
import xxl.core.collections.queues.Queue;
import xxl.core.collections.queues.ThreadsafeQueue;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class ThreadsafeQueue.
 */
public class TestThreadsafeQueue {

	/**
	 * The main method contains an examples to demonstrate the usage
	 * of the ThreadsafeQueue.
	 * 
	 * @param args the arguments for the <tt>main</tt> method.
	 */
	public static void main(String[] args){

		//////////////////////////////////////////////////////////////////
		//                      Usage example (1).                      //
		//////////////////////////////////////////////////////////////////
		System.out.println("Simple example for the ThreadsafeQueue");
		// create a threadsafe array queue
		Queue<Integer> queue = new ThreadsafeQueue<Integer>(new BoundedQueue<Integer>(new ArrayQueue<Integer>(),100));
		// open the queue
		queue.open();
		// create an enumeration with 100 elements
		xxl.core.cursors.Cursor<Integer> cursor = new xxl.core.cursors.sources.Enumerator(100);
		// insert all elements in the queue
		for (; cursor.hasNext(); queue.enqueue(cursor.next()));
		System.out.println("There were "+queue.size()+" elements in the queue");
		
		// close the queue and the cursor
		queue.close();
		cursor.close();
	}

}
