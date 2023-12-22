package xxl.tests.collections.bags;

import xxl.core.collections.bags.ArrayBag;
import xxl.core.cursors.Cursor;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class ArrayBag.
 */
public class TestArrayBag {

	/**
	 * The main method contains some examples how to use an ArrayBag. It can
	 * also be used to test the functionality of an ArrayBag.
	 *
	 * @param args array of <code>String</code> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main(String[] args) {

		//////////////////////////////////////////////////////////////////
		//                      Usage example (1).                      //
		//////////////////////////////////////////////////////////////////

		// create a new array bag
		ArrayBag<Integer> bag = new ArrayBag<Integer>(20);
		// create an iteration over 20 random Integers (between 0 and 100)
		Cursor<Integer> iterator = new xxl.core.cursors.sources.DiscreteRandomNumber(new xxl.core.util.random.JavaDiscreteRandomWrapper(100), 20);
		// insert all elements of the given iterator
		bag.insertAll(iterator);
		// create a cursor that iterates over the elements of the bag
		Cursor<Integer> cursor = bag.cursor();
		// print all elements of the cursor (bag)
		while (cursor.hasNext())
			System.out.println(cursor.next());
		System.out.println();
		// close the open iterator, cursor and bag after use
		iterator.close();
		cursor.close();
		bag.close();

		//////////////////////////////////////////////////////////////////
		//                      Usage example (2).                      //
		//////////////////////////////////////////////////////////////////

		// create an iteration over the Integer between 0 and 19
		iterator = new xxl.core.cursors.sources.Enumerator(20);
		// create a new array bag that contains all elements of the given
		// iterator
		bag = new ArrayBag<Integer>(20);
		// add all elements
		bag.insertAll(iterator);
		// create a cursor that iterates over the elements of the bag
		cursor = bag.cursor();
		// remove every even Integer from the cursor (and the underlying
		// array bag)
		while (cursor.hasNext()) {
			int i = cursor.next();
			if (i%2 == 0)
				cursor.remove();
		}
		// create a cursor that iterates over the elements of the bag
		cursor = bag.cursor();
		// print all elements of the cursor (bag)
		while (cursor.hasNext())
			System.out.println(cursor.next());
		System.out.println();
		// close the open iterator, cursor and bag after use
		iterator.close();
		cursor.close();
		bag.close();
	}

}
