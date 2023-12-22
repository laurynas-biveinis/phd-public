package xxl.tests.cursors.identities;

import java.util.Iterator;

import xxl.core.collections.queues.ListQueue;
import xxl.core.collections.queues.Queue;
import xxl.core.cursors.identities.DelayCursor;
import xxl.core.cursors.identities.ThreadedIterator;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class ThreadedIterator.
 */
public class TestThreadedIterator extends ThreadedIterator {

	/**
	 * Constructs a new thread iterator. The given iterators run in their own
	 * threads. The threads start working after the first demand (a call to
	 * <tt>hasNext</tt> or <tt>next</tt>).
	 *
	 * @param iterators an array of iterators that should run in single threads.
	 * @param queue a queue that stores the delivered objects of the given
	 *        iterators. In order to get a buffer of a fixed size use a
	 *        {@link xxl.core.collections.queues.BoundedQueue bounded queue}.
	 * @param threadPriority the priority of the threads.
	 */
	public TestThreadedIterator(Iterator[] iterators, Queue queue, int threadPriority) {
		super(iterators, queue, threadPriority);
	}
	
	/**
	 * Constructs a new thread iterator wrapping a single iterator and using a
	 * list-queue for storing the elements of the wrapped iterator. The priority
	 * of the internally used thread is set to
	 * {@link java.lang.Thread#NORM_PRIORITY}.
	 *
	 * @param iterator the iterator that should run in a thread.
	 */
	public TestThreadedIterator(Iterator iterator) {
		super(iterator);
	}
	
	/**
	 * This method consumes the given cursor and checks whether it contains
	 * the specified number of elements. If not so an exception is thrown.
	 * 
	 * @param it the iterator to be consumed.
	 * @param numberOfElementsExpected the expected number of the given
	 *        iterator's elements.
	 */
	private static void consumeCursor(Iterator it, int numberOfElementsExpected) {
		int count = 0;
		while (it.hasNext()) {
			System.out.print(it.next()+" ");
			count++;
		}
		
		if (count != numberOfElementsExpected) {
			// for main maker
			System.out.println("Number of elements: " + count);
			System.out.println("Number of elements (expected): " + numberOfElementsExpected);
			throw new RuntimeException("Error in ThreadedIterator (1)!!!");
		}
			
		// Print an exception!
		try {
			it.next();
			// for main maker
			throw new RuntimeException("Error in ThreadedIterator (2)!!!");
		}
		catch (Exception e) {
			System.out.println("Everything ok!");
		}
	}

	/**
	 * This method consumes the given cursor and checks whether it contains
	 * the specified number of elements. If not so an exception is thrown.
	 * Additionally an output will be generated for the iterator's elements.
	 * 
	 * @param it the iterator to be consumed.
	 * @param numberOfElementsExpected the expected number of the given
	 *        iterator's elements.
	 */
	private static void consumeCursorWithoutOutput(Iterator it, int numberOfElementsExpected) {
		long t1 = System.currentTimeMillis();
		int count = 0;
		while (it.hasNext()) {
			it.next();
			count++;
			try { it.wait(10); } catch (Exception e) {}
		}
		long t2 = System.currentTimeMillis();
		
		System.out.println("Time for consuming cursor: " + (t2-t1));
		
		if (count != numberOfElementsExpected) {
			// for main maker
			System.out.println("Number of elements: " + count);
			System.out.println("Number of elements (expected): " + numberOfElementsExpected);
			if (it instanceof ThreadedIterator)
				System.out.println("Number of threads running: " + ((TestThreadedIterator) it).numThreadsRunning);
			throw new RuntimeException("Error in ThreadedIterator (1)!!!");
		}
		
		// Print an exception!
		try {
			it.next();
			// for main maker
			throw new RuntimeException("Error in ThreadedIterator (2)!!!");
		}
		catch (Exception e) {
			System.out.println("Everything ok!");
		}
	}	
	
