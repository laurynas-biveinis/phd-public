package xxl.tests.collections.queues;

import java.util.Comparator;
import java.util.ConcurrentModificationException;

import xxl.core.collections.queues.Heap;
import xxl.core.comparators.ComparableComparator;
import xxl.core.cursors.Cursor;
import xxl.core.predicates.Equal;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class Heap.
 * 
 * @param <E> the type of the heap's elements.
 */
public class TestHeap<E> extends Heap<E> {

	/**
	 * Constructs a new heap.
	 *
	 * @param array the object array that is used to store the heap and
	 *        initialize the internally used array.
	 * @param size the number of elements of the specified array which
	 *        should be used to initialize the heap.
	 * @param comparator the comparator to determine the order of the
	 *        heap.
	 * @throws IllegalArgumentException if the specified size argument is
	 *         negative, or if it is greater than the length of the
	 *         specified array.
	 */
	public TestHeap(E[] array, int size, Comparator<? super E> comparator) throws IllegalArgumentException {
		super(array, size, comparator);
	}

	/**
	 * Constructs a new heap.
	 *
	 * @param array the object array that is used to store the heap and
	 *        initialize the internally used array.
	 * @param comparator the comparator to determine the order of the
	 *        heap.
	 */
	public TestHeap(E[] array, Comparator<? super E> comparator) {
		super(array, comparator);
	}

	/**
	 * Constructs an new heap.
	 *
	 * @param size the maximal number of elements the heap is able to
	 *        contain.
	 * @param comparator the comparator to determine the order of the
	 *        heap.
	 */
	public TestHeap(int size, Comparator<? super E> comparator) {
		super(size, comparator);
	}

	/**
	 * Creates an array for testing below.
	 * @return The test array.
	 */
	private static Object[] createTestArray() {
		return new Object[] {
			new Object[] {
				new Integer(1),
				new String("first")
			},
			new Object[] {
				new Integer(2),
				new String("first")
			},
			new Object[] {
				new Integer(1),
				new String("second")
			},
			new Object[] {
				new Integer(3),
				new String("first")
			},
			new Object[] {
				new Integer(1),
				new String("third")
			}
		};
	}

	/**
	 * Tests if the ordering on the heap is correct.
	 */
	private void testHeap() {
		for (int i=0; i<(size+1)/2; i++) {
			if (comparator.compare((E)array[i], (E)array[2*i])>0)
				throw new RuntimeException("Heap is inconsistent");
			if (2*i+1 < size)
				if (comparator.compare((E)array[i], (E)array[2*i+1])>0)
					throw new RuntimeException("Heap is inconsistent");
		}
	}

	/**
	 * Method used inside main. 
	 * @param heap The heap to be tested.
	 * @param output Determines if the elements are printed on System.out.
	 */
	private static void testIntegerHeapOutput(TestHeap<Integer> heap, boolean output) {
		int lastValue=-1;
		while(!heap.isEmpty()) {
			int currentValue = heap.dequeue().intValue();
			heap.testHeap();
			if (output) 
				System.out.println(currentValue);
			if (currentValue<lastValue)
				throw new RuntimeException("Heap was invalid");
			lastValue = currentValue;
		}
	}
	
