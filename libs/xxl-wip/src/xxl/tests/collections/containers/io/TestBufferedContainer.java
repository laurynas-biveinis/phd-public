package xxl.tests.collections.containers.io;

import java.util.Iterator;

import xxl.core.collections.containers.io.BufferedContainer;
import xxl.core.io.LRUBuffer;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class BufferedContainer.
 */
public class TestBufferedContainer {

	/**
	 * The main method contains some examples how to use a
	 * BufferedContainer. It can also be used to test the functionality
	 * of a BufferedContainer.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main (String [] args) {

		//////////////////////////////////////////////////////////////////
		//                      Usage example (1).                      //
		//////////////////////////////////////////////////////////////////

		// create a new buffered container with ...
		BufferedContainer container = new BufferedContainer(
			// a map container that stores the elements
			new xxl.core.collections.containers.MapContainer(),
			// a LRU buffer with 5 slots
			new LRUBuffer(5)
		);
		// create an iteration over 20 Integers 0-19
		Iterator iterator = new xxl.core.cursors.sources.Enumerator(20);
		// insert all elements of the given iterator
		iterator = container.insertAll(iterator);
		// print all elements of the container
		while (iterator.hasNext())
			System.out.println(container.get(iterator.next()));
		System.out.println();
		// get the ids of the elements of the container
		iterator = container.ids();
		// remove 5 elements
		for (int i = 0; i<5 && iterator.hasNext(); i++) {
			container.remove(iterator.next());
			// refresh the iterator (cause it can be in an invalid state)
			iterator = container.ids();
		}
		// fix 5 elements in the buffer
		for (int i = 0; i<5 && iterator.hasNext(); i++)
			container.get(iterator.next(), false);
		// try to access another element (the buffer will overrun)
		try {
			container.get(iterator.next());
		}
		catch (Exception e) {
			System.out.println("The buffer overflows (as expected), "+
				"because it contains 5 elements but 6 elements were tried to fix");
		}
		// flushes the whole buffer
		container.flush();
		
		System.out.println("Get the objects of the container (via ids and get)");
		iterator = container.ids();
		// print all elements of the queue
		while (iterator.hasNext())
			System.out.println(container.get(iterator.next()));
		System.out.println();
		
		System.out.println("Get the objects of the container (with the objects method)");
		iterator = container.objects();
		// print all elements of the queue
		while (iterator.hasNext())
			System.out.println(iterator.next());
		System.out.println();
		
		// close the open queue after use
		container.close();
	}

}
