package xxl.tests.collections.containers.io;

import java.io.IOException;
import java.util.Iterator;

import xxl.core.collections.containers.io.RawAccessContainer;
import xxl.core.io.Block;
import xxl.core.io.raw.RawAccess;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class RawAccessContainer.
 */
public class TestRawAccessContainer {

	/**
	 * The main method contains some examples how to use a
	 * BlockFileContainer. It can also be used to test the functionality
	 * of a BlockFileContainer.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main (String [] args) {
		
		//////////////////////////////////////////////////////////////////
		//                      Usage example (1).                      //
		//////////////////////////////////////////////////////////////////
		
		RawAccess ra = new xxl.core.io.raw.RAMRawAccess(200,32);
		// create a new block file container with ...
		RawAccessContainer container = new RawAccessContainer(ra, 100);
		// insert 10 blocks containing the integers between 0 and 9
		System.out.println("Insertion");
		for (int i = 0; i<10; i++) {
			System.out.println(i);
			// create a new block
			Block block = new Block(32);
			// catch IOExceptions
			try {
				// write the value of i to the block
				block.dataOutputStream().write(i);
			}
			catch (IOException ioe) {
				System.out.println("An I/O error occured.");
			}
			// insert the block into the block file container
			container.insert(block);
		}
		System.out.println("Retrieval");
		// get the ids of all elements in the container
		Iterator iterator = container.ids();
		// print all elements of the container
		while (iterator.hasNext()) {
			// get the block from the container
			Block block = (Block)container.get(iterator.next());
			// catch IOExceptions
			try {
				// print the data of the block
				System.out.println(block.dataInputStream().read());
			}
			catch (IOException ioe) {
				System.out.println("An I/O error occured.");
			}
		}
		System.out.println("Remove element 5");
		container.remove(new Long(5));
		System.out.println("Close container");
		
		// close the open container and clear its file after use
		container.close();
		container = new RawAccessContainer(ra);
		System.out.println("Reopening Container");
		System.out.println("Retrieval");
		// get the ids of all elements in the container
		iterator = container.ids();
		// print all elements of the container
		while (iterator.hasNext()) {
			// get the block from the container
			Block block = (Block)container.get(iterator.next());
			// catch IOExceptions
			try {
				// print the data of the block
				System.out.println(block.dataInputStream().read());
			}
			catch (IOException ioe) {
				System.out.println("An I/O error occured.");
			}
		}
		
		container.clear();
		container.close();
	}

}
