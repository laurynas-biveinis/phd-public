package xxl.tests.collections.queues;

import xxl.core.collections.queues.ArrayQueue;
import xxl.core.util.ArrayResizer;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class ArrayQueue.
 * 
 * @param <E> the type of the queue's elements.
 */
public class TestArrayQueue<E> extends ArrayQueue<E> {
	
	/**
	 * Constructs a new queue.
	 * 
	 * @param size the number of elements of the specified array which
	 *        should be used to initialize the internally used array.
	 * @param array the object array that is used to initialize the
	 *        internally used array.
	 * @param fmin a control parameter for the growth policy.
	 * @param fover a control parameter for the growth policy.
	 * @param funder a control parameter for the growth policy.
	 * @throws IllegalArgumentException if the specified size argument is
	 *         negative, or if it is greater than the length of the
	 *         specified array.
	 * @see ArrayResizer#ArrayResizer(double, double, double)
	 */
	public TestArrayQueue(int size, E[] array, double fmin, double fover, double funder) throws IllegalArgumentException {
		super(size, array, fmin, fover, funder);
	}

	/**
	 * Constructs a new queue.
	 * 
	 * @param array the object array that is used to initialize the
	 *        internally used array.
	 * @param fmin a control parameter for the growth policy.
	 * @param fover a control parameter for the growth policy.
	 * @param funder a control parameter for the growth policy.
	 */
	public TestArrayQueue(E[] array, double fmin, double fover, double funder) {
		super(array, fmin, fover, funder);
	}

	/**
	 * Constructs a new queue.
	 * 
	 * @param fmin a control parameter for the growth policy.
	 * @param fover a control parameter for the growth policy.
	 * @param funder a control parameter for the growth policy.
	 */
	public TestArrayQueue(double fmin, double fover, double funder) {
		super(fmin, fover, funder);
	}

	/**
	 * Constructs a new queue.
	 * 
	 * @param size the number of elements of the specified array which
	 *        should be used to initialize the internally used array.
	 * @param array the object array that is used to initialize the
	 *        internally used array.
	 * @param fmin a control parameter for the growth policy.
	 * @param f a control parameter for the growth policy.
	 * @throws IllegalArgumentException if the specified size argument is
	 *         negative, or if it is greater than the length of the
	 *         specified array.
	 * @see ArrayResizer#ArrayResizer(double, double)
	 */
	public TestArrayQueue(int size, E[] array, double fmin, double f) throws IllegalArgumentException {
		super(size, array, fmin, f);
	}

	/**
	 * Constructs a new queue.
	 * 
	 * @param array the object array that is used to initialize the
	 *        internally used array.
	 * @param fmin a control parameter for the growth policy.
	 * @param f a control parameter for the growth policy.
	 */
	public TestArrayQueue(E[] array, double fmin, double f) {
		super(array, fmin, f);
	}

	/**
	 * Constructs a new queue.
	 * 
	 * @param fmin a control parameter for the growth policy.
	 * @param f a control parameter for the growth policy.
	 */
	public TestArrayQueue(double fmin, double f) {
		super(fmin, f);
	}

	/**
	 * Constructs a new queue.
	 * 
	 * @param size the number of elements of the specified array which
	 *        should be used to initialize the internally used array.
	 * @param array the object array that is used to initialize the
	 *        internally used array.
	 * @param fmin a control parameter for the growth policy.
	 * @throws IllegalArgumentException if the specified size argument is
	 *         negative, or if it is greater than the length of the
	 *         specified array.
	 * @see ArrayResizer#ArrayResizer(double)
	 */
	public TestArrayQueue(int size, E[] array, double fmin) throws IllegalArgumentException {
		super(size, array, fmin);
	}

	/**
	 * Constructs a new queue.
	 * 
	 * @param array the object array that is used to initialize the
	 *        internally used array.
	 * @param fmin a control parameter for the growth policy.
	 */
	public TestArrayQueue(E[] array, double fmin) {
		super(array, fmin);
	}

