package xxl.tests.collections.queues;

import java.util.Comparator;
import java.util.Iterator;

import xxl.core.collections.queues.DynamicHeap;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class DynamicHeap.
 */
public class TestDynamicHeap {


	/**
	 * The main method contains some examples how to use a DynamicHeap.
	 * It can also be used to test the functionality of a DynamicHeap.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main (String [] args) {

		//////////////////////////////////////////////////////////////////
		//                      Usage example (1).                      //
		//////////////////////////////////////////////////////////////////

		// create an array of objects to store in the heap
		Object[] array = new Object[] {
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
		// create a comparator that compares the objects by comparing
		// their Integers
		Comparator<Object> comparator = new Comparator<Object>() {
			public int compare(Object o1, Object o2) {
				return ((Integer)((Object[])o1)[0]).intValue() - ((Integer)((Object[])o2)[0]).intValue();
			}
		};
		// create a heap that is initialized with the array and that uses
		// the given comparator
		DynamicHeap<Object> heap = new DynamicHeap<Object>(array, comparator);
		// open the heap
		heap.open();
		// insert two elements
		heap.enqueue(new Object[] {
			new Integer(4),
			new String("first")
		});
		heap.enqueue(new Object[] {
			new Integer(1),
			new String("fourth")
		});
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
		// changed
		array = new Object[] {
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
		// create an empty heap that uses the given comparator
		heap = new DynamicHeap<Object>(comparator);
		// open the heap
		heap.open();
		// generate an iteration over the elements of the given array
		Iterator<Object> iterator = new xxl.core.cursors.sources.ArrayCursor<Object>(array);
		// insert all elements of the given iterator
		for (; iterator.hasNext(); heap.enqueue(iterator.next()));
		// print the elements of the heap
		while (!heap.isEmpty()) {
			Object[] o = (Object[])heap.dequeue();
			System.out.println("Integer = "+o[0]+" & String = "+o[1]);
		}
		System.out.println();
		// close the open heap after use
		heap.close();
	}

}
