package xxl.tests.collections.bags;

import xxl.core.collections.bags.ListBag;
import xxl.core.cursors.Cursor;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class ListBag.
 */
public class TestListBag {

	/**
	 * The main method contains some examples how to use a ListBag. It can also
	 * be used to test the functionality of a ListBag.
	 *
	 * @param args array of <code>String</code> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main (String [] args) {

		//////////////////////////////////////////////////////////////////
		//                      Usage example (1).                      //
		//////////////////////////////////////////////////////////////////

		// create a new list bag (that uses a linked list to store its
		// elements per default)
		ListBag<Integer> bag = new ListBag<Integer>();
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
		// create a new list bag (that uses a linked list to store its
		// elements per default) that contains all elements of the given
		// iterator
		bag = new ListBag<Integer>(iterator);
		// create a cursor that iterates over the elements of the bag
		cursor = bag.cursor();
		// remove every even Integer from the cursor (and the underlying
		// list bag)
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

		//////////////////////////////////////////////////////////////////
		//                      Usage example (3).                      //
		//////////////////////////////////////////////////////////////////

		// create a new list bag that uses an array list to store its
		// elements
		bag = new ListBag<Integer>(new java.util.ArrayList<Integer>());
		// create an iteration over 20 random Integers (between 0 and 100)
		iterator = new xxl.core.cursors.sources.DiscreteRandomNumber(new xxl.core.util.random.JavaDiscreteRandomWrapper(100), 20);
		// insert all elements of the given iterator
		bag.insertAll(iterator);
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
