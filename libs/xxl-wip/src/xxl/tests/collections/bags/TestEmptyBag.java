package xxl.tests.collections.bags;

import xxl.core.collections.bags.EmptyBag;
import xxl.core.cursors.Cursor;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class EmptyBag.
 */
public class TestEmptyBag {

	/**
	 * The main method contains some examples how to use an EmptyBag. It
	 * can also be used to test the functionality of an EmptyBag.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main (String [] args) {

		//////////////////////////////////////////////////////////////////
		//                      Usage example (1).                      //
		//////////////////////////////////////////////////////////////////

		// create a new empty bag
		EmptyBag bag = EmptyBag.DEFAULT_INSTANCE;
		// println the number of elements contained by the bag
		System.out.println(bag.size());
		// create an iteration over 20 random Integers (between 0 and 100)
		Cursor cursor = new xxl.core.cursors.sources.DiscreteRandomNumber(new xxl.core.util.random.JavaDiscreteRandomWrapper(100), 20);
		// catch UnsupportedOperationExceptions
		try {
			// insert all elements of the given iterator
			bag.insertAll(cursor);
		}
		catch (UnsupportedOperationException uoe) {
			System.out.println(uoe.getMessage());
		}
		// println the number of elements contained by the bag
		System.out.println(bag.size());
		System.out.println();
		// close the open bag and cursor after use
		bag.close();
		cursor.close();
	}

}