	/**
	 * Constructs a new queue.
	 * 
	 * @param fmin a control parameter for the growth policy.
	 */
	public TestArrayQueue(double fmin) {
		super(fmin);
	}

	/**
	 * Constructs a new queue.
	 * 
	 * @param size the number of elements of the specified array which
	 *        should be used to initialize the internally used array.
	 * @param array the object array that is used to initialize the
	 *        internally used array.
	 * @throws IllegalArgumentException if the specified size argument is
	 *         negative, or if it is greater than the length of the
	 *         specified array.
	 * @see ArrayResizer#ArrayResizer()
	 */
	public TestArrayQueue(int size, E[] array) throws IllegalArgumentException {
		super(size, array);
	}

	/**
	 * Constructs a new queue.
	 * 
	 * @param array the object array that is used to initialize the
	 *        internally used array.
	 */
	public TestArrayQueue(E[] array) {
		super(array);
	}

	/**
	 * Constructs a new queue.
	 */
	public TestArrayQueue() {
		super();
	}
	/**
	 * The main method contains some examples how to use an ArrayQueue.
	 * It can also be used to test the functionality of an ArrayQueue.
	 * 
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main(String[] args) {
		
		//////////////////////////////////////////////////////////////////
		//                      Usage example (1).                      //
		//////////////////////////////////////////////////////////////////
		
		// create a new array
		Integer[] array = new Integer[] {
			new Integer(1),
			new Integer(2),
			new Integer(3),
			new Integer(4),
			new Integer(5)
		};
		// create a new array queue with the given array
		TestArrayQueue<Integer> queue = new TestArrayQueue<Integer>(array);	
		// open the queue
		queue.open();
		// print all elements of the queue
		while (!queue.isEmpty())
			System.out.println(queue.dequeue());
		System.out.println();
		// close the open queue after use
		queue.close();
		
		//////////////////////////////////////////////////////////////////
		//                      Usage example (2).                      //
		//////////////////////////////////////////////////////////////////
		
		// create a new array queue with the first three elements of the
		// given array
		queue = new TestArrayQueue<Integer>(3, array);
		// open the queue
		queue.open();
		// print all elements of the queue
		while (!queue.isEmpty())
			System.out.println(queue.dequeue());
		System.out.println();
		// close the open queue after use
		queue.close();
		
		//////////////////////////////////////////////////////////////////
		//                      Usage example (3).                      //
		//////////////////////////////////////////////////////////////////
		
		// create a new iterator containing the elements of the given
		// array
		xxl.core.cursors.Cursor<Integer> cursor = new xxl.core.cursors.sources.ArrayCursor<Integer>(array);
		// create a new empty array queue
		queue = new TestArrayQueue<Integer>();
		// open the queue
		queue.open();
		// insert all elements of the given iterator into the queue
		for (; cursor.hasNext(); queue.enqueue(cursor.next()));
		// print all elements of the queue
		while (!queue.isEmpty())
			System.out.println(queue.dequeue());
		System.out.println();
		// close the open queue and cursor after use
		queue.close();
		cursor.close();
		
		//////////////////////////////////////////////////////////////////
		//                      Usage example (4).                      //
		//////////////////////////////////////////////////////////////////
		
		// create a new empty queue with a growth policy managed by
		// new ArrayResizer(0.5, 0.6, 0.4)
		queue = new TestArrayQueue<Integer>(0.5, 0.6, 0.4);
		// open the queue
		queue.open();
		// insert and remove 20 elements and print after each operation
		// the size of the queue and the internally used array
		System.out.println("queue.size()="+queue.size()+" & "
			+"queue.array.length="+queue.array.length);
		for (int i = 1; i < 21; i++) {
			queue.enqueue(new Integer(i));
			System.out.println("queue.size()="+queue.size()+" & "
				+"queue.array.length="+queue.array.length);
		}
		while (!queue.isEmpty()) {
			queue.dequeue();
			System.out.println("queue.size()="+queue.size()+" & "
				+"queue.array.length="+queue.array.length);
		}
		System.out.println();
		// close the open queue after use
		queue.close();
	}

}
