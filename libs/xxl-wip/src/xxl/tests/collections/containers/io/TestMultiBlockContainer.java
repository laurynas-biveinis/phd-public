package xxl.tests.collections.containers.io;

import java.io.IOException;

import xxl.core.collections.containers.io.MultiBlockContainer;
import xxl.core.io.Block;
import xxl.core.io.converters.DoubleConverter;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class MultiBlockContainer.
 */
public class TestMultiBlockContainer {

	/**
	 * The main method contains some examples how to use a
	 * MultiBlockContainer. It can also be used to test the functionality
	 * of a MultiBlockContainer.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main (String [] args) {

		//////////////////////////////////////////////////////////////////
		//                      Usage example (1).                      //
		//////////////////////////////////////////////////////////////////
		
		int i;
		java.util.Iterator iterator;
		
		// create a new multi block container with ...
		MultiBlockContainer container = new MultiBlockContainer(
			// files having the file name "MultiBlockContainer"
			xxl.core.util.XXLSystem.getOutPath()+System.getProperty("file.separator")+"MultiBlockContainer",
			// a block size of 11 bytes (8 bytes for the longs pointing to
			// the next container and 3 bytes for data)
			11 // orig:11
		);
		
		System.out.println("Insert 10 Doubles into a MultiBlockContainer");
		// insert 10 blocks containing double values (8 bytes)
		for (i=1; i<=10; i++) {
			// create a new block of 8 bytes
			Block block = new Block(8);
			// catch IOExceptions
			try {
				// write the value of i*pi to the block
				DoubleConverter.DEFAULT_INSTANCE.writeDouble(block.dataOutputStream(), i*Math.PI);
				System.out.println(DoubleConverter.DEFAULT_INSTANCE.readDouble(block.dataInputStream()));
			}
			catch (IOException ioe) {
				System.out.println("An I/O error occured.");
			}
			// insert the block into the multi block container
			container.insert(block);
		}

		System.out.println("Output of the MultiBlockContainer");
		// get the ids of all elements in the container
		
		iterator = container.ids();
		// print all elements of the container
		i=1;
		while (iterator.hasNext()) {
			// get the block from the container
			Object next = iterator.next();
			Block block = (Block)container.get(next);
			// catch IOExceptions
			try {
				// print the data of the block
				double d = DoubleConverter.DEFAULT_INSTANCE.readDouble(block.dataInputStream());
				System.out.println(d);
				double distance = d-i*Math.PI;
				
				if ( ! (Math.abs(distance) <= 0.0001) ) // Achtung: distance kann NaN sein! deshalb mit not geloest!
					throw new RuntimeException("Not the correct values reconstructed!");
			}
			catch (IOException ioe) {
				System.out.println("An I/O error occured.");
			}
			i++;
		}
		if (i!=10+1)
			throw new RuntimeException("Not the correct number of elements");

		System.out.println("Remove five blocks in random order");
		iterator = new xxl.core.cursors.filters.Taker(new xxl.core.cursors.sorters.ShuffleCursor(container.ids()),5);
		
		// print all elements of the container
		while (iterator.hasNext())
			container.remove(iterator.next());

		System.out.println("Output the rest of the elements");

		iterator = container.ids();
		// print all elements of the container
		i=1;
		while (iterator.hasNext()) {
			// get the block from the container
			Object next = iterator.next();
			Block block = (Block)container.get(next);
			// catch IOExceptions
			try {
				// print the data of the block
				double d = DoubleConverter.DEFAULT_INSTANCE.readDouble(block.dataInputStream());
				System.out.println(d);
				double factor = d/Math.PI;
				
				if ( ! (Math.abs(factor-Math.rint(factor)) <= 0.0001) ) // Achtung: distance kann NaN sein! deshalb mit not geloest!
					throw new RuntimeException("Not the correct values reconstructed!");
			}
			catch (IOException ioe) {
				System.out.println("An I/O error occured.");
			}
			i++;
		}
		if (i!=5+1)
			throw new RuntimeException("Not the correct number of elements");
		
		System.out.println("Insert 100 blocks of increasing length");
		Object ids[] = new Object[100];
		for (i=1; i<=100; i++)
			ids[i-1] = container.insert(new Block(i));

		System.out.println("Checking the length of the blocks");
		for (i=1; i<=100; i++)
			if  (((Block) container.get(ids[i-1])).size != i)
				throw new RuntimeException("The size of the blocks is not correct");
		
		// close the open container and clear its file after use
		container.clear();
		container.close();
		// delete the files of the container
		container.delete();
	}

}