	/**
	 * The main method contains some examples to demonstrate the usage and the
	 * functionality of this class.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to submit
	 *        parameters when the main method is called.
	 */
	public static void main(String[] args) {
		
		/*********************************************************************/
		/*                            Example                                */
		/*********************************************************************/
		
		int numbers=100;
		int numbers2=1000;
		int numbers3=100000;
		int queueSize=10;
		
		TestThreadedIterator it;
		
		System.out.println("Example for the ThreadedIterator");
		
		System.out.println();
		System.out.println("Usage with one Enumerator (" + numbers3 + " elements)");

		System.out.println("Performance test: without threading");
		consumeCursorWithoutOutput(
			new xxl.core.cursors.sources.Enumerator(numbers3),
			numbers3
		);
		
		System.out.println("Performance test: with ThreadedIterator");
		it = new TestThreadedIterator(new xxl.core.cursors.sources.Enumerator(numbers3));
		consumeCursorWithoutOutput(
			it,
			numbers3
		);
		
		System.out.println();
		System.out.println("Usage with some Enumerators and a RandomIntegers cursor");
		
		it = new TestThreadedIterator(
			new Iterator[] {
				new xxl.core.cursors.sources.Enumerator(numbers+20),
				new xxl.core.cursors.sources.Enumerator(numbers),
				new xxl.core.cursors.sources.DiscreteRandomNumber(numbers+10),
				new xxl.core.cursors.sources.Enumerator(0),
				new xxl.core.cursors.sources.Enumerator(1)
			},
			new xxl.core.collections.queues.BoundedQueue(
				new ListQueue(),
				queueSize
				),
			Thread.MIN_PRIORITY
		);
		consumeCursor(
			it,
			3*numbers+31
		);
		
		System.out.println();
		System.out.println("Usage with fast consumer without output");
		
		it = new TestThreadedIterator(
			new Iterator[] {
				new DelayCursor(
					new xxl.core.cursors.sources.Enumerator(2*numbers2),
					new xxl.core.cursors.sources.DiscreteRandomNumber(new xxl.core.util.random.JavaDiscreteRandomWrapper(2)),
					true
				),
				new xxl.core.cursors.sources.Enumerator(numbers2),
				new DelayCursor(
					new xxl.core.cursors.sources.DiscreteRandomNumber(numbers2),
					3,
					true
				),
			},
			new xxl.core.collections.queues.BoundedQueue(
				new ListQueue(),
				queueSize
			),
			Thread.MIN_PRIORITY
		);
		consumeCursorWithoutOutput(
			it,
			4*numbers2
		);

		System.out.println();
		System.out.println("Usage with fast consumer without output and delays, with different priorities");
		
		for (int priority=Thread.MIN_PRIORITY; priority <= Thread.MAX_PRIORITY; priority++) {
			System.out.println("Priority: " + priority);
			it = new TestThreadedIterator(
				new Iterator[] {
					new xxl.core.cursors.sources.Enumerator(2*numbers2),
					new xxl.core.cursors.sources.Enumerator(numbers2),
					new xxl.core.cursors.sources.DiscreteRandomNumber(3*numbers2),
					new xxl.core.cursors.sources.Enumerator(numbers2),
					new xxl.core.cursors.sources.DiscreteRandomNumber(numbers2)
				},
				new xxl.core.collections.queues.BoundedQueue(
					new ListQueue(),
					queueSize
				),
				priority
			);
			consumeCursorWithoutOutput(
				it,
				8*numbers2
			);
		}
		
		System.out.println();
		System.out.println("Usage with delayed cursors");

		it = new TestThreadedIterator(
			new Iterator[] {
				new DelayCursor(
					new xxl.core.cursors.sources.Enumerator(2*numbers),
					new xxl.core.cursors.sources.DiscreteRandomNumber(new xxl.core.util.random.JavaDiscreteRandomWrapper(100)),
					true
				),
				new DelayCursor(
					new xxl.core.cursors.sources.Enumerator(numbers),
					10,
					true
				),
				new DelayCursor(
					new xxl.core.cursors.sources.DiscreteRandomNumber(numbers),
					200,
					true
				),
				new xxl.core.cursors.sources.Enumerator(0),
				new xxl.core.cursors.sources.Enumerator(1)
			},
			new xxl.core.collections.queues.BoundedQueue(
				new ListQueue(),
				queueSize
			),
			Thread.MIN_PRIORITY
		);
		consumeCursor(
			it,
			4*numbers+1
		);

		System.out.println();
		System.out.println("ThreadedIterator finished successfully.");
	}

}
