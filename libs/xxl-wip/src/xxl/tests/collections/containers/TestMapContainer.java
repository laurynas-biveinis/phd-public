package xxl.tests.collections.containers;

import java.util.Iterator;

import xxl.core.collections.containers.MapContainer;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class MapContainer.
 */
public class TestMapContainer {

	/**
	 * The main method contains some examples how to use a MapContainer.
	 * It can also be used to test the functionality of a MapContainer.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main (String [] args) {

		//////////////////////////////////////////////////////////////////
		//                      Usage example (1).                      //
		//////////////////////////////////////////////////////////////////

		// create a new map container (that uses a hash map to store its
		// elements per default)
		MapContainer container = new MapContainer();
		// create an iteration over the Integer between 0 and 19
		Iterator iterator = new xxl.core.cursors.sources.Enumerator(20);
		// insert all elements of the given iterator and save the returned
		// iterator of the ids
		iterator = container.insertAll(iterator);
		System.out.println("This should output the numbers 0 to 19");
		// print all elements of the container
		while (iterator.hasNext())
			System.out.println(container.get(iterator.next()));
		System.out.println();
		// close the open container after use
		container.close();

		//////////////////////////////////////////////////////////////////
		//                      Usage example (2).                      //
		//////////////////////////////////////////////////////////////////

		// create a new map container that uses a tree map to store its
		// elements
		container = new MapContainer(new java.util.TreeMap());
		// create an iteration over the Integer between 0 and 19
		iterator = new xxl.core.cursors.sources.Enumerator(20);
		// insert all elements of the given iterator
		iterator = container.insertAll(iterator);
		// consume iterator in order to insert elements (lazy evaluation)
		while (iterator.hasNext())
			iterator.next();
		// generate an iteration over the ids of the container
		iterator = container.ids();
		// look at every id and ...
		while (iterator.hasNext()) {
			Object id = iterator.next();
			int i = ((Integer)container.get(id)).intValue();
			// remove every Integer that is smaller than 5
			if (i < 5) {
				container.remove(id);
				// update the iteration over the ids because the queue has
				// been modified
				iterator = container.ids();
			}
			else
				// update every odd Integer by multiplying it with 10
				if (i%2 != 0)
					container.update(id, new Integer(i*10));
		}
		// generate an iteration over the objects of the container
		iterator = container.objects();
		// print all elements of the container
		System.out.println("This should output 50, 6, 70, 8, ... (every odd number (5,7,...) is multiplied with 10)");
		while (iterator.hasNext())
			System.out.println(iterator.next());
		System.out.println();
		// close the open container after use
		container.close();

		//////////////////////////////////////////////////////////////////
		//                      Usage example (3).                      //
		//////////////////////////////////////////////////////////////////

		System.out.println("This example shows side effekts when no cloning is used.");
		
		xxl.core.io.Block block;
		Object id;
		
		container = new MapContainer(false);
		block = new xxl.core.io.Block(16);
		id = container.insert(block);
		block.writeLong(0,-1);
		block.writeLong(8,42);
		System.out.println("Without clone: ");
		System.out.println(container.get(id));
		container.close();
		
		container = new MapContainer(true);
		block = new xxl.core.io.Block(16);
		id = container.insert(block);
		block.writeLong(0,-1);
		block.writeLong(8,42);
		System.out.println("With clone: ");
		System.out.println(container.get(id));
		container.close();
	}

}
