package xxl.tests.collections.containers.io;

import java.io.IOException;
import java.util.Iterator;

import xxl.core.collections.containers.io.BlockFileContainer;
import xxl.core.io.Block;
import xxl.core.util.WrappingRuntimeException;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class BlockFileContainer.
 */
public class TestBlockFileContainer {

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

		// create a new block file container with ...
		BlockFileContainer container = new BlockFileContainer(
			// files having the file name "BlockFileContainer"
			xxl.core.util.XXLSystem.getOutPath()+System.getProperty("file.separator")+"BlockFileContainer",
			// a block size of 4 bytes (size of a serialized integer)
			4
		);
		// insert 10 blocks containing the integers between 0 and 9
		for (int i = 0; i<10; i++) {
			// create a new block
			Block block = new Block(4);
			try {
				// write the value of i to the block
				block.dataOutputStream().write(i);
			}
			catch (IOException ioe) {
				throw new WrappingRuntimeException(ioe);
			}
			// insert the block into the block file container
			container.insert(block);
		}
		// get the ids of all elements in the container
		Iterator iterator = container.ids();
		// print all elements of the container
		while (iterator.hasNext()) {
			// get the block from the container
			Block block = (Block)container.get(iterator.next());
			try {
				// print the data of the block
				System.out.println(block.dataInputStream().read());
			}
			catch (IOException ioe) {
				throw new WrappingRuntimeException(ioe);
			}
		}
		System.out.println();
		container.close();
		
		System.out.println("Reopening the BFC");
		
		// close the open container and clear its file after use
		container = new BlockFileContainer(
			// files having the file name "BlockFileContainer"
			xxl.core.util.XXLSystem.getOutPath()+System.getProperty("file.separator")+"BlockFileContainer"
			// a block size of 4 bytes (size of a serialized integer)
		);
		
		// get the ids of all elements in the container
		iterator = container.ids();
		// print all elements of the container
		while (iterator.hasNext()) {
			// get the block from the container
			Block block = (Block)container.get(iterator.next());
			try {
				// print the data of the block
				System.out.println(block.dataInputStream().read());
			}
			catch (IOException ioe) {
				throw new WrappingRuntimeException(ioe);
			}
		}
		System.out.println();
		
		container.clear();
		container.close();
		container.delete();
	}

}
