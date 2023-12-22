package xxl.tests.io;

import xxl.core.io.Block;
import xxl.core.io.ObjectToBlockCursor;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class ObjectToBlockCursor.
 */
public class TestObjectToBlockCursor {

	/**
	 * Use case which serializes Strings to Blocks and prints them.
	 * Remember: each String conversion contains the length of the String
	 * inside the first two bytes.
	 * @param args The command line options are ignored here.
	 */
	public static void main(String args[]) {
		String s = "Hello World";
		ObjectToBlockCursor cursor;
		Block b = null;
		
		System.out.println("Example no. 1");
		System.out.println("=============");
		
		cursor = new ObjectToBlockCursor(
			new xxl.core.cursors.sources.Repeater(s,10),
			new xxl.core.io.converters.StringConverter(),
			5,
			4,
			100,
			null
		);
		
		while (cursor.hasNext()) {
			b = (Block) cursor.next();
			if (b.size!=5)
				throw new RuntimeException("Length error in the first example");
			System.out.print(b.get(4)+"("+(char) b.get(4)+").");
		}
		cursor.close();
		
		System.out.println();

		System.out.println("Example no. 2");
		System.out.println("=============");
		
		cursor = new ObjectToBlockCursor(
			new xxl.core.cursors.sources.Repeater(s,1000),
			new xxl.core.io.converters.StringConverter(),
			105,
			4,
			100,
			null
		);
		// 1000*(10+2) bytes = 12000 bytes, 101 bytes effective per block
		// ==> 118 full blocks + 1 block with 4+82 = 86 used bytes
		// (see the 82 in the forth byte of the last block).
		
		int number=0;
		while (cursor.hasNext()) {
			b = (Block) cursor.next();
			if (b.size!=105)
				throw new RuntimeException("Length error in the second example");
			if (number<3) {
				System.out.println("Block no. "+ number + ":");
				System.out.println(b);
			}
			number++;
		}
		System.out.println();
		System.out.println("A lot of blocks are skipped now (more than 100)!");
		System.out.println();
		System.out.println("The last block (number "+(number-1)+")");
		System.out.println("Number of bytes used inside the last block: "+cursor.getNumberOfBytesInsideLastBlock());
		System.out.println(b);
		cursor.close();
	}

}
