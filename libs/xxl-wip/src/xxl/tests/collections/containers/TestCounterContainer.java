package xxl.tests.collections.containers;

import xxl.core.collections.containers.CounterContainer;
import xxl.core.collections.containers.MapContainer;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class CounterContainer.
 */
public class TestCounterContainer {

	/**
	 * The main method contains some examples how to use a
	 * CounterContainer. It can also be used to test the functionality of
	 * a CounterContainer.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main (String [] args) {

		//////////////////////////////////////////////////////////////////
		//                      Usage example (1).                      //
		//////////////////////////////////////////////////////////////////

		// create a new counter container decorating an empty map
		// container
		CounterContainer container
			= new CounterContainer(new MapContainer());
		// reset the counter container
		container.reset();
		// insert 20 elements and print the counters
		for (int i = 0; i < 20; i++)
			container.insert(new Integer(i));
		System.out.println(container);
		System.out.println();

		// create an iteration over the ids of the counter container
		java.util.Iterator iterator = container.ids();
		// get 10 elements and print the counters
		for (int i = 0; i < 10; i++)
			if (iterator.hasNext())
				container.get(iterator.next());
		System.out.println(container);
		System.out.println();

		// update 5 elements and print the counters
		for (int i = 0; i < 5; i++)
			if (iterator.hasNext())
				container.update(iterator.next(), new Integer(i));
		System.out.println(container);
		System.out.println();

		// remove an element and print the counters
		if (iterator.hasNext())
			container.remove(iterator.next());
		System.out.println(container);
		System.out.println();

		// close to open container after use
		container.close();
	}

}
