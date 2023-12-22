package xxl.tests.cursors.sources.io;

import xxl.core.cursors.sources.io.FileNameCursor;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class FileNameCursor.
 */
public class TestFileNameCursor {

	/**
	 * The main method contains some examples to demonstrate the usage and the
	 * functionality of this class.
	 *
	 * @param args array of <code>String</code> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main(String[] args) {
		
		if (args.length == 1) {
			System.out.println("Outputs the files of the directory " + args[0]);
			FileNameCursor f1 = new FileNameCursor(args[0], true);
	
			int count = 0;
			while (f1.hasNext()) {
				System.out.println(f1.next());
				count++;
			}
	
			System.out.println(count + " Files found");
			System.out.println();
		}
		else {
			System.out.println("Outputs the files of the current directory");
			FileNameCursor f1 = new FileNameCursor(".", false);
	
			int count = 0;
			while (f1.hasNext()) {
				System.out.println(f1.next());
				count++;
			}
	
			System.out.println(count + " Files found");
			System.out.println();
	
			System.out.println("Outputs the files of the current directory and subdirectories");
			FileNameCursor f2 = new FileNameCursor(".", true);
	
			count = 0;
			while (f2.hasNext()) {
				System.out.println(f2.next());
				count++;
			}
	
			System.out.println(count + " Files found");
			System.out.println();
		}
	}

}
