package xxl.tests.collections.containers;

import xxl.core.collections.containers.EmptyContainer;
import xxl.core.cursors.Cursor;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class EmptyContainer.
 */
public class TestEmptyContainer {

	/**
	 * The main method contains some examples how to use an
	 * EmptyContainer. It can also be used to test the functionality of an
	 * EmptyContainer.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main (String [] args) {

		//////////////////////////////////////////////////////////////////
		//                      Usage example (1).                      //
		//////////////////////////////////////////////////////////////////

		// create a new empty container
		EmptyContainer container = EmptyContainer.DEFAULT_INSTANCE;
		// println the number of elements contained by the container
		System.out.println(container.size());
		// create an iteration over 20 random Integers (between 0 and 100)
		Cursor cursor = new xxl.core.cursors.sources.DiscreteRandomNumber(new xxl.core.util.random.JavaDiscreteRandomWrapper(100), 20);
		// catch UnsupportedOperationExceptions
		try {
			// insert all elements of the given iterator
			while (cursor.hasNext())
				container.insert(cursor.next());
		}
		catch (UnsupportedOperationException uoe) {
			System.out.println(uoe.getMessage());
		}
		// println the number of elements contained by the container
		System.out.println(container.size());
		System.out.println();
		// close the open bag and cursor after use
		container.close();
		cursor.close();
	}

}
