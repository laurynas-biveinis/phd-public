package xxl.tests.io;

import java.io.File;
import java.io.IOException;

import xxl.core.io.BufferedRandomAccessFile;
import xxl.core.io.LRUBuffer;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class BufferedRandomAccessFile.
 */
public class TestBufferedRandomAccessFile {
	/**
	 * The main method contains some examples how to use a
	 * BufferedRandomAccessFile. It can also be used to test the functionality
	 * of a BufferedRandomAccessFile.
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
			// create a new buffered random access file with ...
			BufferedRandomAccessFile file = new BufferedRandomAccessFile(
				// a new file
				new File("file.dat"),
				// read and write access
				"rw",
				// an LRU buffer with 5 slots
				new LRUBuffer(5),
				// a block size of 20 bytes
				20
			); 
			// write data to the data output stream
			file.writeInt(200);
			file.writeUTF("Some data!");
			file.writeUTF("More data!");
			file.writeBoolean(true);
			file.writeUTF("Another bundle of data!");
			file.writeUTF("The last bundle of data!");
			// set the file pointer to the first string
			file.seek(4);
			// print the data of the data input stream
			System.out.println(file.readUTF());
			System.out.println(file.readUTF());
			System.out.println(file.readBoolean());
			System.out.println(file.readUTF());
			System.out.println(file.readUTF());
			// set the file pointer to the beginning of the file
			file.seek(0);
			// print the first integer
			System.out.println(file.readInt());
			// close the open streams
			file.close();
		}
		catch (IOException ioe) {
			System.out.println("An I/O error occured.");
		}
		System.out.println();
		// delete the file
		new File("file.dat").delete();
	}

}
