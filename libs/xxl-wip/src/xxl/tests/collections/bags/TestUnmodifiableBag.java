package xxl.tests.collections.bags;

import java.util.Iterator;

import xxl.core.collections.bags.ListBag;
import xxl.core.collections.bags.UnmodifiableBag;
import xxl.core.cursors.Cursor;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class UnmodifiableBag.
 */
public class TestUnmodifiableBag {

	/**
	 * The main method contains some examples how to use an
	 * UnmodifiableBag. It can also be used to test the functionality of
	 * an UnmodifiableBag.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main (String [] args) {

		//////////////////////////////////////////////////////////////////
		//                      Usage example (1).                      //
		//////////////////////////////////////////////////////////////////

		// create a new bag
		ListBag inputBag = new ListBag();
		// create an iteration over 20 random Integers (between 0 and 100)
		Iterator iterator = new xxl.core.cursors.sources.DiscreteRandomNumber(new xxl.core.util.random.JavaDiscreteRandomWrapper(100), 20);
		// insert all elements of the given iterator
		inputBag.insertAll(iterator);
		// create a new unmodifiable bag with the given bag
		UnmodifiableBag bag = new UnmodifiableBag(inputBag);
		// generate a cursor that iterates over all elements of the bag
		Cursor cursor = bag.cursor();
		// print all elements of the cursor (bag)
		while (cursor.hasNext())
			System.out.println(cursor.next());
		System.out.println();
		// close the open iterator, cursor and bag after use
		((Cursor)iterator).close();
		cursor.close();
		bag.close();
	}

}
