package xxl.tests.collections.queues;

import xxl.core.collections.queues.ListQueue;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class ListQueue.
 */
public class TestListQueue {
	
	/**
	 * The main method contains some examples how to use a ListQueue. It
	 * can also be used to test the functionality of a ListQueue.
	 * 
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main (String [] args) {
		
		//////////////////////////////////////////////////////////////////
		//                      Usage example (1).                      //
		//////////////////////////////////////////////////////////////////
		
		// create a new list queue (that uses a linked list to store its
		// elements per default)
		ListQueue<Integer> queue = new ListQueue<Integer>();
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
		// close the open queue and cursor after use
		queue.close();
		cursor.close();
		
		//////////////////////////////////////////////////////////////////
		//                      Usage example (2).                      //
		//////////////////////////////////////////////////////////////////
		
		// create an iteration over 20 random Integers (between 0 and 100)
		cursor = new xxl.core.cursors.sources.DiscreteRandomNumber(new xxl.core.util.random.JavaDiscreteRandomWrapper(100), 20);
		// create a new list queue that uses an array list to store its
		// elements and that contains all elements of the given iterator
		queue = new ListQueue<Integer>(new java.util.ArrayList<Integer>());
		// open the queue
		queue.open();
		// insert all elements into the queue
		for (; cursor.hasNext(); queue.enqueue(cursor.next()));
		// print all elements of the queue
		while (!queue.isEmpty())
			System.out.println(queue.dequeue());
		System.out.println();
		// close the open queue and cursor after use
		queue.close();
		cursor.close();
	}

}
