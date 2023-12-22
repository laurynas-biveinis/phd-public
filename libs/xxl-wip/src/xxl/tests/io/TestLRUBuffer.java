package xxl.tests.io;

import xxl.core.io.LRUBuffer;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class LRUBuffer.
 */
public class TestLRUBuffer {

	/**
	 * The main method contains some examples how to use a LRUBuffer. It can
	 * also be used to test the functionality of a LRUBuffer.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to submit
	 *        parameters when the main method is called.
	 */
	public static void main(String[] args) {
		//////////////////////////////////////////////////////////////////
		//                      Usage example (1).                      //
		//////////////////////////////////////////////////////////////////
		
		// create a new owner
		String owner = "owner";
		// create a new LRU buffer with a capacity of 5 objects
		LRUBuffer<String, Integer, Integer> buffer = new LRUBuffer<String, Integer, Integer>(5);
		// create a new iterator with 100 integers between 0 and 5
		java.util.Iterator<Integer> iterator = new xxl.core.cursors.sources.DiscreteRandomNumber(new xxl.core.util.random.JavaDiscreteRandomWrapper(6), 100);
		// insert all elements of the iterator with a flush function that
		// prints the flushed integer into the buffer
		while (iterator.hasNext()) {
			Integer i = iterator.next();
			System.out.println("insert " + i);
			buffer.update(
				owner,
				i,
				i,
				new xxl.core.functions.AbstractFunction<Object, Object>() {
					public Object invoke(Object o1, Object o2) {
						System.out.println("flush " + o1);
						return o1;
					}
				},
				true
			);
		}
		buffer.checkBuffer();
		System.out.println();
	}

}
