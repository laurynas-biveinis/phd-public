package xxl.tests.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import xxl.core.io.Block;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class Block.
 */
public class TestBlock {

	/**
	 * The main method contains some examples how to use a Block. It can
	 * also be used to test the functionality of a Block.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main (String [] args) {

		//////////////////////////////////////////////////////////////////
		//                      Usage example (1).                      //
		//////////////////////////////////////////////////////////////////

		// create a new block of five bytes
		Block block = new Block(5);
		// catch IOExceptions
		try {
			// open a data output stream on the block
			DataOutputStream output = block.dataOutputStream();
			// write the bytes 0 to 4 to the data output stream
			for (int i = 0; i<5; i++)
				output.write(i);
			// invert every bit of the block
			for (int i = 0; i<block.size; i++)
				block.set(i, block.get(i));
			// open a data input stream on the block
			DataInputStream input = block.dataInputStream();
			// print the five bytes contained by the block
			while (input.available()>0)
				System.out.println(input.read());
			
			Block b2 = (Block) block.clone();
			if (b2.array==block.array)
				throw new RuntimeException("Array has not been cloned");
			if (!b2.equals(block))
				throw new RuntimeException("Cloned objects are not equal");
			b2.set(1,(byte) 42);
			if (b2.equals(block))
				throw new RuntimeException("Something is wrong here");
			System.out.println("clone-method has been tested and works.");
			
			block = new Block(512);
			for (int i=0; i<512; i++)
				block.set(i, (byte) (i%64));
			System.out.println("A new Block");
			System.out.println(block);

			Block compressedBlock = block.compress();

			System.out.println("The compressed Block");
			System.out.println(compressedBlock);

			block = compressedBlock.decompress();
			System.out.println("Again decompressed");
			System.out.println(block);
		}
		catch (Exception e) {
			throw new xxl.core.util.WrappingRuntimeException(e);
		}
		// release the block
		block.release();
	}

}
