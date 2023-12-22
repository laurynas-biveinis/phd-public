package xxl.tests.collections.queues.io;

import xxl.core.collections.queues.io.BlockBasedQueue;
import xxl.core.functions.Constant;
import xxl.core.io.converters.IntegerConverter;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class BlockBasedQueue.
 */
public class TestBlockBasedQueue {

	/**
	 * The main method contains some examples how to use a
	 * BlockBasedQueue. It can also be used to test the functionality of a
	 * BlockBasedQueue.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main(String[] args) {

		//////////////////////////////////////////////////////////////////
		//                      Usage example (1).                      //
		//////////////////////////////////////////////////////////////////

		// create a new block based queue with ...
		BlockBasedQueue queue = new BlockBasedQueue(
			// a map container for storing the serialized elements
			new xxl.core.collections.containers.MapContainer(),
			// a block size of 20 bytes
			20,
			// an integer converter
			IntegerConverter.DEFAULT_INSTANCE,
			// an input buffer of 0 bytes
			new Constant(0),
			// an output buffer of 4 bytes (size of a serialized integer)
			new Constant(4)
		);
		// open the queue
		queue.open();
		// create an iteration over 20 random Integers (between 0 and 100)
		java.util.Iterator iterator = new xxl.core.cursors.sources.Enumerator(20);
		// insert all elements of the given iterator
		for (; iterator.hasNext(); queue.enqueue(iterator.next()));
		// print all elements of the queue
		while (!queue.isEmpty())
			System.out.println(queue.dequeue());
		System.out.println();
		// close the open queue after use
		queue.close();
	}

}
