package xxl.tests.collections.bags;

import java.util.Iterator;

import xxl.core.collections.bags.ContainerBag;
import xxl.core.collections.containers.Container;
import xxl.core.collections.containers.MapContainer;
import xxl.core.cursors.Cursor;
import xxl.core.cursors.Cursors;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class ContainerBag.
 */
public class TestContainerBag {

	/**
	 * The main method contains some examples how to use a ContainerBag.
	 * It can also be used to test the functionality of a ContainerBag.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main (String [] args) {

		//////////////////////////////////////////////////////////////////
		//                      Usage example (1).                      //
		//////////////////////////////////////////////////////////////////

		// create a new container bag
		ContainerBag bag = new ContainerBag();
		// create an iteration over 20 random Integers (between 0 and 100)
		Iterator iterator = new xxl.core.cursors.sources.DiscreteRandomNumber(new xxl.core.util.random.JavaDiscreteRandomWrapper(100), 20);
		// insert all elements of the given iterator
		bag.insertAll(iterator);
		// create a cursor that iterates over the elements of the bag
		Cursor cursor = bag.cursor();
		// print all elements of the cursor (bag)
		while (cursor.hasNext())
			System.out.println(cursor.next());
		System.out.println();
		// close the open iterator, cursor and bag after use
		((Cursor)iterator).close();
		cursor.close();
		bag.close();

		//////////////////////////////////////////////////////////////////
		//                      Usage example (2).                      //
		//////////////////////////////////////////////////////////////////

		// create a new container
		Container container = new MapContainer();
		// create an iteration over the Integer between 0 and 19
		iterator = new xxl.core.cursors.sources.Enumerator(20);
		// insert all elements of the iterator into the container
		Cursors.consume(container.insertAll(iterator));
		// create a new container bag that contains all elements of the
		// container
		bag = new ContainerBag(container);
		// create a cursor that iterates over the elements of the bag
		cursor = bag.cursor();
		// remove every even Integer from the cursor (and the underlying
		// container bag)
		while (cursor.hasNext()) {
			int i = ((Integer)cursor.next()).intValue();
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
		((Cursor)iterator).close();
		cursor.close();
		bag.close();
	}

}
