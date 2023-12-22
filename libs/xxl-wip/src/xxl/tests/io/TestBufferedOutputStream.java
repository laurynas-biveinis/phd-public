package xxl.tests.io;

import java.io.IOException;

import xxl.core.functions.AbstractFunction;
import xxl.core.functions.Function;
import xxl.core.io.Block;
import xxl.core.io.BufferedOutputStream;
import xxl.core.io.LRUBuffer;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class BufferedOutputStream.
 */
public class TestBufferedOutputStream {

	/**
	 * The main method contains some examples how to use a
	 * BufferedOutputStream. It can also be used to test the functionality
	 * of a BufferedOutputStream.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main (String [] args) {

		//////////////////////////////////////////////////////////////////
		//                      Usage example (1).                      //
		//////////////////////////////////////////////////////////////////

		// catch IOExceptions
		try {
			// create a new byte array output stream
			java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
			// create a new LRU buffer with 5 slots
			LRUBuffer buffer = new LRUBuffer(5);
			// create a new block factory method
			Function newBlock = new AbstractFunction() {
				public Object invoke() {
					return new Block(new byte [20]);
				}
			};
			// buffer the output stream
			BufferedOutputStream buffered = new BufferedOutputStream(output, buffer, newBlock);
			// create a new data output stream
			java.io.DataOutputStream dataOutput = new java.io.DataOutputStream(buffered);
			// write data to the data output stream
			dataOutput.writeUTF("Some data!");
			dataOutput.writeUTF("More data!");
			dataOutput.writeUTF("Another bundle of data!");
			dataOutput.writeUTF("The last bundle of data!");
			// flush the buffered output stream
			buffered.flush();
			// create a byte array input stream on the output stream
			java.io.ByteArrayInputStream input = new java.io.ByteArrayInputStream(output.toByteArray());
			// create a new data input stream
			java.io.DataInputStream dataInput = new java.io.DataInputStream(input);
			// print the data of the data input stream
			while (dataInput.available()>0)
				System.out.println(dataInput.readUTF());
			// close the open streams
			dataOutput.close();
			dataInput.close();
		}
		catch (IOException ioe) {
			System.out.println("An I/O error occured.");
		}
		System.out.println();
	}

}
