package xxl.tests.collections.containers.io;

import xxl.core.collections.containers.io.ConverterContainer;
import xxl.core.io.converters.IntegerConverter;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class ConverterContainer.
 */
public class TestConverterContainer {

	/**
	 * The main method contains some examples how to use a
	 * ConverterContainer. It can also be used to test the functionality
	 * of a ConverterContainer.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main (String [] args) {

		//////////////////////////////////////////////////////////////////
		//                      Usage example (1).                      //
		//////////////////////////////////////////////////////////////////

		// create a new coverter container with ...
		ConverterContainer container = new ConverterContainer(
			// a map container that stores the elements
			new xxl.core.collections.containers.MapContainer(),
			// an integer converter for converting the elements
			IntegerConverter.DEFAULT_INSTANCE
		);
		// create an iteration over 20 random Integers (between 0 and 100)
		java.util.Iterator iterator = new xxl.core.cursors.sources.Enumerator(20);
		// insert all elements of the given iterator
		iterator = container.insertAll(iterator);
		// print all elements of the queue
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
		// update 5 elements
		for (int i = 0; i<5 && iterator.hasNext(); i++) {
			Object id = iterator.next();
			container.update(id, new Integer(((Integer)container.get(id)).intValue()+100));
		}
		// get the ids of all elements of the container
		iterator = container.ids();
		// print all elements of the queue
		while (iterator.hasNext())
			System.out.println(container.get(iterator.next()));
		System.out.println();
		// close the open queue after use
		container.close();
	}

}
