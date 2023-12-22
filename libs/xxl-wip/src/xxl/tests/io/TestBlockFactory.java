package xxl.tests.io;

import xxl.core.io.Block;
import xxl.core.io.BlockFactory;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class BlockFactory.
 */
public class TestBlockFactory {

	/**
	 * The main method contains some examples how to use a BlockFactory.
	 * It can also be used to test the functionality of a BlockFactory.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main (String [] args) {

		//////////////////////////////////////////////////////////////////
		//                      Usage example (1).                      //
		//////////////////////////////////////////////////////////////////

		// create a new byte array of 100 bytes
		byte [] array = new byte [100];
		// create a new block factory that creates block of 20 bytes
		BlockFactory factory = new BlockFactory(array, 20);
		// create a new array of 6 blocks
		Block [] blocks = new Block [6];
		// try to allocate 6 blocks
		for (int i = 0; i<6; i++) {
			blocks[i] = factory.allocate();
			if (blocks[i]==null) {
				System.out.println("array is exhausted");
				break;
			}
			else
				System.out.println("block allocated");
		}
		// release a block
		blocks[0].release();
		// try again to allocate the sixth block
		blocks[5] = factory.allocate();
		if (blocks[5]==null)
			System.out.println("array is exhausted");
		else
			System.out.println("block allocated");
		// release the rest of the blocks
		for (int i = 1; i<6; i++)
			blocks[i].release();
		System.out.println();
	}

}
