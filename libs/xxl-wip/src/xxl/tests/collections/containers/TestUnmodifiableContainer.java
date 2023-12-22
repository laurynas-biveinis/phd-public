package xxl.tests.collections.containers;

import java.util.Iterator;

import xxl.core.collections.containers.MapContainer;
import xxl.core.collections.containers.UnmodifiableContainer;
import xxl.core.cursors.Cursor;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class UnmodifiableContainer.
 */
public class TestUnmodifiableContainer {

	/**
	 * The main method contains some examples how to use an
	 * UnmodifiableContainer. It can also be used to test the
	 * functionality of an UnmodifiableContainer.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main (String [] args) {

		//////////////////////////////////////////////////////////////////
		//                      Usage example (1).                      //
		//////////////////////////////////////////////////////////////////

		// create a new container
		MapContainer inputContainer = new MapContainer();
		// create an iteration over 20 random Integers (between 0 and 100)
		Iterator iterator = new xxl.core.cursors.sources.DiscreteRandomNumber(new xxl.core.util.random.JavaDiscreteRandomWrapper(100), 20);
		// insert all elements of the given iterator
		while (iterator.hasNext())
			inputContainer.insert(iterator.next());
		// create a new unmodifiable container with the given container
		UnmodifiableContainer container = new UnmodifiableContainer(inputContainer);
		// generate a cursor that iterates over all elements of the container
		Cursor cursor = container.objects();
		// print all elements of the cursor (container)
		while (cursor.hasNext())
			System.out.println(cursor.next());
		System.out.println();
		// close the open iterator, cursor and container after use
		((Cursor)iterator).close();
		cursor.close();
		container.close();
	}

}