	/**
	 * The main method contains some examples how to use a Heap. It can
	 * also be used to test the functionality of a Heap.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main(String[] args) {

		//////////////////////////////////////////////////////////////////
		//                      Usage example (1).                      //
		//////////////////////////////////////////////////////////////////

		// create an array of objects to store in the heap
		Object[] array = createTestArray();

		// create a comparator that compares the objects by comparing
		// their Integers
		Comparator<Object> comparator = new Comparator<Object>() {
			public int compare(Object o1, Object o2) {
				return ((Integer)((Object[])o1)[0]).intValue() - ((Integer)((Object[])o2)[0]).intValue();
			}
		};
		// create a heap that is initialized with the array (the heap has
		// maximum capacity of 5 elements (array.length) and contains 5
		// elements) and that uses the given comparator
		TestHeap<Object> heap = new TestHeap<Object>(array, comparator);
		// open the heap
		heap.open();
		// print the elements of the heap
		while (!heap.isEmpty()) {
			Object[] o = (Object[])heap.dequeue();
			System.out.println("Integer = "+o[0]+" & String = "+o[1]);
		}
		System.out.println();
		// close the open heap after use
		heap.close();
		
		//////////////////////////////////////////////////////////////////
		//                      Usage example (2).                      //
		//////////////////////////////////////////////////////////////////

		// refresh the array (it was internally used into the heap and has
		// changed)
		array = createTestArray();
		
		// create a heap that is initialized with the first three elements
		// of the array (the heap has a maximum capacity of 5 elements
		// (array.length) and contains 3 elements) and that uses the given
		// comparator
		heap = new TestHeap<Object>(array, 3, comparator);
		// open the heap
		heap.open();
		// print the elements of the heap
		while (!heap.isEmpty()) {
			Object[] o = (Object[])heap.dequeue();
			System.out.println("Integer = "+o[0]+" & String = "+o[1]);
		}
		System.out.println();
		// close the open heap after use
		heap.close();
		
		//////////////////////////////////////////////////////////////////
		//                      Usage example (3).                      //
		//////////////////////////////////////////////////////////////////
		
		// refresh the array (it was internally used into the heap and has
		// changed)
		array = createTestArray();
		
		// create an empty heap with a maximum capacity of 5 elements and
		// that uses the given comparator
		heap = new TestHeap<Object>(5, comparator);
		// open the heap
		heap.open();
		// generate an iteration over the elements of the given array
		xxl.core.cursors.Cursor<? extends Object> cursor = new xxl.core.cursors.sources.ArrayCursor<Object>(array);
		// insert all elements of the given iterator
		for (; cursor.hasNext(); heap.enqueue(cursor.next()));
		// print the elements of the heap
		while (!heap.isEmpty()) {
			Object[] o = (Object[])heap.dequeue();
			System.out.println("Integer = "+o[0]+" & String = "+o[1]);
		}
		System.out.println();
		// close the open heap and cursor after use
		heap.close();
		cursor.close();
		
		//////////////////////////////////////////////////////////////////
		//                      Usage example (4).                      //
		//////////////////////////////////////////////////////////////////
		TestHeap<Integer> heap2 = new TestHeap<Integer>(1000, ComparableComparator.INTEGER_COMPARATOR);
		Cursor<Integer> cursor2 = new xxl.core.cursors.sources.DiscreteRandomNumber(1000);
		while(cursor2.hasNext()) {
			heap2.enqueue(cursor2.next());
			heap2.testHeap();
		}
		cursor.close();
		
		testIntegerHeapOutput(heap2, false);
		
		//////////////////////////////////////////////////////////////////
		//                      Usage example (5).                      //
		//////////////////////////////////////////////////////////////////
		System.out.println("Test remove on the cursor of the heap (1)");
		
		heap2 = new TestHeap<Integer>(1000, ComparableComparator.INTEGER_COMPARATOR);
		cursor2 = new xxl.core.cursors.sources.DiscreteRandomNumber(new xxl.core.util.random.JavaDiscreteRandomWrapper(0l), 1000);
		while(cursor2.hasNext()) {
			heap2.enqueue(cursor2.next());
			heap2.testHeap();
		}
		cursor2.close();
		
		java.util.Random r = new java.util.Random(0); // System.currentTimeMillis());
		while(!heap.isEmpty()) {
			Cursor<Integer> c = heap2.cursor();
			int number = (int) (r.nextDouble()*heap.size);
			xxl.core.cursors.Cursors.nth(c, number);
			c.remove();
			c.close();
			heap.testHeap();
		}
		System.out.println("Succeeded.");
		
		//////////////////////////////////////////////////////////////////
		//                      Usage example (6).                      //
		//////////////////////////////////////////////////////////////////
		System.out.println("Test remove on the cursor of the heap (2)");
		
		heap2 = new TestHeap<Integer>(1000, ComparableComparator.INTEGER_COMPARATOR);
		cursor2 = new xxl.core.cursors.sources.Permutator(1000, r);
		while(cursor2.hasNext()) {
			heap2.enqueue(cursor2.next());
			heap2.testHeap();
		}
		cursor2.close();
		
		Cursor<Integer> removeObjects = new xxl.core.cursors.sources.Permutator(1000, r);
		
		while(!heap.isEmpty()) {
			Integer removeObject = removeObjects.next();
			
			Cursor<Integer> c = new xxl.core.cursors.filters.Filter<Integer>(
				heap2.cursor(),
				new xxl.core.predicates.LeftBind<Integer>(
					new Equal<Integer>(),
					removeObject
				)
			);
			if (!c.hasNext())
				throw new RuntimeException("Element not found for removal");
			c.next();
			c.remove();
			c.close();
			heap.testHeap();
		}
		System.out.println("Succeeded.");
		
		//////////////////////////////////////////////////////////////////
		//                      Usage example (7).                      //
		//////////////////////////////////////////////////////////////////
		System.out.println("Test update on the cursor of the heap (value*2)");
		
		heap2 = new TestHeap<Integer>(1000, ComparableComparator.INTEGER_COMPARATOR);
		cursor2 = new xxl.core.cursors.sources.Permutator(1000, r);
		while(cursor2.hasNext()) {
			heap2.enqueue(cursor2.next());
			heap2.testHeap();
		}
		cursor2.close();
		
		Cursor<Integer> updateObjects = new xxl.core.cursors.sources.Permutator(1000, r);
		
		while(updateObjects.hasNext()) {
			Integer updateObject = updateObjects.next();
			
			Cursor<Integer> c = new xxl.core.cursors.filters.Filter<Integer>(
				heap2.cursor(),
				new xxl.core.predicates.LeftBind<Integer>(
					new Equal<Integer>(),
					updateObject
				)
			);
			if (!c.hasNext())
				throw new RuntimeException("Element not found for update");
			Integer i = c.next();
			c.update(new Integer(i.intValue()*2));
			
			try {
				// if this does not lead to an exception ...
				if (c.hasNext())
					c.next();
				// ... something is wrong!
				throw new RuntimeException("Concurrent modification has not been recognized");
			}
			catch (ConcurrentModificationException e) {
				// ok!
			}
			c.close();
			heap.testHeap();
		}
		testIntegerHeapOutput(heap2, false);
		
		System.out.println("Succeeded.");
	}

}
